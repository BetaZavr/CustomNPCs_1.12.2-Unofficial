package noppes.npcs.entity.data;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.entity.data.IEnchantSet;
import noppes.npcs.util.ValueUtil;

public class EnchantSet implements IEnchantSet {

	public double chance;
	public Enchantment ench;
	public int[] lvls;
	public DropSet parent;

	public EnchantSet(DropSet p) {
		this.parent = p;
		this.lvls = new int[] { 0, 1 };
		this.ench = Enchantment.getEnchantmentByID(0);
		this.chance = 100.0d;
	}

	@Override
	public double getChance() {
		return Math.round(this.chance * 10000.0d) / 10000.0d;
	}

	@Override
	public String getEnchant() {
		return this.ench.getName();
	}

	public String getKey() {
		String lvl = " [" + this.lvls[0] + "]";
		if (this.lvls[0] != this.lvls[1]) {
			lvl = " [" + this.lvls[0] + "<->" + this.lvls[1] + "]";
		}
		String keyName = new TextComponentTranslation(this.ench.getName(), new Object[0]).getFormattedText() + lvl
				+ " (" + this.getChance() + ")";
		keyName += " #" + this.toString().substring(this.toString().indexOf("@") + 1);
		return keyName;
	}

	@Override
	public int getMaxLevel() {
		return this.lvls[1];
	}

	@Override
	public int getMinLevel() {
		return this.lvls[0];
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound nbtES = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		list.appendTag(new NBTTagInt(this.lvls[0]));
		list.appendTag(new NBTTagInt(this.lvls[1]));
		nbtES.setTag("Levels", list);
		nbtES.setInteger("ID", Enchantment.getEnchantmentID(this.ench));
		nbtES.setDouble("Chance", this.chance);
		return nbtES;
	}

	public void load(NBTTagCompound nbtES) {
		int[] newLv = new int[2];
		for (int i = 0; i < 2; i++) {
			newLv[i] = nbtES.getTagList("Levels", 3).getIntAt(i);
		}
		this.lvls = newLv;
		this.ench = Enchantment.getEnchantmentByID(nbtES.getInteger("ID"));
		this.chance = nbtES.getDouble("Chance");
	}

	@Override
	public void remove() {
		this.parent.removeEnchant((EnchantSet) this);
	}

	@Override
	public void setChance(double chance) {
		double newChance = ValueUtil.correctDouble(chance, 0.0001d, 100.0d);
		this.chance = Math.round(newChance * 10000.0d) / 10000.0d;
	}

	@Override
	public void setEnchant(Enchantment enchant) {
		if (enchant == null) {
			this.parent.removeEnchant((EnchantSet) this);
			return;
		}
		this.ench = enchant;
	}

	@Override
	public boolean setEnchant(int id) {
		Enchantment newEnch = Enchantment.getEnchantmentByID(id);
		if (newEnch != null) {
			this.ench = newEnch;
			return true;
		}
		return false;
	}

	@Override
	public boolean setEnchant(String name) {
		Enchantment newEnch = Enchantment.getEnchantmentByLocation(name);
		if (newEnch != null) {
			this.ench = newEnch;
			return true;
		}
		return false;
	}

	@Override
	public void setLevels(int min, int max) {
		int newMin = min;
		int newMax = max;
		if (min > max) {
			newMin = max;
			newMax = min;
		}
		this.lvls = new int[] { newMin, newMax };
	}
}
