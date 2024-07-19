package noppes.npcs.roles.companion;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumGuiType;

public class CompanionTrader extends CompanionJobInterface {
	@Override
	public NBTTagCompound getNBT() {
		return new NBTTagCompound();
	}

	public void interact(EntityPlayer player) {
		NoppesUtilServer.sendOpenGui(player, EnumGuiType.CompanionTrader, this.npc);
	}

	@Override
	public void setNBT(NBTTagCompound compound) {
	}
}
