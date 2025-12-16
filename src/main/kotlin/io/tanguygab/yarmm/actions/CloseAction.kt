package io.tanguygab.yarmm.actions

import io.github.tanguygab.conditionalactions.actions.Action
import io.tanguygab.yarmm.MenuManager
import me.neznamy.tab.shared.TAB
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class CloseAction(val menuManager: MenuManager) : Action("close") {

    override fun getSuggestion() = "close"

    override fun execute(player: OfflinePlayer?, match: String) {
        if (player !is Player) return
        val player = TAB.getInstance().getPlayer(player.uniqueId)
        player?.let { menuManager.closeMenu(it) }
    }


}