package noppes.npcs.api.item;

public interface IItemScripted extends IItemStack {
	int getColor();

	int getDurabilityColor();

	boolean getDurabilityShow();

	double getDurabilityValue();

	String getTexture(int p0);

	boolean hasTexture(int p0);

	void setColor(int p0);

	void setDurabilityColor(int p0);

	void setDurabilityShow(boolean p0);

	void setDurabilityValue(float p0);

	void setMaxStackSize(int p0);

	void setTexture(int p0, String p1);
}
