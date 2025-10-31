package pw.binom.s3

import kotlinx.coroutines.flow.flow
import kotlinx.serialization.modules.SerializersModule
import pw.binom.crypto.Sha256MessageDigest
import pw.binom.date.DateTime
import pw.binom.date.parseIso8601DateTime
import pw.binom.date.parseRfc822Date
import pw.binom.http.client.Http11ClientExchange
import pw.binom.http.client.HttpClientRunnable
import pw.binom.io.AsyncOutput
import pw.binom.io.bufferedWriter
import pw.binom.io.http.range.Range
import pw.binom.io.useAsync
import pw.binom.s3.dto.*
import pw.binom.s3.exceptions.S3ErrorException
import pw.binom.s3.exceptions.S3Exception
import pw.binom.s3.serialization.DateSerialization
import pw.binom.s3.v4.s3Call
import pw.binom.url.Query
import pw.binom.url.URL
import pw.binom.xml.XmlParser
import pw.binom.xml.dom.XElement
import pw.binom.xml.dom.xmlTree
import pw.binom.xml.serialization.Xml
import pw.binom.xml.singleWithName
import pw.binom.xml.tags
import pw.binom.xml.text
import pw.binom.xml.withName

private val dd =
  SerializersModule {
    contextual(DateTime::class, DateSerialization)
  }
private val xml = Xml(serializersModule = dd)

object S3ClientApi {
  private suspend fun Http11ClientExchange.throwErrorText(code: Int): Nothing {

    val resp = readAllText()
    val element by lazy { resp.xmlTree(true) }
    if (resp.isEmpty()) {
      throw S3Exception("Unknown response $code")
    } else {
      val error = xml.decodeFromXmlElement(Error.serializer(), element)
      throw S3ErrorException(
        code = error.key,
        description = error.message,
      )
    }
  }

  suspend fun createBucket(
    client: HttpClientRunnable,
    locationConstraint: String?,
    regin: String,
    name: String,
    url: URL,
    accessKey: String,
    secretAccessKey: String,
  ) {
    val payload =
      xml.encodeToString(
        serializer = CreateBucketConfiguration.serializer(),
        value = CreateBucketConfiguration(locationConstraint = locationConstraint),
        withHeader = true,
      )
    val fullPath = url.copy(path = url.path.append(name))
    s3Call(
      client = client,
      method = "PUT",
      url = fullPath,
      regin = regin,
      accessKey = accessKey,
      secretAccessKey = secretAccessKey,
    ) { output ->
      output.bufferedWriter(closeParent = false).useAsync {
        it.append(payload)
      }
    }.useAsync {
      when (val code = it.getResponseCode()) {
        200 -> null
        else -> it.throwErrorText(code)
      }
    }
  }

  suspend fun deleteObject(
    client: HttpClientRunnable,
    regin: String,
    bucket: String,
    key: String,
    url: URL,
    accessKey: String,
    secretAccessKey: String,
  ) = s3Call(
    client = client,
    method = "DELETE",
    url = url.copy(path = url.path.append(bucket).append(key)),
    regin = regin,
    accessKey = accessKey,
    secretAccessKey = secretAccessKey,
    payloadContentLength = 0,
  ).useAsync {
    when (val code = it.getResponseCode()) {
      200, 204 -> true
      404 -> false
      else -> it.throwErrorText(code)
    }
  }

  suspend fun putObject(
    client: HttpClientRunnable,
    regin: String,
    bucket: String,
    key: String,
    url: URL,
    partNumber: Int? = null,
    uploadId: String? = null,
    accessKey: String,
    secretAccessKey: String,
    payloadContentLength: Long?,
    payloadSha256: ByteArray? = null,
    payload: suspend (AsyncOutput) -> Unit,
  ) {
    val query =
      Query.build {
        if (partNumber != null) {
          add("partNumber", partNumber.toString())
        }
        if (uploadId != null) {
          add("uploadId", uploadId)
        }
      }
    s3Call(
      client = client,
      method = "PUT",
      url = url.copy(path = url.path.append(bucket).append(key), query = query),
      regin = regin,
      accessKey = accessKey,
      secretAccessKey = secretAccessKey,
      payloadContentLength = payloadContentLength,
      payloadSha256 = payloadSha256,
    ) { output ->
      payload(output)
    }.useAsync {
      when (val code = it.getResponseCode()) {
        200 -> null
        else -> it.throwErrorText(code)
      }
    }
  }

  suspend fun copyObject(
    client: HttpClientRunnable,
    regin: String,
    sourceBucket: String,
    sourceKey: String,
    destinationBucket: String,
    destinationKey: String,
    url: URL,
    accessKey: String,
    secretAccessKey: String,
  ) {
    s3Call(
      client = client,
      method = "PUT",
      url = url.copy(path = url.path.append(destinationBucket).append(destinationKey)),
      regin = regin,
      accessKey = accessKey,
      secretAccessKey = secretAccessKey,
      xAmzCopySource = "$sourceBucket/$sourceKey",
    ).useAsync {
      when (val code = it.getResponseCode()) {
        200 -> null
        else -> it.throwErrorText(code)
      }
    }
  }

  suspend fun listObject2(
    client: HttpClientRunnable,
    url: URL,
    continuationToken: String? = null,
    delimiter: String? = null,
    fetchOwner: Boolean? = null,
    maxKeys: Int,
    prefix: String? = null,
    startAfter: String? = null,
    xAmzExpectedBucketOwner: String? = null,
    xAmzRequestPayer: String? = null,
    regin: String,
    bucket: String,
    accessKey: String,
    secretAccessKey: String,
  ): ListBucketResultV2 {
    val query =
      Query.build {
        if (continuationToken != null) {
          add("continuation-token", continuationToken)
        }
        if (delimiter != null) {
          add("delimiter", delimiter)
        }
        if (fetchOwner != null) {
          add("fetch-owner", fetchOwner.toString())
        }
        add("list-type", "2")
        add("max-keys", maxKeys.toString())
        if (prefix != null) {
          add("prefix", prefix)
        }
        if (startAfter != null) {
          add("start-after", startAfter)
        }
        if (xAmzExpectedBucketOwner != null) {
          add("x-amz-expected-bucket-owner", xAmzExpectedBucketOwner)
        }
        if (xAmzRequestPayer != null) {
          add("x-amz-request-payer", xAmzRequestPayer)
        }
      }
    val result =
      s3Call(
        client = client,
        method = "GET",
        url = url.copy(query = query, path = url.path.append(bucket)),
        regin = regin,
        accessKey = accessKey,
        secretAccessKey = secretAccessKey,
      ).useAsync {
        it.readAllText()
      }
    val xx = XmlParser.parse(result)
    val errorTag = xx.tags().withName("Error").singleOrNull()
    if (errorTag != null) {
      TODO("ERROR")
    }
    val bucketResult = xx.tags().singleWithName("ListBucketResult")
    return ListBucketResultV2(
      name = bucketResult.tags().singleWithName("Name").text(),
      prefix=bucketResult.tags().singleWithName("Prefix").text(),
      nextContinuationToken = bucketResult.tags().withName("NextContinuationToken").singleOrNull()?.text(),
      keyCount = bucketResult.tags().singleWithName("KeyCount").text().toInt(),
      maxKeys = bucketResult.tags().singleWithName("MaxKeys").text().toInt(),
      delimiter = bucketResult.tags().withName("Delimiter").singleOrNull()?.text()?:"",
      isTruncated = bucketResult.tags().singleWithName("IsTruncated").text().toBoolean(),
      contents = bucketResult.tags().withName("Contents").map {el->
        Content(
          key = el.tags().singleWithName("Key").text(),
          lastModified = el.tags().singleWithName("LastModified").text(),
          eTag = el.tags().singleWithName("ETag").text(),
          size = el.tags().singleWithName("Size").text().toULong(),
          owner = el.tags().withName("Owner").singleOrNull()?.let {el->
            Owner(
              id = el.tags().singleWithName("ID").text(),
              displayName = el.tags().singleWithName("DisplayName").text()
            )
          },
          StorageClass = el.tags().singleWithName("StorageClass").text(),
        )
      }.toList()
    )
//    val element = result.xmlTree(true)
//    if (element.tag == "Error") {
//      val error = xml.decodeFromXmlElement(Error.serializer(), element)
//      throw S3ErrorException(
//        code = error.key,
//        description = error.message,
//      )
//    }
//
//
//    return xml.decodeFromXmlElement(ListBucketResultV2.serializer(), element)
  }

  fun listObjectFlow(
    client: HttpClientRunnable,
    url: URL,
    blockSize: Int = 500,
    fetchOwner: Boolean? = null,
    prefix: String? = null,
    startAfter: String? = null,
    xAmzExpectedBucketOwner: String? = null,
    xAmzRequestPayer: String? = null,
    regin: String,
    bucket: String,
    accessKey: String,
    secretAccessKey: String,
  ) = flow {
    var token: String? = null
    while (true) {
      val result =
        listObject2(
          client = client,
          url = url,
          continuationToken = token,
          fetchOwner = fetchOwner,
          maxKeys = blockSize,
          startAfter = startAfter,
          prefix = prefix,
          xAmzExpectedBucketOwner = xAmzExpectedBucketOwner,
          xAmzRequestPayer = xAmzRequestPayer,
          regin = regin,
          bucket = bucket,
          accessKey = accessKey,
          secretAccessKey = secretAccessKey,
        )
      result.contents.forEach {
        emit(it)
      }
      token = result.nextContinuationToken ?: break
    }
  }

  suspend fun headObject(
    client: HttpClientRunnable,
    url: URL,
    regin: String,
    bucket: String,
    key: String,
    partNumber: Int? = null,
    versionId: String? = null,
    accessKey: String,
    secretAccessKey: String,
  ): ContentHead? {
    val query =
      Query.build {
        if (partNumber != null) {
          add("partNumber", partNumber.toString())
        }
        if (versionId != null) {
          add("versionId", versionId.toString())
        }
      }
    return s3Call(
      client = client,
      method = "HEAD",
      url = url.copy(query = query, path = url.path.append(bucket).append(key)),
      regin = regin,
      accessKey = accessKey,
      secretAccessKey = secretAccessKey,
    ).useAsync {
      val region = it.getResponseHeaders().getSingleOrNull("X-Amz-Bucket-Region")
      val length = it.getResponseHeaders().contentLength?.toLong()
      val type = it.getResponseHeaders().contentType
      val eTag = it.getResponseHeaders().getSingleOrNull("ETag")
      val lastModify = it.getResponseHeaders().getSingleOrNull("Last-Modified")?.parseRfc822Date()
      when (val code = it.getResponseCode()) {
        200 ->
          ContentHead(
            region = region,
            length = length,
            contentType = type,
            eTag = eTag,
            lastModify = lastModify,
          )

        404 -> null
        else -> it.throwErrorText(code)
      }
    }
  }

  suspend fun getObject(
    client: HttpClientRunnable,
    url: URL,
    regin: String,
    bucket: String,
    key: String,
    partNumber: Int? = null,
    versionId: String? = null,
    accessKey: String,
    range: List<Range> = emptyList(),
    secretAccessKey: String,
  ): S3ObjectStream? {
    val query =
      Query.build {
        if (partNumber != null) {
          add("partNumber", partNumber.toString())
        }
        if (versionId != null) {
          add("versionId", versionId.toString())
        }
      }
    val resp = s3Call(
      client = client,
      method = "GET",
      url = url.copy(query = query, path = url.path.append(bucket).append(key)),
      regin = regin,
      accessKey = accessKey,
      range = range,
      secretAccessKey = secretAccessKey,
    )
    return when (val code = resp.getResponseCode()) {
      200, 206 -> {
        val region = resp.getResponseHeaders().getSingleOrNull("X-Amz-Bucket-Region")
        val length = resp.getResponseHeaders().contentLength?.toLong()
        val type = resp.getResponseHeaders().contentType
        val eTag = resp.getResponseHeaders().getSingleOrNull("ETag")
        val lastModify = resp.getResponseHeaders().getSingleOrNull("Last-Modified")?.parseRfc822Date()
        val data =
          ContentHead(
            region = region,
            length = length,
            contentType = type,
            eTag = eTag,
            lastModify = lastModify,
          )
        S3ObjectStream(
          data = data,
          source = resp.getInput()
        )
      }

      404 -> null
      else -> resp.useAsync { resp.throwErrorText(code) }
    }
  }

  suspend fun createMultipartUpload(
    client: HttpClientRunnable,
    regin: String,
    url: URL,
    bucket: String,
    key: String,
    contentType: String?,
    accessKey: String,
    secretAccessKey: String,
  ): String {
    val query =
      Query.build {
        add("uploads")
      }
    return s3Call(
      client = client,
      method = "POST",
      url = url.copy(query = query, path = url.path.append(bucket).append(key)),
      contentType = contentType,
      regin = regin,
      accessKey = accessKey,
      secretAccessKey = secretAccessKey,
      payloadSha256 = emptySha256,
      payloadContentLength = 0,
    ).useAsync {
      when (val code = it.getResponseCode()) {
        200 -> {
          val responseText = it.readAllText()
          val r = XmlParser.parse(responseText)
          r
            .tags()
            .singleWithName("InitiateMultipartUploadResult")
            .tags()
            .singleWithName("UploadId")
            .text()
//          val element = responseText.xmlTree(true)
//          xml.decodeFromXmlElement(InitiateMultipartUploadResult.serializer(), element).uploadId
        }

        else -> it.throwErrorText(code)
      }
    }
  }

  suspend fun completeMultipartUpload(
    client: HttpClientRunnable,
    regin: String,
    url: URL,
    bucket: String,
    key: String,
    uploadId: String,
    accessKey: String,
    secretAccessKey: String,
    parts: List<Part>,
  ): CompleteMultipartUploadResult {
    val payloadStr =
      xml.encodeToString(
        serializer = CompleteMultipartUpload.serializer(),
        value = CompleteMultipartUpload(parts),
        withHeader = true,
      )
    val payload = payloadStr.encodeToByteArray()
    val query =
      Query.build {
        add("uploadId", uploadId)
      }
    val b = Sha256MessageDigest()
    b.update(payload)
    val requestHash = b.finish()
    s3Call(
      client = client,
      method = "POST",
      url = url.copy(query = query, path = url.path.append(bucket).append(key)),
      regin = regin,
      accessKey = accessKey,
      secretAccessKey = secretAccessKey,
      payloadContentLength = payload.size.toLong(),
      payloadSha256 = requestHash,
    ) { output ->
      output.bufferedWriter(closeParent = false).useAsync {
        it.append(payloadStr)
      }
//            output.bufferedOutput(closeStream = false).use {
//                it.writeFully(payload)
//            }
    }.useAsync {
      val txt =
        when (val code = it.getResponseCode()) {
          200 -> it.readAllText()
          else -> it.throwErrorText(code)
        }
//      println("XML:\n$txt")
      val res = XmlParser.parse(txt)
        .tags()
        .singleWithName("CompleteMultipartUploadResult")

      return CompleteMultipartUploadResult(
        Location = res.tags().singleWithName("Location").text(),
        Bucket = res.tags().singleWithName("Bucket").text(),
        Key = res.tags().singleWithName("Key").text(),
        ETag = res.tags().singleWithName("ETag").text(),
      )
//      val element = txt.xmlTree(true)
//      return xml.decodeFromXmlElement(CompleteMultipartUploadResult.serializer(), element)
    }
  }

  suspend fun listBuckets(
    client: HttpClientRunnable,
    regin: String,
    url: URL,
    accessKey: String,
    secretAccessKey: String,
  ) = s3Call(
    client = client,
    method = "GET",
    url = url,
    regin = regin,
    accessKey = accessKey,
    secretAccessKey = secretAccessKey,
  ).useAsync {
    when (val code = it.getResponseCode()) {
      200 -> {
        val txt = it.readAllText()
        val root = XmlParser.parse(txt).filterIsInstance<XElement.Tag>().single()
        val owner = root.tags().withName("Owner").single()
        Buckets(
          owner = Owner(
            id = owner.tags().withName("ID").single().text(),
            displayName = owner.tags().withName("DisplayName").single().text(),
          ),
          list = root.tags().withName("Buckets").singleOrNull()
            ?.tags()
            ?.withName("Bucket")
            ?.map {
              Bucket(
                name = it.tags().withName("Name").single().text(),
                creationDate = it.tags().withName("CreationDate").single().text().parseIso8601DateTime(0)!!
              )
            }?.toList() ?: emptyList()
        )


//        println("txt = $txt")
//        val result =
//          xml.decodeFromXmlElement(
//            serializer = ListAllMyBucketsResult.serializer(),
//            xmlElement = txt.xmlTree(true),
//          )
//        Buckets(
//          owner = result.owner,
//          list = result.buckets,
//        )
      }

      else -> it.throwErrorText(code)
    }
  }
}
