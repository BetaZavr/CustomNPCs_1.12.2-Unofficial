package noppes.npcs.mixin.scoreboard;

import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import noppes.npcs.api.mixin.scoreboard.IServerScoreboardMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(value = ServerScoreboard.class)
public class ServerScoreboardMixin implements IServerScoreboardMixin {

    @Final
    @Shadow
    private Set<ScoreObjective> addedObjectives;

    @Override
    public Set<ScoreObjective> npcs$getAddedObjectives() { return addedObjectives; }

}
