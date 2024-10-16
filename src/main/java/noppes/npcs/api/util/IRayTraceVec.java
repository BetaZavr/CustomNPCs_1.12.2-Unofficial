package noppes.npcs.api.util;

import noppes.npcs.api.IPos;

public interface IRayTraceVec {

    double getYaw();

    double getDistance();

    IPos getStartPos();

    IPos getEndPos();

    double getX();

    double getY();

    double getZ();

}
