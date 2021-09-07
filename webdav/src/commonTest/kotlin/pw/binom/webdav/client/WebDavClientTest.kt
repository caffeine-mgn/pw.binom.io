package pw.binom.webdav.client

import pw.binom.charset.Charsets
import pw.binom.io.*
import pw.binom.io.httpClient.BaseHttpClient
import pw.binom.net.toURI
import pw.binom.network.NetworkDispatcher
import pw.binom.nextUuid
import pw.binom.webdav.BasicAuthorization
import kotlin.random.Random
import kotlin.test.*

class WebDavClientTest:AbstractWebDavClientTest() {
    private val user = BasicAuthorization(login = "root", password = "root")
    private val PATH = "http://127.0.0.1:8055".toURI()

    override fun clientWithUser(func: suspend (WebDavClient) -> Unit) {
        val nd = NetworkDispatcher()
        val client = BaseHttpClient(nd)
        nd.runSingle {
            val client = WebDavClient(client = client, url = PATH)
            client.useUser(user = user) {
                func(client)
            }
        }
    }

    @Test
    fun stab(){

    }

}