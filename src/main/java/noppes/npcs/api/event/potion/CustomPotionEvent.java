package noppes.npcs.api.event.potion;

import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.event.CustomNPCsEvent;

public class CustomPotionEvent extends CustomNPCsEvent {

	public ICustomElement potion = null;

	public CustomPotionEvent(ICustomElement potion) {
		super();
		this.potion = potion;
	}

}
