package noppes.npcs.entity.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.ability.AbstractAbility;
import noppes.npcs.constants.EnumAbilityType;
import noppes.npcs.entity.EntityNPCInterface;

public class DataAbilities {
	public List<AbstractAbility> abilities;
	public EntityNPCInterface npc;

	public DataAbilities(EntityNPCInterface npc) {
		this.abilities = new ArrayList<>();
		this.npc = npc;
	}

	public AbstractAbility getAbility(EnumAbilityType type) {
		EntityLivingBase target = this.npc.getAttackTarget();
		for (AbstractAbility ability : this.abilities) {
			if (ability.isType(type) && ability.canRun(target)) {
				return ability;
			}
		}
		return null;
	}

	public void readToNBT() {
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		return compound;
	}
}
