package pw.binom

import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(NativeRuntimeApi::class)
actual object System {
  actual fun gc() {
    GC.collect()
  }
}
