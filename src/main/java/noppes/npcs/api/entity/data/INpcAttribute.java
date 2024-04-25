package noppes.npcs.api.entity.data;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

public interface INpcAttribute {

	IAttributeModifier addModifier(IAttributeModifier modifier);

	IAttributeModifier addModifier(String modifierName, double amount, int operation);

	double getBaseValue();

	String getDisplayName();

	double getMaxValue();

	IAttributeInstance getMCAttribute();

	IAttribute getMCBaseAttribute();

	double getMinValue();

	IAttributeModifier getModifier(String uuidOrName);

	IAttributeModifier[] getModifiers();

	IAttributeModifier[] getModifiersByOperation(int operation);

	String getName();

	double getTotalValue();

	boolean hasModifier(IAttributeModifier modifier);

	boolean hasModifier(String uuidOrName);

	boolean isCustom();

	void removeAllModifiers();

	boolean removeModifier(IAttributeModifier modifier);

	boolean removeModifier(String uuid);

	void setBaseValue(double baseValue);

	void setDisplayName(String displayName);

	void setMaxValue(double maxValue);

	void setMinValue(double minValue);

}
