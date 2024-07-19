package noppes.npcs.roles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.entity.data.role.IRoleBank;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.BankData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

public class RoleBank extends RoleInterface implements IRoleBank {

	public int bankId;

	public RoleBank(EntityNPCInterface npc) {
		super(npc);
		this.bankId = -1;
		this.type = RoleType.BANK;
	}

	public Bank getBank() {
		Bank bank = BankController.getInstance().banks.get(this.bankId);
		if (bank != null) {
			return bank;
		}
		return BankController.getInstance().banks.values().iterator().next();
	}

	@Override
	public void interact(EntityPlayer player) {
		BankData data = PlayerData.get(player).bankData.get(this.bankId);
		if (data == null) {
			return;
		}
		data.openBankGui(player, this.npc, 0);
		this.npc.say(player, this.npc.advanced.getInteractLine());
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = RoleType.BANK;
		this.bankId = compound.getInteger("RoleBankID");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", RoleType.BANK.get());
		compound.setInteger("RoleBankID", this.bankId);
		return compound;
	}
}
