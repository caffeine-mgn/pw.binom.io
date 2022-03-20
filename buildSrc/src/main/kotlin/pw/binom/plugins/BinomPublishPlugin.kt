package pw.binom.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.plugins.signing.SigningExtension
import java.net.URI
import java.util.logging.Logger

const val BINOM_REPO_URL = "binom.repo.url"
const val BINOM_REPO_USER = "binom.repo.user"
const val BINOM_REPO_PASSWORD = "binom.repo.password"

private fun Project.propertyOrNull(property: String) =
    if (hasProperty(property)) property(property) as String else null

class BinomPublishPlugin : Plugin<Project> {
    private val logger = Logger.getLogger(this::class.java.name)
    override fun apply(target: Project) {

        val gpgKeyId = target.propertyOrNull("binom.gpg.key_id")
        val gpgPassword = target.propertyOrNull("binom.gpg.password")
        val gpgPrivateKey = target.propertyOrNull("binom.gpg.private_key")?.replace("|", "\n")
        val centralUserName = target.propertyOrNull("binom.central.username")
        val centralPassword = target.propertyOrNull("binom.central.password")
        val signApply = gpgKeyId != null && gpgPassword != null && gpgPrivateKey != null
        if (signApply) {
            target.apply {
//                it.plugin("org.gradle.signing")
                it.plugin("signing")
            }
        }
        if (!target.hasProperty(BINOM_REPO_URL)) {
            logger.warning("Property [$BINOM_REPO_URL] not found publication plugin will not apply")
            return
        }
        if (!target.hasProperty(BINOM_REPO_USER)) {
            logger.warning("Property [$BINOM_REPO_USER] not found publication plugin will not apply")
            return
        }
        if (!target.hasProperty(BINOM_REPO_PASSWORD)) {
            logger.warning("Property [$BINOM_REPO_PASSWORD] not found publication plugin will not apply")
            return
        }

        target.apply {
            it.plugin("maven-publish")
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
            if (centralUserName != null && centralPassword != null) {
                it.maven {
                    it.name = "Central"
                    val url = if ((target.version as String).endsWith("-SNAPSHOT"))
                        "https://s01.oss.sonatype.org/content/repositories/snapshots"
                    else
                        "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2"
                    it.url = URI(url)
                    it.credentials {
                        it.username = centralUserName
                        it.password = centralPassword
                    }
                }
            }
        }

        publishing.publications.withType(MavenPublication::class.java) {
            it.pom {
                it.scm {
                    it.connection.set(PublishInfo.GIT_PATH_TO_PROJECT)
                    it.url.set(PublishInfo.HTTP_PATH_TO_PROJECT)
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
        if (signApply) {
            target.extensions.configure(SigningExtension::class.java) {
                it.useInMemoryPgpKeys(gpgKeyId, gpgPrivateKey, gpgPassword)
                it.sign(publishing.publications)
                it.setRequired(target.tasks.filterIsInstance<PublishToMavenRepository>())
            }
        } else {
            logger.warning("gpg configuration missing. Jar will be publish without sign")
        }
    }
}
