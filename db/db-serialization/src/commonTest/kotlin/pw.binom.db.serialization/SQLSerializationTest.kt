package pw.binom.db.serialization

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
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
    fun testDecodeByteArray() = runBlocking {
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

    @Test
    fun selectEmbedded() = runTest {
        @Serializable
        data class Em(
            val t2: String
        )

        @Serializable
        data class Root(
            val t1: String,
            @Embedded("t2_")
            val embebbedT2: Em
        )

        val result = ListStaticSyncResultSet(
            list = listOf(
                listOf(
                    "test-t1",
                    "test-t2"
                )
            ),
            columns = listOf(
                "t1",
                "t2_t2"
            )
        )
        result.next()
        val value = SQLSerialization.DEFAULT.mapper<Root>().invoke(result)
        println("value:$value")
    }
}
