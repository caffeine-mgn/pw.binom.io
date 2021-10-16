package pw.binom.db.serialization

import kotlinx.serialization.SerialInfo

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@SerialInfo
annotation class EnumNameValue