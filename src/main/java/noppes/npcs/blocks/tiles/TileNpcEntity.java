package noppes.npcs.blocks.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.wrapper.data.Data;

import javax.annotation.Nonnull;

public class TileNpcEntity extends TileEntity {

	public Data tempData = new Data();
	public Data storedData = new Data();

	public void readFromNBT(@Nonnull NBTTagCompound compound) {
		CustomNpcs.debugData.start(this);
		super.readFromNBT(compound);
		NBTTagCompound extraData = compound.getCompoundTag("ExtraData");
		if (extraData.getSize() > 0) {
			getTileData().setTag("CustomNPCsData", extraData);
		}
		CustomNpcs.debugData.end(this);
	}

	public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
		CustomNpcs.debugData.start(this);
		compound.setTag("ExtraData", storedData.getNbt().getMCNBT());
		CustomNpcs.debugData.end(this);
		return super.writeToNBT(compound);
	}

}
