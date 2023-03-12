package pw.binom.webdav.server

import pw.binom.ByteBufferPool
import pw.binom.copyTo
import pw.binom.date.DateTime
import pw.binom.io.*
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.http.headersOf
import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.url.Path
import pw.binom.url.UrlEncoder
import pw.binom.url.toPath
import pw.binom.url.toURL
import pw.binom.webdav.DAV_NS
import pw.binom.webdav.MULTISTATUS_TAG
import pw.binom.xml.dom.findElements
import pw.binom.xml.dom.writeXml
import pw.binom.xml.dom.xmlTree

private suspend fun FileSystem.getD(path: Path, d: Int, out: ArrayList<FileSystem.Entity>) {
    if (d <= 0) {
        return
    }
    getDir(path)?.forEach {
        out.add(it)
        if (!it.isFile) {
            getD(it.path, d - 1, out)
        }
    }
}

suspend fun FileSystem.getEntitiesWithDepth(path: Path, depth: Int): List<FileSystem.Entity>? {
    val out = ArrayList<FileSystem.Entity>()
    val e = get(path.toString().removeSuffix("/").toPath) ?: return null
    out += e

    if (!e.isFile) {
        getD(path, depth, out)
    }

    return out
}

abstract class AbstractWebDavHandler<U> : HttpHandler {

    protected abstract val bufferPool: ByteBufferPool

    protected abstract fun getFS(req: HttpServerExchange): FileSystem
    protected abstract fun getUser(req: HttpServerExchange): U

    protected abstract fun getGlobalURI(req: HttpServerExchange): Path
    protected abstract fun getLocalURI(req: HttpServerExchange, globalURI: Path): String

    private suspend fun buildRropFind(req: HttpServerExchange) {
        val fs = getFS(req)
        val user = getUser(req)
        val currentEntry = fs.get(UrlEncoder.pathDecode(req.requestURI.path.raw).toPath)

        if (currentEntry == null) {
            req.startResponse(404)
            return
        }
        val node = req.input.bufferedReader().use { it.xmlTree() }

        val properties =
            node.findElements { it.tag.endsWith("prop") }.first().childs
                .asSequence()
                .map {
                    it.nameSpace to it.tag
                }.toMutableSet()
        val depth = req.requestHeaders["Depth"]?.firstOrNull()?.toInt() ?: 0
        val entities = fs.getEntitiesWithDepth(
            UrlEncoder.pathDecode(req.requestURI.path.raw).toPath,
            depth,
        )!! // if (depth <= 0) listOf(currentEntry) else fs.getEntities(user, req.contextUri)!! + currentEntry
        req.startResponse(207, headersOf(Headers.CONTENT_TYPE to "application/xml; charset=UTF-8"))
        req.output.bufferedWriter().use {
            it.writeXml("UTF-8") {
                node(MULTISTATUS_TAG, DAV_NS) {
                    entities.forEach { e ->
                        node("response", DAV_NS) {
                            node("href", DAV_NS) {
                                if (e.isFile) {
                                    value(getGlobalURI(req).append(e.path).raw)
                                } else {
                                    value(getGlobalURI(req).append(UrlEncoder.pathEncode(e.path.toString() + "/")).raw)
                                }
                            }
                            node("propstat", DAV_NS) {
                                node("prop", DAV_NS) {
                                    properties.forEach { prop ->
                                        when {
                                            prop.first == DAV_NS && prop.second == "displayname" -> node(
                                                "displayname",
                                                DAV_NS,
                                            ) {
                                                // value(e.name)
                                            }

                                            prop.first == DAV_NS && prop.second == "getlastmodified" ->
                                                node("getlastmodified", DAV_NS) {
                                                    value(DateTime(e.lastModified).toUTC().asString())
                                                }

                                            prop.first == DAV_NS && prop.second == "getcontentlength" ->
                                                node("getcontentlength", DAV_NS) {
                                                    if (e.isFile) {
                                                        value(e.length.toString())
                                                    } else {
                                                        value("0")
                                                    }
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

    override suspend fun handle(exchange: HttpServerExchange) {
        val user = getUser(exchange)

        val fs = getFS(exchange)
        try {
            // resp.resetHeader("Connection", "close")
            if (exchange.requestMethod == "OPTIONS") {
                fs.useUser2(user) {
                    fs.get(UrlEncoder.pathDecode(exchange.requestURI.path.raw).toPath)
                    val headers = HashHeaders2()
                    headers["Allow"] =
                        "GET, POST, OPTIONS, HEAD, MKCOL, PUT, PROPFIND, PROPPATCH, ORDERPATCH, DELETE, MOVE, COPY, GETLIB, LOCK, UNLOCK"
                    headers["DAV"] = "1, 2, ordered-collections"
                    exchange.startResponse(200, headers)
                }
                return
            }
            if (exchange.requestMethod == "MKCOL") {
                fs.useUser2(user) {
                    fs.mkdir(UrlEncoder.pathDecode(exchange.requestURI.path.raw).toPath)
                    exchange.startResponse(201)
                }
                return
            }
            if (exchange.requestMethod == "MOVE") {
                fs.useUser2(user) {
                    val destination = exchange.requestHeaders["Destination"]?.firstOrNull()?.let { it.toURL() }

                    if (destination == null) {
                        exchange.startResponse(406)
                        return@useUser2
                    }
                    val destinationPath = getLocalURI(exchange, destination.path).let { UrlEncoder.pathDecode(it) }
                    val overwrite = exchange.requestHeaders["Overwrite"]?.firstOrNull()?.let { it == "T" } ?: true
                    val source = fs.get(UrlEncoder.pathDecode(exchange.requestURI.path.raw).toPath)
                    if (source == null) {
                        exchange.startResponse(404)
                        return@useUser2
                    }

                    source.move(destinationPath.toPath, overwrite)
                    exchange.startResponse(201)
                }
                return
            }
            if (exchange.requestMethod == "COPY") {
                fs.useUser2(user) {
                    val destination = exchange.requestHeaders["Destination"]?.firstOrNull()?.let { it.toURL() }

                    if (destination == null) {
                        exchange.startResponse(406)
                        return@useUser2
                    }
                    val destinationPath = getLocalURI(exchange, destination.path).let { UrlEncoder.pathDecode(it) }
                    val overwrite = exchange.requestHeaders["Overwrite"]?.firstOrNull()?.let { it == "T" } ?: true
                    val source = fs.get(UrlEncoder.pathDecode(exchange.requestURI.path.raw).toPath)
                    if (source == null) {
                        exchange.startResponse(404)
                        return@useUser2
                    }

                    source.copy(destinationPath.toPath, overwrite)
                    exchange.startResponse(201)
                }
                return
            }
            if (exchange.requestMethod == "DELETE") {
                fs.useUser2(user) {
                    val e = fs.get(UrlEncoder.pathDecode(exchange.requestURI.path.raw).toPath)
                    if (e == null) {
                        exchange.startResponse(404)
                        return@useUser2
                    }
                    e.delete()
                    exchange.startResponse(200)
                }
                return
            }
            if (exchange.requestMethod == "PUT") {
                fs.useUser2(user) {
                    val path = UrlEncoder.pathDecode(exchange.requestURI.path.raw).toPath
                    val e = fs.get(path)?.rewrite() ?: fs.new(path)

                    e.use {
                        exchange.input.use { b ->
                            b.copyTo(dest = it, pool = bufferPool)
                        }
                    }
                    exchange.startResponse(201)
                }
                return
            }
            if (exchange.requestMethod == "GET") {
                fs.useUser2(user) {
                    val e = fs.get(UrlEncoder.pathDecode(exchange.requestURI.path.raw).removeSuffix("/").toPath)

                    if (e == null) {
                        exchange.startResponse(404)
                        return@useUser2
                    }
                    val stream = e.read()!!
                    exchange.startResponse(200, headersOf(Headers.CONTENT_LENGTH to e.length.toULong().toString()))
                    exchange.output.use { b ->
                        stream.use {
                            it.copyTo(b, bufferPool)
                        }
                    }
                }
                return
            }
            if (exchange.requestMethod == "PROPFIND") {
                buildRropFind(exchange)
                return
            }

            if (exchange.requestMethod == "HEAD") {
                exchange.startResponse(405)
                return
            }
            exchange.startResponse(404)
        } catch (e: FileSystemAccess.AccessException.ForbiddenException) {
            exchange.startResponse(403)
        } catch (e: FileSystemAccess.AccessException.UnauthorizedException) {
            exchange.startResponse(401, headersOf("WWW-Authenticate" to "Basic realm=\"ownCloud\""))
            return
        } catch (e: FileSystem.EntityExistException) {
            exchange.startResponse(409)
        } catch (e: FileSystem.FileNotFoundException) {
            exchange.startResponse(404)
        }
    }
}

private suspend fun <T> FileSystem.useUser2(user: Any?, func: suspend () -> T): T =
    if (isSupportUserSystem && user != null) {
        useUser(user, func)
    } else {
        func()
    }
