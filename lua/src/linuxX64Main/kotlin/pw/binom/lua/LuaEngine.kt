package pw.binom.lua

import kotlinx.cinterop.CPointer
import platform.internal_lua.*
import pw.binom.io.Closeable

actual class LuaEngine : Closeable {

    val state: CPointer<cnames.structs.lua_State> = luaL_newstate()!!

    init {
        luaL_openlibs(state)
    }

    override fun close() {
        lua_close(state)
    }

    actual fun eval(text: String) {
        val value = luaL_loadstring(state, text)
        lua_pcall(state, 0, 0, 0)
    }

}

fun lua_pcall(L: CPointer<cnames.structs.lua_State>, n: Int, r: Int, f: Int) =
        lua_pcallk(L, (n), (r), (f), 0, null)