package pw.binom.webdav.server

import pw.binom.ByteBuffer
import pw.binom.URL
import pw.binom.copyTo
import pw.binom.date.Date
import pw.binom.io.*
import pw.binom.io.http.Headers
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.pool.ObjectPool
import pw.binom.xml.dom.findElements
import pw.binom.xml.dom.xml
import pw.binom.xml.dom.xmlTree

private fun urlEncode(url: String) =
        url.splitToSequence("/").map { UTF8.urlEncode(it) }.joinToString("/")

private fun urlDecode(url: String) =
        url.splitToSequence("/").map { UTF8.urlDecode(it) }.joinToString("/")

suspend fun <U> FileSystem<U>.getD(user: U, path: String, d: Int, out: ArrayList<FileSystem.Entity<U>>) {
    if (d <= 0)
        return
    getDir(user, path)?.forEach {
        out.add(it)
        if (!it.isFile)
            getD(user, it.path, d - 1, out)
    }
}

suspend fun <U> FileSystem<U>.getEntitiesWithDepth(user: U, path: String, depth: Int): List<FileSystem.Entity<U>>? {
    val out = ArrayList<FileSystem.Entity<U>>()
    val e = get(user, path.removeSuffix("/")) ?: return null
    out += e


    if (!e.isFile)
        getD(user, path, depth, out)

    return out
}

abstract class AbstractWebDavHandler<U> : Handler {

    protected abstract val bufferPool: ObjectPool<ByteBuffer>

    protected abstract fun getFS(req: HttpRequest, resp: HttpResponse): FileSystem<U>
    protected abstract fun getUser(req: HttpRequest, resp: HttpResponse): U

    protected abstract fun getGlobalURI(req: HttpRequest): String
    protected abstract fun getLocalURI(req: HttpRequest, globalURI: String): String

    private suspend fun buildRropFind(req: HttpRequest, resp: HttpResponse) {
        val fs = getFS(req, resp)
        val user = getUser(req, resp)
        val currentEntry = fs.get(user, urlDecode(req.contextUri))

        if (currentEntry == null) {
            resp.status = 404
            return
        }

        val node = req.input.utf8Reader().xmlTree()!!

        val properties =
                node.findElements { it.tag.endsWith("prop") }.first().childs
                        .asSequence()
                        .map {
                            it.nameSpace?.url to it.tag
                        }.toMutableSet()
        val depth = req.headers["Depth"]?.firstOrNull()?.toInt() ?: 0
        val entities = fs.getEntitiesWithDepth(user, urlDecode(req.contextUri), depth)!!//if (depth <= 0) listOf(currentEntry) else fs.getEntities(user, req.contextUri)!! + currentEntry
        val DAV_NS = "DAV:"
        resp.status = 207
        resp.resetHeader(Headers.CONTENT_TYPE, "application/xml")
        xml(resp.complete().utf8Appendable()) {
            node("multistatus", DAV_NS) {
                entities.forEach { e ->
                    node("response", DAV_NS) {
                        node("href", DAV_NS) {
                            if (e.isFile)
                                value(urlEncode("${getGlobalURI(req)}${e.path}"))
                            else
                                value(urlEncode("${getGlobalURI(req)}${e.path}/"))
                        }
                        node("propstat", DAV_NS) {
                            node("prop", DAV_NS) {
                                properties.forEach { prop ->
                                    when {
                                        prop.first == DAV_NS && prop.second == "displayname" -> node("displayname", DAV_NS) {
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
                                    fs.get(user, e.path)
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

    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        val user = getUser(req, resp)
        val fs = getFS(req, resp)
        try {
            //resp.resetHeader("Connection", "close")
            if (req.method == "OPTIONS") {
                fs.get(user, urlDecode(req.contextUri))
                resp.resetHeader("Allow", "GET, POST, OPTIONS, HEAD, MKCOL, PUT, PROPFIND, PROPPATCH, ORDERPATCH, DELETE, MOVE, COPY, GETLIB, LOCK, UNLOCK")
                resp.resetHeader("DAV", "1, 2, ordered-collections")
                resp.status = 200
                return
            }
            if (req.method == "MKCOL") {
                fs.mkdir(user, urlDecode(req.contextUri))
                resp.status = 201
                return
            }
            if (req.method == "MOVE") {
                val destination = req.headers["Destination"]?.firstOrNull()?.let { URL(it) }

                if (destination == null) {
                    resp.status = 406
                    return
                }
                val destinationPath = getLocalURI(req, destination.uri).let { urlDecode(it) }
                val overwrite = req.headers["Overwrite"]?.firstOrNull()?.let { it == "T" } ?: true
                val source = fs.get(user, urlDecode(req.contextUri))
                if (source == null) {
                    resp.status = 404
                    return
                }

                source.move(destinationPath, overwrite)
                resp.status = 201
                return
            }
            if (req.method == "COPY") {
                val destination = req.headers["Destination"]?.firstOrNull()?.let { URL(it) }

                if (destination == null) {
                    resp.status = 406
                    return
                }
                val destinationPath = getLocalURI(req, destination.uri).let { urlDecode(it) }
                val overwrite = req.headers["Overwrite"]?.firstOrNull()?.let { it == "T" } ?: true
                val source = fs.get(user, urlDecode(req.contextUri))
                if (source == null) {
                    resp.status = 404
                    return
                }

                source.copy(destinationPath, overwrite)
                resp.status = 201
                return
            }
            if (req.method == "DELETE") {
                val e = fs.get(user, urlDecode(req.contextUri))
                if (e == null) {
                    resp.status = 404
                    return
                }
                e.delete()
                resp.status = 200
                return
            }
            if (req.method == "PUT") {
                val path = urlDecode(req.contextUri)
                val e = fs.get(user, path)?.rewrite() ?: fs.new(user, path)

                e.use {
                    req.input.copyTo(it, bufferPool)
                }
                resp.status = 201
                return
            }
            if (req.method == "GET") {
                val e = fs.get(user, urlDecode(req.contextUri).removeSuffix("/"))

                if (e == null) {
                    resp.status = 404
                    return
                }
                val stream = e.read()!!
                resp.status = 200
                resp.resetHeader(Headers.CONTENT_LENGTH, e.length.toString())
                val out = resp.complete()
                stream.use {
                    it.copyTo(out, bufferPool)
                }
                out.flush()
                return
            }
            if (req.method == "PROPFIND") {
                buildRropFind(req, resp)
                return
            }

            if (req.method == "HEAD") {
                resp.status = 405
                return
            }
            resp.status = 404
        } catch (e: FileSystemAccess.AccessException.ForbiddenException) {
            resp.status = 403
        } catch (e: FileSystemAccess.AccessException.UnauthorizedException) {
            resp.status = 401
            resp.resetHeader("WWW-Authenticate", "Basic realm=\"ownCloud\"")
            return
        } catch (e: FileSystem.EntityExistException) {
            resp.status = 409
        } catch (e: FileSystem.FileNotFoundException) {
            resp.status = 404
        }
    }

}