@file:OptIn(ExperimentalSerializationApi::class)

package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.CompositeDecoder
import pw.binom.collections.defaultMutableMap
import pw.binom.db.DatabaseEngine
import pw.binom.db.async.AsyncResultSet
import pw.binom.db.async.pool.AsyncConnectionPool

internal class DBContextImpl(override val pool: AsyncConnectionPool,override val sql: SQLSerialization) : AbstractDBContext() {

}
