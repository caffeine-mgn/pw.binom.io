package pw.binom.wasm.runner

import pw.binom.io.file.File
import pw.binom.io.file.openRead
import pw.binom.io.use
import pw.binom.wasm.FunctionId
import pw.binom.wasm.Primitive
import pw.binom.wasm.StreamReader
import pw.binom.wasm.node.ValueType
import pw.binom.wasm.node.WasmModule
import pw.binom.wasm.node.inst.CallFunction
import pw.binom.wasm.node.inst.I32Const
import pw.binom.wasm.readers.WasmReader
import kotlin.test.Test

class RunTest {
  @Test
  fun run() {

    val module = WasmModule()
//    val filePath = "/home/subochev/tmp/wasm-test/c/dot.wasm"
    val filePath = "/home/subochev/tmp/wasm-test/c/dot-wasi.wasm"
    StreamReader(File(filePath).openRead()).use {
      WasmReader.read(it, module)
    }
    module.codeSection.forEachIndexed { index, codeFunction ->
      codeFunction.code.forEach {
        if (it !is I32Const) {
          return@forEach
        }
      }
    }
    val resolver = object : ImportResolver {
      override fun global(module: String, field: String, type: ValueType, mutable: Boolean): GlobalVar =
        when {
          module == "env" && field == "__stack_pointer" && type.number != null && type.number!!.type == Primitive.I32 ->
            GlobalVarMutable.S32(0)

          else -> TODO()
        }

      override fun memory(module: String, field: String, inital: UInt, max: UInt?): MemorySpace =
        when {
          module == "env" && (field == "__linear_memory" || field == "memory") -> MemorySpace(1024 * 1024)
          else -> TODO("Not yet implemented. module=$module, field=$field, inital: $inital, max: $max")
        }

      override fun func(module: String, field: String): (ExecuteContext) -> Unit =
        when (module) {
          "binom" -> when (field) {
            "print" -> { e ->
              val address = e.args[0] as Int
              val size = e.args[1] as Int
              val bytesForPrint = e.runner.memory[0].data.copyOfRange(address, address + size)
              print(bytesForPrint.decodeToString())
            }

            else -> TODO("Unknown $field for module $module")
          }

          "wasi_snapshot_preview1" -> when (field) {
            "args_get" -> { e -> }
            "args_sizes_get" -> { e -> }
            "proc_exit" -> { e -> }
            else -> TODO("Unknown $field for module $module")
          }

          else -> TODO("Unknown $module")
        }

    }
    val runner = Runner(module, resolver)
    val fff = runner.findFunction("fff")!!
    val realFFFIndex = module.codeSection.indexOfFirst {
      it.code.any { it is I32Const && it.value == 1000 }
    }

    val fffAfterConvert = fff.id.id - runner.importFunc.size.toUInt()

    val code = module.codeSection[fff.id.id.toInt()]

    val result = runner.runFunc("fff", args = listOf())
    println("result: $result")
//    println("start: ${module.startFunctionId}")
  }
}

fun WasmModule.findWhoCall(function: FunctionId) {
  codeSection
    .asSequence()
    .mapIndexed { index, function ->
      index to function
    }
    .filter {
      it.second.code.any { it is CallFunction && it.id.id == function.id }
    }
    .map { it.first }
    .toList()
}
