package com.technovision.voodoo.registry;

import com.technovision.voodoo.Voodoo;
import com.technovision.voodoo.blocks.PoppetShelfBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlocks {

    public static final PoppetShelfBlock POPPET_SHELF_BLOCK = new PoppetShelfBlock();

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, new Identifier(Voodoo.MOD_ID, "poppet_shelf"), POPPET_SHELF_BLOCK);
    }
}
