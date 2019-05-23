package pw.binom.json

import pw.binom.io.AsyncAppendable

interface ObjectCtx {
    suspend fun node(name: String, func: suspend ObjectCtx.() -> Unit)
    suspend fun array(name: String, func: suspend ArrayCtx.() -> Unit)
    suspend fun string(name: String, value: String)
    suspend fun number(name: String, value: Double)
    suspend fun number(name: String, value: Float)
    suspend fun number(name: String, value: Int)
    suspend fun number(name: String, value: Long)
    suspend fun bool(name: String, value: Boolean)
    suspend fun attrNull(name: String)
}

interface ArrayCtx {
    suspend fun node(func: suspend ObjectCtx.() -> Unit)
    suspend fun array(func: suspend ArrayCtx.() -> Unit)
    suspend fun item(value: String)
    suspend fun item(value: Double)
    suspend fun item(value: Float)
    suspend fun item(value: Int)
    suspend fun item(value: Long)
    suspend fun bool(value: Boolean)
    suspend fun itemNull()
}

class ArrayCtxImpl(private val visiter: JsonArrayVisiter) : ArrayCtx {
    override suspend fun bool(value: Boolean) {
        visiter.element().booleanValue(value)
    }

    override suspend fun itemNull() {
        visiter.element().nullValue()
    }

    override suspend fun array(func: suspend ArrayCtx.() -> Unit) {
        val w = visiter.element().arrayValue()
        w.start()
        ArrayCtxImpl(w).func()
        w.end()
    }

    override suspend fun node(func: suspend ObjectCtx.() -> Unit) {
        val w = visiter.element().objectValue()
        w.start()
        ObjectCtxImpl(w).func()
        w.end()
    }

    override suspend fun item(value: String) {
        visiter.element().textValue(value)
    }

    override suspend fun item(value: Double) {
        visiter.element().numberValue(value.toString())
    }

    override suspend fun item(value: Float) {
        visiter.element().numberValue(value.toString())
    }

    override suspend fun item(value: Int) {
        visiter.element().numberValue(value.toString())
    }

    override suspend fun item(value: Long) {
        visiter.element().numberValue(value.toString())
    }
}

class ObjectCtxImpl(private val visiter: JsonObjectVisiter) : ObjectCtx {
    override suspend fun attrNull(name: String) {
        visiter.property(name).nullValue()
    }

    override suspend fun bool(name: String, value: Boolean) {
        visiter.property(name).booleanValue(value)
    }

    override suspend fun array(name: String, func: suspend ArrayCtx.() -> Unit) {
        val w = visiter.property(name).arrayValue()
        w.start()
        ArrayCtxImpl(w).func()
        w.end()
    }

    override suspend fun node(name: String, func: suspend ObjectCtx.() -> Unit) {
        val w = visiter.property(name).objectValue()
        w.start()
        ObjectCtxImpl(w).func()
        w.end()
    }

    override suspend fun string(name: String, value: String) {
        visiter.property(name).textValue(value)
    }

    override suspend fun number(name: String, value: Double) {
        visiter.property(name).numberValue(value.toString())
    }

    override suspend fun number(name: String, value: Float) {
        visiter.property(name).numberValue(value.toString())
    }

    override suspend fun number(name: String, value: Int) {
        visiter.property(name).numberValue(value.toString())
    }

    override suspend fun number(name: String, value: Long) {
        visiter.property(name).numberValue(value.toString())
    }
}

suspend fun jsonNode(appendable: AsyncAppendable, func: suspend ObjectCtx.() -> Unit) {
    val w = JsonWriter(appendable)
    val v = w.objectValue()
    v.start()
    ObjectCtxImpl(v).func()
    v.end()
}

suspend fun jsonArray(appendable: AsyncAppendable, func: suspend ArrayCtx.() -> Unit) {
    val w = JsonWriter(appendable)
    val v = w.arrayValue()
    v.start()
    ArrayCtxImpl(v).func()
    v.end()
}