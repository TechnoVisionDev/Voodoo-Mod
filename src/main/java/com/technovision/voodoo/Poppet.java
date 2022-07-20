package com.technovision.voodoo;

import com.technovision.voodoo.items.PoppetItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Created by Timor Morrien
 */
public class Poppet {
    private final PlayerEntity player;
    private final PoppetItem item;
    private final ItemStack stack;

    public Poppet(PlayerEntity player, PoppetItem item, ItemStack stack) {
        this.player = player;
        this.item = item;
        this.stack = stack;
    }

    public PoppetItem getItem() {
        return item;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void use() {
        use(1);
    }

    public void use(int amount) {
        int durability = item.getPoppetType().getDurability();
        if (durability > 0) {
            stack.setDamage(stack.getDamage() + amount);
            if (stack.getMaxDamage() <= stack.getDamage()) {
                decrement();
            }
        } else {
            decrement();
        }
    }

    private void decrement() {
        stack.decrement(1);
        player.sendMessage(Text.translatable("text.voodoo.poppet.used_up", Text.translatable(item.getTranslationKey())), false);
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
            return WordUtils.capitalize(super.toString().replaceAll("_", " ").toLowerCase());
        }
    }
}
