package io.tanguygab.yarmm.listeners

import io.tanguygab.yarmm.MenuCloseReason
import io.github.tanguygab.conditionalactions.hook.tab.ThreadPlaceholder
import io.tanguygab.yarmm.YARMM
import io.tanguygab.yarmm.tab
import me.neznamy.tab.shared.TAB
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent

class InventoryListener(val plugin: YARMM) : Listener {

    val sessions get() = plugin.menuManager.sessions
    val clickPlaceholder = TAB.getInstance().placeholderManager.registerPlaceholder(ThreadPlaceholder("%menu-click%"))!!

    @EventHandler
    fun onMenuOpen(e: InventoryOpenEvent) {
        val player = e.player
        if (player !is Player || player.tab !in sessions) return

        if (e.inventory != sessions[player.tab]?.inventory)
            plugin.menuManager.closeMenu(player.tab!!, MenuCloseReason.OPEN_NEW)
    }

    @EventHandler
    fun onMenuClose(e: InventoryCloseEvent) {
        if (e.reason === InventoryCloseEvent.Reason.OPEN_NEW) return

        val player = e.player
        if (player !is Player || player.tab !in sessions) return

        val reason = when (e.reason) {
            // Also called when YARMM closes the menu
            InventoryCloseEvent.Reason.DISCONNECT -> {
                // If closed inv is different from current session,
                // YARMM opened a new menu (most likely from an OpenAction)
                if (e.inventory != sessions[player.tab]?.inventory) return
                MenuCloseReason.UNLOAD
            }
            else -> MenuCloseReason.PLAYER
        }
        plugin.menuManager.closeMenu(player.tab!!, reason)
    }

    @EventHandler
    fun onMenuClick(e: InventoryClickEvent) {
        val player = e.whoClicked
        if (player !is Player || player.tab !in sessions) return
        e.isCancelled = true

        val item = sessions[player.tab!!]!!.items.findLast { it.getSlot() == e.rawSlot && it.isVisible() }
        if (item == null || item.isOnCooldown()) return

        item.lastClick = System.currentTimeMillis()
        plugin.server.asyncScheduler.runNow(plugin) {
            Thread.currentThread().let {
                clickPlaceholder.updateValue(e.click.name)
                item.config.clickActions.execute(player)
                clickPlaceholder.updateValue(null)
            }
        }
    }

//    @EventHandler
//    fun onMenuDrag(e: InventoryDragEvent) {}

}