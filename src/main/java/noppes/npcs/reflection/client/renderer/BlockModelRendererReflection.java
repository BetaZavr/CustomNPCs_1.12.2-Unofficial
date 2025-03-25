package noppes.npcs.reflection.client.renderer;

import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SideOnly(Side.CLIENT)
public class BlockModelRendererReflection {

    private static Field blockColors;

    public static BlockColors getBlockColors(BlockModelRenderer blockModelRenderer) {
        if (blockModelRenderer == null) { return null; }
        if (blockColors == null) {
            Exception error = null;
            try { blockColors = BlockModelRenderer.class.getDeclaredField("field_187499_a"); } catch (Exception e) { error = e; }
            if (blockColors == null) {
                try {
                    blockColors = BlockModelRenderer.class.getDeclaredField("blockColors");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"blockColors\"", error);
                return null;
            }
        }
        try {
            blockColors.setAccessible(true);

            if (Modifier.isFinal(blockColors.getModifiers())) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(blockColors, blockColors.getModifiers() & ~Modifier.FINAL);
            }

            return (BlockColors) blockColors.get(blockModelRenderer);
        } catch (Exception e) {
            LogWriter.error("Error get \"blockColors\" in " + blockModelRenderer, e);
        }
        return null;
    }

}
