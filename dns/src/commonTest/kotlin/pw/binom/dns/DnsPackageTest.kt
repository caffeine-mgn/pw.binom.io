package pw.binom.dns

import pw.binom.dns.protocol.QueryPackage
import pw.binom.dns.protocol.ResourcePackage
import pw.binom.io.ByteBuffer
import pw.binom.io.use
import pw.binom.io.wrap
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DnsPackageTest {
  private val data =
    byteArrayOf(
      16,
      -82,
      1,
      32,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      1,
      6,
      103,
      111,
      111,
      103,
      108,
      101,
      3,
      99,
      111,
      109,
      0,
      0,
      1,
      0,
      1,
      0,
      0,
      41,
      4,
      -48,
      0,
      0,
      0,
      0,
      0,
      12,
      0,
      10,
      0,
      8,
      -84,
      63,
      81,
      -41,
      -95,
      -92,
      -16,
      46,
    )

  val dns =
    DnsPackage(
      header =
        Header(
          id = 4270,
          rd = true,
          tc = false,
          aa = false,
          opcode = Opcode.QUERY,
          qr = false,
          ra = false,
          z = false,
          ad = true,
          cd = false,
          rcode = Rcode.NOERROR,
        ),
      question = listOf(QueryPackage(name = "google.com", type = Type.A, clazz = Class.IN)),
      answer = emptyList(),
      authority = emptyList(),
      additional =
        listOf(
          ResourcePackage(
            name = "",
            type = Type.OPT,
            clazz = Class(1232u),
            ttl = 0u,
            rdata = byteArrayOf(0, 10, 0, 8, -84, 63, 81, -41, -95, -92, -16, 46),
          ),
        ),
    )

  @Test
  fun readTest() {
    val dns =
      data.wrap {
        DnsPackage.readWithoutSize(it)
      }
    println(dns)
    println(this.dns)
    assertEquals(this.dns, dns)
  }

  @Test
  fun writeTest() {
    val out =
      ByteBuffer(1000).use { buffer ->
        dns.createBuffer(buffer)
        buffer.flip()
        buffer.toByteArray()
      }
    println(out.toList())
    println(data.toList())
    assertContentEquals(data, out)
  }

  @Test
  fun aaa() {
    val bytes =
      ByteBuffer(100).use { buffer ->
        QueryPackage(
          name = "google.com",
          type = Type.A,
          clazz = Class.IN,
        ).write(buffer)
        buffer.flip()
        buffer.toByteArray()
      }

    println(bytes.toList())
  }

  @Test
  fun aa() {
    val originalPackage =
      DnsPackage(
        header =
          Header(
            id = 30565,
            rd = true,
            tc = false,
            aa = false,
            opcode = Opcode.QUERY,
            qr = false,
            ra = false,
            z = false,
            ad = true,
            cd = false,
            rcode = Rcode.NOERROR,
          ),
        question =
          listOf(
            QueryPackage(
              name = "google.com",
              type = Type.A,
              clazz = Class.IN,
            ),
          ),
        answer = emptyList(),
        authority = emptyList(),
        additional =
          listOf(
            ResourcePackage(
              name = "",
              type = Type.OPT,
              clazz = Class(1232u),
              ttl = 0u,
              rdata = byteArrayOf(0, 10, 0, 8, 55, 3, 114, 21, 3, -117, 28, -36),
            ),
          ),
      )
    ByteBuffer(1000).use { buffer ->
      buffer.clear()
      originalPackage.createBuffer(buffer)
      buffer.flip()
      DnsPackage.readWithoutSize(buffer)
    }
  }
}
