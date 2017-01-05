package wtf.log.lederhosen.common.util

import com.jakewharton.rxrelay.BehaviorRelay
import kotlin.reflect.KProperty

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 12/28/16
 * (C) 2016
 */

/**
 * Getter for relay value
 */
operator fun <T> BehaviorRelay<T>.getValue(thisRef: Any?, property: KProperty<*>): T = value

/**
 * Setter for relay value
 */
operator fun <T> BehaviorRelay<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) = call(value)
