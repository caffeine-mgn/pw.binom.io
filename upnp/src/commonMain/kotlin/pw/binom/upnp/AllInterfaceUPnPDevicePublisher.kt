package pw.binom.upnp

import kotlinx.coroutines.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.socket.NetworkInterface
import pw.binom.network.NetworkManager
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class AllInterfaceUPnPDevicePublisher(
  val networkManager: NetworkManager,
  val context: CoroutineContext,
  val checkInterval: Duration = 1.minutes,
  val source: UPnPDevicePublisher.Source,
) : UPnPDevicePublisher {
  private val lock = SpinLock()
  private val devices = HashMap<String, UPnPDevicePublisherImpl>()
  private val refreshing = AtomicBoolean(false)

  @OptIn(DelicateCoroutinesApi::class)
  private val job = GlobalScope.launch(context) {
    while (isActive) {
      try {
        println("Refreshing...")
        refresh()
        println("Refresh OK")
        delay(checkInterval)
      } catch (e: CancellationException) {
        break
      }
    }
  }

  suspend fun refresh() {
    if (!refreshing.compareAndSet(false, true)) {
      return
    }
    try {
      var forCreate: ArrayList<NetworkInterface>? = null
      var forRemove: HashSet<String>? = null
      val interfaces = NetworkInterface.getAvailable().associateBy { "${it.name}-${it.ip}" }
      lock.synchronize {
        interfaces.forEach { (key, netIf) ->
          if (key !in devices) {
            forCreate = forCreate ?: ArrayList()
            forCreate!!.add(netIf)
          }
        }
        devices.keys.forEach {
          if (!interfaces.containsKey(it)) {
            forRemove = forRemove ?: HashSet()
            forRemove!!.add(it)
          }
        }
      }
      println("forCreate=$forCreate")
      println("forRemove=$forRemove")
      forCreate?.forEach {
        val publicator = UPnPDevicePublisherImpl(
          nm = networkManager,
          networkInterface = it,
          context = context,
          source = source,
        )
        lock.synchronize {
          devices["${it.name}-${it.ip}"] = publicator
        }
      }
      forRemove?.forEach {
        lock.synchronize {
          devices.remove(it)?.asyncCloseAnyway()
        }
      }
    } finally {
      refreshing.setValue(false)
    }
  }

  override suspend fun asyncClose() {
    job.cancelAndJoin()
    lock.synchronize {
      devices.values.forEach {
        it.asyncCloseAnyway()
      }
      devices.clear()
    }
  }
}
