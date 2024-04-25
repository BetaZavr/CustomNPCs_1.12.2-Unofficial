package moe.plushie.armourers_workshop.api.common;

import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDescriptor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface ISkinNBTUtils {

	public ISkinDescriptor getSkinDescriptor(ItemStack itemStack);

	public ISkinDescriptor getSkinDescriptor(NBTTagCompound compound);

	public boolean hasSkinDescriptor(ItemStack itemStack);

	public boolean hasSkinDescriptor(NBTTagCompound compound);

	public void removeSkinDescriptor(ItemStack itemStack);

	public void removeSkinDescriptor(NBTTagCompound compound);

	// Item stack.
	public void setSkinDescriptor(ItemStack itemStack, ISkinDescriptor skinDescriptor);

	// Tag compound.
	public void setSkinDescriptor(NBTTagCompound compound, ISkinDescriptor skinDescriptor);
}
