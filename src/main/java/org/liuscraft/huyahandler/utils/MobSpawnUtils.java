package org.liuscraft.huyahandler.utils;

import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.liuscraft.huyahandler.GiantEntity;
import org.liuscraft.huyahandler.HuyaHandlerMain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobSpawnUtils
{
    public static List<EntityType> entityTypes;
    public static EntityType boss_wither;

    public static void spawnMob(final Player player, final int radius, final int maxTryTime) {
        final EntityType entityType = randomMob();
        for (int i = 0; i < maxTryTime; ++i) {
            final Location spawnLoc = randomLocation(player.getLocation(), radius);
            final Material downType = spawnLoc.getWorld().getBlockAt(spawnLoc).getType();
            final Material upType = spawnLoc.getWorld().getBlockAt(spawnLoc.add(0.0, 1.0, 0.0)).getType();
            if ((downType.isAir() || !downType.isSolid()) && (upType.isAir() || !upType.isSolid())) {
                spawnEntity(entityType, spawnLoc.add(0.0, -1.0, 0.0));
                return;
            }
        }
        spawnEntity(entityType, getLocation(entityType, player.getLocation(), radius));
    }

    public static boolean spawnBossEntity(final Player player, final int radius){
        if (boss_wither == null){
            return false;
        }
        spawnEntity(boss_wither, getLocation(boss_wither, player.getLocation(), radius));
        return true;
    }

    public static void spawnEntity(final EntityType entityType, final Location location) {

        if (HuyaHandlerMain.instance.getConfig().getBoolean("enableItemDrops", false)) {
            ItemSpawnUtils.spawnRandomItem(location);
        }
        if (!HuyaHandlerMain.instance.getConfig().getBoolean("disableMonsters", false)) {
            new BukkitRunnable() {
                public void run() {
                    if (entityType == EntityType.GIANT) {

                        GiantEntity.spawn((Entity)new GiantEntity((World)((CraftWorld)location.getWorld()).getHandle()), location);
                    }
                    else {
                        location.getWorld().spawnEntity(location, entityType);
                    }
                }
            }.runTask((Plugin)HuyaHandlerMain.instance);
        }
    }

    public static Location randomLocation(final Location center, final int radius) {
        final Random random = new Random();
        final double x = random.nextInt(radius) + center.getX() - radius / 2.0;
        final double y = random.nextInt(3) + center.getY() - 1.5;
        final double z = random.nextInt(radius) + center.getZ() - radius / 2.0;
        return new Location(center.getWorld(), x, y, z);
    }

    public static Location getLocation(final EntityType entityType, final Location center, final int radius) {
        List<String> monsterLocation = HuyaHandlerMain.instance.getConfig().getStringList("monsterLocation");
        for (String t : monsterLocation) {
            String[] ts = t.split(";");
            if (ts.length == 2){
                if (entityType.name().equals(ts[0])){
                    final double x = center.getX();
                    final double y = center.getY() + 1.5;
                    final double z = center.getZ()-Integer.parseInt(ts[1]);
                    return new Location(center.getWorld(), x, y, z);
                }
            }
        }
        return randomLocation(center, radius);
    }

    public static EntityType randomMob() {
        final EntityType entityType = MobSpawnUtils.entityTypes.get(new Random().nextInt(MobSpawnUtils.entityTypes.size()));
        return entityType;
    }

    static {
        MobSpawnUtils.entityTypes = new ArrayList<EntityType>();
        for (final EntityType entityType : EntityType.values()) {
            final Class<? extends org.bukkit.entity.Entity> entityClass = (Class<? extends org.bukkit.entity.Entity>)entityType.getEntityClass();
            if (entityClass != null) {
                final boolean isMonster = Monster.class.isAssignableFrom(entityClass) || Flying.class.isAssignableFrom(entityClass) || Slime.class.isAssignableFrom(entityClass) || entityClass.equals(Shulker.class);
                final boolean isBoss = Boss.class.isAssignableFrom(entityClass);
                if (isBoss && Wither.class.isAssignableFrom(entityClass)){
                    boss_wither = entityType;
                }
                if (isMonster && !isBoss) {
                    MobSpawnUtils.entityTypes.add(entityType);
                }
            }
        }
    }
}

