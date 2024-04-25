package noppes.npcs.api.entity.data;

import net.minecraft.entity.ai.attributes.AttributeModifier;

public interface IAttributeModifier {

	double getAmount();

	String getID();

	AttributeModifier getMCModifier();

	String getName();

	int getOperation();

	IAttributeModifier setAmount(double amount);

	IAttributeModifier setName(String name);

	void setOperation(int operation);

}
