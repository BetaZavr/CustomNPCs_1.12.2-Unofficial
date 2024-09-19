package noppes.npcs.mixin.scoreboard;

import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import noppes.npcs.mixin.api.scoreboard.ScoreboardAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = Scoreboard.class)
public class ScoreboardMixin implements ScoreboardAPIMixin {

    @Final
    @Shadow(aliases = "teams")
    private Map<String, ScorePlayerTeam> teams;

    @Override
    public Map<String, ScorePlayerTeam> npcs$getTeams() { return teams; }

}
