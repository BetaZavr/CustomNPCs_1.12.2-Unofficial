package noppes.npcs.client.model.animation;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.util.math.MathHelper;
import noppes.npcs.Server;
import noppes.npcs.api.entity.data.IEmotion;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.entity.EntityNPCInterface;

public class EmotionConfig
implements IEmotion {

	public static final PartEmotion EMPTY_PART = new PartEmotion(null);
	
	public Map<Integer, PartEmotion> frames;
	public int id, frame, browThickness;
	public long startTick, blinkStart;
	public String name;
	private float val, valNext;
	private EntityNPCInterface npc;
	private Random rnd = new Random();

	public EmotionConfig(EntityNPCInterface npc) {
		this.npc = npc;
		this.frames = Maps.<Integer, PartEmotion>newTreeMap();
		this.name = "Default Emotion";
		this.id = 0;
		this.reset();
	}
	
	public void reset() {
		this.frame = 0;
		this.val = 0;
		this.valNext = 0;
		this.blinkStart = 0L;
		this.browThickness = 4;
		this.startTick = this.npc.world.getTotalWorldTime();
	}
	
	private float calcValue(float value_0, float value1, int speed, float ticks, float partialTicks) {
		if (ticks >= speed - 1) { ticks = speed - 1; }
		float pi = (float) Math.PI;
		this.val = -0.5f * MathHelper.cos((float) ticks / (float) speed * pi) + 0.5f;
		this.valNext = -0.5f * MathHelper.cos((float) (ticks+1) / (float) speed * pi) + 0.5f;
		float f = this.val + (this.valNext - this.val) * partialTicks;
		float value = (value_0 + (value1 - value_0) * f);
		return value;
	}
	
	/**
	 * Return asix values
	 * @param valueType - 0:offsetEye, 1:scales 2:skinColor, 3:browColor, 4:eyeColor, 5:browThickness
	 * @param partialTicks
	 * @param npc
	 * @return values float[ u, v ]
	 */
	public float[] getValues(int valueType, float partialTicks, EntityNPCInterface npc) {
		if (this.frames.size()==0) { return null; }
		this.npc = npc;
		float[] values = new float[] { 0.0f, 0.0f, 0.0f};
		PartEmotion part_1;
		PartEmotion part_0 = (PartEmotion) this.frames.get(this.frame);
		if (this.frames.containsKey(this.frame+1)) { part_1 = (PartEmotion) this.frames.get(this.frame + 1); }
		else {
			npc.animation.stopEmotion();
			return values;
		}
		if (part_0.isDisabled()) { part_0 = EmotionConfig.EMPTY_PART; }
		if (part_1.isDisabled()) { part_1 = EmotionConfig.EMPTY_PART; }
		long ticks = this.npc.world.getTotalWorldTime() - this.startTick;
		
		for (int i=0; i<3; i++) {
			float value_0;
			float value_1;
			switch(valueType) {
				case 1: {
					value_0 = part_0.scale[i] * 5.0f;
					value_1 = part_1.scale[i] * 5.0f;
					break;
				}
				case 2: {
					value_0 = part_0.skinColor - 0xFF00000;
					value_1 = part_1.skinColor - 0xFF00000;
					break;
				}
				case 3: {
					value_0 = part_0.browColor - 0xFF00000;
					value_1 = part_1.browColor - 0xFF00000;
					break;
				}
				case 4: {
					value_0 = part_0.color - 0xFF00000;
					value_1 = part_1.color - 0xFF00000;
					break;
				}
				case 5: {
					value_0 = part_0.browThickness;
					value_1 = part_1.browThickness;
					break;
				}
				default: {
					value_0 = part_0.offsetEye[i] - 0.5f;
					value_1 = part_1.offsetEye[i] - 0.5f;
				}
			}
			values[i] = this.calcValue(value_0, value_1, part_0.speed, ticks, partialTicks);
			if (valueType>1) {
				if (valueType<5) { values[i] += 0xFF00000; }
				break;
			}
		}
		if (ticks + part_0.delay  + 1>=part_0.speed) {
			this.frame++;
			if (!this.frames.containsKey(this.frame)) { npc.animation.stopEmotion(); }
			this.startTick = this.npc.world.getTotalWorldTime();
		}
		return values;
	}

	public void update(EntityNPCInterface npc) {
		if (!npc.isEntityAlive() || !npc.isServerWorld()) {
			return;
		}
		if (this.blinkStart < 0L) {
			++this.blinkStart;
		} else if (this.blinkStart == 0L) {
			if (this.rnd.nextInt(140) == 1) {
				this.blinkStart = System.currentTimeMillis();
				if (npc != null) {
					Server.sendAssociatedData(npc, EnumPacketClient.EYE_BLINK, npc.getEntityId());
				}
			}
		} else if (System.currentTimeMillis() - this.blinkStart > 300L) {
			this.blinkStart = -20L;
		}
	}
	
}
