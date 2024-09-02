package pw.binom.wasm

import pw.binom.io.*
import pw.binom.io.file.*
import pw.binom.wasm.dom.WasmModule
import pw.binom.wasm.readers.CodeSectionReader
import pw.binom.wasm.readers.ImportSectionReader
import pw.binom.wasm.readers.TypeSectionReader
import pw.binom.wasm.readers.WasmReader
import pw.binom.wasm.writers.CodeSectionWriter
import pw.binom.wasm.writers.ImportSectionWriter
import pw.binom.wasm.writers.TypeSectionWriter
import pw.binom.wasm.writers.WasmWriter
import pw.binom.wrap
import kotlin.test.Test

@OptIn(ExperimentalStdlibApi::class)
class ReadFromFileTest {
  @Test
  fun aaa() {
//    val path = "/home/subochev/tmp/wasm-test/c/dot.wasm"
//    val path = "/home/subochev/tmp/wasm-test/build/compileSync/wasmWasi/main/developmentExecutable/kotlin/www-wasm-wasi.wasm"
//    val path =
//      "/home/subochev/tmp/wasm-test/build/compileSync/wasmWasi/main/productionExecutable/kotlin/www-wasm-wasi.wasm"
//    val path =
//      "/home/subochev/tmp/wasm-test/build/compileSync/wasmJs/main/developmentExecutable/kotlin/www-wasm-js.wasm"
    val path = "/home/subochev/tmp/wasm-test/build/compileSync/wasmJs/main/productionExecutable/kotlin/www-wasm-js.wasm"
//    val path = "/home/subochev/tmp/wasm-test/build/compileSync/wasmJs/main/productionExecutable/optimized/www-wasm-js.wasm"

    val data = File(path).readBinary()


//    val importSection = data.copyOfRange(0x1f, 0x1f + 66)
//    DiffOutput(importSection).asWasm.use { v ->
//      ImportSectionReader.readImportSection(input = ByteArrayInput(importSection).asWasm(), ImportSectionWriter(v))
//    }

//    val typeSection = data.copyOfRange(0xe, 0xe + 11)
//    DiffOutput(typeSection).asWasm.use { v ->
//      try {
//        TypeSectionReader.read(input = ByteArrayInput(typeSection).asWasm(), TypeSectionWriter(v))
//      } catch (e:Throwable){
//        println("0x5 ${v.callback[0x5]}")
//        throw   e
//      }
//    }
//    return

//    println("------DATA------")
//    for (i in 0xefe - 5..0xefe + 5) {
//      println("0x${i.toString(16)} -> 0x${data[i].toUByte().toString(16).padStart(2, '0')}")
//    }
//    println("------DATA------")
    /*    val typeSection = data.copyOfRange(fromIndex = 11, toIndex = 11 + 3188)
        println("---->${typeSection.size}")

        val bbbbb = typeSection.copyOfRange(fromIndex = 1, toIndex = typeSection.size)



        val b = DiffOutput(typeSection).asWasm
        val r = ByteArrayInput(typeSection).asWasm()
        try {
          TypeSectionReader.read(
            input = r,
            visitor = TypeSectionWriter(mm),
          )
        } catch (e: Throwable) {
          Console.err.append(b.callback[0x4a])
          Console.err.append(b.callback[0x4b])
          Console.err.append(b.callback[0x4c])
          throw e
        }*/


    /*try {
      repeat(14) { index ->

        println("----------------$index-----------------")
        TypeSectionReader.DecodeTypeSection(
          input = r,
          visitor = TypeSectionWriter.RecTypeWriter(b),
        )
        println("----------------$index-----------------")
      }
    } catch (e: Throwable) {
      println("--->${b.callback[0x4b]}")
//      b.callback.forEachIndexed { index, s ->
//        println("0x${index.toString(16)} -> ${s.lines().joinToString("  -->  ")}")
//      }
      throw e
    }*/
//    val bb = StreamReader(ByteArrayInput(typeSection))
//    TypeSectionReader.read(
//      input = bb,
//      visitor = TypeSectionWriter(StreamWriter(DiffOutput(typeSection)))
//    )

    /*

            val codeSection = data.copyOfRange(0xa376,0xa376+134764)
            val v = DiffOutput(codeSection).asWasm
            println("-1 = ${codeSection[172]}")
            val ee = codeSection.copyOfRange(10505, 10505 + 3)
            ee.forEach {
              println("--->${it}")
            }
            try {
              CodeSectionReader.read(
                input = StreamReader(ByteBuffer.wrap(codeSection)),
                visitor = CodeSectionWriter(v)
              )
            } catch (e: Exception) {
              println("0x33c " + v.callback[0x33c])
              e.printStackTrace()
              throw e
            }
            return
    */


//    val globalSection = data.from(0xf00)
//
//    println("------DATA------")
//    for (i in 0x64 - 5..0x64 + 5) {
//      println("0x${i.toString(16)} -> 0x${globalSection[i].toUByte().toString(16).padStart(2, '0')}")
//    }
//    println("------DATA------")

//    val v = DiffOutput(globalSection).asWasm
//    try {
//      GlobalSectionReader.read(
//        input = StreamReader(ByteBuffer.wrap(globalSection)),
//        visitor = GlobalSectionWriter(v)
//      )
//    } catch (e: Throwable) {
//      println("0x64 " + v.callback[0x64])
//      println("0x65 " + v.callback[0x65])
//      println("0x66 " + v.callback[0x66])
//      println("0x67 " + v.callback[0x67])
//      println("0x68 " + v.callback[0x68])
//      println("0x69 " + v.callback[0x69])
//      throw e
//    }
//    return

//    val r = DiffOutput(data).asWasm
//    try {
//      println("HEX: ${data.copyOfRange(0x9,0x9+6).toHexString()}")
//      WasmReader.read(StreamReader(ByteBuffer.wrap(data)), WasmWriter(r))
//    } catch (e: Throwable) {
////      println("0x6c4b " + r.callback[0x6c4b])
//      throw e
//    }


    val outFile = File("/home/subochev/tmp/wasm-test/other/www-wasm-js.wasm")
    File(path).openRead().asWasm().use { input ->
      outFile.openWrite().asWasm.use { output ->
        WasmReader.read(input, WS(WasmWriter(output)))
      }
    }


    val m = WasmModule()
    WasmReader.read(StreamReader(ByteBuffer.wrap(data)), m)
    println(m)
//    val e = ByteArrayOutput().use {
//      m.accept(WasmWriter(it.asWasm))
//      it.toByteArray().size
//    }
//    println("Result size: $e")
//    //    WasmReader.read(StreamReader(ByteBuffer.wrap(data)), WasmWriter(mm))
//
//    println("Original data: ${data.size}")
  }
}
