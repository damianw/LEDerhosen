package wtf.log.lederhosen.server.util

import com.illposed.osc.AddressSelector
import com.illposed.osc.OSCListener
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCPortIn
import com.illposed.osc.utility.OSCPatternAddressSelector
import com.jakewharton.rxrelay.BehaviorRelay
import rx.Emitter
import rx.Observable
import rx.Subscription
import wtf.log.lederhosen.common.util.Observables
import wtf.log.lederhosen.common.util.getValue
import wtf.log.lederhosen.common.util.setValue
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 12/28/16
 * (C) 2016
 *
 * OSC Utilities and DSL Experimentation
 */

/**
 * OSC server factories using RxJava
 */
object Osc {

  /**
   * Returns an [Observable] which starts an OSC server upon subscription.
   */
  fun startServer(
      port: Int,
      charset: Charset = Charset.defaultCharset()
  ): Observable<OSCPortIn> = Observables.fromEmitter(Emitter.BackpressureMode.BUFFER) { emitter ->
    val osc = OSCPortIn(port, charset).apply(OSCPortIn::startListening)
    emitter.setCancellation {
      osc.stopListening()
      osc.close()
    }
    emitter.onNext(osc)
  }
  /**
   * Starts an OSC server which can be stopped by unsubscribing from the returned [Subscription].
   * Listeners can be added in [addListeners].
   */
  fun startServer(
      port: Int,
      charset: Charset = Charset.defaultCharset(),
      addListeners: OscBuilder.() -> Unit
  ): Subscription {
    val stopEvent = BehaviorRelay.create<Unit>()
    return startServer(port, charset)
        .doOnNext { OscBuilder(it, stopEvent).addListeners() }
        .doOnUnsubscribe { stopEvent.call(Unit) }
        .subscribe()
  }

}

/**
 * DSL-like builder for OSC servers.
 */
class OscBuilder(private val port: OSCPortIn, val stopEvent: Observable<Unit>) {

  fun events(addressSelector: String): Observable<OscEvent> = port.events(addressSelector).takeUntil(stopEvent)

  fun events(addressSelector: AddressSelector): Observable<OscEvent> = port.events(addressSelector).takeUntil(stopEvent)

}

/**
 * Adds a reactive listener for an address selector.
 */
fun OSCPortIn.events(addressSelector: String) = events(OSCPatternAddressSelector(addressSelector))

/**
 * Adds a reactive listener for an address selector.
 */
fun OSCPortIn.events(addressSelector: AddressSelector) = Observables.fromEmitter<OscEvent>(Emitter.BackpressureMode.BUFFER) { emitter ->
  val selector = ToggleableOscAddressSelector(addressSelector)
  addListener(selector, OSCListener { time, message ->
    emitter.onNext(OscEvent.from(time, message))
  })
  emitter.setCancellation {
    selector.enabled = false
  }
}

/**
 * Address selector which can be disabled, since the OSC library does not support removing listeners.
 */
private class ToggleableOscAddressSelector(private val source: AddressSelector) : AddressSelector {

  /**
   * Whether this selector should be enabled.
   */
  var enabled: Boolean by AtomicBoolean(true)

  override fun matches(messageAddress: String): Boolean {
    return if (enabled) source.matches(messageAddress) else false
  }

}

/**
 * A nicer API for OSC messages
 */
@Suppress("ArrayInDataClass")
data class OscEvent(
    val address: String,
    val arguments: List<Any?>,
    val byteArray: ByteArray,
    val charset: Charset,
    val time: Date?
) {

  val pathComponents: List<String> = address.split("/")

  companion object {

    fun from(time: Date?, message: OSCMessage) = OscEvent(
        address = message.address,
        arguments = message.arguments,
        byteArray = message.byteArray,
        charset = message.charset,
        time = time
    )

  }

}

