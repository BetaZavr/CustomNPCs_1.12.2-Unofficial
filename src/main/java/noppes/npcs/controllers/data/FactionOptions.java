package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.controllers.FactionController;

public class FactionOptions {

	public final List<FactionOption> fps = new ArrayList<>();

	public void addPoints(EntityPlayer player) {
        PlayerData playerdata = PlayerData.get(player);
		PlayerFactionData data = playerdata.factionData;
		for (FactionOption fo : this.fps) {
			if (fo.factionId < 0 || fo.factionPoints == 0) {
				continue;
			}
			int value = fo.factionPoints;
			boolean take = fo.decreaseFactionPoints;
			if (value < 0) {
				value *= -1;
                take = !take;
			}
			this.addPoints(player, data, fo.factionId, take, value);
		}
    }

	private void addPoints(EntityPlayer player, PlayerFactionData data, int factionId, boolean decrease, int points) {
		Faction faction = FactionController.instance.getFaction(factionId);
		if (faction == null) {
			return;
		}
		if (!faction.hideFaction) {
			String message = decrease ? "faction.decreasepoints" : "faction.increasepoints";
			player.sendMessage(new TextComponentTranslation(message, faction.name, points));
		}
		data.increasePoints(player, factionId, decrease ? (-points) : points);
	}

	public FactionOptions copy() {
		FactionOptions fp = new FactionOptions();
		fp.readFromNBT(this.writeToNBT(new NBTTagCompound()));
		return fp;
	}

	public FactionOption get(int factionID) {
		for (FactionOption fo : this.fps) {
			if (fo.factionId == factionID) {
				return fo;
			}
		}
		return null;
	}

	public boolean hasFaction(int id) {
		for (FactionOption fo : this.fps) {
			if (fo.factionId == id) {
				return true;
			}
		}
		return false;
	}

	public boolean hasOptions() {
		for (FactionOption fo : this.fps) {
			if (fo.factionId > 0 && fo.factionPoints != 0) {
				return true;
			}
		}
		return false;
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.fps.clear();
		if (!compound.hasKey("FactionOptions", 9)) { // OLD
			if (compound.getInteger("OptionFactions1") > 0) {
				this.fps.add(new FactionOption(compound.getInteger("OptionFactions1"),
						compound.getInteger("OptionFaction1Points"), compound.getBoolean("DecreaseFaction1Points")));
			}
			if (compound.getInteger("OptionFactions2") > 0) {
				this.fps.add(new FactionOption(compound.getInteger("OptionFactions2"),
						compound.getInteger("OptionFaction2Points"), compound.getBoolean("DecreaseFaction2Points")));
			}
		} else {
			for (int i = 0; i < compound.getTagList("FactionOptions", 10).tagCount(); i++) {
				this.fps.add(new FactionOption(compound.getTagList("FactionOptions", 10).getCompoundTagAt(i)));
			}
		}
	}

	public boolean remove(int factionID) {
		for (FactionOption fo : this.fps) {
			if (fo.factionId == factionID) {
				this.fps.remove(fo);
				return true;
			}
		}
		return false;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for (FactionOption fo : this.fps) {
			list.appendTag(fo.writeToNBT());
		}
		compound.setTag("FactionOptions", list);
		return compound;
	}

}
