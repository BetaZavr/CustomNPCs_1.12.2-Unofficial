package noppes.npcs.client.model.animation;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.data.IAnimationFrame;
import noppes.npcs.api.entity.data.IAnimationPart;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.constants.EnumParts;

public class AnimationFrameConfig implements IAnimationFrame {

	public static final AnimationFrameConfig EMPTY_PART;
	static {
		EMPTY_PART = new AnimationFrameConfig();
		for (PartConfig p : EMPTY_PART.parts.values()) { p.disable = true; }
	}

	public boolean smooth, isNowDamage, showMainHand = true, showOffHand = true, showHelmet = true, showBody = true, showLegs = true, showFeets = true;
	public int speed = 10;
	public int delay = 0;
	public int id = -1;
	private int holdRightType = 0, holdLeftType = 0;
	private IItemStack holdRightStack = ItemStackWrapper.AIR, holdLeftStack = ItemStackWrapper.AIR;

	/* 0:head
	 * 1:left arm
	 * 2:right arm
	 * 3:body
	 * 4:left leg
	 * 5:right leg
	 * 6:left stack
	 * 7:right stack
	 */
	public final Map<Integer, PartConfig> parts = Maps.newTreeMap();
	public ResourceLocation sound = null;
	public int emotionId = -1;

    public AnimationFrameConfig(int id) {
		this();
		this.id = id;
	}

	public AnimationFrameConfig() {
        for (int i = 0; i < 8; i++) {
			PartConfig pc = new PartConfig(i, AnimationFrameConfig.getPartType(i));
			this.parts.put(i, pc);
		}
		this.clear();
	}

	public void clear() {
		this.smooth = false;
		this.speed = 10;
		this.delay = 0;
	}

	public AnimationFrameConfig copy() {
		AnimationFrameConfig newAfc = new AnimationFrameConfig();
		newAfc.readNBT(this.writeNBT());
		return newAfc;
	}

	private void fixParts() {
		int i = 0;
		Map<Integer, PartConfig> newParts = Maps.newTreeMap();
		boolean change = false;
		for (Integer id : this.parts.keySet()) {
			PartConfig ps = this.parts.get(id);
			if (id != i || ps.id != i) {
				change = true;
			}
			ps.id = i;
			newParts.put(i, ps);
			i++;
		}
		if (change) {
			this.parts.clear();
			this.parts.putAll(newParts);
			for (int p = 0; p < 8; p++) {
				if (!this.parts.containsKey(p)) {
					this.parts.put(p, new PartConfig(p, AnimationFrameConfig.getPartType(p)));
				}
			}
		}
	}

	@Override
	public int getEndDelay() {
		if (this.delay < 0) {
			this.delay *= -1;
		}
		if (this.delay > 1200) {
			this.delay = -1200;
		}
		return this.delay;
	}

	@Override
	public IAnimationPart getPart(int id) {
		if (id < 0) {
			id *= -1;
		}
		if (id > this.parts.size()) {
			id %= this.parts.size();
		}
		return this.parts.get(id);
	}

	@Override
	public int getSpeed() {
		if (this.speed < 0) {
			this.speed *= -1;
		}
		if (this.speed > 1200) {
			this.speed = 1200;
		}
		return this.speed;
	}

	@Override
	public boolean isSmooth() {
		return this.smooth;
	}

	public void readNBT(NBTTagCompound compound) {
		this.id = compound.getInteger("ID");
		this.setSmooth(compound.getBoolean("IsSmooth"));
		this.setSpeed(compound.getInteger("Speed"));
		this.setEndDelay(compound.getInteger("EndDelay"));
		if (compound.hasKey("StartSound", 8)) { this.setStartSound(compound.getString("StartSound")); }
		if (compound.hasKey("EmotionID", 3)) { this.setStartEmotion(compound.getInteger("EmotionID")); }
		if (compound.hasKey("IsNowDamage", 1)) { this.isNowDamage = compound.getBoolean("IsNowDamage"); }
		if (compound.hasKey("ShowStacks", 7)) {
			byte[] array = compound.getByteArray("ShowStacks");
			this.showMainHand = array.length == 0 || array[0] != (byte) 0;
			this.showOffHand = array.length < 1 || array[1] != (byte) 0;
			this.showHelmet = array.length < 2 || array[2] != (byte) 0;
			this.showBody = array.length < 3 || array[3] != (byte) 0;
			this.showLegs = array.length < 4 || array[4] != (byte) 0;
			this.showFeets = array.length < 5 || array[5] != (byte) 0;
		}

		this.setHoldRightStackType(compound.getInteger("HoldRightType"));
		this.setHoldLeftStackType(compound.getInteger("HoldLeftType"));
		NpcAPI api = NpcAPI.Instance();
		if (compound.hasKey("HoldRightStack", 10)) {
            assert api != null;
            this.setHoldRightStack(api.getIItemStack(new ItemStack(compound.getCompoundTag("HoldRightStack")))); }
		if (compound.hasKey("HoldLeftStack", 10)) {
            assert api != null;
            this.setHoldLeftStack(api.getIItemStack(new ItemStack(compound.getCompoundTag("HoldLeftStack")))); }

		this.parts.clear();
		for (int i = 0; i < compound.getTagList("PartConfigs", 10).tagCount(); i++) {
			NBTTagCompound nbt = compound.getTagList("PartConfigs", 10).getCompoundTagAt(i);
			PartConfig pc;
			if (nbt.hasKey("Part", 3) && this.parts.containsKey(nbt.getInteger("Part"))) { pc = this.parts.get(nbt.getInteger("Part")); }
			else { pc = new PartConfig(i, AnimationFrameConfig.getPartType(i)); }
			pc.readNBT(nbt);
			if (pc.type == EnumParts.WRIST_RIGHT || pc.type == EnumParts.WRIST_LEFT || pc.type == EnumParts.FOOT_RIGHT || pc.type == EnumParts.FOOT_LEFT) { continue; }
			this.parts.put(pc.id, pc);
		}
		for (int p = 0; p < 8; p++) {
			if (!this.parts.containsKey(p)) {
				this.parts.put(p, new PartConfig(p, AnimationFrameConfig.getPartType(p)));
			}
		}
		fixParts();
	}

	public static EnumParts getPartType(int id) {
		switch(id) {
			case 0: return EnumParts.HEAD;
			case 1: return EnumParts.ARM_LEFT;
			case 2: return EnumParts.ARM_RIGHT;
			case 3: return EnumParts.BODY;
			case 4: return EnumParts.LEG_LEFT;
			case 5: return EnumParts.LEG_RIGHT;
			case 6: return EnumParts.LEFT_STACK;
			case 7: return EnumParts.RIGHT_STACK;
			default: return EnumParts.CUSTOM;
		}
	}

	public void removePart(PartConfig part) {
		if (part == null || this.parts.size() <= 8) {
			return;
		}
		for (Integer id : this.parts.keySet()) {
			PartConfig p = this.parts.get(id);
			if (p.equals(part) || p.id == part.id) {
				this.parts.remove(id);
				fixParts();
				return;
			}
		}
	}

	@Override
	public void setEndDelay(int ticks) {
		if (ticks < 0) {
			ticks *= -1;
		}
		if (ticks > 1200) {
			ticks = 1200;
		}
		this.delay = ticks;
	}

	@Override
	public void setSmooth(boolean isSmooth) {
		this.smooth = isSmooth;
	}

	@Override
	public void setSpeed(int ticks) {
		if (ticks < 0) {
			ticks *= -1;
		}
		if (ticks > 1200) {
			ticks = 1200;
		}
		this.speed = ticks;
	}

	public NBTTagCompound writeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("IsSmooth", this.smooth);
		compound.setBoolean("IsNowDamage", this.isNowDamage);
		compound.setInteger("ID", this.id);
		compound.setInteger("Speed", this.speed);
		compound.setInteger("EndDelay", this.delay);
		NBTTagList list = new NBTTagList();
		for (int id : this.parts.keySet()) {
			list.appendTag(this.parts.get(id).writeNBT());
		}
		compound.setTag("PartConfigs", list);
		compound.setString("StartSound", this.getStartSound());
		compound.setInteger("EmotionID", this.emotionId);
        compound.setInteger("Version", 1);
		compound.setInteger("HoldRightType", this.holdRightType);
		compound.setInteger("HoldLeftType", this.holdLeftType);
		compound.setTag("HoldRightStack", this.holdRightStack.getMCItemStack().writeToNBT(new NBTTagCompound()));
		compound.setTag("HoldLeftStack", this.holdLeftStack.getMCItemStack().writeToNBT(new NBTTagCompound()));
		compound.setByteArray("ShowStacks", new byte[] { (byte) (showMainHand ? 1 : 0), (byte) (showOffHand ? 1 : 0), (byte) (showHelmet ? 1 : 0), (byte) (showBody ? 1 : 0), (byte) (showLegs ? 1 : 0), (byte) (showFeets ? 1 : 0) });
		return compound;
	}

	@Override
	public String getStartSound() { return this.sound == null ? "" : this.sound.toString(); }

	@Override
	public void setStartSound(String sound) {
		if (sound == null || sound.isEmpty()) { this.sound = null; }
		if (sound != null) {
			this.sound = new ResourceLocation(sound);
			if (this.sound.getResourcePath().isEmpty() || this.sound.getResourceDomain().isEmpty()) {
				this.sound = null;
			}
		}
	}

	public void setStartSound(ResourceLocation resource) { this.sound = resource; }

	@Override
	public int getStartEmotion() { return emotionId; }

	@Override
	public void setStartEmotion(int id) { this.emotionId = id; }

	public void setRotationAngles(ModelBiped modelNpcAlt) {
		for (int partId : parts.keySet()) {
			ModelRenderer biped = null;
			switch(partId) {
				case 0: biped = modelNpcAlt.bipedHead; break;
				case 1: biped = modelNpcAlt.bipedLeftArm; break;
				case 2: biped = modelNpcAlt.bipedRightArm; break;
				case 3: biped = modelNpcAlt.bipedBody; break;
				case 4: biped = modelNpcAlt.bipedLeftLeg; break;
				case 5: biped = modelNpcAlt.bipedRightLeg; break;
			}
			if (biped == null || !biped.showModel) { continue; }
			PartConfig part = parts.get(partId);
			part.rotation[0] = 0.025330f * (float) Math.pow(biped.rotateAngleX, 2.0d) + 0.238732f * biped.rotateAngleX + 0.5f;
			part.rotation[1] = 0.025330f * (float) Math.pow(biped.rotateAngleY, 2.0d) + 0.238732f * biped.rotateAngleY + 0.5f;
			part.rotation[2] = 0.025330f * (float) Math.pow(biped.rotateAngleZ, 2.0d) + 0.238732f * biped.rotateAngleZ + 0.5f;
			for (int i = 0; i < 3; i++) {
				part.scale[i] = 0.2f;
				part.offset[i] = 0.5f;
			}
		}
	}

	@Override
	public boolean isNowDamage() { return this.isNowDamage; }

	@Override
	public int getHoldRightStackType() { return this.holdRightType; }

	@Override
	public int getHoldLeftStackType() { return this.holdLeftType; }

	@Override
	public IItemStack getHoldRightStack() { return this.holdRightStack; }

	@Override
	public IItemStack getHoldLeftStack() { return this.holdLeftStack; }

	@Override
	public void setHoldRightStackType(int type) {
		if (type < 0) { type *= -1; }
		this.holdRightType = type % 8;
	}

	@Override
	public void setHoldLeftStackType(int type) {
		if (type < 0) { type *= -1; }
		this.holdLeftType = type % 8;
	}

	@Override
	public void setHoldRightStack(IItemStack stack) {
		if (stack == null) { stack = ItemStackWrapper.AIR; }
		this.holdRightStack = stack;
	}

	@Override
	public void setHoldLeftStack(IItemStack stack) {
		if (stack == null) { stack = ItemStackWrapper.AIR; }
		this.holdLeftStack = stack;
	}

	public void setHoldRightStack(ItemStack stack) {
		if (stack == null) { stack = ItemStack.EMPTY; }
		this.holdRightStack = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(stack);
	}

	public void setHoldLeftStack(ItemStack stack) {
		if (stack == null) { stack = ItemStack.EMPTY; }
		this.holdLeftStack = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(stack);
	}

}
