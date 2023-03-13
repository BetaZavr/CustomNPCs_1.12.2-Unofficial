package noppes.npcs.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CommonProxy;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.Server;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumBuilder;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.util.BuilderData;
import noppes.npcs.util.IPermission;

public class ItemBuilder
extends Item
implements IPermission {

	public ItemBuilder() {
		this.setRegistryName(CustomNpcs.MODID, "npcbuilder");
		this.setUnlocalizedName("npcbuilder");
		this.maxStackSize = 1;
		this.setCreativeTab((CreativeTabs) CustomItems.tab);
        this.setHasSubtypes(true);
	}

	@Override
	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.BuilderSetting ||
				e == EnumPacketServer.OpenBuilder ||
				e == EnumPacketServer.SchematicsBuild;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
		if (list==null) { return; }
		list.add(new TextComponentTranslation("info.item.builder").getFormattedText());
		list.add(new TextComponentString(((char) 167)+"cWIP").getFormattedText());
		for (int i=0; i<5; i++) {
			list.add(new TextComponentTranslation("info.item.builder."+i).getFormattedText());
			if (i==4 && (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("ID", 3))) { return; }
		}
		ItemBuilder.cheakStack(stack);
		BuilderData builder = CommonProxy.dataBuilder.get(stack.getTagCompound().getInteger("ID"));
		if (builder==null) {
			Client.sendDataDelayCheck(EnumPlayerPacket.GetBuildData, this, 5000);
			return;
		}
		if (builder.type!=4) {
			list.add(new TextComponentTranslation("info.item.builder."+builder.type+".0").getFormattedText());
			if (builder.type!=3) {
				list.add(new TextComponentTranslation("info.item.builder."+(builder.type==2 ? "6" : "5")).getFormattedText());
				list.add(new TextComponentTranslation("info.item.builder.range.0", ""+builder.region[0], ""+builder.region[1], ""+builder.region[2]).getFormattedText());
			} else {
				list.add(new TextComponentTranslation("info.item.builder.range.1", ""+builder.region[0], ""+builder.region[1], ""+builder.region[2]).getFormattedText());
			}
		} else {
			for (int i=0; i<4; i++) { list.add(new TextComponentTranslation("info.item.builder.4."+i).getFormattedText()); }
			list.add(new TextComponentTranslation("info.item.builder.range.1", ""+builder.region[0], ""+builder.region[1], ""+builder.region[2]).getFormattedText());
			list.add(new TextComponentTranslation("gui.name").getFormattedText()+": "+(builder.schematicaName.isEmpty() ? "empty" : builder.schematicaName+".schematic"));
		}
	}
	
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) { return; }
        ItemStack stack = new ItemStack(this);
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("BuilderType", 0);
        compound.setIntArray("Region", new int[] { 5, 2, 3 });
		stack.setTagCompound(compound);
        items.add(stack);
    }
	
	public String getUnlocalizedName(ItemStack stack) {
        int i = stack.getMetadata();
        return super.getUnlocalizedName() + "." +EnumBuilder.values()[i].name();
    }
	
	public void leftClick(ItemStack stack, EntityPlayerMP player) {
		if (true) { return; }
		PlayerData data = PlayerData.get(player);
		if (data==null || !stack.hasTagCompound()) { return; }
		ItemBuilder.cheakStack(stack);
		BuilderData builder = CommonProxy.dataBuilder.get(stack.getTagCompound().getInteger("ID"));
		if (data.hud.hasOrKeysPressed(new int [] { 29, 157 })) { // Ctrl pressed <-
			builder.undo();
			return;
		}
		Vec3d vec3d = player.getPositionEyes(1.0f);
		Vec3d vec3d2 = player.getLook(1.0f);
		Vec3d vec3d3 = vec3d.addVector(vec3d2.x * 5.0d, vec3d2.y * 5.0d, vec3d2.z * 5.0d);
		RayTraceResult result = player.world.rayTraceBlocks(vec3d, vec3d3, false, false, false);
		BlockPos pos = null;
		if (result!=null && result.getBlockPos()!=null) { pos = result.getBlockPos(); }
		if (pos==null) { return; }
		if (builder.type==4 && data.hud.hasOrKeysPressed(new int [] { 42, 54 })) { // Shift pressed
			builder.schMap.clear();
			Server.sendData(player, EnumPacketClient.BUILDER_SETTING, builder.getNbt());
			return;
		}
		if (builder.type==4) { builder.schMap.clear(); return; }
		ItemStack st = new ItemStack(player.world.getBlockState(pos).getBlock());
		if (builder.inv.isFull() || st.isEmpty()) {
			return;
		}
		TileEntity tile = player.world.getTileEntity(pos);
		if (tile!=null) { st.setTagCompound(tile.writeToNBT(new NBTTagCompound())); }
		String name = st.getItem().getRegistryName().toString();
		
		if (builder.type==2) {
			builder.inv.items.set(0, st);
			player.sendMessage(new TextComponentTranslation("builder.put.block", name));
			return;
		}
		
		int emp = 0;
		for (int i=1; i<10; i++) {
			if (emp==0 && builder.inv.getStackInSlot(i).isEmpty()) { emp = i; }
			if (!builder.inv.getStackInSlot(i).isEmpty() && builder.inv.getStackInSlot(i).isItemEqual(st)) { 
				player.sendMessage(new TextComponentTranslation("builder.err.add.block.0"));
				return;
			}
		}
		if (emp==0) {
			player.sendMessage(new TextComponentTranslation("builder.err.add.block.1"));
			return;
		}
		builder.inv.items.set(emp, st);
		builder.chances.put(emp, 100);
		player.sendMessage(new TextComponentTranslation("builder.add.block", name));
	}
	
	public void rightClick(ItemStack stack, EntityPlayerMP player) {
		if (true) { return; }
		PlayerData data = PlayerData.get(player);
		if (data==null || !stack.hasTagCompound()) { return; }
		ItemBuilder.cheakStack(stack);
		BuilderData builder = CommonProxy.dataBuilder.get(stack.getTagCompound().getInteger("ID"));
		if (data.hud.hasOrKeysPressed(new int [] { 29, 157 })) { // Ctrl pressed ->
			builder.redo();
			return;
		}
		Vec3d vec3d = player.getPositionEyes(1.0f);
		Vec3d vec3d2 = player.getLook(1.0f);
		Vec3d vec3d3 = vec3d.addVector(vec3d2.x * 5.0d, vec3d2.y * 5.0d, vec3d2.z * 5.0d);
		RayTraceResult result = player.world.rayTraceBlocks(vec3d, vec3d3, false, false, false);
		BlockPos pos = null;
		if (result!=null && result.getBlockPos()!=null) { pos = result.getBlockPos(); }
		if (pos==null) { return; }
		if (data.hud.hasOrKeysPressed(new int [] { 42, 54 })) { // Shift pressed
			Server.sendData(player, EnumPacketClient.BUILDER_SETTING, builder.getNbt());
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.BuilderSetting, null, builder.id, 0, 0);
			return;
		}
		builder.work(pos, player);
	}

	public static void cheakStack(ItemStack stack) {
		NBTTagCompound compound = stack.getTagCompound();
		if (compound==null) { stack.setTagCompound(compound = new NBTTagCompound()); }
		int id = compound.getInteger("ID");
		if (!compound.hasKey("ID", 3) || id<1) {
			id = 1;
			while (CommonProxy.dataBuilder.containsKey(id)) { id++; }
			compound.setInteger("ID", id);
		}
		if (!CommonProxy.dataBuilder.containsKey(id)) {
			BuilderData builder = new BuilderData();
			builder.read(compound);
			builder.id = id;
			CommonProxy.dataBuilder.put(id, builder);
		}
		else {
			NBTTagCompound nbt = CommonProxy.dataBuilder.get(id).getNbt();
			for (String key : nbt.getKeySet()) { compound.setTag(key, nbt.getTag(key)); }
		}
		stack.setItemDamage(CommonProxy.dataBuilder.get(id).type);
	}

	public static boolean isBulderItem(BuilderData bd, ItemStack stack) {
		if (bd==null || stack==null || stack.isEmpty()) { return false; }
		return stack.getItem() instanceof ItemBuilder && stack.hasTagCompound() && stack.getTagCompound().getInteger("ID")==bd.id;
	}
	
}
