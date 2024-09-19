package noppes.npcs.mixin.api.scoreboard;

import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(value = ServerScoreboard.class)
public interface ServerScoreboardAPIMixin {

    @Accessor(value="addedObjectives")
    Set<ScoreObjective> npcs$getAddedObjectives();

}
