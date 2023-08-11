package noppes.npcs.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;

public class CustomParticle
extends Particle
implements ICustomElement
{
	
	private NBTTagCompound nbtData;
	private ResourceLocation texture, obj;
	private boolean full = false;
	
	public CustomParticle(NBTTagCompound data, TextureManager textureManager, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int ... parametrs) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
		this.nbtData = data;
		if (data.hasKey("IsFullTexture", 1)) { this.full = data.getBoolean("IsFullTexture"); }
		if (data.hasKey("MaxAge", 3)) { this.particleMaxAge = data.getInteger("MaxAge"); }
		if (data.hasKey("Gravity", 5)) { this.particleGravity = data.getFloat("Gravity"); }
		if (data.hasKey("Scale", 5)) { this.particleScale = data.getFloat("Scale"); }
		if (data.hasKey("Texture", 8)) { this.texture = new ResourceLocation(CustomNpcs.MODID, "textures/particle/"+data.getString("Texture")+".png"); }
		if (data.hasKey("UVpos", 11) && data.getIntArray("UVpos").length > 1) {
			int[] p = data.getIntArray("UVpos");
			for (int i = 0; i<2; i++) { if (p[i]<0) { p[i] = 0; } else if (p[i]>15) { p[i] = 15; } }
			this.setParticleTextureIndex(p[0] + p[1]*16);
		}
        if (xSpeedIn==0.0d) { this.motionX = 0.0d; }
        if (ySpeedIn==0.0d) { this.motionY = 0.0d; }
        if (zSpeedIn==0.0d) { this.motionZ = 0.0d; }
		if (data.hasKey("StartMotion", 9) && data.getTagList("StartMotion", 6).tagCount()>2) { 
			NBTTagList list = data.getTagList("StartMotion", 6);
			if (data.getBoolean("IsRandomMotion")) {
		        this.motionX = (Math.random() < 0.5d ? -1.0d : 1.0d) * Math.random() * list.getDoubleAt(0);
		        this.motionY = (Math.random() < 0.5d && !data.getBoolean("NotMotionY") ? -1.0d : 1.0d) * Math.random() * list.getDoubleAt(1);
		        this.motionZ = (Math.random() < 0.5d ? -1.0d : 1.0d) * Math.random() * list.getDoubleAt(2);
			} else {
				this.motionX = list.getDoubleAt(0);
				this.motionY = list.getDoubleAt(1);
				this.motionZ = list.getDoubleAt(2);
			}
		}
	}
	
	/** Every tick */
	@Override
	public void onUpdate() {
		if (this.particleAge++ >= this.particleMaxAge) { this.setExpired(); }
		
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		
		this.motionY -= 0.04D * (double)this.particleGravity;
		
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.98D;
		this.motionY *= 0.98D;
		this.motionZ *= 0.98D;

		if (this.onGround) {
			this.motionX *= 0.7D;
			this.motionZ *= 0.7D;
		}
	}
	
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if (this.obj!=null) {
			return;
		}
		if (this.texture==null) { return; }
		Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
		
		float f = (float)this.particleTextureIndexX / 16.0F;
		float f1 = f + 0.0624375F;
		float f2 = (float)this.particleTextureIndexY / 16.0F;
		float f3 = f2 + 0.0624375F;
		float f4 = 0.1F * this.particleScale;
		
		if (this.full) {
			f = 0.0F;
			f1 = 1.0F;
			f2 = 0.0F;
			f3 = 1.0F;
		}
		
		float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
		float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
		float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
		int i = this.getBrightnessForRender(partialTicks);
		int j = i >> 16 & 65535;
		int k = i & 65535;
		Vec3d[] avec3d = new Vec3d[] {new Vec3d((double)(-rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(-rotationYZ * f4 - rotationXZ * f4)), new Vec3d((double)(-rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(-rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(rotationYZ * f4 - rotationXZ * f4))};

		if (this.particleAngle != 0.0F) {
			float f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
			float f9 = MathHelper.cos(f8 * 0.5F);
			float f10 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.x;
			float f11 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.y;
			float f12 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.z;
			Vec3d vec3d = new Vec3d((double)f10, (double)f11, (double)f12);

			for (int l = 0; l < 4; ++l) {
				avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((double)(f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((double)(2.0F * f9)));
			}
		}
		buffer.pos((double)f5 + avec3d[0].x, (double)f6 + avec3d[0].y, (double)f7 + avec3d[0].z).tex((double)f1, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
		buffer.pos((double)f5 + avec3d[1].x, (double)f6 + avec3d[1].y, (double)f7 + avec3d[1].z).tex((double)f1, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
		buffer.pos((double)f5 + avec3d[2].x, (double)f6 + avec3d[2].y, (double)f7 + avec3d[2].z).tex((double)f, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
		buffer.pos((double)f5 + avec3d[3].x, (double)f6 + avec3d[3].y, (double)f7 + avec3d[3].z).tex((double)f, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
	}
	
	@Override
	public INbt getCustomNbt() { return NpcAPI.Instance().getINbt(this.nbtData); }

	@Override
	public String getCustomName() { return this.nbtData.getString("RegistryName").toLowerCase(); }
	
}
