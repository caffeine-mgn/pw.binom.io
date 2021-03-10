package pw.binom.db.tarantool

import pw.binom.db.tarantool.protocol.QueryIterator
import pw.binom.uuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class UpsertTest {

    @Test
    fun updateTest() {
        val schemaName = Random.uuid().toShortString()
        tarantool {
            it.eval(
                """
                s=box.schema.space.create('$schemaName',{engine = 'memtx', if_not_exists = true})
s:format({
    {name = 'id', type = 'uuid', is_nullable=false},
    {name = 'password', type = 'uuid', is_nullable=false}
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
            """
            )

            it.select(
                space = schemaName,
                index = "primary",
                key = emptyList<Any>(),
                offset = null,
                limit = 10,
                QueryIterator.ALL
            ).also {
                assertEquals(0, it.size)
            }

            val data1 = Random.uuid()
            val data2 = Random.uuid()
            val id = Random.uuid()
            it.upsert(
                space = schemaName,
                indexValues = listOf(id, data1),
                values = listOf(FieldUpdate(1, data2, UpdateOperator.ASSIGN))
            )
            it.select(
                space = schemaName,
                index = "primary",
                key = emptyList<Any>(),
                offset = null,
                limit = 10,
                QueryIterator.ALL
            ).also {
                it.forEach {
                    println("${it[0]}   ${it[1]}   data1=$data1  data2=$data2")
                    assertEquals(data1, it[1])
                }
                assertEquals(1, it.size)
            }


            it.upsert(
                space = schemaName,
                indexValues = listOf(id, data1),
                values = listOf(FieldUpdate(1, data2, UpdateOperator.ASSIGN))
            )
            it.select(
                space = schemaName,
                index = "primary",
                key = emptyList<Any>(),
                offset = null,
                limit = 10,
                QueryIterator.ALL
            ).also {
                it.forEach {
                    println("${it[0]}   ${it[1]}   data1=$data1  data2=$data2")
                    assertEquals(data2, it[1])
                }
                assertEquals(1, it.size)
            }
        }
    }
}