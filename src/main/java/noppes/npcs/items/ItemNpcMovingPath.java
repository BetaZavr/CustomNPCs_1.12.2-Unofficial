package noppes.npcs.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.CustomRegisters;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.item.INPCToolItem;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.IPermission;

public class ItemNpcMovingPath extends Item implements IPermission, INPCToolItem {

	public ItemNpcMovingPath() {
		this.setRegistryName(CustomNpcs.MODID, "npcmovingpath");
		this.setUnlocalizedName("npcmovingpath");
		this.setFull3D();
		this.maxStackSize = 1;
		this.setCreativeTab(CustomRegisters.tab);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> list, @Nonnull ITooltipFlag flagIn) {
        list.add(new TextComponentTranslation("info.item.moving.path").getFormattedText());
		for (int i = 0; i < 3; i++) {
			if (i == 1 || i == 2) {
				list.add(new TextComponentTranslation("info.item.moving.path." + i,
						new TextComponentTranslation("ai.movingpath").getFormattedText()).getFormattedText());
				continue;
			}
			list.add(new TextComponentTranslation("info.item.moving.path." + i).getFormattedText());
		}
	}

	/** Registered in EntityNPCInterface.processInteract() */
	private EntityNPCInterface getNpc(ItemStack item, World world) {
		if (world.isRemote || item.getTagCompound() == null) {
			return null;
		}
		Entity entity = world.getEntityByID(item.getTagCompound().getInteger("NPCID"));
		if (!(entity instanceof EntityNPCInterface)) {
			return null;
		}
		return (EntityNPCInterface) entity;
	}

	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.MovingPathGet || e == EnumPacketServer.MovingPathSave;
	}

	public @Nonnull ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
		ItemStack stack;
		if (hand == EnumHand.OFF_HAND) {
			return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(EnumHand.OFF_HAND));
		}
		stack = player.getHeldItem(hand);
		if (!world.isRemote) {
			if (CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.TOOL_MOUNTER)) {
				EntityNPCInterface npc = this.getNpc(stack, world);
				if (npc != null && (player.isSneaking() || npc.ais.getMovingType() == 2)) {
					NoppesUtilServer.sendOpenGui(player, EnumGuiType.MovingPath, npc);
				}
				return new ActionResult<>(EnumActionResult.SUCCESS, stack);
			}
		}
		return new ActionResult<>(EnumActionResult.PASS, stack);
	}

	public @Nonnull EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos bpos, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote && hand == EnumHand.MAIN_HAND) {
			if (CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.TOOL_MOUNTER)) {
				ItemStack stack = player.getHeldItem(hand);
				EntityNPCInterface npc = this.getNpc(stack, world);
				if (npc == null) {
					return EnumActionResult.PASS;
				}
				List<int[]> list = npc.ais.getMovingPath();
				int[] pos = list.get(list.size() - 1);
				int x = bpos.getX();
				int y = bpos.getY();
				int z = bpos.getZ();
				if (npc.ais.getMovingType() != 2) {
					npc.setHomePosAndDistance(new BlockPos(x, y, z), (int) npc.getMaximumHomeDistance());
					player.sendMessage(new TextComponentTranslation("message.pather.home", ((char) 167) + "6" + x, ((char) 167) + "6" + y, ((char) 167) + "6" + z, npc.getName()));
				} else {
					boolean added = true;
					if (!list.isEmpty()) {
						int[] p = list.get(list.size() - 1);
						added = !(p[0] == x && p[1] == y && p[2] == z);
					}
					if (added) {
						list.add(new int[] { x, y, z });
						double d3 = x - pos[0];
						double d4 = y - pos[1];
						double d5 = z - pos[2];
						double distance = MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
						player.sendMessage(new TextComponentTranslation("message.pather.add", ((char) 167) + "6" + x, ((char) 167) + "6" + y, ((char) 167) + "6" + z, npc.getName()));
						if (distance > CustomNpcs.NpcNavRange) {
							player.sendMessage(new TextComponentTranslation("message.pather.warn.add", ((char) 167) + "6" + CustomNpcs.NpcNavRange));
						}
					}
				}
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.FAIL;
	}
}
