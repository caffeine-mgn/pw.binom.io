package pw.binom.webdav.server

import pw.binom.Date
import pw.binom.URL
import pw.binom.asUTF8String
import pw.binom.io.*
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.xml.dom.findElements
import pw.binom.xml.dom.xml
import pw.binom.xml.dom.xmlTree

abstract class AbstractWebDavHandler<U> : Handler {

    protected abstract fun getFS(req: HttpRequest, resp: HttpResponse): FileSystem<U>
    protected abstract fun getUser(req: HttpRequest, resp: HttpResponse): U

    protected abstract fun getGlobalURI(req: HttpRequest): String
    protected abstract fun getLocalURI(req: HttpRequest, globalURI: String): String

    private suspend fun buildRropFind(req: HttpRequest, resp: HttpResponse) {
        val fs = getFS(req, resp)
        val user = getUser(req, resp)
        println("Getting ${req.contextUri}")
        val currentEntry = fs.getEntry(user, req.contextUri)

        if (currentEntry == null) {
            resp.status = 404
            return
        }

        val o = ByteArrayOutputStream()
        req.input.copyTo(o)

        val node = o.toByteArray().asUTF8String().asReader().asAsync().xmlTree()!!

        val properties =
                node.findElements { it.tag.endsWith("prop") }.first().childs
                        .asSequence()
                        .map {
                            it.nameSpace?.url to it.tag
                        }.toMutableSet()
        val depth = req.headers["Depth"]?.firstOrNull()?.toInt() ?: 0
        println("Depth=${req.headers["Depth"]?.firstOrNull()}   depth=$depth")
        val entities = if (depth <= 0) listOf(currentEntry) else fs.getEntities(user, req.contextUri)!! + currentEntry
        val DAV_NS = "DAV:"

        val ss = StringBuilder()
        xml(ss.asAsync()) {
            node("multistatus", DAV_NS) {
                entities.forEach { e ->
                    node("response", DAV_NS) {
                        node("href", DAV_NS) {
                            value("${getGlobalURI(req)}${e.path.removePrefix("/")}")
                        }
                        node("propstat", DAV_NS) {
                            node("prop", DAV_NS) {
                                properties.forEach { prop ->
                                    when {
                                        prop.first == DAV_NS && prop.second == "displayname" -> node("displayname", DAV_NS) {
                                            value(e.name)
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
                                value("HTTP/1.1 200 OK")
                            }
                        }
                    }
                }
            }
        }

        resp.status = 207
        resp.resetHeader("Content-Length", ss.length.toString())
        resp.resetHeader("Content-Type", "application/xml")
        AsyncAppendableUTF8(resp.output).append(ss.toString())
        return

    }

    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        val user = getUser(req, resp)
        val fs = getFS(req, resp)
        try {
            //resp.resetHeader("Connection", "close")
            if (req.method == "OPTIONS") {
                println("Check ${req.contextUri}")
                fs.getEntry(user, req.contextUri)
                resp.resetHeader("Allow", "GET, POST, OPTIONS, HEAD, MKCOL, PUT, PROPFIND, PROPPATCH, ORDERPATCH, DELETE, MOVE, COPY, GETLIB, LOCK, UNLOCK")
                resp.resetHeader("DAV", "1, 2, ordered-collections")
                resp.status = 200
                return
            }
            if (req.method == "MKCOL") {
                fs.mkdir(user, req.contextUri)
                resp.status = 201
                return
            }
            if (req.method == "COPY") {
                val destination = req.headers["Destination"]?.firstOrNull()?.let { URL(it) }

                if (destination == null) {
                    resp.status = 406
                    return
                }
                val destinationPath = getLocalURI(req, destination.uri)
                val overwrite = req.headers["Overwrite"]?.firstOrNull()?.let { it == "T" } ?: true
                val source = fs.getEntry(user, req.contextUri)
                if (source == null) {
                    resp.status = 404
                    return
                }
                if (!overwrite && fs.getEntry(user, destinationPath) != null) {
                    resp.status = 409
                    return
                }


                fs.rewriteFile(user, destinationPath).use { d ->
                    fs.read(user, source.path)!!.use { s ->
                        s.copyTo(d)
                    }
                }
                resp.status = 201
                return
            }
            if (req.method == "DELETE") {
                fs.delete(user, req.contextUri)
                resp.status = 200
                return
            }
            if (req.method == "PUT") {
                fs.rewriteFile(user, req.contextUri).use {
                    req.input.copyTo(it)
                }
                resp.status = 201
                return
            }
            if (req.method == "GET") {
                val e = fs.getEntry(user, req.contextUri)
                val stream = fs.read(user, req.contextUri)
                if (e == null || stream == null) {
                    resp.status = 404
                    return
                }
                resp.status = 200
                resp.resetHeader("Content-Length", e.length.toString())
                stream.use {
                    it.copyTo(resp.output)
                }
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
            println("ForbiddenException")
        } catch (e: FileSystemAccess.AccessException.UnauthorizedException) {
            resp.status = 401
            resp.resetHeader("WWW-Authenticate", "Basic realm=\"ownCloud\"")
            println("UnauthorizedException")
            return
        } catch (e: Throwable) {
            println("ERROR: $e")
        }
    }

}