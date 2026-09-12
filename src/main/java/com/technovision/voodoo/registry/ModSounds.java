package com.technovision.voodoo.registry;
import com.technovision.voodoo.Voodoo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
public class ModSounds {
    public static final SoundEvent VOODOO_PROTECTION_POPPET_USED = Registry.register(BuiltInRegistries.SOUND_EVENT, Voodoo.id("poppet.voodoo_protection.used"), SoundEvent.createVariableRangeEvent(Voodoo.id("poppet.voodoo_protection.used")));
    public static void registerSounds() {}
}
