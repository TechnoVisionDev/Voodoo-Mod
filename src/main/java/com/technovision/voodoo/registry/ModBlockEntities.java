package com.technovision.voodoo.registry;
import com.technovision.voodoo.Voodoo;
import com.technovision.voodoo.blocks.entities.PoppetShelfBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
public class ModBlockEntities {
    public static final BlockEntityType<PoppetShelfBlockEntity> POPPET_SHELF_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Voodoo.id("poppet_shelf_block_entity"), FabricBlockEntityTypeBuilder.create(PoppetShelfBlockEntity::new, ModBlocks.POPPET_SHELF_BLOCK).build());
    public static void registerBlockEntities() {}
}
