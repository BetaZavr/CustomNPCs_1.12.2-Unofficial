package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface IJobItemGiver {

    IItemStack[] getItemStacks();

    void setItemStacks(@ParamName("stacks") IItemStack[] stacks);

    String[] getLines();

    void setLines(String[] linesIn);

    int getCooldownType();

    void setCooldownType(int type);

    int getGivingType();

    void setGivingType(int type);
}
