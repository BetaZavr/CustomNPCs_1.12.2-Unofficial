package noppes.npcs.blocks.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPlayerPacket;

public class CustomTileEntityPortal
extends TileEntityEndPortal {
	
	public int type = 0, dimensionId = 100, homeDimensionId = 0;
	public float speed = 800.0f;
	public ResourceLocation SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
	public ResourceLocation PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
	
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderFace(EnumFacing facing) {
		switch(this.type) {
			case 1: return facing == EnumFacing.SOUTH || facing == EnumFacing.NORTH;
			case 2: return facing == EnumFacing.WEST || facing == EnumFacing.EAST;
			default: return facing == EnumFacing.UP || facing == EnumFacing.DOWN;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("FacingType", 3) && compound.hasKey("DimensionID", 3) && compound.hasKey("HomeDimensionID", 3) &&
				compound.hasKey("SkyTexture", 8) && compound.hasKey("PortalTexture", 8) &&
				compound.hasKey("SecondSpeed", 5)) {
			this.type = compound.getInteger("FacingType");
			this.dimensionId = compound.getInteger("DimensionID");
			this.homeDimensionId = compound.getInteger("HomeDimensionID");
			this.speed = compound.getFloat("SecondSpeed");
			this.SKY_TEXTURE = new ResourceLocation(compound.getString("SkyTexture"));
			this.PORTAL_TEXTURE = new ResourceLocation(compound.getString("PortalTexture"));
		}
		else if (this.world==null || this.world.isRemote) {
			Client.sendDataDelayCheck(EnumPlayerPacket.GetTileData, this, 0, compound);
		}
    }
	
	@Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("FacingType", this.type);
		compound.setInteger("DimensionID", this.dimensionId);
		compound.setInteger("HomeDimensionID", this.homeDimensionId);
		compound.setFloat("SecondSpeed", this.speed);
		compound.setString("SkyTexture", this.SKY_TEXTURE.toString());
		compound.setString("PortalTexture", this.PORTAL_TEXTURE.toString());
        return compound;
    }
	
}
