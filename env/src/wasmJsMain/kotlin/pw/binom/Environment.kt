package pw.binom

actual val Environment.workDirectory: String
  get() = getWorkDirectory().toString()

@JsFun("() => window.location.protocol + '//' + window.location.host + window.location.pathname")
private external fun getWorkDirectory(): JsString

@JsFun("() => Date.now()")
external fun dataNowTime(): JsNumber

actual val Environment.currentTimeMillis: Long
  get() = dataNowTime().toInt().toLong()

actual fun Environment.getProperty(name: String): String? = null

actual val Environment.currentTimeNanoseconds: Long
  get() = getCurrentTimeNanoseconds().toDouble().toLong()

actual val Environment.currentExecutionPath: String
  get() = getCurrentExecutionPath().toString()

@JsFun("() => window.location.origin + window.location.pathname")
private external fun getCurrentExecutionPath(): JsString

@JsFun("() => window.performance.now() * 1000000.0")
private external fun getCurrentTimeNanoseconds(): JsNumber
