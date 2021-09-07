package pw.binom.webdav.client

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.date.Date
import pw.binom.io.*
import pw.binom.io.http.HTTPMethod
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.addHeader
import pw.binom.net.URI
import pw.binom.skipAll
import pw.binom.webdav.DAV_NS
import pw.binom.webdav.DEPTH_HEADER
import pw.binom.webdav.MULTISTATUS_TAG
import pw.binom.webdav.WebAuthAccess
import pw.binom.xml.dom.xmlTree
import kotlin.coroutines.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
open class WebDavClient constructor(val client: HttpClient, val url: URI, val timeout: Duration? = null) :
    FileSystem {

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
                }
            )
        }
    }

    override suspend fun mkdir(path: String): FileSystem.Entity? {

        val allPathUrl = url.appendPath(path, encode = true)
        val r = client.connect(HTTPMethod.MKCOL.code, allPathUrl, timeout = timeout)
        WebAuthAccess.getCurrentUser()?.apply(r)
        val responseCode = r.getResponse().use { it.responseCode }
        if (responseCode == 405) {
            return null
        }
        if (responseCode != 201) {
            TODO("Invalid response code $responseCode")
        }
        val ss = path.split('/')
        return WebdavEntity(
            user = WebAuthAccess.getCurrentUser(),
            lastModified = Date.nowTime,
            path = ss.subList(0, ss.lastIndex - 1).joinToString("/"),
            isFile = false,
            length = 0,
            fileSystem = this
        )
    }

    override suspend fun getDir(path: String): Sequence<FileSystem.Entity>? =
        getDir(user = WebAuthAccess.getCurrentUser(), path = path, depth = 1, excludeCurrent = true)

    suspend fun getDir(
        user: WebAuthAccess?,
        path: String,
        depth: Int,
        excludeCurrent: Boolean
    ): Sequence<WebdavEntity>? {
        val allPathUrl = url.appendPath(path, encode = true)
        val r = client.connect(HTTPMethod.PROPFIND.code, allPathUrl, timeout = timeout)
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
        val reader=resp.readText().use { it.xmlTree() }
        resp.asyncClose()
        if (reader.tag != MULTISTATUS_TAG || reader.nameSpace != DAV_NS) {
            throw IllegalStateException("Invalid response. Except $MULTISTATUS_TAG response. Got $reader")
        }

        val currentDir = PropResponse(reader.childs.minByOrNull { PropResponse(it).href.length }!!)
        val currentDirHref = currentDir.href
        val realCurrentUrl = path.removePrefix(currentDirHref) + "/"
        return reader.childs.mapNotNull {
            if (excludeCurrent && it === currentDir.element) {
                return@mapNotNull null
            }
            val prop = PropResponse(it)
            val realPath =
                if (it === currentDir.element) path else realCurrentUrl + prop.href.removePrefix(currentDirHref)
            WebdavEntity(
                length = prop.props.length ?: 0L,
                lastModified = prop.props.getLastModified?.time ?: 0L,
                path = realPath,
                user = user,
                isFile = !prop.props.isDirection,
                fileSystem = this,
            )
        }.asSequence()
    }

    override suspend fun get(path: String): WebdavEntity? {
        return getDir(
            user = WebAuthAccess.getCurrentUser(),
            path = path,
            depth = 0,
            excludeCurrent = false
        )?.firstOrNull()
    }

    override suspend fun new(path: String): AsyncOutput {
        val allPathUrl = url.appendPath(path, encode = true)
        val r = client.connect(HTTPMethod.PUT.code, allPathUrl, timeout = timeout)
        WebAuthAccess.getCurrentUser()?.apply(r)
        val upload = r.writeData()
        return object : AsyncOutput {
            override suspend fun write(data: ByteBuffer): Int = upload.write(data)

            override suspend fun flush() {
                upload.flush()
            }

            override suspend fun asyncClose() {
                upload.flush()
                val response = upload.getResponse()
                response.readData().use {
                    it.skipAll()
                }
                response.asyncClose()
            }
        }
    }
}