package noppes.npcs.reflection.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

public class BlockReflection {

    private static Field blockState;

    @SuppressWarnings("all")
    public static void setBlockState(Block block, BlockStateContainer newBlockState) {
        if (block == null || newBlockState == null) { return; }
        if (blockState == null) {
            Exception error = null;
            try { blockState = Block.class.getDeclaredField("field_176227_L"); } catch (Exception e) { error = e; }
            if (blockState == null) {
                try {
                    blockState = Block.class.getDeclaredField("blockState");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"blockState\"", error);
            }
        }
        try {
            blockState.setAccessible(true);
            blockState.set(block, newBlockState);
        } catch (Exception e) {
            LogWriter.error("Error set \"closedSet\":\"" + newBlockState + "\" in " + block, e);
        }
    }
}
