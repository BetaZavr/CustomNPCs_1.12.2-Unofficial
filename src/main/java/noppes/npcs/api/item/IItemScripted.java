package noppes.npcs.api.item;

public interface IItemScripted
extends IItemStack {
	
	int getColor();

	int getDurabilityColor();

	boolean getDurabilityShow();

	double getDurabilityValue();

	String getTexture(int damage);

	boolean hasTexture(int damage);

	void setColor(int color);

	void setDurabilityColor(int color);

	void setDurabilityShow(boolean bo);

	void setDurabilityValue(float value);

	void setMaxStackSize(int size);

	void setTexture(int damage, String texture);
	
}
