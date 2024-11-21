package pw.binom.io.socket

import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import java.lang.management.ManagementFactory
import java.util.*
import javax.management.*


interface SelectorMBean : DynamicMBean {
  val selectorCount: Int
  val selectorStatus: String
  fun wakeUp(selectorId: Int): Boolean
}

class SelectorMBeanImpl : SelectorMBean {
  override val selectorCount: Int
    get() = selectorMapLock.synchronize {
      selectorMap.size
    }
  override val selectorStatus: String
    get() =
      selectorMapLock.synchronize {
        selectorMap.keys.asSequence().map {
          "${System.identityHashCode(it.native)} listenFlags=${it.listenFlags}, isClosed=${it.isClosed}, attachment=${it.attachment}"
        }.joinToString("\n")
      }

  override fun wakeUp(selectorId: Int): Boolean {
    val selector = selectorMapLock.synchronize {
      selectorMap.keys.find { System.identityHashCode(it.native) == selectorId } ?: return false
    }
    selector.selector.wakeup()
    return true
  }

  override fun getAttribute(attribute: String?): Any {
    TODO("Not yet implemented")
  }

  override fun setAttribute(attribute: Attribute?) {
    TODO("Not yet implemented")
  }

  override fun getAttributes(attributes: Array<out String>?): AttributeList {
    AttributeList().add(Attribute("", null))
    TODO("Not yet implemented")
  }

  override fun setAttributes(attributes: AttributeList?): AttributeList {
    TODO("Not yet implemented")
  }

  override fun invoke(actionName: String?, params: Array<out Any>?, signature: Array<out String>?): Any {
    TODO("Not yet implemented")
  }

  override fun getMBeanInfo(): MBeanInfo {
    TODO("Not yet implemented")
  }

  companion object {

    internal val selectorMap = WeakHashMap<SelectorKey, Boolean>()
    internal val selectorMapLock = SpinLock()

//    init {
//      try {
//        val objectName = ObjectName("pw.binom.io.socket:type=basic,name=selectors")
//        val server = ManagementFactory.getPlatformMBeanServer()
//        server.registerMBean(SelectorMBeanImpl(), objectName)
//      } catch (e: MalformedObjectNameException) {
//        println("Can't registered MBean:\n${e.stackTraceToString()}")
//        // handle exceptions
//      } catch (e: InstanceAlreadyExistsException) {
//        println("Can't registered MBean:\n${e.stackTraceToString()}")
//      } catch (e: MBeanRegistrationException) {
//        println("Can't registered MBean:\n${e.stackTraceToString()}")
//      } catch (e: NotCompliantMBeanException) {
//        println("Can't registered MBean:\n${e.stackTraceToString()}")
//      }
//    }
  }
}
