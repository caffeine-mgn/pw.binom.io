package pw.binom.wasm.visitors

import pw.binom.wasm.FunctionId
import pw.binom.wasm.GlobalId
import pw.binom.wasm.MemoryId
import pw.binom.wasm.TableId

/**
 * https://webassembly.github.io/gc/core/binary/modules.html#binary-exportsec
 */
interface ExportSectionVisitor {
  companion object {
    val SKIP = object : ExportSectionVisitor {}
  }

  fun start() {}
  fun end() {}
  fun func(name: String, value: FunctionId) {}
  fun table(name: String, value: TableId) {}
  fun memory(name: String, value: MemoryId) {}
  fun global(name: String, value: GlobalId) {}
}
