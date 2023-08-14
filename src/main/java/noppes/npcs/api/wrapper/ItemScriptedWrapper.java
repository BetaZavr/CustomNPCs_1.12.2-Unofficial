package noppes.npcs.api.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.item.IItemScripted;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.SyncController;
import noppes.npcs.items.ItemScripted;

public class ItemScriptedWrapper
extends ItemStackWrapper
implements IItemScripted, IScriptHandler {
	
	public int durabilityColor;
	public boolean durabilityShow;
	public double durabilityValue;
	public boolean enabled;
	public int itemColor;
	public long lastInited;
	public boolean loaded;
	public String scriptLanguage;
	public List<ScriptContainer> scripts;
	public int stackSize;
	public boolean updateClient;

	public ItemScriptedWrapper(ItemStack item) {
		super(item);
		this.scripts = new ArrayList<ScriptContainer>();
		this.scriptLanguage = "ECMAScript";
		this.enabled = false;
		this.lastInited = -1L;
		this.updateClient = false;
		this.durabilityShow = true;
		this.durabilityValue = 1.0;
		this.durabilityColor = -1;
		this.itemColor = -1;
		this.stackSize = 64;
		this.loaded = false;
	}

	@Override
	public void clearConsole() {
		for (ScriptContainer script : this.getScripts()) {
			script.console.clear();
		}
	}

	@Override
	public int getColor() {
		return this.itemColor;
	}

	@Override
	public Map<Long, String> getConsoleText() {
		Map<Long, String> map = new TreeMap<Long, String>();
		int tab = 0;
		for (ScriptContainer script : this.getScripts()) {
			++tab;
			for (Map.Entry<Long, String> entry : script.console.entrySet()) {
				map.put(entry.getKey(), " tab " + tab + ":\n" + entry.getValue());
			}
		}
		return map;
	}

	@Override
	public int getDurabilityColor() {
		return this.durabilityColor;
	}

	@Override
	public boolean getDurabilityShow() {
		return this.durabilityShow;
	}

	@Override
	public double getDurabilityValue() {
		return this.durabilityValue;
	}

	@Override
	public boolean getEnabled() {
		return this.enabled;
	}

	@Override
	public String getLanguage() {
		return this.scriptLanguage;
	}

	@Override
	public int getMaxStackSize() {
		return this.stackSize;
	}

	@Override
	public NBTTagCompound getMCNbt() {
		NBTTagCompound compound = super.getMCNbt();
		this.getScriptNBT(compound);
		compound.setBoolean("DurabilityShow", this.durabilityShow);
		compound.setDouble("DurabilityValue", this.durabilityValue);
		compound.setInteger("DurabilityColor", this.durabilityColor);
		compound.setInteger("ItemColor", this.itemColor);
		compound.setInteger("MaxStackSize", this.stackSize);
		return compound;
	}

	public NBTTagCompound getScriptNBT(NBTTagCompound compound) {
		compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
		compound.setString("ScriptLanguage", this.scriptLanguage);
		compound.setBoolean("ScriptEnabled", this.enabled);
		return compound;
	}

	@Override
	public List<ScriptContainer> getScripts() {
		return this.scripts;
	}

	@Override
	public String getTexture(int damage) {
		return ItemScripted.Resources.get(damage);
	}

	@Override
	public int getType() {
		return 6;
	}

	@Override
	public boolean hasTexture(int damage) {
		return ItemScripted.Resources.containsKey(damage);
	}

	@Override
	public boolean isClient() {
		return false;
	}

	private boolean isEnabled() {
		return this.enabled && ScriptController.HasStart;
	}

	public void loadScriptData() {
		NBTTagCompound c = this.item.getTagCompound();
		if (c == null) {
			return;
		}
		this.setScriptNBT(c.getCompoundTag("ScriptedData"));
	}

	@Override
	public String noticeString() {
		return "ScriptedItem";
	}

	@Override
	public void runScript(EnumScriptType type, Event event) {
		if (!this.loaded) {
			this.loadScriptData();
			this.loaded = true;
		}
		if (!this.isEnabled()) {
			return;
		}
		if (ScriptController.Instance.lastLoaded > this.lastInited) {
			this.lastInited = ScriptController.Instance.lastLoaded;
			if (type != EnumScriptType.INIT) {
				EventHooks.onScriptItemInit(this);
			}
		}
		for (ScriptContainer script : this.scripts) {
			script.run(type, event, !this.isClient());
		}
	}

	public void saveScriptData() {
		NBTTagCompound c = this.item.getTagCompound();
		if (c == null) {
			this.item.setTagCompound(c = new NBTTagCompound());
		}
		c.setTag("ScriptedData", this.getScriptNBT(new NBTTagCompound()));
	}

	@Override
	public void setColor(int color) {
		if (color != this.itemColor) {
			this.updateClient = true;
		}
		this.itemColor = color;
	}

	@Override
	public void setDurabilityColor(int color) {
		if (color != this.durabilityColor) {
			this.updateClient = true;
		}
		this.durabilityColor = color;
	}

	@Override
	public void setDurabilityShow(boolean bo) {
		if (bo != this.durabilityShow) {
			this.updateClient = true;
		}
		this.durabilityShow = bo;
	}

	@Override
	public void setDurabilityValue(float value) {
		if (value != this.durabilityValue) {
			this.updateClient = true;
		}
		this.durabilityValue = value;
	}

	@Override
	public void setEnabled(boolean bo) {
		this.enabled = bo;
	}

	@Override
	public void setLanguage(String lang) {
		this.scriptLanguage = lang;
	}

	@Override
	public void setMaxStackSize(int size) {
		if (size < 1 || size > 64) {
			throw new CustomNPCsException("Stacksize has to be between 1 and 64", new Object[0]);
		}
		this.stackSize = size;
	}

	@Override
	public void setMCNbt(NBTTagCompound compound) {
		super.setMCNbt(compound);
		this.setScriptNBT(compound);
		this.durabilityShow = compound.getBoolean("DurabilityShow");
		this.durabilityValue = compound.getDouble("DurabilityValue");
		if (compound.hasKey("DurabilityColor")) {
			this.durabilityColor = compound.getInteger("DurabilityColor");
		}
		this.itemColor = compound.getInteger("ItemColor");
		this.stackSize = compound.getInteger("MaxStackSize");
	}

	public void setScriptNBT(NBTTagCompound compound) {
		if (!compound.hasKey("Scripts")) {
			return;
		}
		this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this, false);
		this.scriptLanguage = compound.getString("ScriptLanguage");
		this.enabled = compound.getBoolean("ScriptEnabled");
	}

	@Override
	public void setTexture(int damage, String texture) {
		if (damage == 0) {
			throw new CustomNPCsException("Can't set texture for 0", new Object[0]);
		}
		String old = ItemScripted.Resources.get(damage);
		if (old == texture || (old != null && texture != null && old.equals(texture))) {
			return;
		}
		ItemScripted.Resources.put(damage, texture);
		SyncController.syncScriptItemsEverybody();
	}
}
