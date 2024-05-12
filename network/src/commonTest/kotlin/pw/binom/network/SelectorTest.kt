package pw.binom.network

import pw.binom.concurrency.SpinLock
import pw.binom.io.ByteBuffer
import pw.binom.io.socket.*
import pw.binom.io.use
import pw.binom.thread.Thread
import kotlin.random.Random
import kotlin.test.*
import kotlin.time.*
import kotlin.time.Duration.Companion.INFINITE
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SelectorTest {
  @Test
  @Ignore
  fun wakeupTest() {
    val selector = Selector()
    val selectKeys = SelectedKeys()
    Thread {
      Thread.sleep(1000)
      selector.wakeup()
    }.start()
    val it = selector.select(selectedKeys = selectKeys, timeout = INFINITE)
    println("Count: $it")
    selectKeys.forEach {
      fail()
    }
  }

  @Test
  @Ignore
  fun selectTimeoutTest1() {
    val selectKeys = SelectedKeys()
    val selector = Selector()
    val it = selector.select(timeout = 1.seconds, selectedKeys = selectKeys)
    val selected = selectKeys.toList()
    assertEquals(0, selected.size)
  }

  @OptIn(ExperimentalTime::class)
  @Test
  @Ignore
  fun selectTimeoutTest2() {
    val selectKeys = SelectedKeys()
    val selector = Selector()
    val client = TcpClientNetSocket()
    client.blocking = false
    selector.attach(client)
    val beforeSelecting = TimeSource.Monotonic.markNow()
    selector.select(timeout = 1.seconds, selectedKeys = selectKeys)
    val it = selectKeys.toList()
    assertEquals(0, it.size)
    val selectingTime = beforeSelecting.elapsedNow()
    assertTrue(selectingTime > 0.5.seconds && selectingTime < 1.5.seconds)
  }

  @Test
  @Ignore
  fun connectTest2() {
    val selectKeys = SelectedKeys()
    val selector = Selector()
    val client = TcpClientNetSocket()
    client.blocking = false
    val key = selector.attach(client)
    try {
      key.updateListenFlags(ListenFlags.ZERO)
      client.connect(InetSocketAddress.resolve(host = "google.com", port = 443))
      selector.select(timeout = 2.seconds, selectedKeys = selectKeys)
      assertTrue(selectKeys.toList().isEmpty())
    } finally {
      key.close()
      client.close()
      selector.close()
    }
  }

  @Test
  @Ignore
  fun connectTest() {
    val selectKeys = SelectedKeys()
    val selector = Selector()
    val client = TcpClientNetSocket()
    client.blocking = false
    val key = selector.attach(client)
    key.updateListenFlags(ListenFlags.WRITE)
    client.connect(InetSocketAddress.resolve(host = "google.com", port = 443))
    selector.select(timeout = 5.seconds, selectedKeys = selectKeys)
    val it = selectKeys.toList()
    assertTrue(it.isNotEmpty())
    val mode = it.first().also { println("Event: $it") }.flags
    assertTrue(ListenFlags.WRITE in mode, "KeyListenFlags.WRITE fail")
    selector.select(timeout = 1.seconds, selectedKeys = selectKeys)
//        selectKeys.iterator().forEach {
//            println("->$it")
//        }
    assertTrue(selectKeys.toList().isEmpty())
  }

  @Test
  @Ignore
  fun connectionRefusedTest() {
    val selectKeys = SelectedKeys()
    val selector = Selector()
    val client = TcpClientNetSocket()
    client.blocking = false
    selector.attach(client)
    client.connect(InetSocketAddress.resolve("127.0.0.1", 12))

    selector.select(timeout = 5.seconds, selectedKeys = selectKeys)
    selectKeys.forEach {
      val mode = it.flags
      val key = it.key
      assertTrue(ListenFlags.ERROR in mode)
      client.close()
    }
    selector.select(timeout = 1.seconds, selectedKeys = selectKeys)
    assertTrue(selectKeys.toList().isEmpty())
  }

  @Test
  @Ignore
  fun bindTest() {
    val selectKeys = SelectedKeys()
    val selector = Selector()
    val server = TcpNetServerSocket()
    val addr = InetSocketAddress.resolve(host = "0.0.0.0", Random.nextInt(1000, 5999))
    server.bind(addr)
    server.blocking = false
    selector.attach(server)
    selector.select(timeout = 1.seconds, selectedKeys = selectKeys)
    assertTrue(selectKeys.toList().isEmpty())
  }

  @Test
  @Ignore
  fun testSeveralKeyOfOneSocket() {
    val server = TcpNetServerSocket()
    server.blocking = false
    server.bind(InetSocketAddress.resolve(host = "127.0.0.1", port = 0))
    val selectKeys1 = SelectedKeys()
    val selector1 = Selector()
    println("#1")
    val key1 = selector1.attach(server)
    println("#2")
    key1.updateListenFlags(ListenFlags.READ)
    val selectKeys2 = SelectedKeys()
    val selector2 = Selector()
    println("#3")
    val key2 = selector2.attach(server)
    println("#4")
    key2.updateListenFlags(ListenFlags.READ)
    TcpClientNetSocket().connect(InetSocketAddress.resolve(host = "127.0.0.1", port = server.port!!))

    val v1 = selector1.select(selectedKeys = selectKeys1, timeout = 10.milliseconds)
    val v2 = selector2.select(selectedKeys = selectKeys2, timeout = 10.milliseconds)

    println("v1=$v1, v2=$v2")
  }

  @OptIn(ExperimentalTime::class)
  @Test
  @Ignore
  fun exclusiveTest() {
//        val rr = ReentrantLock()
//        val con = rr.newCondition()
// EPOLLET
    val selector = Selector()

    class NThread : ThreadWorker() {
      val list = SelectedKeys()
      private val lock = SpinLock()
      var lastSelectCount = 0
        private set

      fun trySelect() {
        dispatch {
          println("Selecting...")
          val count = measureTimedValue { selector.select(selectedKeys = list, timeout = 1.seconds) }
          lastSelectCount = list.toList().size
          println("select count: $count")
        }
      }
    }

    val s1 = NThread()
    val s2 = NThread()
    val server = UdpNetSocket()
    server.blocking = false
    server.bind(InetSocketAddress.resolve(host = "0.0.0.0", port = 0))
    val addr = InetSocketAddress.resolve(host = "127.0.0.1", port = server.port!!)
    val s1Key = selector.attach(server)
    s1Key.updateListenFlags(ListenFlags.READ)
//        val s2Key = selector.attach(server, mode = Selector.INPUT_READY)

    s1.trySelect()
    s2.trySelect()
    val client = UdpNetSocket()

    fun sendNow() {
      ByteBuffer(16).use { buffer ->
        client.send(buffer, addr)
      }
    }
    sendNow()
    Thread.sleep(3_000)
    val threads = listOf(s1, s2)
    assertEquals(1, threads.count { it.lastSelectCount == 0 })
    assertEquals(1, threads.count { it.lastSelectCount == 1 })
  }

  private fun sendUdp(port: Int?):Unit = sendUdp(InetSocketAddress.resolve(host = "127.0.0.1", port = port!!))

  fun sendUdp(address: InetSocketAddress) {
    UdpNetSocket().use { c ->
      c.blocking = true
      ByteBuffer(42).use { buffer ->
        c.send(buffer, address)
      }
    }
  }

  @Test
  @Ignore
  fun resetListenKeysAfterSelect() {
    val selectKeys1 = SelectedKeys()
    val selector1 = Selector()
    val b = UdpNetSocket()
    b.blocking = false
    b.bind(InetSocketAddress.resolve(host = "127.0.0.1", port = 0))
    val udpChannel = selector1.attach(b)
    udpChannel.updateListenFlags(ListenFlags.READ)
    sendUdp(b.port)
    selector1.select(selectedKeys = selectKeys1, timeout = INFINITE)
    val event = selectKeys1.toList().first()
    assertTrue(ListenFlags.READ in event.flags)
    assertEquals(ListenFlags.ZERO, udpChannel.listenFlags)
  }

  @Test
  @Ignore
  fun testReselect() {
    val selectKeys1 = SelectedKeys()
    val selector1 = Selector()
    val b = UdpNetSocket()
    b.blocking = false
    b.bind(InetSocketAddress.resolve(host = "127.0.0.1", port = 0))
    val udpChannel = selector1.attach(b)
    udpChannel.updateListenFlags(ListenFlags.READ)
    val c = UdpNetSocket()
    c.blocking = true
    ByteBuffer(42).use { buffer ->
      c.send(buffer, InetSocketAddress.resolve(host = "127.0.0.1", port = b.port!!))
    }
    selector1.select(selectedKeys = selectKeys1, timeout = 1.seconds)
    assertEquals(1, selectKeys1.toList().count())
    selector1.select(selectedKeys = selectKeys1, timeout = 1.seconds)
    assertEquals(0, selectKeys1.toList().count())
  }

  @Deprecated(message = "")
  @Ignore
  @OptIn(ExperimentalTime::class)
  @Test
  fun reattachTest() {
    val port = UdpConnection.randomPort()

    fun sendDataWithDelay(
      port: Int,
      delay: Duration,
    ) {
      Thread {
        Thread.sleep(delay.inWholeMilliseconds)
        UdpNetSocket().use { channel ->
          ByteBuffer(30).use { buffer ->
            println("send tmp data")
            channel.send(buffer, InetSocketAddress.resolve(host = "127.0.0.1", port = port))
          }
        }
      }.start()
    }

    fun UdpNetSocket.skip(dataSize: Int) {
      val read =
        ByteBuffer(dataSize).use { buffer ->
          this.receive(buffer, null)
        }
      assertEquals(dataSize, read)
    }

    val selectKeys1 = SelectedKeys()
    val selector1 = Selector()

    val selectKeys2 = SelectedKeys()
    val selector2 = Selector()

    val server = UdpNetSocket()
    server.blocking = false
    var k1: SelectorKey? = null
    var k2: SelectorKey? = null
    k1 = selector1.attach(server)
    k1.updateListenFlags(ListenFlags.READ)
    k2 = selector2.attach(server)
    k2.updateListenFlags(ListenFlags.READ)
    server.bind(InetSocketAddress.resolve(host = "127.0.0.1", port = port))
    sendDataWithDelay(port = port, delay = 300.milliseconds)
    val vv1 = measureTimedValue { selector1.select(selectedKeys = selectKeys1, timeout = 1.seconds) }
//        val vv2 = measureTimedValue { selector2.select(selectedEvents = selectKeys2, timeout = 1000) }
    println("vv1=$vv1")
//        println("vv2=$vv2")
//        assertEquals(0, selectKeys1.count())
    selectKeys1.forEach {
      println("selectKeys1->$it ${k1 === it.key} ${k2 === it.key}")
    }
    selectKeys2.forEach {
      println("selectKeys2->$it ${k1 === it.key} ${k2 === it.key}")
    }
//        server.skip(30)
  }
}
