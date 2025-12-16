package io.tanguygab.yarmm.inventory

import io.tanguygab.yarmm.MenuData
import io.tanguygab.yarmm.bukkit
import io.tanguygab.yarmm.config.MenuConfig
import me.neznamy.tab.shared.platform.TabPlayer
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory

class MenuInventory(val config: MenuConfig) {
    fun get(player: TabPlayer, data: MenuData): Inventory {
        val title = MiniMessage.miniMessage().deserialize(data.title.get())

        return if (config.type == InventoryType.CHEST)
            Bukkit.createInventory(player.bukkit, config.rows * 9, title)
        else Bukkit.createInventory(player.bukkit, config.type, title)
    }
}