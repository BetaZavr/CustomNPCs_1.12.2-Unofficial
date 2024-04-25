package noppes.npcs.api;

public interface IScoreboardObjective {

	IScoreboardScore createScore(String player);

	String getCriteria();

	String getDisplayName();

	String getName();

	IScoreboardScore getScore(String player);

	IScoreboardScore[] getScores();

	boolean hasScore(String player);

	boolean isReadyOnly();

	void removeScore(String player);

	void setDisplayName(String name);
}
