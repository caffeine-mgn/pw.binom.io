package pw.binom.webdav.server

import pw.binom.io.AsyncInputStream
import pw.binom.io.AsyncOutputStream

interface FileSystem<U> {
    interface Entity {
        val name: String
        val length: Long
        val isFile: Boolean
        val lastModified: Long
        val path: String
    }

    suspend fun rewriteFile(user: U, path: String): AsyncOutputStream
    suspend fun mkdir(user: U, path: String)
    suspend fun delete(user: U, path: String)
    suspend fun getEntities(user: U, path: String): List<Entity>?
    suspend fun getEntry(user: U, path: String): Entity?
    suspend fun read(user: U, path: String): AsyncInputStream?
    suspend fun copy(user: U, from: String, to: String)
    suspend fun move(user: U, from: String, to: String)

    class FileNotFoundException(val path: String) : RuntimeException() {
        override fun toString(): String = "File \"$path\" not found"
    }
}