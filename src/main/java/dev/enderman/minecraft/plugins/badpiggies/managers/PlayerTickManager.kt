package dev.enderman.minecraft.plugins.badpiggies.managers

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitRunnable

class PlayerTickManager : BukkitRunnable(), Listener {
    private val playerTicksExistedMap: MutableMap<Player, Int> = HashMap()

    fun getPlayerTicksExisted(player: Player): Int {
        return playerTicksExistedMap[player]!!
    }

    private fun setPlayerTicksExisted(player: Player, ticksExisted: Int) {
        playerTicksExistedMap[player] = ticksExisted
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        playerTicksExistedMap[event.player] = 0
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        playerTicksExistedMap.remove(event.player)
    }

    override fun run() {
        for (player in playerTicksExistedMap.keys) {
            val ticksLived = getPlayerTicksExisted(player)

            setPlayerTicksExisted(player, ticksLived + 1)
        }
    }
}
