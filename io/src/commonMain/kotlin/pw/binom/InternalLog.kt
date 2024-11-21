package pw.binom

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicReference
import pw.binom.atomic.synchronize

interface InternalLog {
  companion object : InternalLog {
    val NULL: InternalLog =
      object : InternalLog {
        override val enabled: Boolean
          get() = false

        override fun <T> tx(func: (Transaction) -> T): T = func(Transaction.NULL)

        override fun log(
          level: Level,
          file: String?,
          line: Int?,
          method: String?,
          text: () -> String,
        ) = Unit
      }

    override val enabled: Boolean
      get() = default.enabled

    override fun <T> tx(func: (Transaction) -> T): T = default.tx(func)

    private var internalDefault: InternalLog = NULL
    val default: InternalLog
      get() = internalDefault

    fun replace(func: (InternalLog) -> InternalLog) {
      internalDefault = func(internalDefault)
    }

    override fun log(
      level: Level,
      file: String?,
      line: Int?,
      method: String?,
      text: () -> String,
    ) {
      default.log(level = level, file = file, text = text, line = line, method = method)
    }
  }

  enum class Level {
    INFO,
    WARNING,
    ERROR,
    CRITICAL,
    FATAL,
  }

  interface Transaction {
    companion object {
      val NULL = object : Transaction {
        override fun clear(): Boolean = false

        override fun stop(): Boolean = false

        override fun resume(): Boolean = false

      }
    }

    fun clear(): Boolean
    fun stop(): Boolean
    fun resume(): Boolean
  }

  val enabled: Boolean

  fun <T> tx(func: (Transaction) -> T): T

  fun log(
    level: Level,
    file: String? = null,
    line: Int? = null,
    method: String? = null,
    text: () -> String,
  )

  fun info(
    file: String? = null,
    line: Int? = null,
    method: String? = null,
    text: () -> String,
  ) = log(level = Level.INFO, file = file, text = text, line = line, method = method)

  fun warn(
    file: String? = null,
    line: Int? = null,
    method: String? = null,
    text: () -> String,
  ) = log(level = Level.WARNING, file = file, text = text, line = line, method = method)

  fun err(
    file: String? = null,
    line: Int? = null,
    method: String? = null,
    text: () -> String,
  ) = log(level = Level.ERROR, file = file, text = text, line = line, method = method)

  fun critical(
    file: String? = null,
    line: Int? = null,
    method: String? = null,
    text: () -> String,
  ) = log(level = Level.CRITICAL, file = file, text = text, line = line, method = method)

  fun fatal(
    file: String? = null,
    line: Int? = null,
    method: String? = null,
    text: () -> String,
  ) = log(level = Level.FATAL, file = file, text = text, line = line, method = method)

  fun prefix(prefix: () -> String): InternalLog =
    object : InternalLog {

      override fun <T> tx(func: (Transaction) -> T): T = this@InternalLog.tx(func)

      override val enabled: Boolean
        get() = this@InternalLog.enabled

      override fun log(
        level: Level,
        file: String?,
        line: Int?,
        method: String?,
        text: () -> String,
      ) {
        this@InternalLog.log(file = file, line = line, level = level, method = method) { prefix() + text() }
      }
    }

  fun file(file: String): InternalLog {
    val newFile = file
    return object : InternalLog {
      override val enabled: Boolean
        get() = this@InternalLog.enabled

      override fun <T> tx(func: (Transaction) -> T): T = this@InternalLog.tx(func)

      override fun log(
        level: Level,
        file: String?,
        line: Int?,
        method: String?,
        text: () -> String,
      ) {
        this@InternalLog.log(file = file ?: newFile, level = level, text = text, line = line, method = method)
      }
    }
  }
}
