package noppes.npcs.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.IPermission;

public class ItemNpcWand extends Item implements IPermission {
	public ItemNpcWand() {
		this.setRegistryName(CustomNpcs.MODID, "npcwand");
		this.setUnlocalizedName("npcwand");
		this.setFull3D();
		this.maxStackSize = 1;
		this.setCreativeTab((CreativeTabs) CustomItems.tab);
	}

	public int getMaxItemUseDuration(ItemStack par1ItemStack) {
		return 72000;
	}

	public boolean isAllowed(EnumPacketServer e) {
		return true;
	}

	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		if (!world.isRemote) {
			return (ActionResult<ItemStack>) new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
		}
		CustomNpcs.proxy.openGui(0, 0, 0, EnumGuiType.NpcRemote, player);
		return (ActionResult<ItemStack>) new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
	}

	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos bpos, EnumHand hand, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return EnumActionResult.SUCCESS;
		}
		if (CustomNpcs.OpsOnly && !player.getServer().getPlayerList().canSendCommands(player.getGameProfile())) {
			player.sendMessage(new TextComponentTranslation("availability.permission", new Object[0]));
		} else if (CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.NPC_CREATE)) {
			EntityCustomNpc npc = new EntityCustomNpc(world);
			npc.ais.setStartPos(bpos.up());
			npc.setLocationAndAngles((bpos.getX() + 0.5f), npc.getStartYPos(), (bpos.getZ() + 0.5f), player.rotationYaw,
					player.rotationPitch);
			world.spawnEntity(npc);
			npc.setHealth(npc.getMaxHealth());
			CustomNPCsScheduler.runTack(() -> NoppesUtilServer.sendOpenGui(player, EnumGuiType.MainMenuDisplay, npc),
					100);
		} else {
			player.sendMessage(new TextComponentTranslation("availability.permission", new Object[0]));
		}
		return EnumActionResult.SUCCESS;
	}

	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase playerIn) {
		return stack;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
		if (list==null) { return; }
		list.add(new TextComponentTranslation("info.item.wand").getFormattedText());
		for (int i=0; i<3; i++) {
			list.add(new TextComponentTranslation("info.item.wand."+i).getFormattedText());
		}
	}
	
}
