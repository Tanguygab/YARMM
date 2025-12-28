package io.tanguygab.yarmm.config.menu.meta

import io.papermc.paper.registry.RegistryKey
import io.tanguygab.yarmm.config.menu.ItemMetaConfig
import io.tanguygab.yarmm.config.menu.getFromRegistry
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.MusicInstrumentMeta

class InstrumentMetaConfig(val instrument: String) : ItemMetaConfig(MusicInstrumentMeta::class) {
    override fun storeData(item: MenuItemView, player: TabPlayer) = mapOf(
        "instrument" to property(item, player, instrument)
    )

    override fun refresh(meta: ItemMeta, data: Map<String, Property>, force: Boolean) {
        val instrument = data["instrument"]!!
        if (instrument.update() || force) {
            (meta as MusicInstrumentMeta).instrument = instrument.getFromRegistry(RegistryKey.INSTRUMENT)
        }
    }

}