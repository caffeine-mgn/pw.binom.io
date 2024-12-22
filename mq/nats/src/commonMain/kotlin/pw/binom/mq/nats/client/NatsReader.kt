@file:Suppress("ktlint:standard:no-wildcard-imports")

package pw.binom.mq.nats.client

import kotlinx.coroutines.*
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.AsyncCloseable
import pw.binom.io.ByteBuffer
import pw.binom.mq.nats.client.NatsReader.IncomeMessage
import pw.binom.uuid.nextUuid
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.random.Random

class NatsReader
@OptIn(DelicateCoroutinesApi::class)
private constructor(
  val connection: NatsProtoConnection,
  private val incomeListener: IncomeMessage,
  private val scope: CoroutineScope = GlobalScope,
  private val context: CoroutineContext = EmptyCoroutineContext,
) : AsyncCloseable {
  fun interface IncomeMessage {
    companion object {
      val stub = IncomeMessage {}
    }

    suspend fun income(message: NatsMessage)
  }

  private val listeners = HashMap<String, IncomeMessage>()
  private val oneShotWatersLock = SpinLock()
  private val subscribeWatersLock = SpinLock()
  private val subscribeWaiters = HashMap<String, CancellableContinuation<NatsMessage>>()
  private val oneShotSubjectPrefix = "one-shot-" + Random.nextUuid().toShortString() + "-"
  private val subscribeSubjectPrefix = "subscribe-" + Random.nextUuid().toShortString() + "-"

  private val job =
    scope.launch(context) {
      while (isActive) {
        val msg = try {
          connection.readMessage()
        } catch (e: Throwable) {
          println("Nats connection closed! ${e::class}")
          e.printStackTrace()
          throw e
        }
        if (msg.subject.startsWith(oneShotSubjectPrefix)) {
          val con = oneShotWatersLock.synchronize { subscribeWaiters.remove(msg.subject) }
          if (con != null) {
            con.resume(msg.clone())
            continue
          }
        }
        if (msg.subscribeId.startsWith(subscribeSubjectPrefix)) {
          val listener = subscribeWatersLock.synchronize { listeners[msg.subscribeId] }
          if (listener != null) {
            scope.launch(context) {
              listener.income(msg)
            }
            continue
          }
        }
        scope.launch(context) {
          incomeListener.income(msg)
        }
      }
    }

  suspend fun subscribe(
    subject: String,
    group: String? = null,
    listener: IncomeMessage,
  ): AsyncCloseable {
    val subscribeId = subscribeSubjectPrefix + Random.nextUuid().toString()
    subscribeWatersLock.synchronize { listeners[subscribeId] = listener }
    connection.subscribe(
      subject = subject,
      group = group,
      subscribeId = subscribeId,
    )
    return AsyncCloseable {
      connection.unsubscribe(subscribeId = subscribeId)
    }
  }

  suspend fun sendAndReceive(
    subject: String,
    headers: HeadersBody = HeadersBody.empty,
    data: ByteArray?,
  ) = sendAndReceive { con, responseSubject ->
    con.publish(
      subject = subject,
      replyTo = responseSubject,
      headers = headers,
      data = data,
    )
  }

  suspend fun sendAndReceive(
    subject: String,
    headers: HeadersBody = HeadersBody.empty,
    data: ByteBuffer?,
  ) = sendAndReceive { con, responseSubject ->
    con.publish(
      subject = subject,
      replyTo = responseSubject,
      data = data,
      headers = headers,
    )
  }

  internal suspend fun waitMessage(subject: String): NatsMessage {
    val r = suspendCancellableCoroutine<NatsMessage> { con ->
      con.invokeOnCancellation {
        oneShotWatersLock.synchronize { subscribeWaiters.remove(subject) }
      }
      oneShotWatersLock.synchronize { subscribeWaiters[subject] = con }
    }
    return r
  }

  internal suspend inline fun sendAndReceive(crossinline func: suspend (NatsProtoConnection, String) -> Unit): NatsMessage {
    val responseSubject = oneShotSubjectPrefix + Random.nextUuid().toString()
    val subscribeId = "subscribe-" + Random.nextUuid().toString()
    connection.subscribe(
      subscribeId = subscribeId,
      subject = responseSubject,
    )
    connection.unsubscribe(subscribeId = subscribeId, afterMessages = 1)
    func(connection, responseSubject)
    return waitMessage(responseSubject)
  }

  /*
      internal suspend inline fun sendAndReceive(crossinline func: suspend (NatsConnection, String) -> Unit): NatsMessage {
        val responseSubject = oneShotSubjectPrefix + Random.nextUuid().toString()
        val subscribeId = "subscribe-" + Random.nextUuid().toString()
        connection.subscribe(
          subscribeId = subscribeId,
          subject = responseSubject,
        )
        connection.unsubscribe(id = subscribeId, afterMessages = 1)
        println("NATS:: SUCCESS SEND! Wait until end!")
        return suspendCancellableCoroutine {con->
          con.invokeOnCancellation {
            scope.launch(context) {
              oneShotWatersLock.synchronize { subscribeWaiters.remove(responseSubject) }
              connection.unsubscribe(id = subscribeId)
            }
          }
          scope.launch(context) {
            println("NATS:: one shot success!")
            oneShotWatersLock.synchronize { subscribeWaiters[responseSubject] = con }
            try {
              func(connection, responseSubject)
            } catch (e: Throwable) {
              oneShotWatersLock.synchronize { subscribeWaiters.remove(responseSubject) }
              con.resumeWithException(e)
            }
          }
        }
      }
  */
  val config
    get() = connection.config

  companion object {
    @OptIn(DelicateCoroutinesApi::class)
    fun start(
      con: NatsProtoConnection,
      scope: CoroutineScope = GlobalScope,
      context: CoroutineContext = EmptyCoroutineContext,
      incomeListener: IncomeMessage = IncomeMessage.stub,
    ) = NatsReader(
      connection = con,
      incomeListener = incomeListener,
      scope = scope,
      context = context,
    )
  }

  override suspend fun asyncClose() {
    job.cancelAndJoin()
    connection.asyncClose()
  }
}
