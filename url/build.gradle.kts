import pw.binom.publish.useDefault

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}

apply<pw.binom.KotlinConfigPlugin>()
kotlin {
    allTargets()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
            }
        }
        useDefault()
        val runnableMain by creating {
            dependsOn(commonMain)
        }

        val jvmLikeMain by getting {
            dependsOn(commonMain)
            dependsOn(runnableMain)
        }
        val jvmMain by getting {
            dependsOn(jvmLikeMain)
        }
        val nativeCommonMain by getting {
            dependsOn(commonMain)
            dependsOn(runnableMain)
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
            }
        }
    }
}

tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
