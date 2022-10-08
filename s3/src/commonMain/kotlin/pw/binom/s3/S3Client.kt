package pw.binom.s3

import kotlinx.coroutines.flow.flow
import pw.binom.io.AsyncOutput
import pw.binom.io.http.range.Range
import pw.binom.io.httpClient.HttpClient
import pw.binom.net.URL
import pw.binom.s3.dto.ContentHead

class S3Client(
    val url: URL,
    val accessKey: String,
    val secretAccessKey: String,
    val client: HttpClient
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
        secretAccessKey = secretAccessKey
    )

    suspend fun putObject(
        bucket: String,
        key: String,
        regin: String,
        payload: suspend (AsyncOutput) -> Unit,
    ) {
        S3ClientApi.putObject(
            client = client,
            regin = regin,
            bucket = bucket,
            key = key,
            url = url,
            accessKey = accessKey,
            secretAccessKey = secretAccessKey,
            payload = payload
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
        delimiter = delimiter
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
            val result = listObject2(
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
    ): ContentHead? = S3ClientApi.headObject(
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

    suspend fun <T> getObject(
        regin: String,
        bucket: String,
        key: String,
        partNumber: Int? = null,
        versionId: String? = null,
        range: List<Range> = emptyList(),
        consumer: suspend (InputFile?) -> T
    ): T = S3ClientApi.getObject(
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
        consumer = consumer,
    )

    suspend fun listBuckets(regin: String) = S3ClientApi.listBuckets(
        client = client,
        regin = regin,
        url = url,
        accessKey = accessKey,
        secretAccessKey = secretAccessKey,
    )
}
