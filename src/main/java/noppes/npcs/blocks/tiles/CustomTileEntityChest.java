package noppes.npcs.blocks.tiles;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import noppes.npcs.CustomNpcs;
import noppes.npcs.blocks.CustomChest;
import noppes.npcs.containers.ContainerChestCustom;

public class CustomTileEntityChest
extends TileEntityLockableLoot
implements ITickable {
	
	public float lidAngle;
	public float prevLidAngle;
	
	public NonNullList<ItemStack> inventory;
	public ResourceLocation chestTexture;
	private int size, numPlayersUsing = 0, ticksSinceSync = 0;
	public int guiColor = -1;
	public boolean isLock, isChest;
	private String name, blockName;
	public int[] guiColorArr;

	public CustomTileEntityChest() {
		this.setBlock(new CustomChest(Material.WOOD, new NBTTagCompound()));
	}
	
	public void setBlock(CustomChest block) {
		this.blockType = block;
		this.isChest = block.isChest;
		this.isLock = false;
		this.name = "custom.chest." + block.getCustomName();
		this.size = 9;
		this.blockName = block.getRegistryName().toString();
		this.chestTexture = block.nbtData.getKeySet().isEmpty() ? null : new ResourceLocation(CustomNpcs.MODID, "textures/entity/chest/" + block.getRegistryName().getResourcePath() + ".png");
		this.guiColor = -1;
		this.guiColorArr = null;
		if (block.nbtData.hasKey("Name", 8)) { this.name = block.nbtData.getString("Name"); }
		if (block.nbtData.hasKey("Size", 3)) { this.size = block.nbtData.getInteger("Size"); }
		if (block.nbtData.hasKey("isLock", 1)) { this.isLock = block.nbtData.getBoolean("isLock"); }
		if (block.nbtData.hasKey("GUIColor", 3)) { this.guiColor = block.nbtData.getInteger("GUIColor"); }
		if (block.nbtData.hasKey("GUIColor", 11)) {
			this.guiColor = -1;
			this.guiColorArr = block.nbtData.getIntArray("GUIColor");
		}
		this.inventory = NonNullList.<ItemStack>withSize(this.size, ItemStack.EMPTY);
	}
	
	@Override
	public int getSizeInventory() { return this.size; }
	
	@Override
	public boolean isEmpty() {
		for (ItemStack stack : this.inventory) {
			if (!stack.isEmpty()) { return false; }
		}
		return true;
	}
	
	@Override
	public int getInventoryStackLimit() { return 64; }
	
	@Override
	public String getName() {
		if (this.name.isEmpty()) { return "custom.chest.chestexample"; }
		return this.name;
	}
	
	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		this.fillWithLoot(playerIn);
		return new ContainerChestCustom(playerInventory, this, playerIn);
	}
	
	@Override
	public String getGuiID() { return this.blockName; }
	
	@Override
	protected NonNullList<ItemStack> getItems() { return this.inventory; }
	
	@Override
	public void update() {
		if ((this.inventory.size()==0 || this.chestTexture==null) && this.world!=null && this.world.isRemote) {
			if (this.world.getTotalWorldTime()%20==0) { CustomNpcs.proxy.fixTileEntityData(this); }
			return;
		}
		if (!this.world.isRemote && this.numPlayersUsing != 0 && (this.ticksSinceSync + this.pos.getX() + this.pos.getY() + this.pos.getZ()) % 200 == 0) {
			this.numPlayersUsing = 0;
			for (EntityPlayer entityplayer : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB((double)((float) this.pos.getX() - 5.0F), (double)((float) this.pos.getY() - 5.0F), (double)((float)pos.getZ() - 5.0F), (double)((float)( this.pos.getX() + 1) + 5.0F), (double)((float)( this.pos.getY() + 1) + 5.0F), (double)((float)( this.pos.getZ() + 1) + 5.0F)))) {
				if (entityplayer.openContainer instanceof ContainerChestCustom) {
					if (((ContainerChestCustom) entityplayer.openContainer).getPos().equals(this.pos)) { ++this.numPlayersUsing; }
				}
			}
		}
		this.prevLidAngle = this.lidAngle;
		if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F) {
			double d1 = (double) this.pos.getX() + 0.5D;
			double d2 = (double) this.pos.getZ() + 0.5D;
			this.world.playSound((EntityPlayer)null, d1, (double) this.pos.getY() + 0.5D, d2, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
		}
		if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
			float f2 = this.lidAngle;
			if (this.numPlayersUsing > 0) { this.lidAngle += 0.1F; }
			else { this.lidAngle -= 0.1F; }
			if (this.lidAngle > 1.0F) { this.lidAngle = 1.0F; }
			if (this.lidAngle < 0.5F && f2 >= 0.5F) {
				double d3 = (double) this.pos.getX() + 0.5D;
				double d0 = (double) this.pos.getZ() + 0.5D;
				this.world.playSound((EntityPlayer)null, d3, (double) this.pos.getY() + 0.5D, d0, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
			}
			if (this.lidAngle < 0.0F) { this.lidAngle = 0.0F; }
		}		
	}

	@Override
	public void openInventory(EntityPlayer player) {
		if (this.inventory==null || this.world==null) { return; }
		++this.numPlayersUsing;
		this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
		this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
	}
	
	@Override
	public void closeInventory(EntityPlayer player) {
		if (this.world==null) { return; }
		--this.numPlayersUsing;
		this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
		this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
	}

	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (!compound.hasKey("Items", 9)) { return; }
		this.isLock = compound.getBoolean("IsLock");
		this.isChest = compound.getBoolean("IsChest");
		this.guiColor = -1;
		this.guiColorArr = null;
		this.name = compound.getString("CustomName");
		this.size = compound.getInteger("Size");
		if (compound.hasKey("Texture", 8)) { this.chestTexture = new ResourceLocation(compound.getString("Texture")); }
		if (compound.hasKey("GUIColor", 3)) { this.guiColor = compound.getInteger("GUIColor"); }
		if (compound.hasKey("GUIColor", 11)) { this.guiColorArr = compound.getIntArray("GUIColor"); }
		if (this.size != this.inventory.size()) { this.inventory = NonNullList.<ItemStack>withSize(this.size, ItemStack.EMPTY); }
		this.inventory.clear();
		ItemStackHelper.loadAllItems(compound, this.inventory);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setBoolean("IsLock", this.isLock);
		compound.setBoolean("IsChest", this.isChest);
		compound.setInteger("Size", this.size);
		compound.setString("CustomName", this.name.isEmpty() ? "custom.chest.chestexample" : this.name);
		if (this.chestTexture!=null) { compound.setString("Texture", this.chestTexture.toString()); }
		if (this.guiColor!=-1) { compound.setInteger("GUIColor", this.guiColor); }
		if (this.guiColorArr!=null) { compound.setIntArray("GUIColor", this.guiColorArr); }
		ItemStackHelper.saveAllItems(compound, this.inventory);
		return compound;
	}

	public CustomTileEntityChest copy() {
		CustomTileEntityChest ctec = new CustomTileEntityChest();
		ctec.readFromNBT(this.writeToNBT(new NBTTagCompound()));
		return ctec;
	}
	
}
