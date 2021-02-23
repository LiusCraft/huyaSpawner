package org.liuscraft.huyahandler.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.liuscraft.huyahandler.HuyaHandlerMain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ItemSpawnUtils
{
    public static void spawnRandomItem(final Location location) {
        final List<Material> forbiddenItems = new ArrayList<Material>();
        for (final String materialName : HuyaHandlerMain.instance.getConfig().getStringList("forbiddenItems")) {
            forbiddenItems.add(Material.getMaterial(materialName));
        }
        Random random;
        List<Material> materials;
        Material material;
        for (random = new Random(), materials = Arrays.asList(Material.values()), material = materials.get(random.nextInt(materials.size())); forbiddenItems.contains(material) || !material.isItem(); material = materials.get(random.nextInt(materials.size()))) {}
        final ItemStack itemStack = new ItemStack(material);
        new BukkitRunnable() {
            public void run() {
                final Item item = (Item)location.getWorld().spawnEntity(location, EntityType.DROPPED_ITEM);
                item.setItemStack(itemStack);
            }
        }.runTask((Plugin)HuyaHandlerMain.instance);
    }
}
