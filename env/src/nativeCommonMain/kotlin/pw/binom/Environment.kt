package pw.binom

import kotlin.native.Platform as KPlatform

@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalStdlibApi::class)
actual val Environment.availableProcessors: Int
    get() = KPlatform.getAvailableProcessors()

actual val Environment.os: OS
    get() = internalOs

private val internalOs = when (KPlatform.osFamily) {
    OsFamily.ANDROID -> OS.ANDROID
    OsFamily.IOS -> OS.IOS
    OsFamily.LINUX -> OS.LINUX
    OsFamily.MACOSX -> OS.MACOS
    OsFamily.TVOS -> OS.TVOS
    OsFamily.UNKNOWN -> OS.UNKNOWN
    OsFamily.WASM -> OS.WEB
    OsFamily.WATCHOS -> OS.WATCHOS
    OsFamily.WINDOWS -> OS.WINDOWS
}

private val internalPlatform = when (KPlatform.cpuArchitecture) {
    CpuArchitecture.WASM32 -> Platform.WASM32
    CpuArchitecture.ARM32 -> when (KPlatform.osFamily) {
        OsFamily.WINDOWS -> Platform.MINGW_ARM_32
        OsFamily.LINUX -> Platform.LINUX_ARM_32
        OsFamily.ANDROID -> Platform.ANDROID_ARM_X32
        OsFamily.MACOSX -> Platform.MACOS_ARM_32
        OsFamily.UNKNOWN -> Platform.UNKNOWN
        OsFamily.IOS -> Platform.IOS_ARM_32
        OsFamily.WASM -> Platform.WASM32
        OsFamily.TVOS -> Platform.TVOS_ARM_32
        OsFamily.WATCHOS -> Platform.WATCHOS_ARM_32
    }

    CpuArchitecture.ARM64 -> when (KPlatform.osFamily) {
        OsFamily.WINDOWS -> Platform.MINGW_ARM_64
        OsFamily.LINUX -> Platform.LINUX_ARM_64
        OsFamily.ANDROID -> Platform.ANDROID_ARM_X64
        OsFamily.MACOSX -> Platform.MACOS_ARM_X64
        OsFamily.UNKNOWN -> Platform.UNKNOWN
        OsFamily.IOS -> Platform.IOS_ARM_64
        OsFamily.WASM -> Platform.WASM32
        OsFamily.TVOS -> Platform.TVOS_ARM_64
        OsFamily.WATCHOS -> Platform.WATCHOS_ARM_64
    }

    CpuArchitecture.X64 -> when (KPlatform.osFamily) {
        OsFamily.WINDOWS -> Platform.MINGW_X64
        OsFamily.LINUX -> Platform.LINUX_64
        OsFamily.ANDROID -> Platform.ANDROID_X64
        OsFamily.MACOSX -> Platform.MACOS_X64
        OsFamily.UNKNOWN -> Platform.UNKNOWN
        OsFamily.IOS -> Platform.IOS_X64
        OsFamily.WASM -> Platform.WASM64
        OsFamily.TVOS -> Platform.TVOS_X86
        OsFamily.WATCHOS -> Platform.WATCHOS_X86
    }

    CpuArchitecture.X86 -> when (KPlatform.osFamily) {
        OsFamily.WINDOWS -> Platform.MINGW_X86
        OsFamily.LINUX -> Platform.LINUX_X86
        OsFamily.ANDROID -> Platform.ANDROID_X86
        OsFamily.MACOSX -> Platform.MACOS_X86
        OsFamily.UNKNOWN -> Platform.UNKNOWN
        OsFamily.IOS -> Platform.IOS_X86
        OsFamily.WASM -> Platform.WASM32
        OsFamily.TVOS -> Platform.TVOS_X86
        OsFamily.WATCHOS -> Platform.WATCHOS_X86
    }

    CpuArchitecture.MIPS32 -> Platform.LINUX_MIPS_32
    CpuArchitecture.MIPSEL32 -> Platform.LINUX_MIPSEL_32
    CpuArchitecture.UNKNOWN -> Platform.UNKNOWN
}

actual val Environment.isBigEndian: Boolean
    get() = !KPlatform.isLittleEndian

actual val Environment.platform: Platform
    get() = internalPlatform
