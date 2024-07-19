package noppes.npcs.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class ModelPonyArmor extends ModelBase {
	public boolean aimedBow;
	public ModelRenderer Body;
	public ModelRenderer BodyBack;
	public ModelRenderer head;
	public int heldItemRight;
	public boolean isFlying;
	public boolean isGlow;
	public boolean isPegasus;
	public boolean isSleeping;
	public boolean isSneak;
	public boolean isUnicorn;
	public ModelRenderer LeftArm;
	public ModelRenderer LeftArm2;
	public ModelRenderer LeftLeg;
	public ModelRenderer LeftLeg2;
    public ModelRenderer rightarm;
	public ModelRenderer rightarm2;
	public ModelRenderer RightLeg;
	public ModelRenderer RightLeg2;

	public ModelPonyArmor(float f) {
		this.isPegasus = false;
		this.isUnicorn = false;
		this.isSleeping = false;
		this.isFlying = false;
		this.isGlow = false;
		this.isSneak = false;
		this.init(f, 0.0f);
	}

	public void init(float scale, float f) {
		float f2 = 0.0f;
		float f3 = 0.0f;
		float f4 = 0.0f;
		(this.head = new ModelRenderer(this, 0, 0)).addBox(-4.0f, -4.0f, -6.0f, 8, 8, 8, scale);
		this.head.setRotationPoint(f2, f3, f4);
		float f5 = 0.0f;
		float f6 = 0.0f;
		float f7 = 0.0f;
		(this.Body = new ModelRenderer(this, 16, 16)).addBox(-4.0f, 4.0f, -2.0f, 8, 8, 4, scale);
		this.Body.setRotationPoint(f5, f6 + f, f7);
		(this.BodyBack = new ModelRenderer(this, 0, 0)).addBox(-4.0f, 4.0f, 6.0f, 8, 8, 8, scale);
		this.BodyBack.setRotationPoint(f5, f6 + f, f7);
		(this.rightarm = new ModelRenderer(this, 0, 16)).addBox(-2.0f, 4.0f, -2.0f, 4, 12, 4, scale);
		this.rightarm.setRotationPoint(-3.0f, 8.0f + f, 0.0f);
		this.LeftArm = new ModelRenderer(this, 0, 16);
		this.LeftArm.mirror = true;
		this.LeftArm.addBox(-2.0f, 4.0f, -2.0f, 4, 12, 4, scale);
		this.LeftArm.setRotationPoint(3.0f, 8.0f + f, 0.0f);
		(this.RightLeg = new ModelRenderer(this, 0, 16)).addBox(-2.0f, 4.0f, -2.0f, 4, 12, 4, scale);
		this.RightLeg.setRotationPoint(-3.0f, 0.0f + f, 0.0f);
		this.LeftLeg = new ModelRenderer(this, 0, 16);
		this.LeftLeg.mirror = true;
		this.LeftLeg.addBox(-2.0f, 4.0f, -2.0f, 4, 12, 4, scale);
		this.LeftLeg.setRotationPoint(3.0f, 0.0f + f, 0.0f);
		(this.rightarm2 = new ModelRenderer(this, 0, 16)).addBox(-2.0f, 4.0f, -2.0f, 4, 12, 4, scale * 0.5f);
		this.rightarm2.setRotationPoint(-3.0f, 8.0f + f, 0.0f);
		this.LeftArm2 = new ModelRenderer(this, 0, 16);
		this.LeftArm2.mirror = true;
		this.LeftArm2.addBox(-2.0f, 4.0f, -2.0f, 4, 12, 4, scale * 0.5f);
		this.LeftArm2.setRotationPoint(3.0f, 8.0f + f, 0.0f);
		(this.RightLeg2 = new ModelRenderer(this, 0, 16)).addBox(-2.0f, 4.0f, -2.0f, 4, 12, 4, scale * 0.5f);
		this.RightLeg2.setRotationPoint(-3.0f, 0.0f + f, 0.0f);
		this.LeftLeg2 = new ModelRenderer(this, 0, 16);
		this.LeftLeg2.mirror = true;
		this.LeftLeg2.addBox(-2.0f, 4.0f, -2.0f, 4, 12, 4, scale * 0.5f);
		this.LeftLeg2.setRotationPoint(3.0f, 0.0f + f, 0.0f);
	}

	public void render(@Nonnull Entity entity, float f, float f1, float f2, float f3, float f4, float scale) {
		this.setRotationAngles(f, f1, f2, f3, f4, scale, entity);
		this.head.render(scale);
		this.Body.render(scale);
		this.BodyBack.render(scale);
		this.LeftArm.render(scale);
		this.rightarm.render(scale);
		this.LeftLeg.render(scale);
		this.RightLeg.render(scale);
		this.LeftArm2.render(scale);
		this.rightarm2.render(scale);
		this.LeftLeg2.render(scale);
		this.RightLeg2.render(scale);
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, @Nonnull Entity entity) {
		EntityNPCInterface npc = (EntityNPCInterface) entity;
		if (!this.isRiding) {
			this.isRiding = (npc.currentAnimation == 1);
		}
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
		this.head.rotateAngleY = f6;
		this.head.rotateAngleX = f7;
		float f8;
		float f9;
		float f10;
		float f11;
		if (!this.isFlying || !this.isPegasus) {
			f8 = MathHelper.cos(f * 0.6662f + 3.141593f) * 0.6f * f1;
			f9 = MathHelper.cos(f * 0.6662f) * 0.6f * f1;
			f10 = MathHelper.cos(f * 0.6662f) * 0.3f * f1;
			f11 = MathHelper.cos(f * 0.6662f + 3.141593f) * 0.3f * f1;
			this.rightarm.rotateAngleY = 0.0f;
			this.LeftArm.rotateAngleY = 0.0f;
			this.RightLeg.rotateAngleY = 0.0f;
			this.LeftLeg.rotateAngleY = 0.0f;
			this.rightarm2.rotateAngleY = 0.0f;
			this.LeftArm2.rotateAngleY = 0.0f;
			this.RightLeg2.rotateAngleY = 0.0f;
			this.LeftLeg2.rotateAngleY = 0.0f;
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
			this.rightarm.rotateAngleY = 0.2f;
			this.LeftArm.rotateAngleY = -0.2f;
			this.RightLeg.rotateAngleY = -0.2f;
			this.LeftLeg.rotateAngleY = 0.2f;
			this.rightarm2.rotateAngleY = 0.2f;
			this.LeftArm2.rotateAngleY = -0.2f;
			this.RightLeg2.rotateAngleY = -0.2f;
			this.LeftLeg2.rotateAngleY = 0.2f;
		}
		if (this.isSleeping) {
			f8 = 4.712f;
			f9 = 4.712f;
			f10 = 1.571f;
			f11 = 1.571f;
		}
		this.rightarm.rotateAngleX = f8;
		this.LeftArm.rotateAngleX = f9;
		this.RightLeg.rotateAngleX = f10;
		this.LeftLeg.rotateAngleX = f11;
		this.rightarm.rotateAngleZ = 0.0f;
		this.LeftArm.rotateAngleZ = 0.0f;
		this.rightarm2.rotateAngleX = f8;
		this.LeftArm2.rotateAngleX = f9;
		this.RightLeg2.rotateAngleX = f10;
		this.LeftLeg2.rotateAngleX = f11;
		this.rightarm2.rotateAngleZ = 0.0f;
		this.LeftArm2.rotateAngleZ = 0.0f;
		if (this.heldItemRight != 0 && !rainboom && !this.isUnicorn) {
			this.rightarm.rotateAngleX = this.rightarm.rotateAngleX * 0.5f - 0.3141593f;
			this.rightarm2.rotateAngleX = this.rightarm2.rotateAngleX * 0.5f - 0.3141593f;
		}
		float f12 = 0.0f;
		if (f5 > -9990.0f && !this.isUnicorn) {
			f12 = MathHelper.sin(MathHelper.sqrt(f5) * 3.141593f * 2.0f) * 0.2f;
		}
		this.Body.rotateAngleY = f12 * 0.2f;
		this.BodyBack.rotateAngleY = f12 * 0.2f;
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
			this.rightarm.rotationPointZ = f13 + 2.0f;
			this.rightarm2.rotationPointZ = f13 + 2.0f;
			this.LeftArm.rotationPointZ = 0.0f - f13 + 2.0f;
			this.LeftArm2.rotationPointZ = 0.0f - f13 + 2.0f;
		} else {
			this.rightarm.rotationPointZ = f13 + 1.0f;
			this.rightarm2.rotationPointZ = f13 + 1.0f;
			this.LeftArm.rotationPointZ = 0.0f - f13 + 1.0f;
			this.LeftArm2.rotationPointZ = 0.0f - f13 + 1.0f;
		}
		this.rightarm.rotationPointX = 0.0f - f14 - 1.0f + f15;
		this.rightarm2.rotationPointX = 0.0f - f14 - 1.0f + f15;
		this.LeftArm.rotationPointX = f14 + 1.0f - f15;
		this.LeftArm2.rotationPointX = f14 + 1.0f - f15;
		this.RightLeg.rotationPointX = 0.0f - f14 - 1.0f + f15;
		this.RightLeg2.rotationPointX = 0.0f - f14 - 1.0f + f15;
		this.LeftLeg.rotationPointX = f14 + 1.0f - f15;
		this.LeftLeg2.rotationPointX = f14 + 1.0f - f15;
        this.rightarm.rotateAngleY += this.Body.rotateAngleY;
        this.rightarm2.rotateAngleY += this.Body.rotateAngleY;
        this.LeftArm.rotateAngleY += this.Body.rotateAngleY;
        this.LeftArm2.rotateAngleY += this.Body.rotateAngleY;
        this.LeftArm.rotateAngleX += this.Body.rotateAngleX;
        this.LeftArm2.rotateAngleX += this.Body.rotateAngleX;
		this.rightarm.rotationPointY = 8.0f;
		this.LeftArm.rotationPointY = 8.0f;
		this.rightarm2.rotationPointY = 8.0f;
		this.LeftArm2.rotationPointY = 8.0f;
		if (this.isSneak && !this.isFlying) {
			float f19 = 0.4f;
			float f20 = 7.0f;
			float f21 = -4.0f;
			this.Body.rotateAngleX = f19;
			this.Body.rotationPointY = f20;
			this.Body.rotationPointZ = f21;
			this.BodyBack.rotateAngleX = f19;
			this.BodyBack.rotationPointY = f20;
			this.BodyBack.rotationPointZ = f21;
			this.RightLeg.rotateAngleX -= 0.0f;
			this.LeftLeg.rotateAngleX -= 0.0f;
			this.rightarm.rotateAngleX -= 0.4f;
			this.LeftArm.rotateAngleX -= 0.4f;
			this.RightLeg.rotationPointZ = 10.0f;
			this.LeftLeg.rotationPointZ = 10.0f;
			this.RightLeg.rotationPointY = 7.0f;
			this.LeftLeg.rotationPointY = 7.0f;
			this.RightLeg2.rotateAngleX -= 0.0f;
			this.LeftLeg2.rotateAngleX -= 0.0f;
			this.rightarm2.rotateAngleX -= 0.4f;
			this.LeftArm2.rotateAngleX -= 0.4f;
			this.RightLeg2.rotationPointZ = 10.0f;
			this.LeftLeg2.rotationPointZ = 10.0f;
			this.RightLeg2.rotationPointY = 7.0f;
			this.LeftLeg2.rotationPointY = 7.0f;
			float f22;
			float f23;
			float f24;
			if (this.isSleeping) {
				f22 = 2.0f;
				f23 = -1.0f;
				f24 = 1.0f;
			} else {
				f22 = 6.0f;
				f23 = -2.0f;
				f24 = 0.0f;
			}
			this.head.rotationPointY = f22;
			this.head.rotationPointZ = f23;
			this.head.rotationPointX = f24;
		} else {
			float f25 = 0.0f;
			float f26 = 0.0f;
			float f27 = 0.0f;
			this.Body.rotateAngleX = f25;
			this.Body.rotationPointY = f26;
			this.Body.rotationPointZ = f27;
			this.BodyBack.rotateAngleX = f25;
			this.BodyBack.rotationPointY = f26;
			this.BodyBack.rotationPointZ = f27;
			this.RightLeg.rotationPointZ = 10.0f;
			this.LeftLeg.rotationPointZ = 10.0f;
			this.RightLeg.rotationPointY = 8.0f;
			this.LeftLeg.rotationPointY = 8.0f;
			this.RightLeg2.rotationPointZ = 10.0f;
			this.LeftLeg2.rotationPointZ = 10.0f;
			this.RightLeg2.rotationPointY = 8.0f;
			this.LeftLeg2.rotationPointY = 8.0f;
			this.head.rotationPointY = 0.0f;
			this.head.rotationPointZ = 0.0f;
		}
		if (this.isSleeping) {
			this.rightarm.rotationPointZ += 6.0f;
			this.LeftArm.rotationPointZ += 6.0f;
			this.RightLeg.rotationPointZ -= 8.0f;
			this.LeftLeg.rotationPointZ -= 8.0f;
			this.rightarm.rotationPointY += 2.0f;
			this.LeftArm.rotationPointY += 2.0f;
			this.RightLeg.rotationPointY += 2.0f;
			this.LeftLeg.rotationPointY += 2.0f;
			this.rightarm2.rotationPointZ += 6.0f;
			this.LeftArm2.rotationPointZ += 6.0f;
			this.RightLeg2.rotationPointZ -= 8.0f;
			this.LeftLeg2.rotationPointZ -= 8.0f;
			this.rightarm2.rotationPointY += 2.0f;
			this.LeftArm2.rotationPointY += 2.0f;
			this.RightLeg2.rotationPointY += 2.0f;
			this.LeftLeg2.rotationPointY += 2.0f;
		}
		if (this.aimedBow && !this.isUnicorn) {
			this.rightarm.rotateAngleZ = 0.0f;
			this.rightarm.rotateAngleY = -(0.1f) + this.head.rotateAngleY;
			this.rightarm.rotateAngleX = 4.712f + this.head.rotateAngleX;
			this.rightarm.rotateAngleZ += MathHelper.cos(f2 * 0.09f) * 0.05f + 0.05f;
			this.rightarm.rotateAngleX += MathHelper.sin(f2 * 0.067f) * 0.05f;
			this.rightarm2.rotateAngleZ = 0.0f;
			this.rightarm2.rotateAngleY = -(0.1f - f2 * 0.6f) + this.head.rotateAngleY;
			this.rightarm2.rotateAngleX = 4.712f + this.head.rotateAngleX;
			this.rightarm2.rotateAngleX -= f2 * 1.2f - f2 * 0.4f;
			this.rightarm2.rotateAngleZ += MathHelper.cos(f2 * 0.09f) * 0.05f + 0.05f;
			this.rightarm2.rotateAngleX += MathHelper.sin(f2 * 0.067f) * 0.05f;
			++this.rightarm.rotationPointZ;
			++this.rightarm2.rotationPointZ;
		}
	}
}
