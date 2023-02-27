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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;

public class ItemNbtBook
extends Item
implements IPermission {
	
	public ItemNbtBook() {
		this.setRegistryName(CustomNpcs.MODID, "nbt_book");
		this.setUnlocalizedName("nbt_book");
		this.maxStackSize = 1;
		this.setCreativeTab((CreativeTabs) CustomItems.tab);
	}
	
	public void itemEvent(PlayerInteractEvent.RightClickItem event) {
		Server.sendData((EntityPlayerMP) event.getEntityPlayer(), EnumPacketClient.GUI, EnumGuiType.NbtBook, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("Item", true);
		compound.setTag("Data", event.getEntityPlayer().getHeldItemOffhand().writeToNBT(new NBTTagCompound()));
		Server.sendData((EntityPlayerMP) event.getEntityPlayer(), EnumPacketClient.GUI_DATA, compound);
	}

	public void blockEvent(PlayerInteractEvent.RightClickBlock event) {
		Server.sendData((EntityPlayerMP) event.getEntityPlayer(), EnumPacketClient.GUI, EnumGuiType.NbtBook,
				event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
		NBTTagCompound data = new NBTTagCompound();
		TileEntity tile = event.getWorld().getTileEntity(event.getPos());
		if (tile != null) {
			tile.writeToNBT(data);
		}
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("Data", data);
		Server.sendData((EntityPlayerMP) event.getEntityPlayer(), EnumPacketClient.GUI_DATA, compound);
	}

	public void entityEvent(PlayerInteractEvent.EntityInteract event) {
		Server.sendData((EntityPlayerMP) event.getEntityPlayer(), EnumPacketClient.GUI, EnumGuiType.NbtBook, 0, 0, 0);
		NBTTagCompound data = new NBTTagCompound();
		event.getTarget().writeToNBTAtomically(data);
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("EntityId", event.getTarget().getEntityId());
		compound.setTag("Data", data);
		Server.sendData((EntityPlayerMP) event.getEntityPlayer(), EnumPacketClient.GUI_DATA, compound);
	}

	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.NbtBookSaveItem || e == EnumPacketServer.NbtBookSaveEntity || e == EnumPacketServer.NbtBookSaveBlock;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
		if (list==null) { return; }
		list.add(new TextComponentTranslation("info.item.nbt.book").getFormattedText());
	}
	
}
