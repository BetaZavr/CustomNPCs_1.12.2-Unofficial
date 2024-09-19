package noppes.npcs.util;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import noppes.npcs.constants.EnumParts;

import java.util.Objects;
import java.util.UUID;

public class ModData {

    private static NBTTagCompound exampleBlocks;
    private static NBTTagCompound exampleItems;
    private static NBTTagCompound exampleParticles;

    public static NBTTagCompound getExampleBlocks() {
        if (exampleBlocks == null) {
            exampleBlocks = new NBTTagCompound();
                NBTTagList listBlocks = new NBTTagList();
                listBlocks.appendTag(getExampleBlock());
                listBlocks.appendTag(getExampleFacingBlock());
                listBlocks.appendTag(getExampleLiquid());
                listBlocks.appendTag(getExampleChest());
                listBlocks.appendTag(getExampleContainer());
                listBlocks.appendTag(getExampleStairs());
                listBlocks.appendTag(getExampleSlab());
                listBlocks.appendTag(getExamplePortal());
                listBlocks.appendTag(getExampleDoor());
            exampleBlocks.setTag("Blocks", listBlocks);
        }
        return exampleBlocks;
    }

    private static NBTTagCompound getExampleBlock() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "blockexample");
        compound.setByte("BlockType", (byte) 0);
        compound.setFloat("Hardness", 5.0f);
        compound.setFloat("Resistance", 10.0f);
        compound.setFloat("LightLevel", 0.0f);
        compound.setString("SoundType", "GROUND");
        compound.setString("Material", "STONE");
            NBTTagList aabb = new NBTTagList();
            aabb.appendTag(new NBTTagDouble(0.0625d));
            aabb.appendTag(new NBTTagDouble(0.0625d));
            aabb.appendTag(new NBTTagDouble(0.0625d));
            aabb.appendTag(new NBTTagDouble(0.9375d));
            aabb.appendTag(new NBTTagDouble(0.9375d));
            aabb.appendTag(new NBTTagDouble(0.9375d));
        compound.setTag("AABB", aabb);
        compound.setString("BlockRenderType", "MODEL");
        compound.setBoolean("IsLadder", false);
        compound.setBoolean("IsPassable", false);
        compound.setBoolean("IsOpaqueCube", false);
        compound.setBoolean("IsFullCube", false);
        compound.setBoolean("CreateAllFiles", true);
        return compound;
    }

    private static NBTTagCompound getExampleFacingBlock() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "facingblockexample");
        compound.setByte("BlockType", (byte) 0);
        compound.setString("BlockRenderType", "MODEL");
            NBTTagCompound nbtProperty = new NBTTagCompound();
            nbtProperty.setByte("Type", (byte) 4);
            nbtProperty.setString("Name", "facing");
        compound.setTag("Property", nbtProperty);
        compound.setBoolean("CreateAllFiles", true);
        return compound;
    }

    private static NBTTagCompound getExampleLiquid() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "liquidexample");
        compound.setByte("BlockType", (byte) 1);
        compound.setFloat("Resistance", 2.0f);
        compound.setInteger("Density", 1100);
        compound.setBoolean("IsGaseous", false);
        compound.setInteger("Luminosity", 5);
        compound.setInteger("Viscosity", 900);
        compound.setInteger("Temperature", 300);
        compound.setInteger("Color", 0xFFFFFFFF);
        compound.setBoolean("CreateAllFiles", true);
        compound.setString("Material", "WATER");
        return compound;
    }

    private static NBTTagCompound getExampleChest() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "chestexample");
        compound.setByte("BlockType", (byte) 2);
        compound.setString("Material", "WOOD");
        compound.setBoolean("CreateAllFiles", true);
        compound.setBoolean("IsChest", true);
        compound.setInteger("Size", 14);
        compound.setInteger("GUIColor", 0x46AB86);
        compound.setString("Name", "Custom Chest");
        return compound;
    }

    private static NBTTagCompound getExampleContainer() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "containerexample");
        compound.setByte("BlockType", (byte) 2);
        compound.setString("Material", "STONE");
        compound.setBoolean("CreateAllFiles", true);
        compound.setInteger("Size", 96);
        compound.setIntArray("GUIColor", new int[] { 0x00DC8C, 0xDC8000 });
        compound.setString("Name", "Custom Container");
            NBTTagList aabb = new NBTTagList();
            aabb.appendTag(new NBTTagDouble(0.0625d));
            aabb.appendTag(new NBTTagDouble(0.0d));
            aabb.appendTag(new NBTTagDouble(0.0625d));
            aabb.appendTag(new NBTTagDouble(0.9375d));
            aabb.appendTag(new NBTTagDouble(1.0d));
            aabb.appendTag(new NBTTagDouble(0.9375d));
        compound.setTag("AABB", aabb);
        return compound;
    }

    private static NBTTagCompound getExampleStairs() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "stairsexample");
        compound.setByte("BlockType", (byte) 3);
        compound.setString("Material", "STONE");
        compound.setBoolean("CreateAllFiles", true);
        compound.setBoolean("IsFullCube", false);
        compound.setBoolean("IsOpaqueCube", false);
        return compound;
    }

    private static NBTTagCompound getExampleSlab() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "slabexample");
        compound.setByte("BlockType", (byte) 4);
        compound.setString("Material", "STONE");
        compound.setBoolean("CreateAllFiles", true);
        compound.setBoolean("IsFullCube", false);
        compound.setBoolean("IsOpaqueCube", false);
        return compound;
    }

    private static NBTTagCompound getExamplePortal() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "portalexample");
        compound.setByte("BlockType", (byte) 5);
        compound.setString("Material", "PORTAL");
            NBTTagCompound nbtRender = new NBTTagCompound();
            nbtRender.setFloat("SecondSpeed", 800.0f);
            nbtRender.setString("SpawnParticle", "CRIT");
            nbtRender.setFloat("Transparency", 0.5f);
        compound.setTag("RenderData", nbtRender);
        compound.setInteger("DimensionID", 100);
        compound.setInteger("HomeDimensionID", 0);
        compound.setBoolean("CreateAllFiles", true);
        return compound;
    }

    private static NBTTagCompound getExampleDoor() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "doorexample");
        compound.setByte("BlockType", (byte) 6);
        compound.setString("Material", "IRON");
        compound.setFloat("Hardness", 1.0f);
        compound.setFloat("Resistance", 25.0f);
        compound.setBoolean("CreateAllFiles", true);
        compound.setBoolean("InteractOpen", true);
        compound.setFloat("LightLevel", 2.0f);
        return compound;
    }

    public static NBTTagCompound getExampleItems() {
        if (exampleItems == null) {
            exampleItems = new NBTTagCompound();
            NBTTagList listItems = new NBTTagList();
                listItems.appendTag(getExampleItem());
                listItems.appendTag(getExampleWeapon());
                listItems.appendTag(getExampleTool());
                listItems.appendTag(getExampleAxe());
                listItems.appendTag(getExampleArmor());
                listItems.appendTag(getExampleOBJArmor());
                listItems.appendTag(getExampleShield());
                listItems.appendTag(getExampleBow());
                listItems.appendTag(getExampleFood());
                listItems.appendTag(getExampleFishingRod());
            exampleItems.setTag("Items", listItems);

            NBTTagList listPotion = new NBTTagList();
                listPotion.appendTag(getExamplePotion());
            exampleItems.setTag("Potions", listPotion);
        }
        return exampleItems;
    }

    private static NBTTagCompound getExampleItem() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "itemexample");
        compound.setByte("ItemType", (byte) 0);
        compound.setInteger("MaxStackSize", 64);
        compound.setBoolean("CreateAllFiles", true);
        return compound;
    }

    private static NBTTagCompound getExampleWeapon() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "weaponexample");
        compound.setByte("ItemType", (byte) 1);
        compound.setInteger("MaxStackDamage", 2500);
        compound.setDouble("EntityDamage", 2.5d);
        compound.setDouble("SpeedAttack", -2.4d);
        compound.setBoolean("IsFull3D", true);
        compound.setString("Material", "GOLD");
        compound.setTag("RepairItem", (new ItemStack(Items.GOLD_NUGGET)).writeToNBT(new NBTTagCompound()));
            NBTTagCompound collectionMaterial = new NBTTagCompound();
            collectionMaterial.setString("Material", "WEB");
            collectionMaterial.setFloat("Speed", 15.0f);
        compound.setTag("CollectionMaterial", collectionMaterial);
        compound.setBoolean("CreateAllFiles", true);
        return compound;
    }

    private static NBTTagCompound getExampleTool() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "toolexample");
        compound.setByte("ItemType", (byte) 2);
        compound.setInteger("MaxStackDamage", 2000);
        compound.setBoolean("IsFull3D", true);
        compound.setFloat("Efficiency", 4.0f);
        compound.setDouble("EntityDamage", 0.0d);
        compound.setString("ToolClass", "pickaxe");
        compound.setString("Material", "GOLD");
        compound.setTag("RepairItem", (new ItemStack(Items.GOLD_NUGGET)).writeToNBT(new NBTTagCompound()));
        compound.setInteger("HarvestLevel", 2);
        compound.setInteger("Enchantability", 25);
            NBTTagList collectionBlocks = new NBTTagList();
            collectionBlocks.appendTag(new NBTTagString(Objects.requireNonNull(Blocks.STONE.getRegistryName()).toString()));
            collectionBlocks.appendTag(new NBTTagString(Objects.requireNonNull(Blocks.OBSIDIAN.getRegistryName()).toString()));
        compound.setTag("CollectionBlocks", collectionBlocks);
        compound.setBoolean("CreateAllFiles", true);
        return compound;
    }

    private static NBTTagCompound getExampleAxe() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "axeexample");
        compound.setByte("ItemType", (byte) 2);
        compound.setInteger("MaxStackDamage", 2200);
        compound.setBoolean("IsFull3D", true);
        compound.setFloat("Efficiency", 4.25f);
        compound.setDouble("EntityDamage", 5.0d);
        compound.setString("ToolClass", "axe");
        compound.setString("Material", "GOLD");
        compound.setTag("RepairItem", (new ItemStack(Items.GOLD_INGOT)).writeToNBT(new NBTTagCompound()));
        compound.setInteger("HarvestLevel", 2);
        compound.setInteger("Enchantability", 28);
        compound.setBoolean("CreateAllFiles", true);
        return compound;
    }

    private static NBTTagCompound getExampleArmor() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "armorexample");
        compound.setByte("ItemType", (byte) 3);
        compound.setDouble("EntityDamage", 0.0d);
        compound.setInteger("RenderIndex", 4);
        compound.setString("Material", "GOLD");
        compound.setTag("RepairItem", (new ItemStack(Items.GOLD_NUGGET)).writeToNBT(new NBTTagCompound()));
        compound.setBoolean("CreateAllFiles", true);
        compound.setIntArray("MaxStackDamage", new int[] { 2250, 3100, 1800 });
            NBTTagList slots = new NBTTagList();
            slots.appendTag(new NBTTagString("HEAD"));
            slots.appendTag(new NBTTagString("Chest"));
            slots.appendTag(new NBTTagString("feet"));
        compound.setTag("EquipmentSlots", slots);
        compound.setIntArray("DamageReduceAmount", new int[] { 5, 7, 4 });
            NBTTagList toughness = new NBTTagList();
            toughness.appendTag(new NBTTagFloat(2.2f));
            toughness.appendTag(new NBTTagFloat(3.5f));
            toughness.appendTag(new NBTTagFloat(1.8f));
        compound.setTag("Toughness", toughness);
        return compound;
    }

    private static NBTTagCompound getExampleOBJArmor() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "armorobjexample");
        compound.setByte("ItemType", (byte) 3);
            NBTTagList toughness = new NBTTagList();
            toughness.appendTag(new NBTTagFloat(2.2f));
            toughness.appendTag(new NBTTagFloat(3.5f));
            toughness.appendTag(new NBTTagFloat(2.6f));
            toughness.appendTag(new NBTTagFloat(1.8f));
        compound.setTag("Toughness", toughness);
        compound.setIntArray("DamageReduceAmount", new int[] { 5, 7, 6, 4 });
        compound.setString("Material", "IRON");
        compound.setTag("RepairItem", (new ItemStack(Items.IRON_INGOT)).writeToNBT(new NBTTagCompound()));
        compound.setIntArray("MaxStackDamage", new int[] { 2250, 3100, 2700, 1800 });
            NBTTagList slots = new NBTTagList();
            slots.appendTag(new NBTTagString("HEAD"));
            slots.appendTag(new NBTTagString("Chest"));
            slots.appendTag(new NBTTagString("LeGs"));
            slots.appendTag(new NBTTagString("feet"));
        compound.setTag("EquipmentSlots", slots);
        compound.setDouble("EntityDamage", 0.0d);

        NBTTagCompound objData = new NBTTagCompound();
            NBTTagList meshes = new NBTTagList();
            meshes.appendTag(new NBTTagString(EnumParts.HEAD.name));
            objData.setTag("Head Mesh Names", meshes);

            meshes = new NBTTagList();
            meshes.appendTag(new NBTTagString(EnumParts.BODY.name));
            objData.setTag("Body Mesh Names", meshes);

            meshes = new NBTTagList();
            meshes.appendTag(new NBTTagString(EnumParts.ARM_RIGHT.name));
            objData.setTag("Arm Right Mesh Names", meshes);

            meshes = new NBTTagList();
            meshes.appendTag(new NBTTagString(EnumParts.WRIST_RIGHT.name));
            objData.setTag("Wrist Right Mesh Names", meshes);

            meshes = new NBTTagList();
            meshes.appendTag(new NBTTagString(EnumParts.ARM_LEFT.name));
            objData.setTag("Arm Left Mesh Names", meshes);

            meshes = new NBTTagList();
            meshes.appendTag(new NBTTagString(EnumParts.WRIST_LEFT.name));
            objData.setTag("Wrist Left Mesh Names", meshes);

            meshes = new NBTTagList();
            meshes.appendTag(new NBTTagString(EnumParts.BELT.name));
            objData.setTag("Belt Mesh Names", meshes);

            meshes = new NBTTagList();
            meshes.appendTag(new NBTTagString(EnumParts.LEG_RIGHT.name));
            objData.setTag("Leg Right Mesh Names", meshes);

            meshes = new NBTTagList();
            meshes.appendTag(new NBTTagString(EnumParts.FOOT_RIGHT.name));
            objData.setTag("Foot Right Mesh Names", meshes);

            meshes = new NBTTagList();
            meshes.appendTag(new NBTTagString(EnumParts.LEG_LEFT.name));
            objData.setTag("Leg Left Mesh Names", meshes);

            meshes = new NBTTagList();
            meshes.appendTag(new NBTTagString(EnumParts.FOOT_LEFT.name));
            objData.setTag("Foot Left Mesh Names", meshes);

            meshes = new NBTTagList();
            meshes.appendTag(new NBTTagString(EnumParts.FEET_LEFT.name));
            objData.setTag("Boot Left Mesh Names", meshes);

            meshes = new NBTTagList();
            meshes.appendTag(new NBTTagString(EnumParts.FEET_RIGHT.name));
        objData.setTag("Boot Right Mesh Names", meshes);
        compound.setTag("OBJData", objData);

        NBTTagCompound display = new NBTTagCompound();
        for (int s = 0; s < 4; s++) {
            String slot = s == 0 ? "CHEST" : s == 1 ? "LEGS" : s == 2 ? "FEET" : "HEAD";
            NBTTagCompound cameraData = new NBTTagCompound();
            for (int i = 0; i < 8; i++) {
                String part;
                NBTTagList rotation = new NBTTagList();
                NBTTagList translation = new NBTTagList();
                NBTTagList scale = new NBTTagList();
                switch(i) {
                    case 0: { // THIRD_PERSON_LEFT_HAND
                        part = "thirdperson_lefthand";
                        switch(slot) {
                            case "CHEST": {
                                translation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
                                break;
                            }
                            case "LEGS": {
                                translation.appendTag(new NBTTagFloat(-0.15f));
                                translation.appendTag(new NBTTagFloat(0.35f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.65f)); }
                                break;
                            }
                            case "FEET": {
                                rotation.appendTag(new NBTTagFloat(90.0f));
                                rotation.appendTag(new NBTTagFloat(180.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(1.15f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.65f)); }
                                break;
                            }
                            default: {
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(180.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(1.0f));
                                translation.appendTag(new NBTTagFloat(-0.375f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
                                break;
                            }
                        }
                        break;
                    }
                    case 1: { // THIRD_PERSON_RIGHT_HAND
                        part = "thirdperson_righthand";
                        switch(slot) {
                            case "CHEST": {
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
                                break;
                            }
                            case "LEGS": {
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(0.35f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.65f)); }
                                break;
                            }
                            case "FEET": {
                                rotation.appendTag(new NBTTagFloat(90.0f));
                                rotation.appendTag(new NBTTagFloat(180.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.65f)); }
                                break;
                            }
                            default: {
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(180.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(-0.375f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
                                break;
                            }
                        }
                        break;
                    }
                    case 2: { // FIRST_PERSON_LEFT_HAND
                        part = "firstperson_lefthand";
                        switch(slot) {
                            case "CHEST": {
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(280.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.57f));
                                translation.appendTag(new NBTTagFloat(0.1f));
                                translation.appendTag(new NBTTagFloat(-0.085f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
                                break;
                            }
                            case "LEGS": {
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(280.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.65f));
                                translation.appendTag(new NBTTagFloat(0.4f));
                                translation.appendTag(new NBTTagFloat(-0.085f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
                                break;
                            }
                            case "FEET": {
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(280.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.72f));
                                translation.appendTag(new NBTTagFloat(0.435f));
                                translation.appendTag(new NBTTagFloat(-0.585f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.85f)); }
                                break;
                            }
                            default: {
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(280.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.57f));
                                translation.appendTag(new NBTTagFloat(-0.225f));
                                translation.appendTag(new NBTTagFloat(-0.085f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
                                break;
                            }
                        }
                        break;
                    }
                    case 3: { // FIRST_PERSON_RIGHT_HAND
                        part = "firstperson_righthand";
                        switch(slot) {
                            case "CHEST": {
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(280.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.85f));
                                translation.appendTag(new NBTTagFloat(-0.1f));
                                translation.appendTag(new NBTTagFloat(0.2f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.6f)); }
                                break;
                            }
                            case "LEGS": {
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(280.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.95f));
                                translation.appendTag(new NBTTagFloat(0.25f));
                                translation.appendTag(new NBTTagFloat(0.2f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.6f)); }
                                break;
                            }
                            case "FEET": {
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(280.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.95f));
                                translation.appendTag(new NBTTagFloat(0.4f));
                                translation.appendTag(new NBTTagFloat(0.2f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.85f)); }
                                break;
                            }
                            default: {
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(280.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.85f));
                                translation.appendTag(new NBTTagFloat(-0.5f));
                                translation.appendTag(new NBTTagFloat(0.2f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.6f)); }
                                break;
                            }
                        }
                        break;
                    }
                    case 4: { // HEAD
                        part = "head";
                        switch(slot) {
                            case "CHEST": {
                                rotation.appendTag(new NBTTagFloat(270.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(1.0f));
                                translation.appendTag(new NBTTagFloat(1.65f));
                                break;
                            }
                            case "LEGS": {
                                rotation.appendTag(new NBTTagFloat(270.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(1.0f));
                                translation.appendTag(new NBTTagFloat(1.0f));
                                break;
                            }
                            case "FEET": {
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(180.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(0.925f));
                                translation.appendTag(new NBTTagFloat(0.4f));
                                break;
                            }
                            default: { break; }
                        }
                        break;
                    }
                    case 5: { // GUI
                        part = "gui";
                        switch(slot) {
                            case "CHEST": {
                                rotation.appendTag(new NBTTagFloat(30.0f));
                                rotation.appendTag(new NBTTagFloat(45.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.49f));
                                translation.appendTag(new NBTTagFloat(-0.41f));
                                translation.appendTag(new NBTTagFloat(0.0f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.9f)); }
                                break;
                            }
                            case "LEGS": {
                                rotation.appendTag(new NBTTagFloat(30.0f));
                                rotation.appendTag(new NBTTagFloat(45.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(0.05f));
                                translation.appendTag(new NBTTagFloat(0.0f));
                                break;
                            }
                            case "FEET": {
                                rotation.appendTag(new NBTTagFloat(30.0f));
                                rotation.appendTag(new NBTTagFloat(45.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(0.3f));
                                translation.appendTag(new NBTTagFloat(0.0f));
                                break;
                            }
                            default: {
                                rotation.appendTag(new NBTTagFloat(30.0f));
                                rotation.appendTag(new NBTTagFloat(45.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(-1.0f));
                                translation.appendTag(new NBTTagFloat(0.0f));
                                break;
                            }
                        }
                        break;
                    }
                    case 6: { // GROUND
                        part = "ground";
                        switch(slot) {
                            case "CHEST": {
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
                                break;
                            }
                            case "LEGS": {
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(0.25f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.6f)); }
                                break;
                            }
                            case "FEET": {
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(0.35f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.65f)); }
                                break;
                            }
                            default: {
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(-0.375f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
                                break;
                            }
                        }
                        break;
                    }
                    default: { // FIXED
                        part = "fixed";
                        switch(slot) {
                            case "CHEST": {
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(180.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(-0.65f));
                                translation.appendTag(new NBTTagFloat(0.45f));
                                break;
                            }
                            case "LEGS": {
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(180.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(0.05f));
                                translation.appendTag(new NBTTagFloat(0.475f));
                                break;
                            }
                            case "FEET": {
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(180.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(0.2f));
                                translation.appendTag(new NBTTagFloat(0.475f));
                                break;
                            }
                            default: {
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                rotation.appendTag(new NBTTagFloat(180.0f));
                                rotation.appendTag(new NBTTagFloat(0.0f));
                                translation.appendTag(new NBTTagFloat(0.5f));
                                translation.appendTag(new NBTTagFloat(-0.85f));
                                translation.appendTag(new NBTTagFloat(0.4f));
                                for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.75f)); }
                                break;
                            }
                        }
                        break;
                    }
                }
                NBTTagCompound transform = new NBTTagCompound();
                if (rotation.tagCount() > 0) { transform.setTag("rotation", rotation); }
                if (translation.tagCount() > 0) { transform.setTag("translation", translation); }
                if (scale.tagCount() > 0) { transform.setTag("scale", scale); }
                cameraData.setTag(part, transform);
            }
            display.setTag(slot, cameraData);
        }
        compound.setTag("Display", display);

        compound.setBoolean("CreateAllFiles", true);
        return compound;
    }

    private static NBTTagCompound getExampleShield() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "shieldexample");
        compound.setByte("ItemType", (byte) 4);
        compound.setInteger("MaxStackDamage", 6500);
        compound.setDouble("EntityDamage", 0.0d);
        compound.setString("Material", "IRON");
        compound.setTag("RepairItem", (new ItemStack(Items.IRON_NUGGET)).writeToNBT(new NBTTagCompound()));
        compound.setBoolean("CreateAllFiles", true);
        return compound;
    }

    private static NBTTagCompound getExampleBow() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "bowexample");
        compound.setByte("ItemType", (byte) 5);
        compound.setInteger("MaxStackDamage", 1250);
        compound.setDouble("EntityDamage", 2.0d);
        compound.setString("Material", "WOOD");
        compound.setTag("RepairItem", (new ItemStack(Blocks.PLANKS)).writeToNBT(new NBTTagCompound()));
        compound.setBoolean("SetFlame", false);
        compound.setFloat("CritChance", 0.25f);
        compound.setFloat("DrawstringSpeed", 20.0f);
        compound.setBoolean("CreateAllFiles", true);
        return compound;
    }

    private static NBTTagCompound getExampleFood() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "foodexample");
        compound.setByte("ItemType", (byte) 6);
        compound.setInteger("MaxStackSize", 32);
        compound.setInteger("UseDuration", 32);
        compound.setInteger("HealAmount", 1);
        compound.setFloat("SaturationModifier", 0.1f);
        compound.setBoolean("IsWolfFood", false);
        compound.setBoolean("AlwaysEdible", true);
            NBTTagCompound potionEffect = new NBTTagCompound();
            potionEffect.setString("Potion", "minecraft:fire_resistance");
            potionEffect.setInteger("DurationTicks", 45);
            potionEffect.setInteger("Amplifier", 0);
            potionEffect.setBoolean("Ambient", true);
            potionEffect.setBoolean("ShowParticles", false);
            potionEffect.setFloat("Probability", 0.95f);
        compound.setTag("PotionEffect", potionEffect);
        compound.setBoolean("CreateAllFiles", true);
        return compound;
    }

    private static NBTTagCompound getExampleFishingRod() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "fishingrodexample");
        compound.setByte("ItemType", (byte) 8);
        compound.setInteger("MaxStackSize", 1);
        compound.setTag("RepairItem", (new ItemStack(Items.STICK)).writeToNBT(new NBTTagCompound()));
        compound.setInteger("MaxStackDamage", 150);
        compound.setInteger("Enchantability", 5);
        compound.setBoolean("CreateAllFiles", true);
        return compound;
    }

    private static NBTTagCompound getExamplePotion() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "potionexample");
        compound.setByte("ItemType", (byte) 7);
        compound.setBoolean("CreateAllFiles", true);
        compound.setBoolean("IsBadEffect", false);
        compound.setBoolean("IsInstant", false);
        compound.setBoolean("IsBeneficial", true);
        compound.setInteger("LiquidColor", 0xFFFFFF);
        compound.setInteger("MaxStackSize", 16);
        compound.setInteger("BaseDelay", 200);
        compound.setInteger("Duration", 20);
        compound.setTag("CureItem", (new ItemStack(Items.CARROT)).writeToNBT(new NBTTagCompound()));
            NBTTagList potionModifiers = new NBTTagList();
            potionModifiers.appendTag(getExamplePotionModifier());
        compound.setTag("Modifiers", potionModifiers);
        return compound;
    }

    private static NBTTagCompound getExamplePotionModifier() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("AttributeName", "generic.maxHealth");
        compound.setString("UUID", UUID.randomUUID().toString());
        compound.setDouble("AttributeDefValue", 5.0d);
        compound.setDouble("AttributeMinValue", -50.0d);
        compound.setDouble("AttributeMaxValue", 50.0d);
        compound.setDouble("Amount", 2.0d);
        compound.setInteger("Operation", 2);
        return compound;
    }

    public static NBTTagCompound getExampleParticles() {
        if (exampleParticles == null) {
            exampleParticles = new NBTTagCompound();
            NBTTagList listItems = new NBTTagList();
            listItems.appendTag(getExampleParticle());
            listItems.appendTag(getExampleOBJParticle());
            exampleParticles.setTag("Particles", listItems);
        }
        return exampleParticles;
    }

    private static NBTTagCompound getExampleParticle() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "PARTICLE_EXAMPLE");
        compound.setBoolean("ShouldIgnoreRange", false);
        compound.setInteger("ArgumentCount", 0);
        compound.setInteger("MaxAge", 60);
        compound.setIntArray("UVpos", new int[]{1, 5});
        compound.setFloat("Gravity", 0.25f);
        compound.setFloat("Scale", 1.5f);
        compound.setString("Texture", "particles");
        compound.setBoolean("IsFullTexture", false);
        compound.setBoolean("CreateAllFiles", true);
        NBTTagList motion = new NBTTagList();
        motion.appendTag(new NBTTagDouble(0.2d));
        motion.appendTag(new NBTTagDouble(0.1d));
        motion.appendTag(new NBTTagDouble(0.2d));
        compound.setTag("StartMotion", motion);
        compound.setBoolean("IsRandomMotion", true);
        compound.setBoolean("NotMotionY", true);
        return compound;
    }

    private static NBTTagCompound getExampleOBJParticle() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("RegistryName", "PARTICLE_OBJ_EXAMPLE");
        compound.setBoolean("ShouldIgnoreRange", false);
        compound.setInteger("MaxAge", 60);
        compound.setFloat("Gravity", 1.0f / 3.0f);
        compound.setFloat("Scale", 1.0f);
        compound.setString("OBJModel", "ring");
        compound.setBoolean("CreateAllFiles", true);
        return compound;
    }

}
