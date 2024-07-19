package noppes.npcs.api.wrapper.data;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import noppes.npcs.api.entity.data.IAttributeModifier;
import noppes.npcs.api.entity.data.INpcAttribute;
import noppes.npcs.util.ObfuscationHelper;
import noppes.npcs.util.ValueUtil;

public class AttributeModifierWrapper implements IAttributeModifier {

	private final INpcAttribute parent;
	private final AttributeModifier modifer;

	public AttributeModifierWrapper(INpcAttribute attribute, AttributeModifier modifer) {
		this.modifer = modifer;
		this.parent = attribute;
	}

	@Override
	public double getAmount() {
		return this.modifer.getAmount();
	}

	@Override
	public String getID() {
		return this.modifer.getID().toString();
	}

	@Override
	public AttributeModifier getMCModifier() {
		return this.modifer;
	}

	@Override
	public String getName() {
		return this.modifer.getName();
	}

	@Override
	public int getOperation() {
		return this.modifer.getOperation();
	}

	@Override
	public IAttributeModifier setAmount(double amount) {
		if (this.parent == null) {
			ObfuscationHelper.setValue(AttributeModifier.class, this.modifer, amount, double.class);
			return this;
		}
		AttributeModifier newModifer = new AttributeModifier(this.modifer.getID(), this.modifer.getName(), amount,
				this.modifer.getOperation());
		this.parent.getMCAttribute().removeModifier(this.modifer);
		this.parent.getMCAttribute().applyModifier(newModifer);
		return this.parent.getModifier(newModifer.getName());
	}

	@Override
	public IAttributeModifier setName(String name) {
		if (this.parent == null) {
			ObfuscationHelper.setValue(AttributeModifier.class, this.modifer, name, String.class);
			return this;
		}
		AttributeModifier newModifer = new AttributeModifier(this.modifer.getID(), name, this.modifer.getAmount(),
				this.modifer.getOperation());
		this.parent.getMCAttribute().removeModifier(this.modifer);
		this.parent.getMCAttribute().applyModifier(newModifer);
		return this.parent.getModifier(newModifer.getName());
	}

	@Override
	public void setOperation(int operation) {
		ObfuscationHelper.setValue(AttributeModifier.class, this.modifer, ValueUtil.correctInt(operation, 0, 2),
				int.class);
	}

	public String toString() {
		return this.modifer.toString().replace("AttributeModifier", "AttributeModifierWrapper");
	}

}
