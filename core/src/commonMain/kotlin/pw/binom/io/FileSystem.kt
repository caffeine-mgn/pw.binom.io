package pw.binom.io

import pw.binom.net.Path
import pw.binom.net.toPath

data class Quota(
    val availableBytes: Long,
    val usedBytes: Long,
)

interface FileSystem {
    interface Entity {
        val name: String
            get() = path.name
        val length: Long
        val isFile: Boolean
        val lastModified: Long
        val path: Path
        val fileSystem: FileSystem
        suspend fun read(offset: ULong = 0uL, length: ULong? = null): AsyncInput?
        suspend fun copy(path: Path, overwrite: Boolean = false): Entity
        suspend fun move(path: Path, overwrite: Boolean = false): Entity
        suspend fun delete()
        suspend fun rewrite(): AsyncOutput
        suspend fun isLocked(): Boolean? = null
        suspend fun lock(): Boolean = false
        suspend fun unlock(): Boolean = false
    }

    suspend fun getQuota(path: Path): Quota?
    val isSupportUserSystem: Boolean

    /**
     * If [isSupportUserSystem]==[false], then [user] not affected to execution of [func].
     * If [isSupportUserSystem]==[true] will put [user] into context and will use during [func] execution
     *
     * @param user User for current execution of [func]
     * @param func Lambda function for run with user [user]
     * @return Returns result of [func]
     */
    suspend fun <T> useUser(user: Any?, func: suspend () -> T): T
    suspend fun mkdir(path: Path): Entity?
    suspend fun getDir(path: Path): List<Entity>?

    suspend fun get(path: Path): Entity?
    suspend fun new(path: Path): AsyncOutput

    class FileNotFoundException(val path: Path) : IOException("File \"$path\" not found")
    class FileLockedException(val path: Path) : IOException("File \"$path\" is locked")
    class EntityExistException(val path: Path) : IOException("Entity \"$path\" already exist")

    suspend fun mkdirs(path: Path): Entity? {
        var first = true
        val sb = StringBuilder()
        var last: Entity? = null
        path.elements.forEach {
            if (!first) {
                sb.append("/")
            }
            first = false

            sb.append(it)
            last = get(sb.toString().toPath)
            if (last == null) {
                last = mkdir(sb.toString().toPath)
            }
        }
        return last
    }
}

val FileSystem.Entity.extension: String
    get() {
        val name = name
        return name.lastIndexOf('.').let {
            if (it == -1)
                return ""
            else
                name.substring(it + 1)
        }
    }

val FileSystem.Entity.nameWithoutExtension: String
    get() {
        val name = name
        return name.lastIndexOf('.').let {
            if (it == -1)
                return name
            else
                name.substring(0, it)
        }
    }
