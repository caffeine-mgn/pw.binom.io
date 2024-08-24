package pw.binom.wasm.readers

import pw.binom.io.EOFException
import pw.binom.io.use
import pw.binom.wasm.FunctionId
import pw.binom.wasm.Sections
import pw.binom.wasm.StreamReader
import pw.binom.wasm.visitors.WasmVisitor

/**
 * https://webassembly.github.io/exception-handling/core/binary/modules.html
 */
object WasmReader {

  fun read(input: StreamReader, visitor: WasmVisitor) {
    visitor.start()
//      val ver = input.readByteArray(4)
//      ver.forEach {
//        println("MAGIC BYTE 0x${it.toUByte().toString(16)}")
//      }

    check(input.readInt2() == 0x6d736100L)
    val version = input.readInt2()
    visitor.version(version.toInt())
    check(version == 1L)

    while (true) {
      val sectionId = try {
        input.sByte().toInt() and 0xff
//          input.readVarUInt7().toInt()
      } catch (e: EOFException) {
        break
      }
      if (sectionId > Sections.maxIndex) throw RuntimeException("IoErr.InvalidSectionId($sectionId), cursor: ${input.cursor}")
      val section = Sections.byIndex(sectionId)
      println("Section $section 0x${(input.globalCursor - 1).toUInt().toString(16)}")
      val sectionLen = input.v32u()

      input.withLimit(sectionLen).use { sectionInput ->
        when (section) {
          Sections.CUSTOM_SECTION -> CustomSectionReader.read(input = sectionInput, visitor = visitor.customSection())
          Sections.TYPE_SECTION -> sectionInput.readVec {
            TypeSectionReader.read(
              input = sectionInput,
              visitor = visitor.typeSection()
            )
          }

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
          Sections.CODE_SECTION -> sectionInput.readVec {
            CodeSectionReader.read(
              input = sectionInput,
              visitor = visitor.codeVisitor(),
            )
          }

          Sections.DATA_SECTION -> DataSectionReader.read(input = sectionInput, visitor = visitor.dataSection())
          Sections.DATA_COUNT_SECTION -> DataCountSectionReader.read(
            input = sectionInput,
            visitor = visitor.dataCountSection()
          )

          Sections.TAG_SECTION -> TagSectionReader.read(input = sectionInput)
          else -> sectionInput.skipOther()
        }
        sectionInput.skipOther()
      }
    }
    visitor.end()
  }
}

fun Long.toIntExact() =
  if (this > Int.MAX_VALUE.toLong() || this < Int.MIN_VALUE.toLong())
    throw NumberFormatException("Expected within int range, got $this")
  else this.toInt()

fun Int.toUnsignedLong() = toLong() and 0xffffffffL
