package noppes.npcs.client.renderer;

import noppes.npcs.client.model.ModelNpcCrystal;
import noppes.npcs.entity.EntityNPCInterface;

public class RenderNpcCrystal<T extends EntityNPCInterface> extends RenderNPCInterface<T> {
	ModelNpcCrystal mainmodel;

	public RenderNpcCrystal(ModelNpcCrystal model) {
		super(model, 0.0f);
		this.mainmodel = model;
	}
}
