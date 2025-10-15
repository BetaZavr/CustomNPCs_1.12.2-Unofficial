package noppes.npcs.api.item;

import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface IItemScripted extends IItemStack {

	int getColor();

	int getDurabilityColor();

	boolean getDurabilityShow();

	double getDurabilityValue();

	String getTexture(@ParamName("damage") int damage);

	boolean hasTexture(@ParamName("damage") int damage);

	void setColor(@ParamName("color") int color);

	void setDurabilityColor(@ParamName("color") int color);

	void setDurabilityShow(@ParamName("bo") boolean bo);

	void setDurabilityValue(@ParamName("value") float value);

	void setMaxStackSize(@ParamName("size") int size);

	void setTexture(@ParamName("damage") int damage, @ParamName("texture") String texture);

}
