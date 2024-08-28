package pw.binom.wasm.writers

import pw.binom.wasm.FunctionId
import pw.binom.wasm.InMemoryWasmOutput
import pw.binom.wasm.Sections
import pw.binom.wasm.WasmOutput
import pw.binom.wasm.readers.WasmReader
import pw.binom.wasm.visitors.*

class WasmWriter(private val out: WasmOutput) : WasmVisitor {
  private var state = 0
  private var sectionData = InMemoryWasmOutput()
  override fun start() {
    check(state == 0)
    state++
    println("---------WRITE MAGIC---------")
    WasmReader.MAGIC.forEach { magicByte ->
      out.i8u(magicByte)
    }
  }

  override fun version(version: Int) {
    check(state == 1)
    state++
    println("---------WRITE VERSION---------")
    out.i32s(version)
  }

  private fun flushSection() {
    println("---------FLUSH SECTION $sectionCode, len: ${sectionData.size}---------")
    out.v32u(sectionData.size.toUInt())
    sectionData.moveTo(out)
  }

  private var sectionCode: UByte = 0u
  private fun startSection(code: UByte) {
    when (state) {
      2 -> state++
      3 -> flushSection()
      else -> throw IllegalStateException()
    }
    sectionCode = code
    println("---------START SECTION $code---------")
    out.i8u(code)
  }

  override fun elementSection(): ElementSectionVisitor {
    startSection(Sections.ELEMENT_SECTION.index.toUByte())
    return ElementSectionWriter(sectionData)
  }

  override fun importSection(): ImportSectionVisitor {
    startSection(Sections.IMPORT_SECTION.index.toUByte())
    return ImportSectionWriter(sectionData)
  }

  override fun functionSection(): FunctionSectionVisitor {
    startSection(Sections.FUNCTION_SECTION.index.toUByte())
    return FunctionSectionWriter(sectionData)
  }

  override fun codeVisitor(): CodeSectionVisitor {
    startSection(Sections.CODE_SECTION.index.toUByte())
    return CodeSectionWriter(sectionData)
  }

  override fun typeSection(): TypeSectionVisitor {
    startSection(Sections.TYPE_SECTION.index.toUByte())
    return TypeSectionWriter(sectionData)
  }

  override fun customSection(): CustomSectionVisitor {
    startSection(Sections.CUSTOM_SECTION.index.toUByte())
    return CustomSectionWriter(sectionData)
  }

  override fun startSection(function: FunctionId) {
    startSection(Sections.START_SECTION.index.toUByte())
    out.v32u(function.id)
    state = 2
  }

  override fun tableSection(): TableSectionVisitor {
    startSection(Sections.TABLE_SECTION.index.toUByte())
    return TableSectionWriter(sectionData)
  }

  override fun memorySection(): MemorySectionVisitor {
    startSection(Sections.MEMORY_SECTION.index.toUByte())
    return MemorySectionWriter(sectionData)
  }

  override fun globalSection(): GlobalSectionVisitor {
    println("---------1--------")
    startSection(Sections.GLOBAL_SECTION.index.toUByte())
    println("---------2--------")
    return GlobalSectionWriter(sectionData)
  }

  override fun exportSection(): ExportSectionVisitor {
    startSection(Sections.EXPORT_SECTION.index.toUByte())
    return ExportSectionWriter(sectionData)
  }

  override fun dataCountSection(): DataCountSectionVisitor {
    startSection(Sections.DATA_COUNT_SECTION.index.toUByte())
    return DataCountSectionWriter(sectionData)
  }

  override fun tagSection(): TagSectionVisitor {
    startSection(Sections.TAG_SECTION.index.toUByte())
    return TagSectionWriter(sectionData)
  }

  override fun dataSection(): DataSectionVisitor {
    startSection(Sections.DATA_SECTION.index.toUByte())
    return DataSectionWriter(sectionData)
  }

  override fun end() {
    check(state == 2 || state == 3)
    if (state == 3) {
      flushSection()
    }
    state = 0
  }
}
