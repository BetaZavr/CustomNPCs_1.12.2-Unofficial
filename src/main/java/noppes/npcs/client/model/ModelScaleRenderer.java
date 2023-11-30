package noppes.npcs.client.model;

import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.items.CustomArmor;

public class ModelScaleRenderer
extends ModelRenderer {
	
	public ModelPartConfig config;
	public int displayList, displayOBJList;
	public boolean isCompiled, smallArms;
	public EnumParts part;

	public ModelScaleRenderer(ModelBase modelBase, EnumParts part) {
		super(modelBase);
		this.part = part;
	}

	public ModelScaleRenderer(ModelBase modelBase, int x, int y, EnumParts part) {
		this(modelBase, part);
		this.setTextureOffset(x, y);
	}

	public void compile(float scale) {
		GlStateManager.glNewList(this.displayList = GLAllocation.generateDisplayLists(1), 4864);
		BufferBuilder tessellator = Tessellator.getInstance().getBuffer();
		for (int i = 0; i < this.cubeList.size(); ++i) {
			this.cubeList.get(i).render(tessellator, scale);
		}
		GL11.glEndList();
		this.isCompiled = true;
	}

	public void parentRender(float scale) {
		super.render(scale);
	}

	public void postRender(float scale) {
		if (this.config != null) {
			GlStateManager.translate(this.config.offsetBase[0] + this.config.offsetAnimation[0], this.config.offsetBase[1] + this.config.offsetAnimation[1], this.config.offsetBase[2] + this.config.offsetAnimation[2]);
		}
		this.postRenderAnimRotate(scale);
		if (this.config != null) {
			GlStateManager.scale(this.config.scaleBase[0] * this.config.scaleAnimation[0], this.config.scaleBase[1] * this.config.scaleAnimation[1], this.config.scaleBase[2] * this.config.scaleAnimation[2]);
		}
	}

	public void postRenderAnimRotate(float scale) {
		if (this.isHidden || !this.showModel) { return; }
		if (!this.isCompiled) { this.compile(scale); }
		float x = this.rotateAngleX + (this.config != null ? this.config.rotateAnimation[0] : 0.0f);
		float y = this.rotateAngleY + (this.config != null ? this.config.rotateAnimation[1] : 0.0f);
		float z = this.rotateAngleZ + (this.config != null ? this.config.rotateAnimation[2] : 0.0f);
		if (x == 0.0F && y == 0.0F && z == 0.0F) {
			if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F) {
				GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
			}
		}
		else {
			GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
			if (z != 0.0F) { GlStateManager.rotate(z * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F); }
			if (y != 0.0F) { GlStateManager.rotate(y * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F); }
			if (x != 0.0F) { GlStateManager.rotate(x * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F); }
		}
	}

	public void render(float scale) {
		if (!this.showModel || this.isHidden) { return; }
		if (!this.isCompiled) {
			this.compile(scale);
		}
		GlStateManager.pushMatrix();
		this.postRender(scale);
		// render
		if (this.displayOBJList <= 0) { GlStateManager.callList(this.displayList); } // Vanila Render
		else { // OBJ Render
			switch(this.part) {
				case HEAD: { GlStateManager.translate(0.0f, 1.5f, 0.0f); break; }
				case MOHAWK: { GlStateManager.translate(0.0f, 1.5f, 0.0f); break; }
				case BODY: { GlStateManager.translate(0.0f, 1.5f, 0.0f); break; }
				case ARM_RIGHT: {
					if (this.smallArms) { GlStateManager.scale(0.75f, 1.0f, 1.0f); }
					float addX = this.smallArms ? 0.0175f : 0.0f;
					GlStateManager.translate(0.3175f + addX, 1.375f, 0.0f);
					break;
				}
				case ARM_LEFT: {
					if (this.smallArms) { GlStateManager.scale(0.75f, 1.0f, 1.0f); }
					float addX = this.smallArms ? -0.0175f : 0.0f;
					GlStateManager.translate(-0.3175f + addX, 1.375f, 0.0f);
					break;
				}
				case BELT: { GlStateManager.translate(0.0f, 1.5f, 0.0f); break; }
				case LEG_RIGHT: { GlStateManager.translate(0.125f, 0.75f, 0.0f); break; }
				case LEG_LEFT: { GlStateManager.translate(-0.115f, 0.75f, 0.0f); break; }
				case FEET_RIGHT: { GlStateManager.translate(0.125f, 0.75f, 0.0f); break; }
				case FEET_LEFT: { GlStateManager.translate(-0.115f, 0.75f, 0.0f); break; }
				default: { break; }
			}
			GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
			GlStateManager.callList(this.displayOBJList);
		}
		if (this.childModels != null) {
			for (int i = 0; i < this.childModels.size(); ++i) {
				this.childModels.get(i).render(scale);
			}
		}
		GlStateManager.popMatrix();
	}

	public void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
	
	public void clearAnim() {
		for (int i=0; i<3; i++) {
			this.config.rotateAnimation[i] = 0.0f;
			this.config.offsetAnimation[i] = 0.0f;
			this.config.scaleAnimation[i] = 1.0f;
		}
	}
	
	public void setAnim(Float[] values) {
		if (values==null) {
			this.clearAnim();
			return;
		}
		for (int i=0; i<3; i++) {
			this.config.rotateAnimation[i] = i>=values.length || values[i]==null ? 0.0f : values[i];
			this.config.offsetAnimation[i] = i+3>=values.length || values[i+3]==null ? 0.0f : values[i+3];
			this.config.scaleAnimation[i] = i+6>=values.length || values[i+6]==null ? 1.0f : values[i+6];
		}
	}

	public void setAnim(ModelScaleRenderer source) {
		if (source==null) {
			this.clearAnim();
			return;
		}
		if (this.config==null) { this.config = new ModelPartConfig(); }
		for (int i=0; i<3; i++) {
			this.config.rotateAnimation[i] = source.config.rotateAnimation[i];
			this.config.offsetAnimation[i] = source.config.offsetAnimation[i];
			this.config.scaleAnimation[i] = source.config.scaleAnimation[i];
		}
	}

	public void clearRotation() {
		this.rotateAngleX = 0.0f;
		this.rotateAngleY = 0.0f;
		this.rotateAngleZ = 0.0f;
	}

	public void setOBJModel(ItemStack stack, EnumParts part2) {
		CustomArmor armor = (CustomArmor) stack.getItem();
		Map<String, String> map = null;
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("OBJTexture")) {
			ResourceLocation mainTexture = ModelBuffer.getMainOBJTexture(armor.objModel);
			if (mainTexture != null) {
				map = Maps.<String, String>newHashMap();
				map.put(mainTexture.toString(), stack.getTagCompound().getString("OBJTexture"));
			}
		}
		this.displayOBJList = ModelBuffer.getDisplayList(armor.objModel, armor.getMeshNames(part2 != null ? part2 : this.part), map);
	}
	
}
