package com.technovision.voodoo.events;

import com.technovision.voodoo.Poppet;
import com.technovision.voodoo.util.PoppetUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;

import static com.technovision.voodoo.Poppet.PoppetType.*;

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
