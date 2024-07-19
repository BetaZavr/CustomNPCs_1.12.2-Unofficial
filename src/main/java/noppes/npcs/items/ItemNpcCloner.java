package noppes.npcs.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.api.item.INPCToolItem;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.util.IPermission;

public class ItemNpcCloner extends Item implements IPermission, INPCToolItem {

	public ItemNpcCloner() {
		this.setRegistryName(CustomNpcs.MODID, "npcmobcloner");
		this.setUnlocalizedName("npcmobcloner");
		this.setFull3D();
		this.maxStackSize = 1;
		this.setCreativeTab(CustomRegisters.tab);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> list, @Nonnull ITooltipFlag flagIn) {
		list.add(new TextComponentTranslation("info.item.cloner").getFormattedText());
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || !nbt.hasKey("Settings", 10)) {
			list.add(new TextComponentTranslation("info.item.cloner.empty.0").getFormattedText());
			list.add(new TextComponentTranslation("info.item.cloner.empty.1").getFormattedText());
		} else {
			list.add(new TextComponentTranslation("info.item.cloner.set.0",
					nbt.getCompoundTag("Settings").getString("Name")).getFormattedText());
			list.add(new TextComponentTranslation("info.item.cloner.set.1").getFormattedText());
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean hasEffect(@Nonnull ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		return super.hasEffect(stack) || (nbt != null && nbt.hasKey("Settings", 10)
				&& !nbt.getCompoundTag("Settings").getString("Name").isEmpty());
	}

	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.CloneList || e == EnumPacketServer.SpawnMob || e == EnumPacketServer.MobSpawner
				|| e == EnumPacketServer.ClonePreSave || e == EnumPacketServer.CloneRemove
				|| e == EnumPacketServer.CloneSave || e == EnumPacketServer.GetClone || e == EnumPacketServer.Gui;
	}

	public @Nonnull EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			PlayerData data = CustomNpcs.proxy.getPlayerData(player);
			boolean summon = false;
			ItemStack stackCloner = player.getHeldItemMainhand();
			if (stackCloner.getItem() instanceof ItemNpcCloner && data != null && data.hud.hasOrKeysPressed(42, 54)) {
				NBTTagCompound nbt = stackCloner.getTagCompound();
				if (nbt != null && nbt.hasKey("Settings", 10)) {
					NBTTagCompound nbtData = nbt.getCompoundTag("Settings");
					if (nbtData.getBoolean("isServerClone")) {
						Client.sendData(EnumPacketServer.SpawnMob, true, pos.getX(), pos.getY(), pos.getZ(),
								nbtData.getString("Name"), nbtData.getInteger("Tab"));
                    } else {
						Client.sendData(EnumPacketServer.SpawnMob, false, pos.getX(), pos.getY(), pos.getZ(),
								nbtData.getCompoundTag("EntityNBT"));
                    }
                    summon = true;
                }
			}
			if (!summon) {
				Client.sendData(EnumPacketServer.Gui, EnumGuiType.MobSpawner, pos.getX(), pos.getY(), pos.getZ());
			}
		}
		return EnumActionResult.SUCCESS;
	}

}
