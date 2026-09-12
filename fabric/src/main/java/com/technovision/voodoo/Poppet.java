package com.technovision.voodoo;

import com.technovision.voodoo.blocks.entities.PoppetShelfBlockEntity;
import com.technovision.voodoo.items.PoppetItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;


import java.util.Optional;

/**
 * Represents a basic poppet object.
 *
 * @author TechnoVision
 */
public class Poppet {
    private final Player player;
    private final Optional<PoppetShelfBlockEntity> poppetShelf;
    private final PoppetItem item;
    private final ItemStack stack;

    public Poppet(PoppetShelfBlockEntity poppetShelf, Player player, PoppetItem item, ItemStack stack) {
        this.poppetShelf = Optional.of(poppetShelf);
        this.player = player;
        this.item = item;
        this.stack = stack;
    }
    public Poppet(Player player, PoppetItem item, ItemStack stack) {
        this.poppetShelf = Optional.empty();
        this.player = player;
        this.item = item;
        this.stack = stack;
    }

    public PoppetItem getItem() {
        return item;
    }

    public ItemStack getStack() {
        poppetShelf.ifPresent(PoppetShelfBlockEntity::inventoryTouched);
        return stack;
    }

    public Optional<PoppetShelfBlockEntity> getPoppetShelf() {
        return poppetShelf;
    }

    public void use() {
        use(1);
    }

    public void use(int amount) {
        int durability = item.getPoppetType().getDurability();
        if (durability > 0) {
            stack.setDamageValue(stack.getDamageValue() + amount);
            if (stack.getMaxDamage() <= stack.getDamageValue()) {
                shrink();
            }
        } else {
            shrink();
        }
        poppetShelf.ifPresent(PoppetShelfBlockEntity::inventoryTouched);
    }

    private void shrink() {
        stack.shrink(1);
        player.sendSystemMessage(Component.translatable("text.voodoo.poppet.used_up", Component.translatable(item.getDescriptionId())));
    }

    public enum PoppetType {
        BLANK(),
        VOODOO(20),
        VAMPIRIC(20),
        REFLECTOR(10),
        VOODOO_PROTECTION(1),
        DEATH_PROTECTION(2),
        FIRE_PROTECTION(10),
        WATER_PROTECTION(5),
        FALL_PROTECTION(30),
        EXPLOSION_PROTECTION(4),
        PROJECTILE_PROTECTION(10),
        WITHER_PROTECTION(10),
        HUNGER_PROTECTION(5),
        POTION_PROTECTION(6),
        VOID_PROTECTION(0);

        private final Integer durability;

        PoppetType() {
            this.durability = null;
        }

        PoppetType(Integer durability) {
            this.durability = durability;
        }

        public boolean hasDurability() {
            return durability != null && durability > 0;
        }

        public int getDurability() {
            return durability == null ? 0 : durability;
        }

        @Override
        public String toString() {
            return java.util.Arrays.stream(name().toLowerCase(java.util.Locale.ROOT).split("_")).map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1)).collect(java.util.stream.Collectors.joining(" "));
        }
    }
}
