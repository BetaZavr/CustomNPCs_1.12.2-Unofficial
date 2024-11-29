package noppes.npcs.api.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.ILayerModel;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.block.IBlockScripted;
import noppes.npcs.api.block.ITextPlane;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.LayerModel;

public class BlockScriptedWrapper
		extends BlockWrapper
		implements IBlockScripted {

	private TileScripted tile;

	public BlockScriptedWrapper(World world, Block block, BlockPos pos) {
		super(world, block, pos);
		this.tile = (TileScripted) super.tile;
	}

	@Override
	public ILayerModel createLayerModel() {
		ILayerModel[] ls = new ILayerModel[this.tile.layers.length + 1];
		int i;
		for (i = 0; i < this.tile.layers.length; i++) {
			((LayerModel) this.tile.layers[i]).pos = i;
			ls[i] = this.tile.layers[i];
		}
		ls[i] = new LayerModel(i);
		this.tile.layers = ls;
		return this.tile.layers[i];
	}

	@Override
	public String executeCommand(String command) {
		if (!Objects.requireNonNull(this.tile.getWorld().getMinecraftServer()).isCommandBlockEnabled()) {
			throw new CustomNPCsException("Command blocks need to be enabled to executeCommands");
		}
		FakePlayer player = EntityNPCInterface.CommandPlayer;
		player.setWorld(this.tile.getWorld());
		player.setPosition(this.getX(), this.getY(), this.getZ());
		return NoppesUtilServer.runCommand(this.tile.getWorld(), this.tile.getPos(),
				"ScriptBlock: " + this.tile.getPos(), command, null, player);
	}

	@Override
	public float getHardness() {
		return this.tile.blockHardness;
	}

	@Override
	public boolean getIsLadder() {
		return this.tile.isLadder;
	}

	@Override
	public boolean getIsPassable() {
		return this.tile.isPassable;
	}

	@Override
	public ILayerModel[] getLayerModels() {
		for (int i = 0; i < this.tile.layers.length; i++) {
			((LayerModel) this.tile.layers[i]).pos = i;
		}
		return this.tile.layers;
	}

	@Override
	public int getLight() {
		return this.tile.lightValue;
	}

	@Override
	public IItemStack getModel() {
		return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(this.tile.itemModel);
	}

	@Override
	public int getRedstonePower() {
		return this.tile.prevPower;
	}

	@Override
	public float getResistance() {
		return this.tile.blockResistance;
	}

	@Override
	public int getRotationX() {
		return this.tile.rotationX;
	}

	@Override
	public int getRotationY() {
		return this.tile.rotationY;
	}

	@Override
	public int getRotationZ() {
		return this.tile.rotationZ;
	}

	@Override
	public float getScaleX() {
		return this.tile.scaleX;
	}

	@Override
	public float getScaleY() {
		return this.tile.scaleY;
	}

	@Override
	public float getScaleZ() {
		return this.tile.scaleZ;
	}

	@Override
	public ITextPlane getTextPlane() {
		return this.tile.text1;
	}

	@Override
	public ITextPlane getTextPlane2() {
		return this.tile.text2;
	}

	@Override
	public ITextPlane getTextPlane3() {
		return this.tile.text3;
	}

	@Override
	public ITextPlane getTextPlane4() {
		return this.tile.text4;
	}

	@Override
	public ITextPlane getTextPlane5() {
		return this.tile.text5;
	}

	@Override
	public ITextPlane getTextPlane6() {
		return this.tile.text6;
	}

	@Override
	public ITimers getTimers() {
		return this.tile.timers;
	}

	@Override
	public boolean removeLayerModel(ILayerModel layer) {
		List<ILayerModel> newLM = new ArrayList<>();
		boolean found = false;
		for (int i = 0; i < this.tile.layers.length; i++) {
			if (this.tile.layers[i] == null) {
				continue;
			}
			if (!this.tile.layers[i].equals(layer)) {
				newLM.add(this.tile.layers[i]);
			} else {
				found = true;
			}
		}
		if (found) {
			this.tile.layers = newLM.toArray(new ILayerModel[0]);
		}
		return found;
	}

	@Override
	public boolean removeLayerModel(int id) {
		if (id < 0 || id >= this.tile.layers.length) {
			return false;
		}
		List<ILayerModel> newLM = new ArrayList<>();
		for (int i = 0; i < this.tile.layers.length; i++) {
			if (this.tile.layers[i] == null) {
				continue;
			}
			if (i != id) {
				newLM.add(this.tile.layers[i]);
			}
		}
		this.tile.layers = newLM.toArray(new ILayerModel[0]);
		return true;
	}

	@Override
	public void setHardness(float hardness) {
		this.tile.blockHardness = hardness;
	}

	@Override
	public void setIsLadder(boolean bo) {
		this.tile.isLadder = bo;
		this.tile.needsClientUpdate = true;
	}

	@Override
	public void setIsPassible(boolean passable) {
		this.tile.isPassable = passable;
		this.tile.needsClientUpdate = true;
	}

	@Override
	public void setLight(int value) {
		this.tile.setLightValue(value);
	}

	@Override
	public void setModel(IBlock iblock) {
		if (iblock == null || iblock.getMCBlock() == null || iblock.getWorld() == null) {
			this.tile.setItemModel(null, null);
		} else {
			this.setModel(Objects.requireNonNull(iblock.getMCBlock().getRegistryName()).toString(), iblock.getMetadata());
		}
	}

	@Override
	public void setModel(IItemStack item) {
		if (item == null) {
			this.tile.setItemModel(null, null);
		} else {
			this.tile.setItemModel(item.getMCItemStack(), Block.getBlockFromItem(item.getMCItemStack().getItem()));
		}
	}

	@Override
	public void setModel(String name) {
		if (name == null || name.isEmpty()) {
			this.tile.setItemModel(null, null);
		} else {
			ResourceLocation loc = new ResourceLocation(name);
			Block block = Block.REGISTRY.getObject(loc);
			this.tile.setItemModel(new ItemStack(Objects.requireNonNull(Item.REGISTRY.getObject(loc))), block);
		}
	}

	@Override
	public void setModel(String blockName, int meta) {
		if (blockName == null || meta < 0) {
			this.tile.setItemModel(null, null);
		} else {
			ResourceLocation loc = new ResourceLocation(blockName);
			Block block = Block.REGISTRY.getObject(loc);
			ItemStack stack = new ItemStack(Item.getItemFromBlock(block));
			if (stack.isEmpty()) {
				stack = new ItemStack(Objects.requireNonNull(Item.getByNameOrId(blockName)));
			}
			try {
				@SuppressWarnings("deprecation")
				IBlockState state = block.getStateFromMeta(meta);
                block = state.getBlock();
            } catch (Exception e) {
				meta = 0;
			}
			this.tile.setItemModel(stack, block, meta);
		}
	}

	@Override
	public void setRedstonePower(int strength) {
		this.tile.setRedstonePower(strength);
	}

	@Override
	public void setResistance(float resistance) {
		this.tile.blockResistance = resistance;
	}

	@Override
	public void setRotation(int x, int y, int z) {
		this.tile.setRotation(x % 360, y % 360, z % 360);
	}

	@Override
	public void setScale(float x, float y, float z) {
		this.tile.setScale(x, y, z);
	}

	@Override
	public void setTile(TileEntity tile) {
		this.tile = (TileScripted) tile;
		super.setTile(tile);
	}

	@Override
	public void trigger(int id, Object... arguments) {
		EventHooks.onScriptTriggerEvent(this.tile, id, this.getWorld(), this.getPos(), null, arguments);
	}

	@Override
	public void updateModel() {
		this.tile.needsClientUpdate = true;
	}

}
