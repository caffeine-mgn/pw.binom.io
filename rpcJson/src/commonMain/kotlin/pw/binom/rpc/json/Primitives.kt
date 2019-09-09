package pw.binom.rpc.json

import pw.binom.json.*

interface Primitive<T> : JDTO {
    val value: T
}

class JByte(override val value: Byte) : Primitive<Byte> {
    override val factory: JDTOFactory<JDTO>
        get() = asDefault

    companion object : JDTOFactory<JByte> {
        override val type: String
            get() = "b"

        override suspend fun read(node: JsonObject): JByte =
                JByte(node["value"]!!.byte)

        override suspend fun write(obj: JByte): JsonObject =
                jsonNode {
                    number("value", obj.value)
                }
    }
}

class JBoolean(override val value: Boolean) : Primitive<Boolean> {
    override val factory: JDTOFactory<JDTO>
        get() = asDefault


    companion object : JDTOFactory<JBoolean> {
        override val type: String
            get() = "a"

        override suspend fun read(node: JsonObject): JBoolean =
                JBoolean(node["value"]!!.boolean)

        override suspend fun write(obj: JBoolean) =
                jsonNode {
                    bool("value", obj.value)
                }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as JBoolean

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}

class JLong(override val value: Long) : Primitive<Long> {
    override val factory: JDTOFactory<JDTO>
        get() = asDefault

    companion object : JDTOFactory<JLong> {
        override val type: String
            get() = "z"

        override suspend fun read(node: JsonObject): JLong =
                JLong(node["value"]!!.long)

        override suspend fun write(obj: JLong) =
                jsonNode {
                    number("value", obj.value)
                }
    }
}

class JInt(override val value: Int) : Primitive<Int> {
    override val factory: JDTOFactory<JDTO>
        get() = asDefault

    companion object : JDTOFactory<JInt> {
        override val type: String
            get() = "i"

        override suspend fun read(node: JsonObject): JInt =
                JInt(node["value"]!!.int)

        override suspend fun write(obj: JInt) =
                jsonNode {
                    number("value", obj.value)
                }
    }
}

class JFloat(override val value: Float) : Primitive<Float> {
    override val factory: JDTOFactory<JDTO>
        get() = asDefault

    companion object : JDTOFactory<JFloat> {
        override val type: String
            get() = "f"

        override suspend fun read(node: JsonObject): JFloat =
                JFloat(node["value"]!!.float)

        override suspend fun write(obj: JFloat) =
                jsonNode {
                    number("value", obj.value)
                }
    }
}

class JString(override val value: String) : Primitive<String> {
    override val factory: JDTOFactory<JDTO>
        get() = asDefault


    companion object : JDTOFactory<JString> {

        val EMPTY: JString = "".jdto

        override val type: String
            get() = "s"

        override suspend fun read(node: JsonObject): JString =
                JString(node["value"]!!.text)

        override suspend fun write(obj: JString) =
                jsonNode {
                    string("value", obj.value)
                }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as JString

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}

class JSet<T : JDTO?>(val value: Set<T>) : Set<T> by value, JDTO {
    override val factory: JDTOFactory<JDTO>
        get() = asDefault

    companion object : JDTOFactory<JSet<*>> {
        override val type: String
            get() = "jset"

        override suspend fun read(node: JsonObject): JSet<*> =
                JSet(
                        node["values"]!!.array.map {
                            it?.let { JsonFactory.read<JDTO>(it.obj) }
                        }.toSet()
                )

        override suspend fun write(obj: JSet<*>) =
                jsonNode {
                    array("values") {
                        obj.value.forEach {
                            val value = JsonFactory.write(it)
                            if (value == null)
                                itemNull()
                            else
                                node(value)
                        }

                    }
                }
    }
}

class JList<T : JDTO?>(val value: List<T>) : List<T> by value, JDTO {
    override val factory: JDTOFactory<JDTO>
        get() = asDefault

    constructor() : this(emptyList())

    companion object : JDTOFactory<JList<*>> {
        override val type: String
            get() = "jlist"

        override suspend fun read(node: JsonObject): JList<*> =
                JList(
                        node["values"]!!.array.map {
                            it?.let { JsonFactory.read<JDTO>(it.obj) }
                        }
                )

        override suspend fun write(obj: JList<*>) =
                jsonNode {
                    array("values") {
                        obj.value.forEach {
                            val value = JsonFactory.write(it)
                            if (value == null)
                                itemNull()
                            else
                                node(value)
                        }
                    }
                }
    }
}

fun <T : JDTO?> JList.Companion.empty() = JList<T>(emptyList())

class JMap<K : JDTO?, V : JDTO?>(val value: Map<K, V> = emptyMap()) : Map<K, V>, JDTO {

    override val factory: JDTOFactory<JDTO>
        get() = asDefault

    override val entries: Set<Map.Entry<K, V>>
        get() = value.entries
    override val keys: Set<K>
        get() = value.keys
    override val size: Int
        get() = value.size
    override val values: Collection<V>
        get() = value.values

    override fun containsKey(key: K): Boolean = value.containsKey(key)
    override fun containsValue(value: V): Boolean = this.value.containsValue(value)
    override fun get(key: K): V? = value.get(key)
    override fun isEmpty(): Boolean = value.isEmpty()

    companion object : JDTOFactory<JMap<*, *>> {
        override val type: String
            get() = "jmap"

        override suspend fun read(node: JsonObject): JMap<*, *> =
                JMap(
                        node["values"]!!.array.map { JPair.read(it!!.obj) }.associate {
                            it.first to it.secod
                        }
                )

        override suspend fun write(obj: JMap<*, *>): JsonObject {
            val out = JsonArray()
            obj.value.forEach {
                out.add(JPair(it.key, it.value).write())
            }
            return jsonNode {
                array("values", out)
            }
        }
    }
}

class JPair<FIRST : JDTO?, SECOD : JDTO?>(val first: FIRST, val secod: SECOD) : JDTO {
    override val factory: JDTOFactory<JDTO>
        get() = asDefault

    companion object : JDTOFactory<JPair<*, *>> {
        override val type: String
            get() = "pair"

        override suspend fun read(node: JsonObject) =
                JPair(
                        first = node["first"]?.let { JsonFactory.read<JDTO>(it.obj) },
                        secod = node["second"]?.let { JsonFactory.read<JDTO>(it.obj) }
                )

        override suspend fun write(obj: JPair<*, *>) =
                jsonNode {
                    node("first", JsonFactory.write(obj.first))
                    node("second", JsonFactory.write(obj.secod))
                }
    }
}

val <A : JDTO?, B : JDTO> Pair<A, B>.jdto: JPair<A, B>
    get() = JPair(first, second)

fun <FIRST : JDTO?, SECOD : JDTO?> jpair(first: FIRST, secod: SECOD) = JPair(first, secod)

fun <K : JDTO?, V : JDTO?> Map<K, V>.jdto() = JMap(this)
fun <T : JDTO?> List<T>.jdto() = JList(this)
fun <T : JDTO?> Set<T>.jdto() = JSet(this)

val Long.jdto: JLong
    get() = JLong(this)

val Int.jdto: JInt
    get() = JInt(this)

val String.jdto: JString
    get() = JString(this)

val Float.jdto: JFloat
    get() = JFloat(this)

val Boolean.jdto: JBoolean
    get() = JBoolean(this)

val Byte.jdto: JByte
    get() = JByte(this)