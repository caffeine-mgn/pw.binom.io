package pw.binom.db.tarantool

import pw.binom.db.tarantool.protocol.QueryIterator
import pw.binom.nextUuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class Testing:BaseTest() {

    @Test
    fun updateTest() {
        val schemaName = Random.nextUuid().toShortString()
        pg {
            it.eval(
                """
s=box.schema.space.create('$schemaName',{engine = 'memtx', if_not_exists = true})
s:format({
    {name = 'id', type = 'uuid', is_nullable=false},
    {name = 'login', type = 'string', is_nullable=false},
    {name = 'password', type = 'varbinary', is_nullable=false}
})
s:create_index('primary',
    {
        type = 'tree',
        unique = true,
        if_not_exists = true,
        parts = {
                {1, 'uuid'}
            }
    }
)

s:create_index('login_pwd',
    {
        type = 'tree',
        unique = true,
        if_not_exists = true,
        parts = {
                {2, 'string'},
                {3, 'varbinary'}
            }
    }
)
    """
            )
            val id = Random.nextUuid()
            val login = Random.nextUuid().toString()
            val password = Random.nextBytes(10)
            val newPassword = Random.nextBytes(10)
            it.insert(schemaName, listOf(Random.nextUuid(), Random.nextUuid().toString(), Random.nextBytes(10)))
            it.insert(schemaName, listOf(id, login, password))
            println("Try update")
            it.update(
                space = schemaName,
                key = listOf(id),
                values = listOf(
                    FieldUpdate(
                        operator = UpdateOperator.ASSIGN,
                        fieldId = 2,
                        value = password
                    )
                )
            )

            it.update(
                space = schemaName,
                key = listOf(id),
                listOf(FieldUpdate(fieldId = 2, value = newPassword, operator = UpdateOperator.ASSIGN))
            ).also {
                assertNotNull(it)
                assertEquals(id, it[0])
                assertEquals(login, it[1])
                assertEquals(newPassword.size, (it[2] as ByteArray).size)
                (it[2] as ByteArray).forEachIndexed { index, byte ->
                    assertEquals(newPassword[index], byte)
                }
            }


            it.select(
                space = schemaName,
                index = "primary",
                key = listOf<Any>(),
                offset = 0,
                limit = 20,
                iterator = QueryIterator.ALL
            ).also {
                assertEquals(2, it.size)
            }

            it.select(
                space = schemaName,
                index = "primary",
                key = listOf<Any>(id),
                offset = 0,
                limit = 20,
                iterator = QueryIterator.EQ
            ).also {
                assertEquals(1, it.size)
                it.first().also {
                    assertEquals(id, it[0])
                    assertEquals(login, it[1])
                    assertEquals(newPassword.size, (it[2] as ByteArray).size)
                    (it[2] as ByteArray).forEachIndexed { index, byte ->
                        assertEquals(newPassword[index], byte)
                    }
                }
            }
        }
    }
}