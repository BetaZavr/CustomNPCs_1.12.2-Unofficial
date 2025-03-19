package noppes.npcs.api.wrapper.data;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.wrapper.BlockWrapper;
import noppes.npcs.api.wrapper.EntityWrapper;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.util.Util;

public class StoredData extends TempData implements IData {

	private EntityWrapper<?> entity;
	private ScriptController controller;

	public StoredData() { super(); }

	public StoredData(BlockWrapper wrapper) {
		super(wrapper);
	}

	public StoredData(ScriptController controllerIn) {
		controller = controllerIn;
	}

	public StoredData(EntityWrapper<?> wrapper) {
		super();
		entity = wrapper;
	}

	@Override
	public void clear() {
		super.clear();
		if (entity != null) {
			if (entity.getMCEntity() instanceof EntityPlayer) {
				PlayerData data = PlayerData.get((EntityPlayer) entity.getMCEntity());
				for (String key : data.scriptStoreddata.getKeySet()) { data.scriptStoreddata.removeTag(key); }
			}
			else { entity.getMCEntity().getEntityData().setTag("CNPCStoredData", new NBTTagCompound()); }
		}
		else if (controller != null) {
			controller.compound = new NBTTagCompound();
			controller.shouldSave = true;
		}
	}

	@Override
	public void put(String key, Object value) {
		if (value == null) {
			remove(key);
			return;
		}
		NBTBase tag = Util.instance.writeObjectToNbt(value);
		if (tag == null || Util.instance.readObjectFromNbt(tag) == null) { throw new NullPointerException("Unsupported data type to put in StoredData. Key: \"" + key + "\"; Value: " + value); }
		map.put(key, value);
		if (block != null && block.storage != null) { block.storage.tempData.put(key, value); }
		else if (entity != null) {
			if (entity.getMCEntity() instanceof EntityPlayer) { PlayerData.get((EntityPlayer) entity.getMCEntity()).scriptStoreddata.setTag(key, tag); }
			else {
				if (!entity.getMCEntity().getEntityData().hasKey("CNPCStoredData", 10)) { entity.getMCEntity().getEntityData().setTag("CNPCStoredData", new NBTTagCompound()); }
				entity.getMCEntity().getEntityData().getCompoundTag("CNPCStoredData").setTag(key, tag);
			}
		}
		else if (controller != null) {
			if (controller.compound == null) { controller.compound = new NBTTagCompound(); }
			controller.compound.setTag(key, tag);
		}
	}

	@Override
	public void remove(String key) {
		super.remove(key);
		if (entity != null) {
			if (entity.getMCEntity() instanceof EntityPlayer ) { PlayerData.get((EntityPlayer) entity.getMCEntity()).scriptStoreddata.removeTag(key); }
			else {
				if (!entity.getMCEntity().getEntityData().hasKey("CNPCStoredData", 10)) { entity.getMCEntity().getEntityData().setTag("CNPCStoredData", new NBTTagCompound()); }
				entity.getMCEntity().getEntityData().getCompoundTag("CNPCStoredData").removeTag(key);
			}
		}
		else if (controller != null && controller.compound != null) {
			controller.compound.removeTag(key);
		}
	}

	@Override
	public void setNbt(NBTTagCompound compound) {
		super.setNbt(compound);
		NBTTagCompound nbt = getNbt().getMCNBT();
		if (entity != null) {
			if (entity.getMCEntity() instanceof EntityPlayer) {
				PlayerData data = PlayerData.get((EntityPlayer) entity.getMCEntity());
				for (String key : data.scriptStoreddata.getKeySet()) { data.scriptStoreddata.removeTag(key); }
				for (String key : nbt.getKeySet()) { data.scriptStoreddata.setTag(key, Objects.requireNonNull(nbt.getTag(key))); }
			}
			else {
				if (!entity.getMCEntity().getEntityData().hasKey("CNPCStoredData", 10)) { entity.getMCEntity().getEntityData().setTag("CNPCStoredData", new NBTTagCompound()); }
				for (String key : nbt.getKeySet()) { entity.getMCEntity().getEntityData().getCompoundTag("CNPCStoredData").setTag(key, Objects.requireNonNull(nbt.getTag(key))); }
			}
		} else if (controller != null && controller.compound != null) {
			for (String key : nbt.getKeySet()) { controller.compound.setTag(key, Objects.requireNonNull(nbt.getTag(key))); }
		}
	}

}