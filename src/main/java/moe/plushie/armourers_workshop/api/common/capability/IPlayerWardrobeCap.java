package moe.plushie.armourers_workshop.api.common.capability;

import moe.plushie.armourers_workshop.api.common.skin.type.ISkinType;
import net.minecraft.inventory.EntityEquipmentSlot;

public interface IPlayerWardrobeCap extends IWardrobeCap {

	public boolean getArmourOverride(EntityEquipmentSlot equipmentSlot);

	public int getMaxSlotsForSkinType(ISkinType skinType);

	public int getUnlockedSlotsForSkinType(ISkinType skinType);

	public void setArmourOverride(EntityEquipmentSlot equipmentSlot, boolean override);

	public void setUnlockedSlotsForSkinType(ISkinType skinType, int count);
}
