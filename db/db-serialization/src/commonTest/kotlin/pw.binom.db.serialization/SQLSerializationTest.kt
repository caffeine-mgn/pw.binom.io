package pw.binom.db.serialization

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import pw.binom.getOrException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SQLSerializationTest {
    @Serializable
    data class EntityWithArray(val array: ByteArray)

    @Test
    fun testEncodeByteArray() {
        val data = byteArrayOf(10, 15, 20)
        val args = SQLSerialization.DEFAULT.nameParams(EntityWithArray.serializer(), EntityWithArray(data))
        val pair = args[0]
        assertEquals("array", pair.first)
        assertContentEquals(data, pair.second as ByteArray)
    }

    @Test
    fun testDecodeByteArray() = runBlocking{
        val data = byteArrayOf(10, 15, 20)
        val mapper = SQLSerialization.DEFAULT.mapper<EntityWithArray>()
        val resultSet = ListStaticSyncResultSet(
            list = listOf(
                listOf(data)
            ),
            columns = listOf("array")
        )

        val decoded = async {
            resultSet.next()
            mapper(resultSet)
        }.await()

        assertContentEquals(data, decoded.array)
    }
}