package pw.binom.wasm.dom

import pw.binom.wasm.FunctionId
import pw.binom.wasm.GlobalId
import pw.binom.wasm.MemoryId
import pw.binom.wasm.TableId
import pw.binom.wasm.visitors.ExportSectionVisitor

sealed interface Export {
  val name: String
  fun accept(visitor: ExportSectionVisitor)

  class Function(override val name: String, val id: FunctionId) : Export {
    override fun accept(visitor: ExportSectionVisitor) {
      visitor.func(name = name, value = id)
    }
  }

  class Memory(override val name: String, val id: MemoryId) : Export {
    override fun accept(visitor: ExportSectionVisitor) {
      visitor.memory(name = name, value = id)
    }
  }

  class Table(override val name: String, val id: TableId) : Export {
    override fun accept(visitor: ExportSectionVisitor) {
      visitor.table(name = name, value = id)
    }
  }

  class Global(override val name: String, val id: GlobalId) : Export {
    override fun accept(visitor: ExportSectionVisitor) {
      visitor.global(name = name, value = id)
    }
  }
}
