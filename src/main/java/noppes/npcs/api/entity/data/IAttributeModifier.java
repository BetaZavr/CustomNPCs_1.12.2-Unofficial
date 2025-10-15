package noppes.npcs.api.entity.data;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import noppes.npcs.api.ParamName;

public interface IAttributeModifier {

	double getAmount();

	String getId();

	AttributeModifier getMCModifier();

	String getName();

	int getOperation();

	IAttributeModifier setAmount(@ParamName("amount") double amount);

	IAttributeModifier setName(@ParamName("name") String name);

	void setOperation(@ParamName("operation") int operation);

}
