package pw.binom.wasm.runner

import pw.binom.wasm.FunctionId

sealed interface Table {
  class FuncTable(val size: Int) : Table {
    var offset = 0
    private val list = arrayOfNulls<FunctionId>(size)

    operator fun get(index: Int): FunctionId? =
      list[index]

    operator fun set(index: Int, value: FunctionId?) {
      list[index] = value
    }
  }
}
