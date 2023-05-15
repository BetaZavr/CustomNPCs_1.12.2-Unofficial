package noppes.npcs.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.blocks.tiles.CustomTileEntityPortal;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.util.AdditionalMethods;

public class CustomBlockPortal
extends BlockEndPortal
implements ICustomElement {
	
	public NBTTagCompound nbtData = new NBTTagCompound();
	public static AxisAlignedBB PORTAL_AABB_0 = new AxisAlignedBB(0.0D, 0.26D, 0.0D, 1.0D, 0.74D, 1.0D);
	public static AxisAlignedBB PORTAL_AABB_1 = new AxisAlignedBB(0.0D, 0.0D, 0.26D, 1.0D, 1.0D, 0.74D);
	public static AxisAlignedBB PORTAL_AABB_2 = new AxisAlignedBB(0.26D, 0.0D, 0.0D, 0.74D, 1.0D, 1.0D);
	public static PropertyInteger TYPE = PropertyInteger.create("type", 0, 2);

	public CustomBlockPortal(Material material, NBTTagCompound nbtBlock) {
		super(material);
		this.nbtData = nbtBlock;
		String name = "custom_"+nbtBlock.getString("RegistryName");
		this.setRegistryName(CustomNpcs.MODID, name.toLowerCase());
		this.setUnlocalizedName(name.toLowerCase());
		this.setDefaultState(this.blockState.getBaseState().withProperty(CustomBlockPortal.TYPE, 0));
		
		this.enableStats = true;
		this.blockParticleGravity = 1.0F;
		this.lightOpacity = this.fullBlock ? 255 : 0;
		this.translucent = !this.blockMaterial.blocksLight();
		
		this.setHardness(-1.0F);
		this.setResistance(6000000.0F);
		if (nbtBlock.hasKey("LightLevel", 5)) { this.setLightLevel(nbtBlock.getFloat("LightLevel")); }
		
		this.setCreativeTab((CreativeTabs) CustomItems.tabBlocks);
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		int id = 0, homeId = 0;
		if (this.nbtData.hasKey("DimentionID", 3)) { id = this.nbtData.getInteger("DimentionID"); }
		if (this.nbtData.hasKey("HomeDimentionID", 3)) { homeId = this.nbtData.getInteger("HomeDimentionID"); }
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof CustomTileEntityPortal) {
			if (DimensionManager.isDimensionRegistered(((CustomTileEntityPortal) tile).dimensionId)) { id = ((CustomTileEntityPortal) tile).dimensionId; }
			if (DimensionManager.isDimensionRegistered(((CustomTileEntityPortal) tile).homeDimensionId)) { homeId = ((CustomTileEntityPortal) tile).homeDimensionId; }
		}
		if (!DimensionManager.isDimensionRegistered(id)) { id = 0; }
		if (!DimensionManager.isDimensionRegistered(homeId)) { homeId = 0; }
		if (!worldIn.isRemote && !entityIn.isRiding() && !entityIn.isBeingRidden() && entityIn.isNonBoss() && entityIn.getEntityBoundingBox().intersects(state.getBoundingBox(worldIn, pos).offset(pos))) {
			boolean isHome = worldIn.provider.getDimension()==id;
			BlockPos p = null;
			if (tile instanceof CustomTileEntityPortal) { p = ((CustomTileEntityPortal) tile).getPosTp(isHome); }
			if (p == null) {
				WorldServer world = entityIn.getServer().getWorld(isHome ? homeId : id);
				p = world.getSpawnCoordinate();
				if (p == null) { p = world.getSpawnPoint(); }
				if (p != null) {
					if (!world.isAirBlock(p)) { p = world.getTopSolidOrLiquidBlock(p); }
					else if (!world.isAirBlock(p.up())) {
						while (world.isAirBlock(p) && p.getY() > 0) { p = p.down(); }
						if (p.getY() == 0) { p = world.getTopSolidOrLiquidBlock(p); }
					}
				}
			}
			if (p == null) { return; }
			if (entityIn instanceof EntityPlayerMP) {
				NoppesUtilPlayer.teleportPlayer((EntityPlayerMP) entityIn, p.getX()+0.5d, p.getY(), p.getZ()+0.5d, isHome ? homeId : id);
			} else {
				entityIn = AdditionalMethods.travelEntity(CustomNpcs.Server, entityIn, isHome ? homeId : id);
				entityIn.setPosition(p.getX()+0.5d, p.getY(), p.getZ()+0.5d);
			}
		}
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) { return false; }

	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (this.nbtData.hasKey("RenderData", 10) && Math.random()<0.25f) {
			double d0 = (double)((float)pos.getX() + rand.nextFloat());
			double d1 = (double)((float)pos.getY() + 0.8F);
			double d2 = (double)((float)pos.getZ() + rand.nextFloat());
			EnumParticleTypes p = EnumParticleTypes.CRIT;
			for (EnumParticleTypes ept : EnumParticleTypes.values()) {
				if (ept.name().equalsIgnoreCase(this.nbtData.getCompoundTag("RenderData").getString("SpawnParticle"))) {
					p = ept;
					break;
				}
			}
			worldIn.spawnParticle(p, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}
	
	@Override
	public INbt getCustomNbt() { return NpcAPI.Instance().getINbt(this.nbtData); }

	@Override
	public String getCustomName() { return this.nbtData.getString("RegistryName"); }

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		int meta = this.getMetaFromState(state);
		if (meta==1) { return PORTAL_AABB_1; }
		if (meta==2) { return PORTAL_AABB_2; }
		return PORTAL_AABB_0;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) { return new CustomTileEntityPortal(); }

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return side == EnumFacing.DOWN ? super.shouldSideBeRendered(blockState, blockAccess, pos, side) : false;
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof CustomTileEntityPortal && placer!=null) {
			int type = placer.rotationPitch<-45 || placer.rotationPitch>45 ? 0 : 1;
			if (type==1 && (placer.getHorizontalFacing()==EnumFacing.EAST || placer.getHorizontalFacing()==EnumFacing.WEST)) { type = 2; }
			world.setBlockState(pos, state.withProperty(CustomBlockPortal.TYPE, type));
			if (this.nbtData.hasKey("RenderData", 10) && this.nbtData.getCompoundTag("RenderData").hasKey("SecondSpeed", 5)) {
				NBTTagCompound nbtRender = this.nbtData.getCompoundTag("RenderData");
				if (nbtRender.hasKey("SecondSpeed", 5)) {
					((CustomTileEntityPortal) tile).speed = nbtRender.getFloat("SecondSpeed");
					if (((CustomTileEntityPortal) tile).speed<10.0f) { ((CustomTileEntityPortal) tile).speed = 10.0f; }
					else if (((CustomTileEntityPortal) tile).speed>10000.0f) { ((CustomTileEntityPortal) tile).speed = 10000.0f; }
				}
				if (nbtRender.hasKey("Transparency", 5)) {
					((CustomTileEntityPortal) tile).alpha = nbtRender.getFloat("Transparency");
					if (((CustomTileEntityPortal) tile).alpha<0.15f) { ((CustomTileEntityPortal) tile).alpha = 0.15f; }
					else if (((CustomTileEntityPortal) tile).alpha>1.0f) { ((CustomTileEntityPortal) tile).alpha = 1.0f; }
				}
			}
			CustomTileEntityPortal adjacent = this.getTile(world, pos.south());
			if (adjacent == null) {
				for (int i=0; i<26; i++) {
					switch(i) {
						case 0: { adjacent = this.getTile(world, pos.south()); break; }
						case 1: { adjacent = this.getTile(world, pos.north()); break; }
						case 2: { adjacent = this.getTile(world, pos.east()); break; }
						case 3: { adjacent = this.getTile(world, pos.west()); break; }
						case 4: { adjacent = this.getTile(world, pos.up()); break; }
						case 5: { adjacent = this.getTile(world, pos.down()); break; }
						case 6: { adjacent = this.getTile(world, pos.south().east()); break; }
						case 7: { adjacent = this.getTile(world, pos.south().west()); break; }
						case 8: { adjacent = this.getTile(world, pos.north().east()); break; }
						case 9: { adjacent = this.getTile(world, pos.north().west()); break; }
						case 10: { adjacent = this.getTile(world, pos.down().south()); break; }
						case 11: { adjacent = this.getTile(world, pos.down().north()); break; }
						case 12: { adjacent = this.getTile(world, pos.down().east()); break; }
						case 13: { adjacent = this.getTile(world, pos.down().west()); break; }
						case 14: { adjacent = this.getTile(world, pos.down().south().east()); break; }
						case 15: { adjacent = this.getTile(world, pos.down().south().west()); break; }
						case 16: { adjacent = this.getTile(world, pos.down().north().east()); break; }
						case 17: { adjacent = this.getTile(world, pos.down().north().west()); break; }
						case 18: { adjacent = this.getTile(world, pos.up().south()); break; }
						case 19: { adjacent = this.getTile(world, pos.up().north()); break; }
						case 20: { adjacent = this.getTile(world, pos.up().east()); break; }
						case 21: { adjacent = this.getTile(world, pos.up().west()); break; }
						case 22: { adjacent = this.getTile(world, pos.up().south().east()); break; }
						case 23: { adjacent = this.getTile(world, pos.up().south().west()); break; }
						case 24: { adjacent = this.getTile(world, pos.up().north().east()); break; }
						case 25: { adjacent = this.getTile(world, pos.up().north().west()); break; }
					}
					if (adjacent != null) { break; }
				}
			}
			if (adjacent != null) {
				if (adjacent.posTp.getY()>-1) { ((CustomTileEntityPortal) tile).posTp = NpcAPI.Instance().getIPos(adjacent.posTp.getX(), adjacent.posTp.getY(), adjacent.posTp.getZ()); }
				if (adjacent.posHomeTp.getY()>-1) { ((CustomTileEntityPortal) tile).posHomeTp = NpcAPI.Instance().getIPos(adjacent.posHomeTp.getX(), adjacent.posHomeTp.getY(), adjacent.posHomeTp.getZ()); }
				((CustomTileEntityPortal) tile).dimensionId = adjacent.dimensionId;
				((CustomTileEntityPortal) tile).homeDimensionId = adjacent.homeDimensionId;
				((CustomTileEntityPortal) tile).speed = adjacent.speed;
				((CustomTileEntityPortal) tile).alpha = adjacent.alpha;
			}
		}
		super.onBlockPlacedBy(world, pos, state, placer, stack);
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(Item.getItemFromBlock(this), 1, 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { CustomBlockPortal.TYPE });
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(CustomBlockPortal.TYPE);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(CustomBlockPortal.TYPE, meta % 3);
	}
	
	private CustomTileEntityPortal getTile(World world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		Block block = world.getBlockState(pos).getBlock();
		if (tile instanceof CustomTileEntityPortal && block instanceof CustomBlockPortal && ((CustomBlockPortal) block).getCustomName().equals(this.getCustomName())) {
			return (CustomTileEntityPortal) tile;
		}
		return null;
	}
}
