package noppes.npcs.api.event;

import noppes.npcs.api.EventName;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.constants.EnumScriptType;

public class ProjectileEvent extends CustomNPCsEvent {

	@EventName(EnumScriptType.PROJECTILE_IMPACT)
	public static class ImpactEvent extends ProjectileEvent {
		public Object target;
		public int type;

		public ImpactEvent(IProjectile<?> projectile, int type, Object target) {
			super(projectile);
			this.type = type;
			this.target = target;
		}
	}

	@EventName(EnumScriptType.TICK)
	public static class UpdateEvent extends ProjectileEvent {
		public UpdateEvent(IProjectile<?> projectile) {
			super(projectile);
		}
	}

	public IProjectile<?> projectile;

	public ProjectileEvent(IProjectile<?> projectile) {
		this.projectile = projectile;
	}

}
