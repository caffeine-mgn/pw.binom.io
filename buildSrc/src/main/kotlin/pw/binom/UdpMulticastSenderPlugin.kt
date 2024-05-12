package pw.binom

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.StandardSocketOptions
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import kotlin.time.Duration

class UdpMulticastSenderPlugin : Plugin<Project> {
  companion object {
    internal val sendThread = ArrayList<Thread>()

    fun add(record: UdpMulticastSenderExtension.Record) {
      val t = Thread {
        val datagramChannel = DatagramChannel.open()
        datagramChannel.bind(null)
        val networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName(record.networkHost))
        datagramChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface)
        val data = ByteBuffer.wrap(record.data.encodeToByteArray())
        val inetSocketAddress = InetSocketAddress(record.ip, record.port)
        while (!Thread.currentThread().isInterrupted) {
          data.clear()
          println("Sending \"$data\" to ${record.ip}:${record.port}")
          datagramChannel.send(data, inetSocketAddress)
          try {
            Thread.sleep(record.interval.inWholeMilliseconds)
          } catch (e: InterruptedException) {
            break
          }
        }
      }
      t.isDaemon = true
      t.start()
    }

    internal fun stop() {
      sendThread.forEach {
        it.interrupt()
      }
      sendThread.forEach {
        it.join()
      }
      sendThread.clear()
    }
  }

  abstract class UdpMulticastStartSendTasks() : DefaultTask() {
    private val ext: UdpMulticastSenderExtension = project.extensions.getByType(UdpMulticastSenderExtension::class.java)

    @TaskAction
    fun execute() {
      ext.publications.forEach {
        UdpMulticastSenderPlugin.add(it)
      }
    }
  }

  abstract class UdpMulticastStopSendTasks() : DefaultTask() {
    private val ext: UdpMulticastSenderExtension = project.extensions.getByType(UdpMulticastSenderExtension::class.java)

    @TaskAction
    fun execute() {
      UdpMulticastSenderPlugin.stop()
    }
  }

  override fun apply(target: Project) {
    target.extensions.create("udpMulticast", UdpMulticastSenderExtension::class.java)
    target.tasks.register("startSendUdpMulicast", UdpMulticastStartSendTasks::class.java)
    target.tasks.register("stopSendUdpMulicast", UdpMulticastStopSendTasks::class.java)
  }

  abstract class UdpMulticastSenderExtension {
    data class Record(
      val networkHost: String,
      val ip: String,
      val port: Int,
      val data: String,
      val interval: Duration,
    )

    private val records = ArrayList<Record>()
    val publications: List<Record>
      get() = records

    fun define(networkHost: String, ip: String, port: Int, interval: Duration, data: String) {
      records += Record(
        networkHost = networkHost,
        ip = ip,
        port = port,
        data = data,
        interval = interval,
      )
    }
  }
}
