package pw.binom.wasm.dom

import pw.binom.wasm.MemoryId

data class Data(
  var memoryId: MemoryId?,
  var expressions: Expressions?,
  var data: ByteArray,
)
