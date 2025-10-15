package noppes.npcs.api.handler;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.handler.data.IKeySetting;

@SuppressWarnings("all")
public interface IKeyBinding {

	IKeySetting createKeySetting();

	IKeySetting getKeySetting(@ParamName("id") int id);

	IKeySetting[] getKeySettings();

	void removeKeySetting(@ParamName("id") int id);

}
