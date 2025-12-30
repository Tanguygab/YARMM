package io.tanguygab.yarmm

import io.github.tanguygab.conditionalactions.Utils
import io.github.tanguygab.conditionalactions.hook.tab.ArgPlaceholders
import io.tanguygab.yarmm.config.menu.MenuConfig
import io.tanguygab.yarmm.inventory.MenuInventory
import me.neznamy.tab.shared.config.file.YamlConfigurationFile
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl
import me.neznamy.tab.shared.platform.TabPlayer
import java.io.File

class MenuManager(val plugin: YARMM) {

    val menus = mutableMapOf<String, MenuInventory>()
    val sessions = mutableMapOf<TabPlayer, MenuSession>()

    lateinit var argPlaceholders: ArgPlaceholders<PlayerPlaceholderImpl>

    fun load() {
        val folder = plugin.dataFolder.resolve( "menus")
        if (!folder.exists()) {
            YamlConfigurationFile(plugin.getResource("menus/default-menu.yml"), File(folder, "default-menu.yml"))
        }
        Utils.loadFiles(folder, "") { file, path ->
            val name = (if (plugin.config.includeFilePath) path.substringAfter("menus/") else "") + file.name.substringBeforeLast(".yml")
            menus[name] = MenuInventory(name, MenuConfig.fromFile(file))
        }


        argPlaceholders = object : ArgPlaceholders<PlayerPlaceholderImpl>("menu-") {
            override fun new(identifier: String, default: String) = PlayerPlaceholderImpl(identifier, -1) { default }
            override fun update(placeholder: PlayerPlaceholderImpl, player: TabPlayer?, value: String) = placeholder.updateValue(player!!, value)
        }
    }
    fun unload() {
        sessions.values.forEach { it.close(MenuCloseReason.UNLOAD) }
        sessions.clear()
    }

    fun openMenu(player: TabPlayer, menu: MenuInventory, args: List<String> = emptyList()): MenuSession? {
        if (!closeMenu(player, MenuCloseReason.OPEN_NEW)) return sessions[player]

        val args = args.map { it.replace("%", "") }
        argPlaceholders.update(player, args)
        if (!menu.config.openActions.execute(player.bukkit)) {
            sessions[player]?.close(MenuCloseReason.UNLOAD)
            argPlaceholders.update(player, args)
            sessions.remove(player)
            return null
        }

        return MenuSession(plugin, player, menu).apply { sessions[player] = this }
    }

    fun closeMenu(player: TabPlayer, reason: MenuCloseReason): Boolean {
        if (player !in sessions) return true
        if (sessions[player]?.close(reason) != true) return false
        if (reason === MenuCloseReason.OPEN_NEW) return true

        argPlaceholders.update(player)
        sessions.remove(player)
        return true
    }
}