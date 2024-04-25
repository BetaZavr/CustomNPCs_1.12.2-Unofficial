package moe.plushie.armourers_workshop.api.common.skin.entity;

import net.minecraft.entity.Entity;

public interface ISkinnableEntityRegisty {

	public ISkinnableEntity getSkinnableEntity(Entity entity);

	public boolean isValidEntity(Entity entity);

	public void registerEntity(ISkinnableEntity skinnableEntity);
}
