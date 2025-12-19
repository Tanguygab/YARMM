package io.tanguygab.yarmm

import io.tanguygab.yarmm.inventory.MenuInventory
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.features.types.RefreshableFeature
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class MenuSession(
    val plugin: YARMM,
    val player: TabPlayer,
    val menu: MenuInventory
) : RefreshableFeature() {

    override fun getFeatureName() = "YARMM"
    override fun getRefreshDisplayName() = "Updating menu title"

    val data = MenuData(Property(this, player, menu.config.title))

    lateinit var inventory: Inventory
    val items = mutableListOf<MenuItemView>()

    init {
        menu.config.items.forEach { item ->
            item.slots.forEach { slot ->
                val i = MenuItemView(item, slot, player, data)
                items.add(i)
                TAB.getInstance().featureManager.registerFeature("menu-item-${player.name}-${slot}", i)
            }
        }
        TAB.getInstance().featureManager.registerFeature("menu-session-${player.name}", this)
        refresh(player, true)
    }

    fun TabPlayer.openInventory() {
        bukkit.scheduler.run(plugin, { bukkit.openInventory(inventory) }, null)
    }
    fun TabPlayer.closeInventory() {
        if (plugin.server.isStopping) return
        bukkit.scheduler.run(plugin, { bukkit.closeInventory(InventoryCloseEvent.Reason.DISCONNECT) }, null)
    }



    fun close(reason: MenuCloseReason): Boolean {
        if (reason != MenuCloseReason.UNLOAD && !menu.config.closeActions.execute(player.bukkit)) {
            if (reason != MenuCloseReason.REOPEN) player.openInventory()
            return false
        }

        TAB.getInstance().featureManager.apply {
            val usages = TAB.getInstance().placeholderManager.placeholderUsage.values
            usages.forEach {
                it.removeAll(items.toSet())
                it.remove(this@MenuSession)
            }
            items.forEach { item -> unregisterFeature("menu-item-${player.name}-${item.slot}") }
            unregisterFeature("menu-session-${player.name}")
        }

        // Prevent cursor position from resetting when changing menu
        if (reason != MenuCloseReason.OPEN_NEW) player.closeInventory()
        return true
    }

    override fun refresh(player: TabPlayer, force: Boolean) {
        if (force || data.title.update()) {
            inventory = menu.get(player, data)
            items.forEach { it.inventory = inventory }

            player.openInventory()
        }
    }
}

data class MenuData(
    val title: Property,
    val materials: MutableMap<MenuItemView, Property> = mutableMapOf(),
    val amounts: MutableMap<MenuItemView, Property> = mutableMapOf(),
    val names: MutableMap<MenuItemView, Property> = mutableMapOf(),
    val lores: MutableMap<MenuItemView, Property> = mutableMapOf(),
    val slots: MutableMap<MenuItemView, Property> = mutableMapOf(),
    val viewConditions: MutableMap<MenuItemView, Property> = mutableMapOf(),
)