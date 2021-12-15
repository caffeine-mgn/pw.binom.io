package pw.binom.webdav.client

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import pw.binom.concurrency.sleep
import pw.binom.io.http.BasicAuth
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.BaseHttpClient
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.create
import pw.binom.io.use
import pw.binom.net.toPath
import pw.binom.net.toURI
import pw.binom.testContainer.TestContainer
import pw.binom.testContainer.invoke
import pw.binom.webdav.BasicAuthorization
import kotlin.test.Ignore
import kotlin.test.Test

class WebDavClientTest : AbstractWebDavClientTest() {
    private val user = BasicAuthorization(login = "root", password = "root")


    object NginxContainer : TestContainer(
        image = "ugeek/webdav:amd64",
        environments = mapOf(
            "USERNAME" to "root",
            "PASSWORD" to "root",
            "TZ" to "GMT"
        ),
        ports = listOf(
            Port(internalPort = 80)
        ),
        reuse = true,
    ) {
        val port
            get() = ports[0].externalPort
    }

    override fun clientWithUser(func: suspend (WebDavClient) -> Unit) = runBlocking {
        NginxContainer {
            sleep(2000)
            val uri = "http://127.0.0.1:${NginxContainer.port}".toURI()
            val client = HttpClient.create()
            val webDavClient = WebDavClient(client = client, url = uri)
            webDavClient.useUser(user = user) {
                func(webDavClient)
            }
        }
    }

    @Test
    fun stab() {

    }

}