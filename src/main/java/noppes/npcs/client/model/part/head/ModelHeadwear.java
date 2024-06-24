package noppes.npcs.client.model.part.head;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import noppes.npcs.client.model.Model2DRenderer;
import noppes.npcs.client.model.ModelRendererAlt;
import noppes.npcs.constants.EnumParts;

public class ModelHeadwear
extends ModelRendererAlt {
	
	int size = 64;
	
	public ModelHeadwear(ModelBase modelBase, EnumParts part, int size) {
		this(modelBase, size);
		this.part = part;
	}
	
	public ModelHeadwear(ModelBase modelBase, EnumParts part, int x, int y, int size) {
		this(modelBase, part, size);
		this.setTextureOffset(x, y);
	}
	
	public ModelHeadwear(ModelBase base, int size) {
		super(base, EnumParts.HEAD, size, size, true);
		this.size = size;
		
		int s = size / 8;
		float p8 = (float) size / 8.0f;
		float p32 = (float) size / 2.0f;
		float p40 = p32 + p8;
		float p48 = p32 + 2.0f * p8;
		float p56 = 2.0f * p32 - p8;
		
		Model2DRenderer right = new Model2DRenderer(base, p32, p8, s, s, size, size);
		right.setRotationPoint(-4.641f, 0.8f, 4.64f);
		right.setScale(0.58f);
		right.setThickness(0.65f);
		this.setRotation(right, 0.0f, 1.5707964f, 0.0f);
		this.addChild(right);
		
		Model2DRenderer left = new Model2DRenderer(base, p48, p8, s, s, size, size);
		left.setRotationPoint(4.639f, 0.8f, -4.64f);
		left.setScale(0.58f);
		left.setThickness(0.65f);
		this.setRotation(left, 0.0f, -1.5707964f, 0.0f);
		this.addChild(left);
		
		Model2DRenderer front = new Model2DRenderer(base, p40, p8, s, s, size, size);
		front.setRotationPoint(-4.64f, 0.801f, -4.641f);
		front.setScale(0.58f);
		front.setThickness(0.65f);
		this.setRotation(front, 0.0f, 0.0f, 0.0f);
		this.addChild(front);
		
		Model2DRenderer back = new Model2DRenderer(base, p56, p8, s, s, size, size);
		back.setRotationPoint(4.64f, 0.801f, 4.639f);
		back.setScale(0.58f);
		back.setThickness(0.65f);
		this.setRotation(back, 0.0f, 3.1415927f, 0.0f);
		this.addChild(back);
		
		Model2DRenderer top = new Model2DRenderer(base, p40, 0.0f, s, s, size, size);
		top.setRotationPoint(-4.64f, -8.5f, -4.64f);
		top.setScale(0.5795f);
		top.setThickness(0.65f);
		this.setRotation(top, -1.5707964f, 0.0f, 0.0f);
		this.addChild(top);
		
		Model2DRenderer bottom = new Model2DRenderer(base, p48, 0.0f, s, s, size, size);
		bottom.setRotationPoint(-4.64f, 0.0f, -4.64f);
		bottom.setScale(0.5795f);
		bottom.setThickness(0.65f);
		this.setRotation(bottom, -1.5707964f, 0.0f, 0.0f);
		this.addChild(bottom);
	}

	public void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
	
}
