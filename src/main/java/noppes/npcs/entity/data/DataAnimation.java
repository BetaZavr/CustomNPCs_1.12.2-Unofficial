package noppes.npcs.entity.data;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.constants.EnumAnimationType;
import noppes.npcs.entity.EntityNPCInterface;

public class DataAnimation {

	public AnimationConfig activeAnim;
	public Map<EnumAnimationType, List<AnimationConfig>> data;
	private EntityNPCInterface npc;
	private Random rnd = new Random();
	
	public DataAnimation(EntityNPCInterface npc) {
		this.npc = npc;
		this.activeAnim = null;
		this.data = Maps.<EnumAnimationType, List<AnimationConfig>>newHashMap();
		for (EnumAnimationType eat : EnumAnimationType.values()) {
			this.data.put(eat, Lists.<AnimationConfig>newArrayList());
		}
	}

	public void reset() {
		if (this.activeAnim!=null) { this.activeAnim.reset(); }
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.data.clear();
		for (int c=0; c<compound.getTagList("AllAnimations", 10).tagCount(); c++) {
			NBTTagCompound nbtCategory = compound.getTagList("AllAnimations", 10).getCompoundTagAt(c);
			int t = nbtCategory.getInteger("Category");
			if (t<0) { t *= -1; }
			t %= EnumAnimationType.values().length;
			EnumAnimationType eat = EnumAnimationType.values()[t];
			List<AnimationConfig> list = Lists.<AnimationConfig>newArrayList();
			for (int a=0; a<nbtCategory.getTagList("Animations", 10).tagCount(); a++) {
				AnimationConfig ac = new AnimationConfig(this.npc, t);
				ac.readFromNBT(nbtCategory.getTagList("Animations", 10).getCompoundTagAt(a));
				list.add(ac);
			}
			this.data.put(eat, list);
		}
	}

	public void writeToNBT(NBTTagCompound compound) {
		NBTTagList allAnimations = new NBTTagList();
		for (EnumAnimationType eat : this.data.keySet()) {
			NBTTagCompound nbtCategory = new NBTTagCompound();
			nbtCategory.setInteger("Category", eat.ordinal());
			NBTTagList animations = new NBTTagList();
			for (AnimationConfig ac : this.data.get(eat)) { animations.appendTag(ac.writeToNBT(new NBTTagCompound())); }
			compound.setTag("Animations", animations);
			allAnimations.appendTag(nbtCategory);
		}
		compound.setTag("AllAnimations", allAnimations);
	}

	public AnimationConfig getActive(EnumAnimationType type) {
		if (this.activeAnim!=null && this.activeAnim.type==type) { return this.activeAnim; }
		this.activeAnim = null;
		List<AnimationConfig> list = this.data.get(type);
		if (list.size()>0) {
			this.activeAnim = list.get(this.rnd.nextInt(list.size()));
		}
		if (this.activeAnim!=null) { this.activeAnim.reset(); }
		return this.activeAnim;
	}
	
}
