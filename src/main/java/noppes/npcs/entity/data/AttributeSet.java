package noppes.npcs.entity.data;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.data.IAttributeSet;
import noppes.npcs.util.ValueUtil;

public class AttributeSet implements IAttributeSet {

	public IAttribute attr;
	public double chance;
	public DropSet parent;
	public int slot;
	public double[] values;

	public AttributeSet(DropSet p) {
		this.parent = p;
		this.values = new double[] { 0.0d, 0.05d };
		this.attr = SharedMonsterAttributes.MAX_HEALTH;
		this.chance = 100.0d;
		this.slot = 0;
	}

	@Override
	public String getAttribute() {
		return this.attr.getName();
	}

	@Override
	public double getChance() {
		return Math.round(this.chance * 10000.0d) / 10000.0d;
	}

	public String getKey() {
		String keyName = "";
		char c = ((char) 167);
		double ch = Math.round(this.chance*10.0d) / 10.d;
		String chance = String.valueOf(ch).replace(".", ",");
		if (ch == (int) ch) { chance = String.valueOf((int) ch); }
		chance += "%";
		keyName += c + "e" + chance;
		double v0 = Math.round(this.values[0]*1000.0d) / 1000.d;
		String tv0 = String.valueOf(v0).replace(".", ",");
		if (v0 == (int) v0) { tv0 = String.valueOf((int) v0); }
		double v1 = Math.round(this.values[1]*1000.0d) / 1000.d;
		String tv1 = String.valueOf(v1).replace(".", ",");
		if (v1 == (int) v1) { tv1 = String.valueOf((int) v1); }
		if (this.values[0] == this.values[1]) {
			keyName += c + "7[" + c + "6" + tv0 + c + "7] ";
		} else {
			keyName += c + "7[" + c + "6" + tv0 + c + "7-" + c + "6" + tv1 + c + "7] ";
		}
		String name = new TextComponentTranslation("attribute.name." + this.attr.getName(), new Object[0]).getFormattedText();
		if (name.equals("attribute.name." + this.attr.getName()) || name.equals("attribute.name.")) { name = this.attr.getName(); }
		keyName += c + "r" + name;
		keyName += c + "8 #" + this.toString().substring(this.toString().indexOf("@") + 1);
		return keyName;
	}

	@Override
	public double getMaxValue() {
		return this.values[1];
	}

	@Override
	public double getMinValue() {
		return this.values[0];
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound nbtAS = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		list.appendTag(new NBTTagDouble(this.values[0]));
		list.appendTag(new NBTTagDouble(this.values[1]));
		nbtAS.setTag("Values", list);
		nbtAS.setString("Name", this.attr.getName());
		nbtAS.setDouble("Chance", this.chance);
		nbtAS.setInteger("Slot", this.slot);
		return nbtAS;
	}

	@Override
	public int getSlot() {
		return this.slot;
	}

	public void load(NBTTagCompound nbtAS) {
		double[] newVs = new double[2];
		for (int i = 0; i < 2; i++) {
			newVs[i] = nbtAS.getTagList("Values", 6).getDoubleAt(i);
		}
		this.values = newVs;
		setAttribute(nbtAS.getString("Name"));
		this.chance = nbtAS.getDouble("Chance");
		this.slot = nbtAS.getInteger("Slot");
	}

	@Override
	public void remove() {
		this.parent.removeAttribute((AttributeSet) this);
	}

	public void setAttribute(IAttribute attribute) {
		this.attr = attribute;
	}

	@Override
	public void setAttribute(String name) {
		if (name.equals("generic.maxHealth")) {
			this.attr = SharedMonsterAttributes.MAX_HEALTH;
		} else if (name.equals("generic.followRange")) {
			this.attr = SharedMonsterAttributes.FOLLOW_RANGE;
		} else if (name.equals("generic.knockbackResistance")) {
			this.attr = SharedMonsterAttributes.KNOCKBACK_RESISTANCE;
		} else if (name.equals("generic.movementSpeed")) {
			this.attr = SharedMonsterAttributes.MOVEMENT_SPEED;
		} else if (name.equals("generic.attackDamage")) {
			this.attr = SharedMonsterAttributes.ATTACK_DAMAGE;
		} else if (name.equals("generic.attackSpeed")) {
			this.attr = SharedMonsterAttributes.ATTACK_SPEED;
		} else if (name.equals("generic.armor")) {
			this.attr = SharedMonsterAttributes.ARMOR;
		} else if (name.equals("generic.luck")) {
			this.attr = SharedMonsterAttributes.LUCK;
		} else { // new
			this.attr = (IAttribute) (new RangedAttribute((IAttribute) null, name, 0.0D, -1024.0D, 1024.0D)).setShouldWatch(true);
		}
	}

	@Override
	public void setChance(double chance) {
		double newChance = ValueUtil.correctDouble(chance, 0.0001d, 100.0d);
		this.chance = Math.round(newChance * 10000.0d) / 10000.0d;
	}

	@Override
	public void setSlot(int slot) {
		if (slot < -1 || slot > 5) {
			throw new CustomNPCsException("Slot has to be between -1 and 5, given was: " + slot, new Object[0]);
		}
		this.slot = slot;
	}

	@Override
	public void setValues(double min, double max) {
		double newMin = min;
		double newMax = max;
		if (min > max) {
			newMin = max;
			newMax = min;
		}
		this.values = new double[] { newMin, newMax };
	}
}

