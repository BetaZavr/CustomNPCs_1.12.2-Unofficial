package noppes.npcs.client.model.animation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;

public class AddedPartConfig extends PartConfig {

	public ResourceLocation location;
	public ResourceLocation obj;
	public int parentPart;

	public AddedPartConfig() {
		super();
	}

	public AddedPartConfig(int id) {
		super(id);
	}

	@Override
	public void clear() {
		super.clear();
		location = new ResourceLocation(CustomNpcs.MODID, "textures/gui/animation/default_part.png");
		obj = null;
		parentPart = -1;
	}

	public void readNBT(NBTTagCompound compound) {
		super.readNBT(compound);
		this.location = new ResourceLocation(compound.getString("Location"));
		this.parentPart = compound.getInteger("ParentPart");
		if (compound.hasKey("OBJLocation", 8)) {
			this.obj = new ResourceLocation(compound.getString("LOBJLocationocation"));
		}
	}

	public NBTTagCompound writeNBT() {
		NBTTagCompound compound = super.writeNBT();
		compound.setString("Location", this.location.toString());
		compound.setInteger("ParentPart", this.parentPart);
		if (this.obj != null) {
			compound.setString("OBJLocation", this.obj.toString());
		}
		return compound;
	}
}
