package pw.binom

import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import kotlin.coroutines.*
import kotlin.jvm.JvmInline

interface GeneratorScope<T> {
  val isClosed: Boolean
  suspend fun yaid(value: T)
}

@JvmInline
value class GeneratorResult<out T> private constructor(val value: Any?) {
  private object Eof
  companion object {
    fun <T> success(value: T) = GeneratorResult<T>(value)
    fun <T> end() = GeneratorResult<T>(Eof)
  }

  val isEmpty
    get() = value === Eof

  fun get() = if (isEmpty) throw ClosedException() else value as T
  fun getOrNull() = if (isEmpty) null else value as T
}

inline fun <T, R> GeneratorResult<T>.ifNotEmpty(func: (T) -> R): R? {
  if (isEmpty) {
    return null
  }
  return func(get())
}

suspend fun <T> Generator<T>.collect(func: suspend (T) -> Unit) {
  while (true) {
    val e = next()
    if (e.isEmpty) {
      break
    }
    func(e.get())
  }
}

interface Generator<T> : Closeable {
  suspend fun next(): GeneratorResult<T>
  val eof: Boolean

  fun <R> map(mapping: suspend (T) -> R): Generator<R> = object : Generator<R> {
    override val eof: Boolean
      get() = this@Generator.eof

    override suspend fun next(): GeneratorResult<R> {
      val r = this@Generator.next()
      return if (r.isEmpty) {
        GeneratorResult.end()
      } else {
        GeneratorResult.success(mapping(r.get()))
      }
    }

    override fun close() {
      this@Generator.close()
    }
  }
}

private class GeneratorImpl<T>(val context: CoroutineContext, val func: suspend GeneratorScope<T>.() -> Unit) :
  Generator<T> {
  private var isClosed = false
  private var dataProviderContinuation: Continuation<Unit>? = null
  private var exchangeContinuation: Continuation<Unit>? = null
  private var value: T? = null

  override val eof: Boolean
    get() = isClosed

  private val ctx = object : GeneratorScope<T> {
    override val isClosed: Boolean
      get() = this@GeneratorImpl.isClosed

    override suspend fun yaid(value: T) {
      if (isClosed) {
        throw ClosedException()
      }
      this@GeneratorImpl.value = value
      suspendCoroutine {
        dataProviderContinuation = it
        val ec = exchangeContinuation
        exchangeContinuation = null
        ec?.resume(Unit)
      }
    }
  }

  override suspend fun next(): GeneratorResult<T> {
    if (isClosed) {
      return GeneratorResult.end()
    }
    if (dataProviderContinuation == null) {
      val currentContext = coroutineContext + context
      suspendCoroutine {
        exchangeContinuation = it
        func.startCoroutine(
          ctx,
          object : Continuation<Unit> {
            override val context: CoroutineContext = currentContext
            override fun resumeWith(result: Result<Unit>) {
              isClosed = true
              value = null
              dataProviderContinuation = null
              val ec = exchangeContinuation
              exchangeContinuation = null
              if (result.isFailure) {
                ec?.resumeWithException(result.exceptionOrNull()!!)
              } else {
                ec?.resume(Unit)
              }
            }
          },
        )
      }
      if (dataProviderContinuation == null) {
        isClosed = true
        return GeneratorResult.end()
      } else {
        val ret = GeneratorResult.success(value as T)
        value = null
        return ret
      }
    } else {
      suspendCoroutine {
        exchangeContinuation = it
        dataProviderContinuation!!.resume(Unit)
      }
      return if (isClosed) {
        GeneratorResult.end()
      } else {
        GeneratorResult.success(value as T)
      }
    }
  }

  override fun close() {
    dataProviderContinuation?.resumeWithException(ClosedException())
    exchangeContinuation?.resumeWithException(ClosedException())
    dataProviderContinuation = null
    exchangeContinuation = null
    isClosed = true
  }
}

operator fun <A : T, B : T, T> Generator<A>.plus(other: Generator<B>): Generator<T> = object : Generator<T> {
  override val eof: Boolean
    get() = this@plus.eof && other.eof

  override suspend fun next(): GeneratorResult<T> {
    val r = this@plus.next()
    if (!r.isEmpty) {
      return r
    }
    return other.next()
  }

  override fun close() {
    try {
      this@plus.close()
    } finally {
      other.close()
    }
  }
}

fun <T> generator(
  context: CoroutineContext = EmptyCoroutineContext,
  func: suspend GeneratorScope<T>.() -> Unit,
): Generator<T> = GeneratorImpl(
  func = func,
  context = context,
)

fun <CURSOR : Any, VALUE> generatorFlow(
  cursor: (VALUE) -> CURSOR,
  func: suspend (CURSOR?) -> Collection<VALUE>,
) = generator<VALUE> {
  var afterDate: CURSOR? = null
  while (!isClosed) {
    val values = func(afterDate)
    if (values.isEmpty()) {
      break
    }
    afterDate = cursor(values.last())
    values.forEach {
      if (isClosed) {
        return@generator
      }
      yaid(it)
    }
  }
}
