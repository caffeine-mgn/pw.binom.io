package pw.binom.db.serialization.codes

import kotlinx.serialization.Serializable
import pw.binom.db.serialization.DefaultSQLSerializePool
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ByteArrayTest {

    @Serializable
    class Obj(val data: ByteArray)

    @Test
    fun encodeByteArray() {
        val result = HashMap<String, Any?>()
        DefaultSQLSerializePool.encode(
            serializer = Obj.serializer(),
            value = Obj(byteArrayOf(10, 20, 30)),
            name = "",
            output = result.toMutableDataBinder(),
        )
        assertEquals(1, result.size)
        val data = result["data"] as ByteArray
        assertContentEquals(byteArrayOf(10, 20, 30), data)
    }

    @Test
    fun decodeByteArray() {
        val data = hashMapOf<String, Any?>("data" to byteArrayOf(10, 20, 30))
        val d = DefaultSQLSerializePool.decode(
            serializer = Obj.serializer(),
            name = "",
            input = data.toMutableDataBinder(),
        )
        assertContentEquals(byteArrayOf(10, 20, 30), d.data)
    }
}
