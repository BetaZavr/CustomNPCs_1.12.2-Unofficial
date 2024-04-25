package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;

public class PotionScriptData implements IScriptHandler {

	private boolean enabled;
	public boolean hadInteract;
	public long lastInited;
	private String scriptLanguage;
	protected List<ScriptContainer> scripts;

	public PotionScriptData() {
		this.scripts = new ArrayList<ScriptContainer>();
		this.scriptLanguage = "ECMAScript";
		this.lastInited = -1L;
		this.hadInteract = true;
		this.enabled = false;
	}

	public void clear() {
		this.scripts = new ArrayList<ScriptContainer>();
	}

	@Override
	public void clearConsole() {
		for (ScriptContainer script : this.getScripts()) {
			script.console.clear();
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
		return this.scripts;
	}

	@Override
	public boolean isClient() {
		return Thread.currentThread().getName().toLowerCase().indexOf("client") != -1;
	}

	public boolean isEnabled() {
		return this.enabled && ScriptController.HasStart && this.scripts.size() > 0;
	}

	@Override
	public String noticeString() {
		return "PotionScript";
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.scripts.clear();
		this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this, false);
		this.scriptLanguage = compound.getString("ScriptLanguage");
		this.enabled = compound.getBoolean("ScriptEnabled");
		if (this.scripts.isEmpty() || this.scripts.get(0).script.isEmpty()) {
			ScriptContainer script = new ScriptContainer(this, false);
			char chr = Character.toChars(0x000A)[0];
			script.script = "// IPotion.getCustomName() - String (custom potion name)" + chr
					+ "// IPotion.getNbt() - INbt (nbt data)" + chr + "function isReady(event) {" + chr
					+ "  /* event.potion - IPotion" + chr + "     event.duration - int (tiks)" + chr
					+ "     event.amplifier - int (potion power) */" + chr + "}" + chr
					+ "function performEffect(event) {" + chr + "  /* event.potion - IPotion" + chr
					+ "     event.entity - IEntity" + chr + "     event.amplifier - int (potion power) */" + chr + "}"
					+ chr + "function affectEntity(event) {" + chr + "  /* event.potion - IPotion" + chr
					+ "     event.entity - IEntity" + chr + "     event.source - IEntity" + chr
					+ "     event.indirectSource - IEntity" + chr + "     event.amplifier - int (potion power)" + chr
					+ "     event.health - double (health value) */" + chr + "}" + chr + "function endEffect(event) {"
					+ chr + "  /* event.potion - IPotion" + chr + "     event.entity - IEntity" + chr
					+ "     event.amplifier - int (potion power) */" + chr + "}";
			if (this.scripts.isEmpty()) {
				this.scripts.add(script);
			} else {
				this.scripts.remove(0);
				this.scripts.add(0, script);
			}
		}
	}

	@Override
	public void runScript(String type, Event event) {
		if (!this.isEnabled()) {
			return;
		}
		try {
			if (ScriptController.Instance.lastLoaded > this.lastInited) {
				this.lastInited = ScriptController.Instance.lastLoaded;
				if (!type.equalsIgnoreCase(EnumScriptType.INIT.function)) {
					EventHooks.onPotoinInit(this);
				}
			}
			Iterator<ScriptContainer> iterator = this.scripts.iterator();
			boolean bo = !this.isClient();
			while (iterator.hasNext()) {
				iterator.next().run(type, event, bo);
			}
		} catch (Exception e) {
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

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
		compound.setString("ScriptLanguage", this.scriptLanguage);
		compound.setBoolean("ScriptEnabled", this.enabled);
		return compound;
	}

}