package noppes.npcs.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.Server;
import noppes.npcs.api.item.ISpecBuilder;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.controllers.SyncController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.util.BuilderData;
import noppes.npcs.util.IPermission;

public class ItemBuilder // set schematics
extends Item
implements IPermission, ISpecBuilder {
	
	private EnumGuiType guiType = EnumGuiType.BuilderSetting;
	
	public ItemBuilder() {
		this.setRegistryName(CustomNpcs.MODID, "npcbuilder");
		this.setUnlocalizedName("npcbuilder");
		this.maxStackSize = 1;
		this.setCreativeTab((CreativeTabs) CustomRegisters.tab);
		this.setHasSubtypes(true);
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
			list.add(new TextComponentTranslation("info.item.builder").getFormattedText());
			for (int i = 3; i <= 5; i++) {
				list.add(new TextComponentTranslation("info.item.builder.main." + i).getFormattedText());
			}
			list.add(new TextComponentTranslation("info.item.builder.range.0", "" + builder.region[0], "" + builder.region[1], "" + builder.region[2]).getFormattedText());
		} else {
			list.add(new TextComponentTranslation("info.item.builder.main.2").getFormattedText());
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("ID", 3) && stack.getTagCompound().hasKey("BuilderType", 3)) {
				NoppesUtilPlayer.sendDataCheakDelay(EnumPlayerPacket.GetBuildData, stack, 2000, stack.getTagCompound().getInteger("ID"), stack.getTagCompound().getInteger("BuilderType"));
			}
		}
	}
	
	@Override
	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.BuilderSetting || e == EnumPacketServer.Gui;
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
		IBlockState state = player.world.getBlockState(pos);
		ItemStack st = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		if (builder.inv.isFull() || st.isEmpty()) {
			return;
		}
		TileEntity tile = player.world.getTileEntity(pos);
		if (tile != null) { st.setTagCompound(tile.writeToNBT(new NBTTagCompound())); }
		String name = st.getItem().getRegistryName().toString() + (st.getItemDamage() != 0 ? " [" + st.getItemDamage() + "]" : "");
		int emp = 0;
		for (int i = 1; i < 10; i++) {
			if (emp == 0 && builder.inv.getStackInSlot(i).isEmpty()) {
				emp = i;
			}
			if (!builder.inv.getStackInSlot(i).isEmpty() && builder.inv.getStackInSlot(i).isItemEqual(st)) {
				player.sendMessage(new TextComponentTranslation("builder.err.add.block.0"));
				return;
			}
		}
		if (emp == 0) {
			player.sendMessage(new TextComponentTranslation("builder.err.add.block.1"));
			return;
		}
		builder.inv.items.set(emp, st);
		builder.chances.put(emp, 100);
		player.sendMessage(new TextComponentTranslation("builder.add.block", name));
		NBTTagCompound nbtStack = builder.getNbt();
		stack.setTagCompound(nbtStack);
		player.openContainer.detectAndSendChanges();
		Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.BuilderData, nbtStack);
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
	public int getType() { return 1; }

	public static BuilderData getBuilder(ItemStack stack, EntityPlayer player) {
		if (stack.getItem() instanceof ISpecBuilder && stack.hasTagCompound() && stack.getTagCompound().hasKey("ID", 3)) {
			BuilderData builder = SyncController.dataBuilder.get(stack.getTagCompound().getInteger("ID"));
			if (builder == null) {
				builder = new BuilderData(stack.getTagCompound().getInteger("ID"), ((ISpecBuilder) stack.getItem()).getType());
				builder.read(stack.getTagCompound());
				SyncController.dataBuilder.put(stack.getTagCompound().getInteger("ID"), builder);
				if (player instanceof EntityPlayerMP) { Server.sendData((EntityPlayerMP) player, EnumPacketClient.SYNC_UPDATE, EnumSync.BuilderData, builder.getNbt()); }
			}
			return builder;
		}
		return null;
	}
	
	public static boolean isBuilder(ItemStack stack, BuilderData bd) {
		if (stack.getItem() instanceof ISpecBuilder && stack.hasTagCompound() && stack.getTagCompound().hasKey("ID", 3)) {
			BuilderData baseBD = SyncController.dataBuilder.get(stack.getTagCompound().getInteger("ID"));
			return baseBD != null && baseBD.getID() == bd.getID() && baseBD.getType() == bd.getType();
		}
		return false;
	}

	@Override
	public EnumGuiType getGUIType() { return this.guiType; }

}
