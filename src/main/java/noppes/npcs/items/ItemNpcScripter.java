package noppes.npcs.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.api.item.INPCToolItem;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;

public class ItemNpcScripter extends Item implements IPermission, INPCToolItem {

	public ItemNpcScripter() {
		this.setRegistryName(CustomNpcs.MODID, "npcscripter");
		this.setUnlocalizedName("npcscripter");
		this.setFull3D();
		this.maxStackSize = 1;
		this.setCreativeTab(CustomRegisters.tab);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> list, @Nonnull ITooltipFlag flagIn) {
        list.add(new TextComponentTranslation("info.item.scripter").getFormattedText());
		list.add(new TextComponentTranslation("info.item.scripter.0").getFormattedText());
	}

	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.ScriptDataGet || e == EnumPacketServer.ScriptDataSave
				|| e == EnumPacketServer.ScriptBlockDataSave || e == EnumPacketServer.ScriptDoorDataSave
				|| e == EnumPacketServer.ScriptPlayerGet || e == EnumPacketServer.ScriptPlayerSave
				|| e == EnumPacketServer.ScriptForgeGet || e == EnumPacketServer.ScriptForgeSave
				|| e == EnumPacketServer.ScriptNpcsGet || e == EnumPacketServer.ScriptNpcsSave
				|| e == EnumPacketServer.ScriptPotionGet || e == EnumPacketServer.ScriptPotionSave
				|| e == EnumPacketServer.ScriptClientGet || e == EnumPacketServer.ScriptClientSave;
	}

	public @Nonnull ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		if (!world.isRemote || hand != EnumHand.MAIN_HAND) {
			return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
		}
		CustomNpcs.proxy.openGui(0, 0, 0, EnumGuiType.ScriptPlayers, player);
		return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
	}

}
