package pw.binom.io.httpClient

import kotlinx.coroutines.Dispatchers
import pw.binom.concurrency.sleep
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.net.toURI
import pw.binom.network.Network
import pw.binom.network.NetworkDispatcher
import pw.binom.testContainer.TestContainer
import pw.binom.testContainer.invoke
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class ApacheServerTest {

    object ApacheContainer : TestContainer(
        image = "php:7.2-apache",
        ports = listOf(
            Port(internalPort = 80)
        ),
        reuse = true,
    ) {
        val port
            get() = ports[0].externalPort
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun test() {
        ApacheContainer {
            sleep(1000)
            NetworkDispatcher().use { nd ->
                val time = measureTime {
                    nd.runSingle {
                        HttpClient.create1(Dispatchers.Network).use { http ->
                            repeat(500) {
                                http.connect(
                                    method = "GET",
                                    uri = "http://127.0.0.1:${ApacheContainer.port}/".toURI()
                                ).use {
                                    it.getResponse().use {
                                        it.readText().use { it.readText() }
                                    }
                                }
                            }
                        }
                    }
                }
                println("time: $time")
            }
        }
    }
}