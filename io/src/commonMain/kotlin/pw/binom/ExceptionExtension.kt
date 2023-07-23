package pw.binom

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <R : Throwable> R.beforeThrow(func: (R) -> Unit): Nothing {
  contract {
    callsInPlace(func, InvocationKind.EXACTLY_ONCE)
  }
  try {
    func(this)
    throw this
  } catch (e: Throwable) {
    e.addSuppressed(this)
    throw e
  }
}

@OptIn(ExperimentalContracts::class)
inline fun <R : Throwable, T> R.processing(func: (R) -> T): T {
  contract {
    callsInPlace(func, InvocationKind.EXACTLY_ONCE)
  }
  return try {
    func(this)
  } catch (e: Throwable) {
    e.addSuppressed(this)
    throw e
  }
}
