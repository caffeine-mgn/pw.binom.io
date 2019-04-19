package pw.binom.io

open class ConnectException(val host:String,val port:Int) : IOException(message = "$host:$port")