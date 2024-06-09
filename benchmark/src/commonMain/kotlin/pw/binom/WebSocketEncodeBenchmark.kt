package pw.binom

import kotlinx.benchmark.*
import pw.binom.io.ByteBuffer
import pw.binom.io.http.websocket.Message
import pw.binom.io.use

@State(Scope.Benchmark)
@Measurement(iterations = 3, time = 3, timeUnit = BenchmarkTimeUnit.SECONDS)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
class WebSocketEncodeBenchmark {

  @Param("1024", "8192", "1048576")
  var initSize: Int = 0

  @Benchmark
  fun encode() {
    ByteBuffer(initSize).use { buf ->
      Message.encode(mask = 123, data = buf)
    }
  }
}
