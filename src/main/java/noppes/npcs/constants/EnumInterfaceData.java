package noppes.npcs.constants;

import java.awt.Point;
import java.io.File;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
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
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.IContainerCustomChest;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IDimension;
import noppes.npcs.api.IEntityDamageSource;
import noppes.npcs.api.ILayerModel;
import noppes.npcs.api.IMetods;
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
import noppes.npcs.api.entity.data.IEmotion;
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
import noppes.npcs.api.event.NpcEvent.DamagedEvent;
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
import noppes.npcs.api.handler.IKeyBinding;
import noppes.npcs.api.handler.IQuestHandler;
import noppes.npcs.api.handler.IRecipeHandler;
import noppes.npcs.api.handler.capability.INbtHandler;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.api.handler.data.IBorder;
import noppes.npcs.api.handler.data.IDataElement;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IDialogCategory;
import noppes.npcs.api.handler.data.IDialogOption;
import noppes.npcs.api.handler.data.IFaction;
import noppes.npcs.api.handler.data.IKeySetting;
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
import noppes.npcs.api.wrapper.WrapperEntityData;
import noppes.npcs.api.wrapper.WrapperNpcAPI;
import noppes.npcs.api.wrapper.data.DataElement;
import noppes.npcs.api.wrapper.data.StoredData;
import noppes.npcs.api.wrapper.data.TempData;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiComponentWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiItemSlotWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiLabelWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiScrollWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTextFieldWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTexturedRectWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTimerWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.blocks.CustomBlockPortal;
import noppes.npcs.blocks.CustomBlockSlab;
import noppes.npcs.blocks.CustomBlockStairs;
import noppes.npcs.blocks.CustomChest;
import noppes.npcs.blocks.CustomLiquid;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.blocks.tiles.TileScriptedDoor;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.client.model.animation.EmotionConfig;
import noppes.npcs.client.util.InterfaseData;
import noppes.npcs.client.util.MethodData;
import noppes.npcs.client.util.ParameterData;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.IScriptBlockHandler;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.KeyController;
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
import noppes.npcs.controllers.data.KeyConfig;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.PlayerCompassHUDData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.PlayerOverlayHUD;
import noppes.npcs.controllers.data.PlayerScriptData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
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
import noppes.npcs.items.CustomShield;
import noppes.npcs.items.CustomTool;
import noppes.npcs.items.CustomWeapon;
import noppes.npcs.items.crafting.NpcShapedRecipes;
import noppes.npcs.items.crafting.NpcShapelessRecipes;
import noppes.npcs.particles.CustomParticle;
import noppes.npcs.particles.CustomParticleSettings;
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
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.LayerModel;
import noppes.npcs.util.ScriptData;

public enum EnumInterfaceData {
	
	IAbility(new InterfaseData(IAbility.class, 
			null,
			new Class<?>[] { AbstractAbility.class },
			"interfase.iability", 
			new MethodData(int.class, "getRNG", "method.iability.getrnd"),
			new MethodData(void.class, "startCombat", "method.iability.startCombat"),
			new MethodData(boolean.class, "canRun", "method.iability.canrun",
				new ParameterData(EntityLivingBase.class, "target", "parameter.entity")
			),
			new MethodData(void.class, "endAbility", "method.iability.endability")
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
			new MethodData(void.class, "handleEvent", "method.iabilitydamaged.handleevent",
				new ParameterData(DamagedEvent.class, "damagedEvent", "parameter.iabilitydamaged.damagedevent")
			)
		)
	),
	IAbilityUpdate(new InterfaseData(IAbilityUpdate.class, 
			IAbility.class,
			new Class<?>[] { AbilitySmash.class, AbilityTeleport.class, AbilitySnare.class, AbilityPull.class },
			"interfase.iabilityupdate", 
			new MethodData(void.class, "update", "method.iabilityupdate.update"),
			new MethodData(boolean.class, "isActive", "method.iabilityupdate.isactive")
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
			new MethodData(String.class, "getName", "method.ianimation.getname"),
			new MethodData(void.class, "setName", "method.ianimation.setname",
				new ParameterData(String.class, "name", "parameter.name")
			),
			new MethodData(int.class, "getType", "method.ianimation.gettype"),
			new MethodData(IAnimationFrame.class, "addFrame", "method.ianimation.addframe",
				new ParameterData(IAnimationFrame.class, "", "parameter.animation.frame")
			),
			new MethodData(IAnimationFrame.class, "addFrame", "method.ianimation.addframe"),
			new MethodData(IAnimationFrame.class, "getFrame", "method.ianimation.getframe",
				new ParameterData(int.class, "", "parameter.animation.frame")
			),
			new MethodData(IAnimationFrame[].class, "getFrames", "method.ianimation.getframes"),
			new MethodData(void.class, "setNbt", "method.ianimation.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MethodData(boolean.class, "isDisable", "method.ianimation.isdisable"),
			new MethodData(void.class, "setRepeatLast", "method.ianimation.setrepeatLast",
				new ParameterData(int.class, "frames", "parameter.count")
			),
			new MethodData(int.class, "getRepeatLast", "method.ianimation.getrepeatLast"),
			new MethodData(boolean.class, "removeFrame", "method.ianimation.removeframe",
				new ParameterData(IAnimationFrame.class, "frame", "parameter.animation.frame")
			),
			new MethodData(boolean.class, "removeFrame", "method.ianimation.removeframe",
				new ParameterData(int.class, "frame", "parameter.animation.frame")
			),
			new MethodData(void.class, "startToNpc", "method.ianimation.starttonpc",
				new ParameterData(ICustomNpc.class, "npc", "parameter.npc")
			),
			new MethodData(void.class, "setDisable", "method.ianimation.setdisable",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MethodData(INbt.class, "getNbt", "method.ianimation.getnbt")
		)
	),
	IAnimationFrame(new InterfaseData(IAnimationFrame.class, 
			null,
			new Class<?>[] { AnimationFrameConfig.class },
			"interfase.ianimationframe", 
			new MethodData(void.class, "setSpeed", "method.ianimationframe.setspeed",
				new ParameterData(int.class, "ticks", "parameter.ticks")
			),
			new MethodData(int.class, "getSpeed", "method.ianimationframe.getspeed"),
			new MethodData(void.class, "setSmooth", "method.ianimationframe.setsmooth",
				new ParameterData(boolean.class, "isSmooth", "parameter.boolean")
			),
			new MethodData(void.class, "setEndDelay", "method.ianimationframe.setenddelay",
				new ParameterData(int.class, "ticks", "parameter.ticks")
			),
			new MethodData(boolean.class, "isSmooth", "method.ianimationframe.issmooth"),
			new MethodData(int.class, "getEndDelay", "method.ianimationframe.getenddelay"),
			new MethodData(IAnimationPart.class, "getPart", "method.ianimationframe.getpart",
				new ParameterData(int.class, "id", "parameter.ianimationframe.id")
			)
		)
	),
	IAnimationHandler(new InterfaseData(IAnimationHandler.class, 
			null,
			new Class<?>[] { AnimationController.class },
			"interfase.ianimationhandler", 
			new MethodData(IAnimation[].class, "getAnimations", "method.ianimationhandler.getanimations",
				new ParameterData(int.class, "animationType", "parameter.animation.type")
			),
			new MethodData(IAnimation.class, "getAnimation", "method.ianimationhandler.getanimation",
				new ParameterData(String.class, "animationType", "parameter.animation.type")
			),
			new MethodData(IAnimation.class, "getAnimation", "method.ianimationhandler.getanimation",
				new ParameterData(int.class, "animationType", "parameter.animation.id")
			),
			new MethodData(boolean.class, "removeAnimation", "method.ianimationhandler.removeanimation",
				new ParameterData(String.class, "animationId", "parameter.ianimationhandler.animationid")
			),
			new MethodData(boolean.class, "removeAnimation", "method.ianimationhandler.removeanimation",
				new ParameterData(int.class, "animationId", "parameter.animation.id")
			),
			new MethodData(IAnimation.class, "createNew", "method.ianimationhandler.createnew",
				new ParameterData(int.class, "animationType", "parameter.animation.type")
			)
		)
	),
	IAnimationPart(new InterfaseData(IAnimationPart.class, 
			null,
			null,
			"interfase.ianimationpart", 
			new MethodData(void.class, "clear", "method.ianimationpart.clear"),
			new MethodData(float[].class, "getOffset", "method.ianimationpart.getoffset"),
			new MethodData(void.class, "setOffset", "method.ianimationpart.setoffset",
				new ParameterData(float.class, "x", "parameter.posdx"),
				new ParameterData(float.class, "y", "parameter.posdx"),
				new ParameterData(float.class, "z", "parameter.posdx")
			),
			new MethodData(void.class, "setScale", "method.ianimationpart.setscale",
				new ParameterData(float.class, "x", "parameter.scalex"),
				new ParameterData(float.class, "y", "parameter.scalex"),
				new ParameterData(float.class, "z", "parameter.scalex")
			),
			new MethodData(float[].class, "getScale", "method.ianimationpart.getscale"),
			new MethodData(void.class, "setRotation", "method.ianimationpart.setrotation",
				new ParameterData(float.class, "x", "parameter.rotx"),
				new ParameterData(float.class, "y", "parameter.rotx"),
				new ParameterData(float.class, "z", "parameter.rotx")
			),
			new MethodData(float[].class, "getRotation", "method.ianimationpart.getrotation"),
			new MethodData(boolean.class, "isDisable", "method.ianimationpart.isdisable"),
			new MethodData(void.class, "setDisable", "method.ianimationpart.setdisable",
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
			new MethodData(void.class, "remove", "method.iattributeset.remove"),
			new MethodData(int.class, "getSlot", "method.iattributeset.getslot"),
			new MethodData(void.class, "setValues", "method.iattributeset.setvalues",
				new ParameterData(double.class, "min", "parameter.iattributeset.min"),
				new ParameterData(double.class, "max", "parameter.iattributeset.max")
			),
			new MethodData(double.class, "getMaxValue", "method.iattributeset.getmaxvalue"),
			new MethodData(double.class, "getMinValue", "method.iattributeset.getminvalue"),
			new MethodData(void.class, "setSlot", "method.iattributeset.setslot",
				new ParameterData(int.class, "slot", "parameter.slot")
			),
			new MethodData(String.class, "getAttribute", "method.iattributeset.getattribute"),
			new MethodData(void.class, "setAttribute", "method.iattributeset.setattribute",
				new ParameterData(String.class, "attribute", "parameter.iattributeset.attribute")
			),
			new MethodData(void.class, "setAttribute", "method.iattributeset.setattribute",
				new ParameterData(IAttribute.class, "attribute", "parameter.attribute")
			),
			new MethodData(double.class, "getChance", "method.iattributeset.getchance"),
			new MethodData(void.class, "setChance", "method.iattributeset.setchance",
				new ParameterData(double.class, "chance", "drop.chance")
			)
		)
	),
	IAvailability(new InterfaseData(IAvailability.class, 
			null,
			new Class<?>[] { Availability.class },
			"interfase.iavailability", 
			new MethodData(boolean.class, "isAvailable", "method.iavailability.isavailable",
				new ParameterData(IPlayer.class, "player", "parameter.player")
			),
			new MethodData(void.class, "setHealth", "method.iavailability.sethealth",
				new ParameterData(int.class, "value", "parameter.health"),
				new ParameterData(int.class, "type", "parameter.health.type")
			),
			new MethodData(void.class, "setQuest", "method.iavailability.setquest",
				new ParameterData(int.class, "id", "parameter.quest.id"),
				new ParameterData(int.class, "type", "parameter.quest.type")
			),
			new MethodData(void.class, "setDialog", "method.iavailability.setdialog",
				new ParameterData(int.class, "id", "parameter.dialog.id"),
				new ParameterData(int.class, "type", "parameter.dialog.type")
			),
			new MethodData(int.class, "getHealth", "method.iavailability.gethealth"),
			new MethodData(boolean.class, "hasQuest", "method.iavailability.hasquest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MethodData(void.class, "setScoreboard", "method.iavailability.setscoreboard",
				new ParameterData(String.class, "objective", "parameter.score.objective"),
				new ParameterData(int.class, "type", "parameter.score.type"),
				new ParameterData(int.class, "value", "parameter.score")
			),
			new MethodData(void.class, "setFaction", "method.iavailability.setfaction",
				new ParameterData(int.class, "id", "parameter.faction.id"),
				new ParameterData(int.class, "type", "parameter.faction.type"),
				new ParameterData(int.class, "stance", "parameter.faction.stance")
			),
			new MethodData(boolean.class, "hasDialog", "method.iavailability.hasdialog",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			),
			new MethodData(void.class, "removeDialog", "method.iavailability.removedialog",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			),
			new MethodData(void.class, "removeQuest", "method.iavailability.removequest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MethodData(int[].class, "getDaytime", "method.iavailability.getdaytime"),
			new MethodData(boolean.class, "hasFaction", "method.iavailability.hasfaction",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MethodData(boolean.class, "hasScoreboard", "method.iavailability.hasscoreboard",
				new ParameterData(String.class, "objective", "parameter.score.objective")
			),
			new MethodData(void.class, "removeFaction", "method.iavailability.removefaction",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MethodData(void.class, "removeScoreboard", "method.iavailability.removescoreboard",
				new ParameterData(String.class, "objective", "parameter.score.objective")
			),
			new MethodData(void.class, "setDaytime", "method.iavailability.setdaytime",
				new ParameterData(int.class, "type", "parameter.score.daytype")
			),
			new MethodData(void.class, "setDaytime", "method.iavailability.setdaytime",
				new ParameterData(int.class, "minHour", "parameter.min.hour"),
				new ParameterData(int.class, "maxHour", "parameter.max.hour")
			),
			new MethodData(int.class, "getHealthType", "method.iavailability.gethealthtype"),
			new MethodData(int.class, "getMinPlayerLevel", "method.iavailability.getminplayerlevel"),
			new MethodData(void.class, "setMinPlayerLevel", "method.iavailability.setminplayerlevel",
				new ParameterData(int.class, "level", "parameter.level")
			)
		)
	),
	IBlock(new InterfaseData(IBlock.class, 
			null,
			new Class<?>[] { BlockWrapper.class },
			"interfase.iblock", 
			new MethodData(void.class, "remove", "method.iblock.remove"),
			new MethodData(String.class, "getName", "method.iblock.getname"),
			new MethodData(String.class, "getDisplayName", "method.iblock.getdisplayname"),
			new MethodData(boolean.class, "hasTileEntity", "method.iblock.hastileentity"),
			new MethodData(int.class, "getX", "method.getx"),
			new MethodData(int.class, "getZ", "method.getz"),
			new MethodData(int.class, "getY", "method.gety"),
			new MethodData(boolean.class, "isAir", "method.iblock.isair"),
			new MethodData(IPos.class, "getPos", "method.getpos"),
			new MethodData(IWorld.class, "getWorld", "method.iblock.getworld"),
			new MethodData(IContainer.class, "getContainer", "method.iblock.getcontainer"),
			new MethodData(IData.class, "getStoreddata", "method.iblock.getstoreddata"),
			new MethodData(IData.class, "getTempdata", "method.gettempdata"),
			new MethodData(void.class, "interact", "method.iblock.interact",
				new ParameterData(int.class, "side", "parameter.iblock.interact")
			),
			new MethodData(boolean.class, "isContainer", "method.iblock.iscontainer"),
			new MethodData(int.class, "getMetadata", "method.iblock.getmetadata"),
			new MethodData(Block.class, "getMCBlock", "method.iblock.getmcblock"),
			new MethodData(IBlockState.class, "getMCBlockState", "method.iblock.getmcblockstate"),
			new MethodData(TileEntity.class, "getMCTileEntity", "method.iblock.getmctileentity"),
			new MethodData(INbt.class, "getTileEntityNBT", "method.iblock.gettileentitynbt"),
			new MethodData(void.class, "setMetadata", "method.iblock.setmetadata",
				new ParameterData(int.class, "i", "parameter.block.metadata")
			),
			new MethodData(void.class, "setTileEntityNBT", "method.iblock.settileentitynbt",
				new ParameterData(INbt.class, "nbt", "parameter.iblock.settileentitynbt")
			),
			new MethodData(IBlock.class, "setBlock", "method.iblock.setblock",
				new ParameterData(IBlock.class, "block", "parameter.iblock.setblock.0")
			),
			new MethodData(IBlock.class, "setBlock", "method.iblock.setblock",
				new ParameterData(String.class, "block", "parameter.block.name")
			),
			new MethodData(boolean.class, "isRemoved", "method.iblock.isremoved"),
			new MethodData(void.class, "blockEvent", "method.iblock.blockevent",
				new ParameterData(int.class, "type", "parameter.iblock.blockevent.0"),
				new ParameterData(int.class, "data", "parameter.iblock.blockevent.1")
			)
		)
	),
	IBlockFluidContainer(new InterfaseData(IBlockFluidContainer.class, 
			IBlock.class,
			new Class<?>[] { BlockFluidContainerWrapper.class },
			"interfase.iblockfluidcontainer", 
			new MethodData(String.class, "getFluidName", "method.iblockfluidcontainer.getfluidname"),
			new MethodData(float.class, "getFluidPercentage", "method.iblockfluidcontainer.getfluidpercentage"),
			new MethodData(float.class, "getFuildTemperature", "method.iblockfluidcontainer.getfuildtemperature"),
			new MethodData(float.class, "getFluidValue", "method.iblockfluidcontainer.getfluidvalue"),
			new MethodData(float.class, "getFuildDensity", "method.iblockfluidcontainer.getfuilddensity")
		)
	),
	IBlockScripted(new InterfaseData(IBlockScripted.class, 
			IBlock.class,
			new Class<?>[] { BlockScriptedWrapper.class },
			"interfase.iblockscripted", 
			new MethodData(void.class, "trigger", "method.trigger",
				new ParameterData(int.class, "id", "parameter.trigger.id"),
				new ParameterData(Object[].class, "arguments", "parameter.trigger.arguments")
			),
			new MethodData(int.class, "getRedstonePower", "method.iblockscripted.getredstonepower"),
			new MethodData(int.class, "getRotationX", "method.getrotx"),
			new MethodData(int.class, "getRotationZ", "method.getrotz"),
			new MethodData(void.class, "setScale", "method.iblockscripted.setscale",
				new ParameterData(float.class, "x", "parameter.iblockscripted.scalex"),
				new ParameterData(float.class, "y", "parameter.iblockscripted.scaley"),
				new ParameterData(float.class, "z", "parameter.iblockscripted.scalez")
			),
			new MethodData(String.class, "executeCommand", "method.executecommand",
				new ParameterData(String.class, "command", "parameter.command")
			),
			new MethodData(int.class, "getLight", "method.iblockscripted.getlight"),
			new MethodData(IItemStack.class, "getModel", "method.iblockscripted.getmodel"),
			new MethodData(void.class, "setModel", "method.iblockscripted.setmodel.3",
				new ParameterData(IBlock.class, "block", "parameter.block")
			),
			new MethodData(void.class, "setModel", "method.iblockscripted.setmodel.1",
				new ParameterData(String.class, "blockName", "parameter.iblockscripted.blockname")
			),
			new MethodData(void.class, "setModel", "method.iblockscripted.setmodel.0",
				new ParameterData(String.class, "blockName", "parameter.iblockscripted.blockname"),
				new ParameterData(int.class, "meta", "parameter.iblockscripted.meta")
			),
			new MethodData(void.class, "setModel", "method.iblockscripted.setmodel.2",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(ITimers.class, "getTimers", "method.iblockscripted.gettimers"),
			new MethodData(void.class, "setRotation", "method.iblockscripted.setrotation",
				new ParameterData(int.class, "x", "parameter.rotx"),
				new ParameterData(int.class, "y", "parameter.roty"),
				new ParameterData(int.class, "z", "parameter.rotz")
			),
			new MethodData(void.class, "setRedstonePower", "method.iblockscripted.setredstonepower",
				new ParameterData(int.class, "power", "parameter.iblockscripted.strength")
			),
			new MethodData(float.class, "getHardness", "method.iblockscripted.gethardness"),
			new MethodData(float.class, "getResistance", "method.iblockscripted.getresistance"),
			new MethodData(boolean.class, "getIsLadder", "method.iblockscripted.getisladder"),
			new MethodData(boolean.class, "getIsPassible", "method.iblockscripted.getispassible"),
			new MethodData(int.class, "getRotationY", "method.getroty"),
			new MethodData(float.class, "getScaleZ", "method.iblockscripted.getscalez"),
			new MethodData(ITextPlane.class, "getTextPlane", "method.iblockscripted.gettextplane"),
			new MethodData(ITextPlane.class, "getTextPlane2", "method.iblockscripted.gettextplane2"),
			new MethodData(ITextPlane.class, "getTextPlane3", "method.iblockscripted.gettextplane3"),
			new MethodData(ITextPlane.class, "getTextPlane4", "method.iblockscripted.gettextplane4"),
			new MethodData(ITextPlane.class, "getTextPlane5", "method.iblockscripted.gettextplane5"),
			new MethodData(ITextPlane.class, "getTextPlane6", "method.iblockscripted.gettextplane6"),
			new MethodData(void.class, "setIsLadder", "method.iblockscripted.setIsLadder",
				new ParameterData(boolean.class, "enabled", "parameter.iblockscripted.enabled")
			),
			new MethodData(void.class, "setIsPassible", "method.iblockscripted.setispassible",
				new ParameterData(boolean.class, "passible", "parameter.iblockscripted.passible")
			),
			new MethodData(void.class, "setLight", "method.iblockscripted.setlight",
				new ParameterData(int.class, "value", "parameter.iblockscripted.light")
			),
			new MethodData(ILayerModel[].class, "getLayerModels", "method.iblockscripted.getlayermodels"),
			new MethodData(ILayerModel.class, "createLayerModel", "method.iblockscripted.createlayermodel"),
			new MethodData(void.class, "updateModel", "method.iblockscripted.updatemodel"),
			new MethodData(void.class, "setResistance", "method.iblockscripted.setresistance",
				new ParameterData(float.class, "resistance", "parameter.iblockscripted.resistance")
			),
			new MethodData(void.class, "setHardness", "method.iblockscripted.sethardness",
				new ParameterData(float.class, "hardness", "parameter.iblockscripted.hardness")
			),
			new MethodData(float.class, "getScaleX", "method.iblockscripted.getscalex"),
			new MethodData(float.class, "getScaleY", "method.iblockscripted.getscaley")
		)
	),
	IBlockScriptedDoor(new InterfaseData(IBlockScriptedDoor.class, 
			IBlock.class,
			new Class<?>[] { BlockScriptedDoorWrapper.class },
			"interfase.iblockscripteddoor", 
			new MethodData(ITimers.class, "getTimers", "method.iblockscripted.gettimers"),
			new MethodData(float.class, "getHardness", "method.iblockscripted.gethardness"),
			new MethodData(boolean.class, "getOpen", "method.iblockscripteddoor.getopen"),
			new MethodData(String.class, "getBlockModel", "method.iblockscripted.getmodel"),
			new MethodData(float.class, "getResistance", "method.iblockscripted.getresistance"),
			new MethodData(void.class, "setBlockModel", "method.iblockscripted.setmodel.1",
				new ParameterData(String.class, "name", "parameter.block.name")
			),
			new MethodData(void.class, "setOpen", "method.iblockscripteddoor.setopen",
				new ParameterData(boolean.class, "open", "parameter.boolean")
			),
			new MethodData(void.class, "setResistance", "method.iblockscripted.setresistance",
				new ParameterData(float.class, "resistance", "parameter.iblockscripted.resistance")
			),
			new MethodData(void.class, "setHardness", "method.iblockscripted.sethardness",
				new ParameterData(float.class, "hardness", "parameter.iblockscripted.hardness")
			)
		)
	),
	IBorder(new InterfaseData(IBorder.class, 
			null,
			new Class<?>[] { Zone3D.class },
			"interfase.iborder", 
			new MethodData(void.class, "offset", "method.iborder.offset",
				new ParameterData(Point.class, "point", "parameter.point")
			),
			new MethodData(void.class, "offset", "method.iborder.offset",
				new ParameterData(IPos.class, "point", "parameter.iborder.point")
			),
			new MethodData(void.class, "offset", "method.iborder.offset",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(void.class, "update", "method.iborder.update"),
			new MethodData(void.class, "clear", "method.iborder.removepoint"),
			new MethodData(String.class, "getName", "method.iborder.getname"),
			new MethodData(boolean.class, "contains", "method.iborder.contains",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(boolean.class, "contains", "method.iborder.contains",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz"),
				new ParameterData(double.class, "height", "parameter.height")
			),
			new MethodData(int.class, "size", "method.iborder.size"),
			new MethodData(String.class, "getMessage", "method.iborder.getmessage"),
			new MethodData(int.class, "getId", "method.iborder.getid"),
			new MethodData(void.class, "setName", "method.iborder.setname",
				new ParameterData(String.class, "name", "parameter.name")
			),
			new MethodData(void.class, "setColor", "method.iborder.setcolor",
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MethodData(void.class, "setMessage", "method.iborder.setmessage",
				new ParameterData(String.class, "message", "parameter.message")
			),
			new MethodData(int.class, "getColor", "method.iborder.getcolor"),
			new MethodData(Point[].class, "getClosestPoints", "method.iborder.getclosestpoints",
				new ParameterData(Point.class, "point", "parameter.point"),
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MethodData(int.class, "getClosestPoint", "method.iborder.getclosestpoint",
				new ParameterData(Point.class, "point", "parameter.point"),
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MethodData(void.class, "setDimensionId", "method.iborder.setdimensionid",
				new ParameterData(int.class, "dimID", "parameter.dimension.id")
			),
			new MethodData(IPos.class, "getHomePos", "method.iborder.gethomepos"),
			new MethodData(Point[].class, "getPoints", "method.iborder.getpoints"),
			new MethodData(void.class, "setShowToPlayers", "method.iborder.setshowtoplayers",
				new ParameterData(boolean.class, "show", "parameter.boolean")
			),
			new MethodData(boolean.class, "isShowToPlayers", "method.iborder.isshowtoplayers"),
			new MethodData(void.class, "scaling", "method.iborder.scaling",
				new ParameterData(double.class, "scale", "parameter.iborder.scale"),
				new ParameterData(boolean.class, "type", "parameter.iborder.type")
			),
			new MethodData(void.class, "scaling", "method.iborder.scaling",
				new ParameterData(float.class, "scale", "parameter.scale"),
				new ParameterData(boolean.class, "type", "parameter.iborder.type")
			),
			new MethodData(void.class, "centerOffsetTo", "method.iborder.centeroffsetto",
				new ParameterData(IPos.class, "point", "parameter.iborder.point"),
				new ParameterData(boolean.class, "type", "parameter.iborder.type")
			),
			new MethodData(void.class, "centerOffsetTo", "method.iborder.centeroffsetto",
				new ParameterData(Point.class, "point", "parameter.point"),
				new ParameterData(boolean.class, "type", "parameter.iborder.type")
			),
			new MethodData(void.class, "centerOffsetTo", "method.iborder.centeroffsetto",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz"),
				new ParameterData(boolean.class, "type", "parameter.iborder.type")
			),
			new MethodData(int.class, "getDimensionId", "method.iborder.getdimensionid"),
			new MethodData(int.class, "getMaxX", "method.iborder.getmaxx"),
			new MethodData(int.class, "getMaxY", "method.iborder.getmaxy"),
			new MethodData(int.class, "getMinX", "method.iborder.getminx"),
			new MethodData(int.class, "getMinY", "method.iborder.getminy"),
			new MethodData(IAvailability.class, "getAvailability", "method.iborder.getavailability"),
			new MethodData(void.class, "setHomePos", "method.iborder.sethomepos",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(IPos.class, "getCenter", "method.iborder.getcenter"),
			new MethodData(void.class, "setNbt", "method.iborder.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MethodData(double.class, "distanceTo", "method.iborder.distanceto",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MethodData(double.class, "distanceTo", "method.iborder.distanceto",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "z", "parameter.posz")
			),
			new MethodData(Point.class, "setPoint", "method.iborder.setpoint",
				new ParameterData(int.class, "index", "parameter.iborder.index"),
				new ParameterData(IPos.class, "point", "parameter.iborder.point")
			),
			new MethodData(Point.class, "setPoint", "method.iborder.setpoint",
				new ParameterData(int.class, "index", "parameter.iborder.index"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(Point.class, "setPoint", "method.iborder.setpoint",
				new ParameterData(int.class, "index", "parameter.iborder.index"),
				new ParameterData(Point.class, "point", "parameter.point")
			),
			new MethodData(Point.class, "setPoint", "method.iborder.setpoint",
				new ParameterData(int.class, "index", "parameter.iborder.index"),
				new ParameterData(Point.class, "point", "parameter.point"),
				new ParameterData(int.class, "y", "parameter.posy")
			),
			new MethodData(INbt.class, "getNbt", "method.iborder.getnbt"),
			new MethodData(Point.class, "addPoint", "method.iborder.addpoint",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(Point.class, "addPoint", "method.iborder.addpoint",
				new ParameterData(Point.class, "point", "parameter.point"),
				new ParameterData(int.class, "y", "parameter.posy")
			),
			new MethodData(Point.class, "addPoint", "method.iborder.addpoint",
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MethodData(int.class, "getMaxZ", "method.iborder.getmaxz"),
			new MethodData(int.class, "getMinZ", "method.iborder.getminz"),
			new MethodData(boolean.class, "removePoint", "method.iborder.removepoint",
				new ParameterData(Point.class, "point", "parameter.point")
			),
			new MethodData(boolean.class, "removePoint", "method.iborder.removepoint",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(boolean.class, "insertPoint", "method.iborder.insertpoint",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz"),
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MethodData(boolean.class, "insertPoint", "method.iborder.insertpoint",
				new ParameterData(IPos.class, "pos0", "parameter.pos"),
				new ParameterData(IPos.class, "pos1", "parameter.pos")
			),
			new MethodData(boolean.class, "insertPoint", "method.iborder.insertpoint",
				new ParameterData(Point.class, "point", "parameter.point"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(IPos.class, "pos", "parameter.pos")
			)
		)
	),
	IBorderHandler(new InterfaseData(IBorderHandler.class, 
			null,
			new Class<?>[] { BorderController.class },
			"interfase.iborderhandler", 
			new MethodData(IBorder.class, "getRegion", "method.iborderhandler.getregion",
				new ParameterData(int.class, "regionId", "parameter.iborderhandler.regionid")
			),
			new MethodData(boolean.class, "removeRegion", "method.iborderhandler.removeregion",
				new ParameterData(int.class, "regionId", "parameter.iborderhandler.regionid")
			),
			new MethodData(IBorder[].class, "getRegions", "method.iborderhandler.getregions",
				new ParameterData(int.class, "dimID", "parameter.dimension.id")
			),
			new MethodData(IBorder[].class, "getAllRegions", "method.iborderhandler.getregions"),
			new MethodData(IBorder.class, "createNew", "method.iborderhandler.createnew",
				new ParameterData(int.class, "dimID", "parameter.dimension.id"),
				new ParameterData(IPos.class, "pos", "parameter.pos")
			)
		)
	),
	IButton(new InterfaseData(IButton.class, 
			ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiButtonWrapper.class },
			"interfase.ibutton", 
			new MethodData(IButton.class, "setSize", "method.component.setsize",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MethodData(String.class, "getTexture", "method.component.gettexture"),
			new MethodData(String.class, "getLabel", "method.ibutton.getlabel"),
			new MethodData(int.class, "getWidth", "method.component.getwidth"),
			new MethodData(int.class, "getHeight", "method.component.getheight"),
			new MethodData(IButton.class, "setTexture", "method.component.settexture",
				new ParameterData(String.class, "texture", "parameter.texture")
			),
			new MethodData(int.class, "getTextureX", "method.component.gettexturex"),
			new MethodData(int.class, "getTextureY", "method.component.gettexturey"),
			new MethodData(boolean.class, "hasTexture", "method.ibutton.hastexture"),
			new MethodData(IButton.class, "setLabel", "method.ibutton.setlabel",
				new ParameterData(String.class, "lable", "parameter.component.title")
			),
			new MethodData(IButton.class, "setTextureOffset", "method.component.settextureoffset",
				new ParameterData(int.class, "textureX", "parameter.texturex"),
				new ParameterData(int.class, "textureY", "parameter.texturey")
			)
		)
	),
	ICloneHandler(new InterfaseData(ICloneHandler.class, 
			null,
			new Class<?>[] { ServerCloneController.class },
			"interfase.iclonehandler", 
			new MethodData(void.class, "remove", "method.iclone.remove",
				new ParameterData(int.class, "tab", "parameter.iclone.tab"),
				new ParameterData(String.class, "name", "parameter.iclone.name")
			),
			new MethodData(IEntity.class, "get", "method.iclone.get",
				new ParameterData(int.class, "tab", "parameter.iclone.tab"),
				new ParameterData(String.class, "name", "parameter.iclone.name"),
				new ParameterData(IWorld.class, "world", "parameter.world")
			),
			new MethodData(void.class, "set", "method.iclone.set",
				new ParameterData(int.class, "tab", "parameter.iclone.tab"),
				new ParameterData(String.class, "name", "parameter.iclone.name"),
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MethodData(IEntity.class, "spawn", "method.iclone.spawn",
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
			new MethodData(String.class, "getName", "method.icompassdata.getname"),
			new MethodData(void.class, "setName", "method.icompassdata.setname",
				new ParameterData(String.class, "name", "parameter.icompassdata.name")
			),
			new MethodData(int.class, "getType", "method.icompassdata.gettype"),
			new MethodData(void.class, "setPos", "method.icompassdata.setpos",
				new ParameterData(IPos.class, "pos", "parameter.icompassdata.pos")
			),
			new MethodData(void.class, "setPos", "method.icompassdata.setpos",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(IPos.class, "getPos", "method.icompassdata.getpos"),
			new MethodData(void.class, "setTitle", "method.icompassdata.settitle",
				new ParameterData(String.class, "title", "parameter.icompassdata.title")
			),
			new MethodData(void.class, "setType", "method.icompassdata.settype",
				new ParameterData(int.class, "type", "parameter.icompassdata.type")
			),
			new MethodData(String.class, "getTitle", "method.icompassdata.gettitle"),
			new MethodData(void.class, "setRange", "method.icompassdata.setrange",
				new ParameterData(int.class, "range", "parameter.icompassdata.range")
			),
			new MethodData(int.class, "getDimensionID", "method.icompassdata.getdimensionid"),
			new MethodData(void.class, "setDimensionID", "method.icompassdata.setdimensionid",
				new ParameterData(int.class, "dimID", "parameter.dimension.id")
			),
			new MethodData(void.class, "setShow", "method.icompassdata.setshow",
				new ParameterData(boolean.class, "show", "parameter.icompassdata.show")
			),
			new MethodData(boolean.class, "isShow", "method.icompassdata.isshow"),
			new MethodData(String.class, "getNPCName", "method.icompassdata.getnpcname"),
			new MethodData(void.class, "setNPCName", "method.icompassdata.setnpcname",
				new ParameterData(String.class, "npcName", "parameter.icompassdata.npcname")
			),
			new MethodData(int.class, "getRange", "method.icompassdata.getrange")
		)
	),
	IContainer(new InterfaseData(IContainer.class, 
			null,
			new Class<?>[] { ContainerWrapper.class },
			"interfase.icontainer", 
			new MethodData(int.class, "count", "method.icontainer.count",
				new ParameterData(IItemStack.class, "item", "parameter.item.found"),
				new ParameterData(boolean.class, "ignoreDamage", "parameter.ignoredamage"),
				new ParameterData(boolean.class, "ignoreNBT", "parameter.ignorenbt")
			),
			new MethodData(IItemStack.class, "getSlot", "method.icontainer.getslot",
				new ParameterData(int.class, "slot", "parameter.slot")
			),
			new MethodData(int.class, "getSize", "method.icontainer.getsize"),
			new MethodData(Container.class, "getMCContainer", "method.icontainer.getmccontainer"),
			new MethodData(IInventory.class, "getMCInventory", "method.icontainer.getmcinventory"),
			new MethodData(void.class, "setSlot", "method.icontainer.setslot",
				new ParameterData(int.class, "slot", "parameter.slot"),
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(IItemStack[].class, "getItems", "method.icontainer.getitems")
		)
	),
	IContainerCustomChest(new InterfaseData(IContainerCustomChest.class, 
			IContainer.class,
			new Class<?>[] { ContainerCustomChestWrapper.class },
			"interfase.icontainercustomchest", 
			new MethodData(String.class, "getName", "method.icontainercustomchest.getname"),
			new MethodData(void.class, "setName", "method.icontainercustomchest.setname",
				new ParameterData(String.class, "name", "parameter.icontainercustomchest.name")
			)
		)
	),
	ICustomDrop(new InterfaseData(ICustomDrop.class, 
			null,
			new Class<?>[] { DropSet.class },
			"interfase.icustomdrop", 
			new MethodData(void.class, "remove", "method.icustomdrop.remove"),
			new MethodData(IItemStack.class, "getItem", "method.icustomdrop.getitem"),
			new MethodData(IAttributeSet.class, "addAttribute", "method.icustomdrop.addattribute",
				new ParameterData(String.class, "attributeName", "parameter.attribute.name")
			),
			new MethodData(void.class, "removeAttribute", "method.icustomdrop.removeattribute",
				new ParameterData(IAttributeSet.class, "attribute", "parameter.icustomdrop.attribute")
			),
			new MethodData(void.class, "removeEnchant", "method.icustomdrop.removeenchant",
				new ParameterData(IEnchantSet.class, "enchant", "parameter.icustomdrop.enchant")
			),
			new MethodData(int.class, "getQuestID", "method.icustomdrop.getquestid"),
			new MethodData(IItemStack.class, "createLoot", "method.icustomdrop.createloot",
				new ParameterData(double.class, "addChance", "parameter.chance")
			),
			new MethodData(boolean.class, "getLootMode", "method.icustomdrop.getlootmode"),
			new MethodData(boolean.class, "getTiedToLevel", "method.icustomdrop.gettiedtolevel"),
			new MethodData(void.class, "setQuestID", "method.icustomdrop.setquestid",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MethodData(void.class, "setItem", "method.icustomdrop.setitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(float.class, "getDamage", "method.icustomdrop.getdamage"),
			new MethodData(void.class, "setDamage", "method.icustomdrop.setdamage",
				new ParameterData(float.class, "dam", "parameter.icustomdrop.dam")
			),
			new MethodData(void.class, "setAmount", "method.icustomdrop.setamount",
				new ParameterData(int.class, "min", "parameter.min"),
				new ParameterData(int.class, "max", "parameter.max")
			),
			new MethodData(int.class, "getMinAmount", "method.icustomdrop.getminamount"),
			new MethodData(int.class, "getMaxAmount", "method.icustomdrop.getmaxamount"),
			new MethodData(double.class, "getChance", "method.icustomdrop.getchance"),
			new MethodData(IEnchantSet[].class, "getEnchantSets", "method.icustomdrop.getenchantsets"),
			new MethodData(IAttributeSet[].class, "getAttributeSets", "method.icustomdrop.getattributesets"),
			new MethodData(IDropNbtSet[].class, "getDropNbtSets", "method.icustomdrop.getdropnbtsets"),
			new MethodData(void.class, "resetTo", "method.icustomdrop.resetto",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(IEnchantSet.class, "addEnchant", "method.icustomdrop.addenchant",
				new ParameterData(int.class, "enchantId", "parameter.enchant.id")
			),
			new MethodData(IEnchantSet.class, "addEnchant", "method.icustomdrop.addenchant",
				new ParameterData(String.class, "enchantId", "parameter.enchant.id")
			),
			new MethodData(IDropNbtSet.class, "addDropNbtSet", "method.icustomdrop.adddropnbtset",
				new ParameterData(int.class, "type", "parameter.idropnbtset.type"),
				new ParameterData(double.class, "chance", "parameter.chance"),
				new ParameterData(String.class, "paht", "parameter.idropnbtset.path"),
				new ParameterData(String[].class, "values", "parameter.idropnbtset.values.1")
			),
			new MethodData(void.class, "removeDropNbt", "method.icustomdrop.removedropnbt",
				new ParameterData(IDropNbtSet.class, "nbt", "parameter.icustomdrop.nbt")
			),
			new MethodData(void.class, "setLootMode", "method.icustomdrop.setlootmode",
				new ParameterData(boolean.class, "lootMode", "parameter.icustomdrop.lootMode")
			),
			new MethodData(void.class, "setTiedToLevel", "method.icustomdrop.settiedtolevel",
				new ParameterData(boolean.class, "tiedToLevel", "parameter.icustomdrop.tiedtolevel")
			),
			new MethodData(void.class, "setChance", "method.icustomdrop.setchance",
				new ParameterData(double.class, "chance", "drop.chance")
			)
		)
	),
	ICustomElement(new InterfaseData(ICustomElement.class, 
			null,
			new Class<?>[] { CustomBow.class, CustomLiquid.class, CustomParticle.class, CustomFood.class, CustomArmor.class, CustomTool.class, CustomBlockSlab.class, CustomChest.class, CustomBlockPortal.class, CustomPotion.class, CustomShield.class, CustomParticleSettings.class, CustomBlockStairs.class, CustomFishingRod.class, CustomWeapon.class },
			"interfase.icustomelement", 
			new MethodData(String.class, "getCustomName", "method.icustomelement.getcustomname"),
			new MethodData(INbt.class, "getCustomNbt", "method.icustomelement.getcustomnbt")
		)
	),
	ICustomGui(new InterfaseData(ICustomGui.class, 
			null,
			new Class<?>[] { CustomGuiWrapper.class },
			"interfase.icustomgui", 
			new MethodData(void.class, "update", "method.icustomgui.update",
				new ParameterData(IPlayer.class, "player", "parameter.player")
			),
			new MethodData(int.class, "getId", "method.icustomgui.getid"),
			new MethodData(void.class, "setSize", "method.component.setsize",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MethodData(void.class, "setDoesPauseGame", "method.icustomgui.setdoespausegame",
				new ParameterData(boolean.class, "pauseGame", "parameter.icustomgui.pausegame")
			),
			new MethodData(ICustomGuiComponent.class, "getComponent", "method.icustomgui.getcomponent",
				new ParameterData(int.class, "id", "parameter.component.id")
			),
			new MethodData(IButton.class, "addButton", "method.icustomgui.addbutton",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(String.class, "label", "parameter.component.title"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy")
			),
			new MethodData(IButton.class, "addButton", "method.icustomgui.addbutton",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(String.class, "label", "parameter.component.title"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MethodData(int.class, "getWidth", "method.component.getwidth"),
			new MethodData(int.class, "getHeight", "method.component.getheight"),
			new MethodData(IButton.class, "addTexturedButton", "method.icustomgui.addtexturedbutton",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(String.class, "label", "parameter.component.title"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(String.class, "texture", "parameter.texture")
			),
			new MethodData(IButton.class, "addTexturedButton", "method.icustomgui.addtexturedbutton",
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
			new MethodData(ICustomGuiComponent[].class, "getComponents", "method.icustomgui.getcomponents"),
			new MethodData(ITexturedRect.class, "addTexturedRect", "method.icustomgui.addtexturedrect",
				new ParameterData(int.class, "id", "parameter.icustomgui.id"),
				new ParameterData(String.class, "texture", "parameter.texture"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(int.class, "textureX", "parameter.texturex"),
				new ParameterData(int.class, "textureY", "parameter.texturey")
			),
			new MethodData(ITexturedRect.class, "addTexturedRect", "method.icustomgui.addtexturedrect",
				new ParameterData(int.class, "id", "parameter.icustomgui.id"),
				new ParameterData(String.class, "texture", "parameter.texture"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MethodData(IItemSlot.class, "addItemSlot", "method.icustomgui.additemslot",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy")
			),
			new MethodData(IItemSlot.class, "addItemSlot", "method.icustomgui.additemslot",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(IItemStack.class, "stack", "parameter.stack")
			),
			new MethodData(IItemSlot[].class, "getSlots", "method.icustomgui.getslots"),
			new MethodData(ILabel.class, "addLabel", "method.icustomgui.addlabel",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(String.class, "label", "parameter.component.title"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MethodData(ILabel.class, "addLabel", "method.icustomgui.addlabel",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(String.class, "label", "parameter.component.title"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MethodData(IScroll.class, "addScroll", "method.icustomgui.addscroll",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(String[].class, "list", "parameter.list")
			),
			new MethodData(ITextField.class, "addTextField", "method.icustomgui.addtextfield",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MethodData(void.class, "updateComponent", "method.icustomgui.updatecomponent",
				new ParameterData(ICustomGuiComponent.class, "component", "parameter.component")
			),
			new MethodData(void.class, "setBackgroundTexture", "method.icustomgui.setbackgroundtexture",
				new ParameterData(String.class, "resourceLocation", "parameter.texture")
			),
			new MethodData(void.class, "setBackgroundTexture", "method.icustomgui.setbackgroundtexture",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(int.class, "textureX", "parameter.texturex"),
				new ParameterData(int.class, "textureY", "parameter.texturey"),
				new ParameterData(int.class, "stretched", "parameter.component.stretched"),
				new ParameterData(String.class, "resourceLocation", "parameter.texture")
			),
			new MethodData(void.class, "showPlayerInventory", "method.icustomgui.showplayerinventory",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy")
			),
			new MethodData(void.class, "removeComponent", "method.icustomgui.removecomponent",
				new ParameterData(int.class, "id", "parameter.component.id")
			)
		)
	),
	ICustomGuiComponent(new InterfaseData(ICustomGuiComponent.class, 
			null,
			new Class<?>[] { CustomGuiComponentWrapper.class },
			"interfase.icustomguicomponent", 
			new MethodData(int.class, "getId", "method.icustomguicom.getid"),
			new MethodData(int.class, "getPosX", "method.icustomguicom.getposx"),
			new MethodData(int.class, "getPosY", "method.icustomguicom.getposy"),
			new MethodData(ICustomGuiComponent.class, "setId", "method.icustomguicom.setid",
				new ParameterData(int.class, "id", "parameter.component.id")
			),
			new MethodData(ICustomGuiComponent.class, "setPos", "method.icustomguicom.setpos",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy")
			),
			new MethodData(void.class, "offSet", "method.icustomguicom.offset",
				new ParameterData(int.class, "type", "parameter.component.offset")
			),
			new MethodData(String[].class, "getHoverText", "method.icustomguicom.getHoverText"),
			new MethodData(boolean.class, "hasHoverText", "method.icustomguicom.hashovertext"),
			new MethodData(ICustomGuiComponent.class, "setHoverText", "method.icustomguicom.setHovertext",
				new ParameterData(String.class, "hover", "parameter.hover")
			),
			new MethodData(ICustomGuiComponent.class, "setHoverText", "method.icustomguicom.sethovertext",
				new ParameterData(String[].class, "hover", "parameter.hover")
			)
		)
	),
	ICustomNpc(new InterfaseData(ICustomNpc.class, 
			IEntityLiving.class,
			new Class<?>[] { NPCWrapper.class },
			"interfase.icustomnpc", 
			new MethodData(void.class, "trigger", "method.trigger",
				new ParameterData(int.class, "id", "parameter.trigger.id"),
				new ParameterData(Object[].class, "arguments", "parameter.trigger.arguments")
			),
			new MethodData(void.class, "reset", "method.icustomnpc.reset"),
			new MethodData(IEntityLivingBase.class, "getOwner", "method.icustomnpc.getowner"),
			new MethodData(void.class, "say", "method.icustomnpc.say",
				new ParameterData(String.class, "message", "parameter.message")
			),
			new MethodData(INPCAnimation.class, "getAnimations", "method.getanimations"),
			new MethodData(INPCAdvanced.class, "getAdvanced", "method.icustomnpc.getadvanced"),
			new MethodData(INPCAi.class, "getAi", "method.icustomnpc.getai"),
			new MethodData(int.class, "getHomeX", "method.icustomnpc.gethomex"),
			new MethodData(int.class, "getHomeY", "method.icustomnpc.gethomey"),
			new MethodData(int.class, "getHomeZ", "method.icustomnpc.gethomez"),
			new MethodData(INPCJob.class, "getJob", "method.icustomnpc.getjob"),
			new MethodData(INPCRole.class, "getRole", "method.icustomnpc.getrole"),
			new MethodData(void.class, "sayTo", "method.icustomnpc.sayto",
				new ParameterData(IPlayer.class, "player", "parameter.player"),
				new ParameterData(String.class, "message", "parameter.message")
			),
			new MethodData(void.class, "setDialog", "method.icustomnpc.setdialog",
				new ParameterData(int.class, "id", "parameter.dialog.id"),
				new ParameterData(IDialog.class, "dialog", "parameter.dialog")
			),
			new MethodData(void.class, "setHome", "method.icustomnpc.sethome",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(IProjectile.class, "shootItem", "method.icustomnpc.shootitem",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz"),
				new ParameterData(IItemStack.class, "item", "parameter.stack"),
				new ParameterData(int.class, "count", "parameter.count")
			),
			new MethodData(IProjectile.class, "shootItem", "method.icustomnpc.shootitem",
				new ParameterData(IEntityLivingBase.class, "entity", "parameter.entity"),
				new ParameterData(IItemStack.class, "item", "parameter.stack"),
				new ParameterData(int.class, "count", "parameter.count")
			),
			new MethodData(String.class, "executeCommand", "method.executecommand",
				new ParameterData(String.class, "command", "parameter.command")
			),
			new MethodData(void.class, "updateClient", "method.icustomnpc.updateclient"),
			new MethodData(IFaction.class, "getFaction", "method.icustomnpc.getfaction"),
			new MethodData(void.class, "setFaction", "method.icustomnpc.setfaction",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MethodData(IDialog.class, "getDialog", "method.icustomnpc.getdialog",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			),
			new MethodData(ITimers.class, "getTimers", "method.icustomnpc.gettimers"),
			new MethodData(void.class, "giveItem", "method.icustomnpc.giveitem",
				new ParameterData(IPlayer.class, "player", "parameter.player"),
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(INPCStats.class, "getStats", "method.icustomnpc.getstats"),
			new MethodData(INPCInventory.class, "getInventory", "method.icustomnpc.getinventory"),
			new MethodData(INPCDisplay.class, "getDisplay", "method.icustomnpc.getdisplay")
		)
	),
	IDamageSource(new InterfaseData(IDamageSource.class, 
			null,
			new Class<?>[] { DamageSourceWrapper.class },
			"interfase.idamagesource", 
			new MethodData(String.class, "getType", "method.idamagesource.gettype"),
			new MethodData(boolean.class, "isProjectile", "method.idamagesource.isprojectile"),
			new MethodData(IEntity.class, "getTrueSource", "method.idamagesource.gettruesource"),
			new MethodData(boolean.class, "isUnblockable", "method.idamagesource.isunblockable"),
			new MethodData(IEntity.class, "getImmediateSource", "method.idamagesource.getimmediatesource"),
			new MethodData(DamageSource.class, "getMCDamageSource", "method.idamagesource.getmcdamagesource")
		)
	),
	IData(new InterfaseData(IData.class, 
			null,
			new Class<?>[] { StoredData.class, TempData.class },
			"interfase.idata", 
			new MethodData(boolean.class, "remove", "method.idata.remove",
				new ParameterData(String.class, "key", "parameter.key")
			),
			new MethodData(Object.class, "get", "method.idata.get",
				new ParameterData(String.class, "key", "parameter.key")
			),
			new MethodData(void.class, "put", "method.idata.put",
				new ParameterData(String.class, "key", "parameter.key"),
				new ParameterData(Object.class, "value", "parameter.value")
			),
			new MethodData(void.class, "clear", "method.idata.clear"),
			new MethodData(String[].class, "getKeys", "method.idata.getkeys"),
			new MethodData(boolean.class, "has", "method.idata.has",
				new ParameterData(String.class, "key", "parameter.key")
			),
			new MethodData(void.class, "setNbt", "method.idata.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MethodData(INbt.class, "getNbt", "method.idata.getnbt")
		)
	),
	IDataElement(new InterfaseData(IDataElement.class, 
			null,
			new Class<?>[] { DataElement.class },
			"interfase.idataelement", 
			new MethodData(Object.class, "invoke", "method.idatablement.getobject",
				new ParameterData(Object[].class, "values", "parameter.idatablement.values")
			),
			new MethodData(Object.class, "getObject", "method.idatablement.getobject"),
			new MethodData(String.class, "getName", "method.idatablement.getname"),
			new MethodData(Object.class, "getValue", "method.idatablement.getvalue"),
			new MethodData(Class.class, "getParent", "method.idatablement.getparent"),
			new MethodData(boolean.class, "setValue", "method.idatablement.setvalue",
				new ParameterData(Object.class, "value", "parameter.idatablement.value")
			),
			new MethodData(int.class, "getType", "method.idatablement.gettype"),
			new MethodData(String.class, "getData", "method.idatablement.getdata"),
			new MethodData(boolean.class, "isBelong", "method.idatablement.isbelong",
				new ParameterData(Class.class, "clazz", "parameter.idatablement.class")
			)
		)
	),
	IDataObject(new InterfaseData(IDataObject.class, 
			null,
			new Class<?>[] { DataObject.class },
			"interfase.idataobject", 
			new MethodData(String.class, "get", "method.idataobject.get"),
			new MethodData(IDataElement[].class, "getClasses", "method.idataobject.getclasses"),
			new MethodData(IDataElement[].class, "getConstructors", "method.idataobject.getconstructors"),
			new MethodData(IDataElement.class, "getField", "method.idataobject.getfield",
				new ParameterData(String.class, "name", "parameter.field.name")
			),
			new MethodData(IDataElement[].class, "getFields", "method.idataobject.getfields"),
			new MethodData(IDataElement.class, "getMethod", "method.idataobject.getmethod",
				new ParameterData(String.class, "name", "parameter.method.name")
			),
			new MethodData(IDataElement[].class, "getMethods", "method.idataobject.getmethods"),
			new MethodData(String.class, "getInfo", "method.idataobject.getinfo"),
			new MethodData(String.class, "getConstructorsInfo", "method.idataobject.getconstructorsinfo"),
			new MethodData(IDataElement.class, "getClazz", "method.idataobject.getclazz",
				new ParameterData(String.class, "name", "parameter.class.name")
			),
			new MethodData(String.class, "getClassesInfo", "method.idataobject.getclassesinfo"),
			new MethodData(String.class, "getFieldsInfo", "method.idataobject.getfieldsinfo"),
			new MethodData(String.class, "getMethodsInfo", "method.idataobject.getmethodsinfo")
		)
	),
	IDialog(new InterfaseData(IDialog.class, 
			null,
			new Class<?>[] { Dialog.class },
			"interfase.idialog", 
			new MethodData(String.class, "getName", "method.idialog.getname"),
			new MethodData(int.class, "getId", "method.idialog.getid"),
			new MethodData(void.class, "setName", "method.idialog.setname",
				new ParameterData(String.class, "name", "parameter.name")
			),
			new MethodData(void.class, "save", "method.idialog.save"),
			new MethodData(IQuest.class, "getQuest", "method.idialog.getquest"),
			new MethodData(void.class, "setQuest", "method.idialog.setquest",
				new ParameterData(IQuest.class, "quest", "parameter.quest")
			),
			new MethodData(String.class, "getText", "method.idialog.gettext"),
			new MethodData(IDialogOption[].class, "getOptions", "method.idialog.getoptions"),
			new MethodData(void.class, "setText", "method.idialog.settext",
				new ParameterData(String.class, "text", "parameter.text")
			),
			new MethodData(IDialogCategory.class, "getCategory", "method.idialog.getcategory"),
			new MethodData(IAvailability.class, "getAvailability", "method.idialog.getavailability"),
			new MethodData(String.class, "getCommand", "method.idialog.getcommand"),
			new MethodData(void.class, "setCommand", "method.idialog.setcommand",
				new ParameterData(String.class, "command", "parameter.command")
			),
			new MethodData(IDialogOption.class, "getOption", "method.idialog.getOption",
				new ParameterData(int.class, "slot", "parameter.idialog.slot")
			)
		)
	),
	IDialogCategory(new InterfaseData(IDialogCategory.class, 
			null,
			new Class<?>[] { DialogCategory.class },
			"interfase.idialogcategory", 
			new MethodData(String.class, "getName", "method.idialogcategory.getname"),
			new MethodData(IDialog.class, "create", "method.idialogcategory.create"),
			new MethodData(IDialog[].class, "dialogs", "method.idialogcategory.dialogs")
		)
	),
	IDialogHandler(new InterfaseData(IDialogHandler.class, 
			null,
			new Class<?>[] { DialogController.class },
			"interfase.idialoghandler", 
			new MethodData(IDialog.class, "get", "method.idialoghandler.get",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			),
			new MethodData(IDialogCategory[].class, "categories", "method.idialoghandler.categories")
		)
	),
	IDialogOption(new InterfaseData(IDialogOption.class, 
			null,
			new Class<?>[] { DialogOption.class },
			"interfase.idialogoption", 
			new MethodData(String.class, "getName", "method.idialogoption.getname"),
			new MethodData(int.class, "getType", "method.idialogoption.gettype"),
			new MethodData(int.class, "getSlot", "method.idialogoption.getslot")
		)
	),
	IDimension(new InterfaseData(IDimension.class, 
			null,
			new Class<?>[] { DimensionWrapper.class },
			"interfase.idimension", 
			new MethodData(String.class, "getName", "method.idimension.getname"),
			new MethodData(int.class, "getId", "method.idimension.getid"),
			new MethodData(String.class, "getSuffix", "method.idimension.getsuffix")
		)
	),
	IDimensionHandler(new InterfaseData(IDimensionHandler.class, 
			null,
			new Class<?>[] { DimensionHandler.class },
			"interfase.idimensionhandler", 
			new MethodData(int[].class, "getAllIDs", "method.idimensionhandler.getallids"),
			new MethodData(IWorldInfo.class, "getMCWorldInfo", "method.idimensionhandler.getmcworldinfo",
				new ParameterData(int.class, "dimensionID", "parameter.dimension.id")
			),
			new MethodData(void.class, "deleteDimension", "method.idimensionhandler.delete",
				new ParameterData(int.class, "dimensionID", "parameter.dimension.id")
			),
			new MethodData(IWorldInfo.class, "createDimension", "method.idimensionhandler.create"),
			new MethodData(void.class, "setNbt", "method.idimensionhandler.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MethodData(INbt.class, "getNbt", "method.idimensionhandler.getnbt")
		)
	),
	IDropNbtSet(new InterfaseData(IDropNbtSet.class, 
			null,
			new Class<?>[] { DropNbtSet.class },
			"interfase.idropnbtset", 
			new MethodData(void.class, "remove", "method.idropnbtset.remove"),
			new MethodData(int.class, "getType", "method.idropnbtset.gettype"),
			new MethodData(String.class, "getPath", "method.idropnbtset.getpath"),
			new MethodData(void.class, "setValues", "method.idropnbtset.setvalues",
				new ParameterData(String.class, "values", "parameter.idropnbtset.values.0")
			),
			new MethodData(void.class, "setValues", "method.idropnbtset.setvalues",
				new ParameterData(String[].class, "values", "parameter.idropnbtset.values.1")
			),
			new MethodData(void.class, "setType", "method.idropnbtset.settype",
				new ParameterData(int.class, "type", "parameter.idropnbtset.type")
			),
			new MethodData(void.class, "setPath", "method.idropnbtset.setpath",
				new ParameterData(String.class, "path", "parameter.idropnbtset.path")
			),
			new MethodData(void.class, "setTypeList", "method.idropnbtset.settypelist",
				new ParameterData(int.class, "type", "parameter.idropnbtset.typelist")
			),
			new MethodData(int.class, "getTypeList", "method.idropnbtset.gettypelist"),
			new MethodData(String[].class, "getValues", "method.idropnbtset.getvalues"),
			new MethodData(double.class, "getChance", "method.idropnbtset.getchance"),
			new MethodData(void.class, "setChance", "method.idropnbtset.setchance",
				new ParameterData(double.class, "chance", "parameter.idropnbtset.chance")
			),
			new MethodData(INbt.class, "getConstructoredTag", "method.idropnbtset.getconstructoredtag",
				new ParameterData(INbt.class, "nbt", "parameter.idropnbtset.nbt")
			)
		)
	),
	IEmotion(new InterfaseData(IEmotion.class, 
			null,
			new Class<?>[] { EmotionConfig.class },
			"interfase.iemotion"
		)
	),
	IEnchantSet(new InterfaseData(IEnchantSet.class, 
			null,
			new Class<?>[] { EnchantSet.class },
			"interfase.ienchantset", 
			new MethodData(void.class, "remove", "method.ienchantset.remove"),
			new MethodData(int.class, "getMinLevel", "method.ienchantset.getminlevel"),
			new MethodData(void.class, "setLevels", "method.ienchantset.setlevels",
				new ParameterData(int.class, "min", "parameter.min"),
				new ParameterData(int.class, "max", "parameter.max")
			),
			new MethodData(void.class, "setEnchant", "method.ienchantset.setenchant",
				new ParameterData(Enchantment.class, "enchant", "parameter.enchant")
			),
			new MethodData(boolean.class, "setEnchant", "method.ienchantset.setenchant",
				new ParameterData(int.class, "enchant", "parameter.enchant")
			),
			new MethodData(boolean.class, "setEnchant", "method.ienchantset.setenchant",
				new ParameterData(String.class, "enchant", "parameter.enchant.id")
			),
			new MethodData(String.class, "getEnchant", "method.ienchantset.getenchant"),
			new MethodData(int.class, "getMaxLevel", "method.ienchantset.getmaxlevel"),
			new MethodData(double.class, "getChance", "method.ienchantset.getchance"),
			new MethodData(void.class, "setChance", "method.ienchantset.setchance",
				new ParameterData(double.class, "chance", "parameter.ienchantset.chance")
			)
		)
	),
	IEntity(new InterfaseData(IEntity.class, 
			Entity.class,
			new Class<?>[] { EntityWrapper.class },
			"interfase.ientity", 
			new MethodData(String.class, "getName", "method.ientity.getname"),
			new MethodData(String.class, "getTypeName", "method.ientity.gettypename"),
			new MethodData(boolean.class, "isAlive", "method.ientity.isalive"),
			new MethodData(void.class, "setName", "method.ientity.setname",
				new ParameterData(String.class, "name", "parameter.ientity.setname")
			),
			new MethodData(int.class, "getType", "method.ientity.gettype"),
			new MethodData(boolean.class, "isSneaking", "method.ientity.issneaking"),
			new MethodData(String.class, "getUUID", "method.ientity.getuuid"),
			new MethodData(void.class, "extinguish", "method.ientity.extinguish"),
			new MethodData(void.class, "setPosition", "method.ientity.setpos",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz")
			),
			new MethodData(boolean.class, "isSprinting", "method.ientity.issprinting"),
			new MethodData(float.class, "getEyeHeight", "method.ientity.geteyeheight"),
			new MethodData(void.class, "spawn", "method.ientity.spawn"),
			new MethodData(void.class, "damage", "method.ientity.damage",
				new ParameterData(float.class, "amount", "parameter.ientity.damageamount")
			),
			new MethodData(void.class, "damage", "method.ientity.damage",
				new ParameterData(float.class, "amount", "parameter.ientity.damageamount"),
				new ParameterData(IEntityDamageSource.class, "source", "parameter.ientity.damagesource")
			),
			new MethodData(String.class, "getEntityName", "method.ientity.getentityname"),
			new MethodData(long.class, "getAge", "method.ientity.getage"),
			new MethodData(IEntityItem.class, "dropItem", "method.ientity.dropitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(double.class, "getX", "method.getx"),
			new MethodData(double.class, "getZ", "method.getz"),
			new MethodData(double.class, "getY", "method.gety"),
			new MethodData(float.class, "getPitch", "method.ientity.getpitch"),
			new MethodData(void.class, "setPos", "method.ientity.setpos",
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MethodData(IPos.class, "getPos", "method.getpos"),
			new MethodData(IWorld.class, "getWorld", "method.ientity.getworld"),
			new MethodData(boolean.class, "isBurning", "method.ientity.isburning"),
			new MethodData(float.class, "getWidth", "method.ientity.getwidth"),
			new MethodData(float.class, "getHeight", "method.ientity.getheight"),
			new MethodData(IData.class, "getStoreddata", "method.getstoreddata"),
			new MethodData(IData.class, "getTempdata", "method.gettempdata"),
			new MethodData(Entity.class, "getMCEntity", "method.ientity.getmcentity"),
			new MethodData(void.class, "addRider", "method.ientity.addrider",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MethodData(void.class, "clearRiders", "method.ientity.clearriders"),
			new MethodData(void.class, "despawn", "method.ientity.despawn"),
			new MethodData(String.class, "generateNewUUID", "method.ientity.newuuid"),
			new MethodData(IEntity[].class, "getAllRiders", "method.ientity.getallriders"),
			new MethodData(int.class, "getBlockX", "method.ientity.getblockx"),
			new MethodData(int.class, "getBlockY", "method.ientity.getblocky"),
			new MethodData(int.class, "getBlockZ", "method.ientity.getblockz"),
			new MethodData(INbt.class, "getEntityNbt", "method.ientity.getentitynbt"),
			new MethodData(double.class, "getMotionX", "method.ientity.getmotionx"),
			new MethodData(double.class, "getMotionY", "method.ientity.getmotiony"),
			new MethodData(double.class, "getMotionZ", "method.ientity.getmotionz"),
			new MethodData(IEntity.class, "getMount", "method.ientity.getmount"),
			new MethodData(IEntity[].class, "getRiders", "method.ientity.getriders"),
			new MethodData(boolean.class, "hasTag", "method.ientity.hastag",
				new ParameterData(String.class, "tag", "parameter.ientity.tag")
			),
			new MethodData(boolean.class, "inFire", "method.ientity.infire"),
			new MethodData(boolean.class, "inLava", "method.ientity.inlava"),
			new MethodData(void.class, "playAnimation", "method.ientity.playanimation",
				new ParameterData(int.class, "type", "parameter.ientity.animtype")
			),
			new MethodData(IRayTrace.class, "rayTraceBlock", "method.ientity.raytraceblock",
				new ParameterData(double.class, "distance", "parameter.ientity.raydistance"),
				new ParameterData(boolean.class, "stopOnLiquid", "parameter.ientity.raystoponliquid"),
				new ParameterData(boolean.class, "ignoreBlockWithoutBoundingBox", "parameter.ientity.rayignoreblockwithoutboundingbox")
			),
			new MethodData(IEntity[].class, "rayTraceEntities", "method.ientity.raytraceentities",
				new ParameterData(double.class, "distance", "parameter.ientity.raydistance"),
				new ParameterData(boolean.class, "stopOnLiquid", "parameter.ientity.raystoponliquid"),
				new ParameterData(boolean.class, "ignoreBlockWithoutBoundingBox", "parameter.ientity.rayignoreblockwithoutboundingbox")
			),
			new MethodData(void.class, "setEntityNbt", "method.ientity.setentitynbt",
				new ParameterData(INbt.class, "nbt", "parameter.ientity.entitynbt")
			),
			new MethodData(void.class, "setMotionX", "method.ientity.setmotionx",
				new ParameterData(double.class, "motion", "parameter.ientity.motion")
			),
			new MethodData(void.class, "setMotionY", "method.ientity.setmotiony",
				new ParameterData(double.class, "motion", "parameter.ientity.motion")
			),
			new MethodData(void.class, "setMotionZ", "method.ientity.setmotionz",
				new ParameterData(double.class, "motion", "parameter.ientity.motion")
			),
			new MethodData(void.class, "setMount", "method.ientity.setmount",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MethodData(void.class, "setX", "method.setx",
				new ParameterData(double.class, "x", "parameter.posx")
			),
			new MethodData(void.class, "setZ", "method.setz",
				new ParameterData(double.class, "z", "parameter.posz")
			),
			new MethodData(void.class, "storeAsClone", "method.ientity.storeasclone",
				new ParameterData(int.class, "tab", "parameter.clone.tab"),
				new ParameterData(String.class, "name", "parameter.clone.file")
			),
			new MethodData(boolean.class, "typeOf", "method.ientity.typeof",
				new ParameterData(int.class, "type", "parameter.ientity.typeof")
			),
			new MethodData(void.class, "setRotation", "method.ientity.setrotation",
				new ParameterData(float.class, "rotation", "parameter.ientity.setrotation")
			),
			new MethodData(boolean.class, "inWater", "method.ientity.inwater"),
			new MethodData(String[].class, "getTags", "method.ientity.gettags"),
			new MethodData(void.class, "addTag", "method.ientity.addtag",
				new ParameterData(String.class, "tag", "parameter.ientity.tag")
			),
			new MethodData(void.class, "removeTag", "method.ientity.removetag",
				new ParameterData(String.class, "tag", "parameter.ientity.tag")
			),
			new MethodData(void.class, "setY", "method.sety",
				new ParameterData(double.class, "y", "parameter.posy")
			),
			new MethodData(boolean.class, "hasCustomName", "method.ientity.hascustomname"),
			new MethodData(void.class, "knockback", "method.ientity.knockback",
				new ParameterData(int.class, "power", "parameter.ientity.power"),
				new ParameterData(float.class, "direction", "parameter.yaw")
			),
			new MethodData(void.class, "setBurning", "method.ientity.setburning",
				new ParameterData(int.class, "seconds", "parameter.seconds")
			),
			new MethodData(float.class, "getRotation", "method.ientity.getrotation"),
			new MethodData(void.class, "setPitch", "method.ientity.setpitch",
				new ParameterData(float.class, "pitch", "parameter.ientity.setpitch")
			),
			new MethodData(INbt.class, "getNbt", "method.ientity.getnbt"),
			new MethodData(void.class, "kill", "method.ientity.kill")
		)
	),
	IEntityDamageSource(new InterfaseData(IEntityDamageSource.class, 
			null,
			new Class<?>[] { NpcEntityDamageSource.class },
			"interfase.ientitydamagesource", 
			new MethodData(String.class, "getType", "method.idamagesource.gettype"),
			new MethodData(IEntity.class, "getITrueSource", "method.idamagesource.gettruesource"),
			new MethodData(void.class, "setTrueSource", "method.ientitydamagesource.settruesource",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MethodData(String.class, "getDeadMessage", "method.ientitydamagesource.getdeadmessage"),
			new MethodData(void.class, "setDeadMessage", "method.ientitydamagesource.setdeadmessage",
				new ParameterData(String.class, "message", "parameter.message")
			),
			new MethodData(void.class, "setType", "method.ientitydamagesource.settype",
				new ParameterData(String.class, "damageType", "parameter.ientitydamagesource.damageType")
			),
			new MethodData(IEntity.class, "getIImmediateSource", "method.idamagesource.getimmediatesource"),
			new MethodData(void.class, "setImmediateSource", "method.ientitydamagesource.setimmediatesource",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			)
		)
	),
	IEntityItem(new InterfaseData(IEntityItem.class, 
			IEntity.class,
			new Class<?>[] { EntityItemWrapper.class },
			"interfase.ientityitem", 
			new MethodData(String.class, "getOwner", "method.ientityitem.getowner"),
			new MethodData(IItemStack.class, "getItem", "method.ientityitem.getitem"),
			new MethodData(void.class, "setPickupDelay", "method.ientityitem.setpickupdelay",
				new ParameterData(int.class, "delay", "parameter.ticks")
			),
			new MethodData(void.class, "setOwner", "method.ientityitem.setowner",
				new ParameterData(String.class, "name", "parameter.player.name")
			),
			new MethodData(long.class, "getAge", "method.ientity.getage"),
			new MethodData(void.class, "setLifeSpawn", "method.ientityitem.setlifespawn",
				new ParameterData(int.class, "age", "parameter.ticks")
			),
			new MethodData(int.class, "getPickupDelay", "method.ientityitem.getpickupdelay"),
			new MethodData(void.class, "setAge", "method.ientity.setage",
				new ParameterData(long.class, "age", "parameter.ticks")
			),
			new MethodData(int.class, "getLifeSpawn", "method.ientityitem.getlifespawn"),
			new MethodData(void.class, "setItem", "method.ientityitem.setitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			)
		)
	),
	IEntityLiving(new InterfaseData(IEntityLiving.class, 
			IEntityLivingBase.class,
			new Class<?>[] { EntityLivingWrapper.class },
			"interfase.ientityliving", 
			new MethodData(void.class, "jump", "method.ientityliving.jump"),
			new MethodData(void.class, "clearNavigation", "method.ientityliving.clearnavigation"),
			new MethodData(void.class, "navigateTo", "method.ientityliving.navigateto",
				new ParameterData(Integer[][].class, "posses", "Integer"),
				new ParameterData(double.class, "speed", "parameter.speed")
			),
			new MethodData(void.class, "navigateTo", "method.ientityliving.navigateto",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz"),
				new ParameterData(double.class, "speed", "parameter.speed")
			),
			new MethodData(boolean.class, "isNavigating", "method.ientityliving.isnavigating"),
			new MethodData(Entity.class, "getMCEntity", "method.ientity.getmcentity"),
			new MethodData(EntityLivingBase.class, "getMCEntity", "method.ientity.getmcentity"),
			new MethodData(EntityLiving.class, "getMCEntity", "method.ientity.getmcentity"),
			new MethodData(IPos.class, "getNavigationPath", "method.ientityliving.getnavigationpath")
		)
	),
	IEntityLivingBase(new InterfaseData(IEntityLivingBase.class, 
			IEntity.class,
			new Class<?>[] { EntityLivingBaseWrapper.class },
			"interfase.ientitylivingbase", 
			new MethodData(float.class, "getMaxHealth", "method.ientitylivingbase.getmaxhealth"),
			new MethodData(void.class, "setHealth", "method.ientitylivingbase.sethealth",
				new ParameterData(float.class, "health", "parameter.health")
			),
			new MethodData(void.class, "addPotionEffect", "method.ientitylivingbase.addpotioneffect",
				new ParameterData(int.class, "effect", "parameter.effect.id"),
				new ParameterData(int.class, "duration", "parameter.effect.duration"),
				new ParameterData(int.class, "strength", "parameter.effect.strength"),
				new ParameterData(boolean.class, "hideParticles", "parameter.effect.hideparticles")
			),
			new MethodData(boolean.class, "isChild", "method.ientitylivingbase.ischild"),
			new MethodData(float.class, "getHealth", "method.ientitylivingbase.gethealth"),
			new MethodData(IMark.class, "addMark", "method.ientitylivingbase.addmark",
				new ParameterData(int.class, "type", "parameter.imark.type")
			),
			new MethodData(IItemStack.class, "getArmor", "method.ientitylivingbase.getarmor",
				new ParameterData(int.class, "slot", "parameter.armor.slot")
			),
			new MethodData(Entity.class, "getMCEntity", "method.ientity.getmcentity"),
			new MethodData(EntityLivingBase.class, "getMCEntity", "method.ientity.getmcentity"),
			new MethodData(void.class, "setMaxHealth", "method.ientitylivingbase.setmaxhealth",
				new ParameterData(float.class, "health", "parameter.health")
			),
			new MethodData(boolean.class, "canSeeEntity", "method.ientitylivingbase.canseeentity",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MethodData(IEntityLivingBase.class, "getLastAttacked", "method.ientitylivingbase.getlastattacked"),
			new MethodData(IItemStack.class, "getMainhandItem", "method.ientitylivingbase.getmainhanditem"),
			new MethodData(IMark[].class, "getMarks", "method.ientitylivingbase.getmarks"),
			new MethodData(float.class, "getMoveForward", "method.ientitylivingbase.getmoveforward"),
			new MethodData(float.class, "getMoveStrafing", "method.ientitylivingbase.getmovestrafing"),
			new MethodData(float.class, "getMoveVertical", "method.ientitylivingbase.getmovevertical"),
			new MethodData(IItemStack.class, "getOffhandItem", "method.ientitylivingbase.getoffhanditem"),
			new MethodData(int.class, "getPotionEffect", "method.ientitylivingbase.getpotioneffect",
				new ParameterData(int.class, "effect", "parameter.effect.id")
			),
			new MethodData(void.class, "removeMark", "method.ientitylivingbase.removemark",
				new ParameterData(IMark.class, "mark", "parameter.imark")
			),
			new MethodData(void.class, "setArmor", "method.ientitylivingbase.setarmor",
				new ParameterData(int.class, "slot", "parameter.armor.slot"),
				new ParameterData(IItemStack.class, "item", "parameter.ientitylivingbase.item")
			),
			new MethodData(void.class, "setMainhandItem", "method.ientitylivingbase.setmainhanditem",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(void.class, "setOffhandItem", "method.ientitylivingbase.setoffhanditem",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(void.class, "swingMainhand", "method.ientitylivingbase.swingmainhand"),
			new MethodData(void.class, "swingOffhand", "method.ientitylivingbase.swingoffhand"),
			new MethodData(IEntityLivingBase.class, "getAttackTarget", "method.ientitylivingbase.getattacktarget"),
			new MethodData(void.class, "setAttackTarget", "method.ientitylivingbase.setattacktarget",
				new ParameterData(IEntityLivingBase.class, "living", "parameter.entity")
			),
			new MethodData(void.class, "setMoveForward", "method.ientitylivingbase.setmoveforward",
				new ParameterData(float.class, "move", "parameter.forward.move")
			),
			new MethodData(void.class, "setMoveVertical", "method.ientitylivingbase.setmovevertical",
				new ParameterData(float.class, "move", "parameter.vertical.move")
			),
			new MethodData(void.class, "setMoveStrafing", "method.ientitylivingbase.setmovestrafing",
				new ParameterData(float.class, "move", "parameter.strafing.move")
			),
			new MethodData(boolean.class, "isAttacking", "method.ientitylivingbase.isattacking"),
			new MethodData(void.class, "clearPotionEffects", "method.ientitylivingbase.clearpotioneffects"),
			new MethodData(int.class, "getLastAttackedTime", "method.ientitylivingbase.getlastattackedtime")
		)
	),
	IFaction(new InterfaseData(IFaction.class, 
			null,
			new Class<?>[] { Faction.class },
			"interfase.ifaction", 
			new MethodData(String.class, "getName", "method.ifaction.getname"),
			new MethodData(int.class, "getId", "method.ifaction.getid"),
			new MethodData(void.class, "save", "method.ifaction.save"),
			new MethodData(int.class, "getColor", "method.ifaction.getcolor"),
			new MethodData(void.class, "addHostile", "method.ifaction.addhostile",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MethodData(int[].class, "getHostileList", "method.ifaction.gethostilelist"),
			new MethodData(boolean.class, "getIsHidden", "method.ifaction.getishidden"),
			new MethodData(int.class, "getDefaultPoints", "method.ifaction.getdefaultpoints"),
			new MethodData(boolean.class, "hasHostile", "method.ifaction.hashostile",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MethodData(boolean.class, "hostileToFaction", "method.ifaction.hostiletofaction",
				new ParameterData(int.class, "factionId", "parameter.faction.id")
			),
			new MethodData(boolean.class, "hostileToNpc", "method.ifaction.hostiletonpc",
				new ParameterData(ICustomNpc.class, "npc", "parameter.npc")
			),
			new MethodData(int.class, "playerStatus", "method.ifaction.playerstatus",
				new ParameterData(IPlayer.class, "player", "parameter.player")
			),
			new MethodData(void.class, "removeHostile", "method.ifaction.removehostile",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MethodData(void.class, "setDefaultPoints", "method.ifaction.setdefaultpoints",
				new ParameterData(int.class, "points", "parameter.ifaction.points")
			),
			new MethodData(void.class, "setIsHidden", "method.ifaction.setIsHidden",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MethodData(boolean.class, "getAttackedByMobs", "method.ifaction.getAttackedByMobs"),
			new MethodData(void.class, "setAttackedByMobs", "method.ifaction.setattackedbymobs",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			)
		)
	),
	IFactionHandler(new InterfaseData(IFactionHandler.class, 
			null,
			new Class<?>[] { FactionController.class },
			"interfase.ifactionhandler", 
			new MethodData(IFaction.class, "get", "method.ifactionhandler.get",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MethodData(IFaction.class, "delete", "method.ifactionhandler.delete",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MethodData(IFaction.class, "create", "method.ifactionhandler.get",
				new ParameterData(String.class, "name", "parameter.faction.name"),
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MethodData(IFaction[].class, "list", "method.ifactionhandler.list")
		)
	),
	IGuiTimer(new InterfaseData(IGuiTimer.class, 
			ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiTimerWrapper.class },
			"interfase.iguitimer", 
			new MethodData(IGuiTimer.class, "setSize", "method.component.setsize",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MethodData(void.class, "setTime", "method.iguitimer.settime",
				new ParameterData(long.class, "start", "parameter.posx"),
				new ParameterData(long.class, "end", "parameter.posy")
			),
			new MethodData(IGuiTimer.class, "setColor", "method.component.setcolor",
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MethodData(String.class, "getText", "method.component.gettext"),
			new MethodData(IGuiTimer.class, "setScale", "method.component.setscale",
				new ParameterData(float.class, "scale", "parameter.scale")
			),
			new MethodData(int.class, "getColor", "method.iguitimer.getcolor"),
			new MethodData(int.class, "getWidth", "method.component.getwidth"),
			new MethodData(int.class, "getHeight", "method.component.getheight"),
			new MethodData(float.class, "getScale", "method.component.getscale")
		)
	),
	IItemArmor(new InterfaseData(IItemArmor.class, 
			IItemStack.class,
			new Class<?>[] { ItemArmorWrapper.class },
			"interfase.iitemarmor", 
			new MethodData(String.class, "getArmorMaterial", "method.iitemarmor.getarmormaterial"),
			new MethodData(int.class, "getArmorSlot", "method.iitemarmor.getarmorslot")
		)
	),
	IItemBlock(new InterfaseData(IItemBlock.class, 
			IItemStack.class,
			new Class<?>[] { ItemBlockWrapper.class },
			"interfase.iitemblock", 
			new MethodData(String.class, "getBlockName", "method.iitemblock.getblockname")
		)
	),
	IItemBook(new InterfaseData(IItemBook.class, 
			IItemStack.class,
			new Class<?>[] { ItemBookWrapper.class },
			"interfase.iitembook", 
			new MethodData(String[].class, "getText", "method.iitembook.gettext"),
			new MethodData(void.class, "setText", "method.iitembook.settext",
				new ParameterData(String[].class, "pages", "parameter.iitembook.pages")
			),
			new MethodData(void.class, "setTitle", "method.iitembook.settitle",
				new ParameterData(String.class, "title", "parameter.book.title")
			),
			new MethodData(String.class, "getTitle", "method.iitembook.gettitle"),
			new MethodData(void.class, "setAuthor", "method.iitembook.setauthor",
				new ParameterData(String.class, "author", "parameter.book.author")
			),
			new MethodData(String.class, "getAuthor", "method.iitembook.getauthor")
		)
	),
	IItemScripted(new InterfaseData(IItemScripted.class, 
			IItemStack.class,
			new Class<?>[] { ItemScriptedWrapper.class },
			"interfase.iitemscripted", 
			new MethodData(void.class, "setColor", "method.iitemscripted.setcolor",
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MethodData(String.class, "getTexture", "method.component.gettexture",
				new ParameterData(int.class, "damage", "parameter.item.meta")
			),
			new MethodData(int.class, "getColor", "method.iitemscripted.getcolor"),
			new MethodData(void.class, "setTexture", "method.iitemscripted.settexture",
				new ParameterData(int.class, "damage", "parameter.item.meta"),
				new ParameterData(String.class, "texture", "parameter.texture")
			),
			new MethodData(boolean.class, "hasTexture", "method.iitemscripted.hastexture",
				new ParameterData(int.class, "damage", "parameter.item.meta")
			),
			new MethodData(void.class, "setMaxStackSize", "method.iitemscripted.setmaxstacksize",
				new ParameterData(int.class, "size", "parameter.size")
			),
			new MethodData(int.class, "getDurabilityColor", "method.iitemscripted.getdurabilitycolor"),
			new MethodData(boolean.class, "getDurabilityShow", "method.iitemscripted.getdurabilityshow"),
			new MethodData(double.class, "getDurabilityValue", "method.iitemscripted.getdurabilityvalue"),
			new MethodData(void.class, "setDurabilityColor", "method.iitemscripted.setdurabilitycolor",
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MethodData(void.class, "setDurabilityShow", "method.iitemscripted.setdurabilityshow",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MethodData(void.class, "setDurabilityValue", "method.iitemscripted.setdurabilityvalue",
				new ParameterData(float.class, "value", "parameter.value")
			)
		)
	),
	IItemSlot(new InterfaseData(IItemSlot.class, 
			ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiItemSlotWrapper.class },
			"interfase.iitemslot", 
			new MethodData(IItemSlot.class, "setStack", "method.iitemslot.setstack",
				new ParameterData(IItemStack.class, "stack", "parameter.stack")
			),
			new MethodData(IItemStack.class, "getStack", "method.iitemslot.getstack"),
			new MethodData(Slot.class, "getMCSlot", "method.iitemslot.getmcslot"),
			new MethodData(boolean.class, "hasStack", "method.iitemslot.hasstack")
		)
	),
	IItemStack(new InterfaseData(IItemStack.class, 
			null,
			new Class<?>[] { ItemStackWrapper.class },
			"interfase.iitemstack", 
			new MethodData(String.class, "getName", "method.iitemstack.getname"),
			new MethodData(boolean.class, "compare", "method.iitemstack.compare",
				new ParameterData(IItemStack.class, "item", "parameter.stack"),
				new ParameterData(boolean.class, "ignoreNBT", "parameter.ignorenbt")
			),
			new MethodData(boolean.class, "isEmpty", "method.iitemstack.isempty"),
			new MethodData(IItemStack.class, "copy", "method.iitemstack.copy"),
			new MethodData(int.class, "getType", "method.iitemstack.gettype"),
			new MethodData(String.class, "getDisplayName", "method.iitemstack.getdisplayname"),
			new MethodData(int.class, "getFoodLevel", "method.iitemstack.getfoodlevel"),
			new MethodData(void.class, "damageItem", "method.iitemstack.damageitem",
				new ParameterData(int.class, "damage", "parameter.item.meta"),
				new ParameterData(IEntityLiving.class, "living", "parameter.entity")
			),
			new MethodData(double.class, "getAttribute", "method.iitemstack.getattribute",
				new ParameterData(String.class, "name", "parameter.attribute.name")
			),
			new MethodData(void.class, "setAttribute", "method.iitemstack.setattribute",
				new ParameterData(String.class, "name", "parameter.attribute.name"),
				new ParameterData(double.class, "value", "parameter.attribute.value"),
				new ParameterData(int.class, "slot", "parameter.ceil.slot")
			),
			new MethodData(void.class, "setAttribute", "method.iitemstack.setattribute",
				new ParameterData(String.class, "name", "parameter.attribute.name"),
				new ParameterData(double.class, "value", "parameter.attribute.value")
			),
			new MethodData(boolean.class, "hasAttribute", "method.iitemstack.hasattribute",
				new ParameterData(String.class, "name", "parameter.attribute.name")
			),
			new MethodData(ItemStack.class, "getMCItemStack", "method.iitemstack.getmcitemstack"),
			new MethodData(INbt.class, "getItemNbt", "method.iitemstack.getitemnbt"),
			new MethodData(String[].class, "getLore", "method.iitemstack.getlore"),
			new MethodData(int.class, "getMaxItemDamage", "method.iitemstack.getmaxitemdamage"),
			new MethodData(int.class, "getStackSize", "method.iitemstack.getstacksize"),
			new MethodData(IData.class, "getStoreddata", "method.getstoreddata"),
			new MethodData(IData.class, "getTempdata", "method.gettempdata"),
			new MethodData(boolean.class, "hasEnchant", "method.iitemstack.hasenchant",
				new ParameterData(int.class, "name", "parameter.iitemstack.name")
			),
			new MethodData(boolean.class, "hasEnchant", "method.iitemstack.hasenchant",
				new ParameterData(String.class, "name", "parameter.enchant.name")
			),
			new MethodData(boolean.class, "isEnchanted", "method.iitemstack.isenchanted"),
			new MethodData(boolean.class, "hasNbt", "method.iitemstack.hasnbt"),
			new MethodData(boolean.class, "isBlock", "method.iitemstack.isblock"),
			new MethodData(boolean.class, "isBook", "method.iitemstack.isbook"),
			new MethodData(boolean.class, "isWearable", "method.iitemstack.iswearable"),
			new MethodData(boolean.class, "removeEnchant", "method.iitemstack.removeenchant",
				new ParameterData(int.class, "name", "parameter.iitemstack.name")
			),
			new MethodData(boolean.class, "removeEnchant", "method.iitemstack.removeenchant",
				new ParameterData(String.class, "name", "parameter.enchant.name")
			),
			new MethodData(void.class, "removeNbt", "method.iitemstack.removenbt"),
			new MethodData(void.class, "setLore", "method.iitemstack.setlore",
				new ParameterData(String[].class, "lore", "parameter.iitemstack.lore")
			),
			new MethodData(void.class, "setStackSize", "method.iitemstack.setstacksize",
				new ParameterData(int.class, "size", "parameter.itemcount")
			),
			new MethodData(void.class, "addEnchantment", "method.iitemstack.addenchantment",
				new ParameterData(int.class, "id", "parameter.enchant.id"),
				new ParameterData(int.class, "level", "parameter.enchant.level")
			),
			new MethodData(void.class, "addEnchantment", "method.iitemstack.addenchantment",
				new ParameterData(String.class, "name", "parameter.enchant.name"),
				new ParameterData(int.class, "level", "parameter.enchant.level")
			),
			new MethodData(void.class, "setCustomName", "method.iitemstack.setcustomname",
				new ParameterData(String.class, "name", "parameter.iitemstack.name")
			),
			new MethodData(boolean.class, "hasCustomName", "method.iitemstack.hascustomName"),
			new MethodData(int.class, "getMaxStackSize", "method.iitemstack.getmaxstacksize"),
			new MethodData(void.class, "setItemDamage", "method.iitemstack.setitemdamage",
				new ParameterData(int.class, "value", "parameter.item.meta")
			),
			new MethodData(double.class, "getAttackDamage", "method.iitemstack.getattackdamage"),
			new MethodData(int.class, "getItemDamage", "method.iitemstack.getitemdamage"),
			new MethodData(String.class, "getItemName", "method.iitemstack.getitemname"),
			new MethodData(INbt.class, "getNbt", "method.iitemstack.getnbt")
		)
	),
	IJobBard(new InterfaseData(IJobBard.class, 
			null,
			new Class<?>[] { JobBard.class },
			"interfase.ijobbard", 
			new MethodData(String.class, "getSong", "method.ijobbard.getsong"),
			new MethodData(void.class, "setSong", "method.ijobbard.setsong",
				new ParameterData(String.class, "song", "parameter.sound.name")
			)
		)
	),
	IJobBuilder(new InterfaseData(IJobBuilder.class, 
			null,
			new Class<?>[] { JobBuilder.class },
			"interfase.ijobbuilder", 
			new MethodData(boolean.class, "isBuilding", "method.ijobbuilder.isbuilding")
		)
	),
	IJobFarmer(new InterfaseData(IJobFarmer.class, 
			null,
			new Class<?>[] { JobFarmer.class },
			"interfase.ijobfarmer", 
			new MethodData(boolean.class, "isPlucking", "method.ijobfarmer.isplucking")
		)
	),
	IJobFollower(new InterfaseData(IJobFollower.class, 
			INPCJob.class,
			new Class<?>[] { JobFollower.class },
			"interfase.ijobfollower", 
			new MethodData(boolean.class, "isFollowing", "method.ijobfollower.isFollowing"),
			new MethodData(String.class, "getFollowing", "method.ijobfollower.getfollowing"),
			new MethodData(ICustomNpc.class, "getFollowingNpc", "method.ijobfollower.getfollowingnpc"),
			new MethodData(void.class, "setFollowing", "method.ijobfollower.setfollowing",
				new ParameterData(String.class, "name", "parameter.entity.name")
			)
		)
	),
	IJobSpawner(new InterfaseData(IJobSpawner.class, 
			null,
			new Class<?>[] { JobSpawner.class },
			"interfase.ijobspawner", 
			new MethodData(IEntityLivingBase.class, "spawnEntity", "method.ijobspawner.spawnentity",
				new ParameterData(int.class, "pos", "parameter.ijobspawner.pos"),
				new ParameterData(boolean.class, "isDead", "parameter.ijobspawner.isDead")
			),
			new MethodData(void.class, "removeAllSpawned", "method.ijobspawner.removeallspawned")
		)
	),
	IKeyBinding(new InterfaseData(IKeyBinding.class, 
			null,
			new Class<?>[] { KeyController.class },
			"interfase.ikeybinding", 
			new MethodData(IKeySetting.class, "createKeySetting", "method.ikeybinding.createkeysetting"),
			new MethodData(IKeySetting.class, "getKeySetting", "method.ikeybinding.getkeysetting",
				new ParameterData(int.class, "id", "parameter.keyboard.key")
			),
			new MethodData(boolean.class, "removeKeySetting", "method.ikeybinding.removekeysetting",
				new ParameterData(int.class, "id", "parameter.keyboard.key")
			),
			new MethodData(IKeySetting[].class, "getKeySettings", "method.ikeybinding.getkeysettings")
		)
	),
	IKeySetting(new InterfaseData(IKeySetting.class, 
			null,
			new Class<?>[] { KeyConfig.class },
			"interfase.ikeysetting", 
			new MethodData(String.class, "getName", "method.ikeysetting.getname"),
			new MethodData(int.class, "getId", "method.ikeysetting.getid"),
			new MethodData(void.class, "setName", "method.ikeysetting.setname",
				new ParameterData(String.class, "name", "parameter.ikeysetting.name")
			),
			new MethodData(void.class, "setCategory", "method.ikeysetting.setcategory",
				new ParameterData(String.class, "name", "parameter.ikeysetting.catname")
			),
			new MethodData(String.class, "getCategory", "method.ikeysetting.getcategory"),
			new MethodData(void.class, "setKeyId", "method.ikeysetting.setkeyid",
				new ParameterData(int.class, "keyId", "parameter.keyboard.key")
			),
			new MethodData(void.class, "setModiferType", "method.ikeysetting.setmodifertype",
				new ParameterData(int.class, "type", "parameter.ikeysetting.type")
			),
			new MethodData(void.class, "setNbt", "method.ikeysetting.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MethodData(INbt.class, "getNbt", "method.ikeysetting.getnbt"),
			new MethodData(int.class, "getModiferType", "method.ikeysetting.getmodifertype"),
			new MethodData(int.class, "getKeyId", "method.ikeysetting.getkeyid")
		)
	),
	ILabel(new InterfaseData(ILabel.class, 
			ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiLabelWrapper.class },
			"interfase.ilabel", 
			new MethodData(ILabel.class, "setSize", "method.component.setsize",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MethodData(ILabel.class, "setColor", "method.component.setcolor",
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MethodData(String.class, "getText", "method.component.gettext"),
			new MethodData(ILabel.class, "setScale", "method.component.setscale",
				new ParameterData(float.class, "scale", "parameter.scale")
			),
			new MethodData(int.class, "getColor", "method.ilabel.getcolor"),
			new MethodData(ILabel.class, "setText", "method.component.settext",
				new ParameterData(String.class, "label", "parameter.component.title")
			),
			new MethodData(int.class, "getWidth", "method.component.getwidth"),
			new MethodData(int.class, "getHeight", "method.component.getheight"),
			new MethodData(float.class, "getScale", "method.component.getscale"),
			new MethodData(boolean.class, "isShedow", "method.ilabel.isshedow"),
			new MethodData(void.class, "setShedow", "method.ilabel.setshedow",
				new ParameterData(boolean.class, "showShedow", "parameter.boolean")
			)
		)
	),
	ILayerModel(new InterfaseData(ILayerModel.class, 
			null,
			new Class<?>[] { LayerModel.class },
			"interfase.ilayermodel", 
			new MethodData(float.class, "getOffset", "method.ilayermodel.getoffset",
				new ParameterData(int.class, "axis", "parameter.axis")
			),
			new MethodData(void.class, "setOffset", "method.ilayermodel.setoffset",
				new ParameterData(float.class, "x", "parameter.posx"),
				new ParameterData(float.class, "y", "parameter.posy"),
				new ParameterData(float.class, "z", "parameter.posz")
			),
			new MethodData(String.class, "getOBJModel", "method.ilayermodel.getobjmodel"),
			new MethodData(void.class, "setOBJModel", "method.ilayermodel.setoffset",
				new ParameterData(String.class, "path", "parameter.resource")
			),
			new MethodData(float.class, "getRotate", "method.ilayermodel.getrotate",
				new ParameterData(int.class, "axis", "parameter.axis")
			),
			new MethodData(void.class, "setIsRotate", "method.ilayermodel.setisrotate",
				new ParameterData(boolean.class, "x", "parameter.posx"),
				new ParameterData(boolean.class, "y", "parameter.posy"),
				new ParameterData(boolean.class, "z", "parameter.posz")
			),
			new MethodData(int.class, "getRotateSpeed", "method.ilayermodel.getrotatespeed"),
			new MethodData(void.class, "setRotateSpeed", "method.ilayermodel.setrotatespeed",
				new ParameterData(int.class, "speed", "parameter.speed")
			),
			new MethodData(void.class, "setScale", "method.ilayermodel.setscale",
				new ParameterData(float.class, "x", "parameter.posx"),
				new ParameterData(float.class, "y", "parameter.posy"),
				new ParameterData(float.class, "z", "parameter.posz")
			),
			new MethodData(int.class, "getPos", "method.ilayermodel.getpos"),
			new MethodData(IItemStack.class, "getModel", "method.ilayermodel.getmodel"),
			new MethodData(void.class, "setModel", "method.ilayermodel.getoffset",
				new ParameterData(IItemStack.class, "stack", "parameter.stack")
			),
			new MethodData(float.class, "getScale", "method.ilayermodel.getscale",
				new ParameterData(int.class, "axis", "parameter.axis")
			),
			new MethodData(void.class, "setNbt", "method.ilayermodel.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MethodData(void.class, "setRotate", "method.ilayermodel.setrotate",
				new ParameterData(float.class, "x", "parameter.posx"),
				new ParameterData(float.class, "y", "parameter.posy"),
				new ParameterData(float.class, "z", "parameter.posz")
			),
			new MethodData(INbt.class, "getNbt", "method.ilayermodel.getnbt"),
			new MethodData(boolean.class, "isRotate", "method.ilayermodel.isrotate",
				new ParameterData(int.class, "axis", "parameter.axis")
			)
		)
	),
	ILine(new InterfaseData(ILine.class, 
			null,
			new Class<?>[] { Line.class },
			"interfase.iline", 
			new MethodData(String.class, "getText", "method.iline.gettext"),
			new MethodData(void.class, "setText", "method.iline.settext",
				new ParameterData(String.class, "text", "parameter.iline.text")
			),
			new MethodData(String.class, "getSound", "method.iline.getsound"),
			new MethodData(boolean.class, "getShowText", "method.iline.getshowtext"),
			new MethodData(void.class, "setShowText", "method.iline.setshowtext",
				new ParameterData(boolean.class, "show", "parameter.iline.show")
			),
			new MethodData(void.class, "setSound", "method.iline.setsound",
				new ParameterData(String.class, "sound", "parameter.sound.name")
			)
		)
	),
	IMark(new InterfaseData(IMark.class, 
			null,
			null,
			"interfase.imark", 
			new MethodData(void.class, "update", "method.imark.update"),
			new MethodData(int.class, "getType", "method.imark.gettype"),
			new MethodData(void.class, "setColor", "method.imark.setcolor",
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MethodData(int.class, "getColor", "method.imark.getcolor"),
			new MethodData(void.class, "setType", "method.imark.settype",
				new ParameterData(int.class, "type", "parameter.imark.type")
			),
			new MethodData(IAvailability.class, "getAvailability", "method.getavailability"),
			new MethodData(void.class, "setRotate", "method.imark.setrotate",
				new ParameterData(boolean.class, "rotate", "parameter.imark.rotate")
			),
			new MethodData(boolean.class, "isRotate", "method.imark.isrotate")
		)
	),
	IMetods(new InterfaseData(IMetods.class, 
			null,
			new Class<?>[] { AdditionalMethods.class },
			"interfase.imetods", 
			new MethodData(double[].class, "getPosition", "method.imetods.getposition",
				new ParameterData(double.class, "cx", "parameter.posx"),
				new ParameterData(double.class, "cy", "parameter.posy"),
				new ParameterData(double.class, "cz", "parameter.posz"),
				new ParameterData(double.class, "yaw", "parameter.yaw"),
				new ParameterData(double.class, "pitch", "parameter.pitch"),
				new ParameterData(double.class, "radius", "parameter.range")
			),
			new MethodData(double[].class, "getPosition", "method.imetods.getposition",
				new ParameterData(IEntity.class, "entity", "parameter.entity"),
				new ParameterData(double.class, "yaw", "parameter.yaw"),
				new ParameterData(double.class, "pitch", "parameter.pitch"),
				new ParameterData(double.class, "radius", "parameter.range")
			),
			new MethodData(double[].class, "getAngles3D", "method.imetods.getangles3d",
				new ParameterData(double.class, "dx", "parameter.posx"),
				new ParameterData(double.class, "dy", "parameter.posy"),
				new ParameterData(double.class, "dz", "parameter.posz"),
				new ParameterData(double.class, "mx", "parameter.posx"),
				new ParameterData(double.class, "my", "parameter.posy"),
				new ParameterData(double.class, "mz", "parameter.posz")
			),
			new MethodData(double[].class, "getAngles3D", "method.imetods.getangles3d",
				new ParameterData(IEntity.class, "entity", "parameter.entity"),
				new ParameterData(IEntity.class, "target", "parameter.entity")
			),
			new MethodData(double[].class, "getVector3D", "method.imetods.getvector3d",
				new ParameterData(IEntity.class, "entity", "parameter.entity"),
				new ParameterData(IPos.class, "target", "parameter.pos")
			),
			new MethodData(double[].class, "getVector3D", "method.imetods.getvector3d",
				new ParameterData(double.class, "dx", "parameter.posx"),
				new ParameterData(double.class, "dy", "parameter.posy"),
				new ParameterData(double.class, "dz", "parameter.posz"),
				new ParameterData(double.class, "mx", "parameter.posx"),
				new ParameterData(double.class, "my", "parameter.posy"),
				new ParameterData(double.class, "mz", "parameter.posz")
			),
			new MethodData(double[].class, "getVector3D", "method.imetods.getvector3d",
				new ParameterData(IEntity.class, "entity", "parameter.entity"),
				new ParameterData(IEntity.class, "target", "parameter.entity")
			),
			new MethodData(IEntity.class, "transferEntity", "method.imetods.transferentity",
				new ParameterData(IEntity.class, "entity", "parameter.entity"),
				new ParameterData(int.class, "dimension", "parameter.dimension.id"),
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MethodData(NBTBase.class, "writeObjectToNbt", "method.imetods.writeobjecttonbt",
				new ParameterData(Object.class, "value", "parameter.value")
			),
			new MethodData(double.class, "distanceTo", "method.imetods.distanceto",
				new ParameterData(IEntity.class, "entity", "parameter.entity"),
				new ParameterData(IEntity.class, "target", "parameter.entity")
			),
			new MethodData(double.class, "distanceTo", "method.imetods.distanceto",
				new ParameterData(double.class, "x0", "parameter.posx"),
				new ParameterData(double.class, "y0", "parameter.posy"),
				new ParameterData(double.class, "z0", "parameter.posz"),
				new ParameterData(double.class, "x1", "parameter.posx"),
				new ParameterData(double.class, "y1", "parameter.posy"),
				new ParameterData(double.class, "z1", "parameter.posz")
			),
			new MethodData(String.class, "deleteColor", "method.imetods.deletecolor",
				new ParameterData(String.class, "str", "parameter.color")
			),
			new MethodData(String.class, "getJSONStringFromObject", "method.imetods.getjsonstringfromobject",
				new ParameterData(Object.class, "obj", "parameter.imetods.obj")
			),
			new MethodData(Object.class, "readObjectFromNbt", "method.imetods.readobjectfromnbt",
				new ParameterData(NBTBase.class, "tag", "parameter.nbt")
			)
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
			new MethodData(String.class, "getSound", "method.inpcadvanced.getsound",
				new ParameterData(int.class, "type", "parameter.inpcadvanced.type")
			),
			new MethodData(void.class, "setLine", "method.inpcadvanced.setline",
				new ParameterData(int.class, "type", "parameter.inpcadvanced.type"),
				new ParameterData(int.class, "slot", "parameter.inpcadvanced.slot"),
				new ParameterData(String.class, "text", "parameter.message"),
				new ParameterData(String.class, "sound", "parameter.sound.name")
			),
			new MethodData(int.class, "getLineCount", "method.inpcadvanced.getlinecount",
				new ParameterData(int.class, "type", "parameter.inpcadvanced.type")
			),
			new MethodData(void.class, "setSound", "method.inpcadvanced.setsound",
				new ParameterData(int.class, "type", "parameter.inpcadvanced.type"),
				new ParameterData(String.class, "sound", "parameter.sound.name")
			),
			new MethodData(String.class, "getLine", "method.inpcadvanced.getline",
				new ParameterData(int.class, "type", "parameter.inpcadvanced.type"),
				new ParameterData(int.class, "slot", "parameter.inpcadvanced.slot")
			)
		)
	),
	INPCAi(new InterfaseData(INPCAi.class, 
			null,
			new Class<?>[] { DataAI.class },
			"interfase.inpcai", 
			new MethodData(int.class, "getAnimation", "method.inpcai.getanimation"),
			new MethodData(boolean.class, "getAttackInvisible", "method.inpcai.getattackinvisible"),
			new MethodData(int.class, "getCurrentAnimation", "method.inpcai.getcurrentAnimation"),
			new MethodData(boolean.class, "getInteractWithNPCs", "method.inpcai.getinteractwithnpcs"),
			new MethodData(boolean.class, "getMovingPathPauses", "method.inpcai.getmovingpathpauses"),
			new MethodData(boolean.class, "getStopOnInteract", "method.inpcai.getstoponinteract"),
			new MethodData(int.class, "getWanderingRange", "method.inpcai.getwanderingrange"),
			new MethodData(void.class, "setAttackInvisible", "method.inpcai.setattackinvisible",
				new ParameterData(boolean.class, "attack", "parameter.enabled")
			),
			new MethodData(void.class, "setInteractWithNPCs", "method.inpcai.setinteractwithnpcs",
				new ParameterData(boolean.class, "interact", "parameter.boolean")
			),
			new MethodData(void.class, "setMovingPathType", "method.inpcai.setmovingpathtype",
				new ParameterData(int.class, "type", "parameter.inpcai.movingpathtype"),
				new ParameterData(boolean.class, "pauses", "parameter.inpcai.pauses")
			),
			new MethodData(void.class, "setNavigationType", "method.inpcai.setnavigationtype",
				new ParameterData(int.class, "type", "parameter.inpcai.waytype")
			),
			new MethodData(void.class, "setStopOnInteract", "method.inpcai.setstoponinteract",
				new ParameterData(boolean.class, "stopOnInteract", "parameter.boolean")
			),
			new MethodData(void.class, "setWanderingRange", "method.inpcai.setwanderingrange",
				new ParameterData(int.class, "range", "parameter.inpcai.wanderrange")
			),
			new MethodData(boolean.class, "getAttackLOS", "method.inpcai.getattacklos"),
			new MethodData(boolean.class, "getAvoidsWater", "method.inpcai.getavoidswater"),
			new MethodData(int.class, "getDoorInteract", "method.inpcai.getdoorinteract"),
			new MethodData(boolean.class, "getLeapAtTarget", "method.inpcai.getleapattarget"),
			new MethodData(int.class, "getRetaliateType", "method.inpcai.getretaliatetype"),
			new MethodData(boolean.class, "getReturnsHome", "method.inpcai.getreturnshome"),
			new MethodData(int.class, "getSheltersFrom", "method.inpcai.getsheltersfrom"),
			new MethodData(int.class, "getTacticalType", "method.inpcai.gettacticaltype"),
			new MethodData(void.class, "setAttackLOS", "method.inpcai.setattacklos",
				new ParameterData(boolean.class, "enabled", "parameter.enabled")
			),
			new MethodData(void.class, "setDoorInteract", "method.inpcai.setdoortnteract",
				new ParameterData(int.class, "type", "parameter.inpcai.doortype")
			),
			new MethodData(void.class, "setLeapAtTarget", "method.inpcai.setleapattarget",
				new ParameterData(boolean.class, "leap", "parameter.boolean")
			),
			new MethodData(void.class, "setRetaliateType", "method.inpcai.setretaliatetype",
				new ParameterData(int.class, "type", "parameter.inpcai.retaltype")
			),
			new MethodData(void.class, "setReturnsHome", "method.inpcai.setreturnshome",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MethodData(void.class, "setSheltersFrom", "method.inpcai.setsheltersfrom",
				new ParameterData(int.class, "type", "parameter.inpcai.sheltype")
			),
			new MethodData(void.class, "setTacticalType", "method.inpcai.settacticaltype",
				new ParameterData(int.class, "type", "parameter.inpcai.tacttype")
			),
			new MethodData(int.class, "getMovingPathType", "method.inpcai.getmovingpathtype"),
			new MethodData(int.class, "getNavigationType", "method.inpcai.getnavigationtype"),
			new MethodData(int.class, "getStandingType", "method.inpcai.getstandingtype"),
			new MethodData(int.class, "getWalkingSpeed", "method.inpcai.getwalkingspeed"),
			new MethodData(int.class, "getMovingType", "method.inpcai.getmovingtype"),
			new MethodData(void.class, "setWalkingSpeed", "method.inpcai.setwalkingspeed",
				new ParameterData(int.class, "speed", "parameter.speed")
			),
			new MethodData(void.class, "setStandingType", "method.inpcai.setstandingtype",
				new ParameterData(int.class, "type", "parameter.inpcai.standingtype")
			),
			new MethodData(void.class, "setMovingType", "method.inpcai.setmovingtype",
				new ParameterData(int.class, "type", "parameter.inpcai.movingtype")
			),
			new MethodData(void.class, "setAnimation", "method.inpcai.setanimation",
				new ParameterData(int.class, "type", "parameter.inpcai.animationtype")
			),
			new MethodData(void.class, "setCanSwim", "method.inpcai.setcanswim",
				new ParameterData(boolean.class, "canSwim", "parameter.enabled")
			),
			new MethodData(boolean.class, "getCanSwim", "method.inpcai.getcanswim"),
			new MethodData(void.class, "setAvoidsWater", "method.inpcai.setavoidswater",
				new ParameterData(boolean.class, "enabled", "parameter.enabled")
			),
			new MethodData(int.class, "getTacticalRange", "method.inpcai.gettacticalrange"),
			new MethodData(void.class, "setTacticalRange", "method.inpcai.settacticalrange",
				new ParameterData(int.class, "range", "parameter.inpcai.tactrange")
			)
		)
	),
	INPCAnimation(new InterfaseData(INPCAnimation.class, 
			null,
			new Class<?>[] { DataAnimation.class },
			"interfase.inpcanimation", 
			new MethodData(void.class, "update", "method.inpcanimation.update"),
			new MethodData(void.class, "clear", "method.inpcanimation.clear"),
			new MethodData(void.class, "reset", "method.inpcanimation.reset"),
			new MethodData(IAnimation[].class, "getAnimations", "method.inpcanimation.getanimations",
				new ParameterData(int.class, "animationType", "parameter.animation.type")
			),
			new MethodData(IAnimation.class, "getAnimation", "method.inpcanimation.getanimation",
				new ParameterData(int.class, "animationType", "parameter.animation.type"),
				new ParameterData(int.class, "variant", "parameter.animation.variant")
			),
			new MethodData(boolean.class, "removeAnimation", "method.inpcanimation.removeanimation",
				new ParameterData(int.class, "type", "parameter.animation.type"),
				new ParameterData(String.class, "name", "parameter.animation.name")
			),
			new MethodData(void.class, "startAnimationFromSaved", "method.inpcanimation.startanimationfromsaved",
				new ParameterData(String.class, "animationId", "parameter.animation.name")
			),
			new MethodData(void.class, "startAnimationFromSaved", "method.inpcanimation.startanimationfromsaved",
				new ParameterData(int.class, "animationId", "parameter.animation.id")
			),
			new MethodData(void.class, "stopEmotion", "method.inpcanimation.stopemotion"),
			new MethodData(void.class, "removeAnimations", "method.inpcanimation.removeanimations",
				new ParameterData(int.class, "type", "parameter.animation.type")
			),
			new MethodData(AnimationConfig.class, "createAnimation", "method.inpcanimation.createanimation",
				new ParameterData(int.class, "animationType", "parameter.animation.type")
			),
			new MethodData(void.class, "stopAnimation", "method.inpcanimation.stopanimation"),
			new MethodData(void.class, "startAnimation", "method.inpcanimation.startanimation",
				new ParameterData(int.class, "animationType", "parameter.animation.type"),
				new ParameterData(int.class, "variant", "parameter.animation.variant")
			),
			new MethodData(void.class, "startAnimation", "method.inpcanimation.startanimation",
				new ParameterData(int.class, "animationType", "parameter.animation.type")
			),
			new MethodData(void.class, "setNbt", "method.inpcanimation.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MethodData(INbt.class, "getNbt", "method.inpcanimation.getnbt")
		)
	),
	INPCDisplay(new InterfaseData(INPCDisplay.class, 
			null,
			new Class<?>[] { DataDisplay.class },
			"interfase.inpcdisplay", 
			new MethodData(String.class, "getName", "method.inpcdisplay.getname"),
			new MethodData(void.class, "setName", "method.inpcdisplay.setname",
				new ParameterData(String.class, "name", "parameter.entity.name")
			),
			new MethodData(int.class, "getSize", "method.inpcdisplay.getsize"),
			new MethodData(void.class, "setSize", "method.inpcdisplay.setsize",
				new ParameterData(int.class, "size", "parameter.size")
			),
			new MethodData(float[].class, "getModelScale", "method.inpcdisplay.getmodelscale",
				new ParameterData(int.class, "part", "parameter.body.part.0")
			),
			new MethodData(void.class, "setModelScale", "method.inpcdisplay.setmodelscale",
				new ParameterData(int.class, "part", "parameter.body.part.0"),
				new ParameterData(float.class, "x", "parameter.posx"),
				new ParameterData(float.class, "y", "parameter.posy"),
				new ParameterData(float.class, "z", "parameter.posz")
			),
			new MethodData(void.class, "setTitle", "method.inpcdisplay.settitle",
				new ParameterData(String.class, "title", "parameter.inpcdisplay.title")
			),
			new MethodData(String.class, "getTitle", "method.inpcdisplay.gettitle"),
			new MethodData(String.class, "getModel", "method.inpcdisplay.getmodel"),
			new MethodData(void.class, "setModel", "method.inpcdisplay.setmodel",
				new ParameterData(String.class, "model", "parameter.inpcdisplay.model")
			),
			new MethodData(boolean.class, "getHasLivingAnimation", "method.inpcdisplay.gethaslivinganimation"),
			new MethodData(String.class, "getOverlayTexture", "method.inpcdisplay.getoverlaytexture"),
			new MethodData(void.class, "setHasLivingAnimation", "method.inpcdisplay.sethaslivinganimation",
				new ParameterData(boolean.class, "enabled", "parameter.boolean")
			),
			new MethodData(void.class, "setOverlayTexture", "method.inpcdisplay.setoverlaytexture",
				new ParameterData(String.class, "texture", "parameter.inpcdisplay.overtexture")
			),
			new MethodData(boolean.class, "getHasHitbox", "method.inpcdisplay.gethashitbox"),
			new MethodData(int.class, "getVisible", "method.inpcdisplay.getvisible"),
			new MethodData(int.class, "getBossbar", "method.inpcdisplay.getbossbar"),
			new MethodData(boolean.class, "isVisibleTo", "method.inpcdisplay.isvisibleto",
				new ParameterData(IPlayer.class, "player", "parameter.inpcdisplay.player")
			),
			new MethodData(void.class, "setVisible", "method.inpcdisplay.setvisible",
				new ParameterData(int.class, "type", "parameter.inpcdisplay.vistype")
			),
			new MethodData(int.class, "getShowName", "method.inpcdisplay.getshowname"),
			new MethodData(String.class, "getSkinPlayer", "method.inpcdisplay.getskinplayer"),
			new MethodData(String.class, "getCapeTexture", "method.inpcdisplay.getcapetexture"),
			new MethodData(int.class, "getBossColor", "method.inpcdisplay.getbosscolor"),
			new MethodData(void.class, "setShowName", "method.inpcdisplay.setshowname",
				new ParameterData(int.class, "type", "parameter.inpcdisplay.showtype")
			),
			new MethodData(void.class, "setSkinUrl", "method.inpcdisplay.setskinurl",
				new ParameterData(String.class, "url", "parameter.inpcdisplay.urltexture")
			),
			new MethodData(void.class, "setSkinPlayer", "method.inpcdisplay.setskinplayer",
				new ParameterData(String.class, "name", "parameter.player.name")
			),
			new MethodData(void.class, "setBossbar", "method.inpcdisplay.setbossbar",
				new ParameterData(int.class, "type", "parameter.inpcdisplay.bartype")
			),
			new MethodData(void.class, "setBossColor", "method.inpcdisplay.setbosscolor",
				new ParameterData(int.class, "color", "parameter.inpcdisplay.bosscolor")
			),
			new MethodData(void.class, "setHasHitbox", "method.inpcdisplay.sethashitbox",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MethodData(void.class, "setSkinTexture", "method.inpcdisplay.setskintexture",
				new ParameterData(String.class, "texture", "parameter.inpcdisplay.skintexture")
			),
			new MethodData(void.class, "setTint", "method.inpcdisplay.settint",
				new ParameterData(int.class, "color", "parameter.inpcdisplay.tintcolor")
			),
			new MethodData(void.class, "setCapeTexture", "method.inpcdisplay.setcapetexture",
				new ParameterData(String.class, "texture", "parameter.inpcdisplay.capetexture")
			),
			new MethodData(String.class, "getSkinTexture", "method.inpcdisplay.getskintexture"),
			new MethodData(int.class, "getTint", "method.inpcdisplay.gettint"),
			new MethodData(String.class, "getSkinUrl", "method.inpcdisplay.getskinurl")
		)
	),
	INPCInventory(new InterfaseData(INPCInventory.class, 
			null,
			new Class<?>[] { DataInventory.class },
			"interfase.inpcinventory", 
			new MethodData(ICustomDrop.class, "getDrop", "method.inpcinv.getdrop",
				new ParameterData(int.class, "slot", "parameter.inpcinv.dropslot")
			),
			new MethodData(boolean.class, "getXPLootMode", "method.inpcinv.getxplootmode"),
			new MethodData(void.class, "setLeftHand", "method.inpcinv.setlefthand",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(void.class, "setXPLootMode", "method.inpcinv.setxplootmode",
				new ParameterData(boolean.class, "mode", "parameter.boolean")
			),
			new MethodData(boolean.class, "removeDrop", "method.inpcinv.removedrop",
				new ParameterData(ICustomDrop.class, "drop", "parameter.inpcinv.drop")
			),
			new MethodData(boolean.class, "removeDrop", "method.inpcinv.removedrop",
				new ParameterData(int.class, "drop", "parameter.inpcinv.drop")
			),
			new MethodData(ICustomDrop.class, "addDropItem", "method.inpcinv.adddropitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack"),
				new ParameterData(double.class, "chance", "parameter.chance")
			),
			new MethodData(IItemStack.class, "getProjectile", "method.inpcinv.getprojectile"),
			new MethodData(IItemStack.class, "getRightHand", "method.inpcinv.getrighthand"),
			new MethodData(IItemStack.class, "getLeftHand", "method.inpcinv.getlefthand"),
			new MethodData(IItemStack.class, "getArmor", "method.inpcinv.getarmor",
				new ParameterData(int.class, "slot", "parameter.armor.slot")
			),
			new MethodData(IItemStack[].class, "getItemsRNG", "method.inpcinv.getitemsrnd",
				new ParameterData(EntityLivingBase.class, "attacking", "parameter.inpcinv.attacking")
			),
			new MethodData(IItemStack[].class, "getItemsRNGL", "method.inpcinv.getitemsrndl",
				new ParameterData(EntityLivingBase.class, "attacking", "parameter.inpcinv.attacking")
			),
			new MethodData(int.class, "getExpRNG", "method.inpcinv.getexprnd"),
			new MethodData(void.class, "setArmor", "method.inpcinv.setarmor",
				new ParameterData(int.class, "slot", "parameter.inpcinv.armslot"),
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(void.class, "setRightHand", "method.inpcinv.setrighthand",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(ICustomDrop[].class, "getDrops", "method.inpcinv.getdrops"),
			new MethodData(int.class, "getExpMin", "method.inpcinv.getexpmin"),
			new MethodData(int.class, "getExpMax", "method.inpcinv.getexpmax"),
			new MethodData(void.class, "setExp", "method.inpcinv.setexp",
				new ParameterData(int.class, "min", "parameter.min"),
				new ParameterData(int.class, "max", "parameter.max")
			),
			new MethodData(void.class, "setProjectile", "method.inpcinv.setprojectile",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			)
		)
	),
	INPCJob(new InterfaseData(INPCJob.class, 
			null,
			new Class<?>[] { JobInterface.class },
			"interfase.inpcjob", 
			new MethodData(int.class, "getType", "method.inpcjob.gettype")
		)
	),
	INPCMelee(new InterfaseData(INPCMelee.class, 
			null,
			new Class<?>[] { DataMelee.class },
			"interfase.inpcmelee", 
			new MethodData(void.class, "setDelay", "method.inpcmelee.setdelay",
				new ParameterData(int.class, "speed", "parameter.speed")
			),
			new MethodData(void.class, "setEffect", "method.inpcmelee.seteffect",
				new ParameterData(int.class, "type", "parameter.effect.type"),
				new ParameterData(int.class, "strength", "parameter.effect.strength"),
				new ParameterData(int.class, "time", "parameter.effect.duration")
			),
			new MethodData(void.class, "setKnockback", "method.inpcmelee.setknockback",
				new ParameterData(int.class, "knockback", "parameter.ientity.power")
			),
			new MethodData(int.class, "getKnockback", "method.inpcmelee.getknockback"),
			new MethodData(int.class, "getEffectType", "method.inpcmelee.geteffecttype"),
			new MethodData(int.class, "getEffectTime", "method.inpcmelee.geteffecttime"),
			new MethodData(void.class, "setRange", "method.inpcmelee.setrange",
				new ParameterData(int.class, "range", "parameter.range")
			),
			new MethodData(int.class, "getRange", "method.inpcmelee.getrange"),
			new MethodData(int.class, "getStrength", "method.inpcmelee.getstrength"),
			new MethodData(int.class, "getDelay", "method.inpcmelee.getdelay"),
			new MethodData(void.class, "setStrength", "method.inpcmelee.setstrength",
				new ParameterData(int.class, "strength", "parameter.count")
			),
			new MethodData(int.class, "getEffectStrength", "method.inpcmelee.geteffectstrength")
		)
	),
	INPCRanged(new InterfaseData(INPCRanged.class, 
			null,
			new Class<?>[] { DataRanged.class },
			"interfase.inpcranged", 
			new MethodData(int.class, "getSize", "method.inpcranged.getsize"),
			new MethodData(void.class, "setSize", "method.inpcranged.setsize",
				new ParameterData(int.class, "size", "parameter.size")
			),
			new MethodData(void.class, "setDelay", "method.inpcranged.setdelay",
				new ParameterData(int.class, "min", "parameter.min"),
				new ParameterData(int.class, "max", "parameter.max")
			),
			new MethodData(void.class, "setHasAimAnimation", "method.inpcranged.sethasaimanimation",
				new ParameterData(boolean.class, "aim", "parameter.boolean")
			),
			new MethodData(String.class, "getSound", "method.inpcranged.getsound",
				new ParameterData(int.class, "type", "parameter.sound.type")
			),
			new MethodData(void.class, "setEffect", "method.inpcranged.seteffect",
				new ParameterData(int.class, "type", "parameter.effect.type"),
				new ParameterData(int.class, "strength", "parameter.effect.strength"),
				new ParameterData(int.class, "time", "parameter.ticks")
			),
			new MethodData(void.class, "setKnockback", "method.inpcranged.setknockback",
				new ParameterData(int.class, "punch", "parameter.knockback")
			),
			new MethodData(int.class, "getDelayMax", "method.inpcranged.getdelaymax"),
			new MethodData(void.class, "setAccelerate", "method.inpcranged.setaccelerate",
				new ParameterData(boolean.class, "accelerate", "parameter.boolean")
			),
			new MethodData(void.class, "setBurst", "method.inpcranged.setburst",
				new ParameterData(int.class, "count", "parameter.range")
			),
			new MethodData(void.class, "setBurstDelay", "method.inpcranged.setburstdelay",
				new ParameterData(int.class, "delay", "parameter.ticks")
			),
			new MethodData(void.class, "setExplodeSize", "method.inpcranged.setexplodesize",
				new ParameterData(int.class, "size", "parameter.explode.size")
			),
			new MethodData(void.class, "setFireType", "method.inpcranged.setfiretype",
				new ParameterData(int.class, "type", "parameter.inpcranged.firetype")
			),
			new MethodData(void.class, "setGlows", "method.inpcranged.setglows",
				new ParameterData(boolean.class, "glows", "parameter.boolean")
			),
			new MethodData(void.class, "setMeleeRange", "method.inpcranged.setmeleemange",
				new ParameterData(int.class, "range", "parameter.range")
			),
			new MethodData(void.class, "setRender3D", "method.inpcranged.setrender3d",
				new ParameterData(boolean.class, "render3d", "parameter.boolean")
			),
			new MethodData(void.class, "setShotCount", "method.inpcranged.setshotcount",
				new ParameterData(int.class, "count", "parameter.count")
			),
			new MethodData(void.class, "setSpins", "method.inpcranged.setspins",
				new ParameterData(boolean.class, "spins", "parameter.boolean")
			),
			new MethodData(void.class, "setSticks", "method.inpcranged.setsticks",
				new ParameterData(boolean.class, "sticks", "parameter.boolean")
			),
			new MethodData(int.class, "getKnockback", "method.inpcranged.getknockback"),
			new MethodData(int.class, "getEffectType", "method.inpcranged.geteffecttype"),
			new MethodData(int.class, "getEffectTime", "method.inpcranged.geteffecttime"),
			new MethodData(int.class, "getAccuracy", "method.inpcranged.getaccuracy"),
			new MethodData(int.class, "getShotCount", "method.inpcranged.getshotcount"),
			new MethodData(void.class, "setRange", "method.inpcranged.setrange",
				new ParameterData(int.class, "range", "parameter.range")
			),
			new MethodData(int.class, "getRange", "method.inpcranged.getrange"),
			new MethodData(int.class, "getStrength", "method.inpcranged.getstrength"),
			new MethodData(int.class, "getDelayMin", "method.inpcranged.getdelaymin"),
			new MethodData(int.class, "getBurstDelay", "method.inpcranged.getburstdelay"),
			new MethodData(int.class, "getMeleeRange", "method.inpcranged.getmeleerange"),
			new MethodData(int.class, "getFireType", "method.inpcranged.getfiretype"),
			new MethodData(int.class, "getBurst", "method.inpcranged.getburst"),
			new MethodData(int.class, "getDelayRNG", "method.inpcranged.getdelayrng"),
			new MethodData(boolean.class, "getAccelerate", "method.inpcranged.getaccelerate"),
			new MethodData(int.class, "getExplodeSize", "method.inpcranged.getexplodesize"),
			new MethodData(boolean.class, "getGlows", "method.inpcranged.getglows"),
			new MethodData(boolean.class, "getHasGravity", "method.inpcranged.gethasgravity"),
			new MethodData(void.class, "setHasGravity", "method.inpcranged.sethasgravity",
				new ParameterData(boolean.class, "hasGravity", "parameter.boolean")
			),
			new MethodData(boolean.class, "getRender3D", "method.inpcranged.getrender3d"),
			new MethodData(boolean.class, "getSpins", "method.inpcranged.getspins"),
			new MethodData(boolean.class, "getSticks", "method.inpcranged.getsticks"),
			new MethodData(void.class, "setStrength", "method.inpcranged.setstrength",
				new ParameterData(int.class, "strength", "parameter.inpcranged.strength")
			),
			new MethodData(void.class, "setSound", "method.inpcranged.setsound",
				new ParameterData(int.class, "type", "parameter.sound.type"),
				new ParameterData(String.class, "sound", "parameter.sound.name")
			),
			new MethodData(int.class, "getParticle", "method.inpcranged.getparticle"),
			new MethodData(void.class, "setParticle", "method.inpcranged.setparticle",
				new ParameterData(int.class, "type", "parameter.particle.type")
			),
			new MethodData(void.class, "setSpeed", "method.inpcranged.setspeed",
				new ParameterData(int.class, "speed", "parameter.speed")
			),
			new MethodData(int.class, "getSpeed", "method.inpcranged.getspeed"),
			new MethodData(void.class, "setAccuracy", "method.inpcranged.setaccuracy",
				new ParameterData(int.class, "accuracy", "parameter.inpcranged.accuracy")
			),
			new MethodData(boolean.class, "getHasAimAnimation", "method.inpcranged.gethasaimanimation"),
			new MethodData(int.class, "getEffectStrength", "method.inpcranged.geteffectstrength")
		)
	),
	INPCRole(new InterfaseData(INPCRole.class, 
			null,
			new Class<?>[] { RoleInterface.class },
			"interfase.inpcrole", 
			new MethodData(int.class, "getType", "method.inpcrole.gettype")
		)
	),
	INPCStats(new InterfaseData(INPCStats.class, 
			null,
			new Class<?>[] { DataStats.class },
			"interfase.inpcstats", 
			new MethodData(int.class, "getMaxHealth", "method.inpcstats.getmaxhealth"),
			new MethodData(int.class, "getLevel", "method.inpcstats.getlevel"),
			new MethodData(void.class, "setLevel", "method.inpcstats.setlevel",
				new ParameterData(int.class, "level", "type.level")
			),
			new MethodData(int.class, "getRarity", "method.inpcstats.getrarity"),
			new MethodData(int.class, "getAggroRange", "method.inpcstats.getaggrorange"),
			new MethodData(int.class, "getCombatRegen", "method.inpcstats.getcombatregen"),
			new MethodData(int.class, "getCreatureType", "method.inpcstats.getcreaturetype"),
			new MethodData(int.class, "getHealthRegen", "method.inpcstats.gethealthregen"),
			new MethodData(boolean.class, "getHideDeadBody", "method.inpcstats.gethidedeadbody"),
			new MethodData(boolean.class, "getImmune", "method.inpcstats.getimmune",
				new ParameterData(int.class, "type", "parameter.inpcstats.immune.type")
			),
			new MethodData(INPCMelee.class, "getMelee", "method.inpcstats.getmelee"),
			new MethodData(INPCRanged.class, "getRanged", "method.inpcstats.getranged"),
			new MethodData(int.class, "getRespawnTime", "method.inpcstats.getrespawntime"),
			new MethodData(int.class, "getRespawnType", "method.inpcstats.getrespawntype"),
			new MethodData(boolean.class, "isCalmdown", "method.inpcstats.isCalmdown"),
			new MethodData(void.class, "setAggroRange", "method.inpcstats.setaggrorange",
				new ParameterData(int.class, "range", "parameter.inpcstats.aggrorange")
			),
			new MethodData(void.class, "setCalmdown", "method.inpcstats.setcalmdown",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MethodData(void.class, "setCombatRegen", "method.inpcstats.setcombatregen",
				new ParameterData(int.class, "regen", "parameter.inpcstats.combatregen")
			),
			new MethodData(void.class, "setCreatureType", "method.inpcstats.setcreaturetype",
				new ParameterData(int.class, "type", "parameter.inpcstats.creaturetype")
			),
			new MethodData(void.class, "setHideDeadBody", "method.inpcstats.sethidedeadbody",
				new ParameterData(boolean.class, "hide", "parameter.boolean")
			),
			new MethodData(void.class, "setImmune", "method.inpcstats.setimmune",
				new ParameterData(int.class, "type", "parameter.inpcstats.immune.type"),
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MethodData(void.class, "setRespawnTime", "method.inpcstats.setrespawntime",
				new ParameterData(int.class, "seconds", "parameter.inpcstats.seconds")
			),
			new MethodData(void.class, "setRespawnType", "method.inpcstats.setrespawntype",
				new ParameterData(int.class, "type", "parameter.inpcstats.respawn.type")
			),
			new MethodData(void.class, "setRarityTitle", "method.inpcstats.setraritytitle",
				new ParameterData(String.class, "rarity", "parameter.inpcstats.rarity")
			),
			new MethodData(void.class, "setMaxHealth", "method.inpcstats.setmaxmealth",
				new ParameterData(int.class, "maxHealth", "parameter.health")
			),
			new MethodData(String.class, "getRarityTitle", "method.inpcstats.getraritytitle"),
			new MethodData(void.class, "setRarity", "method.inpcstats.setrarity",
				new ParameterData(int.class, "rarity", "stats.rarity")
			),
			new MethodData(float.class, "getResistance", "method.inpcstats.getresistance",
				new ParameterData(int.class, "type", "parameter.inpcstats.resistancetype")
			),
			new MethodData(void.class, "setResistance", "method.inpcstats.setresistance",
				new ParameterData(int.class, "type", "parameter.inpcstats.resistancetype"),
				new ParameterData(float.class, "value", "parameter.inpcstats.resistancevalue")
			),
			new MethodData(void.class, "setHealthRegen", "method.inpcstats.sethealthregen",
				new ParameterData(int.class, "regen", "parameter.inpcstats.healthregen")
			)
		)
	),
	INbt(new InterfaseData(INbt.class, 
			null,
			new Class<?>[] { NBTWrapper.class },
			"interfase.inbt", 
			new MethodData(void.class, "remove", "method.inbt.remove",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt")
			),
			new MethodData(boolean.class, "getBoolean", "method.inbt.getboolean",
				new ParameterData(String.class, "key", "parameter.inbt.key.boolean")
			),
			new MethodData(byte.class, "getByte", "method.inbt.getbyte",
				new ParameterData(String.class, "key", "parameter.inbt.key.byte")
			),
			new MethodData(short.class, "getShort", "method.inbt.getshort",
				new ParameterData(String.class, "key", "parameter.inbt.key.short")
			),
			new MethodData(long.class, "getLong", "method.inbt.getlong",
				new ParameterData(String.class, "key", "parameter.inbt.key.long")
			),
			new MethodData(float.class, "getFloat", "method.inbt.getfloat",
				new ParameterData(String.class, "key", "parameter.inbt.key.float")
			),
			new MethodData(double.class, "getDouble", "method.inbt.getdouble",
				new ParameterData(String.class, "key", "parameter.inbt.key.double")
			),
			new MethodData(void.class, "clear", "method.inbt.clear"),
			new MethodData(void.class, "merge", "method.inbt.merge",
				new ParameterData(INbt.class, "nbt", "parameter.inbt.key.nbt")
			),
			new MethodData(int.class, "getType", "method.inbt.gettype",
				new ParameterData(String.class, "key", "parameter.inbt.key.string")
			),
			new MethodData(void.class, "setBoolean", "method.inbt.setboolean",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(boolean.class, "value", "parameter.inbt.key.boolean")
			),
			new MethodData(void.class, "setByte", "method.inbt.setbyte",
				new ParameterData(String.class, "key", "parameter.inbt.key"),
				new ParameterData(byte.class, "value", "parameter.inbt.key.bytearr")
			),
			new MethodData(void.class, "setDouble", "method.inbt.setdouble",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(double.class, "value", "parameter.inbt.key.double")
			),
			new MethodData(void.class, "setFloat", "method.inbt.setfloat",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(float.class, "value", "parameter.inbt.key.float")
			),
			new MethodData(void.class, "setLong", "method.inbt.setlong",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(long.class, "value", "parameter.inbt.key.long")
			),
			new MethodData(void.class, "setShort", "method.inbt.setshort",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(short.class, "value", "parameter.inbt.key.short")
			),
			new MethodData(int.class, "getInteger", "method.inbt.getinteger",
				new ParameterData(String.class, "key", "parameter.inbt.key.int")
			),
			new MethodData(String[].class, "getKeys", "method.inbt.getkeys"),
			new MethodData(boolean.class, "has", "method.inbt.has",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt")
			),
			new MethodData(String.class, "getString", "method.inbt.getstring",
				new ParameterData(String.class, "key", "parameter.inbt.key.string")
			),
			new MethodData(void.class, "setInteger", "method.inbt.setinteger",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(int.class, "value", "parameter.inbt.key.int")
			),
			new MethodData(int[].class, "getIntegerArray", "method.inbt.getintarr",
				new ParameterData(String.class, "key", "parameter.inbt.key.intarr")
			),
			new MethodData(int.class, "getListType", "method.inbt.getlisttype",
				new ParameterData(String.class, "key", "parameter.inbt.key")
			),
			new MethodData(void.class, "setCompound", "method.inbt.setcompound",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(INbt.class, "value", "parameter.inbt.key.compound")
			),
			new MethodData(void.class, "setIntegerArray", "method.inbt.setintegerarray",
				new ParameterData(String.class, "key", "parameter.inbt.key"),
				new ParameterData(int[].class, "value", "parameter.inbt.key.intarr")
			),
			new MethodData(String.class, "toJsonString", "method.inbt.tojsonstring"),
			new MethodData(boolean.class, "isEqual", "method.inbt.isequal",
				new ParameterData(INbt.class, "nbt", "parameter.inbt.key.nbt")
			),
			new MethodData(byte[].class, "getByteArray", "method.inbt.getbytearray",
				new ParameterData(String.class, "key", "parameter.inbt.key.bytearr")
			),
			new MethodData(void.class, "setString", "method.inbt.setstring",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(String.class, "value", "parameter.inbt.key.string")
			),
			new MethodData(NBTTagCompound.class, "getMCNBT", "method.inbt.getmcnbt"),
			new MethodData(void.class, "setByteArray", "method.inbt.setbytearray",
				new ParameterData(String.class, "key", "parameter.inbt.key.nbt"),
				new ParameterData(byte[].class, "value", "parameter.inbt.key.bytearr")
			),
			new MethodData(Object[].class, "getList", "method.inbt.getlist",
				new ParameterData(String.class, "key", "parameter.inbt.key.list"),
				new ParameterData(int.class, "type", "parameter.inbt.type.list")
			),
			new MethodData(void.class, "setList", "method.inbt.setlist",
				new ParameterData(String.class, "key", "parameter.inbt.key.list"),
				new ParameterData(Object[].class, "value", "parameter.inbt.list.objarr")
			),
			new MethodData(INbt.class, "getCompound", "method.inbt.getcompound",
				new ParameterData(String.class, "key", "parameter.inbt.key.compound")
			)
		)
	),
	INbtHandler(new InterfaseData(INbtHandler.class, 
			null,
			new Class<?>[] { ItemStackWrapper.class, MarkData.class, WrapperEntityData.class, PlayerData.class },
			"interfase.inbthandler", 
			new MethodData(NBTTagCompound.class, "getCapabilityNBT", "method.inbthandler.getcapabilitynbt"),
			new MethodData(void.class, "setCapabilityNBT", "method.inbthandler.setcapabilitynbt",
				new ParameterData(NBTTagCompound.class, "compound", "parameter.nbt")
			)
		)
	),
	INpcRecipe(new InterfaseData(INpcRecipe.class, 
			null,
			new Class<?>[] { NpcShapelessRecipes.class, NpcShapedRecipes.class },
			"interfase.inpcrecipe", 
			new MethodData(String.class, "getName", "method.inpcrecipe.getname"),
			new MethodData(void.class, "delete", "method.inpcrecipe.delete"),
			new MethodData(int.class, "getId", "method.inpcrecipe.getid"),
			new MethodData(void.class, "copy", "method.inpcrecipe.copy",
				new ParameterData(INpcRecipe.class, "recipe", "parameter.inpcrecipe.copy")
			),
			new MethodData(boolean.class, "isValid", "method.inpcrecipe.isvalid"),
			new MethodData(void.class, "setIsGlobal", "method.inpcrecipe.setisglobal",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MethodData(boolean.class, "isKnown", "method.inpcrecipe.isknown"),
			new MethodData(void.class, "saves", "method.inpcrecipe.saves",
				new ParameterData(boolean.class, "", "parameter.boolean")
			),
			new MethodData(boolean.class, "saves", "method.inpcrecipe.saves"),
			new MethodData(boolean.class, "equal", "method.inpcrecipe.equal",
				new ParameterData(INpcRecipe.class, "recipe", "parameter.recipe")
			),
			new MethodData(int.class, "getWidth", "method.inpcrecipe.getwidth"),
			new MethodData(int.class, "getHeight", "method.inpcrecipe.getheight"),
			new MethodData(IAvailability.class, "getAvailability", "method.inpcrecipe.getavailability"),
			new MethodData(void.class, "setNbt", "method.inpcrecipe.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MethodData(IItemStack[][].class, "getRecipe", "method.inpcrecipe.getrecipe"),
			new MethodData(INbt.class, "getNbt", "method.inpcrecipe.getnbt"),
			new MethodData(void.class, "setKnown", "method.inpcrecipe.setknown",
				new ParameterData(boolean.class, "known", "parameter.boolean")
			),
			new MethodData(boolean.class, "getIgnoreDamage", "method.inpcrecipe.getignoredamage"),
			new MethodData(void.class, "setIgnoreNBT", "method.inpcrecipe.setignorenbt",
				new ParameterData(boolean.class, "bo", "parameter.ignorenbt")
			),
			new MethodData(void.class, "setIgnoreDamage", "method.inpcrecipe.setignoredamage",
				new ParameterData(boolean.class, "bo", "parameter.ignoredamage")
			),
			new MethodData(boolean.class, "getIgnoreNBT", "method.inpcrecipe.getignorenbt"),
			new MethodData(boolean.class, "isGlobal", "method.inpcrecipe.isglobal"),
			new MethodData(String.class, "getNpcGroup", "method.inpcrecipe.getnpcgroup"),
			new MethodData(IItemStack.class, "getProduct", "method.inpcrecipe.getproduct"),
			new MethodData(boolean.class, "isShaped", "method.inpcrecipe.isshaped")
		)
	),
	IOverlayHUD(new InterfaseData(IOverlayHUD.class, 
			null,
			new Class<?>[] { PlayerOverlayHUD.class },
			"interfase.ioverlayhud", 
			new MethodData(void.class, "update", "method.ihud.update"),
			new MethodData(void.class, "clear", "method.ihud.clear"),
			new MethodData(ICustomGuiComponent.class, "getComponent", "method.ihud.getcomponent",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(int.class, "componentID", "parameter.component.id")
			),
			new MethodData(ICustomGuiComponent[].class, "getComponents", "method.ihud.getcomponents",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype")
			),
			new MethodData(ICustomGuiComponent[].class, "getComponents", "method.ihud.getcomponents"),
			new MethodData(IGuiTimer.class, "addTimer", "method.ihud.addtimer",
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
			new MethodData(IGuiTimer.class, "addTimer", "method.ihud.addtimer",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(long.class, "start", "parameter.ihud.timer.start"),
				new ParameterData(long.class, "end", "parameter.ihud.timer.end"),
				new ParameterData(int.class, "x", "parameter.posu"),
				new ParameterData(int.class, "y", "parameter.posv"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MethodData(ITexturedRect.class, "addTexturedRect", "method.ihud.addtexturedrect",
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
			new MethodData(ITexturedRect.class, "addTexturedRect", "method.ihud.addtexturedrect",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(String.class, "texture", "parameter.resource"),
				new ParameterData(int.class, "x", "parameter.posu"),
				new ParameterData(int.class, "y", "parameter.posv"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MethodData(boolean.class, "removeSlot", "method.ihud.removeslot",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(int.class, "slotID", "parameter.slot")
			),
			new MethodData(IItemSlot.class, "addItemSlot", "method.ihud.additemslot",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(int.class, "x", "parameter.posu"),
				new ParameterData(int.class, "y", "parameter.posv"),
				new ParameterData(IItemStack.class, "stack", "parameter.stack")
			),
			new MethodData(IItemSlot.class, "addItemSlot", "method.ihud.additemslot",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(int.class, "x", "parameter.posu"),
				new ParameterData(int.class, "y", "parameter.posv")
			),
			new MethodData(ICompassData.class, "getCompasData", "method.ioverlayhud.getcompasdata"),
			new MethodData(IItemSlot[].class, "getSlots", "method.ihud.getslots"),
			new MethodData(IItemSlot[].class, "getSlots", "method.ihud.getslots",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype")
			),
			new MethodData(ILabel.class, "addLabel", "method.ihud.addlabel",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(String.class, "label", "parameter.label.text"),
				new ParameterData(int.class, "x", "parameter.posu"),
				new ParameterData(int.class, "y", "parameter.posv"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(int.class, "color", "parameter.color")
			),
			new MethodData(ILabel.class, "addLabel", "method.ihud.addlabel",
				new ParameterData(int.class, "id", "parameter.component.id"),
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(String.class, "label", "parameter.label.text"),
				new ParameterData(int.class, "x", "parameter.posu"),
				new ParameterData(int.class, "y", "parameter.posv"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MethodData(boolean.class, "isShowElementType", "method.ihud.isshowelementtype",
				new ParameterData(int.class, "type", "parameter.ihud.elementtype")
			),
			new MethodData(void.class, "setShowElementType", "method.ihud.setshowelementtype",
				new ParameterData(int.class, "type", "parameter.ihud.elementtype"),
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MethodData(void.class, "setShowElementType", "method.ihud.setshowelementtype",
				new ParameterData(String.class, "type", "parameter.ihud.elementtype"),
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MethodData(boolean.class, "removeComponent", "method.ihud.removecomponent",
				new ParameterData(int.class, "orientationType", "parameter.ihud.ortype"),
				new ParameterData(int.class, "componentID", "parameter.component.id")
			)
		)
	),
	IPixelmon(new InterfaseData(IPixelmon.class, 
			IAnimal.class,
			new Class<?>[] { PixelmonWrapper.class },
			"interfase.ipixelmon", 
			new MethodData(Object.class, "getPokemonData", "method.ipixelmon.getpokemondata")
		)
	),
	IPixelmonPlayerData(new InterfaseData(IPixelmonPlayerData.class, 
			null,
			null,
			"interfase.ipixelmonplayerdata", 
			new MethodData(Object.class, "getPC", "method.ipixelmonplayerdata.getpc"),
			new MethodData(Object.class, "getParty", "method.ipixelmonplayerdata.getparty")
		)
	),
	IPlayer(new InterfaseData(IPlayer.class, 
			IEntityLivingBase.class,
			new Class<?>[] { PlayerWrapper.class },
			"interfase.iplayer", 
			new MethodData(void.class, "trigger", "method.trigger",
				new ParameterData(int.class, "id", "parameter.trigger.id"),
				new ParameterData(Object[].class, "arguments", "parameter.trigger.arguments")
			),
			new MethodData(String.class, "getLanguage", "method.iplayer.getlanguage"),
			new MethodData(void.class, "message", "method.iplayer.message",
				new ParameterData(String.class, "message", "parameter.message")
			),
			new MethodData(String.class, "getDisplayName", "method.iplayer.getdisplayname"),
			new MethodData(void.class, "removeAllItems", "method.iplayer.removeallitems",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(boolean.class, "removeItem", "method.iplayer.removeitem",
				new ParameterData(String.class, "id", "parameter.item.name"),
				new ParameterData(int.class, "damage", "parameter.item.meta"),
				new ParameterData(int.class, "amount", "parameter.count")
			),
			new MethodData(boolean.class, "removeItem", "method.iplayer.removeitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack"),
				new ParameterData(int.class, "amount", "parameter.itemcount")
			),
			new MethodData(IBlock.class, "getSpawnPoint", "method.iplayer.getspawnpoint"),
			new MethodData(void.class, "setSpawnPoint", "method.iplayer.setspawnpoint",
				new ParameterData(IBlock.class, "block", "parameter.block")
			),
			new MethodData(void.class, "playSound", "method.iplayer.playsound",
				new ParameterData(String.class, "sound", "parameter.sound.name"),
				new ParameterData(float.class, "volume", "parameter.sound.volume"),
				new ParameterData(float.class, "pitch", "parameter.sound.pitch")
			),
			new MethodData(void.class, "playSound", "method.iplayer.playsound",
				new ParameterData(int.class, "categoryType", "parameter.sound.cat.type"),
				new ParameterData(IPos.class, "pos", "parameter.pos"),
				new ParameterData(String.class, "sound", "parameter.sound.name"),
				new ParameterData(float.class, "volume", "parameter.sound.volume"),
				new ParameterData(float.class, "pitch", "parameter.sound.pitch")
			),
			new MethodData(void.class, "sendNotification", "method.iplayer.sendnotification",
				new ParameterData(String.class, "title", "parameter.title"),
				new ParameterData(String.class, "message", "parameter.message"),
				new ParameterData(int.class, "type", "parameter.message.type")
			),
			new MethodData(void.class, "sendTo", "method.iplayer.sendto",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MethodData(boolean.class, "hasFinishedQuest", "method.iplayer.hasfinishedquest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MethodData(EntityPlayer.class, "getMCEntity", "method.ientity.getmcentity"),
			new MethodData(EntityLivingBase.class, "getMCEntity", "method.ientity.getmcentity"),
			new MethodData(Entity.class, "getMCEntity", "method.ientity.getmcentity"),
			new MethodData(int.class, "getFactionPoints", "method.iplayer.getfactionpoints",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MethodData(void.class, "addDialog", "method.iplayer.adddialog",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			),
			new MethodData(void.class, "addFactionPoints", "method.iplayer.addfactionpoints",
				new ParameterData(int.class, "faction", "parameter.faction.id"),
				new ParameterData(int.class, "points", "parameter.value")
			),
			new MethodData(void.class, "clearData", "method.iplayer.cleardata"),
			new MethodData(void.class, "closeGui", "method.iplayer.closegui"),
			new MethodData(int.class, "factionStatus", "method.iplayer.factionstatus",
				new ParameterData(int.class, "id", "parameter.faction.id")
			),
			new MethodData(boolean.class, "finishQuest", "method.iplayer.finishquest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MethodData(IQuest[].class, "getActiveQuests", "method.iplayer.getactivequests"),
			new MethodData(ICustomGui.class, "getCustomGui", "method.iplayer.getcustomgui"),
			new MethodData(int.class, "getExpLevel", "method.iplayer.getexplevel"),
			new MethodData(int.class, "getGamemode", "method.iplayer.getgamemode"),
			new MethodData(int.class, "getHunger", "method.iplayer.gethunger"),
			new MethodData(IContainer.class, "getOpenContainer", "method.iplayer.getopencontainer"),
			new MethodData(Object.class, "getPixelmonData", "method.iplayer.getpixelmondata"),
			new MethodData(ITimers.class, "getTimers", "method.iplayer.gettimers"),
			new MethodData(boolean.class, "giveItem", "method.iplayer.giveitem",
				new ParameterData(String.class, "id", "parameter.item.name"),
				new ParameterData(int.class, "damage", "parameter.item.meta"),
				new ParameterData(int.class, "amount", "parameter.itemcount")
			),
			new MethodData(boolean.class, "giveItem", "method.iplayer.giveitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(boolean.class, "hasAchievement", "method.iplayer.hasachievement",
				new ParameterData(String.class, "achievement", "parameter.iplayer.achievement")
			),
			new MethodData(boolean.class, "hasActiveQuest", "method.iplayer.hasactivequest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MethodData(boolean.class, "isComleteQuest", "method.iplayer.iscomletequest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MethodData(boolean.class, "hasReadDialog", "method.iplayer.hasreaddialog",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			),
			new MethodData(void.class, "kick", "method.iplayer.kick",
				new ParameterData(String.class, "message", "parameter.message")
			),
			new MethodData(void.class, "removeDialog", "method.iplayer.removedialog",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			),
			new MethodData(void.class, "removeQuest", "method.iplayer.removequest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MethodData(void.class, "resetSpawnpoint", "method.iplayer.resetspawnpoint"),
			new MethodData(void.class, "sendMail", "method.iplayer.sendmail",
				new ParameterData(IPlayerMail.class, "mail", "parameter.mail")
			),
			new MethodData(void.class, "setExpLevel", "method.iplayer.setexplevel",
				new ParameterData(int.class, "level", "type.level")
			),
			new MethodData(void.class, "setGamemode", "method.iplayer.setgamemode",
				new ParameterData(int.class, "mode", "parameter.gamemode")
			),
			new MethodData(void.class, "setHunger", "method.iplayer.sethunger",
				new ParameterData(int.class, "level", "type.level")
			),
			new MethodData(void.class, "setSpawnpoint", "method.iplayer.setspawnpoint",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(IContainer.class, "showChestGui", "method.iplayer.showchestgui",
				new ParameterData(int.class, "rows", "parameter.chestgui.rows")
			),
			new MethodData(void.class, "showCustomGui", "method.iplayer.showcustomgui",
				new ParameterData(ICustomGui.class, "gui", "parameter.customgui")
			),
			new MethodData(void.class, "showDialog", "method.iplayer.showdialog",
				new ParameterData(int.class, "id", "parameter.dialog.id"),
				new ParameterData(String.class, "name", "parameter.showdialog.entity.name")
			),
			new MethodData(void.class, "startQuest", "method.iplayer.startquest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MethodData(void.class, "stopQuest", "method.iplayer.stopquest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MethodData(boolean.class, "isMoved", "method.iplayer.ismoved"),
			new MethodData(void.class, "addMoney", "method.iplayer.addmoney",
				new ParameterData(long.class, "value", "parameter.money.value")
			),
			new MethodData(long.class, "getMoney", "method.iplayer.getmoney"),
			new MethodData(void.class, "setMoney", "method.iplayer.setmoney",
				new ParameterData(long.class, "value", "parameter.money.value")
			),
			new MethodData(int[].class, "getKeyPressed", "method.iplayer.getkeypressed"),
			new MethodData(boolean.class, "hasOrKeyPressed", "method.iplayer.hasorkeyspressed",
				new ParameterData(int[].class, "key", "parameter.keyboard.key")
			),
			new MethodData(int[].class, "getMousePressed", "method.iplayer.getmousepressed"),
			new MethodData(boolean.class, "hasMousePress", "method.iplayer.hasmousepress",
				new ParameterData(int.class, "key", "parameter.mouse.key")
			),
			new MethodData(void.class, "completeQuest", "method.iplayer.completequest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MethodData(IOverlayHUD.class, "getOverlayHUD", "method.iplayer.gethud"),
			new MethodData(double[].class, "getWindowSize", "method.iplayer.getwindowsize"),
			new MethodData(void.class, "stopSound", "method.iplayer.stopsound",
				new ParameterData(int.class, "categoryType", "parameter.sound.cat.type"),
				new ParameterData(String.class, "sound", "parameter.sound.name")
			),
			new MethodData(boolean.class, "hasPermission", "method.iplayer.haspermission",
				new ParameterData(String.class, "permission", "parameter.npcapi.permission")
			),
			new MethodData(IContainer.class, "getInventory", "method.iplayer.getinventory"),
			new MethodData(int.class, "inventoryItemCount", "method.iplayer.inventoryitemcount",
				new ParameterData(IItemStack.class, "stack", "parameter.stack"),
				new ParameterData(boolean.class, "ignoreDamage", "parameter.ignoredamage"),
				new ParameterData(boolean.class, "ignoreNBT", "parameter.ignorenbt")
			),
			new MethodData(int.class, "inventoryItemCount", "method.iplayer.inventoryitemcount",
				new ParameterData(String.class, "id", "parameter.item.name"),
				new ParameterData(int.class, "amount", "parameter.itemcount")
			),
			new MethodData(int.class, "inventoryItemCount", "method.iplayer.inventoryitemcount",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(boolean.class, "canQuestBeAccepted", "method.iplayer.canquestbeaccepted",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MethodData(IQuest[].class, "getFinishedQuests", "method.iplayer.getfinishedquests"),
			new MethodData(IItemStack.class, "getInventoryHeldItem", "method.iplayer.getinventoryhelditem"),
			new MethodData(void.class, "updatePlayerInventory", "method.iplayer.updateplayerinventory"),
			new MethodData(IContainer.class, "getBubblesInventory", "method.iplayer.getbubblesinventory")
		)
	),
	IPlayerMail(new InterfaseData(IPlayerMail.class, 
			null,
			new Class<?>[] { PlayerMail.class },
			"interfase.iplayermail", 
			new MethodData(IQuest.class, "getQuest", "method.iplayermail.getquest"),
			new MethodData(String.class, "getSubject", "method.iplayermail.getsubject"),
			new MethodData(void.class, "setQuest", "method.iplayermail.setquest",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MethodData(void.class, "setSender", "method.iplayermail.setsender",
				new ParameterData(String.class, "sender", "parameter.mail.sender")
			),
			new MethodData(String[].class, "getText", "method.iplayermail.gettext"),
			new MethodData(void.class, "setText", "method.iplayermail.settext",
				new ParameterData(String[].class, "text", "parameter.iplayermail.text")
			),
			new MethodData(IContainer.class, "getContainer", "method.iplayermail.getcontainer"),
			new MethodData(void.class, "setSubject", "method.iplayermail.setsubject",
				new ParameterData(String.class, "subject", "parameter.mail.subject")
			),
			new MethodData(String.class, "getSender", "method.iplayermail.getsender")
		)
	),
	IPos(new InterfaseData(IPos.class, 
			null,
			new Class<?>[] { BlockPosWrapper.class },
			"interfase.ipos", 
			new MethodData(IPos.class, "add", "method.ipos.add",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(IPos.class, "add", "method.ipos.add",
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MethodData(IPos.class, "offset", "method.ipos.offset.0",
				new ParameterData(int.class, "direction", "parameter.direction"),
				new ParameterData(int.class, "n", "parameter.blocks")
			),
			new MethodData(IPos.class, "offset", "method.ipos.offset.0",
				new ParameterData(int.class, "direction", "parameter.direction")
			),
			new MethodData(double[].class, "normalize", "method.ipos.normalize"),
			new MethodData(int.class, "getX", "method.getx"),
			new MethodData(int.class, "getZ", "method.getz"),
			new MethodData(IPos.class, "up", "method.ipos.up.0",
				new ParameterData(int.class, "", "parameter.blocks")
			),
			new MethodData(IPos.class, "up", "method.ipos.up.0"),
			new MethodData(int.class, "getY", "method.gety"),
			new MethodData(IPos.class, "west", "method.ipos.west.0",
				new ParameterData(int.class, "", "parameter.blocks")
			),
			new MethodData(IPos.class, "west", "method.ipos.west.0"),
			new MethodData(IPos.class, "east", "method.ipos.east.0",
				new ParameterData(int.class, "", "parameter.blocks")
			),
			new MethodData(IPos.class, "east", "method.ipos.east.0"),
			new MethodData(IPos.class, "down", "method.ipos.down.0",
				new ParameterData(int.class, "", "parameter.blocks")
			),
			new MethodData(IPos.class, "down", "method.ipos.down.0"),
			new MethodData(IPos.class, "north", "method.ipos.north.0"),
			new MethodData(IPos.class, "north", "method.ipos.north.0",
				new ParameterData(int.class, "", "parameter.blocks")
			),
			new MethodData(IPos.class, "south", "method.ipos.south.0",
				new ParameterData(int.class, "", "parameter.blocks")
			),
			new MethodData(IPos.class, "south", "method.ipos.south.0"),
			new MethodData(IPos.class, "subtract", "method.ipos.subtract",
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MethodData(IPos.class, "subtract", "method.ipos.subtract",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(BlockPos.class, "getMCBlockPos", "method.ipos.getmcblockpos"),
			new MethodData(double.class, "distanceTo", "method.ipos.distanceto",
				new ParameterData(IPos.class, "pos", "parameter.pos")
			)
		)
	),
	IProjectile(new InterfaseData(IProjectile.class, 
			IThrowable.class,
			new Class<?>[] { ProjectileWrapper.class },
			"interfase.iprojectile", 
			new MethodData(IItemStack.class, "getItem", "method.iprojectile.getitem"),
			new MethodData(int.class, "getAccuracy", "method.iprojectile.getaccuracy"),
			new MethodData(void.class, "setHeading", "method.iprojectile.setheading",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MethodData(void.class, "setHeading", "method.iprojectile.setheading",
				new ParameterData(float.class, "yaw", "parameter.yaw"),
				new ParameterData(float.class, "pitch", "parameter.pitch")
			),
			new MethodData(void.class, "setHeading", "method.iprojectile.setheading",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz")
			),
			new MethodData(boolean.class, "getHasGravity", "method.iprojectile.gethasgravity"),
			new MethodData(void.class, "setHasGravity", "method.iprojectile.sethasgravity",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MethodData(void.class, "setItem", "method.iprojectile.setitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(void.class, "enableEvents", "method.iprojectile.enableevents"),
			new MethodData(void.class, "setAccuracy", "method.iprojectile.setaccuracy",
				new ParameterData(int.class, "accuracy", "parameter.inpcranged.accuracy")
			)
		)
	),
	IQuest(new InterfaseData(IQuest.class, 
			null,
			new Class<?>[] { Quest.class },
			"interfase.iquest", 
			new MethodData(String.class, "getName", "method.iquest.getname"),
			new MethodData(int.class, "getId", "method.iquest.getid"),
			new MethodData(void.class, "setName", "method.iquest.setname",
				new ParameterData(String.class, "name", "parameter.quest.name")
			),
			new MethodData(void.class, "save", "method.iquest.save"),
			new MethodData(int.class, "getLevel", "method.iquest.getlevel"),
			new MethodData(void.class, "setLevel", "method.iquest.setlevel",
				new ParameterData(int.class, "level", "parameter.level")
			),
			new MethodData(boolean.class, "isCancelable", "method.iquest.iscancelable"),
			new MethodData(IQuestCategory.class, "getCategory", "method.iquest.getcategory"),
			new MethodData(String.class, "getTitle", "method.iquest.gettitle"),
			new MethodData(String.class, "getNpcName", "method.iquest.getnpcname"),
			new MethodData(void.class, "setLogText", "method.iquest.setlogtext",
				new ParameterData(String.class, "text", "parameter.quest.log")
			),
			new MethodData(void.class, "setRewardText", "method.iquest.setrewardtext",
				new ParameterData(String.class, "text", "parameter.quest.reward.n")
			),
			new MethodData(int.class, "getRewardType", "method.iquest.getrewardtype"),
			new MethodData(void.class, "setCancelable", "method.iquest.setcancelable",
				new ParameterData(boolean.class, "cancelable", "parameter.boolean")
			),
			new MethodData(void.class, "setNextQuest", "method.iquest.setnextquest",
				new ParameterData(IQuest.class, "quest", "parameter.quest")
			),
			new MethodData(boolean.class, "getIsRepeatable", "method.iquest.getisrepeatable"),
			new MethodData(void.class, "sendChangeToAll", "method.iquest.sendchangetoall"),
			new MethodData(IQuest.class, "getNextQuest", "method.iquest.getnextquest"),
			new MethodData(IContainer.class, "getRewards", "method.iquest.getrewards"),
			new MethodData(void.class, "setNpcName", "method.iquest.setnpcname",
				new ParameterData(String.class, "name", "parameter.quest.npcname")
			),
			new MethodData(void.class, "setForgetQuests", "method.iquest.setforgetquests",
				new ParameterData(int[].class, "forget", "parameter.quest.forget.q")
			),
			new MethodData(void.class, "setCompleteText", "method.iquest.setcancelable",
				new ParameterData(String.class, "text", "parameter.quest.completetext")
			),
			new MethodData(String.class, "getCompleteText", "method.iquest.getcompletetext"),
			new MethodData(int[].class, "getForgetQuests", "method.iquest.getforgetquests"),
			new MethodData(String.class, "getLogText", "method.iquest.getlogtext"),
			new MethodData(IQuestObjective[].class, "getObjectives", "method.iquest.getobjectives",
				new ParameterData(IPlayer.class, "player", "parameter.player")
			),
			new MethodData(int[].class, "getForgetDialogues", "method.iquest.getforgetdialogues"),
			new MethodData(void.class, "setForgetDialogues", "method.iquest.setforgetdialogues",
				new ParameterData(int[].class, "forget", "parameter.quest.forget.d")
			),
			new MethodData(IQuestObjective.class, "addTask", "method.iquest.addtask"),
			new MethodData(boolean.class, "removeTask", "method.iquest.removetask",
				new ParameterData(IQuestObjective.class, "task", "parameter.quest.task")
			),
			new MethodData(void.class, "setRewardType", "method.iquest.setrewardtype",
				new ParameterData(int.class, "type", "parameter.quest.reward.t")
			),
			new MethodData(boolean.class, "isSetUp", "method.iquest.issetup")
		)
	),
	IQuestCategory(new InterfaseData(IQuestCategory.class, 
			null,
			new Class<?>[] { QuestCategory.class },
			"interfase.iquestcategory", 
			new MethodData(String.class, "getName", "method.iquestcat.getname"),
			new MethodData(IQuest.class, "create", "method.iquestcat.create"),
			new MethodData(IQuest[].class, "quests", "method.iquestcat.quests")
		)
	),
	IQuestHandler(new InterfaseData(IQuestHandler.class, 
			null,
			new Class<?>[] { QuestController.class },
			"interfase.iquesthandler", 
			new MethodData(IQuest.class, "get", "method.iquesthandler.get",
				new ParameterData(int.class, "id", "parameter.quest.id")
			),
			new MethodData(IQuestCategory[].class, "categories", "method.iquesthandler.categories")
		)
	),
	IQuestObjective(new InterfaseData(IQuestObjective.class, 
			null,
			new Class<?>[] { QuestObjective.class },
			"interfase.iquestobjective", 
			new MethodData(int.class, "getType", "method.iquestobj.gettype"),
			new MethodData(IItemStack.class, "getItem", "method.iquestobj.getitem"),
			new MethodData(String.class, "getText", "method.iquestobj.gettext"),
			new MethodData(int.class, "getProgress", "method.iquestobj.getprogress"),
			new MethodData(void.class, "setType", "method.iquestobj.settype",
				new ParameterData(int.class, "type", "parameter.iquestobj.type")
			),
			new MethodData(int.class, "getTargetID", "method.iquestobj.gettargetid"),
			new MethodData(void.class, "setProgress", "method.iquestobj.setprogress",
				new ParameterData(int.class, "value", "parameter.value")
			),
			new MethodData(int.class, "getMaxProgress", "method.iquestobj.getmaxprogress"),
			new MethodData(String.class, "getOrientationEntityName", "method.iquestobj.getorientationentityname"),
			new MethodData(boolean.class, "isCompleted", "method.iquestobj.iscompleted"),
			new MethodData(int.class, "getCompassDimension", "method.iquestobj.getcompassdimension"),
			new MethodData(void.class, "setCompassDimension", "method.iquestobj.setcompassdimension",
				new ParameterData(int.class, "dimensionID", "parameter.dimension.id")
			),
			new MethodData(void.class, "setOrientationEntityName", "method.iquestobj.setorientationentityname",
				new ParameterData(String.class, "name", "parameter.entity.name")
			),
			new MethodData(void.class, "setItem", "method.iquestobj.setitem",
				new ParameterData(IItemStack.class, "item", "parameter.stack")
			),
			new MethodData(String.class, "getTargetName", "method.iquestobj.gettargetname"),
			new MethodData(int.class, "getAreaRange", "method.iquestobj.getarearange"),
			new MethodData(void.class, "setItemIgnoreDamage", "method.iquestobj.setitemignoredamage",
				new ParameterData(boolean.class, "bo", "parameter.ignoredamage")
			),
			new MethodData(boolean.class, "isItemLeave", "method.iquestobj.isitemleave"),
			new MethodData(boolean.class, "isIgnoreDamage", "method.iquestobj.isignoredamage"),
			new MethodData(boolean.class, "isItemIgnoreNBT", "method.iquestobj.isitemignorenbt"),
			new MethodData(void.class, "setItemLeave", "method.iquestobj.setitemleave",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MethodData(void.class, "setItemIgnoreNBT", "method.iquestobj.setitemignorenbt",
				new ParameterData(boolean.class, "bo", "parameter.ignorenbt")
			),
			new MethodData(void.class, "setMaxProgress", "method.iquestobj.setmaxprogress",
				new ParameterData(int.class, "value", "parameter.value")
			),
			new MethodData(void.class, "setAreaRange", "method.iquestobj.setarearange",
				new ParameterData(int.class, "range", "parameter.questobj.range")
			),
			new MethodData(void.class, "setTargetID", "method.iquestobj.settargetid",
				new ParameterData(int.class, "id", "parameter.dialog.id")
			),
			new MethodData(void.class, "setTargetName", "method.iquestobj.settargetname",
				new ParameterData(String.class, "name", "parameter.entity.name")
			),
			new MethodData(IPos.class, "getCompassPos", "method.iquestobj.getcompasspos"),
			new MethodData(void.class, "setCompassPos", "method.iquestobj.setcompasspos",
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MethodData(void.class, "setCompassPos", "method.iquestobj.setcompasspos",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(int.class, "getCompassRange", "method.iquestobj.getcompassrange"),
			new MethodData(void.class, "setCompassRange", "method.iquestobj.setcompassrange",
				new ParameterData(int.class, "range", "parameter.range")
			)
		)
	),
	IRayTrace(new InterfaseData(IRayTrace.class, 
			null,
			new Class<?>[] { RayTraceWrapper.class },
			"interfase.iraytrace", 
			new MethodData(IBlock.class, "getBlock", "method.iraytrace.getblock"),
			new MethodData(IPos.class, "getPos", "method.getpos"),
			new MethodData(int.class, "getSideHit", "method.iraytrace.getsidehit")
		)
	),
	IRecipeHandler(new InterfaseData(IRecipeHandler.class, 
			null,
			new Class<?>[] { RecipeController.class },
			"interfase.irecipehandler", 
			new MethodData(boolean.class, "delete", "method.irecipe.delete",
				new ParameterData(int.class, "id", "parameter.irecipe.id")
			),
			new MethodData(boolean.class, "delete", "method.irecipe.delete",
				new ParameterData(String.class, "group", "parameter.irecipe.group"),
				new ParameterData(String.class, "name", "parameter.irecipe.name")
			),
			new MethodData(INpcRecipe.class, "addRecipe", "method.irecipe.getrecipe",
				new ParameterData(String.class, "group", "parameter.irecipe.group"),
				new ParameterData(String.class, "name", "parameter.irecipe.name"),
				new ParameterData(boolean.class, "global", "parameter.irecipe.isglobal"),
				new ParameterData(boolean.class, "known", "parameter.irecipe.isknown"),
				new ParameterData(ItemStack.class, "result", "parameter.stack"),
				new ParameterData(Object[].class, "objects", "parameter.irecipe.objects")
			),
			new MethodData(INpcRecipe.class, "addRecipe", "method.irecipe.getrecipe",
				new ParameterData(String.class, "group", "parameter.irecipe.group"),
				new ParameterData(String.class, "name", "parameter.irecipe.name"),
				new ParameterData(boolean.class, "global", "parameter.irecipe.isglobal"),
				new ParameterData(boolean.class, "known", "parameter.irecipe.isknown"),
				new ParameterData(ItemStack.class, "result", "parameter.stack"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(ItemStack[].class, "stacks", "parameter.stack")
			),
			new MethodData(INpcRecipe[].class, "getCarpentryData", "method.irecipe.getcarpentryrecipes"),
			new MethodData(INpcRecipe[].class, "getGlobalData", "method.irecipe.getglobalrecipes"),
			new MethodData(INpcRecipe[].class, "getGlobalRecipes", "method.irecipe.getglobalrecipes",
				new ParameterData(String.class, "group", "parameter.irecipe.group")
			),
			new MethodData(INpcRecipe.class, "getRecipe", "method.irecipe.getrecipe",
				new ParameterData(String.class, "group", "parameter.irecipe.group"),
				new ParameterData(String.class, "name", "parameter.irecipe.name")
			),
			new MethodData(INpcRecipe.class, "getRecipe", "method.irecipe.getrecipe",
				new ParameterData(int.class, "id", "parameter.irecipe.id")
			),
			new MethodData(INpcRecipe[].class, "getCarpentryRecipes", "method.irecipe.getcarpentryrecipes",
				new ParameterData(String.class, "group", "parameter.irecipe.group")
			)
		)
	),
	IRoleDialog(new InterfaseData(IRoleDialog.class, 
			null,
			new Class<?>[] { RoleDialog.class },
			"interfase.iroledialog", 
			new MethodData(void.class, "setDialog", "method.iroledialog.setdialog",
				new ParameterData(String.class, "text", "parameter.dialog.text")
			),
			new MethodData(String.class, "getDialog", "method.iroledialog.getdialog"),
			new MethodData(String.class, "getOptionDialog", "method.iroledialog.getoptiondialog",
				new ParameterData(int.class, "option", "parameter.dialog.option.pos")
			),
			new MethodData(void.class, "setOptionDialog", "method.iroledialog.setoptiondialog",
				new ParameterData(int.class, "option", "parameter.dialog.option.pos"),
				new ParameterData(String.class, "text", "parameter.dialog.text")
			),
			new MethodData(String.class, "getOption", "method.iroledialog.option",
				new ParameterData(int.class, "option", "parameter.dialog.option.pos")
			),
			new MethodData(void.class, "setOption", "method.iroledialog.setoption",
				new ParameterData(int.class, "option", "parameter.dialog.option.pos"),
				new ParameterData(String.class, "text", "parameter.dialog.text")
			)
		)
	),
	IRoleFollower(new InterfaseData(IRoleFollower.class, 
			INPCRole.class,
			new Class<?>[] { RoleFollower.class },
			"interfase.irolefollower", 
			new MethodData(void.class, "reset", "method.irolefollower.reset"),
			new MethodData(void.class, "addDays", "method.irolefollower.adddays",
				new ParameterData(int.class, "days", "parameter.rl.days")
			),
			new MethodData(boolean.class, "isFollowing", "method.irolefollower.isfollowing"),
			new MethodData(boolean.class, "getGuiDisabled", "method.irolefollower.getguidisabled"),
			new MethodData(boolean.class, "getInfinite", "method.irolefollower.getinfinite"),
			new MethodData(void.class, "setGuiDisabled", "method.irolefollower.setguidisabled",
				new ParameterData(boolean.class, "disabled", "parameter.boolean")
			),
			new MethodData(void.class, "setInfinite", "method.irolefollower.setinfinite",
				new ParameterData(boolean.class, "infinite", "parameter.boolean")
			),
			new MethodData(int.class, "getDays", "method.irolefollower.getdays"),
			new MethodData(boolean.class, "getRefuseSoulstone", "method.irolefollower.getrefusesoulstone"),
			new MethodData(void.class, "setRefuseSoulstone", "method.irolefollower.setrefusesoulstone",
				new ParameterData(boolean.class, "refuse", "parameter.boolean")
			),
			new MethodData(IPlayer.class, "getFollowing", "method.irolefollower.getfollowing"),
			new MethodData(void.class, "setFollowing", "method.irolefollower.setfollowing",
				new ParameterData(IPlayer.class, "player", "parameter.player")
			)
		)
	),
	IRoleTrader(new InterfaseData(IRoleTrader.class, 
			INPCRole.class,
			new Class<?>[] { Marcet.class },
			"interfase.iroletrader", 
			new MethodData(void.class, "remove", "method.iroletrader.remove",
				new ParameterData(int.class, "position", "parameter.position")
			),
			new MethodData(String.class, "getName", "method.iroletrader.getname"),
			new MethodData(void.class, "setName", "method.iroletrader.setname",
				new ParameterData(String.class, "name", "parameter.name")
			),
			new MethodData(void.class, "set", "method.iroletrader.set",
				new ParameterData(int.class, "position", "parameter.position"),
				new ParameterData(IItemStack.class, "product", "parameter.iroletrader.product"),
				new ParameterData(IItemStack[].class, "currencys", "parameter.iroletrader.currencys")
			),
			new MethodData(IItemStack.class, "getCurrency", "method.iroletrader.getcurrency",
				new ParameterData(int.class, "position", "parameter.position"),
				new ParameterData(int.class, "slot", "parameter.slot")
			),
			new MethodData(IItemStack.class, "getProduct", "method.iroletrader.getproduct",
				new ParameterData(int.class, "position", "parameter.position")
			)
		)
	),
	IRoleTransporter(new InterfaseData(IRoleTransporter.class, 
			INPCRole.class,
			new Class<?>[] { RoleTransporter.class },
			"interfase.iroletransporter", 
			new MethodData(ITransportLocation.class, "getLocation", "method.iroletransporter.getlocation")
		)
	),
	ISchematic(new InterfaseData(ISchematic.class, 
			null,
			new Class<?>[] { Blueprint.class, Schematic.class },
			"interfase.ischematic", 
			new MethodData(short.class, "getLength", "method.ischematic.getlength"),
			new MethodData(String.class, "getName", "method.ischematic.getname"),
			new MethodData(BlockPos.class, "getOffset", "method.ischematic.getoffset"),
			new MethodData(NBTTagList.class, "getEntitys", "method.ischematic.getentitys"),
			new MethodData(IBlockState.class, "getBlockState", "method.ischematic.getblockstate",
				new ParameterData(int.class, "state", "parameter.ischematic.pos")
			),
			new MethodData(IBlockState.class, "getBlockState", "method.ischematic.getblockstate",
				new ParameterData(int.class, "x", "parameter.ischematic.x"),
				new ParameterData(int.class, "y", "parameter.ischematic.y"),
				new ParameterData(int.class, "z", "parameter.ischematic.z")
			),
			new MethodData(NBTTagCompound.class, "getTileEntity", "method.ischematic.gettileentity",
				new ParameterData(int.class, "pos", "parameter.ischematic.pos")
			),
			new MethodData(boolean.class, "hasEntitys", "method.ischematic.hasentitys"),
			new MethodData(short.class, "getWidth", "method.ischematic.getwidth"),
			new MethodData(short.class, "getHeight", "method.ischematic.getheight"),
			new MethodData(int.class, "getTileEntitySize", "method.ischematic.gettileentitysize"),
			new MethodData(NBTTagCompound.class, "getNBT", "method.ischematic.getnbt")
		)
	),
	IScoreboard(new InterfaseData(IScoreboard.class, 
			null,
			new Class<?>[] { ScoreboardWrapper.class },
			"interfase.iscoreboard", 
			new MethodData(IScoreboardTeam.class, "getTeam", "method.iscoreboard.getteam",
				new ParameterData(String.class, "name", "parameter.score.teamname")
			),
			new MethodData(void.class, "deletePlayerScore", "method.iscoreboard.deletePlayerScore",
				new ParameterData(String.class, "player", "parameter.player.name"),
				new ParameterData(String.class, "objective", "parameter.score.objective"),
				new ParameterData(String.class, "datatag", "parameter.score.datatag")
			),
			new MethodData(boolean.class, "hasPlayerObjective", "method.iscoreboard.hasplayerobjective",
				new ParameterData(String.class, "player", "parameter.player.name"),
				new ParameterData(String.class, "objective", "parameter.score.objective"),
				new ParameterData(String.class, "datatag", "parameter.score.datatag")
			),
			new MethodData(IScoreboardObjective.class, "addObjective", "method.iscoreboard.addobjective",
				new ParameterData(String.class, "objective", "parameter.score.objective"),
				new ParameterData(String.class, "criteria", "parameter.score.criteria")
			),
			new MethodData(void.class, "removeObjective", "method.iscoreboard.removeobjective",
				new ParameterData(String.class, "objective", "parameter.score.objective")
			),
			new MethodData(void.class, "removeTeam", "method.iscoreboard.removeteam",
				new ParameterData(String.class, "name", "parameter.score.teamname")
			),
			new MethodData(IScoreboardTeam[].class, "getTeams", "method.iscoreboard.getteams"),
			new MethodData(IScoreboardObjective[].class, "getObjectives", "method.iscoreboard.getobjectives"),
			new MethodData(String[].class, "getPlayerList", "method.iscoreboard.getplayerlist"),
			new MethodData(IScoreboardObjective.class, "getObjective", "method.iscoreboard.getobjective",
				new ParameterData(String.class, "name", "parameter.score.objective")
			),
			new MethodData(IScoreboardTeam.class, "addTeam", "method.iscoreboard.addTeam",
				new ParameterData(String.class, "name", "parameter.score.teamname")
			),
			new MethodData(int.class, "getPlayerScore", "method.iscoreboard.getplayerscore",
				new ParameterData(String.class, "player", "parameter.player.name"),
				new ParameterData(String.class, "objective", "parameter.score.objective"),
				new ParameterData(String.class, "datatag", "parameter.score.datatag")
			),
			new MethodData(IScoreboardTeam.class, "getPlayerTeam", "method.iscoreboard.getplayerteam",
				new ParameterData(String.class, "player", "parameter.player.name")
			),
			new MethodData(boolean.class, "hasObjective", "method.iscoreboard.hasobjective",
				new ParameterData(String.class, "objective", "parameter.score.objective")
			),
			new MethodData(boolean.class, "hasTeam", "method.iscoreboard.hasteam",
				new ParameterData(String.class, "name", "parameter.score.teamname")
			),
			new MethodData(void.class, "removePlayerTeam", "method.iscoreboard.removeteam",
				new ParameterData(String.class, "player", "parameter.player.name")
			),
			new MethodData(void.class, "setPlayerScore", "method.iscoreboard.setplayerscore",
				new ParameterData(String.class, "player", "parameter.player.name"),
				new ParameterData(String.class, "objective", "parameter.score.objective"),
				new ParameterData(int.class, "score", "parameter.score"),
				new ParameterData(String.class, "datatag", "parameter.score.datatag")
			)
		)
	),
	IScoreboardObjective(new InterfaseData(IScoreboardObjective.class, 
			null,
			new Class<?>[] { ScoreboardObjectiveWrapper.class },
			"interfase.iscoreboardobjective", 
			new MethodData(String.class, "getName", "method.iscoreboardobjective.getname"),
			new MethodData(String.class, "getDisplayName", "method.iscoreboardobjective.getdisplayname"),
			new MethodData(IScoreboardScore.class, "getScore", "method.iscoreboardobjective.getscore",
				new ParameterData(String.class, "player", "parameter.player.name")
			),
			new MethodData(IScoreboardScore[].class, "getScores", "method.iscoreboardobjective.getscores"),
			new MethodData(IScoreboardScore.class, "createScore", "method.iscoreboardobjective.createscore",
				new ParameterData(String.class, "player", "parameter.player.name")
			),
			new MethodData(boolean.class, "hasScore", "method.iscoreboardobjective.hasscore",
				new ParameterData(String.class, "player", "parameter.player.name")
			),
			new MethodData(boolean.class, "isReadyOnly", "method.iscoreboardobjective.isreadyonly"),
			new MethodData(void.class, "removeScore", "method.iscoreboardobjective.removescore",
				new ParameterData(String.class, "player", "parameter.player.name")
			),
			new MethodData(String.class, "getCriteria", "method.iscoreboardobjective.getcriteria"),
			new MethodData(void.class, "setDisplayName", "method.iscoreboardobjective.setdisplayname",
				new ParameterData(String.class, "name", "parameter.score.objective")
			)
		)
	),
	IScoreboardScore(new InterfaseData(IScoreboardScore.class, 
			null,
			new Class<?>[] { ScoreboardScoreWrapper.class },
			"interfase.iscoreboardscore", 
			new MethodData(int.class, "getValue", "method.iscoreboardscore.getvalue"),
			new MethodData(void.class, "setValue", "method.iscoreboardscore.setvalue",
				new ParameterData(int.class, "value", "parameter.score")
			),
			new MethodData(String.class, "getPlayerName", "method.getplayername")
		)
	),
	IScoreboardTeam(new InterfaseData(IScoreboardTeam.class, 
			null,
			new Class<?>[] { ScoreboardTeamWrapper.class },
			"interfase.iscoreboardteam", 
			new MethodData(String.class, "getName", "method.iscoreboardteam.getname"),
			new MethodData(String.class, "getDisplayName", "method.iscoreboardteam.getdisplayname"),
			new MethodData(void.class, "setColor", "method.iscoreboardteam.setColor",
				new ParameterData(String.class, "color", "parameter.colorname")
			),
			new MethodData(String.class, "getColor", "method.iscoreboardteam.getcolor"),
			new MethodData(String[].class, "getPlayers", "method.iscoreboardteam.getplayers"),
			new MethodData(boolean.class, "getSeeInvisibleTeamPlayers", "method.iscoreboardteam.getseeinvisibleteamplayers"),
			new MethodData(void.class, "setSeeInvisibleTeamPlayers", "method.iscoreboardteam.setseeinvisibleteamplayers",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MethodData(boolean.class, "hasPlayer", "method.iscoreboardteam.hasplayer",
				new ParameterData(String.class, "player", "parameter.player.name")
			),
			new MethodData(void.class, "clearPlayers", "method.iscoreboardteam.clearplayers"),
			new MethodData(boolean.class, "getFriendlyFire", "method.iscoreboardteam.getfriendlyfire"),
			new MethodData(void.class, "setFriendlyFire", "method.iscoreboardteam.setfriendlyfire",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MethodData(void.class, "addPlayer", "method.iscoreboardteam.addplayer",
				new ParameterData(String.class, "player", "parameter.player.name")
			),
			new MethodData(void.class, "removePlayer", "method.iscoreboardteam.removeplayer",
				new ParameterData(String.class, "player", "parameter.player.name")
			),
			new MethodData(void.class, "setDisplayName", "method.iscoreboardteam.setdisplayname",
				new ParameterData(String.class, "name", "parameter.score.teamname")
			)
		)
	),
	IScriptBlockHandler(new InterfaseData(IScriptBlockHandler.class, 
			IScriptHandler.class,
			new Class<?>[] { TileScripted.class, TileScriptedDoor.class },
			"interfase.iscriptblockhandler", 
			new MethodData(IBlock.class, "getBlock", "method.iscriptblockhandler.getblock")
		)
	),
	IScriptData(new InterfaseData(IScriptData.class, 
			null,
			new Class<?>[] { ScriptData.class },
			"interfase.iscriptdata", 
			new MethodData(Object.class, "getObject", "method.iscriptdata.getobject"),
			new MethodData(String.class, "getName", "method.iscriptdata.getname"),
			new MethodData(String.class, "getValue", "method.iscriptdata.getvalue"),
			new MethodData(int.class, "getType", "method.iscriptdata.gettype"),
			new MethodData(INbt.class, "getNBT", "method.iscriptdata.getnbt"),
			new MethodData(void.class, "setNBT", "method.iscriptdata.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			)
		)
	),
	IScriptHandler(new InterfaseData(IScriptHandler.class, 
			null,
			new Class<?>[] { DataScript.class, ClientScriptData.class, PlayerScriptData.class, ForgeScriptData.class },
			"interfase.iscripthandler", 
			new MethodData(String.class, "getLanguage", "method.iscripthandler.getlanguage"),
			new MethodData(boolean.class, "isClient", "method.iscripthandler.isclient"),
			new MethodData(void.class, "clearConsole", "method.iscripthandler.clearconsole"),
			new MethodData(Map.class, "getConsoleText", "method.iscripthandler.getconsoletext"),
			new MethodData(boolean.class, "getEnabled", "method.iscripthandler.getenabled"),
			new MethodData(List.class, "getScripts", "method.iscripthandler.getscripts"),
			new MethodData(String.class, "noticeString", "method.iscripthandler.noticestring"),
			new MethodData(void.class, "runScript", "method.iscripthandler.runscript",
				new ParameterData(EnumScriptType.class, "type", "parameter.iscripthandler.type"),
				new ParameterData(Event.class, "event", "parameter.iscripthandler.event")
			),
			new MethodData(void.class, "setLanguage", "method.iscripthandler.setlanguage",
				new ParameterData(String.class, "language", "parameter.iscripthandler.language")
			),
			new MethodData(void.class, "setEnabled", "method.iscripthandler.setenabled",
				new ParameterData(boolean.class, "bo", "parameter.enabled")
			)
		)
	),
	IScroll(new InterfaseData(IScroll.class, 
			ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiScrollWrapper.class },
			"interfase.iscroll", 
			new MethodData(IScroll.class, "setSize", "method.component.setsize",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MethodData(int.class, "getDefaultSelection", "method.iscroll.getdefaultselection"),
			new MethodData(IScroll.class, "setDefaultSelection", "method.iscroll.getdefaultselection",
				new ParameterData(int.class, "defaultSelection", "parameter.iscroll.defaultselection")
			),
			new MethodData(int.class, "getWidth", "method.component.getwidth"),
			new MethodData(int.class, "getHeight", "method.component.getheight"),
			new MethodData(IScroll.class, "setMultiSelect", "method.iscroll.setmultiselect",
				new ParameterData(boolean.class, "multiSelect", "parameter.boolean")
			),
			new MethodData(String[].class, "getList", "method.iscroll.getlist"),
			new MethodData(IScroll.class, "setList", "method.iscroll.setlist",
				new ParameterData(String[].class, "list", "parameter.s.list")
			),
			new MethodData(boolean.class, "isMultiSelect", "method.iscroll.ismultiselect")
		)
	),
	ITextField(new InterfaseData(ITextField.class, 
			ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiTextFieldWrapper.class },
			"interfase.itextfield", 
			new MethodData(ITextField.class, "setSize", "method.component.setsize",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MethodData(String.class, "getText", "method.itextfield.gettext"),
			new MethodData(ITextField.class, "setText", "method.itextfield.settext",
				new ParameterData(String.class, "text", "parameter.itextfield.text")
			),
			new MethodData(int.class, "getWidth", "method.component.getwidth"),
			new MethodData(int.class, "getHeight", "method.component.getheight")
		)
	),
	ITextPlane(new InterfaseData(ITextPlane.class, 
			null,
			null,
			"interfase.itextplane", 
			new MethodData(void.class, "setOffsetX", "method.itextplane.setOffsetx",
				new ParameterData(float.class, "x", "parameter.posx")
			),
			new MethodData(void.class, "setOffsetY", "method.itextplane.setOffsety",
				new ParameterData(float.class, "y", "parameter.posy")
			),
			new MethodData(void.class, "setOffsetZ", "method.itextplane.setOffsetz",
				new ParameterData(float.class, "z", "parameter.posz")
			),
			new MethodData(void.class, "setRotationX", "method.setrotx",
				new ParameterData(int.class, "x", "parameter.rotx")
			),
			new MethodData(void.class, "setRotationY", "method.setroty",
				new ParameterData(int.class, "y", "parameter.roty")
			),
			new MethodData(void.class, "setRotationZ", "method.setrotz",
				new ParameterData(int.class, "z", "parameter.rotz")
			),
			new MethodData(int.class, "getRotationX", "method.getrotx"),
			new MethodData(int.class, "getRotationZ", "method.getrotz"),
			new MethodData(String.class, "getText", "method.itextplane.gettext"),
			new MethodData(void.class, "setScale", "method.itextplane.setscale",
				new ParameterData(float.class, "scale", "parameter.itextplane.scale")
			),
			new MethodData(void.class, "setText", "method.itextplane.settext",
				new ParameterData(String.class, "text", "parameter.itextplane.text")
			),
			new MethodData(float.class, "getScale", "method.itextplane.getscale"),
			new MethodData(int.class, "getRotationY", "method.getroty"),
			new MethodData(float.class, "getOffsetX", "method.itextplane.getoffsetx"),
			new MethodData(float.class, "getOffsetY", "method.itextplane.getoffsety"),
			new MethodData(float.class, "getOffsetZ", "method.itextplane.getoffsetz")
		)
	),
	ITexturedButton(new InterfaseData(ITexturedButton.class, 
			IButton.class,
			null,
			"interfase.itexturedbutton", 
			new MethodData(String.class, "getTexture", "method.component.gettexture"),
			new MethodData(IButton.class, "setTexture", "method.component.settexture",
				new ParameterData(String.class, "texture", "parameter.texture")
			),
			new MethodData(ITexturedButton.class, "setTexture", "method.component.settexture",
				new ParameterData(String.class, "texture", "parameter.texture")
			),
			new MethodData(int.class, "getTextureX", "method.component.gettexturex"),
			new MethodData(int.class, "getTextureY", "method.component.gettexturey"),
			new MethodData(ITexturedButton.class, "setTextureOffset", "method.component.settextureoffset",
				new ParameterData(int.class, "textureX", "parameter.texturex"),
				new ParameterData(int.class, "textureY", "parameter.texturey")
			),
			new MethodData(IButton.class, "setTextureOffset", "method.component.settextureoffset",
				new ParameterData(int.class, "textureX", "parameter.texturex"),
				new ParameterData(int.class, "textureY", "parameter.texturey")
			)
		)
	),
	ITexturedRect(new InterfaseData(ITexturedRect.class, 
			ICustomGuiComponent.class,
			new Class<?>[] { CustomGuiTexturedRectWrapper.class },
			"interfase.itexturedrect", 
			new MethodData(ITexturedRect.class, "setSize", "method.component.setsize",
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height")
			),
			new MethodData(String.class, "getTexture", "method.component.gettexture"),
			new MethodData(ITexturedRect.class, "setScale", "method.component.setscale",
				new ParameterData(float.class, "scale", "parameter.scale")
			),
			new MethodData(int.class, "getWidth", "method.component.getwidth"),
			new MethodData(int.class, "getHeight", "method.component.getheight"),
			new MethodData(ITexturedRect.class, "setTexture", "method.component.settexture",
				new ParameterData(String.class, "texture", "parameter.texture")
			),
			new MethodData(int.class, "getTextureX", "method.component.gettexturex"),
			new MethodData(float.class, "getScale", "method.component.getscale"),
			new MethodData(int.class, "getTextureY", "method.component.gettexturey"),
			new MethodData(ITexturedRect.class, "setTextureOffset", "method.component.settextureoffset",
				new ParameterData(int.class, "textureX", "parameter.texturex"),
				new ParameterData(int.class, "textureY", "parameter.texturey")
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
			new MethodData(void.class, "clear", "method.itimers.clear"),
			new MethodData(void.class, "start", "method.itimers.start",
				new ParameterData(int.class, "id", "parameter.itimers.id"),
				new ParameterData(int.class, "ticks", "parameter.ticks"),
				new ParameterData(boolean.class, "repeat", "parameter.itimers.repeat")
			),
			new MethodData(boolean.class, "stop", "method.itimers.stop",
				new ParameterData(int.class, "id", "parameter.itimers.id")
			),
			new MethodData(void.class, "reset", "method.itimers.reset",
				new ParameterData(int.class, "id", "parameter.itimers.id")
			),
			new MethodData(boolean.class, "has", "method.itimers.has",
				new ParameterData(int.class, "id", "parameter.itimers.id")
			),
			new MethodData(void.class, "forceStart", "method.itimers.forcestart",
				new ParameterData(int.class, "id", "parameter.itimers.id"),
				new ParameterData(int.class, "ticks", "parameter.ticks"),
				new ParameterData(boolean.class, "repeat", "parameter.itimers.repeat")
			)
		)
	),
	IVillager(new InterfaseData(IVillager.class, 
			IEntityLiving.class,
			new Class<?>[] { VillagerWrapper.class },
			"interfase.ivillager", 
			new MethodData(MerchantRecipeList.class, "getRecipes", "method.ivillager.getrecipes",
				new ParameterData(IPlayer.class, "player", "parameter.player")
			),
			new MethodData(IInventory.class, "getVillagerInventory", "method.ivillager.getvillagerinventory")
		)
	),
	IWorld(new InterfaseData(IWorld.class, 
			null,
			new Class<?>[] { WorldWrapper.class },
			"interfase.iworld", 
			new MethodData(void.class, "trigger", "method.trigger",
				new ParameterData(int.class, "id", "parameter.trigger.id"),
				new ParameterData(Object[].class, "arguments", "parameter.trigger.arguments")
			),
			new MethodData(String.class, "getName", "method.iworld.getname"),
			new MethodData(long.class, "getTime", "method.iworld.gettime"),
			new MethodData(void.class, "setTime", "method.iworld.settime",
				new ParameterData(long.class, "ticks", "parameter.ticks")
			),
			new MethodData(IEntity.class, "getEntity", "method.iworld.getentity",
				new ParameterData(String.class, "uuid", "parameter.entity.uuid")
			),
			new MethodData(IEntity.class, "createEntity", "method.iworld.createentity",
				new ParameterData(String.class, "id", "parameter.entity.regname")
			),
			new MethodData(int.class, "getRedstonePower", "method.iworld.getredstonepower",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(IBlock.class, "getSpawnPoint", "method.iworld.getspawnpoint"),
			new MethodData(IScoreboard.class, "getScoreboard", "method.iworld.getscoreboard"),
			new MethodData(IDimension.class, "getDimension", "method.iworld.getdimension"),
			new MethodData(IPlayer[].class, "getAllPlayers", "method.iworld.getallplayers"),
			new MethodData(IItemStack.class, "createItem", "method.iworld.createitem",
				new ParameterData(String.class, "name", "parameter.item.name"),
				new ParameterData(int.class, "damage", "parameter.item.meta"),
				new ParameterData(int.class, "size", "parameter.itemcount")
			),
			new MethodData(IEntity[].class, "getAllEntities", "method.iworld.getallentities",
				new ParameterData(int.class, "type", "parameter.entity.type")
			),
			new MethodData(IEntity.class, "getClone", "method.iworld.getclone",
				new ParameterData(int.class, "tab", "parameter.clone.tab"),
				new ParameterData(String.class, "name", "parameter.clone.file")
			),
			new MethodData(IEntity.class, "getClosestEntity", "method.iworld.getclosestentity",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz"),
				new ParameterData(int.class, "range", "parameter.range"),
				new ParameterData(int.class, "type", "parameter.entity.type")
			),
			new MethodData(IEntity.class, "getClosestEntity", "method.iworld.getclosestentity",
				new ParameterData(IPos.class, "pos", "parameter.pos"),
				new ParameterData(int.class, "range", "parameter.range"),
				new ParameterData(int.class, "type", "parameter.entity.type")
			),
			new MethodData(long.class, "getTotalTime", "method.iworld.gettotaltime"),
			new MethodData(boolean.class, "isDay", "method.iworld.isday"),
			new MethodData(void.class, "playSoundAt", "method.iworld.playsoundat",
				new ParameterData(IPos.class, "pos", "parameter.pos"),
				new ParameterData(String.class, "sound", "parameter.range"),
				new ParameterData(float.class, "volume", "parameter.sound.volume"),
				new ParameterData(float.class, "pitch", "parameter.sound.pitch")
			),
			new MethodData(void.class, "removeBlock", "method.iworld.removeblock",
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MethodData(void.class, "removeBlock", "method.iworld.removeblock",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(IEntity.class, "spawnClone", "method.iworld.spawnclone",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz"),
				new ParameterData(int.class, "tab", "parameter.clone.tab"),
				new ParameterData(String.class, "name", "parameter.clone.file")
			),
			new MethodData(void.class, "thunderStrike", "method.iworld.thunderstrike",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz")
			),
			new MethodData(IEntity[].class, "getEntitys", "method.iworld.getentitys",
				new ParameterData(int.class, "type", "parameter.entity.type")
			),
			new MethodData(void.class, "forcePlaySoundAt", "method.iworld.playsoundat",
				new ParameterData(int.class, "categoryType", "parameter.sound.cat.type"),
				new ParameterData(IPos.class, "pos", "parameter.pos"),
				new ParameterData(String.class, "sound", "parameter.sound.name"),
				new ParameterData(float.class, "volume", "parameter.sound.volume"),
				new ParameterData(float.class, "pitch", "parameter.sound.pitch")
			),
			new MethodData(IBlock.class, "getBlock", "method.iworld.getblock",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(IBlock.class, "getBlock", "method.iworld.getblock",
				new ParameterData(IPos.class, "pos", "parameter.pos")
			),
			new MethodData(void.class, "spawnEntity", "method.iworld.spawnentity",
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MethodData(void.class, "setSpawnPoint", "method.iworld.setspawnpoint",
				new ParameterData(IBlock.class, "block", "parameter.iblock.setblock.0")
			),
			new MethodData(float.class, "getLightValue", "method.iworld.getlightvalue",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(void.class, "spawnParticle", "method.iworld.spawnparticle",
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
			new MethodData(boolean.class, "isRaining", "method.iworld.israining"),
			new MethodData(void.class, "setRaining", "method.iworld.setraining",
				new ParameterData(boolean.class, "bo", "parameter.boolean")
			),
			new MethodData(void.class, "broadcast", "method.iworld.broadcast",
				new ParameterData(String.class, "message", "parameter.message")
			),
			new MethodData(IData.class, "getStoreddata", "method.getstoreddata"),
			new MethodData(IData.class, "getTempdata", "method.gettempdata"),
			new MethodData(World.class, "getMCWorld", "method.iworld.getmcworld"),
			new MethodData(BlockPos.class, "getMCBlockPos", "method.ipos.getmcblockpos",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(IEntity[].class, "getNearbyEntities", "method.iworld.getnearbyEntities",
				new ParameterData(IPos.class, "pos", "parameter.pos"),
				new ParameterData(int.class, "range", "parameter.range"),
				new ParameterData(int.class, "type", "parameter.entity.type")
			),
			new MethodData(IEntity[].class, "getNearbyEntities", "method.iworld.getnearbyEntities",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz"),
				new ParameterData(int.class, "range", "parameter.range"),
				new ParameterData(int.class, "type", "parameter.entity.type")
			),
			new MethodData(IEntity.class, "createEntityFromNBT", "method.iworld.createentity",
				new ParameterData(INbt.class, "nbt", "parameter.entity.nbt")
			),
			new MethodData(String.class, "getBiomeName", "method.iworld.getbiomename",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(void.class, "explode", "method.iworld.explode",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz"),
				new ParameterData(float.class, "range", "parameter.range"),
				new ParameterData(boolean.class, "fire", "parameter.iworld.fire"),
				new ParameterData(boolean.class, "grief", "parameter.iworld.grief")
			),
			new MethodData(IPlayer.class, "getPlayer", "method.iworld.getplayer",
				new ParameterData(String.class, "name", "parameter.player.name")
			),
			new MethodData(void.class, "setBlock", "method.iworld.setblock",
				new ParameterData(IPos.class, "pos", "parameter.pos"),
				new ParameterData(String.class, "name", "parameter.block.name"),
				new ParameterData(int.class, "meta", "parameter.block.metadata")
			),
			new MethodData(void.class, "setBlock", "method.iworld.setblock",
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz"),
				new ParameterData(String.class, "name", "parameter.iblock.setblock.1"),
				new ParameterData(int.class, "meta", "parameter.block.metadata")
			),
			new MethodData(IItemStack.class, "createItemFromNbt", "method.iworld.createitem",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			)
		)
	),
	IWorldInfo(new InterfaseData(IWorldInfo.class, 
			null,
			new Class<?>[] { CustomWorldInfo.class },
			"interfase.iworldinfo", 
			new MethodData(int.class, "getId", "method.iworldinfo.getid"),
			new MethodData(void.class, "setNbt", "method.iworldinfo.setnbt",
				new ParameterData(INbt.class, "nbt", "parameter.nbt")
			),
			new MethodData(INbt.class, "getNbt", "method.iworldinfo.getnbt")
		)
	),
	NpcAPI(new InterfaseData(NpcAPI.class, 
			null,
			new Class<?>[] { WrapperNpcAPI.class },
			"interfase.npcapi", 
			new MethodData(ICustomGui.class, "createCustomGui", "method.npcapi.createcustomgui",
				new ParameterData(int.class, "id", "parameter.customgui.id"),
				new ParameterData(int.class, "width", "parameter.width"),
				new ParameterData(int.class, "height", "parameter.height"),
				new ParameterData(boolean.class, "pauseGame", "parameter.icustomgui.pausegame")
			),
			new MethodData(IPlayerMail.class, "createMail", "method.npcapi.createmail",
				new ParameterData(String.class, "sender", "parameter.mail.sender"),
				new ParameterData(String.class, "subject", "parameter.mail.subject")
			),
			new MethodData(ICustomNpc.class, "createNPC", "method.npcapi.createnpc",
				new ParameterData(World.class, "world", "parameter.world")
			),
			new MethodData(IDialogHandler.class, "getDialogs", "method.npcapi.getdialogs"),
			new MethodData(IFactionHandler.class, "getFactions", "method.npcapi.getfactions"),
			new MethodData(File.class, "getGlobalDir", "method.npcapi.getglobaldir"),
			new MethodData(IBlock.class, "getIBlock", "method.npcapi.getiblock",
				new ParameterData(World.class, "world", "parameter.world"),
				new ParameterData(BlockPos.class, "pos", "parameter.pos")
			),
			new MethodData(IEntityDamageSource.class, "getIDamageSource", "method.npcapi.getidamagesource",
				new ParameterData(String.class, "name", "parameter.damagesource.name"),
				new ParameterData(IEntity.class, "entity", "parameter.entity")
			),
			new MethodData(IDamageSource.class, "getIDamageSource", "method.npcapi.getidamagesource",
				new ParameterData(DamageSource.class, "source", "parameter.damagesource")
			),
			new MethodData(IWorld[].class, "getIWorlds", "method.npcapi.getiworlds"),
			new MethodData(INbt.class, "getRawPlayerData", "method.npcapi.getrawplayerdata",
				new ParameterData(String.class, "uuid", "parameter.entity.uuid")
			),
			new MethodData(File.class, "getWorldDir", "method.npcapi.getworlddir"),
			new MethodData(ICustomNpc.class, "spawnNPC", "method.npcapi.spawnnpc",
				new ParameterData(World.class, "world", "parameter.world"),
				new ParameterData(int.class, "x", "parameter.posx"),
				new ParameterData(int.class, "y", "parameter.posy"),
				new ParameterData(int.class, "z", "parameter.posz")
			),
			new MethodData(INbt.class, "stringToNbt", "method.npcapi.stringtonbt",
				new ParameterData(String.class, "str", "parameter.npcapi.nbtstr")
			),
			new MethodData(IPlayer.class, "getIPlayer", "method.npcapi.getiplayer",
				new ParameterData(String.class, "nameOrUUID", "parameter.entity.name")
			),
			new MethodData(IPlayer[].class, "getAllPlayers", "method.npcapi.getallplayers"),
			new MethodData(IBorderHandler.class, "getBorders", "method.npcapi.getborders"),
			new MethodData(IAnimationHandler.class, "getAnimations", "method.npcapi.getanimations"),
			new MethodData(IMetods.class, "getMetods", "method.npcapi.getmetods"),
			new MethodData(IKeyBinding.class, "getIKeyBinding", "method.npcapi.getikeybinding"),
			new MethodData(IRecipeHandler.class, "getRecipes", "method.npcapi.getrecipes"),
			new MethodData(String.class, "executeCommand", "method.executecommand",
				new ParameterData(IWorld.class, "world", "parameter.world"),
				new ParameterData(String.class, "command", "parameter.command")
			),
			new MethodData(EventBus.class, "events", "method.npcapi.events"),
			new MethodData(INbt.class, "getINbt", "method.npcapi.getinbt",
				new ParameterData(NBTTagCompound.class, "nbt", "parameter.nbt")
			),
			new MethodData(IWorld.class, "getIWorld", "method.npcapi.getiworld",
				new ParameterData(int.class, "dimensionId", "parameter.dimension.id")
			),
			new MethodData(IWorld.class, "getIWorld", "method.npcapi.getiworld",
				new ParameterData(World.class, "world", "parameter.world")
			),
			new MethodData(IPos.class, "getIPos", "method.npcapi.getipos",
				new ParameterData(double.class, "x", "parameter.posx"),
				new ParameterData(double.class, "y", "parameter.posy"),
				new ParameterData(double.class, "z", "parameter.posz")
			),
			new MethodData(IPos.class, "getIPos", "method.npcapi.getipos",
				new ParameterData(BlockPos.class, "pos", "parameter.pos")
			),
			new MethodData(ICloneHandler.class, "getClones", "method.npcapi.getclones"),
			new MethodData(IEntity.class, "getIEntity", "method.npcapi.getientity",
				new ParameterData(Entity.class, "entity", "parameter.entity")
			),
			new MethodData(IItemStack.class, "getIItemStack", "method.npcapi.getiitemstack",
				new ParameterData(ItemStack.class, "stack", "parameter.stack")
			),
			new MethodData(void.class, "registerCommand", "method.npcapi.registercommand",
				new ParameterData(CommandNoppesBase.class, "command", "parameter.command.c")
			),
			new MethodData(String.class, "getRandomName", "method.npcapi.getrandomname",
				new ParameterData(int.class, "dictionary", "parameter.npcapi.dictionary"),
				new ParameterData(int.class, "gender", "parameter.npcapi.gender")
			),
			new MethodData(IQuestHandler.class, "getQuests", "method.npcapi.getquests"),
			new MethodData(IContainer.class, "getIContainer", "method.npcapi.geticontainer",
				new ParameterData(Container.class, "container", "parameter.container")
			),
			new MethodData(IContainer.class, "getIContainer", "method.npcapi.geticontainer",
				new ParameterData(IInventory.class, "container", "parameter.container")
			),
			new MethodData(boolean.class, "hasPermissionNode", "method.npcapi.haspermissionnode",
				new ParameterData(String.class, "permission", "parameter.npcapi.permission")
			),
			new MethodData(void.class, "registerPermissionNode", "method.npcapi.registerpermissionnode",
				new ParameterData(String.class, "permission", "parameter.npcapi.permission"),
				new ParameterData(int.class, "defaultType", "parameter.npcapi.default.type")
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
