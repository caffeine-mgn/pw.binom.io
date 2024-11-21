package pw.binom.metric.prometheus

import pw.binom.metric.MetricType
import kotlin.test.Test

class MetricWriterTest {

  @Test
  fun test() {
    val sb = StringBuilder()
    val writer = MetricWriter(sb)

    writer.type(name = "a", type = MetricType.GAUGE)
    writer.help(name = "a", text = "123")
    writer.start(name = "a")
    writer.field("a", "1")
    writer.field("b", "2")
    writer.value("0")
    writer.end()

    writer.start(name = "b")
    writer.type(name = "b", type = MetricType.COUNTER)
    writer.help(name = "b", text = "23423")
    writer.field("c", "3")
    writer.field("d", "4")
    writer.value("1")
    writer.end()

    println(sb)
  }
}
