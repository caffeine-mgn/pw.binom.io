package pw.binom.wasm.runner

import pw.binom.wasm.MemoryId

operator fun List<MemorySpace>.get(index: MemoryId) =
  this[index.raw.toInt()]
