package noppes.npcs.api.wrapper;

import net.minecraft.scoreboard.Score;
import noppes.npcs.api.IScoreboardScore;

public class ScoreboardScoreWrapper implements IScoreboardScore {
	private Score score;

	public ScoreboardScoreWrapper(Score score) {
		this.score = score;
	}

	@Override
	public String getPlayerName() {
		return this.score.getPlayerName();
	}

	@Override
	public int getValue() {
		return this.score.getScorePoints();
	}

	@Override
	public void setValue(int val) {
		this.score.setScorePoints(val);
	}
}
