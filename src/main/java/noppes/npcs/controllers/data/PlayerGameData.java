package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;
import noppes.npcs.util.ValueUtil;

public class PlayerGameData {

	public static class FollowerSet {

		public UUID id;
		public int dimId;
		public EntityNPCInterface npc;

		public FollowerSet(EntityNPCInterface npcIn) {
			npc = npcIn;
			id = npcIn.getUniqueID();
			dimId = npcIn.world.provider.getDimension();
		}

		public FollowerSet(NBTTagCompound nbt) {
			id = UUID.fromString(nbt.getString("UUID"));
			dimId = nbt.getInteger("DimID");
		}

	}
	protected long money = 0L;
	protected long donat = 0L;

	public boolean updateClient; // ServerTickHandler.onPlayerTick() 122
	public boolean op = false; // ServerTickHandler.onPlayerTick() 62
	public final List<MarkupData> marketData = new ArrayList<>(); // ID market, slot

	public double[] logPos;
	private final List<FollowerSet> followers = new ArrayList<>();

	public double blockReachDistance = 5.0;
	public double renderDistance = 128.0;
	public int dimID = 0;

	public FollowerSet addFollower(EntityNPCInterface npc) {
		FollowerSet fs = new FollowerSet(npc);
		followers.add(fs);
		return fs;
	}

	public void addMarkupXP(int marketID, int xp) {
		if (xp == 0) { return; }
		MarkupData md = getMarkupData(marketID);
		md.addXP(xp);
		Marcet marcet = MarcetController.getInstance().getMarcet(marketID);
		if (marcet != null) {
			MarkupData d = marcet.markup.get(md.level);
			if (md.level < marcet.markup.size() - 1 && d != null && d.xp <= md.xp) {
				md.level++;
				md.xp = 0;
			}
		}
		updateClient = true;
	}

	public void addMoney(long moneyIn) {
		money = ValueUtil.correctLong(money + moneyIn, 0, Integer.MAX_VALUE);
        updateClient = true;
	}

	public FollowerSet getFollower(EntityNPCInterface npc) {
		for (FollowerSet fs : followers) {
			if (npc.equals(fs.npc) || fs.id.equals(npc.getUniqueID())) {
				return fs;
			}
		}
		return null;
	}

	public List<FollowerSet> getFollowers() {
		return followers;
	}

	public int getMarcetLevel(int marketID) {
		return getMarkupData(marketID).level;
	}

	public MarkupData getMarkupData(int marketID) {
		MarkupData md = null;
		for (MarkupData m : marketData) {
			if (m.id == marketID) {
				md = m;
				break;
			}
		}
		if (md == null) {
			md = new MarkupData(marketID, 0, 0);
			marketData.add(md);
		}
		return md;
	}

	public List<EntityNPCInterface> getMercenaries() {
		List<EntityNPCInterface> npcs = new ArrayList<>();
		for (FollowerSet fs : followers) {
			if (fs.npc != null && !fs.npc.isDead) {
				npcs.add(fs.npc);
			}
		}
		return npcs;
	}

	public long getMoney() { return money; }

	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setLong("Money", money);
		compound.setLong("Donat", donat);
		compound.setDouble("BlockReachDistance", blockReachDistance);
		compound.setDouble("RenderDistance", renderDistance);
		compound.setBoolean("IsOP", op);
		NBTTagList markup = new NBTTagList();
		for (MarkupData data : marketData) { markup.appendTag(data.getPlayerNBT()); }
		compound.setTag("MarketData", markup);
		if (logPos != null) {
			NBTTagList pos = new NBTTagList();
			for (double d : logPos) { pos.appendTag(new NBTTagDouble(d)); }
			compound.setTag("LoginPos", pos);
		}
		NBTTagList fls = new NBTTagList();
		for (FollowerSet fs : followers) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("UUID", fs.id.toString());
			nbt.setInteger("DimID", fs.dimId);
			fls.appendTag(nbt);
		}
		compound.setTag("Followers", fls);
		return compound;
	}

	public String getTextMoney() {
		return Util.instance.getTextReducedNumber(money, true, true, false);
	}

	public void readFromNBT(NBTTagCompound compound) {
		if (compound != null && compound.hasKey("GameData", 10)) {
			NBTTagCompound gameNBT = compound.getCompoundTag("GameData");
			money = gameNBT.getLong("Money");
			donat = gameNBT.getLong("Donat");
			if (compound.hasKey("BlockReachDistance", 6)) { blockReachDistance = compound.getDouble("BlockReachDistance"); }
			if (compound.hasKey("RenderDistance", 6)) { renderDistance = compound.getDouble("RenderDistance"); }
			op = gameNBT.getBoolean("IsOP");
			if (gameNBT.hasKey("MarketData", 9)) {
				marketData.clear();
				for (int i = 0; i < gameNBT.getTagList("MarketData", 10).tagCount(); i++) {
					NBTTagCompound nbt = gameNBT.getTagList("MarketData", 10).getCompoundTagAt(i);
					marketData.add(new MarkupData(nbt.getInteger("id"), nbt.getInteger("level"), nbt.getInteger("xp")));
				}
				logPos = null;
				if (gameNBT.hasKey("LoginPos", 9) && gameNBT.getTagList("LoginPos", 6).tagCount() > 3) {
					NBTTagList list = gameNBT.getTagList("LoginPos", 6);
					logPos = new double[] { list.getDoubleAt(0), list.getDoubleAt(1), list.getDoubleAt(2), list.getDoubleAt(3) };
				}
			}
			if (gameNBT.hasKey("Followers", 9)) {
				followers.clear();
				for (int i = 0; i < gameNBT.getTagList("Followers", 10).tagCount(); i++) {
					followers.add(new FollowerSet(gameNBT.getTagList("Followers", 10).getCompoundTagAt(i)));
				}
			}
		}
	}

	public void removeFollower(EntityNPCInterface npc) {
		for (FollowerSet fs : followers) {
			if (fs.id.equals(npc.getUniqueID())) {
				followers.remove(fs);
				return;
			}
		}
	}

	public void removeFollower(FollowerSet fs) {
		followers.remove(fs);
	}

	public NBTTagCompound saveNBTData(NBTTagCompound compound) {
		compound.setTag("GameData", getNBT());
		return compound;
	}

	public void setMoney(long moneyIn) {
		money = ValueUtil.correctLong(moneyIn, 0, Integer.MAX_VALUE);
		updateClient = true;
	}

	public long getDonat() { return donat; }

	@SuppressWarnings("all")
	public void addDonat(long moneyIn) {
		donat = ValueUtil.correctLong(donat + moneyIn, 0, Long.MAX_VALUE);
		updateClient = true;
	}

	public void setDonat(long moneyIn) {
		donat = ValueUtil.correctLong(moneyIn, 0, Integer.MAX_VALUE);
		updateClient = true;
	}

	@SuppressWarnings("all")
	public String getTextDonat() { return Util.instance.getTextReducedNumber(donat, true, true, false); }

}
