package pw.binom.s3

import kotlinx.coroutines.flow.flow
import pw.binom.io.AsyncOutput
import pw.binom.io.http.range.Range
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.useAsync
import pw.binom.s3.dto.ContentHead
import pw.binom.s3.dto.Part
import pw.binom.url.URL

class S3Client(
  val url: URL,
  val accessKey: String,
  val secretAccessKey: String,
  val client: HttpClient,
) {
  suspend fun createBucket(
    name: String,
    regin: String,
    locationConstraint: String? = null,
  ) {
    S3ClientApi.createBucket(
      client = client,
      locationConstraint = locationConstraint,
      regin = regin,
      name = name,
      url = url,
      accessKey = accessKey,
      secretAccessKey = secretAccessKey,
    )
  }

  suspend fun deleteObject(
    bucket: String,
    key: String,
    regin: String,
  ) = S3ClientApi.deleteObject(
    client = client,
    regin = regin,
    bucket = bucket,
    key = key,
    url = url,
    accessKey = accessKey,
    secretAccessKey = secretAccessKey,
  )

  suspend fun putObjectContent(
    bucket: String,
    key: String,
    regin: String,
    contentType: String? = null,
    packageSize: Int = ObjectAsyncOutput.MIN_PACKAGE_SIZE,
    payload: suspend (ObjectAsyncOutput) -> Unit,
  ) {
    ObjectAsyncOutput(
      bucket = bucket,
      key = key,
      regin = regin,
      contentType = contentType,
      client = this,
      bufferSize = packageSize,
    ).useAsync { output ->
      payload(output)
    }
  }

  suspend fun putObject(
    bucket: String,
    key: String,
    regin: String,
    payloadContentLength: Long? = null,
    partNumber: Int? = null,
    uploadId: String? = null,
    payloadSha256: ByteArray? = null,
    payload: suspend (AsyncOutput) -> Unit,
  ) {
    S3ClientApi.putObject(
      client = client,
      regin = regin,
      bucket = bucket,
      key = key,
      url = url,
      partNumber = partNumber,
      payloadContentLength = payloadContentLength,
      accessKey = accessKey,
      secretAccessKey = secretAccessKey,
      payload = payload,
      uploadId = uploadId,
      payloadSha256 = payloadSha256,
    )
  }

  suspend fun copyObject(
    regin: String,
    sourceBucket: String,
    sourceKey: String,
    destinationBucket: String,
    destinationKey: String,
  ) {
    S3ClientApi.copyObject(
      client = client,
      regin = regin,
      sourceBucket = sourceBucket,
      sourceKey = sourceKey,
      destinationBucket = destinationBucket,
      destinationKey = destinationKey,
      url = url,
      accessKey = accessKey,
      secretAccessKey = secretAccessKey,
    )
  }

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
  ) = S3ClientApi.listObject2(
    client = client,
    url = url,
    secretAccessKey = secretAccessKey,
    accessKey = accessKey,
    regin = regin,
    bucket = bucket,
    continuationToken = continuationToken,
    fetchOwner = fetchOwner,
    maxKeys = maxKeys,
    startAfter = startAfter,
    prefix = prefix,
    xAmzExpectedBucketOwner = xAmzExpectedBucketOwner,
    xAmzRequestPayer = xAmzRequestPayer,
    delimiter = delimiter,
  )

  fun listObject2Flow(
    regin: String,
    bucket: String,
    fetchOwner: Boolean? = null,
    partSize: Int = 1000,
    startAfter: String? = null,
    prefix: String? = null,
    xAmzExpectedBucketOwner: String? = null,
    xAmzRequestPayer: String? = null,
  ) = flow {
    var token: String? = null
    while (true) {
      val result =
        listObject2(
          continuationToken = token,
          fetchOwner = fetchOwner,
          maxKeys = partSize,
          startAfter = startAfter,
          prefix = prefix,
          xAmzExpectedBucketOwner = xAmzExpectedBucketOwner,
          xAmzRequestPayer = xAmzRequestPayer,
          regin = regin,
          bucket = bucket,
        )
      result.contents.forEach {
        emit(it)
      }
      token = result.nextContinuationToken ?: break
    }
  }

  suspend fun headObject(
    regin: String,
    bucket: String,
    key: String,
    partNumber: Int? = null,
    versionId: String? = null,
  ): ContentHead? =
    S3ClientApi.headObject(
      client = client,
      url = url,
      partNumber = partNumber,
      versionId = versionId,
      accessKey = accessKey,
      secretAccessKey = secretAccessKey,
      key = key,
      bucket = bucket,
      regin = regin,
    )

  suspend fun getObject(
    regin: String,
    bucket: String,
    key: String,
    partNumber: Int? = null,
    versionId: String? = null,
    range: List<Range> = emptyList(),
  ) = S3ClientApi.getObject(
    client = client,
    url = url,
    regin = regin,
    bucket = bucket,
    key = key,
    partNumber = partNumber,
    versionId = versionId,
    accessKey = accessKey,
    range = range,
    secretAccessKey = secretAccessKey,
  )


  suspend fun listBuckets(regin: String) =
    S3ClientApi.listBuckets(
      client = client,
      regin = regin,
      url = url,
      accessKey = accessKey,
      secretAccessKey = secretAccessKey,
    )

  suspend fun createMultipartUpload(
    regin: String,
    bucket: String,
    key: String,
    contentType: String? = null,
  ) = S3ClientApi.createMultipartUpload(
    client = client,
    regin = regin,
    url = url,
    bucket = bucket,
    key = key,
    contentType = contentType,
    accessKey = accessKey,
    secretAccessKey = secretAccessKey,
  )

  suspend fun completeMultipartUpload(
    regin: String,
    bucket: String,
    key: String,
    uploadId: String,
    parts: List<Part>,
  ) = S3ClientApi.completeMultipartUpload(
    client = client,
    regin = regin,
    url = url,
    bucket = bucket,
    key = key,
    uploadId = uploadId,
    accessKey = accessKey,
    secretAccessKey = secretAccessKey,
    parts = parts,
  )
}
