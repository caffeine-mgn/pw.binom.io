package pw.binom.io.httpClient

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.net.toURL
import pw.binom.network.Network
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

//class ApacheServerTest {
//
//    object ApacheContainer : TestContainer(
//        image = "php:7.2-apache",
//        ports = listOf(
//            Port(internalPort = 80)
//        ),
//        reuse = true,
//    ) {
//        val port
//            get() = ports[0].externalPort
//    }
//
//    @OptIn(ExperimentalTime::class)
//    @Test
//    fun test() = runTest {
//        ApacheContainer {
//            realDelay(1000)
//            val time = measureTime {
//                HttpClient.create(Dispatchers.Network).use { http ->
//                    repeat(500) {
//                        http.connect(
//                            method = "GET",
//                            uri = "http://127.0.0.1:${ApacheContainer.port}/".toURL()
//                        ).use {
//                            it.getResponse().use {
//                                it.readText().use { it.readText() }
//                            }
//                        }
//                    }
//                }
//            }
//            println("time: $time")
//        }
//    }
//}