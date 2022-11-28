package pw.binom.url

val String.toQuery
    get() = Query(this)
