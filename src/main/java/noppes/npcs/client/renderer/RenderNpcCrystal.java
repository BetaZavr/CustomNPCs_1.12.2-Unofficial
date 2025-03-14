package noppes.npcs.client.renderer;

import noppes.npcs.client.model.ModelNpcCrystal;
import noppes.npcs.entity.EntityNPCInterface;

public class RenderNpcCrystal<T extends EntityNPCInterface> extends RenderNPCInterface<T> {

	public RenderNpcCrystal(ModelNpcCrystal model) {
		super(model, 0.0f);
	}

}
