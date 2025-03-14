package noppes.npcs.client.renderer;

import net.minecraft.client.model.ModelBase;
import noppes.npcs.client.layer.LayerSlimeNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class RenderNpcSlime<T extends EntityNPCInterface> extends RenderNPCInterface<T> {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RenderNpcSlime(ModelBase par1ModelBase, ModelBase modelBase, float par3) {
		super(par1ModelBase, par3);
		this.addLayer(new LayerSlimeNpc(this));
	}
}
