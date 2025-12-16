package io.tanguygab.yarmm.config

import io.github.tanguygab.conditionalactions.ConditionalActions
import io.github.tanguygab.conditionalactions.actions.ActionGroup
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.config.file.YamlConfigurationFile
import org.bukkit.event.inventory.InventoryType
import java.io.File

class MenuConfig(file: File, config: MainConfig) : YamlConfigurationFile(null, file) {

    private val actionManager = ConditionalActions.INSTANCE.actionManager

    val title = getString("title", "<red>No title set")!!
    val rows = getInt("rows", 6)!!
    val type = InventoryType.entries.find { it.name == getString("type", null) } ?: InventoryType.CHEST

    val openActions = getActionGroup("open-actions")
    val closeActions = getActionGroup("close-actions")

    private fun getActionGroup(path: String) = ActionGroup(actionManager, getObject(path, emptyList<Any>()) as List<*>)

    val items = getMap<String, Any>("items").keys
        .map { getConfigurationSection("items.$it") }
        .map { MenuItemConfig(it, config) }
}

class MenuItemConfig(val section: ConfigurationSection, config: MainConfig) {
    val material = section.getString("material") ?: "STONE"
    val name = config.itemNamePrefix + (section.getString("name") ?: "")
    val amount = section.getString("amount") ?: "1"
    val lore = section.getStringList("lore")?.map { config.itemLorePrefix + it} ?: emptyList()
    val slots = getSlotRanges()

    private fun getSlotRanges(): List<String> {
        val slots = section.getStringList("slots")?.toMutableList()
            ?: mutableListOf(section.getObject("slot", "0").toString())

        val ranges = mutableMapOf<String, List<String>>()
        slots.forEach { it ->
            if (it.contains("%")) return@forEach

            val range = it.split("-")
            if (range.size == 2) {
                ranges[it] = (range[0].toInt() .. range[1].toInt()).map { it.toString() }
            }
        }
        slots.removeAll(ranges.keys)
        slots.addAll(ranges.values.flatten())
        return slots.toList()
    }
}