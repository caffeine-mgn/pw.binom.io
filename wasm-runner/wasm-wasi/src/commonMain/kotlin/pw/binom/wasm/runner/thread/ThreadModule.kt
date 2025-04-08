package pw.binom.wasm.runner.thread

import pw.binom.thread.Thread
import pw.binom.wasm.runner.*

object ThreadModule : ImportResolver {
  override fun func(module: String, field: String, type: RType.Function): ((ExecuteContext) -> Unit)? {
    if (module != "wasi") {
      return super.func(module, field, type)
    }
    return when (field) {
      "thread-spawn" -> { e ->
        val arg = e.args[0] as Value.Primitive.I32
        val t = Thread {
          e.runner.runFunc(
            name = "wasi_thread_start",
            args = listOf(
              MutableVariable2Impl(value = Value.Primitive.I32(arg.value), VType.Primitive(RType.Primitive.I32)),
              MutableVariable2Impl(value = Value.Primitive.I32(it.id.toInt()), VType.Primitive(RType.Primitive.I32)),
            )
          )
        }
        e.pushResult(Value.Primitive.I32(t.id.toInt()))
      }

      "thread_id" -> { e ->
        e.pushResult(Value.Primitive.I32(Thread.currentThread.id.toInt()))
      }

      else -> super.func(module, field, type)
    }
  }
}
