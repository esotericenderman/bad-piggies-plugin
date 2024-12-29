package dev.enderman.minecraft.plugins.badpiggies.util

import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import kotlin.math.floor

object EntityUtil {
    @JvmStatic
    fun getTouchedBlocks(entity: Entity, locationOverride: Location): List<Block> {
        val box = entity.boundingBox

        box.expand(0.01)

        val locationDifference = locationOverride.clone().subtract(entity.location).toVector()

        val minX = box.minX + locationDifference.x
        val maxX = box.maxX + locationDifference.x

        val minY = box.minY + locationDifference.y
        val maxY = box.maxY + locationDifference.y

        val minZ = box.minZ + locationDifference.z
        val maxZ = box.maxZ + locationDifference.z

        val world = entity.world

        val touchedBlocks: MutableList<Block> = ArrayList()

        var x = floor(minX).toInt()
        while (x <= floor(maxX)) {
            var y = floor(minY).toInt()
            while (y <= floor(maxY)) {
                var z = floor(minZ).toInt()
                while (z <= floor(maxZ)) {
                    touchedBlocks.add(
                        world.getBlockAt(
                            x,
                            y,
                            z
                        )
                    )
                    z++
                }
                y++
            }
            x++
        }

        return touchedBlocks
    }

    fun getTouchedBlocks(entity: Entity): List<Block> {
        return getTouchedBlocks(entity, entity.location)
    }
}
