package wtf.log.lederhosen.common.util

import com.facebook.rebound.BaseSpringSystem
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringListener
import com.facebook.rebound.SpringLooper
import rx.Emitter
import rx.Observable

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 12/28/16
 * (C) 2016
 */

/**
 * Simple implementation of a [SpringLooper] using a new thread.
 */
class ThreadedSpringLooper : SpringLooper() {

  private class LooperThread(val system: BaseSpringSystem) : Thread() {

    private var lastTime: Long = 0L

    @Volatile var stopped: Boolean = false

    override fun run() {
      lastTime = System.nanoTime()
      while (!stopped) {
        val currentTime = System.nanoTime()
        val timeElapsed = (currentTime - lastTime) / 1000000
        system.loop(timeElapsed.toDouble())
        lastTime = currentTime
      }
      interrupt()
    }

  }

  private var thread: LooperThread? = null

  override fun start() {
    stop()
    thread = LooperThread(mSpringSystem).apply { start() }
  }

  override fun stop() {
    thread?.apply {
      if (!stopped) {
        stopped = true
        join()
      }
    }
    thread = null
  }

}

/**
 * Overrides members of [SpringListener] with correct nullability and default implementations.
 */
interface KtSpringListener : SpringListener {

  override fun onSpringUpdate(spring: Spring) {
  }

  override fun onSpringEndStateChange(spring: Spring) {
  }

  override fun onSpringAtRest(spring: Spring) {
  }

  override fun onSpringActivate(spring: Spring) {
  }

}

/**
 * Spring events for reactive callbacks
 */
sealed class SpringEvent {

  /**
   * The [Spring] emitting the event. This is an abstract val so that we can leverage data classes in the subtypes.
   */
  abstract val spring: Spring

  open operator fun component1(): Spring = spring

  data class EndStateChange(override val spring: Spring): SpringEvent()
  data class Activate(override val spring: Spring): SpringEvent()
  data class Update(override val spring: Spring): SpringEvent()
  data class AtRest(override val spring: Spring): SpringEvent()

}

/**
 * [SpringListener] for reactive stuff
 */
private class RxSpringListener(private val emitter: Emitter<in SpringEvent>) : KtSpringListener {

  override fun onSpringEndStateChange(spring: Spring) = emitter.onNext(SpringEvent.EndStateChange(spring))

  override fun onSpringActivate(spring: Spring) = emitter.onNext(SpringEvent.Activate(spring))

  override fun onSpringUpdate(spring: Spring) = emitter.onNext(SpringEvent.Update(spring))

  override fun onSpringAtRest(spring: Spring) = emitter.onNext(SpringEvent.AtRest(spring))

}

/**
 * Reactive callbacks for a [Spring]
 */
fun Spring.events(backpressure: Emitter.BackpressureMode = Emitter.BackpressureMode.DROP): Observable<SpringEvent> {
  return Observables.fromEmitter(backpressure) { emitter ->
    val listener = RxSpringListener(emitter)
    addListener(listener)
    emitter.setCancellation {
      removeListener(listener)
    }
  }
}

/**
 * [Spring] factories
 */
object Springs {

  fun newSpringSystem(): Observable<BaseSpringSystem> {
    val looper = ThreadedSpringLooper()
    val system = BaseSpringSystem(looper)
    return Observable.just(system)
        .mergeWith(Observable.never())
        .doOnSubscribe { looper.start() }
        .doOnUnsubscribe { looper.stop() }
  }

  inline fun newSingleSpring(crossinline initializer: Spring.() -> Unit): Observable<SpringEvent> {
    return newSpringSystem()
        .map { system ->
          system.allSprings.forEach(Spring::destroy)
          system.createSpring().apply(initializer)
        }
        .flatMap { it.events() }
  }

}
