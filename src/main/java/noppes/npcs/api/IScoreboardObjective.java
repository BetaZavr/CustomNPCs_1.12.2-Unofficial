package noppes.npcs.api;

public interface IScoreboardObjective {
	IScoreboardScore createScore(String p0);

	String getCriteria();

	String getDisplayName();

	String getName();

	IScoreboardScore getScore(String p0);

	IScoreboardScore[] getScores();

	boolean hasScore(String p0);

	boolean isReadyOnly();

	void removeScore(String p0);

	void setDisplayName(String p0);
}
