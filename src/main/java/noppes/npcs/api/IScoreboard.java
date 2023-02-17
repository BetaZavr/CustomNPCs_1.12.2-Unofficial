package noppes.npcs.api;

public interface IScoreboard {
	
	IScoreboardObjective addObjective(String objective, String criteria);

	IScoreboardTeam addTeam(String name);

	void deletePlayerScore(String player, String objective, String datatag);

	IScoreboardObjective getObjective(String name);

	IScoreboardObjective[] getObjectives();

	String[] getPlayerList();

	int getPlayerScore(String player, String objective, String datatag);

	IScoreboardTeam getPlayerTeam(String player);

	IScoreboardTeam getTeam(String name);

	IScoreboardTeam[] getTeams();

	boolean hasObjective(String p0);

	boolean hasPlayerObjective(String player, String objective, String datatag);

	boolean hasTeam(String name);

	void removeObjective(String objective);

	void removePlayerTeam(String player);

	void removeTeam(String name);

	void setPlayerScore(String player, String objective, int score, String datatag);
}
