@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package pw.binom.signal

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.posix.SIGINT
import platform.posix.SIGTERM
import platform.posix.usleep
import kotlin.concurrent.AtomicInt

private const val SIGBREAK = 21

private var signalListening = AtomicInt(0)
private val listeners = ArrayList<(Signal.Type) -> Unit>()
private val listenersLock = AtomicInt(0)

private fun lockListeners() {
  while (true) {
    if (listenersLock.compareAndSet(0, 1)) {
      break
    }
    usleep((1 * 1000).toUInt())
  }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun addSignalListener(func: (Signal.Type) -> Unit) {
  if (signalListening.compareAndSet(0, 1)) {
    platform.posix.signal(SIGINT, signalListener)
    platform.posix.signal(SIGBREAK, signalListener)
    platform.posix.signal(SIGTERM, signalListener)
  }
  lockListeners()
  listeners += func
  listenersLock.value = 0
}

private val signalListener = staticCFunction<Int, Unit> handler@{ signal ->
  val type = when (signal) {
    SIGINT -> Signal.Type.Sigint
    SIGBREAK -> Signal.Type.Sigbreak
    SIGTERM -> Signal.Type.Sigterm
    else -> return@handler
  }

  lockListeners()
  try {
    listeners.forEach {
      it(type)
    }
  } finally {
    listenersLock.value = 0
  }
  return@handler
}
