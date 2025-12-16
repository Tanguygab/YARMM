package io.tanguygab.yarmm

import io.tanguygab.yarmm.config.MenuConfig
import io.tanguygab.yarmm.inventory.MenuInventory
import me.neznamy.tab.shared.platform.TabPlayer
import java.io.File

class MenuManager(val plugin: YARMM) {

    val menus = mutableMapOf<String, MenuInventory>()
    val sessions = mutableMapOf<TabPlayer, MenuSession>()

    private fun loadFiles(folder: File, name: String) {
        File(folder, name).listFiles().forEach {
            if (it.isDirectory) {
                loadFiles(it, it.name)
                return
            }
            if (!it.name.endsWith(".yml")) return
            menus[it.name.substringBeforeLast(".yml")] = MenuInventory(MenuConfig(it, plugin.config))
        }
    }

    fun load() {
        plugin.saveResource("menus/default-menu.yml", false)
        loadFiles(plugin.dataFolder, "menus")

    }
    fun unload() {
        sessions.values.forEach { it.close() }
    }

    fun openMenu(player: TabPlayer, menu: MenuInventory): MenuSession? {
        if (!menu.config.openActions.execute(player.bukkit)) return null
        closeMenu(player)
        return MenuSession(plugin, player, menu).apply { sessions[player] = this }
    }

    fun closeMenu(player: TabPlayer) {
        sessions[player]?.close()
        sessions.remove(player);
    }
}