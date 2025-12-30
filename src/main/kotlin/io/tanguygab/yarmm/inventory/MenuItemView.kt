package io.tanguygab.yarmm.inventory

import io.papermc.paper.registry.RegistryKey
import io.tanguygab.yarmm.MenuSession
import io.tanguygab.yarmm.config.menu.MenuItemConfig
import io.tanguygab.yarmm.config.menu.getFromRegistry
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.features.types.RefreshableFeature
import me.neznamy.tab.shared.platform.TabPlayer
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.IdentityHashMap

class MenuItemView(
    val config: MenuItemConfig,
    val slot: String,
    val session: MenuSession
): RefreshableFeature() {
    override fun getFeatureName() = "YARMM"
    override fun getRefreshDisplayName() = "Updating menu items"

    val data get() = session.data

    var item = ItemStack(Material.STONE)
    var inventory: Inventory? = null
        set(value) {
            field = value
            value?.setItem(getSlot(), item)
        }

    fun getSlot() = data.slots[this]!!.get().toIntOrNull() ?: 0
    fun isVisible() = (data.displayConditions[this]?.get() ?: "true") == "true"

    init {
        IdentityHashMap<MutableMap<MenuItemView, Property>, String>().apply {
            putAll(listOf(
                data.slots to slot,
                data.materials to config.material,
                data.amounts to config.amount,
                data.names to config.name,
                data.lores to config.lore.joinToString("\n"),
            ))
            if (config.displayCondition != null) put(data.displayConditions, "%ca-condition:${config.displayCondition.name}%")
        }.forEach { (map, raw) -> map[this] = Property(this, session.player, raw
            .replace("%slot%", slot)
            .replace("{slot}", if (slot.contains("%")) "{tab_placeholder_${slot.removeSurrounding("%")}}" else slot)
        ) }

        val enchantments = mutableMapOf<Property, Property>()
        config.enchantments.forEach { (enchant, level) ->
            val key = Property(this, session.player, enchant.replace("{slot}", slot))
            val value = Property(this, session.player, level.replace("{slot}", slot))
            enchantments[key] = value
        }
        data.enchantments[this] = enchantments

        data.meta[this] = config.metas.associateWith { it.storeData(this, session.player) }

        refresh(session.player, true)
    }

    override fun refresh(player: TabPlayer, force: Boolean) {
        if (player !== session.player) return
        val oldSlot = getSlot()

        data.displayConditions[this]?.update()
        val visible = isVisible()

        if ((data.slots[this]!!.update() || !visible) && inventory?.getItem(oldSlot) == item) {
            inventory?.setItem(oldSlot, session.items.findLast { it.getSlot() == oldSlot && it.isVisible() }?.item)
        }

        val material = data.materials[this]!!
        if (force || material.update()) {
            item = ItemStack(Material.getMaterial(material.get().uppercase()) ?: Material.STONE)
            item.addItemFlags(*config.flags.toTypedArray())
        }

        val amount = data.amounts[this]!!
        if (force || amount.update()) item.amount = amount.get().toIntOrNull() ?: 1

        val name = data.names[this]!!
        val lore = data.lores[this]!!

        data.enchantments[this]!!.forEach { (enchant, level) ->
            val old = enchant.get()
            if (!enchant.update().or(level.update()) && !force) return@forEach

            val oldType = old.getFromRegistry(RegistryKey.ENCHANTMENT)
            val type = enchant.getFromRegistry(RegistryKey.ENCHANTMENT)

            if (oldType != type && oldType != null) item.removeEnchantment(oldType)
            if (type != null) item.addUnsafeEnchantment(type, level.get().toIntOrNull() ?: 0)
        }

        item.itemMeta = item.itemMeta.apply {
            if (force || name.update()) {
                displayName(if (name.get().isEmpty()) null else mm.deserialize(session.plugin.config.itemNamePrefix + name.get()))
            }
            if (force || lore.update()) {
                lore(if (lore.get().isEmpty()) listOf() else lore.get().split("\n").map { mm.deserialize(session.plugin.config.itemLorePrefix + it) })
            }
            config.metas
                .filter { it.isMeta(this) }
                .forEach { it.refresh(this, data.meta[this@MenuItemView]!![it]!!, force) }
        }
        if (session.items.findLast { it.getSlot() == getSlot() && it.isVisible() } == this) inventory?.setItem(getSlot(), item)
    }

    companion object {
        private val mm = MiniMessage.miniMessage()
    }
}
