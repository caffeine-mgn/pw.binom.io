package pw.binom.jwt

fun interface JetVerification {
  fun verify(alg: JetAlgorithm, headBase64: String, payloadBase64: String, signBase64: String): Boolean
}
