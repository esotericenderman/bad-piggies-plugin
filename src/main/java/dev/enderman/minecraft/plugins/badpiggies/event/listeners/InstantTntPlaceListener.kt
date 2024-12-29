package dev.enderman.minecraft.plugins.badpiggies.event.listeners

import dev.enderman.minecraft.plugins.badpiggies.BadPiggiesPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class InstantTntPlaceListener(private val plugin: BadPiggiesPlugin) : Listener {
    @EventHandler
    fun onTntPlace(event: BlockPlaceEvent) {
        if (!plugin.instantTntManager!!.isInstantTnt(event.itemInHand)) {
            return
        }

        val instantTntManager = plugin.instantTntManager

        instantTntManager!!.addInstantTnt(event.block)
    }
}
