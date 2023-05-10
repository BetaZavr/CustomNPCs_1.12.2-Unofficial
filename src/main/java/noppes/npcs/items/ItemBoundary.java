package noppes.npcs.items;

import java.awt.Point;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Zone3D;
import noppes.npcs.util.IPermission;

public class ItemBoundary
extends Item
implements IPermission {

	public ItemBoundary() {
		this.setRegistryName(CustomNpcs.MODID, "npcboundary");
		this.setUnlocalizedName("npcboundary");
		this.maxStackSize = 1;
		this.setCreativeTab((CreativeTabs) CustomItems.tab);
	}

	@Override
	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.TeleportTo || e == EnumPacketServer.RegionData;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
		if (list==null) { return; }
		list.add(new TextComponentTranslation("info.item.boundary", new TextComponentTranslation("tile.npcborder.name").getFormattedText()).getFormattedText());
		Zone3D reg = null;
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("RegionID", 3)) {
			reg = (Zone3D) BorderController.getInstance().getRegion(stack.getTagCompound().getInteger("RegionID"));
		}
		if (reg==null) {
			list.add(new TextComponentTranslation("info.item.boundary.2").getFormattedText());
			list.add(new TextComponentTranslation("info.item.boundary.3").getFormattedText());
			return;
		}
		for (int i=0; i<4; i++) {
			list.add(new TextComponentTranslation("info.item.boundary."+i).getFormattedText());
		}
		list.add(new TextComponentTranslation("info.item.boundary.4", ""+reg.getId(), reg.name).getFormattedText());
	}

	public void leftClick(ItemStack stack, EntityPlayerMP player) {
		PlayerData data = PlayerData.get(player);
		if (data==null) { return; }
		int id = -1;
		Vec3d vec3d = player.getPositionEyes(1.0f);
		Vec3d vec3d2 = player.getLook(1.0f);
		Vec3d vec3d3 = vec3d.addVector(vec3d2.x * 5.0d, vec3d2.y * 5.0d, vec3d2.z * 5.0d);
		RayTraceResult result = player.world.rayTraceBlocks(vec3d, vec3d3, false, false, false);
		BlockPos pos = null;
		if (result!=null && result.getBlockPos()!=null) {
			int x = result.getBlockPos().getX();
			int y = result.getBlockPos().getY();
			int z = result.getBlockPos().getZ();
			try {
				switch(result.sideHit) {
					case UP: { y += 1; break; }
					case NORTH: { z -= 1; break; }
					case SOUTH: { z += 1; break; }
					case WEST: { x -= 1; break; }
					case EAST: { x += 1; break; }
					default: { y -= 1; break; }
				}
			}
			catch (Exception e) { }
			pos = new BlockPos(x, y, z);
		}
		if (pos==null) { return; }
		BorderController bData = BorderController.getInstance();
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("RegionID", 3)) { id = stack.getTagCompound().getInteger("RegionID"); }
		// Shift + LMB = New Region
		if (data.hud.hasOrKeysPressed(new int [] { 42, 54 })) {
			Zone3D reg = bData.createNew(player.world.provider.getDimension(), pos);
			bData.saveRegions();
			bData.sendToAll(reg.getId());
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.BoundarySetting, null, reg.getId(), 0, 0);
			NBTTagCompound compound = stack.getTagCompound();
			if (compound==null) { stack.setTagCompound(compound = new NBTTagCompound()); }
			compound.setInteger("RegionID", reg.getId());
			return;
		}
		// LMB = remove point
		Zone3D reg = (Zone3D) bData.getRegion(id);
		if (reg==null) { return; }
		Point p = reg.points.get(reg.getIdNearestPoint(player.getPosition()));
		if (p==null || !reg.contains(p.x, p.y)) { return; }
		boolean remove = reg.removePoint(p.x, p.y);
		player.sendMessage(new TextComponentTranslation("message.boundary.del.vertex."+remove, ""+p.x, ""+p.y, reg.toString()));
		if (remove) {
			reg.fix();
			bData.saveRegions();
			bData.sendToAll(id);
		}
	}

	public void rightClick(ItemStack stack, EntityPlayerMP player) {
		PlayerData data = PlayerData.get(player);
		if (data==null) { return; }
		int id = -1;
		BorderController bData = BorderController.getInstance();
		Vec3d vec3d = player.getPositionEyes(1.0f);
		Vec3d vec3d2 = player.getLook(1.0f);
		Vec3d vec3d3 = vec3d.addVector(vec3d2.x * 5.0d, vec3d2.y * 5.0d, vec3d2.z * 5.0d);
		RayTraceResult result = player.world.rayTraceBlocks(vec3d, vec3d3, false, false, false);
		BlockPos pos = null;
		if (result!=null && result.getBlockPos()!=null) {
			int x = result.getBlockPos().getX();
			int y = result.getBlockPos().getY();
			int z = result.getBlockPos().getZ();
			try {
				switch(result.sideHit) {
					case UP: { y += 1; break; }
					case NORTH: { z -= 1; break; }
					case SOUTH: { z += 1; break; }
					case WEST: { x -= 1; break; }
					case EAST: { x += 1; break; }
					default: { y -= 1; break; }
				}
			}
			catch (Exception e) { }
			pos = new BlockPos(x, y, z);
		}
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("RegionID", 3)) { id = stack.getTagCompound().getInteger("RegionID"); }
		Zone3D reg = (Zone3D) bData.getRegion(id);
		// Shift + RMB = Show Region settings
		if (reg==null || data.hud.hasOrKeysPressed(new int [] { 42, 54 })) { // Shift pressed
			if (reg!=null && pos==null) { pos = player.getPosition(); }
			if (bData.regions.size()>0) {
				NoppesUtilServer.sendOpenGui(player, EnumGuiType.BoundarySetting, null, id, reg==null ? -1 : reg.getIdNearestPoint(pos), 0);
			}
			return;
		}
		// RMB = add point
		if (pos!=null) {
			boolean add = false;
			if (reg.contains(pos.getX(), pos.getZ())) { // Offset Y min/max
				int min = Math.abs(reg.y[0]-pos.getY());
				int max = Math.abs(reg.y[1]-pos.getY());
				if (min<=max) { reg.y[0] = pos.getY(); }
				else { reg.y[1] = pos.getY(); add= true; }
				player.sendMessage(new TextComponentTranslation("message.boundary.offset.y."+add, ""+pos.getX(),""+pos.getY(),""+pos.getZ(), reg.toString()));
				add = true;
			} else { // add new point
				add = reg.insertPoint(pos.getX(), pos.getY(), pos.getZ(), player.getPosition());
				player.sendMessage(new TextComponentTranslation("message.boundary.add.vertex."+add, ""+pos.getX(),""+pos.getY(),""+pos.getZ(), reg.toString()));
			}
			if (add) {
				reg.fix();
				bData.saveRegions();
				bData.sendToAll(reg.getId());
			}
		}
	}

}