package wtf.log.lederhosen.common.util

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 1/19/17
 * (C) 2017
 * Array utils
 */

/*
 * Copying
 */

fun <T> Array<out T>.copyTo(dst: Array<in T>, srcPos: Int = 0, dstPos: Int = 0, length: Int = size) =
    System.arraycopy(this, srcPos, dst, dstPos, length)

fun <T> Array<in T>.copyFrom(src: Array<out T>, srcPos: Int = 0, dstPos: Int = 0, length: Int = src.size) =
    System.arraycopy(src, srcPos, this, dstPos, length)

fun FloatArray.copyTo(dst: FloatArray, srcPos: Int = 0, dstPos: Int = 0, length: Int = size) =
    System.arraycopy(this, srcPos, dst, dstPos, length)

fun FloatArray.copyFrom(src: FloatArray, srcPos: Int = 0, dstPos: Int = 0, length: Int = src.size) =
    System.arraycopy(src, srcPos, this, dstPos, length)

/*
 * Misc
 */

fun FloatArray.setAll(value: Float) {
  forEachIndexed { i, _ -> set(i, value) }
}

fun FloatArray.blank() = setAll(0f)

