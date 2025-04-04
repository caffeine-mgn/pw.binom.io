package pw.binom.wasm.runner

import pw.binom.*
import pw.binom.io.file.File
import pw.binom.io.file.openRead
import pw.binom.io.use
import pw.binom.wasm.FunctionId
import pw.binom.wasm.Primitive
import pw.binom.wasm.StreamReader
import pw.binom.wasm.node.ValueType
import pw.binom.wasm.node.WasmModule
import pw.binom.wasm.readers.WasmReader
import pw.binom.wasm.text.writers.TextExpressionsVisitor
import kotlin.test.Test

/**
/**
 * File descriptor attributes.
*/
typedef struct __wasi_fdstat_t {
File type.
__wasi_filetype_t fs_filetype;

File descriptor flags.
__wasi_fdflags_t fs_flags;

Rights that apply to this file descriptor.
__wasi_rights_t fs_rights_base;

Maximum set of rights that may be installed on new file descriptors that
are created through this file descriptor, e.g., through `path_open`.
__wasi_rights_t fs_rights_inheriting;

} __wasi_fdstat_t;
 */

/**
 * [Список кодов](https://github.com/ericsink/wasm2cil/blob/09ea7bcdddb5e930ff425423f97618695e102508/core_defines/core.h#L54)
 */
class RunTest {
  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun run() {
    val module = WasmModule()
//    val filePath = "/home/subochev/tmp/wasm-test/c/dot.wasm"
//    val filePath = "/home/subochev/tmp/wasm-test/c/dot-wasi.wasm"
//    val filePath = "/home/subochev/tmp/wasm-test/c/binary-trees.wasm"
//    val filePath = "/home/subochev/tmp/wasm-test/build/compileSync/wasmWasi/main/developmentExecutable/kotlin/www-wasm-wasi.wasm"
    val filePath =
      "/home/subochev/tmp/wasm-test/build/compileSync/wasmWasi/main/productionExecutable/optimized/www-wasm-wasi.wasm"
//    val filePath =
//      "/home/subochev/tmp/wasm-test/build/compileSync/wasmWasi/main/productionExecutable/kotlin/www-wasm-wasi.wasm"
    StreamReader(File(filePath).openRead()).use {
      WasmReader.read(it, module)
    }
//    module.codeSection[module.functionSection[8].value.toInt()].code.accept(TextExpressionsVisitor(Console.std))
//    module.codeSection[6].code.accept(TextExpressionsVisitor(Console.std))
//    module.codeSection.forEachIndexed { index, it ->
//      println("------$index------")
//      it.code.accept(TextExpressionsVisitor(Console.std))
//      println("------$index------")
//    }
    val wasiModule = WasiModule(listOf("project.wasm", "9"))
    val pipelineModule = BinomPipeLineModule("Hello world!".encodeToByteArray())
    val resolver = object : ImportResolver {
      override fun func(module: String, field: String, type: RType.Function): ((ExecuteContext) -> Unit)? =
        when (module) {
          "binom" -> when (field) {
            "print" -> { e ->
              val address = e.args[0] as Value.Primitive.I32
              val size = e.args[1] as Value.Primitive.I32
              val bytesForPrint = e.runner.memory[0].getBytes(offset = address.value.toUInt(), len = size.value)
              println("---------->")
              println(bytesForPrint.toHexString())
              print(bytesForPrint.decodeToString())
            }

            "printCode" -> { e ->
              val address = e.args[0] as Value.Primitive.I32
              val size = e.args[1] as Value.Primitive.I32
              val bytesForPrint = e.runner.memory[0].getBytes(offset = address.value.toUInt(), len = size.value)
              val intArray = IntArray(bytesForPrint.size / 4) { index ->
                Int.fromBytes(bytesForPrint, index * 4)
              }
              val str = intArray.map {
                it.toChar()
              }.toTypedArray().contentToString()
              println("---------->")
              println(intArray.toList())
              println(str)
            }

            "int" -> { e ->
              val value = (e.args[0] as Value.Primitive.I32).value
              print(value.toChar())
//              println(
//                "INT: ${value} 0x${value.toString(16)} 0b${value.toString(2)} ${
//                  value.toByteArray().map { it.toUByte() }.joinToString("-")
//                }"
//              )
              Unit
            }

            else -> null
          }

          else -> null
        }
    }
    val runner = Runner(module, wasiModule + resolver + CLangEnv() + pipelineModule)
    val startFuncId = runner.findFunction("fff")?.id ?: runner.findFunction("_initialize")?.id
    ?: TODO("Can't find start function")
//    val startFuncName = "fff"
    val startFuncName = "_initialize"


    val result =
      runner.runFunc(startFuncId, args = listOf())

    println("result: $result")
    println("Exist code: ${wasiModule.exitCode}")
//    println("start: ${module.startFunctionId}")
  }
}

