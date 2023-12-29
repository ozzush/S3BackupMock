package org.example.kotlin

import aws.sdk.kotlin.services.s3.model.CreateBucketRequest
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import kotlinx.coroutines.runBlocking
import org.example.kotlin.backuputility.FakeS3Client
import org.example.kotlin.backuputility.FileSerializer
import kotlin.io.path.Path
import kotlin.io.path.pathString

const val SAVE_CMD = "save"
const val RESTORE_CMD = "restore"
val HELP = """
Usage: ./gradlew run --args="$SAVE_CMD <path>"
       ./gradlew run --args="$RESTORE_CMD <path> <destination>"
""".trimIndent()
const val DEFAULT_BUCKET = "DEFAULT-BUCKET"

fun objectKey(path: String) = Path(path).toAbsolutePath().pathString.replace("/", "%")

fun saveMain(path: String) {
    val bodyBytes = FileSerializer.serialize(path)
    FakeS3Client().use { s3 ->
        runBlocking {
            s3.createBucket(CreateBucketRequest { bucket = DEFAULT_BUCKET })
            s3.putObject(PutObjectRequest {
                bucket = DEFAULT_BUCKET
                key = objectKey(path)
                body = bodyBytes
            })
        }
    }
}

fun restoreMain(path: String, destination: String, filepath: String?) {
    runBlocking {
        FakeS3Client().use { s3 ->
            val obj = s3.getObject(GetObjectRequest {
                bucket = DEFAULT_BUCKET
                key = objectKey(path)
            }) { response ->
                response.body!!
            }
            FileSerializer.dump(obj, destination, filepath)
        }
    }
}

fun main(args: Array<String>) {
    when {
        args.size == 2 && args[0] == SAVE_CMD -> return saveMain(args[1])
        args.size == 3 && args[0] == RESTORE_CMD -> return restoreMain(args[1], args[2], null)
        args.size == 4 && args[0] == RESTORE_CMD -> return restoreMain(args[1], args[2], args[3])
        // unreachable
        else -> {
            println(HELP)
            println("Received arguments: ${args.joinToString()}")
        }
    }
}