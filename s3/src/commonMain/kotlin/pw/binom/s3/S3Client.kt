package pw.binom.s3

import kotlinx.coroutines.flow.Flow
import pw.binom.http.client.HttpClientRunnable
import pw.binom.io.AsyncOutput
import pw.binom.io.http.range.Range
import pw.binom.s3.dto.CompleteMultipartUploadResult
import pw.binom.s3.dto.Content
import pw.binom.s3.dto.ContentHead
import pw.binom.s3.dto.ListBucketResultV2
import pw.binom.s3.dto.Part
import pw.binom.url.URL

interface S3Client {
  companion object {
    fun create(
      url: URL,
      accessKey: String,
      secretAccessKey: String,
      client: HttpClientRunnable,
    ): S3Client = S3ClientImpl(
      url = url,
      accessKey = accessKey,
      secretAccessKey = secretAccessKey,
      client = client,
    )
  }

  suspend fun createBucket(
    name: String,
    regin: String,
    locationConstraint: String? = null,
  )

  suspend fun deleteObject(
    bucket: String,
    key: String,
    regin: String,
  ): Boolean

  suspend fun putObjectContent(
    bucket: String,
    key: String,
    regin: String,
    contentType: String? = null,
    packageSize: Int = ObjectAsyncOutput.MIN_PACKAGE_SIZE,
    payload: suspend (ObjectAsyncOutput) -> Unit,
  )

  suspend fun putObject(
    bucket: String,
    key: String,
    regin: String,
    payloadContentLength: Long? = null,
    partNumber: Int? = null,
    uploadId: String? = null,
    payloadSha256: ByteArray? = null,
    payload: suspend (AsyncOutput) -> Unit,
  )

  suspend fun copyObject(
    regin: String,
    sourceBucket: String,
    sourceKey: String,
    destinationBucket: String,
    destinationKey: String,
  )

  suspend fun listObject2(
    regin: String,
    bucket: String,
    continuationToken: String? = null,
    fetchOwner: Boolean? = null,
    maxKeys: Int = 1000,
    startAfter: String? = null,
    prefix: String? = null,
    xAmzExpectedBucketOwner: String? = null,
    xAmzRequestPayer: String? = null,
    delimiter: String? = null,
  ): ListBucketResultV2

  fun listObject2Flow(
    regin: String,
    bucket: String,
    fetchOwner: Boolean? = null,
    partSize: Int = 1000,
    startAfter: String? = null,
    prefix: String? = null,
    xAmzExpectedBucketOwner: String? = null,
    xAmzRequestPayer: String? = null,
  ): Flow<Content>

  suspend fun headObject(
    regin: String,
    bucket: String,
    key: String,
    partNumber: Int? = null,
    versionId: String? = null,
  ): ContentHead?

  suspend fun getObject(
    regin: String,
    bucket: String,
    key: String,
    partNumber: Int? = null,
    versionId: String? = null,
    range: List<Range> = emptyList(),
  ): S3ObjectStream?

  suspend fun listBuckets(regin: String): Buckets

  suspend fun createMultipartUpload(
    regin: String,
    bucket: String,
    key: String,
    contentType: String? = null,
  ): String

  suspend fun completeMultipartUpload(
    regin: String,
    bucket: String,
    key: String,
    uploadId: String,
    parts: List<Part>,
  ): CompleteMultipartUploadResult
}
