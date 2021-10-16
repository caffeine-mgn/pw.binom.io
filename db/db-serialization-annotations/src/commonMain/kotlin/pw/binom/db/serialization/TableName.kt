package pw.binom.db.serialization

import kotlinx.serialization.SerialInfo

/**
 * Define default table name for entity
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@SerialInfo
annotation class TableName(val tableName: String)