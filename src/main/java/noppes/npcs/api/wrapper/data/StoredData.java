package noppes.npcs.api.wrapper.data;

import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.FakePlayer;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.wrapper.BlockWrapper;
import noppes.npcs.api.wrapper.EntityWrapper;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.Util;

public class StoredData extends TempData implements IData {

	private EntityWrapper<?> entity;
	private ScriptController controller;

	public StoredData() { super(); }

	public StoredData(BlockWrapper wrapper) {
		super(wrapper);
		if (wrapper != null && wrapper.storage != null) { setNbt(wrapper.storage.getTileData()); }
	}

	public StoredData(ScriptController controllerIn) {
		controller = controllerIn;
		setNbt(controller.compound);
	}

	public StoredData(EntityWrapper<?> wrapper) {
		super();
		entity = wrapper;
		if (entity.getMCEntity() instanceof EntityPlayer && !(entity.getMCEntity() instanceof FakePlayer)) {
			final PlayerData[] data = {PlayerData.get((EntityPlayer) entity.getMCEntity())};
			if (data[0] == null) {
				CustomNPCsScheduler.runTack(() -> {
					while (data[0] == null) {
						data[0] = PlayerData.get((EntityPlayer) entity.getMCEntity());
						if (data[0] != null) {
							setNbt(data[0].scriptStoreddata);
						}
					}
				});
			}
		}
		else {
			if (!entity.getMCEntity().getEntityData().hasKey("CNPCStoredData", 10)) {
				entity.getMCEntity().getEntityData().setTag("CNPCStoredData", new NBTTagCompound());
			}
			setNbt(entity.getMCEntity().getEntityData().getCompoundTag("CNPCStoredData"));
		}
	}

	@Override
	public void clear() {
		map.clear();
		NBTTagCompound nbt = getNbt().getMCNBT();
		if (block != null && block.storage != null) { block.storage.set(nbt); }
		if (entity != null) {
			if (entity.getMCEntity() instanceof EntityPlayer) {
				PlayerData data = PlayerData.get((EntityPlayer) entity.getMCEntity());
				for (String key : new ArrayList<>(data.scriptStoreddata.getKeySet())) { data.scriptStoreddata.removeTag(key); }
				for (String key : nbt.getKeySet()) { data.scriptStoreddata.setTag(key, Objects.requireNonNull(nbt.getTag(key))); }
			}
			else { entity.getMCEntity().getEntityData().setTag("CNPCStoredData", nbt); }
		}
		else if (controller != null) {
			controller.compound = nbt;
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
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("K", key);
		nbt.setTag("V", tag);

		if (block != null && block.storage != null) { block.storage.put(key, nbt); }
		else if (entity != null) {
			if (entity.getMCEntity() instanceof EntityPlayer) {
				NBTTagCompound st = PlayerData.get((EntityPlayer) entity.getMCEntity()).scriptStoreddata;
				NBTTagList tagList;
				if (!st.hasKey("Content", 9)) {
					st.setTag("Content", tagList = new NBTTagList());
					st.setInteger("IsMap", 1);
				}
				else { tagList = st.getTagList("Content", 10); }
				for (int i = 0; i < tagList.tagCount(); i++) {
					NBTTagCompound listNbt = tagList.getCompoundTagAt(i);
					if (listNbt.getString("K").equals(key)) {
						tagList.removeTag(i);
						break;
					}
				}
				tagList.appendTag(nbt);
			}
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
		map.remove(key);
		if (block != null && block.storage != null) { block.storage.remove(key); }
		if (entity != null) {
			if (entity.getMCEntity() instanceof EntityPlayer) {
				PlayerData.get((EntityPlayer) entity.getMCEntity()).scriptStoreddata.removeTag(key);
			}
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
	@SuppressWarnings("unchecked")
	public void setNbt(NBTTagCompound compound) {
		Object obj = Util.instance.readObjectFromNbt(compound);
		if (obj instanceof TreeMap) {
			try {
				map.clear();
				map.putAll((TreeMap<String, Object>) obj);
			}
			catch (Exception ignored) { }
		}
		NBTTagCompound nbt = getNbt().getMCNBT();
		if (block != null && block.storage != null) { block.storage.set(nbt); }
		if (entity != null) {
			if (entity.getMCEntity() instanceof EntityPlayer) {
				PlayerData data = PlayerData.get((EntityPlayer) entity.getMCEntity());
				if (data != null) {
					for (String key : data.scriptStoreddata.getKeySet()) { data.scriptStoreddata.removeTag(key); }
					for (String key : nbt.getKeySet()) { data.scriptStoreddata.setTag(key, Objects.requireNonNull(nbt.getTag(key))); }
				}
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