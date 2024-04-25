package noppes.npcs.client.util.aw;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import moe.plushie.armourers_workshop.api.common.skin.Point3D;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkin;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinPart;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.EnumFacing;
import noppes.npcs.client.model.ModelScaleRenderer;
import noppes.npcs.entity.EntityCustomNpc;

/** Changed AWMod.client.model.skin.ModelSkinOutfit extends ModelTypeHelper */
public class CustomModelSkinOutfit extends ModelBiped {

	public void render(EntityCustomNpc npc, ISkin skin, ModelBiped modelBiped, Object renderData, float scale,
			List<Boolean> showList) {
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
				if (showList.get(0)) {
					bipedHead.render(scale);
				}
				if (showList.get(3)) {
					bipedBody.render(scale);
				}
				if (showList.get(1)) {
					bipedLeftArm.render(scale);
				}
				if (showList.get(2)) {
					bipedRightArm.render(scale);
				}
				GL11.glTranslated(0, 0, 0.005F);
				GL11.glTranslated(0.02F, 0, 0);
				if (showList.get(4)) {
					bipedLeftLeg.render(scale);
				}
				GL11.glTranslated(-0.02F, 0, 0);
				if (showList.get(5)) {
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
				if (part.getPartType().getRegistryName().equals("armourers:head.base") && showList.get(0)) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedHead).config != null) {
						((ModelScaleRenderer) modelBiped.bipedHead).postAWRender(scale);
					} else {
						modelBiped.bipedHead.postRender(scale);
					}
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				}

				if (part.getPartType().getRegistryName().equals("armourers:chest.base") && showList.get(3)) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedBody).config != null) {
						((ModelScaleRenderer) modelBiped.bipedBody).postAWRender(scale);
					} else {
						modelBiped.bipedBody.postRender(scale);
					}
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				} else if (part.getPartType().getRegistryName().equals("armourers:chest.leftArm") && showList.get(1)) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedLeftArm).config != null) {
						((ModelScaleRenderer) modelBiped.bipedLeftArm).postAWRender(scale);
					} else {
						modelBiped.bipedLeftArm.postRender(scale);
					}
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				} else if (part.getPartType().getRegistryName().equals("armourers:chest.rightArm") && showList.get(2)) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedRightArm).config != null) {
						((ModelScaleRenderer) modelBiped.bipedRightArm).postAWRender(scale);
					} else {
						modelBiped.bipedRightArm.postRender(scale);
					}
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				}

				if (part.getPartType().getRegistryName().equals("armourers:legs.leftLeg") && showList.get(4)) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedLeftLeg).config != null) {
						((ModelScaleRenderer) modelBiped.bipedLeftLeg).postAWRender(scale);
					} else {
						modelBiped.bipedLeftLeg.postRender(scale);
					}
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				} else if (part.getPartType().getRegistryName().equals("armourers:legs.rightLeg") && showList.get(5)) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedRightLeg).config != null) {
						((ModelScaleRenderer) modelBiped.bipedRightLeg).postAWRender(scale);
					} else {
						modelBiped.bipedRightLeg.postRender(scale);
					}
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				} else if (part.getPartType().getRegistryName().equals("armourers:legs.skirt")) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedBody).config != null) {
						((ModelScaleRenderer) modelBiped.bipedRightLeg).postAWRender(scale);
					} else {
						modelBiped.bipedRightLeg.postRender(scale);
					}
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				}

				if (part.getPartType().getRegistryName().equals("armourers:feet.leftFoot") && showList.get(4)) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedLeftLeg).config != null) {
						((ModelScaleRenderer) modelBiped.bipedLeftLeg).postAWRender(scale);
					} else {
						modelBiped.bipedLeftLeg.postRender(scale);
					}
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				} else if (part.getPartType().getRegistryName().equals("armourers:feet.rightFoot") && showList.get(5)) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedRightLeg).config != null) {
						((ModelScaleRenderer) modelBiped.bipedRightLeg).postAWRender(scale);
					} else {
						modelBiped.bipedRightLeg.postRender(scale);
					}
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				}

				double angle = (double) awu.getFlapAngleForWings.invoke(awu.skinUtils, npc, skin, i);
				if (part.getPartType().getRegistryName().equals("armourers:wings.leftWing")) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedBody).config != null) {
						((ModelScaleRenderer) modelBiped.bipedBody).postAWRender(scale);
					} else {
						modelBiped.bipedBody.postRender(scale);
					}
					renderLeftWing(awu, awu.skinPartRenderDataConstructor.newInstance(part, renderData), angle);
					GL11.glPopMatrix();
				}
				if (part.getPartType().getRegistryName().equals("armourers:wings.rightWing")) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedBody).config != null) {
						((ModelScaleRenderer) modelBiped.bipedBody).postAWRender(scale);
					} else {
						modelBiped.bipedBody.postRender(scale);
					}
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
