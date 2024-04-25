package noppes.npcs.api.wrapper.data;

import java.util.Set;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.wrapper.BlockWrapper;
import noppes.npcs.api.wrapper.EntityWrapper;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.api.wrapper.WorldWrapper;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.util.AdditionalMethods;

public class StoredData implements IData {

	private NBTTagCompound data;
	private BlockWrapper block;
	private EntityWrapper<?> entity;
	private ItemStackWrapper stack;
	private ScriptController controller;

	public StoredData() {
		this.data = new NBTTagCompound();
		this.controller = ScriptController.Instance;
	}

	public StoredData(BlockWrapper wrapper) {
		this();
		this.block = wrapper;
	}

	public StoredData(EntityWrapper<?> wrapper) {
		this();
		this.entity = wrapper;
	}

	public StoredData(ItemStackWrapper stack) {
		this();
		stack.storedData = new NBTTagCompound();
		this.data = stack.storedData;
		this.stack = stack;
	}

	public StoredData(WorldWrapper world) {
		this();
	}

	@Override
	public void clear() {
		this.resetData();
		for (String key : this.data.getKeySet().toArray(new String[this.data.getKeySet().size()])) {
			this.data.removeTag(key);
		}
		if (this.controller != null) {
			this.controller.shouldSave = true;
		}
	}

	@Override
	public Object get(String key) {
		this.resetData();
		if (!this.data.hasKey(key)) {
			return null;
		}
		return AdditionalMethods.instance.readObjectFromNbt(this.data.getTag(key));
	}

	@Override
	public String[] getKeys() {
		this.resetData();
		Set<String> sets = this.data.getKeySet();
		return sets.toArray(new String[sets.size()]);
	}

	@Override
	public INbt getNbt() {
		this.resetData();
		return NpcAPI.Instance().getINbt(this.data);
	}

	@Override
	public boolean has(String key) {
		this.resetData();
		return this.data.hasKey(key);
	}

	@Override
	public void put(String key, Object value) throws CommandException {
		this.resetData();
		NBTBase nbt = AdditionalMethods.instance.writeObjectToNbt(value);
		if (nbt != null) {
			this.data.setTag(key, nbt);
			if (this.controller != null) {
				this.controller.shouldSave = true;
			}
			return;
		}
		throw new CommandException(
				"Unsupported data type to put in StoredData. Key: \"" + key + "\"; Value: " + value.toString(),
				new Object[0]);
	}

	@Override
	public boolean remove(String key) {
		this.resetData();
		if (!this.data.hasKey(key)) {
			return false;
		}
		this.data.removeTag(key);
		if (this.controller != null) {
			this.controller.shouldSave = true;
		}
		return true;
	}

	private void resetData() {
		if (this.block != null) {
			if (this.block.tile == null) {
				return;
			}
			if (!this.block.tile.getTileData().hasKey("CustomNPCsData", 10)) {
				this.block.tile.getTileData().setTag("CustomNPCsData", new NBTTagCompound());
			}
			this.data = this.block.tile.getTileData().getCompoundTag("CustomNPCsData");
			return;
		} else if (this.entity != null) {
			if (this.entity.getMCEntity() instanceof EntityPlayer) {
				this.data = PlayerData.get((EntityPlayer) this.entity.getMCEntity()).scriptStoreddata;
			} else {
				if (!this.entity.getMCEntity().getEntityData().hasKey("CNPCStoredData", 10)) {
					this.entity.getMCEntity().getEntityData().setTag("CNPCStoredData", new NBTTagCompound());
				}
				this.data = this.entity.getMCEntity().getEntityData().getCompoundTag("CNPCStoredData");
			}
		} else if (this.stack != null) {
			this.data = this.stack.storedData;
		} else if (this.controller != null) {
			this.data = this.controller.compound;
		}
	}

	@Override
	public void setNbt(INbt nbt) {
		NBTTagCompound compound = nbt.getMCNBT().copy();
		this.clear();
		for (String key : compound.getKeySet()) {
			this.data.setTag(key, compound.getTag(key));
		}
		if (this.controller != null) {
			this.controller.shouldSave = true;
		}
	}

}