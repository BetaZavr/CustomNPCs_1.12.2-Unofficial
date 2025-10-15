package noppes.npcs.api;

@SuppressWarnings("all")
public interface IScoreboardObjective {

	IScoreboardScore createScore(@ParamName("player") String player);

	String getCriteria();

	String getDisplayName();

	String getName();

	IScoreboardScore getScore(@ParamName("player") String player);

	IScoreboardScore[] getScores();

	boolean hasScore(@ParamName("player") String player);

	boolean isReadyOnly();

	void removeScore(@ParamName("player") String player);

	void setDisplayName(@ParamName("name") String name);
}
