package noppes.npcs.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import noppes.npcs.api.handler.capability.IWrapperEntityDataHandler;

public class WrapperEntityDataStorage implements Capability.IStorage<IWrapperEntityDataHandler> {

	@Override
	public void readNBT(Capability<IWrapperEntityDataHandler> capability, IWrapperEntityDataHandler instance,
			EnumFacing side, NBTBase nbt) {
		instance.setNBT((NBTTagCompound) nbt);
	}

	@Override
	public NBTBase writeNBT(Capability<IWrapperEntityDataHandler> capability, IWrapperEntityDataHandler instance,
			EnumFacing side) {
		return instance.getNBT();
	}

}
