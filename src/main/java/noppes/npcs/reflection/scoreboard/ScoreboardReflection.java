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

    @SuppressWarnings("all")
    public static Map<String, ScorePlayerTeam> getTeams(Scoreboard board) {
        if (board == null) { return Collections.emptyMap(); }
        if (teams == null) {
            Exception error = null;
            try { teams = Scoreboard.class.getDeclaredField("field_96542_e"); } catch (Exception e) { error = e; }
            if (teams == null) {
                try {
                    teams = Scoreboard.class.getDeclaredField("teams");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"teams\"", error);
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

    @SuppressWarnings("all")
    public static Set<ScoreObjective> getAddedObjectives(ServerScoreboard board) {
        if (board == null) { return Collections.emptySet(); }
        if (addedObjectives == null) {
            Exception error = null;
            try { addedObjectives = ServerScoreboard.class.getDeclaredField("field_96553_b"); } catch (Exception e) { error = e; }
            if (addedObjectives == null) {
                try {
                    addedObjectives = ServerScoreboard.class.getDeclaredField("addedObjectives");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"addedObjectives\"", error);
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
