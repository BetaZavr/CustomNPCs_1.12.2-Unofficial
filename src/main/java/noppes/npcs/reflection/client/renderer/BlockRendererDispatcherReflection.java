package noppes.npcs.reflection.client.renderer;

import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.ChestRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class BlockRendererDispatcherReflection {

    private static Field blockModelRenderer;
    private static Field chestRenderer;

    public static BlockModelRenderer getBlockModelRenderer(BlockRendererDispatcher dispatcher) {
        if (dispatcher == null) { return null; }
        if (blockModelRenderer == null) {
            Exception error = null;
            try { blockModelRenderer = BlockRendererDispatcher.class.getDeclaredField("field_175027_c"); } catch (Exception e) { error = e; }
            if (blockModelRenderer == null) {
                try {
                    blockModelRenderer = BlockRendererDispatcher.class.getDeclaredField("blockModelRenderer");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"blockModelRenderer\"", error);
                return null;
            }
        }
        try {
            blockModelRenderer.setAccessible(true);
            return (BlockModelRenderer) blockModelRenderer.get(dispatcher);
        } catch (Exception e) {
            LogWriter.error("Error get \"blockModelRenderer\" in " + dispatcher, e);
        }
        return null;
    }

    public static ChestRenderer getChestRenderer(BlockRendererDispatcher dispatcher) {
        if (dispatcher == null) { return null; }
        if (chestRenderer == null) {
            Exception error = null;
            try { chestRenderer = BlockRendererDispatcher.class.getDeclaredField("field_175024_d"); } catch (Exception e) { error = e; }
            if (chestRenderer == null) {
                try {
                    chestRenderer = BlockRendererDispatcher.class.getDeclaredField("chestRenderer");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"chestRenderer\"", error);
                return null;
            }
        }
        try {
            chestRenderer.setAccessible(true);
            return (ChestRenderer) chestRenderer.get(dispatcher);
        } catch (Exception e) {
            LogWriter.error("Error get \"chestRenderer\" in " + dispatcher, e);
        }
        return null;
    }

}
