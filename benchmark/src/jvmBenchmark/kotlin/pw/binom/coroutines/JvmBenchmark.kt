package pw.binom.coroutines

//import kotlinx.benchmark.*

import org.openjdk.jmh.annotations.*;
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

@State(Scope.Benchmark)
//@Measurement(iterations = 3, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
//@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
class JvmBenchmark {
  @Benchmark
  fun exception(): Double {
    val now = TimeSource.Monotonic.markNow()
    while (now.elapsedNow() < 0.5.seconds) {
      // Do nothing
    }
    return 0.0
  }
}
