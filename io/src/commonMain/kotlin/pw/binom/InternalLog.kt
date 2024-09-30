package pw.binom

import pw.binom.atomic.AtomicReference

interface InternalLog {
  companion object : InternalLog {
    val NULL: InternalLog =
      object : InternalLog {
        override fun log(
          level: Level,
          file: String?,
          line: Int?,
          method: String?,
          text: () -> String,
        ) = Unit
      }

    var internalDefault = AtomicReference(NULL)
    var default: InternalLog
      get() = internalDefault.getValue()
      set(value) {
        internalDefault.setValue(value)
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

  fun prefix(prefix: ()->String): InternalLog =
    object : InternalLog {

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
