package noppes.npcs.reflection.pathfinding;

import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.util.Arrays;

public class PathReflection {

    private static Field points;
    private static Field openSet;
    private static Field closedSet;
    private static Field currentPathIndex;

    @SuppressWarnings("all")
    public static PathPoint[] getPoints(Path path) {
        if (path == null) { return new PathPoint[0]; }
        if (points == null) {
            Exception error = null;
            try { points = Path.class.getDeclaredField("field_75884_a"); } catch (Exception e) { error = e; }
            if (points == null) {
                try {
                    points = Path.class.getDeclaredField("points");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"points\"", error);
                return new PathPoint[0];
            }
        }
        try {
            points.setAccessible(true);
            return (PathPoint[]) points.get(path);
        } catch (Exception e) {
            LogWriter.error("Error get \"points\" in " + path, e);
        }
        return new PathPoint[0];
    }

    public static PathPoint[] getOpenSet(Path path) {
        if (path == null) { return new PathPoint[0]; }
        try {
            Field field = getOpenSetField();
            field.setAccessible(true);
            return (PathPoint[]) field.get(path);
        } catch (Exception e) {
            LogWriter.error("Error get \"openSet\" in " + path, e);
        }
        return new PathPoint[0];
    }

    public static PathPoint[] getClosedSet(Path path) {
        if (path == null) { return new PathPoint[0]; }
        try {
            Field field = getClosedSetField();
            field.setAccessible(true);
            return (PathPoint[]) field.get(path);
        } catch (Exception e) {
            LogWriter.error("Error get \"closedSet\" in " + path, e);
        }
        return new PathPoint[0];
    }

    public static int getCurrentPathIndex(Path path) {
        if (path == null) { return 0; }
        try {
            Field field = getCurrentPathIndexField();
            field.setAccessible(true);
            return (int) field.get(path);
        } catch (Exception e) {
            LogWriter.error("Error get \"currentPathIndex\" in " + path, e);
        }
        return 0;
    }

    public static void setOpenSet(Path path, PathPoint[] newOpenSet) {
        if (path == null || newOpenSet == null) { return; }
        try {
            Field field = getOpenSetField();
            field.setAccessible(true);
            field.set(path, newOpenSet);
        } catch (Exception e) {
            LogWriter.error("Error set \"openSet\":\"" + Arrays.toString(newOpenSet) + "\" in " + path, e);
        }
    }

    public static void setClosedSet(Path path, PathPoint[] newClosedSet) {
        if (path == null || newClosedSet == null) { return; }
        try {
            Field field = getClosedSetField();
            field.setAccessible(true);
            field.set(path, newClosedSet);
        } catch (Exception e) {
            LogWriter.error("Error set \"closedSet\":\"" + Arrays.toString(newClosedSet) + "\" in " + path, e);
        }
    }

    public static void setCurrentPathIndex(Path path, int newCurrentPathIndex) {
        if (path == null || newCurrentPathIndex < 0) { return; }
        try {
            Field field = getCurrentPathIndexField();
            field.setAccessible(true);
            field.set(path, newCurrentPathIndex);
        } catch (Exception e) {
            LogWriter.error("Error set \"currentPathIndex\":\"" + newCurrentPathIndex + "\" in " + path, e);
        }
    }

    @SuppressWarnings("all")
    private static Field getOpenSetField() {
        if (openSet == null) {
            Exception error = null;
            try { openSet = Path.class.getDeclaredField("field_186312_b"); } catch (Exception e) { error = e; }
            if (openSet == null) {
                try {
                    openSet = Path.class.getDeclaredField("openSet");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"openSet\"", error);
            }
        }
        return openSet;
    }

    @SuppressWarnings("all")
    private static Field getClosedSetField() {
        if (closedSet == null) {
            Exception error = null;
            try { closedSet = Path.class.getDeclaredField("field_186313_c"); } catch (Exception e) { error = e; }
            if (closedSet == null) {
                try {
                    closedSet = Path.class.getDeclaredField("closedSet");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"closedSet\"", error);
            }
        }
        return closedSet;
    }

    @SuppressWarnings("all")
    private static Field getCurrentPathIndexField() {
        if (currentPathIndex == null) {
            Exception error = null;
            try { currentPathIndex = Path.class.getDeclaredField("field_75882_b"); } catch (Exception e) { error = e; }
            if (currentPathIndex == null) {
                try {
                    currentPathIndex = Path.class.getDeclaredField("currentPathIndex");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"currentPathIndex\"", error);
            }
        }
        return currentPathIndex;
    }

}
