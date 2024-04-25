package noppes.npcs.api.event.potion;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.entity.IEntity;

public class AffectEntity extends CustomPotionEvent {

	public IEntity<?> source, indirectSource, entity;
	public int amplifier;
	public double health;

	public AffectEntity(ICustomElement potion, Entity source, Entity indirectSource,
			EntityLivingBase entityLivingBaseIn, int amplifier, double health) {
		super(potion);
		this.source = entityLivingBaseIn != null ? this.API.getIEntity(entityLivingBaseIn) : null;
		this.indirectSource = entityLivingBaseIn != null ? this.API.getIEntity(entityLivingBaseIn) : null;
		this.entity = this.API.getIEntity(entityLivingBaseIn);
		this.amplifier = amplifier;
		this.health = health;
	}

}