package pw.binom.io.httpServer.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Context(
  val path: String,
)
