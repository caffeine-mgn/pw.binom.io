package pw.binom.wasm.visitors

interface StorageVisitor {
  interface PackVisitor {
    companion object {
      val SKIP = object : PackVisitor {}
    }

    fun i8() {}
    fun i16() {}
    fun f16() {}
  }

  companion object {
    val SKIP = object : StorageVisitor {}
  }

  fun pack(): PackVisitor = PackVisitor.SKIP
  fun valType(): ValueVisitor = ValueVisitor.SKIP
}
