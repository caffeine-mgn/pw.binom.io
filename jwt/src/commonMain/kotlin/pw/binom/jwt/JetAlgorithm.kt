package pw.binom.jwt

enum class JetAlgorithm {
  HS256,
  HS384,
  HS512,
  RSA,
  ;

  companion object {
    private val byName = entries.associateBy { it.name }
    fun findByName(alg: String) = byName[alg]
    fun getByName(alg: String) = byName[alg] ?: throw IllegalArgumentException("Can't find algorithm \"$alg\"")
  }
}
