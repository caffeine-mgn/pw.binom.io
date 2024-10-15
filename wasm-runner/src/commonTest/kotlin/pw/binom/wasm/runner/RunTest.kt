package pw.binom.wasm.runner

import pw.binom.Console
import pw.binom.Environment
import pw.binom.System
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
  @Test
  fun run() {
    val module = WasmModule()
//    val filePath = "/home/subochev/tmp/wasm-test/c/dot.wasm"
//    val filePath = "/home/subochev/tmp/wasm-test/c/dot-wasi.wasm"
    val filePath = "/home/subochev/tmp/wasm-test/c/binary-trees.wasm"
//    val filePath = "/home/subochev/tmp/wasm-test/build/compileSync/wasmWasi/main/productionExecutable/kotlin/www-wasm-wasi.wasm"
    StreamReader(File(filePath).openRead()).use {
      WasmReader.read(it, module)
    }
    val wasiModule = WasiModule(listOf("project.wasm", "9"))
    val resolver = object : ImportResolver {
      override fun func(module: String, field: String): ((ExecuteContext) -> Unit)? =
        when (module) {
          "binom" -> when (field) {
            "print" -> { e ->
              val address = e.args[0] as Int
              val size = e.args[1] as Int
              val bytesForPrint = e.runner.memory[0].getBytes(offset = address.toUInt(), len = size)
              print(bytesForPrint.decodeToString())
            }

            else -> TODO("Unknown $field for module $module")
          }

          else -> TODO("Unknown $module")
        }
    }
    val runner = Runner(module, wasiModule + resolver + CLangEnv())
    val startFuncName = "_start"
    val result = runner.runFunc(startFuncName, args = listOf())
    println("result: $result")
    println("Exist code: ${wasiModule.exitCode}")
//    println("start: ${module.startFunctionId}")
  }
}

