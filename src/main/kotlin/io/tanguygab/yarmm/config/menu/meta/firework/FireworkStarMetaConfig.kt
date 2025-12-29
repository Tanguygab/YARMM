package io.tanguygab.yarmm.config.menu.meta.firework

import io.tanguygab.yarmm.config.menu.ItemMetaConfig
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.DyeColor
import org.bukkit.FireworkEffect
import org.bukkit.inventory.meta.FireworkEffectMeta
import org.bukkit.inventory.meta.ItemMeta
import kotlin.text.split

fun String.toFireworkEffect(): FireworkEffect? {
    val args = split(" ")
    val type = FireworkEffect.Type.entries.find { type -> type.name.equals(args[0], ignoreCase = true) }
    val colors = args.getOrNull(1)?.split(",")?.mapNotNull {  color ->
        DyeColor.entries.find { entry -> entry.name.equals(color, ignoreCase = true) }?.fireworkColor
    } ?: emptyList()
    val fadeColors = args.getOrNull(2)?.split(",")?.mapNotNull { color ->
        DyeColor.entries.find { entry -> entry.name.equals(color, ignoreCase = true) }?.fireworkColor
    } ?: emptyList()

    return if (type == null) null else FireworkEffect
        .builder()
        .with(type)
        .withColor(colors)
        .withFade(fadeColors)
        .flicker(args.getOrNull(3).equals("true", ignoreCase = true))
        .trail(args.getOrNull(4).equals("true", ignoreCase = true))
        .build()
}

class FireworkStarMetaConfig(val effect: String) : ItemMetaConfig(FireworkEffectMeta::class) {
    override fun storeData(item: MenuItemView, player: TabPlayer) = mapOf(
        "effect" to property(item, player, effect)
    )

    override fun refresh(meta: ItemMeta, data: Map<String, Property>, force: Boolean) {
        val effect = data["effect"]!!
        if (effect.update() || force) (meta as FireworkEffectMeta).effect = effect.get().toFireworkEffect()
    }

}