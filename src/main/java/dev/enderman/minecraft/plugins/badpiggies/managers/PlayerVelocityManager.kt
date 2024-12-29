package dev.enderman.minecraft.plugins.badpiggies.managers

import dev.enderman.minecraft.plugins.badpiggies.BadPiggiesPlugin
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class PlayerVelocityManager(private val plugin: BadPiggiesPlugin) : BukkitRunnable() {
    private val playerVelocityMap: MutableMap<Player, Vector> = HashMap()

    private val playerPositionMap: MutableMap<Player, Vector> = HashMap()

    fun getPlayerVelocity(player: Player): Vector? {
        return playerVelocityMap[player]
    }

    fun getVelocity(entity: Entity): Vector {
        if (entity is Player) {
            val velocity = getPlayerVelocity(entity)!! ?: return Vector()

            return velocity
        }

        return entity.velocity
    }

    fun updatePlayerVelocityData(player: Player) {
        var oldPosition = playerPositionMap[player]
        val newPosition = player.location.toVector()

        if (oldPosition == null) {
            oldPosition = newPosition
        }

        val velocity = newPosition.clone().subtract(oldPosition)

        playerVelocityMap[player] = velocity

        playerPositionMap[player] = newPosition
    }

    fun updateVelocityData() {
        val players = plugin.server.onlinePlayers.stream().toList()

        for (player in players) {
            val ticksLived = plugin.playerTickManager!!.getPlayerTicksExisted(player)

            if (ticksLived > 1) {
                updatePlayerVelocityData(player)
            }
        }
    }

    override fun run() {
        updateVelocityData()
    }
}
