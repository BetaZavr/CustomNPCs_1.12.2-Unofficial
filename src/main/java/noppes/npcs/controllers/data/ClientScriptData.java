package noppes.npcs.controllers.data;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;

public class ClientScriptData
implements IScriptHandler {
	
	private boolean enabled;
	public boolean hadInteract;
	public long lastInited;
	private String scriptLanguage;
	protected ScriptContainer script;

	public ClientScriptData() {
		this.script = null;
		this.scriptLanguage = "ECMAScript";
		this.lastInited = -1L;
		this.hadInteract = true;
		this.enabled = false;
	}

	public void clear() {
		for (ScriptContainer scr : this.getScripts()) {
			scr.fullscript = "";
			scr.script = "";
			scr.scripts.clear();
		}
	}

	@Override
	public void clearConsole() {
		for (ScriptContainer scr : this.getScripts()) {
			scr.console.clear();
		}
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
	public boolean getEnabled() {
		return this.enabled;
	}

	@Override
	public String getLanguage() {
		return this.scriptLanguage;
	}

	@Override
	public List<ScriptContainer> getScripts() {
		if (this.script==null) { this.createScript(); }
		return Lists.<ScriptContainer>newArrayList(this.script);
	}

	private void createScript() {
		if (this.script==null) {
			this.script = new ScriptContainer(this);
		}
	}

	@Override
	public boolean isClient() {
		return CustomNpcs.Server == null || (CustomNpcs.Server != null && !CustomNpcs.Server.isDedicatedServer()) || (CustomNpcs.proxy.getPlayer()!=null && !CustomNpcs.proxy.getPlayer().isServerWorld());
	}

	public boolean isEnabled() {
		return this.enabled && ScriptController.HasStart && this.script != null;
	}

	@Override
	public String noticeString() {
		return "ClientForgeScript";
	}

	public void readFromNBT(NBTTagCompound compound) {
		if (this.script==null) { this.createScript(); }
		this.script.readFromNBT(compound.getCompoundTag("Scripts"));
		this.scriptLanguage = compound.getString("ScriptLanguage");
		this.enabled = compound.getBoolean("ScriptEnabled");
	}

	@Override
	public void runScript(EnumScriptType type, Event event) {
		this.runScript(type.function, event);
	}

	public void runScript(String type, Event event) {
		if (!this.isEnabled()) {
			return;
		}
		CustomNpcs.Server.addScheduledTask(() -> {
			if (ScriptController.Instance.lastLoaded > this.lastInited) {
				this.lastInited = ScriptController.Instance.lastLoaded;
			}
			if (this.script==null) { this.createScript(); }
			this.script.run(type, event);
		});
	}

	@Override
	public void setEnabled(boolean bo) {
		this.enabled = bo;
	}

	@Override
	public void setLanguage(String lang) {
		this.scriptLanguage = lang;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if (this.script==null) { this.createScript(); }
		compound.setTag("Scripts", this.script.writeToNBT(new NBTTagCompound()));
		compound.setString("ScriptLanguage", this.scriptLanguage);
		compound.setBoolean("ScriptEnabled", this.enabled);
		return compound;
	}
	
}
