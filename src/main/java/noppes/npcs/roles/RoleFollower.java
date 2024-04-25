package noppes.npcs.roles;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
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
import noppes.npcs.containers.ContainerNPCFollowerHire;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerGameData.FollowerSet;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

public class RoleFollower extends RoleInterface implements IRoleFollower {

	public boolean disableGui, infiniteDays, isFollowing, refuseSoulStone;
	public int daysHired;
	public String dialogFarewell, dialogHire, dialogFired, ownerUUID;
	public long hiredTime, waitTime;

	public int rentalMoney;
	public NpcMiscInventory rentalItems;
	public NpcMiscInventory inventory;
	public EntityPlayer owner;
	public HashMap<Integer, Integer> rates;

	public RoleFollower(EntityNPCInterface npc) {
		super(npc);
		this.isFollowing = true;
		this.dialogHire = new TextComponentTranslation("follower.hireText").getFormattedText() + " {days} "
				+ new TextComponentTranslation("follower.days").getFormattedText();
		this.dialogFarewell = new TextComponentTranslation("follower.farewellText").getFormattedText() + " {player}";
		this.dialogFired = new TextComponentTranslation("follower.firedText").getFormattedText() + " {player}";
		this.disableGui = false;
		this.infiniteDays = false;
		this.refuseSoulStone = false;
		this.owner = null;
		this.rentalMoney = 0;
		this.rentalItems = new NpcMiscInventory(3);
		this.inventory = new NpcMiscInventory(0);
		this.rates = new HashMap<Integer, Integer>();
		this.type = RoleType.FOLLOWER;
		this.waitTime = 0;
	}

	@Override
	public void addDays(int days) {
		if (this.hiredTime == 0L) {
			this.daysHired = days;
			this.hiredTime = System.currentTimeMillis();
		} else {
			this.daysHired += days;
		}
	}

	@Override
	public boolean aiShouldExecute() {
		if (this.npc.getHealth() <= 0.0f) {
			return false;
		}
		if ((this.ownerUUID == null || this.ownerUUID.isEmpty())
				&& this.npc.world.provider.getDimension() != this.npc.homeDimensionId) {
			try {
				AdditionalMethods.teleportEntity(this.npc.world.getMinecraftServer(), this.npc,
						this.npc.homeDimensionId, this.npc.getStartXPos(), this.npc.getStartYPos(),
						this.npc.getStartZPos());
			} catch (CommandException e) {
				e.printStackTrace();
			}
			return false;
		}
		PlayerData plData = this.getOwnerData();
		if (plData == null) {
			if (this.ownerUUID != null && !this.ownerUUID.isEmpty()) {
				this.killed();
			}
			return false;
		}
		FollowerSet fs = null;
		if (plData != null) {
			fs = plData.game.getFollower(this.npc);
			if (fs == null) {
				fs = plData.game.addFollower(this.npc);
			}
			fs.dimId = this.npc.world.provider.getDimension();
			fs.npc = this.npc;
		}
		this.owner = this.getOwner();
		if (!this.infiniteDays && (System.currentTimeMillis() - this.hiredTime) > this.getDays() * 1440000L) {
			RoleEvent.FollowerFinishedEvent event = new RoleEvent.FollowerFinishedEvent(this.owner,
					this.npc.wrappedNPC);
			EventHooks.onNPCRole(this.npc, event);
			if (this.owner != null) {
				if (this.owner.openContainer instanceof ContainerNPCFollowerHire) {
					this.owner.closeScreen();
				}
				this.owner.sendMessage(new TextComponentTranslation(
						NoppesStringUtils.formatText(this.dialogFarewell, this.owner, this.npc)));
			}
			if (plData != null && fs != null) {
				plData.game.removeFollower(this.npc);
			}
			this.killed();
		}
		if (this.npc.getAttackTarget() != null) {
			return false;
		}
		if (!isFollowing) {
			if (!this.npc.getNavigator().noPath()) {
				this.npc.getNavigator().clearPath();
			}
			return false;
		}
		if (this.owner == null) {
			return false;
		}
		double dist = this.npc.getDistance(this.owner);
		if (this.owner.world.provider.getDimension() != this.npc.world.provider.getDimension()) {
			try {
				Entity entity = AdditionalMethods.teleportEntity(this.npc.world.getMinecraftServer(), this.npc,
						this.owner.world.provider.getDimension(), this.owner.posX, this.owner.posY, this.owner.posZ);
				if (entity instanceof EntityNPCInterface && fs != null) {
					fs.dimId = entity.world.provider.getDimension();
					fs.id = entity.getUniqueID();
					((EntityNPCInterface) entity).getNavigator().tryMoveToEntityLiving(this.owner,
							this.npc.ais.canSprint ? 1.3 : 1.0d);
				}
			} catch (CommandException e) {
				e.printStackTrace();
			}
		} else if (dist <= 2.5d) {
			if (!this.npc.getNavigator().noPath()) {
				this.npc.getNavigator().clearPath();
			}
			return false;
		} else if (dist > getRange()) {
			this.npc.setPosition(this.owner.posX, this.owner.posY, this.owner.posZ);
		} else {
			boolean bo = this.npc.getNavigator().tryMoveToEntityLiving(this.owner, this.npc.ais.canSprint ? 1.3 : 1.0d);
			if (!bo && !this.npc.isMoving()) {
				if (this.waitTime == 0) {
					this.waitTime = 10;
					return false;
				}
				this.waitTime--;
				if (this.waitTime <= 0) {
					this.npc.setPosition(this.owner.posX, this.owner.posY, this.owner.posZ);
				}
			} else {
				this.waitTime = 0;
			}
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
		int daysPassed = (int) Math.floor((double) (System.currentTimeMillis() - this.hiredTime) / 480000.0d);
		return this.daysHired - daysPassed;
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
				MinecraftServer server = null;
				if (this.npc.world != null) {
					server = this.npc.world.getMinecraftServer();
				}
				if (server == null && CustomNpcs.Server != null) {
					server = CustomNpcs.Server;
				}
				if (server != null) {
					return server.getPlayerList().getPlayerByUUID(uuid);
				}
			}
		} catch (IllegalArgumentException ex) {
		}
		return this.npc.world.getPlayerEntityByName(this.ownerUUID);
	}

	private PlayerData getOwnerData() {
		if (this.ownerUUID == null || this.ownerUUID.isEmpty() || CustomNpcs.Server == null || this.npc.world == null
				|| this.npc.world.getMinecraftServer() == null) {
			return null;
		}
		return PlayerDataController.instance.getDataFromUsername(
				CustomNpcs.Server == null ? this.npc.world.getMinecraftServer() : CustomNpcs.Server, this.ownerUUID);
	}

	public int getRange() {
		if (this.npc.stats.aggroRange > CustomNpcs.NpcNavRange) {
			return CustomNpcs.NpcNavRange;
		}
		return this.npc.stats.aggroRange;
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
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerFollowerHire, this.npc, 0, 0, 0);
		} else if (player == this.owner && !this.disableGui) {
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerFollower, this.npc, 1, 0, 0);
		}
	}

	@Override
	public boolean isFollowing() {
		return this.ownerUUID != null && !this.ownerUUID.isEmpty() && this.isFollowing && this.getDays() > 0;
	}

	@Override
	public void killed() {
		if (!this.inventory.isEmpty()) {
			if (this.owner == null) {
				for (ItemStack stack : this.inventory.items) {
					if (NoppesUtilServer.IsItemStackNull(stack) || stack.isEmpty()) {
						continue;
					}
					this.npc.entityDropItem(stack, 0.0f);
				}
			} else if (this.owner.world.provider.getDimension() == this.npc.world.provider.getDimension()) {
				for (ItemStack stack : this.inventory.items) {
					if (NoppesUtilServer.IsItemStackNull(stack) || stack.isEmpty()) {
						continue;
					}
					EntityItem entityitem = new EntityItem(this.owner.world, this.owner.posX, this.owner.posY,
							this.owner.posZ, stack);
					entityitem.setPickupDelay(0);
					this.owner.world.spawnEntity(entityitem);
				}
			}
			this.inventory.clear();
		}
		this.ownerUUID = null;
		this.daysHired = 0;
		this.hiredTime = 0L;
		this.isFollowing = true;
		PlayerData plData = this.getOwnerData();
		if (plData != null) {
			plData.game.removeFollower(this.npc);
			plData.save(true);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = RoleType.FOLLOWER;
		this.ownerUUID = compound.getString("MercenaryOwner");
		this.daysHired = compound.getInteger("MercenaryDaysHired");
		this.hiredTime = compound.getLong("MercenaryHiredTime");
		this.dialogHire = compound.getString("MercenaryDialogHired");
		this.dialogFarewell = compound.getString("MercenaryDialogFarewell");
		if (compound.hasKey("MercenaryDialogFired", 8)) {
			this.dialogFired = compound.getString("MercenaryDialogFired");
		}
		this.rates = NBTTags.getIntegerIntegerMap(compound.getTagList("MercenaryDayRates", 10));
		this.rentalItems.setFromNBT(compound.getCompoundTag("MercenaryInv"));
		if (compound.hasKey("MercenaryInventory", 10)) {
			int size = compound.getCompoundTag("MercenaryInventory").getInteger("NpcMiscInvSize");
			this.inventory = new NpcMiscInventory(size);
			this.inventory.setFromNBT(compound.getCompoundTag("MercenaryInventory"));
		}
		this.rentalMoney = compound.getInteger("MercenaryMoney");
		this.isFollowing = compound.getBoolean("MercenaryIsFollowing");
		this.disableGui = compound.getBoolean("MercenaryDisableGui");
		this.infiniteDays = compound.getBoolean("MercenaryInfiniteDays");
		this.refuseSoulStone = compound.getBoolean("MercenaryRefuseSoulstone");
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
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", RoleType.FOLLOWER.get());
		compound.setInteger("MercenaryDaysHired", this.daysHired);
		compound.setLong("MercenaryHiredTime", this.hiredTime);
		compound.setString("MercenaryDialogHired", this.dialogHire);
		compound.setString("MercenaryDialogFarewell", this.dialogFarewell);
		compound.setString("MercenaryDialogFired", this.dialogFired);
		if (this.hasOwner()) {
			compound.setString("MercenaryOwner", this.ownerUUID);
		}
		compound.setTag("MercenaryDayRates", NBTTags.nbtIntegerIntegerMap(this.rates));
		compound.setTag("MercenaryInv", this.rentalItems.getToNBT());
		compound.setTag("MercenaryInventory", this.inventory.getToNBT());
		compound.setInteger("MercenaryMoney", this.rentalMoney);
		compound.setBoolean("MercenaryIsFollowing", this.isFollowing);
		compound.setBoolean("MercenaryDisableGui", this.disableGui);
		compound.setBoolean("MercenaryInfiniteDays", this.infiniteDays);
		compound.setBoolean("MercenaryRefuseSoulstone", this.refuseSoulStone);
		return compound;
	}
}
