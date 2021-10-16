package pw.binom.db.serialization

import kotlinx.serialization.SerialInfo

/**
 * Define column type for standard SQL
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
@SerialInfo
annotation class ColumnType(val type: String)

/**
 * Define column type for PostgreSQL specific type
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
@SerialInfo
annotation class PGColumnType(val type: String)

/**
 * Define column type for SQLite specific type
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
@SerialInfo
annotation class SqliteColumnType(val type: String)