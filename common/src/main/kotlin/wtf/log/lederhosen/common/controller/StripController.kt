package wtf.log.lederhosen.common.controller

import com.facebook.rebound.SpringConfig
import javafx.scene.paint.Color
import rx.Emitter
import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.schedulers.Schedulers
import rx.subscriptions.BooleanSubscription
import wtf.log.lederhosen.common.util.*
import wtf.log.lederhosen.driver.LightStrip
import wtf.log.lederhosen.driver.util.deriveWith
import java.io.Closeable
import java.util.concurrent.Executors

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 12/27/16
 * (C) 2016
 */
class StripController(val lightStrip: LightStrip) : Closeable {

  private val size = lightStrip.size

  private val lastIndex = lightStrip.lastIndex

  private val lightScheduler: Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())

  private var animationSubscription: Subscription? = null

  var isClosed: Boolean = false
    private set

  var primaryColor: Color = Color.BLUE

  fun stopAnimation() {
    animationSubscription?.unsubscribe()
    lightStrip.reset()
    animationSubscription = null
  }

  fun playAnimation(commands: Observable<LightStrip.Command>) {
    stopAnimation()
    animationSubscription = commands.subscribe(lightStrip::execute)
  }

  private inline fun choreograph(crossinline draw: (Emitter<LightStrip.Command>, timeElapsed: Long) -> Unit): Observable<LightStrip.Command> {
    return Observables.fromEmitter<LightStrip.Command>(Emitter.BackpressureMode.DROP) { emitter ->
      val subscription = BooleanSubscription()
      emitter.setSubscription(subscription)
      val startTime = System.nanoTime()
      while (!subscription.isUnsubscribed) {
        val currentTime = System.nanoTime()
        val timeElapsed = (currentTime - startTime) / 1000000
        draw(emitter, timeElapsed)
      }
    }.subscribeOn(lightScheduler)
  }

  private inline fun choreographRepeating(duration: Long, crossinline draw: (Emitter<LightStrip.Command>, fraction: Float) -> Unit): Observable<LightStrip.Command> {
    return choreograph { emitter, timeElapsed ->
      draw(emitter, (timeElapsed % duration) / duration.toFloat())
    }
  }

  fun rainbowAnimation(duration: Long = 1000, length: Int = 30): Observable<LightStrip.Command> = choreographRepeating(duration) { emitter, fraction ->
    val colors = ArrayList<Color>(size)
    repeat(length) { pixel ->
      val pixelFraction = (fraction + (pixel.toFloat() / length)) % 1f
      val stupidColor = java.awt.Color.getHSBColor(pixelFraction, 1.0f, 1.0f)
      val betterColor = Color.rgb(stupidColor.red, stupidColor.green, stupidColor.blue)
      colors.add(betterColor)
    }
    colors.forEachIndexed { i, color ->
      emitter.onNext(LightStrip.Command.Set(color, *(i until size step length).toList().toIntArray()))
    }
    emitter.onNext(LightStrip.Command.Commit)
  }

  fun bounceAnimation(duration: Long = 10000): Observable<LightStrip.Command> = choreographRepeating(duration) { emitter, fraction ->
    val doubleFraction = fraction * 2f
    val trueFraction = if (doubleFraction > 1f) 1 - doubleFraction + 1 else doubleFraction
    val pixel = (trueFraction * lastIndex).toInt()
    emitter.onNext(LightStrip.Command.Clear)
    emitter.onNext(LightStrip.Command.Set(primaryColor, pixel))
    emitter.onNext(LightStrip.Command.Commit)
  }

  fun springAnimation(haloSize: Int = 8, config: SpringConfig = SpringConfig(20.0, 0.0)): Observable<LightStrip.Command> {
    return Springs
        .newSingleSpring {
          currentValue = 1.0
          endValue = 0.5
          springConfig = config
        }
        .takeUntil { it is SpringEvent.AtRest }
        .filterIsInstance<SpringEvent.Update>()
        .flatMap { (spring) ->
          val commands = ArrayList<LightStrip.Command>().apply {
            ensureCapacity(2 * haloSize + 1)
          }
          val value = spring.currentValue
          val position = (value * size).toInt()
          commands.add(LightStrip.Command.Clear)
          commands.add(LightStrip.Command.Set(primaryColor, position))
          for (offsetLeft in 1..haloSize) {
            val haloPosition = position - offsetLeft
            if (haloPosition < 0) continue
            val brightness = 1 - (offsetLeft.toDouble() / haloSize)
            commands.add(LightStrip.Command.Set(primaryColor.deriveWith(brightnessFactor = brightness), position - offsetLeft))
          }
          for (offsetRight in 1..haloSize) {
            val haloPosition = position + offsetRight
            if (haloPosition > lastIndex) continue
            val brightness = 1 - (offsetRight.toDouble() / haloSize)
            commands.add(LightStrip.Command.Set(primaryColor.deriveWith(brightnessFactor = brightness), position + offsetRight))
          }
          commands.add(LightStrip.Command.Commit)
          Observable.from(commands)
        }
  }

  fun scrubberAnimation(fractions: Observable<Double>): Observable<LightStrip.Command> {
    return fractions.flatMap { fraction ->
      val fractionalPosition = fraction * lastIndex
      val leftIndex = fractionalPosition.toInt()
      val rightIndex = (leftIndex + 1).coerceAtMost(lastIndex)
      val rightBrightness = fractionalPosition - leftIndex
      val leftBrightness = 1 - rightBrightness
      Observable.just(
          LightStrip.Command.Clear,
          LightStrip.Command.Set(primaryColor.deriveWith(brightnessFactor = rightBrightness), rightIndex),
          LightStrip.Command.Set(primaryColor.deriveWith(brightnessFactor = leftBrightness), leftIndex),
          LightStrip.Command.Commit
      )
    }
  }

  override fun close() {
    if (isClosed) return
    stopAnimation()
    lightStrip.close()
    isClosed = true
  }

}
