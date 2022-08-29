package pw.binom.db.tarantool

import kotlinx.coroutines.withTimeout
import pw.binom.nextUuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val CACHE_SPACE_NAME = "cache"
private const val clearIntervalInSeconds = 60 * 10
private const val CACHE_INDEX_PRIMARY = "primary"

const val TARANTOOL_CACHE_TOOL = """
                lastCacheGc = os.time()
                
s=box.schema.space.create('$CACHE_SPACE_NAME',{engine = 'memtx', if_not_exists = true})
s:format({
    {name = 'key', type = 'string', is_nullable=false},
    {name = 'value', type = 'string', is_nullable=false},
    {name = 'exp', type = 'unsigned', is_nullable=false}
})

s:create_index('$CACHE_INDEX_PRIMARY',
    {
        type = 'hash',
        unique = true,
        if_not_exists = true,
        parts = {
                {1, 'string'}
            }
    }
)

s:create_index('by_exp',
    {
        type = 'tree',
        unique = true,
        if_not_exists = true,
        parts = {
                {3, 'unsigned'}
            }
    }
)
                
                function cacheGc()
                    local data = box.space.$CACHE_SPACE_NAME.index.by_exp:select({os.time()*1000}, {iterator='LE'})
                    local removed = 0
                    for key, value in ipairs(data)
                    do
                        box.space.$CACHE_SPACE_NAME.index.$CACHE_INDEX_PRIMARY:delete({value[1]})
                        removed = removed + 1
                    end
                    lastCacheGc = os.time()
                    return removed
                end
                
                function checkCacheGc()
                    if os.time() - lastCacheGc > $clearIntervalInSeconds then
                        cacheGc()
                    end
                end
                
                function getCache(key)
                    checkCacheGc()
                    local data = box.space.$CACHE_SPACE_NAME.index.$CACHE_INDEX_PRIMARY:select({key},{iterator='EQ'})
                    if table.getn(data) == 0 then
                        return nil
                    end
                    if data[1][3] < os.time() * 1000 then
                        return null
                    end
                    return data[1][2]
                end
                
                function setCache(key, value, exp)
                    if exp <  os.time() * 1000 or value == nil then
                        deleteCache(key)
                    else
                        box.space.$CACHE_SPACE_NAME:upsert({key, value, exp}, { {'=', 2, value},{'=', 3, exp} })
                    end
                end
                
                function deleteCache(key)
                    box.space.$CACHE_SPACE_NAME.index.$CACHE_INDEX_PRIMARY:delete({key})
                end
                
                function clearByPrefix(prefix)
                    local data = box.space.$CACHE_SPACE_NAME:select()
                    local removed = 0
                    for key, value in ipairs(data)
                    do
                        local kk = value[1]
                        if kk:sub(1, #prefix) == prefix then
                            box.space.$CACHE_SPACE_NAME.index.$CACHE_INDEX_PRIMARY:delete({kk})
                            removed = removed + 1
                        end
                    end
                    return removed
                end
                
                function clear()
                    box.space.$CACHE_SPACE_NAME:truncate()
                end
            """

class TestConnection : BaseTest() {

    @Test
    fun stringPass() {
        tarantool { con ->
            val text = "Response From Tarantool: ${Random.nextUuid()}"
            val response = con.eval("return '$text'")
            assertTrue(response is List<*>)
            assertEquals(1, response.size)
            assertEquals(text, response[0])
        }
        tarantool { con ->
            val text = "Response From Tarantool: ${Random.nextUuid()}"
            val response = con.eval("return ...", text, text)
            assertTrue(response is List<*>)
            assertEquals(2, response.size)
            assertEquals(text, response[0])
            assertEquals(text, response[1])
        }
    }

    @Test
    fun uuidPass() {
        tarantool { con ->
            val uuid = Random.nextUuid()
            val response = con.eval("return ...", uuid)
            assertTrue(response is List<*>)
            assertEquals(1, response.size)
            assertEquals(uuid, response[0])
        }
    }

    @Test
    fun intPass() {
        tarantool { con ->
            val value = Random.nextInt()
            val response = con.eval("return ...", value)
            assertTrue(response is List<*>)
            assertEquals(1, response.size)
            assertEquals(value, response[0])
        }
    }

    @Test
    fun bytesPass() {
        tarantool { con ->
            val response = con.eval("return type(...)", Random.nextBytes(10))
            assertTrue(response is List<*>)
            assertEquals(1, response.size)
            assertEquals("string", response[0])
        }
    }

    @Test
    fun listPass() {
        tarantool { con ->
            val value = listOf(Random.nextInt(), Random.nextInt())
            val response = con.eval("return ...", value)
            assertTrue(response is List<*>)
            assertEquals(2, response.size)
            assertEquals(value[0], response[0])
            assertEquals(value[1], response[1])
        }
    }

    @Test
    fun listOfListPass() {
        tarantool { con ->
            val value = listOf(listOf(Random.nextInt()), listOf(Random.nextInt()))
            val response = con.eval("return ...", value)
            assertTrue(response is List<*>)
            assertEquals(2, response.size)
            assertEquals(value[0], response[0])
            assertEquals(value[1], response[1])
        }
    }

    @Test
    fun cacheTest() {
        tarantool { con ->
            withTimeout(10_000) {
                con.eval("return cacheDefined~=nil")
                con.eval(TARANTOOL_CACHE_TOOL)
            }
        }
    }
}
