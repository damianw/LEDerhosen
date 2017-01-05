package wtf.log.lederhosen.driver

import com.fazecast.jSerialComm.SerialPort
import javafx.scene.paint.Color
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import wtf.log.lederhosen.driver.util.component1
import wtf.log.lederhosen.driver.util.component2
import wtf.log.lederhosen.driver.util.component3
import java.io.Closeable
import java.util.*

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 12/27/16
 * (C) 2016
 *
 * The "lowest-level" API for the light driver.
 */
class LightStrip private constructor(
    val size: Int,
    private val sink: BufferedSink,
    private val source: BufferedSource
): Closeable {

  private val setQueue = ArrayDeque<Command.Set>()

  var isClosed: Boolean = false
    private set

  val lastIndex: Int = size - 1

  init {
    reset()
  }

  @Synchronized private fun ensureCommit() {
    sink.writeByte(OPCODE_COMMIT)
    sink.flush()
    while (!source.exhausted() && source.readByte() != RESPONSE_OK) {
      sink.writeByte(OPCODE_COMMIT)
      sink.flush()
    }
  }

  /**
   * "Reset" any lingering commands to the driver, or attempt to recover the connection to the driver if it was lost.
   */
  @Synchronized fun reset() {
    setQueue.clear()
    repeat(4) {
      sink.writeByte(OPCODE_COMMIT)
    }
    ensureCommit()
  }

  /**
   * Set one or many pixels to a particular color (but does not commit the changes)
   */
  @Synchronized operator fun set(vararg pixels: Int, color: Color) {
    setQueue.add(Command.Set(color, *pixels))
  }

  /**
   * Executes an arbitrary command
   */
  @Synchronized fun execute(command: Command) {
    when (command) {
      is Command.Set -> {
        setQueue.add(command)
      }
      is Command.Clear -> clear()
      is Command.Commit -> commitChanges()
    }
  }

  /**
   * Turns off all LEDs on the driver and clears the queue of changes.
   */
  @Synchronized fun clear() {
    setQueue.clear()
    sink.writeByte(OPCODE_CLEAR)
    sink.flush()
  }

  /**
   * Commits queued changes immediately.
   */
  @Synchronized fun commitChanges() {
    var next = setQueue.poll()
    while (next != null) {
      val (r, g, b) = next.color
      val pixels = next.pixels
      sink.apply {
        writeByte(OPCODE_SET)
        writeUtf8(r.toString())
        writeUtf8(",")
        writeUtf8(g.toString())
        writeUtf8(",")
        writeUtf8(b.toString())
        writeUtf8(",")
        writeUtf8(pixels.size.toString())
        writeUtf8(",")
        pixels.forEach { pixel ->
          writeUtf8(pixel.toString())
          writeUtf8(",")
        }
      }
      sink.flush()
      next = setQueue.poll()
    }
    ensureCommit()
  }

  /**
   * Closes the connection to the driver.
   */
  @Synchronized override fun close() {
    if (isClosed) return
    source.close()
    sink.close()
    isClosed = true
  }

  /**
   * Available commands for the driver
   */
  sealed class Command {

    /**
     * Sets one or more pixels to a particular color (but does not commit the changes)
     */
    class Set(val color: Color, vararg val pixels: Int): Command()

    /**
     * Commits pending color changes
     */
    object Commit : Command()

    /**
     * Turns off all of the LEDs on the driver.
     */
    object Clear : Command()

  }

  companion object {

    private const val BAUD_RATE = 57600

    private const val OPCODE_SET = 1
    private const val OPCODE_CLEAR = 2
    private const val OPCODE_COMMIT = 127

    private const val RESPONSE_OK: Byte = 127

    /**
     * Opens a connection to a light driver on [port] with [pixelCount] number of pixels.
     */
    fun open(pixelCount: Int, port: SerialPort): LightStrip {
      port.apply {
        baudRate = BAUD_RATE
        openPort()
        setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0)
      }
      return LightStrip(
          size = pixelCount,
          source = Okio.buffer(Okio.source(port.inputStream)),
          sink = Okio.buffer(Okio.sink(port.outputStream))
      )
    }

  }

}
