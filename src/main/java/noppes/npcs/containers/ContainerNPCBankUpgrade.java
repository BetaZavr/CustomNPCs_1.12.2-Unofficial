package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerNPCBankUpgrade extends ContainerNPCBankInterface {
	public ContainerNPCBankUpgrade(EntityPlayer player, int slot, int bankid) {
		super(player, slot, bankid);
	}

	@Override
	public boolean canBeUpgraded() {
		return true;
	}

	@Override
	public int getRowNumber() {
		return 3;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public int xOffset() {
		return 54;
	}
}
