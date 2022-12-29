package pw.binom.s3

import pw.binom.crypto.MD5MessageDigest
import pw.binom.crypto.Sha256MessageDigest
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.holdState
import pw.binom.s3.dto.Part
import pw.binom.s3.v4.toHex

class ObjectAsyncOutput(
    val bucket: String,
    val key: String,
    val regin: String,
    val contentType: String?,
    val client: S3Client,
    bufferSize: Int = MIN_PACKAGE_SIZE
) : AsyncOutput {
    companion object {
        const val MIN_PACKAGE_SIZE = 1024 * 1024 * 5
    }

    init {
        require(bufferSize >= MIN_PACKAGE_SIZE) { "S3 support support minimal size of package is $MIN_PACKAGE_SIZE bytes" }
    }

    var contentSize: Long = 0
        private set
    private val buffer = ByteBuffer(bufferSize)
    private var uploadId: String? = null
    private val parts = ArrayList<Part>()
    override suspend fun write(data: ByteBuffer): Int {
        if (data.remaining == 0) {
            return 0
        }
        if (buffer.remaining <= 0) {
            throw IllegalStateException("No free buffer size")
        }
        val wrote = buffer.write(data)
        if (wrote > 0) {
            contentSize += wrote
        }
        if (buffer.remaining == 0) {
            makePart()
        }
        return wrote
    }

    private suspend fun checkMultipart() {
        if (uploadId == null) {
            uploadId = client.createMultipartUpload(
                regin = regin,
                bucket = bucket,
                key = key,
                contentType = contentType,
            )
        }
    }

    private suspend fun makePart() {
        checkMultipart()
        buffer.flip()
        val md5 = buffer.holdState { buffer ->
            val b = MD5MessageDigest()
            b.update(buffer)
            b.finish()
        }
        val sha256 = buffer.holdState { buffer ->
            val b = Sha256MessageDigest()
            b.update(buffer)
            b.finish()
        }
        val part = Part(
            eTag = md5.toHex(),
            partNumber = parts.size + 1,
            checksumSHA256 = sha256.toHex()
        )
        parts += part
        client.putObject(
            bucket = bucket,
            key = key,
            regin = regin,
            payloadContentLength = buffer.remaining.toLong(),
            payloadSha256 = sha256,
            partNumber = parts.size,
            uploadId = uploadId,
        ) { output ->
            output.writeFully(buffer)
        }
        buffer.clear()
    }

    override suspend fun asyncClose() {
        val uploadId = uploadId
        try {
            if (uploadId != null) {
                if (buffer.position > 0) {
                    makePart()
                }
                client.completeMultipartUpload(
                    regin = regin,
                    bucket = bucket,
                    key = key,
                    uploadId = uploadId,
                    parts = parts,
                )
            } else {
                buffer.flip()
                val sha256 = buffer.holdState { buffer ->
                    val d = Sha256MessageDigest()
                    d.update(buffer)
                    d.finish()
                }
                client.putObject(
                    bucket = bucket,
                    key = key,
                    regin = regin,
                    payloadContentLength = buffer.remaining.toLong(),
                    payloadSha256 = sha256,
                ) {
                    it.writeFully(buffer)
                }
            }
        } finally {
            buffer.close()
        }
    }

    override suspend fun flush() {
        // Do nothing
    }
}
