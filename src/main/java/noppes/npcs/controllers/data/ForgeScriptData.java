package noppes.npcs.controllers.data;

import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;

public class ForgeScriptData
extends BaseScriptData {

	@Override
	public String noticeString() {
		return "ForgeScript";
	}
	
	@Override
	public void runScript(String type, Event event) {
		super.runScript(type, event);
		if (!this.isEnabled()) { return; }
		try {
			CustomNpcs.Server.addScheduledTask(() -> {
				if (ScriptController.Instance.lastLoaded > this.lastInited) {
					this.lastInited = ScriptController.Instance.lastLoaded;
					if (!type.equalsIgnoreCase(EnumScriptType.INIT.function)) {
						EventHooks.onForgeInit(this);
					}
				}
                for (ScriptContainer script : this.scripts) {
                    script.run(type, event, !this.isClient());
                }
			});
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}
	
}
