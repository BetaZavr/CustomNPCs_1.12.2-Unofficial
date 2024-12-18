package noppes.npcs.entity.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.MoreObjects;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;

public class DataScript implements IScriptHandler {

	private boolean enabled = false;
	public long lastInited = -1L;
	public EntityNPCInterface npc;
	private String scriptLanguage = "ECMAScript";
	private List<ScriptContainer> scripts = new ArrayList<>();

	public DataScript(EntityNPCInterface npc) {
		this.npc = npc;
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
	public boolean getEnabled() {
		return this.enabled;
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

	public boolean isEnabled() {
		return this.enabled && ScriptController.HasStart && !this.scripts.isEmpty();
	}

	@Override
	public String noticeString() {
		BlockPos pos = this.npc.getPosition();
		return MoreObjects.toStringHelper(this.npc).add("name", this.npc.getName())
				.add("dimID", this.npc.world.provider.getDimension()).add("x", pos.getX()).add("y", pos.getY())
				.add("z", pos.getZ()).toString();
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this, false);
		this.scriptLanguage = compound.getString("ScriptLanguage");
		this.enabled = compound.getBoolean("ScriptEnabled");
	}

	@Override
	public void runScript(String type, Event event) {
		if (!this.isEnabled()) {
			return;
		}
		if (ScriptController.Instance.lastLoaded > lastInited) {
			lastInited = ScriptController.Instance.lastLoaded;
			if (!type.equalsIgnoreCase(EnumScriptType.INIT.function)) {
				EventHooks.onNPCInit(this.npc);
			}
		}
		for (ScriptContainer script : this.scripts) {
			script.run(type, event, !this.isClient());
		}
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
	public void setLastInited(long timeMC) {
		this.lastInited = timeMC;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
		compound.setString("ScriptLanguage", this.scriptLanguage);
		compound.setBoolean("ScriptEnabled", this.enabled);
		return compound;
	}
}
