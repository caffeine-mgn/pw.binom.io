package pw.binom.io

import pw.binom.AsyncInput
import pw.binom.AsyncOutput

interface FileSystem {
    interface Entity {
        val name: String
            get() {
                val p = path.lastIndexOf('/')
                return if (p == -1)
                    path
                else
                    path.substring(p + 1)
            }
        val length: Long
        val isFile: Boolean
        val lastModified: Long
        val path: String
        val fileSystem: FileSystem
        suspend fun read(offset: ULong = 0uL, length: ULong? = null): AsyncInput?
        suspend fun copy(path: String, overwrite: Boolean = false): Entity
        suspend fun move(path: String, overwrite: Boolean = false): Entity
        suspend fun delete()
        suspend fun rewrite(): AsyncOutput
        suspend fun isLocked(): Boolean? = null
        suspend fun lock(): Boolean = false
        suspend fun unlock(): Boolean = false
    }

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
    suspend fun mkdir(path: String): Entity?
    suspend fun getDir(path: String): Sequence<Entity>?

    suspend fun get(path: String): Entity?
    suspend fun new(path: String): AsyncOutput

    class FileNotFoundException(val path: String) : IOException("File \"$path\" not found")
    class FileLockedException(val path: String) : IOException("File \"$path\" is locked")
    class EntityExistException(val path: String) : IOException("Entity \"$path\" already exist")
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