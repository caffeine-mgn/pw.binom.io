package pw.binom.process

import kotlinx.cinterop.*
import platform.posix.memcpy
import platform.posix.memset
import platform.windows.*
import kotlin.native.concurrent.freeze

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

class WinProcess(exe: String, args: List<String>, workDir: String?, env: Map<String, String>) : Process {

    override var pid: Long = 0

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
            val envList = env.map { "${it.key}=${it.value}" }

            val memCount = envList.sumBy { it.length + 1 } + 1
            val mem = allocArray<UShortVar>(memCount)
            var p = 0
            envList.forEachIndexed { index, s ->
                memcpy(mem + p, s.wcstr.ptr, (s.length * 2 + 2).convert())
                p += s.length + 1
            }

            mem[p + 1] = 0.toUShort()


            val bSuccess = CreateProcess(
                    null,
                    "\"$exe\" ${args.map { "\"$it\"" }.joinToString(" ")}".wcstr.ptr,     // command line
                    null,          // process security attributes
                    null,          // primary thread security attributes
                    TRUE,          // handles are inherited
                    CREATE_UNICODE_ENVIRONMENT.convert(),             // creation flags
                    mem,          // use parent's environment
                    workDir?.wcstr?.ptr,          // use parent's current directory
                    vv.ptr,  // STARTUPINFO pointer
                    piProcInfo.ptr
            ) > 0

            if (!bSuccess)
                TODO("CreateProcessA error: ${GetLastError()}")

            pid = piProcInfo.dwProcessId.toLong()
            processHandle = piProcInfo.hProcess!!
            CloseHandle(piProcInfo.hThread)
        }
    }

    override fun join() {
        if (!isActive)
            return
        WaitForSingleObject(processHandle, INFINITE)
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
                val r = GetExitCodeProcess(processHandle, ex.ptr)
                if (r == FALSE)
                    TODO("GetExitCodeProcess error ${GetLastError()}")
                if (ex.value == STILL_ACTIVE)
                    throw Process.ProcessStillActive()
                return ex.value.toInt()
            }
        }


    override fun close() {
        stdout.close()
        stderr.close()
        stdin.close()

        TerminateProcess(processHandle,1.convert())

        CloseHandle(processHandle)
    }

    init {
        freeze()
    }
}

actual fun Process.Companion.execute(path: String, args: List<String>, env: Map<String, String>, workDir: String?): Process =
        WinProcess(exe = path, args = args, workDir = workDir, env = env)