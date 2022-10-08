package pw.binom.s3

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.*
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.create
import pw.binom.net.toURL
import pw.binom.nextUuid
import pw.binom.s3.exceptions.S3ErrorException
import kotlin.random.Random
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class S3IntegrationTest {
    private val regin = "ru-central1"
    lateinit var client: S3Client
    lateinit var httpClient: HttpClient
    lateinit var bucketName: String

    @BeforeTest
    fun setup() {
        bucketName = Random.nextUuid().toString()
        httpClient = HttpClient.create()
        client = S3Client(
            url = "http://127.0.0.1:7122".toURL(),
            accessKey = "S3RVER",
            secretAccessKey = "S3RVER",
            client = httpClient,
        )
    }

    fun shutdown() {
        httpClient.close()
    }

    fun call(func: suspend (HttpClient) -> Unit) = runTest {
        HttpClient.create().use { client ->
            func(client)
        }
    }

    @Test
    fun bucketAlreadyExist() = runTest {
        client.createBucket(
            name = bucketName,
            regin = regin,
        )

        try {
            client.createBucket(
                name = bucketName,
                regin = regin,
            )
        } catch (e: S3ErrorException) {
            assertEquals(ErrorTexts.BUCKET_ALREADY_EXISTS, e.code)
            assertEquals(ErrorTexts.BUCKET_ALREADY_EXISTS_MESSAGE, e.description)
        }
    }

    @Test
    fun bucketList() = runTest {
        suspend fun list() = client.listBuckets(regin = regin)
        list().list.all { it.name != bucketName }
        client.createBucket(
            name = bucketName,
            regin = regin
        )
        assertTrue(list().list.any { it.name == bucketName })
    }

    @Test
    fun putGetObjectTest() = runTest {
        client.createBucket(
            name = bucketName,
            regin = regin
        )
        val key = Random.nextUuid().toString()
        val expectedContent = (0..9).map { Random.nextUuid().toString() }.joinToString().encodeToByteArray()
        client.putObject(
            bucket = bucketName,
            key = key,
            regin = regin,
        ) { output ->
            ByteBuffer.alloc(DEFAULT_BUFFER_SIZE).use { buffer ->
                output.writeByteArray(expectedContent, buffer)
            }
        }
        val actualContent = client.getObject(
            regin = regin,
            bucket = bucketName,
            key = key,
        ) { input ->
            assertNotNull(input)
            input.input.readBytes()
        }
        assertContentEquals(expectedContent, actualContent)
    }

    @Test
    fun copyTest() = runTest {
        client.createBucket(
            name = bucketName,
            regin = regin
        )
        val key = Random.nextUuid().toString()
        val newKey = Random.nextUuid().toString()
        val expectedContent = (0..9).map { Random.nextUuid().toString() }.joinToString().encodeToByteArray()

        client.putObject(
            bucket = bucketName,
            key = key,
            regin = regin,
        ) { output ->
            ByteBuffer.alloc(DEFAULT_BUFFER_SIZE).use { buffer ->
                output.writeByteArray(expectedContent, buffer)
            }
        }
        client.copyObject(
            regin = regin,
            sourceBucket = bucketName,
            sourceKey = key,
            destinationBucket = bucketName,
            destinationKey = newKey
        )
        val actualContent = client.getObject(
            regin = regin,
            bucket = bucketName,
            key = newKey,
        ) { input ->
            assertNotNull(input)
            input.input.readBytes()
        }
        assertContentEquals(expectedContent, actualContent)
    }
}
