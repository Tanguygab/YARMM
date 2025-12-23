package io.tanguygab.yarmm.config

import io.tanguygab.yarmm.YARMM
import me.neznamy.tab.shared.config.file.YamlConfigurationFile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.io.File

class LangConfig(plugin: YARMM) : YamlConfigurationFile(
    plugin.getResource("lang.yml"),
    File(plugin.dataFolder, "lang.yml")
) {

    fun mm(string: String, vararg tagResolver: TagResolver) = MiniMessage
        .miniMessage()
        .deserialize(string, *tagResolver)

    fun get(path: String, vararg tagResolver: TagResolver) = mm(
        getString(path, null) ?: "<red>Missing translation for path <dark_red>$path</dark_red>!",
        *tagResolver
    )

    fun getCommandsListHeader(total: Int, page: Int, pages: Int) = get("commands.list.header",
        Placeholder.unparsed("total", "$total"),
        Placeholder.unparsed("page", "$page"),
        Placeholder.unparsed("pages", "$pages")
    )

    fun getCommandsListLine(player: String, menu: String): Component {
        val line = get("commands.list.line", Placeholder.unparsed("menu", menu))
        val hover = (getStringList("commands.list.hover", null) ?: emptyList()).joinToString("\n")
        return line
            .hoverEvent(mm(hover))
            .clickEvent(ClickEvent.runCommand("/yarmm open $player $menu"))
            .insertion("/yarmm open $player $menu")
    }
}