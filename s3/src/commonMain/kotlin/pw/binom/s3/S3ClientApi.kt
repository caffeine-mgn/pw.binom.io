package pw.binom.s3

import kotlinx.coroutines.flow.flow
import kotlinx.serialization.modules.SerializersModule
import pw.binom.date.DateTime
import pw.binom.date.parseRfc822Date
import pw.binom.io.AsyncOutput
import pw.binom.io.bufferedWriter
import pw.binom.io.http.range.Range
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.HttpResponse
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.net.Query
import pw.binom.net.URL
import pw.binom.s3.dto.*
import pw.binom.s3.exceptions.S3ErrorException
import pw.binom.s3.exceptions.S3Exception
import pw.binom.s3.serialization.DateSerialization
import pw.binom.s3.v4.s3Call
import pw.binom.xml.dom.xmlTree
import pw.binom.xml.serialization.Xml

private val dd = SerializersModule {
    contextual(DateTime::class, DateSerialization)
}
private val xml = Xml(serializersModule = dd)

object S3ClientApi {

    private suspend fun HttpResponse.throwErrorText(code: Int): Nothing {
        val resp = readText().use { it.readText() }
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
        client: HttpClient,
        locationConstraint: String?,
        regin: String,
        name: String,
        url: URL,
        accessKey: String,
        secretAccessKey: String,
    ) {
        val payload = xml.encodeToString(
            CreateBucketConfiguration.serializer(),
            CreateBucketConfiguration(locationConstraint = locationConstraint)
        )
        s3Call(
            client = client,
            method = "PUT",
            url = url.copy(path = url.path.append(name)),
            regin = regin,
            accessKey = accessKey,
            secretAccessKey = secretAccessKey,
        ) { output ->
            output.bufferedWriter(closeParent = false).use {
                it.append(payload)
            }
        }.use {
            when (val code = it.responseCode) {
                200 -> null
                else -> it.throwErrorText(code)
            }
        }
    }

    suspend fun deleteObject(
        client: HttpClient,
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
    ).use {
        when (val code = it.responseCode) {
            200 -> true
            404 -> false
            else -> it.throwErrorText(code)
        }
    }

    suspend fun putObject(
        client: HttpClient,
        regin: String,
        bucket: String,
        key: String,
        url: URL,
        accessKey: String,
        secretAccessKey: String,
        payload: suspend (AsyncOutput) -> Unit,
    ) {
        s3Call(
            client = client,
            method = "PUT",
            url = url.copy(path = url.path.append(bucket).append(key)),
            regin = regin,
            accessKey = accessKey,
            secretAccessKey = secretAccessKey,
        ) { output ->
            payload(output)
        }.use {
            when (val code = it.responseCode) {
                200 -> null
                else -> it.throwErrorText(code)
            }
        }
    }

    suspend fun copyObject(
        client: HttpClient,
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
            xAmzCopySource = "$sourceBucket/$sourceKey"
        ).use {
            when (val code = it.responseCode) {
                200 -> null
                else -> it.throwErrorText(code)
            }
        }
    }

    suspend fun listObject2(
        client: HttpClient,
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
        val query = Query.build {
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
        val result = s3Call(
            client = client,
            method = "GET",
            url = url.copy(query = query, path = url.path.append(bucket)),
            regin = regin,
            accessKey = accessKey,
            secretAccessKey = secretAccessKey,
        ).use {
            it.readText().use { it.readText() }
        }
        val element = result.xmlTree(true)
        return xml.decodeFromXmlElement(ListBucketResultV2.serializer(), element)
    }

    fun listObjectFlow(
        client: HttpClient,
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
            val result = listObject2(
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
        client: HttpClient,
        url: URL,
        regin: String,
        bucket: String,
        key: String,
        partNumber: Int? = null,
        versionId: String? = null,
        accessKey: String,
        secretAccessKey: String,
    ): ContentHead? {
        val query = Query.build {
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
        ).use {
            val region = it.headers["X-Amz-Bucket-Region"]?.firstOrNull()
            val length = it.headers.contentLength?.toLong()
            val type = it.headers.contentType
            val eTag = it.headers["ETag"]?.firstOrNull()
            val lastModify = it.headers["Last-Modified"]?.firstOrNull()?.parseRfc822Date()
            when (val code = it.responseCode) {
                200 -> ContentHead(
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

    suspend fun <T> getObject(
        client: HttpClient,
        url: URL,
        regin: String,
        bucket: String,
        key: String,
        partNumber: Int? = null,
        versionId: String? = null,
        accessKey: String,
        range: List<Range> = emptyList(),
        secretAccessKey: String,
        consumer: suspend (InputFile?) -> T
    ): T {
        val query = Query.build {
            if (partNumber != null) {
                add("partNumber", partNumber.toString())
            }
            if (versionId != null) {
                add("versionId", versionId.toString())
            }
        }
        return s3Call(
            client = client,
            method = "GET",
            url = url.copy(query = query, path = url.path.append(bucket).append(key)),
            regin = regin,
            accessKey = accessKey,
            range = range,
            secretAccessKey = secretAccessKey,
        ).use {
            when (val code = it.responseCode) {
                200, 206 -> {
                    val region = it.headers["X-Amz-Bucket-Region"]?.firstOrNull()
                    val length = it.headers.contentLength?.toLong()
                    val type = it.headers.contentType
                    val eTag = it.headers["ETag"]?.firstOrNull()
                    val lastModify = it.headers["Last-Modified"]?.firstOrNull()?.parseRfc822Date()
                    val data = ContentHead(
                        region = region,
                        length = length,
                        contentType = type,
                        eTag = eTag,
                        lastModify = lastModify,
                    )
                    it.readData().use { input ->
                        consumer(
                            InputFile(
                                data = data,
                                input = input
                            )
                        )
                    }
                }

                404 -> consumer(null)
                else -> it.throwErrorText(code)
            }
        }
    }

    suspend fun listBuckets(client: HttpClient, regin: String, url: URL, accessKey: String, secretAccessKey: String) =
        s3Call(
            client = client,
            method = "GET",
            url = url,
            regin = regin,
            accessKey = accessKey,
            secretAccessKey = secretAccessKey,
        ).use {
            when (val code = it.responseCode) {
                200 -> {
                    val txt = it.readText().use {
                        it.readText()
                    }
                    val result = xml.decodeFromXmlElement(
                        serializer = ListAllMyBucketsResult.serializer(),
                        xmlElement = txt.xmlTree(true),
                    )
                    Buckets(
                        owner = result.owner,
                        list = result.buckets
                    )
                }

                else -> it.throwErrorText(code)
            }
        }
}
