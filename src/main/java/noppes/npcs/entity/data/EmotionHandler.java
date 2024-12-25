package noppes.npcs.entity.data;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.api.entity.data.IEmotion;
import noppes.npcs.client.model.animation.EmotionConfig;
import noppes.npcs.client.model.animation.EmotionFrame;
import noppes.npcs.constants.EnumAnimationStages;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.util.ValueUtil;

import java.util.*;

public class EmotionHandler {

    private final EntityLivingBase entity;

    // key = 0:eyeRight, 1:eyeLeft, 2:pupilRight, 3:pupilLeft, 4:browRight, 5:browLeft
    public final Map<Integer, Float[]> rotationAngles = new TreeMap<>();
    public EmotionConfig activeEmotion = null;

    public EmotionFrame preFrame = EmotionFrame.EMPTY.copy();
    public EmotionFrame currentFrame;
    public EmotionFrame nextFrame;

    public EnumAnimationStages stage = EnumAnimationStages.Waiting;
    public long startEmotionTime = 0;
    public int baseEmotionId = -1;
    public int speedTicks;
    public int timeTicks;

    public EmotionHandler(EntityLivingBase main) {
        entity = main;
    }

    public EmotionConfig getEmotion() { return activeEmotion; }

    public void calculationEmotionData(float partialTicks) {
        if (stage == EnumAnimationStages.Waiting || activeEmotion == null) { return; }
        int ticks = Math.max(0, (int) (entity.world.getTotalWorldTime() - startEmotionTime));
        // Speed ticks to next frame
        speedTicks = 0;
        switch (stage) {
            case Started: {
                currentFrame = preFrame;
                nextFrame = activeEmotion.frames.get(0);
                break;
            }
            case Looping: {
                currentFrame = activeEmotion.frames.get(activeEmotion.frames.size() - 1);
                int lastFrameId = activeEmotion.frames.size();
                int frameId;
                if (activeEmotion.repeatLast > 0) { frameId = ValueUtil.correctInt(lastFrameId - activeEmotion.repeatLast, 0, lastFrameId - 1); }
                else { frameId = lastFrameId - 1; }
                nextFrame = activeEmotion.frames.get(frameId);
                speedTicks = currentFrame.speed;
                break;
            }
            case Run: {
                int emotionFrame = activeEmotion.getEmotionFrameByTime(ticks);
                if (emotionFrame < 0) { emotionFrame = 0; }
                currentFrame = activeEmotion.frames.get(emotionFrame);
                nextFrame = activeEmotion.frames.get(Math.min(emotionFrame + 1, activeEmotion.frames.size() - 1));
                speedTicks = currentFrame.speed;
                if (activeEmotion.endingFrameTicks.containsKey(emotionFrame - 1)) { ticks -= activeEmotion.endingFrameTicks.get(emotionFrame - 1); }
                break;
            }
            case Ending: {
                currentFrame = activeEmotion.frames.get(activeEmotion.frames.size() - 1);
                nextFrame = EmotionFrame.EMPTY;
                break;
            }
            default: {
                stopEmotion();
            }
        }
        timeTicks = ticks;

        // calculation of exact values for a body part
        for (int partId = 0; partId < 7; partId++) { // 0:eyeRight, 1:eyeLeft, 2:pupilRight, 3:pupilLeft, 4:browRight, 5:browLeft, 6:mouth
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
                                    value_0 = currentFrame.scaleEye[a + 2] + 0.5f;
                                    value_1 = nextFrame.scaleEye[a + 2] + 0.5f;
                                    break;
                                }
                                case 2: { // pupilRight
                                    value_0 = currentFrame.scalePupil[a] + 0.5f;
                                    value_1 = nextFrame.scalePupil[a] + 0.5f;
                                    break;
                                }
                                case 3: { // pupilLeft
                                    value_0 = currentFrame.scalePupil[a + 2] + 0.5f;
                                    value_1 = nextFrame.scalePupil[a + 2] + 0.5f;
                                    break;
                                }
                                case 4: { // browRight
                                    value_0 = currentFrame.scaleBrow[a] + 0.5f;
                                    value_1 = nextFrame.scaleBrow[a] + 0.5f;
                                    break;
                                }
                                case 5: { // browLeft
                                    value_0 = currentFrame.scaleBrow[a + 2] + 0.5f;
                                    value_1 = nextFrame.scaleBrow[a + 2] + 0.5f;
                                    break;
                                }
                                case 6: { // mouth
                                    value_0 = currentFrame.scaleMouth[a] + 0.5f;
                                    value_1 = nextFrame.scaleMouth[a] + 0.5f;
                                    break;
                                }
                                default: { // eyeRight
                                    value_0 = currentFrame.scaleEye[a] + 0.5f;
                                    value_1 = nextFrame.scaleEye[a] + 0.5f;
                                    break;
                                }
                            }
                            break;
                        }
                        case 2: { // rotations
                            switch (partId) {
                                case 1: { // eyeLeft
                                    value_0 = (currentFrame.rotEye[1] - 0.5f) * 360.0f;
                                    value_1 = (nextFrame.rotEye[1] - 0.5f) * 360.0f;
                                    break;
                                }
                                case 2: { // pupilRight
                                    value_0 = (currentFrame.rotPupil[0] - 0.5f) * 360.0f;
                                    value_1 = (nextFrame.rotPupil[0] - 0.5f) * 360.0f;
                                    break;
                                }
                                case 3: { // pupilLeft
                                    value_0 = (currentFrame.rotPupil[1] - 0.5f) * 360.0f;
                                    value_1 = (nextFrame.rotPupil[1] - 0.5f) * 360.0f;
                                    break;
                                }
                                case 4: { // browRight
                                    value_0 = (currentFrame.rotBrow[0] - 0.5f) * 360.0f;
                                    value_1 = (nextFrame.rotBrow[0] - 0.5f) * 360.0f;
                                    break;
                                }
                                case 5: { // browLeft
                                    value_0 = (currentFrame.rotBrow[1] - 0.5f) * 360.0f;
                                    value_1 = (nextFrame.rotBrow[1] - 0.5f) * 360.0f;
                                    break;
                                }
                                case 6: { // mouth
                                    value_0 = (currentFrame.rotMouth - 0.5f) * 360.0f;
                                    value_1 = (nextFrame.rotMouth - 0.5f) * 360.0f;
                                    break;
                                }
                                default: { // eyeRight
                                    value_0 = (currentFrame.rotEye[0] - 0.5f) * 360.0f;
                                    value_1 = (nextFrame.rotEye[0] - 0.5f) * 360.0f;
                                    break;
                                }
                            }
                            break;
                        }
                        default: { // offsets
                            switch (partId) {
                                case 1: { // eyeLeft
                                    value_0 = (currentFrame.offsetEye[a + 2] - 0.5f) * 2.0f;
                                    value_1 = (nextFrame.offsetEye[a + 2] - 0.5f) * 2.0f;
                                    break;
                                }
                                case 2: { // pupilRight
                                    value_0 = (currentFrame.offsetPupil[a] - 0.5f) * 2.0f;
                                    value_1 = (nextFrame.offsetPupil[a] - 0.5f) * 2.0f;
                                    break;
                                }
                                case 3: { // pupilLeft
                                    value_0 = (currentFrame.offsetPupil[a + 2] - 0.5f) * 2.0f;
                                    value_1 = (nextFrame.offsetPupil[a + 2] - 0.5f) * 2.0f;
                                    break;
                                }
                                case 4: { // browRight
                                    value_0 = (currentFrame.offsetBrow[a] - 0.5f) * 2.0f;
                                    value_1 = (nextFrame.offsetBrow[a] - 0.5f) * 2.0f;
                                    break;
                                }
                                case 5: { // browLeft
                                    value_0 = (currentFrame.offsetBrow[a + 2] - 0.5f) * 2.0f;
                                    value_1 = (nextFrame.offsetBrow[a + 2] - 0.5f) * 2.0f;
                                    break;
                                }
                                case 6: { // mouth
                                    value_0 = (currentFrame.offsetMouth[a] - 0.5f) * 2.0f;
                                    value_1 = (nextFrame.offsetMouth[a] - 0.5f) * 2.0f;
                                    break;
                                }
                                default: { // eyeRight
                                    value_0 = (currentFrame.offsetEye[a] - 0.5f) * 2.0f;
                                    value_1 = (nextFrame.offsetEye[a] - 0.5f) * 2.0f;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    values[t * 2 + a] = calcValue(value_0, value_1, speedTicks, ticks, currentFrame.isSmooth(), partialTicks);
                }
            }
            rotationAngles.put(partId, values);
        }
        preFrame.resetFrom(rotationAngles, currentFrame);
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

    public EmotionConfig tryRunEmotion(EmotionConfig emotion) {
        if (emotion == null) { return null; }
        activeEmotion = emotion;
        startEmotionTime = entity.world.getTotalWorldTime();
        stage = EnumAnimationStages.Started;
        return activeEmotion;
    }

    public void tryRunEmotion(int emotionId) { tryRunEmotion(AnimationController.getInstance().emotions.get(emotionId)); }

    public void stopEmotion() {
        currentFrame = EmotionFrame.EMPTY;
        rotationAngles.clear();
        startEmotionTime = 0;
        currentFrame = null;
        nextFrame = null;
        timeTicks = -1;
        speedTicks = -1;
    }

    public void load(NBTTagCompound compound) {
        baseEmotionId = compound.getInteger("BaseEmotionId");
        if (!AnimationController.getInstance().emotions.containsKey(baseEmotionId)) {
            baseEmotionId = -1;
        }
    }

    public void save(NBTTagCompound compound) {
        compound.setInteger("BaseEmotionId", baseEmotionId);
    }

    public boolean isAnimated() {
        return activeEmotion != null && stage != EnumAnimationStages.Waiting && entity.getHealth() > 0.0f;
    }

    public void updateTime() {
        // animation running time
        if (activeEmotion == null) {
            currentFrame = null;
            nextFrame = null;
            stage = EnumAnimationStages.Waiting;
            startEmotionTime = 0;
            timeTicks = -1;
            return;
        }
        if (!AnimationController.getInstance().emotions.containsKey(activeEmotion.id) && stage != EnumAnimationStages.Ending && stage != EnumAnimationStages.Waiting) {
            stage = EnumAnimationStages.Ending;
            startEmotionTime = entity.world.getTotalWorldTime() + 1;
            return;
        }
        int ticks = Math.max(0, (int) (entity.world.getTotalWorldTime() - startEmotionTime));
        int speed;
        if (activeEmotion.editFrame >= 0) {
            stage = EnumAnimationStages.Run;
            speed = 0;
            return;
        }
        if (stage == EnumAnimationStages.Started) {
            speed = 10;
            if (ticks >= speed) {
                startEmotionTime += speed + 1;
                stage = EnumAnimationStages.Run;
            }
            return;
        }
        if (stage == EnumAnimationStages.Looping) {
            speed = activeEmotion.frames.get(activeEmotion.frames.size() - 1).speed;
            if (ticks >= speed) {
                int lastFrameId = activeEmotion.frames.size();
                int frameId;
                if (activeEmotion.repeatLast > 0) {
                    frameId = ValueUtil.correctInt(lastFrameId - activeEmotion.repeatLast, 0, lastFrameId - 1);
                } else { frameId = lastFrameId - 1; }
                if (frameId == 0) {
                    startEmotionTime = entity.world.getTotalWorldTime() + 1;
                } else {
                    startEmotionTime = entity.world.getTotalWorldTime() + activeEmotion.endingFrameTicks.get(frameId - 1) + 1;
                }
                if (frameId != lastFrameId - 1) { stage = EnumAnimationStages.Run; }
            }
            return;
        }
        if (stage == EnumAnimationStages.Run) {
            if (ticks >= activeEmotion.totalTicks) {
                startEmotionTime = entity.world.getTotalWorldTime() + 1;
                stage = EnumAnimationStages.Looping;
            }
            return;
        }
        if (stage == EnumAnimationStages.Ending) {
            speed = 10;
            if (ticks >= speed) {
                startEmotionTime = 0;
                stage = EnumAnimationStages.Waiting;
            }
            return;
        }
        if (stage == EnumAnimationStages.Waiting) {
            stopEmotion();
        }
    }

    public void setRotationAngles(float ignoredLimbSwing, float ignoredLimbSwingAmount, float ignoredAgeInTicks, float ignoredNetHeadYaw, float ignoredHeadPitch, float ignoredScaleFactor, float partialTicks) {
        if (activeEmotion == null && !AnimationController.getInstance().emotions.containsKey(baseEmotionId)) { return; }
        if (!isAnimated()) { tryRunEmotion(baseEmotionId); }
        else { calculationEmotionData(partialTicks); }
    }

}
