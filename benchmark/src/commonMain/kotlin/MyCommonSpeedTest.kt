import kotlinx.benchmark.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

//@State(Scope.Benchmark)
//@Measurement(iterations = 3, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
//@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
//@BenchmarkMode(Mode.AverageTime)
//class MyCommonSpeedTest {
//  @Benchmark
//  fun exception(): Double {
//    val now = TimeSource.Monotonic.markNow()
//    while (now.elapsedNow() < 0.5.seconds) {
//      // Do nothing
//    }
//    return 0.0
//  }
//}
