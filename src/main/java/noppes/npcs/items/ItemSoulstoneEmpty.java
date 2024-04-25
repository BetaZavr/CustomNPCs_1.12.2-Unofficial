package noppes.npcs.items;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.CustomRegisters;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleFollower;

public class ItemSoulstoneEmpty extends Item {

	public ItemSoulstoneEmpty() {
		this.setRegistryName(CustomNpcs.MODID, "npcsoulstoneempty");
		this.setUnlocalizedName("npcsoulstoneempty");
		this.setCreativeTab((CreativeTabs) CustomRegisters.tab);
		this.setMaxStackSize(64);
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add(new TextComponentTranslation("info.item.soulstone.0").getFormattedText());
	}

	public boolean hasPermission(EntityLivingBase entity, EntityPlayer player) {
		if (NoppesUtilServer.isOp(player)) {
			return true;
		}
		if (CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.SOULSTONE_ALL)) {
			return true;
		}
		if (entity instanceof EntityNPCInterface) {
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			if (npc.advanced.roleInterface instanceof RoleCompanion) {
				if (((RoleCompanion) npc.advanced.roleInterface).getOwner() == player) {
					return true;
				}
			}
			if (npc.advanced.roleInterface instanceof RoleFollower) {
				if (((RoleFollower) npc.advanced.roleInterface).getOwner() == player) {
					return !((RoleFollower) npc.advanced.roleInterface).refuseSoulStone;
				}
			}
			return CustomNpcs.SoulStoneNPCs;
		}
		return entity instanceof EntityAnimal && CustomNpcs.SoulStoneAnimals;
	}

	public boolean store(EntityLivingBase entity, ItemStack stack, EntityPlayer player) {
		if (!this.hasPermission(entity, player) || entity instanceof EntityPlayer) {
			return false;
		}
		ItemStack stone = new ItemStack(CustomRegisters.soulstoneFull);
		NBTTagCompound compound = new NBTTagCompound();
		if (!entity.writeToNBTAtomically(compound)) {
			return false;
		}
		if (compound.getString("id").equals("minecraft:customnpcs.customnpc")
				|| compound.getString("id").equals("minecraft:customnpcs:customnpc")) {
			compound.setString("id", CustomNpcs.MODID + ":customnpc");
		}
		ServerCloneController.Instance.cleanTags(compound);
		stone.setTagInfo("Entity", compound);
		String name = EntityList.getEntityString(entity);
		if (name == null) {
			name = "generic";
		}
		stone.setTagInfo("Name", new NBTTagString("entity." + name + ".name"));
		if (entity instanceof EntityNPCInterface) {
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			stone.setTagInfo("DisplayName", new NBTTagString(entity.getName()));
			if (npc.advanced.roleInterface instanceof RoleCompanion) {
				stone.setTagInfo("ExtraText", new NBTTagString(
						"companion.stage,: ," + ((RoleCompanion) npc.advanced.roleInterface).stage.name));
			}
		} else if (entity instanceof EntityLiving && (entity).hasCustomName()) {
			stone.setTagInfo("DisplayName", new NBTTagString((entity).getCustomNameTag()));
		}
		NoppesUtilServer.GivePlayerItem(player, player, stone);
		if (!player.capabilities.isCreativeMode) {
			stack.splitStack(1);
			if (stack.getCount() <= 0) {
				player.inventory.deleteStack(stack);
			}
		}
		return entity.isDead = true;
	}

}
