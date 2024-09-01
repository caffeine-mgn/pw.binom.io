package pw.binom.wasm.dom

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

  @JsName("codeSectionF")
  val codeSection = CodeSection()

  @JsName("globalSectionF")
  val globalSection = GlobalSection()

  @JsName("functionSectionF")
  val functionSection = FunctionSection()

  @JsName("customSectionF")
  val customSection = CustomSection()

  @JsName("dataSectionF")
  var dataSection = DataSection()

  @JsName("importSectionF")
  var importSection = ImportSection()

  @JsName("memorySectionF")
  var memorySection = MemorySection()

  @JsName("typeSectionF")
  var typeSection = TypeSection()

  override fun dataSection() = dataSection

  override fun dataCountSection(): DataCountSectionVisitor {
    return super.dataCountSection()
  }

  override fun exportSection() = exportSection

  override fun tagSection() = tagSection

  override fun globalSection() = globalSection

  override fun elementSection(): ElementSectionVisitor {
    return super.elementSection()
  }

  override fun memorySection() = memorySection

  override fun tableSection(): TableSectionVisitor {
    return super.tableSection()
  }

  override fun startSection(function: FunctionId) {
    startFunctionId = function
  }

  override fun customSection() = customSection

  override fun typeSection() = typeSection

  override fun codeSection() = codeSection

  override fun functionSection() = functionSection

  override fun importSection() = importSection

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
    globalSection.elements.clear()
    functionSection.elements.clear()
    customSection.elements.clear()
    importSection.elements.clear()
    memorySection.elements.clear()
    typeSection.types.clear()
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
    if (codeSection.functions.isNotEmpty()) {
      codeSection.accept(visitor.codeSection())
    }
    if (globalSection.elements.isNotEmpty()) {
      globalSection.accept(visitor.globalSection())
    }
    if (functionSection.elements.isNotEmpty()) {
      functionSection.accept(visitor.functionSection())
    }
    if (dataSection.elements.isNotEmpty()) {
      dataSection.accept(visitor.dataSection())
      visitor.dataCountSection().also {
        it.start()
        it.value(dataSection.elements.size.toUInt())
        it.end()
      }
    }
    if (importSection.elements.isNotEmpty()) {
      importSection.accept(visitor.importSection())
    }
    if (memorySection.elements.isNotEmpty()) {
      memorySection.accept(visitor.memorySection())
    }
    if (typeSection.types.isNotEmpty()) {
      typeSection.accept(visitor.typeSection())
    }
    visitor.end()
  }
}
