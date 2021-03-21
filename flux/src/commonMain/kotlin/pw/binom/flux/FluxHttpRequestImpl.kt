package pw.binom.flux

import pw.binom.io.httpServer.HttpRequest

class FluxHttpRequestImpl(val mask: String, request: HttpRequest) : FluxHttpRequest, HttpRequest by request {
    override fun getPathVariable(name: String): String? = path.getVariable(name = name, mask = mask)
    override fun getPathVariables() = path.getVariables(mask = mask)
}