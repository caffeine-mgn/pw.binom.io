package pw.binom.plugins

import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

private val konanUserDir = File(System.getenv("KONAN_DATA_DIR") ?: "${System.getProperty("user.home")}/.konan")
val konanDeps = konanUserDir.resolve("dependencies")
val toolChainFolderName = when {
    HostManager.hostIsLinux -> "clang-llvm-8.0.0-linux-x86-64"
    HostManager.hostIsMac -> "clang-llvm-apple-8.0.0-darwin-macos"
    HostManager.hostIsMingw -> "msys2-mingw-w64-x86_64-clang-llvm-lld-compiler_rt-8.0.1"
    else -> error("Unknown host OS")
}
private val androidSysRootParent = konanDeps.resolve("target-sysroot-1-android_ndk").resolve("android-21")
val llvmBinFolder = konanDeps.resolve("${toolChainFolderName}/bin")

data class TargetInfo(val targetName: String, val sysRoot: File, val clangArgs: List<String> = emptyList())

val targetInfoMap = mapOf(
        KonanTarget.LINUX_X64 to TargetInfo(
                targetName = "x86_64-unknown-linux-gnu",
                sysRoot = konanDeps.resolve("target-gcc-toolchain-3-linux-x86-64/x86_64-unknown-linux-gnu/sysroot")
        ),
        KonanTarget.MACOS_X64 to TargetInfo(
                targetName = "x86_64-apple-macosx",
                sysRoot = konanDeps.resolve("target-sysroot-10-macos_x64"),
                clangArgs = listOf("-march=x86-64")
        ),
        KonanTarget.MINGW_X64 to TargetInfo(
                targetName = "x86_64-w64-mingw32",
                sysRoot = konanDeps.resolve("msys2-mingw-w64-x86_64-clang-llvm-lld-compiler_rt-8.0.1")
        ),
        KonanTarget.MINGW_X86 to TargetInfo(
                targetName = "i686-w64-mingw32",
                sysRoot = konanDeps.resolve("msys2-mingw-w64-i686-clang-llvm-lld-compiler_rt-8.0.1")
        ),
        KonanTarget.LINUX_MIPSEL32 to TargetInfo(
                targetName = "mipsel-unknown-linux-gnu",
                sysRoot = konanDeps.resolve("target-sysroot-2-mipsel"),
                clangArgs = listOf("-mfpu=vfp", "-mfloat-abi=hard")
        ),
        KonanTarget.LINUX_ARM32_HFP to TargetInfo(
                targetName = "armv6-unknown-linux-gnueabihf",
                sysRoot = konanDeps.resolve("target-sysroot-2-raspberrypi"),
                clangArgs = listOf("-mfpu=vfp", "-mfloat-abi=hard")
        ),
        KonanTarget.ANDROID_ARM32 to TargetInfo(
                targetName = "arm-linux-androideabi",
                sysRoot = androidSysRootParent.resolve("arch-arm")
        ),
        KonanTarget.ANDROID_ARM64 to TargetInfo(
                targetName = "aarch64-linux-android",
                sysRoot = androidSysRootParent.resolve("arch-arm64")
        ),
        KonanTarget.ANDROID_X86 to TargetInfo(
                targetName = "i686-linux-android",
                sysRoot = androidSysRootParent.resolve("arch-x86")
        ),
        KonanTarget.ANDROID_X64 to TargetInfo(
                targetName = "x86_64-linux-android",
                sysRoot = androidSysRootParent.resolve("arch-x64")
        )
)