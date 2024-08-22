package noppes.npcs.client.model.animation;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.entity.data.IAnimationFrame;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.RayTraceRotate;
import noppes.npcs.util.RayTraceVec;
import noppes.npcs.util.ValueUtil;

public class AnimationConfig implements IAnimation {

	public static final AnimationConfig EMPTY;
	static {
		EMPTY = new AnimationConfig();
		EMPTY.frames.put(0, AnimationFrameConfig.EMPTY);
		EMPTY.resetTicks();
	}

	public String name = "Default Animation";
	public int repeatLast = 0;
	public final Map<Integer, AnimationFrameConfig> frames = Maps.newTreeMap(); // {Frame, setting Frame]}\
	// Info
	public final Map<Integer, Integer> endingFrameTicks = Maps.newTreeMap();
	public int totalTicks = 0;
	public int damageTicks;

	public int id = -1;
	public AnimationKind type = AnimationKind.STANDING;
	public float chance = 1.0f;
	public boolean immutable;
	private int damageHitboxType = 0;
	private AxisAlignedBB damageHitbox = new AxisAlignedBB(-0.5d, -0.5d, -0.5d, 0.5d, 0.5d, 0.5d); // new AxisAlignedBB(BlockPos.ORIGIN)
	public float[] offsetHitbox = new float[] { 0.0f, 0.0f, 0.0f }; // [dist, height, horizontal]
	public float[] scaleHitbox = new float[] { 1.0f, 1.0f, 1.0f }; // [x, y, z]

	public AnimationConfig() {
		this.frames.put(0, new AnimationFrameConfig(0));
	}

	@Override
	public IAnimationFrame addFrame() {
		int f = this.frames.size();
		this.frames.put(f, new AnimationFrameConfig(f));
		if (f == 0) { this.frames.get(f).isNowDamage = true; }
		return this.frames.get(f);
	}

	@Override
	public IAnimationFrame addFrame(int frameId, IAnimationFrame frame) {
		if (frame == null) { return this.addFrame(); }
		if (frameId < 0) {
			frameId = this.frames.size();
			this.frames.put(frameId, ((AnimationFrameConfig) frame).copy());
			this.frames.get(frameId).id = frameId;
        } else {
			Map<Integer, AnimationFrameConfig> newFrames = Maps.newTreeMap();
			int j = 0;
			for (int i : this.frames.keySet()) {
				if (i == frameId) {
					newFrames.put(j, ((AnimationFrameConfig) frame).copy());
					newFrames.get(j).id = j;
					j++;
				}
				newFrames.put(j, this.frames.get(i));
				newFrames.get(j).id = j;
				j++;
			}
			this.frames.clear();
			this.frames.putAll(newFrames);
        }
        this.frames.get(frameId).isNowDamage = this.frames.size() == 1;
		return this.frames.get(frameId);
	}

	public AnimationConfig copy() {
		AnimationConfig ac = new AnimationConfig();
		ac.load(this.save());
		return ac;
	}

	/** creates animation of the specified type
	   standard frames are taken into account */
	public AnimationConfig create(AnimationKind type, AnimationFrameConfig preFrame) {
		AnimationConfig ac = this.copy();
		ac.type = type;
		if (type != AnimationKind.EDITING) {
			// add standard frame to beginning
			if (!type.isQuickStart()) {
				Map<Integer, AnimationFrameConfig> newFrames = Maps.newTreeMap();
				int i = 0;
				newFrames.put(i++, preFrame);
				for (AnimationFrameConfig frame : ac.frames.values()) {
					frame.id = i;
					newFrames.put(i, frame);
					i++;
				}
				ac.frames.clear();
				ac.frames.putAll(newFrames);
			}
			// add standard frame to end
			if (ac.repeatLast == 0 && type != AnimationKind.DIES && type != AnimationKind.JUMP && type != AnimationKind.AIM) {
				ac.frames.put(ac.frames.size(), preFrame);
			}
		}
		ac.resetTicks();
		return ac;
	}

	@Override
	public IAnimationFrame getFrame(int frameId) {
		if (!this.frames.containsKey(frameId)) {
			throw new CustomNPCsException("Unknown frame " + frameId);
		}
		return this.frames.get(frameId);
	}

	@Override
	public IAnimationFrame[] getFrames() {
		IAnimationFrame[] frames = new IAnimationFrame[this.frames.size()];
		for (int id : this.frames.keySet()) {
			frames[id] = this.frames.get(id);
		}
		return frames;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public float getChance() {
		return this.chance;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public INbt getNbt() {
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(this.save());
	}

	@Override
	public int getRepeatLast() {
		return this.repeatLast;
	}

	public String getSettingName() {
		return "ID:" +  ((char) 167) + "7" + this.id + ((char) 167) + "r " + this.name;
	}

	@Override
	public boolean hasFrame(int frameId) {
		return this.frames.containsKey(frameId);
	}

	public void load(NBTTagCompound compound) {
		this.frames.clear();
		boolean hasDelayAttack = false;
		for (int i = 0; i < compound.getTagList("FrameConfigs", 10).tagCount(); i++) {
			AnimationFrameConfig afc = new AnimationFrameConfig();
			afc.readNBT(compound.getTagList("FrameConfigs", 10).getCompoundTagAt(i));
			afc.id = i;
			if (!hasDelayAttack) { hasDelayAttack = afc.isNowDamage(); }
			else { afc.isNowDamage = false; }
			this.frames.put(i, afc);
		}
		if (this.frames.isEmpty()) { this.frames.put(0, new AnimationFrameConfig(0)); }
		if (!hasDelayAttack) { this.frames.get(0).isNowDamage = true; }

		this.id = compound.getInteger("ID");
		this.name = compound.getString("Name");
		if (compound.hasKey("Chance", 5)) { this.setChance(compound.getFloat("Chance")); }
		if (compound.hasKey("Immutable", 1)) { this.immutable = compound.getBoolean("Immutable"); }
		if (compound.hasKey("Type", 3)) { this.type = AnimationKind.get(compound.getInteger("Type")); }
		if (compound.hasKey("RepeatLast", 3)) { this.setRepeatLast(compound.getInteger("RepeatLast")); }
		if (compound.hasKey("DamageHitbox", 9) && compound.getTagList("DamageHitbox", 6).tagCount() == 6) {
			NBTTagList list = compound.getTagList("DamageHitbox", 6);
			this.damageHitbox = new AxisAlignedBB(list.getDoubleAt(0), list.getDoubleAt(1), list.getDoubleAt(2), list.getDoubleAt(3), list.getDoubleAt(4), list.getDoubleAt(5));
			for (int i = 0; i < 3; i++) {
				try { this.offsetHitbox[i] = compound.getTagList("OffsetHitbox", 5).getFloatAt(i); } catch (Exception e) { LogWriter.error("Error:", e); }
				try { this.scaleHitbox[i] = ValueUtil.correctFloat(compound.getTagList("ScaleHitbox", 5).getFloatAt(i), 0.0f, Float.MAX_VALUE); } catch (Exception e) { LogWriter.error("Error:", e); }
			}
			this.setDamageHitboxType(compound.getInteger("DamageHitboxType"));
		} else {
			this.damageHitbox = new AxisAlignedBB(-0.5d, -0.5d, -0.5d, 0.5d, 0.5d, 0.5d);
			this.damageHitboxType = 0;
		}
	}

	@Override
	public void removeFrame(IAnimationFrame frameId) {
		if (frameId == null || this.frames.size() <= 1) {
			return;
		}
		for (int f : this.frames.keySet()) {
			if (this.frames.get(f).equals(frameId)) {
				this.removeFrame(f);
				return;
			}
		}
	}

	@Override
	public void removeFrame(int frameId) {
		if (!this.frames.containsKey(frameId)) {
			throw new CustomNPCsException("Unknown frame ID:" + frameId);
		}
		Map<Integer, AnimationFrameConfig> newData = Maps.newTreeMap();
		int i = 0;
		boolean isDel = false;
		for (int f : this.frames.keySet()) {
			if (f == frameId) {
				isDel = true;
				continue;
			}
			newData.put(i, this.frames.get(f).copy());
			newData.get(i).id = i;
			i++;
		}
		if (isDel) {
			this.frames.clear();
			if (newData.isEmpty()) { newData.put(0, new AnimationFrameConfig(0)); }
			this.frames.putAll(newData);
		}
	}

	@Override
	public void setName(String name) {
		if (name == null || name.isEmpty()) {
			name = "Default Animation";
		}
		this.name = name;
	}

	@Override
	public void setNbt(INbt nbt) {
		this.load(nbt.getMCNBT());
	}

	@Override
	public void setRepeatLast(int frames) {
		if (frames < 0) { frames = 0; }
		if (frames > this.frames.size()) { frames = this.frames.size(); }
		this.repeatLast = frames;
	}

	@Override
	public int getDamageHitboxType() { return this.damageHitboxType; }

	@Override
	public void setDamageHitboxType(int type) {
		if (type < 0) { type *= -1; }
		this.damageHitboxType = type % 3;
	}

	@Override
	public void setChance(float chance) {
		if (chance < 0.0f) { chance *= -1.0f; }
		if (chance > 1.0f) { chance = 1.0f; }
		this.chance = chance;
	}

	public void startToNpc(EntityCustomNpc npcEntity) {
		if (npcEntity == null || npcEntity.modelData == null || npcEntity.modelData.entityClass != null) {
			return;
		}
		npcEntity.animation.setAnimation(this, this.type);
		if (npcEntity.world == null || npcEntity.world.isRemote) {
			return;
		}
		NBTTagCompound compound = this.save();
		compound.setInteger("EntityId", npcEntity.getEntityId());
		compound.setTag("CustomAnim", this.save());
		Server.sendAssociatedData(npcEntity, EnumPacketClient.UPDATE_NPC_ANIMATION, 3, compound);
	}

	@Override
	public void startToNpc(ICustomNpc<?> npc) {
		if (npc == null || !(npc.getMCEntity() instanceof EntityCustomNpc)) {
			throw new CustomNPCsException("NPC must not be null");
		}
		this.startToNpc((EntityCustomNpc) npc.getMCEntity());
	}

	public NBTTagCompound save() {
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		Iterator<AnimationFrameConfig> setss = this.frames.values().iterator();
		while(setss.hasNext()) {
			try {
				AnimationFrameConfig afc = setss.next();
				list.appendTag(afc.writeNBT());
			}
			catch (Exception e) {
				LogWriter.error("Error:", e);
				break;
			}
		}
		compound.setTag("FrameConfigs", list);
		compound.setInteger("ID", this.id);
		compound.setInteger("Type", this.type.get());
		compound.setInteger("RepeatLast", this.repeatLast);
		compound.setString("Name", this.name);
		compound.setFloat("Chance", this.chance);
		compound.setBoolean("Immutable", this.immutable);

		NBTTagList aabb = new NBTTagList();
		aabb.appendTag(new NBTTagDouble(this.damageHitbox.minX));
		aabb.appendTag(new NBTTagDouble(this.damageHitbox.minY));
		aabb.appendTag(new NBTTagDouble(this.damageHitbox.minZ));
		aabb.appendTag(new NBTTagDouble(this.damageHitbox.maxX));
		aabb.appendTag(new NBTTagDouble(this.damageHitbox.maxY));
		aabb.appendTag(new NBTTagDouble(this.damageHitbox.maxZ));
		compound.setTag("DamageHitbox", aabb);
		compound.setInteger("DamageHitboxType", this.damageHitboxType);
		NBTTagList listOff = new NBTTagList();
		NBTTagList listSc = new NBTTagList();
		for (int i = 0; i < 3; i++) {
			listOff.appendTag(new NBTTagFloat(this.offsetHitbox[i]));
			listSc.appendTag(new NBTTagFloat(this.scaleHitbox[i]));
		}
		compound.setTag("OffsetHitbox", listOff);
		compound.setTag("ScaleHitbox", listSc);
		return compound;
	}

	public void resetTicks() {
		this.totalTicks = 0;
		this.damageTicks = 0;
		this.endingFrameTicks.clear();
		if (this == EMPTY) {
			this.totalTicks = AnimationFrameConfig.EMPTY.speed + AnimationFrameConfig.EMPTY.delay + 1;
			this.endingFrameTicks.put(0, this.totalTicks);
			return;
		}
		boolean isNowDamage = false;
		for (Integer id : this.frames.keySet()) {
			AnimationFrameConfig frame = this.frames.get(id);
			if (frame.speed < 1) { frame.speed = 1; }
			if (!isNowDamage && frame.isNowDamage()) {
				this.damageTicks += frame.speed;
				isNowDamage = true;
			} else {
				this.damageTicks += frame.speed + frame.delay;
			}
			this.totalTicks += frame.speed + frame.delay;
			this.endingFrameTicks.put(id, this.totalTicks);
		}
		this.totalTicks += 1;
	}

	public boolean hasEmotion() {
		for (AnimationFrameConfig frame : this.frames.values()) {
			if (frame.emotionId >= 0) { return true; }
		}
		return false;
	}

	public AxisAlignedBB getDamageHitbox(EntityLivingBase npc) {
		if (damageHitboxType == 0) { return null; }
		if (damageHitboxType == 1) { return new AxisAlignedBB(BlockPos.ORIGIN); }
		AxisAlignedBB aabb = new AxisAlignedBB(
				this.damageHitbox.minX * this.scaleHitbox[0], this.damageHitbox.minY * this.scaleHitbox[1], this.damageHitbox.minZ * this.scaleHitbox[2],
				this.damageHitbox.maxX * this.scaleHitbox[0], this.damageHitbox.maxY * this.scaleHitbox[1], this.damageHitbox.maxZ * this.scaleHitbox[2]);
		double yaw = 0.0d, pitch = 0.0d;
		if (this.offsetHitbox[0] != 0.0f || this.offsetHitbox[1] != 0.0f || this.offsetHitbox[2] != 0.0f) {
			RayTraceRotate base = AdditionalMethods.instance.getAngles3D(0.0d, 0.0d, 0.0d, this.offsetHitbox[2], this.offsetHitbox[1], this.offsetHitbox[0]);
			yaw = base.yaw;
			pitch = base.pitch;
		}
		RayTraceVec data = AdditionalMethods.instance.getPosition(0.0d, 0.0d, 0.0d, npc.rotationYaw + yaw, pitch, Math.abs(Math.sqrt(Math.pow(this.offsetHitbox[0], 2.0d) + Math.pow(this.offsetHitbox[2], 2.0d))));
		aabb = aabb.offset((float) data.x, (float) data.y + this.offsetHitbox[1], (float) data.z);
		return aabb;
	}

	public int getAnimationFrameByTime(long totalTicks) {
		if (totalTicks > 0) {
			for (int id : this.endingFrameTicks.keySet()) {
				if (totalTicks <= this.endingFrameTicks.get(id)) { return id; }
			}
		}
		return 0;
	}
}