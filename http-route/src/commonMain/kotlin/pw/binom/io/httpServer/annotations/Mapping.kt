package pw.binom.io.httpServer.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Mapping(
  val method: String,
  val path: String,
)
