package pw.binom.webdav.client

import pw.binom.io.*
import pw.binom.io.http.HTTPMethod
import pw.binom.io.httpClient.addHeader
import pw.binom.net.Path
import pw.binom.skipAll
import pw.binom.webdav.WebAuthAccess

class WebdavEntity(
    override val length: Long,
    override val lastModified: Long,
    override val path: Path,
    val user: WebAuthAccess?,
    override val isFile: Boolean,
    override val fileSystem: WebDavClient,
    val quotaUsedBytes: Long?,
    val quotaAvailableBytes: Long?,
) : FileSystem.Entity {

    override suspend fun read(offset: ULong, length: ULong?): AsyncInput? {
        val allPathUrl = fileSystem.url.addPath(path)
        val r = fileSystem.client.connect(HTTPMethod.GET.code, allPathUrl)
        if (offset != 0uL) {
            if (length == null) {
                r.addHeader("Range", "bytes=$offset-")
            } else {
                r.addHeader("Range", "bytes=$offset-${offset + length - 1u}")
            }
        }
        user?.apply(r)
        val resp = r.getResponse()
        if (resp.responseCode == 404)
            return null
        val body = resp.readData()

        return object : AsyncInput {
            override val available: Int
                get() = -1

            override suspend fun read(dest: ByteBuffer): Int = body.read(dest)

            override suspend fun asyncClose() {
                resp.asyncClose()
            }
        }
    }

    override suspend fun copy(path: Path, overwrite: Boolean): FileSystem.Entity {
        val destinationUrl = fileSystem.url.addPath(path)
        val r = fileSystem.client.connect(
            method = HTTPMethod.COPY.code,
            uri = fileSystem.url.addPath(this.path),
        )
        user?.apply(r)
        r.addHeader("Destination", destinationUrl.toString())
        if (overwrite) {
            r.addHeader("Overwrite", "T")
        }
        val responseCode = r.getResponse().use {
            val responseCode = it.responseCode
            it.readText().use { it.readText() }
            responseCode
        }
        if (responseCode == 404) {
            throw FileSystem.FileNotFoundException(this.path)
        }
        if (responseCode != 201 && responseCode != 204) {
            throw TODO("Invalid response code $responseCode")
        }

        return WebdavEntity(
            path = path,
            lastModified = lastModified,
            user = user,
            length = length,
            isFile = true,
            fileSystem = fileSystem,
            quotaAvailableBytes = null,
            quotaUsedBytes = null,
        )
    }

    override suspend fun move(path: Path, overwrite: Boolean): FileSystem.Entity {
        val destinationUrl = fileSystem.url.addPath(path)
        val r = fileSystem.client.connect(
            HTTPMethod.MOVE.code,
            fileSystem.url.addPath(this.path),
        )
        user?.apply(r)
        r.addHeader("Destination", destinationUrl.toString())
        if (overwrite) {
            r.addHeader("Overwrite", "T")
        }
        val responseCode = r.getResponse().use {
            val r = it.responseCode
            it.readData().use { it.skipAll() }
            r
        }
        if (responseCode == 401) {
            throw FileSystemAccess.AccessException.UnauthorizedException()
        }
        if (responseCode == 403) {
            throw FileSystemAccess.AccessException.ForbiddenException()
        }
        if (responseCode == 404) {
            throw FileSystem.FileNotFoundException(path)
        }
        if (responseCode != 201 && responseCode != 204)
            throw TODO("Invalid response code $responseCode")

        return WebdavEntity(
            path = path,
            lastModified = lastModified,
            user = user,
            length = length,
            isFile = true,
            fileSystem = fileSystem,
            quotaAvailableBytes = null,
            quotaUsedBytes = null,
        )
    }

    override suspend fun delete() {
        val r = fileSystem.client.connect(
            HTTPMethod.DELETE.code,
            fileSystem.url.addPath(this.path),
        )
        user?.apply(r)
        val responseCode = r.getResponse().use {
            it.responseCode
        }

        if (responseCode == 404) {
            throw FileSystem.FileNotFoundException(path)
        }
        if (responseCode == 401) {
            throw FileSystemAccess.AccessException.UnauthorizedException()
        }
        if (responseCode == 403) {
            throw FileSystemAccess.AccessException.ForbiddenException()
        }
        if (responseCode != 201 && responseCode != 204) {
            throw TODO("Invalid response code $responseCode")
        }
    }

    override suspend fun rewrite(): AsyncOutput {
        val allPathUrl = fileSystem.url.addPath(path)
        val r = fileSystem.client.connect(HTTPMethod.PUT.code, allPathUrl)
//            r.addHeader("Overwrite", "T")
        user?.apply(r)
        val upload = r.writeData()
        return object : AsyncOutput {
            override suspend fun write(data: ByteBuffer): Int = upload.write(data)

            override suspend fun flush() {
                upload.flush()
            }

            override suspend fun asyncClose() {
                upload.asyncClose()
            }
        }
    }

    override fun toString(): String {
        return "RemoteEntity(length=$length, lastModified=$lastModified, path='$path', isFile=$isFile)"
    }
}
