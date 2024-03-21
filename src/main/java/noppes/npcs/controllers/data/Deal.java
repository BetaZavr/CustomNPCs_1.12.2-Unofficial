package noppes.npcs.controllers.data;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.Server;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.api.handler.data.IDeal;
import noppes.npcs.api.handler.data.IMarcet;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.MarcetController;

public class Deal
implements IDeal {

	private int amount;
	public Availability availability;
	private float chance; // 0.0 <-> 1.0
	private int[] count;
	private int id, sectionId;
	private boolean ignoreDamage, ignoreNBT;
	private final NpcMiscInventory inventoryCurrency;
	private final NpcMiscInventory inventoryProduct;
	private int money, type;
	private int marcetID;
	public boolean update;

	public Deal(int id, int marcetID) {
		this.id = id;
		this.sectionId = 0;
		this.availability = new Availability();
		this.ignoreDamage = false;
		this.ignoreNBT = false;
		this.inventoryCurrency = new NpcMiscInventory(9);
		this.inventoryProduct = new NpcMiscInventory(1);
		this.type = 2;
		this.money = 0;
		this.count = new int[] { 0, 0 };
		this.chance = 1.0f;
		this.amount = 1;
		this.marcetID = marcetID;
	}

	public Deal copy() {
		Deal deal = new Deal(this.id, this.marcetID);
		deal.sectionId = this.sectionId;
		deal.availability = this.availability;
		deal.ignoreDamage = this.ignoreDamage;
		deal.ignoreNBT = this.ignoreNBT;
		for (int i = 0; i < 9; i++) {
			deal.inventoryCurrency.setInventorySlotContents(i, this.inventoryCurrency.getStackInSlot(i).copy());
		}
		deal.inventoryProduct.setInventorySlotContents(0, this.inventoryProduct.getStackInSlot(0).copy());
		deal.type = this.type;
		deal.money = this.money;
		deal.count[0] = this.count[0];
		deal.count[1] = this.count[1];
		deal.chance = this.chance;
		deal.amount = this.amount;
		return deal;
	}
	
	@Override
	public String getName() {
		String name = " - Empty";
		ItemStack stack = this.inventoryProduct.getStackInSlot(0);
		if (!stack.isEmpty()) {
			name = (this.amount == 0 ? new String(Character.toChars(0x00A7)) + "c" : "") + stack.getDisplayName() + " x" + stack.getCount();
		}
		return name;
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("Availability", this.availability.writeToNBT(new NBTTagCompound()));
		compound.setBoolean("IgnoreDamage", this.ignoreDamage);
		compound.setBoolean("IgnoreNBT", this.ignoreNBT);
		compound.setTag("Currency", this.inventoryCurrency.getToNBT());
		compound.setTag("Product", this.inventoryProduct.getToNBT());
		compound.setInteger("Type", this.type);
		compound.setInteger("Money", this.money);
		compound.setIntArray("Count", this.count);
		compound.setFloat("Chance", this.chance);
		compound.setInteger("Amount", this.amount);
		compound.setInteger("MarcetID", this.marcetID);
		compound.setInteger("DealID", this.id);
		compound.setInteger("SectionID", this.sectionId);
		return compound;
	}

	public String getSettingName() {
		ItemStack stack = this.inventoryProduct.getStackInSlot(0);
		Marcet m = MarcetController.getInstance().marcets.get(this.marcetID);
		if (m != null) {
			if (!m.sections.containsKey(this.sectionId)) { this.sectionId = 0; }
		}
		String section = ((char) 167) + "e#" + ((char) 167) + "r" + this.sectionId;
		return section + "; ID:" + this.id + ": " + (stack.isEmpty()
				? ((char) 167) + "4" + new TextComponentTranslation("type.empty").getFormattedText()
				: (this.inventoryCurrency.isEmpty() && this.money == 0 ? new String(Character.toChars(0x00A7)) + "c" : "") + stack.getDisplayName())
				+ (!stack.isEmpty() ? " x" + stack.getCount() : "");
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.availability.readFromNBT(compound.getCompoundTag("Availability"));
		this.ignoreDamage = compound.getBoolean("IgnoreDamage");
		this.ignoreNBT = compound.getBoolean("IgnoreNBT");
		this.inventoryCurrency.setFromNBT(compound.getCompoundTag("Currency"));
		this.inventoryProduct.setFromNBT(compound.getCompoundTag("Product"));
		this.type = compound.getInteger("Type");
		this.money = compound.getInteger("Money");
		this.count = compound.getIntArray("Count");
		this.chance = compound.getFloat("Chance");
		this.amount = compound.getInteger("Amount");
		this.marcetID = compound.getInteger("MarcetID");
		this.sectionId = compound.getInteger("SectionID");
		if (this.sectionId < 0) { this.sectionId = 0; }
	}

	@Override
	public void set(IItemStack product, IItemStack[] currencys) {
		if (product == null) { product = ItemStackWrapper.AIR; }
		ItemStack[] cs = new ItemStack[currencys == null ? 0 : currencys.length];
		if (currencys!=null) {
			int i = 0;
			for (IItemStack stack : currencys) {
				cs[i] = stack.getMCItemStack();
				i++;
			}
		}
		this.set(product.getMCItemStack(), cs);
	}
	
	public void set(ItemStack product, ItemStack[] currencys) {
		if (product == null) { product = ItemStack.EMPTY; }
		this.inventoryProduct.setInventorySlotContents(0, product);

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

	@Override
	public void updateNew() {
		if (this.chance >= (float) Math.random()) {
			if (this.count[1] != 0 && this.count[1] >= this.count[0]) {
				this.amount = this.count[0] + (int) (Math.random() * (this.count[1] - this.count[0]));
			} else {
				this.amount = 1;
			}
		} else {
			this.amount = 0;
		}
	}

	@Override
	public int getId() { return this.id; }

	@Override
	public IMarcet getMarcet() {
		return MarcetController.getInstance().getMarcet(this.marcetID);
	}

	@Override
	public IContainer getCurrency() {
		return NpcAPI.Instance().getIContainer(this.inventoryCurrency);
	}

	@Override
	public IItemStack getProduct() {
		return NpcAPI.Instance().getIItemStack(this.inventoryProduct.getStackInSlot(0));
	}

	@Override
	public int getMoney() { return this.money; }

	@Override
	public void setMoney(int money) {
		if (money < 0) { money = 0; }
		this.money = money;
		this.update = true;
	}

	@Override
	public boolean getIgnoreDamage() { return this.ignoreDamage; }

	@Override
	public boolean getIgnoreNBT() { return this.ignoreNBT; }

	@Override
	public void setIgnoreDamage(boolean bo) {
		if (bo==this.ignoreDamage) { return; }
		this.ignoreDamage = bo;
		this.update = true;
	}

	@Override
	public void setIgnoreNBT(boolean bo) {
		if (bo==this.ignoreNBT) { return; }
		this.ignoreNBT = bo;
		this.update = true;
	}

	@Override
	public IAvailability getAvailability() { return this.availability; }
	
	@Override
	public int getChance() { return (int) (this.chance * 100.0f); }

	@Override
	public void setChance(int chance) {
		this.setChance(((float) chance) / 100.0f);
	}

	public void setChance(float chance) {
		if (chance < 0.0f) { chance = 0.0f; }
		else if (chance > 1.0f) { chance = 1.0f; }
		this.chance = chance;
		this.update = true;
	}
	
	@Override
	public int getAmount() { return this.amount; }

	@Override
	public void setAmount(int amount) {
		if (amount < 0) { amount = 0; }
		this.amount = amount;
		this.update = true;
	}
	
	@Override
	public int getType() { return this.type; }

	@Override
	public void setType(int type) {
		if (type < 0) { type *= -1; }
		this.type = type % 3;
		this.update = true;
	}
	
	@Override
	public int getMinCount() { return this.count[0]; }
	
	@Override
	public int getMaxCount() { return this.count[1]; }

	@Override
	public void setCount(int min, int max) {
		if (min < 0) { min *= -1; }
		if (max < 0) { max *= -1; }
		if (max < min) {
			int m = min;
			min = max;
			max = m;
		}
		this.count[0] = min;
		this.count[1] = max;
		this.update = true;
	}

	public boolean isValid() {
		if (this.inventoryProduct.getStackInSlot(0)==null || this.inventoryProduct.getStackInSlot(0).isEmpty() || (this.money<=0 && this.inventoryCurrency.isEmpty())) { return false; }
		return true;
	}

	@Override
	public void setProduct(IItemStack product) {
		if (product == null) { product = ItemStackWrapper.AIR; }
		this.inventoryProduct.setInventorySlotContents(0, product.getMCItemStack());
	}

	@Override
	public IInventory getMCInventoryProduct() { return this.inventoryProduct; }

	@Override
	public IInventory getMCInventoryCurrency() { return this.inventoryCurrency; }

	public int getMarcetID() { return this.marcetID; }

	public void update() {
		if (this.update) {
			this.update = false;
			Marcet marcet = (Marcet) MarcetController.getInstance().getMarcet(this.marcetID);
			if (marcet!=null) { 
				for (EntityPlayer listener : marcet.listeners) {
					if (listener instanceof EntityPlayerMP) {
						Server.sendData((EntityPlayerMP) listener, EnumPacketClient.MARCET_DATA, 3, this.writeToNBT());
						Server.sendData((EntityPlayerMP) listener, EnumPacketClient.MARCET_DATA, 2);
					}
				}
			}
		}
	}

	public int getSectionID() { return this.sectionId; }
	
	public void setSectionID(int sectionId) {
		if (sectionId < 0) { sectionId = 0; }
		Marcet m = MarcetController.getInstance().marcets.get(this.marcetID);
		if (m != null && !m.sections.containsKey(sectionId)) { sectionId = 0; }
		this.sectionId = sectionId;
	}
	
}
