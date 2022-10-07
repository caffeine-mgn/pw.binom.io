import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.base64.Base16
import pw.binom.base64.Base64
import pw.binom.collections.defaultHashMap
import pw.binom.crypto.HMac
import pw.binom.crypto.MD5MessageDigest
import pw.binom.crypto.Sha256MessageDigest
import pw.binom.date.DateTime
import pw.binom.date.format.toDatePattern
import pw.binom.date.iso8601
import pw.binom.date.parseIso8601Date
import pw.binom.io.*
import pw.binom.io.http.Headers
import pw.binom.io.http.range.Range
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.create
import pw.binom.net.URI
import pw.binom.net.toURI
import pw.binom.net.toURL
import pw.binom.s3.S3ClientApi
import pw.binom.s3.v4.s3Call
import pw.binom.xml.dom.xmlTree
import pw.binom.xml.serialization.Xml
import pw.binom.xml.serialization.annotations.XmlName
import pw.binom.xml.serialization.annotations.XmlNamespace
import pw.binom.xml.serialization.annotations.XmlNode
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

const val AWS_NS = "http://s3.amazonaws.com/doc/2006-03-01/"

@Serializable
@XmlNamespace([AWS_NS])
data class Content(
    @XmlNode
    @XmlName("Key")
    @XmlNamespace([AWS_NS])
    val key: String,

    @XmlNode
    @XmlName("LastModified")
    @XmlNamespace([AWS_NS])
    @Serializable(IsoDateS::class)
    val lastModified: DateTime,

    @XmlNode
    @XmlName("ETag")
    @XmlNamespace([AWS_NS])
    val eTag: String,

    @XmlNode
    @XmlName("Size")
    @XmlNamespace([AWS_NS])
    val size: Long,

    @XmlNode
    @XmlName("Owner")
    @XmlNamespace([AWS_NS])
    val owner: Owner,
)

@Serializable
@XmlNamespace([AWS_NS])
data class Owner(
    @XmlNode
    @XmlName("ID")
    @XmlNamespace([AWS_NS])
    val id: String,

    @XmlNode
    @XmlName("DisplayName")
    @XmlNamespace([AWS_NS])
    val displayName: String,

)

object IsoDateS : KSerializer<DateTime> {
    override val descriptor: SerialDescriptor
        get() = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): DateTime = decoder.decodeString().parseIso8601Date(0)!!

    override fun serialize(encoder: Encoder, value: DateTime) {
        encoder.encodeString(value.iso8601(0))
    }
}

@Serializable
@XmlNamespace([AWS_NS])
data class ListBucketResult(
    @XmlNamespace([AWS_NS])
    @XmlName("Name")
    @XmlNode
    val name: String,
    @XmlNamespace([AWS_NS])
    @XmlName("Prefix")
    @XmlNode
    val prefix: String,
    @XmlNamespace([AWS_NS])
    @XmlName("Marker")
    @XmlNode
    val marker: String,

    @XmlNamespace([AWS_NS])
    @XmlName("NextMarker")
    @XmlNode
    val nextMarker: String? = null,
    @XmlNamespace([AWS_NS])
    @XmlName("MaxKeys")
    @XmlNode
    val maxKeys: Long,
    @XmlNamespace([AWS_NS])
    @XmlName("Delimiter") @XmlNode
    val delimiter: String,
    @XmlNamespace([AWS_NS]) @XmlName("IsTruncated") @XmlNode
    val isTruncated: Boolean,
    @XmlNamespace([AWS_NS]) @XmlName("Contents")
    val contents: List<Content>,
)

object S3Client

class OOOO {

//    @Test
//    fun test2() = runTest {
//        val str = Xml().encodeToString(
//            ListBucketResult.serializer(),
//            ListBucketResult(
//                name = "123",
//                contents = listOf(Content("my-key",), Content("my-key2", "v2")),
//                prefix = "",
//                marker = "",
//                maxKeys = 1000L,
//                delimiter = "",
//                isTruncated = false
//            )
//        )
//        println("result:\n$str")
//        val b = str.xmlTree()!!
//
//        println("->${Xml().decodeFromString(ListBucketResult.serializer(), str)}")
//    }

    @Test
    fun test3() = runTest {
        val url = "http://127.0.0.1:9000".toURL()
        val regin = "ru-central1"
        val bucket = "test"
        val accessKey = "rGIU8vPsmnOx4Prv"
        val secretAccessKey = "bT6YEZsstWsjXh8fJzZdbXvdFZGp3IbR"
        HttpClient.create().use { client ->
            S3ClientApi.listObjectFlow(
                client = client,
                url = url,
                accessKey = accessKey,
                secretAccessKey = secretAccessKey,
                regin = regin,
                bucket = bucket,
                fetchOwner = true,
                prefix = null,
                startAfter = null,
                xAmzExpectedBucketOwner = null,
                xAmzRequestPayer = null,
            ).collect {
                val b = S3ClientApi.head(
                    client = client,
                    url = url,
                    accessKey = accessKey,
                    secretAccessKey = secretAccessKey,
                    regin = regin,
                    bucket = bucket,
                    key = it.key,
                )
                println("->${it.key} exist=$b")
            }

            val dd: String? = S3ClientApi.get(
                client = client,
                url = url,
                accessKey = accessKey,
                secretAccessKey = secretAccessKey,
                regin = regin,
                bucket = bucket,
                key = "test",
                range = listOf(Range.Last("bytes", 7))
            ) { it ->
                it ?: return@get null
                it.input.bufferedReader().readText()
            }
            println("Test: \"$dd\"")
        }
    }

    @Test
    fun test() = runTest {
        println("OLOLO")
        HttpClient.create().use { client ->
            s3Call(
                client = client,
                method = "GET",
                url = "http://127.0.0.1:9000/test".toURL().appendQuery("max-keys", "3").appendQuery("delimiter", "F"),
            ).use { r ->
                println("r.responseCode=${r.responseCode}")
                var txt = r.readText().readText()
                txt = txt.removePrefix("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                println(txt)
                val r = txt.xmlTree()
                println(r)
                val ff = Xml().decodeFromXmlElement(ListBucketResult.serializer(), r!!)
                println(ff)

                ff.contents.forEach {
                    println("--->${it.key}")
                }
            }
        }
    }
}
