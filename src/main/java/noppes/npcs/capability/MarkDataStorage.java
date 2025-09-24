package noppes.npcs.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import noppes.npcs.api.handler.capability.IMarkDataHandler;

public class MarkDataStorage implements Capability.IStorage<IMarkDataHandler> {

	@Override
	public void readNBT(Capability<IMarkDataHandler> capability, IMarkDataHandler instance, EnumFacing side, NBTBase nbt) {
		instance.setNBT((NBTTagCompound) nbt);
	}

	@Override
	public NBTBase writeNBT(Capability<IMarkDataHandler> capability, IMarkDataHandler instance, EnumFacing side) {
		return instance.getNBT();
	}

}
