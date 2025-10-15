package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface IHealerEffect {

    int getEffect();

    int getRange();

    void setRange(@ParamName("range") int range);

    int getSpeed();

    void setSpeed(@ParamName("speed") int speed);

    int getTime();

    void setTime(@ParamName("time") int time);

    int getAmplifier();

    void setAmplifier(@ParamName("amplifier") int amplifier);

    int getType();

    void setType(@ParamName("type") int type);

    boolean isOnHimSelf();

    void setOnHimSelf(@ParamName("bo") boolean bo);

    boolean isPossibleOnMobs();

    void setPossibleOnMobs(@ParamName("bo") boolean bo);

    boolean isMassive();

    void setIsMassive(@ParamName("bo") boolean bo);
}
