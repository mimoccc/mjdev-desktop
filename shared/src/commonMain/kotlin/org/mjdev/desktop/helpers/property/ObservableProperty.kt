package org.mjdev.desktop.helpers.property

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Property whose value changes can be listened to. Backed by a [StateFlow], so it is thread
 * safe, a late listener still receives the latest value and an equal value is not re-sent
 * (no recomposition / listener loops).
 *
 * Usable directly ([value], [changes]) or as a delegate: `var items by property`.
 *
 * @param initial value before the first change
 */
class ObservableProperty<T>(
    initial: T,
) : ReadWriteProperty<Any?, T> {
    private val state = MutableStateFlow(initial)

    /** Stream of values: the current value first, then every distinct change. */
    val changes: StateFlow<T> = state.asStateFlow()

    /** Current value; assigning notifies listeners when it differs from the previous one. */
    var value: T
        get() = state.value
        set(newValue) {
            state.value = newValue
        }

    /** Atomically replaces the value with [transform] of the current one (safe across threads). */
    fun update(transform: (T) -> T) = state.update(transform)

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T = value

    override fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: T,
    ) {
        this.value = value
    }
}
