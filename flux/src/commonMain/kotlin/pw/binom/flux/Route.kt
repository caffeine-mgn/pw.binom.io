package pw.binom.flux

interface Route{
    fun route(path: String): Route
    fun endpoint(method: String, path: String, func: suspend (Action) -> Boolean)
}