package wtf.log.lederhosen.server

import rx.Subscription
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
    val controller: SpectrumController,
    val port: Int,
    val charset: Charset = Charset.defaultCharset()
) : Closeable {

  private var portSubscription: Subscription? = null

  @Suppress("UNCHECKED_CAST")
  private fun onBarsMessageReceived(oscEvent: OscEvent) {
    controller.render(oscEvent.arguments as List<Float>)
  }

  @Synchronized fun start() {
    stop()
    val s = this
    portSubscription = Osc.startServer(port, charset) {
      events("/bars")
          .doOnError(Throwable::printStackTrace)
          .retry()
          .subscribe(s::onBarsMessageReceived)
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
  }

}
