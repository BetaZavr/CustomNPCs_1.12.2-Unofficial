package noppes.npcs.api.event.potion;

import noppes.npcs.api.IPotion;

public class IsReadyEvent
extends CustomPotionEvent {
	
	public boolean ready;
	public int duration, amplifier;
	
	public IsReadyEvent(IPotion potion, boolean isReady, int duration, int amplifier) {
		super(potion);
		this.ready = isReady;
		this.duration = duration;
		this.amplifier = amplifier;
	}
	
}
