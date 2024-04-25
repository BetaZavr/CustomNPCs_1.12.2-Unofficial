package noppes.npcs.items;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.util.ObfuscationHelper;

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
	protected NBTTagCompound nbtData = new NBTTagCompound();
	protected ItemStack repairItemStack = ItemStack.EMPTY;
	protected int enchantability = 0;

	public ResourceLocation objModel = null;

	private Map<EnumParts, List<String>> parts;

	public CustomArmor(ItemArmor.ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn,
			int maxStackDamage, int damageReduceAmount, float toughness, NBTTagCompound nbtItem) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
		this.nbtData = nbtItem;
		String name = "custom_" + nbtItem.getString("RegistryName") + "_" + equipmentSlotIn.name().toLowerCase();
		this.setRegistryName(CustomNpcs.MODID, name);
		this.setUnlocalizedName(name);
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
		if (this.nbtData.hasKey("OBJData", 9)) {
			this.parts = Maps.<EnumParts, List<String>>newHashMap();
			NBTTagList data = this.nbtData.getTagList("OBJData", 10);
			for (EnumParts part : EnumParts.values()) {
				NBTTagCompound nbt = null;
				switch (part) {
				case HEAD: {
					nbt = data.getCompoundTagAt(0);
					break;
				}
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
				default: {
					break;
				}
				}
				if (nbt == null) {
					continue;
				}
				List<String> list = Lists.<String>newArrayList();
				if (nbt != null) {
					for (int i = 0; i < nbt.getTagList("meshes", 8).tagCount(); i++) {
						list.add(nbt.getTagList("meshes", 8).getStringTagAt(i));
					}
				}
				this.parts.put(part, list);
			}
			this.objModel = new ResourceLocation(CustomNpcs.MODID,
					"models/armor/" + nbtItem.getString("RegistryName") + ".obj");
		}
		this.setCreativeTab((CreativeTabs) CustomRegisters.tabItems);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack itemStack, EntityEquipmentSlot slot,
			ModelBiped defModel) {
		if (this.objModel != null) {
			return ModelBuffer.getOBJModelBiped(this, entity, itemStack, slot, defModel);
		}
		return super.getArmorModel(entity, itemStack, slot, defModel);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
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
		return NpcAPI.Instance().getINbt(this.nbtData);
	}

	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
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
		if (this.parts.containsKey(slot)) {
			return this.parts.get(slot);
		}
		return Lists.<String>newArrayList();
	}

	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (tab != CustomRegisters.tabItems || (this.nbtData != null && this.nbtData.hasKey("ShowInCreative", 1)
				&& !this.nbtData.getBoolean("ShowInCreative"))) {
			return;
		}
		items.add(new ItemStack(this));
	}

}
