package noppes.npcs.blocks.tiles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.blocks.CustomBlockPortal;
import noppes.npcs.constants.EnumPacketClient;

import javax.annotation.Nonnull;

public class CustomTileEntityPortal extends TileEntityEndPortal {

	public int dimensionId = 100;
	public int homeDimensionId = 0;
	public float speed = 800.0f;
	public float alpha = 0.5f;
	public final int[] posTp;
	public final int[] posHomeTp;
	private ResourceLocation SKY_TEXTURE, PORTAL_TEXTURE;
	private int type = 3;

	public CustomTileEntityPortal() {
		this.posTp = new int[] { 0, -1, 0 };
		this.posHomeTp = new int[] { 0, -1, 0 };
	}

	public ResourceLocation getPortalTexture() {
		if (this.PORTAL_TEXTURE == null && this.world != null) {
			IBlockState state = this.world.getBlockState(this.pos);
			if (state.getBlock() instanceof CustomBlockPortal) {
				this.PORTAL_TEXTURE = new ResourceLocation(CustomNpcs.MODID, "textures/environment/custom_"
						+ ((CustomBlockPortal) state.getBlock()).getCustomName() + "_portal.png");
			}
		}
		if (this.PORTAL_TEXTURE != null) {
			return this.PORTAL_TEXTURE;
		}
		return new ResourceLocation("textures/entity/end_portal.png");
	}

	public BlockPos getPosTp(boolean isHome) {
		int[] pos = null;
		WorldServer world = null;
		if (isHome) {
			if (DimensionManager.isDimensionRegistered(this.homeDimensionId)) {
				pos = this.posHomeTp;
				if (this.world.getMinecraftServer() != null) {
					world = this.world.getMinecraftServer().getWorld(this.homeDimensionId);
				} else if (CustomNpcs.Server != null) {
					world = CustomNpcs.Server.getWorld(this.homeDimensionId);
				}
			}
		} else {
			if (DimensionManager.isDimensionRegistered(this.dimensionId)) {
				pos = this.posTp;
				if (this.world.getMinecraftServer() != null) {
					world = this.world.getMinecraftServer().getWorld(this.dimensionId);
				} else if (CustomNpcs.Server != null) {
					world = CustomNpcs.Server.getWorld(this.dimensionId);
				}
			}
		}
		if ((pos == null || pos[1] < 0) && world != null) {
			BlockPos p = world.getSpawnCoordinate();
			if (p == null) {
				p = world.getSpawnPoint();
			}
            pos[0] = p.getX();
            pos[1] = p.getY();
            pos[2] = p.getZ();
        }
		if (pos == null) {
			pos = new int[] { 0, 70, 0 };
		}
		BlockPos p = null;
		if (world != null) {
			if (!world.isAirBlock(new BlockPos(pos[0], pos[1], pos[2]))) {
				p = world.getTopSolidOrLiquidBlock(new BlockPos(pos[0], pos[1], pos[2]));
			} else if (!world.isAirBlock(new BlockPos(pos[0], pos[1] + 1, pos[2]))) {
				p = new BlockPos(pos[0], pos[1], pos[2]);
				while (world.isAirBlock(p) && p.getY() > 0) {
					p = p.down();
				}
				if (p.getY() == 0) {
					p = world.getTopSolidOrLiquidBlock(p);
				}
			}
			if (p != null) {
				pos[0] = p.getX();
				pos[1] = p.getY();
				pos[2] = p.getZ();
			}
		}
		return p;
	}

	public ResourceLocation getSkyTexture() {
		if (this.SKY_TEXTURE == null && this.world != null) {
			IBlockState state = this.world.getBlockState(this.pos);
			if (state.getBlock() instanceof CustomBlockPortal) {
				this.SKY_TEXTURE = new ResourceLocation(CustomNpcs.MODID, "textures/environment/custom_"
						+ ((CustomBlockPortal) state.getBlock()).getCustomName() + "_sky.png");
			}
		}
		if (this.SKY_TEXTURE != null) {
			return this.SKY_TEXTURE;
		}
		return new ResourceLocation("textures/environment/end_sky.png");
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (!compound.hasKey("DimensionID", 3)) {
			CustomNpcs.proxy.fixTileEntityData(this);
			return;
		}
		this.dimensionId = compound.getInteger("DimensionID");
		this.homeDimensionId = compound.getInteger("HomeDimensionID");
		this.speed = compound.getFloat("SecondSpeed");
		int[] p = compound.getIntArray("HomePosition");
		if (p.length >= 3) {
			this.posHomeTp[0] = p[0];
			this.posHomeTp[1] = p[1];
			this.posHomeTp[2] = p[2];
		}
		p = compound.getIntArray("TpPosition");
		if (p.length >= 3) {
			this.posTp[0] = p[0];
			this.posTp[1] = p[1];
			this.posTp[2] = p[2];
		}
	}

	@SideOnly(Side.CLIENT)
	public boolean shouldRenderFace(@Nonnull EnumFacing facing) {
		if (this.type == 3 && this.world != null) {
			IBlockState state = this.world.getBlockState(this.pos);
			if (state.getBlock() instanceof CustomBlockPortal) {
				this.type = state.getBlock().getMetaFromState(state);
			}
		}
		switch (this.type) {
		case 1:
			return facing == EnumFacing.SOUTH || facing == EnumFacing.NORTH;
		case 2:
			return facing == EnumFacing.WEST || facing == EnumFacing.EAST;
		default:
			return facing == EnumFacing.UP || facing == EnumFacing.DOWN;
		}
	}

	public void updateToClient() {
		if (this.world == null || this.world.isRemote) {
			return;
		}
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SET_TILE_DATA, this.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("DimensionID", dimensionId);
		compound.setInteger("HomeDimensionID", homeDimensionId);
		compound.setFloat("SecondSpeed", speed);
		compound.setIntArray("HomePosition", posHomeTp);
		compound.setIntArray("TpPosition", posTp);
		return compound;
	}
}
