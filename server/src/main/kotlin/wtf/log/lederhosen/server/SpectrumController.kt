package wtf.log.lederhosen.server

import com.fazecast.jSerialComm.SerialPort
import javafx.scene.paint.Color
import wtf.log.lederhosen.driver.LightStrip
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

    val columnHeight = (magnitude.coerceIn(0f, 1f) * lastRow).toInt()
    val start = columnIndex * rowCount
    val end = start + columnHeight
    val pixels = (start..end).toList().toIntArray()

    lightStrip.set(*pixels, color = Color.RED)
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

    fun open(columnCount: Int, rowCount: Int, port: SerialPort) = SpectrumController(
        lightStrip = LightStrip.open(columnCount * rowCount, port),
        columnCount = columnCount,
        rowCount = rowCount
    )

  }

}
