package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.linux.inet_ntop
import platform.posix.*
import pw.binom.toByteArray
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class, ExperimentalStdlibApi::class)
class AddressTest {

  val addrinfo.toSockedAddressIn4
    get() = ai_addr?.reinterpret<sockaddr_in>()

  val addrinfo.toSockedAddressIn6
    get() = ai_addr?.reinterpret<sockaddr_in6>()

  val addrinfo.host
    get() = toSockedAddressIn4?.pointed?.host

  val sockaddr_in.bytes: ByteArray
    get() = when (sin_family.toInt()) {
      AF_INET -> ntohl(sin_addr.s_addr).toInt().toByteArray()
      AF_INET6 -> {
        val byteArray = reinterpret<sockaddr_in6>().sin6_addr.ptr.reinterpret<ByteVar>()
        ByteArray(16) { byteArray[it] }
      }

      else -> TODO()
    }

  val sockaddr_in6.bytes
    get() = reinterpret<sockaddr_in>().bytes

  val sockaddr_in.host
    get() = memScoped {
      val s = allocArray<ByteVar>(50)
      inet_ntop(
        sin_family.convert(),
        sin_addr.ptr,
        s,
        50.convert()
      )
      s.toKString()
    }

  val sockaddr_in6.host
    get() = reinterpret<sockaddr_in>().host
  val sockaddr_in6.host2
    get() = memScoped {
      val s = allocArray<ByteVar>(50)
      inet_ntop(
        AF_INET6,
        sin6_addr.ptr,
        s,
        50.convert()
      )
      s.toKString()
    }

  @Test
  fun ipv4Test() {
    val addr = InetAddress.resolveOrNull("192.168.88.1")!!
    assertEquals(ProtocolFamily.AF_INET, addr.protocolFamily)
    addr.address.toUByteArray().also {
      assertEquals(4, it.size)
      assertEquals(0xc0u, it[0])
      assertEquals(0xa8u, it[1])
      assertEquals(0x58u, it[2])
      assertEquals(0x1u, it[3])
    }
  }

  @Test
  fun ipv4ToIpv6Test() {
    val addr = InetAddress.resolveOrNull("192.168.88.1")!!
    addr.convertIpV6()
    assertEquals(ProtocolFamily.AF_INET6, addr.protocolFamily)
    addr.address.toUByteArray().also {
      assertEquals(16, it.size)
      assertEquals(0x0u, it[0])
      assertEquals(0x0u, it[1])
      assertEquals(0x0u, it[2])
      assertEquals(0x0u, it[3])
      assertEquals(0x0u, it[4])
      assertEquals(0x0u, it[5])
      assertEquals(0x0u, it[6])
      assertEquals(0x0u, it[7])
      assertEquals(0x0u, it[8])
      assertEquals(0x0u, it[9])
      assertEquals(0xffu, it[10])
      assertEquals(0xffu, it[11])
      assertEquals(0xc0u, it[12])
      assertEquals(0xa8u, it[13])
      assertEquals(0x58u, it[14])
      assertEquals(0x1u, it[15])
    }
  }

  @Test
  fun withPortTest() {
    val localhostIpv6 = InetAddress.resolveOrNull("::1")!!
    println("ipv6 localhost: ${localhostIpv6.host}")
    println("ipv6 localhost: ${localhostIpv6.address.toHexString()}")
    val withPort = localhostIpv6.withPort(23)

    println("with port: ${withPort.address.toHexString()}")
    println("with port: ${withPort.host}")
  }

  @Ignore
  @Test
  fun ipv6Test() {
    val addr = InetAddress.resolveOrNull("0000:0000:2a00:1450:4010:0c0d:0000:0000")!!
    assertEquals(ProtocolFamily.AF_INET6, addr.protocolFamily)
    addr.address.toUByteArray().also {
      assertEquals(16, it.size)
      assertEquals(0x0u, it[0])
      assertEquals(0x0u, it[1])
      assertEquals(0x0u, it[2])
      assertEquals(0x0u, it[3])
      assertEquals(0x2au, it[4])
      assertEquals(0x0u, it[5])
      assertEquals(0x14u, it[6])
      assertEquals(0x50u, it[7])
      assertEquals(0x40u, it[8])
      assertEquals(0x10u, it[9])
      assertEquals(0xcu, it[10])
      assertEquals(0xdu, it[11])
      assertEquals(0x0u, it[12])
      assertEquals(0x0u, it[13])
      assertEquals(0x0u, it[14])
      assertEquals(0x0u, it[15])
    }
  }

  @Test
  fun addressTest() {
    memScoped {
      val hints = alloc<addrinfo>()
      val result = allocPointerTo<addrinfo>()
      memset(hints.ptr, 0, sizeOf<addrinfo>().convert())
      hints.ai_family = AF_INET
      hints.ai_socktype = SOCK_STREAM
      hints.ai_flags = hints.ai_flags or AI_CANONNAME
      if (getaddrinfo("192.168.88.1", null, hints.ptr, result.ptr) != 0) {
        TODO("getaddrinfo")
      }
      println("result.pointed!!.ai_addrlen=${result.pointed!!.ai_addrlen}")
//      val bytes = (0 until (result.pointed!!.ai_addrlen.toInt())).map { index ->
//        result.pointed!!.ai_addr!!.pointed!!.sa_data.get(index)
//        result.pointed!!.ai_addr!!.reinterpret<sockaddr_in>().pointed.sin_addr.s_addr.toInt().toByteArray()
//      }
      println("host: ${result.pointed?.toSockedAddressIn4?.pointed?.host}")
      println("host bytes: ${result.pointed?.toSockedAddressIn4?.pointed?.bytes?.map { it.toUByte().toString(16) }}")
      val r4 = InetAddress.resolveOrNull("192.168.88.1")!!
      val r6 = InetAddress.resolveOrNull("0000:0000:2a00:1450:4010:0c0d:0000:0000")!!
      println("#1 r4.host=${r4.host}")
      println("#1 r4.protocolFamily=${r4.protocolFamily}")
      println("#1 r4 --->${r4.address.map { it.toUByte().toString(16) }.joinToString(" ")}")
      r4.convertIpV6()
      println("#2 r4.host=${r4.host}")
      println("#2 r4.protocolFamily=${r4.protocolFamily}")
      println("#2 r4 --->${r4.address.map { it.toUByte().toString(16) }.joinToString(" ")}")
      r6.native.use { ptr ->
//        NNetworkAddress_set_host(ptr, "128.0.0.1")
//        memcpy(ptr, result.pointed?.toSockedAddressIn4?.pointed?.sin_addr?.ptr!!, 16.convert())
      }
      println("r6 --->${r6.address.map { it.toUByte().toString(16) }.joinToString(" ")}")
      println("r6.host=${r6.host}")
      println("r6 ${r6.protocolFamily}")


//      val bytes = result.pointed!!.ai_addr!!.reinterpret<sockaddr_in>().pointed.sin_addr.s_addr.toInt().toByteArray()
//      println("result.pointed!!.ai_family=${result.pointed!!.ai_family == AF_INET}")
//      val addr = InetNetworkAddress.create("127.0.0.1") as NativeMutableInetNetworkAddress
//      println("addr.host=${addr.host}")
//      addr.convertIpV6()
//      println("addr.protocolFamily->${addr.protocolFamily}")
//      addr.native.use { ptr ->
//        ptr.pointed.data
//      }
//      println("addr.host=${addr.host}")
//      println("ADDRESS->${bytes.map { it.toUByte().toString() }.joinToString(".")}")

    }
  }
}
