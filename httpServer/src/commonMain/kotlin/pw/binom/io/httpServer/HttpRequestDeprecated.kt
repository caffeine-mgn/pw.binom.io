package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.io.UTF8
import pw.binom.network.CrossThreadKeyHolder
import pw.binom.network.TcpConnection

//@Deprecated(message = "Will be removed")
//interface HttpRequestDeprecated {
//    val method: String
//    val uri: String
//    val contextUri: String
//    val input: AsyncInput
//    val rawInput: AsyncInput
//    val rawOutput: AsyncOutput
//    val rawConnection: TcpConnection
//    val headers: Map<String, List<String>>
//}