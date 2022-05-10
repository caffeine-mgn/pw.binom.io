import pw.binom.io.ByteBuffer
import pw.binom.base64.Base16
import pw.binom.base64.Base64
import pw.binom.crypto.HMac
import pw.binom.crypto.MD5MessageDigest
import pw.binom.crypto.Sha256MessageDigest
import pw.binom.date.Date
import pw.binom.date.format.toDatePattern
import pw.binom.io.UTF8
import pw.binom.io.http.Headers
import pw.binom.net.URI
import pw.binom.net.toURI
import kotlin.jvm.JvmName

class Signer private constructor(
    val request: Request,
    val contentSha256: String,
    val date: Date,
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
            date: Date,
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
        val aws4SecretKey = "AWS4" + this.secretKey;

        val dateKey =
            sumHmac(
                aws4SecretKey.encodeToByteArray(),
                SIGNER_DATE_FORMAT.toString(this.date.calendar(0)).encodeToByteArray()
            );

        val dateRegionKey = sumHmac(dateKey, this.region.encodeToByteArray())

        val dateRegionServiceKey =
            sumHmac(dateRegionKey, serviceName.encodeToByteArray())

        this.signingKey = sumHmac(dateRegionServiceKey, "aws4_request".encodeToByteArray());
    }

    public fun sumHmac(key: ByteArray, data: ByteArray): ByteArray {
        val mac = HMac(HMac.Algorithm.SHA256, key)
        mac.update(data);

        return mac.finish()
    }

    private fun setStringToSign() {
        this.stringToSign =
            "AWS4-HMAC-SHA256" + "\n" + AMZ_DATE_FORMAT.toString(this.date.calendar(0)) + "\n" + this.scope + "\n" + this.canonicalRequestHash;
    }

    lateinit var stringToSign: String
    lateinit var scope: String
    lateinit var canonicalRequest: String
    lateinit var canonicalRequestHash: String

    @JvmName("setScope2")
    private fun setScope(serviceName: String) {

        this.scope =
            SIGNER_DATE_FORMAT.toString(date.calendar(0)) + "/" + this.region + "/" + serviceName + "/aws4_request";
    }

    var url: URI = "http://aaa/".toURI()
    lateinit var canonicalHeaders: HashMap<String, String>
    lateinit var signedHeaders: String

    private fun setCanonicalHeaders(ignored_headers: Set<String>) {
        this.canonicalHeaders = HashMap<String, String>()
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
            this.canonicalQueryString = "";
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
        canonicalRequest = (request.method
            .toString() + "\n"
                + this.url.path.raw.split('/').map { UTF8.encode(it) }.joinToString("/")
                + "\n"
                + this.canonicalQueryString
                + "\n"
                + this.canonicalHeaders.map { "${it.key}:${it.value}" }.joinToString("\n")
                + "\n\n"
                + this.signedHeaders
                + "\n"
                + contentSha256)
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

    fun sha256Hash(data:ByteBuffer): String {
        val sha256Digest = Sha256MessageDigest()
        sha256Digest.update(data)
        return Base16.encode(sha256Digest.finish()).lowercase()
    }

    fun md5Hash(data:ByteBuffer): String {
        val md5Digest = MD5MessageDigest()
        md5Digest.update(data)
        return Base64.encode(md5Digest.finish()).lowercase()
    }
}

val SIGNER_DATE_FORMAT = "yyyyMMdd".toDatePattern()
val IGNORED_HEADERS = setOf("accept-encoding", "authorization", "content-type", "content-length", "user-agent", "accept")
val AMZ_DATE_FORMAT = "yyyyMMdd'T'HHmmss'Z'".toDatePattern()

data class Request(val method: String, val url: URI, val headers: Headers)