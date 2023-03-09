package pw.binom.flux

import pw.binom.url.PathMask

interface FluxHttpRouting {
    fun route(path: PathMask, handler: FluxHttpHandler)
    fun route(method: String, path: PathMask, handler: FluxHttpHandler)
    fun get(path: PathMask, handler: FluxHttpHandler)
    fun options(path: PathMask, handler: FluxHttpHandler)
    fun post(path: PathMask, handler: FluxHttpHandler)
    fun put(path: PathMask, handler: FluxHttpHandler)
    fun delete(path: PathMask, handler: FluxHttpHandler)
}
