package noppes.npcs.api.event;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.ICustomParticle;
import noppes.npcs.particles.CustomParticle;

public class CustomParticleEvent extends CustomNPCsEvent {

	public static class CreateEvent extends CustomParticleEvent {

		public CreateEvent(CustomParticle particle, EntityPlayerSP player) {
			super(particle, player);
		}

	}

	@Cancelable
	public static class RenderEvent extends CustomParticleEvent {

		public RenderEvent(CustomParticle particle, EntityPlayerSP player) {
			super(particle, player);
		}

	}

	@Cancelable
	public static class UpdateEvent extends CustomParticleEvent {

		public UpdateEvent(CustomParticle particle, EntityPlayerSP player) {
			super(particle, player);
		}

	}

	public ICustomParticle particle;
	public IPlayer<?> player;

	public CustomParticleEvent(CustomParticle particle, EntityPlayerSP player) {
		super();
		this.particle = particle;
		this.player = player == null ? null : (IPlayer<?>) NpcAPI.Instance().getIEntity(player);
	}

}
