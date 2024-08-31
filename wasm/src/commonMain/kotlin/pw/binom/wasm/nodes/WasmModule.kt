package pw.binom.wasm.nodes

import pw.binom.wasm.FunctionId
import pw.binom.wasm.visitors.*
import kotlin.js.JsName

class WasmModule : WasmVisitor {
  var version = 1
  var startFunctionId: FunctionId? = null

  @JsName("exportSectionElement")
  val exportSection = ExportSection()

  @JsName("tagSectionElement")
  val tagSection = TagSection()

  override fun dataSection(): DataSectionVisitor {
    return super.dataSection()
  }

  override fun dataCountSection(): DataCountSectionVisitor {
    return super.dataCountSection()
  }

  override fun exportSection(): ExportSectionVisitor = exportSection

  override fun tagSection(): TagSectionVisitor {
    return super.tagSection()
  }

  override fun globalSection(): GlobalSectionVisitor {
    return super.globalSection()
  }

  override fun elementSection(): ElementSectionVisitor {
    return super.elementSection()
  }

  override fun memorySection(): MemorySectionVisitor {
    return super.memorySection()
  }

  override fun tableSection(): TableSectionVisitor {
    return super.tableSection()
  }

  override fun startSection(function: FunctionId) {
    startFunctionId = function
  }

  override fun customSection(): CustomSectionVisitor {
    return super.customSection()
  }

  override fun typeSection(): TypeSectionVisitor {
    return super.typeSection()
  }

  override fun codeVisitor(): CodeSectionVisitor {
    return super.codeVisitor()
  }

  override fun functionSection(): FunctionSectionVisitor {
    return super.functionSection()
  }

  override fun importSection(): ImportSectionVisitor {
    return super.importSection()
  }

  override fun version(version: Int) {
    this.version = version
  }

  override fun end() {
    super.end()
  }

  override fun start() {
    startFunctionId = null
    this.version = 1
    exportSection.elements.clear()
    tagSection.elements.clear()
  }

  fun accept(visitor: WasmVisitor) {
    visitor.start()
    visitor.version(version)
    val startFunctionId = startFunctionId
    if (startFunctionId != null) {
      visitor.startSection(startFunctionId)
    }
    if (exportSection.isNotEmpty) {
      exportSection.accept(visitor.exportSection())
    }
    if (tagSection.elements.isNotEmpty()) {
      tagSection.accept(visitor.tagSection())
    }
    visitor.end()
  }
}
