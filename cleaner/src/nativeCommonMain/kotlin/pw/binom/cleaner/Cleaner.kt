package pw.binom.cleaner

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalNativeApi::class)
actual class Cleaner private constructor(val native: kotlin.native.ref.Cleaner) {

  actual companion object {
    actual fun <T> create(value: T, func: (T) -> Unit): Cleaner {
      val native = createCleaner<Pair<T, (T) -> Unit>>(value to func) { it.second(it.first) }
      return Cleaner(native)
    }
  }
}
