package noppes.npcs.api.event.potion;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.IPotion;
import noppes.npcs.api.entity.IEntity;

public class EndEffect
extends CustomPotionEvent {
	
	public IEntity<?> entity;
	public int amplifier;
	
	public EndEffect(IPotion potion, EntityLivingBase entityLivingBaseIn, int amplifier) {
		super(potion);
		this.entity = this.API.getIEntity(entityLivingBaseIn);
		this.amplifier = amplifier;
	}
	
}