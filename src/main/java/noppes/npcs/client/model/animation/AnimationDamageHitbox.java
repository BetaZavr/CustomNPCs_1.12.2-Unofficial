package noppes.npcs.client.model.animation;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.*;
import net.minecraft.util.math.AxisAlignedBB;

public class AnimationDamageHitbox {

    public float[] offset = new float[] { 1.2f, 0.95f, 0.0f }; // [ D:radius, H:height, W:addYaw ]
    public float[] scale = new float[] { 1.2f, 1.5f, 1.2f }; // [ x, y, z ]
    public int id;

    public AnimationDamageHitbox(int i) { id = i; }


    public void clear() {
        offset[0] = 1.2f;
        offset[1] = 0.95f;
        offset[2] = 0.0f;
        for (int i = 0; i < 3; i++) {
            scale[i] = 1.0f;
        }
    }

    public NBTTagCompound getNBT() {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setInteger("ID", id);

        NBTTagList listO = new NBTTagList();
        NBTTagList listS = new NBTTagList();
        for (int i = 0; i < 3; i++) {
            listO.appendTag(new NBTTagFloat(offset[i]));
            listS.appendTag(new NBTTagFloat(scale[i]));
        }
        compound.setTag("Offset", listO);
        compound.setTag("Scale", listS);

        return compound;
    }

    public AnimationDamageHitbox(NBTTagCompound compound, int i) {
        id = i;
        NBTTagList listO = compound.getTagList("Offset", 5);
        for (int j = 0; j < 3 && j < listO.tagCount(); j++) { offset[j] = listO.getFloatAt(j); }
        NBTTagList listS = compound.getTagList("Scale", 5);
        for (int j = 0; j < 3 && j < listS.tagCount(); j++) { scale[j] = listS.getFloatAt(j); }
    }

    public AxisAlignedBB getScaledDamageHitbox(EntityLivingBase entity) {
        AxisAlignedBB aabb = new AxisAlignedBB(-0.5d * scale[0], -0.5d * scale[1], -0.5d * scale[2], 0.5d * scale[0], 0.5d * scale[1], 0.5d * scale[2]).offset(entity.posX, entity.posY, entity.posZ);
        double radYaw = Math.toRadians(entity.rotationYaw) + offset[2];
        return aabb.offset(Math.sin(radYaw) * -offset[0], offset[1], Math.cos(radYaw) * offset[0]);
    }

    public String getKey() {
        AxisAlignedBB damageHitbox = new AxisAlignedBB(-0.5d * scale[0], -0.5d * scale[1], -0.5d * scale[2], 0.5d * scale[0], 0.5d * scale[1], 0.5d * scale[2]);
        char c = (char) 167;
        return c + "7ID:" + c + "r" + (id + 1) +
                c + "7; d:" + c + "a" + Math.round(offset[0] * 10.0d) / 10.0d +
                c + "7, h:" + c + "a" + Math.round(offset[1] * 10.0d) / 10.0d +
                c + "7, w:" + c + "a" + Math.round(offset[2] * 10.0d) / 10.0d +
                c + "7";
    }

}
