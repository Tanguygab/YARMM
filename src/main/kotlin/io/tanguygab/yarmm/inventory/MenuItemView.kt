package io.tanguygab.yarmm.inventory

import io.tanguygab.yarmm.MenuData
import io.tanguygab.yarmm.config.MenuItemConfig
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.features.types.RefreshableFeature
import me.neznamy.tab.shared.platform.TabPlayer
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.IdentityHashMap
import kotlin.text.split
import kotlin.text.toIntOrNull

class MenuItemView(
    config: MenuItemConfig,
    val slot: String,
    player: TabPlayer,
    val data: MenuData
): RefreshableFeature() {
    override fun getFeatureName() = "YARMM"
    override fun getRefreshDisplayName() = "Updating menu items"

    var item = ItemStack(Material.STONE)
    var inventory: Inventory? = null
        set(value) {
            field = value
            value?.setItem(getSlot(), item)
        }

    fun getSlot() = data.slots[this]!!.get().toIntOrNull() ?: 0

    init {
        IdentityHashMap<MutableMap<MenuItemView, Property>, String>().apply {
            putAll(listOf(
                data.slots to slot,
                data.materials to config.material,
                data.amounts to config.amount,
                data.names to config.name,
                data.lores to config.lore.joinToString("\n"))
            )
        }.forEach { (map, raw) -> map[this] = Property(this, player, raw.replace("{slot}", slot)) }
        refresh(player, true)
    }

    override fun refresh(player: TabPlayer, force: Boolean) {
        val oldSlot = getSlot()
        if (data.slots[this]!!.update() && inventory?.getItem(oldSlot) == item) {
            inventory?.setItem(oldSlot, null)
        }

        val material = data.materials[this]!!
        if (force || material.update()) item = ItemStack(Material.getMaterial(material.get().uppercase()) ?: Material.STONE)

        val amount = data.amounts[this]!!
        if (force || amount.update()) item.amount = amount.get().toIntOrNull() ?: 1

        val name = data.names[this]!!
        val lore = data.lores[this]!!

        item.itemMeta = item.itemMeta.apply {
            if (force || name.update()) displayName(mm.deserialize(name.get()))
            if (force || lore.update()) {
                lore(if (lore.get().isEmpty()) listOf() else lore.get().split("\n").map { mm.deserialize(it) })
            }
        }

        inventory?.setItem(getSlot(), item)
    }

    companion object {
        val mm = MiniMessage.miniMessage()
    }
}
