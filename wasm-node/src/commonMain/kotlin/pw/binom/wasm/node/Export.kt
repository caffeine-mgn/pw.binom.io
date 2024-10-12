package pw.binom.wasm.node

import pw.binom.wasm.FunctionId
import pw.binom.wasm.GlobalId
import pw.binom.wasm.MemoryId
import pw.binom.wasm.TableId
import pw.binom.wasm.visitors.ExportSectionVisitor

sealed class Export {
  abstract val name: String
  abstract fun accept(visitor: ExportSectionVisitor)

  data class Function(override val name: String, val id: FunctionId) : Export() {
    override fun accept(visitor: ExportSectionVisitor) {
      visitor.func(name = name, value = id)
    }
  }

  data class Memory(override val name: String, val id: MemoryId) : Export() {
    override fun accept(visitor: ExportSectionVisitor) {
      visitor.memory(name = name, value = id)
    }
  }

  data class Table(override val name: String, val id: TableId) : Export() {
    override fun accept(visitor: ExportSectionVisitor) {
      visitor.table(name = name, value = id)
    }
  }

  data class Global(override val name: String, val id: GlobalId) : Export() {
    override fun accept(visitor: ExportSectionVisitor) {
      visitor.global(name = name, value = id)
    }
  }
}
