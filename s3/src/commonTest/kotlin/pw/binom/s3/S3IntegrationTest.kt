package pw.binom.s3

import kotlinx.coroutines.test.runTest
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.create
import pw.binom.io.use
import pw.binom.net.toURL
import pw.binom.nextUuid
import pw.binom.s3.exceptions.S3ErrorException
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class S3IntegrationTest {
    private val url = "http://127.0.0.1:7122".toURL()
    private val regin = "ru-central1"
    private val accessKey = "S3RVER"
    private val secretAccessKey = "S3RVER"

    @Test
    fun bucketAlreadyExist() = runTest {
        HttpClient.create().use { client ->
            val bucketName = Random.nextUuid().toString()
            S3ClientApi.createBucket(
                client = client,
                locationConstraint = null,
                regin = regin,
                name = bucketName,
                url = url,
                accessKey = accessKey,
                secretAccessKey = secretAccessKey,
            )

            try {
                S3ClientApi.createBucket(
                    client = client,
                    locationConstraint = null,
                    regin = regin,
                    name = bucketName,
                    url = url,
                    accessKey = accessKey,
                    secretAccessKey = secretAccessKey,
                )
            } catch (e: S3ErrorException) {
                assertEquals(ErrorTexts.BUCKET_ALREADY_EXISTS, e.code)
                assertEquals(ErrorTexts.BUCKET_ALREADY_EXISTS_MESSAGE, e.description)
            }
        }
    }

    @Test
    fun bucketList() = runTest {
        HttpClient.create().use { client ->
            val bucketName = Random.nextUuid().toString()
            suspend fun list() = S3ClientApi.listBuckets(
                client = client,
                regin = regin,
                url = url,
                accessKey = accessKey,
                secretAccessKey = secretAccessKey,
            )
            list()
            S3ClientApi.createBucket(
                client = client,
                locationConstraint = null,
                regin = regin,
                name = bucketName,
                url = url,
                accessKey = accessKey,
                secretAccessKey = secretAccessKey,
            )
            list()
        }
    }
}
