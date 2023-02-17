package noppes.npcs.controllers.data;

import noppes.npcs.constants.EnumAvailabilityScoreboard;

public class AvailabilityScoreboardData {

	public EnumAvailabilityScoreboard scoreboardType;
	public int scoreboardValue;

	public AvailabilityScoreboardData(EnumAvailabilityScoreboard type, int value) {
		this.scoreboardType = type;
		this.scoreboardValue = value;
	}

}
