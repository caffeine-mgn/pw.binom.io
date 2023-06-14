package pw.binom.io.httpClient

import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableMap
import pw.binom.coroutines.AsyncReentrantLock
import pw.binom.io.httpClient.protocol.HttpConnect
import pw.binom.url.URL

class DefaultHttpConnectionPool : HttpConnectionPool {

    internal val connections =
        defaultMutableMap<String, MutableList<HttpConnect>>()
    private val lock = AsyncReentrantLock()

    suspend fun cleanup() {
        lock.synchronize {
            val it1 = connections.iterator()
            while (it1.hasNext()) {
                val e = it1.next()
                val it2 = e.value.iterator()
                while (it2.hasNext()) {
                    val f = it2.next()
                    if (!f.isAlive) {
                        f.asyncCloseAnyway()
                        it2.remove()
                    }
                }
                if (e.value.isEmpty()) {
                    it1.remove()
                }
            }
        }
    }

    override suspend fun borrow(url: URL, factory: HttpConnectionPool.Factory): HttpConnect {
        cleanup()
        val key = url.asKey
        val existConnection = lock.synchronize {
            val list = connections[key]
            if (list != null) {
                val existConnection = list.removeLast()
                if (list.isEmpty()) {
                    connections.remove(key)
                }
                existConnection
            } else {
                null
            }
        }
        if (existConnection != null) {
            return existConnection
        }
        return factory.connect(factory = this, url = url)
    }

    override suspend fun recycle(url: URL, channel: HttpConnect) {
        if (!channel.isAlive) {
            channel.asyncCloseAnyway()
            return
        }
        cleanup()
        val key = url.asKey
        lock.synchronize {
            connections.getOrPut(key) { defaultMutableList() }.add(channel)
        }
    }

    override suspend fun asyncClose() {
        lock.synchronize {
            connections.forEach { it.value.forEach { it.asyncClose() } }
        }
    }

    private val URL.asKey
        get() = "$schema://$host:$port"
}
