package pw.binom.io

interface FileSystem<U> {
    interface Entity<U> {
        val name: String
        val length: Long
        val isFile: Boolean
        val lastModified: Long
        val path: String
        val user: U
        val fileSystem: FileSystem<U>
        suspend fun read(): AsyncInputStream?
        suspend fun copy(path: String): Entity<U>
        suspend fun move(path: String): Entity<U>
        suspend fun delete()
        suspend fun rewrite(): AsyncOutputStream
    }

    //    suspend fun rewriteFile(user: U, path: String): AsyncOutputStream
    suspend fun mkdir(user: U, path: String): Entity<U>

    //    suspend fun delete(user: U, path: String)
    suspend fun getDir(user: U, path: String): Sequence<Entity<U>>?

    suspend fun get(user: U, path: String): Entity<U>?
    suspend fun new(user: U, path: String): AsyncOutputStream
//    suspend fun read(user: U, path: String): AsyncInputStream?
//    suspend fun copy(user: U, from: String, to: String)
//    suspend fun move(user: U, from: String, to: String)

    class FileNotFoundException(val path: String) : RuntimeException() {
        override fun toString(): String = "File \"$path\" not found"
    }
}

val FileSystem.Entity<*>.extension: String
    get() {
        val name = name
        return name.lastIndexOf('.').let {
            if (it == -1)
                return ""
            else
                name.substring(it + 1)
        }
    }

val FileSystem.Entity<*>.nameWithoutExtension: String
    get() {
        val name = name
        return name.lastIndexOf('.').let {
            if (it == -1)
                return name
            else
                name.substring(0, it)
        }
    }