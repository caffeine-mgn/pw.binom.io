package pw.binom.s3.v4

import pw.binom.crypto.HMac
import pw.binom.crypto.Sha256MessageDigest
import pw.binom.date.DateTime
import pw.binom.date.format.toDatePattern

internal const val UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD"
internal const val STREAMING_AWS4_HMAC_SHA256_PAYLOAD = "STREAMING-AWS4-HMAC-SHA256-PAYLOAD"
private val aws4_requestEncoded = "aws4_request".encodeToByteArray()
private val awsDateTimePattern = "yyyyMMdd'T'HHmmss'Z'".toDatePattern()
private val awsDatePattern = "yyyyMMdd".toDatePattern()

/**
 * Считает Canonical Request
 *
 * @param method метод. Например `GET`
 * @param uri URI запроса. Например `/`
 * @param query параметры. Например `prefix=somePrefix&marker=someMarker&max-keys=20`
 * @param headers заголовки
 * @param contentSha256 SHA256Hash(payload)
 */
internal fun buildCanonicalRequest(
    method: String,
    uri: String,
    query: String,
    headers: List<Pair<String, String>>,
    contentSha256: ByteArray?
): String {
    val sb = StringBuilder()
    sb
        .append(method).append("\n") // HTTPMethod
        .append(uri.ifEmpty { "/" }).append("\n") // CanonicalURI
        .append(query).append("\n") // CanonicalQueryString
    headers.forEach { (key, value) ->
        sb.append(key).append(":").append(value.trim()).append("\n") // CanonicalHeaders
    }
    sb.append("\n")
    sb.append(headers.map { it.first }.joinToString(";")).append("\n") // SignedHeaders
    if (contentSha256 == null) { // HashedPayload
        sb.append(UNSIGNED_PAYLOAD)
    } else {
        sb.appendHex(contentSha256)
    }
    return sb.toString()
}

/**
 * Калькуляция StringToSign
 *
 * @param date дата запроса
 * @param regin регион
 * @param service название сервиса. Например `iam` или `s3`
 * @param canonicalRequestHashed SHA256Hash(<CanonicalRequest>)
 */
internal fun buildStringToSign(
    date: DateTime,
    regin: String,
    service: String,
    canonicalRequestHashed: ByteArray
): String {
    val sb = StringBuilder()
        .append("AWS4-HMAC-SHA256\n") // Algorithm
        .append(date.awsDateTime()).append("\n") // RequestDateTime
        .append(date.awsDate()).append("/").append(regin).append("/").append(service)
        .append("/aws4_request\n") // CredentialScope
        .appendHex(canonicalRequestHashed) // HashedCanonicalRequest
    return sb.toString()
}

internal fun buildSignature(secretAccessKey: String, date: DateTime, region: String, service: String): ByteArray {
    val dateKey = sumHmac("AWS4$secretAccessKey".encodeToByteArray(), date.awsDate().encodeToByteArray())
    val dateRegionKey = sumHmac(dateKey, region.encodeToByteArray())
    val dateRegionServiceKey = sumHmac(dateRegionKey, service.encodeToByteArray())
    return sumHmac(dateRegionServiceKey, aws4_requestEncoded) // signingKey
}

internal fun buildAuthorizationHeader(
    accessKey: String,
    date: DateTime,
    regin: String,
    service: String,
    headers: List<String>,
    signature: ByteArray
): String {
    val sb = StringBuilder("AWS4-HMAC-SHA256 ")
    sb.append("Credential=").append(accessKey).append("/").append(date.awsDate()).append("/").append(regin)
        .append("/").append(service).append("/").append("aws4_request")
    sb.append(",SignedHeaders=").append(headers.joinToString(";"))
    sb.append(",Signature=").appendHex(signature)
    return sb.toString()
}

internal fun sumHmac(key: ByteArray, data: ByteArray): ByteArray {
    val mac = HMac(HMac.Algorithm.SHA256, key)
    mac.update(data)
    return mac.finish()
}

private fun <T : Appendable> T.appendHex(data: ByteArray): T {
    data.forEach {
        append(it.toUByte().toString(16).padStart(2, '0'))
    }
    return this
}

private fun ByteArray.sha256(): ByteArray {
    val d = Sha256MessageDigest()
    d.update(this)
    return d.finish()
}

internal fun ByteArray.toHex(): String = StringBuilder(size * 2)
    .appendHex(this)
    .toString()

internal fun DateTime.awsDateTime() = awsDateTimePattern.toString(calendar(0))
private fun DateTime.awsDate() = awsDatePattern.toString(calendar(0))

internal fun String.sha256() = encodeToByteArray().sha256()
