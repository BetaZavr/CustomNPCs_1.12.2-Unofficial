package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.ParamName;

public interface IJobGuard {

    String[] getTargets();

    void setTargets(@ParamName("targets") String... targets);
}
