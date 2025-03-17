package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.NBTTags;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.util.Util;

public class BaseScriptData
implements IScriptHandler {

	private final boolean isClient = Thread.currentThread().getName().toLowerCase().contains("client");
	protected boolean enabled = false;
	public boolean hadInteract = true;
	public long lastInited = -1L;
	protected String scriptLanguage = "ECMAScript";
	protected List<ScriptContainer> scripts = new ArrayList<>();

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
	public TreeMap<Long, String> getConsoleText() {
		TreeMap<Long, String> map = new TreeMap<>();
		int tab = 0;
		for (ScriptContainer script : this.getScripts()) {
			++tab;
			for (Map.Entry<Long, String> entry : script.console.entrySet()) {
				String log;
				if (map.containsKey(entry.getKey())) { log = map.get(entry.getKey()) + "\n\n" + "ScriptTab " + tab + ":\n" + entry.getValue(); }
				else { log = " ScriptTab " + tab + ":\n" + entry.getValue(); }
				map.put(entry.getKey(), log);
			}
		}
		return map;
	}

	@Override
	public void clearConsoleText(Long key) {
		for (ScriptContainer script : this.getScripts()) {
			script.console.remove(key);
		}
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
	public boolean isClient() { return this.isClient; }

	@Override
	public boolean getEnabled() { return this.enabled; }

	public boolean isEnabled() {
		return this.enabled && ScriptController.HasStart && !this.scripts.isEmpty();
	}

	@Override
	public String noticeString() { return ""; }

	@Override
	public void runScript(String type, Event event) { }

	public void readFromNBT(NBTTagCompound compound) {
		this.scripts.clear();
		this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this, false);
		this.scriptLanguage = Util.instance.deleteColor(compound.getString("ScriptLanguage"));
		this.enabled = compound.getBoolean("ScriptEnabled");
	}

	@Override
	public void setEnabled(boolean bo) {
		this.enabled = bo;
	}

	@Override
	public void setLanguage(String lang) {
		this.scriptLanguage = Util.instance.deleteColor(lang);
	}

	@Override
	public void setLastInited(long timeMC) {
		this.lastInited = timeMC;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
		compound.setString("ScriptLanguage", this.scriptLanguage);
		compound.setBoolean("ScriptEnabled", this.enabled);
		return compound;
	}

	public boolean isEmpty() {
		for (ScriptContainer cont : this.scripts) {
			if (cont.hasScriptCode()) { return false; }
		}
		return true;
	}

}
