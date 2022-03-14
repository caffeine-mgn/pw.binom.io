package pw.binom.webdav.server

import pw.binom.ByteBuffer
import pw.binom.copyTo
import pw.binom.date.Date
import pw.binom.io.*
import pw.binom.io.httpServer.*
import pw.binom.net.Path
import pw.binom.net.toPath
import pw.binom.net.toURL
import pw.binom.pool.ObjectPool
import pw.binom.webdav.DAV_NS
import pw.binom.webdav.MULTISTATUS_TAG
import pw.binom.xml.dom.findElements
import pw.binom.xml.dom.writeXml
import pw.binom.xml.dom.xmlTree

private suspend fun FileSystem.getD(path: Path, d: Int, out: ArrayList<FileSystem.Entity>) {
    if (d <= 0)
        return
    getDir(path)?.forEach {
        out.add(it)
        if (!it.isFile)
            getD(it.path, d - 1, out)
    }
}

suspend fun FileSystem.getEntitiesWithDepth(path: Path, depth: Int): List<FileSystem.Entity>? {
    val out = ArrayList<FileSystem.Entity>()
    val e = get(path.toString().removeSuffix("/").toPath) ?: return null
    out += e


    if (!e.isFile)
        getD(path, depth, out)

    return out
}

abstract class AbstractWebDavHandler<U> : Handler {

    protected abstract val bufferPool: ObjectPool<ByteBuffer>

    protected abstract fun getFS(req: HttpRequest): FileSystem
    protected abstract fun getUser(req: HttpRequest): U

    protected abstract fun getGlobalURI(req: HttpRequest): Path
    protected abstract fun getLocalURI(req: HttpRequest, globalURI: Path): String

    private suspend fun buildRropFind(req: HttpRequest) {
        val fs = getFS(req)
        val user = getUser(req)
        val currentEntry = fs.get(UTF8.urlDecode(req.path.raw).toPath)

        if (currentEntry == null) {
            req.response().use { it.status = 404 }
            return
        }
        val node = req.readText().use { it.xmlTree()!! }
        val resp = req.response()

        val properties =
            node.findElements { it.tag.endsWith("prop") }.first().childs
                .asSequence()
                .map {
                    it.nameSpace to it.tag
                }.toMutableSet()
        val depth = req.headers["Depth"]?.firstOrNull()?.toInt() ?: 0
        val entities = fs.getEntitiesWithDepth(
            UTF8.urlDecode(req.path.raw).toPath,
            depth
        )!!//if (depth <= 0) listOf(currentEntry) else fs.getEntities(user, req.contextUri)!! + currentEntry
        resp.status = 207
        resp.headers.contentType = "application/xml; charset=UTF-8"
        resp.startWriteText().use {
            it.writeXml {
                node(MULTISTATUS_TAG, DAV_NS) {
                    entities.forEach { e ->
                        node("response", DAV_NS) {
                            node("href", DAV_NS) {

                                if (e.isFile)
                                    value(getGlobalURI(req).append(e.path).raw)
                                else
                                    value(getGlobalURI(req).append(UTF8.urlEncode(e.path.toString() + "/")).raw)
                            }
                            node("propstat", DAV_NS) {
                                node("prop", DAV_NS) {
                                    properties.forEach { prop ->
                                        when {
                                            prop.first == DAV_NS && prop.second == "displayname" -> node(
                                                "displayname",
                                                DAV_NS
                                            ) {
                                                //value(e.name)
                                            }
                                            prop.first == DAV_NS && prop.second == "getlastmodified" ->
                                                node("getlastmodified", DAV_NS) {
                                                    value(Date(e.lastModified).toUTC().asString())
                                                }
                                            prop.first == DAV_NS && prop.second == "getcontentlength" ->
                                                node("getcontentlength", DAV_NS) {
                                                    if (e.isFile)
                                                        value(e.length.toString())
                                                    else
                                                        value("0")
                                                }
                                            prop.first == DAV_NS && prop.second == "resourcetype" -> {
                                                if (e.isFile) {
                                                    node("resourcetype", DAV_NS)
                                                } else {
                                                    node("resourcetype", DAV_NS) {
                                                        node("collection", DAV_NS)
                                                    }
                                                }
                                            }
                                            else -> node(prop.second, prop.first)
                                        }
                                    }
                                }
                                node("status", DAV_NS) {
                                    try {
                                        fs.get(e.path)
                                        value("HTTP/1.1 200 OK")
                                    } catch (e: Throwable) {
                                        value("HTTP/1.1 403 Forbidden")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun request(req: HttpRequest) {
        val user = getUser(req)

        val fs = getFS(req)
        try {
            //resp.resetHeader("Connection", "close")
            if (req.method == "OPTIONS") {
                fs.useUser2(user) {
                    fs.get(UTF8.urlDecode(req.path.raw).toPath)
                    req.response().use {
                        it.headers["Allow"] =
                            "GET, POST, OPTIONS, HEAD, MKCOL, PUT, PROPFIND, PROPPATCH, ORDERPATCH, DELETE, MOVE, COPY, GETLIB, LOCK, UNLOCK"
                        it.headers["DAV"] = "1, 2, ordered-collections"
                        it.status = 200
                    }
                }
                return
            }
            if (req.method == "MKCOL") {
                fs.useUser2(user) {
                    fs.mkdir(UTF8.urlDecode(req.path.raw).toPath)
                    req.response().use {
                        it.status = 201
                    }
                }
                return
            }
            if (req.method == "MOVE") {
                fs.useUser2(user) {
                    val destination = req.headers["Destination"]?.firstOrNull()?.let { it.toURL() }

                    if (destination == null) {
                        req.response().use {
                            it.status = 406
                        }
                        return@useUser2
                    }
                    val destinationPath = getLocalURI(req, destination.path).let { UTF8.urlDecode(it) }
                    val overwrite = req.headers["Overwrite"]?.firstOrNull()?.let { it == "T" } ?: true
                    val source = fs.get(UTF8.urlDecode(req.path.raw).toPath)
                    if (source == null) {
                        req.response().use {
                            it.status = 404
                        }
                        return@useUser2
                    }

                    source.move(destinationPath.toPath, overwrite)
                    req.response().use {
                        it.status = 201
                    }
                }
                return
            }
            if (req.method == "COPY") {
                fs.useUser2(user) {
                    val destination = req.headers["Destination"]?.firstOrNull()?.let { it.toURL() }

                    if (destination == null) {
                        req.response().use {
                            it.status = 406
                        }
                        return@useUser2
                    }
                    val destinationPath = getLocalURI(req, destination.path).let { UTF8.urlDecode(it) }
                    val overwrite = req.headers["Overwrite"]?.firstOrNull()?.let { it == "T" } ?: true
                    val source = fs.get(UTF8.urlDecode(req.path.raw).toPath)
                    if (source == null) {
                        req.response().use {
                            it.status = 404
                        }
                        return@useUser2
                    }

                    source.copy(destinationPath.toPath, overwrite)
                    req.response().use {
                        it.status = 201
                    }
                }
                return
            }
            if (req.method == "DELETE") {
                fs.useUser2(user) {
                    val e = fs.get(UTF8.urlDecode(req.path.raw).toPath)
                    if (e == null) {
                        req.response().use {
                            it.status = 404
                        }
                        return@useUser2
                    }
                    e.delete()
                    req.response().use {
                        it.status = 200
                    }
                }
                return
            }
            if (req.method == "PUT") {
                fs.useUser2(user) {
                    val path = UTF8.urlDecode(req.path.raw).toPath
                    val e = fs.get(path)?.rewrite() ?: fs.new(path)

                    e.use {
                        req.readBinary().use { b ->
                            b.copyTo(it, bufferPool)
                        }
                    }
                    req.response().use {
                        it.status = 201
                    }
                }
                return
            }
            if (req.method == "GET") {
                fs.useUser2(user) {
                    val e = fs.get(UTF8.urlDecode(req.path.raw).removeSuffix("/").toPath)

                    if (e == null) {
                        req.response().use {
                            it.status = 404
                        }
                        return@useUser2
                    }
                    val stream = e.read()!!
                    req.response().use {
                        it.status = 200
                        it.headers.contentLength = e.length.toULong()
                        it.startWriteBinary().use { b ->
                            stream.use {
                                it.copyTo(b, bufferPool)
                            }
                        }
                    }
                }
                return
            }
            if (req.method == "PROPFIND") {
                buildRropFind(req)
                return
            }

            if (req.method == "HEAD") {
                req.response().use {
                    it.status = 405
                }
                return
            }
            req.response().use {
                it.status = 404
            }
        } catch (e: FileSystemAccess.AccessException.ForbiddenException) {
            req.response().use {
                it.status = 403
            }
        } catch (e: FileSystemAccess.AccessException.UnauthorizedException) {
            req.response().use {
                it.headers["WWW-Authenticate"] = "Basic realm=\"ownCloud\""
                it.status = 401
            }

            return
        } catch (e: FileSystem.EntityExistException) {
            req.response().use {
                it.status = 409
            }
        } catch (e: FileSystem.FileNotFoundException) {
            req.response().use {
                it.status = 404
            }
        }
    }

}

private suspend fun <T> FileSystem.useUser2(user: Any?, func: suspend () -> T): T =
    if (isSupportUserSystem && user != null)
        useUser(user, func)
    else
        func()