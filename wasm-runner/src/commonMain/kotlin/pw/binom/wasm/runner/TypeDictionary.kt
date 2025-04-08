package pw.binom.wasm.runner

import pw.binom.wasm.PackPrimitive
import pw.binom.wasm.Primitive
import pw.binom.wasm.TypeId
import pw.binom.wasm.node.*
import kotlin.coroutines.*

class TypeDictionary(val list: List<RecType.SubType>, val list2: List<RType>) {
  companion object {

    private class TypeResolver {
      val types: List<RType>
        get() = internalTypes

      private val waiters = HashMap<TypeId, MutableList<Continuation<RType>>>()
      private val internalTypes = ArrayList<RType>()

      fun finish() {
        if (waiters.isNotEmpty()) {
          TODO()
        }
      }

      suspend fun getType(typeId: TypeId): RType =
        if (typeId.value.toInt() >= internalTypes.size) {
          suspendCoroutine {
            waiters.getOrPut(typeId) { ArrayList() }.add(it)
          }
        } else {
          internalTypes[typeId.value.toInt()]
        }

      fun addType(type: RType) {
        val tt = TypeId(internalTypes.size.toUInt())
        val w = waiters.remove(tt) ?: emptyList()
        internalTypes += type
        w.forEach {
          it.resume(type)
        }
      }
    }

    fun convert(type: HeapType, nullable: Boolean): VType {
      return when {
        type.type != null -> {
          VType.Ref(
            id = type.type!!,
            nullable = nullable,
          )
        }

        type.abs != null -> {
          VType.RefAbsolute(
            type = type.abs!!,
            nullable = nullable,
          )
        }

        else -> TODO("$type")
      }
    }

    fun convert(type: StorageType): VType =
      when {
        type.valueVisitor != null -> convert(type = type.valueVisitor!!)
        type.packPrimitive != null -> when (type.packPrimitive!!) {
          PackPrimitive.I8 -> VType.Primitive(RType.Primitive.I8)
          PackPrimitive.I16 -> VType.Primitive(RType.Primitive.I16)
          PackPrimitive.F16 -> VType.Primitive(RType.Primitive.F16)
        }

        else -> TODO()
      }

    fun convert(type: ValueType): VType {
      return when {
        type.number != null -> {
          when (type.number!!.type) {
            Primitive.I32 -> VType.Primitive(RType.Primitive.I32)
            Primitive.I64 -> VType.Primitive(RType.Primitive.I64)
            Primitive.F32 -> VType.Primitive(RType.Primitive.F32)
            Primitive.F64 -> VType.Primitive(RType.Primitive.F64)
          }
        }

        type.ref != null -> {
          when {
            type.ref!!.heapRef != null -> {
              convert(
                type=type.ref!!.heapRef!!,
                nullable = type.ref!!.isNullable
              )
            }
            else -> TODO("$type")
          }
        }

        type.abs != null -> {
          VType.RefAbsolute(type.abs!!, nullable = false)
        }

        else -> TODO("$type")
      }
    }

    private suspend fun convert(type: RecType.SubType, typeResolver: TypeResolver, id: TypeId): RType {
      return when {
        type.single != null -> {
          val t = type.single!!.type
          when {
            t is RecType.FuncType -> RType.Function(
              args = t.args.map { convert(type = it) },
              results = t.results.map { convert(type = it) },
              shared = t.shared,
            )

            t is RecType.ArrayType -> {
              RType.Ref.Array(
                mutable = t.mutable,
                type = convert(type = t.type!!),
              )
            }

            t is RecType.StructType -> {
              RType.Ref.Object(
                id = id,
                fields = t.fields.map {
                  RType.Ref.Field(
                    mutable = it.mutable,
                    type = convert(type = it.type),
                  )
                },
                parents = emptyList(),
                final = false,
              )
            }

            else -> TODO("$type $t")
          }
        }

        type.struct != null -> {
          val parents = type.struct!!.parents
          val p = type.struct!!.type!!.type as RecType.StructType
          val fields = p.fields.map {
            RType.Ref.Field(
              mutable = it.mutable,
              type = convert(it.type),
            )
          }
          RType.Ref.Object(
            final = type.isFinalStruct,
            parents = parents.map { typeResolver.getType(it) as RType.Ref.Object },
            fields = fields,
            id = id,
          )
        }
//        type.struct!=null->{
//          RType.Ref.Object()
//        }
        else -> TODO("$type")
      }
    }

    private fun async(func: (suspend () -> Unit)) {
      var error: Throwable? = null
      func.startCoroutine(completion = object : Continuation<Unit> {
        override val context: CoroutineContext
          get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<Unit>) {
          error = result.exceptionOrNull()
        }
      })

      error?.let { throw it }
    }

    private fun readTypes(list: List<Type>, types: MutableList<RecType.SubType>, typeResolver: TypeResolver) {
      list.forEach {
        val nextId = TypeId(typeResolver.types.size.toUInt())
        when (it) {
          is RecType -> when {
            it.single != null -> {
              types += it.single!!
              async {
                typeResolver.addType(convert(type = it.single!!, typeResolver = typeResolver, id = nextId))
              }
            }

            it.recursive != null -> readTypes(list = it.recursive!!.types, types = types, typeResolver = typeResolver)
          }

          is RecType.SubType -> {
            types += it
            async {
              typeResolver.addType(convert(type = it, typeResolver = typeResolver, id = nextId))
            }
          }

          else -> TODO()
        }
      }
    }

    fun create(typeSection: List<RecType>): TypeDictionary {
      val typeResolver = TypeResolver()
      val result = ArrayList<RecType.SubType>()
      readTypes(typeSection, result, typeResolver)
      typeResolver.finish()
      return TypeDictionary(result, typeResolver.types)
    }
  }

  operator fun get(type: TypeId) = list[type.value.toInt()]
  fun getRType(type: TypeId) = list2[type.value.toInt()]

  fun isParent(type: RType.Ref.Object, parent: RType.Ref.Object): Boolean {
    if (type.id == parent.id) {
      return true
    }
    type.parents.forEach {
      if (isParent(it, parent)) {
        return true
      }
    }
    return false
  }
}
