package noppes.npcs.controllers.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.ForgeEvent.RunNameEvent;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.wrapper.data.StoredData;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.NBTJsonUtil;

public class ClientScriptData
extends BaseScriptData {

	public boolean loadDefault = false;
	
	public ScriptContainer script = null;

	public final StoredData storedData = new StoredData();

	public void clear() {
		for (ScriptContainer scr : this.getScripts()) {
			scr.fullscript = "";
			scr.script = "";
			scr.scripts.clear();
		}
	}
	
	private void createScript() {
		if (this.script == null) {
			this.script = new ScriptContainer(this, true);
		}
	}
	
	@Override
	public List<ScriptContainer> getScripts() {
		if (this.script == null) {
			this.createScript();
		}
		return Lists.<ScriptContainer>newArrayList(this.script);
	}

	@Override
	public boolean isEnabled() {
		return this.enabled && this.script != null;
	}

	@Override
	public String noticeString() {
		return "ClientScript";
	}

	public void readFromNBT(NBTTagCompound compound) {
		if (this.script == null) {
			this.createScript();
		}
		this.script.clear();
		this.script.readFromNBT(compound.getCompoundTag("Scripts"), true);
		this.scriptLanguage = AdditionalMethods.instance.deleteColor(compound.getString("ScriptLanguage"));
		this.enabled = compound.getBoolean("ScriptEnabled");
	}

	@Override
	public void runScript(String type, Event event) {
		if (!this.isEnabled()) {
			return;
		}
		if (ScriptController.Instance.lastLoaded > this.lastInited) {
			this.lastInited = ScriptController.Instance.lastLoaded;
			if (!type.equalsIgnoreCase(EnumScriptType.INIT.function)) {
				EventHooks.onClientInit(this);
			}
		}
		if (this.script == null) {
			this.createScript();
		}
		this.script.run("runEventName", new RunNameEvent(type), false);
		this.script.run(type, event, false);
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if (this.script == null) {
			this.createScript();
		}
		compound.setTag("Scripts", this.script.writeToNBT(new NBTTagCompound()));
		compound.setString("ScriptLanguage", this.scriptLanguage);
		compound.setBoolean("ScriptEnabled", this.enabled);
		return compound;
	}
	
	public void loadDefaultScripts() {
		if (this.loadDefault) { return; }
		File saveDir = new File(CustomNpcs.Dir, "client_default");
		if (!saveDir.exists()) { saveDir.mkdirs(); }
		// Stored Data
		ScriptController.Instance.compound = new NBTTagCompound();
		File sData = new File(saveDir, "world_data.json");
		try {
			if (!sData.exists()) {
				NBTJsonUtil.SaveFile(sData, new NBTTagCompound());
			} else {
				ScriptController.Instance.compound = NBTJsonUtil.LoadFile(sData);
			}
			LogWriter.debug("Load default client stored data - done");
		} catch (Exception e) {
			LogWriter.error("Error Default loading: " + sData.getName(), e);
		}
		this.storedData.resetData(ScriptController.Instance.compound);
		// Modules
		String language = this.getLanguage().toLowerCase();
		saveDir = new File(saveDir, language);
		if (!saveDir.exists()) { saveDir.mkdirs(); }
		ScriptController.Instance.clients.clear();
		ScriptController.Instance.clientSizes.clear();
		ScriptController.Instance.loadDir(saveDir, "", ScriptController.Instance.languages.get(AdditionalMethods.instance.deleteColor(this.getLanguage())), true);
		LogWriter.debug("Load default client modules - "+ScriptController.Instance.clients.size());
		// Main tab
		saveDir = new File(CustomNpcs.Dir, "client_default");
		File file = new File(saveDir, "client_scripts.json");
		try {
			if (!file.exists()) {
				if (this.script == null) {
					this.enabled = true;
					this.script = new ScriptContainer(this, true);
				}
				NBTJsonUtil.SaveFile(file, writeToNBT(new NBTTagCompound()));
				LogWriter.debug("Create default client scripts - done");
			} else {
				NBTTagCompound nbt = NBTJsonUtil.LoadFile(file);
				if (nbt.hasKey("Constants", 10) || nbt.hasKey("Functions", 9)) {
					NBTTagCompound constants = new NBTTagCompound();
					constants.setTag("Constants", nbt.getCompoundTag("Constants"));
					constants.setTag("Functions", nbt.getTagList("Functions", 8));
					ScriptController.Instance.constants = constants;
				}
				ScriptContainer.reloadConstants();
				this.script = new ScriptContainer(this, true);
				this.readFromNBT(nbt);
				LogWriter.debug("Load default client scripts - done: " + nbt.getCompoundTag("Scripts").toString().length() + " size.");
			}
			EventHooks.onEvent(ScriptController.Instance.clientScripts, EnumScriptType.INIT, new PlayerEvent.InitEvent((IPlayer<?>) null));
		} catch (Exception e) {
			LogWriter.error("Error Default loading: " + file.getName(), e);
		}
		this.loadDefault = true;
		return;
	}

	public void saveDefaultScripts() {
		File saveDir = new File(CustomNpcs.Dir, "client_default");
		if (!saveDir.exists()) { saveDir.mkdirs(); }
		// Stored Data
		try {
			NBTJsonUtil.SaveFile(new File(saveDir, "world_data.json"), ScriptController.Instance.compound.copy());
			LogWriter.debug("Save Default Client stored data - done");
		} catch (Exception e) {
			LogWriter.error("Error Default saving: \"world_data.json\"", e);
		}
		// Modules
		if (!ScriptController.Instance.clients.isEmpty()) {
			String language = this.getLanguage().toLowerCase();
			saveDir = new File(saveDir, language);
			if (!saveDir.exists()) { saveDir.mkdirs(); }
			for (String name : ScriptController.Instance.clients.keySet()) {
				try {
					File f = new File(saveDir, name);
					if (!f.getParentFile().exists()) { f.getParentFile().mkdirs(); }
					Files.write(ScriptController.Instance.clients.get(name).getBytes(), new File(saveDir, name));
				} catch (IOException e) {
					LogWriter.error("Error Default saving: " + name, e);
				}
			}
			LogWriter.debug("Save Default Client modules - done");
		}
		// Main tab
		if (this.script == null) {
			this.enabled = true;
			this.script = new ScriptContainer(this, true);
		}
		try {
			NBTTagCompound nbt = writeToNBT(new NBTTagCompound());
			NBTTagCompound constants = new NBTTagCompound();
			NBTTagList functions = new NBTTagList();
			if (!ScriptController.Instance.constants.hasNoTags()) {
				for (String key : ScriptController.Instance.constants.getCompoundTag("Constants").getKeySet()) {
					constants.setTag(key, ScriptController.Instance.constants.getCompoundTag("Constants").getTag(key));
				}
				for (NBTBase tag : ScriptController.Instance.constants.getTagList("Functions", 8)) {
					functions.appendTag(tag);
				}
			}
			nbt.setTag("Constants", constants);
			nbt.setTag("Functions", functions);
			NBTJsonUtil.SaveFile(new File(saveDir, "client_scripts.json"), nbt);
			LogWriter.debug("Save Default Client scripts - done");
		} catch (Exception e) {
			LogWriter.error("Error Default saving: \"client_scripts.json\"", e);
		}
		this.loadDefault = false;
	}
	
}
