package noppes.npcs.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.item.ISpecBuilder;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.util.BuilderData;
import noppes.npcs.util.IPermission;

public class ItemSaver
extends Item
implements IPermission, ISpecBuilder {

	private EnumGuiType guiType = EnumGuiType.SaverSetting;
	
	public ItemSaver() {
		this.setRegistryName(CustomNpcs.MODID, "npcsaver");
		this.setUnlocalizedName("npcsaver");
		this.maxStackSize = 1;
		this.setCreativeTab((CreativeTabs) CustomRegisters.tab);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
		if (list == null) {
			return;
		}
		BuilderData builder = ItemBuilder.getBuilder(stack, null);
		list.add(new TextComponentTranslation("info.item.builder.main.0").getFormattedText());
		list.add(new TextComponentTranslation("info.item.builder.main.1").getFormattedText());
		if (builder != null) {
			list.add(new TextComponentTranslation("info.item.saver").getFormattedText());
			for (int i = 7; i <= 8; i++) {
				list.add(new TextComponentTranslation("info.item.builder.main." + i).getFormattedText());
			}
			list.add(new TextComponentTranslation("info.item.builder.range.1", "" + builder.region[0], "" + builder.region[1], "" + builder.region[2]).getFormattedText());
		} else {
			list.add(new TextComponentTranslation("info.item.builder.main.2").getFormattedText());
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("ID", 8) && stack.getTagCompound().hasKey("BuilderType", 3)) {
				NoppesUtilPlayer.sendDataCheakDelay(EnumPlayerPacket.GetBuildData, stack, 2000, stack.getTagCompound().getString("ID"), stack.getTagCompound().getInteger("BuilderType"));
			}
		}
	}
	
	@Override
	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.BuilderSetting  || e == EnumPacketServer.Gui || e == EnumPacketServer.SchematicsBuild;
	}

	@Override
	public void leftClick(ItemStack stack, EntityPlayerMP player, BlockPos pos) {
		if (pos == null) { return; }
		PlayerData data = PlayerData.get(player);
		BuilderData builder = ItemBuilder.getBuilder(stack, player);
		if (data == null || !stack.hasTagCompound() || builder == null || builder.getID() == -1) {
			NoppesUtilServer.sendOpenGui(player, this.guiType, null, -1, this.getType(), 0);
			return;
		}
		if (data.hud.hasOrKeysPressed(new int[] { 29, 157 })) { // Ctrl pressed <-
			builder.undo();
			return;
		}
		builder.schMap.clear();
	}

	@Override
	public void rightClick(ItemStack stack, EntityPlayerMP player, BlockPos pos) {
		if (pos == null) { return; }
		PlayerData data = PlayerData.get(player);
		BuilderData builder = ItemBuilder.getBuilder(stack, player);
		if (data == null || !stack.hasTagCompound() || builder == null || builder.getID() == -1) {
			NoppesUtilServer.sendOpenGui(player, this.guiType, null, -1, this.getType(), 0);
			return;
		}
		if (data.hud.hasOrKeysPressed(new int[] { 29, 157 })) { // Ctrl pressed ->
			builder.redo();
			return;
		}
		builder.work(pos, player);
	}

	@Override
	public int getType() { return 4; }

	@Override
	public EnumGuiType getGUIType() { return this.guiType; }

}
