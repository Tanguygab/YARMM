package io.tanguygab.yarmm.listeners

import io.tanguygab.yarmm.MenuManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent

class InventoryListener(val manager: MenuManager) : Listener {

    @EventHandler
    fun onMenuOpen(e: InventoryOpenEvent) {
        println(e.inventory)
    }

    @EventHandler
    fun onMenuClose(e: InventoryCloseEvent) {
        println(e.inventory)
    }

    @EventHandler
    fun onMenuClick(e: InventoryClickEvent) {
        println(e.inventory)
    }

//    @EventHandler
//    fun onMenuDrag(e: InventoryDragEvent) {}

}