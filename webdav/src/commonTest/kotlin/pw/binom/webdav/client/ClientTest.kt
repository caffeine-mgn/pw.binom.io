package pw.binom.webdav.client

import pw.binom.URL
import pw.binom.asUTF8ByteArray
import pw.binom.async
import pw.binom.atomic.AtomicBoolean
import pw.binom.base64.Base64
import pw.binom.date.Date
import pw.binom.io.*
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.stackTrace
import pw.binom.webdav.server.parseDate
import pw.binom.xml.dom.XmlElement
import pw.binom.xml.dom.findElements
import pw.binom.xml.dom.xmlTree
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

interface WebAuthAccess {
    suspend fun apply(connection: AsyncHttpClient.UrlConnect)
}

class BasicAuthorization(login: String, password: String) : WebAuthAccess {
    val str = "Basic ${Base64.encode("$login:$password".asUTF8ByteArray())}"
    override suspend fun apply(connection: AsyncHttpClient.UrlConnect) {
        connection.addRequestHeader("Authorization", str)
    }

}

private fun XmlElement.xpath(path: String): Sequence<XmlElement> {
    val pathList = path.split('/').filter { it.isBlank() }
    if (pathList.isEmpty())
        return emptySequence()
    var p = findElements {
        it.tag == pathList[0]
    }
    for (i in 1 until pathList.size) {
        p = p.flatMap {
            it.findElements { it.tag == pathList[i] }
        }
    }
    return p
}

private fun XmlElement.findTag(name: String) =
        findElements { it.nameSpace?.url == "DAV:" && it.tag == name }

private fun Sequence<XmlElement>.findTag(name: String) =
        filter { it.nameSpace?.url == "DAV:" && it.tag == name }

class Client<T : WebAuthAccess>(val client: AsyncHttpClient, val url: URL) : FileSystem<T> {

    override suspend fun mkdir(user: T, path: String): FileSystem.Entity<T>? {
        val allPathUrl = url.newURI("${url.uri}$path")
        val r = client.request("MKCOL", allPathUrl)
        user.apply(r)
        if (r.responseCode() == 405)
            return null
        if (r.responseCode() != 201)
            TODO("Invalid response code ${r.responseCode()}")
        val ss = path.split('/')
        return RemoteEntity(
                name = ss.lastOrNull() ?: "",
                user = user,
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
    private inner class RemoteEntity(override val name: String, override val length: Long, override val lastModified: Long, override val path: String, override val user: T, override val isFile: Boolean) : FileSystem.Entity<T> {
        override val fileSystem: Client<T>
            get() = this@Client

        override suspend fun read(): AsyncInputStream? {
            val allPathUrl = url.newURI("${url.uri}$path/$name")
            val r = client.request("GET", allPathUrl)
            user.apply(r)
            if (r.responseCode() == 404)
                return null

            return object : AsyncInputStream {
                override suspend fun read(): Byte =
                        r.inputStream.read()

                override suspend fun read(data: ByteArray, offset: Int, length: Int): Int =
                        r.inputStream.read(data, offset, length)

                override suspend fun close() {
                    r.inputStream.close()
                    r.close()
                }

            }
        }

        @OptIn(ExperimentalTime::class)
        override suspend fun copy(path: String, overwrite: Boolean): FileSystem.Entity<T> {
            val destinationUrl = url.appendDirectionURI(path)
            val r = client.request("COPY", url.appendDirectionURI(this.path).appendDirectionURI(name))
            user.apply(r)
            r.addRequestHeader("Destination", destinationUrl.toString())
            if (overwrite)
                r.addRequestHeader("Overwrite", "T")
            val ff = measureTime {
                if (r.responseCode() != 201 && r.responseCode() != 204)
                    throw TODO("Invalid response code ${r.responseCode()}")

                r.getResponseHeaders().forEach {
                    println("${it.key}: ${it.value}")
                }
            }
            println("Getting result: $ff")
            val items = path.split('/');
            return RemoteEntity(
                    name = items.lastOrNull() ?: "",
                    path = items.subList(0, items.lastIndex - 1).joinToString("/"),
                    lastModified = lastModified,
                    user = user,
                    length = length,
                    isFile = true
            )
        }

        override suspend fun move(path: String, overwrite: Boolean): FileSystem.Entity<T> {
            val destinationUrl = url.appendDirectionURI(path)
            val r = client.request("MOVE", url.appendDirectionURI(this.path).appendDirectionURI(name))
            user.apply(r)
            r.addRequestHeader("Destination", destinationUrl.toString())
            if (overwrite)
                r.addRequestHeader("Overwrite", "T")
            if (r.responseCode() != 201 && r.responseCode() != 204)
                throw TODO("Invalid response code ${r.responseCode()}")

            r.getResponseHeaders().forEach {
                println("${it.key}: ${it.value}")
            }
            val items = path.split('/');
            return RemoteEntity(
                    name = items.lastOrNull() ?: "",
                    path = items.subList(0, items.lastIndex - 1).joinToString("/"),
                    lastModified = lastModified,
                    user = user,
                    length = length,
                    isFile = true
            )
        }

        override suspend fun delete() {
            val r = client.request("DELETE", url.appendDirectionURI(this.path).appendDirectionURI(name))
            user.apply(r)
            if (r.responseCode() != 201 && r.responseCode() != 204)
                throw TODO("Invalid response code ${r.responseCode()}")
        }

        override suspend fun rewrite(): AsyncOutputStream {
            TODO("Not yet implemented")
        }

    }

    override suspend fun getDir(user: T, path: String): Sequence<FileSystem.Entity<T>>? =
            getDir(user, path, 1)

    suspend fun getDir(user: T, path: String, depth: Int): Sequence<FileSystem.Entity<T>>? {
        val allPathUrl = url.appendDirectionURI(path)
        val r = client.request("PROPFIND", allPathUrl)
        user.apply(r)
        r.addRequestHeader("Depth", depth.toString())
//        val auth = r.getResponseHeaders()["WWW-Authenticate"]
        if (r.responseCode() == 404) {
            return null
        }
        val txt = r.inputStream.utf8Reader().readText()
        val reader = StringReader(txt).asAsync().xmlTree()!!
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
                                name = href.removePrefix(allPathUrl.uri).removeSuffix("/"),
                                path = path,
                                lastModified = lastModified,
                                user = user,
                                isFile = false,
                                length = 0
                        )
                    else
                        RemoteEntity(
                                name = href.removePrefix(allPathUrl.uri),
                                length = length,
                                user = user,
                                lastModified = lastModified,
                                path = path,
                                isFile = true
                        )
                }
    }

    override suspend fun get(user: T, path: String): FileSystem.Entity<T>? {
        val allPathUrl = url.appendDirectionURI(path)
        val r = client.request("HEAD", allPathUrl)
        user.apply(r)
        return getDir(user, path, 0)?.firstOrNull()
    }

    override suspend fun new(user: T, path: String): AsyncOutputStream {
        val allPathUrl = url.appendDirectionURI(path)
        val r = client.request("PUT", allPathUrl)
        user.apply(r)
        return object : AsyncOutputStream {
            override suspend fun write(data: Byte): Boolean =
                    r.outputStream.write(data)

            override suspend fun write(data: ByteArray, offset: Int, length: Int): Int =
                    r.outputStream.write(data, offset, length)

            override suspend fun flush() {
                r.outputStream.flush()
            }

            override suspend fun close() {
                r.outputStream.close()
                r.close()
            }

        }
    }
}

class ClientTest {

    @OptIn(ExperimentalTime::class)
    @Test
    fun test() {
        try {
            val url = URL("https://192.168.88.117/remote.php/webdav")
            val manager = SocketNIOManager()
            val client = AsyncHttpClient(manager)
            val done = AtomicBoolean(false)
            async {
                try {
                    val auth = BasicAuthorization("admin", "Drovosek319")
                    val clientw = Client<BasicAuthorization>(client, url)
                    val rr = clientw.getDir(auth, "/tmp2")
                    println("Print result:")
                    rr?.toList()
                            ?.forEach {
                                println("${it.name}\t\t${it.length}")
                            }

                    val bb = clientw.mkdir(auth, "/tmp/test")
                    clientw.new(auth, "/tmp/kotlin.txt").use {
                        it.write("Hello from Kotlin".asUTF8ByteArray())
                        it.flush()
                    }
                    println("Folder created!")
                    println("${bb?.path} -> ${bb?.name}")
                    val file = clientw.get(auth, "/tmp/kotlin.txt")
                    println("File: $file")

                    val body = file?.read()?.use {
                        it.utf8Reader()?.readText()
                    }
                    val deleteTime = measureTime {
                        file?.delete()
                    }
                    println("Delete Time: $deleteTime")
                } catch (e: Throwable) {
                    println("Error! $e")
                    e.stackTrace.forEachIndexed { index, s ->
                        println("$index -> $s")
                    }
                } finally {
                    done.value = true
                }
            }

            while (!done.value) {
                manager.update()
            }
        } catch (e: Throwable) {
            println("Exception!!!!!!")
        }
    }
}