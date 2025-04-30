package noppes.npcs.blocks.tiles;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public class TileNpcEntity extends TileEntity {

	public Map<String, Object> tempData = new HashMap<>();
	public NBTTagCompound storedData = new NBTTagCompound();

	public void readFromNBT(@Nonnull NBTTagCompound compound) {
		super.readFromNBT(compound);
		NBTTagCompound extraData = compound.getCompoundTag("ExtraData");
		if (extraData.getSize() > 0) {
			getTileData().setTag("CustomNPCsData", extraData);
		}
	}

	public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
		compound.setTag("ExtraData", storedData);
		return super.writeToNBT(compound);
	}

	public void put(String key, NBTTagCompound value) {
		NBTTagList tagList;
		if (!storedData.hasKey("Content", 9)) {
			storedData.setTag("Content", tagList = new NBTTagList());
			storedData.setInteger("IsMap", 1);
		}
		else { tagList = storedData.getTagList("Content", 10); }
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound nbt = tagList.getCompoundTagAt(i);
			if (nbt.getString("K").equals(key)) {
				tagList.removeTag(i);
				break;
			}
		}
		tagList.appendTag(value);
	}

	public void remove(String key) {
		NBTTagList tagList;
		if (!storedData.hasKey("Content", 9)) {
			storedData.setTag("Content", tagList = new NBTTagList());
			storedData.setInteger("IsMap", 1);
		}
		else { tagList = storedData.getTagList("Content", 10); }
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound nbt = tagList.getCompoundTagAt(i);
			if (nbt.getString("K").equals(key)) {
				tagList.removeTag(i);
				break;
			}
		}
	}

	public void set(NBTTagCompound compound) {
		storedData = compound;
	}

	public void clear() {
		storedData = new NBTTagCompound();
	}

}
