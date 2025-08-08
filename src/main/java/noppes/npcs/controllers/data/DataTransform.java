package noppes.npcs.controllers.data;

import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class DataTransform {

	public boolean isDay;

	public NBTTagCompound advanced;
	public NBTTagCompound ai;
	public NBTTagCompound display;
	public NBTTagCompound inv;
	public NBTTagCompound job;
	public NBTTagCompound role;
	public NBTTagCompound stats;
	public NBTTagCompound animation;

	public boolean editingModus;
	public boolean hasAdvanced;
	public boolean hasAi;
	public boolean hasDisplay;
	public boolean hasInv;
	public boolean hasJob;
	public boolean hasRole;
	public boolean hasStats;
	public boolean hasAnimations;

	private final EntityNPCInterface npc;

	public DataTransform(EntityNPCInterface npcIn) {
		editingModus = false;
		npc = npcIn;
	}

	public NBTTagCompound getDisplay() {
		NBTTagCompound compound = npc.display.writeToNBT(new NBTTagCompound());
		if (npc instanceof EntityCustomNpc) {
			compound.setTag("ModelData", ((EntityCustomNpc) npc).modelData.save());
		}
		return compound;
	}

	public boolean isValid() {
		return hasAdvanced || hasAi || hasDisplay || hasInv || hasStats || hasJob || hasRole || hasAnimations;
	}

	public NBTTagCompound processAdvanced(NBTTagCompound compoundAdv, NBTTagCompound compoundRole, NBTTagCompound compoundJob) {
		if (hasAdvanced) {
			compoundAdv = advanced;
		}
		if (hasRole) {
			compoundRole = role;
		}
		if (hasJob) {
			compoundJob = job;
		}
		Set<String> names = compoundRole.getKeySet();
		for (String name : names) {
			compoundAdv.setTag(name, compoundRole.getTag(name));
		}
		names = compoundJob.getKeySet();
		for (String name : names) {
			compoundAdv.setTag(name, compoundJob.getTag(name));
		}
		return compoundAdv;
	}

	public void readOptions(NBTTagCompound compound) {
		boolean hadDisplay = hasDisplay;
		boolean hadAI = hasAi;
		boolean hadAdvanced = hasAdvanced;
		boolean hadInv = hasInv;
		boolean hadStats = hasStats;
		boolean hadRole = hasRole;
		boolean hadJob = hasJob;
		boolean hadAnimations = hasAnimations;

		hasDisplay = compound.getBoolean("TransformHasDisplay");
		hasAi = compound.getBoolean("TransformHasAI");
		hasAdvanced = compound.getBoolean("TransformHasAdvanced");
		hasInv = compound.getBoolean("TransformHasInv");
		hasStats = compound.getBoolean("TransformHasStats");
		hasRole = compound.getBoolean("TransformHasRole");
		hasJob = compound.getBoolean("TransformHasJob");
		editingModus = compound.getBoolean("TransformEditingModus");
		hasAnimations = compound.getBoolean("TransformHasAnimations");

		if (hasDisplay && !hadDisplay) { display = getDisplay(); }
		if (hasAi && !hadAI) { ai = npc.ais.writeToNBT(new NBTTagCompound()); }
		if (hasStats && !hadStats) { stats = npc.stats.writeToNBT(new NBTTagCompound()); }
		if (hasInv && !hadInv) { inv = npc.inventory.writeEntityToNBT(new NBTTagCompound()); }
		if (hasAdvanced && !hadAdvanced) { advanced = npc.advanced.save(new NBTTagCompound()); }
		if (hasJob && !hadJob) { job = npc.advanced.jobInterface.save(new NBTTagCompound()); }
		if (hasRole && !hadRole) { role = npc.advanced.roleInterface.save(new NBTTagCompound()); }
		if (hasAnimations && !hadAnimations) { animation = npc.animation.save(new NBTTagCompound()); }
	}

	public void load(NBTTagCompound compound) {
		isDay = compound.getBoolean("TransformIsActive");
		readOptions(compound);
		display = (hasDisplay ? compound.getCompoundTag("TransformDisplay") : getDisplay());
		ai = (hasAi ? compound.getCompoundTag("TransformAI") : npc.ais.writeToNBT(new NBTTagCompound()));
		advanced = (hasAdvanced ? compound.getCompoundTag("TransformAdvanced") : npc.advanced.save(new NBTTagCompound()));
		inv = (hasInv ? compound.getCompoundTag("TransformInv") : npc.inventory.writeEntityToNBT(new NBTTagCompound()));
		stats = (hasStats ? compound.getCompoundTag("TransformStats") : npc.stats.writeToNBT(new NBTTagCompound()));
		job = (hasJob ? compound.getCompoundTag("TransformJob") : npc.advanced.jobInterface.save(new NBTTagCompound()));
		role = (hasRole ? compound.getCompoundTag("TransformRole") : npc.advanced.roleInterface.save(new NBTTagCompound()));
		animation = (hasAnimations ? compound.getCompoundTag("TransformAnimations") : npc.animation.save(new NBTTagCompound()));
	}

	public void transform(boolean isDayIn) {
		if (isDay == isDayIn) { return; }

		if (hasDisplay) {
			NBTTagCompound compound = getDisplay();
			npc.display.readToNBT(NBTTags.NBTMerge(compound, display));
			if (npc instanceof EntityCustomNpc) {
				((EntityCustomNpc) npc).modelData.load(NBTTags.NBTMerge(compound.getCompoundTag("ModelData"), display.getCompoundTag("ModelData")));
			}
			display = compound;
		}
		if (hasStats) {
			NBTTagCompound compound = npc.stats.writeToNBT(new NBTTagCompound());
			npc.stats.readToNBT(NBTTags.NBTMerge(compound, stats));
			stats = compound;
		}
		if (hasAdvanced || hasJob || hasRole) {
			NBTTagCompound compoundAdv = npc.advanced.save(new NBTTagCompound());
			NBTTagCompound compoundRole = npc.advanced.roleInterface.save(new NBTTagCompound());
			NBTTagCompound compoundJob = npc.advanced.jobInterface.save(new NBTTagCompound());
			NBTTagCompound compound2 = processAdvanced(compoundAdv, compoundRole, compoundJob);
			npc.advanced.load(compound2);
			npc.advanced.roleInterface.load(NBTTags.NBTMerge(compoundRole, compound2));
			npc.advanced.jobInterface.load(NBTTags.NBTMerge(compoundJob, compound2));
			if (hasAdvanced) { advanced = compoundAdv; }
			if (hasRole) { role = compoundRole; }
			if (hasJob) { job = compoundJob; }
		}
		if (hasAi) {
			NBTTagCompound compound = npc.ais.writeToNBT(new NBTTagCompound());
			npc.ais.readToNBT(NBTTags.NBTMerge(compound, ai));
			ai = compound;
			npc.setCurrentAnimation(npc.ais.animationType);
		}
		if (hasInv) {
			NBTTagCompound compound = npc.inventory.writeEntityToNBT(new NBTTagCompound());
			npc.inventory.readEntityFromNBT(NBTTags.NBTMerge(compound, inv));
			inv = compound;
		}
		if (hasAnimations) {
			NBTTagCompound compound = npc.animation.save(new NBTTagCompound());
			npc.animation.load(NBTTags.NBTMerge(compound, animation));
			animation = compound;
		}
		npc.updateAI = true;
		isDay = isDayIn;
		npc.updateClient = true;
	}

	public NBTTagCompound writeOptions(NBTTagCompound compound) {
		compound.setBoolean("TransformHasDisplay", hasDisplay);
		compound.setBoolean("TransformHasAI", hasAi);
		compound.setBoolean("TransformHasAdvanced", hasAdvanced);
		compound.setBoolean("TransformHasInv", hasInv);
		compound.setBoolean("TransformHasStats", hasStats);
		compound.setBoolean("TransformHasRole", hasRole);
		compound.setBoolean("TransformHasJob", hasJob);
		compound.setBoolean("TransformEditingModus", editingModus);
		compound.setBoolean("TransformHasAnimations", hasAnimations);
		return compound;
	}

	public NBTTagCompound save(NBTTagCompound compound) {
		compound.setBoolean("TransformIsActive", isDay);
		writeOptions(compound);
		if (hasDisplay) { compound.setTag("TransformDisplay", display); }
		if (hasAi) { compound.setTag("TransformAI", ai); }
		if (hasAdvanced) { compound.setTag("TransformAdvanced", advanced); }
		if (hasInv) { compound.setTag("TransformInv", inv); }
		if (hasStats) { compound.setTag("TransformStats", stats); }
		if (hasRole) { compound.setTag("TransformRole", role); }
		if (hasJob) { compound.setTag("TransformJob", job); }
		if (hasAnimations) { compound.setTag("TransformAnimations", animation); }
		return compound;
	}

}
