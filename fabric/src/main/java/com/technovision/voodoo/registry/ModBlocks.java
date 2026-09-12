package com.technovision.voodoo.registry;
import com.technovision.voodoo.Voodoo;
import com.technovision.voodoo.blocks.PoppetShelfBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
public class ModBlocks {
    public static final PoppetShelfBlock POPPET_SHELF_BLOCK = Registry.register(BuiltInRegistries.BLOCK, Voodoo.id("poppet_shelf"),
        new PoppetShelfBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_BRICKS).strength(6, 6).noOcclusion().setId(ResourceKey.create(Registries.BLOCK, Voodoo.id("poppet_shelf")))));
    public static void registerBlocks() {}
}
