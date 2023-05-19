package noppes.npcs.constants;

import java.awt.Point;
import java.io.File;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantRecipeList;
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
import noppes.npcs.api.ILayerModel;
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
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.entity.data.IAnimationFrame;
import noppes.npcs.api.entity.data.IAnimationPart;
import noppes.npcs.api.entity.data.IAttributeSet;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.entity.data.IDropNbtSet;
import noppes.npcs.api.entity.data.IEnchantSet;
import noppes.npcs.api.entity.data.ILine;
import noppes.npcs.api.entity.data.IMark;
import noppes.npcs.api.entity.data.INPCAdvanced;
import noppes.npcs.api.entity.data.INPCAi;
import noppes.npcs.api.entity.data.INPCAnimation;
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
import noppes.npcs.api.entity.data.role.IJobSpawner;
import noppes.npcs.api.entity.data.role.IRoleDialog;
import noppes.npcs.api.entity.data.role.IRoleFollower;
import noppes.npcs.api.entity.data.role.IRoleTrader;
import noppes.npcs.api.entity.data.role.IRoleTransporter;
import noppes.npcs.api.entity.data.role.IRoleTransporter.ITransportLocation;
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
import noppes.npcs.api.handler.IAnimationHandler;
import noppes.npcs.api.handler.IBorderHandler;
import noppes.npcs.api.handler.ICloneHandler;
import noppes.npcs.api.handler.IDataObject;
import noppes.npcs.api.handler.IDialogHandler;
import noppes.npcs.api.handler.IDimensionHandler;
import noppes.npcs.api.handler.IFactionHandler;
import noppes.npcs.api.handler.IQuestHandler;
import noppes.npcs.api.handler.IRecipeHandler;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.api.handler.data.IBorder;
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
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig.PartConfig;
import noppes.npcs.client.util.InterfaseData;
import noppes.npcs.client.util.MetodData;
import noppes.npcs.client.util.ParameterData;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.BorderController;
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
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.controllers.data.Zone3D;
import noppes.npcs.dimensions.CustomWorldInfo;
import noppes.npcs.dimensions.DimensionHandler;
import noppes.npcs.entity.data.AttributeSet;
import noppes.npcs.entity.data.DataAI;
import noppes.npcs.entity.data.DataAdvanced;
import noppes.npcs.entity.data.DataAnimation;
import noppes.npcs.entity.data.DataDisplay;
import noppes.npcs.entity.data.DataInventory;
import noppes.npcs.entity.data.DataMelee;
import noppes.npcs.entity.data.DataRanged;
import noppes.npcs.entity.data.DataStats;
import noppes.npcs.entity.data.DataTimers;
import noppes.npcs.entity.data.DropNbtSet;
import noppes.npcs.entity.data.DropSet;
import noppes.npcs.entity.data.EnchantSet;
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
import noppes.npcs.roles.JobSpawner;
import noppes.npcs.roles.RoleBank;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleDialog;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.roles.RolePostman;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.roles.RoleTransporter;
import noppes.npcs.util.LayerModel;
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
			),
			new MetodData(ILayerModel[].class, "getLayerModels", "method.iblockscripted.getlayermodels"),
			new MetodData(ILayerModel.class, "createLayerModel", "method.iblockscripted.createlayermodel"),
			new MetodData(void.class, "updateModel", "method.iblockscripted.updatemodel")
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
			),
			new MetodData(IEntity[].class, "getEntitys", "method.iworld.getentitys",
				new ParameterData(int.class, "type", "parameter.entitytype" )
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
	
	// need tr
	ILayerModel(new InterfaseData(ILayerModel.class, null,
			new Class<?>[] { LayerModel.class },
			"interfase.ilayermodel",
			new MetodData(int.class, "getPos", "method.ilayermodel.getpos"),
			new MetodData(NBTTagCompound.class, "getNbt", "method.ilayermodel.getnbt"),
			new MetodData(void.class, "setNbt", "method.ilayermodel.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MetodData(float.class, "getOffset", "method.ilayermodel.getoffset",
				new ParameterData(int.class, "axis", "parameter.axis")
			),
			new MetodData(void.class, "setOffset", "method.ilayermodel.setoffset",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(IItemStack.class, "getModel", "method.ilayermodel.getmodel"),
			new MetodData(void.class, "setModel", "method.ilayermodel.getoffset",
				new ParameterData(IItemStack.class, "stack", "parameter.stack")
			),
			new MetodData(String.class, "getOBJModel", "method.ilayermodel.getobjmodel"),
			new MetodData(void.class, "setOBJModel", "method.ilayermodel.setoffset",
				new ParameterData(String.class, "path", "parameter.resource")
			),
			new MetodData(float.class, "getRotate", "method.ilayermodel.getrotate",
				new ParameterData(int.class, "axis", "parameter.axis")
			),
			new MetodData(void.class, "setRotate", "method.ilayermodel.setrotate",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(boolean.class, "isRotate", "method.ilayermodel.isrotate",
				new ParameterData(int.class, "axis", "parameter.axis")
			),
			new MetodData(void.class, "setIsRotate", "method.ilayermodel.setisrotate",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(boolean.class, "getScale", "method.ilayermodel.getscale",
				new ParameterData(int.class, "axis", "parameter.axis")
			),
			new MetodData(void.class, "setScale", "method.ilayermodel.setscale",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(int.class, "getRotateSpeed", "method.ilayermodel.getrotatespeed"),
			new MetodData(boolean.class, "setRotateSpeed", "method.ilayermodel.setRotateSpeed",
				new ParameterData(int.class, "speed", "parameter.speed")
			)
		)
	),
	IRoleTrader(new InterfaseData(IRoleTrader.class, INPCRole.class,
			new Class<?>[] { RoleTrader.class },
			"interfase.iroletrader",
			new MetodData(IItemStack.class, "getCurrency", "method.iroletrader.getcurrency",
				new ParameterData(int.class, "position", "parameter.position"),
				new ParameterData(int.class, "slot", "parameter.slot")
			),
			new MetodData(String.class, "getName", "method.ilayermodel.getname"),
			new MetodData(IItemStack.class, "getProduct", "method.iroletrader.getproduct",
				new ParameterData(int.class, "position", "parameter.position")
			),
			new MetodData(void.class, "remove", "method.iroletrader.remove",
				new ParameterData(int.class, "position", "parameter.position")
			),
			new MetodData(void.class, "set", "method.iroletrader.set",
				new ParameterData(int.class, "position", "parameter.position"),
				new ParameterData(IItemStack.class, "product", "parameter.iroletrader.product"),
				new ParameterData(IItemStack[].class, "currencys", "parameter.iroletrader.currencys")
			),
			new MetodData(void.class, "setName", "method.iroletrader.setname",
				new ParameterData(String.class, "name", "parameter.name")
			)
		)
	),
	IRoleTransporter(new InterfaseData(IRoleTransporter.class, INPCRole.class,
			new Class<?>[] { RoleTransporter.class },
			"interfase.iroletransporter",
			new MetodData(ITransportLocation.class, "getLocation", "method.iroletransporter.getlocation")
		)
	),
	ITransportLocation(new InterfaseData(ITransportLocation.class, null,
			new Class<?>[] { TransportLocation.class },
			"interfase.itransportlocation",
			new MetodData(int.class, "getDimension", "method.itransportlocation.getdimension"),
			new MetodData(int.class, "getId", "method.itransportlocation.getid"),
			new MetodData(String.class, "getName", "method.itransportlocation.getname"),
			new MetodData(int.class, "getType", "method.itransportlocation.gettype"),
			new MetodData(int.class, "getX", "method.getx"),
			new MetodData(int.class, "getY", "method.gety"),
			new MetodData(int.class, "getZ", "method.getz"),
			new MetodData(void.class, "setPos", "method.itransportlocation.setÇos",
				new ParameterData(int.class, "dimentionID", "parameter.dimensionid"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			)
		)
	),
	IAttributeSet(new InterfaseData(IAttributeSet.class, null,
			new Class<?>[] { AttributeSet.class },
			"interfase.iattributeset",
			new MetodData(String.class, "getAttribute", "method.iattributeset.getattribute"),
			new MetodData(double.class, "getChance", "method.iattributeset.getchance"),
			new MetodData(double.class, "getMaxValue", "method.iattributeset.getmaxvalue"),
			new MetodData(double.class, "getMinValue", "method.iattributeset.getminvalue"),
			new MetodData(int.class, "getSlot", "method.iattributeset.getslot"),
			new MetodData(void.class, "remove", "method.iattributeset.remove"),
			new MetodData(void.class, "setAttribute", "method.iattributeset.setattribute",
				new ParameterData(IAttribute.class, "attribute", "parameter.attribute")
			),
			new MetodData(void.class, "setAttribute", "method.iattributeset.setattribute",
				new ParameterData(String.class, "name", "parameter.attribute.name")
			),
			new MetodData(void.class, "setChance", "method.iattributeset.setchance",
				new ParameterData(double.class, "chance", "drop.chance")
			),
			new MetodData(void.class, "setSlot", "method.iattributeset.setslot",
				new ParameterData(int.class, "slot", "parameter.slot")
			),
			new MetodData(void.class, "setAttribute", "method.iattributeset.setattribute",
				new ParameterData(double.class, "min", "parameter.min"),
				new ParameterData(double.class, "max", "parameter.max")
			)
		)
	),
	ICustomDrop(new InterfaseData(ICustomDrop.class, null,
			new Class<?>[] {  DropSet.class },
			"interfase.icustomdrop",
			new MetodData(IAttributeSet.class, "addAttribute", "method.icustomdrop.addattribute",
				new ParameterData(String.class, "attributeName", "parameter.attribute.name")
			),
			new MetodData(IDropNbtSet.class, "addDropNbtSet", "method.icustomdrop.adddropnbtset",
				new ParameterData(int.class, "type", "parameter.idropnbtset.type"),
				new ParameterData(double.class, "type", "parameter.chance"),
				new ParameterData(String.class, "type", "parameter.idropnbtset.paht"),
				new ParameterData(String[].class, "type", "parameter.idropnbtset.values")
			),
			new MetodData(IEnchantSet.class, "addEnchant", "method.icustomdrop.addenchant",
				new ParameterData(int.class, "enchantId", "parameter.enchant.id")
			),
			new MetodData(IEnchantSet.class, "addEnchant", "method.icustomdrop.addenchant",
				new ParameterData(String.class, "enchantName", "parameter.enchant.name")
			),
			new MetodData(IItemStack.class, "createLoot", "method.icustomdrop.createloot",
				new ParameterData(double.class, "addChance", "parameter.icustomdrop.addchance")
			),
			new MetodData(IAttributeSet[].class, "getAttributeSets", "method.icustomdrop.getattributesets"),
			new MetodData(double.class, "getChance", "method.icustomdrop.getchance"),
			new MetodData(float.class, "getDamage", "method.icustomdrop.getdamage"),
			new MetodData(IDropNbtSet[].class, "getDropNbtSets", "method.icustomdrop.getdropnbtsets"),
			new MetodData(IEnchantSet[].class, "getEnchantSets", "method.icustomdrop.getenchantsets"),
			new MetodData(IItemStack.class, "getItem", "method.icustomdrop.getItem"),
			new MetodData(boolean.class, "getLootMode", "method.icustomdrop.getlootmode"),
			new MetodData(int.class, "getMaxAmount", "method.icustomdrop.getmaxamount"),
			new MetodData(int.class, "getMaxAmount", "method.icustomdrop.getmaxamount"),
			new MetodData(int.class, "getQuestID", "method.icustomdrop.getquestid"),
			new MetodData(boolean.class, "getTiedToLevel", "method.icustomdrop.gettiedtolevel"),
			new MetodData(void.class, "remove", "method.icustomdrop.remove"),
			new MetodData(void.class, "removeAttribute", "method.icustomdrop.removeattribute",
				new ParameterData(IAttributeSet.class, "attribute", "parameter.icustomdrop.attribute")
			),
			new MetodData(void.class, "removeDropNbt", "method.icustomdrop.removedropnbt",
				new ParameterData(IDropNbtSet.class, "nbt", "parameter.icustomdrop.nbt")
			),
			new MetodData(void.class, "removeAttribute", "method.icustomdrop.removeattribute",
				new ParameterData(IEnchantSet.class, "enchant", "parameter.icustomdrop.enchant")
			),
			new MetodData(void.class, "resetTo", "method.icustomdrop.resetto",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MetodData(void.class, "setAmount", "method.icustomdrop.setamount",
				new ParameterData(int.class, "min", "parameter.min"),
				new ParameterData(int.class, "max", "parameter.max")
			),
			new MetodData(void.class, "setChance", "method.icustomdrop.setchance",
				new ParameterData(double.class, "chance", "drop.chance")
			),
			new MetodData(void.class, "setDamage", "method.icustomdrop.setdamage",
				new ParameterData(float.class, "dam", "drop.icustomdrop.dam")
			),
			new MetodData(void.class, "setItem", "method.icustomdrop.setitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MetodData(void.class, "setLootMode", "method.icustomdrop.setlootmode",
				new ParameterData(boolean.class, "lootMode", "parameter.icustomdrop.lootMode")
			),
			new MetodData(void.class, "setQuestID", "method.icustomdrop.setquestid",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(void.class, "setTiedToLevel", "method.icustomdrop.settiedtolevel",
				new ParameterData(boolean.class, "tiedToLevel", "parameter.icustomdrop.tiedToLevel")
			)
		)
	),
	IData(new InterfaseData(IData.class, null, null,
			"interfase.idata",
			new MetodData(void.class, "clear", "method.idata.clear"),
			new MetodData(Object.class, "get", "method.idata.get",
				new ParameterData(String.class, "key", "parameter.key")
			),
			new MetodData(String[].class, "getKeys", "method.idata.getkeys"),
			new MetodData(boolean.class, "has", "method.idata.has",
				new ParameterData(String.class, "key", "parameter.key")
			),
			new MetodData(void.class, "put", "method.idata.put",
				new ParameterData(String.class, "key", "parameter.key"),
				new ParameterData(Object.class, "value", "parameter.value")
			),
			new MetodData(void.class, "remove", "method.idata.remove",
				new ParameterData(String.class, "key", "parameter.key")
			)
		)
	),
	IDropNbtSet(new InterfaseData(IDropNbtSet.class, null,
			new Class<?>[] { DropNbtSet.class },
			"interfase.idropnbtset",
			new MetodData(double.class, "getChance", "method.idropnbtset.getchance"),
			new MetodData(INbt.class, "getConstructoredTag", "method.idropnbtset.getconstructoredtag",
				new ParameterData(INbt.class, "nbt", "parameter.idropnbtset.nbt")
			),
			new MetodData(String.class, "getPath", "method.idropnbtset.getpath"),
			new MetodData(int.class, "getType", "method.idropnbtset.gettype"),
			new MetodData(int.class, "getTypeList", "method.idropnbtset.gettypelist"),
			new MetodData(String[].class, "getValues", "method.idropnbtset.getvalues"),
			new MetodData(void.class, "remove", "method.idropnbtset.remove"),
			new MetodData(void.class, "setChance", "method.idropnbtset.setchance",
				new ParameterData(double.class, "chance", "parameter.idropnbtset.chance")
			),
			new MetodData(void.class, "setPath", "method.idropnbtset.setpath",
				new ParameterData(String.class, "path", "parameter.idropnbtset.path")
			),
			new MetodData(void.class, "setType", "method.idropnbtset.settype",
				new ParameterData(int.class, "type", "parameter.idropnbtset.type")
			),
			new MetodData(void.class, "setTypeList", "method.idropnbtset.settypelist",
				new ParameterData(int.class, "type", "parameter.idropnbtset.typelist")
			),
			new MetodData(void.class, "setValues", "method.idropnbtset.setvalues",
				new ParameterData(String.class, "values", "parameter.idropnbtset.values.0")
			),
			new MetodData(void.class, "setValues", "method.idropnbtset.setvalues",
				new ParameterData(String[].class, "values", "parameter.idropnbtset.values.1")
			)
		)
	),
	IEnchantSet(new InterfaseData(IEnchantSet.class, null,
			new Class<?>[] { EnchantSet.class },
			"interfase.ienchantset",
			new MetodData(double.class, "getChance", "method.ienchantset.getchance"),
			new MetodData(String.class, "getEnchant", "method.ienchantset.getenchant"),
			new MetodData(int.class, "getMaxLevel", "method.ienchantset.getmaxlevel"),
			new MetodData(int.class, "getMinLevel", "method.ienchantset.getminlevel"),
			new MetodData(void.class, "remove", "method.ienchantset.remove"),
			new MetodData(void.class, "setChance", "method.ienchantset.setchance",
				new ParameterData(double.class, "chance", "parameter.ienchantset.chance")
			),
			new MetodData(void.class, "setEnchant", "method.idropnbtset.setenchant.0",
				new ParameterData(Enchantment.class, "enchant", "parameter.enchant")
			),
			new MetodData(boolean.class, "setEnchant", "method.idropnbtset.setenchant.1",
				new ParameterData(int.class, "id", "parameter.enchant.id")
			),
			new MetodData(boolean.class, "setEnchant", "method.idropnbtset.setenchant.1",
				new ParameterData(String.class, "name", "parameter.enchant.name")
			),
			new MetodData(void.class, "setLevels", "method.idropnbtset.setlevels",
				new ParameterData(int.class, "min", "parameter.min"),
				new ParameterData(int.class, "max", "parameter.max")
			)
		)
	),
	ILine(new InterfaseData(ILine.class, null,
			new Class<?>[] { noppes.npcs.controllers.data.Line.class },
			"interfase.iline",
			new MetodData(boolean.class, "getShowText", "method.iline.getshowtext"),
			new MetodData(String.class, "getSound", "method.iline.getsound"),
			new MetodData(String.class, "getText", "method.iline.gettext"),
			new MetodData(void.class, "setShowText", "method.iline.setshowtext",
				new ParameterData(boolean.class, "show", "parameter.iline.show")
			),
			new MetodData(void.class, "setSound", "method.iline.setsound",
				new ParameterData(String.class, "sound", "parameter.iline.sound")
			),
			new MetodData(void.class, "setText", "method.iline.settext",
				new ParameterData(String.class, "text", "parameter.iline.text")
			)
		)
	),
	IMark(new InterfaseData(IMark.class, null,
			new Class<?>[] { MarkData.Mark.class },
			"interfase.imark",
			new MetodData(IAvailability.class, "getAvailability", "method.getavailability"),
			new MetodData(int.class, "getColor", "method.imark.getcolor"),
			new MetodData(int.class, "getType", "method.imark.gettype"),
			new MetodData(boolean.class, "isRotate", "method.imark.isrotate"),
			new MetodData(void.class, "setColor", "method.imark.setcolor",
				new ParameterData(int.class, "color", "parameter.imark.color")
			),
			new MetodData(void.class, "setRotate", "method.imark.setrotate",
				new ParameterData(boolean.class, "rotate", "parameter.imark.rotate")
			),
			new MetodData(void.class, "setType", "method.imark.settype",
				new ParameterData(int.class, "type", "parameter.imark.type")
			),
			new MetodData(void.class, "update", "method.imark.update")
		)
	),
	INPCAdvanced(new InterfaseData(INPCAdvanced.class, null,
			new Class<?>[] { DataAdvanced.class },
			"interfase.inpcadvanced",
			new MetodData(String.class, "getLine", "method.inpcadvanced.settype",
				new ParameterData(int.class, "type", "parameter.inpcadvanced.type"),
				new ParameterData(int.class, "slot", "parameter.inpcadvanced.slot")
			),
			new MetodData(int.class, "getLineCount", "method.inpcadvanced.getlinecount",
				new ParameterData(int.class, "type", "parameter.inpcadvanced.type")
			),
			new MetodData(String.class, "getSound", "method.inpcadvanced.getsound",
				new ParameterData(int.class, "type", "parameter.inpcadvanced.type")
			),
			new MetodData(void.class, "setLine", "method.inpcadvanced.setline",
				new ParameterData(int.class, "type", "parameter.inpcadvanced.type"),
				new ParameterData(int.class, "slot", "parameter.inpcadvanced.slot"),
				new ParameterData(String.class, "text", "parameter.inpcadvanced.text"),
				new ParameterData(String.class, "sound", "parameter.inpcadvanced.sound")
			),
			new MetodData(void.class, "setSound", "method.inpcadvanced.setsound",
				new ParameterData(int.class, "type", "parameter.inpcadvanced.type"),
				new ParameterData(String.class, "sound", "parameter.inpcadvanced.sound")
			)
		)
	),
	INPCAi(new InterfaseData(INPCAi.class, null,
			new Class<?>[] { DataAI.class },
			"interfase.inpcai",
			new MetodData(int.class, "getAnimation", "method.inpcai.getanimation"),
			new MetodData(boolean.class, "getAttackInvisible", "method.inpcai.getattackinvisible"),
			new MetodData(boolean.class, "getAttackLOS", "method.inpcai.getattacklos"),
			new MetodData(boolean.class, "getAvoidsWater", "method.inpcai.getavoidswater"),
			new MetodData(boolean.class, "getCanSwim", "method.inpcai.getcanswim"),
			new MetodData(int.class, "getCurrentAnimation", "method.inpcai.getcurrentAnimation"),
			new MetodData(int.class, "getDoorInteract", "method.inpcai.getdoorinteract"),
			new MetodData(boolean.class, "getInteractWithNPCs", "method.inpcai.getinteractwithnpcs"),
			new MetodData(boolean.class, "getLeapAtTarget", "method.inpcai.getleapattarget"),
			new MetodData(boolean.class, "getMovingPathPauses", "method.inpcai.getmovingpathpauses"),
			new MetodData(int.class, "getMovingPathType", "method.inpcai.getmovingpathtype"),
			new MetodData(int.class, "getMovingType", "method.inpcai.getmovingtype"),
			new MetodData(int.class, "getNavigationType", "method.inpcai.getnavigationtype"),
			new MetodData(int.class, "getRetaliateType", "method.inpcai.getretaliatetype"),
			new MetodData(boolean.class, "getReturnsHome", "method.inpcai.getreturnshome"),
			new MetodData(int.class, "getSheltersFrom", "method.inpcai.getsheltersfrom"),
			new MetodData(int.class, "getStandingType", "method.inpcai.getstandingtype"),
			new MetodData(boolean.class, "getStopOnInteract", "method.inpcai.getstoponinteract"),
			new MetodData(int.class, "getTacticalRange", "method.inpcai.gettacticalrange"),
			new MetodData(int.class, "getTacticalType", "method.inpcai.gettacticaltype"),
			new MetodData(int.class, "getWalkingSpeed", "method.inpcai.getwalkingspeed"),
			new MetodData(int.class, "getWanderingRange", "method.inpcai.getwanderingrange"),
			new MetodData(void.class, "setAnimation", "method.inpcai.setanimation",
				new ParameterData(int.class, "type", "parameter.inpcai.animationtype")
			),
			new MetodData(void.class, "setAttackInvisible", "method.inpcai.setattackinvisible",
				new ParameterData(boolean.class, "attack", "parameter.inpcai.invattack")
			),
			new MetodData(void.class, "setAttackLOS", "method.inpcai.setattacklos",
				new ParameterData(boolean.class, "enabled", "parameter.enabled")
			),
			new MetodData(void.class, "setAttackInvisible", "method.inpcai.setavoidswater",
				new ParameterData(boolean.class, "enabled", "parameter.enabled")
			),
			new MetodData(void.class, "setCanSwim", "method.inpcai.setcanswim",
				new ParameterData(boolean.class, "canSwim", "parameter.boolean")
			),
			new MetodData(void.class, "setDoorInteract", "method.inpcai.setdoortnteract",
				new ParameterData(boolean.class, "type", "parameter.doortype")
			),
			new MetodData(void.class, "setInteractWithNPCs", "method.inpcai.setinteractwithnpcs",
				new ParameterData(boolean.class, "interact", "parameter.boolean")
			),
			new MetodData(void.class, "setLeapAtTarget", "method.inpcai.setleapattarget",
				new ParameterData(boolean.class, "leap", "parameter.boolean")
			),
			new MetodData(void.class, "setMovingPathType", "method.inpcai.setmovingpathtype",
				new ParameterData(int.class, "type", "parameter.inpcai.movingpathtype"),
				new ParameterData(boolean.class, "pauses", "parameter.inpcai.pauses")
			),
			new MetodData(void.class, "setMovingType", "method.inpcai.setmovingtype",
				new ParameterData(int.class, "type", "parameter.inpcai.movingtype")
			),
			new MetodData(void.class, "setNavigationType", "method.inpcai.setNavigationType",
				new ParameterData(int.class, "type", "parameter.inpcai.waytype")
			),
			new MetodData(void.class, "setRetaliateType", "method.inpcai.setretaliatetype",
				new ParameterData(int.class, "type", "parameter.inpcai.retaltype")
			),
			new MetodData(void.class, "setReturnsHome", "method.inpcai.setreturnshome",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setSheltersFrom", "method.inpcai.setsheltersfrom",
				new ParameterData(int.class, "type", "parameter.inpcai.sheltype")
			),
			new MetodData(void.class, "setStandingType", "method.inpcai.setstandingtype",
				new ParameterData(int.class, "type", "parameter.inpcai.setsttype")
			),
			new MetodData(void.class, "setStopOnInteract", "method.inpcai.setstoponinteract",
				new ParameterData(boolean.class, "stopOnInteract", "parameter.boolean")
			),
			new MetodData(void.class, "setTacticalRange", "method.inpcai.settacticalrange",
				new ParameterData(int.class, "range", "parameter.inpcai.tactrange")
			),
			new MetodData(void.class, "setTacticalType", "method.inpcai.settacticaltype",
				new ParameterData(int.class, "type", "parameter.inpcai.tacttype")
			),
			new MetodData(void.class, "setWalkingSpeed", "method.inpcai.setwalkingspeed",
				new ParameterData(int.class, "speed", "parameter.speed")
			),
			new MetodData(void.class, "setWanderingRange", "method.inpcai.setwanderingrange",
				new ParameterData(int.class, "range", "parameter.inpcai.wanderrange")
			)
		)
	),
	INPCDisplay(new InterfaseData(INPCDisplay.class, null,
			new Class<?>[] { DataDisplay.class },
			"interfase.inpcdisplay",
			new MetodData(int.class, "getBossbar", "method.inpcdisplay.getbossbar"),
			new MetodData(int.class, "getBossColor", "method.inpcdisplay.getbosscolor"),
			new MetodData(String.class, "getCapeTexture", "method.inpcdisplay.getcapetexture"),
			new MetodData(boolean.class, "getHasHitbox", "method.inpcdisplay.gethashitbox"),
			new MetodData(boolean.class, "getHasLivingAnimation", "method.inpcdisplay.gethaslivinganimation"),
			new MetodData(String.class, "getModel", "method.inpcdisplay.getmodel"),
			new MetodData(float[].class, "getModelScale", "method.inpcdisplay.getmodelscale",
				new ParameterData(int.class, "part", "parameter.body.part.0")
			),
			new MetodData(String.class, "getName", "method.inpcdisplay.getname"),
			new MetodData(String.class, "getOverlayTexture", "method.inpcdisplay.getoverlaytexture"),
			new MetodData(int.class, "getShowName", "method.inpcdisplay.getshowname"),
			new MetodData(int.class, "getSize", "method.inpcdisplay.getsize"),
			new MetodData(String.class, "getSkinPlayer", "method.inpcdisplay.getskinplayer"),
			new MetodData(String.class, "getSkinTexture", "method.inpcdisplay.getskintexture"),
			new MetodData(String.class, "getSkinUrl", "method.inpcdisplay.getskinurl"),
			new MetodData(int.class, "getTint", "method.inpcdisplay.gettint"),
			new MetodData(String.class, "getTitle", "method.inpcdisplay.gettitle"),
			new MetodData(int.class, "getVisible", "method.inpcdisplay.getvisible"),
			new MetodData(boolean.class, "isVisibleTo", "method.inpcdisplay.isvisibleto",
				new ParameterData(int.class, "part", "parameter.player")
			),
			new MetodData(void.class, "setBossbar", "method.inpcdisplay.setbossbar",
				new ParameterData(int.class, "type", "parameter.inpcdisplay.bartype")
			),
			new MetodData(void.class, "setBossColor", "method.inpcdisplay.setbosscolor",
				new ParameterData(int.class, "color", "parameter.inpcdisplay.bosscolor")
			),
			new MetodData(void.class, "setCapeTexture", "method.inpcdisplay.setcapetexture",
				new ParameterData(String.class, "texture", "parameter.inpcdisplay.capetexture")
			),
			new MetodData(void.class, "setHasHitbox", "method.inpcdisplay.sethashitbox",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setHasLivingAnimation", "method.inpcdisplay.sethaslivinganimation",
				new ParameterData(boolean.class, "enabled", "parameter.boolean")
			),
			new MetodData(void.class, "setModel", "method.inpcdisplay.setmodel",
				new ParameterData(String.class, "model", "parameter.inpcdisplay.model")
			),
			new MetodData(void.class, "setModelScale", "method.inpcdisplay.setmodelscale",
				new ParameterData(int.class, "part", "parameter.body.part.0"),
				new ParameterData(float.class, "x", "parameter.posx"),
				new ParameterData(float.class, "y", "parameter.posy"),
				new ParameterData(float.class, "z", "parameter.posz")
			),
			new MetodData(void.class, "setName", "method.inpcdisplay.setname",
				new ParameterData(String.class, "name", "parameter.entity.name")
			),
			new MetodData(void.class, "setOverlayTexture", "method.inpcdisplay.setoverlaytexture",
				new ParameterData(String.class, "texture", "parameter.inpcdisplay.overtexture")
			),
			new MetodData(void.class, "setShowName", "method.inpcdisplay.setshowname",
				new ParameterData(int.class, "type", "parameter.inpcdisplay.showtype")
			),
			new MetodData(void.class, "setSize", "method.inpcdisplay.setsize",
				new ParameterData(int.class, "size", "parameter.size")
			),
			new MetodData(void.class, "setSkinPlayer", "method.inpcdisplay.setskinplayer",
				new ParameterData(String.class, "name", "parameter.playername")
			),
			new MetodData(void.class, "setSkinTexture", "method.inpcdisplay.setskintexture",
				new ParameterData(String.class, "texture", "parameter.inpcdisplay.skintexture")
			),
			new MetodData(void.class, "setSkinUrl", "method.inpcdisplay.setskinurl",
				new ParameterData(String.class, "url", "parameter.inpcdisplay.urltexture")
			),
			new MetodData(void.class, "setTint", "method.inpcdisplay.settint",
				new ParameterData(int.class, "color", "parameter.inpcdisplay.tintcolor")
			),
			new MetodData(void.class, "setTitle", "method.inpcdisplay.settitle",
				new ParameterData(String.class, "title", "parameter.inpcdisplay.title")
			),
			new MetodData(void.class, "setVisible", "method.inpcdisplay.setvisible",
				new ParameterData(int.class, "type", "parameter.inpcdisplay.vistype")
			)
		)
	),
	INPCInventory(new InterfaseData(INPCInventory.class, null,
			new Class<?>[] { DataInventory.class },
			"interfase.inpcinv",
			new MetodData(ICustomDrop.class, "addDropItem", "method.inpcinv.adddropitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack"),
				new ParameterData(double.class, "chance", "parameter.chance")
			),
			new MetodData(IItemStack.class, "getArmor", "method.inpcinv.getarmor",
				new ParameterData(int.class, "slot", "parameter.inpcinv.armslot")
			),
			new MetodData(ICustomDrop.class, "getDrop", "method.inpcinv.getdrop",
				new ParameterData(int.class, "slot", "parameter.inpcinv.dropslot")
			),
			new MetodData(int.class, "getExpMax", "method.inpcinv.getexpmax"),
			new MetodData(int.class, "getExpMin", "method.inpcinv.getexpmin"),
			new MetodData(int.class, "getExpRNG", "method.inpcinv.getexprnd"),
			new MetodData(IItemStack[].class, "getItemsRNG", "method.inpcinv.getitemsrnd",
				new ParameterData(EntityLivingBase.class, "attacking", "parameter.inpcinv.attacking")
			),
			new MetodData(IItemStack[].class, "getItemsRNGL", "method.inpcinv.getitemsrndl",
				new ParameterData(EntityLivingBase.class, "attacking", "parameter.inpcinv.attacking")
			),
			new MetodData(IItemStack.class, "getLeftHand", "method.inpcinv.getlefthand"),
			new MetodData(IItemStack.class, "getProjectile", "method.inpcinv.getprojectile"),
			new MetodData(IItemStack.class, "getRightHand", "method.inpcinv.getrighthand"),
			new MetodData(boolean.class, "getXPLootMode", "method.inpcinv.getxplootmode"),
			new MetodData(boolean.class, "removeDrop", "method.inpcinv.removedrop",
				new ParameterData(ICustomDrop.class, "drop", "parameter.inpcinv.drop")
			),
			new MetodData(boolean.class, "removeDrop", "method.inpcinv.removedrop",
				new ParameterData(int.class, "slot", "parameter.inpcinv.dropslot")
			),
			new MetodData(void.class, "setArmor", "method.inpcinv.setarmor",
				new ParameterData(int.class, "slot", "parameter.inpcinv.armslot"),
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MetodData(void.class, "setExp", "method.inpcinv.setexp",
				new ParameterData(int.class, "min", "parameter.min"),
				new ParameterData(int.class, "max", "parameter.max")
			),
			new MetodData(void.class, "setLeftHand", "method.inpcinv.setlefthand",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MetodData(void.class, "setProjectile", "method.inpcinv.setprojectile",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MetodData(void.class, "setRightHand", "method.inpcinv.setrighthand",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MetodData(void.class, "setXPLootMode", "method.inpcinv.setxplootmode",
				new ParameterData(boolean.class, "mode", "parameter.boolean")
			)
		)
	),
	INPCJob(new InterfaseData(INPCJob.class, null,
			new Class<?>[] { JobBard.class, JobBuilder.class, JobChunkLoader.class, JobConversation.class, JobInterface.class,
				JobFollower.class, JobGuard.class, JobHealer.class, JobItemGiver.class, JobSpawner.class },
			"interfase.inpcjob",
			new MetodData(ICustomDrop.class, "getType", "method.inpcjob.gettype")
		)
	),
	INPCMelee(new InterfaseData(INPCMelee.class, null,
			new Class<?>[] { DataMelee.class },
			"interfase.inpcmelee",
			new MetodData(int.class, "getDelay", "method.inpcmelee.getdelay"),
			new MetodData(int.class, "getEffectStrength", "method.inpcmelee.geteffectstrength"),
			new MetodData(int.class, "getEffectTime", "method.inpcmelee.geteffecttime"),
			new MetodData(int.class, "getKnockback", "method.inpcmelee.getknockback"),
			new MetodData(int.class, "getRange", "method.inpcmelee.getrange"),
			new MetodData(int.class, "getStrength", "method.inpcmelee.getstrength"),
			new MetodData(void.class, "setDelay", "method.inpcmelee.setdelay",
				new ParameterData(int.class, "speed", "parameter.speed")
			),
			new MetodData(void.class, "setEffect", "method.inpcmelee.seteffect",
				new ParameterData(int.class, "type", "parameter.effect.type"),
				new ParameterData(int.class, "strength", "parameter.effect.strength"),
				new ParameterData(int.class, "time", "parameter.effect.time")
			),
			new MetodData(void.class, "setKnockback", "method.inpcmelee.setknockback",
				new ParameterData(int.class, "knockback", "parameter.ientity.power")
			),
			new MetodData(void.class, "setRange", "method.inpcmelee.setrange",
				new ParameterData(int.class, "range", "parameter.range")
			),
			new MetodData(void.class, "setStrength", "method.inpcmelee.setstrength",
				new ParameterData(int.class, "strength", "enchantment.damage")
			)
		)
	),
	INPCRanged(new InterfaseData(INPCRanged.class, null,
			new Class<?>[] { DataRanged.class },
			"interfase.inpcranged",
			new MetodData(boolean.class, "getAccelerate", "method.inpcranged.getaccelerate"),
			new MetodData(int.class, "getAccuracy", "method.inpcranged.getaccuracy"),
			new MetodData(int.class, "getBurst", "method.inpcranged.getburst"),
			new MetodData(int.class, "getBurstDelay", "method.inpcranged.getburstdelay"),
			new MetodData(int.class, "getDelayMax", "method.inpcranged.getmelaymax"),
			new MetodData(int.class, "getDelayMin", "method.inpcranged.getmelaymin"),
			new MetodData(int.class, "getDelayRNG", "method.inpcranged.getdelayrng"),
			new MetodData(int.class, "getEffectStrength", "method.inpcranged.geteffectstrength"),
			new MetodData(int.class, "getEffectTime", "method.inpcranged.geteffecttime"),
			new MetodData(int.class, "getEffectType", "method.inpcranged.geteffecttype"),
			new MetodData(int.class, "getExplodeSize", "method.inpcranged.getexplodesize"),
			new MetodData(int.class, "getFireType", "method.inpcranged.getfiretype"),
			new MetodData(boolean.class, "getGlows", "method.inpcranged.getglows"),
			new MetodData(boolean.class, "getHasAimAnimation", "method.inpcranged.gethasaimanimation"),
			new MetodData(boolean.class, "getHasGravity", "method.inpcranged.gethasgravity"),
			new MetodData(int.class, "getKnockback", "method.inpcranged.getknockback"),
			new MetodData(int.class, "getMeleeRange", "method.inpcranged.getmeleerange"),
			new MetodData(int.class, "getParticle", "method.inpcranged.getparticle"),
			new MetodData(int.class, "getRange", "method.inpcranged.getrange"),
			new MetodData(boolean.class, "getRender3D", "method.inpcranged.getrender3d"),
			new MetodData(int.class, "getShotCount", "method.inpcranged.getshotcount"),
			new MetodData(int.class, "getSize", "method.inpcranged.getsize"),
			new MetodData(String.class, "getSound", "method.inpcranged.getsound",
				new ParameterData(int.class, "type", "parameter.sound.type")
			),
			new MetodData(int.class, "getSpeed", "method.inpcranged.getspeed"),
			new MetodData(boolean.class, "getSpins", "method.inpcranged.getspins"),
			new MetodData(boolean.class, "getSticks", "method.inpcranged.getsticks"),
			new MetodData(int.class, "getStrength", "method.inpcranged.getstrength"),
			new MetodData(void.class, "setAccelerate", "method.inpcranged.setaccelerate",
				new ParameterData(boolean.class, "accelerate", "parameter.boolean")
			),
			new MetodData(void.class, "setAccuracy", "method.inpcranged.setaccuracy",
				new ParameterData(int.class, "accuracy", "parameter.inpcranged.accuracy")
			),
			new MetodData(void.class, "setBurst", "method.inpcranged.setburst",
				new ParameterData(int.class, "count", "parameter.range")
			),
			new MetodData(void.class, "setBurstDelay", "method.inpcranged.setburstdelay",
				new ParameterData(int.class, "delay", "parameter.ticks")
			),
			new MetodData(void.class, "setDelay", "method.inpcranged.setDelay",
				new ParameterData(int.class, "min", "parameter.min"),
				new ParameterData(int.class, "max", "parameter.max")
			),
			new MetodData(void.class, "setEffect", "method.inpcranged.seteffect",
				new ParameterData(int.class, "type", "parameter.effect.type"),
				new ParameterData(int.class, "strength", "parameter.effect.strength"),
				new ParameterData(int.class, "time", "parameter.ticks")
			),
			new MetodData(void.class, "setExplodeSize", "method.inpcranged.setexplodesize",
				new ParameterData(int.class, "delay", "parameter.explode.size")
			),
			new MetodData(void.class, "setFireType", "method.inpcranged.setfiretype",
				new ParameterData(int.class, "type", "parameter.inpcranged.firetype")
			),
			new MetodData(void.class, "setGlows", "method.inpcranged.setglows",
				new ParameterData(boolean.class, "glows", "parameter.boolean")
			),
			new MetodData(void.class, "setHasAimAnimation", "method.inpcranged.sethasaimanimation",
				new ParameterData(boolean.class, "aim", "parameter.boolean")
			),
			new MetodData(void.class, "setHasGravity", "method.inpcranged.sethasgravity",
				new ParameterData(boolean.class, "hasGravity", "parameter.boolean")
			),
			new MetodData(void.class, "setKnockback", "method.inpcranged.setknockback",
				new ParameterData(int.class, "punch", "parameter.knockback")
			),
			new MetodData(void.class, "setMeleeRange", "method.inpcranged.setmeleemange",
				new ParameterData(int.class, "range", "parameter.range")
			),
			new MetodData(void.class, "setParticle", "method.inpcranged.setparticle",
				new ParameterData(int.class, "type", "parameter.particle.type")
			),
			new MetodData(void.class, "setRange", "method.inpcranged.setrange",
				new ParameterData(int.class, "range", "parameter.range")
			),
			new MetodData(void.class, "setRender3D", "method.inpcranged.setrender3d",
				new ParameterData(boolean.class, "render3d", "parameter.boolean")
			),
			new MetodData(void.class, "setShotCount", "method.inpcranged.setshotcount",
				new ParameterData(int.class, "count", "parameter.count")
			),
			new MetodData(void.class, "setSize", "method.inpcranged.setsize",
				new ParameterData(int.class, "size", "parameter.size")
			),
			new MetodData(void.class, "setSize", "method.inpcranged.setsize",
				new ParameterData(int.class, "type", "parameter.sound.type"),
				new ParameterData(String.class, "sound", "parameter.sound.name")
			),
			new MetodData(void.class, "setSpeed", "method.inpcranged.setspeed",
				new ParameterData(int.class, "speed", "parameter.speed")
			),
			new MetodData(void.class, "setSpins", "method.inpcranged.setspins",
				new ParameterData(boolean.class, "spins", "parameter.boolean")
			),
			new MetodData(void.class, "setSticks", "method.inpcranged.setsticks",
				new ParameterData(boolean.class, "spins", "parameter.boolean")
			),
			new MetodData(void.class, "setSticks", "method.inpcranged.setsticks",
				new ParameterData(int.class, "strength", "parameter.inpcranged.strength")
			)
		)
	),
	IAnimationHandler(new InterfaseData(IAnimationHandler.class, null,
			new Class<?>[] { AnimationController.class },
			"interfase.ianimationhandler",
			new MetodData(IAnimation[].class, "getAnimations", "method.ianimationhandler.getanimations",
				new ParameterData(int.class, "animationType", "parameter.animation.type")
			),
			new MetodData(IAnimation.class, "getAnimation", "method.ianimationhandler.getanimation",
				new ParameterData(int.class, "animationId", "parameter.animation.id")
			),
			new MetodData(IAnimation.class, "getAnimation", "method.ianimationhandler.getanimation",
				new ParameterData(int.class, "animationName", "parameter.animation.name")
			),
			new MetodData(boolean.class, "removeAnimation", "method.ianimationhandler.removeanimation",
				new ParameterData(int.class, "animationId", "parameter.animation.id")
			),
			new MetodData(boolean.class, "removeAnimation", "method.ianimationhandler.removeanimation",
				new ParameterData(int.class, "animationName", "parameter.animation.name")
			),
			new MetodData(IAnimation.class, "createNew", "method.ianimationhandler.createnew",
				new ParameterData(int.class, "animationType", "parameter.animation.type")
			)
		)
	),
	INPCAnimation(new InterfaseData(INPCAnimation.class, null,
			new Class<?>[] { DataAnimation.class },
			"interfase.inpcanimation",
			new MetodData(void.class, "reset", "method.inpcanimation.reset"),
			new MetodData(void.class, "stopAnimation", "method.inpcanimation.stopanimation"),
			new MetodData(void.class, "clear", "method.inpcanimation.clear"),
			new MetodData(void.class, "update", "method.inpcanimation.update"),
			new MetodData(IAnimation[].class, "getAnimations", "method.inpcanimation.getanimations",
				new ParameterData(int.class, "animationType", "parameter.animation.type")
			),
			new MetodData(IAnimation.class, "getAnimation", "method.inpcanimation.getanimation",
				new ParameterData(int.class, "animationType", "parameter.animation.type"),
				new ParameterData(int.class, "variant", "parameter.animation.variant")
			),
			new MetodData(void.class, "startAnimation", "method.inpcanimation.startanimation",
				new ParameterData(int.class, "animationType", "parameter.animation.type")
			),
			new MetodData(void.class, "startAnimation", "method.inpcanimation.startanimation",
				new ParameterData(int.class, "animationType", "parameter.animation.type"),
				new ParameterData(int.class, "variant", "parameter.animation.variant")
			),
			new MetodData(void.class, "startAnimationFromSaved", "method.inpcanimation.startanimationfromsaved",
				new ParameterData(int.class, "animationID", "parameter.animation.id")
			),
			new MetodData(void.class, "startAnimationFromSaved", "method.inpcanimation.startanimationfromsaved",
				new ParameterData(String.class, "animationName", "parameter.animation.name")
			),
			new MetodData(INbt.class, "getNbt", "method.inpcanimation.getnbt"),
			new MetodData(void.class, "setNbt", "method.inpcanimation.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MetodData(boolean.class, "isDisable", "method.inpcanimation.isdisable"),
			new MetodData(void.class, "setDisable", "method.inpcanimation.setdisable",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(boolean.class, "getRepeatLast", "method.inpcanimation.getrepeatlast"),
			new MetodData(void.class, "setRepeatLast", "method.inpcanimation.setrepeatlast",
				new ParameterData(int.class, "frames", "parameter.count")
			),
			new MetodData(boolean.class, "removeAnimation", "method.inpcanimation.removeanimation",
				new ParameterData(int.class, "animationType", "parameter.animation.type"),
				new ParameterData(String.class, "animationName", "parameter.animation.name")
			),
			new MetodData(void.class, "removeAnimations", "method.inpcanimation.removeanimations",
				new ParameterData(int.class, "animationType", "parameter.animation.type")
			),
			new MetodData(void.class, "createAnimation", "method.inpcanimation.createanimation",
				new ParameterData(int.class, "animationType", "parameter.animation.type")
			)
		)
	),
	IAnimation(new InterfaseData(IAnimation.class, null,
			new Class<?>[] { AnimationConfig.class },
			"interfase.ianimation",
			new MetodData(IAnimationFrame[].class, "getFrames", "method.ianimation.getframes"),
			new MetodData(IAnimationFrame.class, "getFrame", "method.ianimation.getframe",
				new ParameterData(int.class, "frame", "parameter.animation.frame")
			),
			new MetodData(IAnimationFrame.class, "addFrame", "method.ianimation.addframe"),
			new MetodData(IAnimationFrame.class, "addFrame", "method.ianimation.addframe",
				new ParameterData(IAnimationFrame.class, "frame", "parameter.animation.frame")
			),
			new MetodData(boolean.class, "removeFrame", "method.ianimation.removeframe",
				new ParameterData(int.class, "frame", "parameter.animation.frame")
			),
			new MetodData(boolean.class, "removeFrame", "method.ianimation.removeframe",
				new ParameterData(IAnimationFrame.class, "frame", "parameter.animation.frame")
			),
			new MetodData(int.class, "getType", "method.ianimation.getåype"),
			new MetodData(String.class, "getName", "method.ianimation.getname"),
			new MetodData(void.class, "setName", "method.ianimation.setname",
				new ParameterData(String.class, "name", "parameter.name")
			),
			new MetodData(INbt.class, "getNbt", "method.ianimation.getnbt"),
			new MetodData(void.class, "setNbt", "method.ianimation.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			
			new MetodData(INbt.class, "startToNpc", "method.ianimation.starttonpc",
				new ParameterData(ICustomNpc.class, "nbt", "parameter.npc")
			),
			new MetodData(boolean.class, "isDisabled", "method.ianimationpart.isdisabled"),
			new MetodData(void.class, "setDisabled", "method.ianimationpart.setdisabled",
				new ParameterData(boolean.class, "disabled", "parameter.boolean")
			),
			new MetodData(int.class, "getRepeatLast", "method.ianimationpart.getrepeatLast"),
			new MetodData(void.class, "setRepeatLast", "method.ianimationpart.setrepeatLast",
				new ParameterData(int.class, "frames", "parameter.count")
			)
		)
	),
	IAnimationFrame(new InterfaseData(IAnimationFrame.class, null,
			new Class<?>[] { AnimationFrameConfig.class },
			"interfase.ianimationframe",
			new MetodData(boolean.class, "isSmooth", "method.ianimationpart.issmooth"),
			new MetodData(void.class, "setSmooth", "method.ianimationpart.setsmooth",
				new ParameterData(boolean.class, "isSmooth", "parameter.boolean")
			),
			new MetodData(int.class, "getSpeed", "method.ianimationpart.getspeed"),
			new MetodData(void.class, "setSpeed", "method.ianimationpart.setspeed",
				new ParameterData(int.class, "ticks", "parameter.ticks")
			),
			new MetodData(int.class, "getEndDelay", "method.ianimationpart.getenddelay"),
			new MetodData(void.class, "setEndDelay", "method.ianimationpart.setenddelay",
				new ParameterData(int.class, "ticks", "parameter.ticks")
			)
		)
	),
	IAnimationPart(new InterfaseData(IAnimationPart.class, null,
			new Class<?>[] { PartConfig.class },
			"interfase.ianimationpart",
			new MetodData(void.class, "clear", "method.ianimationpart.clear"),
			new MetodData(float[].class, "getRotation", "method.ianimationpart.getrotation"),
			new MetodData(float[].class, "getOffset", "method.ianimationpart.getoffset"),
			new MetodData(float[].class, "getScale", "method.ianimationpart.getscale"),
			new MetodData(void.class, "setRotation", "method.ianimationpart.getrotation",
				new ParameterData(float.class, "x", "parameter.rotx"),
				new ParameterData(float.class, "y", "parameter.rotx"),
				new ParameterData(float.class, "z", "parameter.rotx")
			),
			new MetodData(void.class, "setOffset", "method.ianimationpart.setoffset",
				new ParameterData(float.class, "x", "parameter.posdx"),
				new ParameterData(float.class, "y", "parameter.posdx"),
				new ParameterData(float.class, "z", "parameter.posdx")
			),
			new MetodData(void.class, "setScale", "method.ianimationpart.setscale",
				new ParameterData(float.class, "x", "parameter.scalex"),
				new ParameterData(float.class, "y", "parameter.scalex"),
				new ParameterData(float.class, "z", "parameter.scalex")
			),
			new MetodData(boolean.class, "isDisabled", "method.ianimationpart.isdisabled"),
			new MetodData(void.class, "setDisabled", "method.ianimationpart.setdisabled",
				new ParameterData(boolean.class, "disabled", "parameter.boolean")
			)
		)
	),
	INPCRole(new InterfaseData(INPCRole.class, null,
			new Class<?>[] { RoleBank.class, RoleCompanion.class, RoleDialog.class, RoleFollower.class, RolePostman.class,
				RoleTrader.class, RoleTransporter.class },
			"interfase.inpcrole",
			new MetodData(int.class, "getType", "method.inpcrole.gettype")
		)
	),
	INPCStats(new InterfaseData(INPCStats.class, null,
			new Class<?>[] { DataStats.class },
			"interfase.inpcstats",
			new MetodData(int.class, "getAggroRange", "method.inpcstats.getaggrorange"),
			new MetodData(int.class, "getCombatRegen", "method.inpcstats.getcombatregen"),
			new MetodData(int.class, "getCreatureType", "method.inpcstats.getcreaturetype"),
			new MetodData(int.class, "getHealthRegen", "method.inpcstats.gethealthregen"),
			new MetodData(boolean.class, "getHideDeadBody", "method.inpcstats.gethidedeadbody"),
			new MetodData(boolean.class, "getImmune", "method.inpcstats.getImmune",
				new ParameterData(int.class, "type", "parameter.inpcstats.immune.type")
			),
			new MetodData(int.class, "getLevel", "method.inpcstats.getlevel"),
			new MetodData(int.class, "getMaxHealth", "method.inpcstats.getmaxhealth"),
			new MetodData(INPCMelee.class, "getMelee", "method.inpcstats.getmelee"),
			new MetodData(INPCRanged.class, "getRanged", "method.inpcstats.getranged"),
			new MetodData(int.class, "getRarity", "method.inpcstats.getrarity"),
			new MetodData(String.class, "getResistance", "method.inpcstats.getresistance"),
			new MetodData(float.class, "getImmune", "method.inpcstats.getImmune",
				new ParameterData(int.class, "type", "parameter.inpcstats.resistance.type")
			),
			new MetodData(int.class, "getRespawnTime", "method.inpcstats.getrespawntime"),
			new MetodData(int.class, "getRespawnType", "method.inpcstats.getrespawntype"),
			new MetodData(boolean.class, "isCalmdown", "method.inpcstats.isCalmdown"),
			new MetodData(void.class, "setAggroRange", "method.inpcstats.setaggrorange",
				new ParameterData(int.class, "range", "parameter.inpcstats.aggrorange")
			),
			new MetodData(void.class, "setCalmdown", "method.inpcstats.setcalmdown",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setCombatRegen", "method.inpcstats.setcombatregen",
				new ParameterData(int.class, "regen", "parameter.inpcstats.combatregen")
			),
			new MetodData(void.class, "setCreatureType", "method.inpcstats.setcreaturetype",
				new ParameterData(int.class, "type", "parameter.inpcstats.creaturetype")
			),
			new MetodData(void.class, "setHealthRegen", "method.inpcstats.sethealthregen",
				new ParameterData(int.class, "regen", "parameter.inpcstats.healthregen")
			),
			new MetodData(void.class, "setHideDeadBody", "method.inpcstats.sethidedeadbody",
				new ParameterData(boolean.class, "hide", "parameter.boolean")
			),
			new MetodData(void.class, "setImmune", "method.inpcstats.setimmune",
				new ParameterData(int.class, "type", "parameter.inpcstats.immunetype"),
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setLevel", "method.inpcstats.setlevel",
				new ParameterData(int.class, "level", "type.level")
			),
			new MetodData(void.class, "setMaxHealth", "method.inpcstats.setmaxmealth",
				new ParameterData(int.class, "maxHealth", "parameter.health")
			),
			new MetodData(void.class, "setRarity", "method.inpcstats.setrarity",
				new ParameterData(int.class, "rarity", "stats.rarity")
			),
			new MetodData(void.class, "setRarityTitle", "method.inpcstats.setraritytitle",
				new ParameterData(String.class, "rarity", "parameter.inpcstats.rarity")
			),
			new MetodData(void.class, "setResistance", "method.inpcstats.setresistance",
				new ParameterData(int.class, "type", "parameter.inpcstats.resistancetype"),
				new ParameterData(float.class, "value", "parameter.inpcstats.resistancevalue")
			),
			new MetodData(void.class, "setRespawnTime", "method.inpcstats.setrespawntime",
				new ParameterData(String.class, "seconds", "parameter.seconds")
			),
			new MetodData(void.class, "setRespawnType", "method.inpcstats.setrespawntype",
				new ParameterData(String.class, "seconds", "parameter.inpcstats.respawntype")
			)
		)
	),
	IPixelmonPlayerData(new InterfaseData(IPixelmonPlayerData.class, null, null,
			"interfase.ipixelmonplayerdata",
			new MetodData(Object.class, "getParty", "method.ipixelmonplayerdata.getparty"),
			new MetodData(Object.class, "getPC", "method.ipixelmonplayerdata.getpc")
		)
	),
	IPlayerMail(new InterfaseData(IPlayerMail.class, null,
			new Class<?>[] { PlayerMail.class },
			"interfase.iplayermail",
			new MetodData(IContainer.class, "getContainer", "method.iplayermail.getcontainer"),
			new MetodData(IQuest.class, "getQuest", "method.iplayermail.getquest"),
			new MetodData(String.class, "getSender", "method.iplayermail.getsender"),
			new MetodData(String.class, "getSubject", "method.iplayermail.getsubject"),
			new MetodData(String[].class, "getText", "method.iplayermail.gettext"),
			new MetodData(void.class, "setQuest", "method.iplayermail.setquest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(void.class, "setSender", "method.iplayermail.setsender",
				new ParameterData(String.class, "sender", "parameter.iplayermail.sender")
			),
			new MetodData(void.class, "setSubject", "method.iplayermail.setsubject",
				new ParameterData(String.class, "subject", "parameter.iplayermail.subject")
			),
			new MetodData(void.class, "setText", "method.iplayermail.settext",
				new ParameterData(String[].class, "text", "parameter.iplayermail.text")
			)
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
			),
			new MetodData(INPCAnimation.class, "getAnimations", "method.getanimations")
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
				new ParameterData(float.class, "direction", "parameter.yaw")
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
			"interfase.ientityitem",
			new MetodData(long.class, "getAge", "method.ientityitem.getage"),
			new MetodData(IItemStack.class, "getItem", "method.ientityitem.getitem"),
			new MetodData(int.class, "getLifeSpawn", "method.ientityitem.getlifespawn"),
			new MetodData(String.class, "getOwner", "method.ientityitem.getowner"),
			new MetodData(int.class, "getPickupDelay", "method.ientityitem.getpickupdelay"),
			new MetodData(void.class, "setAge", "method.ientityitem.setquest",
				new ParameterData(long.class, "age", "parameter.age")
			),
			new MetodData(void.class, "setItem", "method.ientityitem.setquest",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MetodData(void.class, "setLifeSpawn", "method.ientityitem.setlifespawn",
				new ParameterData(int.class, "age", "parameter.age")
			),
			new MetodData(void.class, "setOwner", "method.ientityitem.setowner",
				new ParameterData(String.class, "name", "parameter.playername")
			),
			new MetodData(void.class, "setPickupDelay", "method.ientityitem.setpickupdelay",
				new ParameterData(int.class, "delay", "parameter.ticks")
			)
		)
	),
	IEntityLiving(new InterfaseData(IEntityLiving.class, IEntityLivingBase.class,
			new Class<?>[] { EntityLivingWrapper.class },
			"interfase.ientityliving",
			new MetodData(void.class, "clearNavigation", "method.ientityliving.clearnavigation"),
			new MetodData(Entity.class, "getMCEntity", "method.ientity.getmcentity"),
			new MetodData(IPos.class, "getNavigationPath", "method.ientityliving.getnavigationpath"),
			new MetodData(boolean.class, "isNavigating", "method.ientityliving.isnavigating"),
			new MetodData(void.class, "jump", "method.ientityliving.jump"),
			new MetodData(void.class, "navigateTo", "method.ientityliving.navigateto",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz"),
				new ParameterData(double.class, "speed", "parameter.speed")
			)
		)
	),
	IEntityLivingBase(new InterfaseData(IEntityLivingBase.class, IEntity.class,
			new Class<?>[] { EntityLivingBaseWrapper.class },
			"interfase.ientitylivingbase",
			new MetodData(IMark.class, "addMark", "method.ientitylivingbase.addmark",
				new ParameterData(int.class, "type", "parameter.mark.type")
			),
			new MetodData(void.class, "addPotionEffect", "method.ientitylivingbase.addpotioneffect",
				new ParameterData(int.class, "effect", "parameter.effect.id"),
				new ParameterData(int.class, "duration", "parameter.effect.duration"),
				new ParameterData(int.class, "type", "parameter.effect.strength"),
				new ParameterData(boolean.class, "hideParticles", "parameter.effect.hideparticles")
			),
			new MetodData(boolean.class, "canSeeEntity", "method.ientitylivingbase.canseeentity",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MetodData(void.class, "clearPotionEffects", "method.ientitylivingbase.clearpotioneffects"),
			new MetodData(IItemStack.class, "getArmor", "method.ientitylivingbase.getarmor",
				new ParameterData(IEntity.class, "slot", "parameter.armor.slot")
			),
			new MetodData(IEntityLivingBase.class, "getAttackTarget", "method.ientitylivingbase.getattacktarget"),
			new MetodData(float.class, "getHealth", "method.ientitylivingbase.gethealth"),
			new MetodData(IEntityLivingBase.class, "getLastAttacked", "method.ientitylivingbase.getlastattacked"),
			new MetodData(int.class, "getLastAttackedTime", "method.ientitylivingbase.getlastattackedtime"),
			new MetodData(IItemStack.class, "getMainhandItem", "method.ientitylivingbase.getmainhanditem"),
			new MetodData(IMark[].class, "getMarks", "method.ientitylivingbase.getmarks"),
			new MetodData(float.class, "getMaxHealth", "method.ientitylivingbase.getmaxhealth"),
			new MetodData(Entity.class, "getMCEntity", "method.ientity.getmcentity"),
			new MetodData(float.class, "getMoveForward", "method.ientitylivingbase.getmoveforward"),
			new MetodData(float.class, "getMoveStrafing", "method.ientitylivingbase.getmovestrafing"),
			new MetodData(float.class, "getMoveVertical", "method.ientitylivingbase.getmovevertical"),
			new MetodData(IItemStack.class, "getOffhandItem", "method.ientitylivingbase.getoffhanditem"),
			new MetodData(int.class, "getPotionEffect", "method.ientitylivingbase.getpotioneffect",
				new ParameterData(int.class, "effect", "parameter.effect.id")
			),
			new MetodData(boolean.class, "isAttacking", "method.ientitylivingbase.isattacking"),
			new MetodData(boolean.class, "isChild", "method.ientitylivingbase.ischild"),
			new MetodData(void.class, "removeMark", "method.ientitylivingbase.removemark",
				new ParameterData(IMark.class, "mark", "parameter.mark")
			),
			new MetodData(void.class, "setArmor", "method.ientitylivingbase.setarmor",
				new ParameterData(IMark.class, "slot", "parameter.armor.slot"),
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MetodData(void.class, "setAttackTarget", "method.ientitylivingbase.setattacktarget",
				new ParameterData(IEntityLivingBase.class, "living", "parameter.entity")
			),
			new MetodData(void.class, "setHealth", "method.ientitylivingbase.sethealth",
				new ParameterData(float.class, "health", "parameter.health")
			),
			new MetodData(void.class, "setMainhandItem", "method.ientitylivingbase.setmainhanditem",
					new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MetodData(void.class, "setMaxHealth", "method.ientitylivingbase.setmaxhealth",
				new ParameterData(float.class, "health", "parameter.health")
			),
			new MetodData(void.class, "setMoveForward", "method.ientitylivingbase.setmoveforward",
				new ParameterData(float.class, "move", "parameter.forward.move")
			),
			new MetodData(void.class, "setMoveStrafing", "method.ientitylivingbase.setmovestrafing",
				new ParameterData(float.class, "move", "parameter.strafing.move")
			),
			new MetodData(void.class, "setMoveVertical", "method.ientitylivingbase.setmovevertical",
				new ParameterData(float.class, "move", "parameter.vertical.move")
			),
			new MetodData(void.class, "setOffhandItem", "method.ientitylivingbase.setoffhanditem",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MetodData(void.class, "swingMainhand", "method.ientitylivingbase.swingmainhand"),
			new MetodData(void.class, "swingMainhand", "method.ientitylivingbase.swingoffhand")
		)
	),
	IMonster(new InterfaseData(IMonster.class, IEntityLiving.class,
			new Class<?>[] { MonsterWrapper.class },
			"interfase.imonster"
		)
	),
	IPixelmon(new InterfaseData(IPixelmon.class, IAnimal.class,
			new Class<?>[] { PixelmonWrapper.class },
			"interfase.ipixelmon",
			new MetodData(Object.class, "getPokemonData", "method.ipixelmon.getpokemondata")
		)
	),
	IPlayer(new InterfaseData(IPlayer.class, IEntityLivingBase.class,
			new Class<?>[] { PlayerWrapper.class },
			"interfase.iplayer",
			new MetodData(void.class, "addDialog", "method.iplayer.adddialog",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			),
			new MetodData(void.class, "addFactionPoints", "method.iplayer.addfactionpoints",
				new ParameterData(int.class, "id", "parameter.faction.id"),
				new ParameterData(int.class, "points", "parameter.value")
			),
			new MetodData(boolean.class, "canQuestBeAccepted", "method.iplayer.canquestbeaccepted",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(void.class, "clearData", "method.iplayer.cleardata"),
			new MetodData(void.class, "closeGui", "method.iplayer.closegui"),
			new MetodData(int.class, "factionStatus", "method.iplayer.factionstatus",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MetodData(boolean.class, "finishQuest", "method.iplayer.finishquest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(IQuest[].class, "getActiveQuests", "method.iplayer.getactivequests"),
			new MetodData(ICustomGui.class, "getCustomGui", "method.iplayer.getcustomGui"),
			new MetodData(String.class, "getDisplayName", "method.iplayer.getdisplayname"),
			new MetodData(int.class, "getExpLevel", "method.iplayer.getexplevel"),
			new MetodData(int.class, "getFactionPoints", "method.iplayer.getfactionpoints",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MetodData(IQuest[].class, "getFinishedQuests", "method.iplayer.getfinishedquests"),
			new MetodData(int.class, "getGamemode", "method.iplayer.getgamemode"),
			new MetodData(int.class, "getHunger", "method.iplayer.gethunger"),
			new MetodData(IContainer.class, "getInventory", "method.iplayer.getinventory"),
			new MetodData(IItemStack.class, "getInventoryHeldItem", "method.iplayer.getinventoryhelditem"),
			new MetodData(EntityPlayer.class, "getMCEntity", "method.ientity.getmcentity"),
			new MetodData(IContainer.class, "getOpenContainer", "method.iplayer.getopencontainer"),
			new MetodData(Object.class, "getPixelmonData", "method.iplayer.getpixelmondata"),
			new MetodData(IBlock.class, "getSpawnPoint", "method.iplayer.getspawnpoint"),
			new MetodData(ITimers.class, "getTimers", "method.iplayer.gettimers"),
			new MetodData(boolean.class, "giveItem", "method.iplayer.giveitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MetodData(boolean.class, "giveItem", "method.iplayer.giveitem",
				new ParameterData(String.class, "id", "parameter.itemname"),
				new ParameterData(int.class, "damage", "parameter.itemmeta"),
				new ParameterData(int.class, "amount", "parameter.itemcount")
			),
			new MetodData(boolean.class, "hasAchievement", "method.iplayer.hasachievement",
				new ParameterData(String.class, "achievement", "parameter.iplayer.achievement")
			),
			new MetodData(boolean.class, "hasActiveQuest", "method.iplayer.hasactivequest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(boolean.class, "isComleteQuest", "method.iplayer.iscomletequest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(boolean.class, "hasFinishedQuest", "method.iplayer.hasfinishedquest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(boolean.class, "hasPermission", "method.iplayer.haspermission",
				new ParameterData(String.class, "permission", "parameter.npcapi.permission")
			),
			new MetodData(boolean.class, "hasReadDialog", "method.iplayer.hasreaddialog",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			),
			new MetodData(int.class, "inventoryItemCount", "method.iplayer.inventoryitemcount",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			).setDeprecated(),
			new MetodData(int.class, "inventoryItemCount", "method.iplayer.inventoryitemcount",
				new ParameterData(IItemStack.class, "item", "parameter.stack"),
				new ParameterData(boolean.class, "ignoreDamage", "parameter.ignoredamage"),
				new ParameterData(boolean.class, "ignoreNBT", "parameter.ignorenbt")
			),
			new MetodData(int.class, "inventoryItemCount", "method.iplayer.inventoryitemcount",
				new ParameterData(String.class, "id", "parameter.itemname"),
				new ParameterData(int.class, "amount", "parameter.itemcount")
			).setDeprecated(),
			new MetodData(void.class, "kick", "method.iplayer.kick",
				new ParameterData(String.class, "message", "parameter.message")
			),
			new MetodData(void.class, "message", "method.iplayer.message",
				new ParameterData(String.class, "message", "parameter.message")
			),
			new MetodData(void.class, "playSound", "method.iplayer.playsound",
				new ParameterData(String.class, "sound", "parameter.sound.name"),
				new ParameterData(float.class, "volume", "parameter.sound.volume"),
				new ParameterData(float.class, "pitch", "parameter.sound.pitch")
			),
			new MetodData(void.class, "removeAllItems", "method.iplayer.removeallitems",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MetodData(void.class, "removeDialog", "method.iplayer.removedialog",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			),
			new MetodData(boolean.class, "removeItem", "method.iplayer.removeitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack"),
				new ParameterData(int.class, "amount", "parameter.itemcount")
			),
			new MetodData(int.class, "inventoryItemCount", "method.iplayer.inventoryitemcount",
				new ParameterData(String.class, "id", "parameter.itemname"),
				new ParameterData(int.class, "damage", "parameter.itemmeta"),
				new ParameterData(int.class, "amount", "parameter.itemcount")
			),
			new MetodData(void.class, "removeQuest", "method.iplayer.removequest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(void.class, "resetSpawnpoint", "method.iplayer.resetspawnpoint"),
			new MetodData(void.class, "sendMail", "method.iplayer.sendmail",
				new ParameterData(IPlayerMail.class, "mail", "parameter.mail")
			),
			new MetodData(void.class, "sendNotification", "method.iplayer.sendnotification",
				new ParameterData(String.class, "title", "parameter.title"),
				new ParameterData(String.class, "message", "parameter.message"),
				new ParameterData(int.class, "type", "parameter.message.type")
			),
			new MetodData(void.class, "setExpLevel", "method.iplayer.setexplevel",
				new ParameterData(int.class, "level", "type.level")
			),
			new MetodData(void.class, "setGamemode", "method.iplayer.setgamemode",
				new ParameterData(int.class, "mode", "parameter.gamemode")
			),
			new MetodData(void.class, "setHunger", "method.iplayer.sethunger",
				new ParameterData(int.class, "level", "type.level")
			),
			new MetodData(void.class, "setSpawnpoint", "method.iplayer.setspawnpoint",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(void.class, "setSpawnpoint", "method.iplayer.setspawnpoint",
				new ParameterData(IBlock.class, "block", "parameter.block")
			),
			new MetodData(IContainer.class, "showChestGui", "method.iplayer.showchestgui",
				new ParameterData(int.class, "rows", "parameter.chestgui.rows")
			).setDeprecated(),
			new MetodData(void.class, "showCustomGui", "method.iplayer.showcustomgui",
				new ParameterData(ICustomGui.class, "gui", "parameter.customgui")
			),
			new MetodData(void.class, "showDialog", "method.iplayer.showdialog",
				new ParameterData(int.class, "id", "parameter.dialog.id"),
				new ParameterData(String.class, "name", "parameter.entity.name")
			),
			new MetodData(void.class, "startQuest", "method.iplayer.startquest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(void.class, "stopQuest", "method.iplayer.stopquest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(void.class, "updatePlayerInventory", "method.iplayer.updateplayerinventory"),
			new MetodData(boolean.class, "isMoved", "method.iplayer.ismoved"),
			new MetodData(long.class, "getMoney", "method.iplayer.getmoney"),
			new MetodData(void.class, "addMoney", "method.iplayer.addmoney",
				new ParameterData(long.class, "value", "parameter.money.value")
			),
			new MetodData(void.class, "setMoney", "method.iplayer.setmoney",
				new ParameterData(long.class, "value", "parameter.money.value")
			),
			new MetodData(int[].class, "getKeyPressed", "method.iplayer.getkeypressed"),
			new MetodData(boolean.class, "hasKeyPressed", "method.iplayer.haskeypressed",
				new ParameterData(long.class, "key", "parameter.keyboardkey")
			),
			new MetodData(int[].class, "getMousePressed", "method.iplayer.getmousepressed"),
			new MetodData(boolean.class, "hasMousePress", "method.iplayer.hasmousepress",
				new ParameterData(long.class, "key", "parameter.mousekey")
			),
			new MetodData(void.class, "completeQuest", "method.iplayer.completequest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(IOverlayHUD.class, "getIOverlayHUD", "method.iplayer.gethud"),
			new MetodData(void.class, "trigger", "method.trigger",
				new ParameterData(int.class, "id", "parameter.trigger.id"),
				new ParameterData(Object[].class, "arguments", "parameter.trigger.arguments")
			)
		)
	),
	IProjectile(new InterfaseData(IProjectile.class, IThrowable.class,
			new Class<?>[] { ProjectileWrapper.class },
			"interfase.iprojectile",
			new MetodData(void.class, "enableEvents", "method.iprojectile.enableevents"),
			new MetodData(int.class, "getAccuracy", "method.iprojectile.getaccuracy"),
			new MetodData(boolean.class, "getHasGravity", "method.iprojectile.gethasgravity"),
			new MetodData(IItemStack.class, "getItem", "method.iprojectile.getitem"),
			new MetodData(void.class, "setAccuracy", "method.iplayer.setaccuracy",
				new ParameterData(int.class, "accuracy", "parameter.iprojectile.accuracy")
			),
			new MetodData(void.class, "setHasGravity", "method.iplayer.sethasgravity",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setHeading", "method.iplayer.setheading",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz")
			),
			new MetodData(void.class, "setHeading", "method.iplayer.setheading",
				new ParameterData(float.class, "yaw", "parameter.yaw"),
				new ParameterData(float.class, "pitch", "parameter.pitch")
			),
			new MetodData(void.class, "setHeading", "method.iplayer.setheading",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MetodData(void.class, "setItem", "method.iplayer.setitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			)
		)
	),
	IThrowable(new InterfaseData(IThrowable.class, IEntity.class,
			new Class<?>[] { ThrowableWrapper.class },
			"interfase.ithrowable"
		)
	),
	IVillager(new InterfaseData(IVillager.class, IEntityLiving.class,
			new Class<?>[] { VillagerWrapper.class },
			"interfase.ivillager",
			new MetodData(MerchantRecipeList.class, "getRecipes", "method.ivillager.getrecipes",
				new ParameterData(IPlayer.class, "player", "parameter.player")
			),
			new MetodData(IInventory.class, "getVillagerInventory", "method.ivillager.getvillagerinventory")
		)
	),
	IButton(new InterfaseData(IButton.class, ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiButtonWrapper.class },
			"interfase.ibutton",
			new MetodData(int.class, "getHeight", "method.component.getheight"),
			new MetodData(String.class, "getLabel", "method.ibutton.getlabel"),
			new MetodData(int.class, "getTextureX", "method.component.gettexturex"),
			new MetodData(int.class, "getTextureY", "method.component.gettexturey"),
			new MetodData(int.class, "getWidth", "method.component.getwidth"),
			new MetodData(IButton.class, "setLabel", "method.ibutton.setlabel",
				new ParameterData(String.class, "lable", "gui.name")
			),
			new MetodData(IButton.class, "setSize", "method.ibutton.setsize",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MetodData(IButton.class, "setTexture", "method.ibutton.settexture",
				new ParameterData(String.class, "texture", "parameter.texture")
			),
			new MetodData(IButton.class, "setTextureOffset", "method.ibutton.setåextureùffset",
				new ParameterData(String.class, "textureX", "parameter.texturex"),
				new ParameterData(String.class, "textureY", "parameter.texturey")
			)
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
	IOverlayHUD(new InterfaseData(IOverlayHUD.class, null,
			new Class<?>[] { PlayerOverlayHUD.class },
			"interfase.ihud",
			new MetodData(boolean.class, "isShowElementType", "method.ihud.isshowelementtype",
				new ParameterData(int.class, "type", "parameter.ihud.elementtype")
			),
			new MetodData(void.class, "setShowElementType", "method.ihud.setshowelementtype",
				new ParameterData(int.class, "type", "parameter.ihud.elementtype"),
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "isShowElementType", "method.ihud.isShowElementType",
				new ParameterData(String.class, "name", "parameter.ihud.elementname"),
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(IItemSlot.class, "addItemSlot", "method.ihud.additemslot",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(int.class, "x", "parameter.posu"),
				new ParameterData(int.class, "y", "parameter.posv")
			),
			new MetodData(IItemSlot.class, "addItemSlot", "method.ihud.additemslot",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(int.class, "x", "parameter.posu"),
				new ParameterData(int.class, "y", "parameter.posv"),
				new ParameterData(IItemStack.class, "stack", "parameter.stack")
			),
			new MetodData(ILabel.class, "addLabel", "method.ihud.addlabel",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(String.class, "label", "parameter.label.text"),
				new ParameterData(int.class, "x", "parameter.posu"),
				new ParameterData(int.class, "y", "parameter.posv"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MetodData(ILabel.class, "addLabel", "method.ihud.addlabel",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(String.class, "label", "parameter.label.text"),
				new ParameterData(int.class, "x", "parameter.posu"),
				new ParameterData(int.class, "y", "parameter.posv"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MetodData(ITexturedRect.class, "addTexturedRect", "method.ihud.addtexturedrect",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(String.class, "texture", "parameter.resource"),
				new ParameterData(int.class, "x", "parameter.posu"),
				new ParameterData(int.class, "y", "parameter.posv"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MetodData(ITexturedRect.class, "addTexturedRect", "method.ihud.addtexturedrect",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(String.class, "texture", "parameter.resource"),
				new ParameterData(int.class, "x", "parameter.posu"),
				new ParameterData(int.class, "y", "parameter.posv"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(int.class, "textureX", "parameter.posu"),
				new ParameterData(int.class, "textureY", "parameter.posv")
			),
			new MetodData(IGuiTimer.class, "addTimer", "method.ihud.addtimer",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(long.class, "start", "parameter.ihud.timer.start"),
				new ParameterData(long.class, "end", "parameter.ihud.timer.end"),
				new ParameterData(int.class, "x", "parameter.posu"),
				new ParameterData(int.class, "y", "parameter.posv"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MetodData(IGuiTimer.class, "addTimer", "method.ihud.addtimer",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(long.class, "start", "parameter.ihud.timer.start"),
				new ParameterData(long.class, "end", "parameter.ihud.timer.end"),
				new ParameterData(int.class, "x", "parameter.posu"),
				new ParameterData(int.class, "y", "parameter.posv"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MetodData(int[].class, "getKeyPressed", "method.ihud.getkeypressed"),
			new MetodData(int[].class, "getMousePressed", "method.ihud.getmousepressed"),
			new MetodData(boolean.class, "isMoved", "method.ihud.ismoved"),
			new MetodData(boolean.class, "hasMousePress", "method.ihud.hasmousepress",
				new ParameterData(int.class, "key", "parameter.mousekey")
			),
			new MetodData(boolean.class, "hasOrKeysPressed", "method.ihud.hasorkeyspressed",
				new ParameterData(int[].class, "key", "parameter.keyboardkey")
			),
			new MetodData(double[].class, "getWindowSize", "method.ihud.getwindowsize"),
			new MetodData(String.class, "getCurrentLanguage", "method.ihud.getcurrentlanguage"),
			new MetodData(ICustomGuiComponent.class, "getComponent", "method.ihud.getcomponent",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(int.class, "id", "parameter.component.id")
			),
			new MetodData(ICustomGuiComponent[].class, "getComponents", "method.ihud.getcomponents",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype")
			),
			new MetodData(ICustomGuiComponent[].class, "getComponents", "method.ihud.getcomponents"),
			new MetodData(IItemSlot[].class, "getSlots", "method.ihud.getslots",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype")
			),
			new MetodData(IItemSlot[].class, "getSlots", "method.ihud.getslots"),
			new MetodData(boolean.class, "removeComponent", "method.ihud.removecomponent",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(int.class, "id", "parameter.component.id")
			),
			new MetodData(boolean.class, "removeSlot", "method.ihud.removeslot",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(int.class, "id", "parameter.slot")
			),
			new MetodData(void.class, "update", "method.ihud.update"),
			new MetodData(void.class, "clear", "method.ihud.clear")
		)
	),
	IBorder(new InterfaseData(IBorder.class, null,
			new Class<?>[] { Zone3D.class },
			"interfase.iborder",
			new MetodData(boolean.class, "contains", "method.iborder.contains",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(boolean.class, "contains", "method.iborder.contains",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz"),
				new ParameterData(double.class, "height", "parameter.height")
			),
			new MetodData(void.class, "setHomePos", "method.iborder.sethomepos",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(IPos.class, "getHomePos", "method.iborder.gethomepos"),
			new MetodData(Point[].class, "getPoints", "method.iborder.getpoints"),
			new MetodData(INbt.class, "getNbt", "method.iborder.getnbt"),
			new MetodData(double.class, "distanceTo", "method.iborder.distanceto",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MetodData(double.class, "distanceTo", "method.iborder.distanceto",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "z", "parameter.posz")
			),
			new MetodData(Point[].class, "getClosestPoints", "method.iborder.getclosestpoints",
				new ParameterData(Point.class, "point", "parameter.point"),
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MetodData(int.class, "getClosestPoint", "method.iborder.getclosestpoint",
				new ParameterData(Point.class, "point", "parameter.point"),
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MetodData(boolean.class, "removePoint", "method.iborder.removepoint",
				new ParameterData(Point.class, "point", "parameter.point")
			),
			new MetodData(boolean.class, "removePoint", "method.iborder.removepoint",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(void.class, "clear", "method.iborder.removepoint"),
			new MetodData(int.class, "size", "method.iborder.size"),
			new MetodData(IPos.class, "getCenter", "method.iborder.getcenter"),
			new MetodData(int.class, "getMinX", "method.iborder.getminx"),
			new MetodData(int.class, "getMaxX", "method.iborder.getmaxx"),
			new MetodData(int.class, "getMinY", "method.iborder.getminy"),
			new MetodData(int.class, "getMaxY", "method.iborder.getmaxy"),
			new MetodData(int.class, "getMinZ", "method.iborder.getminz"),
			new MetodData(int.class, "getMaxZ", "method.iborder.getmaxz"),
			new MetodData(void.class, "setNbt", "method.iborder.setnbt",
				new ParameterData(INbt.class, "x", "parameter.nbt")
			),
			new MetodData(void.class, "scaling", "method.iborder.scaling.0",
				new ParameterData(float.class, "scale", "parameter.scale"),
				new ParameterData(boolean.class, "type", "parameter.iborder.type")
			),
			new MetodData(void.class, "scaling", "method.iborder.scaling.1",
				new ParameterData(double.class, "range", "parameter.range"),
				new ParameterData(boolean.class, "type", "parameter.iborder.type")
			),
			new MetodData(void.class, "centerOffsetTo", "method.iborder.centeroffsetto",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz"),
				new ParameterData(boolean.class, "type", "parameter.iborder.type")
			),
			new MetodData(void.class, "centerOffsetTo", "method.iborder.centeroffsetto",
				new ParameterData(Point.class, "point", "parameter.point"),
				new ParameterData(boolean.class, "type", "parameter.iborder.type")
			),
			new MetodData(void.class, "centerOffsetTo", "method.iborder.centeroffsetto",
				new ParameterData(IPos.class, "pos", "parameter.pos"),
				new ParameterData(boolean.class, "type", "parameter.iborder.type")
			),
			new MetodData(void.class, "offset", "method.iborder.offset",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(void.class, "offset", "method.iborder.offset",
				new ParameterData(Point.class, "point", "parameter.point")
			),
			new MetodData(void.class, "offset", "method.iborder.offset",
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MetodData(void.class, "insertPoint", "method.iborder.insertpoint",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz"),
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MetodData(void.class, "insertPoint", "method.iborder.insertpoint",
				new ParameterData(Point.class, "point", "parameter.point"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MetodData(void.class, "insertPoint", "method.iborder.insertpoint",
				new ParameterData(IPos.class, "pos0", "parameter.pos"),
				new ParameterData(IPos.class, "pos1", "parameter.pos")
			),
			new MetodData(Point.class, "addPoint", "method.iborder.addpoint",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(Point.class, "addPoint", "method.iborder.addpoint",
				new ParameterData(Point.class, "point", "parameter.point"),
				new ParameterData(int.class, "y", "parameter.posy")
			),
			new MetodData(Point.class, "addPoint", "method.iborder.addpoint",
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MetodData(Point.class, "setPoint", "method.iborder.setpoint",
				new ParameterData(int.class, "index", "parameter.iborder.index"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(Point.class, "setPoint", "method.iborder.setpoint",
				new ParameterData(int.class, "index", "parameter.iborder.index"),
				new ParameterData(Point.class, "point", "parameter.point"),
				new ParameterData(int.class, "y", "parameter.posy")
			),
			new MetodData(Point.class, "setPoint", "method.iborder.setpoint",
				new ParameterData(int.class, "index", "parameter.iborder.index"),
				new ParameterData(Point.class, "point", "parameter.point")
			),
			new MetodData(Point.class, "setPoint", "method.iborder.setpoint",
				new ParameterData(int.class, "index", "parameter.iborder.index"),
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MetodData(int.class, "getId", "method.iborder.getid"),
			new MetodData(String.class, "getName", "method.iborder.getname"),
			new MetodData(void.class, "setName", "method.iborder.setname",
				new ParameterData(String.class, "name", "parameter.name")
			),
			new MetodData(int.class, "getDimensionId", "method.iborder.getdimensionid"),
			new MetodData(void.class, "setDimensionId", "method.iborder.setdimensionid",
				new ParameterData(int.class, "dimID", "parameter.dimensionid")
			),
			new MetodData(int.class, "getColor", "method.iborder.getcolor"),
			new MetodData(void.class, "setColor", "method.iborder.setcolor",
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MetodData(IAvailability.class, "getAvailability", "method.iborder.getavailability"),
			new MetodData(int.class, "getMessage", "method.iborder.getmessage"),
			new MetodData(void.class, "setMessage", "method.iborder.setmessage",
				new ParameterData(String.class, "message", "parameter.message")
			),
			new MetodData(void.class, "update", "method.iborder.update")
		)
	),
	IBorderHandler(new InterfaseData(IBorderHandler.class, null,
			new Class<?>[] { BorderController.class },
			"interfase.iborderhandler",
			new MetodData(IBorder.class, "getRegion", "method.iborderhandler.getcolor",
				new ParameterData(int.class, "regionId", "parameter.iborderhandler.regionid")
			),
			new MetodData(INbt.class, "getNbt", "method.iborderhandler.getnbt"),
			new MetodData(boolean.class, "removeRegion", "method.iborderhandler.removeregion",
				new ParameterData(int.class, "regionId", "parameter.iborderhandler.regionid")
			),
			new MetodData(IBorder[].class, "getRegions", "method.iborderhandler.getregions",
				new ParameterData(int.class, "dimID", "parameter.dimensionid")
			),
			new MetodData(IBorder[].class, "getAllRegions", "method.iborderhandler.getallregions"),
			new MetodData(void.class, "update", "method.iborderhandler.update",
				new ParameterData(int.class, "regionId", "parameter.iborderhandler.regionid")
			),
			new MetodData(IBorder.class, "createNew", "method.iborderhandler.createnew",
				new ParameterData(int.class, "dimID", "parameter.dimensionid"),
				new ParameterData(IPos.class, "pos", "parameter.pos")
			)
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
				new ParameterData(int.class, "dimensionId", "parameter.dimensionid")
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
			),
			new MetodData(IPlayer.class, "getIPlayer", "method.npcapi.getiplayer",
				new ParameterData(String.class, "nameOrUUID", "parameter.playername")
			),
			new MetodData(IBorderHandler.class, "getBorders", "method.npcapi.getborders"),
			new MetodData(IAnimationHandler.class, "getAnimations", "method.npcapi.getanimations")
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
				new ParameterData(int.class, "dimensionID", "parameter.dimensionid")
			),
			new MetodData(int[].class, "getAllIDs", "method.idimensionhandler.getallids"),
			new MetodData(void.class, "deleteDimension", "method.idimensionhandler.delete",
				new ParameterData(int.class, "dimensionID", "parameter.dimensionid")
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
