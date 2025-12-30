package io.tanguygab.yarmm.config.menu

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.tanguygab.yarmm.config.menu.meta.*
import io.tanguygab.yarmm.config.menu.meta.banner.BannerMetaConfig
import io.tanguygab.yarmm.config.menu.meta.banner.ShieldMetaConfig
import io.tanguygab.yarmm.config.menu.meta.firework.FireworkMetaConfig
import io.tanguygab.yarmm.config.menu.meta.firework.FireworkStarMetaConfig
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.Color
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.meta.ItemMeta
import kotlin.reflect.KClass

fun <T : Keyed> String.getFromRegistry(registryKey: RegistryKey<T>): T? {
    val key = NamespacedKey.fromString(lowercase()) ?: return null
    return RegistryAccess.registryAccess().getRegistry(registryKey).get(key)
}
fun <T : Keyed> Property.getFromRegistry(registryKey: RegistryKey<T>) = get().getFromRegistry(registryKey)

fun String.toColor(): Color? {
    return try {
        val rgb = removePrefix("#").hexToInt()
        Color.fromRGB(rgb)
    } catch (_: Exception) { null }
}

abstract class ItemMetaConfig(private val clazz: KClass<out ItemMeta>) {
    abstract fun storeData(item: MenuItemView, player: TabPlayer): Map<String, Property>
    abstract fun refresh(meta: ItemMeta, data: Map<String, Property>, force: Boolean)

    fun isMeta(meta: ItemMeta) = clazz.isInstance(meta)

    companion object {
        fun property(item: MenuItemView, player: TabPlayer, value: String) = Property(item, player, value
            .replace("%slot%", item.slot)
            .replace("{slot}", if (item.slot.contains("%")) "{tab_placeholder_${item.slot.removeSurrounding("%")}}" else item.slot)
        )

        val types = mutableMapOf(
            "tooltip" to TooltipMetaConfig::class,
            "model" to ModelMetaConfig::class,

            "instrument" to InstrumentMetaConfig::class,
            "head" to SkullMetaConfig::class,
            "damage" to DamageableMetaConfig::class,
            "light-level" to LightMetaConfig::class,

            "armor" to ArmorMetaConfig::class,
            "book" to BookMetaConfig::class,
            "compass" to CompassMetaConfig::class,
            "potion" to PotionMetaConfig::class,

            "firework-star" to FireworkStarMetaConfig::class,
            "firework" to FireworkMetaConfig::class,

            "patterns" to BannerMetaConfig::class,
            "shield-color" to ShieldMetaConfig::class,

            "projectiles" to CrossbowMetaConfig::class,
            "inventory" to BundleMetaConfig::class
        )
        fun fromItem(item: ConfigurationSection) = types
            .filter { it.key in item.keys }
            .map {
                val constructor = it.value.java.constructors.first()
                val arg = when (constructor.parameterTypes.first()) {
                    ConfigurationSection::class.java -> item.getConfigurationSection(it.key)
                    List::class.java -> item.getStringList(it.key)
                    else -> item.getObject(it.key).toString()
                }
                constructor.newInstance(arg) as ItemMetaConfig
            }
    }
}

