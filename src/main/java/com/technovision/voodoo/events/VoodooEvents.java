package com.technovision.voodoo.events;

import com.technovision.voodoo.Poppet;
import com.technovision.voodoo.VoodooDamageSource;
import com.technovision.voodoo.util.PoppetUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static com.technovision.voodoo.Poppet.PoppetType.*;
import static net.minecraft.entity.damage.DamageSource.*;

/**
 * Handles functionality for protection poppets.
 *
 * @author TechnoVision
 */
public class VoodooEvents {

    private static int tickCount = 0;

    /**
     * Event that runs every time the server ticks. There are 20 ticks per second.
     * Checks player food and potion status and applies poppets as needed.
     */
    public static void onServerTickEvent() {
        ServerTickEvents.START_SERVER_TICK.register((server) -> {
            tickCount++;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                checkPotionEffects(player);
                if (tickCount % 100 != 0) {
                    tickCount = 0;
                    checkFoodStatus(player);
                }
            }
        });
    }

    /**
     * Event that runs every time a player dies.
     * Checks for a death protection poppet to save the player.
     */
    public static void onPlayerDeathEvent() {
        ServerPlayerEvents.ALLOW_DEATH.register((player, damageSource, damageAmount) -> {
            if (damageSource == DamageSource.OUT_OF_WORLD) return true;
            Poppet poppet = PoppetUtil.getPlayerPoppet(player, DEATH_PROTECTION);
            if (poppet != null) {
                poppet.use();
                player.setHealth(player.getMaxHealth() / 2);
                player.clearStatusEffects();
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 45 * 20, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 5 * 20, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40 * 20, 0));
                player.getWorld().sendEntityStatus(player, (byte) 35);
                return false;
            }
            return true;
        });
    }

    /**
     * Event that fires every time a player receives damage on the server side.
     * @param event the event details.
     * @return true if event is canceled and player takes no damage, otherwise false.
     */
    public static boolean onDamageReceivedEvent(DamageReceivedEvent event) {
        ServerPlayerEntity player = event.getPlayer();
        if (player.isCreative() || player.isDead() || (event.getSource().isFire() && player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))) return false;
        if (tryReflectorPoppet(event)) return true;

        final DamageSource damageSource = event.getSource();
        final List<Poppet.PoppetType> validPoppets = getProtectionPoppets(event);

        final List<Poppet> poppetsInInventory = PoppetUtil.getPoppetsInInventory(player);
        poppetsInInventory.removeIf(poppet -> !validPoppets.contains(poppet.getItem().getPoppetType()));

        int originalDurabilityCost = getDurabilityCost(event);
        int durabilityCost = originalDurabilityCost;
        for (int i = 0; i < poppetsInInventory.size() && durabilityCost > 0; i++) {
            Poppet poppet = poppetsInInventory.get(i);
            durabilityCost = usePoppet(poppet, durabilityCost);
        }
        if (durabilityCost > 0) {
            // TODO: Add poppet shelf use
            /**
            final List<Poppet> poppetsInShelves = PoppetUtil.getPoppetsInShelves(player);
            poppetsInShelves.removeIf(poppet -> !validPoppets.contains(poppet.getItem().getPoppetType()));

            for (int i = 0; i < poppetsInShelves.size() && durabilityCost > 0; i++) {
                Poppet poppet = poppetsInShelves.get(i);
                durabilityCost = usePoppet(poppet, durabilityCost);
            }
             */
        }
        if (durabilityCost != originalDurabilityCost) {
            doSpecialActions(event);
            if (durabilityCost > 0) {
                float percentage = ((float) durabilityCost) / ((float) originalDurabilityCost);
                player.damage(damageSource, event.getAmount() * percentage);
            }
            return true;
        }
        return false;
    }

    /**
     * Uses the reflector poppet to reflect damage back to attacker.
     * @param event the event details.
     * @return true if event should be canceled.
     */
    private static boolean tryReflectorPoppet(DamageReceivedEvent event) {
        if (!(event.getSource() instanceof EntityDamageSource)) return false;
        final Entity sourceEntity = event.getSource().getSource();
        if (sourceEntity == null) return false;
        final ServerPlayerEntity player =  event.getPlayer();
        if (player == sourceEntity) return false;
        final Poppet reflectorPoppet = PoppetUtil.getPlayerPoppet(player, REFLECTOR);
        if (reflectorPoppet == null) return false;
        sourceEntity.damage(event.getSource(), event.getAmount());
        reflectorPoppet.use();
        return true;
    }

    /**
     * Get a list of all poppets that may apply to the given damage event.
     * @param event the event details.
     * @return a list of poppet types that apply to given damage event.
     */
    public static List<Poppet.PoppetType> getProtectionPoppets(DamageReceivedEvent event) {
        final DamageSource damageSource = event.getSource();
        List<Poppet.PoppetType> suitablePoppets = new ArrayList<>();

        if (damageSource instanceof VoodooDamageSource)
            suitablePoppets.add(VOODOO_PROTECTION);
        if (damageSource.getSource() instanceof PotionEntity)
            suitablePoppets.add(POTION_PROTECTION);
        if (damageSource == FALL)
            suitablePoppets.add(FALL_PROTECTION);
        if (damageSource.isProjectile())
            suitablePoppets.add(PROJECTILE_PROTECTION);
        if (damageSource.isFire())
            suitablePoppets.add(FIRE_PROTECTION);
        if (damageSource.isExplosive())
            suitablePoppets.add(EXPLOSION_PROTECTION);
        if (damageSource == DROWN)
            suitablePoppets.add(WATER_PROTECTION);
        if (damageSource == STARVE)
            suitablePoppets.add(HUNGER_PROTECTION);
        if (damageSource == OUT_OF_WORLD && event.getPlayer().getY() < 0)
            suitablePoppets.add(VOID_PROTECTION);
        return suitablePoppets;
    }

    /**
     *
     * @param event
     */
    private static void doSpecialActions(DamageReceivedEvent event) {
        final DamageSource damageSource = event.getSource();
        final ServerPlayerEntity player = event.getPlayer();
        if (damageSource.isFire()) {
            player.setOnFire(false);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 10 * 20, 0));
        }
        if (damageSource == DROWN) {
            player.setAir(300);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 20 * 20, 0));
        }
        if (damageSource.isProjectile() && damageSource.getSource() instanceof ArrowEntity) {
            damageSource.getSource().kill();
        }
        if (damageSource instanceof final VoodooDamageSource voodooDamageSource) {
            PoppetUtil.useVoodooProtectionPuppet(voodooDamageSource.getVoodooPoppet(), voodooDamageSource.getFromEntity());
        }
        if (damageSource == OUT_OF_WORLD && player.getY() < 0) {
            player.fallDistance = 0;
            BlockPos spawnPos = player.getSpawnPointPosition();
            ServerWorld serverWorld = player.server.getWorld(player.getSpawnPointDimension());
            if (serverWorld == null)
                serverWorld = player.server.getOverworld();
            if (spawnPos == null) {
                spawnPos = new BlockPos(
                        serverWorld.getSpawnPos().getX(),
                        serverWorld.getSpawnPos().getY(),
                        serverWorld.getSpawnPos().getZ()
                );
            }
            player.teleport(
                    serverWorld,
                    spawnPos.getX(),
                    spawnPos.getY() + 1,
                    spawnPos.getZ(),
                    player.getYaw(),
                    player.getPitch()
            );
        }
    }

    private static int getDurabilityCost(DamageReceivedEvent event) {
        final DamageSource damageSource = event.getSource();
        if (damageSource instanceof VoodooDamageSource)
            return 1;
        if ((damageSource.getSource() instanceof PotionEntity || damageSource.getSource() instanceof AreaEffectCloudEntity))
            return (int) (Math.log(event.getAmount() / 6) / Math.log(2)) + 1;
        if (damageSource == FALL)
            return (int) Math.min(event.getAmount(), Math.ceil(Math.log(event.getAmount()) * 3));
        if (damageSource.isProjectile())
            return 1;
        if (damageSource.isFire())
            return 1;
        if (damageSource.isExplosive())
            return 1;
        if (damageSource == DROWN)
            return 1;
        if (damageSource == OUT_OF_WORLD)
            return 1;
        return 0;
    }

    /**
     * Checks if a user has a harmful potion affect and seeks to use a poppet to remedy it.
     * @param player the player being inspected.
     */
    private static void checkPotionEffects(ServerPlayerEntity player) {
        final ArrayList<StatusEffectInstance> effects = new ArrayList<>(player.getStatusEffects());
        for (StatusEffectInstance potionEffect : effects) {
            if (potionEffect.getEffectType().getCategory() != StatusEffectCategory.HARMFUL) continue;
            if (potionEffect.getEffectType() == StatusEffects.WITHER) {
                removeWitherEffect(player, potionEffect);
            } else {
                removePotionEffect(player, potionEffect);
            }
        }
    }

    /**
     * Removes a wither status effect from a player.
     * @param player the player to remove the effect from.
     * @param potionEffect the instance of the wither status effect.
     */
    private static void removeWitherEffect(ServerPlayerEntity player, StatusEffectInstance potionEffect) {
        Poppet witherPoppet = PoppetUtil.getPlayerPoppet(player, WITHER_PROTECTION);
        if (witherPoppet == null) return;
        player.removeStatusEffect(potionEffect.getEffectType());
        witherPoppet.use();
    }

    /**
     * Removes a potion status effect from a player.
     * @param player the player to remove the effect from.
     * @param potionEffect the instance of the potion status effect.
     */
    private static void removePotionEffect(ServerPlayerEntity player, StatusEffectInstance potionEffect) {
        int durabilityCost = potionEffect.getAmplifier() + 1;
        while (durabilityCost > 0) {
            Poppet poppet = PoppetUtil.getPlayerPoppet(player, POTION_PROTECTION);
            if (poppet == null) break;
            durabilityCost = usePoppet(poppet, durabilityCost);
        }
        if (durabilityCost == potionEffect.getAmplifier() + 1) return;
        player.removeStatusEffect(potionEffect.getEffectType());
        if (durabilityCost > 0) {
            final StatusEffectInstance effectInstance = new StatusEffectInstance(
                    potionEffect.getEffectType(),
                    potionEffect.getDuration(),
                    durabilityCost - 1,
                    potionEffect.isAmbient(),
                    potionEffect.shouldShowParticles(),
                    potionEffect.shouldShowIcon()
            );
            player.addStatusEffect(effectInstance);
        }
    }

    /**
     * Checks if a user is at half hunger and seeks to use a poppet to remedy.
     * @param player the player being inspected.
     */
    private static void checkFoodStatus(ServerPlayerEntity player) {
        if (player.getHungerManager().getFoodLevel() > 10) return;
        final Poppet hungerPoppet = PoppetUtil.getPlayerPoppet(player, HUNGER_PROTECTION);
        if (hungerPoppet == null) return;
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 60 * 20, 1));
        usePoppet(hungerPoppet, 1);
    }

    /**
     * Uses a poppet ability with a specified durability hit.
     * @param poppet the poppet being used.
     * @param durabilityCost the amount of durability to remove from poppet.
     * @return the durability remaining on the poppet.
     */
    private static int usePoppet(Poppet poppet, int durabilityCost) {
        final int currentDamage = poppet.getStack().getDamage();
        final int maxDamage = Math.max(1, poppet.getStack().getMaxDamage());
        final int remaining = maxDamage - currentDamage;
        if (remaining < durabilityCost) {
            poppet.use(remaining);
            return durabilityCost - remaining;
        } else {
            poppet.use(durabilityCost);
            return 0;
        }
    }
}
