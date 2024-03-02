package pw.binom.network

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import pw.binom.concurrency.*
import pw.binom.coroutines.SimpleAsyncLock
import pw.binom.io.ByteBuffer
import pw.binom.io.bufferedAsciiWriter
import pw.binom.io.socket.*
import pw.binom.io.use
import pw.binom.readByte
import pw.binom.thread.Thread
import pw.binom.writeByte
import kotlin.test.*
import kotlin.time.Duration.Companion.INFINITE
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalCoroutinesApi::class)
class TcpConnectionTest {
  @Test
  @Ignore
  fun serverFlagsOnAttachTest() {
    val selectKeys = SelectedKeys()
    val selector = Selector()
    val server = Socket.createTcpServerNetSocket()
    server.bind(InetNetworkAddress.create(host = "127.0.0.1", port = 0))
    server.blocking = false
    selector.attach(
      socket = server,
    ).also {
      it.updateListenFlags(KeyListenFlags.READ)
    }
//        serverKey.listensFlag =
//            Selector.INPUT_READY or Selector.OUTPUT_READY or Selector.EVENT_CONNECTED or Selector.EVENT_ERROR
    val client = Socket.createTcpClientNetSocket()
    client.connect(InetNetworkAddress.create(host = "127.0.0.1", port = server.port!!))
    Thread.sleep(500)
    selector.select(selectedKeys = selectKeys, timeout = 1.seconds)
    assertTrue(selectKeys.toList().isNotEmpty(), "No event for select. Socket selector should be read for input")
  }

  @Test
  @Ignore
  fun autoCloseKey() =
    runTest(dispatchTimeoutMs = 10_000) {
      val nd = NetworkCoroutineDispatcherImpl()
      val serverConnection = nd.bindTcp(InetNetworkAddress.create("127.0.0.1", 0))
      val lock = SpinLock()
      lock.lock()
      launch(nd) {
        val client = serverConnection.accept()
        lock.synchronize {
          client.close()
          println("Server Side: connection closed!")
        }
      }
      val selector = Selector()
      val channel = Socket.createTcpClientNetSocket()
      channel.blocking = false
      val clientKey =
        selector.attach(
          channel,
        )
      clientKey.updateListenFlags(KeyListenFlags.ERROR or KeyListenFlags.READ or KeyListenFlags.WRITE)
      println("Connect...")
      channel.connect(InetNetworkAddress.create(host = "127.0.0.1", serverConnection.port))
      println("Connect started!")
      val selectKeys = SelectedKeys()
//        LOOP_CONNECT@ while (true) {
//            selector.select(selectedKeys = selectKeys, timeout = INFINITE)
//            val o = selectKeys.iterator()
//            while (o.hasNext()) {
//                val e = o.next()
//                if (e.mode and KeyListenFlags.WRITE > 0) {
//                    println("Connected!")
//                    break@LOOP_CONNECT
//                }
//            }
//        }
//        clientKey.listensFlag =
//            Selector.EVENT_ERROR or Selector.EVENT_CONNECTED or Selector.INPUT_READY or Selector.OUTPUT_READY
//        assertEquals(1, selector.getAttachedKeys().size)
      lock.unlock()
      clientKey.updateListenFlags(KeyListenFlags.READ or KeyListenFlags.ERROR)
      val list = SelectedKeys()
      var c = 0
      val buffer = ByteBuffer(512)
      LOOP_ERROR@ while (true) {
        println("Selecting...")
        val eventCount = selector.select(selectedKeys = list, timeout = INFINITE)
        println("Selected! eventCount=$eventCount")
        val o = list.toList().iterator()
        while (o.hasNext()) {
          c++
          if (c > 100) {
            break@LOOP_ERROR
          }
          val e = o.next()

          if (e.flags and KeyListenFlags.READ > 0) {
            println("Try read...")
            val count = channel.receive(buffer)
            assertEquals(-1, count)
            break@LOOP_ERROR
            println("Ready for connect. count=$count")
          }

          println("Event! ${e.flags.toString(2)}")
          if (e.flags and KeyListenFlags.ERROR > 0) {
            println("Error!!")
            break@LOOP_ERROR
          }
        }
      }
//        assertEquals(0, selector.getAttachedKeys().size)

//        try {
//            clientKey.close()
//            fail()
//        } catch (e: ClosedException) {
//            // Do nothing
//        }
    }

  @Test
  @Ignore
  fun writeErrorTest() =
    runTest {
      val nd = NetworkCoroutineDispatcherImpl()
      val port = TcpServerConnection.randomPort()
      val address = InetNetworkAddress.create("127.0.0.1", port)
      val worker = Worker()
      val spinLock = SpinLock()
      val server = nd.bindTcp(address)
      sleep(500)
      val client = nd.tcpConnect(address)
      launch {
        try {
          spinLock.synchronize {
            println("Try read...")
            val readed =
              ByteBuffer(5).use {
                println("try read...")
                client.read(it)
              }
            println("Reded $readed")
            client.close()
            println("Stop client!")
          }
        } catch (e: Throwable) {
          e.printStackTrace()
        }
      }
      println("Wait client connect")
      val remoteClient = server.accept()
      println("Client connected")
      server.close()
      ByteBuffer(10).use { buf ->
        remoteClient.write(buf)
        withContext(ThreadCoroutineDispatcher()) {
          spinLock.lock()
          spinLock.unlock()
        }

        try {
          buf.clear()
          remoteClient.write(buf)
          remoteClient.flush()
          fail()
        } catch (e: SocketClosedException) {
          // ok
        }

        println("Done!")
        remoteClient.close()
      }
    }

  @Test
  fun waitWriteTest() =
    runTest(dispatchTimeoutMs = 10_000) {
      val nd = NetworkCoroutineDispatcherImpl()
      val server = nd.bindTcp(InetNetworkAddress.create(host = "127.0.0.1", port = 0))
      val port = server.port

      val worker = Worker()

      val r =
        launch {
          val newClient = server.accept()
          withContext(ThreadCoroutineDispatcher()) {
            println("Wait send...")
            println("Wait net thread...")
            ByteBuffer(10).use { buf ->
              newClient.writeByte(buffer = buf, value = 42)
              newClient.flush()
              println("wrote!")
            }
          }
        }

      val r2 =
        launch {
          val client = nd.tcpConnect(InetNetworkAddress.create(host = "127.0.0.1", port = port))
          ByteBuffer(10).use { b ->
            println("Reading...")
            assertEquals(42, client.readByte(b))
            println("Read")
          }
        }
      r.join()
      r2.join()
    }

  @Test
  fun connectTest() =
    runTest(dispatchTimeoutMs = 10_000) {
      NetworkCoroutineDispatcherImpl().use { nd ->
        println("OK-1")
        val con = nd.tcpConnect(HTTP_SERVER_ADDRESS)
        println("OK-2")
        con.close()
        println("OK-3")
      }
    }

  fun aaa() =
    runTest(dispatchTimeoutMs = 10_000) {
      NetworkCoroutineDispatcherImpl().use { nd ->
        withContext(nd) {
          val c = nd.bindTcp(InetNetworkAddress.create("127.0.0.1", 8030))
          val vv = c.accept()
          val writer = vv.bufferedAsciiWriter()
          writer.append("HTTP/1.0 200 OK\r\n")
        }
      }
    }

  @OptIn(ExperimentalTime::class)
  @Test
  fun tryToWriteBreakConnection() =
    runTest(dispatchTimeoutMs = 5_000) {
      NetworkCoroutineDispatcherImpl().use { nd ->
        withContext(nd) {
          val server = nd.bindTcp(InetNetworkAddress.create(host = "127.0.0.1", port = 0))
          val client = nd.tcpConnect(InetNetworkAddress.create(host = "127.0.0.1", port = server.port))
          val connectedClient = server.accept()
          val lock = SimpleAsyncLock()
          lock.lock()
          launch {
            try {
              println("Try read first bytes...")
              ByteBuffer(50).use { buf ->
                client.readFully(buf)
              }
              println("Byte read! Try close and unlock")
              client.close()
              lock.unlock()
              println("Unlocked!")
            } catch (e: Throwable) {
              e.printStackTrace()
            }
          }
          try {
            ByteBuffer(50).use { buf ->
              delay(500)
              buf.clear()
              connectedClient.write(buf)
              println("first 50 bytes wrote")
//                        delay(1000)
              lock.synchronize {
                println("start cycle")
                val now = TimeSource.Monotonic.markNow()
                while (now.elapsedNow() < 2.seconds) {
                  buf.clear()
                  val b = connectedClient.write(buf)
                  println("b=$b")
                }
              }
            }
            fail("Should throws SocketClosedException")
          } catch (e: SocketClosedException) {
            e.printStackTrace()
            // Do nothing
          }
        }
      }
    }
}
