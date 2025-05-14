package noppes.npcs.controllers.data;

import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class DataTransform {

	public NBTTagCompound advanced;
	public NBTTagCompound ai;
	public NBTTagCompound display;
	public boolean editingModus;
	public boolean hasAdvanced;
	public boolean hasAi;
	public boolean hasDisplay;
	public boolean hasInv;
	public boolean hasJob;
	public boolean hasRole;
	public boolean hasStats;
	public NBTTagCompound inv;
	public NBTTagCompound job;
	private final EntityNPCInterface npc;
	public NBTTagCompound role;
	public NBTTagCompound stats;
	public boolean isDay;

	public DataTransform(EntityNPCInterface npc) {
		this.editingModus = false;
		this.npc = npc;
	}

	public NBTTagCompound getAdvanced() {
		return this.npc.advanced.writeToNBT(new NBTTagCompound());
	}

	public NBTTagCompound getDisplay() {
		NBTTagCompound compound = this.npc.display.writeToNBT(new NBTTagCompound());
		if (this.npc instanceof EntityCustomNpc) {
			compound.setTag("ModelData", ((EntityCustomNpc) this.npc).modelData.save());
		}
		return compound;
	}

	public NBTTagCompound getJob() {
		NBTTagCompound compound = new NBTTagCompound();
		this.npc.advanced.jobInterface.writeToNBT(compound);
		return compound;
	}

	public NBTTagCompound getRole() {
		NBTTagCompound compound = new NBTTagCompound();
		this.npc.advanced.roleInterface.writeToNBT(compound);
		return compound;
	}

	public boolean isValid() {
		return this.hasAdvanced || this.hasAi || this.hasDisplay || this.hasInv || this.hasStats || this.hasJob
				|| this.hasRole;
	}

	public NBTTagCompound processAdvanced(NBTTagCompound compoundAdv, NBTTagCompound compoundRole,
			NBTTagCompound compoundJob) {
		if (this.hasAdvanced) {
			compoundAdv = this.advanced;
		}
		if (this.hasRole) {
			compoundRole = this.role;
		}
		if (this.hasJob) {
			compoundJob = this.job;
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
		boolean hadDisplay = this.hasDisplay;
		boolean hadAI = this.hasAi;
		boolean hadAdvanced = this.hasAdvanced;
		boolean hadInv = this.hasInv;
		boolean hadStats = this.hasStats;
		boolean hadRole = this.hasRole;
		boolean hadJob = this.hasJob;
		this.hasDisplay = compound.getBoolean("TransformHasDisplay");
		this.hasAi = compound.getBoolean("TransformHasAI");
		this.hasAdvanced = compound.getBoolean("TransformHasAdvanced");
		this.hasInv = compound.getBoolean("TransformHasInv");
		this.hasStats = compound.getBoolean("TransformHasStats");
		this.hasRole = compound.getBoolean("TransformHasRole");
		this.hasJob = compound.getBoolean("TransformHasJob");
		this.editingModus = compound.getBoolean("TransformEditingModus");
		if (this.hasDisplay && !hadDisplay) {
			this.display = this.getDisplay();
		}
		if (this.hasAi && !hadAI) {
			this.ai = this.npc.ais.writeToNBT(new NBTTagCompound());
		}
		if (this.hasStats && !hadStats) {
			this.stats = this.npc.stats.writeToNBT(new NBTTagCompound());
		}
		if (this.hasInv && !hadInv) {
			this.inv = this.npc.inventory.writeEntityToNBT(new NBTTagCompound());
		}
		if (this.hasAdvanced && !hadAdvanced) {
			this.advanced = getAdvanced();
		}
		if (this.hasJob && !hadJob) {
			this.job = this.getJob();
		}
		if (this.hasRole && !hadRole) {
			this.role = this.getRole();
		}
	}

	public void readToNBT(NBTTagCompound compound) {
		this.isDay = compound.getBoolean("TransformIsActive");
		this.readOptions(compound);
		this.display = (this.hasDisplay ? compound.getCompoundTag("TransformDisplay") : this.getDisplay());
		this.ai = (this.hasAi ? compound.getCompoundTag("TransformAI") : this.npc.ais.writeToNBT(new NBTTagCompound()));
		this.advanced = (this.hasAdvanced ? compound.getCompoundTag("TransformAdvanced") : this.getAdvanced());
		this.inv = (this.hasInv ? compound.getCompoundTag("TransformInv")
				: this.npc.inventory.writeEntityToNBT(new NBTTagCompound()));
		this.stats = (this.hasStats ? compound.getCompoundTag("TransformStats")
				: this.npc.stats.writeToNBT(new NBTTagCompound()));
		this.job = (this.hasJob ? compound.getCompoundTag("TransformJob") : this.getJob());
		this.role = (this.hasRole ? compound.getCompoundTag("TransformRole") : this.getRole());
	}

	public void transform(boolean isDayIn) {
		if (isDay == isDayIn) { return; }

		if (hasDisplay) {
			NBTTagCompound compound = getDisplay();
			npc.display.readToNBT(NBTTags.NBTMerge(compound, this.display));
			if (npc instanceof EntityCustomNpc) {
				((EntityCustomNpc) npc).modelData.load(NBTTags.NBTMerge(compound.getCompoundTag("ModelData"), display.getCompoundTag("ModelData")));
			}
			display = compound;
		}
		if (hasStats) {
			NBTTagCompound compound = this.npc.stats.writeToNBT(new NBTTagCompound());
			npc.stats.readToNBT(NBTTags.NBTMerge(compound, this.stats));
			stats = compound;
		}
		if (hasAdvanced || hasJob || hasRole) {
			NBTTagCompound compoundAdv = getAdvanced();
			NBTTagCompound compoundRole = getRole();
			NBTTagCompound compoundJob = getJob();
			NBTTagCompound compound2 = processAdvanced(compoundAdv, compoundRole, compoundJob);
			npc.advanced.readToNBT(compound2);
			npc.advanced.roleInterface.readFromNBT(NBTTags.NBTMerge(compoundRole, compound2));
			npc.advanced.jobInterface.readFromNBT(NBTTags.NBTMerge(compoundJob, compound2));
			if (hasAdvanced) { advanced = compoundAdv; }
			if (hasRole) { role = compoundRole; }
			if (hasJob) { job = compoundJob; }
		}
		if (hasAi) {
			NBTTagCompound compound = npc.ais.writeToNBT(new NBTTagCompound());
			npc.ais.readToNBT(NBTTags.NBTMerge(compound, ai));
			ai = compound;
			npc.setCurrentAnimation(this.npc.ais.animationType);
		}
		if (hasInv) {
			NBTTagCompound compound = this.npc.inventory.writeEntityToNBT(new NBTTagCompound());
			npc.inventory.readEntityFromNBT(NBTTags.NBTMerge(compound, inv));
			inv = compound;
		}
		npc.updateAI = true;
		isDay = isDayIn;
		npc.updateClient = true;
	}

	public Object writeOptions(NBTTagCompound compound) {
		compound.setBoolean("TransformHasDisplay", this.hasDisplay);
		compound.setBoolean("TransformHasAI", this.hasAi);
		compound.setBoolean("TransformHasAdvanced", this.hasAdvanced);
		compound.setBoolean("TransformHasInv", this.hasInv);
		compound.setBoolean("TransformHasStats", this.hasStats);
		compound.setBoolean("TransformHasRole", this.hasRole);
		compound.setBoolean("TransformHasJob", this.hasJob);
		compound.setBoolean("TransformEditingModus", this.editingModus);
		return compound;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("TransformIsActive", isDay);
		writeOptions(compound);
		if (hasDisplay) { compound.setTag("TransformDisplay", display); }
		if (hasAi) { compound.setTag("TransformAI", ai); }
		if (hasAdvanced) { compound.setTag("TransformAdvanced", advanced); }
		if (hasInv) { compound.setTag("TransformInv", inv); }
		if (hasStats) { compound.setTag("TransformStats", stats); }
		if (hasRole) { compound.setTag("TransformRole", role); }
		if (hasJob) { compound.setTag("TransformJob", job); }
		return compound;
	}
}
