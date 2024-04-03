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

  @Param("1024", "8192", "1048576")
  var initSize: Int = 0

  @Param("10", "30", "50", "70", "90", "120", "130")
  var copyPercent: Int = 0

  @Benchmark
  fun realloc() {
    ByteBuffer(initSize).use { original ->
      original.realloc((initSize * copyPercent.toFloat()).roundToInt()).close()
    }
  }
}
