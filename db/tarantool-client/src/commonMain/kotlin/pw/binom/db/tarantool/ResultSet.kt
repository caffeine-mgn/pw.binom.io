@file:Suppress("UNCHECKED_CAST")
package pw.binom.db.tarantool

import pw.binom.collections.defaultMutableList
import pw.binom.db.tarantool.protocol.Key
import kotlin.jvm.JvmInline

@JvmInline
value class Row(val values: List<Any?>) : Iterable<Any?> {
    override fun toString(): String = "Row $values"
    operator fun get(index: Int): Any? {
        if (index < 0 || index >= values.size) {
            throw IllegalArgumentException("Can't get column with index $index")
        }
        return values[index]
    }

    inline val size: Int
        get() = values.size

    override fun iterator(): Iterator<Any?> = values.iterator()
}

@JvmInline
value class Column(private val column: Map<Int, Any>) {
    val name
        get() = column[0]!! as String
    val type
        get() = column[1]!! as String

    override fun toString(): String = "Column(name: [$name], type: [$type])"
}

@JvmInline
value class ResultSet constructor(val body: Map<Int, Any?>) : Iterable<Row> {
    private inline val data
        get() = body[Key.DATA.id] as List<List<Any?>>
    private inline val meta
        get() = body[Key.METADATA.id] as List<Map<Int, Any>>?
    val columnSize
        get() = meta?.size ?: 0

    override fun toString(): String = "ResultSet(size: [${data.size}])"
    val size
        get() = data.size
    val columnNames: List<String>?
        get() {
            val meta = meta ?: return null
            val list = defaultMutableList<String>(columnSize)
            repeat(columnSize) {
                list += meta[it][0] as String
            }
            return list
        }

    fun getColumn(index: Int): Column {
        val meta = meta ?: throw IllegalArgumentException("Meta not exist")
        if (index < 0 || index >= meta.size) {
            throw IllegalArgumentException("Can't get column with index $index")
        }
        return Column(meta[index])
    }

    override fun iterator(): Iterator<Row> = object : Iterator<Row> {
        private var cur = -1
        override fun hasNext(): Boolean = cur < data.size - 1

        override fun next(): Row {
            if (cur + 1 >= data.size) {
                throw NoSuchElementException()
            }
            cur++
            return Row(data[cur])
        }
    }
}
