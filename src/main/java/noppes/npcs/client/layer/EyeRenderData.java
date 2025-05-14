package noppes.npcs.client.layer;

import noppes.npcs.util.ValueUtil;

public class EyeRenderData {

    private final boolean isLeft;
    private boolean isUpdate = false;
    public boolean isShow = true;

    // Eye
    public final float[] eyeColor = new float[] { 1.0f, 1.0f, 1.0f};
    public float eyePosX = 0.0f;
    public float eyePosY = 0.0f;
    public float eyeScaleX = 1.0f;
    public float eyeScaleY = 1.0f;
    public float eyeWeight = 2.0f;
    public float eyeHeight = 1.0f;
    public float eyeHoverY;

    // Pupil
    public final float[] pupilColor = new float[] { 1.0f, 1.0f, 1.0f};
    public double pupilLeft;
    public double pupilTop;
    public double pupilRight;
    public double pupilBottom;
    public double pupilX;
    public double pupilY;
    public double pupilScaleX;
    public double pupilScaleY;

    // Glint
    public boolean glintShow = true;
    public float glintAlpha = 1.0f;
    public int glintColor = 0xFFFFFFFF;
    public double glintLeft = -0.1;
    public double glintRight = 0.1;
    public double glintTop = -0.1;
    public double glintBottom = 0.1;

    public EyeRenderData(boolean isLeftIn) {
        isLeft = isLeftIn;
    }

    public void update(noppes.npcs.entity.EntityCustomNpc npc, noppes.npcs.client.model.part.ModelEyeData eyes, int closedType, float[] data, Float[] eyeData, Float[] pupilData, boolean isDisableMoved) {
        isShow = eyes.closed != 1 && eyes.closed != closedType && eyes.pattern != 1 && npc.isEntityAlive() && !npc.isPlayerSleeping();
        if (!isShow) { return; }

        if (isUpdate) { return; }
        isUpdate = true;

        // Eye
        eyePosX = isLeft ? 1.0f : -1.0f;
        eyePosY = -5.0f + data[0];
        eyeScaleX = 1.0f;
        eyeScaleY = 1.0f;
        if (eyeData != null && !isDisableMoved) {
            eyePosX += eyeData[0];
            eyePosY += eyeData[1];
            eyeScaleX = eyeData[2];
            eyeScaleY = eyeData[3];
        }
        eyeWeight = isLeft ? 2.0f : -2.0f;
        eyeHeight = 1.0f + data[1] - data[0];
        eyeHoverY = data[1] - data[0];
        if (eyes.type == 2) {
            eyeColor[0] = (float)(eyes.eyeColor[1] >> 16 & 255) / 127.5F;
            eyeColor[1] = (float)(eyes.eyeColor[1] >> 8 & 255) / 127.5F;
            eyeColor[2] = (float)(eyes.eyeColor[1] & 255) / 127.5F;
        }

        // Pupil
        pupilScaleX = 1.0f;
        pupilScaleY = 1.0f;
        if (pupilData != null && !isDisableMoved) {
            pupilScaleX = pupilData[2];
            pupilScaleY = pupilData[3];
        }
        if (!eyes.activeLeft || isDisableMoved) { data[4] = 0.0f; data[5] = 0.0f; }
        pupilLeft = -0.5;
        pupilRight = 0.5;
        pupilTop = eyes.type == 1 ? -0.85 : -0.5;
        pupilBottom = eyes.type == 1 ? 0.85 : 0.5;
        // X
        pupilX = (isLeft ? 1.0 : -1.0) + data[4];
        if (data[4] < -0.5 / pupilScaleX) {
            if (isLeft) { pupilLeft = pupilX / -pupilScaleX; }
            else { pupilLeft = (2.0 + pupilX) / -pupilScaleX; }
        }
        else if (data[4] > -0.5 * pupilScaleX + 1.0) {
            if (isLeft) { pupilRight = (2.0 - pupilX) / pupilScaleX; }
            else { pupilRight = pupilX / -pupilScaleX; }
        }
        // Y
        if (eyes.type == 1) { pupilY = 0.85 + data[5]; }
        else { pupilY = 0.5 + data[5] / 2.0; }
        if (data[5] < 0) { pupilTop = pupilY / -pupilScaleY; }
        else if (data[5] > 0) { pupilBottom = (pupilBottom - pupilTop - pupilY) / pupilScaleY; }
        if (eyes.type == 2) {
            pupilColor[0] = (float)(eyes.pupilColor[1] >> 16 & 255) / 127.5F;
            pupilColor[1] = (float)(eyes.pupilColor[1] >> 8 & 255) / 127.5F;
            pupilColor[2] = (float)(eyes.pupilColor[1] & 255) / 127.5F;
        }

        // Glint
        if (eyes.glint) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
            glintShow = false;
            glintAlpha = 1.0f;
            if (mc.world != null) {
                long time = mc.world.getWorldTime();
                float npcRot = npc.rotationYawHead % 360.0f;
                if (npcRot < 0) { npcRot += 360.0f; }

                float lightYaw;
                if (time <= 23200 && time > 12800) { // night
                    lightYaw = Math.round(-0.017308f * (float) time + 491.538462f);
                    glintAlpha = -0.096154f * Math.abs((float) time - 18000.0f) / 1000.0f + 1.0f;
                    if (time >= 18000) {
                        glintShow = time < 21000 || (npcRot >= 30 && npcRot <= 150);
                        if (glintShow) {
                            if (npcRot >= 210 && npcRot <= 330) {
                                data[6] = ValueUtil.correctFloat(0.005417f * npcRot - 0.4875f, -0.325f, 0.325f);
                                data[7] = ValueUtil.correctFloat(-0.003611f * lightYaw + 0.325f, -0.325f, 0.0f);
                            } else {
                                data[6] = 0.0f;
                                data[7] = -0.325f;
                            }
                        }
                    }
                    else {
                        glintShow = time > 15200 || (npcRot >= 210 && npcRot <= 330);
                        if (glintShow) {
                            if (npcRot >= 210 && npcRot <= 330) {
                                data[6] = ValueUtil.correctFloat(0.005417f * npcRot - 1.4625f, -0.325f, 0.325f);
                                data[7] = ValueUtil.correctFloat(0.003611f * (lightYaw - 180.0f) - 0.325f, -0.325f, 0.0f);
                            } else {
                                data[6] = 0.0f;
                                data[7] = -0.325f;
                            }
                        }
                    }
                }
                else {
                    if (time >= 6000 && time < 12800) {
                        glintAlpha = -0.073529f * Math.abs((float) time - 6000.0f) / 1000.0f + 1.0f;
                        lightYaw = Math.round(-0.013235f * (float) time + 259.411765f);
                        glintShow = time < 9000 || (npcRot >= 30 && npcRot <= 150);
                        if (glintShow) {
                            if (npcRot >= 30 && npcRot <= 150) {
                                data[6] = ValueUtil.correctFloat(0.005417f * npcRot - 0.4875f, -0.325f, 0.325f);
                                data[7] = ValueUtil.correctFloat(-0.003611f * lightYaw + 0.325f, -0.325f, 0.0f);
                            } else {
                                data[6] = 0.0f;
                                data[7] = -0.325f;
                            }
                        }
                    }
                    else {
                        if (time > 23200) { time -= 23200; }
                        else { time += 800; }
                        glintAlpha = -0.073529f * Math.abs((float) time - 6800.0f) / 1000.0f + 1.0f;
                        lightYaw = Math.round(-0.013237f * (float) time + 270.0f);
                        glintShow = time > 6000 || (npcRot >= 210 && npcRot <= 330);
                        if (glintShow) {
                            if (npcRot >= 210 && npcRot <= 330) {
                                data[6] = ValueUtil.correctFloat(0.005417f * npcRot - 1.4625f, -0.325f, 0.325f);
                                data[7] = ValueUtil.correctFloat(0.003611f * (lightYaw - 180.0f) - 0.325f, -0.325f, 0.0f);
                            } else {
                                data[6] = 0.0f;
                                data[7] = -0.325f;
                            }
                        }
                    }
                }
            }
            glintLeft = -0.1 + data[6];
            glintRight = 0.1 + data[6];
            glintTop = -0.1 + data[7];
            glintBottom = 0.1 + data[7];
            if (glintShow && eyes.type != 2) {
                glintColor = 0xFFFFFF | (int) Math.ceil(glintAlpha * 255.0F) << 24;
            }
        }
        isUpdate = false;
    }

}
