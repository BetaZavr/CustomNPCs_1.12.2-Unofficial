package noppes.npcs.items;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.lwjgl.util.vector.Vector3f;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.ObfuscationHelper;

import javax.annotation.Nonnull;

@SuppressWarnings("deprecation")
public class CustomArmor extends ItemArmor implements ICustomElement {

	public static ItemArmor.ArmorMaterial getMaterialArmor(NBTTagCompound nbtItem) {
		String materialName = nbtItem.hasKey("Material", 8) ? nbtItem.getString("Material").toLowerCase() : "leather";
		switch (materialName) {
			case "diamond":
				return ItemArmor.ArmorMaterial.DIAMOND;
			case "chain":
				return ItemArmor.ArmorMaterial.CHAIN;
			case "iron":
				return ItemArmor.ArmorMaterial.IRON;
			case "gold":
				return ItemArmor.ArmorMaterial.GOLD;
			default:
				return ItemArmor.ArmorMaterial.LEATHER;
		}
	}
	
	public static EntityEquipmentSlot getSlotEquipment(String slotName) {
		slotName = slotName.toLowerCase();
		switch (slotName) {
		case "head":
			return EntityEquipmentSlot.HEAD;
		case "chest":
			return EntityEquipmentSlot.CHEST;
		case "legs":
			return EntityEquipmentSlot.LEGS;
		default:
			return EntityEquipmentSlot.FEET;
		}
	}
	
	protected NBTTagCompound nbtData;
	protected ItemStack repairItemStack = ItemStack.EMPTY;
	protected int enchantability = 0;

	public ResourceLocation objModel = null;

	private final Map<EnumParts, List<String>> parts = Maps.newHashMap();
	
	private final Map<TransformType, Optional<TRSRTransformation>> cameraData = Maps.newHashMap();

	public CustomArmor(ItemArmor.ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, int maxStackDamage, int damageReduceAmount, float toughness, NBTTagCompound nbtItem) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
		this.nbtData = nbtItem;
		String name = "custom_" + nbtItem.getString("RegistryName") + "_" + equipmentSlotIn.name().toLowerCase();
		this.setRegistryName(CustomNpcs.MODID, name);
		this.setUnlocalizedName(name);
		this.setCreativeTab(CustomRegisters.tabItems);
		if (nbtItem.hasKey("IsFull3D", 1) && nbtItem.getBoolean("IsFull3D")) {
			this.setFull3D();
		}
		if (maxStackDamage > 1) {
			this.setMaxDamage(maxStackDamage);
		}
		if (damageReduceAmount > 0) {
			ObfuscationHelper.setValue(ItemArmor.class, this, damageReduceAmount, 5);
		}
		if (toughness > 0.0f) {
			ObfuscationHelper.setValue(ItemArmor.class, this, toughness, 6);
		}
		if (nbtItem.hasKey("RepairItem", 10)) {
			this.repairItemStack = new ItemStack(nbtItem.getCompoundTag("RepairItem"));
		} else {
			this.repairItemStack = materialIn.getRepairItemStack();
		}
		if (nbtItem.hasKey("Enchantability", 3)) {
			this.enchantability = nbtItem.getInteger("Enchantability");
		}
		if (this.nbtData.hasKey("OBJData", 10)) {
			NBTTagCompound data = this.nbtData.getCompoundTag("OBJData");
			NBTTagList tagList = data.getTagList("Head Mesh Names", 8);
			List<String> listHead = Lists.newArrayList();
			if (tagList.tagCount() > 0) {
				for (int i = 0; i < tagList.tagCount(); i++) { listHead.add(tagList.getStringTagAt(i)); }
			}
			this.parts.put(EnumParts.HEAD, listHead);
			this.parts.put(EnumParts.MOHAWK, listHead);
			tagList = data.getTagList("Body Mesh Names", 8);
			List<String> listBody = Lists.newArrayList();
			if (tagList.tagCount() > 0) {
				for (int i = 0; i < tagList.tagCount(); i++) { listBody.add(tagList.getStringTagAt(i)); }
			}
			this.parts.put(EnumParts.BODY, listBody);
			tagList = data.getTagList("Arm Right Mesh Names", 8);
			List<String> listArmRight = Lists.newArrayList();
			if (tagList.tagCount() > 0) {
				for (int i = 0; i < tagList.tagCount(); i++) { listArmRight.add(tagList.getStringTagAt(i)); }
			}
			this.parts.put(EnumParts.ARM_RIGHT, listArmRight);
			tagList = data.getTagList("Wrist Right Mesh Names", 8);
			List<String> listWristRight = Lists.newArrayList();
			if (tagList.tagCount() > 0) {
				for (int i = 0; i < tagList.tagCount(); i++) { listWristRight.add(tagList.getStringTagAt(i)); }
			}
			this.parts.put(EnumParts.WRIST_RIGHT, listWristRight);
			tagList = data.getTagList("Arm Left Mesh Names", 8);
			List<String> listArmLeft = Lists.newArrayList();
			if (tagList.tagCount() > 0) {
				for (int i = 0; i < tagList.tagCount(); i++) { listArmLeft.add(tagList.getStringTagAt(i)); }
			}
			this.parts.put(EnumParts.ARM_LEFT, listArmLeft);
			tagList = data.getTagList("Wrist Left Mesh Names", 8);
			List<String> listWristLeft = Lists.newArrayList();
			if (tagList.tagCount() > 0) {
				for (int i = 0; i < tagList.tagCount(); i++) { listWristLeft.add(tagList.getStringTagAt(i)); }
			}
			this.parts.put(EnumParts.WRIST_LEFT, listWristLeft);
			tagList = data.getTagList("Belt Mesh Names", 8);
			List<String> listBelt = Lists.newArrayList();
			if (tagList.tagCount() > 0) {
				for (int i = 0; i < tagList.tagCount(); i++) { listBelt.add(tagList.getStringTagAt(i)); }
			}
			this.parts.put(EnumParts.BELT, listBelt);
			tagList = data.getTagList("Leg Right Mesh Names", 8);
			List<String> listLegRight = Lists.newArrayList();
			if (tagList.tagCount() > 0) {
				for (int i = 0; i < tagList.tagCount(); i++) { listLegRight.add(tagList.getStringTagAt(i)); }
			}
			this.parts.put(EnumParts.LEG_RIGHT, listLegRight);
			tagList = data.getTagList("Foot Right Mesh Names", 8);
			List<String> listFootRight = Lists.newArrayList();
			if (tagList.tagCount() > 0) {
				for (int i = 0; i < tagList.tagCount(); i++) { listFootRight.add(tagList.getStringTagAt(i)); }
			}
			this.parts.put(EnumParts.FOOT_RIGHT, listFootRight);
			tagList = data.getTagList("Leg Left Mesh Names", 8);
			List<String> listLegLeft = Lists.newArrayList();
			if (tagList.tagCount() > 0) {
				for (int i = 0; i < tagList.tagCount(); i++) { listLegLeft.add(tagList.getStringTagAt(i)); }
			}
			this.parts.put(EnumParts.LEG_LEFT, listLegLeft);
			tagList = data.getTagList("Foot Left Mesh Names", 8);
			List<String> listFootLeft = Lists.newArrayList();
			if (tagList.tagCount() > 0) {
				for (int i = 0; i < tagList.tagCount(); i++) { listFootLeft.add(tagList.getStringTagAt(i)); }
			}
			this.parts.put(EnumParts.FOOT_LEFT, listFootLeft);
			tagList = data.getTagList("Boot Right Mesh Names", 8);
			List<String> listBootRight = Lists.newArrayList();
			if (tagList.tagCount() > 0) {
				for (int i = 0; i < tagList.tagCount(); i++) { listBootRight.add(tagList.getStringTagAt(i)); }
			}
			this.parts.put(EnumParts.FEET_RIGHT, listBootRight);
			tagList = data.getTagList("Boot Left Mesh Names", 8);
			List<String> listBootLeft = Lists.newArrayList();
			if (tagList.tagCount() > 0) {
				for (int i = 0; i < tagList.tagCount(); i++) { listBootLeft.add(tagList.getStringTagAt(i)); }
			}
			this.parts.put(EnumParts.FEET_LEFT, listBootLeft);
			this.objModel = new ResourceLocation(CustomNpcs.MODID, "models/armor/" + nbtItem.getString("RegistryName") + ".obj");
			if (Thread.currentThread().getName().toLowerCase().contains("client")) {
				this.createCameraData();
			}
		}
		else if (this.nbtData.hasKey("OBJData", 9)) { // OLD
			NBTTagList data = this.nbtData.getTagList("OBJData", 10);
			for (EnumParts part : EnumParts.values()) {
				NBTTagCompound nbt = null;
				switch (part) {
					case HEAD:
                    case MOHAWK: {
						nbt = data.getCompoundTagAt(0);
						break;
					}
                    case BODY: {
						nbt = data.getCompoundTagAt(1);
						break;
					}
					case ARM_RIGHT: {
						nbt = data.getCompoundTagAt(2);
						break;
					}
					case ARM_LEFT: {
						nbt = data.getCompoundTagAt(3);
						break;
					}
					case BELT: {
						nbt = data.getCompoundTagAt(4);
						break;
					}
					case LEG_RIGHT: {
						nbt = data.getCompoundTagAt(5);
						break;
					}
					case LEG_LEFT: {
						nbt = data.getCompoundTagAt(6);
						break;
					}
					case FEET_RIGHT: {
						nbt = data.getCompoundTagAt(7);
						break;
					}
					case FEET_LEFT: {
						nbt = data.getCompoundTagAt(8);
						break;
					}
					case WRIST_RIGHT: {
						nbt = data.getCompoundTagAt(9);
						break;
					}
					case WRIST_LEFT: {
						nbt = data.getCompoundTagAt(10);
						break;
					}
					case FOOT_RIGHT: {
						nbt = data.getCompoundTagAt(11);
						break;
					}
					case FOOT_LEFT: {
						nbt = data.getCompoundTagAt(12);
						break;
					}
					default: {
						break;
					}
				}
				if (nbt == null) { continue; }
				List<String> list = Lists.newArrayList();
                for (int i = 0; i < nbt.getTagList("meshes", 8).tagCount(); i++) {
                    list.add(nbt.getTagList("meshes", 8).getStringTagAt(i));
                }
                this.parts.put(part, list);
			}
			this.objModel = new ResourceLocation(CustomNpcs.MODID, "models/armor/" + nbtItem.getString("RegistryName") + ".obj");
			if (Thread.currentThread().getName().toLowerCase().contains("client")) {
				this.createCameraData();
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(@Nonnull EntityLivingBase entity, @Nonnull ItemStack itemStack, @Nonnull EntityEquipmentSlot slot, @Nonnull ModelBiped defModel) {
		if (this.objModel != null) {
			return ModelBuffer.getOBJModelBiped(this, entity, defModel);
		}
		return super.getArmorModel(entity, itemStack, slot, defModel);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getArmorTexture(@Nonnull ItemStack stack, @Nonnull Entity entity, @Nonnull EntityEquipmentSlot slot, @Nonnull String type) {
		if (this.objModel != null) {
			return CustomNpcs.MODID + ":textures/items/null.png";
		}
		return CustomNpcs.MODID + ":textures/models/armor/" + this.nbtData.getString("RegistryName") + "_layer_"
				+ (slot == EntityEquipmentSlot.LEGS ? "2" : "1") + ".png";
	}

	@Override
	public String getCustomName() {
		return this.nbtData.getString("RegistryName");
	}

	@Override
	public INbt getCustomNbt() {
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(this.nbtData);
	}

	public boolean getIsRepairable(@Nonnull ItemStack toRepair, @Nonnull ItemStack repair) {
		ItemStack mat = this.repairItemStack;
		if (this.repairItemStack.isEmpty()) {
			mat = this.getArmorMaterial().getRepairItemStack();
		}
		if (!mat.isEmpty() && net.minecraftforge.oredict.OreDictionary.itemMatches(mat, repair, false)) {
			return true;
		}
		return super.getIsRepairable(toRepair, repair);
	}

	public int getItemEnchantability() {
		if (this.enchantability > 0) {
			return this.enchantability;
		}
		return super.getItemEnchantability();
	}

	public List<String> getMeshNames(EnumParts slot) {
		if (this.parts.containsKey(slot)) { return this.parts.get(slot); }
		return Lists.newArrayList();
	}

	private void createCameraData() {
		this.cameraData.clear();
		NBTTagCompound display = this.nbtData.hasKey("Display", 10) ? this.nbtData.getCompoundTag("Display") : new NBTTagCompound();

		NBTTagCompound head = display.hasKey("HEAD", 10) ? this.nbtData.getCompoundTag("HEAD") : new NBTTagCompound();
		NBTTagCompound chest = display.hasKey("CHEST", 10) ? this.nbtData.getCompoundTag("CHEST") : new NBTTagCompound();
		NBTTagCompound legs = display.hasKey("LEGS", 10) ? this.nbtData.getCompoundTag("LEGS") : new NBTTagCompound();
		NBTTagCompound feet = display.hasKey("FEET", 10) ? this.nbtData.getCompoundTag("FEET") : new NBTTagCompound();
		
		for (TransformType transformType : TransformType.values()) {
			Vector3f rotation = new Vector3f();
			Vector3f translation = new Vector3f();
			Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);
			switch(transformType) {
				case THIRD_PERSON_LEFT_HAND: {
					switch(this.getEquipmentSlot()) {
						case CHEST: {
							if (!chest.hasKey("thirdperson_lefthand", 10)) {
								translation.z = 0.5f;
								scale.x = 0.5f;
								scale.y = 0.5f;
								scale.z = 0.5f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, chest.getCompoundTag("thirdperson_lefthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case LEGS: {
							if (!legs.hasKey("thirdperson_lefthand", 10)) {
								translation.x = -0.15f;
								translation.y = 0.35f;
								translation.z = 0.5f;
								scale.x = 0.65f;
								scale.y = 0.65f;
								scale.z = 0.65f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, legs.getCompoundTag("thirdperson_lefthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case FEET: {
							if (!feet.hasKey("thirdperson_lefthand", 10)) {
								rotation.x = 90.0f;
								rotation.y = 180.0f;
								translation.x = 1.15f;
								translation.y = 0.5f;
								translation.z = 0.5f;
								scale.x = 0.65f;
								scale.y = 0.65f;
								scale.z = 0.65f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, feet.getCompoundTag("thirdperson_lefthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						default: {
							if (!head.hasKey("thirdperson_lefthand", 10)) {
								rotation.y = 180.0f;
								translation.x = 1.0f;
								translation.y = -0.375f;
								translation.z = 0.5f;
								scale.x = 0.5f;
								scale.y = 0.5f;
								scale.z = 0.5f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, head.getCompoundTag("thirdperson_lefthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
					}
					break;
				}
				case THIRD_PERSON_RIGHT_HAND: {
					switch(this.getEquipmentSlot()) {
						case CHEST: {
							if (!chest.hasKey("thirdperson_righthand", 10)) {
								translation.x = 0.5f;
								translation.z = 0.5f;
								scale.x = 0.5f;
								scale.y = 0.5f;
								scale.z = 0.5f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, chest.getCompoundTag("thirdperson_righthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case LEGS: {
							if (!legs.hasKey("thirdperson_righthand", 10)) {
								translation.x = 0.5f;
								translation.y = 0.35f;
								translation.z = 0.5f;
								scale.x = 0.65f;
								scale.y = 0.65f;
								scale.z = 0.65f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, legs.getCompoundTag("thirdperson_righthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case FEET: {
							if (!feet.hasKey("firstperson_righthand", 10)) {
								rotation.x = 90.0f;
								rotation.y = 180.0f;
								translation.x = 0.5f;
								translation.y = 0.5f;
								translation.z = 0.5f;
								scale.x = 0.65f;
								scale.y = 0.65f;
								scale.z = 0.65f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, feet.getCompoundTag("firstperson_righthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						default: {
							if (!head.hasKey("thirdperson_righthand", 10)) {
								rotation.y = 180.0f;
								translation.x = 0.5f;
								translation.y = -0.375f;
								translation.z = 0.5f;
								scale.x = 0.5f;
								scale.y = 0.5f;
								scale.z = 0.5f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, head.getCompoundTag("thirdperson_righthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
					}
					break;
				}
				case FIRST_PERSON_LEFT_HAND: {
					switch(this.getEquipmentSlot()) {
						case CHEST: {
							if (!chest.hasKey("firstperson_lefthand", 10)) {
								rotation.y = 280.0f;
								translation.x = 0.57f;
								translation.y = 0.1f;
								translation.z = -0.085f;
								scale.x = 0.5f;
								scale.y = 0.5f;
								scale.z = 0.5f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, chest.getCompoundTag("firstperson_lefthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case LEGS: {
							if (!legs.hasKey("firstperson_lefthand", 10)) {
								rotation.y = 280.0f;
								translation.x = 0.65f;
								translation.y = 0.4f;
								translation.z = -0.085f;
								scale.x = 0.5f;
								scale.y = 0.5f;
								scale.z = 0.5f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, legs.getCompoundTag("firstperson_lefthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case FEET: {
							if (!feet.hasKey("firstperson_lefthand", 10)) {
								rotation.y = 280.0f;
								translation.x = 0.72f;
								translation.y = 0.435f;
								translation.z = -0.585f;
								scale.x = 0.85f;
								scale.y = 0.85f;
								scale.z = 0.85f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, feet.getCompoundTag("firstperson_lefthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						default: {
							if (!head.hasKey("firstperson_lefthand", 10)) {
								rotation.y = 280.0f;
								translation.x = 0.57f;
								translation.y = -0.225f;
								translation.z = -0.085f;
								scale.x = 0.5f;
								scale.y = 0.5f;
								scale.z = 0.5f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, head.getCompoundTag("firstperson_lefthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
					}
					break;
				}
				case FIRST_PERSON_RIGHT_HAND: {
					switch(this.getEquipmentSlot()) {
						case CHEST: {
							if (!chest.hasKey("firstperson_righthand", 10)) {
								rotation.y = 280.0f;
								translation.x = 0.85f;
								translation.y = -0.1f;
								translation.z = 0.2f;
								scale.x = 0.6f;
								scale.y = 0.6f;
								scale.z = 0.6f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, chest.getCompoundTag("firstperson_righthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case LEGS: {
							if (!legs.hasKey("firstperson_righthand", 10)) {
								rotation.y = 280.0f;
								translation.x = 0.95f;
								translation.y = 0.25f;
								translation.z = 0.2f;
								scale.x = 0.6f;
								scale.y = 0.6f;
								scale.z = 0.6f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, legs.getCompoundTag("firstperson_righthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case FEET: {
							if (!feet.hasKey("firstperson_righthand", 10)) {
								rotation.y = 280.0f;
								translation.x = 0.95f;
								translation.y = 0.4f;
								translation.z = 0.2f;
								scale.x = 0.85f;
								scale.y = 0.85f;
								scale.z = 0.85f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, feet.getCompoundTag("firstperson_righthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						default: {
							if (!head.hasKey("firstperson_righthand", 10)) {
								rotation.y = 280.0f;
								translation.x = 0.85f;
								translation.y = -0.5f;
								translation.z = 0.2f;
								scale.x = 0.6f;
								scale.y = 0.6f;
								scale.z = 0.6f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, head.getCompoundTag("firstperson_righthand"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
					}
					break;
				}
				case HEAD: {
					switch(this.getEquipmentSlot()) {
						case CHEST: {
							if (!chest.hasKey("head", 10)) {
								rotation.x = 270.0f;
								translation.x = 0.5f;
								translation.y = 1.0f;
								translation.z = 1.65f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, chest.getCompoundTag("head"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case LEGS: {
							if (!legs.hasKey("head", 10)) {
								rotation.x = 270.0f;
								translation.x = 0.5f;
								translation.y = 1.0f;
								translation.z = 1.0f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, legs.getCompoundTag("head"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case FEET: {
							if (!feet.hasKey("head", 10)) {
								rotation.y = 180.0f;
								translation.x = 0.5f;
								translation.y = 0.925f;
								translation.z = 0.4f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, feet.getCompoundTag("head"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						default: { break; }
					}
					break;
				}
				case GUI: {
					switch(this.getEquipmentSlot()) {
						case CHEST: {
							if (!chest.hasKey("gui", 10)) {
								rotation.x = 30.0f;
								rotation.y = 45.0f;
								translation.x = 0.49f;
								translation.y = -0.41f;
								scale.x = 0.9f;
								scale.y = 0.9f;
								scale.z = 0.9f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, chest.getCompoundTag("gui"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case LEGS: {
							if (!legs.hasKey("gui", 10)) {
								rotation.x = 30.0f;
								rotation.y = 45.0f;
								translation.x = 0.5f;
								translation.y = 0.05f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, legs.getCompoundTag("gui"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case FEET: {
							if (!feet.hasKey("gui", 10)) {
								rotation.x = 30.0f;
								rotation.y = 45.0f;
								translation.x = 0.5f;
								translation.y = 0.3f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, feet.getCompoundTag("gui"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						default: {
							if (!head.hasKey("gui", 10)) {
								rotation.x = 30.0f;
								rotation.y = 45.0f;
								translation.x = 0.5f;
								translation.y = -1.0f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, head.getCompoundTag("gui"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
					}
					break;
				}
				case GROUND: {
					switch(this.getEquipmentSlot()) {
						case CHEST: {
							if (!chest.hasKey("ground", 10)) {
								translation.x = 0.5f;
								translation.z = 0.5f;
								scale.x = 0.5f;
								scale.y = 0.5f;
								scale.z = 0.5f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, chest.getCompoundTag("ground"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case LEGS: {
							if (!legs.hasKey("ground", 10)) {
								translation.x = 0.5f;
								translation.y = 0.25f;
								translation.z = 0.5f;
								scale.x = 0.6f;
								scale.y = 0.6f;
								scale.z = 0.6f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, legs.getCompoundTag("ground"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case FEET: {
							if (!feet.hasKey("ground", 10)) {
								translation.x = 0.5f;
								translation.y = 0.35f;
								translation.z = 0.5f;
								scale.x = 0.65f;
								scale.y = 0.65f;
								scale.z = 0.65f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, feet.getCompoundTag("ground"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						default: {
							if (!head.hasKey("ground", 10)) {
								translation.x = 0.5f;
								translation.y = -0.375f;
								translation.z = 0.5f;
								scale.x = 0.5f;
								scale.y = 0.5f;
								scale.z = 0.5f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, head.getCompoundTag("ground"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
					}
					break;
				}
				case FIXED: {
					switch(this.getEquipmentSlot()) {
						case CHEST: {
							if (!chest.hasKey("fixed", 10)) {
								rotation.y = 180.0f;
								translation.x = 0.5f;
								translation.y = -0.65f;
								translation.z = 0.45f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, chest.getCompoundTag("fixed"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case LEGS: {
							if (!legs.hasKey("fixed", 10)) {
								rotation.y = 180.0f;
								translation.x = 0.5f;
								translation.y = 0.05f;
								translation.z = 0.475f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, legs.getCompoundTag("fixed"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						case FEET: {
							if (!feet.hasKey("fixed", 10)) {
								rotation.y = 180.0f;
								translation.x = 0.5f;
								translation.y = 0.2f;
								translation.z = 0.475f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, feet.getCompoundTag("fixed"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
						default: {
							if (!head.hasKey("fixed", 10)) {
								rotation.y = 180.0f;
								translation.x = 0.5f;
								translation.y = -0.85f;
								translation.z = 0.4f;
								scale.x = 0.75f;
								scale.y = 0.75f;
								scale.z = 0.75f;
							} else {
								Vector3f[] data = this.setOptional(rotation, translation, scale, head.getCompoundTag("fixed"));
								rotation = data[0];
								translation = data[1];
								scale = data[2];
							}
							break;
						}
					}
					break;
				}
				default: { break; } // NONE
			}
			this.cameraData.put(transformType, Optional.of(TRSRTransformation.from(new ItemTransformVec3f(rotation, translation, scale))));
		}
	}
	
	private Vector3f[] setOptional(Vector3f rotation, Vector3f translation, Vector3f scale, NBTTagCompound compound) {
		if (compound.hasKey("rotation", 9)) {
			NBTTagList list = compound.getTagList("rotation", 5);
			if (list.tagCount() > 0) { rotation.x = list.getFloatAt(0); }
			if (list.tagCount() > 1) { rotation.y = list.getFloatAt(1); }
			if (list.tagCount() > 2) { rotation.z = list.getFloatAt(2); }
		}
		if (compound.hasKey("translation", 9)) {
			NBTTagList list = compound.getTagList("translation", 5);
			if (list.tagCount() > 0) { translation.x = list.getFloatAt(0); }
			if (list.tagCount() > 1) { translation.y = list.getFloatAt(1); }
			if (list.tagCount() > 2) { translation.z = list.getFloatAt(2); }
		}
		if (compound.hasKey("scale", 9)) {
			NBTTagList list = compound.getTagList("scale", 5);
			if (list.tagCount() > 0) { scale.x = list.getFloatAt(0); }
			if (list.tagCount() > 1) { scale.y = list.getFloatAt(1); }
			if (list.tagCount() > 2) { scale.z = list.getFloatAt(2); }
		}
		return new Vector3f[] { rotation, translation, scale };
	}

	public Optional<TRSRTransformation> getOptional(TransformType transformType) { return this.cameraData.get(transformType); }

	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
		if (tab != CustomRegisters.tabItems && tab != CreativeTabs.SEARCH) { return; }
		if (this.nbtData != null && this.nbtData.hasKey("ShowInCreative", 1) && !this.nbtData.getBoolean("ShowInCreative")) { return; }
		items.add(new ItemStack(this));
		if (tab == CustomRegisters.tabItems) { AdditionalMethods.instance.sort(items); }
	}
	
	@Override
	public int getType() {
		if (this.nbtData != null && this.nbtData.hasKey("ItemType", 1)) { return this.nbtData.getByte("ItemType"); }
		return 0;
	}

}
