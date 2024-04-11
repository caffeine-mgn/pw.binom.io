package pw.binom.s3

import kotlinx.coroutines.test.runTest
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.concurrency.sleep
import pw.binom.crypto.MD5MessageDigest
import pw.binom.crypto.Sha256MessageDigest
import pw.binom.io.*
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.create
import pw.binom.s3.dto.Part
import pw.binom.s3.exceptions.S3ErrorException
import pw.binom.s3.v4.toHex
import pw.binom.url.toURL
import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.test.*

class S3IntegrationTest {
  private val regin = "us-east-1"
  lateinit var client: S3Client
  lateinit var httpClient: HttpClient
  lateinit var bucketName: String

  suspend fun S3Client.createBucket() = createBucket(name = bucketName, regin = regin, locationConstraint = regin)

  private var started = false

  @BeforeTest
  fun setup() {
    if (!started) {
      sleep(5_000)
      started = true
    }
    bucketName = Random.nextUuid().toShortString()
    httpClient = HttpClient.create()
    client =
      S3Client(
        url = "http://127.0.0.1:7122/".toURL(),
        accessKey = "accessKey1",
        secretAccessKey = "verySecretKey1",
        client = httpClient,
      )
  }

  fun shutdown() {
    httpClient.close()
  }

  fun call(func: suspend (HttpClient) -> Unit) =
    runTest {
      HttpClient.create().use { client ->
        func(client)
      }
    }

  @Test
  fun bucketAlreadyExist() =
    runTest {
      client.createBucket(
        name = bucketName,
        regin = regin,
        locationConstraint = regin,
      )

      try {
        client.createBucket(
          name = bucketName,
          regin = regin,
          locationConstraint = regin,
        )
      } catch (e: S3ErrorException) {
        assertEquals(ErrorTexts.BUCKET_ALREADY_EXISTS, e.code)
        assertEquals(ErrorTexts.BUCKET_ALREADY_EXISTS_MESSAGE, e.description)
      }
    }

  @Test
  fun bucketList() =
    runTest {
      suspend fun list() = client.listBuckets(regin = regin)
      list().list.all { it.name != bucketName }
      client.createBucket(
        name = bucketName,
        regin = regin,
        locationConstraint = regin,
      )
      assertTrue(list().list.any { it.name == bucketName })
    }

  @Test
  fun putGetObjectTest() =
    runTest {
      client.createBucket(
        name = bucketName,
        regin = regin,
        locationConstraint = regin,
      )
      val key = Random.nextUuid().toString()
      val expectedContent = (0..9).map { Random.nextUuid().toString() }.joinToString().encodeToByteArray()
      client.putObject(
        bucket = bucketName,
        key = key,
        regin = regin,
        payloadContentLength = expectedContent.size.toLong(),
      ) { output ->
        ByteBuffer(DEFAULT_BUFFER_SIZE).use { buffer ->
          output.writeByteArray(expectedContent, buffer)
        }
      }
      val actualContent =
        client.getObject(
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
  fun copyTest() =
    runTest {
      client.createBucket(
        name = bucketName,
        regin = regin,
        locationConstraint = regin,
      )
      val key = Random.nextUuid().toString()
      val newKey = Random.nextUuid().toString()
      val expectedContent = (0..9).map { Random.nextUuid().toString() }.joinToString().encodeToByteArray()

      client.putObject(
        bucket = bucketName,
        key = key,
        regin = regin,
        payloadContentLength = expectedContent.size.toLong(),
      ) { output ->
        ByteBuffer(DEFAULT_BUFFER_SIZE).use { buffer ->
          output.writeByteArray(expectedContent, buffer)
        }
      }
      client.copyObject(
        regin = regin,
        sourceBucket = bucketName,
        sourceKey = key,
        destinationBucket = bucketName,
        destinationKey = newKey,
      )
      val actualContent =
        client.getObject(
          regin = regin,
          bucket = bucketName,
          key = newKey,
        ) { input ->
          assertNotNull(input)
          input.input.readBytes()
        }
      assertContentEquals(expectedContent, actualContent)
    }

  @Test
  fun multipartUpload() =
    runTest(dispatchTimeoutMs = 20_000) {
      val key = Random.nextUuid().toShortString()

      client.createBucket()
      val part1 = ByteArray(1024 * 1024 * 5)
      val part2 = ByteArray(1024 * 1024 * 5)
      val part3 = ByteArray(1024 * 1024 * 4)
      val full = part1 + part2 + part3
      val uploadId =
        client.createMultipartUpload(
          regin = regin,
          bucket = bucketName,
          key = key,
        )

      suspend fun putPart(
        number: Int,
        data: ByteArray,
      ): Part {
        val d = MD5MessageDigest()
        d.update(data)
        val md5 = d.finish()
        val b = Sha256MessageDigest()
        b.update(data)
        val sha256 = b.finish()
        client.putObject(
          bucket = bucketName,
          key = key,
          regin = regin,
          payloadContentLength = data.size.toLong(),
          partNumber = number,
          uploadId = uploadId,
//                payloadSha256 = sha256,
          payload = { data.wrap().use { buffer -> it.write(buffer) } },
        )
        return Part(
          checksumSHA256 = sha256.toHex(),
          eTag = md5.toHex(),
          partNumber = number,
        )
      }

      val p1 = putPart(number = 1, data = part1)
      val p2 = putPart(number = 2, data = part2)
      val p3 = putPart(number = 3, data = part3)
      client.completeMultipartUpload(
        regin = regin,
        bucket = bucketName,
        key = key,
        uploadId = uploadId,
        parts =
          listOf(
            p1,
            p2,
            p3,
          ),
      )
      val actualData =
        client.getObject(
          regin = regin,
          bucket = bucketName,
          key = key,
        ) { it?.input?.readBytes() }

      assertContentEquals(full, actualData)
    }

  @Test
  fun putObjectContentTest() =
    runTest {
      val key = Random.nextUuid().toShortString()
      client.createBucket()

      val full = ByteArray(1024 * 1024 * 14)
      client.putObjectContent(
        bucket = bucketName,
        key = key,
        regin = regin,
      ) { output ->
        full.wrap().use { data ->
          output.writeFully(data)
        }
      }

      val actualData =
        client.getObject(
          regin = regin,
          bucket = bucketName,
          key = key,
        ) { it?.input?.readBytes() }

      assertContentEquals(full, actualData)
    }

  @Test
  fun listOfObjectTest() =
    runTest {
      client.createBucket()
      val full = ByteArray(1024)
      val names = (0 until 10).map { Random.nextUuid().toString() }
      names.forEach { name ->
        client.putObjectContent(
          bucket = bucketName,
          key = name,
          regin = regin,
        ) { output ->
          full.wrap().use { data ->
            output.writeFully(data)
          }
        }
      }
      val list1 =
        client.listObject2(
          regin = regin,
          bucket = bucketName,
          continuationToken = null,
          maxKeys = 5,
        )

      assertNotNull(list1.nextContinuationToken, "Continuation Token is null")
      val list2 =
        client.listObject2(
          regin = regin,
          bucket = bucketName,
          continuationToken = list1.nextContinuationToken,
          maxKeys = 5,
        )

      val totalList = list1.contents.map { it.key } + list2.contents.map { it.key }
      assertContentEquals(names.sorted(), totalList.sorted())
    }
}
