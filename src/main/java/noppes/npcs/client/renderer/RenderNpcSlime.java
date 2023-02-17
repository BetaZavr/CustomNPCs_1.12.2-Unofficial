package noppes.npcs.client.renderer;

import net.minecraft.client.model.ModelBase;
import noppes.npcs.client.layer.LayerSlimeNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class RenderNpcSlime<T extends EntityNPCInterface> extends RenderNPCInterface<T> {
	public ModelBase scaleAmount;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RenderNpcSlime(ModelBase par1ModelBase, ModelBase par2ModelBase, float par3) {
		super(par1ModelBase, par3);
		this.scaleAmount = par2ModelBase;
		this.addLayer(new LayerSlimeNpc(this));
	}
}
