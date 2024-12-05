package noppes.npcs.client.layer;

import java.util.HashMap;
import java.util.Map;

import noppes.npcs.items.ItemNpcWand;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.animation.GuiNpcEmotion;
import noppes.npcs.client.model.ModelNpcAlt;
import noppes.npcs.client.model.animation.EmotionConfig;
import noppes.npcs.client.model.animation.EmotionFrame;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.controllers.AnimationController;

public class LayerEyes<T extends EntityLivingBase>
		extends LayerInterface<T> {

	private float alpha;

    public LayerEyes(RenderLiving<?> render) {
		super(render);
		this.alpha = 1.0f;
	}

	private void drawBrows(Float[] browRight, Float[] browLeft, Float[] eyeRight, Float[] eyeLeft, int h, int hoverColor, EmotionFrame frame) {
		float oYl = 0.0f, oYr = 0.0f;
		int cld = this.playerdata.eyes.closed;
		if (this.npc.getHealth() <= 0.0f || this.npc.isPlayerSleeping()) { cld = 1; }
		float oUp = 0.0f;
		if (this.playerdata.eyes.type == 1) { oUp = 0.3f; }

		float olx = 0.0f, oly = 0.0f, orx = 0.0f, ory = 0.0f, rerx = 0.0f, rery = 0.0f, lerx = 0.0f, lery = 0.0f;
		float slx = 1.0f, sly = 1.0f, srx = 1.0f, sry = 1.0f, srerx = 1.0f, srery = 1.0f, slerx = 1.0f, slery = 1.0f;
		float rl = 0.0f, rr = 0.0f, rer = 0.0f, ler = 0.0f;
		if (browRight != null) {
			orx = (float) (browRight[0] / Math.PI) * 0.25f;
			ory = (float) (browRight[1] / Math.PI) * 0.25f;
			srx = browRight[2];
			sry = browRight[3];
			rr = browRight[4];
		}
		if (browLeft != null) {
			olx = (float) (browLeft[0] / Math.PI) * 0.25f;
			oly = (float) (browLeft[1] / Math.PI) * 0.25f;
			slx = browLeft[2];
			sly = browLeft[3];
			rl = browLeft[4];
		}
		if (eyeRight != null) {
			rerx = (float) (eyeRight[0] / Math.PI) * 0.25f;
			rery = (float) (eyeRight[1] / Math.PI) * 0.25f;
			srerx = eyeRight[2];
			srery = eyeRight[3];
			rer = eyeRight[4];
		}
		if (eyeLeft != null) {
			lerx = (float) (eyeLeft[0] / Math.PI) * 0.25f;
			lery = (float) (eyeLeft[1] / Math.PI) * 0.25f;
			slerx = eyeLeft[2];
			slery = eyeLeft[3];
			ler = eyeLeft[4];
		}

		// skin close
		boolean close = false;
		if (frame != null) {
			close = this.playerdata.eyes.ticks > 3 && frame.isEndBlink();
		}
		if (cld != 0 || close) {
			if (cld == 1 || close || cld == 2) {
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.translate(0.99 + lerx, -5.0 + oUp + lery, 4.02f);
				if (ler != 0.0f) {
					float ry = 0.85f;
					GlStateManager.translate(1.01, ry, 0.0f);
					GlStateManager.rotate(ler, 0.0f, 0.0f, 1.0f);
					GlStateManager.translate(-1.01, -ry, 0.0f);
				}
				GlStateManager.scale(slerx, slery, 1.0f);
				this.drawRect(0, 0, 2.02, (this.playerdata.eyes.type != 0) ? 1.7f : 1, this.playerdata.eyes.skinColor, 0.0f);
				GlStateManager.popMatrix();
			}
			if (cld == 1 || close ||  cld == 3) {
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.translate(-0.99 + rerx, -5.0 + oUp + rery, 4.02f);
				if (rer != 0.0f) {
					float ry = 0.85f;
					GlStateManager.translate(-1.01, ry, 0.0f);
					GlStateManager.rotate(rer, 0.0f, 0.0f, 1.0f);
					GlStateManager.translate(1.01, -ry, 0.0f);
				}
				GlStateManager.scale(srerx, srery, 1.0f);
				this.drawRect(0, 0, -2.02, (this.playerdata.eyes.type != 0) ? 1.7f : 1, this.playerdata.eyes.skinColor, 0.0f);
				GlStateManager.popMatrix();
			}
		}

		// skin blink
		if (cld != 1 && this.playerdata.eyes.blinkStart > 0L && this.npc.isEntityAlive() && this.npc.deathTime == 0) {
			float f = (System.currentTimeMillis() - this.playerdata.eyes.blinkStart) / 150.0f;
			if (f > 1.0f) { f = 2.0f - f; }
			if (f < 0.0f) {
				this.playerdata.eyes.blinkStart = 0L;
				f = 0.0f;
			}
			if (cld == 0 || cld == 2) {
				oYl = ((this.playerdata.eyes.type != 0) ? 2.0f : 1.0f) * f;
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.translate(-0.99 + rerx, -5.0 + oUp + rery, 4.02f);
				if (rer != 0.0f) {
					float ry = 0.85f;
					GlStateManager.translate(-1.01, ry, 0.0f);
					GlStateManager.rotate(rer, 0.0f, 0.0f, 1.0f);
					GlStateManager.translate(1.01, -ry, 0.0f);
				}
				GlStateManager.scale(srerx, srery, 1.0f);
				this.drawRect(0, 0, -2.02, oYl, this.playerdata.eyes.skinColor, 0.0f);
				GlStateManager.popMatrix();
			}
			if (cld == 0 || cld == 3) {
				oYr = ((this.playerdata.eyes.type != 0) ? 2 : 1) * f;
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.translate(0.99 + lerx, -5.0 + oUp + lery, 4.02f);
				if (ler != 0.0f) {
					float ry = 0.85f;
					GlStateManager.translate(1.01, ry, 0.0f);
					GlStateManager.rotate(ler, 0.0f, 0.0f, 1.0f);
					GlStateManager.translate(-1.01, -ry, 0.0f);
				}
				GlStateManager.scale(slerx, slery, 1.0f);
				this.drawRect(0, 0, 2.02, oYr, this.playerdata.eyes.skinColor, 0.0f);
				GlStateManager.popMatrix();
			}
		}

		// brow
		if (this.playerdata.eyes.browThickness == 0) { return; }

		float thickness = this.playerdata.eyes.browThickness / 10.0f;
		oYl *= 0.075f;
		oYr *= 0.075f;
		if (this.playerdata.eyes.type == 0 ) { oYl -= 0.35f; oYr -= 0.35f; }

		// Right
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(-0.99 + orx, -4.8 + oYl + ory, 4.01f);
		if (rr != 0.0f) {
			float ry = thickness / -2.0f;
			GlStateManager.translate(-1.01, ry, 0.0f);
			GlStateManager.rotate(rr, 0.0f, 0.0f, 1.0f);
			GlStateManager.translate(1.01, -ry, 0.0f);
		}
		GlStateManager.scale(srx, sry, 1.0f);
		if (h == 1) {
			this.drawRect(0.125d, 0.125d, -2.145, -0.125 -thickness, hoverColor, 0.0D);
		}
		if (this.playerdata.eyes.type == 2) {
			float sH = (float) (this.playerdata.eyes.browThickness - 1) * 0.166667f + 0.333333f;
			float red = (float)(this.playerdata.eyes.browColor[0] >> 16 & 255) / 127.5F;
			float green = (float)(this.playerdata.eyes.browColor[0] >> 8 & 255) / 127.5F;
			float blue = (float)(this.playerdata.eyes.browColor[0] & 255) / 127.5F;
			GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
			GlStateManager.pushMatrix();
			GlStateManager.translate(-2.2f, -0.1f - thickness, 0.005f);
			GlStateManager.scale(0.009, 0.0035f * sH, 1.0f);
			GlStateManager.enableBlend();
			this.render.bindTexture(this.playerdata.eyes.browRight);
			GlStateManager.color(red, green, blue, 1.0f);
			this.drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, true);
			GlStateManager.popMatrix();
		}
		else { this.drawRect(0, 0, -2.02, -thickness, this.playerdata.eyes.browColor[0], 0.0D); }
		GlStateManager.popMatrix();

		// Left
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(0.99 + olx, -4.8 + oYr + oly, 4.01f);
		if (rl != 0.0f) {
			float ry = thickness / -2.0f;
			GlStateManager.translate(1.01, ry, 0.0f);
			GlStateManager.rotate(rl, 0.0f, 0.0f, 1.0f);
			GlStateManager.translate(-1.01, -ry, 0.0f);
		}
		GlStateManager.scale(slx, sly, 1.0f);
		if (h == 2) {
			this.drawRect(-0.125d, 0.125d, 2.145, -0.125 -thickness, hoverColor, 0.0D);
		}
		if (this.playerdata.eyes.type == 2) {
			float sH = (float) (this.playerdata.eyes.browThickness - 1) * 0.166667f + 0.333333f;
			float red = (float)(this.playerdata.eyes.browColor[0] >> 16 & 255) / 127.5F;
			float green = (float)(this.playerdata.eyes.browColor[0] >> 8 & 255) / 127.5F;
			float blue = (float)(this.playerdata.eyes.browColor[0] & 255) / 127.5F;
			GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
			GlStateManager.pushMatrix();
			GlStateManager.translate(-0.1f, -0.1f - thickness, 0.005f);
			GlStateManager.scale(0.009, 0.0035f * sH, 1.0f);
			GlStateManager.enableBlend();
			this.render.bindTexture(this.playerdata.eyes.browRight);
			GlStateManager.color(red, green, blue, 1.0f);
			this.drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, false);
			GlStateManager.popMatrix();
		}
		else { this.drawRect(0, 0, 2.02, -thickness, this.playerdata.eyes.browColor[1], 0.0D); }
		GlStateManager.popMatrix();
	}

	private void drawLeft(float[] s, Float[] eye, Float[] pupil, int h, int hoverColor) {
		int cld = this.playerdata.eyes.closed;
		if (cld == 1 || cld == 2 || this.playerdata.eyes.pattern == 1 || this.npc.isDead || this.npc.isPlayerSleeping()) { return; }
		float ex = 0.0f, ey = 0.0f, esx = 1.0f, esy = 1.0f, er = 0.0f;
		if (eye != null) {
			ex = (float) (eye[0] / Math.PI) * 0.25f;
			ey = (float) (eye[1] / Math.PI) * 0.25f;
			esx = eye[2];
			esy = eye[3];
			er = eye[4];
		}
		float px = s[4], py = s[5], psx = 1.0f, psy = 1.0f, pr = 0.0f;
		if (pupil != null) {
			px = (float) (pupil[0] / Math.PI) * 0.275f; // 0.55
			if (this.playerdata.eyes.type != 0) { py = (float) (pupil[1] / Math.PI) * 0.175f; } // 0.35
			else { py = (float) (pupil[1] / Math.PI) * 0.075f; } // 0.15
			psx = pupil[2];
			psy = pupil[3];
			pr = pupil[4];
		}
		// Eye
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(0.99 + ex, -5.0 + s[0] + ey, 4.01f);
		if (er != 0.0f) {
			float ry = (float) ((1.0 + s[1] - s[0]) / 2.0f);
			GlStateManager.translate(1.01, ry, 0.0f);
			GlStateManager.rotate(er, 0.0f, 0.0f, 1.0f);
			GlStateManager.translate(-1.01, -ry, 0.0f);
		}
		GlStateManager.scale(esx, esy, 1.0f);
		if (h == 1) {
			this.drawRect(-0.125d, -0.125d, 2.135, 1.125 + s[1] - s[0], hoverColor, 0.0D);
		}
		if (this.playerdata.eyes.type == 2) {
			float red = (float)(this.playerdata.eyes.eyeColor[1] >> 16 & 255) / 127.5F;
			float green = (float)(this.playerdata.eyes.eyeColor[1] >> 8 & 255) / 127.5F;
			float blue = (float)(this.playerdata.eyes.eyeColor[1] & 255) / 127.5F;
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.007875f, 0.0067204f, 1.0f);
			GlStateManager.enableBlend();
			this.render.bindTexture(this.playerdata.eyes.eyeLeft);
			GlStateManager.color(red, green, blue, 1.0f);
			this.drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, true);
			GlStateManager.popMatrix();
		}
		else {
			this.drawRect(0, 0, 2.01, 1.0 + s[1] - s[0], this.playerdata.eyes.eyeColor[0], 0.0D);
		}

		// Pupil
		if (!this.playerdata.eyes.activeLeft) { px = 0.0f; py = 0.0f; }
		if (this.playerdata.eyes.type == 0) { py -= 0.075f; }
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate((1.45 + px) * (0.3f * psx + 0.7f), (0.65 + py) * (float) (2.0f * Math.pow(psy, 3.0d) - 6.2f * Math.pow(psy, 2.0d) + 5.2f * psy) - s[0], 0.005f);
		if (pr != 0.0f) {
			float ry = (s[3] - s[2]) / 2.0f;
			GlStateManager.translate(-0.45, ry, 0.0f);
			GlStateManager.rotate(pr, 0.0f, 0.0f, 1.0f);
			GlStateManager.translate(0.45, -ry, 0.0f);
		}
		GlStateManager.scale(psx, psy, 1.0f);
		if (h == 2) {
			this.drawRect(0.125d, -0.125d, -1.025, 0.125 + s[3], hoverColor, 0.0D);
		}
		if (this.playerdata.eyes.type == 2) {
			float red = (float)(this.playerdata.eyes.pupilColor[1] >> 16 & 255) / 127.5F;
			float green = (float)(this.playerdata.eyes.pupilColor[1] >> 8 & 255) / 127.5F;
			float blue = (float)(this.playerdata.eyes.pupilColor[1] & 255) / 127.5F;
			GlStateManager.pushMatrix();
			GlStateManager.translate(-0.8f, 0.1f, 0.0f);
			GlStateManager.scale(0.003f, 0.003f, 1.0f);
			GlStateManager.enableBlend();
			this.render.bindTexture(this.playerdata.eyes.pupilLeft);
			GlStateManager.color(red, green, blue, 1.0f);
			this.drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, true);
			GlStateManager.popMatrix();
		}
		else { this.drawRect(0, s[2], -0.9, s[3], this.playerdata.eyes.pupilColor[1], 0.0D); }
		// Glint
		if (this.playerdata.eyes.glint && this.npc.isEntityAlive()) {
			if (this.playerdata.eyes.type == 2) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(-0.8f, 0.1f, 0.0f);
				GlStateManager.scale(0.003f, 0.003f, 1.0f);
				GlStateManager.enableBlend();
				this.render.bindTexture(this.playerdata.eyes.glintRes);
				GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
				this.drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, false);
				GlStateManager.popMatrix();
			}
			else { this.drawRect(-0.05, -0.35 + s[6], -0.3, -0.1 + s[7], 0xFFFFFFFF, 0.0D); }
		}
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
	}

	private void drawRight(float[] s, Float[] eye, Float[] pupil, int h, int hoverColor) {
		int cld = this.playerdata.eyes.closed;
		if (cld == 1 || cld == 3 || this.playerdata.eyes.pattern == 1 || this.npc.isDead || this.npc.isPlayerSleeping()) { return; }
		float ex = 0.0f, ey = 0.0f, esx = 1.0f, esy = 1.0f, er = 0.0f;
		if (eye != null) { // ofsX, ofsY, scX, scY, rot
			ex = (float) (eye[0] / Math.PI) * 0.25f;
			ey = (float) (eye[1] / Math.PI) * 0.25f;
			esx = eye[2];
			esy = eye[3];
			er = eye[4];
		}
		float px = s[4], py = s[5], psx = 1.0f, psy = 1.0f, pr = 0.0f;
		if (pupil != null) {
			px = (float) (pupil[0] / Math.PI) * 0.275f; // 0.55
			if (this.playerdata.eyes.type != 0) { py = (float) (pupil[1] / Math.PI) * 0.175f; } // 0.35
			else { py = (float) (pupil[1] / Math.PI) * 0.075f; } // 0.15
			psx = pupil[2];
			psy = pupil[3];
			pr = pupil[4];
		}
		// Eye
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(-0.99 + ex, -5.0 + s[0] + ey, 4.01f);
		if (er != 0.0f) {
			float ry = (float) ((1.0 + s[1] - s[0]) / 2.0f);
			GlStateManager.translate(-1.01, ry, 0.0f);
			GlStateManager.rotate(er, 0.0f, 0.0f, 1.0f);
			GlStateManager.translate(1.01, -ry, 0.0f);
		}
		GlStateManager.scale(esx, esy, 1.0f);
		if (h == 1) {
			this.drawRect(0.125d, -0.125d, -2.135, 1.125 + s[1] - s[0], hoverColor, 0.0D);
		}
		if (this.playerdata.eyes.type == 2) {
			float red = (float)(this.playerdata.eyes.eyeColor[0] >> 16 & 255) / 127.5F;
			float green = (float)(this.playerdata.eyes.eyeColor[0] >> 8 & 255) / 127.5F;
			float blue = (float)(this.playerdata.eyes.eyeColor[0] & 255) / 127.5F;
			GlStateManager.pushMatrix();
			GlStateManager.translate(-2.01f, 0.0f, 0.0f);
			GlStateManager.scale(0.007875f, 0.0067204f, 1.0f);
			GlStateManager.enableBlend();
			this.render.bindTexture(this.playerdata.eyes.eyeRight);
			GlStateManager.color(red, green, blue, 1.0f);
			this.drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, false);
			GlStateManager.popMatrix();
		}
		else {
			this.drawRect(0, 0, -2.01, 1.0 + s[1] - s[0], this.playerdata.eyes.eyeColor[1], 0.0D);
		}

		// Pupil
		if (!this.playerdata.eyes.activeRight) { px = 0.0f; py = 0.0f; }
		if (this.playerdata.eyes.type == 0) { py -= 0.075f; }
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate((-0.56 + px) * (-0.7f * psx + 1.7f), (0.65 + py) * (float) (2.0f * Math.pow(psy, 3.0d) - 6.2f * Math.pow(psy, 2.0d) + 5.2f * psy) - s[0], 0.005f);
		if (pr != 0.0f) {
			float ry = (s[3] - s[2]) / 2.0f;
			GlStateManager.translate(-0.45, ry, 0.0f);
			GlStateManager.rotate(pr, 0.0f, 0.0f, 1.0f);
			GlStateManager.translate(0.45, -ry, 0.0f);
		}
		GlStateManager.scale(psx, psy, 1.0f);
		if (h == 2) {
			this.drawRect(0.125d, -0.125d, -1.025, 0.125 + s[3], hoverColor, 0.0D);
		}
		if (this.playerdata.eyes.type == 2) {
			float red = (float)(this.playerdata.eyes.pupilColor[0] >> 16 & 255) / 127.5F;
			float green = (float)(this.playerdata.eyes.pupilColor[0] >> 8 & 255) / 127.5F;
			float blue = (float)(this.playerdata.eyes.pupilColor[0] & 255) / 127.5F;
			GlStateManager.pushMatrix();
			GlStateManager.translate(-0.8f, 0.1f, 0.0f);
			GlStateManager.scale(0.003f, 0.003f, 1.0f);
			GlStateManager.enableBlend();
			this.render.bindTexture(this.playerdata.eyes.pupilRight);
			GlStateManager.color(red, green, blue, 1.0f);
			this.drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, false);
			GlStateManager.popMatrix();
		}
		else { this.drawRect(0, s[2], -0.9, s[3], this.playerdata.eyes.pupilColor[0], 0.0D); }
		// Glint
		if (this.playerdata.eyes.glint && this.npc.isEntityAlive()) {
			if (this.playerdata.eyes.type == 2) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(-0.8f, 0.1f, 0.0f);
				GlStateManager.scale(0.003f, 0.003f, 1.0f);
				GlStateManager.enableBlend();
				this.render.bindTexture(this.playerdata.eyes.glintRes);
				GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
				this.drawTexturedModalRect(0, 0, 0, 0, 0, 256, 256, false);
				GlStateManager.popMatrix();
			}
			else { this.drawRect(-0.05, -0.35 + s[6], -0.3, -0.1 + s[7], 0xFFFFFFFF, 0.0D); }
		}
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
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
		float f0 = (color >> 24 & 0xFF) / 255.0f;
		if (f0 == 0.0f) { f0 = this.alpha; }
		float f1 = (color >> 16 & 0xFF) / 255.0f;
		float f2 = (color >> 8 & 0xFF) / 255.0f;
		float f3 = (color & 0xFF) / 255.0f;
		GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(x, y, z).color(f1, f2, f3, f0).endVertex();
		buffer.pos(x, y2, z).color(f1, f2, f3, f0).endVertex();
		buffer.pos(x2, y2, z).color(f1, f2, f3, f0).endVertex();
		buffer.pos(x2, y, z).color(f1, f2, f3, f0).endVertex();
		Tessellator.getInstance().draw();
	}

	@Override
	public void render(float par2, float par3, float par4, float par5, float par6, float par7) {
		if (!this.playerdata.eyes.isEnabled() || this.npc.display.getModel() != null || !this.npc.getClass().getSimpleName().equals("EntityCustomNpc") || !this.npc.animation.showParts.get(EnumParts.HEAD)) { return; }
		boolean isInvisible = false;
        Minecraft mc = Minecraft.getMinecraft();
		if (this.npc.display.getVisible() == 1) { isInvisible = this.npc.display.getAvailability().isAvailable(mc.player); }
		else if (this.npc.display.getVisible() == 2) { isInvisible = !(mc.player.getHeldItemMainhand().getItem() instanceof ItemNpcWand); }
		if (isInvisible) { alpha = 0.5f; }
		else if (this.npc.equals(ModelNpcAlt.editAnimDataSelect.displayNpc) && ModelNpcAlt.editAnimDataSelect.part != EnumParts.HEAD) {
			if (ModelNpcAlt.editAnimDataSelect.alpha >= 1.0f) { alpha = 1.0f; } else { alpha = 0.5f; }
		}
		else { alpha = 1.0f; }

		GlStateManager.pushMatrix();
		this.model.bipedHead.postRender(0.0625f);
		if (this.npc.isSneaking()) { GlStateManager.translate(0.0F, -0.2F, 0.0F); }
		GlStateManager.scale(par7, par7, -par7);
		GlStateManager.translate(0.0f, (((this.playerdata.eyes.type != 0) ? 1 : 2) - this.playerdata.eyes.eyePos), 0.0f);
		GlStateManager.enableRescaleNormal();
		GlStateManager.shadeModel(7425);
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.enableCull();
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.disableLighting();
		int i = this.npc.getBrightnessForRender();

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, i % 65536.0F, i / 65536.0F);
		mc.entityRenderer.setupFogColor(true);
		float oU = 0.0f, oD = 0.0f, pU = -0.4f, pD = 0.25f, ox = 0.0f, oy = 0.0f, gU = 0.0f, gD = 0.0f;
		if (this.playerdata.eyes.type != 0) { oU = 0.3f; oD = 1.0f; pU = 0.0f; pD = 1.0f; gU = 0.7f; gD = 0.15f; }
		Map<Integer, Float[]> emotionData = new HashMap<>();
		EmotionFrame frame = npc.animation.getCurrentEmotionFrame();
		EmotionConfig activeEmotion = npc.animation.getActiveEmotion();
		int baseEmotionId = npc.animation.getBaseEmotionId();
		if (activeEmotion == null && baseEmotionId >= 0) {
			activeEmotion = (EmotionConfig) AnimationController.getInstance().getEmotion(baseEmotionId);
		}
		if (CustomNpcs.ShowCustomAnimation && activeEmotion != null) {
			emotionData = npc.animation.getEmotionData();
			if (frame != null && frame.isBlink()) {
				playerdata.eyes.ticks = (int) (npc.world.getTotalWorldTime() - npc.animation.getStartEmotionTime());
				if (playerdata.eyes.ticks == 0) { playerdata.eyes.blinkStart = System.currentTimeMillis(); }
			}
			else if (!activeEmotion.canBlink()) {
				this.playerdata.eyes.blinkStart = -20L;
			}
		}
		else {
			if (this.npc.lookAt != null || (this.npc.lookPos[0] != -1 && this.npc.lookPos[1] != -1)) {
				float yaw, pitch;
				if (this.npc.lookAt != null) {
					double d0 = this.npc.posX - this.npc.lookAt.posX;
					double d1 = (this.npc.posY + (double) this.npc.getEyeHeight()) - (this.npc.lookAt.posY + (double) this.npc.lookAt.getEyeHeight());
					double d2 = this.npc.posZ - this.npc.lookAt.posZ;
					double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
					yaw = MathHelper.wrapDegrees(this.npc.rotationYawHead - (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F) / (npc.ais.getStandingType() == 4 ? 2.0f : 1.0f);
					pitch = MathHelper.wrapDegrees(this.npc.rotationPitch + (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI)))) / (npc.ais.getStandingType() == 4 ? 2.0f : 1.0f);
				} else {
					yaw = this.npc.lookPos[0];
					pitch = this.npc.lookPos[1];
				}
				if (yaw < -45.0f) { yaw = -45.0f; }
				else if (yaw > 45.0f) { yaw = 45.0f; }
				if (pitch < -45.0f) { pitch = -45.0f; }
				else if (pitch > 45.0f) { pitch = 45.0f; }
				if (this.playerdata.eyes.type == 2) {
					ox = (float) (Math.sin(yaw * Math.PI / 180D) * (-0.006667D * (pitch < 0 ? -pitch : pitch) + 0.8D));
					oy = (float) (Math.sin(pitch * Math.PI / 180D) * (-0.003333D * (yaw < 0 ? -yaw : yaw) + 0.6D));
				} else {
					ox = yaw / 89.0f; // 45 -> 0.505
					if (this.playerdata.eyes.type == 1) { oy = pitch / 150.0f; } // 45 -> 0.505
					else { oy = pitch / 320.0f; } // 45 -> 0.505
				}
			}
		}
		float[] s = new float[] { oU, oD, pU, pD, ox, -oy, gU, gD };
		int e = -1;
		boolean r = true;
		if (mc.currentScreen instanceof GuiNpcEmotion && ((GuiNpcEmotion) mc.currentScreen).npcEmtn.equals(this.npc)) {
			e = ((GuiNpcEmotion) mc.currentScreen).elementType;
			r = ((GuiNpcEmotion) mc.currentScreen).isRight;
			this.playerdata.eyes.update(this.npc);
		}
		int t = (int) (this.npc.world.getTotalWorldTime() % 40L);
		String a = Integer.toHexString((int) ((-12.25f * t + 255.0f) * (t > 20 ? -1.0f : 1.0f))) + "00FF00";
		int c = (int) Long.parseLong(a, 16);
		int h = 0;

		if (e < 2 && !r) { h = e + 1; }
		this.drawLeft(s, emotionData.get(1), emotionData.get(3), h, c);
		h = 0;
		if (e < 2 && r) { h = e + 1; }
		this.drawRight(s, emotionData.get(0), emotionData.get(2), h, c);
		h = 0;
		if (e == 2) { h = r ? 1 : 2; }
		this.drawBrows(emotionData.get(4), emotionData.get(5), emotionData.get(0), emotionData.get(1), h, c, frame);

		mc.entityRenderer.setupFogColor(false);
		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.shadeModel(7424);
		GlStateManager.enableAlpha();
		GlStateManager.disableCull();
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
		GlStateManager.enableTexture2D();
	}

	@Override
	public void rotate(float par1, float par2, float par3, float par4, float par5, float par6) { }

	public void drawTexturedModalRect(double x, double y, double z, int textureX, int textureY, int width, int height, boolean rev) {
		float f = 0.00390625F;
		GlStateManager.enableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();
		GlStateManager.depthMask(true);
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		if (rev) {
			bufferbuilder.pos(x, y + height, z).tex((textureX + width) * f, (textureY + height) * f).endVertex();
			bufferbuilder.pos(x + width, y + height, z).tex(textureX * f, (textureY + height) * f).endVertex();
			bufferbuilder.pos(x + width, y, z).tex(textureX * f, textureY * f).endVertex();
			bufferbuilder.pos(x, y, z).tex((textureX + width) * f, textureY * f).endVertex();
		} else {
			bufferbuilder.pos(x, y + height, z).tex((textureX) * f, (textureY + height) * f).endVertex();
			bufferbuilder.pos(x + width, y + height, z).tex((textureX + width) * f, (textureY + height) * f).endVertex();
			bufferbuilder.pos(x + width, y, z).tex((textureX + width) * f, textureY * f).endVertex();
			bufferbuilder.pos(x, y, z).tex(textureX * f, textureY* f).endVertex();
		}
		tessellator.draw();
		GlStateManager.disableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
	}

}
