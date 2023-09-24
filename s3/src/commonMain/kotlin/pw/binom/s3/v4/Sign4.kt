package pw.binom.s3.v4

import pw.binom.date.DateTime
import pw.binom.io.AsyncOutput
import pw.binom.io.http.Headers
import pw.binom.io.http.range.Range
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.HttpResponse
import pw.binom.io.use
import pw.binom.url.URL
import pw.binom.url.UrlEncoder

internal suspend fun s3Call(
  client: HttpClient,
  method: String,
  url: URL,
  payloadSha256: ByteArray? = null,
  overrideHost: String? = null,
  xAmzCopySource: String? = null,
  payloadContentLength: Long? = null,
  contentType: String? = null,
  service: String = "s3",
  regin: String,
  accessKey: String,
  secretAccessKey: String,
  range: List<Range> = emptyList(),
  payload: (suspend (AsyncOutput) -> Unit)? = null,
): HttpResponse {
  client.connect(method = method, uri = url).use { connection ->
    val host = overrideHost ?: url.host
    val date = DateTime.now
    val specialHeaders: List<Pair<String, String>> = buildList {
      add("host" to host)
      val contentSha256 = when (payloadSha256) {
//                null -> if (method == "PUT") STREAMING_AWS4_HMAC_SHA256_PAYLOAD else UNSIGNED_PAYLOAD
        null -> UNSIGNED_PAYLOAD
        else -> payloadSha256.toHex()
      }
      add("x-amz-content-sha256" to contentSha256)
      if (xAmzCopySource != null) {
        add("x-amz-copy-source" to UrlEncoder.pathEncode(xAmzCopySource))
      }
      add("x-amz-date" to date.awsDateTime())
    }.sortedBy { it.first }
    val query = url.query?.let {
      it.toMap().entries.sortedBy { it.key }
        .map { "${UrlEncoder.encode(it.key)}=${it.value?.let { UrlEncoder.encode(it) } ?: ""}" }
        .joinToString("&")
    }
    val canonicalRequest = buildCanonicalRequest(
      method = method,
      uri = url.path.toString(),
      query = query ?: "",
      headers = specialHeaders,
      contentSha256 = payloadSha256,
    )
    val stringToSign = buildStringToSign(
      date = date,
      regin = regin,
      service = service,
      canonicalRequestHashed = canonicalRequest.sha256(),
    )
    val signingKey = buildSignature(
      secretAccessKey = secretAccessKey,
      date = date,
      region = regin,
      service = service,
    )
    val authHeader = buildAuthorizationHeader(
      accessKey = accessKey,
      date = date,
      regin = regin,
      service = service,
      headers = specialHeaders.map { it.first },
      signature = sumHmac(signingKey, stringToSign.encodeToByteArray()),
    )
    connection.headers[Headers.AUTHORIZATION] = authHeader
    if (contentType != null) {
      connection.headers.contentType = contentType
    }
    if (payloadContentLength != null) {
      connection.headers.contentLength = payloadContentLength.toULong()
    }
    specialHeaders.forEach { (key, value) ->
      connection.headers[key] = value
    }
    if (range.isNotEmpty()) {
      connection.headers.range = range
    }
    return if (payload != null) {
      connection.writeBinaryAndGetResponse {
        payload(it)
      }
    } else {
      connection.getResponse()
    }
  }
}
