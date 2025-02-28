package noppes.npcs.client.gui.util;

import net.minecraft.util.ResourceLocation;

public interface IResourceData {

    ResourceLocation getResource();

    int getWidth();

    int getHeight();

    float getTextureHeight();

    float getScaleX();

    float getScaleY();

    int getU();

    int getV();

}
