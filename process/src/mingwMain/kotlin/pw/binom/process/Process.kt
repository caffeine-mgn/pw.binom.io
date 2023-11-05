package pw.binom.process

import kotlinx.cinterop.*
import platform.posix.memcpy
import platform.posix.memset
import platform.windows.*

@OptIn(ExperimentalForeignApi::class)
private inline fun CreateProcess(
  lpApplicationName: LPCWSTR?,
  lpCommandLine: LPWSTR?,
  lpProcessAttributes: LPSECURITY_ATTRIBUTES?,
  lpThreadAttributes: LPSECURITY_ATTRIBUTES?,
  bInheritHandles: WINBOOL,
  dwCreationFlags: DWORD,
  lpEnvironment: LPVOID?,
  lpCurrentDirectory: LPCWSTR?,
  lpStartupInfo: LPSTARTUPINFOW?,
  lpProcessInformation: LPPROCESS_INFORMATION?,
) = CreateProcess!!(
  lpApplicationName,
  lpCommandLine,
  lpProcessAttributes,
  lpThreadAttributes,
  bInheritHandles,
  dwCreationFlags,
  lpEnvironment,
  lpCurrentDirectory,
  lpStartupInfo,
  lpProcessInformation,
)

@OptIn(ExperimentalForeignApi::class)
inline fun <R> processHandler(pid: Long, func: (HANDLE?) -> R): R {
  val handler = OpenProcess((PROCESS_QUERY_INFORMATION or PROCESS_VM_READ).convert(), FALSE, pid.convert()) ?: TODO()
  try {
    return func(handler)
  } finally {
    CloseHandle(handler)
  }
}

@OptIn(ExperimentalForeignApi::class)
class WinProcess(val processStarter: MingwProcessStarter) : Process {

  override var pid: Long = 0

  override val stdout
    get() = processStarter.io.stdout
  override val stderr
    get() = processStarter.io.stderr
  override val stdin
    get() = processStarter.io.stdin
  private lateinit var processHandle: HANDLE

  init {
    memScoped {
      val piProcInfo = alloc<PROCESS_INFORMATION>()
      memset(piProcInfo.ptr, 0, sizeOf<PROCESS_INFORMATION>().convert())

      val vv = alloc<STARTUPINFO>()
      vv.cb = sizeOf<STARTUPINFO>().convert()
      vv.hStdError = processStarter.io.stderr.handler
      vv.hStdOutput = processStarter.io.stdout.handler
      vv.hStdInput = processStarter.io.stdin.handler

      vv.dwFlags = STARTF_USESTDHANDLES.convert()
      val envList = processStarter.env.map { "${it.key}=${it.value}" }

      val memCount = envList.sumOf { it.length + 1 } + 1
      val mem = allocArray<UShortVar>(memCount)
      var p = 0
      envList.forEach { s ->
        memcpy(mem + p, s.wcstr.ptr, (s.length * 2 + 2).convert())
        p += s.length + 1
      }

      mem[p + 1] = 0.toUShort()

      val bSuccess = CreateProcess(
        null,
        "\"${processStarter.exe}\" ${
          processStarter.args.map { "\"$it\"" }.joinToString(" ")
        }".wcstr.ptr, // command line
        null, // process security attributes
        null, // primary thread security attributes
        TRUE, // handles are inherited
        CREATE_UNICODE_ENVIRONMENT.convert(), // creation flags
        mem, // use parent's environment
        processStarter.workDir?.wcstr?.ptr, // use parent's current directory
        vv.ptr, // STARTUPINFO pointer
        piProcInfo.ptr,
      ) > 0

      if (!bSuccess) {
        TODO("CreateProcessA error: ${GetLastError()}")
      }

      pid = piProcInfo.dwProcessId.toLong()
      processHandle = piProcInfo.hProcess!!
      stdout.processHandle = processHandle
      stderr.processHandle = processHandle
      CloseHandle(piProcInfo.hThread)
    }
  }

  override fun join() {
    if (!isActive) {
      return
    }
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
        if (r == FALSE) {
          TODO("GetExitCodeProcess error ${GetLastError()}")
        }
        if (ex.value == STILL_ACTIVE) {
          throw Process.ProcessStillActive()
        }
        return ex.value.toInt()
      }
    }

  override fun close() {
    stdout.close()
    stderr.close()
    stdin.close()

    TerminateProcess(processHandle, 1.convert())

    CloseHandle(processHandle)
  }
}
