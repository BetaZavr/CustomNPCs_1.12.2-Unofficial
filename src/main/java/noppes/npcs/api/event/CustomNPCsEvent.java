package noppes.npcs.api.event;

import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.api.NpcAPI;

public class CustomNPCsEvent extends Event {

	public NpcAPI API;

	public CustomNPCsEvent() {
		this.API = NpcAPI.Instance();
	}

}
