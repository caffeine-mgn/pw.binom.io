package pw.binom.rpc.json

import pw.binom.json.*

interface JDTO {
    val factory: JDTOFactory<JDTO>

    suspend fun write() = JsonFactory.write(this)
}

object JUnit : JDTO, JDTOFactory<JUnit> {
    override val type: String
        get() = "unit"

    override suspend fun read(node: JsonObject) = JUnit

    override suspend fun write(obj: JUnit) = JsonObject()

    override val factory: JDTOFactory<JDTO>
        get() = this.asDefault


}

interface JDTOFactory<T : JDTO> {
    val type: String

    suspend fun read(node: JsonObject): T
    suspend fun write(obj: T): JsonObject
}

val JDTOFactory<*>.asDefault: JDTOFactory<JDTO>
    get() = this as JDTOFactory<JDTO>

fun JDTOFactory<*>.reg() {
    JsonFactory.reg(this)
}

suspend fun <T : JDTO> JsonObject.read():T = JsonFactory.read<T>(this)

suspend fun <V : JDTO, T : JDTOFactory<V>> T.read(json: String) = read(json.parseJSON().obj)

object JsonFactory {
    private val factories = HashMap<String, JDTOFactory<JDTO>>()

    fun clearFactories() {
        factories.clear()
    }

    fun reg(factory: JDTOFactory<*>) {
        if (factories.containsKey(factory.type))
            TODO("Factory \"${factory.type}\" already exist")

        factories[factory.type] = factory as JDTOFactory<JDTO>
    }

//    fun <T : JDTO?> read(node: String): T = read(JsonNode.parse(node))

    suspend fun <T : JDTO?> read(node: JsonObject): T {
        val type = node["@type"]?.text ?: TODO("Type not set. node=$node")
        val factory = factories[type] ?: TODO("Can't find factory for ${type}")
        return factory.read(node) as T
    }

    suspend fun <T : JDTO> write(obj: T?): JsonObject? {
        if (obj == null) {
            return null
        }
        val n = obj.factory.write(obj)
        n["@type"] = JsonString(obj.factory.type)
        return n
    }

    suspend fun <T : JDTO?> readArray(node: JsonArray): List<T> {
        return node.map {
            if (it == null)
                null as T
            else
                read<T>(it.obj)
        }
    }

    suspend fun <T : JDTO> writeArray(array: List<T?>): JsonNode {
        val out = JsonArray()
        array.forEach {
            if (it == null)
                out.add(null)
            else
                out.add(write(it))
        }

        return out
    }

    suspend fun <T : JDTO> writeArray(array: Array<T?>): JsonArray {
        val out = JsonArray()
        array.forEach {
            if (it == null)
                out.add(null)
            else
                out.add(write(it))
        }

        return out
    }

    init {
        reg(JInt)
        reg(JFloat)
        reg(JString)
        reg(JMap)
        reg(JList)
        reg(JLong)
        reg(JBoolean)
        reg(JByte)
        reg(JSet)
        reg(JUnit)
        reg(JPair)
        reg(JRequest)
        reg(JResponce)
        reg(JVMException)
    }
}

suspend fun <T : JDTO> JsonObject.dto(): T = JsonFactory.read(this)
suspend fun <T : JDTO> T.json() = JsonFactory.write(this)
/*
internal expect fun objectKeys(obj: JDTO): Array<String>
internal expect fun getFieldValue(obj: JDTO, field: String): Any?
internal expect fun setFieldValue(obj: JDTO, field: String, value: Any?)
internal expect fun <T : JDTO> newInstance(clazz: KClass<T>): T

internal expect fun checkConstructor(any: KClass<*>)

abstract class AutoJDTOFactory<T : JDTO>(val clazz: KClass<T>, override val type: String = clazz.simpleName!!) : JDTOFactory<T> {
    init {
        checkConstructor(clazz)
    }

    override fun read(node: JsonNode): T {
        val out = newInstance(clazz)
        node.fields().forEach { it ->
            if (it == "Companion" || it == "@type")
                return@forEach
            setFieldValue(out, it, readValue(node[it]))
        }

        return out
    }

    override fun write(obj: T): JsonNode {
        val out = JsonNode.obj()
        objectKeys(obj).forEach {
            if (it == "Companion" || it == "@type")
                return@forEach
            try {
                out[it] = writeValue(getFieldValue(obj, it))
            } catch (e: Throwable) {
                throw RuntimeException("Can't write field $it of class ${clazz.simpleName}")
            }
        }
        return out
    }

    private fun readValue(node: JsonNode?): Any? {
        node ?: return null
        return JsonFactory.read<JDTO>(node)
    }

    private fun writeValue(obj: Any?): JsonNode? {
        obj ?: return null
        return when (obj) {
            is JDTO -> JsonFactory.write(obj)
            else -> TODO("Unknown type of ${obj::class.simpleName}")
        }
    }
}
*/