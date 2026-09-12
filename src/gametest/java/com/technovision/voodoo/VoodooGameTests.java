package com.technovision.voodoo;

import com.mojang.authlib.GameProfile;
import com.technovision.voodoo.blocks.entities.PoppetShelfBlockEntity;
import com.technovision.voodoo.events.*;
import com.technovision.voodoo.recipes.BindPoppetRecipe;
import com.technovision.voodoo.registry.*;
import com.technovision.voodoo.util.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.*;

public class VoodooGameTests {
    private ServerPlayer player(GameTestHelper h) {
        var fixture = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), new GameProfile(UUID.randomUUID(), "voodoo-test"), ClientInformation.createDefault()) {
            @Override public GameType gameMode() { return GameType.SURVIVAL; }
            @Override public boolean isInvulnerableTo(ServerLevel level, DamageSource source) { return false; }
            @Override public void sendSystemMessage(Component message) { }
            @Override public void sendOverlayMessage(Component message) { }
        };
        fixture.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),
            new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND), fixture,
            net.minecraft.server.network.CommonListenerCookie.createInitial(fixture.getGameProfile(), false)) {
                @Override public void send(net.minecraft.network.protocol.Packet<?> packet) { }
                @Override public boolean hasClientLoaded() { return true; }
            };
        GameType.SURVIVAL.updatePlayerAbilities(fixture.getAbilities());
        return fixture;
    }
    private ItemStack bound(ServerPlayer player, Poppet.PoppetType type) {
        ItemStack stack = new ItemStack(ModItems.poppetMap.get(type));
        BindingUtil.bind(stack, player); return stack;
    }
    @GameTest public void allItemsAndRecipesLoad(GameTestHelper h) {
        h.assertTrue(ModItems.poppetMap.size() == 15, "All original poppets registered");
        for (var type : Poppet.PoppetType.values()) {
            String id = type.name().toLowerCase(Locale.ROOT) + "_poppet";
            h.assertTrue(h.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, Voodoo.id(id))).isPresent(), "Recipe loaded: " + id);
        }
        for (String id : List.of("needle", "taglock_kit", "poppet_shelf", "voodoo_manual", "bind_poppet"))
            h.assertTrue(h.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, Voodoo.id(id))).isPresent(), "Recipe loaded: " + id);
        h.succeed();
    }
    @GameTest public void bindingCraftingAndSerialization(GameTestHelper h) {
        var player = player(h);
        var kit = new ItemStack(ModItems.TAGLOCK_KIT); BindingUtil.bind(kit, player);
        var poppet = new ItemStack(ModItems.poppetMap.get(Poppet.PoppetType.FALL_PROTECTION)); poppet.setDamageValue(7);
        var input = CraftingInput.of(2, 1, List.of(kit, poppet)); var recipe = new BindPoppetRecipe();
        h.assertTrue(recipe.matches(input, h.getLevel()), "Taglock binds an unbound poppet");
        var output = recipe.assemble(input);
        h.assertTrue(player.getUUID().equals(BindingUtil.getBoundUUID(output)) && output.getDamageValue() == 7, "Crafting keeps binding and wear");
        h.assertTrue(!BindingUtil.isBound(poppet) && BindingUtil.isBound(kit), "Craft preview does not mutate inputs");
        h.assertTrue(!recipe.matches(CraftingInput.of(2, 1, List.of(kit, output)), h.getLevel()), "Cannot overwrite an existing binding");
        h.assertTrue(!recipe.matches(CraftingInput.of(2, 1, List.of(kit, new ItemStack(ModItems.poppetMap.get(Poppet.PoppetType.BLANK)))), h.getLevel()), "Blank poppets cannot bind");
        var ops = h.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var restored = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, output).getOrThrow()).getOrThrow();
        h.assertTrue(player.getUUID().equals(BindingUtil.getBoundUUID(restored)) && restored.getDamageValue() == 7, "Binding and wear survive save/load");
        h.assertTrue(kit.get(DataComponents.ITEM_MODEL).equals(Voodoo.id("taglock_kit_filled")), "Bound taglock uses filled model");
        BindingUtil.unbind(kit); h.assertTrue(!BindingUtil.isBound(kit), "Taglock can be unbound");
        h.succeed();
    }
    @GameTest public void protectionFamiliesCancelDamage(GameTestHelper h) {
        var player = player(h);
        var types = List.of(Poppet.PoppetType.FALL_PROTECTION, Poppet.PoppetType.FIRE_PROTECTION, Poppet.PoppetType.WATER_PROTECTION, Poppet.PoppetType.EXPLOSION_PROTECTION, Poppet.PoppetType.HUNGER_PROTECTION);
        var sources = List.of(player.damageSources().fall(), player.damageSources().inFire(), player.damageSources().drown(), player.damageSources().explosion(null, null), player.damageSources().starve());
        for (int i = 0; i < types.size(); i++) {
            player.removeAllEffects(); var stack = bound(player, types.get(i)); player.getInventory().setItem(0, stack);
            h.assertTrue(VoodooEvents.onDamageReceivedEvent(new DamageReceivedEvent(player, sources.get(i), 4)), "Protection activates: " + types.get(i));
            h.assertTrue(stack.getDamageValue() > 0 || stack.isEmpty(), "Protection spends durability");
        }
        player.removeAllEffects(); var stack = bound(player, Poppet.PoppetType.FALL_PROTECTION); player.getInventory().setItem(0, stack);
        float health = player.getHealth();
        h.assertTrue(!player.hurtServer(h.getLevel(), player.damageSources().fall(), 4) && player.getHealth() == health, "Actual damage mixin prevents fall damage");
        h.succeed();
    }
    @GameTest public void shelfPersistenceAndOwnership(GameTestHelper h) {
        var owner = player(h); var other = player(h); var pos = new BlockPos(1, 2, 1);
        h.setBlock(pos, ModBlocks.POPPET_SHELF_BLOCK);
        var shelf = h.getBlockEntity(pos, PoppetShelfBlockEntity.class);
        shelf.setOwnerUuid(owner.getUUID()); shelf.setOwnerName("Owner");
        shelf.setItem(0, bound(owner, Poppet.PoppetType.FIRE_PROTECTION));
        h.assertTrue(PoppetUtil.getPoppetsInShelves(owner).size() == 1, "Loaded shelf protects owner");
        h.assertTrue(PoppetUtil.getPoppetsInShelves(other).isEmpty(), "Shelf does not protect a different owner");
        var tag = shelf.saveWithFullMetadata(h.getLevel().registryAccess());
        var restored = (PoppetShelfBlockEntity)BlockEntity.loadStatic(shelf.getBlockPos(), shelf.getBlockState(), tag, h.getLevel().registryAccess());
        h.assertTrue(restored != null && owner.getUUID().equals(restored.getOwnerUuid()) && BindingUtil.isBound(restored.getItem(0)), "Shelf inventory and owner survive save/load");
        h.assertTrue(!shelf.canPlaceItemThroughFace(1, new ItemStack(Items.DIRT), Direction.UP), "Hoppers reject non-poppets");
        shelf.setRemoved(); h.assertTrue(PoppetUtil.getPoppetsInShelves(owner).isEmpty(), "Removed shelves stop protecting");
        h.succeed();
    }
    @GameTest public void deathProtection(GameTestHelper h) {
        var player = player(h); var stack = bound(player, Poppet.PoppetType.DEATH_PROTECTION); player.getInventory().setItem(0, stack);
        player.setHealth(0);
        h.assertTrue(!ServerLivingEntityEvents.ALLOW_DEATH.invoker().allowDeath(player, player.damageSources().generic(), 100), "Death cancelled");
        h.assertTrue(player.getHealth() == player.getMaxHealth() / 2 && player.hasEffect(MobEffects.REGENERATION) && player.hasEffect(MobEffects.ABSORPTION) && player.hasEffect(MobEffects.FIRE_RESISTANCE), "Death protection restores health and effects");
        h.assertTrue(stack.getDamageValue() == 1, "Death protection costs one of two uses");
        h.assertTrue(ServerLivingEntityEvents.ALLOW_DEATH.invoker().allowDeath(player, player.damageSources().fellOutOfWorld(), 100), "Death poppet does not replace void poppet");
        h.succeed();
    }
    @GameTest public void statusAndHungerProtection(GameTestHelper h) {
        var player = player(h); var server = h.getLevel().getServer();
        // Register the fixture only for synchronous event dispatch, then remove it before world ticking.
        server.getPlayerList().getPlayers().add(player);
        try {
            var wither = bound(player, Poppet.PoppetType.WITHER_PROTECTION); player.getInventory().setItem(0, wither);
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1));
            ServerTickEvents.END_SERVER_TICK.invoker().onEndTick(server);
            h.assertTrue(!player.hasEffect(MobEffects.WITHER) && wither.getDamageValue() == 1, "Wither protection removes wither");
            var potion = bound(player, Poppet.PoppetType.POTION_PROTECTION); player.getInventory().setItem(0, potion);
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
            ServerTickEvents.END_SERVER_TICK.invoker().onEndTick(server);
            h.assertTrue(!player.hasEffect(MobEffects.POISON) && potion.getDamageValue() == 3, "Potion protection pays amplifier plus one");
            var hunger = bound(player, Poppet.PoppetType.HUNGER_PROTECTION); player.getInventory().setItem(0, hunger); player.getFoodData().setFoodLevel(10);
            for (int i = 0; i < 100; i++) ServerTickEvents.END_SERVER_TICK.invoker().onEndTick(server);
            h.assertTrue(player.hasEffect(MobEffects.SATURATION) && hunger.getDamageValue() == 1, "Hunger checked once per five seconds");
        } finally { server.getPlayerList().getPlayers().remove(player); }
        h.succeed();
    }
    @GameTest public void voodooProtectionDestroysAttackingPoppet(GameTestHelper h) {
        var target = player(h); var attacker = player(h);
        var protection = bound(target, Poppet.PoppetType.VOODOO_PROTECTION); target.getInventory().setItem(0, protection);
        var attack = bound(target, Poppet.PoppetType.VOODOO);
        h.assertTrue(VoodooEvents.onDamageReceivedEvent(new DamageReceivedEvent(target, new VoodooDamageSource(VoodooDamageSource.VoodooDamageType.NEEDLE, attack, attacker), 2)), "Voodoo protection blocks needle");
        h.assertTrue(attack.isEmpty() && protection.isEmpty(), "Both poppets consumed");
        h.succeed();
    }
    @GameTest public void partialProtectionAndReflection(GameTestHelper h) {
        var defender = player(h); var attacker = player(h);
        var almostBroken = bound(defender, Poppet.PoppetType.FALL_PROTECTION); almostBroken.setDamageValue(29);
        defender.getInventory().setItem(0, almostBroken);
        h.assertTrue(VoodooEvents.onDamageReceivedEvent(new DamageReceivedEvent(defender, defender.damageSources().fall(), 10)), "Partial protection handled");
        h.assertTrue(almostBroken.isEmpty() && defender.getHealth() < 20 && defender.getHealth() > 10, "Partial protection applies only remaining damage");
        defender.setHealth(20); defender.invulnerableTime = 0;
        var reflector = bound(defender, Poppet.PoppetType.REFLECTOR); defender.getInventory().setItem(0, reflector);
        var attackerReflector = bound(attacker, Poppet.PoppetType.REFLECTOR); attacker.getInventory().setItem(0, attackerReflector);
        h.assertTrue(!defender.hurtServer(h.getLevel(), defender.damageSources().playerAttack(attacker), 4), "Reflector intercepts melee");
        h.assertTrue(defender.getHealth() == 20 && attacker.getHealth() == 16, "Damage returns to attacker exactly once");
        h.assertTrue(reflector.getDamageValue() == 1 && attackerReflector.getDamageValue() == 0, "Two reflectors cannot recurse");
        h.succeed();
    }
    @GameTest public void needleThrowingVampirismAndDroppedFire(GameTestHelper h) {
        var attacker = player(h); var target = player(h);
        var players = h.getLevel().getServer().getPlayerList().getPlayersByUUID();
        players.put(target.getUUID(), target);
        try {
            var poppet = bound(target, Poppet.PoppetType.VOODOO);
            attacker.setItemInHand(InteractionHand.MAIN_HAND, poppet);
            attacker.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.NEEDLE, 2));
            ModItems.poppetMap.get(Poppet.PoppetType.VOODOO).releaseUsing(poppet, h.getLevel(), attacker, 71980);
            h.assertTrue(target.getHealth() == 18 && poppet.getDamageValue() == 1 && attacker.getOffhandItem().getCount() == 1, "Needle damages target and consumes one needle");
            attacker.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            ModItems.poppetMap.get(Poppet.PoppetType.VOODOO).releaseUsing(poppet, h.getLevel(), attacker, 71980);
            h.assertTrue(target.getDeltaMovement().lengthSqr() > 0 && target.hurtMarked && poppet.getDamageValue() == 3, "Throwing pushes target and costs two uses");
            target.setHealth(20); target.invulnerableTime = 0; attacker.setHealth(10);
            var vampiric = bound(target, Poppet.PoppetType.VAMPIRIC);
            ModItems.poppetMap.get(Poppet.PoppetType.VAMPIRIC).onUseTick(h.getLevel(), attacker, vampiric, 71981);
            h.assertTrue(attacker.getHealth() == 13 && target.getHealth() == 17 && vampiric.getDamageValue() == 1, "Vampiric poppet transfers three health");
            target.setHealth(6); target.invulnerableTime = 0;
            ModItems.poppetMap.get(Poppet.PoppetType.VAMPIRIC).onUseTick(h.getLevel(), attacker, vampiric, 71961);
            h.assertTrue(target.getHealth() == 6 && vampiric.getDamageValue() == 1, "Vampirism respects target health floor");
            target.setHealth(20); target.invulnerableTime = 0;
            var dropped = new ItemEntity(h.getLevel(), 0, 0, 0, poppet);
            h.assertTrue(!dropped.hurtServer(h.getLevel(), dropped.damageSources().inFire(), 1), "Dropped poppet resists fire destruction");
            h.assertTrue(target.isOnFire() && target.getHealth() == 19 && poppet.getDamageValue() == 5, "Dropped fire reaches bound player");
        } finally { players.remove(target.getUUID()); }
        h.succeed();
    }
    @GameTest public void projectileAndVoidProtection(GameTestHelper h) {
        var player = player(h); var stack = bound(player, Poppet.PoppetType.PROJECTILE_PROTECTION);
        player.getInventory().setItem(0, stack);
        var arrow = new net.minecraft.world.entity.projectile.arrow.Arrow(net.minecraft.world.entity.EntityTypes.ARROW, h.getLevel());
        var source = player.damageSources().arrow(arrow, null);
        h.assertTrue(VoodooEvents.onDamageReceivedEvent(new DamageReceivedEvent(player, source, 5)) && arrow.isRemoved() && stack.getDamageValue() == 1, "Projectile protection removes arrow and spends one use");
        player.getInventory().setItem(0, bound(player, Poppet.PoppetType.VOID_PROTECTION));
        player.setPos(0, h.getLevel().getMinY() - 70, 0);
        h.assertTrue(VoodooEvents.getProtectionPoppets(new DamageReceivedEvent(player, player.damageSources().fellOutOfWorld(), 4)).contains(Poppet.PoppetType.VOID_PROTECTION), "Void detected below this dimension's minimum height");
        h.succeed();
    }

}
