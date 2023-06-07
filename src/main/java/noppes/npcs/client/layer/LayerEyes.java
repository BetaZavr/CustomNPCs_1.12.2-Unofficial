package noppes.npcs.client.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;

public class LayerEyes<T extends EntityLivingBase>
extends LayerInterface<T> {
	
	private BufferBuilder buffer;

	public LayerEyes(RenderLiving<?> render) {
		super(render);
	}

	private void drawBrows() {
		float offsetY = 0.0f;
		if (this.playerdata.eyes.blinkStart > 0L && this.npc.isEntityAlive() && this.npc.deathTime == 0) {
			float f = (System.currentTimeMillis() - this.playerdata.eyes.blinkStart) / 150.0f;
			if (f > 1.0f) { f = 2.0f - f; }
			if (f < 0.0f) {
				this.playerdata.eyes.blinkStart = 0L;
				f = 0.0f;
			}
			offsetY = ((this.playerdata.eyes.type == 1) ? 2 : 1) * f;
			this.drawRect(-3.0, -5.0, -1.0, -5.0f + offsetY, this.playerdata.eyes.skinColor, 4.013);
			this.drawRect(3.0, -5.0, 1.0, -5.0f + offsetY, this.playerdata.eyes.skinColor, 4.013);
		}
		if (this.playerdata.eyes.browThickness > 0) {
			float thickness = this.playerdata.eyes.browThickness / 10.0f;
			this.drawRect(-3.0, -5.0f + offsetY, -1.0, -5.0f - thickness + offsetY, this.playerdata.eyes.browColor, 4.014);
			this.drawRect(1.0, -5.0f + offsetY, 3.0, -5.0f - thickness + offsetY, this.playerdata.eyes.browColor, 4.014);
		}
	}

	private void drawLeft() {
		if (this.playerdata.eyes.pattern == 2) { return; }
		this.drawRect(3.0, -5.0, 1.0, -4.0, 0xFFF6F6F6, 4.01);
		this.drawRect(2.0, -5.0, 1.0, -4.0, this.playerdata.eyes.color, 4.011);
		if (this.playerdata.eyes.glint && this.npc.isEntityAlive()) {
			this.drawRect(1.5, -4.9, 1.9, -4.5, -1, 4.012);
		}
		if (this.playerdata.eyes.type == 1) {
			this.drawRect(3.0, -4.0, 1.0, -3.0, 0xFFFFFFFF, 4.01);
			this.drawRect(2.0, -4.0, 1.0, -3.0, this.playerdata.eyes.color, 4.011);
		}
	}

	private void drawRight() {
		if (this.playerdata.eyes.pattern == 1) {
			return;
		}
		this.drawRect(-3.0, -5.0, -1.0, -4.0, 0xFFF6F6F6, 4.01);
		this.drawRect(-2.0, -5.0, -1.0, -4.0, this.playerdata.eyes.color, 4.011);
		if (this.playerdata.eyes.glint && this.npc.isEntityAlive()) {
			this.drawRect(-1.5, -4.9, -1.1, -4.5, -1, 4.012);
		}
		if (this.playerdata.eyes.type == 1) {
			this.drawRect(-3.0, -4.0, -1.0, -3.0, 0xFFFFFFFF, 4.01);
			this.drawRect(-2.0, -4.0, -1.0, -3.0, this.playerdata.eyes.color, 4.011);
		}
	}
	
	public void drawRect(double x, double y, double x2, double y2, int color, double z) {
		if (x < x2) {
			double j1 = x;
			x = x2;
			x2 = j1;
		}
		if (y < y2) {
			double j1 = y;
			y = y2;
			y2 = j1;
		}
		float f1 = (color >> 16 & 0xFF) / 255.0f;
		float f2 = (color >> 8 & 0xFF) / 255.0f;
		float f3 = (color & 0xFF) / 255.0f;
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.buffer.pos(x, y, z).color(f1, f2, f3, 1.0f).endVertex();
		this.buffer.pos(x, y2, z).color(f1, f2, f3, 1.0f).endVertex();
		this.buffer.pos(x2, y2, z).color(f1, f2, f3, 1.0f).endVertex();
		this.buffer.pos(x2, y, z).color(f1, f2, f3, 1.0f).endVertex();
	}

	/*private void drawBrows(EmotionConfig emotion, PartEmotion part, float partialTicks) {
		float offsetY = 0.0f;
		if (emotion.blinkStart > 0L && this.npc.isEntityAlive() && this.npc.deathTime == 0) {
			float f = (System.currentTimeMillis() - emotion.blinkStart) / 150.0f;
			if (f > 1.0f) {
				f = 2.0f - f;
			}
			if (f < 0.0f) {
				emotion.blinkStart = 0L;
				f = 0.0f;
			}
			offsetY = ((part.eyesType == 1) ? 2 : 1) * f;
			int skinColor = (int) emotion.getValues(2, partialTicks, this.npc)[0];
			this.drawRect(-3.0, -5.0, -1.0, -5.0f + offsetY, skinColor, 4.013);
			this.drawRect(3.0, -5.0, 1.0, -5.0f + offsetY, skinColor, 4.013);
		}
		float browThickness = emotion.getValues(5, partialTicks, this.npc)[0];
		if (browThickness > 0) {
			float thickness = browThickness / 10.0f;
			int browColor = (int) emotion.getValues(3, partialTicks, this.npc)[0];
			this.drawRect(-3.0, -5.0f + offsetY, -1.0, -5.0f - thickness + offsetY, browColor, 4.014);
			this.drawRect(1.0, -5.0f + offsetY, 3.0, -5.0f - thickness + offsetY, browColor, 4.014);
		}
	}

	private void drawLeft(EmotionConfig emotion, PartEmotion part, float partialTicks) {
		this.drawRect(3.0, -5.0, 1.0, -4.0, 0xFFF6F6F6, 4.01);
		int color = (int) emotion.getValues(4, partialTicks, this.npc)[0];
		this.drawRect(2.0, -5.0, 1.0, -4.0, color, 4.011);
		if (part.glint && this.npc.isEntityAlive()) {
			this.drawRect(1.5, -4.9, 1.9, -4.5, -1, 4.012);
		}
		if (part.eyesType == 1) {
			this.drawRect(3.0, -4.0, 1.0, -3.0, 0xFFFFFFFF, 4.01);
			this.drawRect(2.0, -4.0, 1.0, -3.0, color, 4.011);
		}
	}
	
	private void drawRight(EmotionConfig emotion, PartEmotion part, float partialTicks) {
		if (this.playerdata.eyes.pattern == 1) {
			return;
		}
		this.drawRect(-3.0, -5.0, -1.0, -4.0, 0xFFF6F6F6, 4.01);
		this.drawRect(-2.0, -5.0, -1.0, -4.0, this.playerdata.eyes.color, 4.011);
		if (this.playerdata.eyes.glint && this.npc.isEntityAlive()) {
			this.drawRect(-1.5, -4.9, -1.1, -4.5, -1, 4.012);
		}
		if (this.playerdata.eyes.type == 1) {
			this.drawRect(-3.0, -4.0, -1.0, -3.0, 0xFFFFFFFF, 4.01);
			this.drawRect(-2.0, -4.0, -1.0, -3.0, this.playerdata.eyes.color, 4.011);
		}
	}*/

	@Override
	public void render(float par2, float par3, float par4, float par5, float par6, float par7) {
		/*if (this.npc.animation.getEmotion()!=null) {
			EmotionConfig emotion = this.npc.animation.getEmotion();
			if (emotion.frame<0 || emotion.frame>=emotion.frames.size()) { this.npc.animation.activeEmtn = null; return; }
			PartEmotion part = emotion.frames.get(emotion.frame);
			if (part==null) { this.npc.animation.activeEmtn = null; return; }
			GlStateManager.pushMatrix();
			this.model.bipedHead.postRender(0.0625f);
			GlStateManager.scale(par7, par7, -par7);
			GlStateManager.translate(0.0f, (((this.playerdata.eyes.type == 1) ? 1 : 2) - this.playerdata.eyes.eyePos), 0.0f);
			GlStateManager.enableRescaleNormal();
			GlStateManager.shadeModel(7425);
			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.enableCull();
			GlStateManager.disableAlpha();
			GlStateManager.depthMask(false);
			int i = this.npc.getBrightnessForRender();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, i % 65536, i / 65536);
			Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
			(this.buffer = Tessellator.getInstance().getBuffer()).begin(7, DefaultVertexFormats.POSITION_COLOR);
			
			this.drawLeft(emotion, part, par7);
			this.drawRight(emotion, part, par7);
			this.drawBrows(emotion, part, par7);
			
			Tessellator.getInstance().draw();
			Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
			GlStateManager.depthMask(true);
			GlStateManager.disableBlend();
			GlStateManager.shadeModel(7424);
			GlStateManager.enableAlpha();
			GlStateManager.disableCull();
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
			GlStateManager.enableTexture2D();
			return;
		}*/
		if (!this.playerdata.eyes.isEnabled()) {
			return;
		}
		GlStateManager.pushMatrix();
		this.model.bipedHead.postRender(0.0625f);
		GlStateManager.scale(par7, par7, -par7);
		GlStateManager.translate(0.0f, (((this.playerdata.eyes.type == 1) ? 1 : 2) - this.playerdata.eyes.eyePos), 0.0f);
		GlStateManager.enableRescaleNormal();
		GlStateManager.shadeModel(7425);
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.enableCull();
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		int i = this.npc.getBrightnessForRender();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, i % 65536, i / 65536);
		Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
		(this.buffer = Tessellator.getInstance().getBuffer()).begin(7, DefaultVertexFormats.POSITION_COLOR);
		
		this.drawLeft();
		this.drawRight();
		this.drawBrows();
		
		Tessellator.getInstance().draw();
		Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.shadeModel(7424);
		GlStateManager.enableAlpha();
		GlStateManager.disableCull();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		GlStateManager.enableTexture2D();
	}

	@Override
	public void rotate(float par1, float par2, float par3, float par4, float par5, float par6) {
	}
}
