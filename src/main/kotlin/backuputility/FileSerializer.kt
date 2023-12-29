package org.example.kotlin.backuputility

import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.content.toInputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeToSequence
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*

@Serializable
data class FileData(val path: String, val bytes: ByteArray?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileData

        if (path != other.path) return false
        if (bytes != null) {
            if (other.bytes == null) return false
            if (!bytes.contentEquals(other.bytes)) return false
        } else if (other.bytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + (bytes?.contentHashCode() ?: 0)
        return result
    }
}

class FileSerializer {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        @JvmStatic
        fun serialize(path: String): ByteStream {
            val prefixPath = Path(path).parent
            val file = Path(path).toFile()
            val tempFile = File.createTempFile("backup-utility-tmp", null)
            tempFile.deleteOnExit()
            val output = tempFile.outputStream()
            output.write("[".toByteArray())
            var isFirstIteration = true
            file.walkTopDown().forEach { oneFile ->
                val bytes = oneFile.takeIf { it.isFile }?.readBytes()
                val relativePath = Path(oneFile.path).relativeTo(prefixPath)
                val fileData = FileData(relativePath.pathString, bytes)
                if (!isFirstIteration) {
                    output.write(",".toByteArray())
                }
                Json.encodeToStream(fileData, output)
                isFirstIteration = false
            }
            output.write("]".toByteArray())
            return tempFile.asByteStream()
        }

        @OptIn(ExperimentalSerializationApi::class)
        @JvmStatic
        fun dump(byteStream: ByteStream, destination: String, filepath: String?) {
            val fileStream = Json.decodeToSequence<FileData>(byteStream.toInputStream())
            if (filepath == null) {
                dumpFiles(fileStream, destination)
            } else {
                dumpSingleFile(fileStream, destination, filepath)
            }
        }

        @JvmStatic
        private fun dumpFiles(files: Sequence<FileData>, destination: String) {
            for (file in files) {
                val realPath = Paths.get(destination, file.path)
                file.createFile(realPath)
            }
        }

        @JvmStatic
        private fun dumpSingleFile(
            files: Sequence<FileData>,
            destination: String,
            filepath: String
        ) {
            for (file in files) {
                if (filepath != file.path) {
                    continue
                }
                if (file.bytes == null) {
                    break
                }
                val realPath = Paths.get(destination, Path(file.path).fileName.pathString)
                file.createFile(realPath)
                return
            }
            throw Exception("File $filepath not found in the object")
        }


        private fun FileData.createFile(path: Path) {
            if (bytes == null) {
                path.createDirectory()
            } else {
                path.writeBytes(bytes)
            }
        }
    }
}