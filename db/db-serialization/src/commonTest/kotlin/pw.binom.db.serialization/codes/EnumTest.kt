package pw.binom.db.serialization.codes

import kotlinx.serialization.Serializable
import pw.binom.db.serialization.DefaultSQLSerializePool
import pw.binom.db.serialization.EnumCodeValue
import pw.binom.db.serialization.EnumOrderValue
import kotlin.test.Test
import kotlin.test.assertEquals

class EnumTest {

    @Serializable
    enum class EnumByName {
        A, B
    }

    @Serializable
    enum class EnumByCodes {
        @EnumCodeValue(10)
        A,

        @EnumCodeValue(20)
        B
    }

    @Serializable
    @EnumOrderValue
    enum class EnumByOrder {
        A, B
    }

    @Test
    fun encodeByName() {
        val result = HashMap<String, Any?>()
        DefaultSQLSerializePool.encode(
            serializer = EnumByName.serializer(),
            value = EnumByName.A,
            name = "enum",
            output = result.toMutableDataBinder(),
        )
        assertEquals("A", result["enum"])
        assertEquals(1, result.size)
    }

    @Test
    fun encodeByOrder() {
        val result = HashMap<String, Any?>()
        DefaultSQLSerializePool.encode(
            serializer = EnumByOrder.serializer(),
            value = EnumByOrder.A,
            name = "enum",
            output = result.toMutableDataBinder(),
        )
        assertEquals(0, result["enum"])
        assertEquals(1, result.size)
    }

    @Test
    fun encodeByCode() {
        val result = HashMap<String, Any?>()
        DefaultSQLSerializePool.encode(
            serializer = EnumByCodes.serializer(),
            value = EnumByCodes.A,
            name = "enum",
            output = result.toMutableDataBinder(),
        )
        assertEquals(10, result["enum"])
        assertEquals(1, result.size)
    }

    // ---------------//
    @Test
    fun decodeByName() {
        val data = hashMapOf<String, Any?>("enum" to "A")
        val d = DefaultSQLSerializePool.decode(
            serializer = EnumByName.serializer(),
            name = "enum",
            input = data.toMutableDataBinder(),
        )
        assertEquals(EnumByName.A, d)
    }

    @Test
    fun decodeByOrder() {
        val data = hashMapOf<String, Any?>("enum" to 0)

        val d = DefaultSQLSerializePool.decode(
            serializer = EnumByOrder.serializer(),
            name = "enum",
            input = data.toMutableDataBinder(),
        )
        assertEquals(EnumByOrder.A, d)
    }

    @Test
    fun decodeByCode() {
        val data = hashMapOf<String, Any?>("enum" to 10)
        val d = DefaultSQLSerializePool.decode(
            serializer = EnumByCodes.serializer(),
            name = "enum",
            input = data.toMutableDataBinder(),
        )
        assertEquals(EnumByCodes.A, d)
    }
}
