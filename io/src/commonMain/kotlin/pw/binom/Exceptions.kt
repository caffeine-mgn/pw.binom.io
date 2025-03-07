package pw.binom

import pw.binom.io.AsyncCloseable
import pw.binom.io.Closeable

interface SafeExceptionContext {
  fun onException(func: () -> Unit)

  fun <T : Closeable> T.closeOnException(): T {
    onException { close() }
    return this
  }

  fun <T : AutoCloseable> T.closeOnException(): T {
    onException { this.close() }
    return this
  }
}

interface SafeAsyncExceptionContext {
  fun onException(func: suspend () -> Unit)

  fun <T : AsyncCloseable> T.closeOnException(): T {
    onException { asyncClose() }
    return this
  }

  fun <T : Closeable> T.closeOnException(): T {
    onException { close() }
    return this
  }

  fun <T : AutoCloseable> T.closeOnException(): T {
    onException { close() }
    return this
  }
}

@PublishedApi
internal class SafeExceptionContextImpl : SafeExceptionContext {
  val rollbackActions = mutableListOf<() -> Unit>()
  override fun onException(func: () -> Unit) {
    rollbackActions += func
  }
}

@PublishedApi
internal class SafeAsyncExceptionContextImpl : SafeAsyncExceptionContext {
  val rollbackActions = mutableListOf<suspend () -> Unit>()
  override fun onException(func: suspend () -> Unit) {
    rollbackActions += func
  }
}

object SafeException {
  inline suspend fun <T> async(func: suspend SafeAsyncExceptionContext.() -> T): T {
    val ctx = SafeAsyncExceptionContextImpl()
    return try {
      func(ctx)
    } catch (funcException: Throwable) {
      var currentException = funcException
      ctx.rollbackActions.reversed().forEach {
        try {
          it.invoke()
        } catch (rollbackException: Throwable) {
          rollbackException.addSuppressed(currentException)
          currentException = rollbackException
        }
      }
      throw currentException
    }
  }

  inline fun <T> sync(func: SafeExceptionContext.() -> T): T {
    val ctx = SafeExceptionContextImpl()
    return try {
      func(ctx)
    } catch (funcException: Throwable) {
      var currentException = funcException
      ctx.rollbackActions.reversed().forEach {
        try {
          it.invoke()
        } catch (rollbackException: Throwable) {
          rollbackException.addSuppressed(currentException)
          currentException = rollbackException
        }
      }
      throw currentException
    }
  }
}
