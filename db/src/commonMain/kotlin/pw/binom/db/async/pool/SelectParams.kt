package pw.binom.db.async.pool

//----------0----------//

class SP0<T : Any>(val u: SelectQueryWithMapper<T>)

fun <T : Any> SelectQueryWithMapper<T>.withParams() = SP0<T>(this)
suspend fun <T : Any, P1> PooledAsyncConnection.select(q: SP0<T>) =
    selectAll(q.u)

//----------1----------//

class SP1<T : Any, P1>(val u: SelectQueryWithMapper<T>, val p1: String)

fun <T : Any, P1> SelectQueryWithMapper<T>.withParams(p1: String) = SP1<T, P1>(this, p1)
suspend fun <T : Any, P1> PooledAsyncConnection.select(q: SP1<T, P1>, p1: P1) =
    selectAll(q.u, q.p1 to p1)

//----------2----------//

class SP2<T : Any, P1, P2>(val u: SelectQueryWithMapper<T>, val p1: String, val p2: String)

fun <T : Any, P1, P2> SelectQueryWithMapper<T>.withParams(p1: String, p2: String) = SP2<T, P1, P2>(this, p1, p2)
suspend fun <T : Any, P1, P2> PooledAsyncConnection.select(q: SP2<T, P1, P2>, p1: P1, p2: P2) =
    selectAll(q.u, q.p1 to p1, q.p2 to p2)

//----------3----------//

class SP3<T : Any, P1, P2, P3>(val u: SelectQueryWithMapper<T>, val p1: String, val p2: String, val p3: String)

fun <T : Any, P1, P2, P3> SelectQueryWithMapper<T>.withParams(p1: String, p2: String, p3: String) =
    SP3<T, P1, P2, P3>(this, p1, p2, p3)

suspend fun <T : Any, P1, P2, P3> PooledAsyncConnection.select(q: SP3<T, P1, P2, P3>, p1: P1, p2: P2, p3: P3) =
    selectAll(q.u, q.p1 to p1, q.p2 to p2, q.p3 to p3)

//----------4----------//

class SP4<T : Any, P1, P2, P3, P4>(
    val u: SelectQueryWithMapper<T>,
    val p1: String,
    val p2: String,
    val p3: String,
    val p4: String
)

fun <T : Any, P1, P2, P3, P4> SelectQueryWithMapper<T>.withParams(p1: String, p2: String, p3: String, p4: String) =
    SP4<T, P1, P2, P3, P4>(this, p1, p2, p3, p4)

suspend fun <T : Any, P1, P2, P3, P4> PooledAsyncConnection.select(
    q: SP4<T, P1, P2, P3, P4>,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4
) =
    selectAll(q.u, q.p1 to p1, q.p2 to p2, q.p3 to p3, q.p4 to p4)


//----------5----------//

class SP5<T : Any, P1, P2, P3, P4, P5>(
    val u: SelectQueryWithMapper<T>,
    val p1: String,
    val p2: String,
    val p3: String,
    val p4: String,
    val p5: String,
)

fun <T : Any, P1, P2, P3, P4, P5> SelectQueryWithMapper<T>.withParams(
    p1: String,
    p2: String,
    p3: String,
    p4: String,
    p5: String
) =
    SP5<T, P1, P2, P3, P4, P5>(this, p1, p2, p3, p4, p5)

suspend fun <T : Any, P1, P2, P3, P4, P5> PooledAsyncConnection.select(
    q: SP5<T, P1, P2, P3, P4, P5>,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    p5: P5,
) =
    selectAll(q.u, q.p1 to p1, q.p2 to p2, q.p3 to p3, q.p4 to p4, q.p5 to p5)


//----------6----------//

class SP6<T : Any, P1, P2, P3, P4, P5, P6>(
    val u: SelectQueryWithMapper<T>,
    val p1: String,
    val p2: String,
    val p3: String,
    val p4: String,
    val p5: String,
    val p6: String,
)

fun <T : Any, P1, P2, P3, P4, P5, P6> SelectQueryWithMapper<T>.withParams(
    p1: String,
    p2: String,
    p3: String,
    p4: String,
    p5: String,
    p6: String,
) =
    SP6<T, P1, P2, P3, P4, P5, P6>(this, p1, p2, p3, p4, p5, p6)

suspend fun <T : Any, P1, P2, P3, P4, P5, P6> PooledAsyncConnection.select(
    q: SP6<T, P1, P2, P3, P4, P5, P6>,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    p5: P5,
    p6: P6,
) =
    selectAll(q.u, q.p1 to p1, q.p2 to p2, q.p3 to p3, q.p4 to p4, q.p5 to p5, q.p6 to p6)


//----------7----------//

class SP7<T : Any, P1, P2, P3, P4, P5, P6, P7>(
    val u: SelectQueryWithMapper<T>,
    val p1: String,
    val p2: String,
    val p3: String,
    val p4: String,
    val p5: String,
    val p6: String,
    val p7: String,
)

fun <T : Any, P1, P2, P3, P4, P5, P6, P7> SelectQueryWithMapper<T>.withParams(
    p1: String,
    p2: String,
    p3: String,
    p4: String,
    p5: String,
    p6: String,
    p7: String,
) =
    SP7<T, P1, P2, P3, P4, P5, P6, P7>(this, p1, p2, p3, p4, p5, p6, p7)

suspend fun <T : Any, P1, P2, P3, P4, P5, P6, P7> PooledAsyncConnection.select(
    q: SP7<T, P1, P2, P3, P4, P5, P6, P7>,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    p5: P5,
    p6: P6,
    p7: P7,
) =
    selectAll(q.u, q.p1 to p1, q.p2 to p2, q.p3 to p3, q.p4 to p4, q.p5 to p5, q.p6 to p6, q.p7 to p7)


//----------8----------//

class SP8<T : Any, P1, P2, P3, P4, P5, P6, P7, P8>(
    val u: SelectQueryWithMapper<T>,
    val p1: String,
    val p2: String,
    val p3: String,
    val p4: String,
    val p5: String,
    val p6: String,
    val p7: String,
    val p8: String,
)

fun <T : Any, P1, P2, P3, P4, P5, P6, P7, P8> SelectQueryWithMapper<T>.withParams(
    p1: String,
    p2: String,
    p3: String,
    p4: String,
    p5: String,
    p6: String,
    p7: String,
    p8: String,
) =
    SP8<T, P1, P2, P3, P4, P5, P6, P7, P8>(this, p1, p2, p3, p4, p5, p6, p7, p8)

suspend fun <T : Any, P1, P2, P3, P4, P5, P6, P7, P8> PooledAsyncConnection.select(
    q: SP8<T, P1, P2, P3, P4, P5, P6, P7, P8>,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    p5: P5,
    p6: P6,
    p7: P7,
    p8: P8,
) =
    selectAll(
        q.u,
        q.p1 to p1,
        q.p2 to p2,
        q.p3 to p3,
        q.p4 to p4,
        q.p5 to p5,
        q.p6 to p6,
        q.p7 to p7,
        q.p8 to p8
    )

//----------9----------//

class SP9<T : Any, P1, P2, P3, P4, P5, P6, P7, P8, P9>(
    val u: SelectQueryWithMapper<T>,
    val p1: String,
    val p2: String,
    val p3: String,
    val p4: String,
    val p5: String,
    val p6: String,
    val p7: String,
    val p8: String,
    val p9: String,
)

fun <T : Any, P1, P2, P3, P4, P5, P6, P7, P8, P9> SelectQueryWithMapper<T>.withParams(
    p1: String,
    p2: String,
    p3: String,
    p4: String,
    p5: String,
    p6: String,
    p7: String,
    p8: String,
    p9: String,
) =
    SP9<T, P1, P2, P3, P4, P5, P6, P7, P8, P9>(this, p1, p2, p3, p4, p5, p6, p7, p8, p9)

suspend fun <T : Any, P1, P2, P3, P4, P5, P6, P7, P8, P9> PooledAsyncConnection.select(
    q: SP9<T, P1, P2, P3, P4, P5, P6, P7, P8, P9>,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    p5: P5,
    p6: P6,
    p7: P7,
    p8: P8,
    p9: P9,
) =
    selectAll(
        q.u,
        q.p1 to p1,
        q.p2 to p2,
        q.p3 to p3,
        q.p4 to p4,
        q.p5 to p5,
        q.p6 to p6,
        q.p7 to p7,
        q.p8 to p8,
        q.p9 to p9
    )


//---------10----------//

class SP10<T : Any, P1, P2, P3, P4, P5, P6, P7, P8, P9,P10>(
    val u: SelectQueryWithMapper<T>,
    val p1: String,
    val p2: String,
    val p3: String,
    val p4: String,
    val p5: String,
    val p6: String,
    val p7: String,
    val p8: String,
    val p9: String,
    val p10: String,
)

fun <T : Any, P1, P2, P3, P4, P5, P6, P7, P8, P9,P10> SelectQueryWithMapper<T>.withParams(
    p1: String,
    p2: String,
    p3: String,
    p4: String,
    p5: String,
    p6: String,
    p7: String,
    p8: String,
    p9: String,
    p10: String,
) =
    SP10<T, P1, P2, P3, P4, P5, P6, P7, P8, P9,P10>(this, p1, p2, p3, p4, p5, p6, p7, p8, p9,p10)

suspend fun <T : Any, P1, P2, P3, P4, P5, P6, P7, P8, P9,P10> PooledAsyncConnection.select(
    q: SP10<T, P1, P2, P3, P4, P5, P6, P7, P8, P9,P10>,
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4,
    p5: P5,
    p6: P6,
    p7: P7,
    p8: P8,
    p9: P9,
    p10: P10,
) =
    selectAll(
        q.u,
        q.p1 to p1,
        q.p2 to p2,
        q.p3 to p3,
        q.p4 to p4,
        q.p5 to p5,
        q.p6 to p6,
        q.p7 to p7,
        q.p8 to p8,
        q.p9 to p9,
        q.p10 to p10,
    )