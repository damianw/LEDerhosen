@file:Suppress("NOTHING_TO_INLINE")

package wtf.log.lederhosen.common.util

import rx.functions.Action1

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 12/28/16
 * (C) 2016
 *
 * Misc RxJava utilities
 */

/**
 * Kotlin operator for "call"
 */
inline operator fun <T> Action1<T>.invoke(value: T) = call(value)
