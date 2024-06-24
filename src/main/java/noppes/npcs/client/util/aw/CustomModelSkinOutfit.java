package noppes.npcs.client.util.aw;

import java.util.ArrayList;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import moe.plushie.armourers_workshop.api.common.skin.Point3D;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkin;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinPart;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.EnumFacing;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityNPCInterface;

/** Changed AWMod.client.model.skin.ModelSkinOutfit extends ModelTypeHelper */
public class CustomModelSkinOutfit extends ModelBiped {

	public void render(EntityNPCInterface npc, ISkin skin, ModelBiped modelBiped, Object renderData, float scale, Map<EnumParts, Boolean> ba) {
		if (skin == null || npc == null) {
			return;
		}
		try {
			ArrayList<ISkinPart> parts = skin.getSubParts();
			this.isSneak = npc.isSneaking();
			this.isRiding = npc.isRiding();

			GlStateManager.pushAttrib();
			RenderHelper.enableGUIStandardItemLighting();
			ArmourersWorkshopUtil awu = ArmourersWorkshopUtil.getInstance();
			if ((boolean) awu.hasPaintData.invoke(skin) & (boolean) awu.isShowSkinPaint.invoke(renderData)
					& awu.getTexturePaintType.invoke(awu.clientProxy) == awu.TEXTURE_REPLACE) {
				Object st = awu.getTextureForSkin.invoke(awu.clientSkinPaintCache, skin,
						awu.getSkinDye.invoke(renderData), awu.getExtraColours.invoke(renderData));
				awu.bindTexture.invoke(st);
				GL11.glPushMatrix();
				GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
				GL11.glDisable(GL11.GL_CULL_FACE);
				GL11.glEnable(GL11.GL_ALPHA_TEST);
				if (!((boolean) awu.isItemRender.invoke(renderData))) {
					GlStateManager.enablePolygonOffset();
					GlStateManager.doPolygonOffset(-2, 1);
				}
				if (ba.get(EnumParts.HEAD)) {
					bipedHead.render(scale);
				}
				if (ba.get(EnumParts.BODY)) {
					bipedBody.render(scale);
				}
				if (ba.get(EnumParts.ARM_LEFT)) {
					bipedLeftArm.render(scale);
				}
				if (ba.get(EnumParts.ARM_RIGHT)) {
					bipedRightArm.render(scale);
				}
				GL11.glTranslated(0, 0, 0.005F);
				GL11.glTranslated(0.02F, 0, 0);
				if (ba.get(EnumParts.LEG_LEFT)) {
					bipedLeftLeg.render(scale);
				}
				GL11.glTranslated(-0.02F, 0, 0);
				if (ba.get(EnumParts.LEG_RIGHT)) {
					bipedRightLeg.render(scale);
				}
				GL11.glTranslated(0, 0, -0.005F);
				if (!((boolean) awu.isItemRender.invoke(renderData))) {
					GlStateManager.doPolygonOffset(0F, 0F);
					GlStateManager.disablePolygonOffset();
				}
				GL11.glPopAttrib();
				GL11.glPopMatrix();
			}

			for (int i = 0; i < parts.size(); i++) {
				ISkinPart part = parts.get(i);
				GL11.glPushMatrix();
				if (isChild) {
					float f6 = 2.0F;
					GL11.glScalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
					GL11.glTranslatef(0.0F, 24.0F * scale, 0.0F);
				}
				if (part.getPartType().getRegistryName().equals("armourers:head.base") && ba.get(EnumParts.HEAD)) {
					GL11.glPushMatrix();
					GlStateManager.translate(0.0f, -1.5f, 0.0f);
					GlStateManager.scale(2.0f, 2.0f, 2.0f);
					modelBiped.bipedHead.postRender(scale);
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				}

				if (part.getPartType().getRegistryName().equals("armourers:chest.base") && ba.get(EnumParts.BODY)) {
					GL11.glPushMatrix();
					GlStateManager.translate(0.0f, -1.5f, 0.0f);
					GlStateManager.scale(2.0f, 2.0f, 2.0f);
					modelBiped.bipedBody.postRender(scale);
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				} else if (part.getPartType().getRegistryName().equals("armourers:chest.leftArm") && ba.get(EnumParts.ARM_LEFT)) {
					GL11.glPushMatrix();
					GlStateManager.translate(0.0f, -1.5f, 0.0f);
					GlStateManager.scale(2.0f, 2.0f, 2.0f);
					modelBiped.bipedLeftArm.postRender(scale);
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				} else if (part.getPartType().getRegistryName().equals("armourers:chest.rightArm") && ba.get(EnumParts.ARM_RIGHT)) {
					GL11.glPushMatrix();
					GlStateManager.translate(0.0f, -1.5f, 0.0f);
					GlStateManager.scale(2.0f, 2.0f, 2.0f);
					modelBiped.bipedRightArm.postRender(scale);
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				}

				if (part.getPartType().getRegistryName().equals("armourers:legs.leftLeg") && ba.get(EnumParts.LEG_LEFT)) {
					GL11.glPushMatrix();
					GlStateManager.translate(0.0f, -1.5f, 0.0f);
					GlStateManager.scale(2.0f, 2.0f, 2.0f);
					modelBiped.bipedLeftLeg.postRender(scale);
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				} else if (part.getPartType().getRegistryName().equals("armourers:legs.rightLeg") && ba.get(EnumParts.LEG_RIGHT)) {
					GL11.glPushMatrix();
					GlStateManager.translate(0.0f, -1.5f, 0.0f);
					GlStateManager.scale(2.0f, 2.0f, 2.0f);
					modelBiped.bipedRightLeg.postRender(scale);
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				} else if (part.getPartType().getRegistryName().equals("armourers:legs.skirt")) {
					GL11.glPushMatrix();
					GlStateManager.translate(0.0f, -1.5f, 0.0f);
					GlStateManager.scale(2.0f, 2.0f, 2.0f);
					modelBiped.bipedRightLeg.postRender(scale);
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				}

				if (part.getPartType().getRegistryName().equals("armourers:feet.leftFoot") && ba.get(EnumParts.LEG_LEFT)) {
					GL11.glPushMatrix();
					GlStateManager.translate(0.0f, -1.5f, 0.0f);
					GlStateManager.scale(2.0f, 2.0f, 2.0f);
					modelBiped.bipedLeftLeg.postRender(scale);
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				} else if (part.getPartType().getRegistryName().equals("armourers:feet.rightFoot") && ba.get(EnumParts.LEG_RIGHT)) {
					GL11.glPushMatrix();
					GlStateManager.translate(0.0f, -1.5f, 0.0f);
					GlStateManager.scale(2.0f, 2.0f, 2.0f);
					modelBiped.bipedRightLeg.postRender(scale);
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				}

				double angle = (double) awu.getFlapAngleForWings.invoke(awu.skinUtils, npc, skin, i);
				if (part.getPartType().getRegistryName().equals("armourers:wings.leftWing")) {
					GL11.glPushMatrix();
					GlStateManager.translate(0.0f, -1.5f, 0.0f);
					GlStateManager.scale(2.0f, 2.0f, 2.0f);
					modelBiped.bipedBody.postRender(scale);
					renderLeftWing(awu, awu.skinPartRenderDataConstructor.newInstance(part, renderData), angle);
					GL11.glPopMatrix();
				}
				if (part.getPartType().getRegistryName().equals("armourers:wings.rightWing")) {
					GL11.glPushMatrix();
					GlStateManager.translate(0.0f, -1.5f, 0.0f);
					GlStateManager.scale(2.0f, 2.0f, 2.0f);
					modelBiped.bipedBody.postRender(scale);
					renderRightWing(awu, awu.skinPartRenderDataConstructor.newInstance(part, renderData), -angle);
					GL11.glPopMatrix();
				}
				GL11.glPopMatrix();
			}
			GlStateManager.popAttrib();
			GlStateManager.color(1F, 1F, 1F, 1F);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void renderLeftWing(ArmourersWorkshopUtil awu, Object partRenderData, double angle) {
		try {
			GL11.glPushMatrix();
			if (isSneak) {
				GlStateManager.translate(0.0F, 0.2F, 0.0F);
				GlStateManager.rotate((float) Math.toDegrees(bipedBody.rotateAngleX), 1F, 0, 0);
			}
			float scale = (float) awu.getScale.invoke(partRenderData);
			GL11.glTranslated(0, 0, scale * 2);

			Point3D point = new Point3D(0, 0, 0);
			EnumFacing axis = EnumFacing.DOWN;
			ISkinPart skinPart = (ISkinPart) awu.getSkinPart.invoke(partRenderData);
			if (skinPart.getMarkerCount() > 0) {
				point = skinPart.getMarker(0);
				axis = skinPart.getMarkerSide(0);
			}
			GL11.glRotatef((float) Math.toDegrees(this.bipedBody.rotateAngleZ), 0, 0, 1);
			GL11.glRotatef((float) Math.toDegrees(this.bipedBody.rotateAngleY), 0, 1, 0);

			GL11.glTranslated(scale * 0.5F, scale * 0.5F, scale * 0.5F);
			GL11.glTranslated(scale * point.getX(), scale * point.getY(), scale * point.getZ());

			switch (axis) {
			case UP:
				GL11.glRotated(angle, 0, 1, 0);
				break;
			case DOWN:
				GL11.glRotated(angle, 0, -1, 0);
				break;
			case SOUTH:
				GL11.glRotated(angle, 0, 0, -1);
				break;
			case NORTH:
				GL11.glRotated(angle, 0, 0, 1);
				break;
			case EAST:
				GL11.glRotated(angle, 1, 0, 0);
				break;
			case WEST:
				GL11.glRotated(angle, -1, 0, 0);
				break;
			}

			GL11.glTranslated(scale * -point.getX(), scale * -point.getY(), scale * -point.getZ());
			GL11.glTranslated(scale * -0.5F, scale * -0.5F, scale * -0.5F);

			renderPart(partRenderData);
			GL11.glPopMatrix();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void renderPart(Object partRenderData) {
		try {
			ArmourersWorkshopUtil awu = ArmourersWorkshopUtil.getInstance();
			awu.renderPart.invoke(awu.skinPartRenderer, partRenderData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void renderRightWing(ArmourersWorkshopUtil awu, Object partRenderData, double angle) {
		try {
			GL11.glPushMatrix();
			if (isSneak) {
				GlStateManager.translate(0.0F, 0.2F, 0.0F);
				GlStateManager.rotate((float) Math.toDegrees(bipedBody.rotateAngleX), 1F, 0, 0);
			}
			float scale = (float) awu.getScale.invoke(partRenderData);
			GL11.glTranslated(0, 0, scale * 2);

			Point3D point = new Point3D(0, 0, 0);
			EnumFacing axis = EnumFacing.DOWN;

			ISkinPart skinPart = (ISkinPart) awu.getSkinPart.invoke(partRenderData);
			if (skinPart.getMarkerCount() > 0) {
				point = skinPart.getMarker(0);
				axis = skinPart.getMarkerSide(0);
			}
			GL11.glRotatef((float) Math.toDegrees(this.bipedBody.rotateAngleZ), 0, 0, 1);
			GL11.glRotatef((float) Math.toDegrees(this.bipedBody.rotateAngleY), 0, 1, 0);

			GL11.glTranslated(scale * 0.5F, scale * 0.5F, scale * 0.5F);
			GL11.glTranslated(scale * point.getX(), scale * point.getY(), scale * point.getZ());

			switch (axis) {
			case UP:
				GL11.glRotated(angle, 0, 1, 0);
				break;
			case DOWN:
				GL11.glRotated(angle, 0, -1, 0);
				break;
			case SOUTH:
				GL11.glRotated(angle, 0, 0, -1);
				break;
			case NORTH:
				GL11.glRotated(angle, 0, 0, 1);
				break;
			case EAST:
				GL11.glRotated(angle, 1, 0, 0);
				break;
			case WEST:
				GL11.glRotated(angle, -1, 0, 0);
				break;
			}

			GL11.glTranslated(scale * -point.getX(), scale * -point.getY(), scale * -point.getZ());
			GL11.glTranslated(scale * -0.5F, scale * -0.5F, scale * -0.5F);

			renderPart(partRenderData);
			GL11.glPopMatrix();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
