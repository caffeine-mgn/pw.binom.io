package pw.binom.db.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.UUID
import pw.binom.date.DateTime
import pw.binom.date.parseIso8601Date
import pw.binom.db.serialization.codes.*
import pw.binom.toUUID

interface SQLEncoderPool {
    fun encodeValue(
        name: String,
        output: DateContainer,
        serializersModule: SerializersModule,
        useQuotes: Boolean,
        excludeGenerated: Boolean,
    ): SQLEncoder

    fun encodeStruct(
        prefix: String,
        output: DateContainer,
        serializersModule: SerializersModule,
        useQuotes: Boolean,
        excludeGenerated: Boolean,
    ): SQLCompositeEncoder

    fun encodeByteArray(
        size: Int,
        prefix: String,
        output: DateContainer,
        serializersModule: SerializersModule,
        useQuotes: Boolean,
        excludeGenerated: Boolean,
    ): SQLCompositeEncoder

    fun <T> encode(
        serializer: KSerializer<T>,
        value: T,
        name: String,
        output: DateContainer,
        serializersModule: SerializersModule = EmptySerializersModule(),
        useQuotes: Boolean,
        excludeGenerated: Boolean,
    ) {
        val encoder = encodeValue(
            name = name,
            output = output,
            serializersModule = serializersModule,
            useQuotes = useQuotes,
            excludeGenerated = excludeGenerated,
        )
        serializer.serialize(encoder, value)
    }
}

interface SQLDecoderPool {
    fun decoderValue(
        name: String,
        input: DataProvider,
        serializersModule: SerializersModule,
    ): SQLDecoder

    fun decoderStruct(
        prefix: String,
        input: DataProvider,
        serializersModule: SerializersModule,
    ): SQLCompositeDecoder

    fun decodeByteArray(
        prefix: String,
        input: DataProvider,
        serializersModule: SerializersModule,
        data: ByteArray
    ): SQLCompositeDecoder

    fun <T> decode(
        serializer: KSerializer<T>,
        name: String,
        input: DataProvider,
        serializersModule: SerializersModule = EmptySerializersModule()
    ): T {
        val decoder = decoderValue(
            name = name,
            input = input,
            serializersModule = serializersModule,
        )
        return serializer.deserialize(decoder)
    }
}

object DefaultSQLSerializePool : SQLEncoderPool, SQLDecoderPool {
    override fun encodeValue(
        name: String,
        output: DateContainer,
        serializersModule: SerializersModule,
        useQuotes: Boolean,
        excludeGenerated: Boolean,
    ): SQLEncoder {
        val c = SQLEncoderImpl(this) {}
        c.name = name
        c.useQuotes = useQuotes
        c.serializersModule = serializersModule
        c.excludeGenerated = excludeGenerated
        c.output = output
        return c
    }

    override fun encodeStruct(
        prefix: String,
        output: DateContainer,
        serializersModule: SerializersModule,
        useQuotes: Boolean,
        excludeGenerated: Boolean,
    ): SQLCompositeEncoder {
        val c = SQLCompositeEncoderImpl(this) {}
        c.prefix = prefix
        c.output = output
        c.useQuotes = useQuotes
        c.serializersModule = serializersModule
        c.excludeGenerated = excludeGenerated
        return c
    }

    override fun encodeByteArray(
        size: Int,
        prefix: String,
        output: DateContainer,
        serializersModule: SerializersModule,
        useQuotes: Boolean,
        excludeGenerated: Boolean,
    ): SQLCompositeEncoder {
        val c = ByteArraySQLCompositeEncoderImpl {}
        c.prefix = prefix
        c.output = output
        c.useQuotes = useQuotes
        c.excludeGenerated = excludeGenerated
        c.reset(
            size = size,
            serializersModule = serializersModule
        )
        return c
    }

    override fun decoderValue(
        name: String,
        input: DataProvider,
        serializersModule: SerializersModule,
    ): SQLDecoder {
        val c = SQLDecoderImpl(this) {}
        c.name = name
        c.input = input
        c.serializersModule = serializersModule
        return c
    }

    override fun decoderStruct(
        prefix: String,
        input: DataProvider,
        serializersModule: SerializersModule,
    ): SQLCompositeDecoder {
        val c = SQLCompositeDecoderImpl(this) {}
        c.input = input
        c.serializersModule = serializersModule
        c.prefix = prefix
        return c
    }

    override fun decodeByteArray(
        prefix: String,
        input: DataProvider,
        serializersModule: SerializersModule,
        data: ByteArray
    ): SQLCompositeDecoder {
        val c = ByteArraySQLCompositeDecoder {}
        c.reset(
            data = data,
            serializersModule = serializersModule
        )
        return c
    }
}

interface DataProvider {
    fun getString(key: String): String {
        val value = get(key) ?: throw NullPointerException("Value \"$key\" is null")
        return value.toString()
    }

    fun getBoolean(key: String): Boolean = getString(key).let { it == "t" || it == "true" }
    fun isNull(key: String): Boolean = get(key) == null
    fun getInt(key: String): Int = getString(key).toInt()
    fun getLong(key: String): Long = getString(key).toLong()
    fun getFloat(key: String): Float = getString(key).toFloat()
    fun getDouble(key: String): Double = getString(key).toDouble()
    fun getShort(key: String): Short = getString(key).toShort()
    fun getByte(key: String): Byte = getString(key).toByte()
    fun getByteArray(key: String): ByteArray = getString(key).encodeToByteArray()
    fun getChar(key: String): Char = getString(key).let { it[0] }
    fun getUUID(key: String): UUID = getString(key).toUUID()
    fun getDateTime(key: String): DateTime {
        val str = getString(key)
        return str.parseIso8601Date() ?: throw SerializationException("Can't parse $str to DateTime")
    }

    operator fun get(key: String): Any?
    operator fun contains(key: String): Boolean

    companion object {
        val EMPTY = object : DataProvider {
            private fun throwException(): Nothing = throw IllegalStateException("Not supported")
            override fun getString(key: String) = throwException()
            override fun getBoolean(key: String) = throwException()
            override fun isNull(key: String) = throwException()
            override fun getInt(key: String) = throwException()
            override fun getLong(key: String) = throwException()
            override fun getFloat(key: String) = throwException()
            override fun getDouble(key: String) = throwException()
            override fun getShort(key: String) = throwException()
            override fun getByte(key: String) = throwException()
            override fun getChar(key: String) = throwException()
            override fun getUUID(key: String) = throwException()
            override fun get(key: String) = throwException()
            override fun contains(key: String) = throwException()
        }
    }
}

fun interface DateContainer {
    operator fun set(key: String, value: Any?, useQuotes: Boolean)

    companion object {
        val EMPTY = DateContainer { key, value, useQuotes -> throw IllegalStateException("Not supported") }
    }
}

interface DataBinder : DataProvider, DateContainer
