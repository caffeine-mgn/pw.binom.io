package pw.binom.db.postgresql.async

import kotlinx.coroutines.test.runTest
import pw.binom.charset.Charsets
import pw.binom.date.parseIso8601Date
import pw.binom.db.ColumnType
import pw.binom.db.async.firstOrNull
import pw.binom.db.async.map
import pw.binom.io.use
import pw.binom.network.NetworkAddress
import pw.binom.uuid.UUID
import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.test.*
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class TestConnect : BaseTest() {

    //    @Test
    fun test() = runTest {
        val con = PGConnection.connect(
            address = NetworkAddress.Immutable(
                host = "localhost",
                port = 25432,
            ),
            charset = Charsets.UTF8,
            userName = "postgres",
            password = "postgres",
            dataBase = "sellsystem"
        )

//                val msg = con.sendQuery("select now()")
        val msg = con.query("update \"user\" set login='' where login=''") as QueryResponse.Status
        println("Updated ${msg.status}")

        val msg2 = con.query("select * from \"user\" where login='demo'")
        msg2 as QueryResponse.Data
        while (msg2.next()) {
            val row = (0 until msg2.meta.size).map {
                msg2[it]
            }.joinToString(" | ")
            println("->$row")
        }
        println("DONE!")

//                con.prepareStatement(
//                    query = "select * from \"user\" where login=? and company_id=?",
//                    paramColumnTypes = listOf(ResultSet.ColumnType.STRING, ResultSet.ColumnType.INT),
//                    resultColumnTypes = listOf(
//                        ResultSet.ColumnType.LONG,
//                        ResultSet.ColumnType.STRING,
//                        ResultSet.ColumnType.LONG
//                    )
//                ).use { ps ->
//                    ps.set(0, "demo")
//                    ps.set(1, 4)
//                    ps.executeQuery().use {
//                        println(it.columns.joinToString(" | "))
//                        while (it.next()) {
//                            val line = it.columns.map { name ->
//                                it.getString(name)
//                            }
//                                .joinToString(" | ")
//                            println(line)
//                        }
//                    }
//
//                    ps.set(0, "reklama@mirteck.ru")
//                    ps.set(1, 147)
//                    ps.executeQuery().use {
//                        println(it.columns.joinToString(" | "))
//                        while (it.next()) {
//                            val line = it.columns.map { name ->
//                                it.getString(name)
//                            }
//                                .joinToString(" | ")
//                            println(line)
//                        }
//                    }
//                }

        con.prepareStatement(
            "select * from osmi_cards_config where id=?",
            listOf(ColumnType.UUID)
        ).use {
            try {
                it.set(0, UUID.fromString("db5d3f68-ed81-4c01-8ac8-6eb783d67eeb"))
                it.executeQuery().use {
                    println(it.columns.joinToString(" | "))
                    while (it.next()) {
                        val line = it.columns.map { name ->
                            it.getString(name)
                        }
                            .joinToString(" | ")
                        println(line)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }

        con.prepareStatement("SELECT TIMESTAMP '2020-01-05 15:43:36.000000'").use {
            try {
                it.executeQuery().use {
                    println(it.columns.joinToString(" | "))
                    while (it.next()) {
                        val line = it.columns.map { name ->
                            it.getString(name)
                        }
                            .joinToString(" | ")
                        println(line)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }

        con.prepareStatement("select * from buyers limit 3").use {
            try {
                it.executeQuery().use {
                    println(it.columns.joinToString(" | "))
                    while (it.next()) {
                        val line = it.columns.map { name ->
                            it.getString(name)
                        }
                            .joinToString(" | ")
                        println(line)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
    }

    @Test
    fun numArgTest() {
        pg {
            it.prepareStatement("select $1").use {
                it.executeQuery("1").firstOrNull { it.getString(0) }
            }
        }
    }

    @Test
    fun invalidPreparedStatement() {
        pg {
            try {
                it.prepareStatement("select * from ooooo").use {
                    it.executeUpdate()
                }
                fail()
            } catch (e: PostgresqlException) {
                // ok
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun portalCloseTestTest() {
        pg { connect ->
            connect.executeUpdate(
                """
create table member_tag
(
	member_id bigint not null,
	tag_id bigint not null,
	constraint member_tag_pkey
		primary key (member_id, tag_id)
);
            """
            )
            connect.beginTransaction()
            connect.prepareStatement("delete from member_tag where member_id=? and tag_id=?").use { ps ->
                ps.set(0, 0)
                ps.set(1, 0)
//                repeat(30) {
                println("---===START===---")
                val time = measureTime { ps.executeUpdate() }
                println("---===END===--- $time")
//                }
            }
            connect.commit()
        }
    }

    @Test
    fun dateArgumentTest() {
        pg {
            it.executeUpdate(
                """
                create table if not exists date_argument_test
                (
                    id bigserial not null primary key,
                    date_column timestamp
                )
            """
            )
            it.executeUpdate("""insert into date_argument_test (id,date_column) values(1,'2018-02-01 11:42:39.425') """)
            it.executeUpdate("commit")
            val date = it.prepareStatement("select date_column from date_argument_test").use {
                it.executeQuery().firstOrNull {
                    it.getDate(0)
                }
            }
            assertEquals("2018-02-01 11:42:39.425".parseIso8601Date(0)!!.time, date!!.time)
        }
    }

    @Test
    fun blolbTest() {
        pg {
            val vv = byteArrayOf(97)
            val sb = StringBuilder(vv.size * 2 + 2)
            sb.append("\\x")
            vv.forEach {
                val value = it.toInt() and 0xFF
                value.toString(16)
                val first = value shr 4
                val second = value and 0xF
                sb.append(first.toString(16))
                    .append(second.toString(16))
            }
            println("--->\"$sb\"")
            it.executeUpdate(
                """
                create table if not exists blob_test
                (
                    id bigserial primary key,
                    data bytea not null
                )
            """
            )
            it.executeUpdate("insert into blob_test (id, data) values (1,'\\x61')")
            it.prepareStatement(
                """
        select data from "blob_test"
            """
            ).executeQuery().map {
                it.getBlob(0)!!.also {
                    assertEquals(1, it.size)
                    assertEquals(97, it[0])
                }
            }
            it.createStatement().use {
                it.executeQuery(
                    """
        select data from "blob_test"
            """
                ).map {
                    it.getBlob(0)!!.also {
                        assertEquals(1, it.size)
                        assertEquals(97, it[0])
                    }
                }
            }
        }
    }

    @Test
    fun doubleTest() {
        pg {
            it.executeUpdate(
                """
                create table if not exists double_test
                (
                    id bigserial primary key,
                    amount double precision not null,
                    exp timestamp without time zone
                )
            """
            )
            it.executeUpdate("insert into double_test (id, amount, exp) values (1,10, '2021-03-29 10:00:00')")
            it.prepareStatement(
                """
        select
            amount,
            exp
        from "double_test"
            """
            ).executeQuery().map {
                assertEquals(10.0, it.getDouble(0))
                assertEquals(1617012000000L, it.getDate(1)!!.time)
            }
        }
    }

    @Test
    fun missingColumn() {
        pg {
            it.executeUpdate(
                """
                create table if not exists missing_column_test
                (
                    id bigserial not null primary key,
                    amount bigint not null,
                    "end" timestamp,
                    start timestamp not null,
                    account_id bigint,
                    member_id bigint
                )
            """
            )

            try {
                it.prepareStatement("select sum(amount) from missing_column_test where company_id=?").use {
                    it.executeQuery(10).use {
                    }
                }
                fail()
            } catch (e: PostgresqlException) {
                // ok
            }
        }
    }

    @Test
    fun noClosePrepareStatementSet() {
        pg {
            it.prepareStatement("select 1")
        }
    }

    @Test
    fun noCloseResultSet() {
        pg {
            it.prepareStatement("select 1").use {
                it.executeQuery()
            }
        }
    }

    @Test
    fun connectTest() {
        pg {
        }
    }

    @Test
    fun testString() {
        pg { con ->
            con.prepareStatement("""select 'Привет' """).executeQuery().use {
                assertTrue(it.next())
                assertEquals("Привет", it.getString(0))
                println("OK")
            }
            con.prepareStatement("""select 'ПрИвет' """).executeQuery().use {
                assertTrue(it.next())
                assertEquals("ПрИвет", it.getString(0))
                println("OK")
            }

            con.prepareStatement("""select '' """).executeQuery().use {
                assertTrue(it.next())
                assertEquals("", it.getString(0))
                println("OK")
            }
        }
    }

    @Test
    fun testInsert() {
        pg { con ->
            con.createStatement().use {
//                it.executeUpdate(
//                    """
//                    drop table if exists test1
//                """
//                )

                it.executeUpdate(
                    """
                    create table if not exists test1(
                    id               uuid         not null primary key,
                    name             text         not null
                    )
                """
                )
            }
            con.prepareStatement("insert into test1 (id,name) values (?,?)").use {
                try {
                    it.set(0, Random.nextUuid())
                    it.set(1, Random.nextUuid().toString())
                    val update = it.executeUpdate()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
            con.prepareStatement("insert into test1 (id,name) values (?,?)").use {
                it.set(0, Random.nextUuid())
                it.set(1, Random.nextUuid().toString())
                it.executeUpdate()
            }

            con.prepareStatement("insert into test1 (id,name) values (?,?) returning id").use {
                it.set(0, Random.nextUuid())
                it.set(1, Random.nextUuid().toString())
                val insertedId = it.executeQuery().map { it.getUUID(0) }.first()
            }
        }
    }

    @Test
    fun timestampTest() {
        println()
//        pg { con ->
//            con.prepareStatement("SELECT TIMESTAMP '2020-01-05 15:43:36.000000'").use {
//                try {
//                    it.executeQuery().use {
//                        assertEquals(1, it.columns.size)
//                        assertTrue(it.next())
//                        assertEquals(1578239016000L, it.getDate(0)!!.time)
//                        assertFalse(it.next())
//                    }
//                } catch (e: Throwable) {
//                    e.printStackTrace()
//                    throw e
//                }
//            }
//        }

        pg { con ->
            con.prepareStatement("SELECT TIMESTAMPTZ '2020-01-05 15:43:36.000'").use {
                try {
                    it.executeQuery().use {
                        assertEquals(1, it.columns.size)
                        assertTrue(it.next())
                        assertEquals("2020-01-05 15:43:36.000".parseIso8601Date(0)!!.time, it.getDate(0)!!.time)
                        assertFalse(it.next())
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }

//    @Test
//    fun decamalTest() {
//        pg { con ->
//            con.prepareStatement("select 1.5").use {
//                try {
//                    it.executeQuery().use {
//                        assertEquals(1, it.columns.size)
//                        assertTrue(it.next())
//                        assertEquals(1.5, it.getDouble(0))
//                        assertEquals(BigDecimal.fromDouble(1.5), it.getBigDecimal(0))
//                        assertFalse(it.next())
//                    }
//                } catch (e: Throwable) {
//                    e.printStackTrace()
//                    throw e
//                }
//            }
//        }
//        pg { con ->
//            con.prepareStatement("select 1.5f").use {
//                try {
//                    it.executeQuery().use {
//                        assertEquals(1, it.columns.size)
//                        assertTrue(it.next())
//                        assertEquals(1.5, it.getDouble(0))
//                        assertEquals(BigDecimal.fromDouble(1.5), it.getBigDecimal(0))
//                        assertFalse(it.next())
//                    }
//                } catch (e: Throwable) {
//                    e.printStackTrace()
//                    throw e
//                }
//            }
//        }
//    }
}
