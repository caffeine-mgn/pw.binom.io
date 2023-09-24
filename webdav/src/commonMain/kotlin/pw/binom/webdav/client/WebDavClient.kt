package pw.binom.webdav.client

import pw.binom.date.DateTime
import pw.binom.io.*
import pw.binom.io.http.HTTPMethod
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.addHeader
import pw.binom.network.SocketClosedException
import pw.binom.url.Path
import pw.binom.url.URL
import pw.binom.url.toPath
import pw.binom.webdav.DAV_NS
import pw.binom.webdav.DEPTH_HEADER
import pw.binom.webdav.MULTISTATUS_TAG
import pw.binom.webdav.WebAuthAccess
import pw.binom.xml.dom.xmlTree
import kotlin.coroutines.*

open class WebDavClient constructor(val client: HttpClient, val url: URL) :
    FileSystem {
    override suspend fun getQuota(path: Path): Quota? {
        val dir = getDir(user = WebAuthAccess.getCurrentUser(), path = path, depth = 0, excludeCurrent = false)
            ?.firstOrNull() ?: return null
        return Quota(
            availableBytes = dir.quotaAvailableBytes ?: 0L,
            usedBytes = dir.quotaUsedBytes ?: 0L,
        )
    }

    override val isSupportUserSystem: Boolean
        get() = true

    /**
     * Set user for current execution of [func]
     * @param user must be instance of [WebAuthAccess]
     */
    override suspend fun <T> useUser(user: Any?, func: suspend () -> T): T {
        if (user == null) {
            return func()
        }
        require(user is WebAuthAccess)
        return suspendCoroutine { con ->
            val currentUser = con.context[WebAuthAccess.CurrentUserKey]
            if (currentUser != null) {
                con.resumeWithException(IllegalStateException("Already using User ${currentUser.user}"))
                return@suspendCoroutine
            }
            func.startCoroutine(
                object : Continuation<T> {
                    override val context: CoroutineContext = con.context + WebAuthAccess.CurrentUser(user)

                    override fun resumeWith(result: Result<T>) {
                        con.resumeWith(result)
                    }
                },
            )
        }
    }

    override suspend fun mkdir(path: Path): FileSystem.Entity? {
        val allPathUrl = url.addPath(path)
        val r = try {
            client.connect(HTTPMethod.MKCOL.code, allPathUrl)
        } catch (e: SocketClosedException) {
            throw IOException("Can't connect to $allPathUrl", e)
        }
        WebAuthAccess.getCurrentUser()?.apply(r)
        val responseCode = r.getResponse().use { it.responseCode }
        if (responseCode == 405) {
            return null
        }
        if (responseCode == 401) {
            throw FileSystemAccess.AccessException.UnauthorizedException()
        }
        if (responseCode != 201) {
            TODO("Invalid response code $responseCode")
        }
        val ss = path.toString().split('/')
        return WebdavEntity(
            user = WebAuthAccess.getCurrentUser(),
            lastModified = DateTime.nowTime,
            path = (ss.subList(0, ss.lastIndex - 1).joinToString("/")).toPath,
            isFile = false,
            length = 0,
            fileSystem = this,
            quotaUsedBytes = null,
            quotaAvailableBytes = null,
        )
    }

    override suspend fun getDir(path: Path): List<FileSystem.Entity>? =
        getDir(user = WebAuthAccess.getCurrentUser(), path = path, depth = 1, excludeCurrent = true)

    suspend fun getDir(
        user: WebAuthAccess?,
        path: Path,
        depth: Int,
        excludeCurrent: Boolean,
    ): List<WebdavEntity>? {
        val allPathUrl = url.addPath(path)
        val r = try {
            client.connect(HTTPMethod.PROPFIND.code, allPathUrl)
        } catch (e: SocketClosedException) {
            throw IOException("Can't connect to $allPathUrl")
        }
        user?.apply(r)
        r.addHeader(DEPTH_HEADER, depth.toString())
        val resp = r.getResponse()
        if (resp.responseCode == 404) {
            resp.asyncClose()
            return null
        }
        if (resp.responseCode == 401) {
            throw FileSystemAccess.AccessException.UnauthorizedException()
        }
        if (resp.responseCode == 403) {
            throw FileSystemAccess.AccessException.ForbiddenException()
        }
        if (resp.responseCode != 207 && resp.responseCode != 200) {
            throw IllegalStateException("Invalid response code ${resp.responseCode}")
        }
        val reader = resp.readText().use { it.xmlTree() }
        resp.asyncClose()
        if (reader.tag != MULTISTATUS_TAG || reader.nameSpace != DAV_NS) {
            throw IllegalStateException("Invalid response. Except $MULTISTATUS_TAG response. Got $reader")
        }

        val currentDir = PropResponse(reader.childs.minByOrNull { PropResponse(it).href.length }!!)
        val currentDirHref = currentDir.href
        val realCurrentUrl = (path.toString().removePrefix(currentDirHref) + "/").toPath
        return reader.childs.mapNotNull {
            if (excludeCurrent && it === currentDir.element) {
                return@mapNotNull null
            }
            val prop = PropResponse(it)
            val realPath =
                if (it === currentDir.element) {
                    path
                } else {
                    (realCurrentUrl.toString() + prop.href.removePrefix(currentDirHref)).toPath
                }
            WebdavEntity(
                length = prop.props.length ?: 0L,
                lastModified = prop.props.getLastModified?.time ?: 0L,
                path = realPath,
                user = user,
                isFile = !prop.props.isDirection,
                fileSystem = this,
                quotaAvailableBytes = currentDir.props.quotaAvailableBytes,
                quotaUsedBytes = currentDir.props.quotaUsedBytes,
            )
        }
    }

    override suspend fun get(path: Path): WebdavEntity? {
        val folderName = path.name
        return getDir(
            user = WebAuthAccess.getCurrentUser(),
            path = path,
            depth = 0,
            excludeCurrent = false,
        )?.find { it.name == folderName }
    }

    override suspend fun new(path: Path): AsyncOutput {
        val allPathUrl = url.addPath(path)
        val r = client.connect(HTTPMethod.PUT.code, allPathUrl)
        WebAuthAccess.getCurrentUser()?.apply(r)
        val upload = r.writeBinary()
        return object : AsyncOutput {
            override suspend fun write(data: ByteBuffer): Int = upload.write(data)

            override suspend fun flush() {
                upload.flush()
            }

            override suspend fun asyncClose() {
                upload.flush()
                val response = upload.getResponse()
                response.readBinary().use {
                    it.skipAll()
                }
                response.asyncClose()
            }
        }
    }

    override suspend fun new(path: Path, writeAction: suspend (AsyncOutput) -> Unit): FileSystem.Entity {
        var size = 0L
        new(path).use { fout ->
            val foutWithCounter = fout.withCounter()
            writeAction(foutWithCounter)
            size = foutWithCounter.writedBytes
        }
        return WebdavEntity(
            length = size,
            lastModified = DateTime.nowTime,
            path = path,
            user = WebAuthAccess.getCurrentUser(),
            isFile = true,
            fileSystem = this,
            quotaUsedBytes = null,
            quotaAvailableBytes = null,
        )
    }
}
