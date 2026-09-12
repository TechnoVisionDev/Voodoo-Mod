package com.technovision.voodoo;

import com.technovision.voodoo.blocks.entities.PoppetShelfBlockEntity;
import com.technovision.voodoo.client.VoodooManual;
import com.technovision.voodoo.registry.*;
import com.technovision.voodoo.screens.PoppetShelfScreen;
import com.technovision.voodoo.util.BindingUtil;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class VoodooClientGameTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            var server = world.getServer();
            BlockPos pos = server.computeOnServer(s -> {
                var player = world.getConnection().getServerPlayer();
                var level = player.level();
                var shelfPos = player.blockPosition().offset(0, 0, 3);
                level.setBlockAndUpdate(shelfPos.below(), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(shelfPos, ModBlocks.POPPET_SHELF_BLOCK.defaultBlockState());
                var shelf = (PoppetShelfBlockEntity)level.getBlockEntity(shelfPos);
                shelf.setOwnerUuid(player.getUUID()); shelf.setOwnerName(player.getName().getString());
                int i = 0;
                for (var type : Poppet.PoppetType.values()) {
                    var stack = new ItemStack(ModItems.poppetMap.get(type));
                    BindingUtil.bind(stack, player);
                    player.getInventory().setItem(i, stack.copy());
                    if (i < 9) shelf.setItem(i, stack.copy());
                    i++;
                }
                var kit = new ItemStack(ModItems.TAGLOCK_KIT); BindingUtil.bind(kit, player);
                player.getInventory().setItem(15, kit);
                player.getInventory().setItem(16, new ItemStack(ModItems.TAGLOCK_KIT));
                player.getInventory().setItem(17, new ItemStack(ModItems.VOODOO_MANUAL));
                player.teleportTo(shelfPos.getX() + .5, shelfPos.getY() + 1.0, shelfPos.getZ() - 2.5);
                player.setYRot(0); player.setXRot(25);
                return shelfPos;
            });
            world.getConnection().waitForChunksRender();
            context.waitTicks(10);
            context.takeScreenshot("voodoo-shelf-render");
            context.runOnClient(client -> {
                var shelf = (PoppetShelfBlockEntity)client.level.getBlockEntity(pos);
                if (shelf == null || shelf.getItems().stream().allMatch(ItemStack::isEmpty)) throw new AssertionError("Shelf inventory did not synchronize to client");
            });
            server.runOnServer(s -> {
                var player = world.getConnection().getServerPlayer();
                player.openMenu((PoppetShelfBlockEntity)player.level().getBlockEntity(pos));
            });
            context.waitForScreen(PoppetShelfScreen.class);
            context.waitTicks(5);
            context.takeScreenshot("voodoo-shelf-menu");
            context.runOnClient(client -> client.player.closeContainer());
            context.runOnClient(client -> VoodooManual.open());
            context.waitForScreen(BookViewScreen.class);
            context.waitTicks(3);
            context.takeScreenshot("voodoo-manual-index");
            context.runOnClient(client -> ((BookViewScreen)client.gui.screen()).setPage(3));
            context.waitTicks(3);
            context.takeScreenshot("voodoo-manual-content");
            context.runOnClient(client -> client.gui.setScreen(null));
        }
    }
}
