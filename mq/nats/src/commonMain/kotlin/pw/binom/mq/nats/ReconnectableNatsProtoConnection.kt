package pw.binom.mq.nats

import kotlinx.coroutines.delay
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.coroutines.SimpleAsyncLock
import pw.binom.io.ByteBuffer
import pw.binom.io.ClosedException
import pw.binom.io.socket.SocketAddress
import pw.binom.mq.nats.client.*
import pw.binom.network.NetworkManager
import pw.binom.network.SocketClosedException
import pw.binom.network.SocketConnectException
import pw.binom.network.tcpConnect
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

class ReconnectableNatsProtoConnection(
  val address: SocketAddress,
  val clientName: String? = null,
  val lang: String = "kotlin",
  val echo: Boolean = true,
  val tlsRequired: Boolean = false,
  val version: String = "0.1.x",
  val headers: Boolean = true,
  val auth: Auth? = null,
  val readBufferSize: Int = DEFAULT_BUFFER_SIZE,
  val writeBufferSize: Int = DEFAULT_BUFFER_SIZE,
  val context: CoroutineContext = DefaultEmptyCoroutineContext,
  val networkManager: NetworkManager,
  val reconnectDelayConfig: ReconnectDelayConfig,
) : NatsProtoConnection {

  fun interface ReconnectDelayConfig {
    fun getDelay(tryCount: Int): Duration?
  }

  override var config: ConnectInfo = ConnectInfo(
    serverId = "",
    serverName = "",
    maxPayload = 0,
    clientId = 0,
    proto = 0,
  )
    private set

  private var existConnection: NatsProtoConnection? = null
  private var connectionLock = SimpleAsyncLock()
  private val closed = AtomicBoolean(false)

  private class Subscription(
    val subject: String,
    val group: String?,
  ) {
    var remaining = -1
  }

  private suspend fun getConnection(): NatsProtoConnection {
    connectionLock.lock()
    try {
      val con = existConnection
      if (con != null) {
        return con
      }

      var count = 0
      while (true) {
        if (closed.getValue()) {
          throw ClosedException()
        }
        val connection = try {
          networkManager.tcpConnect(address = address.resolve())
        } catch (e: SocketConnectException) {
          count++
          val delay = reconnectDelayConfig.getDelay(count)
          if (delay == null) {
            closed.setValue(true)
            throw e
          }
          delay(delay)
          continue
        }
        val nats = try {
          InternalNatsConnection.connect(
            channel = connection,
            clientName = clientName,
            lang = lang,
            echo = echo,
            tlsRequired = tlsRequired,
            version = version,
            headers = headers,
            auth = auth,
            readBufferSize = readBufferSize,
            writeBufferSize = writeBufferSize,
          )
        } catch (e: Throwable) {
          count++
          connection.asyncClose()
          val delay = reconnectDelayConfig.getDelay(count)
          if (delay == null) {
            closed.setValue(true)
            throw e
          }
          delay(delay)
          continue
        }
        if (!afterConnect(nats)) {
          nats.asyncCloseAnyway()
          continue
        }
        config = nats.config
        existConnection = nats
        return nats
      }
    } finally {
      connectionLock.unlock()
    }
  }

  private val subscriptions = HashMap<String, Subscription>()
  private val subscriptionsWaiting = HashMap<String, Subscription>()
  private val subscriptionsLock = SimpleAsyncLock()
  private val subscriptionsWaitingLock = SpinLock()

  private suspend fun afterConnect(conn: NatsProtoConnection): Boolean {
    try {
      subscriptions.forEach { (subscribeId, subscription) ->
        if (subscription.remaining == -1 || subscription.remaining > 0) {
          conn.subscribe(
            subscribeId = subscribeId,
            subject = subscription.subject,
            group = subscription.group,
          )
          if (subscription.remaining > 0) {
            conn.unsubscribe(
              subscribeId = subscribeId,
              afterMessages = subscription.remaining
            )
          }
        }
      }
      return true
    } catch (e: SocketClosedException) {
      return false
    }
  }

  override suspend fun subscribe(subject: String, group: String?, subscribeId: String) {
    subscriptionsLock.lock()
    try {
      if (subscriptions.containsKey(subscribeId)) {
        throw IllegalArgumentException("Subscription with id \"$subscribeId\" already exists")
      }

      while (true) {
        if (closed.getValue()) {
          throw ClosedException()
        }
        val con = getConnection()
        try {
          con.subscribe(subscribeId = subscribeId, subject = subject, group = group)
          break
        } catch (e: SocketClosedException) {
          disconnected()
          continue
        }
      }
      val subscription = Subscription(
        subject = subject,
        group = group,
      )
      subscriptions[subscribeId] = subscription
    } finally {
      subscriptionsLock.unlock()
    }
  }

  override suspend fun unsubscribe(subscribeId: String, afterMessages: Int) {
    require(afterMessages >= 0) { "afterMessages should be greater or equals than 0" }
    subscriptionsLock.lock()
    try {
      while (true) {
        if (closed.getValue()) {
          throw ClosedException()
        }
        val con = getConnection()
        try {
          con.unsubscribe(subscribeId = subscribeId, afterMessages = afterMessages)
          break
        } catch (e: SocketClosedException) {
          disconnected()
          continue
        }
      }

      if (afterMessages > 0) {
        val subscription = subscriptions[subscribeId]
        if (subscription != null) {
          subscription.remaining = afterMessages
          subscriptionsWaitingLock.synchronize {
            subscriptionsWaiting[subscribeId] = subscription
          }
        }
      } else {
        subscriptions.remove(subscribeId)
      }
    } finally {
      subscriptionsLock.unlock()
    }
  }

  private suspend fun disconnected() {
    connectionLock.synchronize {
      existConnection?.asyncClose()
      existConnection = null
    }
  }

  override suspend fun readMessage(): NatsMessage {
    while (true) {
      val con = getConnection()
      try {
        val msg = con.readMessage()
        subscriptionsWaitingLock.synchronize {
          val subscription = subscriptionsWaiting[msg.subscribeId]
          if (subscription != null) {
            if (subscription.remaining == 0) {
              subscriptionsWaiting.remove(msg.subscribeId)
              subscriptionsLock.synchronize {
                subscriptions.remove(msg.subscribeId)
              }
            } else {
              subscription.remaining--
            }
          }
        }
        return msg
      } catch (e: SocketClosedException) {
        disconnected()
      }
    }
  }

  override suspend fun publish(subject: String, replyTo: String?, headers: HeadersBody, data: ByteArray?) {
    while (true) {
      val con = getConnection()
      try {
        return con.publish(subject = subject, replyTo = replyTo, headers = headers, data = data)
      } catch (e: SocketClosedException) {
        disconnected()
        continue
      }
    }
  }

  override suspend fun publish(subject: String, replyTo: String?, headers: HeadersBody, data: ByteBuffer?) {
    while (true) {
      val con = getConnection()
      try {
        return con.publish(subject = subject, replyTo = replyTo, headers = headers, data = data)
      } catch (e: SocketClosedException) {
        disconnected()
        continue
      }
    }
  }

  override suspend fun asyncClose() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    disconnected()
  }
}
