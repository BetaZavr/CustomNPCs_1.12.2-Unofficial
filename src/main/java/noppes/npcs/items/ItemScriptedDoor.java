package noppes.npcs.items;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;

public class ItemScriptedDoor extends ItemDoor implements IPermission {

	public ItemScriptedDoor(Block block) {
		super(block);
		this.setRegistryName(CustomNpcs.MODID, "npcscripteddoortool");
		this.setUnlocalizedName("npcscripteddoortool");
		this.setFull3D();
		this.maxStackSize = 1;
		this.setCreativeTab((CreativeTabs) CustomRegisters.tab);
	}

	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.ScriptDoorDataSave;
	}

	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing side, float hitX, float hitY, float hitZ) {
		EnumActionResult res = super.onItemUse(playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);
		if (res == EnumActionResult.SUCCESS && !worldIn.isRemote) {
			BlockPos newPos = pos.up();
			NoppesUtilServer.sendOpenGui(playerIn, EnumGuiType.ScriptDoor, null, newPos.getX(), newPos.getY(),
					newPos.getZ());
			return EnumActionResult.SUCCESS;
		}
		return res;
	}

	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase playerIn) {
		return stack;
	}
}
