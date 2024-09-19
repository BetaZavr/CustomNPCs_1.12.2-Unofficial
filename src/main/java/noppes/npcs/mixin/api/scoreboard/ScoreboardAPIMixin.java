package noppes.npcs.mixin.api.scoreboard;

import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = Scoreboard.class)
public interface ScoreboardAPIMixin {

    @Accessor(value="teams")
    Map<String, ScorePlayerTeam> npcs$getTeams();

}
