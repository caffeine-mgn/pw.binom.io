package pw.binom.wasm.runner

object MemoryHistory {
  data class DDD(val from: UInt, val to: UInt, val description: String)

  val arr = ArrayList<DDD>()
  fun push(from: UInt, to: UInt, description: String) {
    arr += DDD(from, to, description)
  }

  operator fun get(index: UInt) = arr.filter {
    it.from <= index && it.to >= index
  }.map { it.description }
}
