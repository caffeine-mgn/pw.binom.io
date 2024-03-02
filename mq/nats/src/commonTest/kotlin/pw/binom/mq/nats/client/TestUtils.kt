package pw.binom.mq.nats.client

import pw.binom.mq.nats.BaseTest

object TestUtils {
  const val NATS_PORT = BaseTest.NATS_PORT
}

// @OptIn(ExperimentalTime::class)
// fun net(func: suspend (NetworkDispatcher) -> Unit) {
//    val nd = NetworkDispatcher()
//    val r = async2 {
//        func(nd)
//    }
//
//    val time = TimeSource.Monotonic.markNow()
//    while (!r.isDone) {
//        if (time.elapsedNow() > 10.0.seconds) {
//            throw RuntimeException("Timeout")
//        }
//        nd.select(500)
//    }
//
//    if (r.isFailure) {
//        throw r.exceptionOrNull!!
//    }
// }
