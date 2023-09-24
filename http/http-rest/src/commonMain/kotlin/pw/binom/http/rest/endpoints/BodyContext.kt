package pw.binom.http.rest.endpoints

import kotlinx.serialization.KSerializer

interface BodyContext<T> {
  val bodySerializer: KSerializer<T>
}
