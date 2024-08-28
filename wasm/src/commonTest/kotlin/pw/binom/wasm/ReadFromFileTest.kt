package pw.binom.wasm

import pw.binom.Console
import pw.binom.io.*
import pw.binom.io.file.File
import pw.binom.io.file.openRead
import pw.binom.io.file.readBinary
import pw.binom.wasm.readers.GlobalSectionReader
import pw.binom.wasm.readers.TypeSectionReader
import pw.binom.wasm.readers.WasmReader
import pw.binom.wasm.visitors.*
import pw.binom.wasm.writers.GlobalSectionWriter
import pw.binom.wasm.writers.TypeSectionWriter
import pw.binom.wasm.writers.WasmWriter
import pw.binom.wrap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ReadDetecter(val input: Input, val func: (Byte, Int) -> Unit) : Input {
  private var cursor = 0
  override fun read(dest: ByteBuffer): DataTransferSize {
    dest.forEach {
      func(it, cursor)
      cursor++
    }
    return input.read(dest)
  }

  override fun close() {
    input.close()
  }

}

class ReadFromFileTest {
  @Test
  fun aaa() {
//    val path = "/home/subochev/tmp/wasm-test/c/dot.o"
//    val path = "/home/subochev/tmp/wasm-test/build/compileSync/wasmWasi/main/developmentExecutable/kotlin/www-wasm-wasi.wasm"
    val path =
      "/home/subochev/tmp/wasm-test/build/compileSync/wasmWasi/main/productionExecutable/kotlin/www-wasm-wasi.wasm"
//    val path =
//      "/home/subochev/tmp/wasm-test/build/compileSync/wasmJs/main/developmentExecutable/kotlin/www-wasm-js.wasm"
//    val path = "/home/subochev/tmp/wasm-test/build/compileSync/wasmJs/main/productionExecutable/kotlin/www-wasm-js.wasm"
//    val path = "/home/subochev/tmp/wasm-test/build/compileSync/wasmJs/main/productionExecutable/optimized/www-wasm-js.wasm"

    val mm = InMemoryWasmOutput()
    val data = File(path).readBinary()
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

    val r = DiffOutput(data).asWasm
    try {
      WasmReader.read(StreamReader(ByteBuffer.wrap(data)), WasmWriter(r))
    } catch (e: Throwable) {
      println("0x15bf "+r.callback[0x15bf])
      throw e
    }

    //    WasmReader.read(StreamReader(ByteBuffer.wrap(data)), WasmWriter(mm))


    mm.locked {
      println("Out data: ${it.remaining}")
    }
    println("Original data: ${data.size}")
  }
}
