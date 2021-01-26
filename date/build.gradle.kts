import java.util.TimeZone
import java.util.Date

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
    jvm()

    linuxX64 { // Use your target instead.
        binaries {
            staticLib()
        }
    }

    linuxArm32Hfp {
        binaries {
            staticLib()
        }
    }

    linuxArm64 {
        binaries {
            staticLib()
        }
    }

    linuxMips32 {
        binaries {
            staticLib()
        }
    }

    linuxMipsel32 {
        binaries {
            staticLib()
        }
    }

    mingwX64 { // Use your target instead.
        binaries {
            staticLib()
        }
    }

    mingwX86 { // Use your target instead.
        binaries {
            staticLib()
        }
    }

    macosX64 {
        binaries {
            framework()
        }
    }
    js(BOTH) {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
        }

        val linuxX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }
        val linuxArm64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }
        val linuxArm32HfpMain by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val linuxMips32Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val linuxMipsel32Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val mingwX64Main by getting {
            dependsOn(commonMain)
        }
        val mingwX86Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/mingwX64Main/kotlin")
        }

        val macosX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api(project(":concurrency"))
                api(project(":file"))
                api(project(":env"))
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

tasks {
    fun prepareTime() {
        project.buildDir.resolve("tmp-date").resolve("currentTZ")
            .also {
                it.parentFile.mkdirs()
                it.writeText(TimeZone.getDefault().rawOffset.let { it / 1000 / 60 }.toString())
            }
        project.buildDir.resolve("tmp-date").resolve("now")
            .also {
                it.parentFile.mkdirs()
                it.writeText(Date().time.toString())
            }
    }

    val mingwX64Test by getting {
        doFirst {
            prepareTime()
        }
    }
    val linuxX64Test by getting {
        doFirst {
            prepareTime()
        }
    }
    val jvmTest by getting {
        doFirst {
            prepareTime()
        }
    }

    val macosX64Test by getting {
        doFirst {
            prepareTime()
        }
    }
}