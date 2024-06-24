package noppes.npcs.controllers.data;

import java.util.Iterator;

import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.api.event.ForgeEvent.RunNameEvent;
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
		if (!this.isEnabled()) { return; }
		try {
			CustomNpcs.Server.addScheduledTask(() -> {
				if (ScriptController.Instance.lastLoaded > this.lastInited) {
					this.lastInited = ScriptController.Instance.lastLoaded;
					if (!type.equalsIgnoreCase(EnumScriptType.INIT.function)) {
						EventHooks.onForgeInit(this);
					}
				}
				Iterator<ScriptContainer> iterator = this.scripts.iterator();
				while (iterator.hasNext()) {
					ScriptContainer script = (ScriptContainer) iterator.next();
					script.run(type, new RunNameEvent(type), !this.isClient());
					script.run(type, event, !this.isClient());
				}
			});
		} catch (Exception e) {
		}
	}
	
}
