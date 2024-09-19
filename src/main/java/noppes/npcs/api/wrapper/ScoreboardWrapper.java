package noppes.npcs.api.wrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IScoreboard;
import noppes.npcs.api.IScoreboardObjective;
import noppes.npcs.api.IScoreboardTeam;
import noppes.npcs.mixin.api.scoreboard.ScoreboardAPIMixin;

public class ScoreboardWrapper implements IScoreboard {

	private final Scoreboard board;
	private final MinecraftServer server;

	protected ScoreboardWrapper(MinecraftServer server) {
		this.server = server;
		this.board = server.getWorld(0).getScoreboard();
	}

	@Override
	public IScoreboardObjective addObjective(String objective, String criteria) {
		IScoreCriteria icriteria = IScoreCriteria.INSTANCES.get(criteria);
		if (icriteria == null) {
			throw new CustomNPCsException("Unknown score criteria: %s", criteria);
		}
		if (objective.isEmpty() || objective.length() > 16) {
			throw new CustomNPCsException("Score objective must be between 1-16 characters: %s", objective);
		}
		ScoreObjective obj = this.board.addScoreObjective(objective, icriteria);
		return new ScoreboardObjectiveWrapper(this.board, obj);
	}

	@Override
	public IScoreboardTeam addTeam(String name) {
		if (this.hasTeam(name)) {
			throw new CustomNPCsException("Team %s already exists", name);
		}
		return new ScoreboardTeamWrapper(this.board.createTeam(name), this.board);
	}

	@Override
	public void deletePlayerScore(String player, String objective, String datatag) {
		ScoreObjective obj = this.getObjectiveWithException(objective);
		if (!this.test(datatag)) {
			return;
		}
		if (this.board.getObjectivesForEntity(player).remove(obj) != null) {
			this.board.removePlayerFromTeams(player);
		}
	}

	@Override
	public IScoreboardObjective getObjective(String name) {
		ScoreObjective obj = this.board.getObjective(name);
		if (obj == null) {
			return null;
		}
		return new ScoreboardObjectiveWrapper(this.board, obj);
	}

	@Override
	public IScoreboardObjective[] getObjectives() {
		List<ScoreObjective> collection = new ArrayList<>(this.board.getScoreObjectives());
		IScoreboardObjective[] objectives = new IScoreboardObjective[collection.size()];
		for (int i = 0; i < collection.size(); ++i) {
			objectives[i] = new ScoreboardObjectiveWrapper(this.board, collection.get(i));
		}
		return objectives;
	}

	private ScoreObjective getObjectiveWithException(String objective) {
		ScoreObjective obj = this.board.getObjective(objective);
		if (obj == null) {
			throw new CustomNPCsException("Score objective does not exist: %s", objective);
		}
		return obj;
	}

	@Override
	public String[] getPlayerList() {
		Collection<String> collection = this.board.getObjectiveNames();
		return collection.toArray(new String[0]);
	}

	@Override
	public int getPlayerScore(String player, String objective, String datatag) {
		ScoreObjective obj = this.getObjectiveWithException(objective);
		if (obj.getCriteria().isReadOnly() || !this.test(datatag)) {
			return 0;
		}
		return this.board.getOrCreateScore(player, obj).getScorePoints();
	}

	@Override
	public IScoreboardTeam getPlayerTeam(String player) {
		ScorePlayerTeam team = this.board.getPlayersTeam(player);
		if (team == null) {
			return null;
		}
		return new ScoreboardTeamWrapper(team, this.board);
	}

	@Override
	public IScoreboardTeam getTeam(String name) {
		ScorePlayerTeam team = this.board.getTeam(name);
        return new ScoreboardTeamWrapper(team, this.board);
	}

	@Override
	public IScoreboardTeam[] getTeams() {
		List<ScorePlayerTeam> list = new ArrayList<>(this.board.getTeams());
		IScoreboardTeam[] teams = new IScoreboardTeam[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			teams[i] = new ScoreboardTeamWrapper(list.get(i), this.board);
		}
		return teams;
	}

	@Override
	public boolean hasObjective(String objective) {
		return this.board.getObjective(objective) != null;
	}

	@Override
	public boolean hasPlayerObjective(String player, String objective, String datatag) {
		ScoreObjective obj = this.getObjectiveWithException(objective);
		return this.test(datatag) && this.board.getObjectivesForEntity(player).get(obj) != null;
	}

	@Override
	public boolean hasTeam(String name) {
		Map<String, ScorePlayerTeam> teams = ((ScoreboardAPIMixin) this.board).npcs$getTeams();
		return teams == null || teams.containsKey(name);
	}

	@Override
	public void removeObjective(String objective) {
		ScoreObjective obj = this.board.getObjective(objective);
		if (obj != null) {
			this.board.removeObjective(obj);
		}
	}

	@Override
	public void removePlayerTeam(String player) {
		this.board.removePlayerFromTeams(player);
	}

	@Override
	public void removeTeam(String name) {
		Map<String, ScorePlayerTeam> teams = ((ScoreboardAPIMixin) this.board).npcs$getTeams();
		if (teams != null && teams.containsKey(name)) {
			this.board.removeTeam(teams.get(name));
		}
	}

	@Override
	public void setPlayerScore(String player, String objective, int score, String datatag) {
		ScoreObjective obj = this.getObjectiveWithException(objective);
		if (obj.getCriteria().isReadOnly() || !this.test(datatag)) {
			return;
		}
		Score sco = this.board.getOrCreateScore(player, obj);
		sco.setScorePoints(score);
	}

	private boolean test(String datatag) {
		if (datatag.isEmpty()) {
			return true;
		}
		try {
			Entity entity = CommandBase.getEntity(this.server, this.server, datatag);
			NBTTagCompound nbttagcompound = JsonToNBT.getTagFromJson(datatag);
			NBTTagCompound compound = new NBTTagCompound();
			entity.writeToNBT(compound);
			return NBTUtil.areNBTEquals(nbttagcompound, compound, true);
		} catch (Exception e) {
			return false;
		}
	}
}
