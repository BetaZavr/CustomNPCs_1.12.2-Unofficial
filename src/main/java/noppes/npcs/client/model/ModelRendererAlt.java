package noppes.npcs.client.model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.client.model.animation.AddedPartConfig;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.data.DataAnimation;
import noppes.npcs.items.CustomArmor;
import noppes.npcs.util.ObfuscationHelper;

public class ModelRendererAlt
extends ModelRenderer {
	// Base
	private static float limit = (float) (Math.PI / 2.0f);
	public static Field a;
	public static Field colorState;
	
	static {
		try {
			ModelRendererAlt.colorState = ObfuscationHelper.getField(GlStateManager.class, 21);
			ModelRendererAlt.colorState.setAccessible(true);
			Object colorState = ModelRendererAlt.colorState.get(null);
			ModelRendererAlt.a = colorState.getClass().getDeclaredFields()[3];
			ModelRendererAlt.a.setAccessible(true);
		} catch (Exception e) { e.printStackTrace(); }
	}

	// Data
	public EnumParts part;
	public int idPart;
	
	private final Map<Integer, PositionTextureVertex> vs = Maps.<Integer, PositionTextureVertex>newHashMap(); // vs
    private final Map<Integer, TexturedQuad> quads = Maps.<Integer, TexturedQuad>newHashMap(); // fases
	private final Vec2f[] tvs = new Vec2f[8];
	
	public float x, y, z, xe, ye0, ye1, ye2, ze, dx, dy0, dy1, dy2, dz, u, v;
    
    private int displayList, displayOBJListUp, displayOBJListDown;
	public float rotateAngleX1 = 0.0f;
	public float rotateAngleY1 = 0.0f;
	
	public float scaleX = 1.0f, scaleY = 1.0f, scaleZ = 1.0f;
	public float offsetAnimX = 0.0f, offsetAnimY = 0.0f, offsetAnimZ = 0.0f;
	private boolean normalTop = false;
	public boolean isNormal = false;
	
	public boolean smallArms;
	public boolean isAnimPart;
	private float r = 1.0f, g = 1.0f, b = 1.0f;
	
	// Custom
	public ResourceLocation location = null;
	public boolean isArmor;
	
	public ModelRendererAlt(ModelBase model, EnumParts part, int textureU, int textureV, boolean isNormal) {
		super(model, textureU, textureV);
		this.part = part;
		this.idPart = part.patterns;
		this.u = textureU;
		this.v = textureV;
		this.isNormal = isNormal;
	}
	
	public ModelRendererAlt(AddedPartConfig part, DataAnimation animation) {
		super(null, part.textureU, part.textureV);
		this.part = EnumParts.CUSTOM;
		this.idPart = part.id;
		this.u = part.textureU;
		this.v = part.textureV;
		this.isNormal = part.isNormal;
		this.location = part.location;
		this.setBox(part.pos[0], part.pos[1], part.pos[2], part.size[0], part.size[1], part.size[2], part.size[3], part.size[4], 0.0f);
		this.setRotationPoint(part.rot[0], part.rot[1], part.rot[2]);
		this.setAnimation(animation);
	}

	public void setBox(float x, float y, float z, float dx, float dy0, float dy1, float dy2, float dz, float wear) {
		this.xe = x + (float) dx + wear;
		this.ye0 = y + (float) dy0;
		this.ye1 = y + (float) (dy0 + dy1);
		this.ye2 = y + (float) (dy0 + dy1 + dy2) + wear;
		this.ze = z + (float) dz + wear;
		this.x = x - wear;
		this.y = y - wear;
		this.z = z - wear;
		this.dx = dx;
		this.dz = dz;
		this.dy0 = dy0;
		this.dy1 = this.dy0 + dy1;
		this.dy2 = this.dy1 + dy2;
		if (this.mirror) {
			float f3 = this.xe;
			this.xe = this.x;
			this.x = f3;
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void postRender(float scale) {
		if (this.offsetAnimX != 0.0f || this.offsetAnimY != 0.0f || this.offsetAnimZ != 0.0f) {
			GlStateManager.translate(this.offsetAnimX, this.offsetAnimY, this.offsetAnimZ);
		}
		GlStateManager.translate(this.rotationPointX * scale, (this.rotationPointY + this.offsetAnimY) * scale, (this.rotationPointZ + this.offsetAnimZ) * scale);
		if (this.rotateAngleZ != 0.0F) { GlStateManager.rotate(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F); }
		if (this.rotateAngleY != 0.0F) { GlStateManager.rotate(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F); }
		if (this.rotateAngleX != 0.0F) { GlStateManager.rotate(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F); }
		if (this.scaleX != 1.0f || this.scaleY != 1.0f || this.scaleZ != 1.0f) { GlStateManager.scale(this.scaleX, this.scaleY, this.scaleZ); }
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void render(float scale) {
		if (this.isHidden || !this.showModel) { return; }
		GlStateManager.pushMatrix();
		this.postRender(scale);
		// Tint
		if (ModelRendererAlt.a != null) {
			try { GlStateManager.color(this.r, this.g, this.b, ModelRendererAlt.a.getFloat(ModelRendererAlt.colorState.get(null))); }
			catch (Exception e) { }
		} else {
			GlStateManager.color(this.r, this.g, this.b);
		}
		// render
		if (this.displayOBJListUp > 0 || this.displayOBJListDown > 0) { this.objDraw(scale); }
		else {
			this.clearData();
			if (this.location != null) { Minecraft.getMinecraft().renderEngine.bindTexture(this.location); }
			if (this.isNormal || !CustomNpcs.ShowJoints || (this.rotateAngleX1 == 0.0f && this.rotateAngleY1 == 0.0f)) {
				this.simpleDraw(scale);
			} else {
				this.rotateTop();
				this.drawJoint(scale);
			}
		}
		// Child Models
		if (this.childModels != null && !this.childModels.isEmpty()) {
			List<ModelRenderer> del = Lists.<ModelRenderer>newArrayList();
			for (ModelRenderer model : this.childModels) {
				if (model instanceof ModelRendererAlt && ((ModelRendererAlt) model).part == EnumParts.CUSTOM) { del.add(model); }
				model.render(scale);
			}
			for (ModelRenderer model : del) { this.childModels.remove(model); }
		}
		GlStateManager.popMatrix();
	}
	
	private void clearData() {
		vs.clear();
		quads.clear();
	}

	private void rotateTop() {
		if (this.rotateAngleY1 == 0.0f) { // Normal
			if (!this.normalTop) {
				tvs[0] = new Vec2f(x, z);
				tvs[1] = new Vec2f(xe, z);
				tvs[2] = new Vec2f(xe, ze);
				tvs[3] = new Vec2f(x, ze);
				tvs[4] = new Vec2f(x, z);
				tvs[5] = new Vec2f(xe, z);
				tvs[6] = new Vec2f(xe, ze);
				tvs[7] = new Vec2f(x, ze);
				this.normalTop = true;
			}
		} else { // rotate
			if (this.rotateAngleY1 > limit) { this.rotateAngleY1 = limit; }
			if (this.rotateAngleY1 < -limit) { this.rotateAngleY1 = -limit; }
			double cos = Math.cos(-this.rotateAngleY1);
			double sin = Math.sin(-this.rotateAngleY1);
			Vec2f xMzM = this.rotate(new Vec2f(x + (xe - x) / 2,  z + (ze - z) / 2), new Vec2f(x, z), cos, sin);
			Vec2f xNzM = this.rotate(new Vec2f(x + (xe - x) / 2,  z + (ze - z) / 2), new Vec2f(xe, z), cos, sin);
			Vec2f xNzN = this.rotate(new Vec2f(x + (xe - x) / 2,  z + (ze - z) / 2), new Vec2f(xe, ze), cos, sin);
			Vec2f xMzN = this.rotate(new Vec2f(x + (xe - x) / 2,  z + (ze - z) / 2), new Vec2f(x, ze), cos, sin);
			tvs[0] = new Vec2f(xMzM.x, xMzM.y);
			tvs[1] = new Vec2f(xNzM.x, xNzM.y);
			tvs[2] = new Vec2f(xNzN.x, xNzN.y);
			tvs[3] = new Vec2f(xMzN.x, xMzN.y);
			tvs[4] = new Vec2f(xMzM.x, xMzM.y);
			tvs[5] = new Vec2f(xNzM.x, xNzM.y);
			tvs[6] = new Vec2f(xNzN.x, xNzN.y);
			tvs[7] = new Vec2f(xMzN.x, xMzN.y);
			this.normalTop = false;
		}
	}
	
	// 3D part model
	private void objDraw(float scale) {
		switch (this.part) {
			case HEAD: {
				GlStateManager.translate(0.0f, 1.5f, 0.0f);
				break;
			}
			case BODY: {
				GlStateManager.translate(0.0f, 1.5f, 0.0f);
				break;
			}
			case ARM_RIGHT: {
				if (this.smallArms) {
					GlStateManager.scale(0.75f, 1.0f, 1.0f);
				}
				float addX = this.smallArms ? 0.0175f : 0.0f;
				GlStateManager.translate(0.3175f + addX, 1.375f, 0.0f);
				break;
			}
			case ARM_LEFT: {
				if (this.smallArms) {
					GlStateManager.scale(0.75f, 1.0f, 1.0f);
				}
				float addX = this.smallArms ? -0.0175f : 0.0f;
				GlStateManager.translate(-0.3175f + addX, 1.375f, 0.0f);
				break;
			}
			case LEG_RIGHT: {
				GlStateManager.translate(0.125f, 0.75f, 0.0f);
				break;
			}
			case LEG_LEFT: {
				GlStateManager.translate(-0.115f, 0.75f, 0.0f);
				break;
			}
			default: {
				break;
			}
		}
		GlStateManager.enableBlend();
		GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
		if (this.displayOBJListUp > 0) {
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.callList(this.displayOBJListUp);
		}
		if (this.displayOBJListDown > 0) {
			if (this.rotateAngleX1 != 0.0f) {
				boolean isArm = this.part.name().toLowerCase().indexOf("arm") != -1;
				float ofsY = dy2 - dy0;
				if (isArm) {
					GlStateManager.translate(0.0f, 0.75f, 0.0f);
				}
				float ofsZ = this.rotateAngleX1 * (dz / 2.0f) / (float) -Math.PI;
				GlStateManager.translate(0.0f, ofsY * 0.0625f, ofsZ * 0.0625f);
				GlStateManager.rotate(this.rotateAngleX1 * 180.0f / (float) Math.PI, 1.0f, 0.0f, 0.0f);
				GlStateManager.translate(0.0f, ofsY * -0.0625f, ofsZ * -0.0625f);
				if (isArm) {
					GlStateManager.translate(0.0f, -0.75f, 0.0f);
				}
			}
			if (this.rotateAngleY1 != 0.0f) {
				boolean isArm = this.part.name().toLowerCase().indexOf("arm") != -1;
				float ofs = (this.part.name().toLowerCase().indexOf("right") != -1 ? -1.0f : 1.0f) * (isArm ? 0.375f : 0.125f);
				GlStateManager.translate(ofs, 0.0f, 0.0f);
				GlStateManager.rotate(this.rotateAngleY1 * 180.0f / (float) Math.PI, 0.0f, 1.0f, 0.0f);
				GlStateManager.translate(-ofs, 0.0f, 0.0f);
			}
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.callList(this.displayOBJListDown);
		}
		GlStateManager.disableBlend();
	}

	// Normal cuboid
	private void simpleDraw(float scale) {
		if (this.displayList > 0) {
			GlStateManager.callList(this.displayList);
			if (this.childModels != null) {
				for (int i = 0; i < this.childModels.size(); ++i) {
					this.childModels.get(i).render(scale);
				}
			}
			return;
		}
		vs.put(0, new PositionTextureVertex(x, y, z, 0.0F, 0.0F));
		vs.put(1, new PositionTextureVertex(xe, y, z, 0.0F, 8.0F));
		vs.put(2, new PositionTextureVertex(xe, y, ze, 8.0F, 8.0F));
		vs.put(3, new PositionTextureVertex(x, y, ze, 8.0F, 0.0F));
		vs.put(4, new PositionTextureVertex(x, ye2, z, 0.0F, 0.0F));
		vs.put(5, new PositionTextureVertex(xe, ye2, z, 0.0F, 8.0F));
		vs.put(6, new PositionTextureVertex(xe, ye2, ze, 8.0F, 8.0F));
		vs.put(7, new PositionTextureVertex(x, ye2, ze, 8.0F, 0.0F));
		// Normals
		if (this.mirror) {
			this.setQuard(0, vs.get(3), vs.get(2), vs.get(1), vs.get(0), u + dz + dx, v, u + dz, v + dz); // up
			this.setQuard(1, vs.get(7), vs.get(6), vs.get(5), vs.get(4), u + 2 * dz + dx, v, u + dz + dx, v + dz); // down
			this.setQuard(2, vs.get(0), vs.get(1), vs.get(5), vs.get(4), u + dz + dx, v + dz, u + dz, v + dz + dy2); // front
			this.setQuard(3, vs.get(2), vs.get(3), vs.get(7), vs.get(6), u + 2 * (dz + dx), v + dz, u + 2 * dz + dx, v + dz + dy2); // back
			this.setQuard(4, vs.get(1), vs.get(2), vs.get(6), vs.get(5), u + 2 * dz + dx, v + dz, u + dz + dx, v + dz + dy2); // in side
			this.setQuard(5, vs.get(3), vs.get(0), vs.get(4), vs.get(7), u + dz, v + dz, u, v + dz + dy2); // out side
		}
		else {
			this.setQuard(0, vs.get(2), vs.get(3), vs.get(0), vs.get(1), u + dz, v, u + dz + dx, v + dz); // up
			this.setQuard(1, vs.get(6), vs.get(7), vs.get(4), vs.get(5), u + dz + dx, v, u + 2 * dz + dx, v + dz); // down
			this.setQuard(2, vs.get(1), vs.get(0), vs.get(4), vs.get(5), u + dz, v + dz, u + dz + dx, v + dz + dy2); // front
			this.setQuard(3, vs.get(3), vs.get(2), vs.get(6), vs.get(7), u + 2 * dz + dx, v + dz, u + 2 * (dz + dx), v + dz + dy2); // back
			this.setQuard(4, vs.get(2), vs.get(1), vs.get(5), vs.get(6), u + dz + dx, v + dz, u + 2 * dz + dx, v + dz + dy2); // in side
			this.setQuard(5, vs.get(0), vs.get(3), vs.get(7), vs.get(4), u, v + dz, u + dz, v + dz + dy2); // out side
		}
		
		GlStateManager.glNewList(this.displayList = GLAllocation.generateDisplayLists(1), 4864);
		this.draw(scale);
		GL11.glEndList();
	}

	// Joint and rotate Top
	private void drawJoint(float scale) {
		// Counterclock-wise
		boolean c = this.rotateAngleY1 <= 0.0f;
		if (this.isArmor && this.part != EnumParts.ARM_RIGHT && this.part != EnumParts.LEG_RIGHT) { c = !c; }
		vs.put(0, new PositionTextureVertex(x, y, z, 0.0F, 0.0F));
		vs.put(1, new PositionTextureVertex(xe, y, z, 0.0F, 8.0F));
		vs.put(2, new PositionTextureVertex(xe, y, ze, 8.0F, 8.0F));
		vs.put(3, new PositionTextureVertex(x, y, ze, 8.0F, 0.0F));
		int i = 0;
		// level #0
		this.setQuard(i++, vs.get(2), vs.get(3), vs.get(0), vs.get(1), u + dz, v, u + dz + dx, v + dz); // up
		// Calculate
		float cos0 = (float) Math.cos(-this.rotateAngleX1 / 3.0f), sin0 = (float) Math.sin(-this.rotateAngleX1 / 3.0f);
		float cos1 = (float) Math.cos(-this.rotateAngleX1 / 1.5f), sin1 = (float) Math.sin(-this.rotateAngleX1 / 1.5f);
		float cos2 = (float) Math.cos(-this.rotateAngleX1), sin2 = (float) Math.sin(-this.rotateAngleX1);
		float tan = (float) Math.tan(-this.rotateAngleX1 / 2.0f);
		Vec2f cr = new Vec2f(ye0, this.rotateAngleX1 * -ze / (float) -Math.PI); // centr
		if (this.rotateAngleX1 < 0.0f) {
			// Calculated fillet positions
			Vec2f g0 = new Vec2f(cr.x + (ze - cr.y) * sin0 + (ye0 - cr.x) * cos0, cr.y + (ze - cr.y) * cos0 - (ye0 - cr.x) * sin0);
			Vec2f g1 = new Vec2f(cr.x + (ze - cr.y) * sin1 + (ye0 - cr.x) * cos1, cr.y + (ze - cr.y) * cos1 - (ye0 - cr.x) * sin1);
			Vec2f g2 = new Vec2f(cr.x + (ze - cr.y) * sin2 + (ye0 - cr.x) * cos2, cr.y + (ze - cr.y) * cos2 - (ye0 - cr.x) * sin2);
			float t = (z - cr.y) * tan;
			Vec2f g3 = new Vec2f(ye0 + t, z);
			float d0 = (float) Math.hypot(g0.x-g1.x, g0.y-g1.y);
			float n0 = dy0 / (dy0 + d0 * 1.5f) * d0 * 1.5f;
			float n1 = (dy1 - dy0) / ((dy1 - dy0) + d0 * 1.5f) * d0 * 1.5f;
			float dy00 = dy0 - n0;
			float dy01 = dy00 + n0 / 1.5f;
			float dy02 = dy0 + n1 / 3.0f;
			float dy03 = (dy0 + n1) * 0.85f;
			vs.put(4, new PositionTextureVertex(x, g3.x, z, 0.0F, 0.0F));
			vs.put(5, new PositionTextureVertex(xe, g3.x, z, 0.0F, 8.0F));
			vs.put(6, new PositionTextureVertex(xe, ye0, ze, 8.0F, 8.0F));
			vs.put(7, new PositionTextureVertex(x, ye0, ze, 8.0F, 0.0F));
			// level #0
			this.setQuard(i++, vs.get(1), vs.get(0), vs.get(4), vs.get(5), u + dz, v + dz, u + dz + dx, v + dz + dy0); // front
			this.setQuard(i++, vs.get(3), vs.get(2), vs.get(6), vs.get(7), u + 2 * dz + dx, v + dz, u + 2 * (dz + dx), v + dz + dy00); // back
			this.setQuard(i++, vs.get(1), vs.get(2), vs.get(6), vs.get(5), // in side
					u + dz + dx, v + dz,
					u + 2 * dz + dx, v + dz,
					u + 2 * dz + dx, v + dz + dy00,
					u + dz + dx, v + dz + dy0);
			this.setQuard(i++, vs.get(0), vs.get(3), vs.get(7), vs.get(4), // out side
					u + dz, v + dz,
					u, v + dz,
					u, v + dz + dy00,
					u + dz, v + dz + dy0);
			// level #1
			vs.put(8, new PositionTextureVertex(xe, g0.x, g0.y, 0.0F, 0.0F));
			vs.put(9, new PositionTextureVertex(x, g0.x, g0.y, 0.0F, 8.0F));
			this.setQuard(i++, vs.get(7), vs.get(6), vs.get(8), vs.get(9), // back
					u + 2 * (dz + dx), v + dz + dy00,
					u + 2 * dz + dx, v + dz + dy00,
					u + 2 * dz + dx, v + dz + dy01,
					u + 2 * (dz + dx), v + dz + dy01);
			this.setQuard(i++, vs.get(5), vs.get(6), vs.get(8), vs.get(5), // in side
					u + dz + dx, v + dz + dy0,
					u + 2 * dz + dx, v + dz + dy00,
					u + 2 * dz + dx, v + dz + dy01,
					u + dz + dx, v + dz + dy0);
			this.setQuard(i++, vs.get(4), vs.get(7), vs.get(9), vs.get(4), // out side
					u + dz, v + dz + dy0,
					u, v + dz + dy00,
					u, v + dz + dy01,
					u + dz, v + dz + dy0);
			// level #2
			vs.put(10, new PositionTextureVertex(xe, g1.x, g1.y, 0.0F, 0.0F));
			vs.put(11, new PositionTextureVertex(x, g1.x, g1.y, 0.0F, 8.0F));
			this.setQuard(i++, vs.get(9), vs.get(8), vs.get(10), vs.get(11), // back
					u + 2 * (dz + dx), v + dz + dy01,
					u + 2 * dz + dx, v + dz + dy01,
					u + 2 * dz + dx, v + dz + dy02,
					u + 2 * (dz + dx), v + dz + dy02);
			this.setQuard(i++, vs.get(5), vs.get(8), vs.get(10), vs.get(5), // in side
					u + dz + dx, v + dz + dy0,
					u + 2 * dz + dx, v + dz + dy01,
					u + 2 * dz + dx, v + dz + dy02,
					u + dz + dx, v + dz + dy0);
			this.setQuard(i++, vs.get(4), vs.get(9), vs.get(11), vs.get(4), // out side
					u + dz, v + dz + dy0,
					u, v + dz + dy01,
					u, v + dz + dy02,
					u + dz, v + dz + dy0);
			// level #3
			vs.put(12, new PositionTextureVertex(xe, g2.x, g2.y, 0.0F, 0.0F));
			vs.put(13, new PositionTextureVertex(x, g2.x, g2.y, 0.0F, 8.0F));
			this.setQuard(i++, vs.get(11), vs.get(10), vs.get(12), vs.get(13), // back
					u + 2 * (dz + dx), v + dz + dy02,
					u + 2 * dz + dx, v + dz + dy02,
					u + 2 * dz + dx, v + dz + dy03,
					u + 2 * (dz + dx), v + dz + dy03);
			this.setQuard(i++, vs.get(5), vs.get(10), vs.get(12), vs.get(5), // in side
					u + dz + dx, v + dz + dy0,
					u + 2 * dz + dx, v + dz + dy02,
					u + 2 * dz + dx, v + dz + dy03,
					u + dz + dx, v + dz + dy0);
			this.setQuard(i++, vs.get(4), vs.get(11), vs.get(13), vs.get(4), // out side
					u + dz, v + dz + dy0,
					u, v + dz + dy02,
					u, v + dz + dy03,
					u + dz, v + dz + dy0);
			// level #4
			Vec2f yz14 = this.rotate(cr, new Vec2f(ye1, tvs[0].y), cos2, sin2);
			Vec2f yz15 = this.rotate(cr, new Vec2f(ye1, tvs[1].y), cos2, sin2);
			Vec2f yz16 = this.rotate(cr, new Vec2f(ye1, tvs[2].y), cos2, sin2);
			Vec2f yz17 = this.rotate(cr, new Vec2f(ye1, tvs[3].y), cos2, sin2);
			vs.put(14, new PositionTextureVertex(tvs[0].x, yz14.x, yz14.y, 0.0F, 0.0F));
			vs.put(15, new PositionTextureVertex(tvs[1].x, yz15.x, yz15.y, 0.0F, 0.0F));
			vs.put(16, new PositionTextureVertex(tvs[2].x, yz16.x, yz16.y, 0.0F, 0.0F));
			vs.put(17, new PositionTextureVertex(tvs[3].x, yz17.x, yz17.y, 0.0F, 0.0F));
			if (c) {
				this.setQuard(i++, vs.get(5), vs.get(4), vs.get(15), vs.get(15), // front 0
						u + dz + dx, v + dz + dy0,
						u + dz, v + dz + dy0,
						u + dz + dx, v + dz + dy1,
						u + dz + dx, v + dz + dy1);
				this.setQuard(i++, vs.get(15), vs.get(4), vs.get(14), vs.get(14), // front 1
						u + dz + dx, v + dz + dy1,
						u + dz, v + dz + dy0,
						u + dx, v + dz + dy1,
						u + dx, v + dz + dy1);
				this.setQuard(i++, vs.get(12), vs.get(13), vs.get(17), vs.get(16), // back
						u + 2 * dz + dx, v + dz + dy03,
						u + 2 * (dz + dx), v + dz + dy03,
						u + 2 * (dz + dx), v + dz + dy1,
						u + 2 * dz + dx, v + dz + dy1);
				this.setQuard(i++, vs.get(12), vs.get(5), vs.get(16), vs.get(16), // in side 0
						u + 2 * dz + dx, v + dz + dy03,
						u + dz + dx, v + dz + dy0,
						u + 2 * dz + dx, v + dz + dy1,
						u + 2 * dz + dx, v + dz + dy1);
				this.setQuard(i++, vs.get(16), vs.get(5), vs.get(15), vs.get(15), // in side 1
						u + 2 * dz + dx, v + dz + dy1,
						u + dz + dx, v + dz + dy0,
						u + dz + dx, v + dz + dy1,
						u + dz + dx, v + dz + dy1);
				this.setQuard(i++, vs.get(4), vs.get(13), vs.get(14), vs.get(14), // out side 0
						u + dz, v + dz + dy0,
						u, v + dz + dy03,
						u + dz, v + dz + dy1,
						u + dz, v + dz + dy1);
				this.setQuard(i++, vs.get(14), vs.get(13), vs.get(17), vs.get(17), // out side 1
						u + dz, v + dz + dy1,
						u, v + dz + dy03,
						u, v + dz + dy1,
						u, v + dz + dy1);
			} else {
				this.setQuard(i++, vs.get(5), vs.get(4), vs.get(14), vs.get(15), u + dz, v + dz + dy0, u + dz + dx, v + dz + dy1); // front
				this.setQuard(i++, vs.get(13), vs.get(12), vs.get(16), vs.get(17), u + 2 * dz + dx, v + dz + dy03, u + 2 * (dz + dx), v + dz + dy1); // back
				this.setQuard(i++, vs.get(12), vs.get(5), vs.get(15), vs.get(16), // in side
						u + 2 * dz + dx, v + dz + dy03,
						u + dz + dx, v + dz + dy0,
						u + dz + dx, v + dz + dy1,
						u + 2 * dz + dx, v + dz + dy1);
				this.setQuard(i++, vs.get(4), vs.get(13), vs.get(17), vs.get(14), // out side
						u + dz, v + dz + dy0,
						u, v + dz + dy03,
						u, v + dz + dy1,
						u + dz, v + dz + dy1);
			}
			// level #5
			Vec2f yz18 = this.rotate(cr, new Vec2f(ye2, tvs[4].y), cos2, sin2);
			Vec2f yz19 = this.rotate(cr, new Vec2f(ye2, tvs[5].y), cos2, sin2);
			Vec2f yz20 = this.rotate(cr, new Vec2f(ye2, tvs[6].y), cos2, sin2);
			Vec2f yz21 = this.rotate(cr, new Vec2f(ye2, tvs[7].y), cos2, sin2);
			vs.put(18, new PositionTextureVertex(tvs[4].x, yz18.x, yz18.y, 0.0F, 0.0F));
			vs.put(19, new PositionTextureVertex(tvs[5].x, yz19.x, yz19.y, 0.0F, 0.0F));
			vs.put(20, new PositionTextureVertex(tvs[6].x, yz20.x, yz20.y, 0.0F, 0.0F));
			vs.put(21, new PositionTextureVertex(tvs[7].x, yz21.x, yz21.y, 0.0F, 0.0F));
			this.setQuard(i++, vs.get(15), vs.get(14), vs.get(18), vs.get(19), u + dz, v + dz + dy1, u + dz + dx, v + dz + dy2); // front
			this.setQuard(i++, vs.get(17), vs.get(16), vs.get(20), vs.get(21), u + 2 * dz + dx, v + dz + dy1, u + 2 * (dz + dx), v + dz + dy2); // back
			this.setQuard(i++, vs.get(16), vs.get(15), vs.get(19), vs.get(20), u + dz + dx, v + dz + dy1, u + 2 * dz + dx, v + dz + dy2);// in side
			this.setQuard(i++, vs.get(14), vs.get(17), vs.get(21), vs.get(18), u, v + dz + dy1, u + dz, v + dz + dy2);// out side
			this.setQuard(i++, vs.get(19), vs.get(18), vs.get(21), vs.get(20), u + dz + dx, v, u + 2 * dz + dx, v + dz); // down
		} else {
			// Calculated fillet positions
			Vec2f g0 = new Vec2f(cr.x + (z - cr.y) * sin0 + (ye0 - cr.x) * cos0, cr.y + (z - cr.y) * cos0 - (ye0 - cr.x) * sin0);
			Vec2f g1 = new Vec2f(cr.x + (z - cr.y) * sin1 + (ye0 - cr.x) * cos1, cr.y + (z - cr.y) * cos1 - (ye0 - cr.x) * sin1);
			Vec2f g2 = new Vec2f(cr.x + (z - cr.y) * sin2 + (ye0 - cr.x) * cos2, cr.y + (z - cr.y) * cos2 - (ye0 - cr.x) * sin2);
			float t = (ze - cr.y) * tan;
			Vec2f g3 = new Vec2f(ye0 + t, ze);
			float d0 = (float) Math.hypot(g0.x-g1.x, g0.y-g1.y);
			float n0 = dy0 / (dy0 + d0 * 1.5f) * d0 * 1.5f;
			float n1 = (dy1 - dy0) / ((dy1 - dy0) + d0 * 1.5f) * d0 * 1.5f;
			float dy00 = dy0 - n0;
			float dy01 = dy00 + n0 / 1.5f;
			float dy02 = dy0 + n1 / 3.0f;
			float dy03 = (dy0 + n1) * 0.85f;
			vs.put(4, new PositionTextureVertex(x, ye0, z, 0.0F, 0.0F));
			vs.put(5, new PositionTextureVertex(xe, ye0, z, 0.0F, 8.0F));
			vs.put(6, new PositionTextureVertex(xe, g3.x, ze, 8.0F, 8.0F));
			vs.put(7, new PositionTextureVertex(x, g3.x, ze, 8.0F, 0.0F));
			// level #0
			this.setQuard(i++, vs.get(1), vs.get(0), vs.get(4), vs.get(5), u + dz, v + dz, u + dz + dx, v + dz + dy00); // front
			this.setQuard(i++, vs.get(3), vs.get(2), vs.get(6), vs.get(7), u + 2 * dz + dx, v + dz, u + 2 * (dz + dx), v + dz + dy0); // back
			this.setQuard(i++, vs.get(1), vs.get(2), vs.get(6), vs.get(5), // in side
					u + dz + dx, v + dz,
					u + 2 * dz + dx, v + dz,
					u + 2 * dz + dx, v + dz + dy0,
					u + dz + dx, v + dz + dy00);
			this.setQuard(i++, vs.get(0), vs.get(3), vs.get(7), vs.get(4), // out side
					u + dz, v + dz,
					u, v + dz, 
					u, v + dz + dy0,
					u + dz, v + dz + dy00);
			// level #1
			vs.put(8, new PositionTextureVertex(xe, g0.x, g0.y, 0.0F, 0.0F));
			vs.put(9, new PositionTextureVertex(x, g0.x, g0.y, 0.0F, 8.0F));
			this.setQuard(i++, vs.get(4), vs.get(5), vs.get(8), vs.get(9), // front
					u + dz, v + dz + dy00,
					u + dz + dx, v + dz + dy00,
					u + dz + dx, v + dz + dy01,
					u + dz, v + dz + dy01);
			this.setQuard(i++, vs.get(5), vs.get(6), vs.get(8), vs.get(5), // in side
					u + dz + dx, v + dz + dy00,
					u + 2 * dz + dx, v + dz + dy0,
					u + dz + dx, v + dz + dy01,
					u + dz + dx, v + dz + dy00);
			this.setQuard(i++, vs.get(4), vs.get(7), vs.get(9), vs.get(4), // out side
					u + dz, v + dz + dy00,
					u, v + dz + dy0,
					u + dz, v + dz + dy01,
					u + dz, v + dz + dy00);
			// level #2
			vs.put(10, new PositionTextureVertex(xe, g1.x, g1.y, 0.0F, 0.0F));
			vs.put(11, new PositionTextureVertex(x, g1.x, g1.y, 0.0F, 8.0F));
			this.setQuard(i++, vs.get(9), vs.get(8), vs.get(10), vs.get(11), // front
					u + dz, v + dz + dy01,
					u + dz + dx, v + dz + dy01,
					u + dz + dx, v + dz + dy02,
					u + dz, v + dz + dy02);
			this.setQuard(i++, vs.get(8), vs.get(6), vs.get(10), vs.get(8), // in side
					u + dz + dx, v + dz + dy01,
					u + 2 * dz + dx, v + dz + dy0,
					u + dz + dx, v + dz + dy02,
					u + dz + dx, v + dz + dy01);
			this.setQuard(i++, vs.get(9), vs.get(7), vs.get(11), vs.get(9), // out side
					u + dz, v + dz + dy01,
					u, v + dz + dy0,
					u + dz, v + dz + dy02,
					u + dz, v + dz + dy01);
			// level #3
			vs.put(12, new PositionTextureVertex(xe, g2.x, g2.y, 0.0F, 0.0F));
			vs.put(13, new PositionTextureVertex(x, g2.x, g2.y, 0.0F, 8.0F));
			this.setQuard(i++, vs.get(11), vs.get(10), vs.get(12), vs.get(13), // front
					u + dz, v + dz + dy02,
					u + dz + dx, v + dz + dy02,
					u + dz + dx, v + dz + dy03,
					u + dz, v + dz + dy03);
			this.setQuard(i++, vs.get(10), vs.get(6), vs.get(12), vs.get(10), // in side
					u + dz + dx, v + dz + dy02,
					u + 2 * dz + dx, v + dz + dy0,
					u + dz + dx, v + dz + dy03,
					u + dz + dx, v + dz + dy02);
			this.setQuard(i++, vs.get(11), vs.get(7), vs.get(13), vs.get(11), // out side
					u + dz, v + dz + dy02,
					u, v + dz + dy0,
					u + dz, v + dz + dy03,
					u + dz, v + dz + dy02);
			// level #4
			Vec2f yz14 = this.rotate(cr, new Vec2f(ye1, tvs[0].y), cos2, sin2);
			Vec2f yz15 = this.rotate(cr, new Vec2f(ye1, tvs[1].y), cos2, sin2);
			Vec2f yz16 = this.rotate(cr, new Vec2f(ye1, tvs[2].y), cos2, sin2);
			Vec2f yz17 = this.rotate(cr, new Vec2f(ye1, tvs[3].y), cos2, sin2);
			vs.put(14, new PositionTextureVertex(tvs[0].x, yz14.x, yz14.y, 0.0F, 0.0F));
			vs.put(15, new PositionTextureVertex(tvs[1].x, yz15.x, yz15.y, 0.0F, 0.0F));
			vs.put(16, new PositionTextureVertex(tvs[2].x, yz16.x, yz16.y, 0.0F, 0.0F));
			vs.put(17, new PositionTextureVertex(tvs[3].x, yz17.x, yz17.y, 0.0F, 0.0F));
			if (c) {
				this.setQuard(i++, vs.get(13), vs.get(12), vs.get(15), vs.get(15), u, v + dz + dy03, u + dz, v + dz + dy1); // front 0
				this.setQuard(i++, vs.get(15), vs.get(13), vs.get(14), vs.get(15),
						u, v + dz + dy1,
						u + dz, v + dz + dy03,
						u + dz, v + dz + dy1,
						u + dz, v + dz + dy1); // front 1
				this.setQuard(i++, vs.get(6), vs.get(7), vs.get(17), vs.get(17), u + dz + dx, v + dz + dy0, u + 2 * dz + dx, v + dz + dy1); // back 0
				this.setQuard(i++, vs.get(6), vs.get(17), vs.get(16), vs.get(16), // back 1
						u + 2 * dz + dx, v + dz + dy0,
						u + dz + dx, v + dz + dy1,
						u + 2 * dz + dx, v + dz + dy1,
						u + 2 * dz + dx, v + dz + dy1);
				this.setQuard(i++, vs.get(12), vs.get(6), vs.get(16), vs.get(16), // in side 0
						u + dz + dx, v + dz + dy03,
						u + 2 * dz + dx, v + dz + dy0,
						u + 2 * dz + dx, v + dz + dy1,
						u + dz + dx, v + dz + dy0);
				this.setQuard(i++, vs.get(12), vs.get(16), vs.get(15), vs.get(15), // in side 1
						u + dz + dx, v + dz + dy03,
						u + 2 * dz + dx, v + dz + dy1,
						u + dz + dx, v + dz + dy1,
						u + dz + dx, v + dz + dy1);
				this.setQuard(i++, vs.get(13), vs.get(7), vs.get(14), vs.get(14), // out side 0
						u + dz, v + dz + dy03,
						u, v + dz + dy0,
						u + dz, v + dz + dy1,
						u + dz, v + dz + dy1);
				this.setQuard(i++, vs.get(14), vs.get(7), vs.get(17), vs.get(14), // out side 1
						u + dz, v + dz + dy1,
						u, v + dz + dy0,
						u, v + dz + dy1,
						u + dz, v + dz + dy1);
			} else {
				this.setQuard(i++, vs.get(13), vs.get(12), vs.get(14), vs.get(14), // front 0
						u + dz, v + dz + dy03,
						u, v + dz + dy03,
						u + dz, v + dz + dy1,
						u + dz, v + dz + dy1);
				this.setQuard(i++, vs.get(12), vs.get(14), vs.get(15), vs.get(15), // front 1
						u, v + dz + dy03,
						u + dz, v + dz + dy1,
						u, v + dz + dy1,
						u, v + dz + dy1);
				this.setQuard(i++, vs.get(7), vs.get(6), vs.get(16), vs.get(16), u + 2 * dz + dx, v + dz + dy0, u + 2 * (dz + dx), v + dz + dy1); // back 0
				this.setQuard(i++, vs.get(7), vs.get(16), vs.get(17), vs.get(17), // back 1
						u + 2 * (dz + dx), v + dz + dy0,
						u + 2 * dz + dx, v + dz + dy1,
						u + 2 * (dz + dx), v + dz + dy1,
						u + 2 * (dz + dx), v + dz + dy1);
				this.setQuard(i++, vs.get(12), vs.get(6), vs.get(15), vs.get(15), // in side 0
						u + dz + dx, v + dz + dy03,
						u + 2 * dz + dx, v + dz + dy0,
						u + dz + dx, v + dz + dy1,
						u + dz + dx, v + dz + dy1);
				this.setQuard(i++, vs.get(15), vs.get(6), vs.get(16), vs.get(16), // in side 1
						u + dz + dx, v + dz + dy1,
						u + 2 * dz + dx, v + dz + dy0,
						u + 2 * dz + dx, v + dz + dy1,
						u + 2 * dz + dx, v + dz + dy1);
				this.setQuard(i++, vs.get(13), vs.get(7), vs.get(17), vs.get(17), // out side 0
						u + dz, v + dz + dy03,
						u, v + dz + dy0,
						u, v + dz + dy1,
						u, v + dz + dy1);
				this.setQuard(i++, vs.get(13), vs.get(17), vs.get(14), vs.get(14), // out side 1
						u + dz, v + dz + dy03,
						u, v + dz + dy1,
						u + dz, v + dz + dy1,
						u + dz, v + dz + dy1);
			}
			// level #5
			Vec2f yz18 = this.rotate(cr, new Vec2f(ye2, tvs[4].y), cos2, sin2);
			Vec2f yz19 = this.rotate(cr, new Vec2f(ye2, tvs[5].y), cos2, sin2);
			Vec2f yz20 = this.rotate(cr, new Vec2f(ye2, tvs[6].y), cos2, sin2);
			Vec2f yz21 = this.rotate(cr, new Vec2f(ye2, tvs[7].y), cos2, sin2);
			vs.put(18, new PositionTextureVertex(tvs[4].x, yz18.x, yz18.y, 0.0F, 0.0F));
			vs.put(19, new PositionTextureVertex(tvs[5].x, yz19.x, yz19.y, 0.0F, 0.0F));
			vs.put(20, new PositionTextureVertex(tvs[6].x, yz20.x, yz20.y, 0.0F, 0.0F));
			vs.put(21, new PositionTextureVertex(tvs[7].x, yz21.x, yz21.y, 0.0F, 0.0F));
			this.setQuard(i++, vs.get(15), vs.get(14), vs.get(18), vs.get(19), u + dz, v + dz + dy1, u + dz + dx, v + dz + dy2); // front
			this.setQuard(i++, vs.get(17), vs.get(16), vs.get(20), vs.get(21), u + 2 * dz + dx, v + dz + dy1, u + 2 * (dz + dx), v + dz + dy2); // back
			this.setQuard(i++, vs.get(16), vs.get(15), vs.get(19), vs.get(20), u + dz + dx, v + dz + dy1, u + 2 * dz + dx, v + dz + dy2);// in side
			this.setQuard(i++, vs.get(14), vs.get(17), vs.get(21), vs.get(18), u, v + dz + dy1, u + dz, v + dz + dy2);// out side
			this.setQuard(i++, vs.get(19), vs.get(18), vs.get(21), vs.get(20), u + dz + dx, v, u + 2 * dz + dx, v + dz); // down
		}
		this.draw(scale);
	}

	private void draw(float scale) {
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder br = tess.getBuffer();
		for (TexturedQuad tq : quads.values()) {
			if (tq == null) { continue; }
			tq.draw(br, scale);
		}
	}

	private Vec2f rotate(Vec2f centr, Vec2f vertex, double cos, double sin) {
		float y = (float) (centr.y + (vertex.y - centr.y) * cos - (vertex.x - centr.x) * sin);
		float x = (float) (centr.x + (vertex.y - centr.y) * sin + (vertex.x - centr.x) * cos);
		return new Vec2f(x, y);
	}

	private void setQuard(int i, PositionTextureVertex ptv0, PositionTextureVertex ptv1, PositionTextureVertex ptv2, PositionTextureVertex ptv3, float u1, float v1, float u2, float v2) {
		quads.put(i, new TexturedQuad(new PositionTextureVertex[] { ptv0, ptv1, ptv2, ptv3}));
		quads.get(i).vertexPositions[0] = ptv0.setTexturePosition(u2 / this.textureWidth, v1 / this.textureHeight);
		quads.get(i).vertexPositions[1] = ptv1.setTexturePosition(u1 / this.textureWidth, v1 / this.textureHeight);
		quads.get(i).vertexPositions[2] = ptv2.setTexturePosition(u1 / this.textureWidth, v2 / this.textureHeight);
		quads.get(i).vertexPositions[3] = ptv3.setTexturePosition(u2 / this.textureWidth, v2 / this.textureHeight);
	}
	
	private void setQuard(int i, PositionTextureVertex ptv0, PositionTextureVertex ptv1, PositionTextureVertex ptv2, PositionTextureVertex ptv3, float u0, float v0, float u1, float v1, float u2, float v2, float u3, float v3) {
		quads.put(i, new TexturedQuad(new PositionTextureVertex[] { ptv0, ptv1, ptv2, ptv3}));
		quads.get(i).vertexPositions[0] = ptv0.setTexturePosition(u0 / this.textureWidth, v0 / this.textureHeight);
		quads.get(i).vertexPositions[1] = ptv1.setTexturePosition(u1 / this.textureWidth, v1 / this.textureHeight);
		quads.get(i).vertexPositions[2] = ptv2.setTexturePosition(u2 / this.textureWidth, v2 / this.textureHeight);
		quads.get(i).vertexPositions[3] = ptv3.setTexturePosition(u3 / this.textureWidth, v3 / this.textureHeight);
	}
	
	public void setIsNormal(boolean bo) { this.isNormal = bo; }

	public void copyModelAngles(ModelRendererAlt source) {
		this.rotateAngleX = source.rotateAngleX;
		this.rotateAngleY = source.rotateAngleY;
		this.rotateAngleZ = source.rotateAngleZ;
		this.rotationPointX = source.rotationPointX;
		this.rotationPointY = source.rotationPointY;
		this.rotationPointZ = source.rotationPointZ;
		this.rotateAngleX1 = source.rotateAngleX1;
		this.rotateAngleY1 = source.rotateAngleY1;
		this.scaleX = source.scaleX;
		this.scaleY = source.scaleY;
		this.scaleZ = source.scaleZ;
		this.offsetAnimX = source.offsetAnimX;
		this.offsetAnimY = source.offsetAnimY;
		this.offsetAnimZ = source.offsetAnimZ;
	}

	public void chechBacklightColor(float r, float g, float b) {
		if (ModelNpcAlt.editAnimDataSelect.isNPC && ModelNpcAlt.editAnimDataSelect.part == this.part) {
			this.r = ModelNpcAlt.editAnimDataSelect.red;
			this.g = ModelNpcAlt.editAnimDataSelect.green;
			this.b = ModelNpcAlt.editAnimDataSelect.blue;
		}
		else {
			this.r = r;
			this.g = g;
			this.b = b;
		}
	}

	public void clearOBJ() {
		if (this.displayOBJListUp > 0) { this.displayOBJListUp = -1; }
		if (this.displayOBJListDown > 0) { this.displayOBJListDown = -1; }
	}

	public void setOBJModel(ItemStack stack, EnumParts part) {
		if (stack == null || !(stack.getItem() instanceof CustomArmor)) {
			this.displayOBJListUp = 0;
			this.displayOBJListDown = 0;
			return;
		}
		CustomArmor armor = (CustomArmor) stack.getItem();
		Map<String, String> map = null;
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("OBJTexture")) {
			ResourceLocation mainTexture = ModelBuffer.getMainOBJTexture(armor.objModel);
			if (mainTexture != null) {
				map = Maps.<String, String>newHashMap();
				map.put(mainTexture.toString(), stack.getTagCompound().getString("OBJTexture"));
			}
		}
		if ((this.part == EnumParts.LEG_RIGHT && part == EnumParts.FEET_RIGHT) ||
			(this.part == EnumParts.LEG_LEFT && part == EnumParts.FEET_LEFT)) {
			this.displayOBJListDown = ModelBuffer.getDisplayList(armor.objModel, armor.getMeshNames(part), map);
		}
		else {
			this.displayOBJListUp = ModelBuffer.getDisplayList(armor.objModel, armor.getMeshNames(part != null ? part : this.part), map);
			switch(this.part) {
				case ARM_RIGHT: {
					this.displayOBJListDown = ModelBuffer.getDisplayList(armor.objModel, armor.getMeshNames(EnumParts.WRIST_RIGHT), map);
					break;
				}
				case ARM_LEFT: {
					this.displayOBJListDown = ModelBuffer.getDisplayList(armor.objModel, armor.getMeshNames(EnumParts.WRIST_LEFT), map);
					break;
				}
				case LEG_RIGHT: {
					this.displayOBJListDown = ModelBuffer.getDisplayList(armor.objModel, armor.getMeshNames(EnumParts.FOOT_RIGHT), map);
					break;
				}
				case LEG_LEFT: {
					this.displayOBJListDown = ModelBuffer.getDisplayList(armor.objModel, armor.getMeshNames(EnumParts.FOOT_LEFT), map);
					break;
				}
				default: { break; }
			}
		}
	}

	public boolean isOBJModel() {
		return this.displayOBJListUp > 0 || this.displayOBJListDown > 0;
	}

	public void setBaseData(ModelPartConfig config) {
		if (config == null) {
			this.offsetAnimX = 0.0f;
			this.offsetAnimY = 0.0f;
			this.offsetAnimZ = 0.0f;
			this.scaleX = 1.0f;
			this.scaleY = 1.0f;
			this.scaleZ = 1.0f;
			return;
		}
		this.offsetAnimX = config.offset[0];
		this.offsetAnimY = config.offset[1];
		this.offsetAnimZ = config.offset[2];
		this.scaleX = config.scale[0];
		this.scaleY = config.scale[1];
		this.scaleZ = config.scale[2];
	}
	
	/**
	 * @param model 
	 * @param animation = rots[ 0:rotX, 1:rotY, 2:rotZ, 3:ofsX, 4:ofsY, 5:ofsZ, 6:scX, 7:scY, 8:scZ, 9:rotX1, 10:rotY1 ]
	 */
	public void setAnimation(DataAnimation animation) {
		if (!this.showModel) { return; }
		if (animation.currentFrame != null && animation.currentFrame.parts.containsKey(this.idPart)) {
			this.showModel = animation.currentFrame.parts.get(this.idPart).show;
		}
		if (!this.showModel) { return; }
		Float[] partSets = animation.rots.get(this.idPart);
		if (partSets != null) {
			this.rotateAngleX = partSets[0];
			this.rotateAngleY = partSets[1];
			this.rotateAngleZ = partSets[2];
			this.offsetAnimX += partSets[3];
			this.offsetAnimY += partSets[4];
			this.offsetAnimZ += partSets[5];
			this.scaleX *= partSets[6];
			this.scaleY *= partSets[7];
			this.scaleZ *= partSets[8];
			if (!this.isNormal) {
				this.rotateAngleX1 = partSets[9];
				this.rotateAngleY1 = partSets[10];
			}
			this.isAnimPart = partSets[0] != 0.0f || partSets[1] != 0.0f || partSets[2] != 0.0f || partSets[3] != 0.0f || partSets[4] != 0.0f || partSets[5] != 0.0f || partSets[6] != 1.0f || partSets[7] != 1.0f || partSets[8] != 1.0f || partSets[9] != 0.0f || partSets[10] != 0.0f;
		}
		if (animation.addParts.containsKey(this.idPart)) {
			for (AddedPartConfig part : animation.addParts.get(this.idPart)) {
				ModelRendererAlt child = new ModelRendererAlt(part, animation);
				this.addChild(child);
			}
		}
	}

	public void clearRotations() {
		this.rotateAngleX = 0.0f;
		this.rotateAngleY = 0.0f;
		this.rotateAngleZ = 0.0f;
		this.rotateAngleX1 = 0.0f;
		this.rotateAngleY1 = 0.0f;
		this.offsetAnimX = 0.0f;
		this.offsetAnimY = 0.0f;
		this.offsetAnimZ = 0.0f;
		this.scaleX = 1.0f;
		this.scaleY = 1.0f;
		this.scaleZ = 1.0f;
		this.isAnimPart = false;
	}

}
