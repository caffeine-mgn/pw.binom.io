package pw.binom.s3.v4

import pw.binom.date.DateTime
import pw.binom.io.AsyncOutput
import pw.binom.io.UTF8
import pw.binom.io.http.Headers
import pw.binom.io.http.range.Range
import pw.binom.io.http.range.toHeader
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.HttpResponse
import pw.binom.io.use
import pw.binom.net.URL

internal suspend fun s3Call(
    client: HttpClient,
    method: String,
    url: URL,
    payloadSha256: ByteArray? = null,
    overrideHost: String? = null,
    xAmzCopySource: String? = null,
    service: String = "s3",
    regin: String = "ru-central1",
    accessKey: String = "rGIU8vPsmnOx4Prv",
    secretAccessKey: String = "bT6YEZsstWsjXh8fJzZdbXvdFZGp3IbR",
    range: List<Range> = emptyList(),
    payload: (suspend (AsyncOutput) -> Unit)? = null,
): HttpResponse {
    client.connect(method = method, uri = url).use { connection ->
        val host = overrideHost ?: url.host
        val date = DateTime.now
        val specialHeaders: List<Pair<String, String>> = buildList {
            add("host" to host)
            add("x-amz-content-sha256" to (payloadSha256?.toHex() ?: UNSIGNED_PAYLOAD))
            if (xAmzCopySource != null) {
                add("x-amz-copy-source" to UTF8.urlEncode(xAmzCopySource))
            }
            add("x-amz-date" to date.awsDateTime())
        }.sortedBy { it.first }
        val query = url.query?.let {
            it.toMap().entries.sortedBy { it.key }.map { if (it.value == null) it.key else "${it.key}=${it.value}" }
                .joinToString("&")
        }
        val canonicalRequest = buildCanonicalRequest(
            method = method,
            uri = url.path.toString(),
            query = query ?: "",
            headers = specialHeaders,
            contentSha256 = payloadSha256,
        )
//        println("---===canonicalRequest===---\n$canonicalRequest\n---===canonicalRequest===---")
        val stringToSign = buildStringToSign(
            date = date,
            regin = regin,
            service = service,
            canonicalRequestHashed = canonicalRequest.sha256()
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
        specialHeaders.forEach { (key, value) ->
            connection.headers[key] = value
        }
        if (range.isNotEmpty()) {
            connection.headers[Headers.RANGE] = range.toHeader()
        }
        return if (payload != null) {
            connection.writeData { payload(it) }
        } else {
            connection.getResponse()
        }
    }
}
