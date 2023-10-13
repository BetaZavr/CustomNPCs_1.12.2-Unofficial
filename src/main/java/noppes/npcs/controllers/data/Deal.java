package noppes.npcs.controllers.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.NpcMiscInventory;

public class Deal {

	public int amount;
	public Availability availability;
	public float chance; // 0.0 <-> 1.0
	public int[] count;
	public int id;
	public boolean ignoreDamage;
	public boolean ignoreNBT;
	public NpcMiscInventory inventoryCurrency;
	public NpcMiscInventory inventorySold;
	public int money;
	public int type; // 

	public Deal() {
		this.id = 0;
		this.availability = new Availability();
		this.ignoreDamage = false;
		this.ignoreNBT = false;
		this.inventoryCurrency = new NpcMiscInventory(9);
		this.inventorySold = new NpcMiscInventory(1);
		this.type = 0;
		this.money = 0;
		this.count = new int[] { 0, 0 };
		this.chance = 1.0f;
		this.amount = 1;
	}

	public Deal copy() {
		Deal deal = new Deal();
		deal.id = this.id;
		deal.availability = this.availability;
		deal.ignoreDamage = this.ignoreDamage;
		deal.ignoreNBT = this.ignoreNBT;
		for (int i = 0; i < 9; i++) {
			deal.inventoryCurrency.setInventorySlotContents(i, this.inventoryCurrency.getStackInSlot(i).copy());
		}
		deal.inventorySold.setInventorySlotContents(0, this.inventorySold.getStackInSlot(0).copy());
		deal.type = this.type;
		deal.money = this.money;
		deal.count[0] = this.count[0];
		deal.count[1] = this.count[1];
		deal.chance = this.chance;
		deal.amount = this.amount;
		return deal;
	}

	public String getName() {
		String name = "Empty";
		ItemStack stack = this.inventorySold.getStackInSlot(0);
		if (!stack.isEmpty()) {
			name = (this.count[1] > 0 && this.amount == 0 ? new String(Character.toChars(0x00A7)) + "c" : "")
					+ stack.getDisplayName() + " x" + stack.getCount();
		}
		return name;
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("Availability", this.availability.writeToNBT(new NBTTagCompound()));
		compound.setBoolean("IgnoreDamage", this.ignoreDamage);
		compound.setBoolean("IgnoreNBT", this.ignoreNBT);
		compound.setTag("Currency", this.inventoryCurrency.getToNBT());
		compound.setTag("Product", this.inventorySold.getToNBT());
		compound.setInteger("Type", this.type);
		compound.setInteger("Money", this.money);
		compound.setIntArray("Count", this.count);
		compound.setFloat("Chance", this.chance);
		compound.setInteger("Amount", this.amount);
		return compound;
	}

	public String getSettingName() {
		ItemStack stack = this.inventorySold.getStackInSlot(0);
		return this.id + ": " + (stack.isEmpty()
				? new String(Character.toChars(0x00A7)) + "4"
						+ new TextComponentTranslation("type.empty").getFormattedText()
				: (this.inventoryCurrency.isEmpty() && this.money == 0 ? new String(Character.toChars(0x00A7)) + "c"
						: "") + stack.getDisplayName())
				+ (!stack.isEmpty() ? " x" + stack.getCount() : "");
	}

	public void read(NBTTagCompound compound) {
		this.availability.readFromNBT(compound.getCompoundTag("Availability"));
		this.ignoreDamage = compound.getBoolean("IgnoreDamage");
		this.ignoreNBT = compound.getBoolean("IgnoreNBT");
		this.inventoryCurrency.setFromNBT(compound.getCompoundTag("Currency"));
		this.inventorySold.setFromNBT(compound.getCompoundTag("Product"));
		this.type = compound.getInteger("Type");
		this.money = compound.getInteger("Money");
		this.count = compound.getIntArray("Count");
		this.chance = compound.getFloat("Chance");
		this.amount = compound.getInteger("Amount");
	}

	public void set(ItemStack product, ItemStack[] currencys) {
		if (product == null) {
			product = ItemStack.EMPTY;
		}
		this.inventorySold.setInventorySlotContents(0, product);

		if (this.count[1] != 0 && this.count[1] >= this.count[0]) {
			this.amount = 0;
			if (this.chance <= (float) Math.random()) {
				this.amount = this.count[0] + (int) (Math.random() * (this.count[1] - this.count[0]));
			}
		} else {
			this.amount = 1;
		}

		this.inventoryCurrency.clear();
		if (currencys != null) {
			for (int i = 0, j = 0; i < currencys.length; i++) {
				if (currencys[i] == null || currencys[i].isEmpty()) {
					continue;
				}
				this.inventoryCurrency.setInventorySlotContents(j, currencys[i]);
				j++;
			}
		}
	}

	public void updateNew() {
		if (this.count[1] != 0 && this.count[1] >= this.count[0]) {
			this.amount = 0;
			if (this.chance >= (float) Math.random()) {
				this.amount = this.count[0] + (int) (Math.random() * (this.count[1] - this.count[0]));
			}
		} else {
			this.amount = 1;
		}
	}

}
