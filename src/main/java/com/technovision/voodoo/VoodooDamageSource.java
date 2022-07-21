package com.technovision.voodoo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import static com.technovision.voodoo.VoodooDamageSource.VoodooDamageType.FIRE;

public class VoodooDamageSource extends DamageSource {

    private VoodooDamageType damageType;
    private ItemStack voodooPoppet;
    private Entity fromEntity;

    public VoodooDamageSource(VoodooDamageType damageType, ItemStack voodooPoppet, Entity fromEntity) {
        super("voodoo_" + damageType.toString());
        this.damageType = damageType;
        this.voodooPoppet = voodooPoppet;
        this.fromEntity = fromEntity;
    }

    @Override
    public Text getDeathMessage(LivingEntity entity) {
        return Text.literal(entity.getName().getString() + " was killed by voodoo-magic."); //TODO: Translation
    }

    @Override
    public boolean isFire() {
        return damageType == FIRE;
    }

    @Override
    public boolean bypassesArmor() {
        return true;
    }

    @Override
    public boolean bypassesProtection() {
        return true;
    }

    @Override
    public boolean isMagic() {
        return true;
    }

    @Override
    public boolean isScaledWithDifficulty() {
        return false;
    }

    public ItemStack getVoodooPoppet() {
        return voodooPoppet;
    }

    public Entity getFromEntity() {
        return fromEntity;
    }

    public enum VoodooDamageType {
        NEEDLE,
        FIRE,
        WATER,
        VAMPIRIC
    }
}
