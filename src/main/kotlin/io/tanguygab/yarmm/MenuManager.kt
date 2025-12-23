package io.tanguygab.yarmm

import io.tanguygab.yarmm.config.MenuConfig
import io.tanguygab.yarmm.inventory.MenuInventory
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.config.file.YamlConfigurationFile
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl
import me.neznamy.tab.shared.platform.TabPlayer
import java.io.File

class MenuManager(val plugin: YARMM) {

    val menus = mutableMapOf<String, MenuInventory>()
    val sessions = mutableMapOf<TabPlayer, MenuSession>()

    lateinit var argsPlaceholder: PlayerPlaceholderImpl
    lateinit var argsSizePlaceholder: PlayerPlaceholderImpl
    val argPlaceholders = mutableListOf<PlayerPlaceholderImpl>()

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

        TAB.getInstance().placeholderManager.apply {
            argsPlaceholder = registerPlayerPlaceholder("%menu-args%", -1) { "" }
            argsSizePlaceholder = registerPlayerPlaceholder("%menu-args-size%", -1) { "0" }
        }
    }
    fun unload() {
        sessions.values.forEach { it.close(MenuCloseReason.UNLOAD) }
        sessions.clear()
    }

    fun updatePlaceholders(player: TabPlayer, args: List<String>) {
        argsPlaceholder.updateValue(player, args.joinToString(" "))
        argsSizePlaceholder.updateValue(player, args.size.toString())
        args.forEachIndexed { index, arg ->
            val placeholder = if (index < argPlaceholders.size) argPlaceholders[index]
            else TAB.getInstance().placeholderManager.registerPlayerPlaceholder("%menu-arg-$index%", -1) { "" }.let {
                argPlaceholders.add(it)
                it
            }
            placeholder.updateValue(player, arg)
        }
        if (args.size < argPlaceholders.size) argPlaceholders.forEachIndexed { index, placeholder ->
            if (index >= args.size)
            placeholder.updateValue(player, "")
        }
    }

    fun openMenu(player: TabPlayer, menu: MenuInventory, args: List<String> = emptyList()): MenuSession? {
        if (!closeMenu(player, MenuCloseReason.OPEN_NEW)) return sessions[player]

        if (!menu.config.openActions.execute(player.bukkit)) {
            sessions[player]?.close(MenuCloseReason.UNLOAD)
            sessions.remove(player)
            return null
        }

        updatePlaceholders(player, args)
        return MenuSession(plugin, player, menu).apply { sessions[player] = this }
    }

    fun closeMenu(player: TabPlayer, reason: MenuCloseReason): Boolean {
        if (player !in sessions) return true
        if (sessions[player]?.close(reason) != true) return false
        if (reason === MenuCloseReason.OPEN_NEW) return true

        updatePlaceholders(player, emptyList())
        sessions.remove(player)
        return true
    }
}