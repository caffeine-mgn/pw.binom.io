package pw.binom.mq.nats.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import pw.binom.network.Network
import pw.binom.network.NetworkAddress
import pw.binom.nextUuid
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class NatsRawConnectionTest {

    @Test
    fun connectTest() {
        runBlocking {
            val client = Dispatchers.Network.tcpConnect(NetworkAddress.Immutable("127.0.0.1", TestUtils.NATS_PORT))
            val con = NatsRawConnection(
                channel = client
            )
            val serverInfo = con.prepareConnect(echo = true)
//            assertEquals(2, serverInfo.clusterAddresses.size)
            con.subscribe(Random.nextUuid().toString(), "S1", null)
            con.publish("S1", null, "Hello".encodeToByteArray())
            assertEquals("Hello", con.readMessage().data.decodeToString())
        }
    }
}

class DataRequest<T> {
    private var dataDone = false
    private var data: T? = null
    private var waiters = ArrayList<Continuation<T>>()
    fun set(data: T) {
        check(!dataDone)
        dataDone = true
        this.data = data
        waiters.forEach {
            it.resume(data)
        }
        waiters.clear()
    }

    suspend fun get(): T {
        if (dataDone) {
            return data as T
        }
        return suspendCoroutine {
            waiters.add(it)
        }
    }
}