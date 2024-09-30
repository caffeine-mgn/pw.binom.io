package pw.binom.io.httpClient

import kotlinx.coroutines.*
import pw.binom.coroutines.AsyncExchange
import pw.binom.io.*
import pw.binom.io.http.websocket.MessageCoder
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.http.websocket.WebSocketClosedException
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.network.MultiFixedSizeThreadNetworkDispatcher
import pw.binom.testing.*
import pw.binom.url.toURL
import pw.binom.uuid.nextUuid
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlin.time.measureTime

@OptIn(DelicateCoroutinesApi::class)
class WebSocketTest {
  @Test
  fun simpleEchoTest() = Testing.async {
    ws { ws ->
      val bufferSize = 10
      test("one shot message") {
        ws.echoText(Random.nextUuid().toString())
      }
      test("long message") {
        ws.echoBytes((bufferSize * 2.7).roundToInt())
      }
      test("Several message in one connect") {
        repeat(10) {
          ws.echoBytes((bufferSize * 2.7).roundToInt())
        }
      }
    }
  }

  @Test
  fun tryParallelWriteTest() = Testing.async {
    ws { ws ->
      val now = TimeSource.Monotonic.markNow()
      var sendFinish1 = Duration.ZERO
      var sendFinish2 = Duration.ZERO
      val firstPartMessage = Random.nextUuid().toString()
      val secondPartMessage = Random.nextUuid().toString()
      val secondMessage = Random.nextUuid().toString()
      val a = GlobalScope.launch {
        ws.write(MessageType.TEXT).bufferedWriter().useAsync { out ->
          out.append(firstPartMessage)
          out.flush()
          realDelay(2.seconds)
          out.append(secondPartMessage)
        }
        sendFinish1 = now.elapsedNow()
      }
      val b = GlobalScope.launch {
        realDelay(1.seconds)
        ws.write(MessageType.TEXT).bufferedWriter().useAsync { out ->
          out.append(secondMessage)
        }
        sendFinish2 = now.elapsedNow()
      }
      val txt1 = ws.read().bufferedReader().useAsync { msg ->
        msg.readText()
      }
      val txt2 = ws.read().bufferedReader().useAsync { msg ->
        msg.readText()
      }
      b.join()
      a.join()
      (sendFinish2 > sendFinish1).shouldBeTrue()
      txt1 shouldEquals firstPartMessage + secondPartMessage
      txt2 shouldEquals secondMessage
    }
  }

  @Test
  fun tryParallelReadTest() = Testing.async {
    ws { ws ->
      val a = GlobalScope.launch {
        ws.write(MessageType.TEXT).bufferedWriter().useAsync { msg ->
          realDelay(1.seconds)
          msg.append("1")
          msg.flush()
          realDelay(1.seconds)
          msg.append("2")
        }
        realDelay(1.seconds)
        ws.write(MessageType.TEXT).bufferedWriter().useAsync { msg ->
          msg.append("1")
          msg.flush()
          msg.append("2")
        }
      }
      val now = TimeSource.Monotonic.markNow()
      var d1 = Duration.ZERO
      var d2 = Duration.ZERO
      val b = GlobalScope.launch {
        ws.read().bufferedReader().useAsync { it.readText() }
        d1 = now.elapsedNow()
      }
      val c = GlobalScope.launch {
        realDelay(0.2.seconds)
        ws.read().bufferedReader().useAsync { it.readText() }
        d2 = now.elapsedNow()
      }
      a.join()
      b.join()
      c.join()
      (d2 > d1).shouldBeTrue()
    }
  }

  @Test
  fun closeConnectionDudingReading() = Testing.async {
    ws { ws ->
      var e1: Throwable? = null
      var e2: Throwable? = null
      val a = GlobalScope.launch {
        try {
          ws.read().readBytes().toList()
        } catch (e: Throwable) {
          e1 = e
        }
      }
      val b = GlobalScope.launch {
        try {
          realDelay(1.seconds)
          ws.read().readBytes()
        } catch (e: Throwable) {
          e2 = e
        }
      }
      realDelay(2.seconds)
      ws.asyncClose()
      realDelay(1.seconds)
      (e1!! is WebSocketClosedException).shouldBeTrue()
      (e2!! is WebSocketClosedException).shouldBeTrue()
      a.isActive.shouldBeFalse()
      b.isActive.shouldBeFalse()
    }
  }

  @Test
  fun closeConnectionDudingWriting() = Testing.async {
    ws { ws ->
      val lock = AsyncExchange<Unit>()
      GlobalScope.launch {
        try {
          ws.write(MessageType.TEXT).bufferedWriter().useAsync {
            it.append("1")
            it.flush()
            lock.extract()
            it.append("2")
          }
        } catch (e: Throwable) {
          e.printStackTrace()
        }
      }
      realDelay(1.seconds)
      ws.asyncClose()
      realDelay(1.seconds)
      lock.push(Unit)
      realDelay(2.seconds)
    }

  }
}
