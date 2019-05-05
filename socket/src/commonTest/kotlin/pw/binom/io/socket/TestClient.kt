package pw.binom.io.socket

import pw.binom.Thread
import pw.binom.io.*
import pw.binom.job.*
import kotlin.test.*

class TestClient {
    @Test
    fun `unknown dns host`() {
        val client = Socket()
        val hostName = "unknown_host"
        try {
            client.connect(hostName, 23)
            fail()
        } catch (e: UnknownHostException) {
            assertEquals(e.message, hostName)
            //NOP
        }
    }

    @Test
    fun `invalid port`() {
        val client = Socket()
        val hostName = "127.0.0.1"
        val port = 0xFFFF + 10
        try {
            client.connect(hostName, port)
            fail()
        } catch (e: IllegalArgumentException) {
            assertEquals(e.message, "port out of range:$port")
            //NOP
        }
    }

    @Test
    fun `unknown port`() {
        val client = Socket()
        val hostName = "127.0.0.1"
        val port = 0xFFFF - 1
        try {
            client.connect(hostName, port)
            fail()
        } catch (e: ConnectException) {
            assertEquals(e.message, "Connection refused: connect")
        }
    }

    @Test
    fun `unknown ip`() {
        val client = Socket()
        val hostName = "127.0.0.2"
        val port = 0xFFFF - 1

        try {
            client.connect(hostName, port)
            fail()
        } catch (e: ConnectException) {
            assertEquals(e.message, "Connection refused: connect")
        }
    }

    @Test
    fun `disconnect on read`() {
        class TaskImlp(val promise: Promise<Unit>) : Task() {
            override fun execute() {
                val server = SocketServer()
                try {
                    server.bind(9919)
                    promise.resume(Unit)
                    val remoteClient = server.accept()!!
                    Thread.sleep(100)
                    remoteClient.close()
                } finally {
                    server.close()
                }
            }
        }

        val p = Promise<Unit>()
        Worker.execute { TaskImlp(p) }
        p.await()
        val client = Socket()
        try {
            client.connect("127.0.0.1", 9919)
            client.read()
            fail()
        } catch (e: SocketClosedException) {
            //NOP
        }
        assertFalse(client.connected)
    }

    @Test
    fun `disconnect on write`() {
        class TaskImlp(val promise: Promise<Unit>) : Task() {
            override fun execute() {
                val server = SocketServer()
                try {
                    server.bind(9919)
                    promise.resume(Unit)
                    val remoteClient = server.accept()!!
                    remoteClient.close()
                } finally {
                    server.close()
                }
            }
        }

        val p = Promise<Unit>()
        Worker.execute { TaskImlp(p) }
        p.await()
        val client = Socket()
        client.connect("127.0.0.1", 9919)
        Thread.sleep(100)
        assertTrue(client.connected)
    }

    @Test
    fun `write to closed socket`(){
        val socket = Socket()
        socket.close()

        try {
            socket.write(0)
            fail()
        } catch (e:SocketClosedException){
            //NOP
        }
    }

    @Test
    fun `write to not connected socket`(){
        val socket = Socket()

        try {
            socket.write(0)
            fail()
        } catch (e: IOException){
            //NOP
        }
    }
}