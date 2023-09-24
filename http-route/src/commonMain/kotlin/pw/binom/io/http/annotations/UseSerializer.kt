package pw.binom.io.http.annotations

import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
annotation class UseSerializer(val with: KClass<out KSerializer<*>>)
