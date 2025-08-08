package noppes.npcs.roles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.entity.data.INPCRole;
import noppes.npcs.entity.EntityNPCInterface;

public class RoleInterface implements INPCRole {

	public EntityNPCInterface npc;
	public RoleType type = RoleType.DEFAULT;

	public RoleInterface(EntityNPCInterface npc) {
		this.npc = npc;
	}

	public boolean aiContinueExecute() {
		return false;
	}

	public void aiDeathExecute(Entity ignoredAttackingEntity) { }

	public boolean aiShouldExecute() {
		return false;
	}

	public void aiStartExecuting() { }

	public void aiUpdateTask() { }

	public void clientUpdate() { }

	public boolean defendOwner() {
		return true;
	}

	public void delete() { }

	public void interact(EntityPlayer player) { }

	public boolean isFollowing() {
		return false;
	}

	public void killed() { }

	// New from Unofficial (BetaZavr)
	public void load(NBTTagCompound compound) {
		type = RoleType.get(compound.getInteger("Type"));
	}

	public NBTTagCompound save(NBTTagCompound compound) {
		compound.setInteger("Type", type.get());
		return compound;
	}

	@Override
	public int getType() { return type.get(); }

	public RoleType getEnumType() { return type; }

}
