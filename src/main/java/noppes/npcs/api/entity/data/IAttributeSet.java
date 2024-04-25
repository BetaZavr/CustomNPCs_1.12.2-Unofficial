package noppes.npcs.api.entity.data;

import net.minecraft.entity.ai.attributes.IAttribute;

public interface IAttributeSet {

	String getAttribute();

	double getChance();

	double getMaxValue();

	double getMinValue();

	int getSlot();

	void remove();

	void setAttribute(IAttribute attribute);

	void setAttribute(String name);

	void setChance(double chance);

	void setSlot(int slot);

	void setValues(double min, double max);

}
