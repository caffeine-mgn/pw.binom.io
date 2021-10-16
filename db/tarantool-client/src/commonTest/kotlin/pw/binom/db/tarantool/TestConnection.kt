package pw.binom.db.tarantool

import pw.binom.nextUuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestConnection:BaseTest() {

    @Test
    fun stringPass() {
        pg { con ->
            val text = "Response From Tarantool: ${Random.nextUuid()}"
            val response = con.eval("return '$text'")
            assertTrue(response is List<*>)
            assertEquals(1, response.size)
            assertEquals(text, response[0])
        }

        pg { con ->
            val text = "Response From Tarantool: ${Random.nextUuid()}"
            val response = con.eval("return ...", text, text)
            assertTrue(response is List<*>)
            assertEquals(2, response.size)
            assertEquals(text, response[0])
            assertEquals(text, response[1])
        }
    }

    @Test
    fun uuidPass() {
        pg { con ->
            val uuid = Random.nextUuid()
            val response = con.eval("return ...", uuid)
            assertTrue(response is List<*>)
            assertEquals(1, response.size)
            assertEquals(uuid, response[0])
        }
    }

    @Test
    fun intPass() {
        pg { con ->
            val value = Random.nextInt()
            val response = con.eval("return ...", value)
            assertTrue(response is List<*>)
            assertEquals(1, response.size)
            assertEquals(value, response[0])
        }
    }

    @Test
    fun bytesPass() {
        pg { con ->
            val response = con.eval("return type(...)", Random.nextBytes(10))
            assertTrue(response is List<*>)
            assertEquals(1, response.size)
            assertEquals("string", response[0])
        }
    }

    @Test
    fun listPass() {
        pg { con ->
            val value = listOf(Random.nextInt(), Random.nextInt())
            val response = con.eval("return ...", value)
            assertTrue(response is List<*>)
            assertEquals(2, response.size)
            assertEquals(value[0], response[0])
            assertEquals(value[1], response[1])
        }
    }

    @Test
    fun listOfListPass() {
        pg { con ->
            val value = listOf(listOf(Random.nextInt()), listOf(Random.nextInt()))
            val response = con.eval("return ...", value)
            assertTrue(response is List<*>)
            assertEquals(2, response.size)
            assertEquals(value[0], response[0])
            assertEquals(value[1], response[1])
        }
    }
}