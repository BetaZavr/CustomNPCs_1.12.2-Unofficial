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
import noppes.npcs.api.IPos;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.blocks.CustomBlockPortal;
import noppes.npcs.constants.EnumPacketClient;

public class CustomTileEntityPortal
extends TileEntityEndPortal {
	
	public int dimensionId = 100, homeDimensionId = 0;
	public float speed = 800.0f, alpha = 0.5f;
	public IPos posTp, posHomeTp;
	private ResourceLocation SKY_TEXTURE, PORTAL_TEXTURE;
	private int type = 3;
	
	public CustomTileEntityPortal() {
		this.posTp = NpcAPI.Instance().getIPos(0, -1, 0);
		this.posHomeTp = NpcAPI.Instance().getIPos(0, -1, 0);
	}
	
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderFace(EnumFacing facing) {
		if (this.type==3 && this.world!=null) {
			IBlockState state = this.world.getBlockState(this.pos);
			if (state.getBlock() instanceof CustomBlockPortal) { this.type = state.getBlock().getMetaFromState(state); }
		}
		switch(this.type) {
			case 1: return facing == EnumFacing.SOUTH || facing == EnumFacing.NORTH;
			case 2: return facing == EnumFacing.WEST || facing == EnumFacing.EAST;
			default: return facing == EnumFacing.UP || facing == EnumFacing.DOWN;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (!compound.hasKey("DimensionID", 3)) {
			CustomNpcs.proxy.fixTileEntityData(this);
			return;
		}	
		this.dimensionId = compound.getInteger("DimensionID");
		this.homeDimensionId = compound.getInteger("HomeDimensionID");
		this.speed = compound.getFloat("SecondSpeed");
		int[] p = compound.getIntArray("HomePosition");
		if (p!=null && p.length>=3) { this.posHomeTp = NpcAPI.Instance().getIPos(p[0], p[1], p[2]); }
		p = compound.getIntArray("TpPosition");
		if (p!=null && p.length>=3) { this.posTp = NpcAPI.Instance().getIPos(p[0], p[1], p[2]); }
    }
	
	@Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("DimensionID", this.dimensionId);
		compound.setInteger("HomeDimensionID", this.homeDimensionId);
		compound.setFloat("SecondSpeed", this.speed);
		compound.setIntArray("HomePosition", new int[] { this.posHomeTp.getX(), this.posHomeTp.getY(), this.posHomeTp.getZ()});
		compound.setIntArray("TpPosition", new int[] { this.posTp.getX(), this.posTp.getY(), this.posTp.getZ()});
        return compound;
    }
	
	public ResourceLocation getSkyTexture() {
		if (this.SKY_TEXTURE==null && this.world!=null) {
			IBlockState state = this.world.getBlockState(this.pos);
			if (state.getBlock() instanceof CustomBlockPortal) {
				this.SKY_TEXTURE = new ResourceLocation(CustomNpcs.MODID, "textures/environment/custom_"+((CustomBlockPortal) state.getBlock()).getCustomName()+"_sky.png");
			}
		}
		if (this.SKY_TEXTURE!=null) { return this.SKY_TEXTURE; }
		return new ResourceLocation("textures/environment/end_sky.png");
	}
	
	public ResourceLocation getPortalTexture() {
		if (this.PORTAL_TEXTURE==null && this.world!=null) {
			IBlockState state = this.world.getBlockState(this.pos);
			if (state.getBlock() instanceof CustomBlockPortal) {
				this.PORTAL_TEXTURE = new ResourceLocation(CustomNpcs.MODID, "textures/environment/custom_"+((CustomBlockPortal) state.getBlock()).getCustomName()+"_portal.png");
			}
		}
		if (this.PORTAL_TEXTURE!=null) { return this.PORTAL_TEXTURE; }
		return new ResourceLocation("textures/entity/end_portal.png");
	}

	public BlockPos getPosTp(boolean isHome) {
		BlockPos pos = null;
		WorldServer world = null;
		if (isHome) {
			if (DimensionManager.isDimensionRegistered(this.homeDimensionId)) {
				pos = this.posHomeTp.getMCBlockPos();
				if (this.world.getMinecraftServer()!=null) { world = this.world.getMinecraftServer().getWorld(this.homeDimensionId); }
				else if (CustomNpcs.Server!=null) { world = CustomNpcs.Server.getWorld(this.homeDimensionId); }
			}
		}
		else {
			if (DimensionManager.isDimensionRegistered(this.dimensionId)) {
				pos = this.posTp.getMCBlockPos();
				if (this.world.getMinecraftServer()!=null) { world = this.world.getMinecraftServer().getWorld(this.dimensionId); }
				else if (CustomNpcs.Server!=null) { world = CustomNpcs.Server.getWorld(this.dimensionId); }
			}
		}
		if ((pos==null || pos.getY()<0) && world!=null) {
			pos = world.getSpawnCoordinate();
			if (pos == null) { pos = world.getSpawnPoint(); }
		}
		if (pos==null) { pos = new BlockPos(0, 70, 0); }
		if (pos!=null && world!=null) {
			if (!world.isAirBlock(pos)) { pos = world.getTopSolidOrLiquidBlock(pos); }
			else if (!world.isAirBlock(pos.up())) {
				while (world.isAirBlock(pos) && pos.getY() > 0) { pos = pos.down(); }
				if (pos.getY() == 0) { pos = world.getTopSolidOrLiquidBlock(pos); }
			}
		}
		return pos;
	}

	public void updateToClient() {
		if (this.world==null || this.world.isRemote) { return; }
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SET_TILE_DATA, this.writeToNBT(new NBTTagCompound()));
	}
}
