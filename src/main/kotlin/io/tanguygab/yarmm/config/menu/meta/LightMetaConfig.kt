package io.tanguygab.yarmm.config.menu.meta

import io.tanguygab.yarmm.config.menu.ItemMetaConfig
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.Material
import org.bukkit.block.data.type.Light
import org.bukkit.inventory.meta.BlockDataMeta
import org.bukkit.inventory.meta.ItemMeta

class LightMetaConfig(val level: String) : ItemMetaConfig(BlockDataMeta::class) {
    override fun storeData(item: MenuItemView, player: TabPlayer) = mapOf(
        "level" to property(item, player, level)
    )

    override fun refresh(meta: ItemMeta, data: Map<String, Property>, force: Boolean) {
        val level = data["level"]!!
        if (level.update() || force) {
            meta as BlockDataMeta
            val data = meta.getBlockData(Material.LIGHT) as Light
            data.level = level.get().toIntOrNull()?.coerceIn(0, data.maximumLevel) ?: data.maximumLevel
        }
    }

}