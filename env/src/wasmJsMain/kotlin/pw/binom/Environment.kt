package pw.binom

import kotlinx.browser.window

actual val Environment.workDirectory: String
  get() = "${window.location.protocol}//${window.location.host}${window.location.pathname}"

@JsFun("() => Date.now()")
external fun dataNowTime():JsNumber

actual val Environment.currentTimeMillis: Long
  get() = dataNowTime().toInt().toLong()

actual val Environment.currentTimeNanoseconds: Long
  get() = (window.performance.now() * 1000000.0).toLong()

actual val Environment.currentExecutionPath: String
  get() = "${window.location.origin}${window.location.pathname}"
