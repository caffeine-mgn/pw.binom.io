package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.common.internal_inet_addr
import platform.common.internal_setsockopt
import platform.posix.SOCKET_ERROR
import platform.posix.errno
import platform.posix.timespec
import platform.windows.*
import pw.binom.io.ByteBuffer
import pw.binom.io.IOException
import kotlin.time.Duration

@OptIn(ExperimentalForeignApi::class)
class MingwSocket(
  native: RawSocket,
  server: Boolean,
) : AbstractSocket(native = native, server = server) {
  override val id: String
    get() = native.toString()

  override var keyHash: Int = 0

  override fun connect(address: InetSocketAddress): ConnectStatus {
    val netaddress =
      if (address is CommonMutableInetNetworkSocketAddress) {
        address
      } else {
        CommonMutableInetNetworkSocketAddress(address)
      }
    return memScoped {
      netaddress.getAsIpV6 { netdata ->
        val con =
          platform.windows.connect(
            native.convert(),
            netdata.reinterpret(),
            sizeOf<sockaddr_in6>().convert(),
          )
        if (con != 0) {
          val error = GetLastError()
          if (error == platform.windows.WSAEWOULDBLOCK.toUInt()) {
            return@getAsIpV6 ConnectStatus.IN_PROGRESS
          }
          if (error == platform.windows.WSAEAFNOSUPPORT.toUInt()) {
            throw IOException("Can't connect to $address. Error: $error An address incompatible with the requested protocol was used.")
          }
          if (error == platform.windows.WSAETIMEDOUT.toUInt()) {
            throw IOException(
              "Can't connect to $address. Error: $error A connection attempt failed because the connected party did not properly respond after a period of time, or established connection failed because connected host has failed to respond.",
            )
          }
          throw IOException("Can't connect to $address: return $con, error $error")
        }
        ConnectStatus.OK
      }
    }
  }

  override fun send(data: ByteBuffer): Int {
    if (closed) {
      return -1
    }
    memScoped {
      val r: Int =
        data.ref(0) { dataPtr, remaining ->
          platform.posix.send(native.convert(), dataPtr, remaining.convert(), 0).convert()
        }
      if (r < 0) {
        val error = GetLastError()
        if (error == WSAEWOULDBLOCK.convert<DWORD>()) {
          return 0
        }

        if (error == WSAECONNABORTED.convert<DWORD>() || error == WSAENOTSOCK.convert<DWORD>() || error == WSAECONNRESET.convert<DWORD>()) {
          return -1
        }
        throw IOException("Error on send data to network. send: [$r], error: [${GetLastError()}]")
      }
      data.position += r
      return r
    }
  }

  override fun receive(data: ByteBuffer): Int {
    if (closed) {
      return -1
    }
    var rem: Int = -10
    val lim = data.limit
    val pos = data.position
    val r =
      data.ref(0) { dataPtr, remaining ->
        rem = remaining
        platform.windows.recv(native.convert(), dataPtr, remaining.convert(), 0).convert()
      }
    if (r == 0) {
      closed = true
      nativeClose()
      return -1
    }
    if (r < 0) {
      val error = GetLastError()
      if (error == platform.windows.WSAEWOULDBLOCK.convert<DWORD>()) {
        return 0
      }
      return -1
//            throw IOException("Error on reading data from network. read: [$r], error: [${GetLastError()}, $errno]")
    }
    if (r > 0) {
      try {
        data.position += r
      } catch (e: Throwable) {
        println("Error on ByteBuffer position update.")
        println("new: pos: ${data.position}, rem: ${data.remaining}, lim: ${data.limit}")
        println("old: pos: $pos, rem: $rem, lim: $lim")
        println("was read: $r")
        throw e
      }
    }
    return r
  }

  override fun bind(address: InetSocketAddress): BindStatus {
    val networkAddress =
      if (address is CommonMutableInetNetworkSocketAddress) {
        address
      } else {
        CommonMutableInetNetworkSocketAddress(address)
      }
    memScoped {
      val bindResult =
        networkAddress.getAsIpV6 { data ->
          platform.posix.bind(
            native.convert(),
            data.reinterpret(),
            sizeOf<sockaddr_in6>().convert(),
          )
        }
      if (bindResult < 0) {
        if (GetLastError().toInt() == platform.windows.WSAEADDRINUSE) { // 10048
          return BindStatus.ADDRESS_ALREADY_IN_USE
        }
        if (GetLastError().toInt() == platform.windows.WSAEACCES) { // 10013
          throw BindException("Can't bind $address: An attempt was made to access a socket in a way forbidden by its access permissions.")
        }

        if (GetLastError().toInt() == platform.windows.WSAEAFNOSUPPORT) { // 10047
          throw BindException("Can't bind to $address: Address family not supported by protocol family")
        }
        if (GetLastError().toInt() == platform.windows.WSAEFAULT) { // 10014
          throw BindException("Can't bind to $address: Bad address")
        }
        if (GetLastError().toInt() == WSAEINVAL) {
          return BindStatus.ALREADY_BINDED
        }

        throw IOException("Bind error. errno: [$errno], GetLastError: [${GetLastError()}]")
      }
      val listenResult = platform.windows.listen(native.convert(), 1000)
      if (listenResult < 0) {
        if (GetLastError().toInt() == platform.windows.WSAEOPNOTSUPP) { // 10045
          return BindStatus.OK // UDP not supported listen. Ignore
        }
        throw IOException("Listen error. errno: [$errno], GetLastError: [${GetLastError()}]")
      }
    }
    return BindStatus.OK
  }

  override fun receive(
    data: ByteBuffer,
    address: MutableInetSocketAddress?,
  ): Int {
    var rem: Int = -10
    val lim = data.limit
    val pos = data.position
    val gotBytes =
      if (address == null) {
        val rr =
          data.ref(0) { dataPtr, remaining ->
            rem = remaining
            platform.windows.recvfrom(
              native.convert(),
              dataPtr,
              remaining.convert(),
              0,
              null,
              null,
            )
          }
        if (rr == SOCKET_ERROR) {
          if (GetLastError().convert<UInt>() == platform.windows.WSAEWOULDBLOCK.convert<UInt>()) {
            return 0
          }
          if (GetLastError().convert<UInt>() == platform.windows.WSAECONNRESET.convert<UInt>()) { // 10054
            throw IOException("Connection reset by peer.")
          }

          throw IOException("Can't read data. Error: $errno  ${GetLastError()}")
        }
        rr
      } else {
        memScoped {
          val netaddress =
            if (address is CommonMutableInetNetworkSocketAddress) {
              address
            } else {
              CommonMutableInetNetworkSocketAddress(address)
            }
          SetLastError(0.convert())
          val len = allocArray<IntVar>(1)
          len[0] = 28

          val rr =
            data.ref(0) { dataPtr, remaining ->
              netaddress.addr { addressPtr ->
                platform.windows.recvfrom(
                  native.convert(),
                  dataPtr.getPointer(this),
                  remaining.convert(),
                  0,
                  addressPtr.reinterpret(),
                  len,
                )
              }
            }

          if (rr == SOCKET_ERROR) {
            if (GetLastError().convert<UInt>() == platform.windows.WSAEWOULDBLOCK.convert<UInt>()) {
              return 0
            }
            if (GetLastError().convert<UInt>() == platform.windows.WSAECONNRESET.convert<UInt>()) { // 10054
              throw IOException("Connection reset by peer.")
            }
            throw IOException("Can't read data. Error: $errno  ${GetLastError()}")
          }
          netaddress.size = len[0]
          if (address !== netaddress) {
            address.update(
              host = netaddress.host,
              port = netaddress.port,
            )
          }
          rr
        }
      }
    if (gotBytes > 0) {
      try {
        data.position += gotBytes
      } catch (e: Throwable) {
        println("Error on ByteBuffer position update.")
        println("new: pos: ${data.position}, rem: ${data.remaining}, lim: ${data.limit}")
        println("old: pos: $pos, rem: $rem, lim: $lim")
        println("was read: $gotBytes")
        throw e
      }
    }
    return gotBytes
  }

  override fun processAfterSendUdp(
    data: ByteBuffer,
    code: Int,
  ): Int {
    if (code == SOCKET_ERROR) {
      if (GetLastError().toInt() == platform.windows.WSAEFAULT) { // 10014
        throw IOException("The system detected an invalid pointer address in attempting to use a pointer argument in a call.")
      }
      if (GetLastError().toInt() == platform.windows.WSAEWOULDBLOCK) { // 10035
        return 0
      }
      throw IOException("Can't send data. Error: $errno  ${GetLastError()}")
    }

    data.position += code.toInt()
    return code
  }

  override fun connect(path: String): ConnectStatus {
    throw RuntimeException("Not supported")
  }

  override fun accept(address: ((String) -> Unit)?): TcpClientNetSocket? {
    throw RuntimeException("Not supported")
  }

  override fun bind(path: String): BindStatus {
    throw RuntimeException("Not supported")
  }

  override fun send(
    data: ByteBuffer,
    address: String,
  ): Int {
    throw RuntimeException("Not supported")
  }

  override fun receive(
    data: ByteBuffer,
    address: (String) -> Unit?,
  ): Int {
    throw RuntimeException("Not supported")
  }

  override fun setSoTimeout(duration: Duration) {
    val success = memScoped {
      val waitUntil = alloc<timespec>()
      waitUntil.set(duration)
      internal_setsockopt(
        native,
        platform.posix.SOL_SOCKET,
        platform.posix.SO_RCVTIMEO,
        waitUntil.ptr,
        sizeOf<timespec>().convert()
      ) >= 0
    }
    if (!success) {
      throw IOException("Can't set SoTimeout")
    }
  }

  override fun setTtl(value: Byte) {
    TODO("Not yet implemented")
  }

  override fun joinGroup(address: InetAddress) {
    joinGroup(
      address = address.host,
      netIf = htonl(platform.posix.INADDR_ANY)
    )
  }

  override fun joinGroup(address: InetSocketAddress, netIf: NetworkInterface) {
    joinGroup(
      address = address.host,
      netIf = internal_inet_addr(netIf.ip)
    )
  }

  override fun leaveGroup(address: InetAddress) {
    leaveGroup(
      address = address.host,
      netIf = htonl(platform.posix.INADDR_ANY)
    )
  }

  override fun leaveGroup(address: InetSocketAddress, netIf: NetworkInterface) {
    leaveGroup(
      address = address.host,
      netIf = internal_inet_addr(netIf.ip)
    )
  }

  private fun joinGroup(address: String, netIf: UInt) {
    memScoped {
      val mreq = alloc<ip_mreq>()
      mreq.imr_multiaddr.S_un.S_addr = internal_inet_addr(address)
      mreq.imr_interface.S_un.S_addr = netIf

      if (internal_setsockopt(
          native,
          platform.posix.IPPROTO_IP,
          IP_ADD_MEMBERSHIP,
          mreq.ptr,
          sizeOf<ip_mreq>().convert()
        ) == -1
      ) {
        throw IOException("Can't leave to group ${address}")
      }
    }
  }

  private fun leaveGroup(address: String, netIf: UInt) {
    memScoped {
      val mreq = alloc<ip_mreq>()
      mreq.imr_multiaddr.S_un.S_addr = internal_inet_addr(address)
      mreq.imr_interface.S_un.S_addr = netIf

      if (internal_setsockopt(
          native,
          platform.posix.IPPROTO_IP,
          IP_DROP_MEMBERSHIP,
          mreq.ptr,
          sizeOf<ip_mreq>().convert()
        ) == -1
      ) {
        throw IOException("Can't leave to group ${address}")
      }
    }
  }
}

@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
private fun timespec.set(diff: Duration) {
  tv_sec = diff.inWholeSeconds
  tv_nsec = (diff.inWholeNanoseconds - diff.inWholeSeconds * 1_000_000_000).convert()
}
