package com.technovision.voodoo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import java.util.Locale;
public class VoodooDamageSource extends DamageSource {
    private final ItemStack voodooPoppet;
    private final Entity fromEntity;
    public VoodooDamageSource(VoodooDamageType type, ItemStack stack, Entity from) {
        super(from.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, Voodoo.id("voodoo_" + type.name().toLowerCase(Locale.ROOT)))));
        voodooPoppet = stack; fromEntity = from;
    }
    @Override public Component getLocalizedDeathMessage(LivingEntity entity) { return Component.translatable("text.voodoo.death", entity.getName().getString()); }
    public ItemStack getVoodooPoppet() { return voodooPoppet; }
    public Entity getFromEntity() { return fromEntity; }
    public static void register() { }
    public enum VoodooDamageType { NEEDLE, FIRE, WATER, VAMPIRIC }
}
