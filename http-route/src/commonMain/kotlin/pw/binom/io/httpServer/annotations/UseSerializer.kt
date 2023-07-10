package pw.binom.io.httpServer.annotations

import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class UseSerializer(val serializer: KClass<KSerializer<Any>>)
