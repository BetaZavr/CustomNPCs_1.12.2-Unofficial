package noppes.npcs.constants;

import java.io.File;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.IContainerCustomChest;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IDimension;
import noppes.npcs.api.IEntityDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IRayTrace;
import noppes.npcs.api.IScoreboard;
import noppes.npcs.api.IScoreboardObjective;
import noppes.npcs.api.IScoreboardScore;
import noppes.npcs.api.IScoreboardTeam;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.block.IBlockFluidContainer;
import noppes.npcs.api.block.IBlockScripted;
import noppes.npcs.api.block.IBlockScriptedDoor;
import noppes.npcs.api.block.ITextPlane;
import noppes.npcs.api.entity.IAnimal;
import noppes.npcs.api.entity.IArrow;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityItem;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IMonster;
import noppes.npcs.api.entity.IPixelmon;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.api.entity.IThrowable;
import noppes.npcs.api.entity.IVillager;
import noppes.npcs.api.entity.data.IAttributeSet;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.entity.data.IDropNbtSet;
import noppes.npcs.api.entity.data.IEnchantSet;
import noppes.npcs.api.entity.data.ILine;
import noppes.npcs.api.entity.data.IMark;
import noppes.npcs.api.entity.data.INPCAdvanced;
import noppes.npcs.api.entity.data.INPCAi;
import noppes.npcs.api.entity.data.INPCDisplay;
import noppes.npcs.api.entity.data.INPCInventory;
import noppes.npcs.api.entity.data.INPCJob;
import noppes.npcs.api.entity.data.INPCMelee;
import noppes.npcs.api.entity.data.INPCRanged;
import noppes.npcs.api.entity.data.INPCRole;
import noppes.npcs.api.entity.data.INPCStats;
import noppes.npcs.api.entity.data.IPixelmonPlayerData;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.entity.data.role.IJobBard;
import noppes.npcs.api.entity.data.role.IJobBuilder;
import noppes.npcs.api.entity.data.role.IJobFarmer;
import noppes.npcs.api.entity.data.role.IJobFollower;
import noppes.npcs.api.entity.data.role.IJobPuppet;
import noppes.npcs.api.entity.data.role.IJobPuppet.IJobPuppetPart;
import noppes.npcs.api.entity.data.role.IJobSpawner;
import noppes.npcs.api.entity.data.role.IRoleDialog;
import noppes.npcs.api.entity.data.role.IRoleFollower;
import noppes.npcs.api.entity.data.role.IRoleTrader;
import noppes.npcs.api.entity.data.role.IRoleTransporter;
import noppes.npcs.api.gui.IButton;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.IGuiTimer;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.gui.ILabel;
import noppes.npcs.api.gui.IOverlayHUD;
import noppes.npcs.api.gui.IScroll;
import noppes.npcs.api.gui.ITextField;
import noppes.npcs.api.gui.ITexturedButton;
import noppes.npcs.api.gui.ITexturedRect;
import noppes.npcs.api.handler.ICloneHandler;
import noppes.npcs.api.handler.IDataObject;
import noppes.npcs.api.handler.IDialogHandler;
import noppes.npcs.api.handler.IDimensionHandler;
import noppes.npcs.api.handler.IFactionHandler;
import noppes.npcs.api.handler.IQuestHandler;
import noppes.npcs.api.handler.IRecipeHandler;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.api.handler.data.IDataElement;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IDialogCategory;
import noppes.npcs.api.handler.data.IDialogOption;
import noppes.npcs.api.handler.data.IFaction;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestCategory;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.handler.data.IScriptData;
import noppes.npcs.api.handler.data.IWorldInfo;
import noppes.npcs.api.item.IItemArmor;
import noppes.npcs.api.item.IItemBlock;
import noppes.npcs.api.item.IItemBook;
import noppes.npcs.api.item.IItemScripted;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.AnimalWrapper;
import noppes.npcs.api.wrapper.ArrowWrapper;
import noppes.npcs.api.wrapper.BlockFluidContainerWrapper;
import noppes.npcs.api.wrapper.BlockPosWrapper;
import noppes.npcs.api.wrapper.BlockScriptedDoorWrapper;
import noppes.npcs.api.wrapper.BlockScriptedWrapper;
import noppes.npcs.api.wrapper.BlockWrapper;
import noppes.npcs.api.wrapper.ContainerCustomChestWrapper;
import noppes.npcs.api.wrapper.ContainerWrapper;
import noppes.npcs.api.wrapper.DamageSourceWrapper;
import noppes.npcs.api.wrapper.DataObject;
import noppes.npcs.api.wrapper.DimensionWrapper;
import noppes.npcs.api.wrapper.EntityItemWrapper;
import noppes.npcs.api.wrapper.EntityLivingBaseWrapper;
import noppes.npcs.api.wrapper.EntityLivingWrapper;
import noppes.npcs.api.wrapper.EntityWrapper;
import noppes.npcs.api.wrapper.ItemArmorWrapper;
import noppes.npcs.api.wrapper.ItemBlockWrapper;
import noppes.npcs.api.wrapper.ItemBookWrapper;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.api.wrapper.MonsterWrapper;
import noppes.npcs.api.wrapper.NBTWrapper;
import noppes.npcs.api.wrapper.NPCWrapper;
import noppes.npcs.api.wrapper.NpcEntityDamageSource;
import noppes.npcs.api.wrapper.PixelmonWrapper;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.api.wrapper.ProjectileWrapper;
import noppes.npcs.api.wrapper.RayTraceWrapper;
import noppes.npcs.api.wrapper.ScoreboardObjectiveWrapper;
import noppes.npcs.api.wrapper.ScoreboardScoreWrapper;
import noppes.npcs.api.wrapper.ScoreboardTeamWrapper;
import noppes.npcs.api.wrapper.ScoreboardWrapper;
import noppes.npcs.api.wrapper.ThrowableWrapper;
import noppes.npcs.api.wrapper.VillagerWrapper;
import noppes.npcs.api.wrapper.WorldWrapper;
import noppes.npcs.api.wrapper.WrapperNpcAPI;
import noppes.npcs.api.wrapper.data.DataElement;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiComponentWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiItemSlotWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiLabelWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiScrollWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTextFieldWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTexturedRectWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTimerWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.blocks.CustomBlock;
import noppes.npcs.blocks.CustomLiquid;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.client.util.InterfaseData;
import noppes.npcs.client.util.MetodData;
import noppes.npcs.client.util.ParameterData;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.PlayerOverlayHUD;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.dimensions.CustomWorldInfo;
import noppes.npcs.dimensions.DimensionHandler;
import noppes.npcs.entity.data.DataAI;
import noppes.npcs.entity.data.DataAdvanced;
import noppes.npcs.entity.data.DataDisplay;
import noppes.npcs.entity.data.DataInventory;
import noppes.npcs.entity.data.DataMelee;
import noppes.npcs.entity.data.DataRanged;
import noppes.npcs.entity.data.DataStats;
import noppes.npcs.entity.data.DataTimers;
import noppes.npcs.items.CustomArmor;
import noppes.npcs.items.CustomBow;
import noppes.npcs.items.CustomFishingRod;
import noppes.npcs.items.CustomFood;
import noppes.npcs.items.CustomItem;
import noppes.npcs.items.CustomShield;
import noppes.npcs.items.CustomTool;
import noppes.npcs.items.CustomWeapon;
import noppes.npcs.items.crafting.NpcShapedRecipes;
import noppes.npcs.items.crafting.NpcShapelessRecipes;
import noppes.npcs.potions.CustomPotion;
import noppes.npcs.quests.QuestObjective;
import noppes.npcs.roles.JobBard;
import noppes.npcs.roles.JobBuilder;
import noppes.npcs.roles.JobChunkLoader;
import noppes.npcs.roles.JobConversation;
import noppes.npcs.roles.JobFarmer;
import noppes.npcs.roles.JobFollower;
import noppes.npcs.roles.JobGuard;
import noppes.npcs.roles.JobHealer;
import noppes.npcs.roles.JobInterface;
import noppes.npcs.roles.JobItemGiver;
import noppes.npcs.roles.JobPuppet;
import noppes.npcs.roles.JobSpawner;
import noppes.npcs.roles.RoleBank;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleDialog;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.roles.RolePostman;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.roles.RoleTransporter;
import noppes.npcs.util.ScriptData;

public enum EnumInterfaceData {

	IBlock(new InterfaseData(IBlock.class, null,
			new Class<?>[] { BlockWrapper.class },
			"interfase.iblock",
			new MetodData(void.class, "blockEvent", "method.iblock.blockevent",
				new ParameterData(int.class, "type", "parameter.iblock.blockevent.0"),
				new ParameterData(int.class, "data", "parameter.iblock.blockevent.1")
			),
			new MetodData(IContainer.class, "getContainer", "method.iblock.getcontainer"),
			new MetodData(String.class, "getDisplayName", "method.iblock.getdisplayname"),
			new MetodData(String.class, "getMCBlock", "method.iblock.getmcblock"),
			new MetodData(net.minecraft.block.state.IBlockState.class, "getMCBlockState", "method.iblock.getmcblockstate"),
			new MetodData(TileEntity.class, "getMCTileEntity", "method.iblock.getmctileentity"),
			new MetodData(int.class, "getMetadata", "method.iblock.getmetadata"),
			new MetodData(String.class, "getName", "method.iblock.getname"),
			new MetodData(IPos.class, "getPos", "method.getpos"),
			new MetodData(IData.class, "getStoreddata", "method.iblock.getstoreddata"),
			new MetodData(IData.class, "getTempdata", "method.gettempdata"),
			new MetodData(INbt.class, "getTileEntityNBT", "method.iblock.gettileentitynbt"),
			new MetodData(IWorld.class, "getWorld", "method.iblock.getworld"),
			new MetodData(int.class, "getX", "method.getx"),
			new MetodData(int.class, "getY", "method.gety"),
			new MetodData(int.class, "getZ", "method.getz"),
			new MetodData(boolean.class, "hasTileEntity", "method.iblock.hastileentity"),
			new MetodData(void.class, "interact", "method.iblock.interact", new ParameterData(int.class, "side", "parameter.iblock.interact")),
			new MetodData(boolean.class, "isAir", "method.iblock.isair"),
			new MetodData(boolean.class, "isContainer", "method.iblock.iscontainer"),
			new MetodData(boolean.class, "isRemoved", "method.iblock.isremoved"),
			new MetodData(void.class, "remove", "method.iblock.remove"),
			new MetodData(IBlock.class, "setBlock", "method.iblock.setblock", new ParameterData(IBlock.class, "block", "parameter.iblock.setblock.0")),
			new MetodData(IBlock.class, "setBlock", "method.iblock.setblock", new ParameterData(String.class, "name", "parameter.iblock.setblock.1")),
			new MetodData(void.class, "setMetadata", "method.iblock.setmetadata", new ParameterData(int.class, "i", "parameter.block.metadata")),
			new MetodData(void.class, "setTileEntityNBT", "method.iblock.settileentitynbt", new ParameterData(INbt.class, "nbt", "parameter.iblock.settileentitynbt"))
		)
	),
	IBlockFluidContainer(new InterfaseData(IBlockFluidContainer.class, IBlock.class,
			new Class<?>[] { BlockFluidContainerWrapper.class },
			"interfase.iblockfluidcontainer",
			new MetodData(String.class, "getFluidName", "method.iblockfluidcontainer.getfluidname"),
			new MetodData(float.class, "getFluidPercentage", "method.iblockfluidcontainer.getfluidpercentage"),
			new MetodData(float.class, "getFluidValue", "method.iblockfluidcontainer.getfluidvalue"),
			new MetodData(float.class, "getFuildDensity", "method.iblockfluidcontainer.getfuilddensity"),
			new MetodData(float.class, "getFuildTemperature", "method.iblockfluidcontainer.getfuildtemperature")
		)
	),
	IBlockScripted(new InterfaseData(IBlockScripted.class, IBlock.class,
			new Class<?>[] { BlockScriptedWrapper.class },
			"interfase.iblockscripted",
			new MetodData(String.class, "executeCommand", "method.executecommand",
				new ParameterData(String.class, "command", "parameter.command")
			),
			new MetodData(float.class, "getHardness", "method.iblockscripted.gethardness"),
			new MetodData(boolean.class, "getIsLadder", "method.iblockscripted.getisladder"),
			new MetodData(boolean.class, "getIsPassible", "method.iblockscripted.getispassible"),
			new MetodData(int.class, "getLight", "method.iblockscripted.getlight"),
			new MetodData(IItemStack.class, "getModel", "method.iblockscripted.getmodel"),
			new MetodData(int.class, "getRedstonePower", "method.iblockscripted.getredstonepower"),
			new MetodData(int.class, "getResistance", "method.iblockscripted.getresistance"),
			new MetodData(int.class, "getRotationX", "method.getrotx"),
			new MetodData(int.class, "getRotationY", "method.getroty"),
			new MetodData(int.class, "getRotationZ", "method.getrotz"),
			new MetodData(float.class, "getScaleX", "method.iblockscripted.getscalex"),
			new MetodData(float.class, "getScaleY", "method.iblockscripted.getscaley"),
			new MetodData(float.class, "getScaleZ", "method.iblockscripted.getscalez"),
			new MetodData(ITextPlane.class, "getTextPlane", "method.iblockscripted.gettextplane"),
			new MetodData(ITextPlane.class, "getTextPlane2", "method.iblockscripted.gettextplane2"),
			new MetodData(ITextPlane.class, "getTextPlane3", "method.iblockscripted.gettextplane3"),
			new MetodData(ITextPlane.class, "getTextPlane4", "method.iblockscripted.gettextplane4"),
			new MetodData(ITextPlane.class, "getTextPlane5", "method.iblockscripted.gettextplane5"),
			new MetodData(ITextPlane.class, "getTextPlane6", "method.iblockscripted.gettextplane6"),
			new MetodData(ITimers.class, "getTimers", "method.iblockscripted.gettimers"),
			new MetodData(void.class, "setHardness", "method.iblockscripted.sethardness",
				new ParameterData(float.class, "hardness", "parameter.iblockscripted.hardness")
			),
			new MetodData(void.class, "setIsLadder", "method.iblockscripted.setIsLadder",
				new ParameterData(boolean.class, "enabled", "parameter.iblockscripted.enabled")
			),
			new MetodData(void.class, "setIsPassible", "method.iblockscripted.setispassible",
				new ParameterData(boolean.class, "passible", "parameter.iblockscripted.passible")
			),
			new MetodData(void.class, "setLight", "method.iblockscripted.setlight",
				new ParameterData(int.class, "value", "parameter.iblockscripted.light")
			),
			new MetodData(void.class, "setModel", "method.iblockscripted.setmodel.0",
				new ParameterData(IItemStack.class, "item", "parameter.iblockscripted.itemstack")
			),
			new MetodData(void.class, "setModel", "method.iblockscripted.setmodel.1",
				new ParameterData(String.class, "name", "parameter.iblockscripted.itemname")
			),
			new MetodData(void.class, "setModel", "method.iblockscripted.setmodel.2",
				new ParameterData(String.class, "blockName", "parameter.iblockscripted.blockname"),
				new ParameterData(int.class, "meta", "parameter.iblockscripted.meta")
			),
			new MetodData(void.class, "setModel", "method.iblockscripted.setmodel.3",
					new ParameterData(IBlock.class, "iblock", "parameter.iblockscripted.iblock")
			),
			new MetodData(void.class, "setRedstonePower", "method.iblockscripted.setredstonepower",
					new ParameterData(int.class, "strength", "parameter.iblockscripted.strength")
			),
			new MetodData(void.class, "setResistance", "method.iblockscripted.setresistance",
				new ParameterData(float.class, "resistance", "parameter.iblockscripted.resistance")
			),
			new MetodData(void.class, "setRotation", "method.iblockscripted.setrotation",
				new ParameterData(int.class, "x", "parameter.rotx"),
				new ParameterData(int.class, "y", "parameter.roty"),
				new ParameterData(int.class, "z", "parameter.rotz")
			),
			new MetodData(void.class, "setScale", "method.iblockscripted.setscale",
				new ParameterData(float.class, "x", "parameter.iblockscripted.scalex"),
				new ParameterData(float.class, "y", "parameter.iblockscripted.scaley"),
				new ParameterData(float.class, "z", "parameter.iblockscripted.scalez")
			),
			new MetodData(void.class, "trigger", "method.trigger",
				new ParameterData(int.class, "id", "parameter.trigger.id"),
				new ParameterData(Object[].class, "arguments", "parameter.trigger.arguments")
			)
		)
	),
	IBlockScriptedDoor(new InterfaseData(IBlockScriptedDoor.class, IBlock.class,
			new Class<?>[] { BlockScriptedDoorWrapper.class },
			"interfase.iblockscripteddoor",
			new MetodData(String.class, "getBlockModel", "method.iblockscripted.getmodel"),
			new MetodData(float.class, "getHardness", "method.iblockscripted.gethardness"),
			new MetodData(boolean.class, "getOpen", "method.iblockscripteddoor.getopen"),
			new MetodData(float.class, "getResistance", "method.iblockscripted.getresistance"),
			new MetodData(ITimers.class, "getTimers", "method.iblockscripted.gettimers"),
			new MetodData(void.class, "setBlockModel", "method.iblockscripted.setmodel.1",
				new ParameterData(String.class, "name", "parameter.iblockscripted.itemname")
			),
			new MetodData(void.class, "setHardness", "method.iblockscripted.sethardness",
				new ParameterData(float.class, "hardness", "parameter.iblockscripted.hardness")
			),
			new MetodData(void.class, "setOpen", "method.iblockscripteddoor.setopen",
				new ParameterData(boolean.class, "open", "parameter.boolean")
			),
			new MetodData(void.class, "setResistance", "method.iblockscripted.setresistance",
				new ParameterData(float.class, "resistance", "parameter.iblockscripted.resistance")
			)
		)
	),
	ICustomElement(new InterfaseData(ICustomElement.class, null,
			new Class<?>[] { CustomBlock.class, CustomLiquid.class, CustomArmor.class, CustomBow.class, CustomFishingRod.class,
		CustomFood.class, CustomItem.class, CustomShield.class, CustomTool.class, CustomWeapon.class, CustomPotion.class },
			"interfase.icustomelement",
			new MetodData(String.class, "getCustomName", "method.icustomelement.getcustomname"),
			new MetodData(INbt.class, "getCustomNbt", "method.icustomelement.getcustomnbt")
		)
	),
	ITextPlane(new InterfaseData(ITextPlane.class, null,
			new Class<?>[] { TileScripted.TextPlane.class },
			"interfase.itextplane",
			new MetodData(float.class, "getOffsetX", "method.itextplane.getoffsetx"),
			new MetodData(float.class, "getOffsetY", "method.itextplane.getoffsety"),
			new MetodData(float.class, "getOffsetZ", "method.itextplane.getoffsetz"),
			new MetodData(int.class, "getRotationX", "method.getrotx"),
			new MetodData(int.class, "getRotationY", "method.getroty"),
			new MetodData(int.class, "getRotationZ", "method.getrotz"),
			new MetodData(float.class, "getScale", "method.itextplane.getscale"),
			new MetodData(String.class, "getText", "method.itextplane.gettext"),
			new MetodData(void.class, "setOffsetX", "method.itextplane.setOffsetx",
				new ParameterData(float.class, "x", "parameter.posx")
			),
			new MetodData(void.class, "setOffsetY", "method.itextplane.setOffsety",
				new ParameterData(float.class, "y", "parameter.posy")
			),
			new MetodData(void.class, "setOffsetZ", "method.itextplane.setOffsetz",
				new ParameterData(float.class, "z", "parameter.posz")
			),
			new MetodData(void.class, "setRotationX", "method.setrotx",
				new ParameterData(int.class, "x", "parameter.rotx")
			),
			new MetodData(void.class, "setRotationY", "method.setroty",
				new ParameterData(int.class, "y", "parameter.roty")
			),
			new MetodData(void.class, "setRotationZ", "method.setrotz",
				new ParameterData(int.class, "z", "parameter.rotz")
			),
			new MetodData(void.class, "setScale", "method.itextplane.setscale",
				new ParameterData(int.class, "scale", "parameter.itextplane.setscale")
			),
			new MetodData(void.class, "setText", "method.itextplane.settext",
				new ParameterData(int.class, "text", "parameter.itextplane.settext")
			)
		)
	),
	IContainer(new InterfaseData(IContainer.class, null,
			new Class<?>[] { ContainerWrapper.class },
			"interfase.icontainer",
			new MetodData(int.class, "count", "method.icontainer.count",
				new ParameterData(IItemStack.class, "item", "parameter.item.found"),
				new ParameterData(boolean.class, "ignoredamage", "parameter.ignoredamage"),
				new ParameterData(boolean.class, "ignorenbt", "parameter.ignorenbt")
			),
			new MetodData(IItemStack[].class, "getItems", "method.icontainer.getitems"),
			new MetodData(Container.class, "getMCContainer", "method.icontainer.getmccontainer"),
			new MetodData(IInventory.class, "getMCInventory", "method.icontainer.getmcinventory"),
			new MetodData(int.class, "getSize", "method.icontainer.getsize"),
			new MetodData(IItemStack.class, "getSlot", "method.icontainer.getslot",
				new ParameterData(int.class, "slot", "parameter.slot")
			),
			new MetodData(void.class, "setSlot", "method.icontainer.setslot",
				new ParameterData(int.class, "slot", "parameter.slot"),
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			)
		)
	),
	IContainerCustomChest(new InterfaseData(IContainerCustomChest.class, IContainer.class,
			new Class<?>[] { ContainerCustomChestWrapper.class },
			"interfase.icontainercustomchest",
			new MetodData(String.class, "getName", "method.icontainercustomchest.getname"),
			new MetodData(void.class, "setName", "method.icontainercustomchest.setname",
				new ParameterData(String.class, "name", "parameter.icontainercustomchest.name")
			)
		)
	),
	IDamageSource(new InterfaseData(IDamageSource.class, null,
			new Class<?>[] { DamageSourceWrapper.class },
			"interfase.idamagesource",
			new MetodData(IEntity.class, "getImmediateSource", "method.idamagesource.getimmediatesource"),
			new MetodData(DamageSource.class, "getMCDamageSource", "method.idamagesource.getmcdamagesource"),
			new MetodData(IEntity.class, "getTrueSource", "method.idamagesource.gettruesource"),
			new MetodData(String.class, "getType", "method.idamagesource.gettype"),
			new MetodData(boolean.class, "isProjectile", "method.idamagesource.isprojectile"),
			new MetodData(boolean.class, "isUnblockable", "method.idamagesource.isunblockable")
		)
	),
	IDimension(new InterfaseData(IDimension.class, null,
			new Class<?>[] { DimensionWrapper.class },
			"interfase.idimension",
			new MetodData(int.class, "getId", "method.idimension.getid"),
			new MetodData(String.class, "getName", "method.idimension.getname"),
			new MetodData(String.class, "getSuffix", "method.idimension.getsuffix")
		)
	),
	IEntityDamageSource(new InterfaseData(IEntityDamageSource.class, null,
			new Class<?>[] { NpcEntityDamageSource.class },
			"interfase.ientitydamagesource",
			new MetodData(String.class, "getType", "method.idamagesource.gettype"),
			new MetodData(void.class, "setType", "method.ientitydamagesource.settype",
				new ParameterData(String.class, "damageType", "parameter.ientitydamagesource.damageType")
			),
			new MetodData(IEntity.class, "getITrueSource", "method.idamagesource.gettruesource"),
			new MetodData(void.class, "setTrueSource", "method.ientitydamagesource.settruesource",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MetodData(IEntity.class, "getIImmediateSource", "method.idamagesource.getimmediatesource"),
			new MetodData(void.class, "setImmediateSource", "method.ientitydamagesource.setimmediatesource",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MetodData(String.class, "getDeadMessage", "method.ientitydamagesource.getdeadmessage"),
			new MetodData(void.class, "setDeadMessage", "method.ientitydamagesource.setdeadmessage",
				new ParameterData(String.class, "message", "parameter.message")
			),
			new MetodData(boolean.class, "getIsThornsDamage", "method.ientitydamagesource.getisthornsdamage"),
			new MetodData(IEntityDamageSource.class, "setIsThornsDamage", "method.ientitydamagesource.setisthornsdamage")
		)
	),
	INbt(new InterfaseData(INbt.class, null,
			new Class<?>[] { NBTWrapper.class },
			"interfase.inbt",
			new MetodData(void.class, "clear", "method.inbt.clear"),
			new MetodData(boolean.class, "getBoolean", "method.inbt.getboolean",
				new ParameterData(String.class, "key", "parameter.inbt.key.boolean")
			),
			new MetodData(byte.class, "getByte", "method.inbt.getbyte",
				new ParameterData(String.class, "key", "parameter.inbt.key.byte")
			),
			new MetodData(byte[].class, "getByteArray", "method.inbt.getbytearray",
				new ParameterData(String.class, "key", "parameter.inbt.key.bytearr")
			),
			new MetodData(INbt.class, "getCompound", "method.inbt.getcompound",
				new ParameterData(String.class, "key", "parameter.inbt.key.compound")
			),
			new MetodData(double.class, "getDouble", "method.inbt.getdouble",
				new ParameterData(String.class, "key", "parameter.inbt.key.double")
			),
			new MetodData(float.class, "getFloat", "method.inbt.getfloat",
				new ParameterData(String.class, "key", "parameter.inbt.key.float")
			),
			new MetodData(String.class, "getInteger", "method.inbt.getinteger",
				new ParameterData(String.class, "key", "parameter.inbt.key.int")
			),
			new MetodData(int[].class, "getIntegerArray", "method.inbt.getintarr",
				new ParameterData(String.class, "key", "parameter.inbt.key.intarr")
			),
			new MetodData(String[].class, "getKeys", "method.inbt.getkeys"),
			new MetodData(Object[].class, "getList", "method.inbt.getlist",
				new ParameterData(String.class, "key", "parameter.inbt.key.list"),
				new ParameterData(int.class, "type", "parameter.inbt.type.list")
			),
			new MetodData(int.class, "getListType", "method.inbt.getlisttype"),
			new MetodData(long.class, "getLong", "method.inbt.getlong",
				new ParameterData(String.class, "key", "parameter.inbt.key.long")
			),
			new MetodData(NBTTagCompound.class, "getMCNBT", "method.inbt.getmcnbt"),
			new MetodData(short.class, "getShort", "method.inbt.getshort",
				new ParameterData(String.class, "key", "parameter.inbt.key.short")
			),
			new MetodData(String.class, "getString", "method.inbt.getstring",
				new ParameterData(String.class, "key", "parameter.inbt.key.string")
			),
			new MetodData(int.class, "getType", "method.inbt.gettype",
				new ParameterData(String.class, "key", "parameter.inbt.key.string")
			),
			new MetodData(boolean.class, "has", "method.inbt.has",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt")
			),
			new MetodData(void.class, "isEqual", "method.inbt.isequal",
				new ParameterData(INbt.class, "nbt", "parameter.inbt.key.nbt")
			),
			new MetodData(void.class, "merge", "method.inbt.merge",
				new ParameterData(INbt.class, "nbt", "parameter.inbt.key.nbt")
			),
			new MetodData(void.class, "remove", "method.inbt.remove",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt")
			),
			new MetodData(void.class, "setBoolean", "method.inbt.setboolean",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(boolean.class, "value", "parameter.inbt.key.boolean")
			),
			new MetodData(void.class, "setByte", "method.inbt.setbyte",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(boolean.class, "value", "parameter.inbt.key.byte")
			),
			new MetodData(void.class, "setByteArray", "method.inbt.setbytearray",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(byte[].class, "value", "parameter.inbt.key.bytearr")
			),
			new MetodData(void.class, "setCompound", "method.inbt.setcompound",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(INbt.class, "value", "parameter.inbt.key.compound")
			),
			new MetodData(void.class, "setDouble", "method.inbt.setdouble",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(double.class, "value", "parameter.inbt.key.double")
			),
			new MetodData(void.class, "setFloat", "method.inbt.setfloat",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(float.class, "value", "parameter.inbt.key.float")
			),
			new MetodData(void.class, "setInteger", "method.inbt.setinteger",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(int.class, "value", "parameter.inbt.key.int")
			),
			new MetodData(void.class, "setIntegerArray", "method.inbt.setintegerarray",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(int.class, "value", "parameter.inbt.key.intarr")
			),
			new MetodData(void.class, "setList", "method.inbt.setlist",
				new ParameterData(String.class, "key", "parameter.inbt.key.list"),
				new ParameterData(Object[].class, "value", "parameter.inbt.list.objarr")
			),
			new MetodData(void.class, "setLong", "method.inbt.setlong",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(long.class, "value", "parameter.inbt.key.long")
			),
			new MetodData(void.class, "setShort", "method.inbt.setshort",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(short.class, "value", "parameter.inbt.key.short")
			),
			new MetodData(void.class, "setString", "method.inbt.setstring",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(String.class, "value", "parameter.inbt.key.string")
			),
			new MetodData(String.class, "toJsonString", "method.inbt.tojsonstring")
		)
	),
	IPos(new InterfaseData(IPos.class, null,
			new Class<?>[] { BlockPosWrapper.class },
			"interfase.ipos",
			new MetodData(IPos.class, "add", "method.ipos.add",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(IPos.class, "add", "method.ipos.add",
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MetodData(double.class, "distanceTo", "method.ipos.distanceto",
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MetodData(IPos.class, "down", "method.ipos.down.0"),
			new MetodData(IPos.class, "down", "method.ipos.down.1",
				new ParameterData(int.class, "n", "parameter.blocks")
			),
			new MetodData(IPos.class, "east", "method.ipos.east.0"),
			new MetodData(IPos.class, "east", "method.ipos.east.1",
				new ParameterData(int.class, "n", "parameter.blocks")
			),
			new MetodData(BlockPos.class, "getMCBlockPos", "method.ipos.getmcblockpos"),
			new MetodData(int.class, "getX", "method.getx"),
			new MetodData(int.class, "getY", "method.gety"),
			new MetodData(int.class, "getZ", "method.getz"),
			new MetodData(int.class, "normalize", "method.ipos.normalize"),
			new MetodData(IPos.class, "north", "method.ipos.north.0"),
			new MetodData(IPos.class, "north", "method.ipos.north.1",
				new ParameterData(int.class, "n", "parameter.blocks")
			),
			new MetodData(IPos.class, "offset", "method.ipos.offset.0",
				new ParameterData(int.class, "direction", "parameter.direction")
			),
			new MetodData(IPos.class, "offset", "method.ipos.offset.1",
				new ParameterData(int.class, "direction", "parameter.direction"),
				new ParameterData(int.class, "n", "parameter.blocks")
			),
			new MetodData(IPos.class, "south", "method.ipos.south.0"),
			new MetodData(IPos.class, "south", "method.ipos.south.1",
				new ParameterData(int.class, "n", "parameter.blocks")
			),
			new MetodData(IPos.class, "subtract", "method.ipos.subtract",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(IPos.class, "up", "method.ipos.up.0"),
			new MetodData(IPos.class, "up", "method.ipos.up.1",
				new ParameterData(int.class, "n", "parameter.blocks")
			),
			new MetodData(IPos.class, "west", "method.ipos.west.0"),
			new MetodData(IPos.class, "west", "method.ipos.west.1",
				new ParameterData(int.class, "n", "parameter.blocks")
			)
		)
	),
	IRayTrace(new InterfaseData(IRayTrace.class, null,
			new Class<?>[] { RayTraceWrapper.class },
			"interfase.iraytrace",
			new MetodData(IBlock.class, "getBlock", "method.iraytrace.getblock"),
			new MetodData(IPos.class, "getPos", "method.getpos"),
			new MetodData(int.class, "getSideHit", "method.iraytrace.getsidehit")
		)
	),
	IScoreboard(new InterfaseData(IScoreboard.class, null,
			new Class<?>[] { ScoreboardWrapper.class },
			"interfase.iscoreboard",
			new MetodData(IScoreboardObjective.class, "addObjective", "method.iscoreboard.addobjective",
				new ParameterData(String.class, "objective", "parameter.iscoreboard.objective"),
				new ParameterData(String.class, "criteria", "parameter.iscoreboard.criteria")
			),
			new MetodData(IScoreboardTeam.class, "addTeam", "method.iscoreboard.addTeam",
				new ParameterData(String.class, "name", "parameter.iscoreboard.teamname")
			),
			new MetodData(void.class, "deletePlayerScore", "method.iscoreboard.deletePlayerScore",
				new ParameterData(String.class, "player", "parameter.playername"),
				new ParameterData(String.class, "objective", "parameter.iscoreboard.objective"),
				new ParameterData(String.class, "datatag", "parameter.iscoreboard.datatag")
			),
			new MetodData(IScoreboardObjective.class, "getObjective", "method.iscoreboard.getobjective",
				new ParameterData(String.class, "name", "parameter.iscoreboard.objective")
			),
			new MetodData(IScoreboardObjective[].class, "getObjectives", "method.iscoreboard.getobjectives"),
			new MetodData(String[].class, "getPlayerList", "method.iscoreboard.getplayerlist"),
			new MetodData(int.class, "getPlayerScore", "method.iscoreboard.getplayerscore",
				new ParameterData(String.class, "player", "parameter.playername"),
				new ParameterData(String.class, "objective", "parameter.iscoreboard.objective"),
				new ParameterData(String.class, "datatag", "parameter.iscoreboard.datatag")
			),
			new MetodData(IScoreboardTeam.class, "getPlayerTeam", "method.iscoreboard.getplayerteam",
				new ParameterData(String.class, "player", "parameter.playername")
			),
			new MetodData(IScoreboardTeam.class, "getPlayerTeam", "method.iscoreboard.getplayerteam",
				new ParameterData(String.class, "name", "parameter.iscoreboard.teamname")
			),
			new MetodData(IScoreboardTeam[].class, "getTeams", "method.iscoreboard.getteams"),
			new MetodData(boolean.class, "hasObjective", "method.iscoreboard.hasobjective",
				new ParameterData(String.class, "objective", "parameter.iscoreboard.objective")
			),
			new MetodData(boolean.class, "hasPlayerObjective", "method.iscoreboard.hasplayerobjective",
				new ParameterData(String.class, "player", "parameter.playername"),
				new ParameterData(String.class, "objective", "parameter.iscoreboard.objective"),
				new ParameterData(String.class, "datatag", "parameter.iscoreboard.datatag")
			),
			new MetodData(boolean.class, "hasTeam", "method.iscoreboard.hasteam",
				new ParameterData(String.class, "name", "parameter.iscoreboard.teamname")
			),
			new MetodData(void.class, "removeObjective", "method.iscoreboard.removeobjective",
				new ParameterData(String.class, "objective", "parameter.iscoreboard.objective")
			),
			new MetodData(boolean.class, "removePlayerTeam", "method.iscoreboard.removeteam",
				new ParameterData(String.class, "player", "parameter.playername")
			),
			new MetodData(boolean.class, "removeTeam", "method.iscoreboard.removeteam",
				new ParameterData(String.class, "name", "parameter.iscoreboard.teamname")
			),
			new MetodData(void.class, "setPlayerScore", "method.iscoreboard.setplayerscore",
				new ParameterData(String.class, "player", "parameter.playername"),
				new ParameterData(String.class, "objective", "parameter.iscoreboard.objective"),
				new ParameterData(int.class, "score", "parameter.iscoreboard.score"),
				new ParameterData(String.class, "datatag", "parameter.iscoreboard.datatag")
			)
		)
	),
	IScoreboardObjective(new InterfaseData(IScoreboardObjective.class, null,
			new Class<?>[] { ScoreboardObjectiveWrapper.class },
			"interfase.iscoreboardobjective",
			new MetodData(IScoreboardScore.class, "createScore", "method.iscoreboardobjective.createscore",
				new ParameterData(String.class, "player", "parameter.playername")
			),
			new MetodData(String.class, "getCriteria", "method.iscoreboardobjective.getcriteria"),
			new MetodData(String.class, "getDisplayName", "method.iscoreboardobjective.getdisplayname"),
			new MetodData(String.class, "getName", "method.iscoreboardobjective.getname"),
			new MetodData(IScoreboardScore.class, "getScore", "method.iscoreboardobjective.getscore",
				new ParameterData(String.class, "player", "parameter.playername")
			),
			new MetodData(IScoreboardScore[].class, "getScores", "method.iscoreboardobjective.getscores"),
			new MetodData(boolean.class, "hasScore", "method.iscoreboardobjective.hasscore",
				new ParameterData(String.class, "player", "parameter.playername")
			),
			new MetodData(boolean.class, "isReadyOnly", "method.iscoreboardobjective.isreadyonly"),
			new MetodData(void.class, "removeScore", "method.iscoreboardobjective.removescore",
				new ParameterData(String.class, "player", "parameter.playername")
			),
			new MetodData(void.class, "setDisplayName", "method.iscoreboardobjective.setdisplayname",
				new ParameterData(String.class, "name", "parameter.iscoreboard.objective")
			)
		)
	),
	IScoreboardScore(new InterfaseData(IScoreboardScore.class, null,
			new Class<?>[] { ScoreboardScoreWrapper.class },
			"interfase.iscoreboardscore",
			new MetodData(String.class, "getPlayerName", "method.getplayername"),
			new MetodData(int.class, "getValue", "method.iscoreboardscore.getvalue"),
			new MetodData(void.class, "setValue", "method.iscoreboardscore.setvalue",
				new ParameterData(int.class, "value", "parameter.score")
			)
		)
	),
	IScoreboardTeam(new InterfaseData(IScoreboardTeam.class, null,
			new Class<?>[] { ScoreboardTeamWrapper.class },
			"interfase.iscoreboardteam",
			new MetodData(void.class, "addPlayer", "method.iscoreboardteam.addplayer",
				new ParameterData(int.class, "player", "parameter.playername")
			),
			new MetodData(void.class, "clearPlayers", "method.iscoreboardteam.clearplayers"),
			new MetodData(String.class, "getColor", "method.iscoreboardteam.getcolor"),
			new MetodData(String.class, "getDisplayName", "method.iscoreboardteam.getdisplayname"),
			new MetodData(boolean.class, "getFriendlyFire", "method.iscoreboardteam.getfriendlyfire"),
			new MetodData(String.class, "getName", "method.iscoreboardteam.getname"),
			new MetodData(String[].class, "getPlayers", "method.iscoreboardteam.getplayers"),
			new MetodData(boolean.class, "getSeeInvisibleTeamPlayers", "method.iscoreboardteam.getseeinvisibleteamplayers"),
			new MetodData(void.class, "hasPlayer", "method.iscoreboardteam.hasplayer",
				new ParameterData(int.class, "player", "parameter.playername")
			),
			new MetodData(void.class, "removePlayer", "method.iscoreboardteam.removeplayer",
				new ParameterData(int.class, "player", "parameter.playername")
			),
			new MetodData(void.class, "setColor", "method.iscoreboardteam.setColor",
				new ParameterData(String.class, "color", "parameter.colorname")
			),
			new MetodData(void.class, "setDisplayName", "method.iscoreboardteam.setdisplayname",
				new ParameterData(String.class, "name", "parameter.iscoreboard.teamname")
			),
			new MetodData(void.class, "setFriendlyFire", "method.iscoreboardteam.setfriendlyfire",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setSeeInvisibleTeamPlayers", "method.iscoreboardteam.setseeinvisibleteamplayers",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			)
		)
	),
	ITimers(new InterfaseData(ITimers.class, null,
			new Class<?>[] { DataTimers.class },
			"interfase.itimers",
			new MetodData(void.class, "clear", "method.itimers.clear"),
			new MetodData(void.class, "forceStart", "method.itimers.forcestart",
				new ParameterData(int.class, "id", "parameter.itimers.id"),
				new ParameterData(int.class, "ticks", "parameter.ticks"),
				new ParameterData(boolean.class, "repeat", "parameter.itimers.repeat")
			),
			new MetodData(boolean.class, "has", "method.itimers.has",
				new ParameterData(int.class, "id", "parameter.itimers.id")
			),
			new MetodData(void.class, "reset", "method.itimers.reset",
				new ParameterData(int.class, "id", "parameter.itimers.id")
			),
			new MetodData(void.class, "start", "method.itimers.start",
				new ParameterData(int.class, "id", "parameter.itimers.id"),
				new ParameterData(int.class, "ticks", "parameter.ticks"),
				new ParameterData(boolean.class, "repeat", "parameter.itimers.repeat")
			),
			new MetodData(boolean.class, "stop", "method.itimers.stop",
				new ParameterData(int.class, "id", "parameter.itimers.id")
			)
		)
	),
	IWorld(new InterfaseData(IWorld.class, null,
			new Class<?>[] { WorldWrapper.class },
			"interfase.iworld",
			new MetodData(void.class, "broadcast", "method.iworld.broadcast",
				new ParameterData(String.class, "message", "parameter.message")
			),
			new MetodData(IEntity.class, "createEntity", "method.iworld.createentity",
				new ParameterData(String.class, "id", "parameter.entity.regname")
			),
			new MetodData(IEntity.class, "createEntityFromNBT", "method.iworld.createentity",
				new ParameterData(INbt.class, "nbt", "parameter.entitynbt")
			),
			new MetodData(IEntity.class, "createItem", "method.iworld.createitem",
				new ParameterData(String.class, "name", "parameter.itemname"),
				new ParameterData(int.class, "damage", "parameter.itemmeta"),
				new ParameterData(int.class, "size", "parameter.itemcount")
			),
			new MetodData(IEntity.class, "createEntityFromNBT", "method.iworld.createitem",
				new ParameterData(INbt.class, "nbt", "parameter.itemnbt")
			),
			new MetodData(void.class, "explode", "method.iworld.explode",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz"),
				new ParameterData(float.class, "range", "parameter.range"),
				new ParameterData(boolean.class, "fire", "parameter.iworld.fire"),
				new ParameterData(boolean.class, "grief", "parameter.iworld.grief")
			),
			new MetodData(IEntity[].class, "getAllEntities", "method.iworld.getallentities",
				new ParameterData(int.class, "type", "parameter.entitytype")
			),
			new MetodData(IPlayer[].class, "getAllPlayers", "method.iworld.getallplayers"),
			new MetodData(String.class, "getBiomeName", "method.iworld.getbiomename",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(IBlock.class, "getBlock", "method.iworld.getblock",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(IEntity.class, "getClone", "method.iworld.getclone",
				new ParameterData(int.class, "tab", "parameter.clone.tab"),
				new ParameterData(String.class, "name", "parameter.clone.file")
			).setDeprecated(),
			new MetodData(IEntity.class, "getClosestEntity", "method.iworld.getclosestentity",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz"),
				new ParameterData(int.class, "range", "parameter.range"),
				new ParameterData(int.class, "type", "parameter.entitytype")
			).setDeprecated(),
			new MetodData(IEntity.class, "getClosestEntity", "method.iworld.getclosestentity",
				new ParameterData(IPos.class, "pos", "parameter.pos"),
				new ParameterData(int.class, "range", "parameter.range"),
				new ParameterData(int.class, "type", "parameter.entitytype")
			),
			new MetodData(IDimension.class, "getDimension", "method.iworld.getdimension"),
			new MetodData(IEntity.class, "getEntity", "method.iworld.getentity",
				new ParameterData(String.class, "uuid", "parameter.entityuuid")
			),
			new MetodData(float.class, "getLightValue", "method.iworld.getlightvalue",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(BlockPos.class, "getMCBlockPos", "method.ipos.getmcblockpos",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(WorldServer.class, "getMCWorld", "method.iworld.getmcworld"),
			new MetodData(String.class, "getName", "method.iworld.getname"),
			new MetodData(IEntity[].class, "getNearbyEntities", "method.iworld.getnearbyEntities",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz"),
				new ParameterData(int.class, "range", "parameter.range"),
				new ParameterData(int.class, "type", "parameter.entitytype")
			).setDeprecated(),
			new MetodData(IEntity[].class, "getNearbyEntities", "method.iworld.getnearbyEntities",
				new ParameterData(IPos.class, "pos", "parameter.pos"),
				new ParameterData(int.class, "range", "parameter.range"),
				new ParameterData(int.class, "type", "parameter.entitytype")
			),
			new MetodData(IPlayer.class, "getPlayer", "method.iworld.getplayer",
				new ParameterData(IPos.class, "name", "parameter.playername")
			),
			new MetodData(int.class, "getRedstonePower", "method.iworld.getredstonepower",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(IScoreboard.class, "getScoreboard", "method.iworld.getscoreboard"),
			new MetodData(IBlock.class, "getSpawnPoint", "method.iworld.getspawnpoint"),
			new MetodData(IData.class, "getTempdata", "method.gettempdata"),
			new MetodData(IData.class, "getStoreddata", "method.getstoreddata"),
			new MetodData(long.class, "getTime", "method.iworld.gettime"),
			new MetodData(long.class, "getTotalTime", "method.iworld.gettotaltime"),
			new MetodData(boolean.class, "isDay", "method.iworld.isday"),
			new MetodData(boolean.class, "isRaining", "method.iworld.israining"),
			new MetodData(void.class, "playSoundAt", "method.iworld.playsoundat",
				new ParameterData(IPos.class, "pos", "parameter.pos"),
				new ParameterData(String.class, "sound", "parameter.range"),
				new ParameterData(float.class, "volume", "parameter.sound.volume"),
				new ParameterData(float.class, "pitch", "parameter.sound.pitch")
			),
			new MetodData(void.class, "removeBlock", "method.iworld.removeblock",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(void.class, "setBlock", "method.iworld.setblock",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz"),
				new ParameterData(String.class, "name", "parameter.iblock.setblock.1"),
				new ParameterData(int.class, "meta", "parameter.block.metadata")
			),
			new MetodData(void.class, "setRaining", "method.iworld.setraining",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setSpawnPoint", "method.iworld.setspawnpoint",
				new ParameterData(IBlock.class, "block", "parameter.iblock.setblock.0")
			),
			new MetodData(void.class, "setTime", "method.iworld.settime",
				new ParameterData(long.class, "ticks", "parameter.ticks")
			),
			new MetodData(IEntity.class, "spawnClone", "method.iworld.spawnclone",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz"),
				new ParameterData(int.class, "tab", "parameter.clone.tab"),
				new ParameterData(String.class, "name", "parameter.clone.file")
			).setDeprecated(),
			new MetodData(void.class, "spawnEntity", "method.iworld.spawnentity",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MetodData(void.class, "spawnParticle", "method.iworld.spawnparticle",
				new ParameterData(String.class, "particle", "parameter.particle"),
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz"),
				new ParameterData(double.class, "dx", "parameter.posdx"),
				new ParameterData(double.class, "dy", "parameter.posdy"),
				new ParameterData(double.class, "dz", "parameter.posdz"),
				new ParameterData(double.class, "speed", "parameter.speed"),
				new ParameterData(int.class, "count", "parameter.count")
			),
			new MetodData(void.class, "thunderStrike", "method.iworld.thunderstrike",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz")
			),
			new MetodData(void.class, "trigger", "method.trigger",
				new ParameterData(int.class, "id", "parameter.trigger.id"),
				new ParameterData(Object[].class, "arguments", "parameter.trigger.arguments")
			)
		)
	),

	IJobBard(new InterfaseData(IJobBard.class, INPCJob.class,
			new Class<?>[] { JobBard.class },
			"interfase.ijobbard",
			new MetodData(String.class, "getSong", "method.ijobbard.getsong"),
			new MetodData(void.class, "setSong", "method.ijobbard.setsong",
				new ParameterData(String.class, "song", "parameter.sound.name")
			)
		)
	),
	IJobBuilder(new InterfaseData(IJobBuilder.class, INPCJob.class,
			new Class<?>[] { JobBuilder.class },
			"interfase.ijobbuilder",
			new MetodData(boolean.class, "isBuilding", "method.ijobbuilder.isbuilding")
		)
	),
	IJobFarmer(new InterfaseData(IJobFarmer.class, INPCJob.class,
			new Class<?>[] { JobFarmer.class },
			"interfase.ijobfarmer",
			new MetodData(boolean.class, "isPlucking", "method.ijobfarmer.isplucking")
		)
	),
	IJobFollower(new InterfaseData(IJobFollower.class, INPCJob.class,
			new Class<?>[] { JobFollower.class },
			"interfase.ijobfollower",
			new MetodData(String.class, "getFollowing", "method.ijobfollower.getfollowing"),
			new MetodData(ICustomNpc.class, "getFollowingNpc", "method.ijobfollower.getfollowingnpc"),
			new MetodData(boolean.class, "isFollowing", "method.ijobfollower.isFollowing"),
			new MetodData(void.class, "setFollowing", "method.ijobfollower.setfollowing",
				new ParameterData(String.class, "name", "parameter.entity.name")
			)
		)
	),
	IJobPuppet(new InterfaseData(IJobPuppet.class, INPCJob.class,
			new Class<?>[] { JobPuppet.class },
			"interfase.ijobpuppet",
			new MetodData(int.class, "getAnimationSpeed", "method.ijobpuppet.getanimationspeed"),
			new MetodData(boolean.class, "getIsAnimated", "method.ijobpuppet.getisanimated"),
			new MetodData(IJobPuppetPart.class, "getPart", "method.ijobpuppet.getpart",
				new ParameterData(int.class, "part", "parameter.ijobpuppet.part")
			),
			new MetodData(void.class, "setAnimationSpeed", "method.ijobpuppet.setanimationspeed",
				new ParameterData(int.class, "speed", "parameter.speed")
			),
			new MetodData(void.class, "setIsAnimated", "method.ijobpuppet.setisanimated",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			)
		)
	),
	IJobPuppetPart(new InterfaseData(IJobPuppetPart.class, null,
			new Class<?>[] { JobPuppet.PartConfig.class },
			"interfase.ijobpuppetpart",
			new MetodData(int.class, "getRotationX", "method.getrotx"),
			new MetodData(int.class, "getRotationY", "method.getroty"),
			new MetodData(int.class, "getRotationZ", "method.getrotz"),
			new MetodData(void.class, "setRotation", "method.setrot",
				new ParameterData(int.class, "x", "parameter.rotx"),
				new ParameterData(int.class, "y", "parameter.roty"),
				new ParameterData(int.class, "z", "parameter.rotz")
			)
		)
	),
	IJobSpawner(new InterfaseData(IJobSpawner.class, INPCJob.class,
			new Class<?>[] { JobSpawner.class },
			"interfase.ijobspawner",
			new MetodData(void.class, "removeAllSpawned", "method.ijobspawner.removeallspawned"),
			new MetodData(IEntityLivingBase.class, "spawnEntity", "method.ijobspawner.spawnentity",
				new ParameterData(int.class, "pos", "parameter.ijobspawner.pos"),
				new ParameterData(boolean.class, "isDead", "parameter.ijobspawner.isDead")
			)
		)
	),
	IRoleDialog(new InterfaseData(IRoleDialog.class, INPCRole.class,
			new Class<?>[] { RoleDialog.class },
			"interfase.iroledialog",
			new MetodData(String.class, "getDialog", "method.iroledialog.getdialog"),
			new MetodData(String.class, "getOption", "method.iroledialog.option",
				new ParameterData(int.class, "option", "parameter.dialog.option.pos")
			),
			new MetodData(String.class, "getOptionDialog", "method.iroledialog.getoptiondialog",
				new ParameterData(int.class, "option", "parameter.dialog.option.pos")
			),
			new MetodData(void.class, "setDialog", "method.iroledialog.setdialog",
				new ParameterData(String.class, "text", "parameter.dialog.text")
			),
			new MetodData(void.class, "setOption", "method.iroledialog.setoption",
				new ParameterData(int.class, "option", "parameter.dialog.option.pos"),
				new ParameterData(String.class, "text", "parameter.dialog.text")
			),
			new MetodData(void.class, "setOptionDialog", "method.iroledialog.setoptiondialog",
				new ParameterData(int.class, "option", "parameter.dialog.option.pos"),
				new ParameterData(String.class, "text", "parameter.dialog.text")
			)
		)
	),
	IRoleFollower(new InterfaseData(IRoleFollower.class, INPCRole.class,
			new Class<?>[] { RoleFollower.class },
			"interfase.irolefollower",
			new MetodData(void.class, "addDays", "method.irolefollower.adddays",
				new ParameterData(int.class, "days", "parameter.rl.days")
			),
			new MetodData(int.class, "getDays", "method.irolefollower.getdays"),
			new MetodData(IPlayer.class, "getFollowing", "method.irolefollower.getfollowing"),
			new MetodData(boolean.class, "getGuiDisabled", "method.irolefollower.getguidisabled"),
			new MetodData(boolean.class, "getInfinite", "method.irolefollower.getinfinite"),
			new MetodData(boolean.class, "getRefuseSoulstone", "method.irolefollower.getrefusesoulstone"),
			new MetodData(boolean.class, "isFollowing", "method.irolefollower.isfollowing"),
			new MetodData(void.class, "reset", "method.irolefollower.reset"),
			new MetodData(void.class, "setFollowing", "method.irolefollower.setfollowing",
				new ParameterData(IPlayer.class, "player", "parameter.player")
			),
			new MetodData(void.class, "setGuiDisabled", "method.irolefollower.setguidisabled",
				new ParameterData(boolean.class, "disabled", "parameter.boolean")
			),
			new MetodData(void.class, "setInfinite", "method.irolefollower.setinfinite",
				new ParameterData(boolean.class, "infinite", "parameter.boolean")
			),
			new MetodData(void.class, "setRefuseSoulstone", "method.irolefollower.setrefusesoulstone",
				new ParameterData(boolean.class, "refuse", "parameter.boolean")
			)
		)
	),
	IRoleTrader(new InterfaseData(IRoleTrader.class, INPCRole.class,
			new Class<?>[] { RoleTrader.class },
			"interfase.iroletrader"
		)
	),
	IRoleTransporter(new InterfaseData(IRoleTransporter.class, INPCRole.class,
			new Class<?>[] { RoleTransporter.class },
			"interfase.iroletransporter"
		)
	),
	IAttributeSet(new InterfaseData(IAttributeSet.class, null,
			new Class<?>[] { DataInventory.AttributeSet.class },
			"interfase.iattributeset"
		)
	),
	ICustomDrop(new InterfaseData(ICustomDrop.class, null,
			new Class<?>[] {  DataInventory.DropSet.class },
			"interfase.icustomdrop"
		)
	),
	IData(new InterfaseData(IData.class, null, null,
			"interfase.idata"
		)
	),
	IDropNbtSet(new InterfaseData(IDropNbtSet.class, null,
			new Class<?>[] { DataInventory.DropNbtSet.class },
			"interfase.idropnbtset"
		)
	),
	IEnchantSet(new InterfaseData(IEnchantSet.class, null,
			new Class<?>[] { DataInventory.EnchantSet.class },
			"interfase.ienchantset"
		)
	),
	ILine(new InterfaseData(ILine.class, null,
			new Class<?>[] { noppes.npcs.controllers.data.Line.class },
			"interfase.iline"
		)
	),
	IMark(new InterfaseData(IMark.class, null,
			new Class<?>[] { MarkData.Mark.class },
			"interfase.imark"
		)
	),
	INPCAdvanced(new InterfaseData(INPCAdvanced.class, null,
			new Class<?>[] { DataAdvanced.class },
			"interfase.inpcadvanced"
		)
	),
	INPCAi(new InterfaseData(INPCAi.class, null,
			new Class<?>[] { DataAI.class },
			"interfase.inpcai"
		)
	),
	INPCDisplay(new InterfaseData(INPCDisplay.class, null,
			new Class<?>[] { DataDisplay.class },
			"interfase.inpcdisplay"
		)
	),
	INPCInventory(new InterfaseData(INPCInventory.class, null,
			new Class<?>[] { DataInventory.class },
			"interfase.inpcinventory"
		)
	),
	INPCJob(new InterfaseData(INPCJob.class, null,
			new Class<?>[] { JobBard.class, JobBuilder.class, JobChunkLoader.class, JobConversation.class, JobInterface.class,
				JobFollower.class, JobGuard.class, JobHealer.class, JobItemGiver.class, JobPuppet.class, JobSpawner.class },
			"interfase.inpcjob"
		)
	),
	INPCMelee(new InterfaseData(INPCMelee.class, null,
			new Class<?>[] { DataMelee.class },
			"interfase.inpcmelee"
		)
	),
	INPCRanged(new InterfaseData(INPCRanged.class, null,
			new Class<?>[] { DataRanged.class },
			"interfase.inpcranged"
		)
	),
	INPCRole(new InterfaseData(INPCRole.class, null,
			new Class<?>[] { RoleBank.class, RoleCompanion.class, RoleDialog.class, RoleFollower.class, RolePostman.class,
				RoleTrader.class, RoleTransporter.class },
			"interfase.inpcrole"
		)
	),
	INPCStats(new InterfaseData(INPCStats.class, null,
			new Class<?>[] { DataStats.class },
			"interfase.inpcstats"
		)
	),
	IPixelmonPlayerData(new InterfaseData(IPixelmonPlayerData.class, null, null,
			"interfase.ipixelmonplayerdata"
		)
	),
	IPlayerMail(new InterfaseData(IPlayerMail.class, null,
			new Class<?>[] { PlayerMail.class },
			"interfase.iplayermail"
		)
	),
	IAnimal(new InterfaseData(IAnimal.class, IEntityLiving.class,
			new Class<?>[] { AnimalWrapper.class },
			"interfase.ianimal"
		)
	),
	IArrow(new InterfaseData(IArrow.class, IEntity.class,
			new Class<?>[] { ArrowWrapper.class },
			"interfase.iarrow"
		)
	),
	ICustomNpc(new InterfaseData(ICustomNpc.class, IEntityLiving.class,
			new Class<?>[] { NPCWrapper.class },
			"interfase.icustomnpc",
			new MetodData(void.class, "trigger", "method.trigger",
				new ParameterData(int.class, "id", "parameter.trigger.id"),
				new ParameterData(Object[].class, "arguments", "parameter.trigger.arguments")
			)
		)
	),
	IEntity(new InterfaseData(IEntity.class, null,
			new Class<?>[] { EntityWrapper.class },
			"interfase.ientity",
			new MetodData(void.class, "addRider", "method.ientity.addrider",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MetodData(void.class, "addTag", "method.ientity.addtag",
				new ParameterData(String.class, "tag", "parameter.ientity.tag")
			),
			new MetodData(void.class, "clearRiders", "method.ientity.clearriders"),
			new MetodData(void.class, "damage", "method.ientity.damage",
				new ParameterData(float.class, "amount", "parameter.ientity.damageamount")
			),
			new MetodData(void.class, "damage", "method.ientity.damage",
				new ParameterData(float.class, "amount", "parameter.ientity.damageamount"),
				new ParameterData(IEntityDamageSource.class, "source", "parameter.ientity.damagesource")
			),
			new MetodData(void.class, "despawn", "method.ientity.despawn"),
			new MetodData(IEntityItem.class, "dropItem", "method.ientity.dropitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MetodData(void.class, "extinguish", "method.ientity.extinguish"),
			new MetodData(String.class, "generateNewUUID", "method.ientity.newuuid"),
			new MetodData(long.class, "getAge", "method.ientity.getage"),
			new MetodData(IEntity[].class, "getAllRiders", "method.ientity.getallriders"),
			new MetodData(int.class, "getBlockX", "method.ientity.getblockx"),
			new MetodData(int.class, "getBlockY", "method.ientity.getblocky"),
			new MetodData(int.class, "getBlockZ", "method.ientity.getblockz"),
			new MetodData(String.class, "getEntityName", "method.ientity.getentityname"),
			new MetodData(INbt.class, "getEntityNbt", "method.ientity.getentitynbt"),
			new MetodData(float.class, "getEyeHeight", "method.ientity.geteyeheight"),
			new MetodData(float.class, "getHeight", "method.ientity.getheight"),
			new MetodData(IEntity.class, "getMCEntity", "method.ientity.getmcentity"),
			new MetodData(double.class, "getMotionX", "method.ientity.getmotionx"),
			new MetodData(double.class, "getMotionY", "method.ientity.getmotiony"),
			new MetodData(double.class, "getMotionZ", "method.ientity.getmotionz"),
			new MetodData(IEntity.class, "getMount", "method.ientity.getmount"),
			new MetodData(String.class, "getName", "method.ientity.getname"),
			new MetodData(INbt.class, "getNbt", "method.ientity.getnbt"),
			new MetodData(float.class, "getPitch", "method.ientity.getpitch"),
			new MetodData(IPos.class, "getPos", "method.getpos"),
			new MetodData(IEntity[].class, "getRiders", "method.ientity.getriders"),
			new MetodData(float.class, "getRotation", "method.ientity.getrotation"),
			new MetodData(IData.class, "getStoreddata", "method.getstoreddata"),
			new MetodData(String[].class, "getTags", "method.ientity.gettags"),
			new MetodData(IData.class, "getTempdata", "method.gettempdata"),
			new MetodData(int.class, "getType", "method.ientity.gettype"),
			new MetodData(String.class, "getTypeName", "method.ientity.gettypename"),
			new MetodData(String.class, "getUUID", "method.ientity.getuuid"),
			new MetodData(float.class, "getWidth", "method.ientity.getwidth"),
			new MetodData(IWorld.class, "getWorld", "method.ientity.getworld"),
			new MetodData(double.class, "getX", "method.getx"),
			new MetodData(double.class, "getY", "method.gety"),
			new MetodData(double.class, "getZ", "method.getz"),
			new MetodData(boolean.class, "hasCustomName", "method.ientity.hascustomname"),
			new MetodData(boolean.class, "hasTag", "method.ientity.hastag",
				new ParameterData(String.class, "tag", "parameter.ientity.tag")
			),
			new MetodData(boolean.class, "inFire", "method.ientity.infire"),
			new MetodData(boolean.class, "inLava", "method.ientity.inlava"),
			new MetodData(boolean.class, "inWater", "method.ientity.inwater"),
			new MetodData(boolean.class, "isAlive", "method.ientity.isalive"),
			new MetodData(boolean.class, "isBurning", "method.ientity.isburning"),
			new MetodData(boolean.class, "isSneaking", "method.ientity.issneaking"),
			new MetodData(boolean.class, "isSprinting", "method.ientity.issprinting"),
			new MetodData(void.class, "kill", "method.ientity.kill"),
			new MetodData(void.class, "knockback", "method.ientity.knockback",
				new ParameterData(int.class, "power", "parameter.ientity.power"),
				new ParameterData(float.class, "direction", "parameter.ientity.direction")
			),
			new MetodData(void.class, "playAnimation", "method.ientity.playanimation",
				new ParameterData(int.class, "type", "parameter.ientity.animtype")
			),
			new MetodData(IRayTrace.class, "rayTraceBlock", "method.ientity.raytraceblock",
				new ParameterData(double.class, "distance", "parameter.ientity.raydistance"),
				new ParameterData(boolean.class, "stopOnLiquid", "parameter.ientity.raystoponliquid"),
				new ParameterData(boolean.class, "ignoreBlockWithoutBoundingBox", "parameter.ientity.rayignoreblockwithoutboundingbox")
			),
			new MetodData(IEntity[].class, "rayTraceEntities", "method.ientity.raytraceentities",
				new ParameterData(double.class, "distance", "parameter.ientity.raydistance"),
				new ParameterData(boolean.class, "stopOnLiquid", "parameter.ientity.raystoponliquid"),
				new ParameterData(boolean.class, "ignoreBlockWithoutBoundingBox", "parameter.ientity.rayignoreblockwithoutboundingbox")
			),
			new MetodData(void.class, "removeTag", "method.ientity.removetag",
				new ParameterData(String.class, "tag", "parameter.ientity.tag")
			),
			new MetodData(void.class, "setBurning", "method.ientity.setburning",
				new ParameterData(int.class, "seconds", "parameter.seconds")
			),
			new MetodData(void.class, "setEntityNbt", "method.ientity.setentitynbt",
				new ParameterData(INbt.class, "nbt", "parameter.ientity.entitynbt")
			),
			new MetodData(void.class, "setMotionX", "method.ientity.setmotionx",
				new ParameterData(double.class, "motion", "parameter.ientity.motion")
			),
			new MetodData(void.class, "setMotionY", "method.ientity.setmotiony",
				new ParameterData(double.class, "motion", "parameter.ientity.motion")
			),
			new MetodData(void.class, "setMotionZ", "method.ientity.setmotionz",
				new ParameterData(double.class, "motion", "parameter.ientity.motion")
			),
			new MetodData(void.class, "setMount", "method.ientity.setmount",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MetodData(void.class, "setName", "method.ientity.setname",
				new ParameterData(String.class, "name", "parameter.ientity.setname")
			),
			new MetodData(void.class, "setPitch", "method.ientity.setpitch",
				new ParameterData(float.class, "pitch", "parameter.ientity.setpitch")
			),
			new MetodData(void.class, "setPos", "method.ientity.setpos",
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MetodData(void.class, "setPosition", "method.ientity.setpos",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz")
			),
			new MetodData(void.class, "setRotation", "method.ientity.setrotation",
				new ParameterData(float.class, "rotation", "parameter.ientity.setrotation")
			),
			new MetodData(void.class, "setX", "method.setx",
				new ParameterData(double.class, "rotation", "parameter.posx")
			),
			new MetodData(void.class, "setY", "method.sety",
				new ParameterData(double.class, "rotation", "parameter.posy")
			),
			new MetodData(void.class, "setZ", "method.setz",
				new ParameterData(double.class, "rotation", "parameter.posz")
			),
			new MetodData(void.class, "spawn", "method.ientity.spawn"),
			new MetodData(void.class, "storeAsClone", "method.ientity.storeasclone",
				new ParameterData(int.class, "tab", "parameter.clone.tab"),
				new ParameterData(String.class, "name", "parameter.clone.file")
			),
			new MetodData(void.class, "typeOf", "method.ientity.typeof",
				new ParameterData(int.class, "type", "parameter.ientity.typeof")
			)
		)
	),
	IEntityItem(new InterfaseData(IEntityItem.class, IEntity.class,
			new Class<?>[] { EntityItemWrapper.class },
			"interfase.ientityitem"
		)
	),
	IEntityLiving(new InterfaseData(IEntityLiving.class, IEntityLivingBase.class,
			new Class<?>[] { EntityLivingWrapper.class },
			"interfase.ientityliving"
		)
	),
	IEntityLivingBase(new InterfaseData(IEntityLivingBase.class, IEntity.class,
			new Class<?>[] { EntityLivingBaseWrapper.class },
			"interfase.ientitylivingbase"
		)
	),
	IMonster(new InterfaseData(IMonster.class, IEntityLiving.class,
			new Class<?>[] { MonsterWrapper.class },
			"interfase.imonster"
		)
	),
	IPixelmon(new InterfaseData(IPixelmon.class, IAnimal.class,
			new Class<?>[] { PixelmonWrapper.class },
			"interfase.ipixelmon"
		)
	),
	IPlayer(new InterfaseData(IPlayer.class, IEntityLivingBase.class,
			new Class<?>[] { PlayerWrapper.class },
			"interfase.iplayer",
			new MetodData(void.class, "trigger", "method.trigger",
				new ParameterData(int.class, "id", "parameter.trigger.id"),
				new ParameterData(Object[].class, "arguments", "parameter.trigger.arguments")
			)
		)
	),
	IProjectile(new InterfaseData(IProjectile.class, IThrowable.class,
			new Class<?>[] { ProjectileWrapper.class },
			"interfase.iprojectile"
		)
	),
	IThrowable(new InterfaseData(IThrowable.class, IEntity.class,
			new Class<?>[] { ThrowableWrapper.class },
			"interfase.ithrowable"
		)
	),
	IVillager(new InterfaseData(IVillager.class, IEntityLiving.class,
			new Class<?>[] { VillagerWrapper.class },
			"interfase.ivillager"
		)
	),
	IButton(new InterfaseData(IButton.class, ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiButtonWrapper.class },
			"interfase.ibutton"
		)
	),
	ICustomGui(new InterfaseData(ICustomGui.class, null,
			new Class<?>[] { CustomGuiWrapper.class },
			"interfase.icustomgui"
		)
	),
	ICustomGuiComponent(new InterfaseData(ICustomGuiComponent.class, null,
			new Class<?>[] { CustomGuiComponentWrapper.class },
			"interfase.icustomguicomponent"
		)
	),
	IGuiTimer(new InterfaseData(IGuiTimer.class, null,
			new Class<?>[] { CustomGuiTimerWrapper.class },
			"interfase.iguitimer"
		)
	),
	IItemSlot(new InterfaseData(IItemSlot.class, ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiItemSlotWrapper.class },
			"interfase.iitemslot"
		)
	),
	ILabel(new InterfaseData(ILabel.class, ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiLabelWrapper.class },
			"interfase.ilabel"
		)
	),
	IOverlayHUD(new InterfaseData(IOverlayHUD.class, null,
			new Class<?>[] { PlayerOverlayHUD.class },
			"interfase.ioverlayhud"
		)
	),
	IScroll(new InterfaseData(IScroll.class, ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiScrollWrapper.class },
			"interfase.iscroll"
		)
	),
	ITextField(new InterfaseData(ITextField.class, ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiTextFieldWrapper.class },
			"interfase.itextfield"
		)
	),
	ITexturedButton(new InterfaseData(ITexturedButton.class, IButton.class,
			new Class<?>[] { CustomGuiButtonWrapper.class },
			"interfase.itexturedbutton"
		)
	),
	ITexturedRect(new InterfaseData(ITexturedRect.class, null,
			new Class<?>[] { CustomGuiTexturedRectWrapper.class },
			"interfase.itexturedrect"
		)
	),
	ICloneHandler(new InterfaseData(ICloneHandler.class, null,
			new Class<?>[] { ServerCloneController.class },
			"interfase.iclonehandler"
		)
	),
	IDataObject(new InterfaseData(IDataObject.class, null,
			new Class<?>[] { DataObject.class },
			"interfase.idataobject"
		)
	),
	IDialogHandler(new InterfaseData(IDialogHandler.class, null,
			new Class<?>[] { DialogController.class },
			"interfase.idialoghandler"
		)
	),
	IFactionHandler(new InterfaseData(IFactionHandler.class, null,
			new Class<?>[] { FactionController.class },
			"interfase.ifactionhandler"
		)
	),
	IQuestHandler(new InterfaseData(IQuestHandler.class, null,
			new Class<?>[] { QuestController.class },
			"interfase.iquesthandler"
		)
	),
	IRecipeHandler(new InterfaseData(IRecipeHandler.class, null,
			new Class<?>[] { RecipeController.class },
			"interfase.irecipehandler"
		)
	),
	IAvailability(new InterfaseData(IAvailability.class, null,
			new Class<?>[] { Availability.class },
			"interfase.iavailability"
		)
	),
	IDataElement(new InterfaseData(IDataElement.class, null,
			new Class<?>[] { DataElement.class },
			"interfase.idatablement"
		)
	),
	IDialog(new InterfaseData(IDialog.class, null,
			new Class<?>[] { Dialog.class },
			"interfase.idialog"
		)
	),
	IDialogCategory(new InterfaseData(IDialogCategory.class, null,
			new Class<?>[] { DialogCategory.class },
			"interfase.idialogcategory"
		)
	),
	IDialogOption(new InterfaseData(IDialogOption.class, null,
			new Class<?>[] { DialogOption.class },
			"interfase.idialogoption"
		)
	),
	IFaction(new InterfaseData(IFaction.class, null,
			new Class<?>[] { Faction.class },
			"interfase.ifaction"
		)
	),
	INpcRecipe(new InterfaseData(INpcRecipe.class, null,
			new Class<?>[] { NpcShapedRecipes.class, NpcShapelessRecipes.class },
			"interfase.inpcrecipe"
		)
	),
	IQuest(new InterfaseData(IQuest.class, null,
			new Class<?>[] { Quest.class },
			"interfase.iquest"
		)
	),
	IQuestCategory(new InterfaseData(IQuestCategory.class, null,
			new Class<?>[] { QuestCategory.class },
			"interfase.iquestcategory"
		)
	),
	IQuestObjective(new InterfaseData(IQuestObjective.class, null,
			new Class<?>[] { QuestObjective.class },
			"interfase.iquestobjective"
		)
	),
	IScriptData(new InterfaseData(IScriptData.class, null,
			new Class<?>[] { ScriptData.class },
			"interfase.iscriptdata"
		)
	),
	IItemArmor(new InterfaseData(IItemArmor.class, IItemStack.class,
			new Class<?>[] { ItemArmorWrapper.class },
			"interfase.iitemarmor"
		)
	),
	IItemBlock(new InterfaseData(IItemBlock.class, IItemStack.class,
			new Class<?>[] { ItemBlockWrapper.class },
			"interfase.iitemblock"
		)
	),
	IItemBook(new InterfaseData(IItemBook.class, IItemStack.class,
			new Class<?>[] { ItemBookWrapper.class },
			"interfase.iitembook"
		)
	),
	IItemScripted(new InterfaseData(IItemScripted.class, IItemStack.class,
			new Class<?>[] { ItemScriptedWrapper.class },
			"interfase.iitemscripted"
		)
	),
	IItemStack(new InterfaseData(IItemStack.class, null,
			new Class<?>[] { ItemStackWrapper.class },
			"interfase.iitemstack",
			new MetodData(void.class, "addEnchantment", "method.iitemstack.addenchantment",
				new ParameterData(String.class, "name", "parameter.ench.name"),
				new ParameterData(String.class, "level", "parameter.ench.lv")
			),
			new MetodData(void.class, "addEnchantment", "method.iitemstack.addenchantment",
				new ParameterData(int.class, "id", "parameter.ench.id"),
				new ParameterData(String.class, "level", "parameter.ench.lv")
			),
			new MetodData(boolean.class, "compare", "method.iitemstack.compare",
				new ParameterData(int.class, "item", "parameter.stack"),
				new ParameterData(boolean.class, "ignoreNBT", "parameter.ignorenbt")
			),
			new MetodData(IItemStack.class, "copy", "method.iitemstack.copy"),
			new MetodData(void.class, "damageItem", "method.iitemstack.damageitem",
				new ParameterData(int.class, "damage", "parameter.itemmeta"),
				new ParameterData(IEntityLiving.class, "living", "parameter.entity")
			),
			new MetodData(double.class, "getAttackDamage", "method.iitemstack.getattackdamage"),
			new MetodData(double.class, "getAttribute", "method.iitemstack.getattribute",
				new ParameterData(String.class, "name", "parameter.attr.name")
			),
			new MetodData(String.class, "getDisplayName", "method.iitemstack.getdisplayname"),
			new MetodData(int.class, "getFoodLevel", "method.iitemstack.getfoodlevel"),
			new MetodData(int.class, "getItemDamage", "method.iitemstack.getitemdamage"),
			new MetodData(String.class, "getItemName", "method.iitemstack.getitemname"),
			new MetodData(INbt.class, "getItemNbt", "method.iitemstack.getitemnbt"),
			new MetodData(String[].class, "getLore", "method.iitemstack.getlore"),
			new MetodData(int.class, "getMaxItemDamage", "method.iitemstack.getmaxitemdamage"),
			new MetodData(int.class, "getMaxStackSize", "method.iitemstack.getmaxstacksize"),
			new MetodData(ItemStack.class, "getMCItemStack", "method.iitemstack.getmcitemstack"),
			new MetodData(String.class, "getName", "method.iitemstack.getname"),
			new MetodData(INbt.class, "getNbt", "method.iitemstack.getnbt"),
			new MetodData(int.class, "getStackSize", "method.iitemstack.getstacksize"),
			new MetodData(IData.class, "getStoreddata", "method.getstoreddata"),
			new MetodData(IData.class, "getTempdata", "method.gettempdata"),
			new MetodData(int.class, "getType", "method.iitemstack.gettype"),
			new MetodData(boolean.class, "hasAttribute", "method.iitemstack.hasattribute",
				new ParameterData(String.class, "name", "parameter.attr.name")
			),
			new MetodData(boolean.class, "hasCustomName", "method.iitemstack.hascustomName"),
			new MetodData(boolean.class, "hasEnchant", "method.iitemstack.hasenchant",
				new ParameterData(String.class, "name", "parameter.ench.name")
			),
			new MetodData(boolean.class, "hasEnchant", "method.iitemstack.hasenchant",
				new ParameterData(int.class, "id", "parameter.ench.id")
			),
			new MetodData(boolean.class, "hasNbt", "method.iitemstack.hasnbt"),
			new MetodData(boolean.class, "isBlock", "method.iitemstack.isblock").setDeprecated(),
			new MetodData(boolean.class, "isBook", "method.iitemstack.isbook").setDeprecated(),
			new MetodData(boolean.class, "isEmpty", "method.iitemstack.isempty"),
			new MetodData(boolean.class, "isEnchanted", "method.iitemstack.isenchanted"),
			new MetodData(boolean.class, "isWearable", "method.iitemstack.iswearable"),
			new MetodData(boolean.class, "removeEnchant", "method.iitemstack.removeenchant",
				new ParameterData(String.class, "name", "parameter.ench.name")
			),
			new MetodData(boolean.class, "removeEnchant", "method.iitemstack.removeenchant",
				new ParameterData(int.class, "id", "parameter.ench.id")
			),
			new MetodData(void.class, "removeNbt", "method.iitemstack.removenbt"),
			new MetodData(void.class, "setAttribute", "method.iitemstack.setattribute",
				new ParameterData(String.class, "name", "parameter.attr.name"),
				new ParameterData(double.class, "value", "parameter.attr.value")
			).setDeprecated(),
			new MetodData(void.class, "setAttribute", "method.iitemstack.setattribute",
				new ParameterData(String.class, "name", "parameter.attr.name"),
				new ParameterData(double.class, "value", "parameter.attr.value"),
				new ParameterData(int.class, "value", "parameter.ceil.slot")
			),
			new MetodData(void.class, "setCustomName", "method.iitemstack.setcustomname",
				new ParameterData(String.class, "name", "parameter.iitemstack.name")
			),
			new MetodData(void.class, "setItemDamage", "method.iitemstack.setitemdamage",
				new ParameterData(int.class, "value", "parameter.itemmeta")
			),
			new MetodData(void.class, "setLore", "method.iitemstack.setlore",
				new ParameterData(String[].class, "lore", "parameter.iitemstack.lore")
			),
			new MetodData(void.class, "setStackSize", "method.iitemstack.setstacksize",
				new ParameterData(int.class, "size", "parameter.itemcount")
			)
		)
	),
	NpcAPI(new InterfaseData(NpcAPI.class, null,
			new Class<?>[] { WrapperNpcAPI.class },
			"interfase.npcapi",
			new MetodData(NpcAPI.class, "Instance", "method.npcapi.instance"),
			new MetodData(ICustomGui.class, "createCustomGui", "method.npcapi.createcustomgui",
				new ParameterData(int.class, "id", "parameter.customgui.id"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(boolean.class, "pauseGame", "parameter.customgui.pause")
			),
			new MetodData(IPlayerMail.class, "createMail", "method.npcapi.createmail",
				new ParameterData(String.class, "sender", "parameter.mail.subject"),
				new ParameterData(String.class, "subject", "parameter.mail.sender")
			),
			new MetodData(ICustomNpc.class, "createNPC", "method.npcapi.createnpc",
				new ParameterData(World.class, "world", "parameter.world")
			),
			new MetodData(EventBus.class, "events", "method.npcapi.events"),
			new MetodData(String.class, "executeCommand", "method.executecommand",
				new ParameterData(IWorld.class, "world", "parameter.world"),
				new ParameterData(String.class, "command", "parameter.command")
			),
			new MetodData(ICloneHandler.class, "getClones", "method.npcapi.getclones"),
			new MetodData(IDialogHandler.class, "getDialogs", "method.npcapi.getdialogs"),
			new MetodData(IFactionHandler.class, "getFactions", "method.npcapi.getfactions"),
			new MetodData(File.class, "getGlobalDir", "method.npcapi.getglobaldir"),
			new MetodData(IBlock.class, "getIBlock", "method.npcapi.getiblock",
				new ParameterData(World.class, "world", "parameter.world"),
				new ParameterData(BlockPos.class, "pos", "parameter.pos")
			),
			new MetodData(IContainer.class, "getIContainer", "method.npcapi.geticontainer",
				new ParameterData(Container.class, "container", "parameter.container")
			),
			new MetodData(IContainer.class, "getIContainer", "method.npcapi.geticontainer",
				new ParameterData(IInventory.class, "inventory", "parameter.inventory")
			),
			new MetodData(IDamageSource.class, "getIDamageSource", "method.npcapi.getidamageSource",
				new ParameterData(DamageSource.class, "source", "parameter.damagesource")
			),
			new MetodData(IEntityDamageSource.class, "getIDamageSource", "method.npcapi.getientitydamageSource",
				new ParameterData(String.class, "name", "parameter.damagesource.name"),
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MetodData(IEntity.class, "getIEntity", "method.npcapi.getientity",
				new ParameterData(Entity.class, "entity", "parameter.entity")
			),
			new MetodData(IItemStack.class, "getIItemStack", "method.npcapi.getiitemstack",
				new ParameterData(ItemStack.class, "stack", "parameter.stack")
			),
			new MetodData(INbt.class, "getINbt", "method.npcapi.getinbbt",
				new ParameterData(NBTTagCompound.class, "nbt", "parameter.nbt")
			),
			new MetodData(IPos.class, "getIPos", "method.npcapi.getipos",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz")
			),
			new MetodData(IWorld.class, "getIWorld", "method.npcapi.getiworldid",
				new ParameterData(int.class, "dimensionId", "parameter.dimensionId")
			),
			new MetodData(IWorld.class, "getIWorld", "method.npcapi.getiworld",
				new ParameterData(WorldServer.class, "world", "parameter.world")
			),
			new MetodData(IWorld[].class, "getIWorlds", "method.npcapi.getiworlds"),
			new MetodData(IQuestHandler.class, "getRandomName", "method.npcapi.getrandomname"),
			new MetodData(String.class, "getIWorld", "method.npcapi.getiworld",
				new ParameterData(int.class, "dictionary", "parameter.npcapi.dictionary"),
				new ParameterData(int.class, "gender", "parameter.npcapi.gender")
			),
			new MetodData(INbt.class, "getRawPlayerData", "method.npcapi.getrawplayerdata",
				new ParameterData(String.class, "uuid", "parameter.entityuuid")
			),
			new MetodData(IRecipeHandler.class, "getRecipes", "method.npcapi.getrecipes"),
			new MetodData(File.class, "getWorldDir", "method.npcapi.getworlddir"),
			new MetodData(boolean.class, "hasPermissionNode", "method.npcapi.haspermissionnode",
				new ParameterData(String.class, "permission", "parameter.npcapi.permission")
			),
			new MetodData(void.class, "registerCommand", "method.npcapi.registercommand",
				new ParameterData(CommandNoppesBase.class, "command", "parameter.npcapi.command")
			),
			new MetodData(void.class, "registerPermissionNode", "method.npcapi.registerpermissionnode",
				new ParameterData(String.class, "permission", "parameter.npcapi.permission"),
				new ParameterData(int.class, "defaultType", "parameter.npcapi.defaultType")
			),
			new MetodData(ICustomNpc.class, "spawnNPC", "method.npcapi.spawnnpc",
					new ParameterData(WorldServer.class, "world", "parameter.world"),
					new ParameterData(int.class, "x", "parameter.posx"),
					new ParameterData(int.class, "y", "parameter.posy"),
					new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(INbt.class, "stringToNbt", "method.npcapi.stringtonbt",
				new ParameterData(String.class, "str", "parameter.npcapi.nbtstr")
			)
		)
	),
	IDimensionHandler(new InterfaseData(IDimensionHandler.class, null,
			new Class<?>[] { DimensionHandler.class },
			"interfase.idimensionhandler",
			new MetodData(IWorldInfo.class, "createDimension", "method.idimensionhandler.create"),
			new MetodData(void.class, "setNbt", "method.idimensionhandler.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MetodData(INbt.class, "getNbt", "method.idimensionhandler.getnbt"),
			new MetodData(IWorldInfo.class, "getMCWorldInfo", "method.idimensionhandler.getmcworldinfo",
				new ParameterData(int.class, "dimensionID", "parameter.dimensionId")
			),
			new MetodData(int[].class, "getAllIDs", "method.idimensionhandler.getallids"),
			new MetodData(void.class, "deleteDimension", "method.idimensionhandler.delete",
				new ParameterData(int.class, "dimensionID", "parameter.dimensionId")
			)
		)
	),
	IWorldInfo(new InterfaseData(IWorldInfo.class, null,
			new Class<?>[] { CustomWorldInfo.class },
			"interfase.iworldinfo",
			new MetodData(int.class, "getID", "method.iworldinfo.getid"),
			new MetodData(INbt.class, "getNbt", "method.iworldinfo.getnbt"),
			new MetodData(void.class, "setNbt", "method.iworldinfo.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			)
		)
	);
	
	
	public InterfaseData it;
	
	EnumInterfaceData(InterfaseData interfaseData) { this.it = interfaseData; }

	public static InterfaseData get(String enumName) {
		for (EnumInterfaceData enumIT : EnumInterfaceData.values()) { 
			if (enumIT.name().equals(enumName)) { return enumIT.it;}
		}
		return null;
	}
	
}
