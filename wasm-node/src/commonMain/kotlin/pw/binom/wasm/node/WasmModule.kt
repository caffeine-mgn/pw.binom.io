package pw.binom.wasm.node

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

  @JsName("tableSectionF")
  var tableSection = TableSection()

  @JsName("elementSectionF")
  var elementSection = ElementSection()

  override fun dataSection() = dataSection

  override fun dataCountSection(): DataCountSectionVisitor {
    return super.dataCountSection()
  }

  override fun exportSection() = exportSection

  override fun tagSection() = tagSection

  override fun globalSection() = globalSection

  override fun elementSection() = elementSection

  override fun memorySection() = memorySection

  override fun tableSection() = tableSection

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
    exportSection.clear()
    tagSection.clear()
    globalSection.clear()
    functionSection.clear()
    customSection.clear()
    importSection.clear()
    memorySection.clear()
    typeSection.clear()
    elementSection.clear()
  }

  fun accept(visitor: WasmVisitor) {
    visitor.start()
    visitor.version(version)
    val startFunctionId = startFunctionId
    if (startFunctionId != null) {
      visitor.startSection(startFunctionId)
    }
    if (exportSection.isNotEmpty()) {
      exportSection.accept(visitor.exportSection())
    }
    if (tagSection.isNotEmpty()) {
      tagSection.accept(visitor.tagSection())
    }
    if (codeSection.isNotEmpty()) {
      codeSection.accept(visitor.codeSection())
    }
    if (globalSection.isNotEmpty()) {
      globalSection.accept(visitor.globalSection())
    }
    if (functionSection.isNotEmpty()) {
      functionSection.accept(visitor.functionSection())
    }
    if (dataSection.isNotEmpty()) {
      dataSection.accept(visitor.dataSection())
      visitor.dataCountSection().also {
        it.start()
        it.value(dataSection.size.toUInt())
        it.end()
      }
    }
    if (importSection.isNotEmpty()) {
      importSection.accept(visitor.importSection())
    }
    if (memorySection.isNotEmpty()) {
      memorySection.accept(visitor.memorySection())
    }
    if (typeSection.isNotEmpty()) {
      typeSection.accept(visitor.typeSection())
    }
    if (tableSection.isNotEmpty()) {
      tableSection.accept(visitor.tableSection())
    }
    if (elementSection.isNotEmpty()) {
      elementSection.accept(visitor.elementSection())
    }
    if (customSection.isNotEmpty()) {
      customSection.accept(visitor)
    }
    visitor.end()
  }
}
