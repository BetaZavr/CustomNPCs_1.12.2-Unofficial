package noppes.npcs.blocks;

import java.util.Objects;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.material.Material;
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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.event.NpcEvent.CustomNpcTeleport;
import noppes.npcs.api.event.PlayerEvent.CustomTeleport;
import noppes.npcs.blocks.tiles.CustomTileEntityPortal;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.CustomNPCsScheduler;

import javax.annotation.Nonnull;

public class CustomBlockPortal extends BlockEndPortal implements ICustomElement {

	public static AxisAlignedBB PORTAL_AABB_0 = new AxisAlignedBB(0.0D, 0.26D, 0.0D, 1.0D, 0.74D, 1.0D);
	public static AxisAlignedBB PORTAL_AABB_1 = new AxisAlignedBB(0.0D, 0.0D, 0.26D, 1.0D, 1.0D, 0.74D);
	public static AxisAlignedBB PORTAL_AABB_2 = new AxisAlignedBB(0.26D, 0.0D, 0.0D, 0.74D, 1.0D, 1.0D);
	public static PropertyInteger TYPE = PropertyInteger.create("type", 0, 2);
	public NBTTagCompound nbtData;

	public CustomBlockPortal(Material material, NBTTagCompound nbtBlock) {
		super(material);
		this.nbtData = nbtBlock;
		String name = "custom_" + nbtBlock.getString("RegistryName");
		this.setRegistryName(CustomNpcs.MODID, name.toLowerCase());
		this.setUnlocalizedName(name.toLowerCase());
		this.setDefaultState(this.blockState.getBaseState().withProperty(CustomBlockPortal.TYPE, 0));

		this.enableStats = true;
		this.blockParticleGravity = 1.0F;
		this.lightOpacity = this.fullBlock ? 255 : 0;
		this.translucent = !this.blockMaterial.blocksLight();

		this.setHardness(-1.0F);
		this.setResistance(6000000.0F);
		if (nbtBlock.hasKey("LightLevel", 5)) {
			this.setLightLevel(nbtBlock.getFloat("LightLevel"));
		}

		this.setCreativeTab(CustomRegisters.tabBlocks);
	}

	@Override
	public boolean canEntityDestroy(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull Entity entity) {
		return false;
	}

	@Override
	protected @Nonnull BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, CustomBlockPortal.TYPE);
	}

	@Override
	public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
		return new CustomTileEntityPortal();
	}

	@Override
	public @Nonnull AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
		int meta = this.getMetaFromState(state);
		if (meta == 1) {
			return PORTAL_AABB_1;
		}
		if (meta == 2) {
			return PORTAL_AABB_2;
		}
		return PORTAL_AABB_0;
	}

	@Override
	public String getCustomName() {
		return this.nbtData.getString("RegistryName");
	}

	@Override
	public INbt getCustomNbt() {
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(this.nbtData);
	}

	@Override
	public int getMetaFromState(@Nonnull IBlockState state) {
		return state.getValue(CustomBlockPortal.TYPE);
	}

	@Override
	public @Nonnull ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
		return new ItemStack(Item.getItemFromBlock(this), 1, 0);
	}

	@Override
	public @Nonnull IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(CustomBlockPortal.TYPE, meta % 3);
	}

	@Override
	public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
		if (tab != CustomRegisters.tabBlocks && tab != CreativeTabs.SEARCH) { return; }
		if (this.nbtData != null && this.nbtData.hasKey("ShowInCreative", 1)
				&& !this.nbtData.getBoolean("ShowInCreative")) {
			return;
		}
		items.add(new ItemStack(this));
		if (tab == CustomRegisters.tabBlocks) { AdditionalMethods.instance.sort(items); }
	}

	private CustomTileEntityPortal getTile(World world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		Block block = world.getBlockState(pos).getBlock();
		if (tile instanceof CustomTileEntityPortal && block instanceof CustomBlockPortal
				&& ((CustomBlockPortal) block).getCustomName().equals(this.getCustomName())) {
			return (CustomTileEntityPortal) tile;
		}
		return null;
	}

	@Override
	public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof CustomTileEntityPortal) {
			int type = placer.rotationPitch < -45 || placer.rotationPitch > 45 ? 0 : 1;
			if (type == 1 && (placer.getHorizontalFacing() == EnumFacing.EAST
					|| placer.getHorizontalFacing() == EnumFacing.WEST)) {
				type = 2;
			}
			world.setBlockState(pos, state.withProperty(CustomBlockPortal.TYPE, type));
			if (this.nbtData.hasKey("RenderData", 10)
					&& this.nbtData.getCompoundTag("RenderData").hasKey("SecondSpeed", 5)) {
				NBTTagCompound nbtRender = this.nbtData.getCompoundTag("RenderData");
				if (nbtRender.hasKey("SecondSpeed", 5)) {
					((CustomTileEntityPortal) tile).speed = nbtRender.getFloat("SecondSpeed");
					if (((CustomTileEntityPortal) tile).speed < 10.0f) {
						((CustomTileEntityPortal) tile).speed = 10.0f;
					} else if (((CustomTileEntityPortal) tile).speed > 10000.0f) {
						((CustomTileEntityPortal) tile).speed = 10000.0f;
					}
				}
				if (nbtRender.hasKey("Transparency", 5)) {
					((CustomTileEntityPortal) tile).alpha = nbtRender.getFloat("Transparency");
					if (((CustomTileEntityPortal) tile).alpha < 0.15f) {
						((CustomTileEntityPortal) tile).alpha = 0.15f;
					} else if (((CustomTileEntityPortal) tile).alpha > 1.0f) {
						((CustomTileEntityPortal) tile).alpha = 1.0f;
					}
				}
			}
			CustomTileEntityPortal adjacent = this.getTile(world, pos.south());
			if (adjacent == null) {
				for (int i = 0; i < 6; i++) {
					switch (i) {
					case 0: {
						adjacent = this.getTile(world, pos.south());
						break;
					}
					case 1: {
						adjacent = this.getTile(world, pos.north());
						break;
					}
					case 2: {
						adjacent = this.getTile(world, pos.east());
						break;
					}
					case 3: {
						adjacent = this.getTile(world, pos.west());
						break;
					}
					case 4: {
						adjacent = this.getTile(world, pos.up());
						break;
					}
					case 5: {
						adjacent = this.getTile(world, pos.down());
						break;
					}
					}
					if (adjacent != null) {
						break;
					}
				}
			}
			if (adjacent != null) {
				final CustomTileEntityPortal ctep = adjacent;
				CustomNPCsScheduler.runTack(() -> {
					TileEntity t = world.getTileEntity(pos);
					if (t instanceof CustomTileEntityPortal) {
						if (ctep.posTp[1] > -1) {
							((CustomTileEntityPortal) t).posTp[0] = ctep.posTp[0];
							((CustomTileEntityPortal) t).posTp[1] = ctep.posTp[1];
							((CustomTileEntityPortal) t).posTp[2] = ctep.posTp[2];
						}
						if (ctep.posHomeTp[1] > -1) {
							((CustomTileEntityPortal) t).posHomeTp[0] = ctep.posHomeTp[0];
							((CustomTileEntityPortal) t).posHomeTp[1] = ctep.posHomeTp[1];
							((CustomTileEntityPortal) t).posHomeTp[2] = ctep.posHomeTp[2];
						}
						((CustomTileEntityPortal) t).dimensionId = ctep.dimensionId;
						((CustomTileEntityPortal) t).homeDimensionId = ctep.homeDimensionId;
						((CustomTileEntityPortal) t).speed = ctep.speed;
						((CustomTileEntityPortal) t).alpha = ctep.alpha;
						((CustomTileEntityPortal) t).updateToClient();
					}
				}, 250);
			}
		}
		super.onBlockPlacedBy(world, pos, state, placer, stack);
	}

	@Override
	public void onEntityCollidedWithBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entityIn) {
		int id = 0, homeId = 0;
		if (this.nbtData.hasKey("DimensionID", 3)) {
			id = this.nbtData.getInteger("DimensionID");
		}
		if (this.nbtData.hasKey("HomeDimensionID", 3)) {
			homeId = this.nbtData.getInteger("HomeDimensionID");
		}
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof CustomTileEntityPortal) {
			if (DimensionManager.isDimensionRegistered(((CustomTileEntityPortal) tile).dimensionId)) {
				id = ((CustomTileEntityPortal) tile).dimensionId;
			}
			if (DimensionManager.isDimensionRegistered(((CustomTileEntityPortal) tile).homeDimensionId)) {
				homeId = ((CustomTileEntityPortal) tile).homeDimensionId;
			}
		}
		if (!DimensionManager.isDimensionRegistered(id)) {
			id = 0;
		}
		if (!DimensionManager.isDimensionRegistered(homeId)) {
			homeId = 0;
		}
		if (!worldIn.isRemote && !entityIn.isRiding() && !entityIn.isBeingRidden() && entityIn.isNonBoss()
				&& entityIn.getEntityBoundingBox().intersects(state.getBoundingBox(worldIn, pos).offset(pos))) {
			boolean isHome = worldIn.provider.getDimension() == id;
			BlockPos p = null;
			if (tile instanceof CustomTileEntityPortal) {
				p = ((CustomTileEntityPortal) tile).getPosTp(isHome);
			}
			if (p == null) {
				WorldServer world = Objects.requireNonNull(entityIn.getServer()).getWorld(isHome ? homeId : id);
				p = world.getSpawnCoordinate();
				if (p == null) {
					p = world.getSpawnPoint();
				}
                if (!world.isAirBlock(p)) {
                    p = world.getTopSolidOrLiquidBlock(p);
                } else if (!world.isAirBlock(p.up())) {
                    while (world.isAirBlock(p) && p.getY() > 0) {
                        p = p.down();
                    }
                    if (p.getY() == 0) {
                        p = world.getTopSolidOrLiquidBlock(p);
                    }
                }
            }
            if (entityIn instanceof EntityPlayerMP) {
				CustomTeleport event = EventHooks.onPlayerTeleport((EntityPlayerMP) entityIn, p, pos,
						isHome ? homeId : id);
				if (!event.isCanceled()) {
					NoppesUtilPlayer.teleportPlayer((EntityPlayerMP) entityIn, event.pos.getX() + 0.5d,
							event.pos.getY(), event.pos.getZ() + 0.5d, event.dimension, entityIn.rotationYaw,
							entityIn.rotationPitch);
				}
			} else {
				if (entityIn instanceof EntityNPCInterface) {
					CustomNpcTeleport event = EventHooks.onNpcTeleport((EntityNPCInterface) entityIn, p, pos,
							isHome ? homeId : id);
					if (event.isCanceled() || entityIn.isDead) {
						return;
					}
				}
				entityIn = AdditionalMethods.travelEntity(CustomNpcs.Server, entityIn, isHome ? homeId : id);
                if (entityIn != null) {
					entityIn.setPosition(p.getX() + 0.5d, p.getY(), p.getZ() + 0.5d);
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(@Nonnull IBlockState stateIn, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Random rand) {
		if (this.nbtData.hasKey("RenderData", 10) && Math.random() < 0.25f) {
			double d0 = (float) pos.getX() + rand.nextFloat();
			double d1 = (float) pos.getY() + 0.8F;
			double d2 = (float) pos.getZ() + rand.nextFloat();
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
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(@Nonnull IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
		return side == EnumFacing.DOWN && super.shouldSideBeRendered(blockState, blockAccess, pos, side);
	}

	@Override
	public int getType() {
		if (this.nbtData != null && this.nbtData.hasKey("BlockType", 1)) { return this.nbtData.getByte("BlockType"); }
		return 5;
	}
}
