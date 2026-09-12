package com.technovision.voodoo.items;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
public class VoodooManualItem extends Item {
    public static Runnable openGuide = () -> {};
    public VoodooManualItem(Properties properties) { super(properties); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) openGuide.run();
        return InteractionResult.SUCCESS;
    }
}
