package io.tanguygab.yarmm.config.menu.meta.firework

import io.tanguygab.yarmm.config.menu.ItemMetaConfig
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.inventory.meta.ItemMeta

class FireworkMetaConfig(section: ConfigurationSection) : ItemMetaConfig(FireworkMeta::class) {
    val power = section.getObject("power")?.toString() ?: "0"
    val effects = section.getStringList("effects", emptyList())

    override fun storeData(item: MenuItemView, player: TabPlayer) = mapOf(
        "power" to property(item, player, power),
        "effects" to property(item, player, effects.joinToString("\n"))
    )

    override fun refresh(meta: ItemMeta, data: Map<String, Property>, force: Boolean) {
        meta as FireworkMeta

        val power = data["power"]!!
        if (power.update() || force) meta.power = power.get().toIntOrNull() ?: 0

        val effects = data["effects"]!!
        if (effects.update() || force) {
            meta.clearEffects()
            meta.addEffects(effects.get().split("\n").mapNotNull { it.toFireworkEffect() })
        }
    }

}