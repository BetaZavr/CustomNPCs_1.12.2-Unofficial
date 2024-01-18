package noppes.npcs.controllers.data;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.handler.data.IMarcet;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.util.AdditionalMethods;

public class PlayerGameData {
	
	private long money;
	public boolean updateClient; // ServerTickHandler.onPlayerTick()
	public boolean op = false; // ServerTickHandler.onPlayerTick()
	public final List<MarkupData> marketData = Lists.<MarkupData>newArrayList(); // ID market, slot
	public double[] logPos;

	public long getMoney() { return this.money; }
	
	public void addMoney(long money) {
		this.money += money;
		if (this.money < 0L) {
			this.money = 0L;
		} else if (this.money > Long.MAX_VALUE) {
			this.money = Long.MAX_VALUE;
		}
		this.updateClient = true;
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setLong("Money", this.money);
		compound.setBoolean("IsOP", this.op);
		NBTTagList markup = new NBTTagList();
		for (MarkupData data : this.marketData) { markup.appendTag(data.getPlayerNBT()); }
		compound.setTag("MarketData", markup);
		if (this.logPos != null) {
			NBTTagList pos = new NBTTagList();
			for (double d : this.logPos) { pos.appendTag(new NBTTagDouble(d)); }
			compound.setTag("LoginPos", pos);
		}
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
					this.marketData.add(new MarkupData(nbt.getInteger("id"), nbt.getInteger("slot"), nbt.getInteger("xp")));
				}
			}
			this.logPos = null;
			if (gameNBT.hasKey("LoginPos", 9) && gameNBT.getTagList("LoginPos", 6).tagCount() > 3) {
				NBTTagList list = gameNBT.getTagList("LoginPos", 6);
				this.logPos = new double[] { list.getDoubleAt(0), list.getDoubleAt(1), list.getDoubleAt(2), list.getDoubleAt(3) };
			}
		}
	}

	public NBTTagCompound saveNBTData(NBTTagCompound compound) {
		compound.setTag("GameData", this.getNBT());
		return compound;
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
	
	public int getMarcetLevel(int marketID) {
		return this.getMarkupData(marketID).level;
	}

	public void setMarkupLevel(int marketID, int level) {
		this.getMarkupData(marketID).level = level;
	}

	public void addMarkupXP(int marketID, int xp) {
		if (xp==0) { return; }
		MarkupData md = this.getMarkupData(marketID);
		md.addXP(xp);
		IMarcet m = MarcetController.getInstance().getMarcet(marketID);
		if (m!=null) {
			MarkupData d = ((Marcet) m).markup.get(md.level);
			if (md.level < ((Marcet) m).markup.size() - 1 && d!=null && d.xp <= md.xp) {
				md.level++;
				md.xp = 0;
			}
		}
		this.updateClient = true;
	}
	
}
