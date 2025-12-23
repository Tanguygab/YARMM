package io.tanguygab.yarmm

import me.neznamy.tab.shared.placeholders.types.TabPlaceholder
import me.neznamy.tab.shared.platform.TabPlayer

class ThreadPlaceholder(identifier: String) : TabPlaceholder(identifier, -1) {

    private val lastPlaceholderValues = mutableMapOf<Thread, String>()

    fun updateValue(value: String?) {
        if (value === null) lastPlaceholderValues.remove(Thread.currentThread())
        else lastPlaceholderValues[Thread.currentThread()] = value
    }

    override fun updateFromNested(p: TabPlayer) {}
    override fun getLastValue(p: TabPlayer?) = lastPlaceholderValues[Thread.currentThread()] ?: identifier
    override fun getLastValueSafe(p: TabPlayer) = lastPlaceholderValues[Thread.currentThread()] ?: identifier

}