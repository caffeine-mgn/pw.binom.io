package pw.binom.db.tarantool.protocol

// Iterator info was taken from here https://github.com/tarantool/tarantool/blob/f66584c3bcdffe61d6d99a4868a9b72d62338a11/src/box/iterator_type.h#L62
enum class QueryIterator(  // tuples in distance ascending order from specified point
        val value: Int) {
    EQ(0),  // key == x ASC order
    REQ(1),  // key == x DESC order
    ALL(2),  // all tuples
    LT(3),  // key <  x
    LE(4),  // key <= x
    GE(5),  // key >= x
    GT(6),  // key >  x
    BITS_ALL_SET(7),  // all bits from x are set in key
    BITS_ANY_SET(8),  // at least one x's bit is set
    BITS_ALL_NOT_SET(9),  // all bits are not set
    OVERLAPS(10),  // key overlaps x
    NEIGHBOR(11);

    companion object {
        fun valueOf(value: Int) = values()
                .find { value == it.value }
                ?: throw IllegalArgumentException()

    }
}
