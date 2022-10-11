package pw.binom.s3

internal val emptySha256 = byteArrayOf(
    -29,
    -80,
    -60,
    66,
    -104,
    -4,
    28,
    20,
    -102,
    -5,
    -12,
    -56,
    -103,
    111,
    -71,
    36,
    39,
    -82,
    65,
    -28,
    100,
    -101,
    -109,
    76,
    -92,
    -107,
    -103,
    27,
    120,
    82,
    -72,
    85
)

fun String?.inArray(array: Array<String>) =
    if (this == null) {
        "" in array
    } else {
        this in array
    }
