package pw.binom.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import java.net.URI
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

const val BINOM_REPO_URL = "binom.repo.url"
const val BINOM_REPO_USER = "binom.repo.user"
const val BINOM_REPO_PASSWORD = "binom.repo.password"

class BinomPublishPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val kotlin = target.extensions.findByName("kotlin")!! as KotlinMultiplatformExtension
        if (!target.hasProperty(BINOM_REPO_URL)) {
            return
        }
        if (!target.hasProperty(BINOM_REPO_USER)) {
            return
        }
        if (!target.hasProperty(BINOM_REPO_PASSWORD)) {
            return
        }

        target.apply {
            val conf = it.plugin("maven-publish")
        }

        val publishing = target.extensions.findByName("publishing") as PublishingExtension
        publishing.repositories {
            it.maven {
                it.name = "BinomRepository"
                it.url = URI(target.property(BINOM_REPO_URL) as String)
                it.credentials {
                    it.username = target.property(BINOM_REPO_USER) as String
                    it.password = target.property(BINOM_REPO_PASSWORD) as String
                }
            }
        }
        publishing.publications.withType(MavenPublication::class.java){
            println("PUBLISH->${it.artifactId} :: ${it.name} :: $it")
            it.pom {
                it.scm {
                    it.connection.set("https://github.com/caffeine-mgn/pw.binom.io.git")
                    it.url.set("https://github.com/caffeine-mgn/pw.binom.io")
                }
                it.developers {
                    it.developer {
                        it.id.set("subochev")
                        it.name.set("Anton Subochev")
                        it.email.set("caffeine.mgn@gmail.com")
                    }
                }
                it.licenses {
                    it.license {
                        it.name.set("The Apache License, Version 2.0")
                        it.url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
    }

}