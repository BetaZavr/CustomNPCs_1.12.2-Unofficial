package noppes.npcs.controllers.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.handler.data.IAvailabilityStack;
import noppes.npcs.api.item.IItemStack;

import java.util.Objects;

public class AvailabilityStack implements IAvailabilityStack {

    private int id;
    private boolean ignoreNBT = false;
    private boolean ignoreDamage = false;
    private ItemStack stack = ItemStack.EMPTY;

    public AvailabilityStack (int id) {
        this.id = id;
    }

    public AvailabilityStack load(NBTTagCompound compound) {
        id = compound.getInteger("id");
        ignoreNBT = compound.getBoolean("ignoreNBT");
        ignoreDamage = compound.getBoolean("ignoreDamage");
        stack = new ItemStack(compound.getCompoundTag("item"));
        return this;
    }

    @Override
    public int getId() { return id; }

    public AvailabilityStack setId(int id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean getIgnoreNBT() { return ignoreNBT; }

    @Override
    public boolean getIgnoreDamage() { return ignoreDamage; }

    public void setStack(ItemStack item) { stack = item; }

    @Override
    public void setStack(IItemStack item) { stack = item.getMCItemStack(); }

    @Override
    public void setIgnoreNBT(boolean bo) { ignoreNBT = bo; }

    @Override
    public void setIgnoreDamage(boolean bo) { ignoreDamage = bo; }

    public ItemStack getStack() { return stack; }

    @Override
    public IItemStack getIStack() { return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(stack); }

    public NBTBase getNBT(int i) {
        id = i;
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("id", i);
        compound.setBoolean("ignoreNBT", ignoreNBT);
        compound.setBoolean("ignoreDamage", ignoreDamage);
        compound.setTag("item", stack.writeToNBT(new NBTTagCompound()));
        return compound;
    }

}
