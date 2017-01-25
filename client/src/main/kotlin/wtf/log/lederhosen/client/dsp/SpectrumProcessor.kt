package wtf.log.lederhosen.client.dsp

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.util.PitchConverter
import be.tarsos.dsp.util.fft.FFT
import rx.Observable
import rx.subjects.PublishSubject
import wtf.log.lederhosen.client.util.AudioFormats
import wtf.log.lederhosen.client.util.Cents
import wtf.log.lederhosen.common.util.Timing
import wtf.log.lederhosen.common.util.blank
import wtf.log.lederhosen.common.util.copyTo

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 1/20/17
 * (C) 2017
 */
class SpectrumProcessor(
    val bucketCount: Int,
    val maxAmplitude: Float,
    val sampleRate: Float = AudioFormats.Defaults.SAMPLE_RATE,
    val bufferSize: Int = AudioFormats.Defaults.BUFFER_SIZE,
    val minFrequency: Double = 50.0,
    val maxFrequency: Double = 17000.0,
    val frameRate: Int = 960
) : AudioProcessor {

  init {
    require(frameRate > 0) { "Frame rate must be positive" }
  }

  private val minCent: Cents = PitchConverter.hertzToAbsoluteCent(minFrequency)
  private val maxCent: Cents = PitchConverter.hertzToAbsoluteCent(maxFrequency)
  private val centRange: Cents = maxCent - minCent

  private val fft = FFT(bufferSize)
  private val amplitudes = FloatArray(bufferSize / 2)
  private val transformBuffer = FloatArray(bufferSize * 2)

  private val bucketedAmplitudes = FloatArray(bucketCount)
  private val relativeBucketedAmplitudes = FloatArray(bucketCount)

  private val frameDelay: Int = (1000f / frameRate).toInt()

  private val _buckets = PublishSubject.create<List<Float>>()

  private var previousFrameTime: Long = 0L

  val buckets: Observable<List<Float>> = _buckets

  private fun doProcessing(audioEvent: AudioEvent) {
    doFft(audioEvent.floatBuffer)
    bucketAmplitudes()
    computeRelativeAmplitudes()
    _buckets.onNext(relativeBucketedAmplitudes.toList())
  }

  private fun doFft(floatBuffer: FloatArray) {
    floatBuffer.copyTo(transformBuffer)
    fft.forwardTransform(transformBuffer)
    fft.modulus(transformBuffer, amplitudes)
  }

  private fun bucketFrequency(frequency: Double): Int {
    val freqCent = PitchConverter.hertzToAbsoluteCent(frequency.coerceAtLeast(0.1))
    val frac = (freqCent - minCent) / centRange
    val estimate = frac * bucketCount

    val bucketIndex: Int = bucketCount - Math.round(estimate).toInt()
    val invertedBucketIndex = (bucketCount - bucketIndex).coerceIn(0, bucketCount - 1)
    return invertedBucketIndex
  }

  private fun bucketAmplitudes() {
    bucketedAmplitudes.blank()
    amplitudes.forEachIndexed { i, amplitude ->
      val frequency = i * sampleRate.toDouble() / (amplitudes.size * 4)
      val bucket = bucketFrequency(frequency)
      bucketedAmplitudes[bucket] += amplitude
    }
  }

  private fun computeRelativeAmplitudes() {
    bucketedAmplitudes.forEachIndexed { bucketIndex, amplitude ->
      val magnitude = Math.log1p(amplitude.toDouble() / maxAmplitude) / TINY_LOG
      relativeBucketedAmplitudes[bucketIndex] = magnitude.toFloat().coerceIn(0f, 1f)
    }
  }

  override fun process(audioEvent: AudioEvent): Boolean {
    val currentTime = Timing.millisTime()
    val elapsed = currentTime - previousFrameTime
    if (elapsed >= frameDelay) {
      previousFrameTime = currentTime
      doProcessing(audioEvent)
    }

    return true
  }

  override fun processingFinished() {
  }

  companion object {

    private val TINY_LOG = Math.log1p(1.0000001)

  }

}
