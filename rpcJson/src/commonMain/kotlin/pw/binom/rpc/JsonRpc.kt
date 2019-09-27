package pw.binom.rpc

import pw.binom.json.*
import pw.binom.krpc.RPCService
import pw.binom.krpc.Struct
import pw.binom.krpc.StructFactory
import pw.binom.krpc.StructLibrary

object JsonRpc {

    private fun fromJson(lib: StructLibrary, node: JsonObject, clazz: StructFactory.Class.Struct?): Struct {
        val name = node["@type"]?.string ?: clazz?.factory?.name
        ?: throw IllegalStateException("Can't detect type of struct")
        val factory = lib.getByName(name) ?: throw IllegalStateException("Can't find factory for \"$name\"")
        return factory.newInstance(factory.fields.map { field ->
            node[field.name]?.let { fromJSON(lib, it, field.type) }
        })
    }

    fun fromJSON(lib: StructLibrary, node: JsonNode, clazz: StructFactory.Class): Any =
            when (clazz) {
                is StructFactory.Class.Boolean -> node.boolean
                is StructFactory.Class.Byte -> node.byte
                is StructFactory.Class.Short -> node.int.toShort()
                is StructFactory.Class.Int -> node.int
                is StructFactory.Class.Long -> node.long
                is StructFactory.Class.Float -> node.float
                is StructFactory.Class.Double -> node.double
                is StructFactory.Class.String -> node.string
                is StructFactory.Class.Char -> node.string[0]
                is StructFactory.Class.Struct -> fromJson(lib, node.obj, clazz)
                is StructFactory.Class.Any -> fromJson(lib, node.obj, null)
                is StructFactory.Class.Array -> node.array.map {
                    it?.let { fromJSON(lib, it, clazz.type) }
                }
                is StructFactory.Class.Void -> Unit
            }

    private fun toJSON(struct: Struct): JsonObject {
        val out = JsonObject()
        val factory = struct.factory as StructFactory<Struct>
        factory.fields.forEach { field ->
            val value = factory.getField(struct, field.index)
            out[field.name] = value?.let { toJSON(it, field.type) }
        }
        out["@type"] = JsonString(factory.name)
        return out
    }

    fun toJSON(obj: Any, clazz: StructFactory.Class): JsonNode =
            when (clazz) {
                is StructFactory.Class.Boolean -> JsonBoolean(obj as Boolean)
                is StructFactory.Class.Byte,
                is StructFactory.Class.Short,
                is StructFactory.Class.Int,
                is StructFactory.Class.Long,
                is StructFactory.Class.Float,
                is StructFactory.Class.Double -> JsonNumber(obj.toString())
                is StructFactory.Class.String,
                is StructFactory.Class.Char -> JsonString(obj.toString())
                is StructFactory.Class.Any,
                is StructFactory.Class.Struct -> toJSON(obj as Struct)
                is StructFactory.Class.Array -> (obj as List<Any?>).map {
                    toJSON(it ?: return@map null, clazz.type)
                }.let { JsonArray(it.toMutableList()) }
                StructFactory.Class.Void -> JsonBoolean(false)
            }

    suspend fun <Sync, Async> callAsync(structLibrary: StructLibrary, service: RPCService<Sync, Async>, implementation: Async, node: JsonObject): JsonNode {
        val methodName = node["method"]!!.string
        val argNode = node["args"]?.obj?: JsonObject()
        val method = service.methods.find { it.name == methodName } ?: throw MethodNotFoundException(methodName)
        val argsList = method.args.map { arg ->
            argNode[arg.first]?.let { JsonRpc.fromJSON(structLibrary, it, arg.second) }
        }
        return try {
            val result = service.callAsync(implementation, method.index, argsList)?.takeIf { it !== Unit }
            jsonNode {
                bool("error", false)
                node("result", result?.let { toJSON(it, method.result) })
            }
        } catch (e: Throwable) {
            check(e is Struct) { "Exception must implementation Struct interface. Exception: $e" }
            jsonNode {
                bool("error", true)
                node("result", toJSON(e))
            }
        }
    }

    open class JsonRpcException(message: String) : RuntimeException(message)
    class MethodNotFoundException(val method: String) : JsonRpcException("Method \"$method\" not found")

    /*
    fun implement(func: (JsonObject) -> Any?): (RPCService.Method, List<Any?>) -> Any? = { method, args ->
        val node = jsonNode {
            string("method", method.name)
            var i = 0
            node("args", JsonObject(method.args.associate { field ->
                field.first to args[i++]?.let { toJSON(it, field.second) }
            }.toMutableMap()))
        }

        func(node)
    }
    */

    fun implementAsync(structLibrary: StructLibrary, func: suspend (JsonObject) -> JsonObject): suspend (RPCService.Method, List<Any?>) -> Any? = { method, args ->
        val node = jsonNode {
            string("method", method.name)
            var i = 0
            node("args", JsonObject(method.args.associate { field ->
                field.first to args[i++]?.let { toJSON(it, field.second) }
            }.toMutableMap()))
        }

        val result = func(node)
        val error = result["error"]?.boolean == true
        val obj = result["result"]
        if (error) {
            obj ?: throw JsonRpcException("Exception is null")
            val e = fromJSON(structLibrary, obj, StructFactory.Class.Any(false)) as? Throwable
            e ?: throw JsonRpcException("$e is not Throuble")
            throw e
        } else {
            if (obj == null)
                null
            else
                fromJSON(structLibrary, obj, method.result)
        }
    }
}