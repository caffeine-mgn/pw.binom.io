package pw.binom

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object System {
  actual fun gc() {
    java.lang.System.gc()
  }
}
