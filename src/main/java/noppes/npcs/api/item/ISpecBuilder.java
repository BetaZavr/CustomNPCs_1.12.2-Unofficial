package noppes.npcs.api.item;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.api.ParamName;
import noppes.npcs.constants.EnumGuiType;

public interface ISpecBuilder {
	
	void leftClick(@ParamName("stack") ItemStack stack, @ParamName("player") EntityPlayerMP player, @ParamName("pos") BlockPos pos);
	
	void rightClick(@ParamName("stack") ItemStack stack, @ParamName("player") EntityPlayerMP player, @ParamName("pos") BlockPos pos);

	int getType();
	
	EnumGuiType getGUIType();
	
}
