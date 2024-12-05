package noppes.npcs.entity.data;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.event.AnimationEvent;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.client.model.animation.PartConfig;
import noppes.npcs.constants.EnumAnimationStages;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

import java.util.*;

public class AnimationHandler {

    private final EntityLivingBase entity;

    // Animation run
    /**
     * Integer key = partID - 0:head, 1:left arm, 2:right arm, 3:body, 4:left leg, 5:right leg, 6:left stack, 7:right stack ... added parts
     * Float[] value = [ 0:rotX, 1:rotY, 2:rotZ, 3:ofsX, 4:ofsY, 5:ofsZ, 6:scX, 7:scY, 8:scZ, 9:rotX1, 10:rotY1 ]
     */
    public final Map<Integer, Float[]> rotationAngles = new TreeMap<>();
    // Animation settings
    private final Map<AnimationKind, List<Integer>> data = new HashMap<>();
    private final Map<Integer, Long> waitData = new HashMap<>(); // animation ID, time

    public AnimationConfig activeAnimation = null;
    public final Map<AnimationKind, AnimationConfig> movementAnimation = new HashMap<>();

    public long startAnimationTime = 0;
    public EnumAnimationStages stage = EnumAnimationStages.Waiting;
    private boolean completeAnimation = false;
    private ResourceLocation animationSound = null;
    public boolean isCyclical = false;

    // current state, used to smoothly start another animation
    public final AnimationFrameConfig preFrame = new AnimationFrameConfig();
    // current frame from animation
    public AnimationFrameConfig currentFrame;
    // next frame from animation
    public AnimationFrameConfig nextFrame;
    // any data
    public boolean isJump = false;
    public boolean isSwing = false;
    private final Random rnd = new Random();

    public AnimationHandler(EntityLivingBase main) {
        entity = main;
        checkData();
    }

    public boolean hasAnim(AnimationKind type) {
        if (!data.containsKey(type) || data.get(type).isEmpty()) { return false; }
        AnimationController aData = AnimationController.getInstance();
        for (int id : data.get(type)) {
            if (aData.animations.containsKey(id)) {return true; }
        }
        return false;
    }

    private void checkData() {
        for (AnimationKind type : AnimationKind.values()) {
            if (!data.containsKey(type)) { data.put(type, new ArrayList<>()); }
        }
        while (data.containsKey(null)) { data.remove(null); }
    }

    public void stopAnimation() {
        if (activeAnimation != null) {
            if (activeAnimation.type != AnimationKind.EDITING_All && activeAnimation.type != AnimationKind.EDITING_PART) {
                int ticks = (int) (entity.world.getTotalWorldTime() - startAnimationTime);
                int animationFrame = activeAnimation.getAnimationFrameByTime(ticks);
                int startFrameTime = 0;
                if (activeAnimation.endingFrameTicks.containsKey(animationFrame - 1)) {
                    startFrameTime = activeAnimation.endingFrameTicks.get(animationFrame - 1);
                }
                startEvent(new AnimationEvent.StopEvent(entity, activeAnimation, animationFrame, ticks - startFrameTime, stage));
            } else { return; }
        }
        if (animationSound != null && !entity.isServerWorld()) { MusicController.Instance.stopSound(animationSound.toString(), SoundCategory.AMBIENT); }
        animationSound = null;
        startAnimationTime = 0;
        completeAnimation = false;
        activeAnimation = null;
        stage = EnumAnimationStages.Waiting;
        isJump = false;
        isSwing = false;
    }

    public void calculationAnimationData(float correctSpeed, float partialTicks) {
        if (stage == EnumAnimationStages.Waiting || activeAnimation == null) { return; }
        int ticks = (int) (entity.world.getTotalWorldTime() - startAnimationTime);
        if (ticks == -1) { ticks = 0; }
        int speed = activeAnimation.type.isQuickStart() ? 4 : 10;
        boolean isEdit = activeAnimation.type == AnimationKind.EDITING_All || activeAnimation.type == AnimationKind.EDITING_PART;
        switch (stage) {
            case Started: {
                currentFrame = preFrame;
                nextFrame = activeAnimation.frames.get(0);
                break;
            }
            case Looping: {
                currentFrame = activeAnimation.frames.get(activeAnimation.frames.size() - 1);
                int lastFrameId = activeAnimation.frames.size();
                int frameId = 0;
                if (activeAnimation.repeatLast > 0) { frameId = ValueUtil.correctInt(lastFrameId - activeAnimation.repeatLast, 0, lastFrameId - 1); }
                nextFrame = activeAnimation.frames.get(frameId);
                speed = currentFrame.speed;
                break;
            }
            case Run: {
                int animationFrame = activeAnimation.getAnimationFrameByTime(ticks);
                currentFrame = activeAnimation.frames.get(animationFrame);
                nextFrame = activeAnimation.frames.get(Math.min(animationFrame + 1, activeAnimation.frames.size() - 1));
                try {
                    speed = currentFrame.speed;
                } catch (Exception e) {
                    System.out.println("CNPCs: animationFrame: "+animationFrame);
                    System.out.println("CNPCs: ticks: "+ticks);
                    System.out.println("CNPCs: currentFrame: "+currentFrame);
                    System.out.println("CNPCs: nextFrame: "+nextFrame);
                    e.printStackTrace();
                    return;
                }
                if (activeAnimation.endingFrameTicks.containsKey(animationFrame - 1)) {
                    ticks -= activeAnimation.endingFrameTicks.get(animationFrame - 1);
                }
                break;
            }
            case Ending: {
                currentFrame = activeAnimation.frames.get(activeAnimation.frames.size() - 1);
                nextFrame = preFrame;
                break;
            }
            default: {
                stopAnimation();
            }
        }
//System.out.println("CNPCs: "+stage+"; {"+currentFrame.id+"->"+nextFrame.id+" /"+(activeAnimation.frames.size()-1)+"}; "+ticks+"/"+speed+"//"+activeAnimation.totalTicks);

        // for start or finish use settings from animation frame
        if (nextFrame.id != -1 && currentFrame.id == -1) {
            for (int id : nextFrame.parts.keySet()) {
                if (!currentFrame.parts.containsKey(id)) { continue; }
                currentFrame.parts.get(id).setDisable(nextFrame.parts.get(id).isDisable());
                currentFrame.parts.get(id).setShow(nextFrame.parts.get(id).isShow());
            }
        }
        if (nextFrame.id == -1 && currentFrame.id != -1) {
            for (int id : nextFrame.parts.keySet()) {
                if (!nextFrame.parts.containsKey(id)) { continue; }
                nextFrame.parts.get(id).setDisable(currentFrame.parts.get(id).isDisable());
                nextFrame.parts.get(id).setShow(currentFrame.parts.get(id).isShow());
            }
        }

        // movement speed depends on the NPCs movement speed
        float correctorRotations = !activeAnimation.type.isMovement() ? 1.0f : correctSpeed;

        // start sound (ignore unloaded entities in GUI)
        if (animationSound == null &&
                currentFrame.sound != null &&
                !isEdit &&
                !entity.isServerWorld() &&
                entity.world.loadedEntityList.contains(entity)) {
            MusicController.Instance.playSound(SoundCategory.AMBIENT, currentFrame.sound.toString(), (float) entity.posX, (float) entity.posY, (float) entity.posZ, 1.0f, 1.0f);
            animationSound = currentFrame.sound;
        }

        // calculation of exact values for a body part
        rotationAngles.clear();
        if (currentFrame.delay != 0) { ticks -= currentFrame.delay; }
        for (int partId = 0; partId < currentFrame.parts.size(); partId++) {
            PartConfig part0 = currentFrame.parts.get(partId);
            PartConfig part1 = nextFrame.parts.get(partId);
            if (part0.isDisable() || !part0.isShow() || part1 == null) {
                rotationAngles.put(part0.id, null);
                continue;
            }
            Float[] values = new Float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f };
            for (int t = 0; t < 3; t++) { // 0:rotations, 1:offsets, 2:scales
                for (int a = 0; a < 5; a++) { // x, y, z, x1, y1
                    float value_0;
                    float value_1;
                    if (t != 0 && a > 2) { continue; }
                    switch (t) {
                        case 1: {
                            value_0 = part0.offset[a];
                            value_1 = part1.offset[a];
                            break;
                        }
                        case 2: {
                            value_0 = part0.scale[a];
                            value_1 = part1.scale[a];
                            break;
                        }
                        default: {
                            value_0 = part0.rotation[a];
                            value_1 = part1.rotation[a];
                            float result =  value_0 - value_1; // adjusting the nearest number
                            if (Math.abs(result) > Math.PI) { // example: to rotate in a circle
                                value_1 = result;
                            }
                            if (correctorRotations != 1.0f && (partId == 1 || partId == 2 || partId == 4 || partId == 5)) {
                                value_0 *= correctorRotations;
                                value_1 *= correctorRotations;
                            }
                            break;
                        }
                    }
                    int id = t * 3 + a;
                    if (a > 2) { id = 6 + a; } // X1, Y1
                    values[id] = calcValue(value_0, value_1, speed, ticks, currentFrame.isSmooth(), partialTicks);
                }
            }
            rotationAngles.put(part0.id, values);
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

    public void save(NBTTagCompound compound) {
        checkData();
        NBTTagList allAnimations = new NBTTagList();
        for (AnimationKind type : data.keySet()) {
            if (data.get(type).isEmpty()) { continue; }
            NBTTagCompound nbtCategory = new NBTTagCompound();
            nbtCategory.setInteger("Category", type.get());
            NBTTagList animations = new NBTTagList();
            for (int id : data.get(type)) {
                animations.appendTag(new NBTTagInt(id));
            }
            nbtCategory.setTag("Animations", animations);
            allAnimations.appendTag(nbtCategory);
        }
        compound.setTag("AllAnimations", allAnimations);
    }

    /**
     * Server used this method how update event
     */
    public boolean isAnimated() {
        // no animation
        if (activeAnimation == null || stage == EnumAnimationStages.Waiting) { return false; }
        if (activeAnimation.type == AnimationKind.DIES && entity.getHealth() <= 0.0f) { return true; }
        boolean isEdit = activeAnimation.type == AnimationKind.EDITING_All || activeAnimation.type == AnimationKind.EDITING_PART;
        return entity.isServerWorld() ? !completeAnimation : stage != EnumAnimationStages.Waiting || isEdit || activeAnimation.type.isMovement();
    }

    public boolean isAnimated(AnimationKind ... types) {
        if (!isAnimated()) { return false; }
        for (AnimationKind type : types) {
            if (activeAnimation.type == type) { return true; }
        }
        return false;
    }

    public void updateTime() {
        // animation running time
        if (activeAnimation == null) {
            currentFrame = null;
            nextFrame = null;
            completeAnimation = false;
            stage = EnumAnimationStages.Waiting;
            startAnimationTime = 0;
            return;
        }
        int ticks = (int) (entity.world.getTotalWorldTime() - startAnimationTime);
//System.out.println("CNPCs: "+stage+"; "+ticks);
        int speed;
        // Started
        if (stage == EnumAnimationStages.Started) {
            speed = activeAnimation.type.isQuickStart() ? 4 : 10;
            if (ticks == 0) { startEvent(new AnimationEvent.StartEvent(entity, activeAnimation, -1, 0, EnumAnimationStages.Started)); }
            else { startEvent(new AnimationEvent.UpdateEvent(entity, activeAnimation, -1, ticks, EnumAnimationStages.Started)); }
            if (ticks >= speed) {
                startAnimationTime += speed + 1;
                stage = EnumAnimationStages.Run;
            }
            return;
        }
        // Looping
        if (stage == EnumAnimationStages.Looping) {
            speed = activeAnimation.frames.get(activeAnimation.frames.size() - 1).speed;
            if (ticks == 0) { startEvent(new AnimationEvent.StartEvent(entity, activeAnimation, -1, 0, EnumAnimationStages.Looping)); }
            else { startEvent(new AnimationEvent.UpdateEvent(entity, activeAnimation, -1, ticks, EnumAnimationStages.Looping)); }
            if (ticks >= speed) {
                int lastFrameId = activeAnimation.frames.size();
                int frameId = 0;
                if (activeAnimation.repeatLast > 0) {
                    frameId = ValueUtil.correctInt(lastFrameId - activeAnimation.repeatLast, 0, lastFrameId - 1);
                }
                if (frameId == 0) {
                    startAnimationTime = entity.world.getTotalWorldTime() + 1;
                } else {
                    startAnimationTime = entity.world.getTotalWorldTime() + activeAnimation.endingFrameTicks.get(frameId - 1) + 1;
                }
                if (frameId != lastFrameId) {
                    stage = EnumAnimationStages.Run;
                }
            }
            return;
        }
        // Run
        if (stage == EnumAnimationStages.Run) {
            int animationFrame = activeAnimation.getAnimationFrameByTime(ticks);
            if (animationFrame < activeAnimation.frames.size() - 1) {
                int startFrameTime = 0;
                if (activeAnimation.endingFrameTicks.containsKey(animationFrame - 1)) {
                    startFrameTime = activeAnimation.endingFrameTicks.get(animationFrame - 1);
                }
                int frameTime = ticks - startFrameTime;
//System.out.println("CNPCs: "+animationFrame+"/"+(activeAnimation.frames.size()-1)+"; "+ticks+"-"+startFrameTime+"="+frameTime+"/"+activeAnimation.frames.get(animationFrame).speed+"; "+isAnimated());
                if (frameTime == 0) {
                    startEvent(new AnimationEvent.NextFrameEvent(entity, activeAnimation, animationFrame, 0, EnumAnimationStages.Run));
                } else {
                    startEvent(new AnimationEvent.UpdateEvent(entity, activeAnimation, animationFrame, frameTime, EnumAnimationStages.Run));
                }
                if (animationFrame == 0 && frameTime == 0) {
                    boolean isEdit = activeAnimation.type == AnimationKind.EDITING_All || activeAnimation.type == AnimationKind.EDITING_PART;
                    AnimationKind type = activeAnimation.type.getParentEnum() != null ? activeAnimation.type.getParentEnum() : activeAnimation.type;
                    isCyclical = activeAnimation.repeatLast > 0 ||
                            type == AnimationKind.JUMP && isJump ||
                            type.isMovement() && (isEdit || activeAnimation.chance >= 1.0f) ||
                            type == AnimationKind.DIES && (isEdit || entity.getHealth() <= 0.0f);
                }
            }
            else {
                startAnimationTime = entity.world.getTotalWorldTime() + 1;
                if (activeAnimation.type != AnimationKind.EDITING_PART) {
                    stage = isCyclical ? EnumAnimationStages.Looping : EnumAnimationStages.Ending;
                }
                completeAnimation = true;
            }
            return;
        }
        // Ending
        if (stage == EnumAnimationStages.Ending) {
            speed = activeAnimation.type.isQuickStart() ? 4 : 10;
            if (ticks == 0) { startEvent(new AnimationEvent.StartEvent(entity, activeAnimation, -1, 0, EnumAnimationStages.Ending)); }
            else { startEvent(new AnimationEvent.UpdateEvent(entity, activeAnimation, -1, ticks, EnumAnimationStages.Ending)); }
            if (ticks >= speed) {
                if (activeAnimation.type == AnimationKind.EDITING_All) {
                    startAnimationTime = entity.world.getTotalWorldTime() + 1;
                    stage = EnumAnimationStages.Started;
                } else {
                    startAnimationTime = 0;
                    stage = EnumAnimationStages.Waiting;
                }
            }
            return;
        }
        // Waiting
        if (stage == EnumAnimationStages.Waiting) {
            stopAnimation();
        }
    }

    private void startEvent(AnimationEvent event) {
        if (event == null || (event.animation != null && (event.animation.type == AnimationKind.EDITING_All || event.animation.type == AnimationKind.EDITING_PART))) { return; }
        if (event instanceof AnimationEvent.UpdateEvent && entity.isServerWorld()) { return; }
        IScriptHandler handler = null;
        if (!entity.isServerWorld()) { handler = ScriptController.Instance.clientScripts; }
        else if (entity instanceof EntityNPCInterface) { handler = ((EntityNPCInterface) entity).script; }
        else if (entity instanceof EntityPlayer) {
            PlayerData data = PlayerData.get((EntityPlayer) entity);
            if (data != null) { handler = data.scriptData; }
        }
        if (handler != null) { EventHooks.onEvent(handler, event.nameEvent, event); }
    }

    public void load(NBTTagCompound compound) {
        data.clear();
        if (!compound.hasKey("AllAnimations", 9)) { return; }
        AnimationController aData = AnimationController.getInstance();
        for (int c = 0; c < compound.getTagList("AllAnimations", 10).tagCount(); c++) {
            NBTTagCompound nbtCategory = compound.getTagList("AllAnimations", 10).getCompoundTagAt(c);
            int t = nbtCategory.getInteger("Category");
            if (t < 0) { t *= -1; }
            AnimationKind type = AnimationKind.get(t % AnimationKind.values().length);
            if (type == null) {
                LogWriter.warn("Try load AnimationKind ID:"+t+". Missed.");
                continue;
            }
            List<Integer> list = new ArrayList<>();
            int tagType = nbtCategory.getTag("Animations").getId();
            if (tagType == 11) { // OLD version
                for (int id : nbtCategory.getIntArray("Animations")) {
                    if (!list.contains(id)) { list.add(id); }
                }
            }
            else if (tagType == 9) { // NEW version
                int listType = ((NBTTagList) nbtCategory.getTag("Animations")).getTagType();
                if (listType == 10 && entity != null && this.entity.isServerWorld()) { // OLD in main CNPCs mod
                    for (int i = 0; i < nbtCategory.getTagList("Animations", 10).tagCount(); i++) {
                        NBTTagCompound nbt = nbtCategory.getTagList("Animations", 10).getCompoundTagAt(i);
                        int id = nbt.getInteger("ID");
                        String name = entity.getName() + "_" + nbt.getString("Name");
                        AnimationConfig anim = (AnimationConfig) aData.getAnimation(id);
                        if (entity.world.getEntityByID(entity.getEntityId()) != null && anim == null || !anim.getName().equals(name)) {
                            boolean found = false;
                            if (anim != null) {
                                for (AnimationConfig ac : aData.animations.values()) {
                                    if (ac.name.equals(anim.name)) {
                                        found = true;
                                        anim = ac;
                                    }
                                }
                            }
                            if (!found) { // Converting
                                anim = (AnimationConfig) aData.createNewAnim();
                                id = anim.id;
                                if (!anim.immutable) { anim.load(nbt); }
                                anim.name = name;
                                anim.id = id;
                                Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, EnumSync.AnimationData, anim.save());
                            }
                        }
                        if (!list.contains(id)) { list.add(id); }
                    }
                }
                else if (listType == 3) { // NOW
                    for (int i = 0; i < nbtCategory.getTagList("Animations", 3).tagCount(); i++) {
                        int id = nbtCategory.getTagList("Animations", 3).getIntAt(i);
                        if (!list.contains(id)) { list.add(id); }
                    }
                }
            }
            Collections.sort(list);
            data.put(type, list);
        }
        checkData();
    }

    public AnimationConfig selectAnimation(AnimationKind type) {
        if (!CustomNpcs.ShowCustomAnimation || !entity.isServerWorld() || data.get(type).isEmpty()) { return null; }
        // get all animation settings
        List<AnimationConfig> list = AnimationController.getInstance().getAnimations(data.get(type));
        if (list.isEmpty()) { return null; }
        // check waits
        List<AnimationConfig> selectList = new ArrayList<>();
        for (AnimationConfig ac : list) {
            if (waitData.containsKey(ac.id) && waitData.get(ac.id) > System.currentTimeMillis()) {
                continue;
            }
            float f = this.rnd.nextFloat();
            if (ac.chance <= f) {
                waitData.put(ac.id, System.currentTimeMillis() + 1000);
                continue;
            }
            selectList.add(ac);
        }
        AnimationConfig anim = null;
        if (!selectList.isEmpty()) { anim = selectList.get(rnd.nextInt(selectList.size())).copy(); }
        if (anim == null && type == AnimationKind.ATTACKING && !list.isEmpty()) { anim = list.get(rnd.nextInt(list.size())).copy(); }
        return anim;
    }

    // (Player or NPC) -> this.reset(AnimationKind), or PacketHandlerClient, or Animation GUI
    public AnimationConfig tryRunAnimation(AnimationConfig anim, AnimationKind type) {
        if (anim == null || anim.frames.isEmpty()) { return null; }
        runAnimation(anim, type);
        // remember option
        isJump = type == AnimationKind.JUMP;
        isSwing = type == AnimationKind.SWING;
        // special settings
        if (type == AnimationKind.DIES) {
            entity.motionX = 0.0d;
            entity.motionY = 0.0d;
            entity.motionZ = 0.0d;
        }
        return activeAnimation;
    }

    // run new movement animation
    private void runAnimation(AnimationConfig anim, AnimationKind type) {
        boolean isEdit = type == AnimationKind.EDITING_All || type == AnimationKind.EDITING_PART;
        if (entity.isServerWorld() && isEdit) { return; }
        type.setEditingBooleans(anim.type);
        if (activeAnimation != null) {
            if (!isEdit && activeAnimation.id == anim.id) { return; }
        }
        activeAnimation = anim.copy();
        activeAnimation.type = type;
        stage = EnumAnimationStages.Started;
        if (type == AnimationKind.EDITING_PART) { stage = EnumAnimationStages.Run; }
        startAnimationTime = entity.world.getTotalWorldTime();
        completeAnimation = false;
    }

    public Map<Integer, Integer> resetWalkAndStandAnimations() {
        if (!entity.isServerWorld() || (activeAnimation != null && !completeAnimation)) { return null; }

        boolean isChanged = false;

        for (AnimationKind ak : AnimationKind.values()) {
            if (!ak.isMovement()) { continue; }
            if (!data.get(ak).isEmpty()) {
                AnimationConfig anim = selectAnimation(ak);
                if (anim != null && !anim.equals(movementAnimation.get(ak))) {
                    movementAnimation.put(ak, anim);
                    isChanged = true;
                }
            } else if (movementAnimation.containsKey(ak)) {
                movementAnimation.remove(ak);
                isChanged = true;
            }
        }

        if (!isChanged) { return null; }

        Map<Integer, Integer> map = new HashMap<>();
        for (AnimationKind ak : movementAnimation.keySet()) {
            map.put(ak.ordinal(), movementAnimation.get(ak).id);
        }
        return map;
    }

    public void loadBaseAnimations(Map<Object, Object> map) {
        AnimationController aData = AnimationController.getInstance();
        movementAnimation.clear();
        for (Object key : map.keySet()) {
            if (!(key instanceof Integer) || !(map.get(key) instanceof Integer)) { continue; }
            AnimationConfig anim = aData.animations.get((int) map.get(key));
            if (anim != null) { movementAnimation.put(AnimationKind.get((int) key), anim.copy()); }
        }
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ignoredAgeInTicks, float ignoredNetHeadYaw, float ignoredHeadPitch, float ignoredScaleFactor, float partialTicks) {
        AnimationKind base = null;
        float correctSpeed = 1.0f;
        boolean isMoving = limbSwing > 0 && limbSwingAmount > 0;
        if (isMoving) { correctSpeed = limbSwingAmount / limbSwing; }
        if (!movementAnimation.isEmpty() && (activeAnimation == null || activeAnimation.type.isMovement())) {
            // try to get AIM
            if (movementAnimation.containsKey(AnimationKind.AIM)) {
                if (entity instanceof EntityPlayer) {
                    if (entity.getActiveItemStack().getItem() instanceof ItemBow) { base = AnimationKind.AIM; }
                } else {
                    if (((EntityNPCInterface) entity).currentAnimation == 6) { base = AnimationKind.AIM; }
                }
            }
            // try to get REVENGE
            if (base == null) {
                if (entity instanceof EntityPlayer) {
                    if (entity.getLastAttackedEntityTime() - entity.ticksExisted < 300) {
                        if (isMoving) { base = AnimationKind.REVENGE_WALK; } else { base = AnimationKind.REVENGE_STAND; }
                    }
                } else {
                    if (((EntityNPCInterface) entity).isAttacking()) {
                        if (isMoving) { base = AnimationKind.REVENGE_WALK; } else { base = AnimationKind.REVENGE_STAND; }
                    }
                }
                if (base != null && !movementAnimation.containsKey(base)) { base = null; }
            }
            // try to get ANY
            if (base == null) {
                // NORMAL
                if (entity.onGround) {
                    if (isMoving) { base = AnimationKind.WALKING; } else { base = AnimationKind.STANDING; }
                    if (!movementAnimation.containsKey(base)) { base = null; }
                }
                // FLY
                if (base == null) {
                    if (entity instanceof EntityPlayer) {
                        if (((EntityPlayer) entity).capabilities.allowFlying) {
                            if (isMoving) { base = AnimationKind.FLY_WALK; } else { base = AnimationKind.FLY_STAND; }
                        }
                    } else {
                        if (((EntityNPCInterface) entity).ais.getNavigationType() == 1) {
                            if (isMoving) { base = AnimationKind.FLY_WALK; } else { base = AnimationKind.FLY_STAND; }
                        }
                    }
                    if (!movementAnimation.containsKey(base)) { base = null; }
                }
                // WATER
                if (base == null) {
                    if (entity.isInWater() || entity.isInLava() || entity.world.isMaterialInBB(entity.getEntityBoundingBox().grow(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), Material.WATER)) {
                        if (isMoving) { base = AnimationKind.WATER_WALK; } else { base = AnimationKind.WATER_STAND; }
                        if (!movementAnimation.containsKey(base)) { base = null; }
                    }
                    if (base == null && entity instanceof EntityNPCInterface && ((EntityNPCInterface) entity).ais.getNavigationType() == 2) {
                        if (isMoving) { base = AnimationKind.WATER_WALK; } else { base = AnimationKind.WATER_STAND; }
                        if (!movementAnimation.containsKey(base)) { base = null; }
                    }
                }
                // BASE
                if (base == null && movementAnimation.containsKey(AnimationKind.BASE)) {
                    base = AnimationKind.BASE;
                }
            }
        }
        if (base != null && (activeAnimation == null || (activeAnimation.type.isMovement() && activeAnimation.id != movementAnimation.get(base).id))) {
            tryRunAnimation(movementAnimation.get(base), base);
        }
        if (isAnimated()) { calculationAnimationData(correctSpeed, partialTicks); }
    }

    public void addAnimation(AnimationKind type, int id) {
        data.get(type).add(id);
    }

    public boolean removeAnimation(AnimationKind type, int id) {
        return data.get(type).remove(Integer.valueOf(id));
    }

    public boolean hasAnimation(AnimationKind type, int id) {
        return data.get(type).contains(id);
    }

}
