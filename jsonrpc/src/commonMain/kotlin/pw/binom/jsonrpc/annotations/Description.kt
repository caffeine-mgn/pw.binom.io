package pw.binom.jsonrpc.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class Description(
  val description: String,
)
