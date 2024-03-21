package noppes.npcs.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import noppes.npcs.api.handler.capability.IPlayerDataHandler;

public class PlayerDataStorage
implements Capability.IStorage<IPlayerDataHandler> {

	@Override
	public NBTBase writeNBT(Capability<IPlayerDataHandler> capability, IPlayerDataHandler instance, EnumFacing side) {
		return instance.getNBT();
	}

	@Override
	public void readNBT(Capability<IPlayerDataHandler> capability, IPlayerDataHandler instance, EnumFacing side, NBTBase nbt) {
        instance.setNBT((NBTTagCompound) nbt);
	}

}
