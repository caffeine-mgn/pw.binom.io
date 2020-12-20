package pw.binom.krpc

import kotlin.Boolean as Bool

interface Struct {
    val factory: StructFactory<out Struct>
}

interface StructFactory<T : Struct> {
    sealed class Class(val nullable: Bool) {
        object Void : Class(false)
        class Boolean(nullable: Bool) : Class(nullable)
        class Byte(nullable: Bool) : Class(nullable)
        class Char(nullable: Bool) : Class(nullable)
        class Short(nullable: Bool) : Class(nullable)
        class Int(nullable: Bool) : Class(nullable)
        class Long(nullable: Bool) : Class(nullable)
        class Float(nullable: Bool) : Class(nullable)
        class Double(nullable: Bool) : Class(nullable)
        class String(nullable: Bool) : Class(nullable)
        class Any(nullable: Bool) : Class(nullable)
        class Array(val type: Class, nullable: Bool) : Class(nullable)
        class Struct(val factory: StructFactory<out pw.binom.krpc.Struct>, nullable: Bool) : Class(nullable)
    }

    class Field(val index: Int, val name: String, val type: Class)

    val name: String
    val uid: UInt
    fun newInstance(fields: List<Any?>): T
    val fields: List<Field>
    fun getField(dto: T, index: Int): Any?
}

interface RPCService<Sync, Async> {
    class Method(val index: Int, val name: String, val args: List<Pair<String, StructFactory.Class>>, val result: StructFactory.Class)

    val methods: List<Method>

    fun call(service: Sync, index: Int, args: List<Any?>): Any?
    suspend fun callAsync(service: Async, index: Int, args: List<Any?>): Any?
}

interface StructLibrary {
    fun getByUid(uid: UInt): StructFactory<out Struct>?
    fun getByName(name: String): StructFactory<out Struct>?
}

class SimpleStructLibrary(val factories: List<StructFactory<out Struct>>) : StructLibrary {
    private val uids = HashMap<UInt, StructFactory<out Struct>>()
    private val names = HashMap<String, StructFactory<out Struct>>()

    init {
        factories.forEach {
            uids[it.uid] = it
            names[it.name] = it
        }
    }

    override fun getByUid(uid: UInt): StructFactory<out Struct>? = uids[uid]

    override fun getByName(name: String): StructFactory<out Struct>? = names[name]
}

abstract class CommonStructLibrary(private val libraries: List<StructLibrary>) : StructLibrary {
    override fun getByUid(uid: UInt): StructFactory<out Struct>? =
            libraries.asSequence().map { it.getByUid(uid) }.filterNotNull().firstOrNull()

    override fun getByName(name: String): StructFactory<out Struct>? =
            libraries.asSequence().map { it.getByName(name) }.filterNotNull().firstOrNull()
}