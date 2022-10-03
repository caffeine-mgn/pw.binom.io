import kotlinx.coroutines.test.runTest
import pw.binom.base64.Base16
import pw.binom.base64.Base64
import pw.binom.collections.defaultHashMap
import pw.binom.crypto.HMac
import pw.binom.crypto.MD5MessageDigest
import pw.binom.crypto.Sha256MessageDigest
import pw.binom.date.DateTime
import pw.binom.date.format.toDatePattern
import pw.binom.io.ByteBuffer
import pw.binom.io.UTF8
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.create
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.net.URI
import pw.binom.net.toURI
import pw.binom.net.toURL
import kotlin.jvm.JvmName
import kotlin.test.Test

class Signer private constructor(
    val request: Request,
    val contentSha256: String,
    val date: DateTime,
    val region: String,
    val accessKey: String,
    val secretKey: String,
    val prevSignature: String?
) {
    companion object {
        fun signV4(
            serviceName: String,
            request: Request,
            region: String,
            accessKey: String,
            secretKey: String,
            contentSha256: String,
            date: DateTime,
        ): String {
            val signer = Signer(request, contentSha256, date, region, accessKey, secretKey, null)
            signer.setScope(serviceName)
            signer.setCanonicalRequest()
            signer.setStringToSign()
            signer.setSigningKey(serviceName)
            signer.setSignature()
            signer.setAuthorization()
            return signer.authorization
//            return request.newBuilder().header("Authorization", signer.authorization).build()
        }
    }

    private fun setAuthorization() {
        authorization =
            "AWS4-HMAC-SHA256 Credential=$accessKey/$scope, SignedHeaders=$signedHeaders, Signature=$signature"
    }

    lateinit var signingKey: ByteArray
    private fun setSigningKey(serviceName: String) {
        val aws4SecretKey = "AWS4" + this.secretKey

        val dateKey =
            sumHmac(
                aws4SecretKey.encodeToByteArray(),
                SIGNER_DATE_FORMAT.toString(this.date.calendar(0)).encodeToByteArray()
            )

        val dateRegionKey = sumHmac(dateKey, this.region.encodeToByteArray())

        val dateRegionServiceKey =
            sumHmac(dateRegionKey, serviceName.encodeToByteArray())

        this.signingKey = sumHmac(dateRegionServiceKey, "aws4_request".encodeToByteArray())
    }

    public fun sumHmac(key: ByteArray, data: ByteArray): ByteArray {
        val mac = HMac(HMac.Algorithm.SHA256, key)
        mac.update(data)

        return mac.finish()
    }

    private fun setStringToSign() {
        this.stringToSign =
            "AWS4-HMAC-SHA256" + "\n" + AMZ_DATE_FORMAT.toString(this.date.calendar(0)) + "\n" + this.scope + "\n" + this.canonicalRequestHash
    }

    lateinit var stringToSign: String
    lateinit var scope: String
    lateinit var canonicalRequest: String
    lateinit var canonicalRequestHash: String

    @JvmName("setScope2")
    private fun setScope(serviceName: String) {
        this.scope =
            SIGNER_DATE_FORMAT.toString(date.calendar(0)) + "/" + this.region + "/" + serviceName + "/aws4_request"
    }

    var url: URI = "http://aaa/".toURI()
    lateinit var canonicalHeaders: MutableMap<String, String>
    lateinit var signedHeaders: String

    private fun setCanonicalHeaders(ignored_headers: Set<String>) {
        this.canonicalHeaders = defaultHashMap<String, String>()
        val headers: Headers = request.headers
        for (name in headers.names) {
            val signedHeader = name.lowercase()
            if (!ignored_headers.contains(signedHeader)) {
                // Convert and add header values as per
                // https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
                // * Header having multiple values should be converted to comma separated values.
                // * Multi-spaced value of header should be trimmed to single spaced value.
                headers.values
                this.canonicalHeaders[signedHeader] =
                    headers[name]!!.map { it.split(' ').filter { it.isNotEmpty() }.joinToString(" ") }.joinToString(",")
            }
        }
        this.signedHeaders = this.canonicalHeaders.keys.joinToString(";")
    }

    lateinit var canonicalQueryString: String
    private fun setCanonicalQueryString() {
        val encodedQuery = this.url.query?.raw
        if (encodedQuery == null) {
            this.canonicalQueryString = ""
            return
        }
        this.canonicalQueryString = encodedQuery
    }

    private fun setCanonicalRequest() {
        setCanonicalHeaders(IGNORED_HEADERS)
        this.url = request.url
        setCanonicalQueryString()

        // CanonicalRequest =
        //   HTTPRequestMethod + '\n' +
        //   CanonicalURI + '\n' +
        //   CanonicalQueryString + '\n' +
        //   CanonicalHeaders + '\n' +
        //   SignedHeaders + '\n' +
        //   HexEncode(Hash(RequestPayload))
        canonicalRequest = (
            request.method
                .toString() + "\n" +
                this.url.path.raw.split('/').map { UTF8.encode(it) }.joinToString("/") +
                "\n" +
                this.canonicalQueryString +
                "\n" +
                this.canonicalHeaders.map { "${it.key}:${it.value}" }.joinToString("\n") +
                "\n\n" +
                this.signedHeaders +
                "\n" +
                contentSha256
            )
        canonicalRequestHash = Digest.sha256Hash(canonicalRequest)
    }

    lateinit var signature: String
    private fun setSignature() {
        val digest = sumHmac(this.signingKey, this.stringToSign.encodeToByteArray())
        this.signature = Base16.encode(digest).lowercase()
    }

    lateinit var authorization: String
//        "AWS4-HMAC-SHA256 Credential=" + this.accessKey + "/" + this.scope + ", SignedHeaders=" + this.signedHeaders + ", Signature=" + this.signature
}

object Digest {
    fun sha256Hash(string: String): String {
        val sha256Digest = Sha256MessageDigest()
        sha256Digest.update(string.encodeToByteArray())
        return Base16.encode(sha256Digest.finish()).lowercase()
    }

    fun sha256Hash(data: ByteBuffer): String {
        val sha256Digest = Sha256MessageDigest()
        sha256Digest.update(data)
        return Base16.encode(sha256Digest.finish()).lowercase()
    }

    fun md5Hash(data: ByteBuffer): String {
        val md5Digest = MD5MessageDigest()
        md5Digest.update(data)
        return Base64.encode(md5Digest.finish()).lowercase()
    }
}

val SIGNER_DATE_FORMAT = "yyyyMMdd".toDatePattern()
val IGNORED_HEADERS =
    setOf("accept-encoding", "authorization", "content-type", "content-length", "user-agent", "accept")
val AMZ_DATE_FORMAT = "yyyyMMdd'T'HHmmss'Z'".toDatePattern()

data class Request(val method: String, val url: URI, val headers: Headers)

class OOOO {
    @Test
    fun test() = runTest {
        println("OLOLO")
        val content = ByteArray(0)
        val contentSha256 = content.sha256()
        HttpClient.create().use { client ->
            client.connect("GET", "http://127.0.0.1:9000/".toURL()).use { connection ->
                val host = "127.0.0.1:9000"
                val date = DateTime.now
                val specialHeaders: List<Pair<String, String>> = listOf(
                    "host" to host,
                    "x-amz-content-sha256" to contentSha256.toHex(),
                    "x-amz-date" to date.awsDateTime(),
                )

                val service = "s3"
                val regin = "ru-central1"
                val accessKey = "rGIU8vPsmnOx4Prv"
                val secretAccessKey = "bT6YEZsstWsjXh8fJzZdbXvdFZGp3IbR"
                val canonicalRequest = buildCanonicalRequest(
                    method = "GET",
                    uri = "/",
                    query = "",
                    headers = specialHeaders,
                    contentSha256 = contentSha256,
                )
                println("--==Canonical Request==--\n$canonicalRequest\n--==Canonical Request==--")
                val stringToSign = buildStringToSign(
                    date = date,
                    regin = regin,
                    service = service,
                    canonicalRequestHashed = canonicalRequest.sha256()
                )
                println("--==StringToSign==--\n$stringToSign\n--==StringToSign==--")
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
                println("--==AuthHeader==--\n$authHeader\n--==AuthHeader==--")
                specialHeaders.forEach { (key, value) ->
                    connection.headers[key] = value
                }

                connection.getResponse().use { r ->
                    println("r.responseCode=${r.responseCode}")
                    println("text: ${r.readText().readText()}")
                }
            }
        }
    }

    fun ByteArray.sha256(): ByteArray {
        val d = Sha256MessageDigest()
        d.update(this)
        return d.finish()
    }

    fun String.sha256() = encodeToByteArray().sha256()

    /**
     * Считает Canonical Request
     * @param method метод. Например `GET`
     * @param uri URI запроса. Например `/`
     * @param query параметры. Например `prefix=somePrefix&marker=someMarker&max-keys=20`
     * @param headers заголовки
     * @param contentSha256 SHA256Hash(payload)
     */
    fun buildCanonicalRequest(
        method: String,
        uri: String,
        query: String,
        headers: List<Pair<String, String>>,
        contentSha256: ByteArray
    ): String {
        val sb = StringBuilder()
        sb
            .append(method).append("\n") // HTTPMethod
            .append(uri).append("\n") // CanonicalURI
            .append(query).append("\n") // CanonicalQueryString
        headers.forEach { (key, value) ->
            sb.append(key).append(":").append(value.trim()).append("\n") // CanonicalHeaders
        }
        sb.append("\n")
        sb.append(headers.map { it.first }.joinToString(";")).append("\n") // SignedHeaders
        sb.append(contentSha256.toHex()) // HashedPayload
        return sb.toString()
    }

    /**
     * Калькуляция String to Sign
     * @param date дата запроса
     * @param regin регион
     * @param service название сервиса. Например `iam` или `s3`
     * @param canonicalRequestHashed SHA256Hash(<CanonicalRequest>)
     */
    fun buildStringToSign(date: DateTime, regin: String, service: String, canonicalRequestHashed: ByteArray): String {
        val sb = StringBuilder()
            .append("AWS4-HMAC-SHA256\n") // Algorithm
            .append(date.awsDateTime()).append("\n") // RequestDateTime
            .append(date.awsDate()).append("/").append(regin).append("/").append(service)
            .append("/aws4_request\n") // CredentialScope
            .append(canonicalRequestHashed.toHex()) // HashedCanonicalRequest
        return sb.toString()
    }

    val aws4_requestEncoded = "aws4_request".encodeToByteArray()
    fun buildSignature(secretAccessKey: String, date: DateTime, region: String, service: String): ByteArray {
        val dateKey = sumHmac("AWS4$secretAccessKey".encodeToByteArray(), date.awsDate().encodeToByteArray())
        val dateRegionKey = sumHmac(dateKey, region.encodeToByteArray())
        val dateRegionServiceKey = sumHmac(dateRegionKey, service.encodeToByteArray())
        return sumHmac(dateRegionServiceKey, aws4_requestEncoded) // signingKey
    }

    fun buildAuthorizationHeader(
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
        sb.append(",Signature=").append(signature.toHex())
        return sb.toString()
//        AWS4-HMAC-SHA256 Credential=AKIAIOSFODNN7EXAMPLE/20130524/us-east-1/s3/aws4_request,SignedHeaders=host;range;x-amz-content-sha256;x-amz-date,Signature=f0e8bdb87c964420e857bd35b5d6ed310bd44f0170aba48dd91039c6036bdb41
    }

    fun sumHmac(key: ByteArray, data: ByteArray): ByteArray {
        val mac = HMac(HMac.Algorithm.SHA256, key)
        mac.update(data)

        return mac.finish()
    }
}

fun ByteArray.toHex() = joinToString("") { it.toUByte().toString(16).padStart(2, '0') }

val awsDateTimePattern = "yyyyMMdd'T'HHmmss'Z'".toDatePattern()
val awsDatePattern = "yyyyMMdd".toDatePattern()
fun DateTime.awsDateTime() = awsDateTimePattern.toString(calendar(0))
fun DateTime.awsDate() = awsDatePattern.toString(calendar(0))

/*
DEBUG: Canonical Request:
GET
/

host:127.0.0.1:9000
x-amz-content-sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
x-amz-date:20221003T002000Z

host;x-amz-content-sha256;x-amz-date
e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
 */
