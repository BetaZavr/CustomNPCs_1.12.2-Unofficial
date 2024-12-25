package noppes.npcs.blocks;

import java.util.Objects;
import java.util.Random;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.EventHooks;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.event.BlockEvent;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.items.ItemNpcBlock;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class CustomDoor extends BlockDoor implements ITileEntityProvider, ICustomElement {

	public NBTTagCompound nbtData;
	private EnumBlockRenderType renderType = EnumBlockRenderType.MODEL;

	public CustomDoor(Material material, NBTTagCompound nbtBlock) {
		super(material);
		this.nbtData = nbtBlock;
		String name = "custom_" + nbtBlock.getString("RegistryName");
		this.setRegistryName(CustomNpcs.MODID, name.toLowerCase());
		this.setUnlocalizedName(name.toLowerCase());
		this.hasTileEntity = false;
		this.setSoundType(CustomBlock.getNbtSoundType(nbtBlock.getString("SoundType")));
		this.setHardness(0.0f);
		this.setResistance(10.0f);

		if (nbtBlock.hasKey("Hardness", 5)) {
			this.setHardness(nbtBlock.getFloat("Hardness"));
		}
		if (nbtBlock.hasKey("Resistance", 5)) {
			this.setResistance(nbtBlock.getFloat("Resistance"));
		}
		if (nbtBlock.hasKey("LightLevel", 5)) {
			this.setLightLevel(nbtBlock.getFloat("LightLevel"));
		}
		if (nbtBlock.hasKey("BlockRenderType", 8)) {
			this.renderType = CustomBlock.getNbtRenderType(nbtBlock.getString("BlockRenderType"));
		}

		this.setCreativeTab(CustomRegisters.tabBlocks);
	}

	public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		worldIn.removeTileEntity(pos);
	}

	public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public @Nonnull BlockRenderLayer getBlockLayer() {
		String name = "";
		if (this.nbtData != null && this.nbtData.hasKey("BlockLayer", 8)) {
			name = this.nbtData.getString("BlockLayer");
		}
		while (name.contains(" ")) {
			name = name.replace(" ", "_");
		}
		switch (name.toLowerCase()) {
		case "cutout":
			return BlockRenderLayer.CUTOUT;
		case "cutout_mipped":
			return BlockRenderLayer.CUTOUT_MIPPED;
		case "translucent":
			return BlockRenderLayer.TRANSLUCENT;
		default:
			return BlockRenderLayer.SOLID;
		}
	}

	@Override
	public String getCustomName() {
		return this.nbtData.getString("RegistryName");
	}

	@Override
	public INbt getCustomNbt() {
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(this.nbtData);
	}

	public @Nonnull ItemStack getItem(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		Item item = new ItemNpcBlock(this);
		for (Item it : Item.REGISTRY) {
			if (!Objects.requireNonNull(it.getRegistryName()).getResourceDomain().equals(CustomNpcs.MODID)) {
				continue;
			}
			if (it.getRegistryName().equals(this.getRegistryName())) {
				item = it;
				break;
			}
		}
		return new ItemStack(item, 1, 0);
	}

	public @Nonnull Item getItemDropped(@Nonnull IBlockState state, @Nonnull Random rand, int fortune) {
		Item item = new ItemNpcBlock(this);
		for (Item it : Item.REGISTRY) {
			if (!Objects.requireNonNull(it.getRegistryName()).getResourceDomain().equals(CustomNpcs.MODID)) {
				continue;
			}
			if (it.getRegistryName().equals(this.getRegistryName())) {
				item = it;
				break;
			}
		}
		return state.getValue(HALF) == EnumDoorHalf.UPPER ? Items.AIR : item;
	}

	@Override
	public @Nonnull EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
		return this.renderType;
	}

	@Override
	public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
		if (tab != CustomRegisters.tabBlocks && tab != CreativeTabs.SEARCH) { return; }
		if (this.nbtData != null && this.nbtData.hasKey("ShowInCreative", 1)
				&& !this.nbtData.getBoolean("ShowInCreative")) {
			return;
		}
		items.add(new ItemStack(this));
		if (tab == CustomRegisters.tabBlocks) { Util.instance.sort(items); }
	}

	public boolean hasTileEntity(@Nonnull IBlockState state) {
		return true;
	}

	@Override
	public boolean isFullCube(@Nonnull IBlockState state) {
		return this.nbtData != null && this.nbtData.hasKey("IsFullCube") && this.nbtData.getBoolean("IsFullCube");
	}

	@Override
	public boolean isLadder(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLivingBase entity) {
		return this.nbtData.getBoolean("IsLadder");
	}

	@Override
	public boolean isOpaqueCube(@Nonnull IBlockState state) {
		return this.nbtData != null && this.nbtData.hasKey("IsOpaqueCube") && this.nbtData.getBoolean("IsOpaqueCube");
	}

	public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (nbtData != null && nbtData.hasKey("InteractOpen") && !nbtData.getBoolean("InteractOpen")) { return false; }

		BlockEvent.DoorToggleEvent event = new BlockEvent.DoorToggleEvent(Objects.requireNonNull(NpcAPI.Instance()).getIBlock(worldIn, pos));
		EventHooks.onEvent(ScriptController.Instance.clientScripts, EnumScriptType.DOOR_TOGGLE, event);
		if (event.isCanceled()) { return false; }

		BlockPos blockpos = state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
		IBlockState iblockstate = pos.equals(blockpos) ? state : worldIn.getBlockState(blockpos);
		if (iblockstate.getBlock() != this) { return false; }

		state = iblockstate.cycleProperty(OPEN);
		worldIn.setBlockState(blockpos, state, 10);
		worldIn.markBlockRangeForRenderUpdate(blockpos, pos);
		worldIn.playEvent(playerIn, state.getValue(OPEN) ? (blockMaterial == Material.IRON ? 1005 : 1006) : (blockMaterial == Material.IRON ? 1011 : 1012), pos, 0);
		return true;
	}

	@Override
	public int getType() {
		if (this.nbtData != null && this.nbtData.hasKey("BlockType", 1)) { return this.nbtData.getByte("BlockType"); }
		return 6;
	}

}
