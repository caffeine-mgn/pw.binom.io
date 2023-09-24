package pw.binom.io.http.annotations

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class HeaderParam(val name: String = "")
