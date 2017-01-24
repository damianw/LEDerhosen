package wtf.log.lederhosen.common.util

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 1/20/17
 * (C) 2017
 */
object Timing {

  fun millisTime(): Long = System.nanoTime() / 1000000

}
