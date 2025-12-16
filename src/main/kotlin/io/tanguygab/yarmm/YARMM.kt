package io.tanguygab.yarmm

import io.tanguygab.yarmm.config.MainConfig
import io.tanguygab.yarmm.listeners.ActionsListener
import io.tanguygab.yarmm.listeners.InventoryListener
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.event.plugin.TabLoadEvent
import me.neznamy.tab.shared.TAB
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

val TabPlayer.bukkit
    get() = player as Player

class YARMM : JavaPlugin() {

    lateinit var menuManager: MenuManager
    lateinit var config: MainConfig

    override fun onEnable() {
        TAB.getInstance().eventBus!!.register(TabLoadEvent::class.java) {
            onDisable()
            load()
        }
        load()
    }

    private fun load() {
        saveDefaultConfig()
        reloadConfig()
        config = MainConfig(getResource("config.yml"), dataFolder)

        menuManager = MenuManager(this)
        menuManager.load()

        listOf(
            InventoryListener(menuManager),
            ActionsListener(menuManager)
        ).forEach { server.pluginManager.registerEvents(it, this) }
    }

    override fun onDisable() {
        HandlerList.unregisterAll(this)
        menuManager.unload()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player && args.isNotEmpty() && args[0] in menuManager.menus) {
            val menu = menuManager.menus[args[0]]!!
            menuManager.openMenu(TAB.getInstance().getPlayer(sender.uniqueId)!!, menu)
        }
        return true
    }
}
