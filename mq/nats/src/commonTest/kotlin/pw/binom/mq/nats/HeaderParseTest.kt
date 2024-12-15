package pw.binom.mq.nats

import pw.binom.mq.nats.client.NatsHeaders
import pw.binom.mq.nats.client.ParsedHeadersMap
import pw.binom.testing.shouldEquals
import pw.binom.testing.shouldNotNull
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.test.Test

class HeaderParseTest {

  private val NORMAL = ("NATS/1.0\r\n" +
    "aaa: bbb\r\n" +
    "ccc: ddd\r\n\r\n").encodeToByteArray()

  private val ERROR_409 = "NATS/1.0 409 Consumer Deleted\r\n".encodeToByteArray()

  private val EMPTY = "NATS/1.0\r\n".encodeToByteArray()

  @Test
  fun errorTest() {
    val h = parseHeaders(ERROR_409)
    h.size shouldEquals 0
    h.code shouldEquals 409
    h.message shouldEquals "Consumer Deleted"
  }

  @Test
  fun emptyTest() {
    val h = parseHeaders(EMPTY)
    h.size shouldEquals 0
    h.code shouldEquals 0
    h.message shouldEquals ""
  }

  @Test
  fun normalTest() {
    val h = parseHeaders(NORMAL)
    h["aaa"].shouldNotNull().also {
      it.single() shouldEquals "bbb"
    }
    h["ccc"].shouldNotNull().also {
      it.single() shouldEquals "ddd"
    }
    h.code shouldEquals 0
    h.message shouldEquals ""
  }
}
