package noppes.npcs.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import noppes.npcs.api.handler.capability.INbtHandler;

public class NbtStorage
implements Capability.IStorage<INbtHandler> {

	@Override
	public NBTBase writeNBT(Capability<INbtHandler> capability, INbtHandler instance, EnumFacing side) {
		return instance.getCapabilityNBT();
	}

	@Override
	public void readNBT(Capability<INbtHandler> capability, INbtHandler instance, EnumFacing side, NBTBase nbt) {
        instance.setCapabilityNBT((NBTTagCompound) nbt);
	}

}
