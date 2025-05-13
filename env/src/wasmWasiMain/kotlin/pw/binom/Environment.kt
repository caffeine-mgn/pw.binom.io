package pw.binom

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.TimeSource
import kotlin.wasm.unsafe.MemoryAllocator
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

actual val Environment.workDirectory: String
    get() = ""

@WasmImport("wasi_snapshot_preview1", "clock_time_get")
private external fun wasiRawClockTimeGet(clockId: Int, precision: Long, resultPtr: Int): Int

private const val CLOCKID_MONOTONIC = 1

@OptIn(UnsafeWasmMemoryApi::class)
private fun nanoTime(): Long = withScopedMemoryAllocator { allocator: MemoryAllocator ->
  val ptrTo8Bytes = allocator.allocate(8)
  val returnCode = wasiRawClockTimeGet(
    clockId = CLOCKID_MONOTONIC,
    precision = 1,
    resultPtr = ptrTo8Bytes.address.toInt()
  )
  check(returnCode == 0) { "clock_time_get failed with the return code $returnCode" }
  ptrTo8Bytes.loadLong()
}

actual val Environment.currentTimeMillis: Long
    get() = nanoTime().nanoseconds.inWholeMilliseconds

actual val Environment.currentTimeNanoseconds: Long
  get() = nanoTime()

actual fun Environment.getProperty(name: String): String? = null

actual val Environment.currentExecutionPath: String
    get() = ""
