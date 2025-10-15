package noppes.npcs.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.EventName;
import noppes.npcs.api.IPos;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.constants.EnumScriptType;

import java.util.Objects;

public class BlockEvent extends CustomNPCsEvent {

	@EventName(EnumScriptType.BROKEN)
	public static class BreakEvent extends BlockEvent {
		public BreakEvent(IBlock block) {
			super(block);
		}
	}

	@EventName(EnumScriptType.CLICKED)
	public static class ClickedEvent extends BlockEvent {
		public IPlayer<?> player;

		public ClickedEvent(IBlock block, EntityPlayer player) {
			super(block);
			this.player = (IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player);
		}
	}

	@EventName(EnumScriptType.COLLIDE)
	public static class CollidedEvent extends BlockEvent {
		public IEntity<?> entity;

		public CollidedEvent(IBlock block, Entity entity) {
			super(block);
			this.entity = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(entity);
		}
	}

	@Cancelable
	@EventName(EnumScriptType.DOOR_TOGGLE)
	public static class DoorToggleEvent extends BlockEvent {
		public DoorToggleEvent(IBlock block) {
			super(block);
		}
	}

	@Cancelable
	@EventName(EnumScriptType.FALLEN_UPON)
	public static class EntityFallenUponEvent extends BlockEvent {
		public float distanceFallen;
		public IEntity<?> entity;

		public EntityFallenUponEvent(IBlock block, Entity entity, float distance) {
			super(block);
			this.distanceFallen = distance;
			this.entity = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(entity);
		}
	}

	@Cancelable
	@EventName(EnumScriptType.EXPLODED)
	public static class ExplodedEvent extends BlockEvent {
		public ExplodedEvent(IBlock block) {
			super(block);
		}
	}

	@Cancelable
	@EventName(EnumScriptType.HARVESTED)
	public static class HarvestedEvent extends BlockEvent {
		public IPlayer<?> player;

		public HarvestedEvent(IBlock block, EntityPlayer player) {
			super(block);
			this.player = (IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player);
		}
	}

	@EventName(EnumScriptType.INIT)
	public static class InitEvent extends BlockEvent {
		public InitEvent(IBlock block) {
			super(block);
		}
	}

	@Cancelable
	@EventName(EnumScriptType.INTERACT)
	public static class InteractEvent extends BlockEvent {
		public float hitX;
		public float hitY;
		public float hitZ;
		public IPlayer<?> player;
		public int side;

		public InteractEvent(IBlock block, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
			super(block);
			this.player = (IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player);
			this.hitX = hitX;
			this.hitY = hitY;
			this.hitZ = hitZ;
			this.side = side;
		}
	}

	@EventName(EnumScriptType.NEIGHBOR_CHANGED)
	public static class NeighborChangedEvent extends BlockEvent {
		public IPos changedPos;

		public NeighborChangedEvent(IBlock block, IPos changedPos) {
			super(block);
			this.changedPos = changedPos;
		}
	}

	@EventName(EnumScriptType.RAIN_FILLED)
	public static class RainFillEvent extends BlockEvent {
		public RainFillEvent(IBlock block) {
			super(block);
		}
	}

	@EventName(EnumScriptType.REDSTONE)
	public static class RedstoneEvent extends BlockEvent {
		public int power;
		public int prevPower;

		public RedstoneEvent(IBlock block, int prevPower, int power) {
			super(block);
			this.power = power;
			this.prevPower = prevPower;
		}
	}

	@EventName(EnumScriptType.TIMER)
	public static class TimerEvent extends BlockEvent {
		public int id;

		public TimerEvent(IBlock block, int id) {
			super(block);
			this.id = id;
		}
	}

	@EventName(EnumScriptType.TICK)
	public static class UpdateEvent extends BlockEvent {
		public UpdateEvent(IBlock block) {
			super(block);
		}
	}

	public IBlock block;

	public BlockEvent(IBlock block) {
		this.block = block;
	}

}
