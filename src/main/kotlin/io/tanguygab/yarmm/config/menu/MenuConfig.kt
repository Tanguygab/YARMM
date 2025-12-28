package io.tanguygab.yarmm.config.menu

import io.github.tanguygab.conditionalactions.ConditionalActions
import io.github.tanguygab.conditionalactions.actions.ActionGroup
import io.tanguygab.yarmm.config.MainConfig
import me.neznamy.tab.shared.config.file.YamlConfigurationFile
import org.bukkit.event.inventory.InventoryType
import java.io.File

internal fun getActionGroup(list: Any?) = ActionGroup(ConditionalActions.INSTANCE.actionManager, list as List<*>? ?: emptyList<Any>())

data class MenuConfig(
    val title: String,
    val rows: Int,
    val type: InventoryType,
    val openActions: ActionGroup,
    val closeActions: ActionGroup,
    val items: List<MenuItemConfig>
) {
    companion object {
        fun fromFile(file: File): MenuConfig {
            val file = YamlConfigurationFile(null, file)
            return MenuConfig(
                title = file.getString("title", null) ?: "<red>No title set",
                rows = file.getInt("rows", null) ?: 6,
                type = InventoryType.entries.find { it.name == file.getString("type", null) } ?: InventoryType.CHEST,

                openActions = getActionGroup(file.getObject("open-actions", null)),
                closeActions = getActionGroup(file.getObject("close-actions",  null)),

                items = file.getMap<String, Any>("items").keys
                    .map { file.getConfigurationSection("items.$it") }
                    .map { MenuItemConfig.fromSection(it) }
            )
        }
    }
}