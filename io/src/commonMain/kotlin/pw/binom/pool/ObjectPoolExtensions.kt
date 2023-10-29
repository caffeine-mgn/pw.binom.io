package pw.binom.pool

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <T : Any, R> ObjectPool<T>.using(action: ((T) -> R)): R {
  contract {
    callsInPlace(action, InvocationKind.EXACTLY_ONCE)
  }
  val value = borrow()
  try {
    return action(value)
  } finally {
    recycle(value)
  }
}
