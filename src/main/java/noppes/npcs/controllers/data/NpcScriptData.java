package noppes.npcs.controllers.data;

import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;

public class NpcScriptData
extends BaseScriptData {
	
	@Override
	public String noticeString(String type, Object event) {
		String notice = "NPC's Script";
		if (type != null) { notice += " hook \""+type+"\""; }
		if (event instanceof NpcEvent && ((NpcEvent) event).npc != null) {
			EntityNPCInterface npc = (EntityNPCInterface) ((NpcEvent) event).npc.getMCEntity();
			notice += ". NPC \"" + npc.getName() + "\"; UUID: \"" + npc.getUniqueID() + "\"" +
					" in dimension ID:" + (npc.world == null ? 0 : npc.world.provider.getDimension()) +
					"; X:" + (Math.round(npc.posX * 100.0d) / 100.0d) +
					"; Y:" + (Math.round(npc.posY * 100.0d) / 100.0d) +
					"; Z:" + (Math.round(npc.posZ * 100.0d) / 100.0d) +
					"; Side: " + (isClient() ? "Client" : "Server");
		}
		return notice;
	}
	
	@Override
	public void runScript(String type, Event event) {
		super.runScript(type, event);
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
