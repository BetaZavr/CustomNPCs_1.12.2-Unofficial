package noppes.npcs.blocks;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.blocks.tiles.CustomTileEntityChest;
import noppes.npcs.constants.EnumGuiType;

public class CustomChest extends BlockInterface implements ICustomElement {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	protected static final AxisAlignedBB CHEST_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.875D,
			0.9375D);
	public NBTTagCompound nbtData = new NBTTagCompound();
	private AxisAlignedBB FULL_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

	public boolean isChest = false;

	public CustomChest(Material material, NBTTagCompound nbtBlock) {
		super(material);
		this.nbtData = nbtBlock;
		this.setName("custom_" + nbtBlock.getString("RegistryName"));
		this.hasTileEntity = true;
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
		if (nbtBlock.hasKey("IsChest", 1)) {
			this.isChest = this.nbtData.getBoolean("IsChest");
		}

		this.setAABB(nbtBlock.getTagList("AABB", 6));

		this.setCreativeTab(CustomRegisters.tabBlocks);
	}

	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tile = worldIn.getTileEntity(pos);
		InventoryHelper.dropInventoryItems(worldIn, pos, (CustomTileEntityChest) tile);
		worldIn.updateComparatorOutputLevel(pos, this);
		super.breakBlock(worldIn, pos, state);
	}

	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { CustomChest.FACING });
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new CustomTileEntityChest();
	}

	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		if (this.isChest) {
			return CHEST_AABB;
		}
		return this.FULL_AABB;
	}

	@Override
	public String getCustomName() {
		return this.nbtData.getString("RegistryName");
	}

	@Override
	public INbt getCustomNbt() {
		return NpcAPI.Instance().getINbt(this.nbtData);
	}

	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing) state.getValue(CustomChest.FACING)).getIndex();
	}

	public EnumBlockRenderType getRenderType(IBlockState state) {
		if (this.isChest) {
			return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
		}
		return EnumBlockRenderType.MODEL;
	}

	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getFront(meta);
		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}
		return this.getDefaultState().withProperty(CustomChest.FACING, enumfacing);
	}

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		if (this.nbtData != null && this.nbtData.hasKey("ShowInCreative", 1)
				&& !this.nbtData.getBoolean("ShowInCreative")) {
			return;
		}
		items.add(new ItemStack(this));
	}

	@SideOnly(Side.CLIENT)
	public boolean hasCustomBreakingProgress(IBlockState state) {
		return true;
	}

	public boolean isFullCube(IBlockState state) {
		return false;
	}

	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote || !(playerIn instanceof EntityPlayerMP)) {
			return true;
		}
		if (this.isChest
				&& worldIn.getBlockState(pos.up()).doesSideBlockChestOpening(worldIn, pos.up(), EnumFacing.DOWN)) {
			return true;
		}
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof CustomTileEntityChest) {
			if (((CustomTileEntityChest) tile).isLocked()) {
				boolean isOwner = false;
				ITextComponent message = new TextComponentTranslation("container.isLocked",
						new Object[] { "" + ((char) 167) + "r" + ((CustomTileEntityChest) tile).getName() });
				message.getStyle().setColor(TextFormatting.RED);
				if (!((CustomTileEntityChest) tile).getLockCode().isEmpty()) {
					String locked = ((CustomTileEntityChest) tile).getLockCode().getLock();
					isOwner = locked.indexOf(playerIn.getName()) != -1;
					ITextComponent added = new TextComponentString(" ");
					added.getStyle().setColor(TextFormatting.GRAY);
					added.appendSibling(new TextComponentTranslation("companion.owner"));
					ITextComponent names = new TextComponentString(": " + locked);
					names.getStyle().setColor(TextFormatting.RESET);
					added.appendSibling(names);
					message.appendSibling(added);
				}
				if (!isOwner) {
					playerIn.sendMessage(message);
					if (playerIn instanceof EntityPlayerMP) {
						((EntityPlayerMP) playerIn).connection
								.sendPacket(new SPacketSoundEffect(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS,
										pos.getX(), pos.getY(), pos.getZ(), 1.0F, 1.0F));
					}
					if (!playerIn.isCreative()) {
						return true;
					} else {
						playerIn.sendMessage(new TextComponentTranslation("gui.allowed"));
					}
				}
			}
			if (this.nbtData.hasKey("GUIColor", 3)) {
				((CustomTileEntityChest) tile).guiColor = this.nbtData.getInteger("GUIColor");
			}
			if (this.nbtData.hasKey("GUIColor", 11)) {
				((CustomTileEntityChest) tile).guiColor = -1;
				((CustomTileEntityChest) tile).guiColorArr = this.nbtData.getIntArray("GUIColor");
			}
			CustomNpcs.proxy.fixTileEntityData(tile);
			NoppesUtilServer.sendOpenGui(playerIn, EnumGuiType.CustomContainer, null, pos.getX(), pos.getY(),
					pos.getZ());
		}
		return true;
	}

	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		if (worldIn.isRemote || CustomChest.FACING == null) {
			return;
		}
		IBlockState iblockstate = worldIn.getBlockState(pos.north());
		IBlockState iblockstate1 = worldIn.getBlockState(pos.south());
		IBlockState iblockstate2 = worldIn.getBlockState(pos.west());
		IBlockState iblockstate3 = worldIn.getBlockState(pos.east());
		EnumFacing enumfacing = (EnumFacing) state.getValue(CustomChest.FACING);
		if (enumfacing == EnumFacing.NORTH && iblockstate.isFullBlock() && !iblockstate1.isFullBlock()) {
			enumfacing = EnumFacing.SOUTH;
		} else if (enumfacing == EnumFacing.SOUTH && iblockstate1.isFullBlock() && !iblockstate.isFullBlock()) {
			enumfacing = EnumFacing.NORTH;
		} else if (enumfacing == EnumFacing.WEST && iblockstate2.isFullBlock() && !iblockstate3.isFullBlock()) {
			enumfacing = EnumFacing.EAST;
		} else if (enumfacing == EnumFacing.EAST && iblockstate3.isFullBlock() && !iblockstate2.isFullBlock()) {
			enumfacing = EnumFacing.WEST;
		}
		worldIn.setBlockState(pos, state.withProperty(CustomChest.FACING, enumfacing), 2);
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof CustomTileEntityChest) {
			((CustomTileEntityChest) tile).setBlock(this);
		}
	}

	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		worldIn.setBlockState(pos, state.withProperty(CustomChest.FACING, placer.getHorizontalFacing().getOpposite()),
				2);
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof CustomTileEntityChest) {
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("BlockEntityTag")) {
				((CustomTileEntityChest) tile).readFromNBT(stack.getTagCompound().getCompoundTag("BlockEntityTag"));
			} else {
				((CustomTileEntityChest) tile).setBlock(this);
			}
		}
	}

	private void setAABB(NBTTagList tagList) {
		double[] v = new double[] { 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D };
		for (int i = 0; i < 6; i++) {
			double s = i < 3 ? 0.0d : 1.0d;
			if (i < tagList.tagCount()) {
				s = tagList.getDoubleAt(i);
			}
			v[i] = s;
		}
		this.FULL_AABB = new AxisAlignedBB(v[0], v[1], v[2], v[3], v[4], v[5]);
	}

	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(CustomChest.FACING)));
	}

	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(CustomChest.FACING, rot.rotate((EnumFacing) state.getValue(CustomChest.FACING)));
	}

}
