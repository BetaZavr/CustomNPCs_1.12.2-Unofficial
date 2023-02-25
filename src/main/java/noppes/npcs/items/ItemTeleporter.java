package noppes.npcs.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.IPermission;

public class ItemTeleporter
extends Item
implements IPermission {
	
	public ItemTeleporter() {
		this.setRegistryName(CustomNpcs.MODID, "npcteleporter");
		this.setUnlocalizedName("npcteleporter");
		this.setFull3D();
		this.maxStackSize = 1;
		this.setCreativeTab((CreativeTabs) CustomItems.tab);
	}

	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.DimensionsGet || e == EnumPacketServer.DimensionTeleport;
	}

	public boolean onEntitySwing(EntityLivingBase par3EntityPlayer, ItemStack stack) {
		if (par3EntityPlayer.world.isRemote) {
			return false;
		}
		float f = 1.0f;
		float f2 = par3EntityPlayer.prevRotationPitch
				+ (par3EntityPlayer.rotationPitch - par3EntityPlayer.prevRotationPitch) * f;
		float f3 = par3EntityPlayer.prevRotationYaw
				+ (par3EntityPlayer.rotationYaw - par3EntityPlayer.prevRotationYaw) * f;
		double d0 = par3EntityPlayer.prevPosX + (par3EntityPlayer.posX - par3EntityPlayer.prevPosX) * f;
		double d2 = par3EntityPlayer.prevPosY + (par3EntityPlayer.posY - par3EntityPlayer.prevPosY) * f + 1.62;
		double d3 = par3EntityPlayer.prevPosZ + (par3EntityPlayer.posZ - par3EntityPlayer.prevPosZ) * f;
		Vec3d vec3 = new Vec3d(d0, d2, d3);
		float f4 = MathHelper.cos(-f3 * 0.017453292f - 3.1415927f);
		float f5 = MathHelper.sin(-f3 * 0.017453292f - 3.1415927f);
		float f6 = -MathHelper.cos(-f2 * 0.017453292f);
		float f7 = MathHelper.sin(-f2 * 0.017453292f);
		float f8 = f5 * f6;
		float f9 = f4 * f6;
		double d4 = 80.0;
		Vec3d vec4 = vec3.addVector(f8 * d4, f7 * d4, f9 * d4);
		RayTraceResult movingobjectposition = par3EntityPlayer.world.rayTraceBlocks(vec3, vec4, true);
		if (movingobjectposition == null) {
			return false;
		}
		Vec3d vec5 = par3EntityPlayer.getLook(f);
		boolean flag = false;
		float f10 = 1.0f;
		List<Entity> list = par3EntityPlayer.world.getEntitiesWithinAABBExcludingEntity(par3EntityPlayer,
				par3EntityPlayer.getEntityBoundingBox().grow(vec5.x * d4, vec5.y * d4, vec5.z * d4).grow(f10, f10,
						f10));
		for (int i = 0; i < list.size(); ++i) {
			Entity entity = list.get(i);
			if (entity.canBeCollidedWith()) {
				float f11 = entity.getCollisionBorderSize();
				AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().grow(f11, f11, f11);
				if (axisalignedbb.contains(vec3)) {
					flag = true;
				}
			}
		}
		if (flag) {
			return false;
		}
		if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos pos;
			for (pos = movingobjectposition.getBlockPos(); par3EntityPlayer.world.getBlockState(pos)
					.getBlock() != Blocks.AIR; pos = pos.up()) {
			}
			par3EntityPlayer.setPositionAndUpdate((pos.getX() + 0.5f), (pos.getY() + 1.0f), (pos.getZ() + 0.5f));
		}
		return true;
	}

	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		if (!world.isRemote) {
			return (ActionResult<ItemStack>) new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
		}
		CustomNpcs.proxy.openGui((EntityNPCInterface) null, EnumGuiType.NpcDimensions);
		return (ActionResult<ItemStack>) new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
	}

	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
		if (list==null) { return; }
		list.add(new TextComponentTranslation("info.item.teleporter").getFormattedText());
		list.add(new TextComponentTranslation("info.item.teleporter.0").getFormattedText());
	}
	
}
