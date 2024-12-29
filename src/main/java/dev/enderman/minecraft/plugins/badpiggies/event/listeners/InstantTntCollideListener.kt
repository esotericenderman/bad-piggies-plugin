package dev.enderman.minecraft.plugins.badpiggies.event.listeners

import dev.enderman.minecraft.plugins.badpiggies.BadPiggiesPlugin
import dev.enderman.minecraft.plugins.badpiggies.util.EntityUtil.getTouchedBlocks
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class InstantTntCollideListener(private val plugin: BadPiggiesPlugin) : Listener {
    @EventHandler
    fun onInstantTntCollide(event: PlayerMoveEvent) {
        onInstantTntCollide(
            EntityMoveEvent(
                event.player,
                event.from,
                event.to
            )
        )
    }

    @EventHandler
    fun onInstantTntCollide(event: EntityMoveEvent) {
        val entity: Entity = event.entity

        val velocity = plugin.playerVelocityManager!!.getVelocity(entity)

        val instantTntManager = plugin.instantTntManager

        val speedMetersPerTick = velocity.length()

        val speedMetersPerSecond = speedMetersPerTick * 20.0

        val configuration = plugin.config as YamlConfiguration

        val minimumCollisionDetonationSpeed =
            configuration.getDouble("features.instant-tnt.minimum-collision-detonation-speed")

        if (speedMetersPerSecond < minimumCollisionDetonationSpeed) {
            return
        }

        val touchedBlocks = getTouchedBlocks(entity, event.to)

        for (touchedBlock in touchedBlocks) {
            if (instantTntManager!!.isInstantTnt(touchedBlock) && instantTntManager.shouldInstantTntDetonate(
                    touchedBlock,
                    entity,
                    event.to
                )
            ) {
                instantTntManager.chainDetonateInstantTnt(touchedBlock, entity)
            }
        }
    }
}
