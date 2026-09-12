package com.technovision.voodoo.items;

import com.technovision.voodoo.Voodoo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.PatchouliAPI;

public class VoodooManualItem extends Item {
    public VoodooManualItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer)
            PatchouliAPI.get().openBookGUI(serverPlayer, Voodoo.id("voodoo_manual"));
        return InteractionResult.SUCCESS;
    }
}
