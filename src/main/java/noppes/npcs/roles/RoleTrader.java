package noppes.npcs.roles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.entity.data.role.IRoleTrader;
import noppes.npcs.api.handler.data.IMarcet;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

public class RoleTrader
extends RoleInterface
implements IRoleTrader {
	
	private int marcetID;

	public RoleTrader(EntityNPCInterface npc) {
		super(npc);
		this.marcetID = -1;
		this.type = RoleType.TRADER;
	}

	@Override
	public void interact(EntityPlayer player) {
		this.npc.say(player, this.npc.advanced.getInteractLine());
		Marcet marcet = (Marcet) getMarket();
		if (marcet == null || !marcet.isValid()) { return; }
		if (player instanceof EntityPlayerMP) {
			marcet.addListener(player, true);
			PlayerData.get(player).game.getMarcetLevel(this.marcetID);
		}
		NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerTrader, this.npc);
	}
		
	@Override
	public int getMarketID() { return this.marcetID; }
		
	@Override
	public void setMarket(int id) {
		IMarcet m = this.getMarket();
		if (m != null) { ((Marcet) m).closeForAllPlayers(); }
		this.marcetID = id;
	}
	
	@Override
	public void setMarket(IMarcet marcet) {
		IMarcet m = this.getMarket();
		if (m != null) { ((Marcet) m).closeForAllPlayers(); }
		this.marcetID = marcet.getId();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = RoleType.TRADER;
		if (!compound.hasKey("MarketID", 3)) { this.marcetID = MarcetController.getInstance().loadOld(compound); } // Old
		else { this.marcetID = compound.getInteger("MarketID"); } // New
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", RoleType.TRADER.get());
		compound.setInteger("MarketID", this.marcetID);
		return compound;
	}

	@Override
	public IMarcet getMarket() { return MarcetController.getInstance().getMarcet(this.marcetID); }
	
}
