package pw.binom.io.socket

import pw.binom.concurrency.sleep
import pw.binom.io.wrap
import pw.binom.thread.Thread
import kotlin.test.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.INFINITE
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class Tandem {
    val selector = Selector()
    val selected = SelectedKeys()

    fun attach(socket: Socket) = selector.attach(socket)

    fun select(duration: Duration = INFINITE): List<Event> {
        selector.select(
            timeout = duration,
            selectedKeys = selected
        )
        return selected.toList()
    }

    fun wakeup() {
        selector.wakeup()
    }
}

@OptIn(ExperimentalTime::class)
class SelectorTest {

    @Test
    fun attachAgain() {
        val tandem = Tandem()
        val client = Socket.createTcpClientNetSocket()
        client.blocking = false
        val first = tandem.attach(client)
        val second = tandem.attach(client)
        assertSame(first, second)
    }

    @Test
    fun attachBlocked() {
        val tandem = Tandem()
        val client = Socket.createTcpClientNetSocket()
        client.blocking = true
        try {
            tandem.attach(client)
            fail("Selector should throw IllegalArgumentException when attaching socket in blocking mode")
        } catch (e: IllegalArgumentException) {
            // Do nothing
        }
    }

    @Test
    fun deleteEventInDeferred() {
        val tandem = Tandem()
        val server = Socket.createTcpServerNetSocket()
        server.bind()
        val client = Socket.createTcpClientNetSocket()
        client.blocking = false
        val clientKey = tandem.attach(client)
        client.connect(server).assertInProgress()
        val serverClient = server.accept(null)
        Thread.sleep(0.5.seconds)
        clientKey.listenFlags = KeyListenFlags.READ or KeyListenFlags.ERROR
        Thread {
            sleep(1.seconds)
            println("Sleep finished!")
            clientKey.close()
            byteArrayOf(1).wrap { buf ->
                serverClient!!.send(buf)
            }
        }.start()
        val l = tandem.select()
        println("l=$l")
        assertTrue(l.isEmpty())
    }

    @Test
    fun wakeupTest() {
        val tandem = Tandem()
        val timeout = 1.seconds
        Thread {
            println("wait...")
            Thread.sleep(timeout)
            println("try call wakeup")
            tandem.wakeup()
        }.start()
        val (events, selectTime) = measureTimedValue {
            tandem.select(1.minutes)
        }
        assertTrue(selectTime > (timeout - 0.5.seconds) && selectTime < (timeout + 0.5.seconds))
        assertTrue(events.isEmpty())
    }

    @Test
    fun connectOneShotSuccessTest() {
        val tandem = Tandem()
        val client = Socket.createTcpClientNetSocket()
        client.blocking = false
        val key = tandem.selector.attach(client)
        key.listenFlags = KeyListenFlags.WRITE or KeyListenFlags.ONCE
        client.connect(httpServerAddress)
        val events = tandem.select()
        assertEquals(1, events.size)
        assertSame(key, events.first().key)
        assertTrue(events.first().flags and KeyListenFlags.ERROR == 0, "flag not contains error flag")
        assertTrue(events.first().flags and KeyListenFlags.WRITE != 0, "flag not contains write flag")
//        assertTrue(events.first().flags and KeyListenFlags.READ == 0, "flag not contains read flag")
        assertEquals(0, key.listenFlags)
        assertTrue(tandem.select(1.seconds).isEmpty(), "should contents no events")
    }

    @Test
    fun connectOneShotFailTest() {
        val tandem = Tandem()
        val client = Socket.createTcpClientNetSocket()
        client.blocking = false
        val key = tandem.selector.attach(client)
        key.listenFlags = KeyListenFlags.WRITE or KeyListenFlags.ERROR or KeyListenFlags.ONCE
        client.connect(NetworkAddress.create(host = "127.0.0.1", port = 1))
        println("#--1")
        val events = tandem.select()
        println("#--2")
        assertEquals(1, events.size)
        assertSame(key, events.first().key)
        println("events=$events")
        assertTrue(events.first().flags and KeyListenFlags.ERROR != 0)
        assertEquals(0, key.listenFlags)
        println("#--3")
        assertTrue(tandem.select(1.seconds).isEmpty())
        println("#--4")
        assertTrue(key.isClosed)
    }
}
