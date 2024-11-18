package noppes.npcs.api.handler.data;

import noppes.npcs.api.item.IItemStack;

public interface IAvailabilityStack {

    int getId();

    boolean getIgnoreNBT();

    boolean getIgnoreDamage();

    void setStack(IItemStack item);

    void setIgnoreNBT(boolean bo);

    void setIgnoreDamage(boolean bo);

    IItemStack getIStack();

}
