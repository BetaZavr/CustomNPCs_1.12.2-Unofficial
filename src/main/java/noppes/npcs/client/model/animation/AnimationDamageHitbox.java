package noppes.npcs.client.model.animation;

import net.minecraft.nbt.*;
import net.minecraft.util.math.AxisAlignedBB;

public class AnimationDamageHitbox {

    public float[] offset = new float[] { 1.2f, 0.8f, 0.0f }; // [ D:radius, H:height, W:addYaw ]
    public float[] scale = new float[] { 1.2f, 1.5f, 1.2f }; // [ x, y, z ]
    public int id = 0;

    public AnimationDamageHitbox(int id) { this.id = id; }

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

    public AnimationDamageHitbox(NBTTagCompound compound, int id) {
        this.id = id;
        if (compound.hasKey("Offset", 9) && compound.getTagList("Offset", 9).getTagType() == 5 && compound.getTagList("Offset", 9).tagCount() > 2) {
            NBTTagList list = compound.getTagList("Offset", 5);
            offset[0] = list.getFloatAt(0);
            offset[1] = list.getFloatAt(1);
            offset[2] = list.getFloatAt(2);
        }
        if (compound.hasKey("Scale", 9) && compound.getTagList("Scale", 9).getTagType() == 5 && compound.getTagList("Scale", 9).tagCount() > 2) {
            NBTTagList list = compound.getTagList("Scale", 5);
            scale[0] = list.getFloatAt(0);
            scale[1] = list.getFloatAt(1);
            scale[2] = list.getFloatAt(2);
        }
    }

    public AxisAlignedBB getScaledDamageHitbox() {
        return new AxisAlignedBB(-0.5d * scale[0], -0.5d * scale[1], -0.5d * scale[2], 0.5d * scale[0], 0.5d * scale[1], 0.5d * scale[2]);
    }

    public String getKey() {
        AxisAlignedBB damageHitbox = getScaledDamageHitbox();
        char c = (char) 167;
        return c + "7ID: " + c + "r" + id +
                c + "7 [" + c + "a" + Math.round((damageHitbox.maxX - damageHitbox.minX) * 1000.0d) / 1000.0d +
                c + "7, " + c + "a" + Math.round((damageHitbox.maxY - damageHitbox.minY) * 1000.0d) / 1000.0d +
                c + "7, " + c + "a" + Math.round((damageHitbox.maxZ - damageHitbox.minZ) * 1000.0d) / 1000.0d +
                c + "7]";
    }
}
