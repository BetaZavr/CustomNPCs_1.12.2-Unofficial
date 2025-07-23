package noppes.npcs.client.model.part;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.constants.EnumParts;

import javax.annotation.Nonnull;

public class LayerModel {

    private ResourceLocation obj;
    public final float[] offset = new float[] { 0.0f, 0.0f, 0.0f };
    public final float[] rotation = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f };
    public final float[] scale = new float[] { 1.0f, 1.0f, 1.0f };
    public int slotID;
    private @Nonnull ItemStack stack = ItemStack.EMPTY;
    public EnumParts part; // temp, to in gui

    public LayerModel() { }

    public LayerModel(NBTTagCompound compound) { load(compound); }

    public void load(NBTTagCompound compound) {
        obj = null;
        if (compound.hasKey("OBJ", 8)) { obj = new ResourceLocation(compound.getString("OBJ")); }
        slotID = compound.getInteger("slotID");
        for (int i = 0; i < 3; i++) {
            offset[i] = compound.getTagList("Offset", 5).getFloatAt(i);
            rotation[i] = compound.getTagList("Rotation", 5).getFloatAt(i);
            scale[i] = compound.getTagList("Scale", 5).getFloatAt(i);
            if (scale[i] < 0) { scale[i] *= -1.0f; }
        }
        stack = ItemStack.EMPTY;
        if (compound.hasKey("Item", 10)) { stack = new ItemStack(compound.getCompoundTag("Item")); }
    }

    public NBTTagCompound save() {
        NBTTagCompound compound = new NBTTagCompound();
        if (obj != null) { compound.setString("OBJ", obj.toString()); }
        compound.setInteger("slotID", slotID);
        NBTTagList listRot = new NBTTagList();
        NBTTagList listOff = new NBTTagList();
        NBTTagList listSc = new NBTTagList();
        for (int i = 0; i < 3; i++) {
            listRot.appendTag(new NBTTagFloat(rotation[i]));
            listOff.appendTag(new NBTTagFloat(offset[i]));
            listSc.appendTag(new NBTTagFloat(scale[i]));
        }
        compound.setTag("Rotation", listRot);
        compound.setTag("Offset", listOff);
        compound.setTag("Scale", listSc);
        compound.setTag("Item", stack.writeToNBT(new NBTTagCompound()));
        return compound;
    }

    public ResourceLocation getOBJ() { return obj; }

    public void setOBJ(String newOBJ) {
        if (newOBJ == null) { obj = null; }
        else { obj = new ResourceLocation(newOBJ); }
    }

    public @Nonnull ItemStack getStack() { return stack; }

    public void setStack(ItemStack newStack) {
        if (newStack == null) { newStack = ItemStack.EMPTY; }
        stack = newStack;
    }

}
