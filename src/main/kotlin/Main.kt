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

fun validateArgs(args: Array<String>): Boolean {
    if (args.size < 2) {
        return false
    }
    return when (args[0]) {
        SAVE_CMD -> {
            args.size == 2
        }
        RESTORE_CMD -> {
            args.size == 3 || args.size == 4
        }
        else -> false
    }
}

fun saveMain(path: String) {
    val bodyBytes = FileSerializer.serialize(path)
    val objectKey = Path(path).toAbsolutePath().pathString.hashCode().toString()
    FakeS3Client().use { s3 ->
        runBlocking {
            s3.createBucket(CreateBucketRequest { bucket = DEFAULT_BUCKET })
            s3.putObject(PutObjectRequest {
                bucket = DEFAULT_BUCKET
                key = objectKey
                body = bodyBytes
            })
        }
    }
}

fun restoreMain(path: String, destination: String, filepath: String?) {
    val objectKey = Path(path).toAbsolutePath().pathString.hashCode().toString()
    runBlocking {
        val client = FakeS3Client()
        client.use { s3 ->
            val obj = s3.getObject(GetObjectRequest {
                bucket = DEFAULT_BUCKET
                key = objectKey
            }) { response ->
                response.body!!
            }
            FileSerializer.dump(obj, destination, filepath)
        }
    }
}

fun main(args: Array<String>) {
    if (!validateArgs(args)) {
        println(HELP)
        println("Received arguments: ${args.joinToString()}")
        return
    }
    when (args[0]) {
        SAVE_CMD -> return saveMain(args[1])
        RESTORE_CMD -> return restoreMain(args[1], args[2], if (args.size == 4) args[3] else null)
        // unreachable
        else -> assert(false)
    }
}