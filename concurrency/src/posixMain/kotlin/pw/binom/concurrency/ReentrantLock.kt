package pw.binom.concurrency

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.internal.createCleaner
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds

@OptIn(ExperimentalForeignApi::class)
private inline val kotlin.concurrent.AtomicNativePtr.cc
  get() = this.value.reinterpret<pthread_cond_t>()!!

@OptIn(ExperimentalForeignApi::class)
private inline val kotlin.concurrent.AtomicNativePtr.mm
  get() = this.value.reinterpret<pthread_mutex_t>()!!

private const val checkTime = 100

@OptIn(ExperimentalForeignApi::class)
fun <T : NativePointed> NativePtr.reinterpret() = interpretNullablePointed<T>(this)

@OptIn(ExperimentalStdlibApi::class, ExperimentalForeignApi::class, UnsafeNumber::class)
actual class ReentrantLock : Lock {

  private val native = malloc(sizeOf<pthread_mutex_t>().convert())!!.reinterpret<pthread_mutex_t>()

  init {
    internalPthread_mutex_init(native, null)
  }

  @OptIn(ExperimentalNativeApi::class)
  private val cleaner = kotlin.native.ref.createCleaner(native) { native ->
    pthread_mutex_destroy(native)
    free(native)
  }

  override fun tryLock(): Boolean {
    val r = pthread_mutex_trylock(native)
    if (r == EBUSY) {
      return false
    }
    if (r != 0) {
      error("Can't lock mutex")
    }
    return true
  }

  override fun lock() {
    if (pthread_mutex_lock(native) != 0) {
      error("Can't lock mutex")
    }
  }

  override fun unlock() {
    if (pthread_mutex_unlock(native) != 0) {
      error("Can't unlock mutex")
    }
  }

  actual fun newCondition() = Condition(native)

  actual class Condition(val mutex: CPointer<pthread_mutex_t>) {
    //        val native = cValue<pthread_cond_t>()//nativeHeap.alloc<pthread_cond_t>()//malloc(sizeOf<pthread_cond_t>().convert())!!.reinterpret<pthread_cond_t>()
    val native =
      malloc(sizeOf<pthread_cond_t>().convert())!!.reinterpret<pthread_cond_t>() // malloc(sizeOf<pthread_cond_t>().convert())!!.reinterpret<pthread_cond_t>()

    init {
      if (internalPthread_cond_init(native, null) != 0) {
        free(native)
        error("Can't init Condition")
      }
    }

    private val cleaner = createCleaner(native) { native ->
      pthread_cond_destroy(native)
      free(native)
    }

    actual fun await() {
      pthread_cond_wait(native, mutex)
    }

    actual fun signal() {
      pthread_cond_signal(native)
    }

    actual fun signalAll() {
      pthread_cond_broadcast(native)
    }

    actual fun await(duration: Duration): Boolean {
      if (duration.isInfinite()) {
        await()
        return true
      }
      return memScoped<Boolean> {
        val now = alloc<timeval>()
        val waitUntil = alloc<timespec>()
        internalGettimeofday(now.ptr, null)
        waitUntil.set(now, duration)
        while (true) {
          val r = pthread_cond_timedwait(native, mutex, waitUntil.ptr)
          if (Worker.current?.isInterrupted == true) {
            throw InterruptedException()
          }
          if (r == ETIMEDOUT) {
            return@memScoped false
          }
          if (r == 0) {
            return@memScoped true
          }
          throw TODO()
        }
        false
      }
    }
  }
}

@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
private fun timespec.set(base: timeval, diff: Duration) {
  val nsecDiff = base.tv_usec.microseconds + diff

  tv_sec = base.tv_sec + nsecDiff.inWholeSeconds.toInt()
  tv_nsec = (nsecDiff.inWholeNanoseconds - nsecDiff.inWholeSeconds * 1_000_000_000).convert()
}
