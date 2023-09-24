package pw.binom.http.rest

@Deprecated(message = "For internal use", level = DeprecationLevel.HIDDEN)
interface Endpoint {
  val defaultResponseCode: Int
    get() = 200
}
