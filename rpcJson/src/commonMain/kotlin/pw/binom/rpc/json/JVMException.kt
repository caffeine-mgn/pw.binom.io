package pw.binom.rpc.json

import pw.binom.json.JsonObject
import pw.binom.json.jsonNode
import pw.binom.json.text

class JVMException(message: String?) : JDTO, Exception(message) {
    override val factory: JDTOFactory<JDTO>
        get() = asDefault

    companion object : JDTOFactory<JVMException> {
        override val type: String
            get() = "JVMException"

        override suspend fun read(node: JsonObject) =
                JVMException(
                        node["message"]?.text
                )

        override suspend fun write(obj: JVMException) =
                jsonNode {
                    string("message", obj.message)
                }
    }
}