import pw.binom.kotlin.clang.clangBuildStatic
import pw.binom.kotlin.clang.compileTaskName
import pw.binom.kotlin.clang.eachNative

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}
apply<pw.binom.KotlinConfigPlugin>()
val sqlitePackageName = "platform.internal_sqlite"
kotlin {
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        android {
            publishAllLibraryVariants()
        }
    }
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
                val sqlite by creating {
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
                api(project(":thread"))
            }
        }

        val commonNative by creating {
            dependencies {
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
                dependsOn(linuxX64Main)
            }
        }

        val mingwX64Main by getting {
            dependsOn(linuxX64Main)
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(linuxX64Main)
            }
        }

        val macosX64Main by getting {
            dependsOn(linuxX64Main)
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api(project(":network"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        val jvmMain by getting {
            dependencies {
                api("org.xerial:sqlite-jdbc:3.36.0.3")
            }
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test"))
            }
        }
        if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
            val androidTest by getting {
                dependsOn(jvmTest)
                dependencies {
                    api("com.android.support.test:runner:0.5")
                }
            }
        }
//        dependsOn("androidNative*Test", linuxX64Main)
        val linuxX64Test by getting {
            dependsOn(commonTest)
        }
    }
}

if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
