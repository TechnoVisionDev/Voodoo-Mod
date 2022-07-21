package com.technovision.voodoo.util;

import com.technovision.voodoo.Poppet;
import com.technovision.voodoo.items.PoppetItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PoppetUtil {

    /**
     * Searches for a specific poppet that is bound to a player.
     *
     * @param player     The player that the poppet must be bound to
     * @param poppetType The type of the poppet
     * @return The found poppet or null
     */
    public static Poppet getPlayerPoppet(ServerPlayerEntity player, Poppet.PoppetType poppetType) {
        return getPoppetsInInventory(player)
                .stream()
                .filter(poppet -> poppet.getItem().getPoppetType() == poppetType)
                .findFirst()
                .orElse(null);
    }

    /**
     * Code for handling all the events that happen when a voodoo protection poppet is activated.
     * If the voodoo poppet was used by a player they will get a message telling them that the target had a protection poppet.
     * If the voodoo poppet was used by the item entity it will not send such a message.
     * In both cases the voodoo poppet that was used will be destroyed.
     *
     * @param voodooPoppet The voodoo poppet that was used
     * @param source       The entity that used the voodoo poppet
     */
    public static void useVoodooProtectionPuppet(ItemStack voodooPoppet, Entity source) {
        if (source instanceof final PlayerEntity fromPlayer) {
            fromPlayer.sendMessage(Text.translatable("text.voodoo.voodoo_protection.had", BindingUtil.getBoundName(voodooPoppet)), true);
            voodooPoppet.damage(Integer.MAX_VALUE, fromPlayer, playerEntity -> {
                playerEntity.sendToolBreakStatus(playerEntity.getActiveHand());
                // TODO: Add sound
                //playerEntity.world.playSound(null, playerEntity, SoundRegistry.voodooProtectionPoppetUsed.get(), SoundSource.PLAYERS, 1, 1);
            });
        } else {
            // TODO: Add sound
            //source.world.playSound(null, source, SoundRegistry.voodooProtectionPoppetUsed.get(), SoundSource.PLAYERS, 1, 1);
            voodooPoppet.decrement(1);
        }
    }

    /**
     * Retrieve all poppets in the inventory of a player.
     *
     * @param player The player
     * @return The found poppets
     */
    public static List<Poppet> getPoppetsInInventory(PlayerEntity player) {
        List<ItemStack> playerItems = new ArrayList<>();
        playerItems.addAll(player.getInventory().offHand);
        playerItems.addAll(player.getInventory().main);
        return playerItems.stream()
                .filter(stack -> stack.getItem() instanceof PoppetItem)
                .filter(stack -> player.getUuid().equals(BindingUtil.getBoundUUID(stack)))
                .map(stack -> new Poppet(player, (PoppetItem) stack.getItem(), stack))
                .collect(Collectors.toList());
    }
}
