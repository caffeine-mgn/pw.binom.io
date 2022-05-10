package pw.binom.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.Worker
import pw.binom.concurrency.sleep
import pw.binom.concurrency.synchronize
import pw.binom.io.ByteBuffer
import pw.binom.io.use
import pw.binom.readByte
import pw.binom.writeByte
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class TcpConnectionTest {

    @Test
    fun serverFlagsOnAttachTest() {
        val selectKeys = SelectedEvents.create()
        val selector = Selector.open()
        val server = TcpServerSocketChannel()
        server.bind(NetworkAddress.Immutable("127.0.0.1", 0))
        server.setBlocking(false)
        selector.attach(
            server,
            Selector.INPUT_READY or Selector.EVENT_ERROR
        )
//        serverKey.listensFlag =
//            Selector.INPUT_READY or Selector.OUTPUT_READY or Selector.EVENT_CONNECTED or Selector.EVENT_ERROR
        val client = TcpClientSocketChannel()
        client.connect(NetworkAddress.Immutable("127.0.0.1", server.port!!))
        selector.select(selectedEvents = selectKeys)
        assertTrue(selectKeys.iterator().hasNext(), "No event for select. Socket selector should be read for input")
    }

    @Test
    fun autoCloseKey() = runTest(dispatchTimeoutMs = 10_000) {
        val serverConnection = Dispatchers.Network.bindTcp(NetworkAddress.Immutable("127.0.0.1", 0))
        val lock = SpinLock()
        lock.lock()
        launch(Dispatchers.Network) {
            val client = serverConnection.accept()
            lock.synchronize {
                client.close()
                println("Server Side: connection closed!")
            }
        }
        val selector = Selector.open()
        val channel = TcpClientSocketChannel()
        channel.setBlocking(false)
        val clientKey = selector.attach(
            channel,
            Selector.EVENT_ERROR or Selector.EVENT_CONNECTED or Selector.INPUT_READY or Selector.OUTPUT_READY
        )
        println("Connect...")
        channel.connect(NetworkAddress.Immutable("127.0.0.1", serverConnection.port))
        println("Connect started!")
        val selectKeys = SelectedEvents.create()
        LOOP_CONNECT@ while (true) {
            selector.select(selectedEvents = selectKeys)
            val o = selectKeys.iterator()
            while (o.hasNext()) {
                val e = o.next()
                if (e.mode and Selector.EVENT_CONNECTED > 0) {
                    println("Connected!")
                    break@LOOP_CONNECT
                }
            }
        }
//        clientKey.listensFlag =
//            Selector.EVENT_ERROR or Selector.EVENT_CONNECTED or Selector.INPUT_READY or Selector.OUTPUT_READY
        assertEquals(1, selector.getAttachedKeys().size)
        lock.unlock()
        clientKey.listensFlag = Selector.INPUT_READY or Selector.EVENT_ERROR
        val list = SelectedEvents.create()
        var c = 0
        val buffer = ByteBuffer.alloc(512)
        LOOP_ERROR@ while (true) {
            println("Selecting...")
            val eventCount = selector.select(selectedEvents = list)
            println("Selected! eventCount=$eventCount")
            val o = list.iterator()
            while (o.hasNext()) {
                c++
                if (c > 100) {
                    break@LOOP_ERROR
                }
                val e = o.next()

                if (e.mode and Selector.INPUT_READY > 0) {
                    println("Try read...")
                    val count = channel.read(buffer)
                    assertEquals(-1, count)
                    break@LOOP_ERROR
                    println("Ready for connect. count=$count")
                }

                println("Event! ${e.mode.toString(2)}")
                if (e.mode and Selector.EVENT_ERROR > 0) {
                    println("Error!!")
                    break@LOOP_ERROR
                }
            }
        }
        assertEquals(0, selector.getAttachedKeys().size)

//        try {
//            clientKey.close()
//            fail()
//        } catch (e: ClosedException) {
//            // Do nothing
//        }
    }

    @Test
    @Ignore
    fun writeErrorTest() = runTest {
        val nd = NetworkCoroutineDispatcherImpl()
        val port = TcpServerConnection.randomPort()
        val address = NetworkAddress.Immutable("127.0.0.1", port)
        val worker = Worker()
        val spinLock = SpinLock()
        val server = nd.bindTcp(address)
        sleep(500)
        val client = nd.tcpConnect(address)
        launch {
            try {
                spinLock.synchronize {
                    println("Try read...")
                    val readed = ByteBuffer.alloc(5).use {
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
        ByteBuffer.alloc(10).use { buf ->
            remoteClient.write(buf)
            withContext(worker) {
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
    fun waitWriteTest() = runTest {
        val nd = NetworkCoroutineDispatcherImpl()
        val port = TcpServerConnection.randomPort()

        val worker = Worker()

        val r = launch {
            val server = nd.bindTcp(NetworkAddress.Immutable(host = "127.0.0.1", port = port))
            val newClient = server.accept()
            withContext(worker) {
                println("Wait send...")
                println("Wait net thread...")
                ByteBuffer.alloc(10).use { buf ->
                    newClient.writeByte(buf, 42)
                    newClient.flush()
                    println("wrote!")
                }
            }
        }

        val r2 = launch {
            val client = nd.tcpConnect(NetworkAddress.Immutable(host = "127.0.0.1", port = port))
            ByteBuffer.alloc(10).use { b ->
                println("Reading...")
                assertEquals(42, client.readByte(b))
                println("Read")
            }
        }
        r.join()
        r2.join()
    }

    @Test
    fun connectTest() = runTest(dispatchTimeoutMs = 10_000) {
        val con = Dispatchers.Network.tcpConnect(
            NetworkAddress.Immutable(
                host = "ya.ru", port = 443
            )
        )
        con.close()
    }
}
