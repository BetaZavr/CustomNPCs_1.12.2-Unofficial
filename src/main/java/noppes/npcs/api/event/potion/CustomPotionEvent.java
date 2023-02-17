package noppes.npcs.api.event.potion;

import noppes.npcs.api.IPotion;
import noppes.npcs.api.event.CustomNPCsEvent;

public class CustomPotionEvent
extends CustomNPCsEvent
{
	
	public IPotion potion = null;
	
	public CustomPotionEvent(IPotion potion) {
		super();
		this.potion = potion;
	}

}
