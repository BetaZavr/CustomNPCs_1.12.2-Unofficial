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

public class RoleTrader extends RoleInterface implements IRoleTrader {

	private int marcetID;

	public RoleTrader(EntityNPCInterface npc) {
		super(npc);
		marcetID = -1;
		type = RoleType.TRADER;
	}

	@Override
	public IMarcet getMarket() { return MarcetController.getInstance().getMarcet(marcetID); }

	@Override
	public int getMarketID() { return marcetID; }

	@Override
	public void interact(EntityPlayer player) {
		npc.say(player, npc.advanced.getInteractLine());
		Marcet marcet = (Marcet) getMarket();
		if (marcet == null || !marcet.isValid()) { return; }
		if (player instanceof EntityPlayerMP) {
			marcet.addListener(player, true);
			PlayerData.get(player).game.getMarcetLevel(marcetID);
		}
		NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerTrader, npc);
	}

	@Override
	public void load(NBTTagCompound compound) {
		super.load(compound);
		type = RoleType.TRADER;
		if (!compound.hasKey("MarketID", 3)) { marcetID = MarcetController.getInstance().loadOld(compound); }
		else { marcetID = compound.getInteger("MarketID"); }
	}

	@Override
	public void setMarket(IMarcet marcet) {
		IMarcet m = getMarket();
		if (m != null) { ((Marcet) m).closeForAllPlayers(); }
		marcetID = marcet.getId();
	}

	@Override
	public void setMarket(int id) {
		IMarcet m = getMarket();
		if (m != null) { ((Marcet) m).closeForAllPlayers(); }
		marcetID = id;
	}

	@Override
	public NBTTagCompound save(NBTTagCompound compound) {
		super.save(compound);
		compound.setInteger("MarketID", marcetID);
		return compound;
	}

}
