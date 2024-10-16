package noppes.npcs.api.util;

import noppes.npcs.api.IPos;

public interface IRayTraceRotate {

    double getYaw();

    double getPitch();

    double getRadiusXZ();

    double getDistance();

    IPos getStartPos();

    IPos getEndPos();

}
