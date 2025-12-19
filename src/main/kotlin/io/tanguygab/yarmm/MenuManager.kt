package io.tanguygab.yarmm

import io.tanguygab.yarmm.config.MenuConfig
import io.tanguygab.yarmm.inventory.MenuInventory
import me.neznamy.tab.shared.config.file.YamlConfigurationFile
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
        val folder = File(plugin.dataFolder, "menus")
        if (!folder.exists()) {
            YamlConfigurationFile(plugin.getResource("menus/default-menu.yml"), File(folder, "default-menu.yml"))
        }
        loadFiles(plugin.dataFolder, "menus")
    }
    fun unload() {
        sessions.values.forEach { it.close(MenuCloseReason.UNLOAD) }
        sessions.clear()
    }

    fun openMenu(player: TabPlayer, menu: MenuInventory): MenuSession? {
        if (!closeMenu(player, MenuCloseReason.OPEN_NEW)) return sessions[player]

        if (!menu.config.openActions.execute(player.bukkit)) {
            sessions[player]?.close(MenuCloseReason.UNLOAD)
            sessions.remove(player)
            return null
        }

        return MenuSession(plugin, player, menu).apply { sessions[player] = this }
    }

    fun closeMenu(player: TabPlayer, reason: MenuCloseReason): Boolean {
        if (player !in sessions) return true
        return sessions[player]?.close(reason) == true && (reason == MenuCloseReason.OPEN_NEW || sessions.remove(player) != null)
    }
}