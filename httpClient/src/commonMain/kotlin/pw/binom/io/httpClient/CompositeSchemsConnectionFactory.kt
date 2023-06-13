package pw.binom.io.httpClient

import pw.binom.collections.defaultMutableMap
import pw.binom.io.AsyncChannel
import pw.binom.network.NetworkManager

class CompositeSchemsConnectionFactory(factories: Map<String, ConnectionFactory> = emptyMap()) : ConnectionFactory {
    private val internalFactories = defaultMutableMap(factories)
    val factories: Map<String, ConnectionFactory>
        get() = internalFactories

    fun add(factory: ConnectionFactory, schema: String): CompositeSchemsConnectionFactory {
        internalFactories[schema] = factory
        return this
    }

    fun add(factory: ConnectionFactory, vararg schemes: String): CompositeSchemsConnectionFactory {
        schemes.forEach { schema ->
            add(factory = factory, schema = schema)
        }
        return this
    }

    private fun findFactoryBySchema(schema: String) =
        internalFactories[schema] ?: throw IllegalArgumentException("Unknown schema \"$schema\"")

    override suspend fun connect(
        networkManager: NetworkManager,
        schema: String,
        host: String,
        port: Int,
    ): AsyncChannel {
        val factory = findFactoryBySchema(schema = schema)
        return factory.connect(
            networkManager = networkManager,
            schema = schema,
            host = host,
            port = port,
        )
    }

    override suspend fun connect(channel: AsyncChannel, schema: String, host: String, port: Int): AsyncChannel {
        val factory = findFactoryBySchema(schema = schema)
        return factory.connect(
            channel = channel,
            schema = schema,
            host = host,
            port = port,
        )
    }
}
