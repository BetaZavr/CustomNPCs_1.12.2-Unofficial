package noppes.npcs.api.entity.data;

import net.minecraft.entity.ai.attributes.IAttribute;
import noppes.npcs.api.ParamName;

public interface IAttributeSet {

	String getAttribute();

	double getChance();

	double getMaxValue();

	double getMinValue();

	int getSlot();

	void remove();

	void setAttribute(@ParamName("attribute") IAttribute attribute);

	void setAttribute(@ParamName("name") String name);

	void setChance(@ParamName("chance") double chance);

	void setSlot(@ParamName("slot") int slot);

	void setValues(@ParamName("min") double min, @ParamName("max") double max);

}
