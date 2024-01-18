package noppes.npcs.blocks;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomRegisters;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;
import noppes.npcs.util.ObfuscationHelper;

public class CustomBlock
extends BlockInterface
implements IPermission, ICustomElement {

	public NBTTagCompound nbtData = new NBTTagCompound();
	public AxisAlignedBB FULL_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	public AxisAlignedBB EAST_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	public AxisAlignedBB SOUTH_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	public AxisAlignedBB WEST_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	private EnumBlockRenderType renderType = EnumBlockRenderType.MODEL;
	public PropertyDirection FACING;
	public PropertyInteger INT;
	public PropertyBool BO;

	public CustomBlock(Material material, NBTTagCompound nbtBlock) {
		super(material);
		this.nbtData = nbtBlock;
		this.setName("custom_"+nbtBlock.getString("RegistryName"));
		
		this.enableStats = true;
		this.blockSoundType = SoundType.STONE;
		this.blockParticleGravity = 1.0F;
		this.lightOpacity = this.fullBlock ? 255 : 0;
		this.translucent = !this.blockMaterial.blocksLight();
		this.setHardness(1.5f);
		this.setResistance(10.0f);
		if (nbtBlock.hasKey("Hardness", 5)) { this.setHardness(nbtBlock.getFloat("Hardness")); }
		if (nbtBlock.hasKey("Resistance", 5)) { this.setResistance(nbtBlock.getFloat("Resistance")); }
		if (nbtBlock.hasKey("LightLevel", 5)) { this.setLightLevel(nbtBlock.getFloat("LightLevel")); }
		if (nbtBlock.hasKey("BlockRenderType", 8)) { this.renderType = CustomBlock.getNbtRenderType(nbtBlock.getString("BlockRenderType"));
		this.setSoundType(CustomBlock.getNbtSoundType(nbtBlock.getString("SoundType")));
		this.setAABB(nbtBlock.getTagList("AABB", 6));
		 }

		this.BO = null;
		this.INT = null;
		this.FACING = null;
		if (nbtBlock.hasKey("Property", 10)) {
			ObfuscationHelper.setValue(Block.class, this, this.createBlockState(), BlockStateContainer.class);
			NBTTagCompound nbtProperty = nbtBlock.getCompoundTag("Property");
			switch(nbtProperty.getByte("Type")) {
				case (byte) 1: {
					this.setDefaultState(this.blockState.getBaseState().withProperty(this.BO, false));
					break;
				}
				case (byte) 3: {
					this.setDefaultState(this.blockState.getBaseState().withProperty(this.INT, 0));
					break;
				}
				case (byte) 4: {
					this.setDefaultState(this.blockState.getBaseState().withProperty(this.FACING, EnumFacing.NORTH));
					break;
				}
			}
		}

		this.setCreativeTab(CustomRegisters.tabBlocks);
	}

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		if (this.nbtData!=null && this.nbtData.hasKey("ShowInCreative", 1) && !this.nbtData.getBoolean("ShowInCreative")) { return; }
		items.add(new ItemStack(this));
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		if (worldIn.isRemote || this.FACING==null) { return; }
		IBlockState iblockstate = worldIn.getBlockState(pos.north());
		IBlockState iblockstate1 = worldIn.getBlockState(pos.south());
		IBlockState iblockstate2 = worldIn.getBlockState(pos.west());
		IBlockState iblockstate3 = worldIn.getBlockState(pos.east());
		EnumFacing enumfacing = (EnumFacing) state.getValue(this.FACING);
		if (enumfacing == EnumFacing.NORTH && iblockstate.isFullBlock() && !iblockstate1.isFullBlock()) { enumfacing = EnumFacing.SOUTH; }
		else if (enumfacing == EnumFacing.SOUTH && iblockstate1.isFullBlock() && !iblockstate.isFullBlock()) { enumfacing = EnumFacing.NORTH; }
		else if (enumfacing == EnumFacing.WEST && iblockstate2.isFullBlock() && !iblockstate3.isFullBlock()) { enumfacing = EnumFacing.EAST; }
		else if (enumfacing == EnumFacing.EAST && iblockstate3.isFullBlock() && !iblockstate2.isFullBlock()) { enumfacing = EnumFacing.WEST; }
		worldIn.setBlockState(pos, state.withProperty(this.FACING, enumfacing), 2);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		if (this.FACING!=null) {
			return this.getDefaultState().withProperty(this.FACING, placer.getHorizontalFacing().getOpposite());
		}
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
	}
	
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (this.FACING!=null) {
			worldIn.setBlockState(pos, state.withProperty(this.FACING, placer.getHorizontalFacing().getOpposite()), 2);
		}
	}

	public static EnumBlockRenderType getNbtRenderType(String name) {
		while(name.indexOf(" ")!=-1) { name = name.replace(" ", "_"); }
		switch(name.toLowerCase()) {
			case "invisible": return EnumBlockRenderType.INVISIBLE;
			case "liquid": return EnumBlockRenderType.MODEL;
			case "entityblock_animated": return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
			default: return EnumBlockRenderType.MODEL;
		}
	}

	private void setAABB(NBTTagList tagList) {
		if (tagList==null || tagList.tagCount()==0) { return; }
		double[] v = new double[] { 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D };
		for (int i=0; i<6; i++) {
			double s = i<3 ? 0.0d : 1.0d;
			if (i < tagList.tagCount()) { s = tagList.getDoubleAt(i); }
			v[i] = s;
		}
		this.FULL_BLOCK_AABB = new AxisAlignedBB(v[0], v[1], v[2], v[3], v[4], v[5]);
		this.WEST_BLOCK_AABB = new AxisAlignedBB(v[2], v[1], 1 - v[3], v[5], v[4], 1 - v[0]);
		this.SOUTH_BLOCK_AABB = new AxisAlignedBB(1 - v[3], v[1], 1 - v[5], 1 - v[0], v[4], 1 - v[2]);
		this.EAST_BLOCK_AABB = new AxisAlignedBB(1 - v[5], v[1], v[0], 1 - v[2], v[4], v[3]);
	}
	
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		if (this.nbtData!=null && this.nbtData.getBoolean("IsPassable")) { return NULL_AABB; }
		if (this.FACING!=null) {
			EnumFacing v = blockState.getValue(this.FACING);
			if (v == EnumFacing.NORTH) { return FULL_BLOCK_AABB; }
			else if (v == EnumFacing.EAST) { return EAST_BLOCK_AABB; }
			else if (v == EnumFacing.SOUTH) { return SOUTH_BLOCK_AABB; }
			else if (v == EnumFacing.WEST) { return WEST_BLOCK_AABB; }
		}
		return blockState.getBoundingBox(worldIn, pos);
	}

	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		if (this.nbtData==null || !this.nbtData.hasKey("IsPassable", 3)) { return !this.blockMaterial.blocksMovement(); }
		return this.nbtData.getBoolean("IsPassable");
	}

	public static SoundType getNbtSoundType(String soundName) {
		switch(soundName.toLowerCase()) {
			case "wood": return SoundType.WOOD;
			case "ground": return SoundType.GROUND;
			case "plant": return SoundType.PLANT;
			case "metal": return SoundType.METAL;
			case "glass": return SoundType.GLASS;
			case "cloth": return SoundType.CLOTH;
			case "sand": return SoundType.SAND;
			case "snow": return SoundType.SNOW;
			case "ladder": return SoundType.LADDER;
			case "anvil": return SoundType.ANVIL;
			case "slime": return SoundType.SLIME;
			default: return SoundType.STONE;
		}
	}

	@Override
	public boolean hasTileEntity() {
		return false;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return null;
	}
	
	@Override
	public boolean isAllowed(EnumPacketServer e) {
		return true;
	}

	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
		return this.nbtData.getBoolean("IsLadder");
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return this.nbtData==null || !this.nbtData.hasKey("IsOpaqueCube") ? true : this.nbtData.getBoolean("IsOpaqueCube");
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return this.nbtData==null || !this.nbtData.hasKey("IsFullCube") ? true : this.nbtData.getBoolean("IsFullCube");
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) { return this.renderType; }

	public boolean hasProperty() { return this.BO!=null || this.INT!=null || this.FACING!=null; }

	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = this.getDefaultState();
		if (this.FACING!=null) {
			EnumFacing enumfacing = EnumFacing.getFront(meta);
			if (enumfacing.getAxis() == EnumFacing.Axis.Y) { enumfacing = EnumFacing.NORTH; }
			return state.withProperty(this.FACING, enumfacing);
		}
		if (this.INT!=null) { return state.withProperty(this.INT, meta); }
		if (this.BO!=null) { return state.withProperty(this.BO, meta!=0); }
		return state;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		if (this.FACING!=null) { return ((EnumFacing)state.getValue(this.FACING)).getIndex(); }
		return super.getMetaFromState(state);
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		if (this.FACING!=null) { return state.withProperty(this.FACING, rot.rotate((EnumFacing)state.getValue(FACING))); }
		return super.withRotation(state, rot);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		if (this.FACING!=null) { return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(this.FACING))); }
		return super.withMirror(state, mirrorIn);
	}

	protected BlockStateContainer createBlockState() {
		if (this.nbtData!=null && this.nbtData.hasKey("Property", 10)) {
			NBTTagCompound nbtProperty = this.nbtData.getCompoundTag("Property");
			switch(nbtProperty.getByte("Type")) {
				case (byte) 1: {
					this.BO = PropertyBool.create(nbtProperty.getString("Name"));
					return new BlockStateContainer(this, new IProperty[] {this.BO});
				}
				case (byte) 3: {
					this.INT = PropertyInteger.create(nbtProperty.getString("Name"), nbtProperty.getInteger("Min"), nbtProperty.getInteger("Max"));
					return new BlockStateContainer(this, new IProperty[] {this.INT});
				}
				case (byte) 4: {
					this.FACING = PropertyDirection.create(nbtProperty.getString("Name"), EnumFacing.Plane.HORIZONTAL);
					return new BlockStateContainer(this, new IProperty[] {this.FACING});
				}
			}
		}
		return new BlockStateContainer(this, new IProperty[0]);
		
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		if (this.FACING!=null) {
			EnumFacing v = state.getValue(this.FACING);
			if (v == EnumFacing.EAST) { return EAST_BLOCK_AABB; }
			else if (v == EnumFacing.SOUTH) { return SOUTH_BLOCK_AABB; }
			else if (v == EnumFacing.WEST) { return WEST_BLOCK_AABB; }
		}
		return this.FULL_BLOCK_AABB;
	}


	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		String name = "";
		if (this.nbtData!=null && this.nbtData.hasKey("BlockLayer", 8)) { name = this.nbtData.getString("BlockLayer"); }
		while(name.indexOf(" ")!=-1) { name = name.replace(" ", "_"); }
		switch(name.toLowerCase()) {
			case "cutout": return BlockRenderLayer.CUTOUT;
			case "cutout_mipped": return BlockRenderLayer.CUTOUT_MIPPED;
			case "translucent": return BlockRenderLayer.TRANSLUCENT;
			default: return BlockRenderLayer.SOLID;
		}
	}

	@Override
	public INbt getCustomNbt() { return NpcAPI.Instance().getINbt(this.nbtData); }

	@Override
	public String getCustomName() { return this.nbtData.getString("RegistryName"); }
	
}
