package pw.binom.db.tarantool

import pw.binom.db.tarantool.protocol.Code
import pw.binom.db.tarantool.protocol.Key
import pw.binom.db.tarantool.protocol.QueryIterator

internal suspend fun <O> TarantoolConnection.select(space: Int, index: Int, key: O, offset: Int, limit: Int, iterator: QueryIterator): Pair<List<Any?>, Int> {
    val result = this.sendReceive(
            code = Code.SELECT,
            body = mapOf(
                    Key.SPACE.id to space,
                    Key.INDEX.id to index,
                    Key.KEY.id to key,
                    Key.ITERATOR.id to iterator.value,
                    Key.LIMIT.id to limit,
                    Key.OFFSET.id to offset
            )
    )
    result.assertException()
    val schemaId = result.header[Key.SCHEMA_ID.id] as Int
    val data = (result.body[Key.DATA.id] as List<Any?>?) ?: emptyList()
    return (data to schemaId)
}

internal suspend fun TarantoolConnection.insert(
        space: Int,
        values: List<Any?>) {

    val result = this.sendReceive(
            code = Code.INSERT,
            body = mapOf(
                    Key.SPACE.id to space,
                    Key.TUPLE.id to values
            )
    )

    println("Header: ${result.header}")
    println("Body: ${result.body}")
    val body = result.body
    val err = body[Key.ERROR.id] as String?
    if (err != null) {
        throw RuntimeException("Tarantool Exception: $err")
    }
}