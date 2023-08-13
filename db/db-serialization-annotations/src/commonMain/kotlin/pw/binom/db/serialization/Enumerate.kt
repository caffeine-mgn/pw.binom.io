package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

@OptIn(ExperimentalSerializationApi::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@SerialInfo
annotation class Enumerate(val type: Type = Type.BY_NAME) {
  enum class Type {
    BY_ORDER,
    BY_NAME,
  }

  @Target(AnnotationTarget.PROPERTY)
  @Retention(AnnotationRetention.BINARY)
  annotation class Alias(vararg val name:String)
}
