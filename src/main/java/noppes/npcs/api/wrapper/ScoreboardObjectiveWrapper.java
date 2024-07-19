package noppes.npcs.api.wrapper;

import java.util.Collection;

import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IScoreboardObjective;
import noppes.npcs.api.IScoreboardScore;

public class ScoreboardObjectiveWrapper implements IScoreboardObjective {

	private final Scoreboard board;
	private final ScoreObjective objective;

	protected ScoreboardObjectiveWrapper(Scoreboard board, ScoreObjective objective) {
		this.objective = objective;
		this.board = board;
	}

	@Override
	public IScoreboardScore createScore(String player) {
		return new ScoreboardScoreWrapper(this.board.getOrCreateScore(player, this.objective));
	}

	@Override
	public String getCriteria() {
		return this.objective.getCriteria().getName();
	}

	@Override
	public String getDisplayName() {
		return this.objective.getDisplayName();
	}

	@Override
	public String getName() {
		return this.objective.getName();
	}

	@Override
	public IScoreboardScore getScore(String player) {
		if (!this.hasScore(player)) {
			return null;
		}
		return new ScoreboardScoreWrapper(this.board.getOrCreateScore(player, this.objective));
	}

	@Override
	public IScoreboardScore[] getScores() {
		Collection<Score> list = this.board.getSortedScores(this.objective);
		IScoreboardScore[] scores = new IScoreboardScore[list.size()];
		int i = 0;
		for (Score score : list) {
			scores[i] = new ScoreboardScoreWrapper(score);
			++i;
		}
		return scores;
	}

	@Override
	public boolean hasScore(String player) {
		return this.board.entityHasObjective(player, this.objective);
	}

	@Override
	public boolean isReadyOnly() {
		return this.objective.getCriteria().isReadOnly();
	}

	@Override
	public void removeScore(String player) {
		this.board.removeObjectiveFromEntity(player, this.objective);
	}

	@Override
	public void setDisplayName(String name) {
		if (name.isEmpty() || name.length() > 16) {
			throw new CustomNPCsException("Score objective display name must be between 1-16 characters: %s", name);
		}
		this.objective.setDisplayName(name);
	}
}
