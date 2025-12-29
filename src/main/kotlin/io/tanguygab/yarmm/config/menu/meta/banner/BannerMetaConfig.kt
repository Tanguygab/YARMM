package io.tanguygab.yarmm.config.menu.meta.banner

import io.papermc.paper.registry.RegistryKey
import io.tanguygab.yarmm.config.menu.ItemMetaConfig
import io.tanguygab.yarmm.config.menu.getFromRegistry
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.DyeColor
import org.bukkit.block.banner.Pattern
import org.bukkit.inventory.meta.BannerMeta
import org.bukkit.inventory.meta.ItemMeta

class BannerMetaConfig(val patterns: List<String>) : ItemMetaConfig(BannerMeta::class) {
    override fun storeData(item: MenuItemView, player: TabPlayer) = mapOf(
        "patterns" to property(item, player, patterns.joinToString("\n"))
    )

    override fun refresh(meta: ItemMeta, data: Map<String, Property>, force: Boolean) {
        val patterns = data["patterns"]!!
        if (!patterns.update() && !force) return
        (meta as BannerMeta).patterns = patterns.get()
            .split("\n")
            .map { it.split(":") }
            .filter { it.size == 2 }
            .mapNotNull {
                val type = it[0].getFromRegistry(RegistryKey.BANNER_PATTERN)
                val color = DyeColor.entries.find { color -> color.name.equals(it[1], ignoreCase = true) }
                if (type == null || color == null) null else Pattern(color, type)
            }
    }

}