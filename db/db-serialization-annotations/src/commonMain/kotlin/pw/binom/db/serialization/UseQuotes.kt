package pw.binom.db.serialization

import kotlinx.serialization.SerialInfo

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@SerialInfo
annotation class UseQuotes