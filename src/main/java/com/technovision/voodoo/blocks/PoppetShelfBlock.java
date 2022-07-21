package com.technovision.voodoo.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class PoppetShelfBlock extends Block {

    protected static final VoxelShape voxelShape = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public PoppetShelfBlock() {
        super(FabricBlockSettings.of(Material.STONE, Blocks.NETHER_BRICKS.getDefaultMapColor())
                .strength(6, 6)
                .requiresTool()
                .sounds(BlockSoundGroup.NETHER_BRICKS)
                .nonOpaque()
        );
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return voxelShape;
    }
}
