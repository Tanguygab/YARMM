package io.tanguygab.yarmm

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.github.tanguygab.conditionalactions.ConditionalActions
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.tanguygab.yarmm.commands.MenuArgumentType
import io.tanguygab.yarmm.config.LangConfig
import io.tanguygab.yarmm.config.MainConfig
import io.tanguygab.yarmm.inventory.MenuInventory
import io.tanguygab.yarmm.listeners.ActionsListener
import io.tanguygab.yarmm.listeners.InventoryListener
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.event.plugin.TabLoadEvent
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.config.file.YamlConfigurationFile
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import kotlin.math.max
import kotlin.math.min

val TabPlayer.bukkit get() = player as Player
val Player.tab get() = TAB.getInstance().getPlayer(uniqueId)

class YARMM : JavaPlugin() {

    lateinit var menuManager: MenuManager
    lateinit var config: MainConfig
    lateinit var lang: LangConfig

    private fun getMenuPage(ctx: CommandContext<CommandSourceStack>, page: Int): Int {
        val max = Math.ceilDiv(menuManager.menus.size, config.listMaxEntries)
        val page = min(max(page, 1), max)

        var message = lang.getCommandsListHeader(menuManager.menus.size, page, max)
        menuManager.menus.keys.forEach { message = message.append(lang.getCommandsListLine(ctx.source.sender.name, it)) }
        ctx.source.sender.sendMessage(message)
        return Command.SINGLE_SUCCESS
    }

    private fun openMenu(ctx: CommandContext<CommandSourceStack>, args: String? = null): Int {
        val player = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
        val menu = ctx.getArgument("menu", MenuInventory::class.java)

        player.resolve(ctx.source).forEach { p ->
            menuManager.openMenu(p.tab!!, menu, args?.split(" ") ?: emptyList())
        }
        return Command.SINGLE_SUCCESS
    }

    override fun onEnable() {
        val command = Commands.literal("yarmm")
            .then(Commands.literal("list")
                .executes { getMenuPage(it, 1) }
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                    .suggests { _, builder ->
                        for (i in 1 .. menuManager.menus.size / config.listMaxEntries) builder.suggest(i)
                        builder.buildFuture()
                    }
                    .executes { getMenuPage(it, it.getArgument("page", Int::class.java)) }
            ))
            .then(Commands.literal("open")
                .then(Commands.argument("player", ArgumentTypes.players())
                    .then(Commands.argument("menu", MenuArgumentType(this))
                        .executes { openMenu(it) }
                        .then(Commands.argument("args", StringArgumentType.greedyString())
                            .executes {
                                val args = it.getArgument("args", String::class.java)
                                openMenu(it, args)
                            }
            ))))
            .then(Commands.literal("info")
                .executes {
                    fun version(plugin: String) = server.pluginManager.getPlugin(plugin)?.pluginMeta?.version
                    it.source.sender.sendRichMessage("<dark_gray><strikethrough>                                                    </strikethrough>\n" +
                            "<green>YARMM<gray>: <gold>${pluginMeta.version}\n" +
                            "<green>ConditionalActions<gray>: <gold>${version("ConditionalActions")}\n" +
                            "<red>TAB<gray>: <gold>${version("TAB")}\n" +
                            "<aqua>PlaceholderAPI<gray>: <gold>${version("PlaceholderAPI")}\n" +
                            "\n" +
                            "<gray>${menuManager.menus.size} Menus:</gray> ${menuManager.menus.keys.joinToString(", ")} \n" +
                            "<dark_gray><strikethrough>                                                    </strikethrough>\n")
                    Command.SINGLE_SUCCESS
                }
            )
            .then(Commands.literal("share")
                .then(Commands.argument("menu", MenuArgumentType(this))
                    .executes {
                        Command.SINGLE_SUCCESS
                    }
                )
            )
            .then(Commands.literal("generate-commands")
                .executes {
                    val ca = server.pluginManager.getPlugin("ConditionalActions")!!
                    val file = File(ca.dataFolder, "commands/yarmm.yml")
                    if (!file.exists()) file.createNewFile()
                    val yaml = YamlConfigurationFile(null, file)

                    val menus = menuManager.menus.keys.filter { menu -> menu !in yaml.values.keys }
                    menus.forEach { menu -> yaml[menu] = mapOf("actions" to listOf("console: yarmm open %player% $menu %conditionalactions_args%")) }
                    server.dispatchCommand(it.source.sender, "conditionalactions reload")

                    it.source.sender.sendRichMessage("<green>${menus.size} menu commands generated!")
                    Command.SINGLE_SUCCESS
                }
            )

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(command.build(), listOf("menumanager", "mm", "menus"))
        }

        TAB.getInstance().eventBus!!.register(TabLoadEvent::class.java) {
            onDisable()
            load()
        }
        load()
    }

    private fun load() {
        saveDefaultConfig()
        reloadConfig()
        config = MainConfig(this)
        lang = LangConfig(this)

        menuManager = MenuManager(this)

        listOf(
            InventoryListener(this),
            ActionsListener(this)
        ).forEach { server.pluginManager.registerEvents(it, this) }

        server.globalRegionScheduler.run(this) {
            menuManager.load()
        }
    }

    override fun onDisable() {
        HandlerList.unregisterAll(this)
        menuManager.unload()
    }

}
