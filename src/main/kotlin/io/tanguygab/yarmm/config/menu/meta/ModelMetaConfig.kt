package io.tanguygab.yarmm.config.menu.meta

import io.tanguygab.yarmm.config.menu.ItemMetaConfig
import io.tanguygab.yarmm.config.menu.toColor
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.NamespacedKey
import org.bukkit.inventory.meta.ItemMeta

class ModelMetaConfig(section: ConfigurationSection) : ItemMetaConfig(ItemMeta::class) {
    val key = section.getString("key") ?: ""
    val data = section.getMap<String, List<Any>>("data") ?: mapOf()

    override fun storeData(item: MenuItemView, player: TabPlayer): MutableMap<String, Property> {
        val map = mutableMapOf("key" to property(item, player, key))
        listOf("floats", "flags", "strings", "colors").filter { it in data }.forEach {
            map["data-$it"] = property(item, player, data[it]!!.joinToString("\n"))
        }
        return map
    }

    fun refreshModelData(data: Map<String, Property>, force: Boolean, type: String, run: (List<String>) -> Unit) {
        data["data-$type"]?.let { if (it.update() || force) run(it.get().split("\n")) }
    }

    override fun refresh(meta: ItemMeta, data: Map<String, Property>, force: Boolean) {
        val key = data["key"]!!
        if (key.update() || force) meta.itemModel = NamespacedKey.fromString(key.get())


        @Suppress("UnstableApiUsage")
        meta.setCustomModelDataComponent(meta.customModelDataComponent.apply {
            refreshModelData(data, force, "floats") { l -> floats = l.mapNotNull { it.toFloatOrNull() }}
            refreshModelData(data, force, "flags") { l -> flags = l.map { it.toBoolean() }}
            refreshModelData(data, force, "strings") { l -> strings = l }
            refreshModelData(data, force, "colors") { l -> colors = l.mapNotNull { it.toColor() }}
        })

    }

}