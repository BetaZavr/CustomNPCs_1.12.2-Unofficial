package noppes.npcs.api;

public interface IScoreboardScore {

	String getPlayerName();

	int getValue();

	void setValue(@ParamName("value") int value);

}
