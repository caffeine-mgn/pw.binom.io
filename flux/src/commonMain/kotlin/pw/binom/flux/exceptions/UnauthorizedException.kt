package pw.binom.flux.exceptions

import pw.binom.io.httpServer.HttpResponse

class UnauthorizedException(val realm: String? = null, val service: String? = null, val response: String? = null) :
    WebException() {
    override suspend fun processing(response: HttpResponse) {
        response.status = 401
        response.headers.requestBasicAuth(realm = realm, service = service)
        if (this.response != null) {
            response.sendText(this.response)
        }
    }
}
