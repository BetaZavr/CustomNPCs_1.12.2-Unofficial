package noppes.npcs.api.entity.data;

public interface INPCAdvanced {
	
	String getLine(int type, int slot);

	int getLineCount(int type);

	String getSound(int type);

	void setLine(int type, int slot, String text, String sound);

	void setSound(int type, String sound);
	
}