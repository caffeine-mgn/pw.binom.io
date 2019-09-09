package pw.binom.rpc.json

import pw.binom.json.JsonObject
import pw.binom.json.boolean
import pw.binom.json.jsonNode
import pw.binom.json.obj

class JResponce(val error: Boolean, val obj: JsonObject?) : JDTO {
    override val factory: JDTOFactory<JDTO>
        get() = asDefault

    companion object : JDTOFactory<JResponce> {
        override val type: String
            get() = "JResponce"

        override suspend fun read(node: JsonObject) = JResponce(
                error = node["error"]!!.boolean,
                obj = node["obj"]?.obj
        )

        override suspend fun write(obj: JResponce) =
                jsonNode {
                    bool("error", obj.error)
                    node("obj",obj.obj)
                }

        suspend fun ok(obj: JDTO?) = JResponce(error = false, obj = obj?.let { it.write() })
        suspend fun error(obj: JDTO) = JResponce(error = true, obj = obj.write())
    }
}