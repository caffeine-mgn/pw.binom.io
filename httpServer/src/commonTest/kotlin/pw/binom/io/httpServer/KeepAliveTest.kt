package pw.binom.io.httpServer

import pw.binom.io.useAsync

val okHandler =
  Handler {
    it.response().useAsync {
      it.status = 202
      it.headers.contentType = "text/html;charset=utf-8"
      it.startWriteText().useAsync {
        it.append("Hello! Привет в UTF-8")
      }
    }
  }

// class KeepAliveTest {
//
//    @Test
//    fun test() {
//        val w = Worker.create()
//        val nd = NetworkDispatcher()
//        val server = HttpServer(
//            manager = nd,
//            handler = okHandler,
//            maxIdleTime = 1_000
//        )
//        val port = Random.nextInt(1000, Short.MAX_VALUE - 1)
//        val addr = NetworkAddress.Immutable("127.0.0.1", port)
//        server.bindHttp(addr)
//
//        val d = nd.startCoroutine {
//            val client = BaseHttpClient(nd)
//            client.connect(HTTPMethod.GET.code, "http://127.0.0.1:${addr.port}".toURI()).getResponse().readText()
//                .use { it.readText() }
//            assertEquals(1, server.idleConnectionSize)
//            server.forceIdleCheck()
//            assertEquals(1, server.idleConnectionSize)
//            w.start {
//                sleep(1500)
//            }
//            assertEquals(1, server.forceIdleCheck())
//            assertEquals(0, server.idleConnectionSize)
//            Unit
//        }
//
//        while (!d.isDone) {
//            nd.select()
//        }
//
//        d.getOrException()
//    }
// }
