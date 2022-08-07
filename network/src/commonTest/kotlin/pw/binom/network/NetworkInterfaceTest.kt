package pw.binom.network

import kotlin.test.Test

class NetworkInterfaceTest {
    @Test
    fun test() {
        try {
            println("Try get address")
            val interfaces = NetworkInterface.interfaces
            println("list:")
            interfaces.forEach {
                println("->$it")
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
