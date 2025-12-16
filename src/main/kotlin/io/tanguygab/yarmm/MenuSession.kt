package io.tanguygab.yarmm

import io.tanguygab.yarmm.inventory.MenuInventory
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.features.types.RefreshableFeature
import me.neznamy.tab.shared.platform.TabPlayer

class MenuSession(
    val plugin: YARMM,
    val player: TabPlayer,
    val menu: MenuInventory
) : RefreshableFeature() {

    override fun getFeatureName() = "YARMM"
    override fun getRefreshDisplayName() = "Updating menu title"

    val data = MenuData(Property(this, player, menu.config.title))

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

    fun close() {
        if (!menu.config.closeActions.execute(player.bukkit)) return

        player.bukkit.closeInventory()
        items.forEach {
            TAB.getInstance().featureManager.unregisterFeature("menu-item-${player.name}-${it.slot}")
        }
        TAB.getInstance().featureManager.unregisterFeature("menu-session-${player.name}")
    }

    override fun refresh(player: TabPlayer, force: Boolean) {
        if (force || data.title.update()) {
            val inventory = menu.get(player, data)
            items.forEach { it.inventory = inventory }

            player.bukkit.scheduler.run(plugin, { player.bukkit.openInventory(inventory) }, null)

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
)