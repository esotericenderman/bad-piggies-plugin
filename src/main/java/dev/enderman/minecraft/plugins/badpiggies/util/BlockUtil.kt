package dev.enderman.minecraft.plugins.badpiggies.util

import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.util.Vector

object BlockUtil {
    fun getBlockCenterLocation(location: Location): Location {
        return location.clone().add(0.5, 0.5, 0.5)
    }

    @JvmStatic
    fun getBlockCenterLocation(block: Block): Location {
        return getBlockCenterLocation(block.location)
    }

    @JvmStatic
    fun getBlockCenterLocation(vector: Vector): Vector {
        return vector.clone().add(Vector(0.5, 0.5, 0.5))
    }
}
