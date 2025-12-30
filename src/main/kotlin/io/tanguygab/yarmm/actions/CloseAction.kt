package io.tanguygab.yarmm.actions

import io.github.tanguygab.conditionalactions.actions.Action
import io.tanguygab.yarmm.MenuCloseReason
import io.tanguygab.yarmm.YARMM
import io.tanguygab.yarmm.tab
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class CloseAction(val yarmm: YARMM) : Action("^(?i)close".toRegex()) {

    override fun getSuggestion() = "close"

    override fun execute(player: OfflinePlayer?, match: String) {
        if (player !is Player) return
        player.tab?.let { yarmm.menuManager.closeMenu(it, MenuCloseReason.REOPEN) }
    }

}