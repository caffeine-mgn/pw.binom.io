package pw.binom.charset

enum class CharsetTransformResult {
    SUCCESS,
    MALFORMED,
    ERROR,
    UNMAPPABLE,
    INPUT_OVER,
    OUTPUT_OVER,
}
