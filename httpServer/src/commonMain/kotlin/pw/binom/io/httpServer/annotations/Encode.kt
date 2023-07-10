package pw.binom.io.httpServer.annotations

import pw.binom.io.httpServer.HttpEncoder
import kotlin.reflect.KClass

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Encode(val encoderClass: KClass<HttpEncoder<Any>>)
