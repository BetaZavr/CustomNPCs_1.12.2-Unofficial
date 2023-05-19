package noppes.npcs.client.model.animation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.entity.data.IAnimationFrame;
import noppes.npcs.api.entity.data.IAnimationPart;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class AnimationFrameConfig
implements IAnimationFrame {
	
	public class PartConfig implements IAnimationPart {
		
		public float[] rotation, offset, scale;
		public int part;
		public boolean disable;
		private EntityNPCInterface npc;

		public PartConfig(EntityNPCInterface npc) {
			this.npc = npc;
			this.part = 0;
			this.clear();
		}

		@Override
		public void clear() {
			this.rotation = new float[] { 0.5f, 0.5f, 0.5f }; // 0.0 = 0; 1.0 = 360
			this.offset = new float[] { 0.5f, 0.5f, 0.5f }; // 0.0 = -5; 1.0 = 5
			this.scale = new float[] { 0.2f, 0.2f, 0.2f }; // 0.0 = 0; 1.0 = 5
			this.disable = false;
		}

		@Override
		public float[] getRotation() { return new float[] { this.rotation[0] * 360.0f, this.rotation[1] * 360.0f, this.rotation[2] * 360.0f }; }

		@Override
		public float[] getOffset() { return new float[] { 10.0f * this.offset[0] - 5.0f, 10.0f * this.offset[1] - 5.0f, 10.0f * this.offset[2] - 5.0f }; }

		@Override
		public float[] getScale() { return new float[] { this.scale[0] * 5.0f, this.scale[1] * 5.0f, this.scale[2] * 5.0f }; }
		
		@Override
		public void setRotation(float x, float y, float z) {
			x %= 360.0f;
			y %= 360.0f;
			z %= 360.0f;
			if (x<0.0f) { x += 360.0f; }
			if (y<0.0f) { y += 360.0f; }
			if (z<0.0f) { z += 360.0f; }
			this.rotation[0] = ValueUtil.correctFloat(x / 360.0f, 0.0f, 1.0f);
			this.rotation[1] = ValueUtil.correctFloat(y / 360.0f, 0.0f, 1.0f);
			this.rotation[2] = ValueUtil.correctFloat(z / 360.0f, 0.0f, 1.0f);
			this.updateClient();
		}
		
		@Override
		public void setOffset(float x, float y, float z) {
			x %= 5.0f;
			y %= 5.0f;
			z %= 5.0f;
			this.offset[0] = ValueUtil.correctFloat(x / 5.0f, 0.0f, 1.0f);
			this.offset[1] = ValueUtil.correctFloat(y / 5.0f, 0.0f, 1.0f);
			this.offset[2] = ValueUtil.correctFloat(z / 5.0f, 0.0f, 1.0f);
			this.updateClient();
		}
		
		@Override
		public void setScale(float x, float y, float z) {
			x %= 5.0f;
			y %= 5.0f;
			z %= 5.0f;
			this.scale[0] = ValueUtil.correctFloat(x / 5.0f, 0.0f, 1.0f);
			this.scale[1] = ValueUtil.correctFloat(y / 5.0f, 0.0f, 1.0f);
			this.scale[2] = ValueUtil.correctFloat(z / 5.0f, 0.0f, 1.0f);
			this.updateClient();
		}

		@Override
		public boolean isDisable() { return this.disable; }

		@Override
		public void setDisable(boolean bo) { this.disable = bo; }

		private void updateClient() {
			if (this.npc!=null) {
				this.npc.updateClient = true;
			} 
		}

		public void readNBT(NBTTagCompound compound) {
			for (int i=0; i<3; i++) {
				try { this.rotation[i] = ValueUtil.correctFloat(compound.getTagList("Rotation", 5).getFloatAt(i), -1.0f, 1.0f); } catch (Exception e) { }
				try { this.offset[i] = ValueUtil.correctFloat(compound.getTagList("Offset", 5).getFloatAt(i), -5.0f, 5.0f); } catch (Exception e) { }
				try { this.scale[i] = ValueUtil.correctFloat(compound.getTagList("Scale", 5).getFloatAt(i), 0.0f, 5.0f); } catch (Exception e) { }
			}
			this.part = compound.getInteger("Part");
			this.disable = compound.getBoolean("Disabled");
		}
		
		public NBTTagCompound writeNBT() {
			NBTTagCompound compound = new NBTTagCompound();
			NBTTagList listRot = new NBTTagList();
			NBTTagList listOff = new NBTTagList();
			NBTTagList listSc = new NBTTagList();
			for (int i=0; i<3; i++) {
				listRot.appendTag(new NBTTagFloat(this.rotation[i]));
				listOff.appendTag(new NBTTagFloat(this.offset[i]));
				listSc.appendTag(new NBTTagFloat(this.scale[i]));
			}
			compound.setTag("Rotation", listRot);
			compound.setTag("Offset", listOff);
			compound.setTag("Scale", listSc);
			compound.setInteger("Part", this.part);
			return compound;
		}
		
		public void setNpc(EntityNPCInterface npc) { this.npc = npc; }
		
	}

	public boolean smooth;
	public int speed, delay;
	public final PartConfig[] parts; // 0:head, 1:left arm, 2:right arm, 3:body, 4:left leg, 5:right leg

	public EntityNPCInterface npc;
	public int id;

	public AnimationFrameConfig() {
		this.parts = new PartConfig[6];
		for (int i=0; i<6; i++) { this.parts[i] = new PartConfig(npc);}
		this.id = 0;
		this.clear();
	}

	public void clear() {
		this.smooth = false;
		this.speed = 20;
		this.delay = 0;
	}
	
	private void updateClient() {
		if (this.npc!=null) {
			this.npc.updateClient = true;
		} 
	}
	
	public void readNBT(NBTTagCompound compound) {
		this.id = compound.getInteger("ID");
		this.setSmooth(compound.getBoolean("IsSmooth"));
		this.setSpeed(compound.getInteger("Speed"));
		this.setSpeed(compound.getInteger("EndDelay"));
		for (int i=0; i<6 && i<compound.getTagList("PartConfigs", 10).tagCount(); i++) {
			this.parts[i].readNBT(compound.getTagList("PartConfigs", 10).getCompoundTagAt(i));
			this.parts[i].part = i;
		}
	}
	
	public NBTTagCompound writeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("IsSmooth", this.smooth);
		compound.setInteger("ID", this.id);
		compound.setInteger("Speed", this.speed);
		compound.setInteger("EndDelay", this.delay);
		
		NBTTagList list = new NBTTagList();
		for (int i=0; i<6; i++) { list.appendTag(this.parts[i].writeNBT()); }
		compound.setTag("PartConfigs", list);
		
		return compound;
	}

	@Override
	public boolean isSmooth() { return this.smooth; }

	@Override
	public void setSmooth(boolean isSmooth) {
		this.smooth = isSmooth;
		this.updateClient();
	}

	@Override
	public int getSpeed() {
		if (this.speed<0) { this.speed *= -1; }
		if (this.speed>1200) { this.speed = 1200; }
		return this.speed;
	}

	@Override
	public void setSpeed(int ticks) {
		if (ticks<0) { ticks *= -1; }
		if (ticks>1200) { ticks = 1200; }
		this.speed = ticks;
		this.updateClient();
	}
	
	@Override
	public int getEndDelay() {
		if (this.delay<0) { this.delay *= -1; }
		return this.delay;
	}

	@Override
	public void setEndDelay(int ticks) {
		if (ticks<0) { ticks *= -1; }
		if (ticks>1200) { ticks = 1200; }
		this.delay = ticks;
		this.updateClient();
	}

	public void setNpc(EntityNPCInterface npc) {
		this.npc = npc;
		for (PartConfig pc : this.parts) { pc.setNpc(npc); }
	}

	public AnimationFrameConfig copy() {
		AnimationFrameConfig newAfc = new AnimationFrameConfig();
		newAfc.smooth = this.smooth;
		newAfc.speed = this.speed;
		newAfc.delay = this.delay;
		for (int i=0; i<6; i++) {
			newAfc.parts[i].readNBT(this.parts[i].writeNBT());
			newAfc.parts[i].part = i;
		}
		return newAfc;
	}
	
}

