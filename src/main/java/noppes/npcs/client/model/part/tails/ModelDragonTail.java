package noppes.npcs.client.model.part.tails;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import noppes.npcs.client.model.ModelPlaneRenderer;

public class ModelDragonTail extends ModelRenderer {

	public ModelDragonTail(ModelBiped base) {
		super(base);
		int x = 52;
		int y = 16;
		ModelRenderer dragon = new ModelRenderer(base, x, y);
		dragon.setRotationPoint(0.0f, 0.0f, 3.0f);
		this.addChild(dragon);
		ModelRenderer DragonTail2 = new ModelRenderer(base, x, y);
		DragonTail2.setRotationPoint(0.0f, 2.0f, 2.0f);
		ModelRenderer DragonTail3 = new ModelRenderer(base, x, y);
		DragonTail3.setRotationPoint(0.0f, 4.5f, 4.0f);
		ModelRenderer DragonTail4 = new ModelRenderer(base, x, y);
		DragonTail4.setRotationPoint(0.0f, 7.0f, 5.75f);
		ModelRenderer DragonTail5 = new ModelRenderer(base, x, y);
		DragonTail5.setRotationPoint(0.0f, 9.0f, 8.0f);
		ModelPlaneRenderer planeLeft = new ModelPlaneRenderer(base, x, y);
		planeLeft.addSidePlane(-1.5f, -1.5f, -1.5f, 3, 3);
		ModelPlaneRenderer planeRight = new ModelPlaneRenderer(base, x, y);
		planeRight.addSidePlane(-1.5f, -1.5f, -1.5f, 3, 3);
		this.setRotation(planeRight, 3.1415927f, 3.1415927f, 0.0f);
		ModelPlaneRenderer planeTop = new ModelPlaneRenderer(base, x, y);
		planeTop.addTopPlane(-1.5f, -1.5f, -1.5f, 3, 3);
		this.setRotation(planeTop, 0.0f, -1.5707964f, 0.0f);
		ModelPlaneRenderer planeBottom = new ModelPlaneRenderer(base, x, y);
		planeBottom.addTopPlane(-1.5f, -1.5f, -1.5f, 3, 3);
		this.setRotation(planeBottom, 0.0f, -1.5707964f, 3.1415927f);
		ModelPlaneRenderer planeBack = new ModelPlaneRenderer(base, x, y);
		planeBack.addBackPlane(-1.5f, -1.5f, -1.5f, 3, 3);
		this.setRotation(planeBack, 0.0f, 0.0f, 1.5707964f);
		ModelPlaneRenderer planeFront = new ModelPlaneRenderer(base, x, y);
		planeFront.addBackPlane(-1.5f, -1.5f, -1.5f, 3, 3);
		this.setRotation(planeFront, 0.0f, 3.1415927f, -1.5707964f);
		dragon.addChild(planeLeft);
		dragon.addChild(planeRight);
		dragon.addChild(planeTop);
		dragon.addChild(planeBottom);
		dragon.addChild(planeFront);
		dragon.addChild(planeBack);
		DragonTail2.addChild(planeLeft);
		DragonTail2.addChild(planeRight);
		DragonTail2.addChild(planeTop);
		DragonTail2.addChild(planeBottom);
		DragonTail2.addChild(planeFront);
		DragonTail2.addChild(planeBack);
		DragonTail3.addChild(planeLeft);
		DragonTail3.addChild(planeRight);
		DragonTail3.addChild(planeTop);
		DragonTail3.addChild(planeBottom);
		DragonTail3.addChild(planeFront);
		DragonTail3.addChild(planeBack);
		DragonTail4.addChild(planeLeft);
		DragonTail4.addChild(planeRight);
		DragonTail4.addChild(planeTop);
		DragonTail4.addChild(planeBottom);
		DragonTail4.addChild(planeFront);
		DragonTail4.addChild(planeBack);
		dragon.addChild(DragonTail2);
		dragon.addChild(DragonTail3);
		dragon.addChild(DragonTail4);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {

	}

}
