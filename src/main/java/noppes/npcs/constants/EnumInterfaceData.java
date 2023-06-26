package noppes.npcs.constants;

import java.awt.Point;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.IChatMessages;
import noppes.npcs.ICompatibilty;
import noppes.npcs.ability.AbilityBlock;
import noppes.npcs.ability.AbilityPull;
import noppes.npcs.ability.AbilitySmash;
import noppes.npcs.ability.AbilitySnare;
import noppes.npcs.ability.AbilityTeleport;
import noppes.npcs.ability.AbstractAbility;
import noppes.npcs.ability.IAbility;
import noppes.npcs.ability.IAbilityAttack;
import noppes.npcs.ability.IAbilityDamaged;
import noppes.npcs.ability.IAbilityUpdate;
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
import noppes.npcs.api.entity.data.IEmotion;
import noppes.npcs.api.entity.data.IEmotion.IEmotionPart;
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
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.gui.IButton;
import noppes.npcs.api.gui.ICompassData;
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
import noppes.npcs.blocks.BlockBorder;
import noppes.npcs.blocks.BlockBuilder;
import noppes.npcs.blocks.BlockCopy;
import noppes.npcs.blocks.BlockNpcRedstone;
import noppes.npcs.blocks.BlockScripted;
import noppes.npcs.blocks.BlockScriptedDoor;
import noppes.npcs.blocks.BlockWaypoint;
import noppes.npcs.blocks.CustomBlock;
import noppes.npcs.blocks.CustomBlockPortal;
import noppes.npcs.blocks.CustomBlockSlab;
import noppes.npcs.blocks.CustomBlockStairs;
import noppes.npcs.blocks.CustomLiquid;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.blocks.tiles.TileScriptedDoor;
import noppes.npcs.client.RenderChatMessages;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.client.model.animation.EmotionConfig;
import noppes.npcs.client.model.animation.PartEmotion;
import noppes.npcs.client.util.InterfaseData;
import noppes.npcs.client.util.MetodData;
import noppes.npcs.client.util.ParameterData;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.IScriptBlockHandler;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.ClientScriptData;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.ForgeScriptData;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.PlayerCompassHUDData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.PlayerOverlayHUD;
import noppes.npcs.controllers.data.PlayerScriptData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.controllers.data.Zone3D;
import noppes.npcs.dimensions.CustomWorldInfo;
import noppes.npcs.dimensions.DimensionHandler;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.AttributeSet;
import noppes.npcs.entity.data.DataAI;
import noppes.npcs.entity.data.DataAdvanced;
import noppes.npcs.entity.data.DataAnimation;
import noppes.npcs.entity.data.DataDisplay;
import noppes.npcs.entity.data.DataInventory;
import noppes.npcs.entity.data.DataMelee;
import noppes.npcs.entity.data.DataRanged;
import noppes.npcs.entity.data.DataScript;
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
import noppes.npcs.items.ItemBoundary;
import noppes.npcs.items.ItemBuilder;
import noppes.npcs.items.ItemMounter;
import noppes.npcs.items.ItemNbtBook;
import noppes.npcs.items.ItemNpcCloner;
import noppes.npcs.items.ItemNpcMovingPath;
import noppes.npcs.items.ItemNpcScripter;
import noppes.npcs.items.ItemNpcWand;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.items.ItemScriptedDoor;
import noppes.npcs.items.ItemTeleporter;
import noppes.npcs.items.crafting.NpcShapedRecipes;
import noppes.npcs.items.crafting.NpcShapelessRecipes;
import noppes.npcs.potions.CustomPotion;
import noppes.npcs.quests.QuestObjective;
import noppes.npcs.roles.JobBard;
import noppes.npcs.roles.JobBuilder;
import noppes.npcs.roles.JobFarmer;
import noppes.npcs.roles.JobFollower;
import noppes.npcs.roles.JobInterface;
import noppes.npcs.roles.JobSpawner;
import noppes.npcs.roles.RoleDialog;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.roles.RoleInterface;
import noppes.npcs.roles.RoleTransporter;
import noppes.npcs.schematics.Blueprint;
import noppes.npcs.schematics.ISchematic;
import noppes.npcs.schematics.Schematic;
import noppes.npcs.util.IPermission;
import noppes.npcs.util.LayerModel;
import noppes.npcs.util.ScriptData;

public enum EnumInterfaceData {

	IAbility(new InterfaseData(IAbility.class, 
			null,
			new Class<?>[] { AbstractAbility.class },
			"interfase.iability",
			new MetodData(boolean.class, "canRun", "method.iability.canrun",
				new ParameterData(EntityLivingBase.class, "target", "parameter.entity")
			),
			new MetodData(int.class, "getRNG", "method.iability.getrnd"),
			new MetodData(void.class, "startCombat", "method.iability.startCombat"),
			new MetodData(void.class, "endAbility", "method.iability.endability")
		)
	),
	IAbilityAttack(new InterfaseData(IAbilityAttack.class, 
			IAbility.class,
			null,
			"interfase.iabilityattack"
		)
	),
	IAbilityDamaged(new InterfaseData(IAbilityDamaged.class, 
			IAbility.class,
			new Class<?>[] { AbilityBlock.class },
			"interfase.iabilitydamaged",
			new MetodData(void.class, "handleEvent", "method.iabilitydamaged.handleevent",
				new ParameterData(NpcEvent.DamagedEvent.class, "damagedEvent", "parameter.iabilitydamaged.damagedevent")
			)
		)
	),
	IAbilityUpdate(new InterfaseData(IAbilityUpdate.class, 
			IAbility.class,
			new Class<?>[] { AbilityTeleport.class, AbilityPull.class, AbilitySnare.class, AbilitySmash.class },
			"interfase.iabilityupdate",
			new MetodData(boolean.class, "isActive", "method.iabilityupdate.isactive"),
			new MetodData(void.class, "update", "method.iabilityupdate.update")
		)
	),
	IAnimal(new InterfaseData(IAnimal.class, 
			IEntityLiving.class,
			new Class<?>[] { AnimalWrapper.class },
			"interfase.ianimal"
		)
	),
	IAnimation(new InterfaseData(IAnimation.class, 
			null,
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
			new MetodData(int.class, "getType", "method.ianimation.gettype"),
			new MetodData(String.class, "getName", "method.ianimation.getname"),
			new MetodData(void.class, "setName", "method.ianimation.setname",
				new ParameterData(String.class, "name", "parameter.name")
			),
			new MetodData(INbt.class, "getNbt", "method.ianimation.getnbt"),
			new MetodData(void.class, "setNbt", "method.ianimation.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MetodData(void.class, "startToNpc", "method.ianimation.starttonpc",
				new ParameterData(ICustomNpc.class, "npc", "parameter.npc")
			),
			new MetodData(boolean.class, "isDisable", "method.ianimation.isdisable"),
			new MetodData(void.class, "setDisable", "method.ianimation.setdisable",
				new ParameterData(boolean.class, "disabled", "parameter.boolean")
			),
			new MetodData(int.class, "getRepeatLast", "method.ianimation.getrepeatLast"),
			new MetodData(void.class, "setRepeatLast", "method.ianimation.setrepeatLast",
				new ParameterData(int.class, "frames", "parameter.count")
			)
		)
	),
	IAnimationFrame(new InterfaseData(IAnimationFrame.class, 
			null,
			new Class<?>[] { AnimationFrameConfig.class },
			"interfase.ianimationframe",
			new MetodData(boolean.class, "isSmooth", "method.ianimationframe.issmooth"),
			new MetodData(void.class, "setSmooth", "method.ianimationframe.setsmooth",
				new ParameterData(boolean.class, "isSmooth", "parameter.boolean")
			),
			new MetodData(int.class, "getSpeed", "method.ianimationframe.getspeed"),
			new MetodData(void.class, "setSpeed", "method.ianimationframe.setspeed",
				new ParameterData(int.class, "ticks", "parameter.ticks")
			),
			new MetodData(void.class, "setEndDelay", "method.ianimationframe.setenddelay",
				new ParameterData(int.class, "ticks", "parameter.ticks")
			),
			new MetodData(int.class, "getEndDelay", "method.ianimationframe.getenddelay")
		)
	),
	IAnimationHandler(new InterfaseData(IAnimationHandler.class, 
			null,
			new Class<?>[] { AnimationController.class },
			"interfase.ianimationhandler",
			new MetodData(IAnimation[].class, "getAnimations", "method.ianimationhandler.getanimations",
				new ParameterData(int.class, "animationType", "parameter.animation.type")
			),
			new MetodData(IAnimation.class, "getAnimation", "method.ianimationhandler.getanimation",
				new ParameterData(int.class, "animationId", "parameter.animation.id")
			),
			new MetodData(IAnimation.class, "getAnimation", "method.ianimationhandler.getanimation",
				new ParameterData(String.class, "animationName", "parameter.ianimationhandler.animationname")
			),
			new MetodData(boolean.class, "removeAnimation", "method.ianimationhandler.removeanimation",
				new ParameterData(int.class, "animationId", "parameter.animation.id")
			),
			new MetodData(boolean.class, "removeAnimation", "method.ianimationhandler.removeanimation",
				new ParameterData(String.class, "animationName", "parameter.ianimationhandler.animationname")
			),
			new MetodData(IAnimation.class, "createNew", "method.ianimationhandler.createnew",
				new ParameterData(int.class, "animationType", "parameter.animation.type")
			)
		)
	),
	IAnimationPart(new InterfaseData(IAnimationPart.class, 
			null,
			null,
			"interfase.ianimationpart",
			new MetodData(void.class, "clear", "method.ianimationpart.clear"),
			new MetodData(float[].class, "getRotation", "method.ianimationpart.getrotation"),
			new MetodData(float[].class, "getOffset", "method.ianimationpart.getoffset"),
			new MetodData(float[].class, "getScale", "method.ianimationpart.getscale"),
			new MetodData(void.class, "setRotation", "method.ianimationpart.setrotation",
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
			new MetodData(boolean.class, "isDisable", "method.ianimationpart.isdisable"),
			new MetodData(void.class, "setDisable", "method.ianimationpart.setdisable",
				new ParameterData(boolean.class, "bo", "parameter.ianimationpart.bo")
			)
		)
	),
	IArrow(new InterfaseData(IArrow.class, 
			IEntity.class,
			new Class<?>[] { ArrowWrapper.class },
			"interfase.iarrow"
		)
	),
	IAttributeSet(new InterfaseData(IAttributeSet.class, 
			null,
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
			new MetodData(void.class, "setValues", "method.iattributeset.setvalues",
				new ParameterData(double.class, "min", "parameter.iattributeset.min"),
				new ParameterData(double.class, "max", "parameter.iattributeset.max")
			)
		)
	),
	IAvailability(new InterfaseData(IAvailability.class, 
			null,
			new Class<?>[] { Availability.class },
			"interfase.iavailability",
			new MetodData(int[].class, "getDaytime", "method.iavailability.getdaytime"),
			new MetodData(int.class, "getMinPlayerLevel", "method.iavailability.getminplayerlevel"),
			new MetodData(boolean.class, "hasDialog", "method.iavailability.hasdialog",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			),
			new MetodData(boolean.class, "hasFaction", "method.iavailability.hasfaction",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MetodData(boolean.class, "hasQuest", "method.iavailability.hasquest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(boolean.class, "hasScoreboard", "method.iavailability.hasscoreboard",
				new ParameterData(String.class, "objective", "parameter.score.objective")
			),
			new MetodData(boolean.class, "isAvailable", "method.iavailability.isavailable",
				new ParameterData(IPlayer.class, "player", "parameter.player")
			),
			new MetodData(void.class, "removeDialog", "method.iavailability.removedialog",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			),
			new MetodData(void.class, "removeFaction", "method.iavailability.removefaction",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MetodData(void.class, "removeQuest", "method.iavailability.removequest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(void.class, "removeScoreboard", "method.iavailability.removescoreboard",
				new ParameterData(String.class, "objective", "parameter.score.objective")
			),
			new MetodData(void.class, "setDaytime", "method.iavailability.setdaytime",
				new ParameterData(int.class, "type", "parameter.score.daytype")
			),
			new MetodData(void.class, "setDaytime", "method.iavailability.setdaytime",
				new ParameterData(int.class, "minHour", "parameter.min.hour"),
				new ParameterData(int.class, "maxHour", "parameter.max.hour")
			),
			new MetodData(void.class, "setDialog", "method.iavailability.setdialog",
				new ParameterData(int.class, "id", "parameter.dialog.id"),
				new ParameterData(int.class, "type", "parameter.dialog.type")
			),
			new MetodData(void.class, "setFaction", "method.iavailability.setfaction",
				new ParameterData(int.class, "id", "parameter.faction.id"),
				new ParameterData(int.class, "type", "parameter.faction.type"),
				new ParameterData(int.class, "stance", "parameter.faction.stance")
			),
			new MetodData(void.class, "setMinPlayerLevel", "method.iavailability.setminplayerlevel",
				new ParameterData(int.class, "level", "parameter.level")
			),
			new MetodData(void.class, "setQuest", "method.iavailability.setquest",
				new ParameterData(int.class, "id", "parameter.quest.id"),
				new ParameterData(int.class, "type", "parameter.quest.type")
			),
			new MetodData(void.class, "setScoreboard", "method.iavailability.setscoreboard",
				new ParameterData(String.class, "objective", "parameter.score.objective"),
				new ParameterData(int.class, "type", "parameter.score.type"),
				new ParameterData(int.class, "value", "parameter.score")
			),
			new MetodData(int.class, "getHealth", "method.iavailability.gethealth"),
			new MetodData(int.class, "getHealthType", "method.iavailability.gethealthtype"),
			new MetodData(void.class, "setHealth", "method.iavailability.sethealth",
				new ParameterData(int.class, "value", "parameter.health"),
				new ParameterData(int.class, "type", "parameter.health.type")
			)
		)
	),
	IBlock(new InterfaseData(IBlock.class, 
			null,
			new Class<?>[] { BlockWrapper.class },
			"interfase.iblock",
			new MetodData(void.class, "blockEvent", "method.iblock.blockevent",
				new ParameterData(int.class, "type", "parameter.iblock.blockevent.0"),
				new ParameterData(int.class, "data", "parameter.iblock.blockevent.1")
			),
			new MetodData(IContainer.class, "getContainer", "method.iblock.getcontainer"),
			new MetodData(String.class, "getDisplayName", "method.iblock.getdisplayname"),
			new MetodData(Block.class, "getMCBlock", "method.iblock.getmcblock"),
			new MetodData(IBlockState.class, "getMCBlockState", "method.iblock.getmcblockstate"),
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
			new MetodData(void.class, "interact", "method.iblock.interact",
				new ParameterData(int.class, "side", "parameter.iblock.interact")
			),
			new MetodData(boolean.class, "isAir", "method.iblock.isair"),
			new MetodData(boolean.class, "isContainer", "method.iblock.iscontainer"),
			new MetodData(boolean.class, "isRemoved", "method.iblock.isremoved"),
			new MetodData(void.class, "remove", "method.iblock.remove"),
			new MetodData(IBlock.class, "setBlock", "method.iblock.setblock",
				new ParameterData(IBlock.class, "block", "parameter.iblock.setblock.0")
			),
			new MetodData(IBlock.class, "setBlock", "method.iblock.setblock",
				new ParameterData(String.class, "name", "parameter.iblock.setblock.1")
			),
			new MetodData(void.class, "setMetadata", "method.iblock.setmetadata",
				new ParameterData(int.class, "i", "parameter.block.metadata")
			),
			new MetodData(void.class, "setTileEntityNBT", "method.iblock.settileentitynbt",
				new ParameterData(INbt.class, "nbt", "parameter.iblock.settileentitynbt")
			)
		)
	),
	IBlockFluidContainer(new InterfaseData(IBlockFluidContainer.class, 
			IBlock.class,
			new Class<?>[] { BlockFluidContainerWrapper.class },
			"interfase.iblockfluidcontainer",
			new MetodData(String.class, "getFluidName", "method.iblockfluidcontainer.getfluidname"),
			new MetodData(float.class, "getFluidPercentage", "method.iblockfluidcontainer.getfluidpercentage"),
			new MetodData(float.class, "getFluidValue", "method.iblockfluidcontainer.getfluidvalue"),
			new MetodData(float.class, "getFuildDensity", "method.iblockfluidcontainer.getfuilddensity"),
			new MetodData(float.class, "getFuildTemperature", "method.iblockfluidcontainer.getfuildtemperature")
		)
	),
	IBlockScripted(new InterfaseData(IBlockScripted.class, 
			IBlock.class,
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
			new MetodData(float.class, "getResistance", "method.iblockscripted.getresistance"),
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
			new MetodData(void.class, "setModel", "method.iblockscripted.setmodel.0",
				new ParameterData(String.class, "name", "parameter.iblockscripted.itemname")
			),
			new MetodData(void.class, "setRedstonePower", "method.iblockscripted.setredstonepower",
				new ParameterData(int.class, "power", "parameter.iblockscripted.strength")
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
			new MetodData(void.class, "setModel", "method.iblockscripted.setmodel.0",
				new ParameterData(String.class, "blockName", "parameter.iblockscripted.blockname"),
				new ParameterData(int.class, "meta", "parameter.iblockscripted.meta")
			),
			new MetodData(void.class, "setModel", "method.iblockscripted.setmodel.0",
				new ParameterData(IBlock.class, "iblock", "parameter.iblockscripted.iblock")
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
	IBlockScriptedDoor(new InterfaseData(IBlockScriptedDoor.class, 
			IBlock.class,
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
	IBorder(new InterfaseData(IBorder.class, 
			null,
			new Class<?>[] { Zone3D.class },
			"interfase.iborder",
			new MetodData(boolean.class, "contains", "method.iborder.contains",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "z", "parameter.posz")
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
			new MetodData(boolean.class, "contains", "method.iborder.contains",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz"),
				new ParameterData(double.class, "height", "parameter.height")
			),
			new MetodData(void.class, "clear", "method.iborder.removepoint"),
			new MetodData(int.class, "size", "method.iborder.size"),
			new MetodData(IPos.class, "getCenter", "method.iborder.getcenter"),
			new MetodData(int.class, "getMaxZ", "method.iborder.getmaxz"),
			new MetodData(int.class, "getMinZ", "method.iborder.getminz"),
			new MetodData(void.class, "setNbt", "method.iborder.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MetodData(int.class, "getMaxX", "method.iborder.getmaxx"),
			new MetodData(int.class, "getMinX", "method.iborder.getminx"),
			new MetodData(int.class, "getMaxY", "method.iborder.getmaxy"),
			new MetodData(int.class, "getMinY", "method.iborder.getminy"),
			new MetodData(void.class, "scaling", "method.iborder.scaling",
				new ParameterData(float.class, "scale", "parameter.scale"),
				new ParameterData(boolean.class, "type", "parameter.iborder.type")
			),
			new MetodData(void.class, "scaling", "method.iborder.scaling",
				new ParameterData(double.class, "radius", "parameter.range"),
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
				new ParameterData(IPos.class, "position", "parameter.pos")
			),
			new MetodData(boolean.class, "insertPoint", "method.iborder.insertpoint",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz"),
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MetodData(boolean.class, "insertPoint", "method.iborder.insertpoint",
				new ParameterData(Point.class, "point", "parameter.point"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MetodData(boolean.class, "insertPoint", "method.iborder.insertpoint",
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
				new ParameterData(Point.class, "point", "parameter.point")
			),
			new MetodData(Point.class, "setPoint", "method.iborder.setpoint",
				new ParameterData(int.class, "index", "parameter.iborder.index"),
				new ParameterData(Point.class, "point", "parameter.point"),
				new ParameterData(int.class, "y", "parameter.posy")
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
				new ParameterData(int.class, "dimID", "parameter.dimension.id")
			),
			new MetodData(int.class, "getColor", "method.iborder.getcolor"),
			new MetodData(void.class, "setColor", "method.iborder.setcolor",
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MetodData(IAvailability.class, "getAvailability", "method.iborder.getavailability"),
			new MetodData(String.class, "getMessage", "method.iborder.getmessage"),
			new MetodData(void.class, "setMessage", "method.iborder.setmessage",
				new ParameterData(String.class, "message", "parameter.message")
			),
			new MetodData(void.class, "update", "method.iborder.update")
		)
	),
	IBorderHandler(new InterfaseData(IBorderHandler.class, 
			null,
			new Class<?>[] { BorderController.class },
			"interfase.iborderhandler",
			new MetodData(IBorder.class, "getRegion", "method.iborderhandler.getregion",
				new ParameterData(int.class, "regionId", "parameter.iborderhandler.regionid")
			),
			new MetodData(boolean.class, "removeRegion", "method.iborderhandler.removeregion",
				new ParameterData(int.class, "regionId", "parameter.iborderhandler.regionid")
			),
			new MetodData(IBorder[].class, "getRegions", "method.iborderhandler.getregions",
				new ParameterData(int.class, "dimID", "parameter.dimension.id")
			),
			new MetodData(IBorder[].class, "getAllRegions", "method.iborderhandler.getregions"),
			new MetodData(IBorder.class, "createNew", "method.iborderhandler.createnew",
				new ParameterData(int.class, "dimID", "parameter.dimension.id"),
				new ParameterData(IPos.class, "pos", "parameter.pos")
			)
		)
	),
	IButton(new InterfaseData(IButton.class, 
			ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiButtonWrapper.class },
			"interfase.ibutton",
			new MetodData(int.class, "getHeight", "method.component.getheight"),
			new MetodData(String.class, "getLabel", "method.ibutton.getlabel"),
			new MetodData(String.class, "getTexture", "method.component.gettexture"),
			new MetodData(int.class, "getTextureX", "method.component.gettexturex"),
			new MetodData(int.class, "getTextureY", "method.component.gettexturey"),
			new MetodData(int.class, "getWidth", "method.component.getwidth"),
			new MetodData(boolean.class, "hasTexture", "method.ibutton.hastexture"),
			new MetodData(IButton.class, "setLabel", "method.ibutton.setlabel",
				new ParameterData(String.class, "lable", "parameter.component.title")
			),
			new MetodData(IButton.class, "setSize", "method.component.setsize",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MetodData(IButton.class, "setTexture", "method.component.settexture",
				new ParameterData(String.class, "texture", "parameter.texture")
			),
			new MetodData(IButton.class, "setTextureOffset", "method.component.settextureoffset",
				new ParameterData(int.class, "textureX", "parameter.ibutton.texturex"),
				new ParameterData(int.class, "textureY", "parameter.ibutton.texturey")
			)
		)
	),
	IChatMessages(new InterfaseData(IChatMessages.class, 
			null,
			new Class<?>[] { RenderChatMessages.class },
			"interfase.ichatmessages",
			new MetodData(void.class, "addMessage", "method.ichatmessages.addmessage",
				new ParameterData(String.class, "message", "parameter.ichatmessages.message"),
				new ParameterData(EntityNPCInterface.class, "npc", "parameter.ichatmessages.npc")
			),
			new MetodData(void.class, "renderMessages", "method.ichatmessages.rendermessages",
				new ParameterData(double.class, "x", "parameter.ichatmessages.x"),
				new ParameterData(double.class, "y", "parameter.ichatmessages.y"),
				new ParameterData(double.class, "z", "parameter.ichatmessages.z"),
				new ParameterData(float.class, "height", "parameter.ichatmessages.height"),
				new ParameterData(boolean.class, "inRange", "parameter.ichatmessages.inrange")
			)
		)
	),
	ICloneHandler(new InterfaseData(ICloneHandler.class, 
			null,
			new Class<?>[] { ServerCloneController.class },
			"interfase.iclonehandler",
			new MetodData(IEntity.class, "get", "method.iclone.get",
				new ParameterData(int.class, "tab", "parameter.iclone.tab"),
				new ParameterData(String.class, "name", "parameter.iclone.name"),
				new ParameterData(IWorld.class, "world", "parameter.world")
			),
			new MetodData(void.class, "remove", "method.iclone.remove",
				new ParameterData(int.class, "tab", "parameter.iclone.tab"),
				new ParameterData(String.class, "name", "parameter.iclone.name")
			),
			new MetodData(void.class, "set", "method.iclone.set",
				new ParameterData(int.class, "tab", "parameter.iclone.tab"),
				new ParameterData(String.class, "name", "parameter.iclone.name"),
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MetodData(IEntity.class, "spawn", "method.iclone.spawn",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz"),
				new ParameterData(int.class, "tab", "parameter.iclone.tab"),
				new ParameterData(String.class, "name", "parameter.iclone.name"),
				new ParameterData(IWorld.class, "world", "parameter.world")
			)
		)
	),
	ICompassData(new InterfaseData(ICompassData.class, 
			null,
			new Class<?>[] { PlayerCompassHUDData.class },
			"interfase.icompassdata",
			new MetodData(int.class, "getDimensionID", "method.icompassdata.getdimensionid"),
			new MetodData(void.class, "setDimensionID", "method.icompassdata.setdimensionid",
				new ParameterData(int.class, "dimID", "parameter.icompassdata.dimid")
			),
			new MetodData(int.class, "getType", "method.icompassdata.gettype"),
			new MetodData(void.class, "setType", "method.icompassdata.settype",
				new ParameterData(int.class, "type", "parameter.icompassdata.type")
			),
			new MetodData(int.class, "getRange", "method.icompassdata.getrange"),
			new MetodData(void.class, "setRange", "method.icompassdata.setrange",
				new ParameterData(int.class, "range", "parameter.icompassdata.range")
			),
			new MetodData(IPos.class, "getPos", "method.icompassdata.getpos"),
			new MetodData(void.class, "setPos", "method.icompassdata.setpos",
				new ParameterData(IPos.class, "pos", "parameter.icompassdata.pos")
			),
			new MetodData(void.class, "setPos", "method.icompassdata.setpos",
				new ParameterData(int.class, "x", "parameter.icompassdata.x"),
				new ParameterData(int.class, "y", "parameter.icompassdata.y"),
				new ParameterData(int.class, "z", "parameter.icompassdata.z")
			),
			new MetodData(String.class, "getName", "method.icompassdata.getname"),
			new MetodData(void.class, "setName", "method.icompassdata.setname",
				new ParameterData(String.class, "name", "parameter.icompassdata.name")
			),
			new MetodData(String.class, "getTitle", "method.icompassdata.gettitle"),
			new MetodData(void.class, "setTitle", "method.icompassdata.settitle",
				new ParameterData(String.class, "title", "parameter.icompassdata.title")
			),
			new MetodData(void.class, "setShow", "method.icompassdata.setshow",
				new ParameterData(boolean.class, "show", "parameter.icompassdata.show")
			),
			new MetodData(boolean.class, "isShow", "method.icompassdata.isshow"),
			new MetodData(String.class, "getNPCName", "method.icompassdata.getnpcname"),
			new MetodData(void.class, "setNPCName", "method.icompassdata.setnpcname",
				new ParameterData(String.class, "npcName", "parameter.icompassdata.npcname")
			)
		)
	),
	ICompatibilty(new InterfaseData(ICompatibilty.class, 
			null,
			new Class<?>[] { Quest.class, Dialog.class, Availability.class },
			"interfase.icompatibilty",
			new MetodData(int.class, "getVersion", "method.icompatibilty.getversion"),
			new MetodData(void.class, "setVersion", "method.icompatibilty.setversion",
				new ParameterData(int.class, "version", "parameter.icompatibilty.version")
			),
			new MetodData(NBTTagCompound.class, "writeToNBT", "method.icompatibilty.writetonbt",
				new ParameterData(NBTTagCompound.class, "nbt", "parameter.icompatibilty.nbt")
			)
		)
	),
	IContainer(new InterfaseData(IContainer.class, 
			null,
			new Class<?>[] { ContainerWrapper.class },
			"interfase.icontainer",
			new MetodData(int.class, "count", "method.icontainer.count",
				new ParameterData(IItemStack.class, "item", "parameter.item.found"),
				new ParameterData(boolean.class, "ignoreDamage", "parameter.ignoredamage"),
				new ParameterData(boolean.class, "ignoreNBT", "parameter.ignorenbt")
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
	IContainerCustomChest(new InterfaseData(IContainerCustomChest.class, 
			IContainer.class,
			new Class<?>[] { ContainerCustomChestWrapper.class },
			"interfase.icontainercustomchest",
			new MetodData(String.class, "getName", "method.icontainercustomchest.getname"),
			new MetodData(void.class, "setName", "method.icontainercustomchest.setname",
				new ParameterData(String.class, "name", "parameter.icontainercustomchest.name")
			)
		)
	),
	ICustomDrop(new InterfaseData(ICustomDrop.class, 
			null,
			new Class<?>[] { DropSet.class },
			"interfase.icustomdrop",
			new MetodData(IAttributeSet.class, "addAttribute", "method.icustomdrop.addattribute",
				new ParameterData(String.class, "attributeName", "parameter.attribute.name")
			),
			new MetodData(IDropNbtSet.class, "addDropNbtSet", "method.icustomdrop.adddropnbtset",
				new ParameterData(int.class, "type", "parameter.idropnbtset.type"),
				new ParameterData(double.class, "chance", "parameter.chance"),
				new ParameterData(String.class, "paht", "parameter.idropnbtset.paht"),
				new ParameterData(String[].class, "values", "parameter.idropnbtset.values")
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
			new MetodData(IItemStack.class, "getItem", "method.icustomdrop.getitem"),
			new MetodData(boolean.class, "getLootMode", "method.icustomdrop.getlootmode"),
			new MetodData(int.class, "getMaxAmount", "method.icustomdrop.getmaxamount"),
			new MetodData(int.class, "getMinAmount", "method.icustomdrop.getminamount"),
			new MetodData(int.class, "getQuestID", "method.icustomdrop.getquestid"),
			new MetodData(boolean.class, "getTiedToLevel", "method.icustomdrop.gettiedtolevel"),
			new MetodData(void.class, "remove", "method.icustomdrop.remove"),
			new MetodData(void.class, "removeAttribute", "method.icustomdrop.removeattribute",
				new ParameterData(IAttributeSet.class, "attribute", "parameter.icustomdrop.attribute")
			),
			new MetodData(void.class, "removeDropNbt", "method.icustomdrop.removedropnbt",
				new ParameterData(IDropNbtSet.class, "nbt", "parameter.icustomdrop.nbt")
			),
			new MetodData(void.class, "removeEnchant", "method.icustomdrop.removeenchant",
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
				new ParameterData(float.class, "dam", "parameter.icustomdrop.dam")
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
	ICustomElement(new InterfaseData(ICustomElement.class, 
			null,
			new Class<?>[] { CustomBlockSlab.class, CustomLiquid.class, CustomBow.class, CustomItem.class, CustomPotion.class, CustomTool.class, CustomBlockStairs.class, CustomWeapon.class, CustomFood.class, CustomBlock.class, CustomFishingRod.class, CustomShield.class, CustomArmor.class, CustomBlockPortal.class },
			"interfase.icustomelement",
			new MetodData(String.class, "getCustomName", "method.icustomelement.getcustomname"),
			new MetodData(INbt.class, "getCustomNbt", "method.icustomelement.getcustomnbt")
		)
	),
	ICustomGui(new InterfaseData(ICustomGui.class, 
			null,
			new Class<?>[] { CustomGuiWrapper.class },
			"interfase.icustomgui",
			new MetodData(IButton.class, "addButton", "method.icustomgui.addbutton",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(String.class, "label", "parameter.component.title"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy")
			),
			new MetodData(IButton.class, "addButton", "method.icustomgui.addbutton",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(String.class, "label", "parameter.component.title"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MetodData(IItemSlot.class, "addItemSlot", "method.icustomgui.additemslot",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy")
			),
			new MetodData(IItemSlot.class, "addItemSlot", "method.icustomgui.additemslot",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(IItemStack.class, "stack", "parameter.stack")
			),
			new MetodData(ILabel.class, "addLabel", "method.icustomgui.addlabel",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(String.class, "label", "parameter.component.title"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MetodData(ILabel.class, "addLabel", "method.icustomgui.addlabel",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(String.class, "label", "parameter.component.title"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MetodData(IScroll.class, "addScroll", "method.icustomgui.addscroll",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(String[].class, "list", "parameter.list")
			),
			new MetodData(ITextField.class, "addTextField", "method.icustomgui.addtextfield",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MetodData(IButton.class, "addTexturedButton", "method.icustomgui.addtexturedbutton",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(String.class, "label", "parameter.component.title"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(String.class, "texture", "parameter.texture")
			),
			new MetodData(IButton.class, "addTexturedButton", "method.icustomgui.addtexturedbutton",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(String.class, "label", "parameter.component.title"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(String.class, "texture", "parameter.texture"),
				new ParameterData(int.class, "textureX", "parameter.texturex"),
				new ParameterData(int.class, "textureY", "parameter.texturey")
			),
			new MetodData(ITexturedRect.class, "addTexturedRect", "method.icustomgui.addtexturedrect",
				new ParameterData(int.class, "id", "parameter.icustomgui.id"),
				new ParameterData(String.class, "texture", "parameter.icustomgui.texture"),
				new ParameterData(int.class, "x", "parameter.icustomgui.x"),
				new ParameterData(int.class, "y", "parameter.icustomgui.y"),
				new ParameterData(int.class, "width", "parameter.icustomgui.width"),
				new ParameterData(int.class, "height", "parameter.icustomgui.height")
			),
			new MetodData(ITexturedRect.class, "addTexturedRect", "method.icustomgui.addtexturedrect",
				new ParameterData(int.class, "id", "parameter.icustomgui.id"),
				new ParameterData(String.class, "texture", "parameter.icustomgui.texture"),
				new ParameterData(int.class, "x", "parameter.icustomgui.x"),
				new ParameterData(int.class, "y", "parameter.icustomgui.y"),
				new ParameterData(int.class, "width", "parameter.icustomgui.width"),
				new ParameterData(int.class, "height", "parameter.icustomgui.height"),
				new ParameterData(int.class, "textureX", "parameter.icustomgui.texturex"),
				new ParameterData(int.class, "textureY", "parameter.icustomgui.texturey")
			),
			new MetodData(ICustomGuiComponent.class, "getComponent", "method.icustomgui.getcomponent",
				new ParameterData(int.class, "id", "parameter.component.id")
			),
			new MetodData(ICustomGuiComponent[].class, "getComponents", "method.icustomgui.getcomponents"),
			new MetodData(int.class, "getHeight", "method.component.getheight"),
			new MetodData(int.class, "getID", "method.icustomgui.getid"),
			new MetodData(IItemSlot[].class, "getSlots", "method.icustomgui.getslots"),
			new MetodData(int.class, "getWidth", "method.component.getwidth"),
			new MetodData(void.class, "removeComponent", "method.icustomgui.removecomponent",
				new ParameterData(int.class, "id", "parameter.component.id")
			),
			new MetodData(void.class, "setBackgroundTexture", "method.icustomgui.setbackgroundtexture",
				new ParameterData(String.class, "resourceLocation", "parameter.texture")
			),
			new MetodData(void.class, "setBackgroundTexture", "method.icustomgui.setbackgroundtexture",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(int.class, "textureX", "parameter.texturex"),
				new ParameterData(int.class, "textureY", "parameter.texturey"),
				new ParameterData(int.class, "stretched", "parameter.component.stretched"),
				new ParameterData(String.class, "resourceLocation", "parameter.texture")
			),
			new MetodData(void.class, "setDoesPauseGame", "method.icustomgui.setdoespausegame",
				new ParameterData(boolean.class, "pauseGame", "parameter.icustomgui.pausegame")
			),
			new MetodData(void.class, "setSize", "method.component.setsize",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MetodData(void.class, "showPlayerInventory", "method.icustomgui.showplayerinventory",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy")
			),
			new MetodData(void.class, "update", "method.icustomgui.update",
				new ParameterData(IPlayer.class, "player", "parameter.player")
			),
			new MetodData(void.class, "updateComponent", "method.icustomgui.updatecomponent",
				new ParameterData(ICustomGuiComponent.class, "component", "parameter.component")
			)
		)
	),
	ICustomGuiComponent(new InterfaseData(ICustomGuiComponent.class, 
			null,
			new Class<?>[] { CustomGuiComponentWrapper.class },
			"interfase.icustomguicomponent",
			new MetodData(String[].class, "getHoverText", "method.icustomguicom.getHoverText"),
			new MetodData(int.class, "getID", "method.icustomguicom.getid"),
			new MetodData(int.class, "getPosX", "method.icustomguicom.getposx"),
			new MetodData(int.class, "getPosY", "method.icustomguicom.getposy"),
			new MetodData(boolean.class, "hasHoverText", "method.icustomguicom.hashovertext"),
			new MetodData(ICustomGuiComponent.class, "setHoverText", "method.icustomguicom.setHovertext",
				new ParameterData(String.class, "hover", "parameter.hover")
			),
			new MetodData(ICustomGuiComponent.class, "setHoverText", "method.icustomguicom.setHovertext",
				new ParameterData(String[].class, "hovers", "parameter.icustomguicomponent.hovers")
			),
			new MetodData(ICustomGuiComponent.class, "setID", "method.icustomguicom.setid",
				new ParameterData(int.class, "id", "parameter.component.id")
			),
			new MetodData(ICustomGuiComponent.class, "setPos", "method.icustomguicom.setpos",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy")
			),
			new MetodData(void.class, "offSet", "method.icustomguicom.offset",
				new ParameterData(int.class, "type", "parameter.component.offset")
			)
		)
	),
	ICustomNpc(new InterfaseData(ICustomNpc.class, 
			IEntityLiving.class,
			new Class<?>[] { NPCWrapper.class },
			"interfase.icustomnpc",
			new MetodData(String.class, "executeCommand", "method.executecommand",
				new ParameterData(String.class, "command", "parameter.icustomnpc.command")
			),
			new MetodData(INPCAdvanced.class, "getAdvanced", "method.icustomnpc.getadvanced"),
			new MetodData(INPCAi.class, "getAi", "method.icustomnpc.getai"),
			new MetodData(IDialog.class, "getDialog", "method.icustomnpc.getdialog",
				new ParameterData(int.class, "id", "parameter.icustomnpc.id")
			),
			new MetodData(INPCDisplay.class, "getDisplay", "method.icustomnpc.getdisplay"),
			new MetodData(IFaction.class, "getFaction", "method.icustomnpc.getfaction"),
			new MetodData(int.class, "getHomeX", "method.icustomnpc.gethomex"),
			new MetodData(int.class, "getHomeY", "method.icustomnpc.gethomey"),
			new MetodData(int.class, "getHomeZ", "method.icustomnpc.gethomez"),
			new MetodData(INPCInventory.class, "getInventory", "method.icustomnpc.getinventory"),
			new MetodData(INPCJob.class, "getJob", "method.icustomnpc.getjob"),
			new MetodData(IEntityLivingBase.class, "getOwner", "method.icustomnpc.getowner"),
			new MetodData(INPCRole.class, "getRole", "method.icustomnpc.getrole"),
			new MetodData(INPCStats.class, "getStats", "method.icustomnpc.getstats"),
			new MetodData(INPCAnimation.class, "getAnimations", "method.getanimations"),
			new MetodData(ITimers.class, "getTimers", "method.icustomnpc.gettimers"),
			new MetodData(void.class, "giveItem", "method.icustomnpc.giveitem",
				new ParameterData(IPlayer.class, "player", "parameter.icustomnpc.player"),
				new ParameterData(IItemStack.class, "item", "parameter.icustomnpc.item")
			),
			new MetodData(void.class, "reset", "method.icustomnpc.reset"),
			new MetodData(void.class, "say", "method.icustomnpc.say",
				new ParameterData(String.class, "message", "parameter.icustomnpc.message")
			),
			new MetodData(void.class, "sayTo", "method.icustomnpc.sayto",
				new ParameterData(IPlayer.class, "player", "parameter.icustomnpc.player"),
				new ParameterData(String.class, "message", "parameter.icustomnpc.message")
			),
			new MetodData(void.class, "setDialog", "method.icustomnpc.setdialog",
				new ParameterData(int.class, "id", "parameter.icustomnpc.id"),
				new ParameterData(IDialog.class, "dialofg", "parameter.icustomnpc.dialofg")
			),
			new MetodData(void.class, "setFaction", "method.icustomnpc.setfaction",
				new ParameterData(int.class, "id", "parameter.icustomnpc.id")
			),
			new MetodData(void.class, "setHome", "method.icustomnpc.sethome",
				new ParameterData(int.class, "x", "parameter.icustomnpc.x"),
				new ParameterData(int.class, "y", "parameter.icustomnpc.y"),
				new ParameterData(int.class, "z", "parameter.icustomnpc.z")
			),
			new MetodData(IProjectile.class, "shootItem", "method.icustomnpc.shootitem",
				new ParameterData(double.class, "x", "parameter.icustomnpc.x"),
				new ParameterData(double.class, "y", "parameter.icustomnpc.y"),
				new ParameterData(double.class, "z", "parameter.icustomnpc.z"),
				new ParameterData(IItemStack.class, "item", "parameter.icustomnpc.item"),
				new ParameterData(int.class, "count", "parameter.icustomnpc.count")
			),
			new MetodData(IProjectile.class, "shootItem", "method.icustomnpc.shootitem",
				new ParameterData(IEntityLivingBase.class, "entity", "parameter.icustomnpc.entity"),
				new ParameterData(IItemStack.class, "item", "parameter.icustomnpc.item"),
				new ParameterData(int.class, "count", "parameter.icustomnpc.count")
			),
			new MetodData(void.class, "updateClient", "method.icustomnpc.updateclient"),
			new MetodData(void.class, "trigger", "method.trigger",
				new ParameterData(int.class, "id", "parameter.trigger.id"),
				new ParameterData(Object[].class, "arguments", "parameter.trigger.arguments")
			)
		)
	),
	IDamageSource(new InterfaseData(IDamageSource.class, 
			null,
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
	IData(new InterfaseData(IData.class, 
			null,
			null,
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
	IDataElement(new InterfaseData(IDataElement.class, 
			null,
			new Class<?>[] { DataElement.class },
			"interfase.idataelement",
			new MetodData(String.class, "getData", "method.idatablement.getdata"),
			new MetodData(String.class, "getName", "method.idatablement.getname"),
			new MetodData(Object.class, "getObject", "method.idatablement.getobject"),
			new MetodData(Class.class, "getParent", "method.idatablement.getparent"),
			new MetodData(int.class, "getType", "method.idatablement.gettype"),
			new MetodData(Object.class, "getValue", "method.idatablement.getvalue"),
			new MetodData(Object.class, "invoke", "method.idatablement.getobject",
				new ParameterData(Object[].class, "values", "parameter.idatablement.values")
			),
			new MetodData(boolean.class, "isBelong", "method.idatablement.isbelong",
				new ParameterData(Class.class, "clazz", "parameter.idatablement.clazz")
			),
			new MetodData(boolean.class, "setValue", "method.idatablement.setvalue",
				new ParameterData(Object.class, "value", "parameter.idatablement.value")
			)
		)
	),
	IDataObject(new InterfaseData(IDataObject.class, 
			null,
			new Class<?>[] { DataObject.class },
			"interfase.idataobject",
			new MetodData(String.class, "get", "method.idataobject.get"),
			new MetodData(IDataElement[].class, "getClasses", "method.idataobject.getclasses"),
			new MetodData(String.class, "getClassesInfo", "method.idataobject.getclassesinfo"),
			new MetodData(IDataElement.class, "getClazz", "method.idataobject.getclazz",
				new ParameterData(String.class, "name", "parameter.class.name")
			),
			new MetodData(IDataElement[].class, "getConstructors", "method.idata.getconstructors"),
			new MetodData(String.class, "getConstructorsInfo", "method.idata.getconstructorsinfo"),
			new MetodData(IDataElement.class, "getField", "method.idata.getfield",
				new ParameterData(String.class, "name", "parameter.field.name")
			),
			new MetodData(IDataElement[].class, "getFields", "method.idata.getfields"),
			new MetodData(String.class, "getFieldsInfo", "method.idata.getfieldsinfo"),
			new MetodData(String.class, "getInfo", "method.idata.getinfo"),
			new MetodData(IDataElement.class, "getMethod", "method.idata.getmethod",
				new ParameterData(String.class, "name", "parameter.method.name")
			),
			new MetodData(IDataElement[].class, "getMethods", "method.idata.getmethods"),
			new MetodData(String.class, "getMethodsInfo", "method.idata.getmethodsinfo")
		)
	),
	IDialog(new InterfaseData(IDialog.class, 
			null,
			new Class<?>[] { Dialog.class },
			"interfase.idialog",
			new MetodData(IAvailability.class, "getAvailability", "method.idialog.getavailability"),
			new MetodData(IDialogCategory.class, "getCategory", "method.idialog.getcategory"),
			new MetodData(String.class, "getCommand", "method.idialog.getcommand"),
			new MetodData(int.class, "getId", "method.idialog.getid"),
			new MetodData(String.class, "getName", "method.idialog.getname"),
			new MetodData(IDialogOption.class, "getOption", "method.idialog.getOption",
				new ParameterData(int.class, "slot", "parameter.idialog.slot")
			),
			new MetodData(IDialogOption[].class, "getOptions", "method.idialog.getoptions"),
			new MetodData(IQuest.class, "getQuest", "method.idialog.getquest"),
			new MetodData(String.class, "getText", "method.idialog.gettext"),
			new MetodData(void.class, "save", "method.idialog.save"),
			new MetodData(void.class, "setCommand", "method.idialog.setcommand",
				new ParameterData(String.class, "command", "parameter.command")
			),
			new MetodData(void.class, "setName", "method.idialog.setname",
				new ParameterData(String.class, "name", "parameter.name")
			),
			new MetodData(void.class, "setQuest", "method.idialog.setquest",
				new ParameterData(IQuest.class, "quest", "parameter.quest")
			),
			new MetodData(void.class, "setText", "method.idialog.settext",
				new ParameterData(String.class, "text", "parameter.text")
			)
		)
	),
	IDialogCategory(new InterfaseData(IDialogCategory.class, 
			null,
			new Class<?>[] { DialogCategory.class },
			"interfase.idialogcategory",
			new MetodData(IDialog.class, "create", "method.idialogcategory.create"),
			new MetodData(IDialog[].class, "dialogs", "method.idialogcategory.dialogs"),
			new MetodData(String.class, "getName", "method.idialogcategory.getname")
		)
	),
	IDialogHandler(new InterfaseData(IDialogHandler.class, 
			null,
			new Class<?>[] { DialogController.class },
			"interfase.idialoghandler",
			new MetodData(IDialogCategory[].class, "categories", "method.idialoghandler.categories"),
			new MetodData(IDialog.class, "get", "method.idialoghandler.get",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			)
		)
	),
	IDialogOption(new InterfaseData(IDialogOption.class, 
			null,
			new Class<?>[] { DialogOption.class },
			"interfase.idialogoption",
			new MetodData(String.class, "getName", "method.idialogoption.getname"),
			new MetodData(int.class, "getSlot", "method.idialogoption.getslot"),
			new MetodData(int.class, "getType", "method.idialogoption.gettype")
		)
	),
	IDimension(new InterfaseData(IDimension.class, 
			null,
			new Class<?>[] { DimensionWrapper.class },
			"interfase.idimension",
			new MetodData(int.class, "getId", "method.idimension.getid"),
			new MetodData(String.class, "getName", "method.idimension.getname"),
			new MetodData(String.class, "getSuffix", "method.idimension.getsuffix")
		)
	),
	IDimensionHandler(new InterfaseData(IDimensionHandler.class, 
			null,
			new Class<?>[] { DimensionHandler.class },
			"interfase.idimensionhandler",
			new MetodData(IWorldInfo.class, "createDimension", "method.idimensionhandler.create"),
			new MetodData(void.class, "setNbt", "method.idimensionhandler.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MetodData(INbt.class, "getNbt", "method.idimensionhandler.getnbt"),
			new MetodData(IWorldInfo.class, "getMCWorldInfo", "method.idimensionhandler.getmcworldinfo",
				new ParameterData(int.class, "dimensionID", "parameter.dimension.id")
			),
			new MetodData(int[].class, "getAllIDs", "method.idimensionhandler.getallids"),
			new MetodData(void.class, "deleteDimension", "method.idimensionhandler.delete",
				new ParameterData(int.class, "dimensionID", "parameter.dimension.id")
			)
		)
	),
	IDropNbtSet(new InterfaseData(IDropNbtSet.class, 
			null,
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
	IEmotion(new InterfaseData(IEmotion.class, 
			null,
			new Class<?>[] { EmotionConfig.class },
			"interfase.iemotion"
		)
	),
	IEmotionPart(new InterfaseData(IEmotionPart.class, 
			null,
			new Class<?>[] { PartEmotion.class },
			"interfase.iemotionpart"
		)
	),
	IEnchantSet(new InterfaseData(IEnchantSet.class, 
			null,
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
			new MetodData(void.class, "setEnchant", "method.ienchantset.setenchant",
				new ParameterData(Enchantment.class, "enchant", "parameter.enchant")
			),
			new MetodData(boolean.class, "setEnchant", "method.ienchantset.setenchant",
				new ParameterData(int.class, "id", "parameter.enchant.id")
			),
			new MetodData(boolean.class, "setEnchant", "method.ienchantset.setenchant",
				new ParameterData(String.class, "name", "parameter.enchant.name")
			),
			new MetodData(void.class, "setLevels", "method.ienchantset.setlevels",
				new ParameterData(int.class, "min", "parameter.min"),
				new ParameterData(int.class, "max", "parameter.max")
			)
		)
	),
	IEntity(new InterfaseData(IEntity.class, 
			Entity.class,
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
			new MetodData(IEntity.class, "getAllRiders", "method.ientity.getallriders"),
			new MetodData(int.class, "getBlockX", "method.ientity.getblockx"),
			new MetodData(int.class, "getBlockY", "method.ientity.getblocky"),
			new MetodData(int.class, "getBlockZ", "method.ientity.getblockz"),
			new MetodData(String.class, "getEntityName", "method.ientity.getentityname"),
			new MetodData(INbt.class, "getEntityNbt", "method.ientity.getentitynbt"),
			new MetodData(float.class, "getEyeHeight", "method.ientity.geteyeheight"),
			new MetodData(float.class, "getHeight", "method.ientity.getheight"),
			new MetodData(Entity.class, "getMCEntity", "method.ientity.getmcentity"),
			new MetodData(double.class, "getMotionX", "method.ientity.getmotionx"),
			new MetodData(double.class, "getMotionY", "method.ientity.getmotiony"),
			new MetodData(double.class, "getMotionZ", "method.ientity.getmotionz"),
			new MetodData(IEntity.class, "getMount", "method.ientity.getmount"),
			new MetodData(String.class, "getName", "method.ientity.getname"),
			new MetodData(INbt.class, "getNbt", "method.ientity.getnbt"),
			new MetodData(float.class, "getPitch", "method.ientity.getpitch"),
			new MetodData(IPos.class, "getPos", "method.getpos"),
			new MetodData(IEntity.class, "getRiders", "method.ientity.getriders"),
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
			new MetodData(IEntity.class, "rayTraceEntities", "method.ientity.raytraceentities",
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
				new ParameterData(double.class, "x", "parameter.posx")
			),
			new MetodData(void.class, "setY", "method.sety",
				new ParameterData(double.class, "y", "parameter.posy")
			),
			new MetodData(void.class, "setZ", "method.setz",
				new ParameterData(double.class, "z", "parameter.posz")
			),
			new MetodData(void.class, "spawn", "method.ientity.spawn"),
			new MetodData(void.class, "storeAsClone", "method.ientity.storeasclone",
				new ParameterData(int.class, "tab", "parameter.clone.tab"),
				new ParameterData(String.class, "name", "parameter.clone.file")
			),
			new MetodData(boolean.class, "typeOf", "method.ientity.typeof",
				new ParameterData(int.class, "type", "parameter.ientity.typeof")
			)
		)
	),
	IEntityDamageSource(new InterfaseData(IEntityDamageSource.class, 
			null,
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
			)
		)
	),
	IEntityItem(new InterfaseData(IEntityItem.class, 
			IEntityLiving.class,
			new Class<?>[] { EntityItemWrapper.class },
			"interfase.ientityitem",
			new MetodData(long.class, "getAge", "method.ientity.getage"),
			new MetodData(IItemStack.class, "getItem", "method.ientityitem.getitem"),
			new MetodData(int.class, "getLifeSpawn", "method.ientityitem.getlifespawn"),
			new MetodData(String.class, "getOwner", "method.ientityitem.getowner"),
			new MetodData(int.class, "getPickupDelay", "method.ientityitem.getpickupdelay"),
			new MetodData(void.class, "setAge", "method.ientity.setage",
				new ParameterData(long.class, "age", "parameter.ticks")
			),
			new MetodData(void.class, "setItem", "method.ientityitem.setquest",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MetodData(void.class, "setLifeSpawn", "method.ientityitem.setlifespawn",
				new ParameterData(int.class, "age", "parameter.ticks")
			),
			new MetodData(void.class, "setOwner", "method.ientityitem.setowner",
				new ParameterData(String.class, "name", "parameter.playername")
			),
			new MetodData(void.class, "setPickupDelay", "method.ientityitem.setpickupdelay",
				new ParameterData(int.class, "delay", "parameter.ticks")
			)
		)
	),
	IEntityLiving(new InterfaseData(IEntityLiving.class, 
			IEntity.class,
			new Class<?>[] { EntityLivingWrapper.class },
			"interfase.ientityliving",
			new MetodData(void.class, "clearNavigation", "method.ientityliving.clearnavigation"),
			new MetodData(EntityLivingBase.class, "getMCEntity", "method.ientity.getmcentity"),
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
	IEntityLivingBase(new InterfaseData(IEntityLivingBase.class, 
			IEntityLiving.class,
			new Class<?>[] { EntityLivingBaseWrapper.class },
			"interfase.ientitylivingbase",
			new MetodData(IMark.class, "addMark", "method.ientitylivingbase.addmark",
				new ParameterData(int.class, "type", "parameter.mark.type")
			),
			new MetodData(void.class, "addPotionEffect", "method.ientitylivingbase.addpotioneffect",
				new ParameterData(int.class, "effect", "parameter.effect.id"),
				new ParameterData(int.class, "duration", "parameter.effect.duration"),
				new ParameterData(int.class, "strength", "parameter.effect.strength"),
				new ParameterData(boolean.class, "hideParticles", "parameter.effect.hideparticles")
			),
			new MetodData(boolean.class, "canSeeEntity", "method.ientitylivingbase.canseeentity",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MetodData(void.class, "clearPotionEffects", "method.ientitylivingbase.clearpotioneffects"),
			new MetodData(IItemStack.class, "getArmor", "method.ientitylivingbase.getarmor",
				new ParameterData(int.class, "slot", "parameter.ientitylivingbase.slot")
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
				new ParameterData(int.class, "slot", "parameter.ientitylivingbase.slot"),
				new ParameterData(IItemStack.class, "item", "parameter.ientitylivingbase.item")
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
			new MetodData(void.class, "swingOffhand", "method.ientitylivingbase.swingoffhand")
		)
	),
	IFaction(new InterfaseData(IFaction.class, 
			null,
			new Class<?>[] { Faction.class },
			"interfase.ifaction",
			new MetodData(void.class, "addHostile", "method.ifaction.addhostile",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MetodData(boolean.class, "getAttackedByMobs", "method.ifaction.getAttackedByMobs"),
			new MetodData(int.class, "getColor", "method.ifaction.getcolor"),
			new MetodData(int.class, "getDefaultPoints", "method.ifaction.getdefaultpoints"),
			new MetodData(int[].class, "getHostileList", "method.ifaction.gethostilelist"),
			new MetodData(int.class, "getId", "method.ifaction.getid"),
			new MetodData(boolean.class, "getIsHidden", "method.ifaction.getishidden"),
			new MetodData(String.class, "getName", "method.ifaction.getname"),
			new MetodData(boolean.class, "hasHostile", "method.ifaction.hashostile",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MetodData(boolean.class, "hostileToFaction", "method.ifaction.hostiletofaction",
				new ParameterData(int.class, "factionId", "parameter.faction.id")
			),
			new MetodData(boolean.class, "hostileToNpc", "method.ifaction.hostiletonpc",
				new ParameterData(ICustomNpc.class, "npc", "parameter.npc")
			),
			new MetodData(int.class, "playerStatus", "method.ifaction.playerstatus",
				new ParameterData(IPlayer.class, "player", "parameter.player")
			),
			new MetodData(void.class, "removeHostile", "method.ifaction.removehostile",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MetodData(void.class, "save", "method.ifaction.save"),
			new MetodData(void.class, "setAttackedByMobs", "method.ifaction.setattackedbymobs",
				new ParameterData(boolean.class, "bo", "parameter.ifaction.bo")
			),
			new MetodData(void.class, "setDefaultPoints", "method.ifaction.setdefaultpoints",
				new ParameterData(int.class, "points", "parameter.ifaction.points")
			),
			new MetodData(void.class, "setIsHidden", "method.ifaction.setIsHidden",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			)
		)
	),
	IFactionHandler(new InterfaseData(IFactionHandler.class, 
			null,
			new Class<?>[] { FactionController.class },
			"interfase.ifactionhandler",
			new MetodData(IFaction.class, "create", "method.ifactionhandler.get",
				new ParameterData(String.class, "name", "parameter.faction.name"),
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MetodData(IFaction.class, "delete", "method.ifactionhandler.delete",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MetodData(IFaction.class, "get", "method.ifactionhandler.get",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MetodData(IFaction[].class, "list", "method.ifactionhandler.list")
		)
	),
	IGuiTimer(new InterfaseData(IGuiTimer.class, 
			ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiTimerWrapper.class },
			"interfase.iguitimer",
			new MetodData(void.class, "setTime", "method.iguitimer.settime",
				new ParameterData(long.class, "start", "parameter.posx"),
				new ParameterData(long.class, "end", "parameter.posy")
			),
			new MetodData(int.class, "getColor", "method.iguitimer.getcolor"),
			new MetodData(int.class, "getHeight", "method.component.getheight"),
			new MetodData(float.class, "getScale", "method.component.getscale"),
			new MetodData(String.class, "getText", "method.component.gettext"),
			new MetodData(int.class, "getWidth", "method.component.getwidth"),
			new MetodData(IGuiTimer.class, "setColor", "method.component.setcolor",
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MetodData(IGuiTimer.class, "setScale", "method.component.setscale",
				new ParameterData(float.class, "scale", "parameter.scale")
			),
			new MetodData(IGuiTimer.class, "setSize", "method.component.setsize",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			)
		)
	),
	IItemArmor(new InterfaseData(IItemArmor.class, 
			IItemStack.class,
			new Class<?>[] { ItemArmorWrapper.class },
			"interfase.iitemarmor",
			new MetodData(String.class, "getArmorMaterial", "method.iitemarmor.getarmormaterial"),
			new MetodData(int.class, "getArmorSlot", "method.iitemarmor.getarmorslot")
		)
	),
	IItemBlock(new InterfaseData(IItemBlock.class, 
			IItemStack.class,
			new Class<?>[] { ItemBlockWrapper.class },
			"interfase.iitemblock",
			new MetodData(String.class, "getBlockName", "method.iitemblock.getblockname")
		)
	),
	IItemBook(new InterfaseData(IItemBook.class, 
			IItemStack.class,
			new Class<?>[] { ItemBookWrapper.class },
			"interfase.iitembook",
			new MetodData(String.class, "getAuthor", "method.iitembook.getauthor"),
			new MetodData(String[].class, "getText", "method.iitembook.gettext"),
			new MetodData(String.class, "getTitle", "method.iitembook.gettitle"),
			new MetodData(void.class, "setAuthor", "method.iitembook.setauthor",
				new ParameterData(String.class, "author", "parameter.book.author")
			),
			new MetodData(void.class, "setText", "method.iitembook.settext",
				new ParameterData(String[].class, "pages", "parameter.iitembook.pages")
			),
			new MetodData(void.class, "setTitle", "method.iitembook.settitle",
				new ParameterData(String.class, "title", "parameter.book.title")
			)
		)
	),
	IItemScripted(new InterfaseData(IItemScripted.class, 
			IItemStack.class,
			new Class<?>[] { ItemScriptedWrapper.class },
			"interfase.iitemscripted",
			new MetodData(int.class, "getColor", "method.iitemscripted.getcolor"),
			new MetodData(int.class, "getDurabilityColor", "method.iitemscripted.getdurabilitycolor"),
			new MetodData(boolean.class, "getDurabilityShow", "method.iitemscripted.getdurabilityshow"),
			new MetodData(double.class, "getDurabilityValue", "method.iitemscripted.getdurabilityvalue"),
			new MetodData(String.class, "getTexture", "method.component.gettexture",
				new ParameterData(int.class, "damage", "parameter.iitems.damage")
			),
			new MetodData(boolean.class, "hasTexture", "method.iitemscripted.hastexture",
				new ParameterData(int.class, "damage", "parameter.iitems.damage")
			),
			new MetodData(void.class, "setColor", "method.iitemscripted.setcolor",
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MetodData(void.class, "setDurabilityColor", "method.iitemscripted.setdurabilitycolor",
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MetodData(void.class, "setDurabilityShow", "method.iitemscripted.setdurabilityshow",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setDurabilityValue", "method.iitemscripted.setdurabilityvalue",
				new ParameterData(float.class, "value", "parameter.value")
			),
			new MetodData(void.class, "setMaxStackSize", "method.iitemscripted.setmaxstacksize",
				new ParameterData(int.class, "size", "parameter.size")
			),
			new MetodData(void.class, "setTexture", "method.iitemscripted.settexture",
				new ParameterData(int.class, "damage", "parameter.iitemscripted.damage"),
				new ParameterData(String.class, "texture", "parameter.iitemscripted.texture")
			)
		)
	),
	IItemSlot(new InterfaseData(IItemSlot.class, 
			ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiItemSlotWrapper.class },
			"interfase.iitemslot",
			new MetodData(Slot.class, "getMCSlot", "method.iitemslot.getmcslot"),
			new MetodData(IItemStack.class, "getStack", "method.iitemslot.getstack"),
			new MetodData(boolean.class, "hasStack", "method.iitemslot.hasstack"),
			new MetodData(IItemSlot.class, "setStack", "method.iitemslot.setstack",
				new ParameterData(IItemStack.class, "stack", "parameter.stack")
			)
		)
	),
	IItemStack(new InterfaseData(IItemStack.class, 
			null,
			new Class<?>[] { ItemStackWrapper.class },
			"interfase.iitemstack",
			new MetodData(void.class, "addEnchantment", "method.iitemstack.addenchantment",
				new ParameterData(String.class, "name", "parameter.iitemstack.name"),
				new ParameterData(int.class, "level", "parameter.iitemstack.level")
			),
			new MetodData(void.class, "addEnchantment", "method.iitemstack.addenchantment",
				new ParameterData(int.class, "id", "parameter.iitemstack.id"),
				new ParameterData(int.class, "level", "parameter.iitemstack.level")
			),
			new MetodData(boolean.class, "compare", "method.iitemstack.compare",
				new ParameterData(IItemStack.class, "item", "parameter.iitemstack.item"),
				new ParameterData(boolean.class, "ignoreNBT", "parameter.iitemstack.ignorenbt")
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
				new ParameterData(int.class, "slot", "parameter.ceil.slot")
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
	IJobBard(new InterfaseData(IJobBard.class, 
			null,
			new Class<?>[] { JobBard.class },
			"interfase.ijobbard",
			new MetodData(String.class, "getSong", "method.ijobbard.getsong"),
			new MetodData(void.class, "setSong", "method.ijobbard.setsong",
				new ParameterData(String.class, "song", "parameter.sound.name")
			)
		)
	),
	IJobBuilder(new InterfaseData(IJobBuilder.class, 
			null,
			new Class<?>[] { JobBuilder.class },
			"interfase.ijobbuilder",
			new MetodData(boolean.class, "isBuilding", "method.ijobbuilder.isbuilding")
		)
	),
	IJobFarmer(new InterfaseData(IJobFarmer.class, 
			null,
			new Class<?>[] { JobFarmer.class },
			"interfase.ijobfarmer",
			new MetodData(boolean.class, "isPlucking", "method.ijobfarmer.isplucking")
		)
	),
	IJobFollower(new InterfaseData(IJobFollower.class, 
			INPCJob.class,
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
	IJobSpawner(new InterfaseData(IJobSpawner.class, 
			null,
			new Class<?>[] { JobSpawner.class },
			"interfase.ijobspawner",
			new MetodData(void.class, "removeAllSpawned", "method.ijobspawner.removeallspawned"),
			new MetodData(IEntityLivingBase.class, "spawnEntity", "method.ijobspawner.spawnentity",
				new ParameterData(int.class, "pos", "parameter.ijobspawner.pos"),
				new ParameterData(boolean.class, "isDead", "parameter.ijobspawner.isDead")
			)
		)
	),
	ILabel(new InterfaseData(ILabel.class, 
			ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiLabelWrapper.class },
			"interfase.ilabel",
			new MetodData(int.class, "getColor", "method.ilabel.getcolor"),
			new MetodData(int.class, "getHeight", "method.component.getheight"),
			new MetodData(float.class, "getScale", "method.component.getscale"),
			new MetodData(String.class, "getText", "method.component.gettext"),
			new MetodData(int.class, "getWidth", "method.component.getwidth"),
			new MetodData(ILabel.class, "setColor", "method.component.setcolor",
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MetodData(ILabel.class, "setScale", "method.component.setscale",
				new ParameterData(float.class, "scale", "parameter.scale")
			),
			new MetodData(ILabel.class, "setSize", "method.component.setsize",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MetodData(ILabel.class, "setText", "method.component.settext",
				new ParameterData(String.class, "label", "parameter.component.title")
			)
		)
	),
	ILayerModel(new InterfaseData(ILayerModel.class, 
			null,
			new Class<?>[] { LayerModel.class },
			"interfase.ilayermodel",
			new MetodData(int.class, "getPos", "method.ilayermodel.getpos"),
			new MetodData(INbt.class, "getNbt", "method.ilayermodel.getnbt"),
			new MetodData(void.class, "setNbt", "method.ilayermodel.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MetodData(float.class, "getOffset", "method.ilayermodel.getoffset",
				new ParameterData(int.class, "axis", "parameter.axis")
			),
			new MetodData(void.class, "setOffset", "method.ilayermodel.setoffset",
				new ParameterData(float.class, "x", "parameter.ilayermodel.x"),
				new ParameterData(float.class, "y", "parameter.ilayermodel.y"),
				new ParameterData(float.class, "z", "parameter.ilayermodel.z")
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
				new ParameterData(float.class, "x", "parameter.ilayermodel.x"),
				new ParameterData(float.class, "y", "parameter.ilayermodel.y"),
				new ParameterData(float.class, "z", "parameter.ilayermodel.z")
			),
			new MetodData(boolean.class, "isRotate", "method.ilayermodel.isrotate",
				new ParameterData(int.class, "axis", "parameter.axis")
			),
			new MetodData(void.class, "setIsRotate", "method.ilayermodel.setisrotate",
				new ParameterData(boolean.class, "x", "parameter.ilayermodel.x"),
				new ParameterData(boolean.class, "y", "parameter.ilayermodel.y"),
				new ParameterData(boolean.class, "z", "parameter.ilayermodel.z")
			),
			new MetodData(float.class, "getScale", "method.ilayermodel.getscale",
				new ParameterData(int.class, "axis", "parameter.axis")
			),
			new MetodData(void.class, "setScale", "method.ilayermodel.setscale",
				new ParameterData(float.class, "x", "parameter.ilayermodel.x"),
				new ParameterData(float.class, "y", "parameter.ilayermodel.y"),
				new ParameterData(float.class, "z", "parameter.ilayermodel.z")
			),
			new MetodData(int.class, "getRotateSpeed", "method.ilayermodel.getrotatespeed"),
			new MetodData(void.class, "setRotateSpeed", "method.ilayermodel.setrotatespeed",
				new ParameterData(int.class, "speed", "parameter.speed")
			)
		)
	),
	ILine(new InterfaseData(ILine.class, 
			null,
			new Class<?>[] { Line.class },
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
	IMark(new InterfaseData(IMark.class, 
			null,
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
	IMonster(new InterfaseData(IMonster.class, 
			IEntityLiving.class,
			new Class<?>[] { MonsterWrapper.class },
			"interfase.imonster"
		)
	),
	INPCAdvanced(new InterfaseData(INPCAdvanced.class, 
			null,
			new Class<?>[] { DataAdvanced.class },
			"interfase.inpcadvanced",
			new MetodData(String.class, "getLine", "method.inpcadvanced.getline",
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
	INPCAi(new InterfaseData(INPCAi.class, 
			null,
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
			new MetodData(void.class, "setAvoidsWater", "method.inpcai.setavoidswater",
				new ParameterData(boolean.class, "enabled", "parameter.inpcai.enabled")
			),
			new MetodData(void.class, "setCanSwim", "method.inpcai.setcanswim",
				new ParameterData(boolean.class, "canSwim", "parameter.boolean")
			),
			new MetodData(void.class, "setDoorInteract", "method.inpcai.setdoortnteract",
				new ParameterData(int.class, "type", "parameter.inpcai.type")
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
			new MetodData(void.class, "setNavigationType", "method.inpcai.setnavigationtype",
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
	INPCAnimation(new InterfaseData(INPCAnimation.class, 
			null,
			new Class<?>[] { DataAnimation.class },
			"interfase.inpcanimation",
			new MetodData(void.class, "reset", "method.inpcanimation.reset"),
			new MetodData(void.class, "stopAnimation", "method.inpcanimation.stopanimation"),
			new MetodData(void.class, "stopEmotion", "method.inpcanimation.stopemotion"),
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
			new MetodData(INbt.class, "getNbt", "method.inpcanimation.getnbt"),
			new MetodData(void.class, "setNbt", "method.inpcanimation.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MetodData(void.class, "startAnimationFromSaved", "method.inpcanimation.startanimationfromsaved",
				new ParameterData(int.class, "animationId", "parameter.animation.id")
			),
			new MetodData(void.class, "startAnimationFromSaved", "method.inpcanimation.startanimationfromsaved",
				new ParameterData(String.class, "animationName", "parameter.animation.name")
			),
			new MetodData(boolean.class, "removeAnimation", "method.inpcanimation.removeanimation",
				new ParameterData(int.class, "type", "parameter.animation.type"),
				new ParameterData(String.class, "name", "parameter.animation.name")
			),
			new MetodData(void.class, "removeAnimations", "method.inpcanimation.removeanimations",
				new ParameterData(int.class, "type", "parameter.animation.type")
			),
			new MetodData(AnimationConfig.class, "createAnimation", "method.inpcanimation.createanimation",
				new ParameterData(int.class, "animationType", "parameter.animation.type")
			)
		)
	),
	INPCDisplay(new InterfaseData(INPCDisplay.class, 
			null,
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
				new ParameterData(IPlayer.class, "player", "parameter.inpcdisplay.player")
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
	INPCInventory(new InterfaseData(INPCInventory.class, 
			null,
			new Class<?>[] { DataInventory.class },
			"interfase.inpcinventory",
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
			new MetodData(ICustomDrop[].class, "getDrops", "method.inpcinv.getdrops"),
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
	INPCJob(new InterfaseData(INPCJob.class, 
			null,
			new Class<?>[] { JobInterface.class },
			"interfase.inpcjob",
			new MetodData(int.class, "getType", "method.inpcjob.gettype")
		)
	),
	INPCMelee(new InterfaseData(INPCMelee.class, 
			null,
			new Class<?>[] { DataMelee.class },
			"interfase.inpcmelee",
			new MetodData(int.class, "getDelay", "method.inpcmelee.getdelay"),
			new MetodData(int.class, "getEffectStrength", "method.inpcmelee.geteffectstrength"),
			new MetodData(int.class, "getEffectTime", "method.inpcmelee.geteffecttime"),
			new MetodData(int.class, "getEffectType", "method.inpcmelee.geteffecttype"),
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
	INPCRanged(new InterfaseData(INPCRanged.class, 
			null,
			new Class<?>[] { DataRanged.class },
			"interfase.inpcranged",
			new MetodData(boolean.class, "getAccelerate", "method.inpcranged.getaccelerate"),
			new MetodData(int.class, "getAccuracy", "method.inpcranged.getaccuracy"),
			new MetodData(int.class, "getBurst", "method.inpcranged.getburst"),
			new MetodData(int.class, "getBurstDelay", "method.inpcranged.getburstdelay"),
			new MetodData(int.class, "getDelayMax", "method.inpcranged.getdelaymax"),
			new MetodData(int.class, "getDelayMin", "method.inpcranged.getdelaymin"),
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
			new MetodData(void.class, "setDelay", "method.inpcranged.setdelay",
				new ParameterData(int.class, "min", "parameter.min"),
				new ParameterData(int.class, "max", "parameter.max")
			),
			new MetodData(void.class, "setEffect", "method.inpcranged.seteffect",
				new ParameterData(int.class, "type", "parameter.effect.type"),
				new ParameterData(int.class, "strength", "parameter.effect.strength"),
				new ParameterData(int.class, "time", "parameter.ticks")
			),
			new MetodData(void.class, "setExplodeSize", "method.inpcranged.setexplodesize",
				new ParameterData(int.class, "size", "parameter.explode.size")
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
			new MetodData(void.class, "setSound", "method.inpcranged.setsound",
				new ParameterData(int.class, "type", "parameter.inpcranged.type"),
				new ParameterData(String.class, "sound", "parameter.inpcranged.sound")
			),
			new MetodData(void.class, "setSpeed", "method.inpcranged.setspeed",
				new ParameterData(int.class, "speed", "parameter.speed")
			),
			new MetodData(void.class, "setSpins", "method.inpcranged.setspins",
				new ParameterData(boolean.class, "spins", "parameter.boolean")
			),
			new MetodData(void.class, "setSticks", "method.inpcranged.setsticks",
				new ParameterData(boolean.class, "sticks", "parameter.boolean")
			),
			new MetodData(void.class, "setStrength", "method.inpcranged.setstrength",
				new ParameterData(int.class, "strength", "parameter.inpcranged.strength")
			)
		)
	),
	INPCRole(new InterfaseData(INPCRole.class, 
			null,
			new Class<?>[] { RoleInterface.class },
			"interfase.inpcrole",
			new MetodData(int.class, "getType", "method.inpcrole.gettype")
		)
	),
	INPCStats(new InterfaseData(INPCStats.class, 
			null,
			new Class<?>[] { DataStats.class },
			"interfase.inpcstats",
			new MetodData(int.class, "getAggroRange", "method.inpcstats.getaggrorange"),
			new MetodData(int.class, "getCombatRegen", "method.inpcstats.getcombatregen"),
			new MetodData(int.class, "getCreatureType", "method.inpcstats.getcreaturetype"),
			new MetodData(int.class, "getHealthRegen", "method.inpcstats.gethealthregen"),
			new MetodData(boolean.class, "getHideDeadBody", "method.inpcstats.gethidedeadbody"),
			new MetodData(boolean.class, "getImmune", "method.inpcstats.getimmune",
				new ParameterData(int.class, "type", "parameter.inpcstats.immune.type")
			),
			new MetodData(int.class, "getLevel", "method.inpcstats.getlevel"),
			new MetodData(int.class, "getMaxHealth", "method.inpcstats.getmaxhealth"),
			new MetodData(INPCMelee.class, "getMelee", "method.inpcstats.getmelee"),
			new MetodData(INPCRanged.class, "getRanged", "method.inpcstats.getranged"),
			new MetodData(int.class, "getRarity", "method.inpcstats.getrarity"),
			new MetodData(String.class, "getRarityTitle", "method.inpcstats.getraritytitle"),
			new MetodData(float.class, "getResistance", "method.inpcstats.getresistance",
				new ParameterData(int.class, "type", "parameter.inpcstats.type")
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
				new ParameterData(int.class, "seconds", "parameter.inpcstats.seconds")
			),
			new MetodData(void.class, "setRespawnType", "method.inpcstats.setrespawntype",
				new ParameterData(int.class, "type", "parameter.inpcstats.type")
			)
		)
	),
	INbt(new InterfaseData(INbt.class, 
			null,
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
			new MetodData(int.class, "getInteger", "method.inbt.getinteger",
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
			new MetodData(int.class, "getListType", "method.inbt.getlisttype",
				new ParameterData(String.class, "key", "parameter.inbt.key")
			),
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
			new MetodData(boolean.class, "isEqual", "method.inbt.isequal",
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
				new ParameterData(String.class, "key", "parameter.inbt.key"),
				new ParameterData(byte.class, "value", "parameter.inbt.value")
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
				new ParameterData(String.class, "key", "parameter.inbt.key"),
				new ParameterData(int[].class, "value", "parameter.inbt.value")
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
	INpcRecipe(new InterfaseData(INpcRecipe.class, 
			null,
			new Class<?>[] { NpcShapelessRecipes.class, NpcShapedRecipes.class },
			"interfase.inpcrecipe",
			new MetodData(void.class, "delete", "method.inpcrecipe.delete"),
			new MetodData(IAvailability.class, "getAvailability", "method.inpcrecipe.getavailability"),
			new MetodData(int.class, "getHeight", "method.inpcrecipe.getheight"),
			new MetodData(int.class, "getId", "method.inpcrecipe.getid"),
			new MetodData(boolean.class, "getIgnoreDamage", "method.inpcrecipe.getignoredamage"),
			new MetodData(boolean.class, "getIgnoreNBT", "method.inpcrecipe.getignorenbt"),
			new MetodData(String.class, "getName", "method.inpcrecipe.getname"),
			new MetodData(IItemStack[][].class, "getRecipe", "method.inpcrecipe.getrecipe"),
			new MetodData(IItemStack.class, "getResult", "method.inpcrecipe.getresult"),
			new MetodData(int.class, "getWidth", "method.inpcrecipe.getwidth"),
			new MetodData(boolean.class, "isGlobal", "method.inpcrecipe.isglobal"),
			new MetodData(boolean.class, "isKnown", "method.inpcrecipe.isknown"),
			new MetodData(boolean.class, "isShaped", "method.inpcrecipe.isshaped"),
			new MetodData(boolean.class, "saves", "method.inpcrecipe.saves"),
			new MetodData(void.class, "saves", "method.inpcrecipe.saves",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setIgnoreDamage", "method.inpcrecipe.setignoredamage",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setIgnoreNBT", "method.inpcrecipe.setignorenbt",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setIsGlobal", "method.inpcrecipe.setisglobal",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setKnown", "method.inpcrecipe.setknown",
				new ParameterData(boolean.class, "known", "parameter.boolean")
			),
			new MetodData(INbt.class, "getNbt", "method.inpcrecipe.getnbt"),
			new MetodData(void.class, "setNbt", "method.inpcrecipe.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MetodData(String.class, "getNpcGroup", "method.inpcrecipe.getnpcgroup"),
			new MetodData(IItemStack.class, "getProduct", "method.inpcrecipe.getproduct"),
			new MetodData(void.class, "copy", "method.inpcrecipe.copy",
				new ParameterData(INpcRecipe.class, "recipe", "parameter.copy")
			),
			new MetodData(boolean.class, "isValid", "method.inpcrecipe.isvalid"),
			new MetodData(boolean.class, "equal", "method.inpcrecipe.equal",
				new ParameterData(INpcRecipe.class, "recipe", "parameter.recipe")
			)
		)
	),
	IOverlayHUD(new InterfaseData(IOverlayHUD.class, 
			null,
			new Class<?>[] { PlayerOverlayHUD.class },
			"interfase.ioverlayhud",
			new MetodData(boolean.class, "isShowElementType", "method.ihud.isshowelementtype",
				new ParameterData(int.class, "type", "parameter.ihud.elementtype")
			),
			new MetodData(void.class, "setShowElementType", "method.ihud.setshowelementtype",
				new ParameterData(int.class, "type", "parameter.ihud.elementtype"),
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setShowElementType", "method.ihud.setshowelementtype",
				new ParameterData(String.class, "name", "parameter.ioverlayhud.name"),
				new ParameterData(boolean.class, "bo", "parameter.ioverlayhud.bo")
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
			new MetodData(ICustomGuiComponent.class, "getComponent", "method.ihud.getcomponent",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(int.class, "componentID", "parameter.component.id")
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
				new ParameterData(int.class, "componentID", "parameter.component.id")
			),
			new MetodData(boolean.class, "removeSlot", "method.ihud.removeslot",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(int.class, "slotID", "parameter.slot")
			),
			new MetodData(void.class, "clear", "method.ihud.clear"),
			new MetodData(void.class, "update", "method.ihud.update"),
			new MetodData(ICompassData.class, "getCompasData", "method.ioverlayhud.getcompasdata")
		)
	),
	IPermission(new InterfaseData(IPermission.class, 
			null,
			new Class<?>[] { BlockScripted.class, ItemNpcScripter.class, BlockCopy.class, ItemBuilder.class, ItemScripted.class, CustomItem.class, ItemNbtBook.class, ItemScriptedDoor.class, BlockScriptedDoor.class, BlockWaypoint.class, ItemTeleporter.class, CustomBlock.class, ItemMounter.class, ItemNpcCloner.class, ItemNpcWand.class, BlockNpcRedstone.class, BlockBorder.class, BlockBuilder.class, ItemNpcMovingPath.class, ItemBoundary.class },
			"interfase.ipermission",
			new MetodData(boolean.class, "isAllowed", "method.ipermission.isallowed",
				new ParameterData(EnumPacketServer.class, "enumPacket", "parameter.ipermission.enumpacket")
			)
		)
	),
	IPixelmon(new InterfaseData(IPixelmon.class, 
			IAnimal.class,
			new Class<?>[] { PixelmonWrapper.class },
			"interfase.ipixelmon",
			new MetodData(Object.class, "getPokemonData", "method.ipixelmon.getpokemondata")
		)
	),
	IPixelmonPlayerData(new InterfaseData(IPixelmonPlayerData.class, 
			null,
			null,
			"interfase.ipixelmonplayerdata",
			new MetodData(Object.class, "getParty", "method.ipixelmonplayerdata.getparty"),
			new MetodData(Object.class, "getPC", "method.ipixelmonplayerdata.getpc")
		)
	),
	IPlayer(new InterfaseData(IPlayer.class, 
			IEntityLivingBase.class,
			new Class<?>[] { PlayerWrapper.class },
			"interfase.iplayer",
			new MetodData(void.class, "addDialog", "method.iplayer.adddialog",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			),
			new MetodData(void.class, "addFactionPoints", "method.iplayer.addfactionpoints",
				new ParameterData(int.class, "faction", "parameter.faction.id"),
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
			new MetodData(ICustomGui.class, "getCustomGui", "method.iplayer.getcustomgui"),
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
			new MetodData(EntityLivingBase.class, "getMCEntity", "method.ientity.getmcentity"),
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
				new ParameterData(IItemStack.class, "stack", "parameter.stack"),
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
			new MetodData(boolean.class, "removeItem", "method.iplayer.removeitem",
				new ParameterData(String.class, "id", "parameter.iplayer.id"),
				new ParameterData(int.class, "damage", "parameter.iplayer.damage"),
				new ParameterData(int.class, "amount", "parameter.iplayer.amount")
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
			new MetodData(void.class, "setSpawnPoint", "method.iplayer.setspawnpoint",
				new ParameterData(IBlock.class, "block", "parameter.iplayer.block")
			),
			new MetodData(IContainer.class, "showChestGui", "method.iplayer.showchestgui",
				new ParameterData(int.class, "rows", "parameter.chestgui.rows")
			).setDeprecated(),
			new MetodData(void.class, "showCustomGui", "method.iplayer.showcustomgui",
				new ParameterData(ICustomGui.class, "gui", "parameter.customgui")
			),
			new MetodData(void.class, "showDialog", "method.iplayer.showdialog",
				new ParameterData(int.class, "id", "parameter.dialog.id"),
				new ParameterData(String.class, "name", "parameter.showdialog.entity.name")
			),
			new MetodData(void.class, "startQuest", "method.iplayer.startquest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(void.class, "stopQuest", "method.iplayer.stopquest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(void.class, "updatePlayerInventory", "method.iplayer.updateplayerinventory"),
			new MetodData(boolean.class, "isMoved", "method.iplayer.ismoved"),
			new MetodData(void.class, "addMoney", "method.iplayer.addmoney",
				new ParameterData(long.class, "value", "parameter.money.value")
			),
			new MetodData(long.class, "getMoney", "method.iplayer.getmoney"),
			new MetodData(void.class, "setMoney", "method.iplayer.setmoney",
				new ParameterData(long.class, "value", "parameter.money.value")
			),
			new MetodData(int[].class, "getKeyPressed", "method.iplayer.getkeypressed"),
			new MetodData(boolean.class, "hasOrKeyPressed", "method.iplayer.hasorkeyspressed",
				new ParameterData(int[].class, "key", "parameter.iplayer.key")
			),
			new MetodData(int[].class, "getMousePressed", "method.iplayer.getmousepressed"),
			new MetodData(boolean.class, "hasMousePress", "method.iplayer.hasmousepress",
				new ParameterData(int.class, "key", "parameter.iplayer.key")
			),
			new MetodData(void.class, "completeQuest", "method.iplayer.completequest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MetodData(IOverlayHUD.class, "getOverlayHUD", "method.iplayer.gethud"),
			new MetodData(void.class, "trigger", "method.trigger",
				new ParameterData(int.class, "id", "parameter.trigger.id"),
				new ParameterData(Object[].class, "arguments", "parameter.trigger.arguments")
			),
			new MetodData(String.class, "getLanguage", "method.iplayer.getlanguage"),
			new MetodData(void.class, "sendTo", "method.iplayer.sendto",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MetodData(boolean.class, "isMoved", "method.iplayer.ismoved"),
			new MetodData(double[].class, "getWindowSize", "method.iplayer.getwindowsize")
		)
	),
	IPlayerMail(new InterfaseData(IPlayerMail.class, 
			null,
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
	IPos(new InterfaseData(IPos.class, 
			null,
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
			new MetodData(IPos.class, "down", "method.ipos.down.0",
				new ParameterData(int.class, "n", "parameter.blocks")
			),
			new MetodData(IPos.class, "east", "method.ipos.east.0"),
			new MetodData(IPos.class, "east", "method.ipos.east.0",
				new ParameterData(int.class, "n", "parameter.blocks")
			),
			new MetodData(BlockPos.class, "getMCBlockPos", "method.ipos.getmcblockpos"),
			new MetodData(int.class, "getX", "method.getx"),
			new MetodData(int.class, "getY", "method.gety"),
			new MetodData(int.class, "getZ", "method.getz"),
			new MetodData(double[].class, "normalize", "method.ipos.normalize"),
			new MetodData(IPos.class, "north", "method.ipos.north.0"),
			new MetodData(IPos.class, "north", "method.ipos.north.0",
				new ParameterData(int.class, "n", "parameter.blocks")
			),
			new MetodData(IPos.class, "offset", "method.ipos.offset.0",
				new ParameterData(int.class, "direction", "parameter.direction")
			),
			new MetodData(IPos.class, "offset", "method.ipos.offset.0",
				new ParameterData(int.class, "direction", "parameter.direction"),
				new ParameterData(int.class, "n", "parameter.blocks")
			),
			new MetodData(IPos.class, "south", "method.ipos.south.0"),
			new MetodData(IPos.class, "south", "method.ipos.south.0",
				new ParameterData(int.class, "n", "parameter.blocks")
			),
			new MetodData(IPos.class, "subtract", "method.ipos.subtract",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(IPos.class, "subtract", "method.ipos.subtract",
				new ParameterData(IPos.class, "pos", "parameter.ipos.pos")
			),
			new MetodData(IPos.class, "up", "method.ipos.up.0"),
			new MetodData(IPos.class, "up", "method.ipos.up.0",
				new ParameterData(int.class, "n", "parameter.blocks")
			),
			new MetodData(IPos.class, "west", "method.ipos.west.0"),
			new MetodData(IPos.class, "west", "method.ipos.west.0",
				new ParameterData(int.class, "n", "parameter.blocks")
			)
		)
	),
	IProjectile(new InterfaseData(IProjectile.class, 
			IThrowable.class,
			new Class<?>[] { ProjectileWrapper.class },
			"interfase.iprojectile",
			new MetodData(void.class, "enableEvents", "method.iprojectile.enableevents"),
			new MetodData(int.class, "getAccuracy", "method.iprojectile.getaccuracy"),
			new MetodData(boolean.class, "getHasGravity", "method.iprojectile.gethasgravity"),
			new MetodData(IItemStack.class, "getItem", "method.iprojectile.getitem"),
			new MetodData(void.class, "setAccuracy", "method.iprojectile.setaccuracy",
				new ParameterData(int.class, "accuracy", "parameter.iprojectile.accuracy")
			),
			new MetodData(void.class, "setHasGravity", "method.iprojectile.sethasgravity",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setHeading", "method.iprojectile.setheading",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz")
			),
			new MetodData(void.class, "setHeading", "method.iprojectile.setheading",
				new ParameterData(float.class, "yaw", "parameter.yaw"),
				new ParameterData(float.class, "pitch", "parameter.pitch")
			),
			new MetodData(void.class, "setHeading", "method.iprojectile.setheading",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MetodData(void.class, "setItem", "method.iprojectile.setitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			)
		)
	),
	IQuest(new InterfaseData(IQuest.class, 
			null,
			new Class<?>[] { Quest.class },
			"interfase.iquest",
			new MetodData(IQuestObjective.class, "addTask", "method.iquest.addtask"),
			new MetodData(IQuestCategory.class, "getCategory", "method.iquest.getcategory"),
			new MetodData(String.class, "getCompleteText", "method.iquest.getcompletetext"),
			new MetodData(int[].class, "getForgetDialogues", "method.iquest.getforgetdialogues"),
			new MetodData(int[].class, "getForgetQuests", "method.iquest.getforgetquests"),
			new MetodData(int.class, "getId", "method.iquest.getid"),
			new MetodData(boolean.class, "getIsRepeatable", "method.iquest.getisrepeatable"),
			new MetodData(int.class, "getLevel", "method.iquest.getlevel"),
			new MetodData(String.class, "getLogText", "method.iquest.getlogtext"),
			new MetodData(String.class, "getName", "method.iquest.getname"),
			new MetodData(IQuest.class, "getNextQuest", "method.iquest.getnextquest"),
			new MetodData(String.class, "getNpcName", "method.iquest.getnpcname"),
			new MetodData(IQuestObjective[].class, "getObjectives", "method.iquest.getobjectives",
				new ParameterData(IPlayer.class, "player", "parameter.player")
			),
			new MetodData(IContainer.class, "getRewards", "method.iquest.getrewards"),
			new MetodData(int.class, "getRewardType", "method.iquest.getrewardtype"),
			new MetodData(String.class, "getTitle", "method.iquest.gettitle"),
			new MetodData(boolean.class, "isCancelable", "method.iquest.iscancelable"),
			new MetodData(boolean.class, "isSetUp", "method.iquest.issetup"),
			new MetodData(boolean.class, "removeTask", "method.iquest.removetask",
				new ParameterData(IQuestObjective.class, "task", "parameter.quest.task")
			),
			new MetodData(void.class, "save", "method.iquest.save"),
			new MetodData(void.class, "sendChangeToAll", "method.iquest.sendchangetoall"),
			new MetodData(void.class, "setCancelable", "method.iquest.setcancelable",
				new ParameterData(boolean.class, "cancelable", "parameter.boolean")
			),
			new MetodData(void.class, "setCompleteText", "method.iquest.setcancelable",
				new ParameterData(String.class, "text", "parameter.quest.completetext")
			),
			new MetodData(void.class, "setForgetDialogues", "method.iquest.setforgetdialogues",
				new ParameterData(int[].class, "forget", "parameter.quest.forget.d")
			),
			new MetodData(void.class, "setForgetQuests", "method.iquest.setforgetquests",
				new ParameterData(int[].class, "forget", "parameter.quest.forget.q")
			),
			new MetodData(void.class, "setLevel", "method.iquest.setlevel",
				new ParameterData(int.class, "level", "parameter.level")
			),
			new MetodData(void.class, "setLogText", "method.iquest.setlogtext",
				new ParameterData(String.class, "text", "parameter.quest.log")
			),
			new MetodData(void.class, "setName", "method.iquest.setname",
				new ParameterData(String.class, "name", "parameter.quest.name")
			),
			new MetodData(void.class, "setNextQuest", "method.iquest.setnextquest",
				new ParameterData(IQuest.class, "quest", "parameter.quest")
			),
			new MetodData(void.class, "setNpcName", "method.iquest.setnpcname",
				new ParameterData(String.class, "name", "parameter.quest.npcname")
			),
			new MetodData(void.class, "setRewardText", "method.iquest.setrewardtext",
				new ParameterData(String.class, "text", "parameter.quest.reward.n")
			),
			new MetodData(void.class, "setRewardType", "method.iquest.setrewardtype",
				new ParameterData(int.class, "type", "parameter.quest.reward.t")
			)
		)
	),
	IQuestCategory(new InterfaseData(IQuestCategory.class, 
			null,
			new Class<?>[] { QuestCategory.class },
			"interfase.iquestcategory",
			new MetodData(IQuest.class, "create", "method.iquestcat.create"),
			new MetodData(String.class, "getName", "method.iquestcat.getname"),
			new MetodData(IQuest[].class, "quests", "method.iquestcat.quests")
		)
	),
	IQuestHandler(new InterfaseData(IQuestHandler.class, 
			null,
			new Class<?>[] { QuestController.class },
			"interfase.iquesthandler",
			new MetodData(IQuestCategory[].class, "categories", "method.iquesthandler.categories"),
			new MetodData(IQuest.class, "get", "method.iquesthandler.get",
				new ParameterData(int.class, "id", "parameter.quest.id")
			)
		)
	),
	IQuestObjective(new InterfaseData(IQuestObjective.class, 
			null,
			new Class<?>[] { QuestObjective.class },
			"interfase.iquestobjective",
			new MetodData(int.class, "getAreaRange", "method.iquestobj.getarearange"),
			new MetodData(IItemStack.class, "getItem", "method.iquestobj.getitem"),
			new MetodData(int.class, "getMaxProgress", "method.iquestobj.getmaxprogress"),
			new MetodData(int.class, "getProgress", "method.iquestobj.getprogress"),
			new MetodData(int.class, "getTargetID", "method.iquestobj.gettargetid"),
			new MetodData(String.class, "getTargetName", "method.iquestobj.gettargetname"),
			new MetodData(String.class, "getText", "method.iquestobj.gettext"),
			new MetodData(int.class, "getType", "method.iquestobj.gettype"),
			new MetodData(boolean.class, "isCompleted", "method.iquestobj.iscompleted"),
			new MetodData(boolean.class, "isIgnoreDamage", "method.iquestobj.isignoredamage"),
			new MetodData(boolean.class, "isItemIgnoreNBT", "method.iquestobj.isitemignorenbt"),
			new MetodData(boolean.class, "isItemLeave", "method.iquestobj.isitemleave"),
			new MetodData(void.class, "setAreaRange", "method.iquestobj.setarearange",
				new ParameterData(int.class, "range", "parameter.questobj.range")
			),
			new MetodData(void.class, "setItem", "method.iquestobj.setitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MetodData(void.class, "setItemIgnoreDamage", "method.iquestobj.setitemignoredamage",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setItemIgnoreNBT", "method.iquestobj.setitemignorenbt",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setItemLeave", "method.iquestobj.setitemleave",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MetodData(void.class, "setMaxProgress", "method.iquestobj.setmaxprogress",
				new ParameterData(int.class, "value", "parameter.value")
			),
			new MetodData(void.class, "setProgress", "method.iquestobj.setprogress",
				new ParameterData(int.class, "value", "parameter.value")
			),
			new MetodData(void.class, "setTargetID", "method.iquestobj.settargetid",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			),
			new MetodData(void.class, "setTargetName", "method.iquestobj.settargetname",
				new ParameterData(String.class, "name", "parameter.entity.name")
			),
			new MetodData(void.class, "setType", "method.iquestobj.settype",
				new ParameterData(int.class, "type", "parameter.iquestobj.type")
			),
			new MetodData(IPos.class, "getCompassPos", "method.iquestobj.getcompasspos"),
			new MetodData(void.class, "setCompassPos", "method.iquestobj.setcompasspos",
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MetodData(void.class, "setCompassPos", "method.iquestobj.setcompasspos",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(int.class, "getCompassDimension", "method.iquestobj.getcompassdimension"),
			new MetodData(void.class, "setCompassDimension", "method.iquestobj.setcompassdimension",
				new ParameterData(int.class, "dimensionID", "parameter.dimension.id")
			),
			new MetodData(int.class, "getCompassRange", "method.iquestobj.getcompassrange"),
			new MetodData(void.class, "setCompassRange", "method.iquestobj.setcompassrange",
				new ParameterData(int.class, "range", "parameter.range")
			),
			new MetodData(String.class, "getOrientationEntityName", "method.iquestobj.getorientationentityname"),
			new MetodData(void.class, "setOrientationEntityName", "method.iquestobj.setorientationentityname",
				new ParameterData(String.class, "name", "parameter.entity.name")
			)
		)
	),
	IRayTrace(new InterfaseData(IRayTrace.class, 
			null,
			new Class<?>[] { RayTraceWrapper.class },
			"interfase.iraytrace",
			new MetodData(IBlock.class, "getBlock", "method.iraytrace.getblock"),
			new MetodData(IPos.class, "getPos", "method.getpos"),
			new MetodData(int.class, "getSideHit", "method.iraytrace.getsidehit")
		)
	),
	IRecipeHandler(new InterfaseData(IRecipeHandler.class, 
			null,
			new Class<?>[] { RecipeController.class },
			"interfase.irecipehandler",
			new MetodData(INpcRecipe.class, "getRecipe", "method.irecipe.getrecipe",
				new ParameterData(String.class, "group", "parameter.irecipe.group"),
				new ParameterData(String.class, "name", "parameter.irecipe.name")
			),
			new MetodData(INpcRecipe.class, "getRecipe", "method.irecipe.getrecipe",
				new ParameterData(int.class, "id", "parameter.irecipe.id")
			),
			new MetodData(INpcRecipe.class, "addRecipe", "method.irecipe.getrecipe",
				new ParameterData(String.class, "group", "parameter.irecipe.group"),
				new ParameterData(String.class, "name", "parameter.irecipe.name"),
				new ParameterData(boolean.class, "global", "parameter.irecipe.isglobal"),
				new ParameterData(boolean.class, "known", "parameter.irecipe.isknown"),
				new ParameterData(ItemStack.class, "result", "parameter.stack"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(ItemStack[].class, "stacks", "parameter.stack")
			),
			new MetodData(INpcRecipe.class, "addRecipe", "method.irecipe.getrecipe",
				new ParameterData(String.class, "group", "parameter.irecipe.group"),
				new ParameterData(String.class, "name", "parameter.irecipe.name"),
				new ParameterData(boolean.class, "global", "parameter.irecipe.isglobal"),
				new ParameterData(boolean.class, "known", "parameter.irecipe.isknown"),
				new ParameterData(ItemStack.class, "result", "parameter.stack"),
				new ParameterData(Object[].class, "objects", "parameter.irecipe.objects")
			),
			new MetodData(boolean.class, "delete", "method.irecipe.delete",
				new ParameterData(String.class, "group", "parameter.irecipe.group"),
				new ParameterData(String.class, "name", "parameter.irecipe.name")
			),
			new MetodData(boolean.class, "delete", "method.irecipe.delete",
				new ParameterData(int.class, "id", "parameter.irecipe.id")
			),
			new MetodData(INpcRecipe[].class, "getCarpentryData", "method.irecipe.getcarpentryrecipes"),
			new MetodData(INpcRecipe[].class, "getCarpentryRecipes", "method.irecipe.getcarpentryrecipes",
				new ParameterData(String.class, "group", "parameter.irecipe.group")
			),
			new MetodData(INpcRecipe[].class, "getGlobalData", "method.irecipe.getglobalrecipes"),
			new MetodData(INpcRecipe[].class, "getGlobalRecipes", "method.irecipe.getglobalrecipes",
				new ParameterData(String.class, "group", "parameter.irecipe.group")
			)
		)
	),
	IRoleDialog(new InterfaseData(IRoleDialog.class, 
			null,
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
	IRoleFollower(new InterfaseData(IRoleFollower.class, 
			INPCRole.class,
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
	IRoleTrader(new InterfaseData(IRoleTrader.class, 
			INPCRole.class,
			new Class<?>[] { Marcet.class },
			"interfase.iroletrader",
			new MetodData(IItemStack.class, "getCurrency", "method.iroletrader.getcurrency",
				new ParameterData(int.class, "position", "parameter.position"),
				new ParameterData(int.class, "slot", "parameter.slot")
			),
			new MetodData(String.class, "getName", "method.iroletrader.getname"),
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
	IRoleTransporter(new InterfaseData(IRoleTransporter.class, 
			INPCRole.class,
			new Class<?>[] { RoleTransporter.class },
			"interfase.iroletransporter",
			new MetodData(ITransportLocation.class, "getLocation", "method.iroletransporter.getlocation")
		)
	),
	ISchematic(new InterfaseData(ISchematic.class, 
			null,
			new Class<?>[] { Schematic.class, Blueprint.class },
			"interfase.ischematic",
			new MetodData(IBlockState.class, "getBlockState", "method.ischematic.getblockstate",
				new ParameterData(int.class, "state", "parameter.ischematic.block.pos")
			),
			new MetodData(IBlockState.class, "getBlockState", "method.ischematic.getblockstate",
				new ParameterData(int.class, "x", "parameter.ischematic.x"),
				new ParameterData(int.class, "y", "parameter.ischematic.y"),
				new ParameterData(int.class, "z", "parameter.ischematic.z")
			),
			new MetodData(short.class, "getHeight", "method.ischematic.getheight"),
			new MetodData(short.class, "getLength", "method.ischematic.getlength"),
			new MetodData(String.class, "getName", "method.ischematic.getname"),
			new MetodData(NBTTagCompound.class, "getNBT", "method.ischematic.getnbt"),
			new MetodData(NBTTagCompound.class, "getTileEntity", "method.ischematic.gettileentity",
				new ParameterData(int.class, "pos", "parameter.ischematic.pos")
			),
			new MetodData(int.class, "getTileEntitySize", "method.ischematic.gettileentitysize"),
			new MetodData(short.class, "getWidth", "method.ischematic.getwidth")
		)
	),
	IScoreboard(new InterfaseData(IScoreboard.class, 
			null,
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
			new MetodData(IScoreboardTeam.class, "getTeam", "method.iscoreboard.getteam",
				new ParameterData(String.class, "name", "parameter.iscoreboard.name")
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
			new MetodData(void.class, "removePlayerTeam", "method.iscoreboard.removeteam",
				new ParameterData(String.class, "player", "parameter.playername")
			),
			new MetodData(void.class, "removeTeam", "method.iscoreboard.removeteam",
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
	IScoreboardObjective(new InterfaseData(IScoreboardObjective.class, 
			null,
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
	IScoreboardScore(new InterfaseData(IScoreboardScore.class, 
			null,
			new Class<?>[] { ScoreboardScoreWrapper.class },
			"interfase.iscoreboardscore",
			new MetodData(String.class, "getPlayerName", "method.getplayername"),
			new MetodData(int.class, "getValue", "method.iscoreboardscore.getvalue"),
			new MetodData(void.class, "setValue", "method.iscoreboardscore.setvalue",
				new ParameterData(int.class, "value", "parameter.score")
			)
		)
	),
	IScoreboardTeam(new InterfaseData(IScoreboardTeam.class, 
			null,
			new Class<?>[] { ScoreboardTeamWrapper.class },
			"interfase.iscoreboardteam",
			new MetodData(void.class, "addPlayer", "method.iscoreboardteam.addplayer",
				new ParameterData(String.class, "player", "parameter.iscoreboardteam.player")
			),
			new MetodData(void.class, "clearPlayers", "method.iscoreboardteam.clearplayers"),
			new MetodData(String.class, "getColor", "method.iscoreboardteam.getcolor"),
			new MetodData(String.class, "getDisplayName", "method.iscoreboardteam.getdisplayname"),
			new MetodData(boolean.class, "getFriendlyFire", "method.iscoreboardteam.getfriendlyfire"),
			new MetodData(String.class, "getName", "method.iscoreboardteam.getname"),
			new MetodData(String[].class, "getPlayers", "method.iscoreboardteam.getplayers"),
			new MetodData(boolean.class, "getSeeInvisibleTeamPlayers", "method.iscoreboardteam.getseeinvisibleteamplayers"),
			new MetodData(boolean.class, "hasPlayer", "method.iscoreboardteam.hasplayer",
				new ParameterData(String.class, "player", "parameter.iscoreboardteam.player")
			),
			new MetodData(void.class, "removePlayer", "method.iscoreboardteam.removeplayer",
				new ParameterData(String.class, "player", "parameter.iscoreboardteam.player")
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
	IScriptBlockHandler(new InterfaseData(IScriptBlockHandler.class, 
			IScriptHandler.class,
			new Class<?>[] { TileScriptedDoor.class, TileScripted.class },
			"interfase.iscriptblockhandler",
			new MetodData(IBlock.class, "getBlock", "method.iscriptblockhandler.getblock")
		)
	),
	IScriptData(new InterfaseData(IScriptData.class, 
			null,
			new Class<?>[] { ScriptData.class },
			"interfase.iscriptdata",
			new MetodData(String.class, "getName", "method.iscriptdata.getname"),
			new MetodData(INbt.class, "getNBT", "method.iscriptdata.getnbt"),
			new MetodData(Object.class, "getObject", "method.iscriptdata.getobject"),
			new MetodData(int.class, "getType", "method.iscriptdata.gettype"),
			new MetodData(String.class, "getValue", "method.iscriptdata.getvalue"),
			new MetodData(void.class, "setNBT", "method.iscriptdata.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			)
		)
	),
	IScriptHandler(new InterfaseData(IScriptHandler.class, 
			null,
			new Class<?>[] { ForgeScriptData.class, ItemScriptedWrapper.class, DataScript.class, PlayerScriptData.class, ClientScriptData.class },
			"interfase.iscripthandler",
			new MetodData(void.class, "clearConsole", "method.iscripthandler.clearconsole"),
			new MetodData(Map.class, "getConsoleText", "method.iscripthandler.getconsoletext"),
			new MetodData(boolean.class, "getEnabled", "method.iscripthandler.getenabled"),
			new MetodData(String.class, "getLanguage", "method.iscripthandler.getlanguage"),
			new MetodData(List.class, "getScripts", "method.iscripthandler.getscripts"),
			new MetodData(boolean.class, "isClient", "method.iscripthandler.isclient"),
			new MetodData(String.class, "noticeString", "method.iscripthandler.noticestring"),
			new MetodData(void.class, "runScript", "method.iscripthandler.runscript",
				new ParameterData(EnumScriptType.class, "type", "parameter.iscripthandler.type"),
				new ParameterData(Event.class, "event", "parameter.iscripthandler.event")
			),
			new MetodData(void.class, "setEnabled", "method.iscripthandler.setenabled",
				new ParameterData(boolean.class, "bo", "parameter.iscripthandler.bo")
			),
			new MetodData(void.class, "setLanguage", "method.iscripthandler.setlanguage",
				new ParameterData(String.class, "language", "parameter.iscripthandler.language")
			)
		)
	),
	IScroll(new InterfaseData(IScroll.class, 
			ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiScrollWrapper.class },
			"interfase.iscroll",
			new MetodData(int.class, "getDefaultSelection", "method.iscroll.getdefaultselection"),
			new MetodData(int.class, "getHeight", "method.component.getheight"),
			new MetodData(String[].class, "getList", "method.iscroll.getlist"),
			new MetodData(int.class, "getWidth", "method.component.getwidth"),
			new MetodData(boolean.class, "isMultiSelect", "method.iscroll.ismultiselect"),
			new MetodData(IScroll.class, "setDefaultSelection", "method.iscroll.getdefaultselection",
				new ParameterData(int.class, "defaultSelection", "parameter.iscroll.defaultselection")
			),
			new MetodData(IScroll.class, "setList", "method.iscroll.setlist",
				new ParameterData(String[].class, "list", "parameter.s.list")
			),
			new MetodData(IScroll.class, "setMultiSelect", "method.iscroll.setmultiselect",
				new ParameterData(boolean.class, "multiSelect", "parameter.iscroll.multiselect")
			),
			new MetodData(IScroll.class, "setSize", "method.component.setsize",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			)
		)
	),
	ITextField(new InterfaseData(ITextField.class, 
			ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiTextFieldWrapper.class },
			"interfase.itextfield",
			new MetodData(int.class, "getHeight", "method.component.getheight"),
			new MetodData(String.class, "getText", "method.itextfield.gettext"),
			new MetodData(int.class, "getWidth", "method.component.getwidth"),
			new MetodData(ITextField.class, "setSize", "method.component.setsize",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MetodData(ITextField.class, "setText", "method.itextfield.settext",
				new ParameterData(String.class, "text", "parameter.itextfield.text")
			)
		)
	),
	ITextPlane(new InterfaseData(ITextPlane.class, 
			null,
			null,
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
				new ParameterData(float.class, "scale", "parameter.itextplane.scale")
			),
			new MetodData(void.class, "setText", "method.itextplane.settext",
				new ParameterData(String.class, "text", "parameter.itextplane.text")
			)
		)
	),
	ITexturedButton(new InterfaseData(ITexturedButton.class, 
			IButton.class,
			null,
			"interfase.itexturedbutton",
			new MetodData(String.class, "getTexture", "method.component.gettexture"),
			new MetodData(int.class, "getTextureX", "method.component.gettexturex"),
			new MetodData(int.class, "getTextureY", "method.component.gettexturey"),
			new MetodData(ITexturedButton.class, "setTexture", "method.component.settexture",
				new ParameterData(String.class, "texture", "parameter.texture")
			),
			new MetodData(ITexturedButton.class, "setTextureOffset", "method.component.settextureoffset",
				new ParameterData(int.class, "textureX", "parameter.itexturedbutton.texturex"),
				new ParameterData(int.class, "textureY", "parameter.itexturedbutton.texturey")
			)
		)
	),
	ITexturedRect(new InterfaseData(ITexturedRect.class, 
			ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiTexturedRectWrapper.class },
			"interfase.itexturedrect",
			new MetodData(int.class, "getHeight", "method.component.getheight"),
			new MetodData(float.class, "getScale", "method.component.getscale"),
			new MetodData(String.class, "getTexture", "method.component.gettexture"),
			new MetodData(int.class, "getTextureX", "method.component.gettexturex"),
			new MetodData(int.class, "getTextureY", "method.component.gettexturey"),
			new MetodData(int.class, "getWidth", "method.component.getwidth"),
			new MetodData(ITexturedRect.class, "setScale", "method.component.setscale",
				new ParameterData(float.class, "scale", "parameter.scale")
			),
			new MetodData(ITexturedRect.class, "setSize", "method.component.setsize",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MetodData(ITexturedRect.class, "setTexture", "method.component.settexture",
				new ParameterData(String.class, "texture", "parameter.texture")
			),
			new MetodData(ITexturedRect.class, "setTextureOffset", "method.component.settextureoffset",
				new ParameterData(int.class, "textureX", "parameter.itexturedrect.texturex"),
				new ParameterData(int.class, "textureY", "parameter.itexturedrect.texturey")
			)
		)
	),
	IThrowable(new InterfaseData(IThrowable.class, 
			IEntity.class,
			new Class<?>[] { ThrowableWrapper.class },
			"interfase.ithrowable"
		)
	),
	ITimers(new InterfaseData(ITimers.class, 
			null,
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
	ITransportLocation(new InterfaseData(ITransportLocation.class, 
			null,
			new Class<?>[] { TransportLocation.class },
			"interfase.itransportlocation",
			new MetodData(int.class, "getDimension", "method.itransportlocation.getdimension"),
			new MetodData(int.class, "getId", "method.itransportlocation.getid"),
			new MetodData(String.class, "getName", "method.itransportlocation.getname"),
			new MetodData(int.class, "getType", "method.itransportlocation.gettype"),
			new MetodData(int.class, "getX", "method.getx"),
			new MetodData(int.class, "getY", "method.gety"),
			new MetodData(int.class, "getZ", "method.getz"),
			new MetodData(void.class, "setPos", "method.itransportlocation.setpos",
				new ParameterData(int.class, "dimentionID", "parameter.dimension.id"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(void.class, "setType", "method.itransportlocation.settype",
				new ParameterData(int.class, "type", "parameter.itransportlocation.type")
			)
		)
	),
	IVillager(new InterfaseData(IVillager.class, 
			IEntityLiving.class,
			new Class<?>[] { VillagerWrapper.class },
			"interfase.ivillager",
			new MetodData(MerchantRecipeList.class, "getRecipes", "method.ivillager.getrecipes",
				new ParameterData(IPlayer.class, "player", "parameter.player")
			),
			new MetodData(IInventory.class, "getVillagerInventory", "method.ivillager.getvillagerinventory")
		)
	),
	IWorld(new InterfaseData(IWorld.class, 
			null,
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
			new MetodData(IItemStack.class, "createItem", "method.iworld.createitem",
				new ParameterData(String.class, "name", "parameter.itemname"),
				new ParameterData(int.class, "damage", "parameter.itemmeta"),
				new ParameterData(int.class, "size", "parameter.itemcount")
			),
			new MetodData(IItemStack.class, "createItemFromNbt", "method.iworld.createitemfromnbt",
				new ParameterData(INbt.class, "nbt", "parameter.iworld.nbt")
			),
			new MetodData(void.class, "explode", "method.iworld.explode",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz"),
				new ParameterData(float.class, "range", "parameter.range"),
				new ParameterData(boolean.class, "fire", "parameter.iworld.fire"),
				new ParameterData(boolean.class, "grief", "parameter.iworld.grief")
			),
			new MetodData(IEntity.class, "getAllEntities", "method.iworld.getallentities",
				new ParameterData(int.class, "type", "parameter.entitytype")
			),
			new MetodData(IPlayer.class, "getAllPlayers", "method.iworld.getallplayers"),
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
			new MetodData(IEntity.class, "getNearbyEntities", "method.iworld.getnearbyEntities",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz"),
				new ParameterData(int.class, "range", "parameter.range"),
				new ParameterData(int.class, "type", "parameter.entitytype")
			).setDeprecated(),
			new MetodData(IEntity.class, "getNearbyEntities", "method.iworld.getnearbyEntities",
				new ParameterData(IPos.class, "pos", "parameter.pos"),
				new ParameterData(int.class, "range", "parameter.range"),
				new ParameterData(int.class, "type", "parameter.entitytype")
			),
			new MetodData(IPlayer.class, "getPlayer", "method.iworld.getplayer",
				new ParameterData(String.class, "name", "parameter.iworld.name")
			),
			new MetodData(int.class, "getRedstonePower", "method.iworld.getredstonepower",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MetodData(IScoreboard.class, "getScoreboard", "method.iworld.getscoreboard"),
			new MetodData(IBlock.class, "getSpawnPoint", "method.iworld.getspawnpoint"),
			new MetodData(IData.class, "getStoreddata", "method.getstoreddata"),
			new MetodData(IData.class, "getTempdata", "method.gettempdata"),
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
			new MetodData(IEntity.class, "getEntitys", "method.iworld.getentitys",
				new ParameterData(int.class, "type", "parameter.entitytype")
			)
		)
	),
	IWorldInfo(new InterfaseData(IWorldInfo.class, 
			null,
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
