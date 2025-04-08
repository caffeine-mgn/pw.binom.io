package pw.binom.wasm.wasi

import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi

@WasmImport(module = "wasi", name = "thread-spawn")
external fun wasiThreadSpawn(arg: Int): Int

@WasmImport(module = "wasi_snapshot_preview1", name = "sched_yield")
external fun wasiSchedYield(): Int


/**
 * @param subscription pointer to `__wasi_subscription_t`
 * @param outEvent pointer to `__wasi_event_t`
 * @param subscriptionsCount `__wasi_size_t`
 * @param eventCount pointer to `__wasi_size_t`
 */
@WasmImport(module = "wasi_snapshot_preview1", name = "poll_oneoff")
external fun wasi_snapshot_preview1_poll_oneoff(
  subscription: UInt,
  outEvent: UInt,
  subscriptionsCount: Int,
  eventCount: UInt,
): Int
