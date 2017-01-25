package wtf.log.lederhosen.server

import com.fazecast.jSerialComm.SerialPort
import javafx.scene.paint.Color
import wtf.log.lederhosen.driver.LightStrip
import wtf.log.lederhosen.driver.util.deriveWith
import java.io.Closeable

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 1/13/17
 * (C) 2017
 */
class SpectrumController(
    private val lightStrip: LightStrip,
    val columnCount: Int,
    val rowCount: Int
) : Closeable by lightStrip {

  init {
    val requested = columnCount * rowCount
    require(columnCount > 0) {
      "At least one column is required"
    }
    require(rowCount > 0) {
      "At least one row is required"
    }
    require(requested <= lightStrip.size) {
      "Light strip only has ${lightStrip.size} pixels, but $columnCount*$rowCount ($requested) were requested"
    }
  }

  private val lastColumn = columnCount - 1

  private val lastRow = rowCount - 1

  private fun set(columnIndex: Int, magnitude: Float) {
    if (columnIndex !in 0..lastColumn) throw IndexOutOfBoundsException()

    val columnStart = columnIndex * rowCount

    val columnHeight = magnitude.coerceIn(0f, 1f) * rowCount
    val primarySegmentHeight = columnHeight.toInt()

    val primaryPixels = (columnStart until (columnStart + primarySegmentHeight)).toList().toIntArray()
    lightStrip.set(*primaryPixels, color = Color.GREEN)

    val topperBrightness = columnHeight - primarySegmentHeight
    val topperPixel = columnStart + primarySegmentHeight
    if (primarySegmentHeight < rowCount) {
      val topBrightnessLog = Math.log1p(topperBrightness.toDouble()) / LOG_TWO
      val baseColor = if (topperPixel == columnStart + lastRow) Color.RED else Color.GREEN
      lightStrip[topperPixel] = baseColor.deriveWith(brightnessFactor = topBrightnessLog)
    } else {
      lightStrip[columnStart + lastRow] = Color.RED
    }
  }

  fun render(magnitudes: List<Float>) {
    require(magnitudes.size == columnCount) {
      "Magnitudes array must be of size equal to column count ($columnCount), was ${magnitudes.size}"
    }

    lightStrip.clear()
    magnitudes.forEachIndexed(this::set)
    lightStrip.commitChanges()
  }

  companion object {

    private val LOG_TWO = Math.log(2.0)

    fun open(columnCount: Int, rowCount: Int, port: SerialPort) = SpectrumController(
        lightStrip = LightStrip.open(columnCount * rowCount, port),
        columnCount = columnCount,
        rowCount = rowCount
    )

  }

}
