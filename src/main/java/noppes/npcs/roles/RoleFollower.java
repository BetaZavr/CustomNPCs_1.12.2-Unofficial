package noppes.npcs.roles;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.role.IRoleFollower;
import noppes.npcs.api.event.RoleEvent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

public class RoleFollower
extends RoleInterface
implements IRoleFollower {
	
	public int daysHired;
	public String dialogFarewell;
	public String dialogHire;
	public boolean disableGui;
	public long hiredTime;
	public boolean infiniteDays;
	public NpcMiscInventory inventory;
	public boolean isFollowing;
	public EntityPlayer owner;
	private String ownerUUID;
	public HashMap<Integer, Integer> rates;
	public boolean refuseSoulStone;

	public RoleFollower(EntityNPCInterface npc) {
		super(npc);
		this.isFollowing = true;
		this.dialogHire = new TextComponentTranslation("follower.hireText").getFormattedText() + " {days} " + new TextComponentTranslation("follower.days").getFormattedText();
		this.dialogFarewell = new TextComponentTranslation("follower.farewellText").getFormattedText() + " {player}";
		this.disableGui = false;
		this.infiniteDays = false;
		this.refuseSoulStone = false;
		this.owner = null;
		this.inventory = new NpcMiscInventory(3);
		this.rates = new HashMap<Integer, Integer>();
		this.type = RoleType.FOLLOWER;
	}

	@Override
	public void addDays(int days) {
		this.daysHired = days + this.getDays();
		this.hiredTime = this.npc.world.getTotalWorldTime();
	}

	@Override
	public boolean aiShouldExecute() {
		this.owner = this.getOwner();
		if (!this.infiniteDays && this.owner != null && this.getDays() <= 0) {
			RoleEvent.FollowerFinishedEvent event = new RoleEvent.FollowerFinishedEvent(this.owner,
					this.npc.wrappedNPC);
			EventHooks.onNPCRole(this.npc, event);
			this.owner.sendMessage(new TextComponentTranslation(
					NoppesStringUtils.formatText(this.dialogFarewell, this.owner, this.npc), new Object[0]));
			this.killed();
		}
		return false;
	}

	@Override
	public boolean defendOwner() {
		return this.isFollowing() && this.npc.advanced.jobInterface.getEnumType() == JobType.GUARD;
	}

	@Override
	public void delete() {
	}

	@Override
	public int getDays() {
		if (this.infiniteDays) {
			return 100;
		}
		if (this.daysHired <= 0) {
			return 0;
		}
		int days = (int) ((this.npc.world.getTotalWorldTime() - this.hiredTime) / 24000L);
		return this.daysHired - days;
	}

	@Override
	public IPlayer<?> getFollowing() {
		EntityPlayer owner = this.getOwner();
		if (owner != null) {
			return (IPlayer<?>) NpcAPI.Instance().getIEntity(owner);
		}
		return null;
	}

	@Override
	public boolean getGuiDisabled() {
		return this.disableGui;
	}

	@Override
	public boolean getInfinite() {
		return this.infiniteDays;
	}

	public EntityPlayer getOwner() {
		if (this.ownerUUID == null || this.ownerUUID.isEmpty()) {
			return null;
		}
		try {
			UUID uuid = UUID.fromString(this.ownerUUID);
			if (uuid != null) {
				return this.npc.world.getPlayerEntityByUUID(uuid);
			}
		} catch (IllegalArgumentException ex) {
		}
		return this.npc.world.getPlayerEntityByName(this.ownerUUID);
	}

	@Override
	public boolean getRefuseSoulstone() {
		return this.refuseSoulStone;
	}

	public boolean hasOwner() {
		return (this.infiniteDays || this.daysHired > 0) && this.ownerUUID != null && !this.ownerUUID.isEmpty();
	}

	@Override
	public void interact(EntityPlayer player) {
		if (this.ownerUUID == null || this.ownerUUID.isEmpty()) {
			this.npc.say(player, this.npc.advanced.getInteractLine());
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerFollowerHire, this.npc);
		} else if (player == this.owner && !this.disableGui) {
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerFollower, this.npc);
		}
	}

	@Override
	public boolean isFollowing() {
		return this.owner != null && this.isFollowing && this.getDays() > 0;
	}

	@Override
	public void killed() {
		this.ownerUUID = null;
		this.daysHired = 0;
		this.hiredTime = 0L;
		this.isFollowing = true;
	}

	@Override
	public void reset() {
		this.killed();
	}

	@Override
	public void setFollowing(IPlayer<?> player) {
		if (player == null) {
			this.setOwner(null);
		} else {
			this.setOwner(player.getMCEntity());
		}
	}

	@Override
	public void setGuiDisabled(boolean disabled) {
		this.disableGui = disabled;
	}

	@Override
	public void setInfinite(boolean infinite) {
		this.infiniteDays = infinite;
	}

	public void setOwner(EntityPlayer player) {
		UUID id = player.getUniqueID();
		if (this.ownerUUID == null || id == null || !this.ownerUUID.equals(id.toString())) {
			this.killed();
		}
		this.ownerUUID = id.toString();
	}

	@Override
	public void setRefuseSoulstone(boolean refuse) {
		this.refuseSoulStone = refuse;
	}


	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = RoleType.FOLLOWER;
		this.ownerUUID = compound.getString("MercenaryOwner");
		this.daysHired = compound.getInteger("MercenaryDaysHired");
		this.hiredTime = compound.getLong("MercenaryHiredTime");
		this.dialogHire = compound.getString("MercenaryDialogHired");
		this.dialogFarewell = compound.getString("MercenaryDialogFarewell");
		this.rates = NBTTags.getIntegerIntegerMap(compound.getTagList("MercenaryDayRates", 10));
		this.inventory.setFromNBT(compound.getCompoundTag("MercenaryInv"));
		this.isFollowing = compound.getBoolean("MercenaryIsFollowing");
		this.disableGui = compound.getBoolean("MercenaryDisableGui");
		this.infiniteDays = compound.getBoolean("MercenaryInfiniteDays");
		this.refuseSoulStone = compound.getBoolean("MercenaryRefuseSoulstone");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", RoleType.FOLLOWER.get());
		compound.setInteger("MercenaryDaysHired", this.daysHired);
		compound.setLong("MercenaryHiredTime", this.hiredTime);
		compound.setString("MercenaryDialogHired", this.dialogHire);
		compound.setString("MercenaryDialogFarewell", this.dialogFarewell);
		if (this.hasOwner()) { compound.setString("MercenaryOwner", this.ownerUUID); }
		compound.setTag("MercenaryDayRates", NBTTags.nbtIntegerIntegerMap(this.rates));
		compound.setTag("MercenaryInv", this.inventory.getToNBT());
		compound.setBoolean("MercenaryIsFollowing", this.isFollowing);
		compound.setBoolean("MercenaryDisableGui", this.disableGui);
		compound.setBoolean("MercenaryInfiniteDays", this.infiniteDays);
		compound.setBoolean("MercenaryRefuseSoulstone", this.refuseSoulStone);
		return compound;
	}
}
