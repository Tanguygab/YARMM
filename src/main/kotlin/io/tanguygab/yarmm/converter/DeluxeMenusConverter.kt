package io.tanguygab.yarmm.converter

import io.tanguygab.yarmm.YARMM
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.config.file.YamlConfigurationFile
import org.bukkit.Color

class DeluxeMenusConverter(plugin: YARMM) : PluginConverter(plugin, "DeluxeMenus/gui_menus", "items") {

    override val rgbPattern = "&(?<rgb>#[0-9a-fA-F]{6})".toRegex()

    private fun String.hexFromRGB(): String? {
        return if (!contains("%")) {
            val arr = split(", ")
            try { Integer.toHexString(Color.fromRGB(arr[0].toInt(), arr[1].toInt(), arr[2].toInt()).asRGB()) }
            catch (_: Exception) { null }
        } else this
    }

    override fun getArgs(input: YamlConfigurationFile): Map<String, String> {
        return input.getStringList("args", emptyList()).mapIndexed { index, string -> "{$string}" to "menu-arg-$index" }.toMap()
    }

    override fun convertMenu(input: YamlConfigurationFile, output: YamlConfigurationFile, args: Map<String, String>) {
        output["title"] = input.getString("menu_title", null)?.convert(args) ?: "<red>No title set"
        output["rows"] = input.getInt("size", null)?.let { it / 9 }
        output["type"] = input.getString("inventory_type", null)
        output["open-actions"] = convertActions(input.getStringList("open_commands", null) ?: listOf(), args, input.getMap<String, Any>("open_requirement")).ifEmpty { null }
        output["close-actions"] = convertActions(input.getStringList("close_commands", null) ?: listOf(), args, input.getMap<String, Any>("open_requirement")).ifEmpty { null }
        // open_command?
    }

    override fun convertItem(input: ConfigurationSection, output: YamlConfigurationFile, path: String, args: Map<String, String>) {
        output["$path.material"] = input.getString("material")?.convert(args)
        output["$path.name"] = input.getString("display_name")?.convert(args)
        output["$path.amount"] = input.getString("dynamic_amount")?.convert(args) ?: input.getObject("amount")
        output["$path.lore"] = input.getStringList("lore")?.map { it.convert(args) }
        output["$path.slot"] = input.getObject("slot")?.toString()?.convert(args)
        output["$path.slots"] = input.getStringList("slots")?.map { it.convert(args) }
        output["$path.enchantments"] = input.getStringList("enchantments")?.map { it.split(";") }?.associate { it[0] to it[1] }
        output["$path.flags"] = input.getStringList("item_flags")

        val clickActions = listOf("left", "right", "middle", "shift_left", "shift_right", "")
            .asSequence()
            .map { it to "${if(it.isEmpty()) "" else "${it}_"}click" }
            .filter { (_, type) -> "${type}_commands" in input.keys }
            .map { (key, type) -> key to convertActions(
                input.getStringList("${type}_commands") ?: listOf(),
                args,
                input.getMap<String, Any>("${type}_requirement")
            ) }
            .map { (key, actions) ->
                if (key.isEmpty()) actions else mapOf(
                    "condition" to "%menu-click% = $key",
                    "success" to actions
                )
            }.toMutableList()
        @Suppress("UNCHECKED_CAST")
        val allClicks = clickActions.find { it is List<*> } as? List<Any>
        if (allClicks != null) {
            clickActions.remove(allClicks)
            clickActions.addAll(allClicks)
        }
        if (clickActions.isNotEmpty()) output["$path.click-actions"] = clickActions

        output["$path.display-condition"] = input.getMap<String, Any>("view_requirement")?.let { convertRequirementType(it, args) }

        output["$path.damage"] = input.getObject("damage")?.toString()?.convert(args)

        output["$path.patterns"] = input.getStringList("banner_meta")?.map { it.split(";").reversed().joinToString(";").convert(args) }
        output["$path.shield-color"] = input.getString("base_color")?.convert(args)

        if (input.keys.any { it in listOf("trim_pattern", "trim_material", "rgb") }) {
            output["$path.armor.pattern"] = input.getString("trim_pattern")?.convert(args)
            output["$path.armor.material"] = input.getString("trim_material")?.convert(args)
            output["$path.armor.color"] = input.getString("rgb")?.hexFromRGB()?.convert(args)
        }

        if (input.keys.any { it in listOf("potion_effects", "rgb") }) {
            output["$path.potion.effects"] = input.getStringList("potion_effects")?.map { it.replace(";", " ").convert(args) }
            output["$path.potion.color"] = input.getString("rgb")?.hexFromRGB()?.convert(args)
        }


        if (input.keys.any { it in listOf("item_model", "model_data_component") }) {
            output["$path.model.key"] = input.getString("item_model")?.convert(args)
            listOf("floats", "flags", "strings", "colors").forEach { type ->
                if (type in input.getConfigurationSection("model_data_component").keys)
                    output["$path.model.data.$type"] = input
                        .getStringList("model_data_component.$type")
                        ?.mapNotNull { (if (type == "colors") it.hexFromRGB() else it)?.convert(args) }
            }
        }


        if (input.keys.any { it in listOf("hide_tooltip", "tooltip_style", "rarity", "enchantment_glint_override", "unbreakable") }) {
            output["$path.tooltip.hide"] = input.getObject("hide_tooltip")?.toString()?.convert(args)
            output["$path.tooltip.style"] = input.getString("tooltip_style")?.convert(args)
            output["$path.tooltip.rarity"] = input.getString("rarity")?.convert(args)
            output["$path.tooltip.glow"] = input.getObject("enchantment_glint_override")?.toString()?.convert(args)
            output["$path.tooltip.unbreakable"] = input.getObject("unbreakable")?.toString()?.convert(args)
        }

        output["$path.light-level"] = input.getObject("light_level")?.toString()?.convert(args)

        // lore append mode, todo after adding custom materials
        // components?
    }

    override fun convertAction(input: String, args: Map<String, String>): String? {
        val type = input.substringAfter("[").substringBefore("]").lowercase()
        val arg = input.substringAfter("]").convert(args).trim()
        return when (type) {
            "console", "player", "chat" -> "$type: $arg"
            "commandevent" -> "player: $arg"
            "message", "minimessage", "json" -> "message: $arg"
            "broadcast", "minibroadcast", "jsonbroadcast", "broadcastjson" -> "broadcast: $arg"
            "openguimenu", "openmenu" -> "menu: $arg"
            "connect" -> "server: $arg"
            "close" -> "close"
            "refresh" -> null
            "broadcastsound", "broadcastsoundworld", "sound" -> {
                val args = arg.split(" ")
                "console: execute at ${if (type.startsWith("broadcast")) "@a" else "%player%"} run playsound ${args[0].lowercase().replace("_", ".")} master @p ~ ~ ~ ${args.getOrNull(1) ?: 0} ${args.getOrNull(2) ?: 0}"
            }
            "takemoney", "givemoney" -> "console: eco ${type.substring(0, 4)} %player% $arg"
            "takeexp", "giveexp" -> "console: xp add %player% ${if (type == "takeexp") "-" else ""}${arg.removeSuffix("L").removeSuffix("l")} ${if (arg.endsWith("l", ignoreCase = true)) "levels" else "points" }"
            "takepermission", "givepermission" -> "${type.substring(0, 4)}-permission: $arg"

            "placeholder" -> "not implemented"
            "meta" -> "not implemented" // Will be added to CA
            "log" -> "not implemented"
            else -> "unknown action \"$input\""
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun convertRequirementType(input: Any, args: Map<String, String>): String {
        input as Map<String, Any>
        val type = input["type"].toString().lowercase().replace(" ", "")
        val input0 = input["input"].toString().convert(args)
        val output = input["output"].toString().convert(args)

        return when (type) {
            "haspermission" -> "permission:${input["permission"]}"
            "haspermissions" -> (input["permissions"] as List<String>).joinToString(" ${if (input["minimum"] == 1) "||" else "&&"} ") { p -> "permission:$p" }
            "hasmoney" -> "%vault_eco_balance% >= ${input["amount"].toString().convert(args)}"
            "hasexp" -> "%player_${if (input["level"] == true) "level" else "current_exp"}% >= ${input["amount"].toString().convert(args)}"
            "isnear" -> {
                val loc = input["location"].toString().convert(args).split(",")
                "%world% == ${loc[0]} && %distance_${loc[1]},${loc[2]},${loc[3]} <= ${input["distance"]}"
            }
            "stringequals" -> "$input0 == $output"
            "stringequalsignorecase" -> "$input0 = $output"
            "stringcontains" -> "$input0 <- $output"
            "stringlength" -> {
                val placeholder = "%string_length_${
                    when {
                        !input0.contains("%") -> input0.convert(args)
                        !input0.contains("{") -> "{" + input0.convert(args).removeSurrounding("%") + "}"
                        else -> "{utils_parse_" + input0.removeSurrounding("%") + "}"
                    }
                }%"
                "$placeholder >= ${input["min"].toString().convert(args)} && $placeholder <= ${input["max"].toString().convert(args)}"
            }
            "hasitem" -> {
                val material = input["material"]?.toString()?.convert(args)
                val data = input["data"]?.toString()?.convert(args)
                val modelData = (input["model_data"]?.toString() ?: input["modeldata"]?.toString())?.convert(args)
                val amount = input["amount"]?.toString()?.convert(args)
                val name = input["name"]?.toString()?.convert(args)
                val lore = (input["lore"] as? List<String>)?.joinToString("|")?.convert(args)
                val nameContains = input["name_contains"] == true
                val loreContains = input["lore_contains"] == true
                val strict = input["strict"] == true
                "%checkitem_" +
                        ((if (material == null) "" else "mat:$material,") +
                                (if (data == null) "" else "data:$data,") +
                                (if (amount == null) "" else "amt:$amount,") +
                                (if (modelData == null) "" else "custommodeldata:$modelData,") +
                                (if (name == null) "" else "name${if (nameContains) "contains" else "equals"}:$name,") +
                                (if (lore == null) "" else "lore${if (loreContains) "contains" else "equals"}:$lore,") +
                                (if (strict) "strict," else "")).removeSuffix(",")
                "%"
            }
            ">=", ">", "<", "<=", "==", "!=" -> "$input0 $type $output"
            "javascript" -> type

            "hasmeta" -> "not implemented" // Will be added to CA
            "regexmatches" -> "not implemented" // Will be Added to CA
            "isobject" -> "not implemented" // idk
            else -> "unknown requirement\"$input0\""
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun convertRequirements(input: Any, args: Map<String, String>): List<Map<String, Any?>> {
        input as Map<String, Any>

        return (input["requirements"] as? Map<String, Map<String, Any>>)?.values?.map {
            val condition = convertRequirementType(it, args)

            val success = it["success_commands"]?.let { commands -> convertActions(commands as List<String>, args) }
            val deny = it["deny_commands"]?.let { commands -> convertActions(commands as List<String>, args) }

            mapOf(
                "condition" to condition,
                "success" to success,
                "deny" to (deny?.also { l -> (l as MutableList<String>).add("return") } ?: listOf("return")),
            ).filterValues { value -> value != null }
        } ?: emptyList()
    }
}