package net.slqmy.bad_piggies_plugin.manager;

import net.slqmy.bad_piggies_plugin.BadPiggiesPlugin;
import net.slqmy.bad_piggies_plugin.util.BlockUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InstantTntManager {

    private final BadPiggiesPlugin plugin;

    private final List<Vector> instantTntBlocks = new ArrayList<>();

    public InstantTntManager(BadPiggiesPlugin plugin) {
        this.plugin = plugin;

        loadInstantTntData();
    }

    public void addInstantTnt(Vector blockCoordinates) {
        instantTntBlocks.add(blockCoordinates);
    }

    public void addInstantTnt(@NotNull Block block) {
        addInstantTnt(block.getLocation().toVector());
    }

    public void removeInstantTnt(Vector blockCoordinates) {
        instantTntBlocks.remove(blockCoordinates);
    }

    public void removeInstantTnt(@NotNull Block block) {
        removeInstantTnt(block.getLocation().toVector());
    }

    public boolean isInstantTnt(Vector blockCoordinates) {
        return instantTntBlocks.contains(blockCoordinates);
    }

    public boolean isInstantTnt(@NotNull Block block) {
        return isInstantTnt(block.getLocation().toVector());
    }

    public boolean isInstantTnt(@NotNull ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();

        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

        return Boolean.TRUE.equals(dataContainer.get(plugin.getInstantTntKey(), PersistentDataType.BOOLEAN));
    }

    public boolean shouldInstantTntDetonate(Block instantTnt, @NotNull Entity cause, Location locationOverride) {
        Location blockCenterLocation = BlockUtil.getBlockCenterLocation(instantTnt);

        double tntX = blockCenterLocation.getX();
        double tntY = blockCenterLocation.getY();
        double tntZ = blockCenterLocation.getZ();

        double minTntY = tntY - 0.5D;
        double maxTntY = tntY + 0.5D;

        double minTntX = tntX - 0.5D;
        double maxTntX = tntX + 0.5D;

        double minTntZ = tntZ - 0.5D;
        double maxTntZ = tntZ + 0.5D;

        Vector entityVelocity = plugin.getPlayerVelocityManager().getVelocity(cause);

        double velocityX = entityVelocity.getX();
        double velocityY = entityVelocity.getY();
        double velocityZ = entityVelocity.getZ();

        double significantValue = 0.0D;

        BoundingBox boundingBox = cause.getBoundingBox();

        double minEntityY = boundingBox.getMinY();
        double maxEntityY = boundingBox.getMaxY();

        double maxEntityX = boundingBox.getMaxX();
        double minEntityX = boundingBox.getMinX();

        double maxEntityZ = boundingBox.getMaxZ();
        double minEntityZ = boundingBox.getMinZ();

        if (maxEntityY < minTntY) {
            plugin.getLogger().info("1");
            significantValue = velocityY;
        } else if (minEntityY > maxTntY) {
            plugin.getLogger().info("2");
            significantValue = -velocityY;
        } else if (maxEntityX < minTntX) {
            plugin.getLogger().info("3");
            significantValue = velocityX;
        } else if (minEntityX > maxTntX) {
            plugin.getLogger().info("4");
            significantValue = -velocityX;
        } else if (maxEntityZ < minTntZ) {
            plugin.getLogger().info("5");
            significantValue = velocityZ;
        } else if (minEntityZ > maxTntZ) {
            plugin.getLogger().info("6");
            significantValue = -velocityZ;
        }

        plugin.getLogger().info("entityVelocity = " + entityVelocity);
        plugin.getLogger().info("significantValue = " + significantValue);

        return significantValue > plugin.getConfig().getDouble("features.instant-tnt.minimum-collision-detonation-speed") / 20.0D;
    }

    public boolean shouldInstantTntDetonate(Block instantTnt, @NotNull Entity cause) {
        return shouldInstantTntDetonate(instantTnt, cause, cause.getLocation());
    }

    public void detonateInstantTnt(@NotNull Block instantTnt) {
        instantTnt.setType(Material.AIR);

        World world = instantTnt.getWorld();

        YamlConfiguration configuration = (YamlConfiguration) plugin.getConfig();

        ConfigurationSection explosionSettings = configuration.getConfigurationSection("features.instant-tnt.explosion");
        assert explosionSettings != null;

        double instantTntExplosionPower = explosionSettings.getDouble("power");
        boolean instantTntBreaksBlocks = explosionSettings.getBoolean("breaks-blocks");
        boolean instantTntSetsFire = explosionSettings.getBoolean("sets-fire");

        world.createExplosion(BlockUtil.getBlockCenterLocation(instantTnt),
                (float) instantTntExplosionPower,
                instantTntSetsFire,
                instantTntBreaksBlocks
        );

        removeInstantTnt(instantTnt);
    }

    public void loadInstantTntData() {

    }

    public void saveInstantTntData() {

    }
}