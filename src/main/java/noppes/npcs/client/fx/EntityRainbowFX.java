package noppes.npcs.client.fx;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EntityRainbowFX extends Particle {
	public static float[][] colorTable = new float[][] { { 1.0f, 0.0f, 0.0f }, { 1.0f, 0.5f, 0.0f },
			{ 1.0f, 1.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { 0.0f, 0.0f, 1.0f }, { 0.0f, 4375.0f, 0.0f, 1.0f },
			{ 0.5625f, 0.0f, 1.0f } };
	float reddustParticleScale;

	public EntityRainbowFX(World world, double d, double d1, double d2, double f, double f1, double f2) {
		this(world, d, d1, d2, 1.0f, f, f1, f2);
	}

	public EntityRainbowFX(World world, double d, double d1, double d2, float f, double f1, double f2, double f3) {
		super(world, d, d1, d2, 0.0, 0.0, 0.0);
		this.motionX *= 0.10000000149011612;
		this.motionY *= 0.10000000149011612;
		this.motionZ *= 0.10000000149011612;
		if (f1 == 0.0) {
			f1 = 1.0;
		}
		int i = world.rand.nextInt(EntityRainbowFX.colorTable.length);
		this.particleRed = EntityRainbowFX.colorTable[i][0];
		this.particleGreen = EntityRainbowFX.colorTable[i][1];
		this.particleBlue = EntityRainbowFX.colorTable[i][2];
		this.particleScale *= 0.75f;
		this.particleScale *= f;
		this.reddustParticleScale = this.particleScale;
		this.particleMaxAge = (int) (16.0 / (Math.random() * 0.8 + 0.2));
		this.particleMaxAge *= f;
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		if (this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		}
		this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
		this.move(this.motionX, this.motionY, this.motionZ);
		if (this.posY == this.prevPosY) {
			this.motionX *= 1.1;
			this.motionZ *= 1.1;
		}
		this.motionX *= 0.9599999785423279;
		this.motionY *= 0.9599999785423279;
		this.motionZ *= 0.9599999785423279;
		if (this.onGround) {
			this.motionX *= 0.699999988079071;
			this.motionZ *= 0.699999988079071;
		}
	}

	public void renderParticle(BufferBuilder tessellator, Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		float f6 = (this.particleAge + f) / this.particleMaxAge * 32.0f;
		if (f6 < 0.0f) {
			f6 = 0.0f;
		} else if (f6 > 1.0f) {
			f6 = 1.0f;
		}
		this.particleScale = this.reddustParticleScale * f6;
		super.renderParticle(tessellator, entity, f, f1, f2, f3, f4, f5);
	}

}
