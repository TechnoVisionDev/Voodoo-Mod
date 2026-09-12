package com.technovision.voodoo.events;

import com.technovision.voodoo.Poppet;
import com.technovision.voodoo.VoodooDamageSource;
import com.technovision.voodoo.util.PoppetUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static com.technovision.voodoo.Poppet.PoppetType.*;


/**
 * Handles functionality for protection poppets.
 *
 * @author TechnoVision
 */
public class VoodooEvents {

    private static int tickCount = 0;
    private static final ThreadLocal<Boolean> applyingDamage = ThreadLocal.withInitial(() -> false);
    private static void applyWithoutPoppets(ServerPlayer player, DamageSource source, float amount) {
        boolean previous = applyingDamage.get();
        applyingDamage.set(true);
        try { player.hurtServer(player.level(), source, amount); }
        finally { applyingDamage.set(previous); }
    }

    /**
     * Event that runs every time the server ticks. There are 20 ticks per second.
     * Checks player food and potion status and applies poppets as needed.
     */
    public static void onServerTickEvent() {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> { PoppetUtil.clear(); tickCount = 0; });
        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            tickCount = (tickCount + 1) % 100;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                checkPotionEffects(player);
                if (tickCount % 100 == 0) {
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
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof ServerPlayer player)) return true;
            if (damageSource.is(DamageTypes.FELL_OUT_OF_WORLD)) return true;
            Poppet poppet = PoppetUtil.getPlayerPoppet(player, DEATH_PROTECTION);
            if (poppet != null) {
                poppet.use();
                player.setHealth(player.getMaxHealth() / 2);
                player.removeAllEffects();
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 45 * 20, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 5 * 20, 1));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40 * 20, 0));
                player.level().broadcastEntityEvent(player, (byte) 35);
                return false;
            }
            return true;
        });
    }

    /**
     * Event that fires every time a player receives damage on the server side.
     *
     * @param event the event details.
     * @return true if event is canceled and player takes no damage, otherwise false.
     */
    public static boolean onDamageReceivedEvent(DamageReceivedEvent event) {
        ServerPlayer player = event.getPlayer();
        if (applyingDamage.get() || event.getAmount() <= 0 || player.isInvulnerableTo(player.level(), event.getSource())) return false;
        if (player.isCreative() || player.isDeadOrDying() || (event.getSource().is(DamageTypeTags.IS_FIRE) && player.hasEffect(MobEffects.FIRE_RESISTANCE))) return false;
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
            final List<Poppet> poppetsInShelves = PoppetUtil.getPoppetsInShelves(player);
            poppetsInShelves.removeIf(poppet -> !validPoppets.contains(poppet.getItem().getPoppetType()));

            for (int i = 0; i < poppetsInShelves.size() && durabilityCost > 0; i++) {
                Poppet poppet = poppetsInShelves.get(i);
                durabilityCost = usePoppet(poppet, durabilityCost);
            }
        }
        if (durabilityCost != originalDurabilityCost) {
            doSpecialActions(event);
            if (durabilityCost > 0) {
                float percentage = ((float) durabilityCost) / ((float) originalDurabilityCost);
                applyWithoutPoppets(player, damageSource, event.getAmount() * percentage);
            }
            return true;
        }
        return false;
    }

    /**
     * Uses the reflector poppet to reflect damage back to attacker.
     *
     * @param event the event details.
     * @return true if event should be canceled.
     */
    private static boolean tryReflectorPoppet(DamageReceivedEvent event) {
        if (event.getSource() instanceof VoodooDamageSource) return false;
        final Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity == null) return false;
        final ServerPlayer player =  event.getPlayer();
        if (player == sourceEntity) return false;
        final Poppet reflectorPoppet = PoppetUtil.getPlayerPoppet(player, REFLECTOR);
        if (reflectorPoppet == null) return false;
        reflectorPoppet.use();
        if (sourceEntity instanceof ServerPlayer target) applyWithoutPoppets(target, event.getSource(), event.getAmount());
        else sourceEntity.hurtServer(player.level(), event.getSource(), event.getAmount());
        return true;
    }

    /**
     * Get a list of all poppets that may apply to the given damage event.
     *
     * @param event the event details.
     * @return a list of poppet types that apply to given damage event.
     */
    public static List<Poppet.PoppetType> getProtectionPoppets(DamageReceivedEvent event) {
        final DamageSource damageSource = event.getSource();
        List<Poppet.PoppetType> suitablePoppets = new ArrayList<>();

        if (damageSource instanceof VoodooDamageSource)
            suitablePoppets.add(VOODOO_PROTECTION);
        if (damageSource.getDirectEntity() instanceof AbstractThrownPotion || damageSource.getDirectEntity() instanceof AreaEffectCloud)
            suitablePoppets.add(POTION_PROTECTION);
        if (damageSource.is(DamageTypes.FALL))
            suitablePoppets.add(FALL_PROTECTION);
        if (damageSource.is(DamageTypeTags.IS_PROJECTILE))
            suitablePoppets.add(PROJECTILE_PROTECTION);
        if (damageSource.is(DamageTypeTags.IS_FIRE))
            suitablePoppets.add(FIRE_PROTECTION);
        if (damageSource.is(DamageTypeTags.IS_EXPLOSION))
            suitablePoppets.add(EXPLOSION_PROTECTION);
        if (damageSource.is(DamageTypes.DROWN))
            suitablePoppets.add(WATER_PROTECTION);
        if (damageSource.is(DamageTypes.STARVE))
            suitablePoppets.add(HUNGER_PROTECTION);
        if (damageSource.is(DamageTypes.FELL_OUT_OF_WORLD) && event.getPlayer().getY() < event.getPlayer().level().getMinY())
            suitablePoppets.add(VOID_PROTECTION);
        return suitablePoppets;
    }

    /**
     * Perform an action from one of the protection poppets.
     * This may include status effects, teleportation, etc.
     *
     * @param event the event details.
     */
    private static void doSpecialActions(DamageReceivedEvent event) {
        final DamageSource damageSource = event.getSource();
        final ServerPlayer player = event.getPlayer();
        if (damageSource.is(DamageTypeTags.IS_FIRE)) {
            player.clearFire();
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 10 * 20, 0));
        }
        if (damageSource.is(DamageTypes.DROWN)) {
            player.setAirSupply(300);
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 20 * 20, 0));
        }
        if (damageSource.is(DamageTypeTags.IS_PROJECTILE) && damageSource.getDirectEntity() instanceof Arrow) {
            damageSource.getDirectEntity().discard();
        }
        if (damageSource instanceof final VoodooDamageSource voodooDamageSource) {
            PoppetUtil.useVoodooProtectionPuppet(voodooDamageSource.getVoodooPoppet(), voodooDamageSource.getFromEntity());
        }
        if (damageSource.is(DamageTypes.FELL_OUT_OF_WORLD) && player.getY() < player.level().getMinY()) {
            player.fallDistance = 0;
            player.teleport(player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING));
        }
    }

    /**
     * Calculate the amount of durability to be removed from the poppet based on the incoming damage.
     *
     * @param event the event details.
     * @return the number of durability to take away from poppet.
     */
    private static int getDurabilityCost(DamageReceivedEvent event) {
        final DamageSource damageSource = event.getSource();
        if (damageSource instanceof VoodooDamageSource)
            return 1;
        if ((damageSource.getDirectEntity() instanceof AbstractThrownPotion || damageSource.getDirectEntity() instanceof AreaEffectCloud))
            return Math.max(1, (int) (Math.log(event.getAmount() / 6) / Math.log(2)) + 1);
        if (damageSource.is(DamageTypes.FALL))
            return Math.max(1, (int) Math.min(event.getAmount(), Math.ceil(Math.log(event.getAmount()) * 3)));
        if (damageSource.is(DamageTypeTags.IS_PROJECTILE))
            return 1;
        if (damageSource.is(DamageTypeTags.IS_FIRE))
            return 1;
        if (damageSource.is(DamageTypeTags.IS_EXPLOSION))
            return 1;
        if (damageSource.is(DamageTypes.DROWN) || damageSource.is(DamageTypes.STARVE))
            return 1;
        if (damageSource.is(DamageTypes.FELL_OUT_OF_WORLD))
            return 1;
        return 0;
    }

    /**
     * Checks if a user has a harmful potion affect and seeks to use a poppet to remedy it.
     *
     * @param player the player being inspected.
     */
    private static void checkPotionEffects(ServerPlayer player) {
        final ArrayList<MobEffectInstance> effects = new ArrayList<>(player.getActiveEffects());
        for (MobEffectInstance potionEffect : effects) {
            if (potionEffect.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) continue;
            if (potionEffect.getEffect() == MobEffects.WITHER) {
                removeWitherEffect(player, potionEffect);
            } else {
                removePotionEffect(player, potionEffect);
            }
        }
    }

    /**
     * Removes a wither status effect from a player.
     *
     * @param player the player to remove the effect from.
     * @param potionEffect the instance of the wither status effect.
     */
    private static void removeWitherEffect(ServerPlayer player, MobEffectInstance potionEffect) {
        Poppet witherPoppet = PoppetUtil.getPlayerPoppet(player, WITHER_PROTECTION);
        if (witherPoppet == null) return;
        player.removeEffect(potionEffect.getEffect());
        witherPoppet.use();
    }

    /**
     * Removes a potion status effect from a player.
     *
     * @param player the player to remove the effect from.
     * @param potionEffect the instance of the potion status effect.
     */
    private static void removePotionEffect(ServerPlayer player, MobEffectInstance potionEffect) {
        int durabilityCost = potionEffect.getAmplifier() + 1;
        while (durabilityCost > 0) {
            Poppet poppet = PoppetUtil.getPlayerPoppet(player, POTION_PROTECTION);
            if (poppet == null) break;
            durabilityCost = usePoppet(poppet, durabilityCost);
        }
        if (durabilityCost == potionEffect.getAmplifier() + 1) return;
        player.removeEffect(potionEffect.getEffect());
        if (durabilityCost > 0) {
            final MobEffectInstance effectInstance = new MobEffectInstance(
                    potionEffect.getEffect(),
                    potionEffect.getDuration(),
                    durabilityCost - 1,
                    potionEffect.isAmbient(),
                    potionEffect.isVisible(),
                    potionEffect.showIcon()
            );
            player.addEffect(effectInstance);
        }
    }

    /**
     * Checks if a user is at half hunger and seeks to use a poppet to remedy.
     *
     * @param player the player being inspected.
     */
    private static void checkFoodStatus(ServerPlayer player) {
        if (player.getFoodData().getFoodLevel() > 10) return;
        final Poppet hungerPoppet = PoppetUtil.getPlayerPoppet(player, HUNGER_PROTECTION);
        if (hungerPoppet == null) return;
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60 * 20, 1));
        usePoppet(hungerPoppet, 1);
    }

    /**
     * Uses a poppet ability with a specified durability hit.
     *
     * @param poppet the poppet being used.
     * @param durabilityCost the amount of durability to remove from poppet.
     * @return the durability remaining on the poppet.
     */
    private static int usePoppet(Poppet poppet, int durabilityCost) {
        final int currentDamage = poppet.getStack().getDamageValue();
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
