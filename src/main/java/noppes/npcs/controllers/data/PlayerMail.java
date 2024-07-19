package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.controllers.QuestController;

import javax.annotation.Nonnull;

public class PlayerMail implements IInventory, IPlayerMail {

	public boolean beenRead, returned;
	public NonNullList<ItemStack> items;
	public NBTTagCompound message;
	// New
	public int money, ransom, questId;
	public String sender, title;
	public long timeWillCome, timeWhenReceived;

	public PlayerMail() {
		this.clear();
		this.timeWillCome = 0L;
		this.timeWhenReceived = System.currentTimeMillis();
	}

	public void clear() {
		this.title = "";
		this.sender = "";
		this.message = new NBTTagCompound();
		this.beenRead = false;
		this.returned = false;
		this.questId = -1;
		if (this.items == null) {
			this.items = NonNullList.withSize(4, ItemStack.EMPTY);
		} else {
			this.items.clear();
		}
		this.money = 0;
		this.ransom = 0;
		this.timeWhenReceived = System.currentTimeMillis();
	}

	public void closeInventory(@Nonnull EntityPlayer player) {
	}

	public PlayerMail copy() {
		PlayerMail mail = new PlayerMail();
		mail.readNBT(this.writeNBT());
		return mail;
	}

	public @Nonnull ItemStack decrStackSize(int slot, int count) {
		ItemStack itemstack = ItemStackHelper.getAndSplit(this.items, slot, count);
		if (!itemstack.isEmpty()) {
			this.markDirty();
		}
		return itemstack;
	}

	public IContainer getContainer() {
		return Objects.requireNonNull(NpcAPI.Instance()).getIContainer(this);
	}

	public @Nonnull ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}

	public int getField(int id) {
		return 0;
	}

	public int getFieldCount() {
		return 0;
	}

	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public int getMoney() {
		return this.money;
	}

	public @Nonnull String getName() {
		return "Mail Inventory";
	}

	public Quest getQuest() {
		return (QuestController.instance != null) ? QuestController.instance.quests.get(this.questId) : null;
	}

	@Override
	public int getRansom() {
		return this.ransom;
	}

	public String getSender() {
		return this.sender;
	}

	public int getSizeInventory() {
		return this.items.size();
	}

	public @Nonnull ItemStack getStackInSlot(int slot) {
		return this.items.get(slot);
	}

	public String getSubject() {
		return this.title;
	}

	public String[] getText() {
		List<String> list = new ArrayList<>();
		NBTTagList pages = this.message.getTagList("pages", 8);
		for (int i = 0; i < pages.tagCount(); ++i) {
			list.add(pages.getStringTagAt(i));
		}
		return list.toArray(new String[0]);
	}

	public boolean hasCustomName() {
		return false;
	}

	public boolean hasQuest() {
		return this.getQuest() != null;
	}

	public boolean isEmpty() {
		for (int slot = 0; slot < this.getSizeInventory(); ++slot) {
			ItemStack item = this.getStackInSlot(slot);
			if (!NoppesUtilServer.IsItemStackNull(item) && !item.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public boolean isItemValidForSlot(int slot, @Nonnull ItemStack item) {
		return true;
	}

	public boolean isReturned() {
		return this.returned;
	}

	public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
		return true;
	}

	public boolean isValid() {
		return !this.title.isEmpty() && !this.message.getKeySet().isEmpty() && !this.sender.isEmpty();
	}

	public void markDirty() {
	}

	public void openInventory(@Nonnull EntityPlayer player) {
	}

	public void readNBT(NBTTagCompound compound) {
		this.title = compound.getString("Subject");
		this.sender = compound.getString("Sender");
		this.message = compound.getCompoundTag("Message");
		this.beenRead = compound.getBoolean("BeenRead");
		this.returned = compound.getBoolean("Returned");
		this.timeWillCome = compound.getLong("TimeWillCome");
		this.timeWhenReceived = compound.getLong("TimeWhenReceived");
		if (compound.hasKey("MailQuest")) {
			this.questId = compound.getInteger("MailQuest");
		}
		this.items.clear();
		NBTTagList nbttaglist = compound.getTagList("MailItems", 10);
		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbt = nbttaglist.getCompoundTagAt(i);
			int j = nbt.getByte("Slot") & 0xFF;
			if (j < this.items.size()) {
				this.items.set(j, new ItemStack(nbt));
			}
		}
		// New
		this.money = compound.getInteger("Money");
		this.ransom = compound.getInteger("Ransom");
	}

	public @Nonnull ItemStack removeStackFromSlot(int slot) {
		return this.items.set(slot, ItemStack.EMPTY);
	}

	public void setField(int id, int value) {
	}

	public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
		this.items.set(index, stack);
		if (stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount(this.getInventoryStackLimit());
		}
		this.markDirty();
	}

	@Override
	public void setMoney(int money) {
		if (money < 0) {
			money = 0;
		}
		this.money = money;
	}

	public void setQuest(int id) {
		this.questId = id;
	}

	@Override
	public void setRansom(int money) {
		if (money < 0) {
			money = 0;
		}
		this.ransom = money;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public void setSubject(String subject) {
		this.title = subject;
	}

	public void setText(String[] pages) {
		NBTTagList list = new NBTTagList();
		if (pages != null) {
			for (String page : pages) {
				list.appendTag(new NBTTagString(page));
			}
		}
		this.message.setTag("pages", list);
	}

	public NBTTagCompound writeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("Subject", this.title);
		compound.setString("Sender", this.sender);
		compound.setTag("Message", this.message);
		compound.setBoolean("BeenRead", this.beenRead);
		compound.setBoolean("Returned", this.returned);

		compound.setLong("TimeWillCome", this.timeWillCome);
		compound.setLong("TimeWhenReceived", this.timeWhenReceived);

		compound.setInteger("MailQuest", this.questId);
		if (this.hasQuest()) {
			compound.setString("MailQuestTitle", this.getQuest().getTitle());
		}
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < this.items.size(); ++i) {
			if (!(this.items.get(i)).isEmpty()) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setByte("Slot", (byte) i);
				(this.items.get(i)).writeToNBT(nbt);
				nbttaglist.appendTag(nbt);
			}
		}
		compound.setTag("MailItems", nbttaglist);
		// New
		compound.setInteger("Money", this.money);
		compound.setInteger("Ransom", this.ransom);
		return compound;
	}

}
