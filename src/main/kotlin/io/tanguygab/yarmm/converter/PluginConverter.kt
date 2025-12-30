package io.tanguygab.yarmm.converter

import io.github.tanguygab.conditionalactions.Utils
import io.tanguygab.yarmm.YARMM
import me.neznamy.tab.shared.chat.EnumChatFormat
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.config.file.YamlConfigurationFile
import org.bukkit.Bukkit
import java.io.File

abstract class PluginConverter(private val plugin: YARMM, val folder: String, private val itemsSection: String) {

    fun convert(): Boolean {
        val folder = Bukkit.getPluginsFolder().resolve(folder)
        if (folder.exists()) {
            Utils.loadFiles(folder, "") { file, _ ->
                convertFile(file, Bukkit.getPluginsFolder().resolve("YARMM/menus/converted/${folder.parentFile.name}/${file.path.substringAfter(this.folder)}"))
            }
            return true
        }
        return false
    }

    private fun convertFile(input: File, output: File): YamlConfigurationFile {
        plugin.logger.info("Converting $input into $output")

        if (!output.exists()) {
            if (!output.parentFile.exists())
                output.parentFile.mkdirs()
            output.createNewFile()
        }
        val old = YamlConfigurationFile(null, input)
        val new = YamlConfigurationFile(null, output)

        val args = getArgs(old)
        convertMenu(old, new, args)
        plugin.logger.info("Converted menu settings")

        val oldSection = old.getConfigurationSection(itemsSection)
        oldSection.keys
            .map { it to oldSection.getConfigurationSection("$it") }
            .sortedBy { (_, section) -> section.getInt("priority") ?: Integer.MAX_VALUE }
            .forEach { (key, section) -> convertItem(section, new, "items.$key", args) }
        plugin.logger.info("Converted ${oldSection.keys.size} menu items")
        return new
    }

    protected fun convertActions(actions: List<String>, args: Map<String, String>, requirements: Any? = null): List<Any> {
        val list = mutableListOf<Any>()
        if (requirements != null) {
            try {
                list.addAll(convertRequirements(requirements, args))
            } catch (e: Exception) {
                println("Failed to convert requirement $requirements")
                e.printStackTrace()
            }
        }
        list.addAll(actions.mapNotNull {
            try {
                convertAction(it, args)
            } catch (e: Exception) {
                println("Failed to convert action $it")
                e.printStackTrace()
                null
            }
        })
        return list
    }

    protected fun String.convert(args: Map<String, String>): String {
        var str = this
        args.forEach { (old, new) ->
            while (str.contains(old)) {
                str = str.replaceFirst(old, if (str.substringBefore(old).count { it == '%' } % 2 == 0) "%$new%" else "{$new}")
            }
        }

        str = str.replace("§", "&")
        for (c in EnumChatFormat.entries) {
            var string = c.name.lowercase()
            if (string == "underline") string += "d"
            str = str.replace("&" + c.character, "<$string>")
        }
        str = str
            .replace("&u", "<rainbow>")
            .replace("<reset>", "<bold:false><italic:false><underlined:false><strikethrough:false><obfuscated:false><white>")

        str = str.replace(rgbPattern) { "<${it.groups["rgb"]!!.value}>" }
        return str
    }

    protected abstract val rgbPattern: Regex
    protected abstract fun getArgs(input: YamlConfigurationFile): Map<String, String>
    protected abstract fun convertMenu(input: YamlConfigurationFile, output: YamlConfigurationFile, args: Map<String, String>)
    protected abstract fun convertItem(input: ConfigurationSection, output: YamlConfigurationFile, path: String, args: Map<String, String>)
    protected abstract fun convertAction(input: String, args: Map<String, String>): String?
    protected abstract fun convertRequirementType(input: Any, args: Map<String, String>): String
    protected abstract fun convertRequirements(input: Any, args: Map<String, String>): List<Map<String, Any?>>

}