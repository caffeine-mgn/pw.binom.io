package pw.binom

import kotlin.test.Test

class ByteBufferPoolTest{
    @Test
    fun closeTest(){
        ByteBufferPool(10).close()
    }
}