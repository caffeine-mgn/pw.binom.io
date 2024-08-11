package pw.binom.wasm

enum class Sections(val index: Int) {
  CUSTOM_SECTION(0),
  TYPE_SECTION(1),
  IMPORT_SECTION(2),
  FUNCTION_SECTION(3),
  TABLE_SECTION(4),
  MEMORY_SECTION(5),
  GLOBAL_SECTION(6),
  EXPORT_SECTION(7),
  START_SECTION(8),
  ELEMENT_SECTION(9),
  CODE_SECTION(10),
  DATA_SECTION(11),
  DATA_COUNT_SECTION(12),
  TAG_SECTION(13),
  ;

  companion object {
    val maxIndex = entries.maxOf { it.index }
    private val indexes = entries.sortedBy { it.index }.toTypedArray()
    fun byIndex(index: Int): Sections {
      require(index in 0..maxIndex)
      return indexes[index]
    }

  }
}
