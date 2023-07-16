package pw.binom

import kotlinx.cinterop.convert
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toLong
import kotlinx.cinterop.value
import platform.common.internval_get_socket_type
import platform.windows.GetLastError
import pw.binom.io.socket.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
  println("Hello world!1")
  val root = RootCompletionPort()
  val dest = RootCompletionPort.Dest()
  val socket = Socket.createTcpClientNetSocket()
  socket.blocking = false
  val ok = root.add(socket.native, completionKey = 10.convert())
  println("result: $ok")

  println("Error before connect: ${GetLastError()}")
  val connectResult = socket.connect(InetNetworkAddress.create(host = "google.com", port = 80))
  println("Error after connect: ${GetLastError()}")
  println("connectResult: $connectResult")

  println("Start thread with wait event")
  val e = TimeSource.Monotonic.markNow()
  while (true) {
    if (e.elapsedNow() > 20.seconds) {
      break
    }
    val waitResult = root.waitEvent(dest = dest, duration = 5.seconds)
    println("dest.dwTransferred: ${dest.dwTransferred.value}")
    println("dest.lpOverlapped: ${dest.lpOverlapped.value.toLong()}")
    println("MingwSocket.CONNECT_OVERLAPPED: ${MingwSocket.CONNECT_OVERLAPPED.ptr.toLong()}")
//    println("dest.lpOverlapped.Internal: ${dest.lpOverlapped.value?.pointed?.Internal}")
//    println("dest.lpOverlapped.Pointer: ${dest.lpOverlapped.value?.pointed?.Pointer}")
    println("dest.ulCompletionKey: ${dest.ulCompletionKey.value}")
    println("waitResult=$waitResult")
  }

//  Thread.sleep(10.seconds)
}

enum class SocketType {
  SERVER,
  CLIENT,
  INVALID,
}

fun RawSocket.getServerSocket() =
  when (val socketType = internval_get_socket_type(convert())) {
    0 -> SocketType.INVALID
    1 -> SocketType.SERVER
    2 -> SocketType.CLIENT
    else -> throw IllegalStateException("Invalid socket type #$socketType")
  }
