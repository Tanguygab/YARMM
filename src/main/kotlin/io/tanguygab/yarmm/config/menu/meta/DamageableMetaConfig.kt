package io.tanguygab.yarmm.config.menu.meta

import io.tanguygab.yarmm.config.menu.ItemMetaConfig
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta

class DamageableMetaConfig(section: ConfigurationSection) : ItemMetaConfig(Damageable::class) {
    val value = section.getObject("value", 0).toString()
    val max = section.getObject("max")?.toString() ?: "-1"

    override fun storeData(item: MenuItemView, player: TabPlayer) = mapOf(
        "value" to property(item, player, value),
        "max" to property(item, player, max)
    )

    override fun refresh(meta: ItemMeta, data: Map<String, Property>, force: Boolean) {
        meta as Damageable
        val value = data["value"]!!
        if (value.update() || force) meta.damage = value.get().toIntOrNull() ?: 0

        val max = data["max"]!!
        if (max.update() || force) {
            val m = max.get().toIntOrNull() ?: 0
            if (m == -1) meta.resetDamage()
            meta.setMaxDamage(m)
        }
    }

}