package pw.binom.io

fun interface Closeable {
  fun close()
  fun closeAnyway() = try {
    close()
    true
  } catch (e: Throwable) {
    false
  }

  companion object {
    val STUB = Closeable { }

    fun close(
      closeable: Closeable?,
    ) {
      var ex: Throwable? = null
      ex = tryClose(ex, closeable)
      ex?.doThrow()
    }

    fun close(
      closeable1: Closeable?,
      closeable2: Closeable?,
    ) {
      var ex: Throwable? = null
      ex = tryClose(ex, closeable1)
      ex = tryClose(ex, closeable2)
      ex?.doThrow()
    }

    fun close(
      closeable1: Closeable?,
      closeable2: Closeable?,
      closeable3: Closeable?,
    ) {
      var ex: Throwable? = null
      ex = tryClose(ex, closeable1)
      ex = tryClose(ex, closeable2)
      ex = tryClose(ex, closeable3)
      ex?.doThrow()
    }

    fun close(
      closeable1: Closeable?,
      closeable2: Closeable?,
      closeable3: Closeable?,
      closeable4: Closeable?,
    ) {
      var ex: Throwable? = null
      ex = tryClose(ex, closeable1)
      ex = tryClose(ex, closeable2)
      ex = tryClose(ex, closeable3)
      ex = tryClose(ex, closeable4)
      ex?.doThrow()
    }

    fun close(
      closeable1: Closeable?,
      closeable2: Closeable?,
      closeable3: Closeable?,
      closeable4: Closeable?,
      closeable5: Closeable?,
    ) {
      var ex: Throwable? = null
      ex = tryClose(ex, closeable1)
      ex = tryClose(ex, closeable2)
      ex = tryClose(ex, closeable3)
      ex = tryClose(ex, closeable4)
      ex = tryClose(ex, closeable5)
      ex?.doThrow()
    }

    fun close(
      vararg closeable: Closeable?,
    ) {
      var ex: Throwable? = null
      closeable.forEach {
        ex = tryClose(ex, it)
      }
      ex?.doThrow()
    }

    fun close(
      closeable: List<Closeable>,
    ) {
      var ex: Throwable? = null
      closeable.forEach {
        ex = tryClose(ex, it)
      }
      ex?.doThrow()
    }
  }
}

private inline fun Throwable.doThrow(): Nothing = throw this
private inline fun tryClose(root: Throwable?, closeable: Closeable?): Throwable? {
  closeable ?: return root
  var newRoot = root
  try {
    closeable.close()
  } catch (e: Throwable) {
    newRoot = appendException(newRoot, e)
  }
  return newRoot
}

private inline fun appendException(root: Throwable?, e: Throwable): Throwable =
  (root ?: RuntimeException("Can't close all closable elements")).apply { addSuppressed(e) }

inline fun <T : Closeable, R> T.use(func: (T) -> R): R {
  var exception: Throwable? = null
  try {
    return func(this)
  } catch (e: Throwable) {
    exception = e
    throw e
  } finally {
    this.closeFinally(exception)
  }
}

@PublishedApi
internal fun Closeable?.closeFinally(cause: Throwable?): Unit = when {
  this == null -> {}
  cause == null -> close()
  else ->
    try {
      close()
    } catch (closeException: Throwable) {
      cause.addSuppressed(closeException)
    }
}
