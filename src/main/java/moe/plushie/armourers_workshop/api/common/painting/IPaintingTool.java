package moe.plushie.armourers_workshop.api.common.painting;

import net.minecraft.item.ItemStack;

public interface IPaintingTool {

	public int getToolColour(ItemStack stack);

	@Deprecated
	public boolean getToolHasColour(ItemStack stack);

	public IPaintType getToolPaintType(ItemStack stack);

	public void setToolColour(ItemStack stack, int colour);

	public void setToolPaintType(ItemStack stack, IPaintType paintType);
}
