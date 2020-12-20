package pw.binom.lua

import pw.binom.io.Closeable

expect class LuaEngine : Closeable {
    constructor()
    fun eval(text: String)
}