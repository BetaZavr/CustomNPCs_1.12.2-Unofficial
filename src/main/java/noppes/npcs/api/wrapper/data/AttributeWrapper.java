package noppes.npcs.api.wrapper.data;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import noppes.npcs.LogWriter;
import noppes.npcs.api.entity.data.IAttributeModifier;
import noppes.npcs.api.entity.data.INpcAttribute;
import noppes.npcs.api.mixin.entity.ai.attributes.IModifiableAttributeInstanceMixin;
import noppes.npcs.api.mixin.entity.ai.attributes.IRangedAttributeMixin;
import noppes.npcs.util.ValueUtil;

public class AttributeWrapper implements INpcAttribute {

	private IAttributeInstance attribute;
	private boolean custom;

	public AttributeWrapper(EntityLivingBase entity, String attributeName, String displayName, double baseValue, double minValue, double maxValue) {
		minValue = ValueUtil.min(minValue, maxValue);
		maxValue = ValueUtil.max(minValue, maxValue);
		RangedAttribute rangedAttribute = new RangedAttribute(null, attributeName, ValueUtil.correctDouble(baseValue, minValue, maxValue), minValue, maxValue);
		rangedAttribute.setDescription(displayName);
		try {
			this.attribute = new ModifiableAttributeInstance(entity.getAttributeMap(), rangedAttribute);
		} catch (Exception e) {
			this.attribute = null;
		}
		this.custom = true;
	}

	public AttributeWrapper(IAttributeInstance mcAttribute) {
		this.attribute = mcAttribute;
		this.custom = false;
		String name = this.getName();
		if (!name.equals("generic.maxHealth") && !name.equals("generic.knockbackResistance")
				&& !name.equals("generic.movementSpeed") && !name.equals("generic.armor")
				&& !name.equals("generic.armorToughness") && !name.equals("generic.attackDamage")
				&& !name.equals("generic.attackSpeed") && !name.equals("generic.luck")
				&& !name.equals("generic.reachDistance") && !name.equals("forge.swimSpeed")) {
			this.custom = true;
		}
	}

	@Override
	public IAttributeModifier addModifier(IAttributeModifier modifier) {
		if (hasModifier(modifier)) {
			return null;
		}
		this.attribute.applyModifier(modifier.getMCModifier());
		return this.getModifier(modifier.getID());
	}

	@Override
	public IAttributeModifier addModifier(String modifierName, double amount, int operation) {
		if (modifierName == null || modifierName.isEmpty() || this.hasModifier(modifierName)) {
			return null;
		}
		AttributeModifier modifier = new AttributeModifier(modifierName, amount, operation);
		this.attribute.applyModifier(modifier);
		return this.getModifier(modifierName);
	}

	@Override
	public double getBaseValue() {
        return this.attribute.getBaseValue();
	}

	@Override
	public String getDisplayName() {
		Object attribute = this.attribute;
		if (this.attribute instanceof ModifiableAttributeInstance) {
			attribute = ((IModifiableAttributeInstanceMixin) this.attribute).npcs$getGenericAttribute();
		}
		if (attribute instanceof RangedAttribute) {
			return ((RangedAttribute) attribute).getDescription();
		}
		return null;
	}

	@Override
	public double getMaxValue() {
		Object attribute = this.attribute;
		if (this.attribute instanceof ModifiableAttributeInstance) {
			attribute = ((IModifiableAttributeInstanceMixin) this.attribute).npcs$getGenericAttribute();
		}
		if (attribute instanceof RangedAttribute) {
			return ((IRangedAttributeMixin) attribute).npcs$getMaxValue();
		}
		return 0.0d;
	}

	@Override
	public IAttributeInstance getMCAttribute() {
		return this.attribute;
	}

	@Override
	public IAttribute getMCBaseAttribute() {
		if (this.attribute instanceof ModifiableAttributeInstance) {
			return ((IModifiableAttributeInstanceMixin) this.attribute).npcs$getGenericAttribute();
		}
		return null;
	}

	@Override
	public double getMinValue() {
		Object attribute = this.attribute;
		if (this.attribute instanceof ModifiableAttributeInstance) {
			attribute = ((IModifiableAttributeInstanceMixin) this.attribute).npcs$getGenericAttribute();
		}
		if (attribute instanceof RangedAttribute) {
			return ((IRangedAttributeMixin) attribute).npcs$getMinValue();
		}
		return 0.0d;
	}

	@Override
	public IAttributeModifier getModifier(String uuidOrName) {
		if (uuidOrName == null || uuidOrName.isEmpty()) {
			return null;
		}
		AttributeModifier modifier = null;
		try {
			UUID uuid = UUID.fromString(uuidOrName);
			modifier = this.attribute.getModifier(uuid);
		} catch (Exception e) { LogWriter.error("Error:", e); }
		if (modifier == null) {
			for (AttributeModifier am : this.attribute.getModifiers()) {
				if (am.getName().equals(uuidOrName)) {
					modifier = am;
					break;
				}
			}
		}
		if (modifier != null) {
			return new AttributeModifierWrapper(this, modifier);
		}
		return null;
	}

	@Override
	public IAttributeModifier[] getModifiers() {
		Collection<AttributeModifier> col = this.attribute.getModifiers();
		IAttributeModifier[] modifiers = new IAttributeModifier[col.size()];
		int i = 0;
		for (AttributeModifier am : col) {
			modifiers[i] = new AttributeModifierWrapper(this, am);
			i++;
		}
		return modifiers;
	}

	@Override
	public IAttributeModifier[] getModifiersByOperation(int operation) {
		Collection<AttributeModifier> col = this.attribute.getModifiersByOperation(operation);
		IAttributeModifier[] modifiers = new IAttributeModifier[col.size()];
		int i = 0;
		for (AttributeModifier am : col) {
			modifiers[i] = new AttributeModifierWrapper(this, am);
			i++;
		}
		return modifiers;
	}

	@Override
	public String getName() {
		if (this.attribute instanceof IAttribute) {
			return ((IAttribute) this.attribute).getName();
		}
		if (this.attribute instanceof ModifiableAttributeInstance) {
			return this.attribute.getAttribute().getName();
		}
		return null;
	}

	@Override
	public double getTotalValue() {
		return this.attribute.getAttributeValue();
	}

	@Override
	public boolean hasModifier(IAttributeModifier modifier) {
		if (modifier == null) {
			return false;
		}
		boolean has = this.attribute.hasModifier(modifier.getMCModifier());
		if (has) {
			return true;
		}
		for (AttributeModifier am : this.attribute.getModifiers()) {
			if (am.getID().equals(modifier.getMCModifier().getID())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasModifier(String uuidOrName) {
		if (uuidOrName == null || uuidOrName.isEmpty()) {
			return false;
		}
		boolean has = false;
		try {
			UUID uuid = UUID.fromString(uuidOrName);
			has = this.attribute.getModifier(uuid) != null;
		} catch (Exception e) { LogWriter.error("Error:", e); }
		if (has) {
			return true;
		}
		for (AttributeModifier am : this.attribute.getModifiers()) {
			if (am.getName().equals(uuidOrName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isCustom() {
		return this.custom;
	}

	@Override
	public void removeAllModifiers() {
		List<AttributeModifier> list = Lists.newArrayList();
        list.addAll(this.attribute.getModifiers());
		for (AttributeModifier am : list) {
			this.attribute.removeModifier(am);
		}
	}

	@Override
	public boolean removeModifier(IAttributeModifier modifier) {
		if (modifier == null) {
			return false;
		}
		if (hasModifier(modifier)) {
			this.attribute.removeModifier(modifier.getMCModifier());
			boolean has = hasModifier(modifier);
			if (!has) {
				return true;
			}
			for (AttributeModifier am : this.attribute.getModifiers()) {
				if (am.getID().equals(modifier.getMCModifier().getID())) {
					this.attribute.removeModifier(am);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean removeModifier(String uuid) {
		return this.removeModifier(this.getModifier(uuid));
	}

	@Override
	public void setBaseValue(double baseValue) {
		this.attribute.setBaseValue(baseValue);
	}

	@Override
	public void setDisplayName(String displayName) {
		Object attribute = this.attribute;
		if (this.attribute instanceof ModifiableAttributeInstance) {
			attribute = ((IModifiableAttributeInstanceMixin) this.attribute).npcs$getGenericAttribute();
		}
		if (attribute instanceof RangedAttribute) {
			((RangedAttribute) attribute).setDescription(displayName);
		}
	}

	@Override
	public void setMaxValue(double maxValue) {
		Object attribute = this.attribute;
		if (this.attribute instanceof ModifiableAttributeInstance) {
			attribute = ((IModifiableAttributeInstanceMixin) this.attribute).npcs$getGenericAttribute();
		}
		if (attribute instanceof RangedAttribute) {
			double minValue = ((IRangedAttributeMixin) attribute).npcs$getMinValue();
			minValue = ValueUtil.min(minValue, maxValue);
			maxValue = ValueUtil.max(minValue, maxValue);
			((IRangedAttributeMixin) attribute).npcs$setMinValue(minValue);
			((IRangedAttributeMixin) attribute).npcs$setMaxValue(maxValue);
		}
	}

	@Override
	public void setMinValue(double minValue) {
		Object attribute = this.attribute;
		if (this.attribute instanceof ModifiableAttributeInstance) {
			attribute = ((IModifiableAttributeInstanceMixin) this.attribute).npcs$getGenericAttribute();
		}
		if (attribute instanceof RangedAttribute) {
			double maxValue = ((IRangedAttributeMixin) attribute).npcs$getMaxValue();
			minValue = ValueUtil.min(minValue, maxValue);
			maxValue = ValueUtil.max(minValue, maxValue);
			((IRangedAttributeMixin) attribute).npcs$setMinValue(minValue);
			((IRangedAttributeMixin) attribute).npcs$setMaxValue(maxValue);
		}
	}

	public void setCustom(boolean bo) { this.custom = bo; }

}
