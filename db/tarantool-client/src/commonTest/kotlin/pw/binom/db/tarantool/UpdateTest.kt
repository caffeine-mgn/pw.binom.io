package pw.binom.db.tarantool

import pw.binom.UUID
import pw.binom.uuid
import kotlin.random.Random
import kotlin.test.Test

class UpdateTest {

    @Test
    fun test() {
        val schemaName = Random.uuid().toShortString()
        tt {
            it.eval(
                """
s=box.schema.space.create('$schemaName',{engine = 'vinyl', if_not_exists = true})
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
            val id = Random.uuid()
            val login = Random.uuid().toString()
            val password = byteArrayOf(0, 1, 2, 3, 4)
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

            it.call(
                "box.space.$schemaName:update",
                listOf(id),
                listOf(
                    listOf("=", 2, password)
                )

            )
        }
    }
}