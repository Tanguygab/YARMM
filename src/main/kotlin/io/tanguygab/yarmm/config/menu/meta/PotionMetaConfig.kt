package io.tanguygab.yarmm.config.menu.meta

import io.papermc.paper.registry.RegistryKey
import io.tanguygab.yarmm.config.menu.ItemMetaConfig
import io.tanguygab.yarmm.config.menu.getFromRegistry
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.Color
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionType

class PotionMetaConfig(section: ConfigurationSection) : ItemMetaConfig(PotionMeta::class) {
    val type = section.getString("type") ?: "water"
    val color = section.getString("color") ?: ""
    val effects = section.getStringList("effects") ?: emptyList()

    override fun storeData(item: MenuItemView, player: TabPlayer) = mapOf(
        "type" to property(item, player, type),
        "color" to property(item, player, color),
        "effects" to property(item, player, effects.joinToString("\n"))
    )

    override fun refresh(meta: ItemMeta, data: Map<String, Property>, force: Boolean) {
        meta as PotionMeta

        val type = data["type"]!!
        if (type.update() || force) meta.basePotionType = PotionType.entries.find { it.name.equals(type.get(), ignoreCase = true) } ?: PotionType.WATER

        val color = data["color"]!!
        if (color.update() || force) meta.color = try {
            val rgb = color.get().removePrefix("#").hexToInt()
            Color.fromRGB(rgb)
        } catch (_: Exception) { null }

        val effects = data["effects"]!!
        if (effects.update() || force) {
            meta.clearCustomEffects()
            effects.get()
                .split("\n")
                .map { it.split(" ") }
                .filter { it.size <= 3 }
                .mapNotNull {
                    val type = it[0].getFromRegistry(RegistryKey.MOB_EFFECT)
                    val duration = it.getOrNull(1)?.toIntOrNull() ?: 0
                    val amplifier = it.getOrNull(2)?.toIntOrNull() ?: 0
                    if (type == null) null else PotionEffect(type, duration, amplifier)
                }.forEach { meta.addCustomEffect(it, false) }
        }
    }

}