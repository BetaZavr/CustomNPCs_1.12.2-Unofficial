package noppes.npcs.api.mixin.scoreboard;

import net.minecraft.scoreboard.ScorePlayerTeam;

import java.util.Map;

public interface IScoreboardMixin {

    Map<String, ScorePlayerTeam> npcs$getTeams();

}
