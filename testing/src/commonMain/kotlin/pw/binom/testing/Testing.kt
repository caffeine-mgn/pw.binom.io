package pw.binom.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

object Testing {

  class SyncTestContextImpl(val index: Int) : SyncTestContext {
    private val forDispose = ArrayList<() -> Unit>()
    private val forInit = ArrayList<() -> Unit>()
    val disposables: List<() -> Unit>
      get() = forDispose

    var testCount = 0
      private set
    private val internalExceptions = ArrayList<Throwable>()
    val exceptions: List<Throwable>
      get() = internalExceptions

    override fun init(func: () -> Unit) {
      forInit += func
    }

    override fun dispose(func: () -> Unit) {
      forDispose += func
    }

    override fun test(name: String, ignore: Boolean, func: () -> Unit) {
      if (ignore) {
        return
      }
      if (index == testCount && !ignore) {
        val now = TimeSource.Monotonic.markNow()
        println("---===TEST $name STARTED===---")
        try {
          forInit.forEach {
            it()
          }
          forInit.clear()
          func()
        } catch (e: Throwable) {
          internalExceptions += RuntimeException("Test \"$name\" fail", e)
        } finally {
          forDispose.forEach {
            try {
              it()
            } catch (e: Throwable) {
              internalExceptions += RuntimeException("Can't dispose resource of test \"$name\"", e)
            }
          }
          forDispose.clear()
        }
        println("---===TEST $name FINISHED in ${now.elapsedNow()}===---")
      }
      testCount++
    }
  }

  class AsyncTestContextImpl(val index: Int) : AsyncTestContext {
    private val forDispose = ArrayList<suspend () -> Unit>()
    private val forInit = ArrayList<suspend () -> Unit>()
    val disposables: List<suspend () -> Unit>
      get() = forDispose

    var testCount = 0
      private set
    private val internalExceptions = ArrayList<Throwable>()
    val exceptions: List<Throwable>
      get() = internalExceptions

    override fun init(func: suspend () -> Unit) {
      forInit += func
    }

    override fun dispose(func: suspend () -> Unit) {
      forDispose += func
    }

    override suspend fun test(name: String, ignore: Boolean, func: suspend () -> Unit) {
      if (ignore) {
        return
      }
      if (index == testCount && !ignore) {
        val now = TimeSource.Monotonic.markNow()
        println("---===TEST $name STARTED===---")
        try {
          forInit.forEach {
            it()
          }
          forInit.clear()
          func()
        } catch (e: Throwable) {
          internalExceptions += RuntimeException("Test \"$name\" fail", e)
        } finally {
          forDispose.forEach {
            try {
              it()
            } catch (e: Throwable) {
              internalExceptions += RuntimeException("Can't dispose resource of test \"$name\"", e)
            }
          }
          forDispose.clear()
        }
        println("---===TEST $name FINISHED in ${now.elapsedNow()}===---")
      }
      testCount++
    }
  }

  fun sync(
    func: SyncTestContext.() -> Unit,
  ) {
    var index = 0
    do {
      val exceptions = ArrayList<Throwable>()
      val ctx = SyncTestContextImpl(index)
      try {
        func(ctx)
      } catch (e: Throwable) {
        exceptions += e
      } finally {
        exceptions += ctx.exceptions
        ctx.disposables.forEach {
          try {
            it()
          } catch (e: Throwable) {
            exceptions += e
          }
        }
      }
      if (exceptions.isNotEmpty()) {
        exceptions.forEach {
          println(it.stackTraceToString())
        }
        val e = RuntimeException("Testing fail")
//        exceptions.forEach {
//          e.addSuppressed(it)
//        }
        throw e
      }
      index++
      if (ctx.testCount <= index) {
        break
      }
    } while (true)
  }

  fun async(
    dispatchTimeoutMs: Duration = 1.minutes,
    func: suspend AsyncTestContext.() -> Unit,
  ) = runTest(timeout = dispatchTimeoutMs) {
    withContext(Dispatchers.Default) {
      var index = 0
      do {
        val exceptions = ArrayList<Throwable>()
        val ctx = AsyncTestContextImpl(index)
        try {
          func(ctx)
        } catch (e: Throwable) {
          exceptions += e
        } finally {
          exceptions += ctx.exceptions
          ctx.disposables.forEach {
            try {
              it()
            } catch (e: Throwable) {
              exceptions += e
            }
          }
        }
        if (exceptions.isNotEmpty()) {
          exceptions.forEach {
            it.printStackTrace()
          }
          val e = RuntimeException("Testing fail")
          exceptions.forEach {
            e.addSuppressed(it)
          }
          throw e
        }
        index++
        if (ctx.testCount <= index) {
          break
        }
      } while (true)
    }
  }
  /*
    fun async(
      dispatchTimeoutMs: Duration = 1.minutes,
      func: suspend AsyncSubTest.() -> Unit,
    ) = runTest(timeout = dispatchTimeoutMs) {
      withContext(Dispatchers.Default) {
        var index = 0
        do {
          if (index > 0) {
            println()
            println("-------------------------")
            println()
          }
          val ctx = AsyncSubTestImpl(index)
          func(ctx)
          index++
          if (ctx.count <= index) {
            break
          }
        } while (true)
      }
    }

    private inline fun commonTest(name: String, ignore: Boolean, func: () -> Unit, index: Int, countInc: () -> Int) {
      if (ignore) {
        return
      }
      val c = countInc()
      if (c == index) {
        try {
          val now = TimeSource.Monotonic.markNow()
          println("---===TEST $name STARTED===---")
          func()
          println("---===TEST $name FINISHED in ${now.elapsedNow()}===---")
        } catch (e: Throwable) {
          throw RuntimeException("Error on $name", e)
        }
      }
    }

      private class SyncSubTestImpl(
        val index: Int,
      ) : SyncSubContext {
        var count = 0

        override fun subTest(
          name: String,
          ignore: Boolean,
          func: () -> Unit,
        ) {
          commonTest(
            name = name,
            ignore = ignore,
            func = func,
            index = index,
            countInc = { count++ }
          )
          if (ignore) {
            return
          }
          val c = count++
          if (c == index) {
            try {
              val now = TimeSource.Monotonic.markNow()
              println("---===TEST $name STARTED===---")
              func()
              println("---===TEST $name FINISHED in ${now.elapsedNow()}===---")
            } catch (e: Throwable) {
              throw RuntimeException("Error on $name", e)
            }
          }
        }
      }

      private class AsyncSubTestImpl(
        val index: Int,
      ) : AsyncSubTest {
        var count = 0

        override suspend fun subTest(
          name: String,
          ignore: Boolean,
          func: suspend () -> Unit,
        ) {
          commonTest(
            name = name,
            ignore = ignore,
            func = { func() },
            index = index,
            countInc = { count++ }
          )
        }
      }
     */
}
