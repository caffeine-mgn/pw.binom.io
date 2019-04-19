package pw.binom.io

open class ConnectException(val host:String,val port:Int) : SocketException(message = "$host:$port")