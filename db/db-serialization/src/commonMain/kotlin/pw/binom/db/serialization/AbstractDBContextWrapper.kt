package pw.binom.db.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import pw.binom.db.DatabaseEngine

abstract class AbstractDBContextWrapper : DBContext {
  protected abstract fun init(): DBContext
  private val instance by lazy {
    init()
  }

  override fun getDescription2(serializer: KSerializer<*>): EntityDescription2 =
    instance.getDescription2(serializer)

  override suspend fun <T> re(function: suspend (DBAccess) -> T): T =
    instance.re(function)

  override suspend fun <T> re2(function: suspend (DBAccess2) -> T): T =
    instance.re2(function)

  override suspend fun <T> su(function: suspend (DBAccess) -> T): T =
    instance.su(function)

  override suspend fun <T> su2(function: suspend (DBAccess2) -> T): T =
    instance.su2(function)

  override suspend fun <T> new(function: suspend (DBAccess) -> T): T =
    instance.new(function)

  override suspend fun <T> new2(function: suspend (DBAccess2) -> T): T =
    instance.new2(function)

  override suspend fun <T> no(function: suspend (DBAccess2) -> T): T =
    instance.no(function)

  override suspend fun createSchema(serializer: KSerializer<out Any>, ifNotExist: Boolean, tableName: String?) {
    instance.createSchema(serializer, ifNotExist, tableName)
  }

  override fun generateSchema(
    engine: DatabaseEngine,
    serializer: KSerializer<out Any>,
    ifNotExist: Boolean,
    tableName: String?,
  ): List<String> =
    instance.generateSchema(engine,serializer, ifNotExist, tableName)

  override fun getDescription(serialDescriptor: SerialDescriptor): EntityDescription =
    instance.getDescription(serialDescriptor)

  override fun getDescription(serializer: KSerializer<out Any>): EntityDescription {
    return super.getDescription(serializer)
  }

  override suspend fun asyncClose() {
    instance.asyncClose()
  }
}
