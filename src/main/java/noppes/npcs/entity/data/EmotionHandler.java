package noppes.npcs.entity.data;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.entity.data.IEmotion;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.EmotionConfig;
import noppes.npcs.client.model.animation.EmotionFrame;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.*;

public class EmotionHandler {

    private final EntityLivingBase entity;

    public final Map<Integer, Float[]> emts = new TreeMap<>();
    public EmotionConfig activeEmotion = null;
    public EmotionFrame currentEmotionFrame = null, nextEmotionFrame;
    public long startEmotionTime = 0;
    public int emotionFrame = 0;
    public int baseEmotionId = -1;

    public EmotionHandler(EntityLivingBase main) {
        entity = main;
    }

    public IEmotion getEmotion() { return this.activeEmotion; }

    public void resetEmtnValues(EntityNPCInterface entity, float pt) {
        if (this.activeEmotion == null || this.activeEmotion.frames.isEmpty() || this.currentEmotionFrame == null) {
            this.activeEmotion = null;
            return;
        }
        if (this.startEmotionTime <= 0) { this.startEmotionTime = this.entity.world.getTotalWorldTime(); }
        // Current anim ticks
        int ticks = (int) (this.entity.world.getTotalWorldTime() - this.startEmotionTime);
        // Speed ticks to next frame
        int speed = this.currentEmotionFrame != null ? this.currentEmotionFrame.speed : 0;
        if (this.emotionFrame < 0) { // new animation
            if (this.activeEmotion == null) { // finishing the old animation
                this.nextEmotionFrame = EmotionFrame.STANDARD.copy();
            }
            else {
                this.nextEmotionFrame = this.activeEmotion.frames.get(0);
                //if (this.currentEmotionFrame == null && this.activeEmotion.isEdit == (byte) 2) { this.currentEmotionFrame = this.activeEmotion.frames.get(0); }
            }
            if (this.currentEmotionFrame == null) { this.currentEmotionFrame = EmotionFrame.STANDARD.copy(); } // start of new animation
            speed = this.nextEmotionFrame.speed;
        } else {
            if (this.activeEmotion == null) { // returns to original position
                this.nextEmotionFrame = EmotionFrame.STANDARD.copy();
                speed = this.nextEmotionFrame.speed;
            } else if (this.activeEmotion.frames.size() == 1) { // simple animation
                this.nextEmotionFrame = this.activeEmotion.frames.get(0);
            } else if (this.activeEmotion.frames.containsKey(this.emotionFrame + 1)) { // next frame
                this.nextEmotionFrame = this.activeEmotion.frames.get(this.emotionFrame + 1);
            } else if (this.activeEmotion.repeatLast > 0) { // repeat frames until animation is turned off
                int f = this.activeEmotion.repeatLast;
                this.emotionFrame = this.activeEmotion.frames.size() - f;
                if (this.emotionFrame < 0) { this.emotionFrame = 0; }
                this.nextEmotionFrame = this.activeEmotion.frames.containsKey(this.emotionFrame) ? this.activeEmotion.frames.get(this.emotionFrame) : this.currentEmotionFrame;
            } else {
                this.nextEmotionFrame = EmotionFrame.STANDARD.copy();
                speed = this.nextEmotionFrame.speed;
            }
        }
        // calculation of exact values for a body part
        for (int partId = 0; partId < 6; partId++) { // 0:eyeRight, 1:eyeLeft, 2:pupilRight, 3:pupilLeft, 4:browRight, 5:browLeft
            Float[] values = new Float[] { 0.0f, 0.0f, 0.0f, 1.0f, 1.0f }; // ofsX, ofsY, scX, scY, rot
            for (int t = 0; t < 3; t++) { // 0:offsets, 1:scales, 2:rotations
                for (int a = 0; a < 2; a++) { // x, y
                    if (t == 2 && a == 1) { continue; }
                    float value_0;
                    float value_1;
                    switch (t) {
                        case 1: { // scales
                            switch (partId) {
                                case 1: { // eyeLeft
                                    value_0 = this.currentEmotionFrame.scaleEye[a + 2] + 0.5f;
                                    value_1 = this.nextEmotionFrame.scaleEye[a + 2] + 0.5f;
                                    break;
                                }
                                case 2: { // pupilRight
                                    value_0 = this.currentEmotionFrame.scalePupil[a] + 0.5f;
                                    value_1 = this.nextEmotionFrame.scalePupil[a] + 0.5f;
                                    break;
                                }
                                case 3: { // pupilLeft
                                    value_0 = this.currentEmotionFrame.scalePupil[a + 2] + 0.5f;
                                    value_1 = this.nextEmotionFrame.scalePupil[a + 2] + 0.5f;
                                    break;
                                }
                                case 4: { // browRight
                                    value_0 = this.currentEmotionFrame.scaleBrow[a] + 0.5f;
                                    value_1 = this.nextEmotionFrame.scaleBrow[a] + 0.5f;
                                    break;
                                }
                                case 5: { // browLeft
                                    value_0 = this.currentEmotionFrame.scaleBrow[a + 2] + 0.5f;
                                    value_1 = this.nextEmotionFrame.scaleBrow[a + 2] + 0.5f;
                                    break;
                                }
                                default: { // eyeRight
                                    value_0 = this.currentEmotionFrame.scaleEye[a] + 0.5f;
                                    value_1 = this.nextEmotionFrame.scaleEye[a] + 0.5f;
                                    break;
                                }
                            }
                            break;
                        }
                        case 2: { // rotations
                            switch (partId) {
                                case 1: { // eyeLeft
                                    value_0 = (this.currentEmotionFrame.rotEye[1] - 0.5f) * 360.0f;
                                    value_1 = (this.nextEmotionFrame.rotEye[1] - 0.5f) * 360.0f;
                                    break;
                                }
                                case 2: { // pupilRight
                                    value_0 = (this.currentEmotionFrame.rotPupil[0] - 0.5f) * 360.0f;
                                    value_1 = (this.nextEmotionFrame.rotPupil[0] - 0.5f) * 360.0f;
                                    break;
                                }
                                case 3: { // pupilLeft
                                    value_0 = (this.currentEmotionFrame.rotPupil[1] - 0.5f) * 360.0f;
                                    value_1 = (this.nextEmotionFrame.rotPupil[1] - 0.5f) * 360.0f;
                                    break;
                                }
                                case 4: { // browRight
                                    value_0 = (this.currentEmotionFrame.rotBrow[0] - 0.5f) * 360.0f;
                                    value_1 = (this.nextEmotionFrame.rotBrow[0] - 0.5f) * 360.0f;
                                    break;
                                }
                                case 5: { // browLeft
                                    value_0 = (this.currentEmotionFrame.rotBrow[1] - 0.5f) * 360.0f;
                                    value_1 = (this.nextEmotionFrame.rotBrow[1] - 0.5f) * 360.0f;
                                    break;
                                }
                                default: { // eyeRight
                                    value_0 = (this.currentEmotionFrame.rotEye[0] - 0.5f) * 360.0f;
                                    value_1 = (this.nextEmotionFrame.rotEye[0] - 0.5f) * 360.0f;
                                    break;
                                }
                            }
                            break;
                        }
                        default: { // offsets
                            switch (partId) {
                                case 1: { // eyeLeft
                                    value_0 = (this.currentEmotionFrame.offsetEye[a + 2] - 0.5f) * 2.0f;
                                    value_1 = (this.nextEmotionFrame.offsetEye[a + 2] - 0.5f) * 2.0f;
                                    break;
                                }
                                case 2: { // pupilRight
                                    value_0 = (this.currentEmotionFrame.offsetPupil[a] - 0.5f) * 2.0f;
                                    value_1 = (this.nextEmotionFrame.offsetPupil[a] - 0.5f) * 2.0f;
                                    break;
                                }
                                case 3: { // pupilLeft
                                    value_0 = (this.currentEmotionFrame.offsetPupil[a + 2] - 0.5f) * 2.0f;
                                    value_1 = (this.nextEmotionFrame.offsetPupil[a + 2] - 0.5f) * 2.0f;
                                    break;
                                }
                                case 4: { // browRight
                                    value_0 = (this.currentEmotionFrame.offsetBrow[a] - 0.5f) * 2.0f;
                                    value_1 = (this.nextEmotionFrame.offsetBrow[a] - 0.5f) * 2.0f;
                                    break;
                                }
                                case 5: { // browLeft
                                    value_0 = (this.currentEmotionFrame.offsetBrow[a + 2] - 0.5f) * 2.0f;
                                    value_1 = (this.nextEmotionFrame.offsetBrow[a + 2] - 0.5f) * 2.0f;
                                    break;
                                }
                                default: { // eyeRight
                                    value_0 = (this.currentEmotionFrame.offsetEye[a] - 0.5f) * 2.0f;
                                    value_1 = (this.nextEmotionFrame.offsetEye[a] - 0.5f) * 2.0f;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    values[t * 2 + a] = calcValue(value_0, value_1, speed, ticks, currentEmotionFrame.isSmooth(), pt);
                    if (t != 0) { values[t * 2 + a] /= 2 * (float) Math.PI; }
                }
            }
            emts.put(partId, values);
        }

        if (ticks >= speed + this.nextEmotionFrame.getEndDelay()) {
            this.emotionFrame++;
            this.startEmotionTime = entity.world.getTotalWorldTime();
            this.currentEmotionFrame = this.nextEmotionFrame;
        }
    }

    private float calcValue(float value_0, float value_1, float speed, float ticks, boolean isSmooth, float partialTicks) {
        if (speed <= 0 || ticks < 0.0f) { return value_0; }
        float progress = Math.min((ticks + partialTicks) / speed, 1.0f);
        if (progress >= 1.0f) { return value_1; }
        if (isSmooth) { // Apply antialiasing if necessary
            progress = -0.5f * MathHelper.cos(progress * (float) Math.PI) + 0.5f;
        }
        return value_0 + (value_1 - value_0) * progress;
    }

    public void startEmotion(int emotionId) {
        IEmotion emotion = AnimationController.getInstance().getEmotion(emotionId);
        if (emotion == null) { return; }
        if (!this.entity.isServerWorld()) {
            this.activeEmotion = (EmotionConfig) emotion;
            this.emotionFrame = 0;
            this.startEmotionTime = 0;
        } else {
            //updateClient(4, emotion.getId());
        }
    }

    public void stopEmotion() {
        if (activeEmotion != null) {
            currentEmotionFrame = activeEmotion.frames.get(emotionFrame);
            activeEmotion = null;
        }
        else { currentEmotionFrame = EmotionFrame.STANDARD; }
        emts.clear();
        startEmotionTime = 0;
        emotionFrame = -1;
        currentEmotionFrame = null;
        nextEmotionFrame = null;
    }

    public void load(NBTTagCompound compound) {
        AnimationController aData = AnimationController.getInstance();

    }

    public void save(NBTTagCompound compound) {
        NBTTagList allEmotions = new NBTTagList();

        compound.setTag("AllEmotions", allEmotions);
        compound.setInteger("BaseEmotionId", baseEmotionId);
    }

    public void updateTime() {
    }

}
