package noppes.npcs.client.fx;

import net.minecraft.client.particle.ParticlePortal;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.entity.EntityCustomNpc;

public class EntityEnderFX extends ParticlePortal {
	private static ResourceLocation resource = new ResourceLocation("textures/particle/particles.png");
	private ResourceLocation location;
	private boolean move;
	private EntityCustomNpc npc;
	private int particleNumber;
	private float portalParticleScale;
	private float startX;
	private float startY;
	private float startZ;

	public EntityEnderFX(EntityCustomNpc npc, double par2, double par4, double par6, double par8, double par10,
			double par12, ModelPartData data) {
		super(npc.world, par2, par4, par6, par8, par10, par12);
		this.move = true;
		this.startX = 0.0f;
		this.startY = 0.0f;
		this.startZ = 0.0f;
		this.npc = npc;
		this.particleNumber = npc.getRNG().nextInt(2);
		float n = this.rand.nextFloat() * 0.2f + 0.5f;
		this.particleScale = n;
		this.portalParticleScale = n;
		this.particleRed = (data.color >> 16 & 0xFF) / 255.0f;
		this.particleGreen = (data.color >> 8 & 0xFF) / 255.0f;
		this.particleBlue = (data.color & 0xFF) / 255.0f;
		if (npc.getRNG().nextInt(3) == 1) {
			this.move = false;
			this.startX = (float) npc.posX;
			this.startY = (float) npc.posY;
			this.startZ = (float) npc.posZ;
		}
		if (data.playerTexture) {
			this.location = npc.textureLocation;
		} else {
			this.location = data.getResource();
		}
	}

	public int getFXLayer() {
		return 0;
	}

	public void renderParticle(BufferBuilder renderer, Entity entity, float partialTicks, float par3, float par4,
			float par5, float par6, float par7) {
		if (this.move) {
			this.startX = (float) (this.npc.prevPosX + (this.npc.posX - this.npc.prevPosX) * partialTicks);
			this.startY = (float) (this.npc.prevPosY + (this.npc.posY - this.npc.prevPosY) * partialTicks);
			this.startZ = (float) (this.npc.prevPosZ + (this.npc.posZ - this.npc.prevPosZ) * partialTicks);
		}
		Tessellator tessellator = Tessellator.getInstance();
		tessellator.draw();
		float scale = (this.particleAge + partialTicks) / this.particleMaxAge;
		scale = 1.0f - scale;
		scale *= scale;
		scale = 1.0f - scale;
		this.particleScale = this.portalParticleScale * scale;
		ClientProxy.bindTexture(this.location);
		float f = 0.875f;
		float f2 = f + 0.125f;
		float f3 = 0.75f - this.particleNumber * 0.25f;
		float f4 = f3 + 0.25f;
		float f5 = 0.1f * this.particleScale;
		float f6 = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - EntityEnderFX.interpPosX
				+ this.startX);
		float f7 = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - EntityEnderFX.interpPosY
				+ this.startY);
		float f8 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - EntityEnderFX.interpPosZ
				+ this.startZ);
		int i = this.getBrightnessForRender(partialTicks);
		int j = i >> 16 & 0xFFFF;
		int k = i & 0xFFFF;
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		renderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		renderer.pos((f6 - par3 * f5 - par6 * f5), (f7 - par4 * f5), (f8 - par5 * f5 - par7 * f5)).tex(f2, f4)
				.color(this.particleRed, this.particleGreen, this.particleBlue, 1.0f).lightmap(j, k).endVertex();
		renderer.pos((f6 - par3 * f5 + par6 * f5), (f7 + par4 * f5), (f8 - par5 * f5 + par7 * f5)).tex(f2, f3)
				.color(this.particleRed, this.particleGreen, this.particleBlue, 1.0f).lightmap(j, k).endVertex();
		renderer.pos((f6 + par3 * f5 + par6 * f5), (f7 + par4 * f5), (f8 + par5 * f5 + par7 * f5)).tex(f, f3)
				.color(this.particleRed, this.particleGreen, this.particleBlue, 1.0f).lightmap(j, k).endVertex();
		renderer.pos((f6 + par3 * f5 - par6 * f5), (f7 - par4 * f5), (f8 + par5 * f5 - par7 * f5)).tex(f, f4)
				.color(this.particleRed, this.particleGreen, this.particleBlue, 1.0f).lightmap(j, k).endVertex();
		tessellator.draw();
		ClientProxy.bindTexture(EntityEnderFX.resource);
		renderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
	}

}
