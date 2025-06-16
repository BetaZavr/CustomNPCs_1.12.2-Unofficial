package noppes.npcs.client.layer;

import noppes.npcs.util.ValueUtil;

public class EyeRenderData {

    private final boolean isLeft;
    private boolean isUpdate = false;
    public boolean isShow = true;

    // Eye
    public final float[] eyeColor = new float[] { 1.0f, 1.0f, 1.0f };
    public float eyePosX = 0.0f;
    public float eyePosY = 0.0f;
    public float eyeScaleX = 1.0f;
    public float eyeScaleY = 1.0f;
    public float eyeWeight = 2.0f;
    public float eyeHeight = 1.0f;
    public float eyeHoverY;

    // Pupil
    public final float[] pupilColor = new float[] { 1.0f, 1.0f, 1.0f };
    public double pupilLeft;
    public double pupilTop;
    public double pupilRight;
    public double pupilBottom;
    public double pupilX;
    public double pupilY;
    public double pupilScaleX;
    public double pupilScaleY;

    // Center
    public double centerLeft;
    public double centerTop;
    public double centerRight;
    public double centerBottom;

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
        if (eyes.type == 2 && !isLeft) { eyePosX -= 2.0f; }
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
        float x = data[4];
        float y = data[5];
        if ((isLeft && !eyes.activeLeft) || (!isLeft && !eyes.activeRight) || isDisableMoved) { x = 0.0f; y = 0.0f; }

        pupilLeft = -0.5;
        pupilRight = 0.5;
        double ySize = eyes.type == 1 ? 0.85f : 0.5f;
        pupilTop = -ySize;
        pupilBottom = ySize;

        if (eyes.type == 2) {
            pupilColor[0] = (float)(eyes.pupilColor[1] >> 16 & 255) / 127.5F;
            pupilColor[1] = (float)(eyes.pupilColor[1] >> 8 & 255) / 127.5F;
            pupilColor[2] = (float)(eyes.pupilColor[1] & 255) / 127.5F;
            pupilX = 0.0;
            pupilY = 0.0;
            if (x != 0.0 || y != 0.0) {
                double maxRadiusSquare = 1.0;
                double ellipseMajorAxis = 0.45;
                double ellipseMinorAxis = 0.3;
                // polar coordinates
                double radiusSquare = ValueUtil.correctDouble(Math.hypot(x, y), -1.0, 1.0);
                double angleTheta = Math.atan2(y, x);
                // радиус эллипса для текущего угла
                double numerator = ellipseMajorAxis * ellipseMinorAxis;
                double denominator = Math.sqrt(Math.pow(ellipseMinorAxis, 2) * Math.cos(angleTheta) * Math.cos(angleTheta) + Math.pow(ellipseMajorAxis, 2) * Math.sin(angleTheta) * Math.sin(angleTheta));
                double radiusEllipse = numerator / denominator;
                // radius correction
                double correctedRadius = radiusEllipse * radiusSquare / maxRadiusSquare;
                // return cartesian coordinates
                pupilX = correctedRadius * Math.cos(angleTheta);
                pupilY = correctedRadius * Math.sin(angleTheta);
            }
            //LogWriter.info("TEST: ["+x+", "+y+"]; ["+pupilX+", "+pupilY+"]");
        } else {
            // X
            pupilX = (isLeft ? 1.0 : -1.0) + x;
            if (pupilX <= 0.5 * pupilScaleX - (!isLeft ? 2.0 : 0.0)) {
                if (isLeft) { pupilLeft = pupilX / -pupilScaleX; }
                else { pupilLeft = (2.0 + pupilX) / -pupilScaleX; }
            }
            if (x > 1.0 - 0.5 * pupilScaleX) {
                if (isLeft) { pupilRight = (2.0 - pupilX) / pupilScaleX; }
                else { pupilRight = pupilX / -pupilScaleX; }
            }
            // Y
            if (eyes.type == 1) { pupilY = 0.85 + y; }
            else { pupilY = 0.5 + y / 2.0; }
            if (pupilY < ySize* pupilScaleY) { pupilTop = pupilY / -pupilScaleY; }
            double start = ySize - ySize * (pupilScaleY - 1.0);
            if (pupilY > start) {
                double e0 = (eyes.type == 1 ? ySize - 1.0 : ySize - 0.5) / pupilScaleY;
                double e1 = eyes.type == 1 ? ySize + 1.0 : ySize + 0.5;
                double a = (ySize - e0) / (start - e1) ;
                double b = ySize - a * start;
                pupilBottom = a * pupilY + b;
            }
        }


        // Center
        double size = 0.2;
        centerLeft = -size;
        centerTop = -size;
        centerRight = size;
        centerBottom = size;
        if (centerLeft < pupilLeft) { centerLeft = pupilLeft; }
        if (centerRight > pupilRight) { centerRight = pupilRight; }
        if (centerTop < pupilTop) { centerTop = pupilTop; }
        if (centerBottom > pupilBottom) { centerBottom = pupilBottom; }

        // Glint
        if (eyes.glint) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
            glintShow = false;
            glintAlpha = 1.0f;
            x = data[6];
            y = data[7];
            if (mc.world != null) {
                long time = mc.world.getWorldTime();
                float npcRot = npc.rotationYawHead % 360.0f;
                if (npcRot < 0) { npcRot += 360.0f; }
                float height = 0.65f * (float) ySize;
                float lightYaw;
                if (time <= 23200 && time > 12800) { // night
                    lightYaw = Math.round(-0.017308f * (float) time + 491.538462f);
                    glintAlpha = -0.096154f * Math.abs((float) time - 18000.0f) / 1000.0f + 1.0f;
                    if (time >= 18000) {
                        glintShow = time < 21000 || (npcRot >= 30 && npcRot <= 150);
                        if (glintShow) {
                            if (npcRot >= 210 && npcRot <= 330) {
                                x = ValueUtil.correctFloat(0.005417f * npcRot - 0.4875f, -height, height);
                                y = ValueUtil.correctFloat(-0.003611f * lightYaw + height, -height, 0.0f);
                            } else {
                                x = 0.0f;
                                y = -height;
                            }
                        }
                    }
                    else {
                        glintShow = time > 15200 || (npcRot >= 210 && npcRot <= 330);
                        if (glintShow) {
                            if (npcRot >= 210 && npcRot <= 330) {
                                x = ValueUtil.correctFloat(0.005417f * npcRot - 1.4625f, -height, height);
                                y = ValueUtil.correctFloat(0.003611f * (lightYaw - 180.0f) - height, -height, 0.0f);
                            } else {
                                x = 0.0f;
                                y = -height;
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
                                x = ValueUtil.correctFloat(0.005417f * npcRot - 0.4875f, -height, height);
                                y = ValueUtil.correctFloat(-0.003611f * lightYaw + height, -height, 0.0f);
                            } else {
                                x = 0.0f;
                                y = -height;
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
                                x = ValueUtil.correctFloat(0.005417f * npcRot - 1.4625f, -height, height);
                                y = ValueUtil.correctFloat(0.003611f * (lightYaw - 180.0f) - height, -height, 0.0f);
                            } else {
                                x = 0.0f;
                                y = -height;
                            }
                        }
                    }
                }
            }
            glintLeft = -0.1 + x;
            glintRight = 0.1 + x;
            glintTop = -0.1 + y;
            glintBottom = 0.1 + y;
            if (glintLeft < pupilLeft) { glintLeft = pupilLeft; }
            if (glintRight > pupilRight) { glintRight = pupilRight; }
            if (glintTop < pupilTop) { glintTop = pupilTop; }
            if (glintBottom > pupilBottom) { glintBottom = pupilBottom; }
            if (glintLeft > glintRight || glintLeft == glintRight) { glintShow = false; }
            if (glintTop > glintBottom || glintTop == glintBottom) { glintShow = false; }

            if (glintShow) {
                if (eyes.type != 2) { glintColor = 0xFFFFFF | (int) Math.ceil(glintAlpha * 255.0F) << 24; }
            } else if (eyes.type == 2) { glintShow = true; }
        }
        isUpdate = false;
    }

}
