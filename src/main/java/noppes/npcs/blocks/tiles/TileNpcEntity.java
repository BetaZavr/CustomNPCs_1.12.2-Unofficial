package noppes.npcs.blocks.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.api.wrapper.data.Data;

import javax.annotation.Nonnull;

public class TileNpcEntity extends TileEntity {

	public Data tempData = new Data();
	public Data storedData = new Data();

	public void readFromNBT(@Nonnull NBTTagCompound compound) {
		super.readFromNBT(compound);
		NBTTagCompound extraData = compound.getCompoundTag("ExtraData");
		if (extraData.getSize() > 0) {
			getTileData().setTag("CustomNPCsData", extraData);
		}
	}

	public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
		compound.setTag("ExtraData", storedData.getNbt().getMCNBT());
		return super.writeToNBT(compound);
	}

}
