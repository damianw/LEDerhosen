package wtf.log.lederhosen.cli

import com.beust.jcommander.IStringConverter
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.beust.jcommander.Parameters
import wtf.log.lederhosen.client.util.Audio
import wtf.log.lederhosen.client.util.AudioFormats
import java.net.InetAddress
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.Mixer

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 1/24/17
 * (C) 2017
 */
@Parameters(
    commandNames = arrayOf(ProgramArguments.COMMAND_CLIENT),
    commandDescription = "Runs the LEDerhosen client"
)
class ClientArguments {

  @Parameter(
      names = arrayOf("--list-inputs", "-l"),
      description = "List available audio inputs",
      help = true
  )
  var listInputs: Boolean = false

  class MixerConverter : IStringConverter<Mixer> {
    override fun convert(value: String): Mixer {
      return Audio.getInputMixers().firstOrNull { it.mixerInfo.name == value }
          ?: throw ParameterException("No such mixer with input: $value")
    }
  }

  @Parameter(
      names = arrayOf("--input", "-i"),
      description = "Name of audio input mixer",
      required = true,
      converter = MixerConverter::class
  )
  var mixer: Mixer? = null

  @Parameter(
      names = arrayOf("--sample-rate", "-r"),
      description = "Audio sample rate"
  )
  var sampleRate: Float = AudioFormats.Defaults.SAMPLE_RATE

  @Parameter(
      names = arrayOf("--sample-size", "-s"),
      description = "Audio sample size (in bits)"
  )
  var sampleSizeInBits: Int = AudioFormats.Defaults.SAMPLE_SIZE

  @Parameter(
      names = arrayOf("--channels", "-n"),
      description = "Number of channels in audio"
  )
  var channels: Int = AudioFormats.Defaults.CHANNELS

  @Parameter(
      names = arrayOf("--signed", "-x"),
      description = "Whether audio input is signed"
  )
  var signed: Boolean = AudioFormats.Defaults.SIGNED

  @Parameter(
      names = arrayOf("--big-endian", "-b"),
      description = "Whether audio input is big endian"
  )
  var bigEndian: Boolean = AudioFormats.Defaults.BIG_ENDIAN

  @Parameter(
      names = arrayOf("--buffer-size", "-u"),
      description = "Audio buffer size"
  )
  var bufferSize: Int = AudioFormats.Defaults.BUFFER_SIZE

  @Parameter(
      names = arrayOf("--buffer-overlap", "-o"),
      description = "Audio buffer overlap"
  )
  var bufferOverlap: Int = AudioFormats.Defaults.BUFFER_OVERLAP

  @Parameter(
      names = arrayOf("--columns", "-c"),
      description = "Number of columns (bars) in the spectrum"
  )
  var columnCount: Int = 16

  @Parameter(
      names = arrayOf("--max-amplitude", "-a"),
      description = "Maximum amplitude for bar scaling"
  )
  var maxAmplitude: Float = 400f

  class InetAddressConverter : IStringConverter<InetAddress> {
    override fun convert(value: String): InetAddress {
      return InetAddress.getByName(value)
    }
  }

  @Parameter(
      names = arrayOf("--host", "-h"),
      description = "Server host to transmit messages to",
      required = true,
      converter = InetAddressConverter::class
  )
  var host: InetAddress? = null

  @Parameter(
      names = arrayOf("--port", "-p"),
      description = "Server port"
  )
  var port: Int = 1605

  val audioFormat: AudioFormat
    get() = AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian)

}
