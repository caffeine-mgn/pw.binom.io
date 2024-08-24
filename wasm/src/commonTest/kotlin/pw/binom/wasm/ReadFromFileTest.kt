package pw.binom.wasm

import pw.binom.io.Input
import pw.binom.io.file.File
import pw.binom.io.file.openRead
import pw.binom.io.use
import pw.binom.wasm.readers.WasmReader
import pw.binom.wasm.visitors.*
import kotlin.test.Test

class ReadFromFileTest {
  @Test
  fun aaa() {
//    val path = "/home/subochev/tmp/wasm-test/c/dot.o"
//    val path = "/home/subochev/tmp/wasm-test/build/compileSync/wasmWasi/main/developmentExecutable/kotlin/www-wasm-wasi.wasm"
//    val path = "/home/subochev/tmp/wasm-test/build/compileSync/wasmWasi/main/productionExecutable/kotlin/www-wasm-wasi.wasm"
    val path =
      "/home/subochev/tmp/wasm-test/build/compileSync/wasmJs/main/developmentExecutable/kotlin/www-wasm-js.wasm"
//    val path = "/home/subochev/tmp/wasm-test/build/compileSync/wasmJs/main/productionExecutable/kotlin/www-wasm-js.wasm"
//    val path = "/home/subochev/tmp/wasm-test/build/compileSync/wasmJs/main/productionExecutable/optimized/www-wasm-js.wasm"
    File(path)
      .openRead()
      .use { buf ->
        WasmReader.read(StreamReader(buf), MyWasmVisitor())
      }
  }
}

class MyWasmVisitor : WasmVisitor {
  override fun start() {
  }

  override fun end() {
  }

  override fun importSection(): ImportSectionVisitor = MyImportSectionVisitor()
  override fun functionSection(): FunctionSectionVisitor = MyFunctionSectionVisitor()
  override fun codeVisitor(): CodeSectionVisitor = MyCodeSectionVisitor()
  override fun typeSection(): TypeSectionVisitor = MyTypeSectionVisitor()
  override fun customSection(): CustomSectionVisitor = MyCustomSectionVisitor()
  override fun startSection(function: FunctionId) {
  }
}

class MyCustomSectionVisitor : CustomSectionVisitor {
  override fun start(name: String) {
    println("Custom section name: $name")
  }

  override fun data(input: Input) {
    input.skipAll()
  }

  override fun end() {
  }
}

class MyFunctionSectionVisitor : FunctionSectionVisitor {
  override fun start() {
  }

  override fun end() {
  }

}

class MyTypeSectionVisitor : TypeSectionVisitor {
  private val args = ArrayList<ValueType>()
  private val results = ArrayList<ValueType>()
  private var index = -1
  override fun start() {
    args.clear()
    results.clear()
    index++
  }

  override fun argument(type: ValueType) {
    args += type
  }

  override fun result(type: ValueType) {
    results += type
  }

  override fun end() {
    args.clear()
    results.clear()
  }

}

class MyImportSectionVisitor : ImportSectionVisitor {
  override fun start() {
  }

  override fun end() {
  }

  override fun memory(module: String, field: String, initial: UInt, maximum: UInt) {
    println("Import $module->$field memory with range $initial->$maximum")
  }

  override fun memory(module: String, field: String, initial: UInt) {
    println("Import $module->$field memory with range $initial->unlimited")
  }
}

class MyCodeSectionVisitor() : CodeSectionVisitor {
  private var c = 0

}
