package moe.plushie.armourers_workshop.api.common.skin.type;

import java.util.ArrayList;

/**
 * Skin type registry is used to register new ISkinType's and get register
 * ISkinType's and ISkinPart's.
 * 
 * @author RiskyKen
 *
 */
public interface ISkinTypeRegistry {

	public ArrayList<ISkinType> getRegisteredSkinTypes();

	public ISkinPartType getSkinPartFromRegistryName(String registryName);

	public ISkinType getSkinTypeFromRegistryName(String registryName);

	/**
	 * Register a new skin type.
	 * 
	 * @param skinType
	 */
	public boolean registerSkin(ISkinType skinType);
}
