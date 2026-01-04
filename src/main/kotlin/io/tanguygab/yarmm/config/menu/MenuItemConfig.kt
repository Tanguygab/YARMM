package io.tanguygab.yarmm.config.menu

import io.github.tanguygab.conditionalactions.ConditionalActions
import io.github.tanguygab.conditionalactions.actions.ActionGroup
import io.github.tanguygab.conditionalactions.conditions.ConditionGroup
import me.neznamy.tab.shared.config.file.ConfigurationSection
import org.bukkit.inventory.ItemFlag

data class MenuItemConfig(
    val material: String,
    val amount: String,
    val slots: List<String>,

    val clickCooldown: String,
    val clickActions: ActionGroup,
    val displayCondition: ConditionGroup?,

    val enchantments: Map<String, String>,
    val flags: List<ItemFlag>,
    val metas: List<ItemMetaConfig>
) {
    companion object {
        fun fromSection(section: ConfigurationSection) = MenuItemConfig(
            material = section.getString("material") ?: "STONE",
            amount = section.getString("amount") ?: "1",
            slots = getSlotRanges(section),

            clickCooldown = section.getObject("click-cooldown")?.toString() ?: "-1",
            clickActions = getActionGroup(section.getObject("click-actions") ?: emptyList<Any>()),
            displayCondition = section.getString("display-condition")?.let { ConditionGroup(ConditionalActions.INSTANCE.conditionManager, it) },

            enchantments = section.getMap<String, Any>("enchantments")
                ?.map { (key, value) -> key to value.toString() }
                ?.toMap()
                ?: emptyMap(),
            flags = section.getStringList("flags")
                ?.mapNotNull { ItemFlag.entries.find { flag -> flag.name.equals(it, ignoreCase = true) } }
                ?: listOf(),
            metas = ItemMetaConfig.fromItem(section)
        )

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