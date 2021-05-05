import pw.binom.*
import pw.binom.base64.Base16
import pw.binom.base64.Base64
import pw.binom.crypto.MD5MessageDigest
import pw.binom.crypto.Sha256MessageDigest
import pw.binom.date.Date
import pw.binom.date.of
import pw.binom.date.rfc822
import pw.binom.io.UTF8
import pw.binom.io.http.*
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.HttpResponse
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpServer
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.net.URI
import pw.binom.net.toURI
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.Test

class S3Client(val accessKey: String, val secretKey: String, val client: HttpClient) {
    suspend fun request(uri: URI, method: HTTPMethod, data: ByteBuffer): HttpResponse {
        val date = Date()
//        val date = Date.of(
//            2021,1,1,1,1,0,0,Date.timeZoneOffset
//        )
        return client.request(method, uri).let {
            it.headers["x-amz-date"] = AMZ_DATE_FORMAT.toString(date.calendar(0))
            it.headers["User-Agent"] = "Binom (${Environment.platform.name})"
            it.headers["x-amz-content-sha256"] =
                if (it.uri.schema == "https")
                    "UNSIGNED-PAYLOAD"
                else {
                    val sha = Sha256MessageDigest()
                    data.set(0, data.capacity) {
                        sha.update(data)
                    }
                    Base16.encode(sha.finish())
                }
            val md5 = MD5MessageDigest()
            data.set(0, data.capacity) {
                md5.update(data)
            }
            it.headers["Content-MD5"] = Base64.encode(md5.finish())
            it.headers.contentLength = data.remaining.toULong()
            it.headers.forEachHeader { key, value ->
                println("${key}: ${value}")
            }
            val ss = Signer.signV4(
                serviceName = "s3",
                request = Request(
                    method = it.method.code,
                    url = it.uri,
                    headers = it.headers
                ),
                region = "us-east-1",
                accessKey = accessKey,
                secretKey = secretKey,
                contentSha256 = it.headers.getSingle("x-amz-content-sha256")!!,
                date = date
            )

            it.headers[Headers.AUTHORIZATION] = ss
            it.writeData { it.write(data) }
        }
    }
}

suspend fun HttpClient.request(uri: URI, accessKey: String, secretKey: String, bucketName: String) {
    val date = Date()
    val fullUri = uri.appendPath("http://minio-test.dev.binom.pw/${UTF8.encode(bucketName)}?location=")
    request(HTTPMethod.HEAD, fullUri).also {
        it.headers["x-amz-date"] = AMZ_DATE_FORMAT.toString(date.calendar(0))
        it.headers["Host"] = it.uri.host
        it.headers["Accept-Encoding"] = "identity"
        it.headers["Content-Length"] = "0"
        it.headers["User-Agent"] = "Binom (${Environment.platform.name})"
        it.headers["x-amz-content-sha256"] =
            if (it.uri.schema == "https") "UNSIGNED-PAYLOAD" else Base16.encode(Sha256MessageDigest().finish())
        it.headers["Content-MD5"] = Base64.encode(MD5MessageDigest().also { it.update(byteArrayOf()) }.finish())
        val ss = Signer.signV4(
            serviceName = "s3",
            request = Request(
                method = it.method.code,
                url = it.uri,
                headers = it.headers
            ),
            region = "us-east-1",
            accessKey = "a7f43c610353c44d2ec0a6c4",
            secretKey = "03eeb764adad82d0d072a78afa35f585ccb70a086abfedee8b4de67d3067315b",
            contentSha256 = it.headers.getSingle("x-amz-content-sha256")!!,
            date = date
        )

        it.headers[Headers.AUTHORIZATION] = ss
    }
}

class AAA() {

    @Test
    fun signTest() {
//        val date = Date.of(
//            year = 2021,
//            month = 5,
//            dayOfMonth = 4,
//            hours = 21,
//            minutes = 44,
//            seconds = 37,
//            millis = 0,
//            timeZoneOffset = 0
//        )
//        Sha256MessageDigest().update(byteArrayOf())
//        val emptySha = Base16.encode(Sha256MessageDigest().finish())
//        println(emptySha)
//        return
        val date = Date()
//        val ee = Signer.signV4(
//            serviceName = "192.168.88.27:1212",
//            request = Request(
//                method = "GET", url = "http://192.168.88.27:1212/?delimiter=%2F".toURI(), headers = headersOf(
//                    "x-amz-date" to AMZ_DATE_FORMAT.toString(date.calendar(0)),
//                    "Host" to "192.168.88.27:1212",
//                    "Accept-Encoding" to "identity",
//                    "Content-Length" to "0",
//                )
//            ),
//            region = "",
//            accessKey = "a7f43c610353c44d2ec0a6c4",
//            secretKey = "03eeb764adad82d0d072a78afa35f585ccb70a086abfedee8b4de67d3067315b",
//            contentSha256 = "",
//            date = date
//        )
//        headersOf("" to "")
//        val h = AWSSign3().sign(
//            method = "GET",
//            contentMd5 = MD5MessageDigest().finish(),
//            contentType = "",
//            date = date.rfc822(),
//            header = HashHeaders(),
//            request = "/",
//            keyId = "a7f43c610353c44d2ec0a6c4",
//            secretKey = "03eeb764adad82d0d072a78afa35f585ccb70a086abfedee8b4de67d3067315b"
//        )
//
//        println("h=$h")
//        println("e=$ee")


        val nd = NetworkDispatcher()

        val bb = async2 {
            HttpClient(nd).use { client ->
                val s3 = S3Client(
                    accessKey = "a7f43c610353c44d2ec0a6c4",
                    secretKey = "03eeb764adad82d0d072a78afa35f585ccb70a086abfedee8b4de67d3067315b",
                    client = client
                )

                val vv = s3.request(
                    "http://minio-test.dev.binom.pw/test?location=".toURI(),
                    HTTPMethod.GET,
                    ByteBuffer.alloc(0)
                )
                val txt = vv.readText().use { it.readText() }
                println("vv=${vv.responseCode}\n$txt")
            }

//            val date = Date()
//            HttpClient(nd).use { client ->
//                client.request(method = HTTPMethod.GET, "https://minio-test.dev.binom.pw/?delimiter=%2F".toURI()).also {
//                    it.headers["x-amz-date"] = AMZ_DATE_FORMAT.toString(date.calendar(0))
//                    it.headers["Host"] = it.uri.host
//                    it.headers["Accept-Encoding"] = "identity"
//                    it.headers["Content-Length"] = "0"
//                    it.headers["User-Agent"] = "Binom (${Environment.platform.name})"
//                    it.headers["x-amz-content-sha256"] =
//                        if (it.uri.schema == "https") "UNSIGNED-PAYLOAD" else Base16.encode(Sha256MessageDigest().finish())
//                    it.headers["Content-MD5"] = Base64.encode(MD5MessageDigest().finish())
//                    val ss = Signer.signV4(
//                        serviceName = "s3",
//                        request = Request(
//                            method = it.method.code,
//                            url = it.uri,
//                            headers = it.headers
//                        ),
//                        region = "us-east-1",
//                        accessKey = "a7f43c610353c44d2ec0a6c4",
//                        secretKey = "03eeb764adad82d0d072a78afa35f585ccb70a086abfedee8b4de67d3067315b",
//                        contentSha256 = it.headers.getSingle("x-amz-content-sha256")!!,
//                        date = date
//                    )
//
//                    it.headers[Headers.AUTHORIZATION] = ss
//
//                    it.getResponse().use { response ->
//                        println("response.responseCode: ${response.responseCode}")
//                        println("ResponseHeaders:\n${response.headers}")
//                        val txt = response.readText().use { it.readText() }
//                        println("txt: $txt")
//                    }
//                }
//            }
        }
        while (!bb.isDone) {
            nd.select()
        }
        bb.getOrException()
    }

    @Test
    fun tt() {
        val nd = NetworkDispatcher()
        val server = HttpServer(nd, Handler {
            val inputText = it.readText().use { it.readText() }
            println("Request: [${it.request}]")
            println("Headers:")
            it.headers.forEachHeader { key, value ->
                println("    [$key] -> [$value]")
            }
            println("Body size: ${inputText.length}")
            it.response {
                val requestId = Random.nextInt().absoluteValue
                it.status = 403
                it.headers["X-Amz-Request-Id"] = requestId.toString()
                it.headers["X-Xss-Protection"] = "X-Xss-Protection: 1; mode=block"
                it.writeText(
                    """
<Error><Code>AccessDenied</Code><Message>Access Denied.</Message><Resource>/</Resource><RequestId>$requestId</RequestId><HostId>8868d5ce-d5eb-4745-8b1d-771332c63806</HostId></Error> 
                """
                )
            }
            println("Request ${it.request} ${it.method} text: ${inputText}, headers: ${it.headers}")
        })
        server.bindHttp(NetworkAddress.Immutable(port = 1212))
        while (true) {
            nd.select()
        }
    }
}