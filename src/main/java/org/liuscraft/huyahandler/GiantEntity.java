package org.liuscraft.huyahandler;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GiantEntity extends EntityGiantZombie
{
    public static List<String> nameList;
    static {
        nameList = new ArrayList<String>();
        nameList.add("夏天y");
        nameList.add("六芒");
        nameList.add("LiusCraft");
        nameList.add("六芒猫");
        nameList.add("盖亚");
        nameList.add("迪迦");
        nameList.add("死ね");
    }
    public GiantEntity(final World var1) {
        super(EntityTypes.GIANT, var1);
    }

    public static void spawn(final Entity entity, final Location location) {
        ((CraftWorld)location.getWorld()).getHandle().addEntity(entity);

        entity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        try {
            final LivingEntity livingEntity = (LivingEntity)Bukkit.getEntity(entity.getUniqueID());
            livingEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(3.0);
            livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30.0);
            livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
            int i = new Random().nextInt(8)-1;
            if(nameList.get(i)!=null){
                livingEntity.setCustomName(nameList.get(i));
                livingEntity.setCustomNameVisible(true);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void initPathfinder() {
        this.goalSelector.a(8, (PathfinderGoal)new PathfinderGoalLookAtPlayer((EntityInsentient)this, (Class)EntityHuman.class, 8.0f));
        this.goalSelector.a(8, (PathfinderGoal)new PathfinderGoalRandomLookaround((EntityInsentient)this));
        this.applyEntityAI();
    }

    protected void applyEntityAI() {
        this.goalSelector.a(2, (PathfinderGoal)new PathfinderGoalGiantAttack(this, 1.0, false));
        this.goalSelector.a(7, (PathfinderGoal)new PathfinderGoalRandomStrollLand((EntityCreature)this, 1.0));
        this.targetSelector.a(1, (PathfinderGoal)new PathfinderGoalHurtByTarget((EntityCreature)this, new Class[0]).a(new Class[] { EntityPigZombie.class }));
        this.targetSelector.a(2, (PathfinderGoal)new PathfinderGoalNearestAttackableTarget((EntityInsentient)this, (Class)EntityHuman.class, true));
        if (this.world.spigotConfig.zombieAggressiveTowardsVillager) {
            this.targetSelector.a(3, (PathfinderGoal)new PathfinderGoalNearestAttackableTarget((EntityInsentient)this, (Class)EntityVillagerAbstract.class, false));
        }
        this.targetSelector.a(3, (PathfinderGoal)new PathfinderGoalNearestAttackableTarget((EntityInsentient)this, (Class)EntityIronGolem.class, true));
        this.targetSelector.a(5, (PathfinderGoal)new PathfinderGoalNearestAttackableTarget((EntityInsentient)this, (Class)EntityTurtle.class, 10, true, false, EntityTurtle.bo));
    }

    protected SoundEffect getSoundAmbient() {
        return SoundEffects.ENTITY_ZOMBIE_AMBIENT;
    }

    protected SoundEffect getSoundHurt(final DamageSource damagesource) {
        return SoundEffects.ENTITY_ZOMBIE_HURT;
    }

    protected SoundEffect getSoundDeath() {
        return SoundEffects.ENTITY_ZOMBIE_DEATH;
    }

    protected SoundEffect getSoundStep() {
        return SoundEffects.ENTITY_ZOMBIE_STEP;
    }

    protected void b(final BlockPosition blockPosition, final IBlockData iblockdata) {
        this.playSound(this.getSoundStep(), 0.15f, 1.0f);
    }

    public static class PathfinderGoalGiantAttack extends PathfinderGoalMeleeAttack
    {
        private final GiantEntity b;
        private int c;

        public PathfinderGoalGiantAttack(final GiantEntity var0, final double var1, final boolean var3) {
            super((EntityCreature)var0, var1, var3);
            this.b = var0;
        }

        public void c() {
            super.c();
            this.c = 0;
        }

        public void d() {
            super.d();
            this.b.setAggressive(false);
        }

        public void e() {
            super.e();
            ++this.c;
            this.b.setAggressive(this.c >= 5 && this.j() < this.k() / 2);
        }
    }
}
