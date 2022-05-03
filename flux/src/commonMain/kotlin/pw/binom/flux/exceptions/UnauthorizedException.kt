package pw.binom.flux.exceptions

import pw.binom.io.httpServer.HttpResponse

class UnauthorizedException(val realm: String? = null, val service: String? = null) : WebException() {
    override fun processing(response: HttpResponse) {
        response.status = 401
        response.headers.requestBasicAuth(realm = realm, service = service)
    }
}
