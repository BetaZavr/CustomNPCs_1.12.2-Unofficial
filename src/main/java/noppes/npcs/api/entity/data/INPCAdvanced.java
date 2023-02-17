package noppes.npcs.api.entity.data;

public interface INPCAdvanced {
	String getLine(int p0, int p1);

	int getLineCount(int p0);

	String getSound(int p0);

	void setLine(int p0, int p1, String p2, String p3);

	void setSound(int p0, String p1);
}
