package noppes.npcs.api;

public interface IScoreboardTeam {
	
	void addPlayer(String player);

	void clearPlayers();

	String getColor();

	String getDisplayName();

	boolean getFriendlyFire();

	String getName();

	String[] getPlayers();

	boolean getSeeInvisibleTeamPlayers();

	boolean hasPlayer(String player);

	void removePlayer(String player);

	void setColor(String color);

	void setDisplayName(String name);

	void setFriendlyFire(boolean bo);

	void setSeeInvisibleTeamPlayers(boolean bo);
	
}
