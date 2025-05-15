package noppes.npcs.client.layer;

import noppes.npcs.client.model.animation.EmotionFrame;

public class BrowsRenderData {

    public boolean showLeft = true;
    public boolean showRight = true;
    public boolean isBlink = false;

    public float offsetBrowLeftX = 1.0f;
    public float offsetBrowLeftY = 0.0f;
    public float offsetBrowRightX = -1.0f;
    public float offsetBrowRightY = 0.0f;
    public float offsetEyeLeftX = 1.0f;
    public float offsetEyeLeftY = 0.0f;
    public float offsetEyeRightX = -1.0f;
    public float offsetEyeRightY = 0.0f;

    public float scaleBrowLeftX = 1.0f;
    public float scaleBrowLeftY = 1.0f;
    public float scaleBrowRightX = 1.0f;
    public float scaleBrowRightY = 1.0f;
    public float scaleEyeLeftX = 1.0f;
    public float scaleEyeLeftY = 1.0f;
    public float scaleEyeRightX = 1.0f;
    public float scaleEyeRightY = 1.0f;

    public float leftBlinkY = 1.0f;
    public float rightBlinkY = 1.0f;
    public float thickness = 0.0f;
    public float scaleBrow = 1.0f;

    public final float[] leftColor = new float[] { 1.0f, 1.0f, 1.0f };
    public final float[] rightColor = new float[] { 1.0f, 1.0f, 1.0f };

    public void update(noppes.npcs.entity.EntityCustomNpc npc, noppes.npcs.client.model.part.ModelEyeData eyes, Float[] browRight, Float[] browLeft, Float[] eyeRight, Float[] eyeLeft, EmotionFrame frame) {
        float oYl = 0.0f, oYr = 0.0f;
        int cld = eyes.closed;
        if (npc.getHealth() <= 0.0f || npc.isPlayerSleeping()) { cld = 1; }
        float offsetBrowUp = 0.0f;
        if (eyes.type == 1) { offsetBrowUp = 0.3f; }

        offsetBrowLeftX = 1.0f;
        offsetBrowLeftY = 0.0f;
        offsetBrowRightX = -1.0f;
        offsetBrowRightY = 0.0f;
        offsetEyeLeftX = 1.0f;
        offsetEyeLeftY = 0.0f;
        offsetEyeRightX = -1.0f;
        offsetEyeRightY = 0.0f;

        scaleBrowLeftX = 1.0f;
        scaleBrowLeftY = 1.0f;
        scaleBrowRightX = 1.0f;
        scaleBrowRightY = 1.0f;
        scaleEyeLeftX = 1.0f;
        scaleEyeLeftY = 1.0f;
        scaleEyeRightX = 1.0f;
        scaleEyeRightY = 1.0f;

        if (browRight != null) {
            offsetBrowRightX = browRight[0];
            offsetBrowRightY = browRight[1];
            scaleBrowRightX = browRight[2];
            scaleBrowRightY = browRight[3];
        }
        if (browLeft != null) {
            offsetBrowLeftX = browLeft[0];
            offsetBrowLeftY = browLeft[1];
            scaleBrowLeftX = browLeft[2];
            scaleBrowLeftY = browLeft[3];
        }
        if (eyeRight != null) {
            offsetEyeRightX = eyeRight[0];
            offsetEyeRightY = eyeRight[1];
            scaleEyeRightX = eyeRight[2];
            scaleEyeRightY = eyeRight[3];
        }
        if (eyeLeft != null) {
            offsetEyeLeftX = eyeLeft[0];
            offsetEyeLeftY = eyeLeft[1];
            scaleEyeLeftX = eyeLeft[2];
            scaleEyeLeftY = eyeLeft[3];
        }

        offsetEyeLeftY += offsetBrowUp - 5.0f;
        offsetEyeRightY += offsetBrowUp - 5.0f;

        // skin close
        boolean close = false;
        if (frame != null) { close = eyes.ticks > 3 && frame.isEndBlink(); }

        showLeft = cld == 1 || cld == 2 || close;
        showRight = cld == 1 || cld == 2 || close;

        // skin blink
        isBlink = cld != 1 && eyes.blinkStart > 0L && npc.isEntityAlive() && npc.deathTime == 0;
        if (isBlink) {
            float f = (System.currentTimeMillis() - eyes.blinkStart) / 150.0f;
            if (f > 1.0f) { f = 2.0f - f; }
            if (f < 0.0f) {
                eyes.blinkStart = 0L;
                f = 0.0f;
            }
            leftBlinkY = ((eyes.type != 0) ? 2.0f : 1.0f) * f;
            rightBlinkY = ((eyes.type != 0) ? 2 : 1) * f;
        }

        // brow
        if (eyes.browThickness == 0) { return; }

        thickness = eyes.browThickness / 10.0f;
        oYl *= 0.075f;
        oYr *= 0.075f;
        if (eyes.type == 0 ) { oYl -= 0.35f; oYr -= 0.35f; }

        offsetBrowLeftY += oYl - 4.8f;
        offsetBrowRightY += oYr - 4.8f;

        if (eyes.type == 2) {
            scaleBrow = 0.0035f * ((eyes.browThickness - 1.0f) * 0.166667f + 0.333333f);
            leftColor[0] = (float)(eyes.browColor[0] >> 16 & 255) / 127.5F;
            leftColor[1] = (float)(eyes.browColor[0] >> 8 & 255) / 127.5F;
            leftColor[2] = (float)(eyes.browColor[0] & 255) / 127.5F;

            rightColor[0] = (float)(eyes.browColor[1] >> 16 & 255) / 127.5F;
            rightColor[1] = (float)(eyes.browColor[1] >> 8 & 255) / 127.5F;
            rightColor[2] = (float)(eyes.browColor[1] & 255) / 127.5F;
        }
    }

}
