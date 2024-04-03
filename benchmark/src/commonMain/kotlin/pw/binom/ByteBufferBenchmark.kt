package pw.binom

import kotlinx.benchmark.*
import pw.binom.io.ByteBuffer
import pw.binom.io.use
import kotlin.math.roundToInt

@State(Scope.Benchmark)
@Measurement(iterations = 3, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
class ByteBufferBenchmark {

  @Benchmark
  fun newBufferPositionTest() {
    ByteBuffer(1024 * 1024).use { original ->
      val capacity = original.capacity
      original.clear()
      repeat(capacity) { index ->
        original.position = index
      }
    }
  }

  @Benchmark
  fun wrappedBufferPositionTest() {
    ByteBuffer(ByteArray(1024 * 1024)).use { original ->
      val capacity = original.capacity
      original.clear()
      repeat(capacity) { index ->
        original.position = index
      }
    }
  }

  @Benchmark
  fun newBufferWriteByteTest() {
    val long = ByteArray(8)
    ByteBuffer(ByteArray(1024 * long.size * 100)).use { original ->
      val capacity = original.capacity
      original.clear()
      repeat(capacity) { _ ->
        original.write(long)
      }
    }
  }

  @Benchmark
  fun newBufferReadIntoByteTest() {
    val long = ByteArray(8)
    ByteBuffer(ByteArray(1024 * long.size * 100)).use { original ->
      val capacity = original.capacity
      original.clear()
      repeat(capacity) { _ ->
        original.readInto(long)
      }
    }
  }
}
