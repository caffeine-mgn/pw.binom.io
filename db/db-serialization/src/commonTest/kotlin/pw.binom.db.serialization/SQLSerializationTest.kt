package pw.binom.db.serialization

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
            @Embedded
            val embebbedT2: Em,
            @Embedded
            val embebbedT3: Em?
        )

        val result = ListStaticSyncResultSet(
            list = listOf(
                listOf(
                    "test-t1",
                    "test-t2",
                    "test-t3",
                )
            ),
            columns = listOf(
                "t1",
                "embebbedT2_t2",
                "embebbedT3_t2",
            )
        )
        result.next()
        val value = SQLSerialization.DEFAULT.mapper<Root>().invoke(result)
        assertEquals("test-t1", value.t1)
        assertEquals("test-t2", value.embebbedT2.t2)
        assertEquals("test-t3", value.embebbedT3?.t2)
        println("value:$value")
    }

    @Test
    fun selectEmbeddedWithNull() = runTest {
        @Serializable
        data class Value(
            val code: String,
            val role: String,
        )

        @Serializable
        data class Root(
            @Embedded
            val embebbedT2: Value,
            @Embedded
            val embebbedT3: Value?
        )

        val result = SQLSerialization.DEFAULT.buildSqlNamedParams(Root.serializer(), Root(Value("1", "2"), null))
        result.entries.forEach {
            println("->${it.key}: \"${it.value}\"")
        }
        println("->$result")

        ListStaticSyncResultSet(
            list = listOf(
                listOf(
                    "t2-code-value",
                    "t2-role-value",
                    null,
                    null,
                )
            ),
            columns = listOf(
                "embebbedT2_code",
                "embebbedT2_role",
                "embebbedT3_code",
                "embebbedT3_role",
            )
        ).also {
            it.next()
            val value = SQLSerialization.DEFAULT.mapper<Root>().invoke(it)
            assertEquals("t2-code-value", value.embebbedT2.code)
            assertEquals("t2-role-value", value.embebbedT2.role)
            assertNull(value.embebbedT3)
        }
    }
}
