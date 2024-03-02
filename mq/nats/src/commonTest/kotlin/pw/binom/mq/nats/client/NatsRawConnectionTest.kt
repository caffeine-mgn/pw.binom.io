package pw.binom.mq.nats.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.io.socket.InetNetworkAddress
import pw.binom.io.useAsync
import pw.binom.mq.nats.client.dto.PullRequestOptionsDto
import pw.binom.mq.nats.client.dto.StorageType
import pw.binom.mq.nats.client.dto.StreamConfig
import pw.binom.network.Network
import pw.binom.network.tcpConnect
import pw.binom.uuid.nextUuid
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class NatsRawConnectionTest {
  fun testing(func: suspend () -> Unit) =
    runTest {
      withContext(Dispatchers.Default) {
        func()
      }
    }

  suspend fun tcpConnect() = Dispatchers.Network.tcpConnect(InetNetworkAddress.create("127.0.0.1", TestUtils.NATS_PORT))

  suspend fun natsConnect() =
    InternalNatsConnection.connect(
      channel = LoggingAsyncChannel(tcpConnect()),
    )

  suspend fun natsReader(incomeMessage: suspend (nats: InternalNatsConnection, message: NatsMessage) -> Unit): NatsReader {
    val con = natsConnect()
    return NatsReader.start(
      con = con,
      incomeListener = { msg ->
        incomeMessage(con, msg)
      },
    )
  }

  fun connection(func: suspend (NatsConnection) -> Unit) =
    testing {
      Dispatchers.Network.tcpConnect(InetNetworkAddress.create("127.0.0.1", TestUtils.NATS_PORT))
        .useAsync { client ->
          InternalNatsConnection.connect(
            channel = client,
          ).useAsync {
            func(it)
          }
        }
    }

  @Test
  fun sendReceiveMsgWithoutHeader() =
    testing {
      val subject = Random.nextUuid().toShortString()
      val replyTo = Random.nextUuid().toShortString()
      val data = Random.nextBytes(70)
      natsConnect().useAsync { nats ->
        nats.subscribeEx(subject = subject)
        nats.publish(
          subject = subject,
          data = data,
          replyTo = replyTo,
        )
        val msg = nats.readMessage()
        assertEquals(subject, msg.subject)
        assertEquals(replyTo, msg.replyTo)
        assertContentEquals(data, msg.data)
      }
    }

  @Test
  fun sendReceiveMsgWithHeader() =
    testing {
      val subject = Random.nextUuid().toShortString()
      val replyTo = Random.nextUuid().toShortString()
      val data = Random.nextBytes(70)
      val header =
        NatsHeaders.build {
          add("Header", "X")
          add("Header1", "X1")
          add("Header1", "X2")
        }.toHeadersBody()
      natsConnect().useAsync { nats ->
        nats.subscribeEx(subject = subject)
        nats.publish(
          subject = subject,
          data = data,
          replyTo = replyTo,
          headers = header,
        )
        val msg = nats.readMessage()
        assertEquals(subject, msg.subject)
        assertEquals(replyTo, msg.replyTo)
        assertContentEquals(header.bytes, msg.headersBody.bytes)
        assertContentEquals(data, msg.data)
      }
    }

  @Test
  fun ttt() =
    testing {
      val reader =
        natsReader { nats, msg ->
          println("--->INPUT $msg")
          if (msg.replyTo != null) {
            nats.publish(subject = msg.replyTo!!, data = byteArrayOf(1, 1, 1))
          }
        }
      reader.connection.subscribeEx(subject = "lololol")
      val resp = reader.sendAndReceive(subject = "lololol", data = byteArrayOf(0, 0, 0))
      println("resp: $resp")
    }

  @Test
  fun createJSStream() =
    testing {
      val reader =
        natsReader { nats, msg ->
          println("Message got!")
        }
      val js = JetStreamImpl(reader)
      val streamName = "test"
      val consumeName = "consume-consumer"
      val streamSubject = "ololo"
      val stream =
        js.create(
          StreamConfig(
            name = streamName,
            storageType = StorageType.Memory,
            subjects = listOf(streamSubject),
            noAck = false,
          ),
        )
      reader.connection.publish(
        subject = streamSubject,
        data = "HELLO".encodeToByteArray(),
      )
      val consumer =
        js.createConsumer(
          streamName = streamName,
          config =
            ConsumerConfiguration(
              durableName = consumeName,
              name = consumeName,
              ackPolicy = AckPolicy.ALL,
            ),
        )
      val tmpO = "fffff"
      reader.subscribe(tmpO) {
        val replyTo = it.replyTo
        if (replyTo != null) {
          js.sendAck(
            subject = replyTo,
          )
        }
        println("Income $it")
      }
      val info1 =
        js.getConsumerInfo(
          streamName = streamName,
          consumerName = consumer.name,
        )
      println("consumer info: delivered=${info1.delivered} ackFloor=${info1.ackFloor}")
      js.pullMessages(
        streamName = stream.config.name,
        consumerName = consumer.name,
        into = tmpO,
        config = PullRequestOptionsDto(batch = 50),
      )
      delay(5.seconds)
      val info2 =
        js.getConsumerInfo(
          streamName = streamName,
          consumerName = consumer.name,
        )
      println("consumer info: delivered=${info2.delivered} ackFloor=${info2.ackFloor}")
//      js.update(
//        StreamConfig(
//          name = "test",
//          storageType = StorageType.Memory,
//          subjects = listOf("ololo"),
//        ),
//      )
//      val e = jsm.getStreamInfoAll(stream.config.name)
//      println("messages===>${e.state?.messages}")
//      println("Stream: $e")
//      js.delete("test")
//      println("new Stream: $bb")
    }

  @Test
  fun connectTest() {
    runBlocking {
      val client = Dispatchers.Network.tcpConnect(InetNetworkAddress.create("127.0.0.1", TestUtils.NATS_PORT))
      val con =
        NatsRawConnection(
          channel = client,
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
