package pw.binom.wasm.node

import pw.binom.wasm.MemoryId

data class Data(
  var active: Boolean,
  var memoryId: MemoryId,
  var expressions: Expressions?,
  var data: ByteArray,
)
