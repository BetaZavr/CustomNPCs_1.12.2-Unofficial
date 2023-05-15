package noppes.npcs.client.model.animation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.entity.data.role.IJobPuppet.IJobPuppetPart;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class PartConfig
implements IJobPuppetPart {
	
	public boolean disabled;
	public float[] rotation;
	private EntityNPCInterface npc;
	public float speed;

	public PartConfig(EntityNPCInterface npc) {
		this.rotation = new float[] { 0.0f, 0.0f, 0.0f };
		this.disabled = false;
		this.npc = npc;
		this.speed = 40.0f;
	}

	@Override
	public int getRotationX() { return (int) ((this.rotation[0] + 1.0f) * 180.0f); }

	@Override
	public int getRotationY() { return (int) ((this.rotation[1] + 1.0f) * 180.0f); }

	@Override
	public int getRotationZ() { return (int) ((this.rotation[2] + 1.0f) * 180.0f); }

	@Override
	public void setRotation(int x, int y, int z) {
		this.disabled = false;
		this.rotation[0] = ValueUtil.correctFloat(x / 180.0f - 1.0f, -1.0f, 1.0f);
		this.rotation[1] = ValueUtil.correctFloat(y / 180.0f - 1.0f, -1.0f, 1.0f);
		this.rotation[2] = ValueUtil.correctFloat(z / 180.0f - 1.0f, -1.0f, 1.0f);
		this.npc.updateClient = true;
	}

	public void readNBT(NBTTagCompound compound) {
		for (int i=0; i<3 && i<compound.getTagList("Rotation", 5).tagCount(); i++) { this.rotation[i] = ValueUtil.correctFloat(compound.getTagList("Rotation", 5).getFloatAt(i), -1.0f, 1.0f); }
		this.disabled = compound.getBoolean("Disabled");
		this.speed = compound.getFloat("Speed");
		if (this.speed<0) { this.speed *= -1; }
	}
	
	public NBTTagCompound writeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (int i=0; i<3; i++) { list.appendTag(new NBTTagFloat(this.rotation[i])); }
		compound.setTag("Rotation", list);
		compound.setBoolean("Disabled", this.disabled);
		compound.setFloat("Speed", this.speed);
		return compound;
	}
	
}

