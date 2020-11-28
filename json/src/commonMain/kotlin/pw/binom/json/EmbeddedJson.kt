package pw.binom.json

interface ObjectCtx {
    fun node(name: String, func: ObjectCtx.() -> Unit)
    fun node(name: String, obj: JsonNode?)
    fun array(name: String, func: ArrayCtx.() -> Unit)
    fun array(name: String, array: JsonArray?)
    fun string(name: String, value: String?)
    fun number(name: String, value: Double?)
    fun number(name: String, value: Float?)
    fun number(name: String, value: Int?)
    fun number(name: String, value: Long?)
    fun number(name: String, value: Byte?)
    fun bool(name: String, value: Boolean?)
    fun nil(name: String)
}

interface ArrayCtx {
    fun node(func: ObjectCtx.() -> Unit)
    fun node(obj: JsonNode?)
    fun array(func: ArrayCtx.() -> Unit)
    fun string(value: String?)
    fun number(value: Double?)
    fun number(value: Float?)
    fun number(value: Int?)
    fun number(value: Long?)
    fun bool(value: Boolean?)
    fun nil()
}

fun Sequence<JsonNode?>.toJsonArray() = JsonArray(toMutableList())

fun Iterable<JsonNode?>.toJsonArray() = JsonArray(toMutableList())

class ArrayCtxImpl : ArrayCtx {
    val node = JsonArray()
    override fun node(obj: JsonNode?) {
        node.add(obj)
    }

    override fun bool(value: Boolean?) {
        node.add(value?.let { JsonBoolean(it) })
    }

    override fun nil() {
        node.add(null)
    }

    override fun array(func: ArrayCtx.() -> Unit) {
        val out = ArrayCtxImpl()
        out.func()
        node(out.node)
    }

    override fun node(func: ObjectCtx.() -> Unit) {
        val out = ObjectCtxImpl()
        out.func()
        node(out.node)
    }

    override fun string(value: String?) {
        node.add(value?.let { JsonString(it) })
    }

    override fun number(value: Double?) {
        node.add(value?.let { JsonNumber(it.toString()) })
    }

    override fun number(value: Float?) {
        node.add(value?.let { JsonNumber(it.toString()) })
    }

    override fun number(value: Int?) {
        node.add(value?.let { JsonNumber(it.toString()) })
    }

    override fun number(value: Long?) {
        node.add(value?.let { JsonNumber(it.toString()) })
    }
}

class ObjectCtxImpl : ObjectCtx {
    val node = JsonObject()
    override fun array(name: String, array: JsonArray?) {
        node[name] = array
    }

    override fun node(name: String, obj: JsonNode?) {
        node[name] = obj
    }

    override fun nil(name: String) {
        node[name] = null
    }

    override fun bool(name: String, value: Boolean?) {
        node[name] = value?.let { JsonBoolean(it) }
    }

    override fun array(name: String, func: ArrayCtx.() -> Unit) {
        val out = ArrayCtxImpl()
        out.func()
        node(name, out.node)
    }

    override fun node(name: String, func: ObjectCtx.() -> Unit) {
        val out = ObjectCtxImpl()
        out.func()
        node(name, out.node)
    }

    override fun string(name: String, value: String?) {
        node[name] = value?.let { JsonString(it) }
    }


    override fun number(name: String, value: Double?) {
        node[name] = value?.let { JsonNumber(it.toString()) }
    }

    override fun number(name: String, value: Float?) {
        node[name] = value?.let { JsonNumber(it.toString()) }
    }

    override fun number(name: String, value: Byte?) {
        node[name] = value?.let { JsonNumber(it.toString()) }
    }

    override fun number(name: String, value: Int?) {
        node[name] = value?.let { JsonNumber(it.toString()) }
    }

    override fun number(name: String, value: Long?) {
        node[name] = value?.let { JsonNumber(it.toString()) }
    }
}

fun jsonNode(func: ObjectCtx.() -> Unit): JsonObject {
    val out = ObjectCtxImpl()
    out.func()
    return out.node
}

fun jsonArray(func: ArrayCtx.() -> Unit): JsonArray {
    val out = ArrayCtxImpl()
    out.func()
    return out.node
}