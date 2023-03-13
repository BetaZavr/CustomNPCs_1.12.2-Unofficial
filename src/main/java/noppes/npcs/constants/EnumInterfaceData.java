package noppes.npcs.constants;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import noppes.npcs.api.IEntityDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.block.ITextPlane;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.item.IItemStack;
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
	IBlockFluidContainer(new InterfaseData(noppes.npcs.api.block.IBlockFluidContainer.class, noppes.npcs.api.block.IBlock.class, "interfase.iblockfluidcontainer",
			new MetodData(String.class, "getFluidName", "metod.iblockfluidcontainer.getfluidname"),
			new MetodData(float.class, "getFluidPercentage", "metod.iblockfluidcontainer.getfluidpercentage"),
			new MetodData(float.class, "getFluidValue", "metod.iblockfluidcontainer.getfluidvalue"),
			new MetodData(float.class, "getFuildDensity", "metod.iblockfluidcontainer.getfuilddensity"),
			new MetodData(float.class, "getFuildTemperature", "metod.iblockfluidcontainer.getfuildtemperature")
		)
	),
	IBlockScripted(new InterfaseData(noppes.npcs.api.block.IBlockScripted.class, noppes.npcs.api.block.IBlock.class, "interfase.iblockscripted",
			new MetodData(String.class, "executeCommand", "metod.iblockscripted.executecommand",
				new ParameterData(String.class, "command", "parameter.iblockscripted.command")
			),
			new MetodData(float.class, "getHardness", "metod.iblockscripted.gethardness"),
			new MetodData(boolean.class, "getIsLadder", "metod.iblockscripted.getisladder"),
			new MetodData(boolean.class, "getIsPassible", "metod.iblockscripted.getispassible"),
			new MetodData(int.class, "getLight", "metod.iblockscripted.getlight"),
			new MetodData(noppes.npcs.api.item.IItemStack.class, "getModel", "metod.iblockscripted.getmodel"),
			new MetodData(int.class, "getRedstonePower", "metod.iblockscripted.getredstonepower"),
			new MetodData(int.class, "getResistance", "metod.iblockscripted.getresistance"),
			new MetodData(int.class, "getRotationX", "metod.iblockscripted.getrotationx"),
			new MetodData(int.class, "getRotationY", "metod.iblockscripted.getrotationy"),
			new MetodData(int.class, "getRotationZ", "metod.iblockscripted.getrotationz"),
			new MetodData(float.class, "getScaleX", "metod.iblockscripted.getscalex"),
			new MetodData(float.class, "getScaleY", "metod.iblockscripted.getscaley"),
			new MetodData(float.class, "getScaleZ", "metod.iblockscripted.getscalez"),
			new MetodData(noppes.npcs.api.block.ITextPlane.class, "getTextPlane", "metod.iblockscripted.gettextplane"),
			new MetodData(noppes.npcs.api.block.ITextPlane.class, "getTextPlane2", "metod.iblockscripted.gettextplane"),
			new MetodData(noppes.npcs.api.block.ITextPlane.class, "getTextPlane3", "metod.iblockscripted.gettextplane"),
			new MetodData(noppes.npcs.api.block.ITextPlane.class, "getTextPlane4", "metod.iblockscripted.gettextplane"),
			new MetodData(noppes.npcs.api.block.ITextPlane.class, "getTextPlane5", "metod.iblockscripted.gettextplane"),
			new MetodData(noppes.npcs.api.block.ITextPlane.class, "getTextPlane6", "metod.iblockscripted.gettextplane"),
			new MetodData(noppes.npcs.api.ITimers.class, "getTimers", "metod.iblockscripted.gettimers"),
			new MetodData(Void.class, "setHardness", "metod.iblockscripted.sethardness",
				new ParameterData(float.class, "hardness", "parameter.iblockscripted.hardness")
			),
			new MetodData(Void.class, "setIsLadder", "metod.iblockscripted.setIsLadder",
				new ParameterData(boolean.class, "enabled", "parameter.iblockscripted.enabled")
			),
			new MetodData(Void.class, "setIsPassible", "metod.iblockscripted.setispassible",
				new ParameterData(boolean.class, "passible", "parameter.iblockscripted.passible")
			),
			new MetodData(Void.class, "setLight", "metod.iblockscripted.setlight",
				new ParameterData(int.class, "value", "parameter.iblockscripted.light")
			),
			new MetodData(Void.class, "setModel", "metod.iblockscripted.setmodel.0",
				new ParameterData(noppes.npcs.api.item.IItemStack.class, "item", "parameter.iblockscripted.itemstack")
			),
			new MetodData(Void.class, "setModel", "metod.iblockscripted.setmodel.1",
				new ParameterData(String.class, "name", "parameter.iblockscripted.itemname")
			),
			new MetodData(Void.class, "setModel", "metod.iblockscripted.setmodel.2",
				new ParameterData(String.class, "blockName", "parameter.iblockscripted.blockname"),
				new ParameterData(int.class, "meta", "parameter.iblockscripted.meta")
			),
			new MetodData(Void.class, "setModel", "metod.iblockscripted.setmodel.3",
					new ParameterData(noppes.npcs.api.block.IBlock.class, "iblock", "parameter.iblockscripted.iblock")
			),
			new MetodData(Void.class, "setRedstonePower", "metod.iblockscripted.setredstonepower",
					new ParameterData(int.class, "strength", "parameter.iblockscripted.strength")
			),
			new MetodData(Void.class, "setResistance", "metod.iblockscripted.setresistance",
				new ParameterData(float.class, "resistance", "parameter.iblockscripted.resistance")
			),
			new MetodData(Void.class, "setRotation", "metod.iblockscripted.setrotation",
				new ParameterData(int.class, "x", "parameter.iblockscripted.rotationx"),
				new ParameterData(int.class, "y", "parameter.iblockscripted.rotationy"),
				new ParameterData(int.class, "z", "parameter.iblockscripted.rotationz")
			),
			new MetodData(Void.class, "setScale", "metod.iblockscripted.setscale",
				new ParameterData(float.class, "x", "parameter.iblockscripted.scalex"),
				new ParameterData(float.class, "y", "parameter.iblockscripted.scaley"),
				new ParameterData(float.class, "z", "parameter.iblockscripted.scalez")
			)
		)
	),
	IBlockScriptedDoor(new InterfaseData(noppes.npcs.api.block.IBlockScriptedDoor.class, noppes.npcs.api.block.IBlock.class, "interfase.iblockscripteddoor",
			new MetodData(String.class, "getBlockModel", "metod.iblockscripted.getmodel"),
			new MetodData(float.class, "getHardness", "metod.iblockscripted.gethardness"),
			new MetodData(boolean.class, "getOpen", "metod.iblockscripteddoor.getopen"),
			new MetodData(float.class, "getResistance", "metod.iblockscripted.getresistance"),
			new MetodData(noppes.npcs.api.ITimers.class, "getTimers", "metod.iblockscripted.gettimers"),
			new MetodData(Void.class, "setBlockModel", "metod.iblockscripted.setmodel.1",
				new ParameterData(String.class, "name", "parameter.iblockscripted.itemname")
			),
			new MetodData(Void.class, "setHardness", "metod.iblockscripted.sethardness",
				new ParameterData(float.class, "hardness", "parameter.iblockscripted.hardness")
			),
			new MetodData(Void.class, "setOpen", "metod.iblockscripteddoor.setopen",
				new ParameterData(boolean.class, "open", "parameter.iblockscripteddoor.open")
			),
			new MetodData(Void.class, "setResistance", "metod.iblockscripted.setresistance",
				new ParameterData(float.class, "resistance", "parameter.iblockscripted.resistance")
			)
		)
	),
	
	ICustmBlock(new InterfaseData(noppes.npcs.api.block.ICustmBlock.class, "interfase.icustmblock",
			new MetodData(String.class, "getCustomName", "metod.icustmblock.getcustomname"),
			new MetodData(noppes.npcs.api.INbt.class, "getCustomNbt", "metod.icustmblock.getcustomnbt")
		)
	),
	ITextPlane(new InterfaseData(noppes.npcs.api.block.ITextPlane.class, "interfase.itextplane",
			new MetodData(float.class, "getOffsetX", "metod.itextplane.getoffsetx"),
			new MetodData(float.class, "getOffsetY", "metod.itextplane.getoffsety"),
			new MetodData(float.class, "getOffsetZ", "metod.itextplane.getoffsetz"),
			new MetodData(int.class, "getRotationX", "metod.itextplane.getrotationx"),
			new MetodData(int.class, "getRotationY", "metod.itextplane.getrotationy"),
			new MetodData(int.class, "getRotationZ", "metod.itextplane.getrotationz"),
			new MetodData(float.class, "getScale", "metod.itextplane.getscale"),
			new MetodData(String.class, "getText", "metod.itextplane.gettext"),
			new MetodData(Void.class, "setOffsetX", "metod.itextplane.setOffsetx",
				new ParameterData(float.class, "x", "parameter.itextplane.setOffsetx")
			),
			new MetodData(Void.class, "setOffsetY", "metod.itextplane.setOffsety",
				new ParameterData(float.class, "y", "parameter.itextplane.setOffsety")
			),
			new MetodData(Void.class, "setOffsetZ", "metod.itextplane.setOffsetz",
				new ParameterData(float.class, "z", "parameter.itextplane.setOffsetz")
			),
			new MetodData(Void.class, "setRotationX", "metod.itextplane.setrotationx",
				new ParameterData(int.class, "x", "parameter.itextplane.setrotationx")
			),
			new MetodData(Void.class, "setRotationY", "metod.itextplane.setrotationy",
				new ParameterData(int.class, "y", "parameter.itextplane.setrotationy")
			),
			new MetodData(Void.class, "setRotationZ", "metod.itextplane.setrotationz",
				new ParameterData(int.class, "z", "parameter.itextplane.setrotationz")
			),
			new MetodData(Void.class, "setScale", "metod.itextplane.setscale",
				new ParameterData(int.class, "scale", "parameter.itextplane.setscale")
			),
			new MetodData(Void.class, "setText", "metod.itextplane.settext",
				new ParameterData(int.class, "text", "parameter.itextplane.settext")
			)
		)
	),
	IContainer(new InterfaseData(noppes.npcs.api.IContainer.class, "interfase.icontainer",
			new MetodData(int.class, "count", "metod.icontainer.count",
				new ParameterData(noppes.npcs.api.item.IItemStack.class, "item", "parameter.icontainer.item.0"),
				new ParameterData(boolean.class, "ignoredamage", "parameter.icontainer.ignoredamage"),
				new ParameterData(boolean.class, "ignorenbt", "parameter.icontainer.ignorenbt")
			),
			new MetodData(noppes.npcs.api.item.IItemStack[].class, "getItems", "metod.icontainer.getitems"),
			new MetodData(Container.class, "getMCContainer", "metod.icontainer.getmccontainer"),
			new MetodData(IInventory.class, "getMCInventory", "metod.icontainer.getmcinventory"),
			new MetodData(int.class, "getSize", "metod.icontainer.getsize"),
			new MetodData(noppes.npcs.api.item.IItemStack.class, "getSlot", "metod.icontainer.getslot",
				new ParameterData(int.class, "slot", "parameter.icontainer.slot")
			),
			new MetodData(Void.class, "setSlot", "metod.icontainer.setslot",
				new ParameterData(int.class, "slot", "parameter.icontainer.slot"),
				new ParameterData(noppes.npcs.api.item.IItemStack.class, "item", "parameter.icontainer.item.1")
			)
		)
	),
	IContainerCustomChest(new InterfaseData(noppes.npcs.api.IContainerCustomChest.class, noppes.npcs.api.IContainer.class, "interfase.icontainercustomchest",
			new MetodData(String.class, "getName", "metod.icontainercustomchest.getname"),
			new MetodData(Void.class, "setName", "metod.icontainercustomchest.setname",
				new ParameterData(String.class, "name", "parameter.icontainercustomchest.name")
			)
		)
	),
	IDamageSource(new InterfaseData(noppes.npcs.api.IDamageSource.class, "interfase.idamagesource",
			new MetodData(noppes.npcs.api.entity.IEntity.class, "getImmediateSource", "metod.idamagesource.getimmediatesource"),
			new MetodData(DamageSource.class, "getMCDamageSource", "metod.idamagesource.getmcdamagesource"),
			new MetodData(noppes.npcs.api.entity.IEntity.class, "getTrueSource", "metod.idamagesource.gettruesource"),
			new MetodData(String.class, "getType", "metod.idamagesource.gettype"),
			new MetodData(boolean.class, "isProjectile", "metod.idamagesource.isprojectile"),
			new MetodData(boolean.class, "isUnblockable", "metod.idamagesource.isunblockable")
		)
	),
	IDimension(new InterfaseData(noppes.npcs.api.IDimension.class, "interfase.idimension",
			new MetodData(int.class, "getId", "metod.idimension.getid"),
			new MetodData(String.class, "getName", "metod.idimension.getname"),
			new MetodData(String.class, "getSuffix", "metod.idimension.getsuffix")
		)
	),
	IEntityDamageSource(new InterfaseData(noppes.npcs.api.IEntityDamageSource.class, "interfase.ientitydamagesource",
			new MetodData(String.class, "getType", "metod.ientitydamagesource.gettype"),
			new MetodData(Void.class, "setType", "metod.ientitydamagesource.settype",
				new ParameterData(String.class, "damageType", "parameter.ientitydamagesource.damageType")
			),
			new MetodData(noppes.npcs.api.entity.IEntity.class, "getITrueSource", "metod.ientitydamagesource.getitruesource"),
			new MetodData(Void.class, "setTrueSource", "metod.ientitydamagesource.settruesource",
				new ParameterData(noppes.npcs.api.entity.IEntity.class, "entity", "parameter.ientitydamagesource.entity.0")
			),
			new MetodData(noppes.npcs.api.entity.IEntity.class, "getIImmediateSource", "metod.ientitydamagesource.getiimmediatesource"),
			new MetodData(Void.class, "setImmediateSource", "metod.ientitydamagesource.setimmediatesource",
				new ParameterData(noppes.npcs.api.entity.IEntity.class, "entity", "parameter.ientitydamagesource.entity.1")
			),
			new MetodData(String.class, "getDeadMessage", "metod.ientitydamagesource.getdeadmessage"),
			new MetodData(Void.class, "setDeadMessage", "metod.ientitydamagesource.setdeadmessage",
				new ParameterData(String.class, "message", "parameter.ientitydamagesource.message")
			),
			new MetodData(boolean.class, "getIsThornsDamage", "metod.ientitydamagesource.getisthornsdamage"),
			new MetodData(IEntityDamageSource.class, "setIsThornsDamage", "metod.ientitydamagesource.setisthornsdamage")
		)
	),
	INbt(new InterfaseData(noppes.npcs.api.INbt.class, "interfase.inbt",
			new MetodData(Void.class, "clear", "metod.inbt.clear"),
			new MetodData(boolean.class, "getBoolean", "metod.inbt.getboolean",
				new ParameterData(String.class, "key", "parameter.inbt.key.boolean")
			),
			new MetodData(byte.class, "getByte", "metod.inbt.getbyte",
				new ParameterData(String.class, "key", "parameter.inbt.key.byte")
			),
			new MetodData(byte[].class, "getByteArray", "metod.inbt.getbytearray",
				new ParameterData(String.class, "key", "parameter.inbt.key.bytearr")
			),
			new MetodData(noppes.npcs.api.INbt.class, "getCompound", "metod.inbt.getcompound",
				new ParameterData(String.class, "key", "parameter.inbt.key.compound")
			),
			new MetodData(double.class, "getDouble", "metod.inbt.getdouble",
				new ParameterData(String.class, "key", "parameter.inbt.key.double")
			),
			new MetodData(float.class, "getFloat", "metod.inbt.getfloat",
				new ParameterData(String.class, "key", "parameter.inbt.key.float")
			),
			new MetodData(String.class, "getInteger", "metod.inbt.getinteger",
				new ParameterData(String.class, "key", "parameter.inbt.key.int")
			),
			new MetodData(int[].class, "getIntegerArray", "metod.inbt.getintarr",
				new ParameterData(String.class, "key", "parameter.inbt.key.intarr")
			),
			new MetodData(String[].class, "getKeys", "metod.inbt.getkeys"),
			new MetodData(Object[].class, "getList", "metod.inbt.getlist",
				new ParameterData(String.class, "key", "parameter.inbt.key.list"),
				new ParameterData(int.class, "type", "parameter.inbt.type.list")
			),
			new MetodData(int.class, "getListType", "metod.inbt.getlisttype"),
			new MetodData(long.class, "getLong", "metod.inbt.getlong",
				new ParameterData(String.class, "key", "parameter.inbt.key.long")
			),
			new MetodData(NBTTagCompound.class, "getMCNBT", "metod.inbt.getmcnbt"),
			new MetodData(short.class, "getShort", "metod.inbt.getshort",
				new ParameterData(String.class, "key", "parameter.inbt.key.short")
			),
			new MetodData(String.class, "getString", "metod.inbt.getstring",
				new ParameterData(String.class, "key", "parameter.inbt.key.string")
			),
			new MetodData(int.class, "getType", "metod.inbt.gettype",
				new ParameterData(String.class, "key", "parameter.inbt.key.string")
			),
			new MetodData(boolean.class, "has", "metod.inbt.has",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt")
			),
			new MetodData(void.class, "isEqual", "metod.inbt.isequal",
				new ParameterData(noppes.npcs.api.INbt.class, "nbt", "parameter.inbt.key.nbtequal")
			),
			new MetodData(void.class, "merge", "metod.inbt.merge",
				new ParameterData(noppes.npcs.api.INbt.class, "nbt", "parameter.inbt.key.nbtmerge")
			),
			new MetodData(void.class, "remove", "metod.inbt.remove",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt")
			),
			new MetodData(void.class, "setBoolean", "metod.inbt.setboolean",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(boolean.class, "value", "parameter.inbt.key.boolean")
			),
			new MetodData(void.class, "setByte", "metod.inbt.setbyte",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(boolean.class, "value", "parameter.inbt.key.byte")
			),
			new MetodData(void.class, "setByteArray", "metod.inbt.setbytearray",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(byte[].class, "value", "parameter.inbt.key.bytearr")
			),
			new MetodData(void.class, "setCompound", "metod.inbt.setcompound",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(noppes.npcs.api.INbt.class, "value", "parameter.inbt.key.compound")
			),
			new MetodData(void.class, "setDouble", "metod.inbt.setdouble",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(double.class, "value", "parameter.inbt.key.double")
			),
			new MetodData(void.class, "setFloat", "metod.inbt.setfloat",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(float.class, "value", "parameter.inbt.key.float")
			),
			new MetodData(void.class, "setInteger", "metod.inbt.setinteger",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(int.class, "value", "parameter.inbt.key.int")
			),
			new MetodData(void.class, "setIntegerArray", "metod.inbt.setintegerarray",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(int.class, "value", "parameter.inbt.key.intarr")
			),
			new MetodData(void.class, "setList", "metod.inbt.setlist",
				new ParameterData(String.class, "key", "parameter.inbt.key.list"),
				new ParameterData(Object[].class, "value", "parameter.inbt.list.objarr")
			),
			new MetodData(void.class, "setLong", "metod.inbt.setlong",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(long.class, "value", "parameter.inbt.key.long")
			),
			new MetodData(void.class, "setShort", "metod.inbt.setshort",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(short.class, "value", "parameter.inbt.key.short")
			),
			new MetodData(void.class, "setString", "metod.inbt.gettype",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(String.class, "value", "parameter.inbt.key.string")
			),
			new MetodData(String.class, "toJsonString", "metod.inbt.tojsonstring")
		)
	),
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
	IJobFollower(new InterfaseData(noppes.npcs.api.entity.data.role.IJobFollower.class, noppes.npcs.api.entity.data.INPCJob.class, "interfase.ijobfollower")),
	IJobPuppet(new InterfaseData(noppes.npcs.api.entity.data.role.IJobPuppet.class, noppes.npcs.api.entity.data.INPCJob.class, "interfase.ijobpuppet")),
	IJobSpawner(new InterfaseData(noppes.npcs.api.entity.data.role.IJobSpawner.class, "interfase.ijobspawner")),
	IRoleDialog(new InterfaseData(noppes.npcs.api.entity.data.role.IRoleDialog.class, "interfase.iroledialog")),
	IRoleFollower(new InterfaseData(noppes.npcs.api.entity.data.role.IRoleFollower.class, noppes.npcs.api.entity.data.INPCRole.class, "interfase.irolefollower")),
	IRoleTrader(new InterfaseData(noppes.npcs.api.entity.data.role.IRoleTrader.class, noppes.npcs.api.entity.data.INPCRole.class, "interfase.iroletrader")),
	IRoleTransporter(new InterfaseData(noppes.npcs.api.entity.data.role.IRoleTransporter.class, noppes.npcs.api.entity.data.INPCRole.class, "interfase.iroletransporter")),
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
	IAnimal(new InterfaseData(noppes.npcs.api.entity.IAnimal.class, noppes.npcs.api.entity.IEntityLiving.class, "interfase.ianimal")),
	IArrow(new InterfaseData(noppes.npcs.api.entity.IArrow.class, noppes.npcs.api.entity.IEntity.class, "interfase.iarrow")),
	ICustomNpc(new InterfaseData(noppes.npcs.api.entity.ICustomNpc.class, noppes.npcs.api.entity.IEntityLiving.class, "interfase.icustomnpc")),
	IEntity(new InterfaseData(noppes.npcs.api.entity.IEntity.class, "interfase.ientity"
		)
	),
	IEntityItem(new InterfaseData(noppes.npcs.api.entity.IEntityItem.class, noppes.npcs.api.entity.IEntity.class, "interfase.ientityitem")),
	IEntityLiving(new InterfaseData(noppes.npcs.api.entity.IEntityLiving.class, noppes.npcs.api.entity.IEntityLivingBase.class, "interfase.ientityliving"
		)
	),
	IEntityLivingBase(new InterfaseData(noppes.npcs.api.entity.IEntityLivingBase.class, noppes.npcs.api.entity.IEntity.class, "interfase.ientitylivingbase"
		)
	),
	IMonster(new InterfaseData(noppes.npcs.api.entity.IMonster.class, noppes.npcs.api.entity.IEntityLiving.class, "interfase.imonster")),
	IPixelmon(new InterfaseData(noppes.npcs.api.entity.IPixelmon.class, noppes.npcs.api.entity.IAnimal.class, "interfase.ipixelmon")),
	IPlayer(new InterfaseData(noppes.npcs.api.entity.IPlayer.class, noppes.npcs.api.entity.IEntityLivingBase.class, "interfase.iplayer")),
	IProjectile(new InterfaseData(noppes.npcs.api.entity.IProjectile.class, noppes.npcs.api.entity.IThrowable.class, "interfase.iprojectile")),
	IThrowable(new InterfaseData(noppes.npcs.api.entity.IThrowable.class, noppes.npcs.api.entity.IEntity.class, "interfase.ithrowable")),
	IVillager(new InterfaseData(noppes.npcs.api.entity.IVillager.class, noppes.npcs.api.entity.IEntityLiving.class, "interfase.ivillager")),
	IButton(new InterfaseData(noppes.npcs.api.gui.IButton.class, noppes.npcs.api.gui.ICustomGuiComponent.class, "interfase.ibutton")),
	ICustomGui(new InterfaseData(noppes.npcs.api.gui.ICustomGui.class, "interfase.icustomgui")),
	ICustomGuiComponent(new InterfaseData(noppes.npcs.api.gui.ICustomGuiComponent.class, "interfase.icustomguicomponent")),
	IGuiTimer(new InterfaseData(noppes.npcs.api.gui.IGuiTimer.class, "interfase.iguitimer")),
	IItemSlot(new InterfaseData(noppes.npcs.api.gui.IItemSlot.class, noppes.npcs.api.gui.ICustomGuiComponent.class, "interfase.iitemslot")),
	ILabel(new InterfaseData(noppes.npcs.api.gui.ILabel.class, noppes.npcs.api.gui.ICustomGuiComponent.class, "interfase.ilabel")),
	IOverlayHUD(new InterfaseData(noppes.npcs.api.gui.IOverlayHUD.class, "interfase.ioverlayhud")),
	IScroll(new InterfaseData(noppes.npcs.api.gui.IScroll.class, noppes.npcs.api.gui.ICustomGuiComponent.class, "interfase.iscroll")),
	ITextField(new InterfaseData(noppes.npcs.api.gui.ITextField.class, noppes.npcs.api.gui.ICustomGuiComponent.class, "interfase.itextfield")),
	ITexturedButton(new InterfaseData(noppes.npcs.api.gui.ITexturedButton.class, noppes.npcs.api.gui.IButton.class, "interfase.itexturedbutton")),
	ITexturedRect(new InterfaseData(noppes.npcs.api.gui.ITexturedRect.class, "interfase.itexturedrect")),
	ICloneHandler(new InterfaseData(noppes.npcs.api.handler.ICloneHandler.class, "interfase.iclonehandler")),
	IDataObject(new InterfaseData(noppes.npcs.api.handler.IDataObject.class, "interfase.idataobject")),
	IDialogHandler(new InterfaseData(noppes.npcs.api.handler.IDialogHandler.class, "interfase.idialoghandler")),
	IFactionHandler(new InterfaseData(noppes.npcs.api.handler.IFactionHandler.class, "interfase.ifactionhandler")),
	IQuestHandler(new InterfaseData(noppes.npcs.api.handler.IQuestHandler.class, "interfase.iquesthandler")),
	IRecipeHandler(new InterfaseData(noppes.npcs.api.handler.IRecipeHandler.class, "interfase.irecipehandler")),
	IAvailability(new InterfaseData(noppes.npcs.api.handler.data.IAvailability.class, "interfase.iavailability")),
	IDataElement(new InterfaseData(noppes.npcs.api.handler.data.IDataElement.class, "interfase.idatablement")),
	IDialog(new InterfaseData(noppes.npcs.api.handler.data.IDialog.class, "interfase.idialog")),
	IDialogCategory(new InterfaseData(noppes.npcs.api.handler.data.IDialogCategory.class, "interfase.idialogcategory")),
	IDialogOption(new InterfaseData(noppes.npcs.api.handler.data.IDialogOption.class, "interfase.idialogoption")),
	IFaction(new InterfaseData(noppes.npcs.api.handler.data.IFaction.class, "interfase.ifaction")),
	INpcRecipe(new InterfaseData(noppes.npcs.api.handler.data.INpcRecipe.class, "interfase.inpcrecipe")),
	IQuest(new InterfaseData(noppes.npcs.api.handler.data.IQuest.class, "interfase.iquest")),
	IQuestCategory(new InterfaseData(noppes.npcs.api.handler.data.IQuestCategory.class, "interfase.iquestcategory")),
	IQuestObjective(new InterfaseData(noppes.npcs.api.handler.data.IQuestObjective.class, "interfase.iquestobjective")),
	IScriptData(new InterfaseData(noppes.npcs.api.handler.data.IScriptData.class, "interfase.iscriptdata")),
	ICustomItem(new InterfaseData(noppes.npcs.api.item.ICustomItem.class, "interfase.icustomitem")),
	IItemArmor(new InterfaseData(noppes.npcs.api.item.IItemArmor.class, noppes.npcs.api.item.IItemStack.class, "interfase.iitemarmor")),
	IItemBlock(new InterfaseData(noppes.npcs.api.item.IItemBlock.class, noppes.npcs.api.item.IItemStack.class, "interfase.iitemblock")),
	IItemBook(new InterfaseData(noppes.npcs.api.item.IItemBook.class, noppes.npcs.api.item.IItemStack.class, "interfase.iitembook")),
	IItemScripted(new InterfaseData(noppes.npcs.api.item.IItemScripted.class, noppes.npcs.api.item.IItemStack.class, "interfase.iitemscripted")),
	IItemStack(new InterfaseData(noppes.npcs.api.item.IItemStack.class, "interfase.iitemstack")),
	NpcAPI(new InterfaseData(noppes.npcs.api.NpcAPI.class, "interfase.npcapi"));
	
	public InterfaseData it;
	
	EnumInterfaceData(InterfaseData interfaseData) { this.it = interfaseData; }
	
}
