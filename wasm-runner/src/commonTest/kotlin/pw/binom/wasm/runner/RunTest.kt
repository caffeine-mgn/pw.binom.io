package pw.binom.wasm.runner

import pw.binom.io.file.File
import pw.binom.io.file.openRead
import pw.binom.io.use
import pw.binom.wasm.FunctionId
import pw.binom.wasm.StreamReader
import pw.binom.wasm.node.WasmModule
import pw.binom.wasm.node.inst.I32Const
import pw.binom.wasm.readers.WasmReader
import kotlin.test.Test

class RunTest {
  @Test
  fun run() {

    val module = WasmModule()
    StreamReader(File("/home/subochev/tmp/wasm-test/c/dot.wasm").openRead()).use {
      WasmReader.read(it, module)
    }
    module.codeSection.functions.forEachIndexed { index, codeFunction ->
      codeFunction.code.elements.forEach {
        if (it !is I32Const) {
          return@forEach
        }
        println("CONST ${it.value} -> $index")
      }
    }
    val runner = Runner(module)
    runner.setGlobal(module = "env", field = "__stack_pointer", value = 0)
    val result = runner.runFunc("fff", args = listOf())
    println("result: $result")
    println("start: ${module.startFunctionId}")
  }
}
