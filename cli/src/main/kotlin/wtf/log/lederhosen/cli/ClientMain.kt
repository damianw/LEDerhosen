package wtf.log.lederhosen.cli

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.jvm.JVMAudioInputStream
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCPortOut
import wtf.log.lederhosen.client.dsp.SpectrumProcessor
import wtf.log.lederhosen.client.util.Audio
import wtf.log.lederhosen.client.util.getInputLine
import javax.sound.sampled.AudioInputStream

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 1/24/17
 * (C) 2017
 */
object ClientMain {

  fun run(clientArguments: ClientArguments) {
    if (clientArguments.listInputs) {
      listInputs()
      return
    }

    val format = clientArguments.audioFormat
    val mixer = clientArguments.mixer!!
    val line = mixer.getInputLine().apply {
      open(format, clientArguments.bufferSize)
      start()
    }

    val processor = SpectrumProcessor(
        bucketCount = clientArguments.columnCount,
        maxAmplitude = clientArguments.maxAmplitude
    )

    val stream = JVMAudioInputStream(AudioInputStream(line))
    val dispatcher = AudioDispatcher(stream, clientArguments.bufferSize, clientArguments.bufferOverlap).apply {
      addAudioProcessor(processor)
    }

    Thread(dispatcher).start()

    val outPort = OSCPortOut(clientArguments.host, clientArguments.port)
    processor.buckets
        .map { OSCMessage("/bars", it) }
        .subscribe(outPort::send)
  }

  private fun listInputs() {
    Audio.getInputMixers()
        .map { it.mixerInfo.name }
        .forEach(::println)
  }

}
