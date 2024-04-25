package noppes.npcs.blocks.tiles;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileNpcEntity extends TileEntity {

	public Map<String, Object> tempData;

	public TileNpcEntity() {
		this.tempData = new HashMap<String, Object>();
	}

	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		NBTTagCompound extraData = compound.getCompoundTag("ExtraData");
		if (extraData.getSize() > 0) {
			this.getTileData().setTag("CustomNPCsData", extraData);
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		return super.writeToNBT(compound);
	}
}
