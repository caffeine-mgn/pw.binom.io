package pw.binom.s3

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.concurrency.sleep
import pw.binom.crypto.MD5MessageDigest
import pw.binom.crypto.Sha256MessageDigest
import pw.binom.http.client.HttpClientRunnable
import pw.binom.http.client.factory.NativeNetChannelFactory
import pw.binom.io.*
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.create
import pw.binom.network.MultiFixedSizeThreadNetworkDispatcher
import pw.binom.network.NetworkManager
import pw.binom.s3.dto.Part
import pw.binom.s3.exceptions.S3ErrorException
import pw.binom.s3.v4.toHex
import pw.binom.url.toURL
import pw.binom.uuid.UUID
import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class S3IntegrationTest : IntegrationTest() {
  private val regin = "us-east-1"
  lateinit var bucketName: String

  suspend fun S3Client.createBucket() = createBucket(name = bucketName, regin = regin, locationConstraint = regin)

  private var started = false

  @BeforeTest
  fun setup2() {
    if (!started) {
      sleep(5_000)
      started = true
    }
    val nm = MultiFixedSizeThreadNetworkDispatcher(4)
    bucketName = Random.nextUuid().toShortString()

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
      s3.createBucket(
        name = bucketName,
        regin = regin,
        locationConstraint = regin,
      )

      try {
        s3.createBucket(
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
      suspend fun list() = s3.listBuckets(regin = regin)
      list().list.all { it.name != bucketName }
      s3.createBucket(
        name = bucketName,
        regin = regin,
        locationConstraint = regin,
      )
      assertTrue(list().list.any { it.name == bucketName })
    }

  @Test
  fun putGetObjectTest() =
    runTest {
      s3.createBucket(
        name = bucketName,
        regin = regin,
        locationConstraint = regin,
      )
      val key = Random.nextUuid().toString()
      val expectedContent = (0..9).map { Random.nextUuid().toString() }.joinToString().encodeToByteArray()
      s3.putObject(
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
        s3.getObject(
          regin = regin,
          bucket = bucketName,
          key = key,
        )!!.readBytes()
      assertContentEquals(expectedContent, actualContent)
    }

  @Test
  fun copyTest() =
    runTest {
      s3.createBucket(
        name = bucketName,
        regin = regin,
        locationConstraint = regin,
      )
      val key = Random.nextUuid().toString()
      val newKey = Random.nextUuid().toString()
      val expectedContent = (0..9).map { Random.nextUuid().toString() }.joinToString().encodeToByteArray()

      s3.putObject(
        bucket = bucketName,
        key = key,
        regin = regin,
        payloadContentLength = expectedContent.size.toLong(),
      ) { output ->
        ByteBuffer(DEFAULT_BUFFER_SIZE).use { buffer ->
          output.writeByteArray(expectedContent, buffer)
        }
      }
      s3.copyObject(
        regin = regin,
        sourceBucket = bucketName,
        sourceKey = key,
        destinationBucket = bucketName,
        destinationKey = newKey,
      )
      val actualContent =
        s3.getObject(
          regin = regin,
          bucket = bucketName,
          key = newKey,
        )!!.readBytes()
      assertContentEquals(expectedContent, actualContent)
    }

  @Test
  fun multipartUpload() =
    runTest(timeout = 20.seconds) {
      val key = Random.nextUuid().toShortString()

      s3.createBucket()
      val part1 = ByteArray(1024 * 1024 * 5)
      val part2 = ByteArray(1024 * 1024 * 5)
      val part3 = ByteArray(1024 * 1024 * 4)
      val full = part1 + part2 + part3
      val uploadId =
        s3.createMultipartUpload(
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
        s3.putObject(
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
      s3.completeMultipartUpload(
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
        s3.getObject(
          regin = regin,
          bucket = bucketName,
          key = key,
        )!!.readBytes()

      assertContentEquals(full, actualData)
    }

  @Test
  fun putObjectContentTest() =
    runTest {
      val key = Random.nextUuid().toShortString()
      s3.createBucket()

      val full = ByteArray(ObjectAsyncOutput.MIN_PACKAGE_SIZE - 1)
      s3.putObjectContent(
        bucket = bucketName,
        key = key,
        regin = regin,
      ) { output ->
        full.wrap().use { data ->
          output.writeFully(data)
        }
      }
      val head = s3.headObject(
        regin = regin,
        bucket = bucketName,
        key = key,
      )!!
      println("head.length->${head.length}")
      val obj = s3.getObject(
        regin = regin,
        bucket = bucketName,
        key = key,
      )!!
      println("obj.data.length=${obj.data.length}")
      val actualData = obj.readBytes()
      assertEquals(full.size, actualData.size, "Invalid size")
//      assertContentEquals(full, actualData)
    }

  @Test
  fun listOfObjectTest() =
    runTest {
      s3.createBucket()
      val full = ByteArray(1024)
      val names = (0 until 10).map { Random.nextUuid().toString() }
      names.forEach { name ->
        s3.putObjectContent(
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
        s3.listObject2(
          regin = regin,
          bucket = bucketName,
          continuationToken = null,
          maxKeys = 5,
        )

      assertNotNull(list1.nextContinuationToken, "Continuation Token is null")
      val list2 =
        s3.listObject2(
          regin = regin,
          bucket = bucketName,
          continuationToken = list1.nextContinuationToken,
          maxKeys = 5,
        )

      val totalList = list1.contents.map { it.key } + list2.contents.map { it.key }
      assertContentEquals(names.sorted(), totalList.sorted())
    }
}
