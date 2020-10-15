package pw.binom.lua

import org.luaj.vm2.lib.jse.JsePlatform
import pw.binom.io.Closeable

actual class LuaEngine : Closeable {
    private val globals = JsePlatform.standardGlobals()
    override fun close() {

    }

    actual fun eval(text: String) {
        val r = globals.load(text)
        r.call()
    }

}