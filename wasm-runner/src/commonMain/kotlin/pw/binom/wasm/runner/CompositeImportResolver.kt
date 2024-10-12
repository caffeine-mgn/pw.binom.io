package pw.binom.wasm.runner

import pw.binom.wasm.node.ValueType

class CompositeImportResolver(val list: Sequence<ImportResolver>) : ImportResolver {
  override fun global(module: String, field: String, type: ValueType, mutable: Boolean): GlobalVar? =
    list.mapNotNull { it.global(module = module, field = field, type = type, mutable = mutable) }.firstOrNull()

  override fun memory(module: String, field: String, inital: UInt, max: UInt?): MemorySpace? =
    list.mapNotNull { it.memory(module = module, field = field, inital = inital, max = max) }.firstOrNull()

  override fun func(module: String, field: String): ((ExecuteContext) -> Unit)? =
    list.mapNotNull { it.func(module = module, field = field) }.firstOrNull()
}

operator fun ImportResolver.plus(other: ImportResolver): ImportResolver =
  when {
    this is CompositeImportResolver && other is CompositeImportResolver -> CompositeImportResolver(list + other.list)
    other is CompositeImportResolver -> CompositeImportResolver(sequenceOf(this) + other.list)
    this is CompositeImportResolver -> CompositeImportResolver(list + sequenceOf(other))
    else -> CompositeImportResolver(sequenceOf(this, other))
  }

