package io.tanguygab.yarmm.config

import io.tanguygab.yarmm.YARMM
import me.neznamy.tab.shared.config.file.YamlConfigurationFile
import java.io.File
import java.io.InputStream

class MainConfig(plugin: YARMM) : YamlConfigurationFile(
    plugin.getResource("config.yml"),
    File(plugin.dataFolder, "config.yml")
) {
    val itemNamePrefix = getString("item-name-prefix", "<white>")!!
    val itemLorePrefix = getString("item-lore-prefix", "<gray><underlined:false>")!!

    val listMaxEntries = getInt("list-max-entries", 10)!!
}