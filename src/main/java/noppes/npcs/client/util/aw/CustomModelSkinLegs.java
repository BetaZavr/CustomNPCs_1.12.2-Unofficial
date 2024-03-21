package noppes.npcs.client.util.aw;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import moe.plushie.armourers_workshop.api.common.skin.data.ISkin;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinPart;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.client.model.ModelScaleRenderer;
import noppes.npcs.entity.EntityCustomNpc;

public class CustomModelSkinLegs
extends ModelBiped {

	@SuppressWarnings("unchecked")
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
				GL11.glPushMatrix();
	            GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
	            GL11.glDisable(GL11.GL_CULL_FACE);
	            GL11.glEnable(GL11.GL_ALPHA_TEST);
	            if (!((boolean) awu.isItemRender.invoke(renderData))) {
	                GlStateManager.enablePolygonOffset();
	                GlStateManager.doPolygonOffset(-2, 1);
	            }
	            GL11.glTranslated(0, 0 , 0.005F);
	            GL11.glTranslated(0.02F, 0 , 0);
				if (showList.get(4)) { bipedLeftLeg.render(scale); }
	            GL11.glTranslated(-0.02F, 0 , 0);
				if (showList.get(5)) { bipedRightLeg.render(scale); }
	            GL11.glTranslated(0, 0 , -0.005F);
	            if (!((boolean) awu.isItemRender.invoke(renderData))) {
	                GlStateManager.doPolygonOffset(0F, 0F);
	                GlStateManager.disablePolygonOffset();
	            }
	            GL11.glPopAttrib();
	            GL11.glPopMatrix();
			}
	        boolean isAdvanced = false;
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
	                GL11.glTranslated(0, -3.0F * scale, 4.0F * scale);
	            }
	            
				if (part.getPartType().getRegistryName().equals("armourers:legs.leftLeg") && showList.get(4)) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedLeftLeg).config != null) { ((ModelScaleRenderer) modelBiped.bipedLeftLeg).postAWRender(scale); }
					else { modelBiped.bipedLeftLeg.postRender(scale); }
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				} else if (part.getPartType().getRegistryName().equals("armourers:legs.rightLeg") && showList.get(5)) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedRightLeg).config != null) { ((ModelScaleRenderer) modelBiped.bipedRightLeg).postAWRender(scale); }
					else { modelBiped.bipedRightLeg.postRender(scale); }
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				} else if (part.getPartType().getRegistryName().equals("armourers:legs.skirt")) {
					GL11.glPushMatrix();
					if (((ModelScaleRenderer) modelBiped.bipedBody).config != null) { ((ModelScaleRenderer) modelBiped.bipedRightLeg).postAWRender(scale); }
					else { modelBiped.bipedRightLeg.postRender(scale); }
					renderPart(awu.skinPartRenderDataConstructor.newInstance(part, renderData));
					GL11.glPopMatrix();
				} else if (part.getPartType().getPartName().equals("advanced_part")) {
	                isAdvanced = true;
	            }
				GL11.glPopMatrix();
			}
			
			if (isAdvanced) {
				
	            Object advancedData = awu.advancedData.newInstance();
	            int partCount = 4;
	            Object base = awu.advancedPart.newInstance(0, "base");
	            Object advParts1[] = new Object[partCount];
	            Object advParts2[] = new Object[partCount];
	            Object advParts3[] = new Object[partCount];
	            
	            for (int i = 0; i < partCount; i++) {
	                advParts1[i] = awu.advancedPart.newInstance(0, String.valueOf(i));
	                awu.posAP.set(advParts1[i], new Vec3d(0D, 0D, 8D));
	                
	                advParts2[i] = awu.advancedPart.newInstance(0, String.valueOf(i));
	                awu.posAP.set(advParts2[i], new Vec3d(0D, 0D, 8D));
	                
	                advParts3[i] = awu.advancedPart.newInstance(0, String.valueOf(i));
	                awu.posAP.set(advParts3[i], new Vec3d(0D, 0D, 8D));
	            }
	            
	            for (int i = 0; i < partCount - 1; i++) {
	            	((ArrayList<Object>) awu.getChildren.invoke(advParts1[i])).add(advParts1[i + 1]);

	            	((ArrayList<Object>) awu.getChildren.invoke(advParts2[i])).add(advParts2[i + 1]);
	            	
	            	((ArrayList<Object>) awu.getChildren.invoke(advParts3[i])).add(advParts3[i + 1]);
	            }

            	((ArrayList<Object>) awu.getChildren.invoke(base)).add(advParts1[0]);
            	((ArrayList<Object>) awu.getChildren.invoke(base)).add(advParts2[0]);
            	((ArrayList<Object>) awu.getChildren.invoke(base)).add(advParts3[0]);
	            
            	awu.rotationAngle.set(base, new Vec3d(-30, 0, 0));

            	awu.rotationAngle.set(advParts1[0], new Vec3d(10, 0, 0));
            	awu.rotationAngle.set(advParts1[1], new Vec3d(10, 0, 0));
            	awu.rotationAngle.set(advParts1[2], new Vec3d(10, 0, 0));

            	awu.rotationAngle.set(advParts2[0], new Vec3d(10, 10, 0));
            	awu.rotationAngle.set(advParts2[1], new Vec3d(10, 0, 0));
            	awu.rotationAngle.set(advParts2[2], new Vec3d(10, 0, 0));

            	awu.rotationAngle.set(advParts3[0], new Vec3d(10, -10, 0));
            	awu.rotationAngle.set(advParts3[1], new Vec3d(10, 0, 0));
            	awu.rotationAngle.set(advParts3[2], new Vec3d(10, 0, 0));
	            
	            GlStateManager.pushMatrix();
	            if (isChild) {
	                float f6 = 2.0F;
	                GL11.glScalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
	                GL11.glTranslatef(0.0F, 24.0F * scale, 0.0F);
	            }
	            if (isSneak) {
	                GlStateManager.translate(0.0F, 0.2F, 0.0F);
	                GL11.glTranslated(0, -3 * scale, 4 * scale);
	            }
	            if (!((boolean) awu.isItemRender.invoke(renderData))) {
	                GlStateManager.translate(0F, 12F * ((float) awu.getScale.invoke(renderData)), 0F);
	            }
	            renderAdvancedSkin(skin, renderData, npc, advancedData, base);
	            GlStateManager.popMatrix();
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
	
	private void renderAdvancedSkin(ISkin skin, Object renderData, EntityCustomNpc npc, Object advancedData, Object base) {
		try {
			ArmourersWorkshopUtil awu = ArmourersWorkshopUtil.getInstance();
			awu.renderAdvancedSkin.invoke(awu.advancedPartRenderer, skin, renderData, npc, advancedData, base);
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
}