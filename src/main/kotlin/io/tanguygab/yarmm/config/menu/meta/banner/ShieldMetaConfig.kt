package io.tanguygab.yarmm.config.menu.meta.banner

import io.tanguygab.yarmm.config.menu.ItemMetaConfig
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.DyeColor
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.ShieldMeta

class ShieldMetaConfig(val shieldColor: String) : ItemMetaConfig(ShieldMeta::class) {
    override fun storeData(item: MenuItemView, player: TabPlayer) = mapOf(
        "shield-color" to property(item, player, shieldColor)
    )

    override fun refresh(meta: ItemMeta, data: Map<String, Property>, force: Boolean) {
        val baseColor = data["shield-color"]!!
        if (baseColor.update() || force)
            (meta as ShieldMeta).baseColor = DyeColor.entries.find { color -> color.name.equals(baseColor.get(), ignoreCase = true) }
    }

}