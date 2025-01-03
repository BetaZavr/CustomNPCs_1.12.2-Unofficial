package noppes.npcs.mixin.scoreboard;

import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import noppes.npcs.api.mixin.scoreboard.IScoreboardMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = Scoreboard.class)
public class ScoreboardMixin implements IScoreboardMixin {

    @Final
    @Shadow
    private Map<String, ScorePlayerTeam> teams;

    @Override
    public Map<String, ScorePlayerTeam> npcs$getTeams() { return teams; }

}
