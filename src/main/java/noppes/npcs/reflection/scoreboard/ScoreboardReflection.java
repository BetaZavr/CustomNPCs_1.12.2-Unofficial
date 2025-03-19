package noppes.npcs.reflection.scoreboard;

import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ScoreboardReflection {

    private static Field teams;
    private static Field addedObjectives;

    @SuppressWarnings("unchecked")
    public static Map<String, ScorePlayerTeam> getTeams(Scoreboard board) {
        if (board == null) { return Collections.emptyMap(); }
        if (teams == null) {
            try {
                try { teams = Scoreboard.class.getDeclaredField("field_96542_e"); }
                catch (Exception e) { teams = Scoreboard.class.getDeclaredField("teams"); }
            } catch (Exception e) {
                LogWriter.error("Not Found field \"teams\" in " + board);
                return Collections.emptyMap();
            }
        }
        try {
            teams.setAccessible(true);
            return (Map<String, ScorePlayerTeam>) teams.get(board);
        } catch (Exception e) {
            LogWriter.error("Not get \"teams\" in " + board);
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    public static Set<ScoreObjective> getAddedObjectives(ServerScoreboard board) {
        if (board == null) { return Collections.emptySet(); }
        if (addedObjectives == null) {
            try {
                try { addedObjectives = ServerScoreboard.class.getDeclaredField("field_96553_b"); }
                catch (Exception e) { addedObjectives = ServerScoreboard.class.getDeclaredField("addedObjectives"); }
            } catch (Exception e) {
                LogWriter.error("Not Found field \"addedObjectives\" in " + board);
                return Collections.emptySet();
            }
        }
        try {
            addedObjectives.setAccessible(true);
            return (Set<ScoreObjective>) addedObjectives.get(board);
        } catch (Exception e) {
            LogWriter.error("Not get \"addedObjectives\" in " + board);
        }
        return Collections.emptySet();
    }

}
