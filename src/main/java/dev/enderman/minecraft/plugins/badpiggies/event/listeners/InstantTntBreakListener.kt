package dev.enderman.minecraft.plugins.badpiggies.event.listeners

import dev.enderman.minecraft.plugins.badpiggies.BadPiggiesPlugin
import dev.enderman.minecraft.plugins.badpiggies.util.BlockUtil.getBlockCenterLocation
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent

class InstantTntBreakListener(private val plugin: BadPiggiesPlugin) : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onInstantTntBreak(event: BlockDropItemEvent) {
        val block = event.block

        val instantTntManager = plugin.instantTntManager

        if (!instantTntManager!!.isInstantTnt(block)) {
            return
        }

        instantTntManager.removeInstantTnt(event.block)

        val items = event.items

        if (items.isEmpty()) {
            return
        }

        items.clear()
        val instantTnt = instantTntManager.instantTntItem

        val world = block.world
        world.dropItem(getBlockCenterLocation(block), instantTnt)
    }
}
