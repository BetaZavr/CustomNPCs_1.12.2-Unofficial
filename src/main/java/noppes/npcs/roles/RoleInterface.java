package noppes.npcs.roles;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.entity.data.INPCRole;
import noppes.npcs.entity.EntityNPCInterface;

public abstract class RoleInterface
implements INPCRole {
	
	public HashMap<String, String> dataString;
	public EntityNPCInterface npc;

	public RoleInterface(EntityNPCInterface npc) {
		this.dataString = new HashMap<String, String>();
		this.npc = npc;
	}

	public boolean aiContinueExecute() {
		return false;
	}

	public boolean aiShouldExecute() {
		return false;
	}

	public void aiStartExecuting() {
	}

	public void aiUpdateTask() {
	}
	
	public void aiDeathExecute(Entity attackingEntity) {
	}

	public void clientUpdate() {
	}

	public boolean defendOwner() {
		return false;
	}

	public void delete() {
	}

	@Override
	public int getType() {
		return this.npc.advanced.role;
	}

	public abstract void interact(EntityPlayer p0);

	public boolean isFollowing() {
		return false;
	}

	public void killed() {
	}

	public abstract void readFromNBT(NBTTagCompound p0);

	public abstract NBTTagCompound writeToNBT(NBTTagCompound p0);

}
