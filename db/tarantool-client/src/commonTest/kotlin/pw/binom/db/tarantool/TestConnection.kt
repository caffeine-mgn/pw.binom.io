package pw.binom.db.tarantool

import pw.binom.uuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestConnection {

    @Test
    fun testEval() {
        tt { con ->
            val text = "Response From Tarantool: ${Random.uuid()}"
            val response = con.eval("return '$text'")
            assertTrue(response is List<*>)
            assertEquals(1, response.size)
            assertEquals(text, response[0])
        }
    }
}