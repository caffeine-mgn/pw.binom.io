package pw.binom.wasm.readers

import pw.binom.io.EOFException
import pw.binom.io.use
import pw.binom.wasm.FunctionId
import pw.binom.wasm.Sections
import pw.binom.wasm.StreamReader
import pw.binom.wasm.WasmInput
import pw.binom.wasm.visitors.WasmVisitor

/**
 * https://webassembly.github.io/exception-handling/core/binary/modules.html
 */
@OptIn(ExperimentalUnsignedTypes::class)
object WasmReader {

  val MAGIC = ubyteArrayOf(0x00u, 0x61u, 0x73u, 0x6du)

  fun read(input: WasmInput, visitor: WasmVisitor) {
    visitor.start()
    UByteArray(4) { input.i8u() }.contentEquals(MAGIC)
    val version = input.i32s()
    visitor.version(version.toInt())
    check(version == 1)

    while (true) {
      val sectionId = try {
        input.i8u().toInt() and 0xff
      } catch (e: EOFException) {
        break
      }
//      if (sectionId > Sections.maxIndex) throw RuntimeException("IoErr.InvalidSectionId($sectionId), cursor: ${input.cursor}")
      val section = Sections.byIndex(sectionId)
      val sectionLen = input.v32u()
      input as StreamReader
      println(
        "---------READING $section 0x${
          input.cursor.toUInt().toString(16)
        } (${input.cursor}) with len $sectionLen---------"
      )
      input.withLimit(sectionLen).use { sectionInput ->
        when (section) {
          Sections.CUSTOM_SECTION -> {
            CustomSectionReader.read(input = sectionInput, visitor = visitor.customSection())
          }

          Sections.TYPE_SECTION -> TypeSectionReader.read(
            input = sectionInput,
            visitor = visitor.typeSection()
          )

          Sections.IMPORT_SECTION -> ImportSectionReader.readImportSection(
            input = sectionInput,
            visitor = visitor.importSection(),
          )

          Sections.FUNCTION_SECTION -> FunctionSectionReader.read(
            input = sectionInput,
            visitor = visitor.functionSection()
          )

          Sections.TABLE_SECTION -> TableSectionReader.read(input = sectionInput, visitor = visitor.tableSection())
          Sections.MEMORY_SECTION -> MemorySectionReader.read(input = sectionInput, visitor = visitor.memorySection())
          Sections.GLOBAL_SECTION -> GlobalSectionReader.read(input = sectionInput, visitor = visitor.globalSection())
          Sections.EXPORT_SECTION -> ExportSectionReader.read(input = sectionInput, visitor = visitor.exportSection())

          Sections.START_SECTION -> visitor.startSection(function = FunctionId(sectionInput.v32u()))
          Sections.ELEMENT_SECTION -> ElementSectionReader.read(input = sectionInput)
          Sections.CODE_SECTION -> CodeSectionReader.read(
            input = sectionInput,
            visitor = visitor.codeVisitor(),
          )

          Sections.DATA_SECTION -> DataSectionReader.read(input = sectionInput, visitor = visitor.dataSection())
          Sections.DATA_COUNT_SECTION -> DataCountSectionReader.read(
            input = sectionInput,
            visitor = visitor.dataCountSection()
          )

          Sections.TAG_SECTION -> TagSectionReader.read(input = sectionInput, visitor.tagSection())
          else -> sectionInput.skipOther()
        }
        sectionInput.skipOther()
      }
    }
    visitor.end()
  }
}
