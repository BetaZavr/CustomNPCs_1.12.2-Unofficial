package noppes.npcs.constants;

import net.minecraft.tileentity.TileEntity;
import noppes.npcs.client.util.InterfaseData;
import noppes.npcs.client.util.MetodData;
import noppes.npcs.client.util.ParameterData;

public enum EnumInterfaceData {

	IBlock(new InterfaseData(noppes.npcs.api.block.IBlock.class, "interfase.iblock",
				new MetodData(Void.class, "blockEvent", "metod.iblock.blockevent",
						new ParameterData(int.class, "type", "parameter.iblock.blockevent.0"),
						new ParameterData(int.class, "data", "parameter.iblock.blockevent.1")
				),
				new MetodData(noppes.npcs.api.IContainer.class, "getContainer", "metod.iblock.getcontainer"),
				new MetodData(String.class, "getDisplayName", "metod.iblock.getdisplayname"),
				new MetodData(String.class, "getMCBlock", "metod.iblock.getmcblock"),
				new MetodData(net.minecraft.block.state.IBlockState.class, "getMCBlockState", "metod.iblock.getmcblockstate"),
				new MetodData(TileEntity.class, "getMCTileEntity", "metod.iblock.getmctileentity"),
				new MetodData(int.class, "getMetadata", "metod.iblock.getmetadata"),
				new MetodData(String.class, "getName", "metod.iblock.getname"),
				new MetodData(noppes.npcs.api.IPos.class, "getPos", "metod.iblock.getpos"),
				new MetodData(noppes.npcs.api.entity.data.IData.class, "getStoreddata", "metod.iblock.getstoreddata"),
				new MetodData(noppes.npcs.api.entity.data.IData.class, "getTempdata", "metod.gettempdata"),
				new MetodData(noppes.npcs.api.INbt.class, "getTileEntityNBT", "metod.iblock.gettileentitynbt"),
				new MetodData(noppes.npcs.api.IWorld.class, "getWorld", "metod.iblock.getworld"),
				new MetodData(int.class, "getX", "metod.getx"),
				new MetodData(int.class, "getY", "metod.gety"),
				new MetodData(int.class, "getZ", "metod.getz"),
				new MetodData(boolean.class, "hasTileEntity", "metod.iblock.hastileentity"),
				new MetodData(Void.class, "interact", "metod.iblock.interact", new ParameterData(int.class, "side", "parameter.iblock.interact")),
				new MetodData(boolean.class, "isAir", "metod.iblock.isair"),
				new MetodData(boolean.class, "isContainer", "metod.iblock.iscontainer"),
				new MetodData(boolean.class, "isRemoved", "metod.iblock.isremoved"),
				new MetodData(Void.class, "remove", "metod.iblock.remove"),
				new MetodData(noppes.npcs.api.block.IBlock.class, "setBlock", "metod.iblock.setblock", new ParameterData(noppes.npcs.api.block.IBlock.class, "block", "parameter.iblock.setblock.0")),
				new MetodData(noppes.npcs.api.block.IBlock.class, "setBlock", "metod.iblock.setblock", new ParameterData(String.class, "name", "parameter.iblock.setblock.1")),
				new MetodData(Void.class, "setMetadata", "metod.iblock.setmetadata", new ParameterData(int.class, "i", "parameter.iblock.setmetadata")),
				new MetodData(Void.class, "setTileEntityNBT", "metod.iblock.settileentitynbt", new ParameterData(noppes.npcs.api.INbt.class, "nbt", "parameter.iblock.settileentitynbt"))
			)
	),
	IBlockFluidContainer(new InterfaseData(noppes.npcs.api.block.IBlockFluidContainer.class, "interfase.iblockfluidcontainer")),
	IBlockScripted(new InterfaseData(noppes.npcs.api.block.IBlockScripted.class, "interfase.iblockscripted")),
	IBlockScriptedDoor(new InterfaseData(noppes.npcs.api.block.IBlockScriptedDoor.class, "interfase.iblockscripteddoor")),
	ICustmBlock(new InterfaseData(noppes.npcs.api.block.ICustmBlock.class, "interfase.icustmblock")),
	ITextPlane(new InterfaseData(noppes.npcs.api.block.ITextPlane.class, "interfase.itextplane")),
	IContainer(new InterfaseData(noppes.npcs.api.IContainer.class, "interfase.icontainer")),
	IContainerCustomChest(new InterfaseData(noppes.npcs.api.IContainerCustomChest.class, "interfase.icontainercustomchest")),
	IDamageSource(new InterfaseData(noppes.npcs.api.IDamageSource.class, "interfase.idamagesource")),
	IDimension(new InterfaseData(noppes.npcs.api.IDimension.class, "interfase.idimension")),
	IEntityDamageSource(new InterfaseData(noppes.npcs.api.IEntityDamageSource.class, "interfase.ientitydamagesource")),
	INbt(new InterfaseData(noppes.npcs.api.INbt.class, "interfase.inbt")),
	IPos(new InterfaseData(noppes.npcs.api.IPos.class, "interfase.ipos")),
	IPotion(new InterfaseData(noppes.npcs.api.IPotion.class, "interfase.ipotion")),
	IRayTrace(new InterfaseData(noppes.npcs.api.IRayTrace.class, "interfase.iraytrace")),
	IScoreboard(new InterfaseData(noppes.npcs.api.IScoreboard.class, "interfase.iscoreboard")),
	IScoreboardObjective(new InterfaseData(noppes.npcs.api.IScoreboardObjective.class, "interfase.iscoreboardobjective")),
	IScoreboardScore(new InterfaseData(noppes.npcs.api.IScoreboardScore.class, "interfase.iscoreboardscore")),
	IScoreboardTeam(new InterfaseData(noppes.npcs.api.IScoreboardTeam.class, "interfase.iscoreboardteam")),
	ITimers(new InterfaseData(noppes.npcs.api.ITimers.class, "interfase.itimers")),
	IWorld(new InterfaseData(noppes.npcs.api.IWorld.class, "interfase.iworld")),
	
	IJobBard(new InterfaseData(noppes.npcs.api.entity.data.role.IJobBard.class, "interfase.ijobbard")),
	IJobBuilder(new InterfaseData(noppes.npcs.api.entity.data.role.IJobBuilder.class, "interfase.ijobbuilder")),
	IJobFarmer(new InterfaseData(noppes.npcs.api.entity.data.role.IJobFarmer.class, "interfase.ijobfarmer")),
	IJobFollower(new InterfaseData(noppes.npcs.api.entity.data.role.IJobFollower.class, "interfase.ijobfollower")),
	IJobPuppet(new InterfaseData(noppes.npcs.api.entity.data.role.IJobPuppet.class, "interfase.ijobpuppet")),
	IJobSpawner(new InterfaseData(noppes.npcs.api.entity.data.role.IJobSpawner.class, "interfase.ijobspawner")),
	IRoleDialog(new InterfaseData(noppes.npcs.api.entity.data.role.IRoleDialog.class, "interfase.iroledialog")),
	IRoleFollower(new InterfaseData(noppes.npcs.api.entity.data.role.IRoleFollower.class, "interfase.irolefollower")),
	IRoleTrader(new InterfaseData(noppes.npcs.api.entity.data.role.IRoleTrader.class, "interfase.iroletrader")),
	IRoleTransporter(new InterfaseData(noppes.npcs.api.entity.data.role.IRoleTransporter.class, "interfase.iroletransporter")),
	IAttributeSet(new InterfaseData(noppes.npcs.api.entity.data.IAttributeSet.class, "interfase.iattributeset")),
	ICustomDrop(new InterfaseData(noppes.npcs.api.entity.data.ICustomDrop.class, "interfase.icustomdrop")),
	IData(new InterfaseData(noppes.npcs.api.entity.data.IData.class, "interfase.idata")),
	IDropNbtSet(new InterfaseData(noppes.npcs.api.entity.data.IDropNbtSet.class, "interfase.idropnbtset")),
	IEnchantSet(new InterfaseData(noppes.npcs.api.entity.data.IEnchantSet.class, "interfase.ienchantset")),
	ILine(new InterfaseData(noppes.npcs.api.entity.data.ILine.class, "interfase.iline")),
	IMark(new InterfaseData(noppes.npcs.api.entity.data.IMark.class, "interfase.imark")),
	INPCAdvanced(new InterfaseData(noppes.npcs.api.entity.data.INPCAdvanced.class, "interfase.inpcadvanced")),
	INPCAi(new InterfaseData(noppes.npcs.api.entity.data.INPCAi.class, "interfase.inpcai")),
	INPCDisplay(new InterfaseData(noppes.npcs.api.entity.data.INPCDisplay.class, "interfase.inpcdisplay")),
	INPCInventory(new InterfaseData(noppes.npcs.api.entity.data.INPCInventory.class, "interfase.inpcinventory")),
	INPCJob(new InterfaseData(noppes.npcs.api.entity.data.INPCJob.class, "interfase.inpcjob")),
	INPCMelee(new InterfaseData(noppes.npcs.api.entity.data.INPCMelee.class, "interfase.inpcmelee")),
	INPCRanged(new InterfaseData(noppes.npcs.api.entity.data.INPCRanged.class, "interfase.inpcranged")),
	INPCRole(new InterfaseData(noppes.npcs.api.entity.data.INPCRole.class, "interfase.inpcrole")),
	INPCStats(new InterfaseData(noppes.npcs.api.entity.data.INPCStats.class, "interfase.inpcstats")),
	IPixelmonPlayerData(new InterfaseData(noppes.npcs.api.entity.data.IPixelmonPlayerData.class, "interfase.ipixelmonplayerdata")),
	IPlayerMail(new InterfaseData(noppes.npcs.api.entity.data.IPlayerMail.class, "interfase.iplayermail")),
	
	NpcAPI(new InterfaseData(noppes.npcs.api.NpcAPI.class, "interfase.npcapi"));
	
	//INbt(new InterfaseData(noppes.npcs.api.INbt.class, "interfase.inbt")),
	
	public InterfaseData it;
	
	EnumInterfaceData(InterfaseData interfaseData) {
		this.it = interfaseData;
	}
	
}
