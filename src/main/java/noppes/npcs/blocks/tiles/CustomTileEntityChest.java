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
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import noppes.npcs.CustomNpcs;
import noppes.npcs.blocks.CustomChest;
import noppes.npcs.containers.ContainerChestCustom;
import noppes.npcs.mixin.api.util.SoundEventAPIMixin;

import javax.annotation.Nonnull;
import java.util.Objects;

public class CustomTileEntityChest extends TileEntityLockableLoot implements ITickable {

	public float lidAngle;
	public float prevLidAngle;

	public NonNullList<ItemStack> inventory;
	public ResourceLocation chestTexture;
	private int size, numPlayersUsing = 0;
	public int guiColor = -1;
	public boolean isChest;
	private String name, blockName;
	public int[] guiColorArr;
	public SoundEvent sound_open = SoundEvents.BLOCK_CHEST_OPEN, sound_close = SoundEvents.BLOCK_CHEST_CLOSE;

	public CustomTileEntityChest() {
		this.setBlock(new CustomChest(Material.WOOD, new NBTTagCompound()));
	}

	@Override
	public void closeInventory(@Nonnull EntityPlayer player) {
		if (this.world == null) {
			return;
		}
		--this.numPlayersUsing;
		this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
		this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
	}

	public CustomTileEntityChest copy() {
		CustomTileEntityChest ctec = new CustomTileEntityChest();
		ctec.readFromNBT(this.writeToNBT(new NBTTagCompound()));
		return ctec;
	}

	@Override
	public @Nonnull Container createContainer(@Nonnull InventoryPlayer playerInventory, @Nonnull EntityPlayer playerIn) {
		this.fillWithLoot(playerIn);
		return new ContainerChestCustom(playerInventory, this, playerIn);
	}

	@Override
	public @Nonnull String getGuiID() {
		return this.blockName;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	protected @Nonnull NonNullList<ItemStack> getItems() {
		return this.inventory;
	}

	@Override
	public @Nonnull String getName() {
		if (this.name.isEmpty()) {
			return "custom.chest.chestexample";
		}
		return this.name;
	}

	@Override
	public int getSizeInventory() {
		return this.inventory.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : this.inventory) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void openInventory(@Nonnull EntityPlayer player) {
		if (this.inventory == null || this.world == null) {
			return;
		}
		++this.numPlayersUsing;
		this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
		this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
	}

	public void readFromNBT(@Nonnull NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (!compound.hasKey("Items", 9)) {
			return;
		}
		this.isChest = compound.getBoolean("IsChest");
		this.guiColor = -1;
		this.guiColorArr = null;
		this.name = compound.getString("CustomName");
		this.size = compound.getInteger("Size");
		if (compound.hasKey("Texture", 8)) {
			this.chestTexture = new ResourceLocation(compound.getString("Texture"));
		}
		if (compound.hasKey("GUIColor", 11)) {
			this.guiColorArr = compound.getIntArray("GUIColor");
		} else if (compound.hasKey("GUIColor", 3)) {
			this.guiColor = compound.getInteger("GUIColor");
		}
		SoundEvent soundopen = SoundEvent.REGISTRY.getObject(new ResourceLocation(compound.getString("SoundOpen")));
		if (soundopen != null) {
			this.sound_open = soundopen;
		}
		SoundEvent soundclose = SoundEvent.REGISTRY.getObject(new ResourceLocation(compound.getString("SoundClose")));
		if (soundclose != null) {
			this.sound_close = soundclose;
		}
		if (this.size != this.inventory.size()) {
			this.inventory = NonNullList.withSize(this.size, ItemStack.EMPTY);
		}
		this.inventory.clear();
		ItemStackHelper.loadAllItems(compound, this.inventory);
	}

	public void setBlock(CustomChest block) {
		this.blockType = block;
		this.isChest = block.isChest;
		this.name = "custom.chest." + block.getCustomName();
		this.size = 9;
		this.blockName = Objects.requireNonNull(block.getRegistryName()).toString();
		this.chestTexture = block.nbtData.getKeySet().isEmpty() ? null
				: new ResourceLocation(CustomNpcs.MODID,
						"textures/entity/chest/" + block.getRegistryName().getResourcePath() + ".png");
		this.guiColor = -1;
		this.guiColorArr = null;
		if (block.nbtData.hasKey("Name", 8)) {
			this.name = block.nbtData.getString("Name");
		}
		if (block.nbtData.hasKey("Size", 3)) {
			this.size = block.nbtData.getInteger("Size");
		}
		if (block.nbtData.hasKey("GUIColor", 3)) {
			this.guiColor = block.nbtData.getInteger("GUIColor");
		}
		if (block.nbtData.hasKey("GUIColor", 11)) {
			this.guiColor = -1;
			this.guiColorArr = block.nbtData.getIntArray("GUIColor");
		}
		if (this.size > 189) {
			this.size = 189;
		}
		this.inventory = NonNullList.withSize(this.size, ItemStack.EMPTY);
	}

	@Override
	public void update() {
		if ((this.inventory.isEmpty() || this.chestTexture == null) && this.world != null && this.world.isRemote) {
			if (this.world.getTotalWorldTime() % 20 == 0) {
				CustomNpcs.proxy.fixTileEntityData(this);
			}
			return;
		}
		if (this.numPlayersUsing < 0) {
			this.numPlayersUsing = 0;
		}
        if (this.world != null && !this.world.isRemote && this.numPlayersUsing != 0
				&& (this.world.getTotalWorldTime() + this.pos.getX() + this.pos.getY() + this.pos.getZ()) % 20 == 0) {
			this.numPlayersUsing = 0;
			for (EntityPlayer entityplayer : this.world.getEntitiesWithinAABB(EntityPlayer.class,
					new AxisAlignedBB((float) this.pos.getX() - 5.0F,
                            (float) this.pos.getY() - 5.0F,
							(float) pos.getZ() - 5.0F,
                            (float) (this.pos.getX() + 1) + 5.0F,
                            (float) (this.pos.getY() + 1) + 5.0F,
                            (float) (this.pos.getZ() + 1) + 5.0F))) {
				if (entityplayer.openContainer instanceof ContainerChestCustom) {
					if (((ContainerChestCustom) entityplayer.openContainer).getPos().equals(this.pos)) {
						++this.numPlayersUsing;
					}
				}
			}
		}
		this.prevLidAngle = this.lidAngle;
		if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F) {
			double d1 = (double) this.pos.getX() + 0.5D;
			double d2 = (double) this.pos.getZ() + 0.5D;
			this.world.playSound(null, d1, (double) this.pos.getY() + 0.5D, d2, this.sound_open,
					SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
		}
		if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
			float f2 = this.lidAngle;
			if (this.numPlayersUsing > 0) {
				this.lidAngle += 0.1F;
			} else {
				this.lidAngle -= 0.1F;
			}
			if (this.lidAngle > 1.0F) {
				this.lidAngle = 1.0F;
			}
			if (this.lidAngle < 0.5F && f2 >= 0.5F) {
				double d3 = (double) this.pos.getX() + 0.5D;
				double d0 = (double) this.pos.getZ() + 0.5D;
				this.world.playSound(null, d3, (double) this.pos.getY() + 0.5D, d0, this.sound_close, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
			}
			if (this.lidAngle < 0.0F) {
				this.lidAngle = 0.0F;
			}
		}
	}

	public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setBoolean("IsChest", this.isChest);
		compound.setInteger("Size", this.size);
		compound.setString("CustomName", this.name.isEmpty() ? "custom.chest.chestexample" : this.name);
		if (this.chestTexture != null) {
			compound.setString("Texture", this.chestTexture.toString());
		}
		if (this.guiColor != -1) {
			compound.setInteger("GUIColor", this.guiColor);
		}
		if (this.guiColorArr != null) {
			compound.setIntArray("GUIColor", this.guiColorArr);
		}
		compound.setString("SoundOpen", ((SoundEventAPIMixin) this.sound_open).npcs$getSoundName().toString());
		compound.setString("SoundClose", ((SoundEventAPIMixin) this.sound_close).npcs$getSoundName().toString());
		ItemStackHelper.saveAllItems(compound, this.inventory);
		return compound;
	}

}
