package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.io.UTF8
import pw.binom.network.CrossThreadKeyHolder
import pw.binom.network.TcpConnection

@Deprecated(message = "Will be removed")
interface HttpRequestDeprecated {
    val method: String
    val uri: String
    val contextUri: String
    val input: AsyncInput
    val rawInput: AsyncInput
    val rawOutput: AsyncOutput
    val rawConnection: TcpConnection
    val headers: Map<String, List<String>>
}

fun HttpRequestDeprecated.withContextURI(uri: String) = object : HttpRequestDeprecated {
    override val method: String
        get() = this@withContextURI.method
    override val uri: String
        get() = this@withContextURI.uri
    override val contextUri: String
        get() = uri
    override val input: AsyncInput
        get() = this@withContextURI.input
    override val rawInput: AsyncInput
        get() = this@withContextURI.rawInput
    override val rawOutput: AsyncOutput
        get() = this@withContextURI.rawOutput
    override val rawConnection: TcpConnection
        get() = this@withContextURI.rawConnection
    override val headers: Map<String, List<String>>
        get() = this@withContextURI.headers
}

fun HttpRequestDeprecated.visitGetParams(func: (key: String, value: String?) -> Boolean) {
    val p = contextUri.lastIndexOf('?')
    if (p == -1) {
        return
    }

    contextUri.substring(p + 1).split('&').forEach {
        val items = it.split('=', limit = 2)
        if (items.size == 1) {
            func(UTF8.decode(items[0]), null)
        } else {
            func(UTF8.decode(items[0]), UTF8.decode(items[1]))
        }
    }
}

fun HttpRequestDeprecated.parseGetParams(): Map<String, List<String?>> {
    val out = HashMap<String, ArrayList<String?>>()
    visitGetParams { key, value ->
        out.getOrPut(key) { ArrayList() }.add(value)
        true
    }
    return out
}