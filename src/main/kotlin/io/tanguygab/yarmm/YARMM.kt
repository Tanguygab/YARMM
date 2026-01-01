package io.tanguygab.yarmm

import com.google.common.io.CharStreams
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.tanguygab.yarmm.commands.MenuArgumentType
import io.tanguygab.yarmm.config.LangConfig
import io.tanguygab.yarmm.config.MainConfig
import io.tanguygab.yarmm.converter.DeluxeMenusConverter
import io.tanguygab.yarmm.inventory.MenuInventory
import io.tanguygab.yarmm.listeners.ActionsListener
import io.tanguygab.yarmm.listeners.InventoryListener
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.event.plugin.TabLoadEvent
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.config.file.YamlConfigurationFile
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets

val TabPlayer.bukkit get() = player as Player
val Player.tab get() = TAB.getInstance().getPlayer(uniqueId)

class YARMM : JavaPlugin() {

    lateinit var menuManager: MenuManager
    lateinit var config: MainConfig
    lateinit var lang: LangConfig
    val converters = mapOf(
        "DeluxeMenus" to DeluxeMenusConverter(this)
    )

    private fun getMenuPage(ctx: CommandContext<CommandSourceStack>, page: Int): Int {
        val max = Math.ceilDiv(menuManager.menus.size, config.listMaxEntries)
        val page = page.coerceIn(1, max)

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
        INSTANCE = this
        val command = Commands.literal("yarmm")
            .requires { it.sender.hasPermission("yarmm.command.list") }
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
                .requires { it.sender.hasPermission("yarmm.command.open") }
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
                .requires { it.sender.hasPermission("yarmm.command.info") }
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
                .requires { it.sender.hasPermission("yarmm.command.share") }
                .then(Commands.argument("menu", MenuArgumentType(this))
                    .executes { ctx ->
                        val menu = ctx.getArgument("menu", MenuInventory::class.java).name
                        val menuFile = File(dataFolder, "menus/$menu.yml")

                        (URI("${PASTE_URL}documents").toURL().openConnection() as HttpURLConnection).apply {
                            requestMethod = "POST"
                            setRequestProperty("Content-Type", "text/plain; charset=utf-8")
                            doOutput = true
                            connect()

                            getOutputStream().use { it.write(menuFile.readBytes()) }

                            getInputStream().use {
                                val json = CharStreams.toString(InputStreamReader(it, StandardCharsets.UTF_8))
                                val key = gson.fromJson(json, JsonObject::class.java)["key"].asString
                                ctx.source.sender.sendRichMessage("<green>Menu shared at <click:open_url:\"$PASTE_URL$key\">$PASTE_URL$key</click> !")
                            }
                        }
                        Command.SINGLE_SUCCESS
                    }
                )
            )
            .then(Commands.literal("generate-commands")
                .requires { it.sender.hasPermission("yarmm.command.generate-commands") }
                .executes {
                    val ca = server.pluginManager.getPlugin("ConditionalActions")!!
                    val file = File(ca.dataFolder, "commands/yarmm.yml")
                    if (!file.exists()) file.createNewFile()
                    val yaml = YamlConfigurationFile(null, file)

                    val menus = menuManager.menus.keys.filter { menu -> menu !in yaml.values.keys }
                    menus.forEach { menu -> yaml[menu] = mapOf("actions" to listOf("open: $menu %conditionalactions_args%")) }
                    server.dispatchCommand(it.source.sender, "conditionalactions reload")

                    it.source.sender.sendRichMessage("<green>${menus.size} menu commands generated!")
                    Command.SINGLE_SUCCESS
                }
            )
            .then(Commands.literal("convert")
                .requires { it.sender.hasPermission("yarmm.command.convert") }
                .then(Commands.argument("plugin", StringArgumentType.word())
                    .suggests { _, builder ->
                        converters.keys.forEach{ builder.suggest(it) }
                        builder.buildFuture()
                    }
                    .executes {
                        val plugin = it.getArgument("plugin", String::class.java)
                        val converter = converters[plugin]!!
                        it.source.sender.sendRichMessage(
                            if (converter.convert()) "<green>$plugin menus converted!"
                            else "<red>$plugin folder not found!"
                        )
                        Command.SINGLE_SUCCESS
                    })
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

    companion object {
        const val PASTE_URL = "https://paste.helpch.at/"
        val gson = Gson()

        lateinit var INSTANCE: YARMM
    }
}
