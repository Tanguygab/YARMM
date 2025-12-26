package io.tanguygab.yarmm.config.menu.meta

import io.papermc.paper.registry.RegistryKey
import io.tanguygab.yarmm.config.menu.ItemMetaConfig
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.Color
import org.bukkit.inventory.meta.ArmorMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.trim.ArmorTrim

class ArmorMetaConfig(section: ConfigurationSection) : ItemMetaConfig(ArmorMeta::class) {
    val material = section.getString("material", "")
    val pattern = section.getString("pattern", "")
    val color = section.getString("color") ?: ""

    override fun storeData(item: MenuItemView, player: TabPlayer) = mapOf(
        "material" to property(item, player, material),
        "pattern" to property(item, player, pattern),
        "color" to property(item, player, color)
    )

    override fun refresh(meta: ItemMeta, data: Map<String, Property>, force: Boolean) {
        val material = data["material"]!!
        val pattern = data["pattern"]!!
        if (material.update().or(pattern.update()) || force) {
            val trimMaterial = getFromRegistry(RegistryKey.TRIM_MATERIAL, material)
            val trimPattern = getFromRegistry(RegistryKey.TRIM_PATTERN, pattern)

            if (trimMaterial != null && trimPattern != null)
                (meta as ArmorMeta).trim = ArmorTrim(trimMaterial, trimPattern)
        }

        if (meta !is LeatherArmorMeta) return
        val color = data["color"]!!
        if (!color.update() && !force) return
        try {
            val rgb = color.get().removePrefix("#").hexToInt()
            meta.setColor(Color.fromRGB(rgb))
        } catch (_: Exception) {}
    }

}