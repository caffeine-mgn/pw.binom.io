package pw.binom

import kotlinx.browser.window
import kotlin.js.Date

actual val Environment.platform: Platform
    get() = Platform.JS

actual fun Environment.getEnv(name: String): String? = null
actual fun Environment.getEnvs(): Map<String, String> = emptyMap()
actual val Environment.isBigEndian: Boolean
    get() = true
actual val Environment.workDirectory: String
    get() = "${window.location.protocol}//${window.location.host}${window.location.pathname}"


actual val Environment.currentTimeMillis: Long
    get() = Date.now().toLong()
actual val Environment.currentTimeNanoseconds: Long
    get() = (window.performance.now() * 1000000.0).toLong()