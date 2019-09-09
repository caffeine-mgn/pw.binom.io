package pw.binom.rpc.json

import pw.binom.json.*

class JRequest(
        val service: String,
        val method: String,
        val args: JsonArray
) : JDTO {
    override val factory: JDTOFactory<JDTO>
        get() = asDefault

    companion object : JDTOFactory<JRequest> {
        override val type: String
            get() = "JRequest"

        override suspend fun read(node: JsonObject) =
                JRequest(
                        service = node["service"]!!.text,
                        method = node["method"]!!.text,
                        args = node["arguments"]!!.array
                )

        override suspend fun write(obj: JRequest) =
                jsonNode {
                    string("service", obj.service)
                    string("method", obj.method)
                    array("arguments", obj.args)
                }
    }


    suspend fun arguments() = JsonFactory.readArray<JDTO?>(args)
}