package pw.binom.http.rest.endpoints

import kotlinx.serialization.KSerializer

@Deprecated(message = "For remove")
interface BodyContext<T> {
  val bodySerializer: KSerializer<T>
}
