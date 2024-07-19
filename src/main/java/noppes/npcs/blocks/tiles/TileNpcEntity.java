package noppes.npcs.blocks.tiles;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public class TileNpcEntity extends TileEntity {

	public Map<String, Object> tempData;

	public TileNpcEntity() {
		this.tempData = new HashMap<>();
	}

	public void readFromNBT(@Nonnull NBTTagCompound compound) {
		super.readFromNBT(compound);
		NBTTagCompound extraData = compound.getCompoundTag("ExtraData");
		if (extraData.getSize() > 0) {
			this.getTileData().setTag("CustomNPCsData", extraData);
		}
	}

	public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
		return super.writeToNBT(compound);
	}
}
