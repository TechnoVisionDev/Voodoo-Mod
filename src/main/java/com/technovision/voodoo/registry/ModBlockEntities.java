package com.technovision.voodoo.registry;

import com.technovision.voodoo.Voodoo;
import com.technovision.voodoo.entities.PoppetShelfBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Creates and registers block entities.
 *
 * @author TechnoVision
 */
public class ModBlockEntities {

    public static final BlockEntityType<PoppetShelfBlockEntity> POPPET_SHELF_ENTITY = FabricBlockEntityTypeBuilder.create(PoppetShelfBlockEntity::new, ModBlocks.POPPET_SHELF_BLOCK).build(null);

    public static void registerBlockEntities() {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(Voodoo.MOD_ID, "poppet_shelf_block_entity"), POPPET_SHELF_ENTITY);
    }
}
