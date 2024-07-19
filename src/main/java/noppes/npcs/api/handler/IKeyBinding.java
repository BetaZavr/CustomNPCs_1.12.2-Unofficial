package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.IKeySetting;

public interface IKeyBinding {

	IKeySetting createKeySetting();

	IKeySetting getKeySetting(int id);

	IKeySetting[] getKeySettings();

	void removeKeySetting(int id);

}
