package dev.enderman.minecraft.plugins.badpiggies

import dev.enderman.minecraft.plugins.badpiggies.event.listeners.InstantTntBreakListener
import dev.enderman.minecraft.plugins.badpiggies.event.listeners.InstantTntCollideListener
import dev.enderman.minecraft.plugins.badpiggies.event.listeners.InstantTntDetonateListener
import dev.enderman.minecraft.plugins.badpiggies.event.listeners.InstantTntPlaceListener
import dev.enderman.minecraft.plugins.badpiggies.managers.InstantTntManager
import dev.enderman.minecraft.plugins.badpiggies.managers.PlayerTickManager
import dev.enderman.minecraft.plugins.badpiggies.managers.PlayerVelocityManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class BadPiggiesPlugin : JavaPlugin() {
    var playerTickManager: PlayerTickManager? = null
        private set

    var playerVelocityManager: PlayerVelocityManager? = null
        private set

    var instantTntManager: InstantTntManager? = null
        private set

    var instantTntKey: NamespacedKey? = null
        private set

    override fun onEnable() {
        dataFolder.mkdir()

        val config = config as YamlConfiguration

        config.options().copyDefaults()
        saveDefaultConfig()

        saveResource("data/instant-tnt-blocks.json", false)

        val pluginManager = Bukkit.getPluginManager()

        playerTickManager = PlayerTickManager()
        pluginManager.registerEvents(playerTickManager!!, this)
        playerTickManager!!.runTaskTimer(this, 0L, 1L)

        playerVelocityManager = PlayerVelocityManager(this)
        playerVelocityManager!!.runTaskTimer(this, 0L, 1L)

        val isInstantTntEnabled = config.getBoolean("features.instant-tnt.enabled")

        if (isInstantTntEnabled) {
            instantTntManager = InstantTntManager(this)
            instantTntKey = NamespacedKey(this, "instant-tnt")

            val instantTnt = instantTntManager!!.instantTntItem

            val plankTypes: Array<Material> = Arrays.stream(Material.entries.toTypedArray())
                .filter { material -> material.name.endsWith("PLANKS") }
                .toArray { size -> arrayOfNulls<Material>(size) as Array<Material> }

            for (plankType in plankTypes) {
                val recipeKey =
                    NamespacedKey(this, "instant-tnt-recipe-" + plankType.name.lowercase(Locale.getDefault()))

                val instantTntRecipe = ShapedRecipe(recipeKey, instantTnt)

                instantTntRecipe.shape("GPG", "PGP", "GPG")

                instantTntRecipe.setIngredient('G', Material.GUNPOWDER)

                instantTntRecipe.setIngredient('P', plankType)

                Bukkit.addRecipe(instantTntRecipe)
            }

            pluginManager.registerEvents(InstantTntPlaceListener(this), this)
            pluginManager.registerEvents(InstantTntBreakListener(this), this)
            pluginManager.registerEvents(InstantTntCollideListener(this), this)
            pluginManager.registerEvents(InstantTntDetonateListener(this), this)
        }
    }

    override fun onDisable() {
        if (instantTntManager == null) {
            return
        }

        instantTntManager!!.saveInstantTntData()
    }
}
