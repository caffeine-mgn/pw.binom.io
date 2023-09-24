package pw.binom.io.httpServer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import pw.binom.io.httpServer.decoders.StringDecoder

//interface HttpSerializer {
//  fun getStringDecoder(
//    key: String,
//    value: String,
//    ser: KSerializer<out Any?>,
//    exchange: HttpServerExchange,
//  ): Decoder {
//    val decoder = StringDecoder()
//    decoder.value = value
//    return decoder
//  }
//
//  fun <T> decodeHeader(
//    key: String,
//    value: String,
//    ser: KSerializer<T>,
//    exchange: HttpServerExchange,
//  ): T {
//    val encoder = getStringDecoder(
//      key = key,
//      value = value,
//      exchange = exchange,
//      ser = ser,
//    )
//    return ser.deserialize(encoder)
//  }
//
//  fun <T> decodeQueryArgument(
//    key: String,
//    value: String?,
//    ser: KSerializer<T>,
//    exchange: HttpServerExchange,
//  ): T {
//    if (value == null) {
//      if (ser.descriptor.isNullable) {
//        return null as T
//      }
//      throw IllegalArgumentException("Can't decode null to ${ser.descriptor.serialName}")
//    }
//    val encoder = getStringDecoder(
//      key = key,
//      value = value,
//      exchange = exchange,
//      ser = ser,
//    )
//    return ser.deserialize(encoder)
//  }
//
//  fun <T : Any> encodeBody(ser: KSerializer<T>, value: T, exchange: HttpServerExchange)
//}
