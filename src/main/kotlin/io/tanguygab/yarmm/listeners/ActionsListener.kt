package io.tanguygab.yarmm.listeners

import io.github.tanguygab.conditionalactions.events.ActionsRegisterEvent
import io.tanguygab.yarmm.MenuManager
import io.tanguygab.yarmm.actions.CloseAction
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ActionsListener(val menuManager: MenuManager) : Listener {

    @EventHandler
    fun onActionsLoad(e: ActionsRegisterEvent) {
        e.addActions(
            CloseAction(menuManager)
        )
    }

}