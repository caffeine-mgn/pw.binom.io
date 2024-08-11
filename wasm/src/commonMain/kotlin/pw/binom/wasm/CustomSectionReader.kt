package pw.binom.wasm

object CustomSectionReader {
  fun read(input: InputReader, visitor: CustomSectionVisitor) {
    val sectionName = input.readString()
    visitor.start(sectionName)
    visitor.data(input)
    visitor.end()
    input.skipOther()
  }
}
