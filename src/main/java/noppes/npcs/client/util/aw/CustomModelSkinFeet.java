package noppes.npcs.client.util.aw;

import java.util.ArrayList;
import java.util.Map;

import noppes.npcs.LogWriter;
import org.lwjgl.opengl.GL11;

import moe.plushie.armourers_workshop.api.common.skin.data.ISkin;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinPart;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityNPCInterface;

public class CustomModelSkinFeet extends ModelBiped {

	public void render(EntityNPCInterface npc, ISkin skin, ModelBiped modelBiped, Object renderData, float scale, Map<EnumParts, Boolean> ba) {
		if (skin == null || npc == null) {
			return;
		}
		try {
			ArrayList<ISkinPart> parts = skin.getSubParts();
			this.isSneak = npc.isSneaking();
			this.isRiding = npc.isRiding();

			GlStateManager.pushAttrib();
			//RenderHelper.enableGUIStandardItemLighting();
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

            for (ISkinPart part : parts) {
                GL11.glPushMatrix();
                if (isChild) {
                    float f6 = 2.0F;
                    GL11.glScalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
                    GL11.glTranslatef(0.0F, 24.0F * scale, 0.0F);
                }
                if (isSneak) {
                    GL11.glTranslatef(0.0F, 0.2F, 0.0F);
                    GL11.glTranslated(0, -3.0F * scale, 4.0F * scale);
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

                GL11.glPopMatrix();
            }
			GlStateManager.popAttrib();
			GlStateManager.color(1F, 1F, 1F, 1F);
		} catch (Exception e) { LogWriter.error(e); }
	}

	private void renderPart(Object partRenderData) {
		try {
			ArmourersWorkshopUtil awu = ArmourersWorkshopUtil.getInstance();
			awu.renderPart.invoke(awu.skinPartRenderer, partRenderData);
		} catch (Exception e) { LogWriter.error(e); }
	}
}