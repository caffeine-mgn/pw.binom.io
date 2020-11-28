package pw.binom.webdav.client

import pw.binom.*
import pw.binom.webdav.*
import pw.binom.date.Date
import pw.binom.io.*
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.webdav.server.parseDate
import pw.binom.xml.dom.XmlElement
import pw.binom.xml.dom.findElements
import pw.binom.xml.dom.xmlTree
import kotlin.coroutines.*

open class WebDavClient(val client: AsyncHttpClient, val url: URL) : FileSystem {

    private fun XmlElement.findTag(name: String) =
        findElements { it.nameSpace?.url == "DAV:" && it.tag == name }

    override val isSupportUserSystem: Boolean
        get() = true

    override suspend fun <T> useUser(user: Any, func: suspend () -> T): T {
        require(user is WebAuthAccess)
        return suspendCoroutine { con ->
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
        val allPathUrl = url.newURI("${url.uri}$path")
        val r = client.request("MKCOL", allPathUrl)
        WebAuthAccess.getCurrentUser()?.apply(r)
        val resp = r.response()
        if (resp.responseCode == 405)
            return null
        if (resp.responseCode != 201)
            TODO("Invalid response code ${resp.responseCode}")
        val ss = path.split('/')
        return RemoteEntity(
            user = WebAuthAccess.getCurrentUser(),
            lastModified = Date.now,
            path = ss.subList(0, ss.lastIndex - 1).joinToString("/"),
            isFile = false,
            length = 0
        )
    }

    /*
        private inner class RemoteFolder(override val name: String, override val lastModified: Long, override val path: String, override val user: T) : FileSystem.Entity<T> {
            override val length: Long
                get() = 0
            override val isFile: Boolean
                get() = false
            override val fileSystem: FileSystem<T>
                get() = this@Client

            override suspend fun read(): AsyncInputStream? = null

            override suspend fun move(path: String): FileSystem.Entity<T> {
                TODO("Not yet implemented")
            }

            override suspend fun delete() {
                TODO("Not yet implemented")
            }

            override suspend fun rewrite(): AsyncOutputStream {
                TODO("Not yet implemented")
            }

            override suspend fun copy(path: String, overwrite: Boolean): FileSystem.Entity<T> {
                TODO("Not yet implemented")
            }

        }
    */
    private inner class RemoteEntity(
        override val length: Long,
        override val lastModified: Long,
        override val path: String,
        val user: WebAuthAccess?,
        override val isFile: Boolean
    ) : FileSystem.Entity {
        override val fileSystem: WebDavClient
            get() = this@WebDavClient

        override suspend fun read(offset: ULong, length: ULong?): AsyncInput? {
            val allPathUrl = url.newURI("${url.uri}$path/$name")
            val r = client.request("GET", allPathUrl)
            if (offset != 0uL) {
                if (length == null) {
                    r.addHeader("Range", "bytes=$offset-")
                } else {
                    r.addHeader("Range", "bytes=$offset-${offset + length}")
                }
            }
            user?.apply(r)
            val resp = r.response()
            if (resp.responseCode == 404)
                return null

            return object : AsyncInput {
                override val available: Int
                    get() = resp.available

                override suspend fun read(dest: ByteBuffer): Int = resp.read(dest)

                override suspend fun close() {
                    resp.close()
                }
            }
        }

        override suspend fun copy(path: String, overwrite: Boolean): FileSystem.Entity {
            val destinationUrl = url.appendDirectionURI(path)
            val r = client.request("COPY", url.appendDirectionURI(this.path).appendDirectionURI(name))
            user?.apply(r)
            r.addHeader("Destination", destinationUrl.toString())
            if (overwrite)
                r.addHeader("Overwrite", "T")
            val resp = r.response()
            if (resp.responseCode == 404)
                throw FileSystem.FileNotFoundException(this.path)
            if (resp.responseCode != 201 && resp.responseCode != 204)
                throw TODO("Invalid response code ${resp.responseCode}")

            val items = path.split('/');
            return RemoteEntity(
                path = items.subList(0, items.lastIndex - 1).joinToString("/"),
                lastModified = lastModified,
                user = user,
                length = length,
                isFile = true
            )
        }

        override suspend fun move(path: String, overwrite: Boolean): FileSystem.Entity {
            val destinationUrl = url.appendDirectionURI(path)
            val r = client.request("MOVE", url.appendDirectionURI(this.path).appendDirectionURI(name))
            user?.apply(r)
            r.addHeader("Destination", destinationUrl.toString())
            if (overwrite) {
                r.addHeader("Overwrite", "T")
            }
            val resp = r.response()
            if (resp.responseCode != 201 && resp.responseCode != 204)
                throw TODO("Invalid response code ${resp.responseCode}")

            return RemoteEntity(
                path = path,
                lastModified = lastModified,
                user = user,
                length = length,
                isFile = true
            )
        }

        override suspend fun delete() {
            val r = client.request("DELETE", url.appendDirectionURI(this.path).appendDirectionURI(name))
            user?.apply(r)
            val resp = r.response()
            try {
                if (resp.responseCode != 201 && resp.responseCode != 204) {
                    throw TODO("Invalid response code ${resp.responseCode}")
                }
            } finally {
                resp.close()
            }
        }

        override suspend fun rewrite(): AsyncOutput {
            val allPathUrl = url.appendDirectionURI(path)
            val r = client.request("PUT", allPathUrl)
//            r.addHeader("Overwrite", "T")
            user?.apply(r)
            val upload = r.upload()
            return object : AsyncOutput {
                override suspend fun write(data: ByteBuffer): Int = upload.write(data)

                override suspend fun flush() {
                    upload.flush()
                }

                override suspend fun close() {
                    upload.response().close()
                }
            }
        }
    }

    override suspend fun getDir(path: String): Sequence<FileSystem.Entity>? =
        getDir(WebAuthAccess.getCurrentUser(), path, 1)

    suspend fun getDir(user: WebAuthAccess?, path: String, depth: Int): Sequence<FileSystem.Entity>? {
        val allPathUrl = url.appendDirectionURI(path)
        val r = client.request("PROPFIND", allPathUrl)
        user?.apply(r)
        r.addHeader("Depth", depth.toString())
        val resp = r.response()
//        val auth = r.getResponseHeaders()["WWW-Authenticate"]
        if (resp.responseCode == 404) {
            return null
        }
        val txt = resp.utf8Reader().readText()
        val reader = StringReader(txt).asAsync().xmlTree()!!
        resp.close()
        return reader
            .findTag("response")
            .mapNotNull {
                val href = it.findTag("href").firstOrNull()?.body ?: return@mapNotNull null
                val length = it.findTag("propstat")
                    .flatMap {
                        it.findTag("prop")
                    }
                    .mapNotNull {
                        it.findTag("getcontentlength")
                            .singleOrNull()?.body
                            ?.toLongOrNull()
                    }.firstOrNull() ?: 0
                val isDirectory = it.findTag("propstat")
                    .flatMap {
                        it.findTag("prop")
                    }
                    .mapNotNull {
                        it.findTag("resourcetype")
                            .singleOrNull()
                            ?.findTag("collection")
                            ?.singleOrNull()
                    }.any()
                val lastModified = it.findTag("propstat")
                    .flatMap {
                        it.findTag("prop")
                    }
                    .mapNotNull {
                        it.findTag("getlastmodified").singleOrNull()?.body
                    }.singleOrNull()
                    ?.parseDate()
                    ?.time
                    ?: 0L
                if (isDirectory)
                    RemoteEntity(
                        path = path,
                        lastModified = lastModified,
                        user = user,
                        isFile = false,
                        length = 0
                    )
                else
                    RemoteEntity(
                        length = length,
                        user = user,
                        lastModified = lastModified,
                        path = path,
                        isFile = true
                    )
            }
    }

    override suspend fun get(path: String): FileSystem.Entity? {
        val allPathUrl = url.appendDirectionURI(path)
        val r = client.request("HEAD", allPathUrl)
        WebAuthAccess.getCurrentUser()?.apply(r)
        return getDir(WebAuthAccess.getCurrentUser(), path, 0)?.firstOrNull()
    }

    override suspend fun new(path: String): AsyncOutput {
        val allPathUrl = url.appendDirectionURI(path)
        val r = client.request("PUT", allPathUrl)
        WebAuthAccess.getCurrentUser()?.apply(r)
        val upload = r.upload()
        return object : AsyncOutput {
            override suspend fun write(data: ByteBuffer): Int = upload.write(data)

            override suspend fun flush() {
                upload.flush()
            }

            override suspend fun close() {
                upload.response().close()
            }
        }
    }
}