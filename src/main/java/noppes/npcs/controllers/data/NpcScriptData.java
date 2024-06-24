package noppes.npcs.controllers.data;

import java.util.Iterator;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;

public class NpcScriptData
extends BaseScriptData {
	
	@Override
	public String noticeString() {
		return "NPCScript";
	}
	
	@Override
	public void runScript(String type, Event event) {
		if (!this.isEnabled()) {
			return;
		}
		try {
			CustomNpcs.Server.addScheduledTask(() -> {
				if (ScriptController.Instance.lastLoaded > this.lastInited) {
					this.lastInited = ScriptController.Instance.lastLoaded;
					if (!type.equalsIgnoreCase(EnumScriptType.INIT.function)) {
						EventHooks.onNPCsInit(this);
					}
				}
				Iterator<ScriptContainer> iterator = this.scripts.iterator();
				while (iterator.hasNext()) {
					((ScriptContainer) iterator.next()).run(type, event, !this.isClient());
				}
			});
		} catch (Exception e) {
		}
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
		compound.setString("ScriptLanguage", this.scriptLanguage);
		compound.setBoolean("ScriptEnabled", this.enabled);
		return compound;
	}
	
}
