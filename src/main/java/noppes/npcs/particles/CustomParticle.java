package noppes.npcs.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.event.CustomParticleEvent;
import noppes.npcs.api.handler.data.ICustomParticle;
import noppes.npcs.client.ClientGuiEventHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class CustomParticle extends Particle implements ICustomElement, ICustomParticle {

	public NBTTagCompound nbtData;
	public ResourceLocation texture, obj;
	public boolean full = false;
	public int objList;
	protected float particleAngleX;
	protected float particleAngleZ;
	protected long rndStart;

	public CustomParticle(NBTTagCompound data, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
		this.nbtData = data;
		this.objList = -1;
		this.rndStart = this.rand.nextInt(7000);
		if (data.hasKey("OBJModel", 8)) {
			this.obj = new ResourceLocation(CustomNpcs.MODID, "models/particle/" + data.getString("OBJModel") + ".obj");
		}
		if (data.hasKey("IsFullTexture", 1)) {
			this.full = data.getBoolean("IsFullTexture");
		}
		if (data.hasKey("MaxAge", 3)) {
			this.particleMaxAge = data.getInteger("MaxAge");
		}
		if (data.hasKey("Gravity", 5)) {
			this.particleGravity = data.getFloat("Gravity");
		}
		if (data.hasKey("Scale", 5)) {
			this.particleScale = data.getFloat("Scale");
		}
		if (data.hasKey("Texture", 8)) {
			this.texture = new ResourceLocation(CustomNpcs.MODID, "textures/particle/" + data.getString("Texture") + ".png");
		}
		if (data.hasKey("UVpos", 11) && data.getIntArray("UVpos").length > 1) {
			int[] p = data.getIntArray("UVpos");
			for (int i = 0; i < 2; i++) {
				if (p[i] < 0) {
					p[i] = 0;
				} else if (p[i] > 15) {
					p[i] = 15;
				}
			}
			this.setParticleTextureIndex(p[0] + p[1] * 16);
		}
		if (xSpeedIn == 0.0d) {
			this.motionX = 0.0d;
		}
		if (ySpeedIn == 0.0d) {
			this.motionY = 0.0d;
		}
		if (zSpeedIn == 0.0d) {
			this.motionZ = 0.0d;
		}
		if (data.hasKey("StartMotion", 9) && data.getTagList("StartMotion", 6).tagCount() > 2) {
			NBTTagList list = data.getTagList("StartMotion", 6);
			if (data.getBoolean("IsRandomMotion")) {
				this.motionX = (Math.random() < 0.5d ? -1.0d : 1.0d) * Math.random() * list.getDoubleAt(0);
				this.motionY = (Math.random() < 0.5d && !data.getBoolean("NotMotionY") ? -1.0d : 1.0d) * Math.random()
						* list.getDoubleAt(1);
				this.motionZ = (Math.random() < 0.5d ? -1.0d : 1.0d) * Math.random() * list.getDoubleAt(2);
			} else {
				this.motionX = list.getDoubleAt(0);
				this.motionY = list.getDoubleAt(1);
				this.motionZ = list.getDoubleAt(2);
			}
		}
		CustomParticleEvent.CreateEvent event = new CustomParticleEvent.CreateEvent(this, Minecraft.getMinecraft().player);
	}

	@Override
	public boolean canCollide() {
		return this.canCollide;
	}

	@Override
	public int getAge() {
		return this.particleAge;
	}

	public float getAlphaF() {
		return this.particleAlpha;
	}

	@Override
	public int getColorMask() {
		return (int) (((int) (this.particleRed * 255.0f) << 16) + ((int) (this.particleGreen * 255.0f) << 8)
				+ this.particleBlue * 255.0f);
	}

	@Override
	public String getCustomName() {
		return this.nbtData.getString("RegistryName").toLowerCase();
	}

	@Override
	public INbt getCustomNbt() {
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(this.nbtData);
	}

	@Override
	public float getHeight() {
		return this.height;
	}

	@Override
	public String getObj() {
		return this.obj.toString();
	}

	@Override
	public double[] getPrevPoses() {
		return new double[] { this.prevPosX, this.prevPosY, this.prevPosZ };
	}

	@Override
	public float getRotationX() {
		return this.particleAngle;
	}

	@Override
	public float getRotationY() {
        return this.particleAngleX;
	}

	@Override
	public float getRotationZ() {
		return this.particleAngleZ;
	}

	@Override
	public float getScale() {
		return this.particleScale;
	}

	@Override
	public String getTexture() {
		return this.texture.toString();
	}

	@Override
	public int getTotalAge() {
		return this.particleMaxAge;
	}

	@Override
	public float getWidth() {
		return this.width;
	}

	@Override
	public IWorld getWorld() {
		return Objects.requireNonNull(NpcAPI.Instance()).getIWorld(this.world);
	}

	@Override
	public boolean onGround() {
		return this.onGround;
	}

	/** Every tick */
	@Override
	public void onUpdate() {
		if (this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		}
		CustomParticleEvent.UpdateEvent event = new CustomParticleEvent.UpdateEvent(this, Minecraft.getMinecraft().player);
		event.setCanceled(true);

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (!event.isCanceled()) {
			return;
		}

		this.motionY -= 0.04D * (double) this.particleGravity;

		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.98D;
		this.motionY *= 0.98D;
		this.motionZ *= 0.98D;

		if (this.onGround) {
			this.motionX *= 0.7D;
			this.motionZ *= 0.7D;
		}
		if (this.obj != null) {
			if (!this.onGround) {
				this.particleAngle = (float) (((System.currentTimeMillis() + this.rndStart) / 7L) % 360L);
				this.particleAngleX = (float) (((System.currentTimeMillis() + this.rndStart) / 7L) % 360L);
			}
		}
	}

	@Override
	public double posX() {
		return this.posX;
	}

	@Override
	public double posY() {
		return this.posY;
	}

	@Override
	public double posZ() {
		return this.posZ;
	}

	public void renderParticle(@Nonnull BufferBuilder buffer, @Nonnull Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		CustomParticleEvent.RenderEvent event = new CustomParticleEvent.RenderEvent(this, Minecraft.getMinecraft().player);
		if (event.isCanceled()) {
			return;
		}
		try {
			if (this.obj != null) {
				if (!ClientGuiEventHandler.customParticle.contains(this)) {
					ClientGuiEventHandler.customParticle.add(this);
				}
				return;
			}
			if (this.texture == null) {
				return;
			}
			Minecraft.getMinecraft().renderEngine.bindTexture(this.texture);

			float f = (float) this.particleTextureIndexX / 16.0F;
			float f1 = f + 0.0624375F;
			float f2 = (float) this.particleTextureIndexY / 16.0F;
			float f3 = f2 + 0.0624375F;
			float f4 = 0.1F * this.particleScale;

			if (this.full) {
				f = 0.0F;
				f1 = 1.0F;
				f2 = 0.0F;
				f3 = 1.0F;
			}

			float f5 = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
			float f6 = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
			float f7 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);
			int i = this.getBrightnessForRender(partialTicks);
			int j = i >> 16 & 65535;
			int k = i & 65535;
			Vec3d[] avec3d = new Vec3d[] {
					new Vec3d(-rotationX * f4 - rotationXY * f4, -rotationZ * f4, -rotationYZ * f4 - rotationXZ * f4),
					new Vec3d(-rotationX * f4 + rotationXY * f4, rotationZ * f4, -rotationYZ * f4 + rotationXZ * f4),
					new Vec3d(rotationX * f4 + rotationXY * f4, rotationZ * f4, rotationYZ * f4 + rotationXZ * f4),
					new Vec3d(rotationX * f4 - rotationXY * f4, -rotationZ * f4, rotationYZ * f4 - rotationXZ * f4) };

			if (this.particleAngle != 0.0F) {
				float f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
				float f9 = MathHelper.cos(f8 * 0.5F);
				float f10 = MathHelper.sin(f8 * 0.5F) * (float) cameraViewDir.x;
				float f11 = MathHelper.sin(f8 * 0.5F) * (float) cameraViewDir.y;
				float f12 = MathHelper.sin(f8 * 0.5F) * (float) cameraViewDir.z;
				Vec3d vec3d = new Vec3d(f10, f11, f12);
				for (int l = 0; l < 4; ++l) {
					avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d))
							.add(avec3d[l].scale((double) (f9 * f9) - vec3d.dotProduct(vec3d)))
							.add(vec3d.crossProduct(avec3d[l]).scale(2.0F * f9));
				}
			}
			buffer.pos((double) f5 + avec3d[0].x, (double) f6 + avec3d[0].y, (double) f7 + avec3d[0].z)
					.tex(f1, f3)
					.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k)
					.endVertex();
			buffer.pos((double) f5 + avec3d[1].x, (double) f6 + avec3d[1].y, (double) f7 + avec3d[1].z)
					.tex(f1, f2)
					.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k)
					.endVertex();
			buffer.pos((double) f5 + avec3d[2].x, (double) f6 + avec3d[2].y, (double) f7 + avec3d[2].z)
					.tex(f, f2)
					.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k)
					.endVertex();
			buffer.pos((double) f5 + avec3d[3].x, (double) f6 + avec3d[3].y, (double) f7 + avec3d[3].z)
					.tex(f, f3)
					.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k)
					.endVertex();
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	@Override
	public void setAge(int ticks) {
		if (ticks < 0) {
			ticks = 0;
		}
		this.particleAge = ticks;
	}

	@Override
	public void setAlphaF(float alpha) {
		if (alpha < 0.0f) {
			alpha = 0.0f;
		} else if (alpha > 1.0f) {
			alpha = 1.0f;
		}
		this.particleAlpha = alpha;
	}

	@Override
	public void setCanCollide(boolean collide) {
		this.canCollide = collide;
	}

	@Override
	public void setColorMask(int color) {
		this.particleRed = (float) (color >> 16 & 255) / 255.0F;
		this.particleGreen = (float) (color >> 8 & 255) / 255.0F;
		this.particleBlue = (float) (color & 255) / 255.0F;
	}

	@Override
	public void setCustomSize(float width, float height) {
		this.setSize(width, height);
	}

	@Override
	public void setObj(String objPath) {
		this.obj = new ResourceLocation(objPath);
	}

	@Override
	public void setPos(double x, double y, double z) {
		this.setPosition(x, y, z);
	}

	@Override
	public void setRotation(float angleX, float angleY, float angleZ) {
		this.particleAngle = angleY % 360.0f;
		this.particleAngleX = angleX % 360.0f;
		this.particleAngleZ = angleZ % 360.0f;
	}

	@Override
	public void setScale(float scale) {
		if (scale == 0.0f) {
			return;
		}
		this.particleScale = scale;
	}

	@Override
	public void setTexture(String texture) {
		this.texture = new ResourceLocation(texture);
	}

	@Override
	public void setTotalAge(int ticks) {
		if (ticks < 0) {
			ticks = 0;
		}
		this.particleMaxAge = ticks;
	}

	@Override
	public int getType() { return this.obj != null ? 1 : 0; }

}
