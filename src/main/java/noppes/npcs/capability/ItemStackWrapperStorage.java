package noppes.npcs.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import noppes.npcs.api.handler.capability.IItemStackWrapperHandler;

public class ItemStackWrapperStorage implements Capability.IStorage<IItemStackWrapperHandler> {

	@Override
	public void readNBT(Capability<IItemStackWrapperHandler> capability, IItemStackWrapperHandler instance,
			EnumFacing side, NBTBase nbt) {
		instance.setMCNbt((NBTTagCompound) nbt);
	}

	@Override
	public NBTBase writeNBT(Capability<IItemStackWrapperHandler> capability, IItemStackWrapperHandler instance, EnumFacing side) {
		return instance.getMCNbt();
	}

}
