package pw.binom.db.serialization

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.EmptySerializersModule
import pw.binom.collections.defaultMutableMap
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SQLSerializationTest {
  @Serializable
  data class EntityWithArray(val array: ByteArray)

  @Enumerate
  @Serializable
  enum class MyEnumOrder {
    VALUE1, VALUE2
  }

  @Serializable
  enum class MyEnumCode {
    VALUE1, VALUE2
  }

  @Serializable
  enum class MyEnumCode2 {
    VALUE1,
    VALUE2,
  }

  @Serializable
  data class EntityWithEnumOrder(val enum: MyEnumOrder)

  @Serializable
  data class EntityWithEnumCode(val enum: MyEnumCode)

  @Serializable
  data class EntityWithEnumCode2(val enum: MyEnumCode2)

  @Test
  fun internalGenerateSelectColumnsTest() {
    val columns = internalGenerateSelectColumns(
      tableName = "a",
      descriptor = User.serializer().descriptor,
      prefix = "a_",
      onlyIndexed = false,
    )
    println("columns: $columns")
  }

  @Serializable
  class User(val id: Long, @Embedded val auth: Auth)

  @Serializable
  class Exp(val time: Int)

  @Serializable
  class Auth(
    val name: String,
    val password: String,
    @Embedded
    val exp: Exp,
  )

  @Test
  fun enumOrderTest() {
    val output = defaultMutableMap<String, Any?>()
    SQLSerialization.toMap(
      serializer = EntityWithEnumOrder.serializer(),
      value = EntityWithEnumOrder(enum = MyEnumOrder.VALUE1),
      map = output,
      columnPrefix = null,
      serializersModule = EmptySerializersModule(),
    )
    assertEquals(0, output["enum"])
  }

  @Test
  fun enumCodeTest() {
    val output = defaultMutableMap<String, Any?>()
    SQLSerialization.toMap(
      serializer = EntityWithEnumCode.serializer(),
      value = EntityWithEnumCode(enum = MyEnumCode.VALUE1),
      map = output,
      columnPrefix = null,
      serializersModule = EmptySerializersModule(),
    )
    assertEquals("VALUE1", output["enum"])
  }

  @Test
  fun customEnumCode() {
    val output = defaultMutableMap<String, Any?>()
    SQLSerialization.toMap(
      serializer = EntityWithEnumCode2.serializer(),
      value = EntityWithEnumCode2(enum = MyEnumCode2.VALUE1),
      map = output,
      columnPrefix = null,
      serializersModule = EmptySerializersModule,
    )
    assertEquals(10, output["enum"])
  }

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
        listOf(data),
      ),
      columns = listOf("array"),
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
      val t2: String,
    )

    @Serializable
    data class Root(
      val t1: String,
      @Embedded
      val embebbedT2: Em,
      @Embedded
      val embebbedT3: Em?,
    )

    val result = ListStaticSyncResultSet(
      list = listOf(
        listOf(
          "test-t1",
          "test-t2",
          "test-t3",
        ),
      ),
      columns = listOf(
        "t1",
        "embebbedT2_t2",
        "embebbedT3_t2",
      ),
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
      val embebbedT3: Value?,
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
        ),
      ),
      columns = listOf(
        "embebbedT2_code",
        "embebbedT2_role",
        "embebbedT3_code",
        "embebbedT3_role",
      ),
    ).also {
      it.next()
      val value = SQLSerialization.DEFAULT.mapper<Root>().invoke(it)
      assertEquals("t2-code-value", value.embebbedT2.code)
      assertEquals("t2-role-value", value.embebbedT2.role)
      assertNull(value.embebbedT3)
    }
  }
}
