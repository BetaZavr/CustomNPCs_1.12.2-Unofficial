package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerNPCBankLarge
extends ContainerNPCBankInterface {
	
	public ContainerNPCBankLarge(EntityPlayer player, int slot, int bankid) {
		super(player, slot, bankid);
	}

	@Override
	public int getRowNumber() {
		return 6;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public boolean isUpgraded() {
		return true;
	}
}
