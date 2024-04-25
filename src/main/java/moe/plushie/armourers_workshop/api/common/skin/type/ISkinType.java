package moe.plushie.armourers_workshop.api.common.skin.type;

import java.util.ArrayList;

import moe.plushie.armourers_workshop.api.common.skin.data.ISkinProperties;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 
 * @author RiskyKen
 *
 */
public interface ISkinType {

	/**
	 * Is this skin enabled?
	 * 
	 * @return Is enabled?
	 */
	public boolean enabled();

	@SideOnly(Side.CLIENT)
	public ResourceLocation getIcon();

	/**
	 * This only exists for backwards compatibility with old world saves. Just
	 * return getRegistryName().
	 * 
	 * @return name
	 */
	public String getName();

	public ArrayList<ISkinProperty<?>> getProperties();

	/**
	 * Gets the name this skin will be registered with. Armourer's Workshop uses the
	 * format armourers:skinName. Example armourers:head is the registry name of
	 * Armourer's Workshop head armour skin.
	 * 
	 * @return registryName
	 */
	public String getRegistryName();

	public ArrayList<ISkinPartType> getSkinParts();

	@SideOnly(Side.CLIENT)
	public ResourceLocation getSlotIcon();

	/**
	 * If this skin is for vanilla armour return the slot id here, otherwise return
	 * -1.
	 * 
	 * @return slotId
	 */
	public int getVanillaArmourSlotId();

	public boolean haveBoundsChanged(ISkinProperties skinPropsOld, ISkinProperties skinPropsNew);

	/**
	 * Should this skin be hidden from the user?
	 * 
	 * @return Is hidden?
	 */
	public boolean isHidden();

	/**
	 * Should the helper check box be shown in the armourer and mini armourer.
	 * 
	 * @return
	 */
	public boolean showHelperCheckbox();

	/**
	 * Should the show skin overlay check box be shown in the armourer and mini
	 * armourer.
	 * 
	 * @return
	 */
	public boolean showSkinOverlayCheckbox();
}
