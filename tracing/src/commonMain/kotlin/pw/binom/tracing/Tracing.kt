package pw.binom.tracing

import pw.binom.date.DateTime
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.TimeSource

object Tracing {
  suspend fun handle(
    scope: String,
    name: String,
    duration: Duration,
    timestamp: DateTime,
    tags: Map<String, String>? = null,
  ) {
    global.handle(
      scope = scope,
      name = name,
      duration = duration,
      timestamp = timestamp,
      tags = tags,
    )
  }

  @OptIn(ExperimentalContracts::class)
  suspend inline fun <T> processing(
    scope: String,
    name: String,
    noinline args: (() -> Map<String, String>)? = null,
    func: () -> T,
  ): T {
    contract {
      callsInPlace(func, InvocationKind.EXACTLY_ONCE)
      callsInPlace(func, InvocationKind.AT_MOST_ONCE)
    }
    if (!global.enable) {
      return func()
    }
    val now = TimeSource.Monotonic.markNow()
    val start = DateTime.now
    return try {
      func()
    } finally {
      handle(
        scope = scope,
        name = name,
        tags = args?.invoke(),
        duration = now.elapsedNow(),
        timestamp = start,
      )
    }
  }

  interface Handler {
    val enable: Boolean
    suspend fun handle(
      scope: String,
      name: String,
      duration: Duration,
      timestamp: DateTime,
      tags: Map<String, String>?,
    )
  }

  object EmptyHandler : Handler {
    override val enable: Boolean
      get() = false

    override suspend fun handle(
      scope: String,
      name: String,
      duration: Duration,
      timestamp: DateTime,
      tags: Map<String, String>?,
    ) {
      // Do nothing
    }

  }

  var global: Handler = EmptyHandler
}


