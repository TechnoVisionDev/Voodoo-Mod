package com.technovision.voodoo.test;

import com.technovision.voodoo.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod("voodoo_test")
public final class VoodooTestMod {
    private final Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
    public VoodooTestMod(IEventBus bus) {
        var suite = new VoodooGameTests();
        tests.put("all_items_and_recipes_load", suite::allItemsAndRecipesLoad);
        tests.put("binding_crafting_and_serialization", suite::bindingCraftingAndSerialization);
        tests.put("protection_families_cancel_damage", suite::protectionFamiliesCancelDamage);
        tests.put("shelf_persistence_and_ownership", suite::shelfPersistenceAndOwnership);
        tests.put("death_protection", suite::deathProtection);
        tests.put("status_and_hunger_protection", suite::statusAndHungerProtection);
        tests.put("voodoo_protection_destroys_attacking_poppet", suite::voodooProtectionDestroysAttackingPoppet);
        tests.put("partial_protection_and_reflection", suite::partialProtectionAndReflection);
        tests.put("needle_throwing_vampirism_and_dropped_fire", suite::needleThrowingVampirismAndDroppedFire);
        tests.put("projectile_and_void_protection", suite::projectileAndVoidProtection);
        tests.put("taglocks_and_dropped_water", suite::taglocksAndDroppedWater);
        bus.addListener(this::registerFunctions);
        bus.addListener(this::registerTests);
    }
    private void registerFunctions(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.TEST_FUNCTION))
            tests.forEach((name, test) -> Registry.register(BuiltInRegistries.TEST_FUNCTION, Voodoo.id(name), helper -> {
                try { test.accept(helper); }
                catch (Throwable failure) {
                    failure.printStackTrace();
                    throw new RuntimeException("Test " + name + " failed: " + failure, failure);
                }
            }));
    }
    private void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(Voodoo.id("tests"));
        tests.forEach((name, test) -> event.registerTest(Voodoo.id(name), new FunctionGameTestInstance(
            ResourceKey.create(Registries.TEST_FUNCTION, Voodoo.id(name)),
            new TestData<>(environment, Voodoo.id("empty"), 100, 0, true))));
    }
}
