package dev.enderman.minecraft.plugins.badpiggies.event.listeners

import dev.enderman.minecraft.plugins.badpiggies.BadPiggiesPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.TNTPrimeEvent

class InstantTntDetonateListener(private val plugin: BadPiggiesPlugin) : Listener {
    @EventHandler
    fun onInstantTntDetonate(event: TNTPrimeEvent) {
        val tnt = event.block

        val manager = plugin.instantTntManager

        if (manager!!.isInstantTnt(tnt)) {
            event.isCancelled = true

            val entity = event.primingEntity
            manager.chainDetonateInstantTnt(tnt, entity)
        }
    }
}
