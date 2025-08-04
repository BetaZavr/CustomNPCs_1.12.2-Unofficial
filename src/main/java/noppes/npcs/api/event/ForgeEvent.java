package noppes.npcs.api.event;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.api.IPos;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.controllers.data.Zone3D;

@Cancelable
public class ForgeEvent extends CustomNPCsEvent {

	public static class EnterToRegion extends CustomNPCsEvent {

		public Entity entity;
		public Zone3D region;

		public EnterToRegion(Entity entityIn, Zone3D zone) {
			super();
			entity = entityIn;
			region = zone;
		}

	}

	public static class LeaveRegion extends CustomNPCsEvent {

		public Entity entity;
		public Zone3D region;

		public LeaveRegion(Entity entityIn, Zone3D zone) {
			super();
			entity = entityIn;
			region = zone;
		}

	}

	public static class InitEvent extends ForgeEvent {

		public InitEvent() {
			super(null);
		}

	}

	@Cancelable
	public static class SoundTickEvent extends ForgeEvent {

		public float milliSeconds, totalSecond;
		public String name, resource;
		public float volume, pitch;
		public IPlayer<?> player;
		public IPos pos;

		public SoundTickEvent(IPlayer<?> playerIn, String nameIn, String resourceIn, IPos posIn, float volumeIn, float pitchIn, float milliSecondsIn, float totalSecondIn) {
			super(null);
			milliSeconds = milliSecondsIn;
			totalSecond = totalSecondIn;
			name = nameIn;
			resource = resourceIn;
			volume = volumeIn;
			pitch = pitchIn;
			pos = posIn;
			player = playerIn;
		}
	}

	public Event event;

	public ForgeEvent(Event eventIn) {
		super();
		event = eventIn;
	}

}
