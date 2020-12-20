package pw.binom.lua

import kotlin.test.Test

class LuaEngineTest{

    @Test
    fun test(){
        val e = LuaEngine()
        e.eval("print('hello!')")
    }
}