package com.technovision.voodoo.event;

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
                    (itemStack, clientWorld, livingEntity, num) -> BindingUtil.isBound(itemStack) ? 1 : 0);
        });
    }
}
