package pw.binom.process

import kotlinx.cinterop.*
import platform.posix.memset
import platform.windows.*

private inline fun CreateProcess(
        lpApplicationName: platform.windows.LPCWSTR?,
        lpCommandLine: platform.windows.LPWSTR?,
        lpProcessAttributes: platform.windows.LPSECURITY_ATTRIBUTES?,
        lpThreadAttributes: platform.windows.LPSECURITY_ATTRIBUTES?,
        bInheritHandles: platform.windows.WINBOOL,
        dwCreationFlags: platform.windows.DWORD,
        lpEnvironment: platform.windows.LPVOID?,
        lpCurrentDirectory: platform.windows.LPCWSTR?,
        lpStartupInfo: platform.windows.LPSTARTUPINFOW?,
        lpProcessInformation: platform.windows.LPPROCESS_INFORMATION?
) = platform.windows.CreateProcess!!(
        lpApplicationName, lpCommandLine, lpProcessAttributes, lpThreadAttributes, bInheritHandles, dwCreationFlags, lpEnvironment, lpCurrentDirectory, lpStartupInfo, lpProcessInformation
)

inline fun <R> processHandler(pid: Long, func: (HANDLE?) -> R): R {
    val handler = OpenProcess((PROCESS_QUERY_INFORMATION or PROCESS_VM_READ).convert(), FALSE, pid.convert()) ?: TODO()
    try {
        return func(handler)
    } finally {
        CloseHandle(handler)
    }
}

class WinProcess(val exe: String, args: List<String>, workDir: String?) : Process {

    override fun join() {
        if (!isActive)
            return
        processHandler(pid) { WaitForSingleObject(it, INFINITE) }
    }

    override val isActive: Boolean
        get() {
            memScoped {
                val ex = alloc<UIntVar>()
                ex.value = 0u
                GetExitCodeProcess(processHandle, ex.ptr)
                return ex.value == STILL_ACTIVE
            }
        }
    override val exitStatus: Int
        get() {
            memScoped {
                val ex = alloc<UIntVar>()
                val r =GetExitCodeProcess(processHandle, ex.ptr)
                if (r == FALSE)
                    TODO("GetExitCodeProcess error ${GetLastError()}")
                if (ex.value == STILL_ACTIVE)
                    throw Process.ProcessStillActive()
                return ex.value.toInt()
            }
        }

    override var pid: Long = 0

    override fun close() {
        stdout.close()
        stderr.close()
        stdin.close()
        CloseHandle(processHandle)
    }

    override val stdout = PipeInput(this)
    override val stderr = PipeInput(this)
    override val stdin = PipeOutput()

    private lateinit var processHandle: HANDLE

    init {
        memScoped {
            val piProcInfo = alloc<PROCESS_INFORMATION>()
            memset(piProcInfo.ptr, 0, sizeOf<PROCESS_INFORMATION>().convert())

            val vv = alloc<STARTUPINFO>()
            vv.cb = sizeOf<STARTUPINFO>().convert()
            vv.hStdError = stderr.handler
            vv.hStdOutput = stdout.handler
            vv.hStdInput = stdin.handler


            vv.dwFlags = STARTF_USESTDHANDLES.convert()


            val bSuccess = CreateProcess(
                    null,
                    "\"$exe\" ${args.map { "\"$it\"" }.joinToString(" ")}".wcstr.ptr,     // command line
                    null,          // process security attributes
                    null,          // primary thread security attributes
                    TRUE,          // handles are inherited
                    0.convert(),             // creation flags
                    null,          // use parent's environment
                    workDir?.wcstr?.ptr,          // use parent's current directory
                    vv.ptr,  // STARTUPINFO pointer
                    piProcInfo.ptr
            ) > 0

            if (!bSuccess)
                TODO("CreateProcessA error: ${GetLastError()}")

            pid = piProcInfo.dwProcessId.toLong()

            processHandle = piProcInfo.hProcess!!
//            CloseHandle(stdout.otherHandler)
//            CloseHandle(stderr.otherHandler)
//            CloseHandle(stdin.otherHandler)
//            CloseHandle(piProcInfo.hProcess)
            CloseHandle(piProcInfo.hThread)
        }
    }
}

actual fun Process.Companion.execute(path: String, args: Array<String>, workDir: String?): Process =
        WinProcess(exe = path, args = args.toList(), workDir = workDir)