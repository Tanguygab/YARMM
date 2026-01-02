package io.tanguygab.yarmm.listeners

import io.tanguygab.yarmm.YARMM
import io.tanguygab.yarmm.tab
import me.neznamy.tab.api.TabPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class PlayerListener(val plugin: YARMM) : Listener {

    private val prompts = mutableMapOf<TabPlayer, CompletableFuture<String>>()

    fun prompt(player: TabPlayer, cooldown: Long): String? {
        val future = CompletableFuture<String>()
        prompts[player] = future
        return try {
            if (cooldown == -1L) future.get()
            else future.get(cooldown, TimeUnit.MILLISECONDS)
        } catch (_: Exception) {
            prompts.remove(player)
            null
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onChat(@Suppress("DEPRECATION") e: AsyncPlayerChatEvent) {
        val player = e.player.tab

        if (player in plugin.menuManager.sessions) e.isCancelled = true
        if (player!! !in prompts) return
        e.isCancelled = true

        prompts[player]!!.complete(e.message)
        prompts.remove(player)
    }

    @EventHandler
    fun onLeave(e: PlayerQuitEvent) {
        val player = e.player.tab ?: return
        prompts.remove(player)
        plugin.menuManager.sessions.remove(player)
    }
}