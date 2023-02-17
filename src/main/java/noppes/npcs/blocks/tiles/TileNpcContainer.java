package noppes.npcs.blocks.tiles;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import noppes.npcs.NoppesUtilServer;

public abstract class TileNpcContainer extends TileColorable implements IInventory {
	public String customName;
	public NonNullList<ItemStack> inventoryContents;
	public int playerUsing;

	public TileNpcContainer() {
		this.customName = "";
		this.playerUsing = 0;
		this.inventoryContents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
	}

	public void clear() {
	}

	public void closeInventory(EntityPlayer player) {
		--this.playerUsing;
	}

	public ItemStack decrStackSize(int index, int count) {
		ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventoryContents, index, count);
		if (!itemstack.isEmpty()) {
			this.markDirty();
		}
		return itemstack;
	}

	public void dropItems(World world, BlockPos pos) {
		for (int i1 = 0; i1 < this.getSizeInventory(); ++i1) {
			ItemStack itemstack = this.getStackInSlot(i1);
			if (!NoppesUtilServer.IsItemStackNull(itemstack)) {
				float f = world.rand.nextFloat() * 0.8f + 0.1f;
				float f2 = world.rand.nextFloat() * 0.8f + 0.1f;
				float f3 = world.rand.nextFloat() * 0.8f + 0.1f;
				while (itemstack.getCount() > 0) {
					int j1 = world.rand.nextInt(21) + 10;
					if (j1 > itemstack.getCount()) {
						j1 = itemstack.getCount();
					}
					itemstack.setCount(itemstack.getCount() - j1);
					EntityItem entityitem = new EntityItem(world, (pos.getX() + f), (pos.getY() + f2),
							(pos.getZ() + f3), new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));
					float f4 = 0.05f;
					entityitem.motionX = world.rand.nextGaussian() * f4;
					entityitem.motionY = world.rand.nextGaussian() * f4 + 0.2f;
					entityitem.motionZ = world.rand.nextGaussian() * f4;
					if (itemstack.hasTagCompound()) {
						entityitem.getItem().setTagCompound(itemstack.getTagCompound().copy());
					}
					world.spawnEntity(entityitem);
				}
			}
		}
	}

	public ITextComponent getDisplayName() {
		return new TextComponentString(this.hasCustomName() ? this.customName : this.getName());
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

	public abstract String getName();

	public int getSizeInventory() {
		return 54;
	}

	public ItemStack getStackInSlot(int index) {
		return this.inventoryContents.get(index);
	}

	public boolean hasCustomName() {
		return !this.customName.isEmpty();
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

	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return true;
	}

	public boolean isUsableByPlayer(EntityPlayer player) {
		return (player.isDead || this.world.getTileEntity(this.pos) == this)
				&& player.getDistanceSq(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64.0;
	}

	public void openInventory(EntityPlayer player) {
		++this.playerUsing;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		NBTTagList nbttaglist = compound.getTagList("Items", 10);
		if (compound.hasKey("CustomName", 8)) {
			this.customName = compound.getString("CustomName");
		}
		this.inventoryContents.clear();
		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound1.getByte("Slot") & 0xFF;
			if (j >= 0 && j < this.inventoryContents.size()) {
				this.inventoryContents.set(j, new ItemStack(nbttagcompound1));
			}
		}
	}

	public boolean receiveClientEvent(int id, int type) {
		if (id == 1) {
			this.playerUsing = type;
			return true;
		}
		return super.receiveClientEvent(id, type);
	}

	public ItemStack removeStackFromSlot(int index) {
		return this.inventoryContents.set(index, ItemStack.EMPTY);
	}

	public void setField(int id, int value) {
	}

	public void setInventorySlotContents(int index, ItemStack stack) {
		this.inventoryContents.set(index, stack);
		if (stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount(this.getInventoryStackLimit());
		}
		this.markDirty();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < this.inventoryContents.size(); ++i) {
			if (!(this.inventoryContents.get(i)).isEmpty()) {
				NBTTagCompound tagCompound = new NBTTagCompound();
				tagCompound.setByte("Slot", (byte) i);
				(this.inventoryContents.get(i)).writeToNBT(tagCompound);
				nbttaglist.appendTag(tagCompound);
			}
		}
		compound.setTag("Items", nbttaglist);
		if (this.hasCustomName()) {
			compound.setString("CustomName", this.customName);
		}
		return super.writeToNBT(compound);
	}
}
