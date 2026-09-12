package com.technovision.voodoo.test;

import com.technovision.voodoo.*;
import com.technovision.voodoo.blocks.entities.PoppetShelfBlockEntity;
import com.technovision.voodoo.registry.*;
import com.technovision.voodoo.screens.PoppetShelfScreen;
import com.technovision.voodoo.util.BindingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.common.book.BookRegistry;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

/** Opt-in integrated-client checks; never included in the release JAR. */
@Mod(value = "voodoo_test", dist = Dist.CLIENT)
public final class VoodooClientSmokeTest {
    private int stage, ticks, total;
    private CompletableFuture<BlockPos> placement;
    private BlockPos shelfPos;
    public VoodooClientSmokeTest() {
        if (Boolean.getBoolean("voodoo.clientSmoke")) NeoForge.EVENT_BUS.addListener(this::tick);
    }
    private void next() { stage++; ticks = 0; }
    private void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private void screenshot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(), 1, ignored -> {});
    }
    private void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (stage == 99) return;
        try {
            if (++total > 6000) throw new AssertionError("Client smoke test timed out at stage " + stage);
            ticks++;
            switch (stage) {
                case 0 -> {
                    if (!(mc.screen instanceof TitleScreen)) return;
                    mc.options.pauseOnLostFocus = false;
                    mc.options.renderDistance().set(4);
                    mc.options.guiScale().set(2);
                    mc.getWindow().setWindowed(1100, 750);
                    next();
                    mc.createWorldOpenFlows().createFreshLevel("voodoo-smoke-" + System.currentTimeMillis(),
                        new LevelSettings("Voodoo Poppets: Reborn Smoke Test", GameType.CREATIVE,
                            LevelSettings.DifficultySettings.DEFAULT, true, WorldDataConfiguration.DEFAULT),
                        new WorldOptions(42, false, false), WorldPresets::createFlatWorldDimensions, mc.screen);
                }
                case 1 -> {
                    if (mc.player == null || mc.level == null || mc.screen != null || ticks < 100) return;
                    var server = mc.getSingleplayerServer();
                    var uuid = mc.player.getUUID();
                    placement = server.submit(() -> {
                        var player = server.getPlayerList().getPlayer(uuid);
                        var level = player.level();
                        var pos = player.blockPosition().offset(0, 0, 3);
                        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
                        level.setBlockAndUpdate(pos, ModBlocks.POPPET_SHELF_BLOCK.defaultBlockState());
                        var shelf = (PoppetShelfBlockEntity)level.getBlockEntity(pos);
                        shelf.setOwnerUuid(uuid); shelf.setOwnerName(player.getName().getString());
                        int i = 0;
                        for (var type : Poppet.PoppetType.values()) {
                            var stack = new ItemStack(ModItems.poppetMap.get(type));
                            BindingUtil.bind(stack, player);
                            if (i < 9) shelf.setItem(i, stack.copy());
                            player.getInventory().setItem(i++, stack);
                        }
                        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.VOODOO_MANUAL));
                        player.teleportTo(pos.getX() + .5, pos.getY() + .2, pos.getZ() - 2.5);
                        player.setYRot(0); player.setXRot(20);
                        return pos;
                    });
                    next();
                }
                case 2 -> {
                    if (!placement.isDone() || ticks < 100) return;
                    shelfPos = placement.join();
                    var shelf = (PoppetShelfBlockEntity)mc.level.getBlockEntity(shelfPos);
                    check(shelf != null && shelf.getItems().stream().filter(s -> !s.isEmpty()).count() == 9,
                        "Shelf contents did not synchronize");
                    mc.player.setYRot(0); mc.player.setXRot(20);
                    screenshot(mc, "01-poppet-shelf");
                    var uuid = mc.player.getUUID();
                    mc.getSingleplayerServer().execute(() -> {
                        var p = mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);
                        p.openMenu((PoppetShelfBlockEntity)p.level().getBlockEntity(shelfPos));
                    });
                    next();
                }
                case 3 -> {
                    if (ticks < 30) return;
                    check(mc.screen instanceof PoppetShelfScreen, "Shelf menu did not open");
                    screenshot(mc, "02-shelf-menu");
                    mc.player.closeContainer();
                    mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                    next();
                }
                case 4 -> {
                    if (ticks < 30) return;
                    check(Voodoo.id("voodoo_manual").equals(PatchouliAPI.get().getOpenBookGui()), "Craftable manual did not open Patchouli");
                    var book = BookRegistry.INSTANCE.books.get(Voodoo.id("voodoo_manual"));
                    check(book != null && !book.getContents().isErrored(), "Patchouli book failed to load");
                    check(book.getContents().entries.size() == 18, "Expected all 18 guide entries");
                    check(book.getContents().categories.size() == 2, "Expected both guide categories");
                    check(book.getBookItem().is(ModItems.VOODOO_MANUAL), "Patchouli book item differs from craftable manual");
                    screenshot(mc, "03-patchouli-guide");
                    PatchouliAPI.get().openBookEntry(Voodoo.id("voodoo_manual"), Voodoo.id("poppets/voodoo_poppet"), 0);
                    next();
                }
                case 5 -> {
                    if (ticks < 30) return;
                    screenshot(mc, "04-patchouli-recipe");
                    var book = BookRegistry.INSTANCE.books.get(Voodoo.id("voodoo_manual"));
                    for (var entry : book.getContents().entries.values()) {
                        check(!entry.getPages().isEmpty(), "Empty guide entry " + entry.getId());
                        for (int page = 0; page < entry.getPages().size(); page += 2)
                            PatchouliAPI.get().openBookEntry(book.id, entry.getId(), page);
                    }
                    next();
                }
                case 6 -> {
                    if (ticks < 30) return;
                    Files.writeString(mc.gameDirectory.toPath().resolve("smoke-test-passed.txt"),
                        "PASS: shelf synchronization, shelf screen, craftable Patchouli manual, 18 entries, all guide spreads.\n");
                    System.out.println("VOODOO_REBORN_CLIENT_SMOKE_PASSED");
                    stage = 99;
                    mc.stop();
                }
            }
        } catch (Throwable failure) {
            failure.printStackTrace();
            try { Files.writeString(mc.gameDirectory.toPath().resolve("smoke-test-failed.txt"), failure.toString()); }
            catch (Exception ignored) { }
            stage = 99;
            mc.stop();
        }
    }
}
