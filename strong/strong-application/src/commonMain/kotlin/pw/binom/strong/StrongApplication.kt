package pw.binom.strong

import kotlinx.coroutines.*
import pw.binom.network.NetworkManager
import pw.binom.strong.properties.BaseStrongProperties
import pw.binom.network.MultiFixedSizeThreadNetworkDispatcher
import pw.binom.Environment
import pw.binom.availableProcessors
import pw.binom.getEnv
import pw.binom.io.file.File
import pw.binom.io.file.readText
import pw.binom.io.file.takeIfFile
import pw.binom.io.file.workDirectoryFile
import pw.binom.io.use
import pw.binom.properties.ini.addIni
import pw.binom.signal.Signal
import pw.binom.strong.properties.StrongProperties
import pw.binom.strong.properties.yaml.addYaml

// import pw.binom.process.Signal

interface StrongApplicationContext {
  val properties: StrongProperties
  val networkManager: NetworkManager
  operator fun Strong.Config.unaryPlus()
}

object StrongApplication {

  private class StrongApplicationContextImpl(
    override val properties: StrongProperties,
    override val networkManager: NetworkManager,
  ) : StrongApplicationContext {
    val configs = ArrayList<Strong.Config>()
    override fun Strong.Config.unaryPlus() {
      configs += this
    }

  }

  @OptIn(DelicateCoroutinesApi::class)
  fun run(args: Array<String>, init: StrongApplicationContext.() -> Unit) {
    val properties = BaseStrongProperties()
      .addEnvironment(prefix = "")
      .addArgs(args)
    val iniConfigPath =
      Environment.getEnv("STRONG_CONFIG_INI")?.let { File(it) } ?: Environment.workDirectoryFile.relative("config.ini")
    val yamlConfigPath =
      Environment.getEnv("STRONG_CONFIG_YAML")?.let { File(it) }
        ?: Environment.workDirectoryFile.relative("config.yaml")

    iniConfigPath.takeIfFile()?.also {
      properties.addYaml(it.readText())
    }
    yamlConfigPath.takeIfFile()?.also {
      properties.addIni(it.readText())
    }
    var strong: Strong? = null
    MultiFixedSizeThreadNetworkDispatcher(Environment.availableProcessors).use { networkManager ->
      val configs = StrongApplicationContextImpl(
        properties = properties,
        networkManager = networkManager,
      )
      init(configs)
      Signal.handler {
        if (it.isInterrupted) {
          GlobalScope.launch(networkManager) {
            strong?.destroy()
          }
        }
      }
      runBlocking {
        configs.configs += Strong.config {
          it.bean { networkManager }
          it.bean { properties }
        }
        val newStrong = Strong.create(
          *configs.configs.toTypedArray()
        )
        strong = newStrong
        newStrong.awaitDestroy()
      }
    }
  }
}
