package noppes.npcs.api.entity.data;

import net.minecraft.entity.ai.attributes.AttributeModifier;

public interface IAttributeModifier {

	AttributeModifier getMCModifier();
	
	String getID();

    String getName();
    
    IAttributeModifier setName(String name);

    int getOperation();
    
    void setOperation(int operation);

    double getAmount();
    
    IAttributeModifier setAmount(double amount);
    
}
