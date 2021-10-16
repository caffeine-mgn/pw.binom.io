package pw.binom.db.serialization

import kotlinx.serialization.SerialInfo

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
@SerialInfo
annotation class EnumAliasValue(val alias:String)