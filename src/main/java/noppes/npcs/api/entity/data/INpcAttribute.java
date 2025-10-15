package noppes.npcs.api.entity.data;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface INpcAttribute {

	IAttributeModifier addModifier(@ParamName("modifier") IAttributeModifier modifier);

	IAttributeModifier addModifier(@ParamName("modifierName") String modifierName, @ParamName("amount") double amount, @ParamName("operation") int operation);

	double getBaseValue();

	String getDisplayName();

	double getMaxValue();

	IAttributeInstance getMCAttribute();

	IAttribute getMCBaseAttribute();

	double getMinValue();

	IAttributeModifier getModifier(@ParamName("uuidOrName") String uuidOrName);

	IAttributeModifier[] getModifiers();

	IAttributeModifier[] getModifiersByOperation(@ParamName("operation") int operation);

	String getName();

	double getTotalValue();

	boolean hasModifier(@ParamName("modifier") IAttributeModifier modifier);

	boolean hasModifier(@ParamName("uuidOrName") String uuidOrName);

	boolean isCustom();

	void removeAllModifiers();

	boolean removeModifier(@ParamName("modifier") IAttributeModifier modifier);

	boolean removeModifier(@ParamName("uuid") String uuid);

	void setBaseValue(@ParamName("baseValue") double baseValue);

	void setDisplayName(@ParamName("displayName") String displayName);

	void setMaxValue(@ParamName("maxValue") double maxValue);

	void setMinValue(@ParamName("minValue") double minValue);

}
