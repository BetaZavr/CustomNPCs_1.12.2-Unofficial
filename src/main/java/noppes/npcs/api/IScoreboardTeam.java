package noppes.npcs.api;

@SuppressWarnings("all")
public interface IScoreboardTeam {

	void addPlayer(@ParamName("player") String player);

	void clearPlayers();

	String getColor();

	String getDisplayName();

	boolean getFriendlyFire();

	String getName();

	String[] getPlayers();

	boolean getSeeInvisibleTeamPlayers();

	boolean hasPlayer(@ParamName("player") String player);

	void removePlayer(@ParamName("player") String player);

	void setColor(@ParamName("color") String color);

	void setDisplayName(@ParamName("name") String name);

	void setFriendlyFire(@ParamName("bo") boolean bo);

	void setSeeInvisibleTeamPlayers(@ParamName("bo") boolean bo);

}
