package com.technovision.voodoo.events;

import com.technovision.voodoo.Poppet;
import com.technovision.voodoo.Voodoo;
import com.technovision.voodoo.registry.ModItems;
import com.technovision.voodoo.util.BindingUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

/**
 * Events that fire on the client side only
 *
 * @author TechnoVision
 */
public class ClientEvents {

    /**
     * Updates predicate for taglock item on client startup,
     */
    public static void propertyOverrideRegistry() {
        ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
            ModelPredicateProviderRegistry.register(
                    ModItems.TAGLOCK_KIT,
                    new Identifier(Voodoo.MOD_ID, "filled"),
                    (itemStack, clientWorld, livingEntity, num) -> BindingUtil.isBound(itemStack) ? 1 : 0
            );
            ModelPredicateProviderRegistry.register(
                    ModItems.poppetMap.get(Poppet.PoppetType.PROJECTILE_PROTECTION),
                    new Identifier(Voodoo.MOD_ID, "percentage_used"),
                    (itemStack, clientWorld, livingEntity, num) -> Math.min(1, Math.max(0, itemStack.getDamage() / (float) itemStack.getMaxDamage()))
            );
        });
    }
}
