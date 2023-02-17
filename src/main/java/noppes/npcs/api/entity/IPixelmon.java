package noppes.npcs.api.entity;

import net.minecraft.entity.passive.EntityTameable;

public interface IPixelmon<T extends EntityTameable> extends IAnimal<T> {
	Object getPokemonData();
}
