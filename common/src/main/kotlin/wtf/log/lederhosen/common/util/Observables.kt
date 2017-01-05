package wtf.log.lederhosen.common.util

import rx.Emitter
import rx.Observable
import rx.Subscription

/**
 * @author Damian Wieczorek {@literal <damian@farmlogs.com>}
 * @since 12/27/16
 * (C) 2016
 *
 * RxJava [Observable] utilities
 */

/**
 * [Observable] factories
 */
object Observables {

  /**
   * Wrapper for [Observable.fromEmitter] with swapped argument order
   */
  inline fun <T> fromEmitter(backpressure: Emitter.BackpressureMode, crossinline emitter: (Emitter<T>) -> Unit): Observable<T> {
    return Observable.fromEmitter({ emitter(it) }, backpressure)
  }

}

/**
 * Convenience for filtering for a specific type
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T> Observable<*>.filterIsInstance(): Observable<T> = filter { it is T } as Observable<T>

/**
 * Subscribe to an [Observable] without consuming items directly
 */
inline fun Observable<*>.subscribe(crossinline action: () -> Unit): Subscription = subscribe { action() }
