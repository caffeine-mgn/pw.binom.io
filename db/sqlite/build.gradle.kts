import org.jetbrains.kotlin.konan.target.KonanTarget
import pw.binom.baseStaticLibConfig
import pw.binom.kotlin.clang.*

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

fun args(target: org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget) =
    listOf(
        "-include-binary", file("${buildDir}/native/${target.konanTarget.name}/libsqlite3.a").absolutePath
    )

val sqlitePackageName = "platform.internal_sqlite"
kotlin {
    jvm()
    linuxX64()
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp()
    }
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64()
    }
    macosX64()
    baseStaticLibConfig()
    eachNative {
        val headersPath = file("${buildFile.parentFile}/src/native")
        val sqliteStaticTask = clangBuildStatic(name = "sqlite3", target = this.konanTarget) {
            include(headersPath)
            compileArgs(
                "-DSQLITE_ENABLE_FTS3",
                "-DSQLITE_ENABLE_FTS4",
                "-DSQLITE_ENABLE_FTS5",
                "-DSQLITE_ENABLE_RTREE",
                "-DSQLITE_ENABLE_DBSTAT_VTAB",
                "-DSQLITE_ENABLE_JSON1",
                "-DSQLITE_ENABLE_RBU",
                "-DSQLITE_THREADSAFE=1",
                "-DSQLITE_ENABLE_EXPLAIN_COMMENTS",
                "-DSQLITE_ENABLE_COLUMN_METADATA=1"
            )
            compileFile(
                file("${buildFile.parentFile}/src/native/sqlite3.c")
            )
        }
        tasks.findByName(compileTaskName)?.dependsOn(sqliteStaticTask)
        binaries {
            compilations["main"].cinterops {
                create("sqlite") {
                    defFile = project.file("src/nativeInterop/nativeSqlite3.def")
                    packageName = sqlitePackageName
                    includeDirs.headerFilterOnly(headersPath.absolutePath)
                }
            }
            val args = listOf("-include-binary", sqliteStaticTask.staticFile.asFile.get().absolutePath)
            compilations["main"].kotlinOptions.freeCompilerArgs = args
            compilations["test"].kotlinOptions.freeCompilerArgs = args
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(project(":core"))
                api(project(":db"))
                api(project(":file"))
                api(project(":concurrency"))
            }
        }

        val linuxX64Main by getting {
            dependsOn(commonMain)
        }
        if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
            val linuxArm64Main by getting {
                dependsOn(commonMain)
            }
        }
        if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
            val linuxArm32HfpMain by getting {
                dependsOn(commonMain)
                kotlin.srcDir("src/linuxX64Main/kotlin")
            }
        }

        val mingwX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(commonMain)
                kotlin.srcDir("src/linuxX64Main/kotlin")
            }
        }

        val macosX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api(project(":network"))
            }
        }
        val jvmMain by getting {
            dependencies {
                api("org.xerial:sqlite-jdbc:3.34.0")
            }
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test-junit"))
            }
        }
        val linuxX64Test by getting {
            dependsOn(commonTest)
        }
    }
}

apply<pw.binom.plugins.DocsPlugin>()