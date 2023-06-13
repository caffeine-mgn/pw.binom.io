package pw.binom.io.httpClient.protocol

import pw.binom.url.URL

class ProtocolSelectorBySchema(factories: Map<String, ConnectFactory2> = emptyMap()) : ProtocolSelector {
    private val internalFactories = HashMap<String, ConnectFactory2>()
    val factories: Map<String, ConnectFactory2>
        get() = internalFactories

    init {
        this.internalFactories.putAll(factories)
    }

    fun set(factory: ConnectFactory2?, schema: String): ProtocolSelectorBySchema {
        if (factory == null) {
            internalFactories.remove(schema)
        } else {
            internalFactories[schema] = factory
        }
        return this
    }

    fun set(factory: ConnectFactory2?, vararg schema: String): ProtocolSelectorBySchema {
        schema.forEach {
            set(factory = factory, schema = it)
        }
        return this
    }

    override fun find(url: URL): ConnectFactory2? =
        internalFactories[url.schema]
}
