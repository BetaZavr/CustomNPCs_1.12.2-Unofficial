package noppes.npcs.client.util.aw;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import moe.plushie.armourers_workshop.api.common.skin.data.ISkin;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinPart;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import noppes.npcs.client.model.ModelScaleRenderer;
import noppes.npcs.entity.EntityCustomNpc;

/** Changed AWMod.client.model.skin.ModelSkinOutfit extends ModelSkinChest */
public class CustomModelSkinChest
extends ModelBiped {

	public void render(EntityCustomNpc npc, ISkin skin, ModelBiped modelBiped, Object renderData, float scale, List<Boolean> showList) {
		if (skin == null || npc == null) { return; }
		try {
			ArrayList<ISkinPart> parts = skin.getSubParts();
			this.isSneak = npc.isSneaking();
			this.isRiding = npc.isRiding();

			GlStateManager.pushAttrib();
			RenderHelper.enableGUIStandardItemLighting();
			ArmourersWorkshopUtil awu = ArmourersWorkshopUtil.getInstance();
			if ((boolean) awu.hasPaintData.invoke(skin) & (boolean) awu.isShowSkinPaint.invoke(renderData) & awu.getTexturePaintType.invoke(awu.clientProxy) == awu.TEXTURE_REPLACE) {
				Object st = awu.getTextureForSkin.invoke(awu.clientSkinPaintCache, skin, awu.getSkinDye.invoke(renderData), awu.getExtraColours.invoke(renderData));
				awu.bindTexture.invoke(st);
				GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
	            GL11.glDisable(GL11.GL_CULL_FACE);
	            GL11.glEnable(GL11.GL_ALPHA_TEST);
				if (showList.get(3)) { bipedBody.render(scale); }
				if (showList.get(1)) { bipedLeftArm.render(scale); }
				if (showList.get(2)) { bipedRightArm.render(scale); }
	            GL11.glPopAttrib();
			}

			for (int i = 0; i < parts.size(); i++) {
				ISkinPart part = parts.get(i);
				GL11.glPushMatrix();
				if (isChild) {
					float f6 = 2.0F;
					GL11.glScalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
					GL11.glTranslatef(0.0F, 24.0F * scale, 0.0F);
				}
	            if (isSneak) {
	            	GL11.glTranslatef(0.0F, 0.2F, 0.0F);
	            }
	            
	            if (part.getPartType().getRegistryName().equals("armourers:chest.base") && showList.get(3)) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedBody).config != null) { ((ModelScaleRenderer) modelBiped.bipedBody).postAWRender(scale); }
					else { modelBiped.bipedBody.postRender(scale); }
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					//boolean overrideChest = (boolean) awsp.getValue.invoke(awsp.PROP_MODEL_OVERRIDE_CHEST, skin.getSkinType().getProperties());
					GL11.glPopMatrix();
				} else if (part.getPartType().getRegistryName().equals("armourers:chest.leftArm") && showList.get(1)) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedLeftArm).config != null) { ((ModelScaleRenderer) modelBiped.bipedLeftArm).postAWRender(scale); }
					else { modelBiped.bipedLeftArm.postRender(scale); }
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				} else if (part.getPartType().getRegistryName().equals("armourers:chest.rightArm") && showList.get(2)) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedRightArm).config != null) { ((ModelScaleRenderer) modelBiped.bipedRightArm).postAWRender(scale); }
					else { modelBiped.bipedRightArm.postRender(scale); }
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				}
				
				GL11.glPopMatrix();
			}
			GlStateManager.popAttrib();
			GlStateManager.color(1F, 1F, 1F, 1F);
		}
		catch (Exception e) { e.printStackTrace(); }
	}


	private void renderPart(Object partRenderData) {
		try {
			ArmourersWorkshopUtil awu = ArmourersWorkshopUtil.getInstance();
			awu.renderPart.invoke(awu.skinPartRenderer, partRenderData);
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
}
