package pw.binom.flux

internal fun String.isWilcardMattech(wildcard: String): Boolean {
    if (wildcard.isEmpty() && this.isEmpty())
        return true
    if (wildcard.isEmpty())
        return false
    if (wildcard[0] == '*' && wildcard.length > 1 && this.isEmpty())
        return false
    if (wildcard[0] == '?' || (this.isNotEmpty() && wildcard[0] == this[0]))
        return this.substring(1).isWilcardMattech(wildcard.substring(1))
    if (wildcard[0] == '*')
        return this.isWilcardMattech(wildcard.substring(1)) || this.substring(1).isWilcardMattech(wildcard)
    return false
}