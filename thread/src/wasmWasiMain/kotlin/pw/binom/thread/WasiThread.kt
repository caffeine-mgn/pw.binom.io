package pw.binom.thread

import pw.binom.wasm.wasi.*
import kotlin.time.Duration
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

internal val threadsForStart = HashMap<Int, Thread>()

@WasmImport(module = "wasi", name = "thread_id")
internal external fun wasiThreadId(): Int

@WasmExport("wasi_thread_start")
private fun wasiThreadStart(threadId: Int, arg: Int) {
  val thread = threadsForStart.remove(arg)!!
  thread.internalId = threadId
  thread.realStart()
}

@OptIn(UnsafeWasmMemoryApi::class)
internal fun wasm_sleep(duration: Duration): Boolean {
  withScopedMemoryAllocator { allocator ->
    val sPtr = allocator.allocate(__wasi_subscription_t.SIZE_IN_BYTES)
    __wasi_subscription_t(sPtr).apply {
      userdata = 0u
      u.tag = EventType.__WASI_EVENTTYPE_CLOCK
      u.u.clock.apply {
        id = __WASI_CLOCKID_REALTIME
        timeout = duration.inWholeNanoseconds.toULong()
        precision = 0u
        flags = __WASI_SUBCLOCKFLAGS_SUBSCRIPTION_CLOCK_ABSTIME
      }
    }

    val ePtr = allocator.allocate(__wasi_event_t.SIZE_IN_BYTES)
    val outEventCount = allocator.allocate(4)
    val r = wasi_snapshot_preview1_poll_oneoff(
      subscription = sPtr.address,
      outEvent = ePtr.address,
      subscriptionsCount = 1,
      eventCount = outEventCount.address,
    )
    if (r != 0) {
      return false
    }
    val count = outEventCount.loadInt()
    if (count == 0) {
      return false
    }
    return __wasi_event_t(ePtr).error == 0.toUShort()
  }
}
