package pw.binom.flux

import pw.binom.io.httpServer.HttpRequest

interface FluxHttpRequest : HttpRequest {
    fun getPathVariable(name: String): String?
    fun getPathVariables(): Map<String, String>
}