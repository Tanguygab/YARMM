package io.tanguygab.yarmm.config

import me.neznamy.tab.shared.config.file.YamlConfigurationFile
import java.io.File
import java.io.InputStream

class MainConfig(default: InputStream?, dataFolder: File) : YamlConfigurationFile(default, File(dataFolder, "config.yml")
) {
    val itemNamePrefix = getString("item-name-prefix", "<white>")!!
    val itemLorePrefix = getString("item-lore-prefix", "<gray><underlined:false>")!!
}