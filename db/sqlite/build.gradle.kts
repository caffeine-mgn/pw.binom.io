import org.jetbrains.kotlin.konan.target.KonanTarget
import pw.binom.plugins.BuildStaticTask
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
    linuxX64 { // Use your target instead.
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("sqlite") {
                    defFile = project.file("src/nativeInterop/nativeSqlite3.def")
                    packageName = sqlitePackageName
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/native")
                }
            }
            compilations["main"].kotlinOptions.freeCompilerArgs = args(target)
            compilations["test"].kotlinOptions.freeCompilerArgs = args(target)
        }
    }
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    linuxArm32Hfp {
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("sqlite") {
                    defFile = project.file("src/nativeInterop/nativeSqlite3.def")
                    packageName = sqlitePackageName
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/native")
                }
            }
            compilations["main"].kotlinOptions.freeCompilerArgs = args(target)
            compilations["test"].kotlinOptions.freeCompilerArgs = args(target)
        }
    }

    mingwX64 { // Use your target instead.
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("sqlite") {
                    defFile = project.file("src/nativeInterop/nativeSqlite3.def")
                    packageName = sqlitePackageName
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/native")
                }
            }
            compilations["main"].kotlinOptions.freeCompilerArgs = args(target)
            compilations["test"].kotlinOptions.freeCompilerArgs = args(target)
        }
    }

    mingwX86 { // Use your target instead.
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("sqlite") {
                    defFile = project.file("src/nativeInterop/nativeSqlite3.def")
                    packageName = sqlitePackageName
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/native")
                }
            }
            compilations["main"].kotlinOptions.freeCompilerArgs = args(target)
            compilations["test"].kotlinOptions.freeCompilerArgs = args(target)
        }
    }

//    linuxArm64 {
//        binaries {
//            staticLib {
//            }
//        }
//    }
    macosX64 {
        binaries {
            framework {
            }
            compilations["main"].cinterops {
                create("sqlite") {
                    defFile = project.file("src/nativeInterop/nativeSqlite3.def")
                    packageName = sqlitePackageName
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/native")
                }
            }
            compilations["main"].kotlinOptions.freeCompilerArgs = args(target)
            compilations["test"].kotlinOptions.freeCompilerArgs = args(target)
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
        val linuxArm32HfpMain by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val mingwX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }
        val mingwX86Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val macosX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
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

fun defineBuild(selectTarget: KonanTarget):BuildStaticTask {
    val task = tasks.create("buildSqlite${selectTarget.name.capitalize()}", BuildStaticTask::class.java)
    task.target = selectTarget
    task.include(file("${buildFile.parentFile}/src/native"))
    task.compileArgs(
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
    task.compileFile(
        file("${buildFile.parentFile}/src/native/sqlite3.c")
    )
    task.staticFile = file("${buildDir}/native/${selectTarget.name}/libsqlite3.a")
    return task
}

val mingwX86Compile = defineBuild(KonanTarget.MINGW_X86)
val mingwX64Compile = defineBuild(KonanTarget.MINGW_X64)
val linuxX64Compile = defineBuild(KonanTarget.LINUX_X64)
val linuxArm64Compile = defineBuild(KonanTarget.LINUX_ARM64)
val linuxArm32HfpCompile = defineBuild(KonanTarget.LINUX_ARM32_HFP)
val macosX64Compile = defineBuild(KonanTarget.MACOS_X64)

tasks["compileKotlinLinuxX64"].dependsOn(linuxX64Compile)
tasks["compileKotlinMingwX64"].dependsOn(mingwX64Compile)
tasks["compileKotlinMingwX86"].dependsOn(mingwX86Compile)
tasks["compileKotlinLinuxArm32Hfp"].dependsOn(linuxArm32HfpCompile)
tasks["compileKotlinMacosX64"].dependsOn(macosX64Compile)
apply<pw.binom.plugins.DocsPlugin>()