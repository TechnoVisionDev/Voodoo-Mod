package com.technovision.voodoo.registry;

import com.technovision.voodoo.Voodoo;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Creates and registers custom sounds.
 *
 * @author TechnoVision
 */
public class ModSounds {

    public static final SoundEvent VOODOO_PROTECTION_POPPET_USED = new SoundEvent(new Identifier(Voodoo.MOD_ID, "poppet.voodoo_protection.used"));

    public static void registerSounds() {
        Registry.register(Registry.SOUND_EVENT, new Identifier(Voodoo.MOD_ID, "poppet.voodoo_protection.used"), VOODOO_PROTECTION_POPPET_USED);
    }
}
