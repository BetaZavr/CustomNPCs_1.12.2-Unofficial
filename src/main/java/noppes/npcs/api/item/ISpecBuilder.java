package noppes.npcs.api.item;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.constants.EnumGuiType;

public interface ISpecBuilder {
	
	void leftClick(ItemStack stack, EntityPlayerMP player, BlockPos pos);
	
	void rightClick(ItemStack stack, EntityPlayerMP player, BlockPos pos);

	int getType();
	
	EnumGuiType getGUIType();
	
}
