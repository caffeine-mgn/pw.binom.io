package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimitiveConvert {

    @Test
    fun testInt(){
        val value = 8081

        println("Origenal:")
        println("0-> ${value[0]}")
        println("1-> ${value[1]}")
        println("2-> ${value[2]}")
        println("3-> ${value[3]}")

        val result = Int.fromBytes(value[0],value[1],value[2],value[3])

        println("\nResult:")
        println("0-> ${result[0]}")
        println("1-> ${result[1]}")
        println("2-> ${result[2]}")
        println("3-> ${result[3]}")

        assertEquals(value,result)
    }
}