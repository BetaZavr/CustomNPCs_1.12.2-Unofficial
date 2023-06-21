package noppes.npcs.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.api.ILayerModel;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.NBTWrapper;

public class LayerModel
implements ILayerModel {

	public ItemStack model = ItemStack.EMPTY;
	public ResourceLocation objModel = null;

	public float[] offsetAxis = new float[] { 0.0f, 0.0f, 0.0f };
	public float[] scaleAxis = new float[] { 1.0f, 1.0f, 1.0f };
	public float[] rotateAxis = new float[] { 0.0f, 0.0f, 0.0f };
	public byte[] isRotate = new byte[] { (byte) 0, (byte) 0, (byte) 0 };
	public int pos = 0;
	public int rotateSpeed = 1;
	
	public LayerModel(NBTTagCompound nbtLayer) { this.setNbt(new NBTWrapper(nbtLayer)); }

	public LayerModel(int i) {
		this.pos = i;
	}

	@Override
	public INbt getNbt() {
		NBTTagCompound nbtLayer = new NBTTagCompound();
		if (!this.model.isEmpty()) {
			nbtLayer.setTag("Model", this.model.writeToNBT(new NBTTagCompound()));
		}
		if (this.objModel!=null) {
			nbtLayer.setString("OBJModel", this.objModel.toString());
		}
		NBTTagList ra = new NBTTagList();
		for (float f : this.rotateAxis) {
			ra.appendTag(new NBTTagFloat(f));
		}
		nbtLayer.setTag("RotateAxis", ra);
		NBTTagList oa = new NBTTagList();
		for (float f : this.offsetAxis) {
			oa.appendTag(new NBTTagFloat(f));
		}
		nbtLayer.setTag("OffsetAxis", oa);
		NBTTagList sa = new NBTTagList();
		for (float f : this.scaleAxis) {
			sa.appendTag(new NBTTagFloat(f));
		}
		nbtLayer.setTag("ScaleAxis", sa);
		nbtLayer.setByteArray("isRotate", this.isRotate);
		nbtLayer.setInteger("Pos", this.pos);
		nbtLayer.setInteger("Speed", this.rotateSpeed);
		return NpcAPI.Instance().getINbt(nbtLayer);
	}

	@Override
	public void setNbt(INbt nbt) {
		NBTTagCompound nbtLayer = nbt.getMCNBT();
		if (nbtLayer.hasKey("Model", 10)) {
			this.model = new ItemStack(nbtLayer.getCompoundTag("Model"));
		}
		if (nbtLayer.hasKey("OBJModel", 8)) {
			this.objModel = new ResourceLocation(nbtLayer.getString("OBJModel"));
		}
		if (nbtLayer.getTagList("RotateAxis", 5).tagCount()==3) {
			for (int i=0; i<nbtLayer.getTagList("RotateAxis", 5).tagCount(); i++) {
				this.rotateAxis[i] = nbtLayer.getTagList("RotateAxis", 5).getFloatAt(i);
			}
		}
		if (nbtLayer.getTagList("OffsetAxis", 5).tagCount()==3) {
			for (int i=0; i<nbtLayer.getTagList("OffsetAxis", 5).tagCount(); i++) {
				this.offsetAxis[i] = nbtLayer.getTagList("OffsetAxis", 5).getFloatAt(i);
			}
		}
		if (nbtLayer.getTagList("ScaleAxis", 5).tagCount()==3) {
			for (int i=0; i<nbtLayer.getTagList("ScaleAxis", 5).tagCount(); i++) {
				this.scaleAxis[i] = nbtLayer.getTagList("ScaleAxis", 5).getFloatAt(i);
			}
		}
		if (nbtLayer.getByteArray("isRotate").length==3) {
			this.isRotate = nbtLayer.getByteArray("isRotate");
		}
		this.pos = nbtLayer.getInteger("Pos");
		this.setRotateSpeed(nbtLayer.getInteger("Speed"));
	}

	@Override
	public IItemStack getModel() { return NpcAPI.Instance().getIItemStack(this.model); }
	
	@Override
	public void setModel(IItemStack stack) { this.model = stack.getMCItemStack(); }

	@Override
	public String getOBJModel() { return this.objModel.toString(); }
	
	@Override
	public void setOBJModel(String path) { this.objModel = new ResourceLocation(path); }
	
	@Override
	public float getOffset(int axis) {
		if (axis<0) { axis *= -1; }
		if (axis>2) { axis %= 3; }
		return this.offsetAxis[axis];
	}
	
	@Override
	public void setOffset(float x, float y, float z) {
		this.offsetAxis[0] = x;
		this.offsetAxis[1] = y;
		this.offsetAxis[2] = z;
	}

	@Override
	public float getRotate(int axis) {
		if (axis<0) { axis *= -1; }
		if (axis>2) { axis %= 3; }
		return this.rotateAxis[axis];
	}
	
	@Override
	public void setRotate(float x, float y, float z) {
		this.rotateAxis[0] = x;
		this.rotateAxis[1] = y;
		this.rotateAxis[2] = z;
	}

	@Override
	public boolean isRotate(int axis) {
		if (axis<0) { axis *= -1; }
		if (axis>2) { axis %= 3; }
		return this.isRotate[axis] == (byte) 1;
	}
	
	@Override
	public void setIsRotate(boolean x, boolean y, boolean z) {
		this.isRotate[0] = x ? (byte) 1 : (byte) 0;
		this.isRotate[1] = y ? (byte) 1 : (byte) 0;
		this.isRotate[2] = z ? (byte) 1 : (byte) 0;
	}

	@Override
	public float getScale(int axis) {
		if (axis<0) { axis *= -1; }
		if (axis>2) { axis %= 3; }
		return this.scaleAxis[axis];
	}
	
	@Override
	public void setScale(float x, float y, float z) {
		if (x<0.1f) { x = 0.1f; }
		if (y<0.1f) { y = 0.1f; }
		if (z<0.1f) { z = 0.1f; }
		this.scaleAxis[0] = x;
		this.scaleAxis[1] = y;
		this.scaleAxis[2] = z;
	}

	@Override
	public int getPos() { return this.pos; }

	@Override
	public int getRotateSpeed() { return this.rotateSpeed; }
	
	@Override
	public void setRotateSpeed(int speed) {
		if (speed<1) { speed = 1; }
		if (speed>7) { speed = 7; }
		this.rotateSpeed = speed;
	}
	
}
