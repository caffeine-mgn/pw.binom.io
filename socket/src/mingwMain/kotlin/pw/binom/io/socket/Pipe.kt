package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.common.*
import platform.posix.*
import platform.windows.*
import platform.windows.HANDLEVar
import pw.binom.io.Closeable

internal actual fun createPipe(): Pair<Int, Int> {
//    val name = "ololo"
    return memScoped {
//        val readPipeHandle = alloc<HANDLEVar>()
//        val writePipeHandle = alloc<HANDLEVar>()
//        val saAttr = alloc<SECURITY_ATTRIBUTES>()
//        memset(saAttr.ptr, 0, sizeOf<SECURITY_ATTRIBUTES>().convert())
//
//        saAttr.nLength = sizeOf<SECURITY_ATTRIBUTES>().convert()
//        saAttr.bInheritHandle = TRUE
//        saAttr.lpSecurityDescriptor = null
//        if (internal_create_pipe_ex(
//                readPipeHandle.ptr,
//                writePipeHandle.ptr,
//                saAttr.ptr,
//                5.convert(),
//                (FILE_FLAG_OVERLAPPED or O_BINARY).convert(),
//                (FILE_FLAG_OVERLAPPED or O_BINARY).convert(),
//            ) == 0
//        ) {
//            throw RuntimeException("Can't create pipe")
//        }
//
//        return internal_handle_to_descriptor(readPipeHandle.value!!) to internal_handle_to_descriptor(writePipeHandle.value!!)

        val fds = allocArray<IntVar>(2)
        val pipeResult = _pipe(fds, 2.convert(), O_BINARY)
        if (pipeResult != 0) {
            throw RuntimeException("Can't create pipe")
        }
        return fds[0] to fds[1]
    }
//    memScoped {
//        val namedPipe = CreateNamedPipe!!(
//            name.wcstr.ptr,             // имя создаваемого канала
//            (PIPE_ACCESS_DUPLEX or FILE_FLAG_OVERLAPPED).convert(),       // разрешен доступ на чтение и запись
//            (PIPE_TYPE_BYTE or   //читаем побайтово
//                    PIPE_WAIT).convert(),                // блокирующий режим
//            PIPE_UNLIMITED_INSTANCES.convert(), // число экземпляров канала неограниченно
//            1.convert(),                  // размер буфера исходящих сообщений
//            1.convert(),                  // размер буфера входящих сообщений
//            0.convert(),                        // тайм-аут ожидания (0=бесконечно)
//            null, // атрибут безопасности по умолчанию – доступ разрешен всем
//        )
//    }
}

internal actual fun closePipe(value: Int) {
    close(value)
}

class Pipe : Closeable {

    val readPipe = platform.posix.malloc(sizeOf<HANDLEVar>().convert())!!.reinterpret<HANDLEVar>()
    val writePipe = platform.posix.malloc(sizeOf<HANDLEVar>().convert())!!.reinterpret<HANDLEVar>()

    init {
        memScoped {
            val saAttr = alloc<SECURITY_ATTRIBUTES>()
            memset(saAttr.ptr, 0, sizeOf<SECURITY_ATTRIBUTES>().convert())

            saAttr.nLength = sizeOf<SECURITY_ATTRIBUTES>().convert()
            saAttr.bInheritHandle = TRUE
            saAttr.lpSecurityDescriptor = null
            if (CreatePipe(readPipe, writePipe, saAttr.ptr, 0) <= 0) {
                TODO("#3")
            }
        }
    }

    override fun close() {
        CloseHandle(readPipe.pointed.value)
        CloseHandle(writePipe.pointed.value)

        free(readPipe)
        free(writePipe)
    }
}
