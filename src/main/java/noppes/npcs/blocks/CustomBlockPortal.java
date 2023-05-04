package noppes.npcs.blocks;

import java.util.Random;

import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.material.Material;
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
import net.minecraft.util.ResourceLocation;
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
import noppes.npcs.util.AdditionalMethods;

public class CustomBlockPortal
extends BlockEndPortal
implements ICustomElement {
	
	public NBTTagCompound nbtData = new NBTTagCompound();
	public static AxisAlignedBB PORTAL_AABB_0 = new AxisAlignedBB(0.0D, 0.25D, 0.0D, 1.0D, 0.75D, 1.0D);
	public static AxisAlignedBB PORTAL_AABB_1 = new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 1.0D, 0.75D);
	public static AxisAlignedBB PORTAL_AABB_2 = new AxisAlignedBB(0.25D, 0.0D, 0.0D, 0.75D, 1.0D, 1.0D);

	public CustomBlockPortal(Material material, NBTTagCompound nbtBlock) {
		super(material);
		this.nbtData = nbtBlock;
		String name = "custom_"+nbtBlock.getString("RegistryName");
		this.setRegistryName(CustomNpcs.MODID, name.toLowerCase());
		this.setUnlocalizedName(name.toLowerCase());	
		
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
		if (this.nbtData.hasKey("DimentionID", 3)) { homeId = this.nbtData.getInteger("HomeDimentionID"); }
		if (this.nbtData.hasKey("HomeDimentionID", 3)) { homeId = this.nbtData.getInteger("HomeDimentionID"); }
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof CustomTileEntityPortal) {
			if (DimensionManager.isDimensionRegistered(((CustomTileEntityPortal) tile).dimensionId)) { id = ((CustomTileEntityPortal) tile).dimensionId; }
			if (DimensionManager.isDimensionRegistered(((CustomTileEntityPortal) tile).homeDimensionId)) { homeId = ((CustomTileEntityPortal) tile).homeDimensionId; }
		}
		if (worldIn.provider.getDimension()==id) { homeId=0; }
		if (!DimensionManager.isDimensionRegistered(id)) { id = 0; }
		if (!DimensionManager.isDimensionRegistered(homeId)) { homeId = 0; }
		
		if (!worldIn.isRemote && !entityIn.isRiding() && !entityIn.isBeingRidden() && entityIn.isNonBoss() && entityIn.getEntityBoundingBox().intersects(state.getBoundingBox(worldIn, pos).offset(pos))) {
			
			WorldServer world = entityIn.getServer().getWorld(id);
			BlockPos coords = world.getSpawnCoordinate();
			double x = 0, y = 70, z = 0;
			if (coords == null) {
				coords = world.getSpawnPoint();
				if (!world.isAirBlock(coords)) {
					coords = world.getTopSolidOrLiquidBlock(coords);
				} else {
					while (world.isAirBlock(coords) && coords.getY() > 0) {
						coords = coords.down();
					}
					if (coords.getY() == 0) {
						coords = world.getTopSolidOrLiquidBlock(coords);
					}
				}
				x = coords.getX();
				y = coords.getY();
				z = coords.getZ();
			}
			if (entityIn instanceof EntityPlayerMP) {
				NoppesUtilPlayer.teleportPlayer((EntityPlayerMP) entityIn, x, y, z, id);
			} else {
				entityIn = AdditionalMethods.travelEntity(CustomNpcs.Server, entityIn, id);
				entityIn.setPosition(x, y, z);
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
		TileEntity tile = source.getTileEntity(pos);
		if (tile instanceof CustomTileEntityPortal) {
			if (((CustomTileEntityPortal) tile).type==1) { return PORTAL_AABB_1; }
			if (((CustomTileEntityPortal) tile).type==2) { return PORTAL_AABB_2; }
		}
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
			((CustomTileEntityPortal) tile).type = placer.rotationPitch<-45 || placer.rotationPitch>45 ? 0 : 1;
			if (((CustomTileEntityPortal) tile).type==1) {
				if (placer.getHorizontalFacing()==EnumFacing.EAST || placer.getHorizontalFacing()==EnumFacing.WEST) { ((CustomTileEntityPortal) tile).type = 2; }
				else { ((CustomTileEntityPortal) tile).type = 1; }
			}
			((CustomTileEntityPortal) tile).SKY_TEXTURE = new ResourceLocation(CustomNpcs.MODID, "textures/environment/custom_"+this.getCustomName()+"_sky.png");
			((CustomTileEntityPortal) tile).PORTAL_TEXTURE = new ResourceLocation(CustomNpcs.MODID, "textures/environment/custom_"+this.getCustomName()+"_portal.png");
			if (this.nbtData.hasKey("RenderData", 10) && this.nbtData.getCompoundTag("RenderData").hasKey("SecondSpeed", 5)) {
				((CustomTileEntityPortal) tile).speed = this.nbtData.getCompoundTag("RenderData").getFloat("SecondSpeed");
				if (((CustomTileEntityPortal) tile).speed<10.0f) { ((CustomTileEntityPortal) tile).speed = 10.0f; }
				else if (((CustomTileEntityPortal) tile).speed>10000.0f) { ((CustomTileEntityPortal) tile).speed = 10000.0f; }
			}
		}
		super.onBlockPlacedBy(world, pos, state, placer, stack);
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(Item.getItemFromBlock(this), 1, 0);
	}
	
}
