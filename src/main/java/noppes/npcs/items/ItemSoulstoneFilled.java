package noppes.npcs.items;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleFollower;

public class ItemSoulstoneFilled extends Item {

	public static Entity Spawn(EntityPlayer player, ItemStack stack, World world, BlockPos pos) {
		if (world.isRemote) {
			return null;
		}
		if (stack.getTagCompound() == null || !stack.getTagCompound().hasKey("Entity", 10)) {
			return null;
		}
		NBTTagCompound compound = stack.getTagCompound().getCompoundTag("Entity");
		if (compound.getString("id").equals("minecraft:customnpcs.customnpc")
				|| compound.getString("id").equals("minecraft:customnpcs:customnpc")) {
			compound.setString("id", CustomNpcs.MODID + ":customnpc");
		}
		Entity entity = EntityList.createEntityFromNBT(compound, world);
		if (entity == null) {
			return null;
		}
		entity.setPosition(pos.getX() + 0.5, (pos.getY() + 1 + 0.2f), pos.getZ() + 0.5);
		if (entity instanceof EntityNPCInterface) {
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			npc.ais.setStartPos(pos);
			npc.setHealth(npc.getMaxHealth());
			npc.setPosition((pos.getX() + 0.5f), npc.getStartYPos(), (pos.getZ() + 0.5f));
			if (npc.advanced.roleInterface instanceof RoleCompanion && player != null) {
				PlayerData data = PlayerData.get(player);
				if (data.hasCompanion()) {
					return null;
				}
				((RoleCompanion) npc.advanced.roleInterface).setOwner(player);
				data.setCompanion(npc);
			}
			if (npc.advanced.roleInterface instanceof RoleFollower && player != null) {
				((RoleFollower) npc.advanced.roleInterface).setOwner(player);
			}
		}
		if (!world.spawnEntity(entity)) {
			player.sendMessage(new TextComponentTranslation("error.failedToSpawn", new Object[0]));
			return null;
		}
		return entity;
	}

	public ItemSoulstoneFilled() {
		this.setRegistryName(CustomNpcs.MODID, "npcsoulstonefilled");
		this.setUnlocalizedName("npcsoulstonefilled");
		this.setMaxStackSize(1);
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		NBTTagCompound compound = stack.getTagCompound();
		if (compound == null || !compound.hasKey("Entity", 10)) {
			list.add(TextFormatting.RED + "Error");
			return;
		}
		String name = new TextComponentTranslation(compound.getString("Name")).getFormattedText();
		if (compound.hasKey("DisplayName")) {
			name = compound.getString("DisplayName") + " (" + name + ")";
		}
		list.add(TextFormatting.BLUE + name);
		if (stack.getTagCompound().hasKey("ExtraText")) {
			String text = "";
			for (String s : compound.getString("ExtraText").split(",")) {
				text += new TextComponentTranslation(s).getFormattedText();
			}
			list.add(text);
		}
	}

	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return EnumActionResult.SUCCESS;
		}
		ItemStack stack = player.getHeldItem(hand);
		if (Spawn(player, stack, world, pos) == null) {
			return EnumActionResult.FAIL;
		}
		if (!player.capabilities.isCreativeMode) {
			stack.splitStack(1);
		}
		return EnumActionResult.SUCCESS;
	}

}
