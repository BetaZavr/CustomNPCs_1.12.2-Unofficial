package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;

public class PotionScriptData
extends ForgeScriptData {

	private static Map<Long, String> console = new TreeMap<Long, String>();
	private static List<Integer> errored = new ArrayList<Integer>();
	
	public PotionScriptData() {
		super();
	}

	public void clear() {
		PotionScriptData.console = new TreeMap<Long, String>();
		PotionScriptData.errored = new ArrayList<Integer>();
		this.scripts = new ArrayList<ScriptContainer>();
	}
	
	@Override
	public String noticeString() {
		return "PotionScript";
	}

	@Override
	public void runScript(String type, Event event) {
		if (!this.isEnabled()) { return; }
		if (ScriptController.Instance.lastLoaded > this.lastInited) {
			this.lastInited = ScriptController.Instance.lastLoaded;
			if (type.equalsIgnoreCase(EnumScriptType.INIT.function)) { EventHooks.onPotoinInit(this); }
		}
		for (int i = 0; i < this.scripts.size(); ++i) {
			ScriptContainer script = this.scripts.get(i);
			if (!PotionScriptData.errored.contains(i)) {
				script.run(type, event, !this.isClient());
				if (script.errored) {
					PotionScriptData.errored.add(i);
				}
				for (Map.Entry<Long, String> entry : script.console.entrySet()) {
					if (!PotionScriptData.console.containsKey(entry.getKey())) {
						PotionScriptData.console.put(entry.getKey(), " tab " + (i + 1) + ":\n" + entry.getValue());
					}
				}
				script.console.clear();
			}
		}
	}

	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		PotionScriptData.console = NBTTags.GetLongStringMap(compound.getTagList("ScriptConsole", 10));
		if (this.scripts.isEmpty() || this.scripts.get(0).script.isEmpty()) {
			ScriptContainer script = new ScriptContainer(this, false);
			char chr = Character.toChars(0x000A)[0];
			script.script = "// IPotion.getCustomName() - String (custom potion name)" + chr +
					"// IPotion.getNbt() - INbt (nbt data)" + chr +
					"function isReady(event) {" + chr +
					"  /* event.potion - IPotion" + chr +
					"     event.duration - int (tiks)" + chr +
					"     event.amplifier - int (potion power) */" + chr + "}" + chr +
					"function performEffect(event) {" + chr +
					"  /* event.potion - IPotion" + chr +
					"     event.entity - IEntity" + chr +
					"     event.amplifier - int (potion power) */" + chr + "}" + chr +
					"function affectEntity(event) {" + chr +
					"  /* event.potion - IPotion" + chr +
					"     event.entity - IEntity" + chr +
					"     event.source - IEntity" + chr +
					"     event.indirectSource - IEntity" + chr +
					"     event.amplifier - int (potion power)" + chr +
					"     event.health - double (health value) */" + chr + "}" + chr +
					"function endEffect(event) {" + chr +
					"  /* event.potion - IPotion" + chr +
					"     event.entity - IEntity" + chr +
					"     event.amplifier - int (potion power) */" + chr + "}";
			if (this.scripts.isEmpty()) { this.scripts.add(script); }
			else {
				this.scripts.remove(0);
				this.scripts.add(0, script);
			}
		}
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setTag("ScriptConsole", NBTTags.NBTLongStringMap(PotionScriptData.console));
		return compound;
	}
	
}