package io.tanguygab.yarmm.config.menu.meta

import io.tanguygab.yarmm.YARMM
import io.tanguygab.yarmm.config.menu.ItemMetaConfig
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.meta.ItemMeta

class TooltipMetaConfig(section: ConfigurationSection) : ItemMetaConfig(ItemMeta::class) {
    val name = section.getString("name") ?: ""
    val lore = section.getStringList("lore") ?: emptyList()
    val hide = section.getObject("hide")?.toString() ?: "false"
    val style = section.getString("style") ?: ""
    val rarity = section.getString("rarity") ?: ""
    val glow = section.getObject("glow")?.toString() ?: ""
    val unbreakable = section.getObject("unbreakable")?.toString() ?: "false"
    // use conditions instead?

    override fun storeData(item: MenuItemView, player: TabPlayer) = mapOf(
        "name" to property(item, player, name),
        "lore" to property(item, player, lore.joinToString("\n")),
        "hide" to property(item, player, hide),
        "style" to property(item, player, style),
        "rarity" to property(item, player, rarity),
        "glow" to property(item, player, glow),
        "unbreakable" to property(item, player, unbreakable)
    )

    override fun refresh(meta: ItemMeta, data: Map<String, Property>, force: Boolean) {
        val name = data["name"]!!
        if (name.update() || force) meta.displayName(if (name.get().isEmpty()) null else mm.deserialize(YARMM.INSTANCE.config.itemNamePrefix + name.get()))

        val lore = data["lore"]!!
        if (lore.update() || force) meta.lore(
            if (lore.get().isEmpty()) listOf()
            else lore.get().split("\n").map { mm.deserialize(YARMM.INSTANCE.config.itemLorePrefix + it) }
        )

        val hide = data["hide"]!!
        if (hide.update() || force) meta.isHideTooltip = hide.get().equals("true", ignoreCase = true)

        val style = data["style"]!!
        if (style.update() || force) meta.tooltipStyle = NamespacedKey.fromString(style.get())

        val rarity = data["rarity"]!!
        if (rarity.update() || force) meta.setRarity(ItemRarity.entries.find { it.name.equals(rarity.get(), ignoreCase = true) })

        val glow = data["glow"]!!
        if (glow.update() || force) meta.setEnchantmentGlintOverride(if (glow.get().isEmpty()) null else glow.get().equals("true", ignoreCase = true))

        val unbreakable = data["unbreakable"]!!
        if (unbreakable.update() || force) meta.isUnbreakable = unbreakable.get().equals("true", ignoreCase = true)
    }
}