package io.tanguygab.yarmm.config.menu.meta

import io.tanguygab.yarmm.config.menu.ItemMetaConfig
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.inventory.meta.CompassMeta
import org.bukkit.inventory.meta.ItemMeta

class CompassMetaConfig(section: ConfigurationSection) : ItemMetaConfig(CompassMeta::class) {
    val world = section.getString("world") ?: "%world%"
    val x = section.getObject("x", 0).toString()
    val y = section.getObject("y", 0).toString()
    val z = section.getObject("z", 0).toString()

    override fun storeData(item: MenuItemView, player: TabPlayer) = mapOf(
        "world" to property(item, player, world),
        "x" to property(item, player, x),
        "y" to property(item, player, y),
        "z" to property(item, player, z)
    )

    override fun refresh(meta: ItemMeta, data: Map<String, Property>, force: Boolean) {
        meta as CompassMeta

        val location = meta.lodestone ?: Location(Bukkit.getWorlds()[0], 0.0, 0.0, 0.0)

        val world = data["world"]!!
        if (world.update() || force) location.world = Bukkit.getWorld(world.get())

        val x = data["x"]!!
        if (x.update() || force) location.x = x.get().toDoubleOrNull() ?: 0.0
        val y = data["y"]!!
        if (y.update() || force) location.y = y.get().toDoubleOrNull() ?: 0.0
        val z = data["z"]!!
        if (z.update() || force) location.z = z.get().toDoubleOrNull() ?: 0.0

        meta.lodestone = location
    }

}