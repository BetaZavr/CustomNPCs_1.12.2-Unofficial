package noppes.npcs.roles;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.data.INPCJob;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.entity.EntityNPCInterface;

public class JobInterface
implements INPCJob {
	
	public EntityNPCInterface npc;
	public boolean overrideMainHand;
	public boolean overrideOffHand;
	public JobType type;

	public JobInterface(EntityNPCInterface npc) {
		this.overrideMainHand = false;
		this.overrideOffHand = false;
		this.npc = npc;
		this.type = JobType.DEFAULT;
	}

	public boolean aiContinueExecute() {
		return this.aiShouldExecute();
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

	public void delete() {
	}

	public IItemStack getMainhand() {
		return null;
	}

	public int getMutexBits() {
		return 0;
	}

	public IItemStack getOffhand() {
		return null;
	}

	@Override
	public int getType() { return this.type.get(); }

	public boolean isFollowing() {
		return false;
	}

	public String itemToString(ItemStack item) {
		if (item == null || item.isEmpty()) { return ""; }
		return Item.REGISTRY.getNameForObject(item.getItem()) + " - " + item.getItemDamage();
	}

	public void killed() {
	}

	public void reset() {
	}

	public void resetTask() {
	}

	public ItemStack stringToItem(String s) {
		if (s.isEmpty()) { return ItemStack.EMPTY; }
		int damage = 0;
		if (s.contains(" - ")) {
			String[] split = s.split(" - ");
			if (split.length == 2) {
				try {
					damage = Integer.parseInt(split[1]);
				} catch (NumberFormatException ex) {
				}
				s = split[0];
			}
		}
		Item item = Item.getByNameOrId(s);
		if (item == null) {
			return ItemStack.EMPTY;
		}
		return new ItemStack(item, 1, damage);
	}

	public JobType getEnumType() { return this.type; }
	
	public void readFromNBT(NBTTagCompound compound) {
		this.type = JobType.get(compound.getInteger("Type"));
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", this.type.get());
		return compound;
	}
}
