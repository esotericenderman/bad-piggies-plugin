package dev.enderman.minecraft.plugins.badpiggies.managers

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import dev.enderman.minecraft.plugins.badpiggies.BadPiggiesPlugin
import dev.enderman.minecraft.plugins.badpiggies.util.BlockUtil.getBlockCenterLocation
import net.minecraft.world.level.Explosion
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.io.*
import kotlin.math.floor
import kotlin.math.pow

class InstantTntManager(private val plugin: BadPiggiesPlugin) {
    var explosionClass: Class<out Explosion?> = Explosion::class.java

    private val instantTntDataFile = File(plugin.dataFolder, "data/instant-tnt-blocks.json")

    private val instantTntBlocks: MutableList<Vector> = ArrayList()

    init {
        loadInstantTntData()
    }

    fun addInstantTnt(blockCoordinates: Vector) {
        instantTntBlocks.add(blockCoordinates)
    }

    fun addInstantTnt(block: Block) {
        addInstantTnt(block.location.toVector())
    }

    fun removeInstantTnt(blockCoordinates: Vector) {
        instantTntBlocks.remove(blockCoordinates)
    }

    fun removeInstantTnt(block: Block) {
        removeInstantTnt(block.location.toVector())
    }

    fun isInstantTnt(blockCoordinates: Vector): Boolean {
        return instantTntBlocks.contains(blockCoordinates)
    }

    fun isInstantTnt(block: Block): Boolean {
        return isInstantTnt(block.location.toVector())
    }

    fun isInstantTnt(itemStack: ItemStack): Boolean {
        val meta = itemStack.itemMeta

        val dataContainer = meta.persistentDataContainer

        return java.lang.Boolean.TRUE == dataContainer.get(plugin.instantTntKey!!, PersistentDataType.BOOLEAN)
    }

    val instantTntItem: ItemStack
        get() {
            val instantTnt = ItemStack(Material.TNT)

            val meta = instantTnt.itemMeta

            val dataContainer = meta.persistentDataContainer
            val instantTntKey = plugin.instantTntKey
            dataContainer.set(instantTntKey!!, PersistentDataType.BOOLEAN, true)

            instantTnt.setItemMeta(meta)

            return instantTnt
        }

    @JvmOverloads
    fun shouldInstantTntDetonate(
        instantTnt: Block,
        cause: Entity,
        locationOverride: Location = cause.location
    ): Boolean {
        val blockCenterLocation = getBlockCenterLocation(instantTnt)

        val tntX = blockCenterLocation.x
        val tntY = blockCenterLocation.y
        val tntZ = blockCenterLocation.z

        val minTntY = tntY - 0.5
        val maxTntY = tntY + 0.5

        val minTntX = tntX - 0.5
        val maxTntX = tntX + 0.5

        val minTntZ = tntZ - 0.5
        val maxTntZ = tntZ + 0.5

        val entityVelocity = plugin.playerVelocityManager!!.getVelocity(cause)

        val velocityX = entityVelocity.x
        val velocityY = entityVelocity.y
        val velocityZ = entityVelocity.z

        var significantValue = 0.0

        val locationDifference = locationOverride.clone().subtract(cause.location).toVector()

        val boundingBox = cause.boundingBox

        val minEntityY = boundingBox.minY + locationDifference.y
        val maxEntityY = boundingBox.maxY + locationDifference.y

        val maxEntityX = boundingBox.maxX + locationDifference.x
        val minEntityX = boundingBox.minX + locationDifference.x

        val maxEntityZ = boundingBox.maxZ + locationDifference.z
        val minEntityZ = boundingBox.minZ + locationDifference.z

        if (maxEntityY <= minTntY) {
            significantValue = velocityY
        } else if (minEntityY >= maxTntY) {
            significantValue = -velocityY
        } else if (maxEntityX <= minTntX) {
            significantValue = velocityX
        } else if (minEntityX >= maxTntX) {
            significantValue = -velocityX
        } else if (maxEntityZ <= minTntZ) {
            significantValue = velocityZ
        } else if (minEntityZ >= maxTntZ) {
            significantValue = -velocityZ
        }

        return significantValue > plugin.config.getDouble("features.instant-tnt.minimum-collision-detonation-speed") / 20.0
    }

    private fun detonateInstantTnt(instantTnt: Block, cause: Entity?) {
        instantTnt.type = Material.AIR

        val power = plugin.config.getDouble("features.instant-tnt.explosion.power").toFloat()
        val shouldSetFire = plugin.config.getBoolean("features.instant-tnt.explosion.sets-fire")
        val shouldBreakBlocks = plugin.config.getBoolean("features.instant-tnt.explosion.breaks-blocks")

        instantTnt.world.createExplosion(
            cause,
            getBlockCenterLocation(instantTnt),
            power,
            shouldSetFire,
            shouldBreakBlocks,
            false
        )

        removeInstantTnt(instantTnt)
    }

    private fun chainDetonateInstantTnt(startingTnt: Block, blocksToDetonate: MutableList<Vector>): List<Vector> {
        val blocksToIterateThrough: MutableList<Vector>

        val explosionRadiusBlocks = plugin.config.getDouble("features.instant-tnt.explosion.spread-radius-blocks")
        val cubeVolume = (explosionRadiusBlocks * 2).pow(3.0)

        val totalInstantTnts = instantTntBlocks.size.toDouble()

        val center = getBlockCenterLocation(startingTnt).toVector()

        if (cubeVolume < totalInstantTnts) {
            blocksToIterateThrough = ArrayList()

            var x = center.x - explosionRadiusBlocks
            while (x <= center.x + explosionRadiusBlocks) {
                var y = center.y - explosionRadiusBlocks
                while (y <= center.y + explosionRadiusBlocks) {
                    var z = center.z - explosionRadiusBlocks
                    while (z <= center.z + explosionRadiusBlocks) {
                        val location = Vector(x, y, z)

                        val distance = center.distance(location)

                        if (distance > explosionRadiusBlocks) {
                            z++
                            continue
                        }

                        blocksToIterateThrough.add(Vector(x, y, z))
                        z++
                    }
                    y++
                }
                x++
            }
        } else {
            blocksToIterateThrough = instantTntBlocks.stream().filter { location: Vector? ->
                getBlockCenterLocation(
                    location!!
                ).distance(center) <= explosionRadiusBlocks
            }.toList()
        }

        val world = startingTnt.world

        for (tntBlockLocation in blocksToIterateThrough) {
            if (blocksToDetonate.contains(tntBlockLocation)) {
                continue
            }

            blocksToDetonate.add(tntBlockLocation)

            chainDetonateInstantTnt(
                world.getBlockAt(
                    Location(
                        world,
                        tntBlockLocation.x,
                        tntBlockLocation.y,
                        tntBlockLocation.z
                    )
                ),
                blocksToDetonate
            )
        }

        return blocksToDetonate
    }

    fun chainDetonateInstantTnt(startingTnt: Block, cause: Entity?) {
        val blocksToDetonate = chainDetonateInstantTnt(startingTnt, ArrayList())

        val explosionOrigin = getBlockCenterLocation(startingTnt).toVector()
        val world = startingTnt.world

        val blocksPerTickDelay = plugin.config.getInt("features.instant-tnt.explosion.blocks-per-tick-delay").toDouble()

        for (explosionLocation in blocksToDetonate) {
            val distance = explosionOrigin.distance(explosionLocation)

            val tickDelay = if (blocksPerTickDelay == 0.0) 0 else floor(distance / blocksPerTickDelay).toInt()

            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                detonateInstantTnt(
                    world.getBlockAt(
                        Location(world, explosionLocation.x, explosionLocation.y, explosionLocation.z)
                    ),
                    cause
                )
            }, tickDelay.toLong())
        }
    }

    fun loadInstantTntData() {
        try {
            val reader: Reader = FileReader(instantTntDataFile)

            val gson = Gson()

            val linkedTreeMaps = gson.fromJson<List<*>>(
                reader,
                MutableList::class.java
            ) as List<LinkedTreeMap<String, Double>>

            reader.close()

            for (linkedTreeMap in linkedTreeMaps) {
                instantTntBlocks.add(
                    Vector(
                        linkedTreeMap["x"]!!,
                        linkedTreeMap["y"]!!,
                        linkedTreeMap["z"]!!
                    )
                )
            }
        } catch (exception: IOException) {
            throw RuntimeException(exception)
        }
    }

    fun saveInstantTntData() {
        try {
            val writer: Writer = FileWriter(instantTntDataFile)

            val gson = Gson()

            gson.toJson(instantTntBlocks, writer)

            writer.flush()
            writer.close()
        } catch (exception: IOException) {
            throw RuntimeException(exception)
        }
    }
}