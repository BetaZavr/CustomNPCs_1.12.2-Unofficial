package noppes.npcs.controllers.data;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.handler.data.IMarcet;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

public class PlayerGameData {

	public class FollowerSet {

		public UUID id;
		public int dimId;
		public EntityNPCInterface npc;

		public FollowerSet(EntityNPCInterface npc) {
			id = npc.getUniqueID();
			dimId = npc.world.provider.getDimension();
			this.npc = npc;
		}

		public FollowerSet(NBTTagCompound nbt) {
			id = UUID.fromString(nbt.getString("UUID"));
			dimId = nbt.getInteger("DimID");
		}

	}
	private long money;
	public boolean updateClient; // ServerTickHandler.onPlayerTick() 122
	public boolean op = false; // ServerTickHandler.onPlayerTick() 62
	public final List<MarkupData> marketData = Lists.<MarkupData>newArrayList(); // ID market, slot

	public double[] logPos;
	private final List<FollowerSet> followers = Lists.<FollowerSet>newArrayList();

	public int dimID = 0;

	public FollowerSet addFollower(EntityNPCInterface npc) {
		FollowerSet fs = new FollowerSet(npc);
		this.followers.add(fs);
		return fs;
	}

	public void addMarkupXP(int marketID, int xp) {
		if (xp == 0) {
			return;
		}
		MarkupData md = this.getMarkupData(marketID);
		md.addXP(xp);
		IMarcet m = MarcetController.getInstance().getMarcet(marketID);
		if (m != null) {
			MarkupData d = ((Marcet) m).markup.get(md.level);
			if (md.level < ((Marcet) m).markup.size() - 1 && d != null && d.xp <= md.xp) {
				md.level++;
				md.xp = 0;
			}
		}
		this.updateClient = true;
	}

	public void addMoney(long money) {
		this.money += money;
		if (this.money < 0L) {
			this.money = 0L;
		} else if (this.money > Long.MAX_VALUE) {
			this.money = Long.MAX_VALUE;
		}
		this.updateClient = true;
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
		return this.getMarkupData(marketID).level;
	}

	public MarkupData getMarkupData(int marketID) {
		MarkupData md = null;
		for (MarkupData m : this.marketData) {
			if (m.id == marketID) {
				md = m;
				break;
			}
		}
		if (md == null) {
			md = new MarkupData(marketID, 0, 0);
			this.marketData.add(md);
		}
		return md;
	}

	public List<EntityNPCInterface> getMercenaries() {
		List<EntityNPCInterface> npcs = Lists.<EntityNPCInterface>newArrayList();
		for (FollowerSet fs : followers) {
			if (fs.npc != null && !fs.npc.isDead) {
				npcs.add(fs.npc);
			}
		}
		return npcs;
	}

	public long getMoney() {
		return this.money;
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setLong("Money", this.money);
		compound.setBoolean("IsOP", this.op);
		NBTTagList markup = new NBTTagList();
		for (MarkupData data : this.marketData) {
			markup.appendTag(data.getPlayerNBT());
		}
		compound.setTag("MarketData", markup);
		if (this.logPos != null) {
			NBTTagList pos = new NBTTagList();
			for (double d : this.logPos) {
				pos.appendTag(new NBTTagDouble(d));
			}
			compound.setTag("LoginPos", pos);
		}

		NBTTagList fls = new NBTTagList();
		for (FollowerSet fs : followers) {
			NBTTagCompound fnbt = new NBTTagCompound();
			fnbt.setString("UUID", fs.id.toString());
			fnbt.setInteger("DimID", fs.dimId);
			fls.appendTag(fnbt);
		}
		compound.setTag("Followers", fls);

		return compound;
	}

	public String getTextMoney() {
		return AdditionalMethods.getTextReducedNumber(this.money, true, true, false);
	}

	public void readFromNBT(NBTTagCompound compound) {
		if (compound != null && compound.hasKey("GameData", 10)) {
			NBTTagCompound gameNBT = compound.getCompoundTag("GameData");
			this.money = gameNBT.getLong("Money");
			this.op = gameNBT.getBoolean("IsOP");
			if (gameNBT.hasKey("MarketData", 9)) {
				this.marketData.clear();
				for (int i = 0; i < gameNBT.getTagList("MarketData", 10).tagCount(); i++) {
					NBTTagCompound nbt = gameNBT.getTagList("MarketData", 10).getCompoundTagAt(i);
					this.marketData
							.add(new MarkupData(nbt.getInteger("id"), nbt.getInteger("level"), nbt.getInteger("xp")));
				}
				this.logPos = null;
				if (gameNBT.hasKey("LoginPos", 9) && gameNBT.getTagList("LoginPos", 6).tagCount() > 3) {
					NBTTagList list = gameNBT.getTagList("LoginPos", 6);
					this.logPos = new double[] { list.getDoubleAt(0), list.getDoubleAt(1), list.getDoubleAt(2),
							list.getDoubleAt(3) };
				}
			}
			if (gameNBT.hasKey("Followers", 9)) {
				this.followers.clear();
				for (int i = 0; i < gameNBT.getTagList("Followers", 10).tagCount(); i++) {
					this.followers.add(new FollowerSet(gameNBT.getTagList("Followers", 10).getCompoundTagAt(i)));
				}
			}
		}
	}

	public boolean removeFollower(EntityNPCInterface npc) {
		for (FollowerSet fs : followers) {
			if (fs.id.equals(npc.getUniqueID())) {
				followers.remove(fs);
				return true;
			}
		}
		return false;
	}

	public void removeFollower(FollowerSet fs) {
		followers.remove(fs);
	}

	public NBTTagCompound saveNBTData(NBTTagCompound compound) {
		compound.setTag("GameData", this.getNBT());
		return compound;
	}

	public void setMarkupLevel(int marketID, int level) {
		this.getMarkupData(marketID).level = level;
	}

	public void setMoney(long money) {
		if (money < 0L) {
			money = 0L;
		} else if (money > Long.MAX_VALUE) {
			money = Long.MAX_VALUE;
		}
		this.money = money;
		this.updateClient = true;
	}

}
