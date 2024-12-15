package pw.binom.mq.nats

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import kotlin.coroutines.resume

internal class SyncPoint {
  companion object {
    private const val FREE = 0
    private const val LOCKED = 1
    private const val UNLOCKED = 2
  }

  private var status = 0
  private val lock = SpinLock()
  private var water: CancellableContinuation<Unit>? = null

  fun release() {
    lock.synchronize {
      when (status) {
        FREE -> {
          status = UNLOCKED
          null
        }

        LOCKED -> {
          status = UNLOCKED
          water
        }

        else -> throw IllegalStateException()
      }
    }?.resume(Unit)
  }

  suspend fun wait() {
    lock.lock()
    when (status) {
      FREE -> suspendCancellableCoroutine {
        water = it
        status = LOCKED
        lock.isLocked
      }

      UNLOCKED -> {
        lock.unlock()
        status = FREE
      }

      else -> throw IllegalStateException()
    }
    status = FREE
  }
}
