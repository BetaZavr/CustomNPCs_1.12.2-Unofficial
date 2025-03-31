package noppes.npcs.client.gui.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface IGuiNpcButton extends IComponentGui {

    void resetDisplay(List<String> list);

    void setDisplay(int value);

    void setDisplayText(String text);

    void setTexture(ResourceLocation location);

    boolean isHovered();

    void setHasDefaultBack(boolean bo);

    void setIsAnim(boolean bo);

    void setTextColor(int color);

    void setStacks(ItemStack... stacks);

    ItemStack[] getStacks();

    void setCurrentStackPos(int pos);

    int getValue();

    String[] getVariants();

    int[] getTextureXY();

    void setTextureXY(int x, int y);

    int[] getTextureUV();

    void setTextureUV(int u, int v);

    void setLayerColor(int color);

    String getDisplayString();

    void setActive(boolean bo);

    void setHasSound(boolean bo);

    boolean hasSound();

    int getCurrentStackID();

    ItemStack getCurrentStack();

}

