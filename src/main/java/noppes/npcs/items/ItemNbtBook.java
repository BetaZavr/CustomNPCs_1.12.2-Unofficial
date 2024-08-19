package noppes.npcs.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
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
import noppes.npcs.Server;
import noppes.npcs.api.item.INPCToolItem;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;

public class ItemNbtBook extends Item implements IPermission, INPCToolItem {

	public ItemNbtBook() {
		this.setRegistryName(CustomNpcs.MODID, "nbt_book");
		this.setUnlocalizedName("nbt_book");
		this.maxStackSize = 1;
		this.setCreativeTab(CustomRegisters.tab);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> list, @Nonnull ITooltipFlag flagIn) {
        list.add(new TextComponentTranslation("info.item.nbt.book").getFormattedText());
	}

	public void blockEvent(EntityPlayerMP player, BlockPos pos) {
		Server.sendData(player, EnumPacketClient.GUI, EnumGuiType.NbtBook, pos.getX(), pos.getY(), pos.getZ());
		NBTTagCompound data = new NBTTagCompound();
		TileEntity tile = player.world.getTileEntity(pos);
		if (tile != null) {
			tile.writeToNBT(data);
		}
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("Data", data);
		Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
	}

	public void entityEvent(EntityPlayerMP player, Entity target) {
		Server.sendData(player, EnumPacketClient.GUI, EnumGuiType.NbtBook, 0, 0, 0);
		NBTTagCompound data = new NBTTagCompound();
		target.writeToNBTAtomically(data);
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("EntityId", target.getEntityId());
		compound.setTag("Data", data);
		Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
	}
	
	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.NbtBookSaveItem || e == EnumPacketServer.NbtBookSaveEntity
				|| e == EnumPacketServer.NbtBookSaveBlock || e == EnumPacketServer.NbtBookCopyStack;
	}

	public void itemEvent(EntityPlayerMP player) {
		Server.sendData(player, EnumPacketClient.GUI, EnumGuiType.NbtBook, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("Item", true);
		compound.setTag("Data", player.getHeldItemOffhand().writeToNBT(new NBTTagCompound()));
		Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
	}


}
