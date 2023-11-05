package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.common.*
import platform.common.Event
import pw.binom.collections.ArrayList2
import pw.binom.collections.defaultMutableMap
import pw.binom.collections.defaultMutableSet
import pw.binom.concurrency.ReentrantLock
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.Closeable
import kotlin.time.*

@OptIn(ExperimentalForeignApi::class)
internal val STUB_BYTE = byteArrayOf(1).pin()
private const val MAX_ELEMENTS = 1042

@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(UnsafeNumber::class, ExperimentalTime::class, ExperimentalForeignApi::class)
actual class Selector : Closeable {
  private val selectLock = ReentrantLock()

  //    internal val keyForRemove = LinkedList<SelectorKey>()
  private val errorsRemove = defaultMutableSet<SelectorKey>()
  private val errorsRemoveLock = SpinLock()
  private val keysLock = SpinLock()

  //    private val keyForRemoveLock = SpinLock("Selector::keyForRemoveLock")
  internal val epoll = Epoll.create(1024)
  private val native = createSelectedList(MAX_ELEMENTS)!!
  internal val eventMem = mallocEvent()!!
  internal val eventMemLock = SpinLock()

  //    internal var pipeRead: Int = 0
//    internal var pipeWrite: Int = 0
  private val fdToKey = defaultMutableMap<RawSocket, SelectorKey>()

  internal inline fun <T> usingEventPtr(func: (CPointer<Event>) -> T): T =
    eventMemLock.synchronize { func(eventMem) }

  private val eventImpl = object : pw.binom.io.socket.Event {
    var internalKey: SelectorKey? = null
    var internalFlag = ListenFlags()
    override val key: SelectorKey
      get() = internalKey ?: throw IllegalStateException("key not set")
    override val flags
      get() = internalFlag

    override fun toString(): String = this.buildToString()
  }

  private val epollInterceptor = EpollInterceptor(this)

//    init {
//        val fds = createPipe()
//        pipeRead = fds.first
//        pipeWrite = fds.second
//
//        val r = usingEventPtr { eventMem ->
//            setEventDataPtr(eventMem, null)
//            setEventFlags(eventMem, FLAG_READ, 0)
//            epoll.add(pipeRead, eventMem)
//        }
//        if (r != Epoll.EpollResult.OK) {
//            platform.posix.close(pipeRead)
//            platform.posix.close(pipeWrite)
//            epoll.close()
//            throw IOException("Can't init epoll. Can't add default pipe. Status: $r")
//        }
//    }

  actual fun attach(socket: Socket): SelectorKey {
    if (socket.blocking) {
      throw IllegalArgumentException("Socket in blocking mode")
    }
    val previous = keysLock.synchronize {
      fdToKey[socket.native]
    }
    if (previous != null) {
      return previous
    }
    val key = SelectorKey(selector = this, socket = socket)
    key.serverFlag = socket.server
    epoll.add(socket.native, key.eventMem)
//        usingEventPtr { eventMem ->
//            setEventDataFd(eventMem, socket.native)
//            setEventFlags(eventMem, 0, 0)
//            epoll.add(socket.native, eventMem)
//        }
    keysLock.synchronize {
      val previousValue = fdToKey.put(socket.native, key)
      if (previousValue != null) {
        errorsRemoveLock.synchronize {
          errorsRemove += previousValue
        }
      }
    }
    return key
//        return keysLock.synchronize {
//            var existKey = fdToKey[socket.native]
//            if (existKey != null && existKey.isClosed) {
//                errorsRemoveLock.synchronize {
//                    errorsRemove += existKey!!
//                }
//                existKey = null
//            }
//            if (existKey != null) {
//                existKey
//            } else {
//                val key = SelectorKey(selector = this, socket = socket)
//                key.serverFlag = socket.server
//                usingEventPtr { eventMem ->
//                    setEventDataFd(eventMem, socket.native)
//                    setEventFlags(eventMem, 0, 0)
//                    epoll.add(socket.native, eventMem)
//                }
//                val previousValue = fdToKey.put(socket.native, key)
//                if (previousValue != null) {
//                    errorsRemoveLock.synchronize {
//                        errorsRemove += previousValue
//                    }
//                }
//                key
//            }
//        }
  }

  // lock errorsRemoveLock
  internal fun updateKey(key: SelectorKey, event: CPointer<Event>): Boolean {
    if (epoll.update(key.socket, event)) {
      return true
    }
    return false
//        if (!epoll.update(key.socket, event)) {
// //            println("Selectoe::updateKey fail update ${key.rawSocket}. Added for error list")
//            errorsRemoveLock.synchronize {
//                errorsRemove.add(key)
//            }
//        }
  }

  internal fun removeKey(key: SelectorKey) {
    epoll.delete(key.socket, failOnError = false)

    val removeTime = keysLock.synchronize() {
      measureTime {
        fdToKey.remove(key.rawSocket)
      }
    }
    key.internalClose()
//        return
//        if (selectingLock.tryLock()) {
//            try {
// //                println("Selector:: removeKeyNow ${key.rawSocket}")
//                val removeResult = epoll.delete(key.socket, failOnError = false, operation = "removeKey")
//                keysLock.synchronize {
//                    val removed = fdToKey.remove(key.rawSocket)
// //                    println("Selector::removeKey fd: ${key.rawSocket}, removed: $removed")
// //                    println("Remove new $key ${key.identityHashCode()} fd=${key.rawSocket}! $removeResult!")
//                }
//                key.internalClose()
//            } finally {
//                selectingLock.unlock()
//            }
//        } else {
// //            println("Add for remove later $key ${key.identityHashCode()} fd=${key.rawSocket}!")
//            keyForRemoveLock.synchronize {
// //                println("Selector::removeKey Can't remove now. Added to keyForRemove. fd: ${key.rawSocket}")
// //                println("Selector:: removeKeyLater ${key.event.data.ptr}")
//                println("Selector::removeKey. Add to keyForRemove. #4. fd: ${key.socket}")
//                keyForRemove.add(key)
//            }
//        }
  }

  private fun prepareList(selectedKeys: MutableList<SelectorKey>, count: Int) {
    when (selectedKeys) {
      is ArrayList -> {
        selectedKeys.clear()
        selectedKeys.ensureCapacity(count + errorsRemove.size)
      }

      is ArrayList2 -> selectedKeys.prepareCapacity(count + errorsRemove.size)
      else -> selectedKeys.clear()
    }
  }

  private fun cleanupPostProcessing(
    selectedKeys: MutableList<SelectorKey>,
    count: Int,
  ) {
    prepareList(selectedKeys = selectedKeys, count = count)
    cleanupPostProcessing(
      count = count,
    ) { key ->
      selectedKeys.add(key)
    }
  }

  /**
   * errorsRemoveLock {
   *   //----//
   * }
   */
  private fun cleanupPostProcessing(
    count: Int,
    func: (SelectorKey) -> Unit,
  ) {
    var currentNum = 0
    errorsRemoveLock.synchronize {
      errorsRemove.forEach { key ->
        key.internalReadFlags = ListenFlags.ERROR
        func(key)
//                key.internalClose()
      }
      errorsRemove.forEach { key ->
//                println("Selector::cleanupPostProcessing. Add to keyForRemove. #3. fd: ${key.socket}")
        removeKey(key)
      }

//            keyForRemove.addAll(errorsRemove)
      errorsRemove.clear()
    }
    while (currentNum < count) {
      val event = getEventFromSelectedList(native, currentNum++)
      val ptr = getEventDataPtr(event)
      if (ptr == null) {
        interruptWakeup()
//                println("Selector::cleanupPostProcessing Event for interrupted!")
        continue
      }
      val socketFd = getEventDataFd(event)
      val key = fdToKey[socketFd]
      if (key == null) {
        val r = epoll.delete(socketFd)
//                val flagsStr = commonFlagsToString(getEventFlags(event))
//                println("Selector::cleanupPostProcessing Got event with key null. fd: $socketFd. result: $r, flagsStr: $flagsStr, pipeRead: $pipeRead, pipeWrite: $pipeWrite")
        continue
      }
      key ?: continue
      val flags = getEventFlags(event)
      if (key.isClosed) {
        val r = epoll.delete(key.socket, failOnError = false)
//                println("Alarm! Event for closed key! $key ${key.identityHashCode()} fd=${getEventDataFd(event)}!")
//                val flagsStr = commonFlagsToString(flags)
//                val removed = keysLock.synchronize {
//                    fdToKey.remove(socketFd)
//                }
//                println("Selector::cleanupPostProcessing. Add to keyForRemove. #2 got event with closed key. socket: ${key.socket}, result: $r, flags: $flagsStr")
//                errorsRemoveLock.synchronize("cleanupPostProcessing-1") {
        removeKey(key)
//                    keyForRemove.add(key)
//                }
        key.internalReadFlags = ListenFlags.ERROR
        func(key)
//                key.internalClose()
        continue
      }
      var e = ListenFlags()
      val listenFlags = flags
//            val flagsStr = commonFlagsToString(flags)
      if (listenFlags and FLAG_ERROR != 0) {
//                errorsRemoveLock.synchronize("cleanupPostProcessing") {
//                    println("Selector::cleanupPostProcessing. Add to keyForRemove. #1. socket: ${key.socket}")
        removeKey(key)
//                    keyForRemove.add(key)
//                }
        e = e.withError
      }

//            if (event.events.toInt() and EPOLLERR.toInt() != 0 || event.events.toInt() and EPOLLHUP.toInt() != 0) {
//                e = e or KeyListenFlags.ERROR
//            }
      if (listenFlags and FLAG_WRITE != 0) {
        e = e.withWrite
      }
      if (listenFlags and FLAG_READ != 0) {
        e = e.withRead
      }
      if (listenFlags and FLAG_ONCE != 0) {
        e = e.withOnce
      }
      key.internalReadFlags = e
//            println("Selector::cleanupPostProcessing Event. socket: ${key.socket}, flags: $flagsStr")
      if (key.internalListenFlags and KeyListenFlags.ONCE != 0) {
        key.internalListenFlags = 0
      }
      func(key)
    }
  }

  private fun select(timeout: Duration) = epoll.select(
    events = native,
    timeout = if (timeout.isInfinite()) -1 else timeout.inWholeMilliseconds.toInt(),
  )

  actual fun select(timeout: Duration, selectedKeys: SelectedKeys) {
    selectLock.synchronize {
      selectedKeys.lock.synchronize {
        val eventCount = select(timeout = timeout)
        cleanupPostProcessing(
          selectedKeys = selectedKeys.selectedKeys,
//                    native = selectedKeys.native,
          count = eventCount,
//                    errors = selectedKeys.errors,
        )
        selectedKeys.selected(eventCount)
      }
    }
  }

  actual fun select(timeout: Duration, eventFunc: (pw.binom.io.socket.Event) -> Unit) {
    selectLock.synchronize {
      val eventCount = select(timeout = timeout)
      cleanupPostProcessing(
        count = eventCount,
      ) { key ->
        eventImpl.internalFlag = key.readFlags
        eventImpl.internalKey = key
        eventFunc(eventImpl)
      }
    }
  }

  override fun close() {
    selectLock.synchronize {
      epollInterceptor.close()
      usingEventPtr { eventMem ->
        freeEvent(eventMem)
      }
      closeSelectedList(native)
      epoll.close()
    }
  }

  actual fun wakeup() {
    epollInterceptor.wakeup()
  }

  internal fun interruptWakeup() {
    epollInterceptor.interruptWakeup()
  }
}
