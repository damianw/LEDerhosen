package wtf.log.lederhosen.server

import com.jakewharton.rxrelay.BehaviorRelay
import javafx.scene.paint.Color
import rx.Subscription
import rx.functions.Func1
import wtf.log.lederhosen.common.controller.StripController
import wtf.log.lederhosen.common.util.getValue
import wtf.log.lederhosen.common.util.invoke
import wtf.log.lederhosen.common.util.setValue
import wtf.log.lederhosen.common.util.subscribe
import wtf.log.lederhosen.server.util.Osc
import wtf.log.lederhosen.server.util.OscEvent
import java.io.Closeable
import java.nio.charset.Charset

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 12/28/16
 * (C) 2016
 */
class OscServer(
    val controller: StripController,
    val port: Int,
    val charset: Charset = Charset.defaultCharset()
) : Closeable {

  private val scrubberRelay = BehaviorRelay.create<Double>(0.0)
  private val primaryColorRelay = BehaviorRelay.create<Color>(Color.WHITE)

  var scrubberProgress: Double by scrubberRelay
    private set

  var primaryColor: Color by primaryColorRelay
    private set

  private var portSubscription: Subscription? = null

  private var colorSubscription = primaryColorRelay.subscribe { color ->
    controller.primaryColor = color
  }

  private fun playRainbowAnimation() {
    controller.playAnimation(controller.rainbowAnimation())
  }

  private fun playBounceAnimation() {
    controller.playAnimation(controller.bounceAnimation())
  }

  private fun playSpringAnimation() {
    controller.playAnimation(controller.springAnimation())
  }

  private fun playScrubberAnimation() {
    controller.playAnimation(controller.scrubberAnimation(scrubberRelay))
  }

  private fun updateColor(red: Double = primaryColor.red, green: Double = primaryColor.green, blue: Double = primaryColor.blue) {
    primaryColorRelay(Color(red, green, blue, 1.0))
  }

  private fun updateRed(red: Double) = updateColor(red = red)

  private fun updateGreen(green: Double) = updateColor(green = green)

  private fun updateBlue(blue: Double) = updateColor(blue = blue)

  @Synchronized fun start() {
    stop()
    val s = this
    portSubscription = Osc.startServer(port, charset) {
      events("/animation/rainbow").subscribe(s::playRainbowAnimation)
      events("/animation/bounce").subscribe(s::playBounceAnimation)
      events("/animation/spring").subscribe(s::playSpringAnimation)
      events("/animation/scrubber").subscribe(s::playScrubberAnimation)
      events("/animation/scrubber/*").map(LAST_DOUBLE).subscribe(scrubberRelay)
      events("/color/primary/red/*").map(LAST_DOUBLE).subscribe(s::updateRed)
      events("/color/primary/green/*").map(LAST_DOUBLE).subscribe(s::updateGreen)
      events("/color/primary/blue/*").map(LAST_DOUBLE).subscribe(s::updateBlue)
    }
  }

  @Synchronized fun stop() {
    portSubscription?.apply {
      if (!isUnsubscribed) unsubscribe()
    }
    portSubscription = null
  }

  @Synchronized override fun close() {
    stop()
    controller.close()
    colorSubscription.unsubscribe()
  }

  companion object {

    private val LAST_DOUBLE = Func1<OscEvent, Double> { it.pathComponents.last().toDouble() }

  }

}
