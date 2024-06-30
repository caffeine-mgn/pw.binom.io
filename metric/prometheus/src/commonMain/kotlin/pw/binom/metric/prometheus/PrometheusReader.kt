package pw.binom.metric.prometheus

import pw.binom.io.AsyncReader
import pw.binom.io.Reader
import pw.binom.io.asReader

object PrometheusReader {

  private inline fun commonRead(
    help: (String) -> Unit,
    type: (String) -> Unit,
    start: (String) -> Unit,
    value: (String) -> Unit,
    field: (String, String) -> Unit,
    end: () -> Unit,
    read: () -> String?,
  ) {
    do {
      val line = read() ?: break
      when {
        line.startsWith("# HELP") -> help(line.substring(7))
        line.startsWith("# TYPE") -> type(line.substring(7))
        else -> {
          val fieldStartIndex = line.indexOf('{')
          if (fieldStartIndex != -1) {
            val fieldEndIndex = line.lastIndexOf('}')
            val valueIndexIndex = line.indexOf(' ', fieldEndIndex + 1)
            if (valueIndexIndex == -1) {
              TODO()
            }
            start(line.substring(0, fieldStartIndex))
            var cursor = fieldStartIndex + 1
            while (true) {
              val separatorIndex = line.indexOf('=', cursor)
              if (separatorIndex == -1) {
                TODO()
              }
              val valueStartIndex = line.indexOf('"', separatorIndex)
              if (valueStartIndex == -1) {
                TODO()
              }
              val valueEndIndex = line.indexOf("\"", valueStartIndex + 1)
              if (valueEndIndex == -1) {
                TODO()
              }
              val name = line.substring(cursor, separatorIndex)
              val value1 = line.substring(valueStartIndex + 1, valueEndIndex)
              field(
                name,
                value1,
              )
              cursor = line.indexOf(',', valueEndIndex + 1)
              if (cursor == -1 || cursor >= fieldEndIndex) {
                break
              }
              cursor++
            }
            value(line.substring(valueIndexIndex + 1))
            end()
          } else {
            val valueIndexIndex = line.lastIndexOf(' ')
            if (valueIndexIndex == -1) {
              TODO()
            }
            start(line.substring(0, valueIndexIndex))
            value(line.substring(valueIndexIndex + 1))
            end()
          }
        }
      }
    } while (true)
  }

  fun read(reader: Reader, visitor: MetricVisitor) {
    commonRead(
      read = { reader.readln() },
      help = { visitor.help(it) },
      type = { visitor.type(it) },
      start = { visitor.start(it) },
      value = { visitor.value(it) },
      field = { name, value -> visitor.field(name = name, value = value) },
      end = { visitor.end() }
    )
  }

  fun read(text: String, visitor: MetricVisitor) {
    read(reader = text.asReader(), visitor = visitor)
  }

  suspend fun read(reader: AsyncReader, visitor: AsyncMetricVisitor) {
    commonRead(
      read = { reader.readln() },
      help = { visitor.help(it) },
      type = { visitor.type(it) },
      start = { visitor.start(it) },
      value = { visitor.value(it) },
      field = { name, value -> visitor.field(name = name, value = value) },
      end = { visitor.end() }
    )
  }
}
