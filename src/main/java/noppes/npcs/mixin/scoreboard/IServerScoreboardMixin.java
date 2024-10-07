package noppes.npcs.mixin.scoreboard;

import net.minecraft.scoreboard.ScoreObjective;

import java.util.Set;

public interface IServerScoreboardMixin {

    Set<ScoreObjective> npcs$getAddedObjectives();

}
