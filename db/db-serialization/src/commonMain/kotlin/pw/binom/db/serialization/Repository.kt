package pw.binom.db.serialization

import kotlinx.serialization.KSerializer
import pw.binom.db.async.pool.*

abstract class Repository<T : Any, ID : Any>(
    val serializer: KSerializer<T>,
    val sqlSerialization: SQLSerialization = SQLSerialization.DEFAULT
) {
    protected abstract val db: AsyncConnectionPool
    private val idFieldName = run {
        for (i in 0 until serializer.descriptor.elementsCount) {
            serializer.descriptor.getElementAnnotation<Id>(i) ?: continue
            return@run serializer.descriptor.getElementName(i)
        }
        throw IllegalArgumentException("Can't find Id field for Entity ${serializer.descriptor.serialName}")
    }

    suspend fun findById(id: ID): T? =
        db.borrow {
            selectOneOrNull(SELECT_BY_ID, "id" to id)
        }

    suspend fun updateById(value: T) {
        db.borrow {
            execute(UPDATE_BY_ID, *sqlSerialization.nameParams(serializer, value))
            commit()
        }
    }

    suspend fun insert(value: T) {
        db.borrow {
            execute(INSERT, *sqlSerialization.nameParams(serializer, value))
            commit()
        }
    }

    suspend fun deleteById(id: ID) {
        db.borrow {
            execute(DELETE_BY_ID, "id" to id)
            commit()
        }
    }

    protected fun buildSelect(where: String): SelectQueryWithMapper<T> =
        SelectQuery(
            SQLSerialization.selectQuery(serializer) + " where $where"
        ).mapper(sqlSerialization.mapper(serializer))

    private val SELECT_BY_ID =
        buildSelect("$idFieldName = :$idFieldName\"")

    private val INSERT = UpdateQuery(
        SQLSerialization.insertQuery(serializer)
    )

    private val UPDATE_BY_ID = UpdateQuery(
        SQLSerialization.updateQuery(serializer, excludes = setOf(idFieldName)) + " where $idFieldName=:id"
    )

    private val DELETE_BY_ID = UpdateQuery(
        """
           delete from ${serializer.tableName} where $idFieldName=:id 
        """
    )
}
