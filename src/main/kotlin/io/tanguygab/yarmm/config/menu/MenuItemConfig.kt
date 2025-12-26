package io.tanguygab.yarmm.config.menu

import io.github.tanguygab.conditionalactions.ConditionalActions
import io.github.tanguygab.conditionalactions.actions.ActionGroup
import io.github.tanguygab.conditionalactions.conditions.ConditionGroup
import io.tanguygab.yarmm.config.MainConfig
import me.neznamy.tab.shared.config.file.ConfigurationSection

data class MenuItemConfig(
    val material: String,
    val name: String,
    val amount: String,
    val lore: List<String>,
    val slots: List<String>,

    val clickActions: ActionGroup,
    val viewCondition: ConditionGroup?,

    val enchantments: Map<String, String>
) {
    companion object {
        fun fromSection(section: ConfigurationSection, config: MainConfig): MenuItemConfig {
            return MenuItemConfig(
                material = section.getString("material") ?: "STONE",
                name = config.itemNamePrefix + (section.getString("name") ?: ""),
                amount = section.getString("amount") ?: "1",
                lore = section.getStringList("lore")?.map { config.itemLorePrefix + it} ?: emptyList(),
                slots = getSlotRanges(section),

                clickActions = getActionGroup(section.getObject("click-actions") ?: emptyList<Any>()),
                viewCondition = section.getString("view-condition")?.let { ConditionGroup(ConditionalActions.INSTANCE.conditionManager, it) },

                enchantments = section.getMap<String, Any>("enchantments")
                    ?.map { (key, value) -> key to value.toString() }
                    ?.toMap()
                    ?: emptyMap()
            )
        }

        private fun getSlotRanges(section: ConfigurationSection): List<String> {
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
}