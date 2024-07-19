package noppes.npcs.controllers.data;

import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
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
                for (ScriptContainer script : this.scripts) {
                    script.run(type, event, !this.isClient());
                }
			});
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}
}
