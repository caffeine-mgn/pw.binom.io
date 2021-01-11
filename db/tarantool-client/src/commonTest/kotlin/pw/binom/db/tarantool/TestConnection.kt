package pw.binom.db.tarantool

import pw.binom.uuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestConnection {

    @Test
    fun stringPass() {
        tt { con ->
            val text = "Response From Tarantool: ${Random.uuid()}"
            val response = con.eval("return '$text'")
            assertTrue(response is List<*>)
            assertEquals(1, response.size)
            assertEquals(text, response[0])
        }

        tt { con ->
            val text = "Response From Tarantool: ${Random.uuid()}"
            val response = con.eval("return ...", text, text)
            assertTrue(response is List<*>)
            assertEquals(2, response.size)
            assertEquals(text, response[0])
            assertEquals(text, response[1])
        }
    }

    @Test
    fun uuidPass() {
        tt { con ->
            val uuid = Random.uuid()
            val response = con.eval("return ...", uuid)
            assertTrue(response is List<*>)
            assertEquals(1, response.size)
            assertEquals(uuid, response[0])
        }
    }

    @Test
    fun intPass() {
        tt { con ->
            val value = Random.nextInt()
            val response = con.eval("return ...", value)
            assertTrue(response is List<*>)
            assertEquals(1, response.size)
            assertEquals(value, response[0])
        }
    }

    @Test
    fun bytesPass() {
        tt { con ->
            val response = con.eval("return type(...)", Random.nextBytes(10))
            assertTrue(response is List<*>)
            assertEquals(1, response.size)
            assertEquals("string", response[0])
        }
    }

    @Test
    fun listPass() {
        tt { con ->
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
        tt { con ->
            val value = listOf(listOf(Random.nextInt()), listOf(Random.nextInt()))
            val response = con.eval("return ...", value)
            assertTrue(response is List<*>)
            assertEquals(2, response.size)
            assertEquals(value[0], response[0])
            assertEquals(value[1], response[1])
        }
    }
}