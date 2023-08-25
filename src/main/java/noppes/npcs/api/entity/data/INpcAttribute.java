package noppes.npcs.api.entity.data;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

public interface INpcAttribute {

	IAttributeInstance getMCAttribute();

	IAttribute getMCBaseAttribute();

	boolean isCustom();

	String getName();

	String getDisplayName();

	void setDisplayName(String displayName);
	
	double getBaseValue();

    void setBaseValue(double baseValue);
    
	double getMinValue();

    void setMinValue(double minValue);
    
	double getMaxValue();

    void setMaxValue(double maxValue);

    IAttributeModifier[] getModifiersByOperation(int operation);

    IAttributeModifier[] getModifiers();

    boolean hasModifier(IAttributeModifier modifier);
    
    boolean hasModifier(String uuidOrName);

    IAttributeModifier getModifier(String uuidOrName);

    IAttributeModifier addModifier(IAttributeModifier modifier);
    
    IAttributeModifier addModifier(String modifierName, double amount, int operation);

    boolean removeModifier(IAttributeModifier modifier);

    boolean removeModifier(String uuid);

    void removeAllModifiers();

    double getTotalValue();

}
