package pw.binom.metric.prometheus

import pw.binom.testing.shouldBeTrue
import pw.binom.testing.shouldEquals
import kotlin.test.Test

class SyncPrometheusReaderTest {
  @Test
  fun withoutFieldsTest() {
    val list = """
      # HELP my-description
      # TYPE my-type
      jdbc_connections_idle 10.0""".trimIndent().parse()
    list.size shouldEquals 1
    list[0].also {
      it.fields.isEmpty().shouldBeTrue()
      it.name shouldEquals "jdbc_connections_idle"
      it.help shouldEquals "my-description"
      it.type shouldEquals "my-type"
      it.value shouldEquals "10.0"
    }
  }

  @Test
  fun withFieldsTest() {
    val list = """
      # HELP my-description
      # TYPE my-type
      jdbc_connections_idle{name="dataSource",id="123123"} 10.0""".trimIndent().parse()
    println(list)
    list.size shouldEquals 1
    list[0].also {
      it.fields.size shouldEquals 2
      it.fields["name"] shouldEquals "dataSource"
      it.fields["id"] shouldEquals "123123"
      it.name shouldEquals "jdbc_connections_idle"
      it.help shouldEquals "my-description"
      it.type shouldEquals "my-type"
      it.value shouldEquals "10.0"
    }
  }

  fun String.parse(): List<PrometheusMetric> {
    val result = ArrayList<PrometheusMetric>()
    PrometheusReader.read(this, PrometheusMetric.readTo(result))
    return result
  }
}
