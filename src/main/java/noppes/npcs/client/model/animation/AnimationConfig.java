package noppes.npcs.client.model.animation;

import java.util.*;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.entity.data.IAnimationFrame;
import noppes.npcs.api.util.IRayTraceRotate;
import noppes.npcs.api.util.IRayTraceVec;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.util.Util;

public class AnimationConfig implements IAnimation {

	public static final AnimationConfig EMPTY;
	static {
		EMPTY = new AnimationConfig();
		EMPTY.frames.put(0, AnimationFrameConfig.EMPTY);
		EMPTY.resetTicks();
	}

	public String name = "Default Animation";
	public int repeatLast = 0;
	public final Map<Integer, AnimationFrameConfig> frames = new TreeMap<>(); // [ Frame ID, Frame setting ]
	public final Map<Integer, List<AddedPartConfig>> addParts = new TreeMap<>(); // [ Parent Frame ID, added Part setting list ]
	public final Map<Integer, Integer> endingFrameTicks = new TreeMap<>(); // ticks info
	public int totalTicks = 0;

	public int id = -1;
	public AnimationKind type = AnimationKind.STANDING;
	public float chance = 1.0f;
	public boolean immutable;

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
			Map<Integer, AnimationFrameConfig> newFrames = new TreeMap<>();
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
		return frames.containsKey(frameId);
	}

	public void load(NBTTagCompound compound) {
		frames.clear();
		boolean hasDelayAttack = false;
		for (int i = 0; i < compound.getTagList("FrameConfigs", 10).tagCount(); i++) {
			AnimationFrameConfig afc = new AnimationFrameConfig();
			afc.readNBT(compound.getTagList("FrameConfigs", 10).getCompoundTagAt(i));
			afc.id = i;
			if (!hasDelayAttack) { hasDelayAttack = afc.isNowDamage(); }
			else { afc.isNowDamage = false; }
			frames.put(i, afc);
		}
		if (frames.isEmpty()) { frames.put(0, new AnimationFrameConfig(0)); }
		if (!hasDelayAttack) { frames.get(0).isNowDamage = true; }

		addParts.clear();
		for (int i = 0, id = 8; i < compound.getTagList("AddedParts", 10).tagCount(); i++, id++) {
			AddedPartConfig addPart = new AddedPartConfig();
			addPart.load(compound.getTagList("AddedParts", 10).getCompoundTagAt(i));
			addPart.id = id;
			if (!addParts.containsKey(addPart.parentPart)) { addParts.put(addPart.parentPart, new ArrayList<>()); }
			addParts.get(addPart.parentPart).add(addPart);
		}

		this.id = compound.getInteger("ID");
		this.name = compound.getString("Name");
		if (compound.hasKey("Chance", 5)) { setChance(compound.getFloat("Chance")); }
		if (compound.hasKey("Immutable", 1)) { immutable = compound.getBoolean("Immutable"); }
		if (compound.hasKey("Type", 3)) { type = AnimationKind.get(compound.getInteger("Type")); }
		if (compound.hasKey("RepeatLast", 3)) { setRepeatLast(compound.getInteger("RepeatLast")); }

		if (compound.hasKey("DamageHitbox", 9) && compound.getTagList("DamageHitbox", 6).tagCount() == 6) { // OLD
			AnimationDamageHitbox aDHB = new AnimationDamageHitbox(0);
			if (compound.hasKey("OffsetHitbox", 9) && compound.getTagList("OffsetHitbox", 9).getTagType() == 5 && compound.getTagList("OffsetHitbox", 9).tagCount() > 2) {
				NBTTagList list = compound.getTagList("OffsetHitbox", 5);
				aDHB.offset[0] = list.getFloatAt(0);
				aDHB.offset[1] = list.getFloatAt(1);
				aDHB.offset[2] = list.getFloatAt(2);
			}
			if (compound.hasKey("ScaleHitbox", 9) && compound.getTagList("ScaleHitbox", 9).getTagType() == 5 && compound.getTagList("ScaleHitbox", 9).tagCount() > 2) {
				NBTTagList list = compound.getTagList("ScaleHitbox", 5);
				aDHB.scale[0] = list.getFloatAt(0);
				aDHB.scale[1] = list.getFloatAt(1);
				aDHB.scale[2] = list.getFloatAt(2);
			}
			int tTicks = 0;
			for (AnimationFrameConfig aFC : frames.values()) {
				tTicks += aFC.speed;
				if (aFC.isNowDamage()) {
					aFC.damageDelay = tTicks;
					aFC.damageHitboxes.clear();
					aFC.damageHitboxes.put(0, aDHB);
					break;
				}
				tTicks += aFC.delay;
			}
		}
		CustomNpcs.proxy.resetAnimationModel(this);
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
		Map<Integer, AnimationFrameConfig> newData = new TreeMap<>();
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
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.UPDATE_NPC_ANIMATION, npcEntity.world.provider.getDimension(), 3, npcEntity.getEntityId(), save());
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
		NBTTagList listFC = new NBTTagList();
		Iterator<AnimationFrameConfig> setts = frames.values().iterator();
		while(setts.hasNext()) {
			try {
				AnimationFrameConfig afc = setts.next();
				listFC.appendTag(afc.writeNBT());
			}
			catch (Exception e) {
				LogWriter.error("Error:", e);
				break;
			}
		}
		compound.setTag("FrameConfigs", listFC);
		compound.setInteger("ID", id);
		compound.setInteger("Type", type.get());
		compound.setInteger("RepeatLast", repeatLast);
		compound.setString("Name", name);
		compound.setFloat("Chance", chance);
		compound.setBoolean("Immutable", immutable);

		NBTTagList listAP = new NBTTagList();
		for (int partId : addParts.keySet()) {
			for (AddedPartConfig addedPart : addParts.get(partId)) {
				listAP.appendTag(addedPart.save());
			}
		}
		compound.setTag("AddedParts", listAP);

		return compound;
	}

	public void resetTicks() {
		totalTicks = 0;
		endingFrameTicks.clear();
		if (this == EMPTY) {
			totalTicks = AnimationFrameConfig.EMPTY.speed + AnimationFrameConfig.EMPTY.delay + 1;
			endingFrameTicks.put(0, totalTicks);
			return;
		}
		int delay = 0;
		for (Integer id : this.frames.keySet()) {
			AnimationFrameConfig frame = frames.get(id);
			if (frame.speed < 1) { frame.speed = 1; }
			totalTicks += frame.speed;
			if (frame.isNowDamage()) {
				frame.damageDelay = totalTicks;
			}
			totalTicks += frame.delay;
			endingFrameTicks.put(id, totalTicks);
		}
		if (totalTicks == 0) { totalTicks = 1; }
	}

	public boolean hasEmotion() {
		for (AnimationFrameConfig frame : this.frames.values()) {
			if (frame.emotionId >= 0) { return true; }
		}
		return false;
	}

	public AxisAlignedBB[] getDamageHitboxes(EntityLivingBase npc, int delay) {
		for (AnimationFrameConfig aFC : frames.values()) {
			if (aFC.isNowDamage() && aFC.damageDelay == delay) {
				List<AxisAlignedBB> hitboxes = new ArrayList<>();
				for (AnimationDamageHitbox aDH : aFC.damageHitboxes.values()) {
					AxisAlignedBB aabb = aDH.getScaledDamageHitbox();
					double yaw = 0.0d, pitch = 0.0d;
					double x = aDH.offset[0];
					double y = aDH.offset[1];
					double z = aDH.offset[2];
					if (x != 0.0f || y != 0.0f || z != 0.0f) {
						IRayTraceRotate base = Util.instance.getAngles3D(0.0d, 0.0d, 0.0d, x, y, z);
						yaw = base.getYaw();
						pitch = base.getPitch();
					}
					IRayTraceVec data = Util.instance.getPosition(0.0d, 0.0d, 0.0d, npc.rotationYaw + yaw, pitch, Math.abs(Math.sqrt(Math.pow(x, 2.0d) + Math.pow(z, 2.0d))));
					hitboxes.add(aabb.offset(npc.posX, npc.posY, npc.posZ).offset(data.getX(), data.getY() + y, data.getZ()));
				}
				if (hitboxes.isEmpty()) {
					AxisAlignedBB aabb = new AxisAlignedBB(BlockPos.ORIGIN);
					hitboxes.add(aabb.offset(npc.posX, npc.posY, npc.posZ));
				}
				return hitboxes.toArray(new AxisAlignedBB[0]);
			}
		}
		return new AxisAlignedBB[0];
	}

	public int getAnimationFrameByTime(long ticks) {
		if (type == AnimationKind.EDITING_PART) { return 0; }
		if (ticks >= 0) {
			for (int id : endingFrameTicks.keySet()) {
				if (ticks <= endingFrameTicks.get(id)) { return id; }
			}
			return frames.size();
		}
		return -1;
	}

	public AddedPartConfig addAddedPart(int parentPartID) {
		AddedPartConfig addPart = new AddedPartConfig(parentPartID);
		if (!addParts.containsKey(parentPartID)) { addParts.put(parentPartID, new ArrayList<>()); }
		addPart.id = 8;
		for (AddedPartConfig apc : addParts.get(parentPartID)) {
			if (apc.id != addPart.id) { break; }
			addPart.id++;
		}
		addParts.get(parentPartID).add(addPart);
		for (AnimationFrameConfig frame : frames.values()) {
			frame.parts.put(addPart.id, new PartConfig(addPart.id, EnumParts.CUSTOM));
		}
		return addPart;
	}

	public AddedPartConfig getAddedPart(int id) {
		for (List<AddedPartConfig> list : addParts.values()) {
			for (AddedPartConfig addedPart : list) {
				if (addedPart.id == id) { return addedPart; }
			}
		}
		return null;
	}

	public void removeAddedPart(AddedPartConfig addedPartConfig) {
		if (addedPartConfig == null) { return; }
		int addedPartId = addedPartConfig.id;
		boolean bo = false;
		if (addParts.containsKey(addedPartConfig.parentPart)) {
			bo = addParts.get(addedPartConfig.parentPart).remove(addedPartConfig);
			if (!bo) {
				for (AddedPartConfig addedPart : addParts.get(addedPartConfig.parentPart)) {
					if (addedPart.id == addedPartId) {
						bo = addParts.get(addedPartConfig.parentPart).remove(addedPart);
						break;
					}
				}
			}
		}
		if (!bo) { removeAddedPart(addedPartId); }
		if (bo) {
			for (AnimationFrameConfig frame : frames.values()) {
				frame.parts.remove(addedPartId);
			}
		}
	}

	public void removeAddedPart(int addedPartId) {
		boolean bo = false;
		for (int partId : addParts.keySet()) {
			for (AddedPartConfig addedPart : addParts.get(partId)) {
				if (addedPart.id == addedPartId) {
					bo = addParts.get(partId).remove(addedPart);
					break;
				}
			}
		}
		if (bo) {
			for (AnimationFrameConfig frame : frames.values()) {
				frame.parts.remove(addedPartId);
			}
		}
	}

}