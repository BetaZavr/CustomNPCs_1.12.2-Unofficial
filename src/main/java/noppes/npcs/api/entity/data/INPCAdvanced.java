package noppes.npcs.api.entity.data;

import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface INPCAdvanced {

	String getLine(@ParamName("type") int type, @ParamName("slot") int slot);

	int getLineCount(@ParamName("type") int type);

	String getSound(@ParamName("type") int type);

	void setLine(@ParamName("type") int type, @ParamName("slot") int slot,
				 @ParamName("text") String text, @ParamName("sound") String sound);

	void setSound(@ParamName("type") int type, @ParamName("sound") String sound);

}