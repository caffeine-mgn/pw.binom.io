package pw.binom.s3

fun String?.inArray(array: Array<String>) =
    if (this == null) {
        "" in array
    } else {
        this in array
    }
