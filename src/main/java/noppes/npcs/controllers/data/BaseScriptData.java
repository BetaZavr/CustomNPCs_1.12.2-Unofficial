package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.NBTTags;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.util.AdditionalMethods;

public class BaseScriptData
implements IScriptHandler {

	protected boolean enabled = false;
	public boolean hadInteract = true;
	public long lastInited = -1L;
	protected String scriptLanguage = "ECMAScript";
	protected List<ScriptContainer> scripts = Lists.newArrayList();

	public void clear() {
		this.scripts = new ArrayList<>();
	}

	@Override
	public void clearConsole() {
		for (ScriptContainer script : this.getScripts()) {
			script.console.clear();
		}
	}

	@Override
	public Map<Long, String> getConsoleText() {
		Map<Long, String> map = new TreeMap<>();
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
	public String getLanguage() {
		return this.scriptLanguage;
	}

	@Override
	public List<ScriptContainer> getScripts() {
		return this.scripts;
	}

	@Override
	public boolean isClient() {
		return Thread.currentThread().getName().toLowerCase().contains("client");
	}

	@Override
	public boolean getEnabled() { return this.enabled; }

	public boolean isEnabled() {
		return this.enabled && ScriptController.HasStart && !this.scripts.isEmpty();
	}

	@Override
	public String noticeString() { return ""; }

	public void readFromNBT(NBTTagCompound compound) {
		this.scripts.clear();
		this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this, false);
		this.scriptLanguage = AdditionalMethods.instance.deleteColor(compound.getString("ScriptLanguage"));
		this.enabled = compound.getBoolean("ScriptEnabled");
	}

	@Override
	public void runScript(String type, Event event) { }

	@Override
	public void setEnabled(boolean bo) {
		this.enabled = bo;
	}

	@Override
	public void setLanguage(String lang) {
		this.scriptLanguage = AdditionalMethods.instance.deleteColor(lang);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
		compound.setString("ScriptLanguage", this.scriptLanguage);
		compound.setBoolean("ScriptEnabled", this.enabled);
		return compound;
	}
	
}
