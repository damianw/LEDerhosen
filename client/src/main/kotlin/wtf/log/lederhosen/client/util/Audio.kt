package wtf.log.lederhosen.client.util

import javax.sound.sampled.*

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 1/19/17
 * (C) 2017
 */
object Audio {

  fun getMixers(): List<Mixer> = AudioSystem.getMixerInfo().map(AudioSystem::getMixer)

  fun getInputMixers(): List<Mixer> = getMixers().filter { it.targetLineInfo.isNotEmpty() }

  fun getOutputMixers(): List<Mixer> = getMixers().filter { it.sourceLineInfo.isNotEmpty() }

}

object AudioFormats {

  val DEFAULT = AudioFormat(
      Defaults.SAMPLE_RATE,
      Defaults.SAMPLE_SIZE,
      Defaults.CHANNELS,
      Defaults.SIGNED,
      Defaults.BIG_ENDIAN
  )

  object Defaults {

    const val SAMPLE_RATE = 44100f
    const val SAMPLE_SIZE = 16
    const val CHANNELS = 1
    const val SIGNED = true
    const val BIG_ENDIAN = false

  }

}

typealias Cents = Double

@Suppress("UNCHECKED_CAST")
fun <T : DataLine> Mixer.getLine(lineClass: Class<T>, format: AudioFormat = AudioFormats.DEFAULT): T {
  return getLine(DataLine.Info(lineClass, format)) as T
}

inline fun <reified T : DataLine> Mixer.getLine(format: AudioFormat = AudioFormats.DEFAULT): T {
  return getLine(T::class.java, format)
}

fun Mixer.getInputLine(format: AudioFormat = AudioFormats.DEFAULT): TargetDataLine = getLine(format)

fun Mixer.getOutputLine(format: AudioFormat = AudioFormats.DEFAULT): SourceDataLine = getLine(format)
