package io.tanguygab.yarmm.config.menu.meta

import io.tanguygab.yarmm.config.menu.ItemMetaConfig
import io.tanguygab.yarmm.inventory.MenuItemView
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.ItemMeta

class BookMetaConfig(section: ConfigurationSection) : ItemMetaConfig(BookMeta::class) {
    val title = section.getString("title", "")
    val author = section.getString("author", "")
    val generation = section.getString("generation") ?: ""

    override fun storeData(item: MenuItemView, player: TabPlayer) = mapOf(
        "title" to property(item, player, title),
        "author" to property(item, player, author),
        "generation" to property(item, player, generation)
    )

    override fun refresh(meta: ItemMeta, data: Map<String, Property>, force: Boolean) {
        meta as BookMeta
        val title = data["title"]!!
        if (title.update() || force) meta.title(mm.deserialize(title.get()))

        val author = data["author"]!!
        if (author.update() || force) meta.author(mm.deserialize(author.get()))

        val generation = data["generation"]!!
        if (generation.update() || force) meta.generation = BookMeta.Generation.entries.find { it.name == generation.get() }
    }

}