package moe.plushie.armourers_workshop.api.common.capability;

import moe.plushie.armourers_workshop.api.common.ISkinInventoryContainer;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDescriptor;
import moe.plushie.armourers_workshop.api.common.skin.type.ISkinType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public interface IEntitySkinCapability {

	/**
	 * Checks if the entity can hold this skin type.
	 * 
	 * @param skinType
	 * @return
	 */
	public boolean canHoldSkinType(ISkinType skinType);

	public void clear();

	public void clearSkin(ISkinType skinType, int slotIndex);

	/**
	 * Get the skin descriptor for the ISkinType and slot index.
	 * 
	 * @param skinType
	 * @param slotIndex
	 * @return
	 */
	public ISkinDescriptor getSkinDescriptor(ISkinType skinType, int slotIndex);

	// TODO Change this to use CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
	public ISkinInventoryContainer getSkinInventoryContainer();

	/**
	 * 
	 * @param skinType
	 * @param slotIndex
	 * @return
	 */
	public ItemStack getSkinStack(ISkinType skinType, int slotIndex);

	/**
	 * Gets the number of this skin that the entity can hold.
	 * 
	 * @return
	 */
	public int getSlotCountForSkinType(ISkinType skinType);

	/**
	 * 
	 * @return
	 */
	public ISkinType[] getValidSkinTypes();

	/**
	 * Sets the skin descriptor for the ISkinType and slot.
	 * 
	 * @param skinType
	 * @param slotIndex
	 * @param skinDescriptor
	 * @return ISkinDescriptor that was in the slot. Returns input ISkinDescriptor
	 *         on fail.
	 */
	public ISkinDescriptor setSkinDescriptor(ISkinType skinType, int slotIndex, ISkinDescriptor skinDescriptor);

	/**
	 * 
	 * @param skinType
	 * @param slotIndex
	 * @param skinStack
	 * @return
	 */
	public ItemStack setSkinStack(ISkinType skinType, int slotIndex, ItemStack skinStack);

	public boolean setStackInNextFreeSlot(ItemStack stack);

	/**
	 * Syncs capability data to all players tracking the entity.
	 */
	public void syncToAllTracking();

	/**
	 * Syncs capability data to a player.
	 * 
	 * @param entityPlayer
	 *            Player to sync to.
	 */
	public void syncToPlayer(EntityPlayerMP entityPlayer);
}
