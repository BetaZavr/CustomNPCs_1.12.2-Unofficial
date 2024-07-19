package noppes.npcs.client.model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.LogWriter;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityNpcPony;

public class ModelPony extends ModelBase {
	public boolean aimedBow;
	public ModelRenderer Body;
	public ModelPlaneRenderer[] Bodypiece;
	public ModelRenderer Head;
	public ModelRenderer[] Headpiece;
	public int heldItemRight;
	public ModelRenderer Helmet;
	public boolean isFlying;
	public boolean isPegasus;
	public boolean isSleeping;
	public boolean isSneak;
	public boolean isUnicorn;
	public ModelRenderer LeftArm;
	public ModelRenderer LeftLeg;
	public ModelRenderer[] LeftWing;
	public ModelRenderer[] LeftWingExt;
    public ModelRenderer RightArm;
	public ModelRenderer RightLeg;
	public ModelRenderer[] RightWing;
	public ModelRenderer[] RightWingExt;
	public ModelPlaneRenderer[] Tail;
	public ModelRenderer unicornarm;
	private float WingRotateAngleZ;

	public ModelPony(float f) {
		this.init(f, 0.0f);
	}

	public void init(float scale, float f) {
		float f2 = 0.0f;
		float f3 = 0.0f;
		float f4 = 0.0f;
		(this.Head = new ModelRenderer(this, 0, 0)).addBox(-4.0f, -4.0f, -6.0f, 8, 8, 8, scale);
		this.Head.setRotationPoint(f2, f3 + f, f4);
		this.Headpiece = new ModelRenderer[3];
		(this.Headpiece[0] = new ModelRenderer(this, 12, 16)).addBox(-4.0f, -6.0f, -1.0f, 2, 2, 2, scale);
		this.Headpiece[0].setRotationPoint(f2, f3 + f, f4);
		(this.Headpiece[1] = new ModelRenderer(this, 12, 16)).addBox(2.0f, -6.0f, -1.0f, 2, 2, 2, scale);
		this.Headpiece[1].setRotationPoint(f2, f3 + f, f4);
		(this.Headpiece[2] = new ModelRenderer(this, 56, 0)).addBox(-0.5f, -10.0f, -4.0f, 1, 4, 1, scale);
		this.Headpiece[2].setRotationPoint(f2, f3 + f, f4);
		(this.Helmet = new ModelRenderer(this, 32, 0)).addBox(-4.0f, -4.0f, -6.0f, 8, 8, 8, scale + 0.5f);
		this.Helmet.setRotationPoint(f2, f3, f4);
		float f5 = 0.0f;
		float f6 = 0.0f;
		float f7 = 0.0f;
		(this.Body = new ModelRenderer(this, 16, 16)).addBox(-4.0f, 4.0f, -2.0f, 8, 8, 4, scale);
		this.Body.setRotationPoint(f5, f6 + f, f7);
		this.Bodypiece = new ModelPlaneRenderer[13];
		(this.Bodypiece[0] = new ModelPlaneRenderer(this, 24, 0)).addSidePlane(-4.0f, 4.0f, 2.0f, 8, 8, scale);
		this.Bodypiece[0].setRotationPoint(f5, f6 + f, f7);
		(this.Bodypiece[1] = new ModelPlaneRenderer(this, 24, 0)).addSidePlane(4.0f, 4.0f, 2.0f, 8, 8, scale);
		this.Bodypiece[1].setRotationPoint(f5, f6 + f, f7);
		(this.Bodypiece[2] = new ModelPlaneRenderer(this, 24, 0)).addTopPlane(-4.0f, 4.0f, 2.0f, 8, 8, scale);
		this.Bodypiece[2].setRotationPoint(f2, f3 + f, f4);
		(this.Bodypiece[3] = new ModelPlaneRenderer(this, 24, 0)).addTopPlane(-4.0f, 12.0f, 2.0f, 8, 8, scale);
		this.Bodypiece[3].setRotationPoint(f2, f3 + f, f4);
		(this.Bodypiece[4] = new ModelPlaneRenderer(this, 0, 20)).addSidePlane(-4.0f, 4.0f, 10.0f, 8, 4, scale);
		this.Bodypiece[4].setRotationPoint(f5, f6 + f, f7);
		(this.Bodypiece[5] = new ModelPlaneRenderer(this, 0, 20)).addSidePlane(4.0f, 4.0f, 10.0f, 8, 4, scale);
		this.Bodypiece[5].setRotationPoint(f5, f6 + f, f7);
		(this.Bodypiece[6] = new ModelPlaneRenderer(this, 24, 0)).addTopPlane(-4.0f, 4.0f, 10.0f, 8, 4, scale);
		this.Bodypiece[6].setRotationPoint(f2, f3 + f, f4);
		(this.Bodypiece[7] = new ModelPlaneRenderer(this, 24, 0)).addTopPlane(-4.0f, 12.0f, 10.0f, 8, 4, scale);
		this.Bodypiece[7].setRotationPoint(f2, f3 + f, f4);
		(this.Bodypiece[8] = new ModelPlaneRenderer(this, 24, 0)).addBackPlane(-4.0f, 4.0f, 14.0f, 8, 8, scale);
		this.Bodypiece[8].setRotationPoint(f2, f3 + f, f4);
		(this.Bodypiece[9] = new ModelPlaneRenderer(this, 32, 0)).addTopPlane(-1.0f, 10.0f, 8.0f, 2, 6, scale);
		this.Bodypiece[9].setRotationPoint(f2, f3 + f, f4);
		(this.Bodypiece[10] = new ModelPlaneRenderer(this, 32, 0)).addTopPlane(-1.0f, 12.0f, 8.0f, 2, 6, scale);
		this.Bodypiece[10].setRotationPoint(f2, f3 + f, f4);
		this.Bodypiece[11] = new ModelPlaneRenderer(this, 32, 0);
		this.Bodypiece[11].mirror = true;
		this.Bodypiece[11].addSidePlane(-1.0f, 10.0f, 8.0f, 2, 6, scale);
		this.Bodypiece[11].setRotationPoint(f2, f3 + f, f4);
		(this.Bodypiece[12] = new ModelPlaneRenderer(this, 32, 0)).addSidePlane(1.0f, 10.0f, 8.0f, 2, 6, scale);
		this.Bodypiece[12].setRotationPoint(f2, f3 + f, f4);
		(this.RightArm = new ModelRenderer(this, 40, 16)).addBox(-2.0f, 4.0f, -2.0f, 4, 12, 4, scale);
		this.RightArm.setRotationPoint(-3.0f, 8.0f + f, 0.0f);
		this.LeftArm = new ModelRenderer(this, 40, 16);
		this.LeftArm.mirror = true;
		this.LeftArm.addBox(-2.0f, 4.0f, -2.0f, 4, 12, 4, scale);
		this.LeftArm.setRotationPoint(3.0f, 8.0f + f, 0.0f);
		(this.RightLeg = new ModelRenderer(this, 40, 16)).addBox(-2.0f, 4.0f, -2.0f, 4, 12, 4, scale);
		this.RightLeg.setRotationPoint(-3.0f, 0.0f + f, 0.0f);
		this.LeftLeg = new ModelRenderer(this, 40, 16);
		this.LeftLeg.mirror = true;
		this.LeftLeg.addBox(-2.0f, 4.0f, -2.0f, 4, 12, 4, scale);
		this.LeftLeg.setRotationPoint(3.0f, 0.0f + f, 0.0f);
		(this.unicornarm = new ModelRenderer(this, 40, 16)).addBox(-3.0f, -2.0f, -2.0f, 4, 12, 4, scale);
		this.unicornarm.setRotationPoint(-5.0f, 2.0f + f, 0.0f);
		float f8 = 0.0f;
		float f9 = 8.0f;
		float f10 = -14.0f;
		float f11 = 0.0f - f8;
		float f12 = 10.0f - f9;
		float f13 = 0.0f;
		this.Tail = new ModelPlaneRenderer[10];
		(this.Tail[0] = new ModelPlaneRenderer(this, 32, 0)).addTopPlane(-2.0f + f8, -7.0f + f9, 16.0f + f10, 4, 4,
				scale);
		this.Tail[0].setRotationPoint(f11, f12 + f, f13);
		(this.Tail[1] = new ModelPlaneRenderer(this, 32, 0)).addTopPlane(-2.0f + f8, 9.0f + f9, 16.0f + f10, 4, 4,
				scale);
		this.Tail[1].setRotationPoint(f11, f12 + f, f13);
		(this.Tail[2] = new ModelPlaneRenderer(this, 32, 0)).addBackPlane(-2.0f + f8, -7.0f + f9, 16.0f + f10, 4, 8,
				scale);
		this.Tail[2].setRotationPoint(f11, f12 + f, f13);
		(this.Tail[3] = new ModelPlaneRenderer(this, 32, 0)).addBackPlane(-2.0f + f8, -7.0f + f9, 20.0f + f10, 4, 8,
				scale);
		this.Tail[3].setRotationPoint(f11, f12 + f, f13);
		(this.Tail[4] = new ModelPlaneRenderer(this, 32, 0)).addBackPlane(-2.0f + f8, 1.0f + f9, 16.0f + f10, 4, 8,
				scale);
		this.Tail[4].setRotationPoint(f11, f12 + f, f13);
		(this.Tail[5] = new ModelPlaneRenderer(this, 32, 0)).addBackPlane(-2.0f + f8, 1.0f + f9, 20.0f + f10, 4, 8,
				scale);
		this.Tail[5].setRotationPoint(f11, f12 + f, f13);
		this.Tail[6] = new ModelPlaneRenderer(this, 36, 0);
		this.Tail[6].mirror = true;
		this.Tail[6].addSidePlane(2.0f + f8, -7.0f + f9, 16.0f + f10, 8, 4, scale);
		this.Tail[6].setRotationPoint(f11, f12 + f, f13);
		(this.Tail[7] = new ModelPlaneRenderer(this, 36, 0)).addSidePlane(-2.0f + f8, -7.0f + f9, 16.0f + f10, 8, 4,
				scale);
		this.Tail[7].setRotationPoint(f11, f12 + f, f13);
		this.Tail[8] = new ModelPlaneRenderer(this, 36, 0);
		this.Tail[8].mirror = true;
		this.Tail[8].addSidePlane(2.0f + f8, 1.0f + f9, 16.0f + f10, 8, 4, scale);
		this.Tail[8].setRotationPoint(f11, f12 + f, f13);
		(this.Tail[9] = new ModelPlaneRenderer(this, 36, 0)).addSidePlane(-2.0f + f8, 1.0f + f9, 16.0f + f10, 8, 4,
				scale);
		this.Tail[9].setRotationPoint(f11, f12 + f, f13);
		float f14 = 0.0f;
		float f15 = 0.0f;
		float f16 = 0.0f;
		(this.LeftWing = new ModelRenderer[3])[0] = new ModelRenderer(this, 56, 16);
		this.LeftWing[0].mirror = true;
		this.LeftWing[0].addBox(4.0f, 5.0f, 2.0f, 2, 6, 2, scale);
		this.LeftWing[0].setRotationPoint(f14, f15 + f, f16);
		this.LeftWing[1] = new ModelRenderer(this, 56, 16);
		this.LeftWing[1].mirror = true;
		this.LeftWing[1].addBox(4.0f, 5.0f, 4.0f, 2, 8, 2, scale);
		this.LeftWing[1].setRotationPoint(f14, f15 + f, f16);
		this.LeftWing[2] = new ModelRenderer(this, 56, 16);
		this.LeftWing[2].mirror = true;
		this.LeftWing[2].addBox(4.0f, 5.0f, 6.0f, 2, 6, 2, scale);
		this.LeftWing[2].setRotationPoint(f14, f15 + f, f16);
		this.RightWing = new ModelRenderer[3];
		(this.RightWing[0] = new ModelRenderer(this, 56, 16)).addBox(-6.0f, 5.0f, 2.0f, 2, 6, 2, scale);
		this.RightWing[0].setRotationPoint(f14, f15 + f, f16);
		(this.RightWing[1] = new ModelRenderer(this, 56, 16)).addBox(-6.0f, 5.0f, 4.0f, 2, 8, 2, scale);
		this.RightWing[1].setRotationPoint(f14, f15 + f, f16);
		(this.RightWing[2] = new ModelRenderer(this, 56, 16)).addBox(-6.0f, 5.0f, 6.0f, 2, 6, 2, scale);
		this.RightWing[2].setRotationPoint(f14, f15 + f, f16);
		float f17 = f2 + 4.5f;
		float f18 = f3 + 5.0f;
		float f19 = f4 + 6.0f;
		(this.LeftWingExt = new ModelRenderer[7])[0] = new ModelRenderer(this, 56, 19);
		this.LeftWingExt[0].mirror = true;
		this.LeftWingExt[0].addBox(0.0f, 0.0f, 0.0f, 1, 8, 2, scale + 0.1f);
		this.LeftWingExt[0].setRotationPoint(f17, f18 + f, f19);
		this.LeftWingExt[1] = new ModelRenderer(this, 56, 19);
		this.LeftWingExt[1].mirror = true;
		this.LeftWingExt[1].addBox(0.0f, 8.0f, 0.0f, 1, 6, 2, scale + 0.1f);
		this.LeftWingExt[1].setRotationPoint(f17, f18 + f, f19);
		this.LeftWingExt[2] = new ModelRenderer(this, 56, 19);
		this.LeftWingExt[2].mirror = true;
		this.LeftWingExt[2].addBox(0.0f, -1.2f, -0.2f, 1, 8, 2, scale - 0.2f);
		this.LeftWingExt[2].setRotationPoint(f17, f18 + f, f19);
		this.LeftWingExt[3] = new ModelRenderer(this, 56, 19);
		this.LeftWingExt[3].mirror = true;
		this.LeftWingExt[3].addBox(0.0f, 1.8f, 1.3f, 1, 8, 2, scale - 0.1f);
		this.LeftWingExt[3].setRotationPoint(f17, f18 + f, f19);
		this.LeftWingExt[4] = new ModelRenderer(this, 56, 19);
		this.LeftWingExt[4].mirror = true;
		this.LeftWingExt[4].addBox(0.0f, 5.0f, 2.0f, 1, 8, 2, scale);
		this.LeftWingExt[4].setRotationPoint(f17, f18 + f, f19);
		this.LeftWingExt[5] = new ModelRenderer(this, 56, 19);
		this.LeftWingExt[5].mirror = true;
		this.LeftWingExt[5].addBox(0.0f, 0.0f, -0.2f, 1, 6, 2, scale + 0.3f);
		this.LeftWingExt[5].setRotationPoint(f17, f18 + f, f19);
		this.LeftWingExt[6] = new ModelRenderer(this, 56, 19);
		this.LeftWingExt[6].mirror = true;
		this.LeftWingExt[6].addBox(0.0f, 0.0f, 0.2f, 1, 3, 2, scale + 0.2f);
		this.LeftWingExt[6].setRotationPoint(f17, f18 + f, f19);
		float f20 = f2 - 4.5f;
		float f21 = f3 + 5.0f;
		float f22 = f4 + 6.0f;
		(this.RightWingExt = new ModelRenderer[7])[0] = new ModelRenderer(this, 56, 19);
		this.RightWingExt[0].mirror = true;
		this.RightWingExt[0].addBox(0.0f, 0.0f, 0.0f, 1, 8, 2, scale + 0.1f);
		this.RightWingExt[0].setRotationPoint(f20, f21 + f, f22);
		this.RightWingExt[1] = new ModelRenderer(this, 56, 19);
		this.RightWingExt[1].mirror = true;
		this.RightWingExt[1].addBox(0.0f, 8.0f, 0.0f, 1, 6, 2, scale + 0.1f);
		this.RightWingExt[1].setRotationPoint(f20, f21 + f, f22);
		this.RightWingExt[2] = new ModelRenderer(this, 56, 19);
		this.RightWingExt[2].mirror = true;
		this.RightWingExt[2].addBox(0.0f, -1.2f, -0.2f, 1, 8, 2, scale - 0.2f);
		this.RightWingExt[2].setRotationPoint(f20, f21 + f, f22);
		this.RightWingExt[3] = new ModelRenderer(this, 56, 19);
		this.RightWingExt[3].mirror = true;
		this.RightWingExt[3].addBox(0.0f, 1.8f, 1.3f, 1, 8, 2, scale - 0.1f);
		this.RightWingExt[3].setRotationPoint(f20, f21 + f, f22);
		this.RightWingExt[4] = new ModelRenderer(this, 56, 19);
		this.RightWingExt[4].mirror = true;
		this.RightWingExt[4].addBox(0.0f, 5.0f, 2.0f, 1, 8, 2, scale);
		this.RightWingExt[4].setRotationPoint(f20, f21 + f, f22);
		this.RightWingExt[5] = new ModelRenderer(this, 56, 19);
		this.RightWingExt[5].mirror = true;
		this.RightWingExt[5].addBox(0.0f, 0.0f, -0.2f, 1, 6, 2, scale + 0.3f);
		this.RightWingExt[5].setRotationPoint(f20, f21 + f, f22);
		this.RightWingExt[6] = new ModelRenderer(this, 56, 19);
		this.RightWingExt[6].mirror = true;
		this.RightWingExt[6].addBox(0.0f, 0.0f, 0.2f, 1, 3, 2, scale + 0.2f);
		this.RightWingExt[6].setRotationPoint(f20, f21 + f, f22);
		this.WingRotateAngleZ = this.LeftWingExt[0].rotateAngleZ;
	}

	public void render(@Nonnull Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		EntityNpcPony pony = (EntityNpcPony) entity;
		if (pony.textureLocation != pony.checked && pony.textureLocation != null) {
			try {
				IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(pony.textureLocation);
				BufferedImage bufferedimage = ImageIO.read(resource.getInputStream());
				pony.isPegasus = false;
				pony.isUnicorn = false;
				Color color = new Color(bufferedimage.getRGB(0, 0), true);
                Color color3 = new Color(136, 202, 240, 255);
				Color color4 = new Color(209, 159, 228, 255);
				Color color5 = new Color(254, 249, 252, 255);
                if (color.equals(color3)) {
					pony.isPegasus = true;
				}
				if (color.equals(color4)) {
					pony.isUnicorn = true;
				}
				if (color.equals(color5)) {
					pony.isPegasus = true;
					pony.isUnicorn = true;
				}
				pony.checked = pony.textureLocation;
			} catch (IOException e) { LogWriter.error("Error:", e); }
		}
		this.isSleeping = pony.isPlayerSleeping();
		this.isUnicorn = pony.isUnicorn;
		this.isPegasus = pony.isPegasus;
		this.isSneak = pony.isSneaking();
        pony.getHeldItemMainhand();
        this.heldItemRight = 1;
		this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		GlStateManager.pushMatrix();
		if (this.isSleeping) {
			GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
			GlStateManager.translate(0.0f, -0.5f, -0.9f);
		}
        this.Head.render(f5);
		this.Headpiece[0].render(f5);
		this.Headpiece[1].render(f5);
		if (this.isUnicorn) {
			this.Headpiece[2].render(f5);
		}
		this.Helmet.render(f5);
		this.Body.render(f5);
        for (ModelPlaneRenderer planeRenderer : this.Bodypiece) {
            planeRenderer.render(f5);
        }
		this.LeftArm.render(f5);
		this.RightArm.render(f5);
		this.LeftLeg.render(f5);
		this.RightLeg.render(f5);
        for (ModelPlaneRenderer modelPlaneRenderer : this.Tail) {
            modelPlaneRenderer.render(f5);
        }
		if (this.isPegasus) {
			if (this.isFlying || this.isSneak) {
                for (ModelRenderer modelRenderer : this.LeftWingExt) {
                    modelRenderer.render(f5);
                }
                for (ModelRenderer modelRenderer : this.RightWingExt) {
                    modelRenderer.render(f5);
                }
			} else {
                for (ModelRenderer modelRenderer : this.LeftWing) {
                    modelRenderer.render(f5);
                }
                for (ModelRenderer modelRenderer : this.RightWing) {
                    modelRenderer.render(f5);
                }
			}
		}
		GlStateManager.popMatrix();
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, @Nonnull Entity entity) {
		EntityNPCInterface npc = (EntityNPCInterface) entity;
		this.isRiding = npc.isRiding();
		if (this.isSneak && (npc.currentAnimation == 7 || npc.currentAnimation == 2)) {
			this.isSneak = false;
		}
        boolean rainboom = false;
		float f6;
		float f7;
		if (this.isSleeping) {
			f6 = 1.4f;
			f7 = 0.1f;
		} else {
			f6 = f3 / 57.29578f;
			f7 = f4 / 57.29578f;
		}
		this.Head.rotateAngleY = f6;
		this.Head.rotateAngleX = f7;
		this.Headpiece[0].rotateAngleY = f6;
		this.Headpiece[0].rotateAngleX = f7;
		this.Headpiece[1].rotateAngleY = f6;
		this.Headpiece[1].rotateAngleX = f7;
		this.Headpiece[2].rotateAngleY = f6;
		this.Headpiece[2].rotateAngleX = f7;
		this.Helmet.rotateAngleY = f6;
		this.Helmet.rotateAngleX = f7;
		this.Headpiece[2].rotateAngleX = f7 + 0.5f;
		float f8;
		float f9;
		float f10;
		float f11;
		if (!this.isFlying || !this.isPegasus) {
			f8 = MathHelper.cos(f * 0.6662f + 3.141593f) * 0.6f * f1;
			f9 = MathHelper.cos(f * 0.6662f) * 0.6f * f1;
			f10 = MathHelper.cos(f * 0.6662f) * 0.3f * f1;
			f11 = MathHelper.cos(f * 0.6662f + 3.141593f) * 0.3f * f1;
			this.RightArm.rotateAngleY = 0.0f;
			this.unicornarm.rotateAngleY = 0.0f;
			this.LeftArm.rotateAngleY = 0.0f;
			this.RightLeg.rotateAngleY = 0.0f;
			this.LeftLeg.rotateAngleY = 0.0f;
		} else {
			if (f1 < 0.9999f) {
                f8 = MathHelper.sin(0.0f - f1 * 0.5f);
				f9 = MathHelper.sin(0.0f - f1 * 0.5f);
				f10 = MathHelper.sin(f1 * 0.5f);
				f11 = MathHelper.sin(f1 * 0.5f);
			} else {
				rainboom = true;
				f8 = 4.712f;
				f9 = 4.712f;
				f10 = 1.571f;
				f11 = 1.571f;
			}
			this.RightArm.rotateAngleY = 0.2f;
			this.LeftArm.rotateAngleY = -0.2f;
			this.RightLeg.rotateAngleY = -0.2f;
			this.LeftLeg.rotateAngleY = 0.2f;
		}
		if (this.isSleeping) {
			f8 = 4.712f;
			f9 = 4.712f;
			f10 = 1.571f;
			f11 = 1.571f;
		}
		this.RightArm.rotateAngleX = f8;
		this.unicornarm.rotateAngleX = 0.0f;
		this.LeftArm.rotateAngleX = f9;
		this.RightLeg.rotateAngleX = f10;
		this.LeftLeg.rotateAngleX = f11;
		this.RightArm.rotateAngleZ = 0.0f;
		this.unicornarm.rotateAngleZ = 0.0f;
		this.LeftArm.rotateAngleZ = 0.0f;
        for (ModelPlaneRenderer planeRenderer : this.Tail) {
            if (rainboom) {
                planeRenderer.rotateAngleZ = 0.0f;
            } else {
                planeRenderer.rotateAngleZ = MathHelper.cos(f * 0.8f) * 0.2f * f1;
            }
        }
		if (this.heldItemRight != 0 && !rainboom && !this.isUnicorn) {
			this.RightArm.rotateAngleX = this.RightArm.rotateAngleX * 0.5f - 0.3141593f;
		}
		float f12 = 0.0f;
		if (f5 > -9990.0f && !this.isUnicorn) {
			f12 = MathHelper.sin(MathHelper.sqrt(f5) * 3.141593f * 2.0f) * 0.2f;
		}
		this.Body.rotateAngleY = f12 * 0.2f;
        for (ModelPlaneRenderer planeRenderer : this.Bodypiece) {
            planeRenderer.rotateAngleY = f12 * 0.2f;
        }
        for (ModelRenderer modelRenderer : this.LeftWing) {
            modelRenderer.rotateAngleY = f12 * 0.2f;
        }
        for (ModelRenderer modelRenderer : this.RightWing) {
            modelRenderer.rotateAngleY = f12 * 0.2f;
        }
        for (ModelPlaneRenderer planeRenderer : this.Tail) {
            planeRenderer.rotateAngleY = f12;
        }
		float f13 = MathHelper.sin(this.Body.rotateAngleY) * 5.0f;
		float f14 = MathHelper.cos(this.Body.rotateAngleY) * 5.0f;
		float f15 = 4.0f;
		if (this.isSneak && !this.isFlying) {
			f15 = 0.0f;
		}
		if (this.isSleeping) {
			f15 = 2.6f;
		}
		if (rainboom) {
			this.RightArm.rotationPointZ = f13 + 2.0f;
			this.LeftArm.rotationPointZ = 0.0f - f13 + 2.0f;
		} else {
			this.RightArm.rotationPointZ = f13 + 1.0f;
			this.LeftArm.rotationPointZ = 0.0f - f13 + 1.0f;
		}
		this.RightArm.rotationPointX = 0.0f - f14 - 1.0f + f15;
		this.LeftArm.rotationPointX = f14 + 1.0f - f15;
		this.RightLeg.rotationPointX = 0.0f - f14 - 1.0f + f15;
		this.LeftLeg.rotationPointX = f14 + 1.0f - f15;
		this.RightArm.rotateAngleY += this.Body.rotateAngleY;
		this.LeftArm.rotateAngleY += this.Body.rotateAngleY;
		this.LeftArm.rotateAngleX += this.Body.rotateAngleX;
		this.RightArm.rotationPointY = 8.0f;
		this.LeftArm.rotationPointY = 8.0f;
		this.RightLeg.rotationPointY = 4.0f;
		this.LeftLeg.rotationPointY = 4.0f;
		if (f5 > -9990.0f) {
			float f16;
			f16 = 1.0f - f5;
			f16 *= f16 * f16;
			f16 = 1.0f - f16;
			float f17 = MathHelper.sin(f16 * 3.141593f);
			float f18 = MathHelper.sin(f5 * 3.141593f);
			float f19 = f18 * -(this.Head.rotateAngleX - 0.7f) * 0.75f;
            this.unicornarm.rotateAngleX -= (float) (f17 * 1.2 + f19);
            this.unicornarm.rotateAngleY += this.Body.rotateAngleY * 2.0f;
            this.unicornarm.rotateAngleZ = f18 * -0.4f;
        }
		if (this.isSneak && !this.isFlying) {
			float f20 = 0.4f;
			float f21 = 7.0f;
			float f22 = -4.0f;
			this.Body.rotateAngleX = f20;
			this.Body.rotationPointY = f21;
			this.Body.rotationPointZ = f22;
            for (ModelPlaneRenderer modelPlaneRenderer : this.Bodypiece) {
                modelPlaneRenderer.rotateAngleX = f20;
                modelPlaneRenderer.rotationPointY = f21;
                modelPlaneRenderer.rotationPointZ = f22;
            }
			float f23 = 3.5f;
			float f24 = 6.0f;
            for (ModelRenderer modelRenderer : this.LeftWingExt) {
                modelRenderer.rotateAngleX = f20 + 2.3561947345733643f;
                modelRenderer.rotationPointY = f21 + f23;
                modelRenderer.rotationPointZ = f22 + f24;
                modelRenderer.rotateAngleX = 2.5f;
                modelRenderer.rotateAngleZ = -6.0f;
            }
			float f25 = 4.5f;
			float f26 = 6.0f;
			for (int i5 = 0; i5 < this.LeftWingExt.length; ++i5) {
				this.RightWingExt[i5].rotateAngleX = f20 + 2.3561947345733643f;
				this.RightWingExt[i5].rotationPointY = f21 + f25;
				this.RightWingExt[i5].rotationPointZ = f22 + f26;
				this.RightWingExt[i5].rotateAngleX = 2.5f;
				this.RightWingExt[i5].rotateAngleZ = 6.0f;
			}
			this.RightLeg.rotateAngleX -= 0.0f;
			this.LeftLeg.rotateAngleX -= 0.0f;
			this.RightArm.rotateAngleX -= 0.4f;
			this.unicornarm.rotateAngleX += 0.4f;
			this.LeftArm.rotateAngleX -= 0.4f;
			this.RightLeg.rotationPointZ = 10.0f;
			this.LeftLeg.rotationPointZ = 10.0f;
			this.RightLeg.rotationPointY = 7.0f;
			this.LeftLeg.rotationPointY = 7.0f;
			float f27;
			float f28;
			float f29;
			if (this.isSleeping) {
				f27 = 2.0f;
				f28 = -1.0f;
				f29 = 1.0f;
			} else {
				f27 = 6.0f;
				f28 = -2.0f;
				f29 = 0.0f;
			}
			this.Head.rotationPointY = f27;
			this.Head.rotationPointZ = f28;
			this.Head.rotationPointX = f29;
			this.Helmet.rotationPointY = f27;
			this.Helmet.rotationPointZ = f28;
			this.Helmet.rotationPointX = f29;
			this.Headpiece[0].rotationPointY = f27;
			this.Headpiece[0].rotationPointZ = f28;
			this.Headpiece[0].rotationPointX = f29;
			this.Headpiece[1].rotationPointY = f27;
			this.Headpiece[1].rotationPointZ = f28;
			this.Headpiece[1].rotationPointX = f29;
			this.Headpiece[2].rotationPointY = f27;
			this.Headpiece[2].rotationPointZ = f28;
			this.Headpiece[2].rotationPointX = f29;
			float f30 = 0.0f;
			float f31 = 8.0f;
			float f32 = -14.0f;
			float f33 = 0.0f - f30;
			float f34 = 9.0f - f31;
			float f35 = -4.0f - f32;
			float f36 = 0.0f;
            for (ModelPlaneRenderer modelPlaneRenderer : this.Tail) {
                modelPlaneRenderer.rotationPointX = f33;
                modelPlaneRenderer.rotationPointY = f34;
                modelPlaneRenderer.rotationPointZ = f35;
                modelPlaneRenderer.rotateAngleX = f36;
            }
		} else {
			float f37 = 0.0f;
			float f38 = 0.0f;
			float f39 = 0.0f;
			this.Body.rotateAngleX = f37;
			this.Body.rotationPointY = f38;
			this.Body.rotationPointZ = f39;
            for (ModelPlaneRenderer planeRenderer : this.Bodypiece) {
                planeRenderer.rotateAngleX = f37;
                planeRenderer.rotationPointY = f38;
                planeRenderer.rotationPointZ = f39;
            }
			if (this.isPegasus) {
				if (!this.isFlying) {
                    for (ModelRenderer modelRenderer : this.LeftWing) {
                        modelRenderer.rotateAngleX = f37 + 1.5707964897155762f;
                        modelRenderer.rotationPointY = f38 + 13.0f;
                        modelRenderer.rotationPointZ = f39 - 3.0f;
                    }
                    for (ModelRenderer modelRenderer : this.RightWing) {
                        modelRenderer.rotateAngleX = f37 + 1.5707964897155762f;
                        modelRenderer.rotationPointY = f38 + 13.0f;
                        modelRenderer.rotationPointZ = f39 - 3.0f;
                    }
				} else {
					float f40 = 5.5f;
					float f41 = 3.0f;
                    for (ModelRenderer modelRenderer : this.LeftWingExt) {
                        modelRenderer.rotateAngleX = f37 + 1.5707964897155762f;
                        modelRenderer.rotationPointY = f38 + f40;
                        modelRenderer.rotationPointZ = f39 + f41;
                    }
					float f42 = 6.5f;
					float f43 = 3.0f;
                    for (ModelRenderer modelRenderer : this.RightWingExt) {
                        modelRenderer.rotateAngleX = f37 + 1.5707964897155762f;
                        modelRenderer.rotationPointY = f38 + f42;
                        modelRenderer.rotationPointZ = f39 + f43;
                    }
				}
			}
			this.RightLeg.rotationPointZ = 10.0f;
			this.LeftLeg.rotationPointZ = 10.0f;
			this.RightLeg.rotationPointY = 8.0f;
			this.LeftLeg.rotationPointY = 8.0f;
			float f44 = MathHelper.cos(f2 * 0.09f) * 0.05f + 0.05f;
			float f45 = MathHelper.sin(f2 * 0.067f) * 0.05f;
			this.unicornarm.rotateAngleZ += f44;
			this.unicornarm.rotateAngleX += f45;
			if (this.isPegasus && this.isFlying) {
				this.WingRotateAngleZ = MathHelper.sin(f2 * 0.067f * 8.0f);
                for (ModelRenderer modelRenderer : this.LeftWingExt) {
                    modelRenderer.rotateAngleX = 2.5f;
                    modelRenderer.rotateAngleZ = -this.WingRotateAngleZ - 4.712f - 0.4f;
                }
                for (ModelRenderer modelRenderer : this.RightWingExt) {
                    modelRenderer.rotateAngleX = 2.5f;
                    modelRenderer.rotateAngleZ = this.WingRotateAngleZ + 4.712f + 0.4f;
                }
			}
			float f46;
			float f47;
			float f48;
			if (this.isSleeping) {
				f46 = 2.0f;
				f47 = 1.0f;
				f48 = 1.0f;
			} else {
				f46 = 0.0f;
				f47 = 0.0f;
				f48 = 0.0f;
			}
			this.Head.rotationPointY = f46;
			this.Head.rotationPointZ = f47;
			this.Head.rotationPointX = f48;
			this.Helmet.rotationPointY = f46;
			this.Helmet.rotationPointZ = f47;
			this.Helmet.rotationPointX = f48;
			this.Headpiece[0].rotationPointY = f46;
			this.Headpiece[0].rotationPointZ = f47;
			this.Headpiece[0].rotationPointX = f48;
			this.Headpiece[1].rotationPointY = f46;
			this.Headpiece[1].rotationPointZ = f47;
			this.Headpiece[1].rotationPointX = f48;
			this.Headpiece[2].rotationPointY = f46;
			this.Headpiece[2].rotationPointZ = f47;
			this.Headpiece[2].rotationPointX = f48;
			float f49 = 0.0f;
			float f50 = 8.0f;
			float f51 = -14.0f;
			float f52 = 0.0f - f49;
			float f53 = 9.0f - f50;
			float f54 = 0.0f - f51;
			float f55 = 0.5f * f1;
            for (ModelPlaneRenderer modelPlaneRenderer : this.Tail) {
                modelPlaneRenderer.rotationPointX = f52;
                modelPlaneRenderer.rotationPointY = f53;
                modelPlaneRenderer.rotationPointZ = f54;
                if (rainboom) {
                    modelPlaneRenderer.rotateAngleX = 1.571f + 0.1f * MathHelper.sin(f);
                } else {
                    modelPlaneRenderer.rotateAngleX = f55;
                }
            }
            for (ModelPlaneRenderer planeRenderer : this.Tail) {
                if (!rainboom) {
                    planeRenderer.rotateAngleX += f45;
                }
            }
		}
		this.LeftWingExt[2].rotateAngleX -= 0.85f;
		this.LeftWingExt[3].rotateAngleX -= 0.75f;
		this.LeftWingExt[4].rotateAngleX -= 0.5f;
		this.LeftWingExt[6].rotateAngleX -= 0.85f;
		this.RightWingExt[2].rotateAngleX -= 0.85f;
		this.RightWingExt[3].rotateAngleX -= 0.75f;
		this.RightWingExt[4].rotateAngleX -= 0.5f;
		this.RightWingExt[6].rotateAngleX -= 0.85f;
		this.Bodypiece[9].rotateAngleX += 0.5f;
		this.Bodypiece[10].rotateAngleX += 0.5f;
		this.Bodypiece[11].rotateAngleX += 0.5f;
		this.Bodypiece[12].rotateAngleX += 0.5f;
		if (rainboom) {
            for (ModelPlaneRenderer modelPlaneRenderer : this.Tail) {
                modelPlaneRenderer.rotationPointY += 6.0f;
                ++modelPlaneRenderer.rotationPointZ;
            }
		}
		if (this.isSleeping) {
			this.RightArm.rotationPointZ += 6.0f;
			this.LeftArm.rotationPointZ += 6.0f;
			this.RightLeg.rotationPointZ -= 8.0f;
			this.LeftLeg.rotationPointZ -= 8.0f;
			this.RightArm.rotationPointY += 2.0f;
			this.LeftArm.rotationPointY += 2.0f;
			this.RightLeg.rotationPointY += 2.0f;
			this.LeftLeg.rotationPointY += 2.0f;
		}
		if (this.aimedBow) {
			if (this.isUnicorn) {
				float f56 = 0.0f;
				float f57 = 0.0f;
				this.unicornarm.rotateAngleZ = 0.0f;
				this.unicornarm.rotateAngleY = -(0.1f - f56 * 0.6f) + this.Head.rotateAngleY;
				this.unicornarm.rotateAngleX = 4.712f + this.Head.rotateAngleX;
				this.unicornarm.rotateAngleX -= f56 * 1.2f - f57 * 0.4f;
                this.unicornarm.rotateAngleZ += MathHelper.cos(f2 * 0.09f) * 0.05f + 0.05f;
				this.unicornarm.rotateAngleX += MathHelper.sin(f2 * 0.067f) * 0.05f;
			} else {
				float f59 = 0.0f;
				float f60 = 0.0f;
				this.RightArm.rotateAngleZ = 0.0f;
				this.RightArm.rotateAngleY = -(0.1f - f59 * 0.6f) + this.Head.rotateAngleY;
				this.RightArm.rotateAngleX = 4.712f + this.Head.rotateAngleX;
				this.RightArm.rotateAngleX -= f59 * 1.2f - f60 * 0.4f;
				this.RightArm.rotateAngleZ += MathHelper.cos(f2 * 0.09f) * 0.05f + 0.05f;
				this.RightArm.rotateAngleX += MathHelper.sin(f2 * 0.067f) * 0.05f;
				++this.RightArm.rotationPointZ;
			}
		}
	}
}
