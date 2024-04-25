package noppes.npcs.api.event;

import noppes.npcs.api.entity.IProjectile;

public class ProjectileEvent extends CustomNPCsEvent {

	public static class ImpactEvent extends ProjectileEvent {
		public Object target;
		public int type;

		public ImpactEvent(IProjectile<?> projectile, int type, Object target) {
			super(projectile);
			this.type = type;
			this.target = target;
		}
	}

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
