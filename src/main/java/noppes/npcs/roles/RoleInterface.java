package noppes.npcs.roles;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.entity.data.INPCRole;
import noppes.npcs.entity.EntityNPCInterface;

public class RoleInterface
implements INPCRole {
	
	public HashMap<String, String> dataString;
	public EntityNPCInterface npc;
	public RoleType type;

	public RoleInterface(EntityNPCInterface npc) {
		this.dataString = new HashMap<String, String>();
		this.npc = npc;
		this.type = RoleType.DEFAULT;
	}

	public boolean aiContinueExecute() { return false; }

	public boolean aiShouldExecute() { return false; }

	public void aiStartExecuting() { }

	public void aiUpdateTask() { }
	
	public void aiDeathExecute(Entity attackingEntity) { }

	public void clientUpdate() { }

	public boolean defendOwner() { return false; }

	public void delete() { }

	@Override
	public int getType() { return this.type.get(); }

	public void interact(EntityPlayer player) { };

	public boolean isFollowing() { return false; }

	public void killed() { }

	public RoleType getEnumType() { return this.type; }

	public void readFromNBT(NBTTagCompound compound) {
		this.type = RoleType.get(compound.getInteger("Type"));
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", this.type.get());
		return compound;
	}

}
