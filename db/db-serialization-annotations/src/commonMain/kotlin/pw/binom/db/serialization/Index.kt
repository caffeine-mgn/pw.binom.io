package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

@OptIn(ExperimentalSerializationApi::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@SerialInfo
@Repeatable
annotation class Index(val name: String, val unique: Boolean, val columns: Array<String>)