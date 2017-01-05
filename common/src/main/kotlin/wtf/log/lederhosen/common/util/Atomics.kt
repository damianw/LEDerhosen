package wtf.log.lederhosen.common.util

import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KProperty

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 1/4/17
 * (C) 2017
 *
 * Atomic Utils
 */

/**
 * Getter for boolean value
 */
operator fun AtomicBoolean.getValue(thisRef: Any?, property: KProperty<*>): Boolean = get()

/**
 * Setter for boolean value
 */
operator fun AtomicBoolean.setValue(thisRef: Any?, property: KProperty<*>, value: Boolean): Unit = set(value)
