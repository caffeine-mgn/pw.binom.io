package pw.binom.io.socket

import pw.binom.InternalLog
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import java.nio.channels.SocketChannel
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration
import java.nio.channels.Selector as JvmSelector

private val SELECTOR_LOGGER = InternalLog.file("Selector")

actual class Selector : Closeable {
  private val native = JvmSelector.open()
  private val lock = ReentrantLock()

  private val eventImpl =
    object : pw.binom.io.socket.Event {
      var internalKey: SelectorKey? = null
      var internalFlag: ListenFlags = ListenFlags()
      override val key: SelectorKey
        get() = internalKey ?: throw IllegalStateException("key not set")
      override val flags: ListenFlags
        get() = internalFlag

      override fun toString(): String = this.buildToString()
    }

  actual fun attach(socket: Socket): SelectorKey {
    if (socket.blocking) {
      throw IllegalArgumentException("Socket in blocking mode")
    }
    val existKey = socket.native.keyFor(native)
    if (existKey != null) {
      SELECTOR_LOGGER.info(method = "attach") { "Socket ${System.identityHashCode(socket.native)} already attached" }
      return existKey.attachment() as SelectorKey
    }
    val jvmKey = socket.native.register(native, 0, null)
    val binomKey = SelectorKey(native = jvmKey, selector = this)
    jvmKey.attach(binomKey)
    SELECTOR_LOGGER.info(method = "attach") { "Socket ${System.identityHashCode(socket.native)} attached success" }
    return binomKey
  }

  private fun select(timeout: Duration): Int {
    native.selectedKeys().clear()
    return when {
      timeout.isInfinite() -> native.select()
      timeout == Duration.ZERO -> native.selectNow()
      else -> native.select(timeout.inWholeMilliseconds)
    }
  }

  actual fun select(
    timeout: Duration,
    selectedKeys: SelectedKeys,
  ) {
    lock.withLock {
      selectedKeys.lock.withLock {
        selectedKeys.errors.clear()
        val l = select(timeout = timeout)
        val list = ArrayList(native.selectedKeys())
        list.forEach { nativeKey ->
          val binomKey = nativeKey.attachment() as SelectorKey
          if (nativeKey.isConnectable) {
            val channel = nativeKey.channel() as? SocketChannel
            if (channel?.isConnectionPending == true) {
              try {
                channel.finishConnect()
                binomKey.isErrorHappened = false
              } catch (e: java.net.ConnectException) {
                binomKey.isErrorHappened = true
                binomKey.clearFlagsIfNeed()
                selectedKeys.errors += binomKey
                return@forEach
              }
            }
          }
          binomKey.isErrorHappened = false
          binomKey.clearFlagsIfNeed()
        }
        selectedKeys.selectedKeys = list
      }
    }
  }

  actual fun select(
    timeout: Duration,
    eventFunc: (pw.binom.io.socket.Event) -> Unit,
  ) {
    lock.withLock {
      SELECTOR_LOGGER.info(method = "select") { "Selecting...." }
      val selected =
        try {
          select(timeout = timeout)
        } catch (e: java.nio.channels.ClosedSelectorException) {
          SELECTOR_LOGGER.info(method = "select") { "Can't select. Selector closed" }
          throw ClosedException()
        }
      SELECTOR_LOGGER.info(method = "select") { "Selecting completed. Events count: $selected" }
      native.selectedKeys().forEach { nativeKey ->
        val binomKey = nativeKey.attachment() as SelectorKey
        eventImpl.internalKey = binomKey
        when {
          !nativeKey.isValid -> {
            SELECTOR_LOGGER.info(method = "select") { "Error happened on ${System.identityHashCode(binomKey.native.channel())}" }
            binomKey.isErrorHappened = false
            eventImpl.internalFlag = ListenFlags().withError.withRead.withWrite
            binomKey.clearFlagsIfNeed()
            eventFunc(eventImpl)
            return@forEach
          }

          nativeKey.isConnectable -> {
            val channel = nativeKey.channel() as? SocketChannel
            if (channel?.isConnectionPending == true) {
              try {
                channel.finishConnect()
                binomKey.isErrorHappened = false
                eventImpl.internalFlag = ListenFlags.WRITE + nativeKey.toCommonReadFlag()
                binomKey.clearFlagsIfNeed()
                SELECTOR_LOGGER.info(method = "select") { "Connecting ${System.identityHashCode(binomKey.native.channel())} finished success" }
                eventFunc(eventImpl)
              } catch (e: java.net.ConnectException) {
                SELECTOR_LOGGER.info(method = "select") { "Connecting ${System.identityHashCode(binomKey.native.channel())} finished with error" }
                binomKey.isErrorHappened = true
                eventImpl.internalFlag = ListenFlags.ERROR.withRead
                binomKey.clearFlagsIfNeed()
                eventFunc(eventImpl)
                return@forEach
              }
            }
          }

          else -> {
            eventImpl.internalFlag = nativeKey.toCommonReadFlag()
            SELECTOR_LOGGER.info(method = "select") {
              "Income event on ${binomKey.native.channel()::class.java.name}@${System.identityHashCode(binomKey.native.channel())}: ${
                commonFlagsToString(
                  eventImpl.internalFlag,
                )
              }"
            }
            binomKey.clearFlagsIfNeed()
            eventFunc(eventImpl)
          }
        }
        binomKey.isErrorHappened = false
      }
    }
  }

  actual fun wakeup() {
    SELECTOR_LOGGER.info(method = "wakeup") { "wakeup" }
    native.wakeup()
  }

  actual override fun close() {
    native.close()
  }

  private fun SelectorKey.clearFlagsIfNeed() {
    if (ListenFlags.ONCE in listenFlags) {
      clearNativeListenFlags()
    }
  }
}
