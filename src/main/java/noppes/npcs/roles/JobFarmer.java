package noppes.npcs.roles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockStem;
import net.minecraft.block.NpcBlockHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.data.role.IJobFarmer;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.controllers.MassBlockController;
import noppes.npcs.controllers.data.BlockData;
import noppes.npcs.entity.EntityNPCInterface;

public class JobFarmer extends JobInterface implements MassBlockController.IMassBlock, IJobFarmer {

	private int blockTicks = 800;
	private BlockPos chest = null;
	public int chestMode = 1;
	private ItemStack holding = ItemStack.EMPTY;
	private BlockPos ripe = null;
	private int ticks = 0;
	private int walkTicks = 0;
	private int range = 16;
	private final List<BlockPos> trackedBlocks = new ArrayList<>();
	private boolean waitingForBlocks = false;

	public JobFarmer(EntityNPCInterface npc) {
		super(npc);
		overrideMainHand = true;
		type = JobType.FARMER;
	}

	@Override
	public boolean isWorking() {
		return !trackedBlocks.isEmpty();
	}

	@Override
	public boolean aiContinueExecute() {
		return false;
	}

	@Override
	public boolean aiShouldExecute() {
		if (!holding.isEmpty()) {
			if (chestMode == 0) {
				setHolding(ItemStack.EMPTY);
			} else if (chestMode == 1) {
				if (chest == null) {
					dropItem(holding);
					setHolding(ItemStack.EMPTY);
				} else {
					chest();
				}
			} else if (chestMode == 2) {
				dropItem(holding);
				setHolding(ItemStack.EMPTY);
			}
			return false;
		}
		if (ripe != null) {
			pluck();
			return false;
		}
		if (!waitingForBlocks && blockTicks++ > 1200) {
			blockTicks = 0;
			waitingForBlocks = true;
			MassBlockController.Queue(this);
		}
		if (ticks++ < 100) {
			return false;
		}
		ticks = 0;
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void aiUpdateTask() {
		Iterator<BlockPos> ite = trackedBlocks.iterator();
		while (ite.hasNext() && ripe == null) {
			BlockPos pos = ite.next();
			IBlockState state = npc.world.getBlockState(pos);
			Block b = state.getBlock();
			if (b instanceof BlockCrops) {
				if (!((BlockCrops) b).isMaxAge(state)) {
					continue;
				}
				ripe = pos;
			} else if (b instanceof BlockStem) {
				state = b.getActualState(state, npc.world, pos);
				EnumFacing facing = state.getValue(BlockStem.FACING);
				if (facing == EnumFacing.UP) {
					continue;
				}
				ripe = pos;
			} else {
				ite.remove();
			}
		}
		npc.ais.returnToStart = (ripe == null);
		if (ripe != null) {
			npc.getNavigator().clearPath();
			npc.getLookHelper().setLookPosition(ripe.getX(), ripe.getY(), ripe.getZ(), 10.0f, npc.getVerticalFaceSpeed());
		}
	}

	private void chest() {
		BlockPos pos = chest;
		npc.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 1.0);
		npc.getLookHelper().setLookPosition(pos.getX(), pos.getY(), pos.getZ(), 10.0f, npc.getVerticalFaceSpeed());
		if (npc.nearPosition(pos) || walkTicks++ > 400) {
			if (walkTicks < 400) {
				npc.swingArm(EnumHand.MAIN_HAND);
			}
			npc.getNavigator().clearPath();
			ticks = 100;
			walkTicks = 0;
			IBlockState state = npc.world.getBlockState(pos);
			if (state.getBlock() instanceof BlockChest) {
				TileEntityChest tile = (TileEntityChest) npc.world.getTileEntity(pos);
				for (int i = 0; !holding.isEmpty() && i < Objects.requireNonNull(tile).getSizeInventory(); ++i) {
					holding = mergeStack(tile, i, holding);
				}
				for (int i = 0; !holding.isEmpty() && i < tile.getSizeInventory(); ++i) {
					ItemStack item = tile.getStackInSlot(i);
					if (item.isEmpty()) {
						tile.setInventorySlotContents(i, holding);
						holding = ItemStack.EMPTY;
					}
				}
				if (!holding.isEmpty()) {
					dropItem(holding);
					holding = ItemStack.EMPTY;
				}
			} else {
				chest = null;
			}
			setHolding(holding);
		}
	}

	private void dropItem(ItemStack item) {
		EntityItem entityitem = new EntityItem(npc.world, npc.posX, npc.posY, npc.posZ, item);
		entityitem.setDefaultPickupDelay();
		npc.world.spawnEntity(entityitem);
	}

	@Override
	public IItemStack getMainhand() {
		String name = npc.getJobData();
		ItemStack item = stringToItem(name);
		if (item.isEmpty()) {
			return npc.inventory.weapons.get(0);
		}
		return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(item);
	}

	@Override
	public int getMutexBits() {
		return npc.getNavigator().noPath() ? 0 : AiMutex.LOOK;
	}

	@Override
	public EntityNPCInterface getNpc() {
		return npc;
	}

	@Override
	public int getRange() {
		return range;
	}

	@Override
	public boolean isPlucking() {
		return ripe != null || !holding.isEmpty();
	}

	private ItemStack mergeStack(IInventory inventory, int slot, ItemStack item) {
		ItemStack item2 = inventory.getStackInSlot(slot);
		if (!NoppesUtilPlayer.compareItems(item, item2, false, false)) {
			return item;
		}
		int size = item2.getMaxStackSize() - item2.getCount();
		if (size >= item.getCount()) {
			item2.setCount(item2.getCount() + item.getCount());
			return ItemStack.EMPTY;
		}
		item2.setCount(item2.getMaxStackSize());
		item.setCount(item.getCount() - size);
		if (item.isEmpty()) {
			return ItemStack.EMPTY;
		}
		return item;
	}

	@SuppressWarnings("deprecation")
	private void pluck() {
		BlockPos pos = ripe;
		npc.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 1.0);
		if (npc.nearPosition(pos) || walkTicks++ > 400) {
			if (walkTicks > 400) {
				pos = NoppesUtilServer.GetClosePos(pos, npc.world);
				npc.setPositionAndUpdate(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			}
			ripe = null;
			npc.getNavigator().clearPath();
			ticks = 90;
			walkTicks = 0;
			npc.swingArm(EnumHand.MAIN_HAND);
			IBlockState state = npc.world.getBlockState(pos);
			Block b = state.getBlock();
			if (b instanceof BlockCrops && ((BlockCrops) b).isMaxAge(state)) {
				BlockCrops crop = (BlockCrops) b;
				npc.world.setBlockState(pos, crop.withAge(0));
				holding = new ItemStack(NpcBlockHelper.getCrop((BlockCrops) b));
			}
			if (b instanceof BlockStem) {
				state = b.getActualState(state, npc.world, pos);
				EnumFacing facing = state.getValue(BlockStem.FACING);
				if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
					return;
				}
				pos = pos.add(facing.getDirectionVec());
				b = npc.world.getBlockState(pos).getBlock();
				npc.world.setBlockToAir(pos);
				if (b != Blocks.AIR) {
					holding = new ItemStack(b);
				}
			}
			setHolding(holding);
		}
	}

	@Override
	public void processed(List<BlockData> list) {
		trackedBlocks.clear();
		chest = null;
		for (BlockData data : list) {
			Block b = data.state.getBlock();
			if (b instanceof BlockChest) {
				if (chest != null && npc.getDistanceSq(chest) <= npc.getDistanceSq(data.pos)) {
					continue;
				}
				chest = data.pos;
			} else {
				if (!(b instanceof BlockCrops) && !(b instanceof BlockStem)) {
					continue;
				}
				if (trackedBlocks.contains(data.pos)) {
					continue;
				}
				trackedBlocks.add(data.pos);
			}
		}
		waitingForBlocks = false;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		type = JobType.FARMER;
		chestMode = compound.getInteger("JobChestMode");
		holding = new ItemStack(compound.getCompoundTag("JobHolding"));
		blockTicks = 1100;
		if (compound.hasKey("Range", 3)) { setRange(compound.getInteger("Range")); }
	}

	public void setHolding(ItemStack item) {
		holding = item;
		npc.setJobData(itemToString(holding));
	}

	@Override
	public void setRange(int dist) {
		if (dist < 2) { dist = 2; }
		if (dist > 32) { dist = 32; }
		range = dist;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", JobType.FARMER.get());
		compound.setInteger("JobChestMode", chestMode);
		if (!holding.isEmpty()) {
			compound.setTag("JobHolding", holding.writeToNBT(new NBTTagCompound()));
		}
		compound.setInteger("Range", range);
		return compound;
	}

}
