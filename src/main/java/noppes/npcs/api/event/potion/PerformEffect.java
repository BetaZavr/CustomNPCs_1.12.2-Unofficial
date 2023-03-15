package noppes.npcs.api.event.potion;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.entity.IEntity;

public class PerformEffect
extends CustomPotionEvent {
	
	public IEntity<?> entity;
	public int amplifier;
		
	public PerformEffect(ICustomElement potion, EntityLivingBase entityLivingBaseIn, int amplifier) {
		super(potion);
		this.entity = this.API.getIEntity(entityLivingBaseIn);
		this.amplifier = amplifier;
	}
	
}