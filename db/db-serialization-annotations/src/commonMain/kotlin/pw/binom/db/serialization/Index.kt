package pw.binom.db.serialization

import kotlinx.serialization.SerialInfo

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@SerialInfo
@Repeatable
annotation class Index(
    val name: String,
    val unique: Boolean,
    val columns: String,
)