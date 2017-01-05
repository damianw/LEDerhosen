package wtf.log.lederhosen.driver.util

import javafx.scene.paint.Color


/**
 * @author Damian Wieczorek <damian@farmlogs.com>
 * @since 12/27/16
 * (C) 2016
 *
 * Color Utils
 */

/**
 * Retrieves the red component of this color, in range 0..255
 */
operator fun Color.component1(): Int = (red * 255).toInt().coerceIn(0, 255)

/**
 * Retrieves the green component of this color, in range 0..255
 */
operator fun Color.component2(): Int = (green * 255).toInt().coerceIn(0, 255)

/**
 * Retrieves the blue component of this color, in range 0..255
 */
operator fun Color.component3(): Int = (blue * 255).toInt().coerceIn(0, 255)

/**
 * Wrapper for [Color.deriveColor] with default values
 */
fun Color.deriveWith(
    hueShift: Double = 0.0,
    saturationFactor: Double = 1.0,
    brightnessFactor: Double = 1.0,
    opacityFactor: Double = 1.0
): Color = deriveColor(hueShift, saturationFactor, brightnessFactor, opacityFactor)
