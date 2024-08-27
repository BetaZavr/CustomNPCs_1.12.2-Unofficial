package noppes.npcs;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.RecipeBook;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.blocks.CustomBlock;
import noppes.npcs.blocks.CustomBlockPortal;
import noppes.npcs.blocks.CustomBlockSlab;
import noppes.npcs.blocks.CustomBlockStairs;
import noppes.npcs.blocks.CustomChest;
import noppes.npcs.blocks.CustomDoor;
import noppes.npcs.blocks.CustomLiquid;
import noppes.npcs.blocks.tiles.CustomTileEntityChest;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.containers.ContainerBuilderSettings;
import noppes.npcs.containers.ContainerCarpentryBench;
import noppes.npcs.containers.ContainerCustomChest;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.containers.ContainerManageBanks;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.containers.ContainerMerchantAdd;
import noppes.npcs.containers.ContainerNPCBank;
import noppes.npcs.containers.ContainerNPCCompanion;
import noppes.npcs.containers.ContainerNPCDropSetup;
import noppes.npcs.containers.ContainerNPCFollowerHire;
import noppes.npcs.containers.ContainerNPCFollowerSetup;
import noppes.npcs.containers.ContainerNPCInv;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.containers.ContainerNPCTraderSetup;
import noppes.npcs.containers.ContainerNPCTransportSetup;
import noppes.npcs.containers.ContainerNpcItemGiver;
import noppes.npcs.containers.ContainerNpcQuestReward;
import noppes.npcs.containers.ContainerNpcQuestRewardItem;
import noppes.npcs.containers.ContainerNpcQuestTypeItem;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.CustomArmor;
import noppes.npcs.items.CustomBow;
import noppes.npcs.items.CustomFishingRod;
import noppes.npcs.items.CustomShield;
import noppes.npcs.items.CustomTool;
import noppes.npcs.items.CustomWeapon;
import noppes.npcs.items.crafting.NpcShapedRecipes;
import noppes.npcs.items.crafting.NpcShapelessRecipes;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.ObfuscationHelper;

public class CommonProxy implements IGuiHandler {

	public boolean newVersionAvailable;
	public int revision;

	public CommonProxy() {
		this.newVersionAvailable = false;
		this.revision = 4;
	}

	public void checkBlockFiles(ICustomElement customblock) {
		String name = customblock.getCustomName();
		String fileName = Objects.requireNonNull(((Block) customblock).getRegistryName()).getResourcePath();
		File blockstatesDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/blockstates");
		if (!blockstatesDir.exists() && !blockstatesDir.mkdirs()) { return; }

		File blockModelsDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/models/block");
		File itemModelsDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/models/item");
		if (!blockModelsDir.exists() && !blockModelsDir.mkdirs()) { return; }
		if (!itemModelsDir.exists() && !itemModelsDir.mkdirs()) { return; }
		String crEnt = "" + ((char) 10);
		String crTab = "" + ((char) 9);

		File orientable = new File(blockModelsDir, "orientable.json");
		if (!orientable.exists()) {
			String jsonOrientable = "{" + crEnt + crTab + "\"_comment\": \"Orientable Block Model created by default\","
					+ crEnt + crTab + "\"parent\": \"block/cube\"," + crEnt + crTab + "\"display\": {" + crEnt + crTab
					+ crTab + "\"firstperson_righthand\": {" + crEnt + crTab + crTab + crTab
					+ "\"rotation\": [ 0, 135, 0 ]," + crEnt + crTab + crTab + crTab + "\"translation\": [ 0, 0, 0 ],"
					+ crEnt + crTab + crTab + crTab + "\"scale\": [ 0.40, 0.40, 0.40 ]" + crEnt + crTab + crTab + "}"
					+ crEnt + crTab + "}," + crEnt + crTab + "\"textures\": {" + crEnt + crTab + crTab
					+ "\"particle\": \"#particle\"," + crEnt + crTab + crTab + "\"down\": \"#bottom\"," + crEnt + crTab
					+ crTab + "\"up\": \"#top\"," + crEnt + crTab + crTab + "\"north\": \"#front\"," + crEnt + crTab
					+ crTab + "\"east\": \"#left\"," + crEnt + crTab + crTab + "\"south\": \"#back\"," + crEnt + crTab
					+ crTab + "\"west\": \"#right\"" + crEnt + crTab + "}" + crEnt + "}";
			if (saveFile(orientable, jsonOrientable)) {
				LogWriter.debug("Create Orientable Block Model for \"orientable\" block");
			}
		}

		File chest = new File(blockModelsDir, "chest.json");
		if (!chest.exists()) {
			String jsonChest = "{" + crEnt + crTab + "\"_comment\": \"Chest Block Model created by default\"," + crEnt
					+ crTab + "\"elements\": [" + crEnt + crTab + crTab + "{" + crEnt + crTab + crTab + crTab
					+ "\"name\": \"chestLid\"," + crEnt + crTab + crTab + crTab + "\"from\": [1, 0, 1]," + crEnt + crTab
					+ crTab + crTab + "\"to\": [15, 10, 15]," + crEnt + crTab + crTab + crTab + "\"faces\": {" + crEnt
					+ crTab + crTab + crTab + crTab
					+ "\"north\": {\"uv\": [3.5, 8.25, 7, 10.75], \"texture\": \"#chest\"}," + crEnt + crTab + crTab
					+ crTab + crTab + "\"east\": {\"uv\": [0, 8.25, 3.5, 10.75], \"texture\": \"#chest\"}," + crEnt
					+ crTab + crTab + crTab + crTab
					+ "\"south\": {\"uv\": [10.5, 8.25, 14, 10.75], \"texture\": \"#chest\"}," + crEnt + crTab + crTab
					+ crTab + crTab + "\"west\": {\"uv\": [0, 8.25, 3.5, 10.75], \"texture\": \"#chest\"}," + crEnt
					+ crTab + crTab + crTab + crTab + "\"up\": {\"uv\": [3.5, 4.75, 7, 8.25], \"texture\": \"#chest\"},"
					+ crEnt + crTab + crTab + crTab + crTab
					+ "\"down\": {\"uv\": [7, 4.75, 10.5, 8.25], \"texture\": \"#chest\"}" + crEnt + crTab + crTab
					+ crTab + "}" + crEnt + crTab + crTab + "}," + crEnt + crTab + crTab + "{" + crEnt + crTab + crTab
					+ crTab + "\"name\": \"chestBelow\"," + crEnt + crTab + crTab + crTab + "\"from\": [1, 9, 1],"
					+ crEnt + crTab + crTab + crTab + "\"to\": [15, 14, 15]," + crEnt + crTab + crTab + crTab
					+ "\"faces\": {" + crEnt + crTab + crTab + crTab + crTab
					+ "\"north\": {\"uv\": [3.5, 3.5, 7, 4.75], \"texture\": \"#chest\"}," + crEnt + crTab + crTab
					+ crTab + crTab + "\"east\": {\"uv\": [0, 3.5, 3.5, 4.75], \"texture\": \"#chest\"}," + crEnt
					+ crTab + crTab + crTab + crTab
					+ "\"south\": {\"uv\": [10.5, 3.5, 14, 4.75], \"texture\": \"#chest\"}," + crEnt + crTab + crTab
					+ crTab + crTab + "\"west\": {\"uv\": [7, 3.5, 10.5, 4.75], \"texture\": \"#chest\"}," + crEnt
					+ crTab + crTab + crTab + crTab + "\"up\": {\"uv\": [3.5, 0, 7, 3.5], \"texture\": \"#chest\"},"
					+ crEnt + crTab + crTab + crTab + crTab
					+ "\"down\": {\"uv\": [7, 0, 10.5, 3.5], \"texture\": \"#chest\"}" + crEnt + crTab + crTab + crTab
					+ "}" + crEnt + crTab + crTab + "}," + crEnt + crTab + crTab + "{" + crEnt + crTab + crTab + crTab
					+ "\"name\": \"chestKnob\"," + crEnt + crTab + crTab + crTab + "\"from\": [7, 7, 0]," + crEnt
					+ crTab + crTab + crTab + "\"to\": [9, 11, 1]," + crEnt + crTab + crTab + crTab + "\"faces\": {"
					+ crEnt + crTab + crTab + crTab + crTab
					+ "\"north\": {\"uv\": [0.25, 0.25, 0.75, 1.25], \"texture\": \"#chest\"}," + crEnt + crTab + crTab
					+ crTab + crTab + "\"east\": {\"uv\": [1, 0.25, 1.25, 1.25], \"texture\": \"#chest\"}," + crEnt
					+ crTab + crTab + crTab + crTab
					+ "\"south\": {\"uv\": [0.75, 0.25, 1.25, 1.25], \"texture\": \"#chest\"}," + crEnt + crTab + crTab
					+ crTab + crTab + "\"west\": {\"uv\": [0.25, 0.25, 0.5, 1.25], \"texture\": \"#chest\"}," + crEnt
					+ crTab + crTab + crTab + crTab + "\"up\": {\"uv\": [0.5, 0, 1, 0.25], \"texture\": \"#chest\"},"
					+ crEnt + crTab + crTab + crTab + crTab
					+ "\"down\": {\"uv\": [0.5, 0, 1, 0.25], \"texture\": \"#chest\"}" + crEnt + crTab + crTab + crTab
					+ "}" + crEnt + crTab + crTab + "}" + crEnt + crTab + "]," + crEnt + crTab + "\"display\": {"
					+ crEnt + crTab + crTab + "\"thirdperson_righthand\": {" + crEnt + crTab + crTab + crTab
					+ "\"rotation\": [90, 0, 0]," + crEnt + crTab + crTab + crTab + "\"translation\": [0, 0.25, 0],"
					+ crEnt + crTab + crTab + crTab + "\"scale\": [0.35, 0.35, 0.35]" + crEnt + crTab + crTab + "},"
					+ crEnt + crTab + crTab + "\"thirdperson_lefthand\": {" + crEnt + crTab + crTab + crTab
					+ "\"rotation\": [90, 0, 0]," + crEnt + crTab + crTab + crTab + "\"scale\": [0.35, 0.35, 0.35]"
					+ crEnt + crTab + crTab + "}," + crEnt + crTab + crTab + "\"firstperson_righthand\": {" + crEnt
					+ crTab + crTab + crTab + "\"translation\": [3, 0, 0]," + crEnt + crTab + crTab + crTab
					+ "\"scale\": [0.5, 0.5, 0.5]" + crEnt + crTab + crTab + "}," + crEnt + crTab + crTab
					+ "\"firstperson_lefthand\": {" + crEnt + crTab + crTab + crTab + "\"translation\": [3, 0, 0],"
					+ crEnt + crTab + crTab + crTab + "\"scale\": [0.5, 0.5, 0.5]" + crEnt + crTab + crTab + "},"
					+ crEnt + crTab + crTab + "\"ground\": {" + crEnt + crTab + crTab + crTab
					+ "\"translation\": [0, -1, 0]," + crEnt + crTab + crTab + crTab + "\"scale\": [0.35, 0.35, 0.35]"
					+ crEnt + crTab + crTab + "}," + crEnt + crTab + crTab + "\"gui\": {" + crEnt + crTab + crTab
					+ crTab + "\"rotation\": [30, -135, 0]," + crEnt + crTab + crTab + crTab
					+ "\"translation\": [0, 0.25, 0]," + crEnt + crTab + crTab + crTab + "\"scale\": [0.65, 0.65, 0.65]"
					+ crEnt + crTab + crTab + "}," + crEnt + crTab + crTab + "\"fixed\": {" + crEnt + crTab + crTab
					+ crTab + "\"scale\": [0.55, 0.55, 0.55]" + crEnt + crTab + crTab + "}" + crEnt + crTab + "}"
					+ crEnt + "}";
			if (saveFile(chest, jsonChest)) {
				LogWriter.debug("Create Chest Block Model for \"custom chest\" block");
			}
		}

		File blockstate = new File(blockstatesDir, fileName.toLowerCase() + ".json");
		if (!blockstate.exists()) {
			StringBuilder jsonState = new StringBuilder();
			if (customblock instanceof CustomLiquid) {
				jsonState = new StringBuilder("{" + crEnt + crTab + "\"_comment\": \"Custom Block Fluid created by default\"," + crEnt
						+ crTab + "\"forge_marker\": 1," + crEnt + crTab + "\"defaults\": {" + crEnt + crTab + crTab
						+ "\"textures\": {" + crEnt + crTab + crTab + crTab + "\"particle\": \"" + CustomNpcs.MODID
						+ ":fluids/" + fileName + "_flow\"," + crEnt + crTab + crTab + crTab + "\"all\": \""
						+ CustomNpcs.MODID + ":fluids/" + fileName + "_flow\"" + crEnt + crTab + crTab + "}," + crEnt
						+ crTab + crTab + "\"model\": \"forge:fluid\"," + crEnt + crTab + crTab
						+ "\"custom\": { \"fluid\": \"" + fileName + "\" }," + crEnt + crTab + crTab
						+ "\"uvlock\": false" + crEnt + crTab + "}," + crEnt + crTab + "\"variants\": {" + crEnt + crTab
						+ crTab + "\"normal\": [{ }]," + crEnt + crTab + crTab + "\"inventory\": [{ }]," + crEnt + crTab
						+ crTab + "\"level\": {" + crEnt + crTab + crTab + crTab + "\"0\": { }," + crEnt + crTab + crTab
						+ crTab + "\"1\": { }," + crEnt + crTab + crTab + crTab + "\"2\": { }," + crEnt + crTab + crTab
						+ crTab + "\"3\": { }," + crEnt + crTab + crTab + crTab + "\"4\": { }," + crEnt + crTab + crTab
						+ crTab + "\"5\": { }," + crEnt + crTab + crTab + crTab + "\"6\": { }," + crEnt + crTab + crTab
						+ crTab + "\"7\": { }," + crEnt + crTab + crTab + crTab + "\"8\": { }," + crEnt + crTab + crTab
						+ crTab + "\"9\": { }," + crEnt + crTab + crTab + crTab + "\"10\": { }," + crEnt + crTab + crTab
						+ crTab + "\"11\": { }," + crEnt + crTab + crTab + crTab + "\"12\": { }," + crEnt + crTab
						+ crTab + crTab + "\"13\": { }," + crEnt + crTab + crTab + crTab + "\"14\": { }," + crEnt
						+ crTab + crTab + crTab + "\"15\": { }" + crEnt + crTab + crTab + "}" + crEnt + crTab + "}"
						+ crEnt + "}");
			} else if (customblock instanceof CustomBlockStairs) {
				jsonState = new StringBuilder("{" + crEnt + crTab + "\"_comment\": \"Custom Block Stairs created by default\"," + crEnt
						+ crTab + "\"variants\": {" + crEnt + crTab + crTab
						+ "\"facing=east,half=bottom,shape=straight\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "\" }," + crEnt + crTab + crTab
						+ "\"facing=west,half=bottom,shape=straight\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "\", \"y\": 180, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=south,half=bottom,shape=straight\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "\", \"y\": 90, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=north,half=bottom,shape=straight\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "\", \"y\": 270, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=east,half=bottom,shape=outer_right\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_outer\" }," + crEnt + crTab + crTab
						+ "\"facing=west,half=bottom,shape=outer_right\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_outer\", \"y\": 180, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=south,half=bottom,shape=outer_right\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_outer\", \"y\": 90, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=north,half=bottom,shape=outer_right\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_outer\", \"y\": 270, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=east,half=bottom,shape=outer_left\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_outer\", \"y\": 270, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=west,half=bottom,shape=outer_left\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_outer\", \"y\": 90, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=south,half=bottom,shape=outer_left\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_outer\" }," + crEnt + crTab + crTab
						+ "\"facing=north,half=bottom,shape=outer_left\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_outer\", \"y\": 180, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=east,half=bottom,shape=inner_right\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_inner\" }," + crEnt + crTab + crTab
						+ "\"facing=west,half=bottom,shape=inner_right\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_inner\", \"y\": 180, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=south,half=bottom,shape=inner_right\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_inner\", \"y\": 90, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=north,half=bottom,shape=inner_right\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_inner\", \"y\": 270, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=east,half=bottom,shape=inner_left\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_inner\", \"y\": 270, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=west,half=bottom,shape=inner_left\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_inner\", \"y\": 90, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=south,half=bottom,shape=inner_left\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_inner\" }," + crEnt + crTab + crTab
						+ "\"facing=north,half=bottom,shape=inner_left\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_inner\", \"y\": 180, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=east,half=top,shape=straight\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "\", \"x\": 180, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=west,half=top,shape=straight\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "\", \"x\": 180, \"y\": 180, \"uvlock\": true }," + crEnt + crTab
						+ crTab + "\"facing=south,half=top,shape=straight\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "\", \"x\": 180, \"y\": 90, \"uvlock\": true }," + crEnt + crTab
						+ crTab + "\"facing=north,half=top,shape=straight\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "\", \"x\": 180, \"y\": 270, \"uvlock\": true }," + crEnt + crTab
						+ crTab + "\"facing=east,half=top,shape=outer_right\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_outer\", \"x\": 180, \"y\": 90, \"uvlock\": true }," + crEnt
						+ crTab + crTab + "\"facing=west,half=top,shape=outer_right\": { \"model\": \""
						+ CustomNpcs.MODID + ":" + fileName.toLowerCase()
						+ "_outer\", \"x\": 180, \"y\": 270, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=south,half=top,shape=outer_right\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_outer\", \"x\": 180, \"y\": 180, \"uvlock\": true }," + crEnt
						+ crTab + crTab + "\"facing=north,half=top,shape=outer_right\": { \"model\": \""
						+ CustomNpcs.MODID + ":" + fileName.toLowerCase() + "_outer\", \"x\": 180, \"uvlock\": true },"
						+ crEnt + crTab + crTab + "\"facing=east,half=top,shape=outer_left\": { \"model\": \""
						+ CustomNpcs.MODID + ":" + fileName.toLowerCase() + "_outer\", \"x\": 180, \"uvlock\": true },"
						+ crEnt + crTab + crTab + "\"facing=west,half=top,shape=outer_left\": { \"model\": \""
						+ CustomNpcs.MODID + ":" + fileName.toLowerCase()
						+ "_outer\", \"x\": 180, \"y\": 180, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=south,half=top,shape=outer_left\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_outer\", \"x\": 180, \"y\": 90, \"uvlock\": true }," + crEnt
						+ crTab + crTab + "\"facing=north,half=top,shape=outer_left\": { \"model\": \""
						+ CustomNpcs.MODID + ":" + fileName.toLowerCase()
						+ "_outer\", \"x\": 180, \"y\": 270, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=east,half=top,shape=inner_right\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_inner\", \"x\": 180, \"y\": 90, \"uvlock\": true }," + crEnt
						+ crTab + crTab + "\"facing=west,half=top,shape=inner_right\": { \"model\": \""
						+ CustomNpcs.MODID + ":" + fileName.toLowerCase()
						+ "_inner\", \"x\": 180, \"y\": 270, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=south,half=top,shape=inner_right\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_inner\", \"x\": 180, \"y\": 180, \"uvlock\": true }," + crEnt
						+ crTab + crTab + "\"facing=north,half=top,shape=inner_right\": { \"model\": \""
						+ CustomNpcs.MODID + ":" + fileName.toLowerCase() + "_inner\", \"x\": 180, \"uvlock\": true },"
						+ crEnt + crTab + crTab + "\"facing=east,half=top,shape=inner_left\": { \"model\": \""
						+ CustomNpcs.MODID + ":" + fileName.toLowerCase() + "_inner\", \"x\": 180, \"uvlock\": true },"
						+ crEnt + crTab + crTab + "\"facing=west,half=top,shape=inner_left\": { \"model\": \""
						+ CustomNpcs.MODID + ":" + fileName.toLowerCase()
						+ "_inner\", \"x\": 180, \"y\": 180, \"uvlock\": true }," + crEnt + crTab + crTab
						+ "\"facing=south,half=top,shape=inner_left\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_inner\", \"x\": 180, \"y\": 90, \"uvlock\": true }," + crEnt
						+ crTab + crTab + "\"facing=north,half=top,shape=inner_left\": { \"model\": \""
						+ CustomNpcs.MODID + ":" + fileName.toLowerCase()
						+ "_inner\", \"x\": 180, \"y\": 270, \"uvlock\": true }" + crEnt + crTab + "}" + crEnt + "}");
			} else if (customblock instanceof CustomBlockSlab) {
				if (customblock instanceof CustomBlockSlab.CustomBlockSlabSingle) {
					jsonState = new StringBuilder("{" + crEnt + crTab + "\"_comment\": \"Custom Block Slab created by default\"," + crEnt
							+ crTab + "\"variants\": {" + crEnt + crTab + crTab
							+ "\"half=bottom,type=normal\": { \"model\": \"" + CustomNpcs.MODID + ":bottom_"
							+ fileName.toLowerCase() + "\" }," + crEnt + crTab + crTab
							+ "\"half=top,type=normal\": { \"model\": \"" + CustomNpcs.MODID + ":upper_"
							+ fileName.toLowerCase() + "\" }," + crEnt + crTab + crTab
							+ "\"inventory\": { \"model\": \"" + CustomNpcs.MODID + ":bottom_" + fileName.toLowerCase()
							+ "\" }" + crEnt + crTab + "}" + crEnt + "}");
				} else {
					jsonState = new StringBuilder("{" + crEnt + crTab + "\"_comment\": \"Custom Block Double Slab created by default\","
							+ crEnt + crTab + "\"variants\": {" + crEnt + crTab + crTab
							+ "\"type=normal\":  { \"model\": \"" + CustomNpcs.MODID + ":" + fileName.toLowerCase()
							+ "\" }," + crEnt + crTab + crTab + "\"type=all\":  { \"model\": \"" + CustomNpcs.MODID
							+ ":" + fileName.toLowerCase() + "_top\" }," + crEnt + crTab + crTab
							+ "\"inventory\":  { \"model\": \"" + CustomNpcs.MODID + ":" + fileName.toLowerCase()
							+ "\" }" + crEnt + crTab + "}" + crEnt + "}");
				}
			} else if (customblock instanceof CustomBlockPortal) {
				jsonState = new StringBuilder("{" + crEnt + crTab + "\"_comment\": \"Custom Block Portal created by default\"," + crEnt
						+ crTab + "\"variants\": {" + crEnt + crTab + crTab + "\"type=0\": { \"model\": \""
						+ CustomNpcs.MODID + ":" + fileName.toLowerCase() + "\" }," + crEnt + crTab + crTab
						+ "\"type=1\": { \"model\": \"" + CustomNpcs.MODID + ":" + fileName.toLowerCase() + "\" },"
						+ crEnt + crTab + crTab + "\"type=2\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "\" }," + crEnt + crTab + crTab + "\"normal\": { \"model\": \""
						+ CustomNpcs.MODID + ":" + fileName.toLowerCase() + "\" }," + crEnt + crTab + crTab
						+ "\"inventory\": { \"model\": \"" + CustomNpcs.MODID + ":" + fileName.toLowerCase() + "\" }"
						+ crEnt + crTab + "}" + crEnt + "}");
			} else if (customblock instanceof CustomBlock && ((CustomBlock) customblock).hasProperty()) {
				NBTTagCompound data = ((CustomBlock) customblock).nbtData.getCompoundTag("Property");
				jsonState = new StringBuilder("{" + crEnt + crTab + "\"_comment\": \"Custom "
						+ (data.getByte("Type") == (byte) 1 ? "Byte"
						: data.getByte("Type") == (byte) 3 ? "Integer" : "Facing")
						+ " Block created by default\"," + crEnt + crTab + "\"variants\": {" + crEnt + crTab + crTab
						+ "\"normal\": { \"model\": \"" + CustomNpcs.MODID + ":" + fileName.toLowerCase() + "\" }, "
						+ crEnt + crTab + crTab + "\"inventory\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "\" }, " + crEnt);
				if (data.getByte("Type") == (byte) 1) {
					jsonState.append(crTab).append(crTab).append("\"").append(data.getString("Name")).append("=true\": { \"model\": \"").append(CustomNpcs.MODID).append(":").append(fileName.toLowerCase()).append("_true\"");
					jsonState.append(crTab).append(crTab).append("\"").append(data.getString("Name")).append("=false\": { \"model\": \"").append(CustomNpcs.MODID).append(":").append(fileName.toLowerCase()).append("_false\"");
				} else if (data.getByte("Type") == (byte) 3) {
					for (int i = data.getInteger("Min"); i <= data.getInteger("Max"); i++) {
						jsonState.append(crTab).append(crTab).append("\"").append(data.getString("Name")).append("=").append(i).append("\": { \"model\": \"").append(CustomNpcs.MODID).append(":").append(fileName.toLowerCase()).append("_").append(i).append("\"");
					}
				} else if (data.getByte("Type") == (byte) 4) {
					for (EnumFacing ef : EnumFacing.VALUES) {
						if (ef == EnumFacing.DOWN || ef == EnumFacing.UP) {
							continue;
						}
						jsonState.append(crTab).append(crTab).append("\"").append(data.getString("Name")).append("=").append(ef.getName2()).append("\": { \"model\": \"").append(CustomNpcs.MODID).append(":").append(fileName.toLowerCase()).append("\"");
						if (ef == EnumFacing.SOUTH) {
							jsonState.append(",\"y\": 180");
						} else if (ef == EnumFacing.WEST) {
							jsonState.append(",\"y\": 270");
						} else if (ef == EnumFacing.EAST) {
							jsonState.append(",\"y\": 90");
						}
						jsonState.append(" },").append(crEnt);
					}
				}
				jsonState = new StringBuilder(jsonState.substring(0, jsonState.length() - 2) + crEnt + crTab + "}" + crEnt + "}");
			} else if (customblock instanceof CustomDoor) {
				jsonState = new StringBuilder("{" + crEnt + crTab + "\"_comment\": \"Custom Block Door created by default\"," + crEnt
						+ crTab + "\"variants\": {" + crEnt + crTab + crTab
						+ "\"facing=east,half=lower,hinge=left,open=false\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom\" }," + crEnt + crTab + crTab
						+ "\"facing=south,half=lower,hinge=left,open=false\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom\", \"y\": 90 }," + crEnt + crTab + crTab
						+ "\"facing=west,half=lower,hinge=left,open=false\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom\", \"y\": 180 }," + crEnt + crTab + crTab
						+ "\"facing=north,half=lower,hinge=left,open=false\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom\", \"y\": 270 }," + crEnt + crTab + crTab
						+ "\"facing=east,half=lower,hinge=right,open=false\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom_rh\" }," + crEnt + crTab + crTab
						+ "\"facing=south,half=lower,hinge=right,open=false\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom_rh\", \"y\": 90 }," + crEnt + crTab + crTab
						+ "\"facing=west,half=lower,hinge=right,open=false\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom_rh\", \"y\": 180 }," + crEnt + crTab + crTab
						+ "\"facing=north,half=lower,hinge=right,open=false\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom_rh\", \"y\": 270 }," + crEnt + crTab + crTab
						+ "\"facing=east,half=lower,hinge=left,open=true\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom_rh\", \"y\": 90 }," + crEnt + crTab + crTab
						+ "\"facing=south,half=lower,hinge=left,open=true\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom_rh\", \"y\": 180 }," + crEnt + crTab + crTab
						+ "\"facing=west,half=lower,hinge=left,open=true\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom_rh\", \"y\": 270 }," + crEnt + crTab + crTab
						+ "\"facing=north,half=lower,hinge=left,open=true\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom_rh\" }," + crEnt + crTab + crTab
						+ "\"facing=east,half=lower,hinge=right,open=true\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom\", \"y\": 270 }," + crEnt + crTab + crTab
						+ "\"facing=south,half=lower,hinge=right,open=true\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom\" }," + crEnt + crTab + crTab
						+ "\"facing=west,half=lower,hinge=right,open=true\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom\", \"y\": 90 }," + crEnt + crTab + crTab
						+ "\"facing=north,half=lower,hinge=right,open=true\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_bottom\", \"y\": 180 }," + crEnt + crTab + crTab
						+ "\"facing=east,half=upper,hinge=left,open=false\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top\" }," + crEnt + crTab + crTab
						+ "\"facing=south,half=upper,hinge=left,open=false\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top\", \"y\": 90 }," + crEnt + crTab + crTab
						+ "\"facing=west,half=upper,hinge=left,open=false\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top\", \"y\": 180 }," + crEnt + crTab + crTab
						+ "\"facing=north,half=upper,hinge=left,open=false\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top\", \"y\": 270 }," + crEnt + crTab + crTab
						+ "\"facing=east,half=upper,hinge=right,open=false\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top_rh\" }," + crEnt + crTab + crTab
						+ "\"facing=south,half=upper,hinge=right,open=false\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top_rh\", \"y\": 90 }," + crEnt + crTab + crTab
						+ "\"facing=west,half=upper,hinge=right,open=false\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top_rh\", \"y\": 180 }," + crEnt + crTab + crTab
						+ "\"facing=north,half=upper,hinge=right,open=false\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top_rh\", \"y\": 270 }," + crEnt + crTab + crTab
						+ "\"facing=east,half=upper,hinge=left,open=true\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top_rh\", \"y\": 90 }," + crEnt + crTab + crTab
						+ "\"facing=south,half=upper,hinge=left,open=true\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top_rh\", \"y\": 180 }," + crEnt + crTab + crTab
						+ "\"facing=west,half=upper,hinge=left,open=true\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top_rh\", \"y\": 270 }," + crEnt + crTab + crTab
						+ "\"facing=north,half=upper,hinge=left,open=true\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top_rh\" }," + crEnt + crTab + crTab
						+ "\"facing=east,half=upper,hinge=right,open=true\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top\", \"y\": 270 }," + crEnt + crTab + crTab
						+ "\"facing=south,half=upper,hinge=right,open=true\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top\" }," + crEnt + crTab + crTab
						+ "\"facing=west,half=upper,hinge=right,open=true\":  { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top\", \"y\": 90 }," + crEnt + crTab + crTab
						+ "\"facing=north,half=upper,hinge=right,open=true\": { \"model\": \"" + CustomNpcs.MODID + ":"
						+ fileName.toLowerCase() + "_top\", \"y\": 180 }" + crEnt + crTab + "}" + crEnt + "}");
			} else if (customblock instanceof CustomChest) {
				boolean type = ((CustomChest) customblock).isChest;
				if (type) {
					jsonState = new StringBuilder("{" + crEnt + crTab + "\"_comment\": \"Custom Chest Block created by default\"," + crEnt
							+ crTab + "\"variants\": {" + crEnt + crTab + crTab + "\"normal\": { \"model\": \""
							+ CustomNpcs.MODID + ":" + fileName.toLowerCase() + "\" }, " + crEnt + crTab + crTab
							+ "\"inventory\": { \"model\": \"" + CustomNpcs.MODID + ":" + fileName.toLowerCase()
							+ "\" }, " + crEnt + crTab + crTab + "\"facing=north\": { \"model\": \"" + CustomNpcs.MODID
							+ ":" + fileName.toLowerCase() + "\" }," + crEnt + crTab + crTab
							+ "\"facing=south\": { \"model\": \"" + CustomNpcs.MODID + ":" + fileName.toLowerCase()
							+ "\",\"y\": 180 }," + crEnt + crTab + crTab + "\"facing=west\": { \"model\": \""
							+ CustomNpcs.MODID + ":" + fileName.toLowerCase() + "\",\"y\": 270 }," + crEnt + crTab
							+ crTab + "\"facing=east\": { \"model\": \"" + CustomNpcs.MODID + ":"
							+ fileName.toLowerCase() + "\",\"y\": 90 }" + crEnt + crEnt + crTab + "}" + crEnt + "}");
				} else {
					jsonState = new StringBuilder("{" + crEnt + crTab + "\"_comment\": \"Custom Container Block created by default\","
							+ crEnt + crTab + "\"forge_marker\": 1," + crEnt + crTab + "\"defaults\": {" + crEnt + crTab
							+ crTab + "\"model\": \"" + CustomNpcs.MODID + ":obj/" + fileName.toLowerCase() + ".obj\" "
							+ crEnt + crTab + "}," + crEnt + crTab + "\"variants\": {" + crEnt + crTab + crTab
							+ "\"inventory\":[{" + crEnt + crTab + crTab + crTab + "\"transform\": {" + crEnt + crTab
							+ crTab + crTab + crTab + "\"scale\": 0.5" + crEnt + crTab + crTab + crTab + "}," + crEnt
							+ crTab + crTab + crTab + "\"firstperson_lefthand\": {" + crEnt + crTab + crTab + crTab
							+ crTab + "\"scale\": 0.5" + crEnt + crTab + crTab + crTab + "}," + crEnt + crTab + crTab
							+ crTab + "\"thirdperson\": {" + crEnt + crTab + crTab + crTab + crTab + "\"scale\": 0.5"
							+ crEnt + crTab + crTab + crTab + "}," + crEnt + crTab + crTab + crTab
							+ "\"thirdperson_lefthand\": {" + crEnt + crTab + crTab + crTab + crTab + "\"scale\": 0.5"
							+ crEnt + crTab + crTab + crTab + "}," + crEnt + crTab + crTab + crTab + "\"gui\": {"
							+ crEnt + crTab + crTab + crTab + crTab + "\"scale\": 0.55," + crEnt + crTab + crTab + crTab
							+ crTab + "\"rotation\": [ { \"x\": 30 }, { \"y\": 45 } ]" + crEnt + crTab + crTab + crTab
							+ "}," + crEnt + crTab + crTab + crTab + "\"ground\": {" + crEnt + crTab + crTab + crTab
							+ crTab + "\"scale\": 0.3" + crEnt + crTab + crTab + crTab + "}," + crEnt + crTab + crTab
							+ crTab + "\"head\": {" + crEnt + crTab + crTab + crTab + crTab + "\"scale\": 0.75" + crEnt
							+ crTab + crTab + crTab + "}," + crEnt + crTab + crTab + crTab + "\"fixed\": {" + crEnt
							+ crTab + crTab + crTab + crTab + "\"scale\": 0.5" + crEnt + crTab + crTab + crTab + "}"
							+ crEnt + crTab + crTab + "}]," + crEnt + crTab + crTab + "\"normal\": [{}]," + crEnt
							+ crTab + crTab + "\"facing=north\": { \"model\": \"" + CustomNpcs.MODID + ":obj/"
							+ fileName.toLowerCase() + ".obj\" }," + crEnt + crTab + crTab
							+ "\"facing=south\": { \"model\": \"" + CustomNpcs.MODID + ":obj/" + fileName.toLowerCase()
							+ ".obj\",\"y\": 180 }," + crEnt + crTab + crTab + "\"facing=west\": { \"model\": \""
							+ CustomNpcs.MODID + ":obj/" + fileName.toLowerCase() + ".obj\",\"y\": 270 }," + crEnt
							+ crTab + crTab + "\"facing=east\": { \"model\": \"" + CustomNpcs.MODID + ":obj/"
							+ fileName.toLowerCase() + ".obj\",\"y\": 90 }" + crEnt + crTab + "}" + crEnt + "}");
				}
			}
			if (jsonState.length() == 0) {
				jsonState = new StringBuilder("{" + crEnt + crTab + "\"_comment\": \"Custom Block created by default\"," + crEnt + crTab
						+ "\"variants\": {" + crEnt + crTab + crTab + "\"normal\": { \"model\": \"" + CustomNpcs.MODID
						+ ":" + fileName.toLowerCase() + "\" }," + crEnt + crTab + crTab
						+ "\"inventory\": { \"model\": \"" + CustomNpcs.MODID + ":" + fileName.toLowerCase() + "\" }"
						+ crEnt + crTab + "}" + crEnt + "}");

			}
			if (saveFile(blockstate, jsonState.toString())) {
				LogWriter.debug("Create Default Blockstate for \"" + fileName.toLowerCase() + "\" block");
			}
		}

		File blockModel = new File(blockModelsDir, fileName.toLowerCase() + ".json");
		if (customblock instanceof CustomBlockSlab.CustomBlockSlabSingle) {
			blockModel = new File(blockModelsDir, "bottom_" + fileName.toLowerCase() + ".json");
		}
		if (!blockModel.exists()) {
			String jsonModel = "";
			if (customblock instanceof CustomBlockStairs) {
				jsonModel = "{" + crEnt + crTab + "\"_comment\": \"Custom Stairs Block created by default\"," + crEnt
						+ crTab + "\"parent\": \"block/stairs\"," + crEnt + crTab + "\"textures\": {" + crEnt + crTab
						+ crTab + "\"top\": \"" + CustomNpcs.MODID + ":blocks/" + fileName.toLowerCase() + "_top\","
						+ crEnt + crTab + crTab + "\"bottom\": \"" + CustomNpcs.MODID + ":blocks/"
						+ fileName.toLowerCase() + "_bottom\"," + crEnt + crTab + crTab + "\"side\": \""
						+ CustomNpcs.MODID + ":blocks/" + fileName.toLowerCase() + "_side\"" + crEnt + crTab + "}"
						+ crEnt + "}";
				if (saveFile(blockModel, jsonModel)) {
					LogWriter.debug("Create Default Stairs Block Model for \"" + fileName.toLowerCase() + "\" block");
				}
				jsonModel = jsonModel.replace("block/stairs", "block/inner_stairs");
				if (saveFile(new File(blockModelsDir, fileName.toLowerCase() + "_inner.json"), jsonModel)) {
					LogWriter.debug(
							"Create Default Inner Stairs Block Model for \"" + fileName.toLowerCase() + "\" block");
				}
				jsonModel = jsonModel.replace("block/inner_stairs", "block/outer_stairs");
				if (saveFile(new File(blockModelsDir, fileName.toLowerCase() + "_outer.json"), jsonModel)) {
					LogWriter.debug(
							"Create Default Outer Stairs Block Model for \"" + fileName.toLowerCase() + "\" block");
				}
			} else if (customblock instanceof CustomBlockSlab) {
				if (customblock instanceof CustomBlockSlab.CustomBlockSlabSingle) {
					jsonModel = "{" + crEnt + crTab + "\"_comment\": \"Custom Slab Simple Block created by default\","
							+ crEnt + crTab + "\"parent\": \"block/half_slab\"," + crEnt + crTab + "\"textures\": {"
							+ crEnt + crTab + crTab + "\"top\": \"" + CustomNpcs.MODID + ":blocks/"
							+ fileName.toLowerCase() + "_top\"," + crEnt + crTab + crTab + "\"bottom\": \""
							+ CustomNpcs.MODID + ":blocks/" + fileName.toLowerCase() + "_bottom\"," + crEnt + crTab
							+ crTab + "\"side\": \"" + CustomNpcs.MODID + ":blocks/" + fileName.toLowerCase()
							+ "_side\"" + crEnt + crTab + "}" + crEnt + "}";
					saveFile(blockModel, jsonModel);
					jsonModel = jsonModel.replace("block/half_slab", "block/upper_slab");
					if (saveFile(new File(blockModelsDir, "upper_" + fileName.toLowerCase() + ".json"),
							jsonModel)) {
						LogWriter.debug(
								"Create Default Slab Simple Block Model for \"" + fileName.toLowerCase() + "\" block");
					}
				}
				if (customblock instanceof CustomBlockSlab.CustomBlockSlabDouble) {
					jsonModel = "{" + crEnt + crTab + "\"_comment\": \"Custom Slab Double Block created by default\","
							+ crEnt + crTab + "\"parent\": \"block/cube_column\"," + crEnt + crTab + "\"textures\": {"
							+ crEnt + crTab + crTab + "\"end\": \"" + CustomNpcs.MODID + ":blocks/"
							+ fileName.toLowerCase().replace("double_", "") + "_top\"," + crEnt + crTab + crTab
							+ "\"side\": \"" + CustomNpcs.MODID + ":blocks/"
							+ fileName.toLowerCase().replace("double_", "") + "_side\"" + crEnt + crTab + "}" + crEnt
							+ "}";
					saveFile(blockModel, jsonModel);

					jsonModel = "{" + crEnt + crTab + "\"_comment\": \"Custom Slab Double Block created by default\","
							+ crEnt + crTab + "\"parent\": \"block/cube_all\"," + crEnt + crTab + "\"textures\": {"
							+ crEnt + crTab + crTab + "\"all\": \"" + CustomNpcs.MODID + ":blocks/"
							+ fileName.toLowerCase().replace("double_", "") + "_top\"" + crEnt + crTab + "}" + crEnt
							+ "}";
					if (saveFile(new File(blockModelsDir, fileName.toLowerCase() + "_top.json"), jsonModel)) {
						LogWriter.debug("Create Default Slab Blocks Model for \"" + fileName.toLowerCase() + "\" block");
					}
				}
			} else if (customblock instanceof CustomDoor) {
				jsonModel = "{" + crEnt + crTab + "\"_comment\": \"Custom Door Block created by default\"," + crEnt
						+ crTab + "\"parent\": \"block/door_bottom\"," + crEnt + crTab + "\"textures\": {" + crEnt
						+ crTab + crTab + "\"bottom\": \"" + CustomNpcs.MODID + ":blocks/" + fileName.toLowerCase()
						+ "_lower\"," + crEnt + crTab + crTab + "\"top\": \"" + CustomNpcs.MODID + ":blocks/"
						+ fileName.toLowerCase() + "_upper\"" + crEnt + crTab + "}" + crEnt + "}";
				if (saveFile(blockModel, jsonModel)) {
					LogWriter.debug("Create Default Door Blocks Model for \"" + fileName.toLowerCase() + "\" block");
				}
				saveFile(new File(blockModelsDir, fileName.toLowerCase() + "_bottom.json"), jsonModel);
				saveFile(new File(blockModelsDir, fileName.toLowerCase() + "_bottom_rh.json"), jsonModel.replace("block/door_bottom", "block/door_bottom_rh"));
				saveFile(new File(blockModelsDir, fileName.toLowerCase() + "_top.json"), jsonModel.replace("block/door_bottom", "block/door_top"));
				saveFile(new File(blockModelsDir, fileName.toLowerCase() + "_top_rh.json"), jsonModel.replace("block/door_bottom", "block/door_top_rh"));
			} else if (customblock instanceof CustomChest) {
				boolean type = ((CustomChest) customblock).isChest;
				if (type) {
					jsonModel = "{" + crEnt + crTab + "\"_comment\": \"Custom Chest Block created by default\"," + crEnt
							+ crTab + "\"parent\": \"" + CustomNpcs.MODID + ":block/chest\"," + crEnt + crTab
							+ "\"textures\": {" + crEnt + crTab + crTab + "\"chest\": \"" + CustomNpcs.MODID
							+ ":entity/chest/" + fileName.toLowerCase() + "\"," + crEnt + crTab + crTab
							+ "\"particle\": \"" + CustomNpcs.MODID + ":entity/chest/" + fileName.toLowerCase() + "\""
							+ crEnt + crTab + "}" + crEnt + "}";
					if (saveFile(blockModel, jsonModel)) {
						LogWriter.debug(
								"Create Default Chest Blocks Model for \"" + fileName.toLowerCase() + "\" block");
					}
				} else {
					blockModel = new File(blockModelsDir, "obj/" + fileName.toLowerCase() + ".mtl");
					jsonModel = "newmtl material" + crEnt + "Kd 1.000000 1.000000 1.000000" + crEnt + "d 1.000000"
							+ crEnt + "map_Kd " + CustomNpcs.MODID + ":blocks/" + fileName.toLowerCase() + "_side"
							+ crEnt + crEnt + "newmtl top" + crEnt + "Kd 1.000000 1.000000 1.000000" + crEnt
							+ "d 1.000000" + crEnt + "map_Kd " + CustomNpcs.MODID + ":blocks/" + fileName.toLowerCase()
							+ "_top";
					saveFile(blockModel, jsonModel);
					blockModel = new File(blockModelsDir, "obj/" + fileName.toLowerCase() + ".obj");
					jsonModel = "mtllib " + fileName.toLowerCase() + ".mtl" + crEnt + "o body" + crEnt
							+ "v 0.062500 0.000000 0.645833" + crEnt + "v 0.062500 1.000000 0.645833" + crEnt
							+ "v 0.354167 0.000000 0.062500" + crEnt + "v 0.354167 1.000000 0.062500" + crEnt
							+ "v 0.645833 0.000000 0.937500" + crEnt + "v 0.645833 1.000000 0.937500" + crEnt
							+ "v 0.937500 0.000000 0.354167" + crEnt + "v 0.937500 1.000000 0.354167" + crEnt
							+ "v 0.062500 0.000000 0.354167" + crEnt + "v 0.062500 1.000000 0.354167" + crEnt
							+ "v 0.937500 0.000000 0.645833" + crEnt + "v 0.937500 1.000000 0.645833" + crEnt
							+ "v 0.645833 0.000000 0.062500" + crEnt + "v 0.645833 1.000000 0.062500" + crEnt
							+ "v 0.354167 0.000000 0.937500" + crEnt + "v 0.354167 1.000000 0.937500" + crEnt
							+ "v 0.791667 1.000000 0.791667" + crEnt + "v 0.208333 1.000000 0.791667" + crEnt
							+ "v 0.791667 0.000000 0.791667" + crEnt + "v 0.208333 0.000000 0.791667" + crEnt
							+ "v 0.208333 1.000000 0.208333" + crEnt + "v 0.791667 1.000000 0.208333" + crEnt
							+ "v 0.208333 0.000000 0.208333" + crEnt + "v 0.791667 0.000000 0.208333" + crEnt
							+ "vt 1.000000 1.000000" + crEnt + "vt 0.000000 1.000000" + crEnt + "vt 0.000000 0.700000"
							+ crEnt + "vt 1.000000 0.700000" + crEnt + "vt 0.000000 0.000000" + crEnt
							+ "vt 1.000000 0.000000" + crEnt + "vt 1.000000 0.300000" + crEnt + "vt 0.000000 0.300000"
							+ crEnt + "vt 1.000000 1.000000" + crEnt + "vt 0.000000 1.000000" + crEnt
							+ "vt 0.000000 0.700000" + crEnt + "vt 1.000000 0.700000" + crEnt + "vt 1.000000 1.000000"
							+ crEnt + "vt 0.000000 1.000000" + crEnt + "vt 0.000000 0.700000" + crEnt
							+ "vt 1.000000 0.700000" + crEnt + "vt 1.000000 0.700000" + crEnt + "vt 0.000000 0.700000"
							+ crEnt + "vt 0.000000 0.300000" + crEnt + "vt 1.000000 0.300000" + crEnt
							+ "vt 0.000000 0.300000" + crEnt + "vt 1.000000 0.300000" + crEnt + "vt 0.000000 0.300000"
							+ crEnt + "vt 1.000000 0.300000" + crEnt + "vt 0.000000 0.000000" + crEnt
							+ "vt 1.000000 0.000000" + crEnt + "vt 1.000000 1.000000" + crEnt + "vt 0.000000 1.000000"
							+ crEnt + "vt 0.000000 0.000000" + crEnt + "vt 1.000000 0.000000" + crEnt
							+ "vt 0.000000 0.000000" + crEnt + "vt 1.000000 0.000000" + crEnt
							+ "vn -0.7071 -0.0000 -0.7071" + crEnt + "vn 0.7071 -0.0000 -0.7071" + crEnt
							+ "vn 0.7071 -0.0000 0.7071" + crEnt + "vn -0.7071 0.0000 0.7071" + crEnt
							+ "vn 1.0000 -0.0000 0.0000" + crEnt + "vn -1.0000 0.0000 0.0000" + crEnt
							+ "vn 0.0000 0.0000 1.0000" + crEnt + "vn 0.0000 0.0000 -1.0000" + crEnt + "usemtl material"
							+ crEnt + "f 23/1/1 21/2/1 4/3/1 3/4/1" + crEnt + "f 24/5/2 22/6/2 8/7/2 7/8/2" + crEnt
							+ "f 19/9/3 17/10/3 6/11/3 5/12/3" + crEnt + "f 20/13/4 18/14/4 2/15/4 1/16/4" + crEnt
							+ "f 7/8/5 8/7/5 12/17/5 11/18/5" + crEnt + "f 1/16/6 2/15/6 10/19/6 9/20/6" + crEnt
							+ "f 5/12/7 6/11/7 16/21/7 15/22/7" + crEnt + "f 3/4/8 4/3/8 14/23/8 13/24/8" + crEnt
							+ "f 15/22/4 16/21/4 18/25/4 20/26/4" + crEnt + "f 11/18/3 12/17/3 17/27/3 19/28/3" + crEnt
							+ "f 13/24/2 14/23/2 22/29/2 24/30/2" + crEnt + "f 9/20/1 10/19/1 21/31/1 23/32/1" + crEnt
							+ "o lid" + crEnt + "v 0.062500 0.000000 0.645833" + crEnt + "v 0.062500 1.000000 0.645833"
							+ crEnt + "v 0.354167 0.000000 0.062500" + crEnt + "v 0.354167 1.000000 0.062500" + crEnt
							+ "v 0.645833 0.000000 0.937500" + crEnt + "v 0.645833 1.000000 0.937500" + crEnt
							+ "v 0.937500 0.000000 0.354167" + crEnt + "v 0.937500 1.000000 0.354167" + crEnt
							+ "v 0.062500 0.000000 0.354167" + crEnt + "v 0.062500 1.000000 0.354167" + crEnt
							+ "v 0.937500 0.000000 0.645833" + crEnt + "v 0.937500 1.000000 0.645833" + crEnt
							+ "v 0.645833 0.000000 0.062500" + crEnt + "v 0.645833 1.000000 0.062500" + crEnt
							+ "v 0.354167 0.000000 0.937500" + crEnt + "v 0.354167 1.000000 0.937500" + crEnt
							+ "v 0.791667 1.000000 0.791667" + crEnt + "v 0.208333 1.000000 0.791667" + crEnt
							+ "v 0.791667 0.000000 0.791667" + crEnt + "v 0.208333 0.000000 0.791667" + crEnt
							+ "v 0.208333 1.000000 0.208333" + crEnt + "v 0.791667 1.000000 0.208333" + crEnt
							+ "v 0.208333 0.000000 0.208333" + crEnt + "v 0.791667 0.000000 0.208333" + crEnt
							+ "vt 0.500000 1.000000" + crEnt + "vt 1.000000 0.500000" + crEnt + "vt 1.000000 0.750000"
							+ crEnt + "vt 0.750000 1.000000" + crEnt + "vt 0.500000 0.000000" + crEnt
							+ "vt 0.750000 0.000000" + crEnt + "vt 1.000000 0.250000" + crEnt + "vt 0.000000 0.500000"
							+ crEnt + "vt 0.000000 0.250000" + crEnt + "vt 0.250000 0.000000" + crEnt
							+ "vt 0.500000 0.000000" + crEnt + "vt 1.000000 0.500000" + crEnt + "vt 0.500000 1.000000"
							+ crEnt + "vt 0.000000 0.500000" + crEnt + "vt 0.000000 0.250000" + crEnt
							+ "vt 0.250000 0.000000" + crEnt + "vt 0.750000 0.000000" + crEnt + "vt 1.000000 0.250000"
							+ crEnt + "vt 1.000000 0.750000" + crEnt + "vt 0.750000 1.000000" + crEnt
							+ "vt 0.250000 1.000000" + crEnt + "vt 0.000000 0.750000" + crEnt + "vt 0.250000 1.000000"
							+ crEnt + "vt 0.000000 0.750000" + crEnt + "vn 0.0000 -1.0000 0.0000" + crEnt
							+ "vn 0.0000 1.0000 0.0000" + crEnt + "usemtl top" + crEnt
							+ "f 43/33/9 48/34/9 31/35/9 35/36/9" + crEnt + "f 48/34/9 47/37/9 27/38/9 37/39/9" + crEnt
							+ "f 47/37/9 44/40/9 25/41/9 33/42/9" + crEnt + "f 46/43/10 45/44/10 42/45/10 41/46/10"
							+ crEnt + "f 46/43/10 41/46/10 36/47/10 32/48/10" + crEnt
							+ "f 45/44/10 46/43/10 38/49/10 28/50/10" + crEnt + "f 42/45/10 45/44/10 34/51/10 26/52/10"
							+ crEnt + "f 44/40/9 43/33/9 29/53/9 39/54/9" + crEnt + "f 48/34/9 43/33/9 44/40/9 47/37/9"
							+ crEnt + "f 41/46/10 42/45/10 40/55/10 30/56/10";
					if (saveFile(blockModel, jsonModel)) {
						LogWriter.debug(
								"Create Default Container Blocks Model for \"" + fileName.toLowerCase() + "\" block");
					}
				}
			} else {
				if (customblock instanceof CustomBlock && ((CustomBlock) customblock).hasProperty()) {
					if (((CustomBlock) customblock).FACING != null) {
						jsonModel = "{" + crEnt + crTab + "\"_comment\": \"Custom Facing Block created by default\","
								+ crEnt + crTab + "\"parent\": \"" + CustomNpcs.MODID + ":block/orientable\"," + crEnt
								+ crTab + "\"textures\": {" + crEnt + crTab + crTab + "\"particle\": \""
								+ CustomNpcs.MODID + ":blocks/" + name.toLowerCase() + "_front\"," + crEnt + crTab
								+ crTab + "\"bottom\": \"" + CustomNpcs.MODID + ":blocks/" + name.toLowerCase()
								+ "_bottom\"," + crEnt + crTab + crTab + "\"top\": \"" + CustomNpcs.MODID + ":blocks/"
								+ name.toLowerCase() + "_top\"," + crEnt + crTab + crTab + "\"front\": \""
								+ CustomNpcs.MODID + ":blocks/" + name.toLowerCase() + "_front\"," + crEnt + crTab
								+ crTab + "\"right\": \"" + CustomNpcs.MODID + ":blocks/" + name.toLowerCase()
								+ "_right\"," + crEnt + crTab + crTab + "\"back\": \"" + CustomNpcs.MODID + ":blocks/"
								+ name.toLowerCase() + "_back\"," + crEnt + crTab + crTab + "\"left\": \""
								+ CustomNpcs.MODID + ":blocks/" + name.toLowerCase() + "_left\"" + crEnt + crTab + "}"
								+ crEnt + "}";
						if (saveFile(blockModel, jsonModel)) {
							LogWriter.debug(
									"Create Default Facing Block Model for \"" + fileName.toLowerCase() + "\" block");
						}
					}
				}
			}
			if (jsonModel.isEmpty()) {
				String texture = CustomNpcs.MODID + ":blocks/" + name.toLowerCase();
				if (customblock instanceof CustomBlockPortal) {
					texture = CustomNpcs.MODID + ":environment/custom_" + name.toLowerCase() + "_portal";
				}
				jsonModel = "{" + crEnt + crTab + "\"_comment\": \"Custom Block Model created by default\"," + crEnt
						+ crTab + "\"parent\": \"block/cube_all\"," + crEnt + crTab + "\"textures\": {" + crEnt + crTab
						+ crTab + "\"all\": \"" + texture + "\"" + crEnt + crTab + "}" + crEnt + "}";
				if (saveFile(blockModel, jsonModel)) {
					LogWriter.debug("Create Default Block Model for \"" + fileName.toLowerCase() + "\" block");
				}
			}
		}

		File itemModel = new File(itemModelsDir, fileName.toLowerCase() + ".json");
		if (!itemModel.exists()) {
			String jsonStr;
			if (customblock instanceof CustomDoor) {
				jsonStr = "{" + crEnt + crTab + "\"_comment\": \"Custom Item Block created by default\"," + crEnt
						+ crTab + "\"parent\": \"minecraft:item/generated\"," + crEnt + crTab + "\"textures\": {"
						+ crEnt + crTab + crTab + "\"layer0\": \"" + CustomNpcs.MODID + ":items/"
						+ fileName.toLowerCase() + "\"" + crEnt + crTab + "}" + crEnt + "}";
			} else {
				jsonStr = "{" + crEnt + crTab + "\"_comment\": \"Custom Item Block created by default\"," + crEnt
						+ crTab + "\"parent\": \"" + CustomNpcs.MODID + ":block/" + fileName.toLowerCase() + "\","
						+ crEnt + crTab + "\"display\": {" + crEnt + crTab + crTab + "\"thirdperson\": {" + crEnt
						+ crTab + crTab + crTab + "\"rotation\": [ 10, -45, 170 ]," + crEnt + crTab + crTab + crTab
						+ "\"translation\": [ 0, 1.5, -2.75 ]," + crEnt + crTab + crTab + crTab
						+ "\"scale\": [ 0.375, 0.375, 0.375 ]" + crEnt + crTab + crTab + "}" + crEnt + crTab + "}"
						+ crEnt + "}";
			}
			if (saveFile(itemModel, jsonStr)) {
				LogWriter.debug("Create Default Block Item Model for \"" + name + "\" block");
			}
		}
	}

	public void checkItemFiles(ICustomElement customitem) {
		String name = customitem.getCustomName();
		String fileName = Objects.requireNonNull(((Item) customitem).getRegistryName()).getResourcePath();
		NBTTagCompound nbtData = customitem.getCustomNbt().getMCNBT();

		File itemModelsDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/models/item");
		if (!itemModelsDir.exists() && !itemModelsDir.mkdirs()) { return; }

		File itemModel = new File(itemModelsDir, fileName.toLowerCase() + ".json");
		String crEnt = "" + ((char) 10);
		String crTab = "" + ((char) 9);
		String jsonModel = "";
		if (customitem instanceof CustomArmor && (nbtData.hasKey("OBJData", 9) || nbtData.hasKey("OBJData", 10))) {
			File armorModelsDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/models/armor");
			if (!armorModelsDir.exists() && !armorModelsDir.mkdirs()) { return; }

			File objModel = new File(armorModelsDir, name.toLowerCase() + ".obj");
			if (!objModel.exists()) {
				InputStream inputStreamOBJ = AdditionalMethods.instance.getModInputStream("armorobjexample.obj");
				if (inputStreamOBJ != null) {
					try {
						ByteArrayOutputStream result = new ByteArrayOutputStream();
						byte[] buffer = new byte[1024];
						for (int length; (length = inputStreamOBJ.read(buffer)) != -1; ) { result.write(buffer, 0, length); }
						saveFile(objModel, result.toString("UTF-8").replace("mtllib armorobjexample.mtl", "mtllib " + name.toLowerCase() + ".mtl"));
					}
					catch (IOException e) { LogWriter.error("Error:", e); }
				}
				InputStream inputStreamMTL = AdditionalMethods.instance.getModInputStream("armorobjexample.mtl");
				if (inputStreamMTL != null) {
					try {
						ByteArrayOutputStream result = new ByteArrayOutputStream();
						byte[] buffer = new byte[1024];
						for (int length; (length = inputStreamMTL.read(buffer)) != -1; ) { result.write(buffer, 0, length); }
						saveFile(new File(armorModelsDir, name.toLowerCase() + ".mtl"), result.toString("UTF-8"));
					}
					catch (IOException e) { LogWriter.error("Error:", e); }
				}
			}
		}
		if (!itemModel.exists()) {
			if (customitem instanceof CustomShield || customitem instanceof CustomBow) {
				boolean isBow = (customitem instanceof CustomBow);
				jsonModel = "{" + crEnt + crTab + "\"_comment\": \"Custom " + (isBow ? "Bow" : "Shield")
						+ " Item Model created by default\"," + crEnt + crTab + "\"parent\": \"item/generated\","
						+ crEnt + crTab + "\"textures\": {" + crEnt + crTab + crTab + "\"layer0\": \""
						+ CustomNpcs.MODID + ":items/" + (isBow ? "weapons/" + name + "_standby" : name) + "\"" + crEnt
						+ crTab + "}," + crEnt + crTab + "\"display\": {" + crEnt + crTab + crTab
						+ "\"thirdperson_righthand\": {" + crEnt + crTab + crTab + crTab + "\"rotation\": [ "
						+ (isBow ? "-80, 260, -40" : "135, 270, 0") + " ]," + crEnt + crTab + crTab + crTab
						+ "\"translation\": [ " + (isBow ? "-1, -2, 2.5" : "3, -4, 3") + " ]," + crEnt + crTab + crTab
						+ crTab + "\"scale\": [ " + (isBow ? "0.9, 0.9, 0.9" : "1.25, 1.25, 1.25") + " ]" + crEnt
						+ crTab + crTab + "}," + crEnt + crTab + crTab + "\"thirdperson_lefthand\": {" + crEnt + crTab
						+ crTab + crTab + "\"rotation\": [ " + (isBow ? "-80, -280, 40" : "135, 90, 0") + " ]," + crEnt
						+ crTab + crTab + crTab + "\"translation\": [ " + (isBow ? "-1, -2, 2.5" : "3, -4, 4") + " ],"
						+ crEnt + crTab + crTab + crTab + "\"scale\": [ "
						+ (isBow ? "0.9, 0.9, 0.9" : "1.25, 1.25, 1.25") + " ]" + crEnt + crTab + crTab + "}," + crEnt
						+ crTab + crTab + "\"firstperson_righthand\": {" + crEnt + crTab + crTab + crTab
						+ "\"rotation\": [ " + (isBow ? "0, -90, 25" : "0, 0, -135") + " ]," + crEnt + crTab + crTab
						+ crTab + "\"translation\": [ " + (isBow ? "1.13, 3.2, 1.13" : "3, -2, 0") + " ]," + crEnt
						+ crTab + crTab + crTab + "\"scale\": [ " + (isBow ? "0.68, 0.68, 0.68" : "1, 1, 1") + " ]"
						+ crEnt + crTab + crTab + "}," + crEnt + crTab + crTab + "\"firstperson_lefthand\": {" + crEnt
						+ crTab + crTab + crTab + "\"rotation\": [ " + (isBow ? "0, 90, -25" : "0, 0, -45") + " ],"
						+ crEnt + crTab + crTab + crTab + "\"translation\": [ "
						+ (isBow ? "1.13, 3.2, 1.13" : "3, 0, 0") + " ]," + crEnt + crTab + crTab + crTab
						+ "\"scale\": [ " + (isBow ? "0.68, 0.68, 0.68" : "1, 1, 1") + " ]" + crEnt + crTab + crTab
						+ "}," + crEnt + crTab + crTab + "\"gui\": {" + crEnt + crTab + crTab + crTab
						+ "\"rotation\": [ " + (isBow ? "0, 0, 0" : "0, 0, -135") + " ]," + crEnt + crTab + crTab
						+ crTab + "\"translation\": [ " + (isBow ? "0, 0, 0" : "0, -1, 0") + " ]," + crEnt + crTab
						+ crTab + crTab + "\"scale\": [ " + (isBow ? "1, 1, 1" : "0.95, 0.95, 0.95") + " ]" + crEnt
						+ crTab + crTab + "}," + crEnt + crTab + crTab + "\"fixed\": {" + crEnt + crTab + crTab + crTab
						+ "\"rotation\": [ " + (isBow ? "0, 180, 0" : "0, 0, -135") + " ]," + crEnt + crTab + crTab
						+ crTab + "\"translation\": [ " + (isBow ? "-2, 4, -5" : "0, 0, 0") + " ]," + crEnt + crTab
						+ crTab + crTab + "\"scale\": [ " + (isBow ? "0.5, 0.5, 0.5" : "1, 1, 1") + " ]" + crEnt + crTab
						+ crTab + "}," + crEnt + crTab + crTab + "\"ground\": {" + crEnt + crTab + crTab + crTab
						+ "\"rotation\": [ " + (isBow ? "0, 0, 0" : "0, 0, -135") + " ]," + crEnt + crTab + crTab
						+ crTab + "\"translation\": [ " + (isBow ? "4, 4, 2" : "0, 3, 0") + " ]," + crEnt + crTab
						+ crTab + crTab + "\"scale\": [ " + (isBow ? "0.25, 0.25, 0.25" : "1, 1, 1") + " ]" + crEnt
						+ crTab + crTab + "}" + crEnt + crTab + "}," + crEnt + crTab + "\"overrides\": [" + crEnt
						+ crTab + crTab + "{" + crEnt + crTab + crTab + crTab + "\"predicate\": {" + crEnt + crTab
						+ crTab + crTab + crTab + "\"" + (isBow ? "pulling" : "blocking") + "\": 1" + crEnt + crTab
						+ crTab + crTab + "}," + crEnt + crTab + crTab + crTab + "\"model\": \"" + CustomNpcs.MODID
						+ ":item/" + fileName + (isBow ? "_pulling_0" : "_blocking") + "\"" + crEnt + crTab + crTab
						+ "}"
						+ (isBow ? "," + crEnt + crTab + crTab + "{" + crEnt + crTab + crTab + crTab
						+ "\"predicate\": {" + crEnt + crTab + crTab + crTab + crTab + "\"pulling\": 1," + crEnt
						+ crTab + crTab + crTab + crTab + "\"pull\": 0.65" + crEnt + crTab + crTab + crTab
						+ "}," + crEnt + crTab + crTab + crTab + "\"model\": \"" + CustomNpcs.MODID + ":item/"
						+ fileName + "_pulling_1\"" + crEnt + crTab + crTab + "}," + crEnt + crTab + crTab + "{"
						+ crEnt + crTab + crTab + crTab + "\"predicate\": {" + crEnt + crTab + crTab + crTab
						+ crTab + "\"pulling\": 1," + crEnt + crTab + crTab + crTab + crTab + "\"pull\": 0.9"
						+ crEnt + crTab + crTab + crTab + "}," + crEnt + crTab + crTab + crTab + "\"model\": \""
						+ CustomNpcs.MODID + ":item/" + fileName + "_pulling_2\"" + crEnt + crTab + crTab + "}"
						+ crEnt
						: crEnt)
						+ crTab + "]" + crEnt + "}";
				if (saveFile(itemModel, jsonModel)) {
					LogWriter.debug(
							"Create Default " + (isBow ? "Bow" : "Shield") + " Item Model for \"" + name + "\" item");
				}
				if (customitem instanceof CustomShield) {
					File blockingModel = new File(itemModelsDir, fileName.toLowerCase() + "_blocking.json");
					if (!blockingModel.exists()) {
						jsonModel = "{" + crEnt + crTab
								+ "\"_comment\": \"Custom Shield Blocking Item Model created by default\"," + crEnt
								+ crTab + "\"parent\": \"item/generated\"," + crEnt + crTab + "\"textures\": {" + crEnt
								+ crTab + crTab + "\"layer0\": \"" + CustomNpcs.MODID + ":items/" + name + "\"" + crEnt
								+ crTab + "}," + crEnt + crTab + "\"display\": {" + crEnt + crTab + crTab
								+ "\"thirdperson_righthand\": {" + crEnt + crTab + crTab + crTab
								+ "\"rotation\": [ 30, -30, 45 ]," + crEnt + crTab + crTab + crTab
								+ "\"translation\": [ 0, 2, -2 ]," + crEnt + crTab + crTab + crTab
								+ "\"scale\": [ 1.25, 1.25, 1.25 ]" + crEnt + crTab + crTab + "}," + crEnt + crTab
								+ crTab + "\"thirdperson_lefthand\": {" + crEnt + crTab + crTab + crTab
								+ "\"rotation\": [ 30, 150, -45 ]," + crEnt + crTab + crTab + crTab
								+ "\"translation\": [ 0, 2, -2 ]," + crEnt + crTab + crTab + crTab
								+ "\"scale\": [ 1.25, 1.25, 1.25 ]" + crEnt + crTab + crTab + "}," + crEnt + crTab
								+ crTab + "\"firstperson_righthand\": {" + crEnt + crTab + crTab + crTab
								+ "\"rotation\": [ 0, 0, -125 ]," + crEnt + crTab + crTab + crTab
								+ "\"translation\": [ -2, 0, 0 ]" + crEnt + crTab + crTab + "}," + crEnt + crTab + crTab
								+ "\"firstperson_lefthand\": {" + crEnt + crTab + crTab + crTab
								+ "\"rotation\": [ 0, 0, -35 ]," + crEnt + crTab + crTab + crTab
								+ "\"translation\": [ 2, -2, 0 ]" + crEnt + crTab + crTab + "}," + crEnt + crTab + crTab
								+ "\"gui\": {" + crEnt + crTab + crTab + crTab + "\"rotation\": [ 0, 0, -135 ]," + crEnt
								+ crTab + crTab + crTab + "\"translation\": [ 0, -1, 0 ]," + crEnt + crTab + crTab
								+ crTab + "\"scale\": [ 0.95, 0.95, 0.95 ]" + crEnt + crTab + crTab + "}" + crEnt
								+ crTab + "}" + crEnt + "}";
						if (saveFile(blockingModel, jsonModel)) {
							LogWriter.debug("Create Default Shield Blocking Item Model for \"" + name + "\" item");
						}
					}
				} else {
					for (int i = 0; i < 3; i++) {
						File pulling = new File(itemModelsDir, fileName.toLowerCase() + "_pulling_" + i + ".json");
						if (!pulling.exists()) {
							jsonModel = "{" + crEnt + crTab + "\"_comment\": \"Custom Bow Pulling " + i
									+ " Item Model created by default\"," + crEnt + crTab + "\"parent\": \""
									+ CustomNpcs.MODID + ":item/" + fileName + "\"," + crEnt + crTab + "\"textures\": {"
									+ crEnt + crTab + crTab + "\"layer0\": \"" + CustomNpcs.MODID + ":items/weapons/"
									+ name.toLowerCase() + "_pulling_" + i + "\"" + crEnt + crTab + "}" + crEnt + "}";
							if (saveFile(pulling, jsonModel)) {
								LogWriter.debug(
										"Create Default Bow Pulling " + i + " Item Model for \"" + name + "\" item");
							}
						}
					}
				}
			} else if (customitem instanceof CustomFishingRod) {
				if (!itemModel.exists()) {
					jsonModel = "{" + crEnt + crTab
							+ "\"_comment\": \"Custom Fishing Rod Uncast Item Model created by default\"," + crEnt
							+ crTab + "\"parent\": \"item/handheld_rod\"," + crEnt + crTab + "\"textures\": {" + crEnt
							+ crTab + crTab + "\"layer0\": \"" + CustomNpcs.MODID + ":items/" + name + "_uncast\""
							+ crEnt + crTab + "}," + crEnt + crTab + "\"overrides\": [" + crEnt + crTab + crTab + "{"
							+ crEnt + crTab + crTab + crTab + "\"predicate\": {" + crEnt + crTab + crTab + crTab + crTab
							+ "\"cast\": 1" + crEnt + crTab + crTab + crTab + "}," + crEnt + crTab + crTab + crTab
							+ "\"model\": \"" + CustomNpcs.MODID + ":item/" + fileName + "_cast\"" + crEnt + crTab
							+ crTab + "}" + crEnt + crTab + "]" + crEnt + "}";
					if (saveFile(itemModel, jsonModel)) {
						LogWriter.debug("Create Default Fishing Rod Uncast Item Model for \"" + name + "\" item");
					}
				}
				File cast = new File(itemModelsDir, fileName.toLowerCase() + "_cast.json");
				if (!cast.exists()) {
					jsonModel = "{" + crEnt + crTab
							+ "\"_comment\": \"Custom Fishing Rod Cast Item Model created by default\"," + crEnt + crTab
							+ "\"parent\": \"item/fishing_rod\"," + crEnt + crTab + "\"textures\": {" + crEnt + crTab
							+ crTab + "\"layer0\": \"" + CustomNpcs.MODID + ":items/" + name + "_cast\"" + crEnt + crTab
							+ "}" + crEnt + "}";
					if (saveFile(cast, jsonModel)) {
						LogWriter.debug("Create Default Fishing Rod Cast Item Model for \"" + name + "\" item");
					}
				}
			} else if (name.equals("axeexample")) {
				File blockModelsDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/models/block/obj");
				if (!blockModelsDir.exists() && !blockModelsDir.mkdirs()) { return; }
				File objModel = new File(blockModelsDir, name.toLowerCase() + ".obj");
				if (!objModel.exists()) {
					String model = "mtllib " + name + ".mtl" + crEnt +
							"o lever" + crEnt +
							"v 0.500000 0.000000 0.475000" + crEnt +
							"v 0.500000 0.031250 0.450000" + crEnt +
							"v 0.514401 0.000000 0.487500" + crEnt +
							"v 0.528801 0.031250 0.475000" + crEnt +
							"v 0.514401 0.000000 0.512500" + crEnt +
							"v 0.528801 0.031250 0.525000" + crEnt +
							"v 0.500000 0.000000 0.525000" + crEnt +
							"v 0.500000 0.031250 0.550000" + crEnt +
							"v 0.485599 0.000000 0.512500" + crEnt +
							"v 0.471199 0.031250 0.525000" + crEnt +
							"v 0.485599 0.000000 0.487500" + crEnt +
							"v 0.471199 0.031250 0.475000" + crEnt +
							"v 0.528801 0.375000 0.453080" + crEnt +
							"v 0.500000 0.375000 0.428080" + crEnt +
							"v 0.528801 0.375000 0.503080" + crEnt +
							"v 0.500000 0.375000 0.528080" + crEnt +
							"v 0.471199 0.375000 0.502500" + crEnt +
							"v 0.471199 0.375000 0.452500" + crEnt +
							"v 0.528801 0.625000 0.486048" + crEnt +
							"v 0.500000 0.625000 0.461048" + crEnt +
							"v 0.528801 0.625000 0.536048" + crEnt +
							"v 0.500000 0.625000 0.561048" + crEnt +
							"v 0.471199 0.625000 0.535000" + crEnt +
							"v 0.471199 0.625000 0.485000" + crEnt +
							"v 0.528801 0.906250 0.475000" + crEnt +
							"v 0.500000 0.906250 0.450000" + crEnt +
							"v 0.528801 0.906250 0.525000" + crEnt +
							"v 0.500000 0.906250 0.550000" + crEnt +
							"v 0.471198 0.906250 0.525000" + crEnt +
							"v 0.471198 0.906250 0.475000" + crEnt +
							"v 0.514400 0.937500 0.487500" + crEnt +
							"v 0.500000 0.937500 0.475000" + crEnt +
							"v 0.514400 0.937500 0.512500" + crEnt +
							"v 0.500000 0.937500 0.525000" + crEnt +
							"v 0.485599 0.937500 0.512500" + crEnt +
							"v 0.485599 0.937500 0.487500" + crEnt +
							"v 0.500000 0.937500 0.500000" + crEnt +
							"v 0.500000 0.000000 0.500000" + crEnt +
							"vt 0.348695 0.664113" + crEnt +
							"vt 0.337822 0.999917" + crEnt +
							"vt 0.000083 0.750561" + crEnt +
							"vt 0.257537 0.580868" + crEnt +
							"vt 0.000083 0.228448" + crEnt +
							"vt 0.243238 0.423492" + crEnt +
							"vt 0.349025 0.000083" + crEnt +
							"vt 0.338152 0.335887" + crEnt +
							"vt 0.686764 0.249439" + crEnt +
							"vt 0.429310 0.419132" + crEnt +
							"vt 0.998413 0.246264" + crEnt +
							"vt 0.999946 0.289130" + crEnt +
							"vt 0.612561 0.286810" + crEnt +
							"vt 0.613155 0.243154" + crEnt +
							"vt 0.686764 0.771552" + crEnt +
							"vt 0.443609 0.576508" + crEnt +
							"vt 0.343423 0.500000" + crEnt +
							"vt 0.614805 0.188081" + crEnt +
							"vt 0.325409 0.250380" + crEnt +
							"vt 0.327744 0.192040" + crEnt +
							"vt 0.998402 0.048901" + crEnt +
							"vt 0.995142 0.104528" + crEnt +
							"vt 0.614792 0.106585" + crEnt +
							"vt 0.613120 0.051496" + crEnt +
							"vt 0.995134 0.190627" + crEnt +
							"vt 0.993464 0.147574" + crEnt +
							"vt 0.615646 0.147534" + crEnt +
							"vt 0.999946 0.006034" + crEnt +
							"vt 0.612551 0.008256" + crEnt +
							"vt 0.328947 0.147508" + crEnt +
							"vt 0.001011 0.189997" + crEnt +
							"vt 0.000054 0.147589" + crEnt +
							"vt 0.325364 0.043865" + crEnt +
							"vt 0.324357 0.000054" + crEnt +
							"vt 0.327685 0.102164" + crEnt +
							"vt 0.324357 0.295006" + crEnt +
							"vt 0.000083 0.771552" + crEnt +
							"vt 0.000083 0.249439" + crEnt +
							"vt 0.257538 0.419132" + crEnt +
							"vt 0.243239 0.576508" + crEnt +
							"vt 0.003100 0.290517" + crEnt +
							"vt 0.002580 0.246612" + crEnt +
							"vt 0.000990 0.105181" + crEnt +
							"vt 0.002604 0.048566" + crEnt +
							"vt 0.003106 0.004637" + crEnt +
							"vt 0.429311 0.580868" + crEnt +
							"vt 0.343424 0.500000" + crEnt +
							"vt 0.443610 0.423492" + crEnt +
							"vt 0.337822 0.000083" + crEnt +
							"vt 0.686765 0.228448" + crEnt +
							"vt 0.348696 0.335887" + crEnt +
							"vt 0.686765 0.750561" + crEnt +
							"vt 0.349025 0.999917" + crEnt +
							"vt 0.338153 0.664113" + crEnt +
							"vn 0.5611 -0.5171 -0.6464" + crEnt +
							"vn 0.9082 -0.4185 0.0000" + crEnt +
							"vn 0.5611 -0.5171 0.6464" + crEnt +
							"vn -0.5611 -0.5171 0.6464" + crEnt +
							"vn -0.6590 0.0485 0.7505" + crEnt +
							"vn -0.9082 -0.4185 0.0000" + crEnt +
							"vn -0.5611 -0.5171 -0.6464" + crEnt +
							"vn 0.0000 -1.0000 0.0000" + crEnt +
							"vn -1.0000 -0.0000 0.0000" + crEnt +
							"vn 1.0000 0.0000 0.0000" + crEnt +
							"vn 0.6548 -0.0481 -0.7543" + crEnt +
							"vn 0.6548 0.0481 0.7543" + crEnt +
							"vn -0.6504 -0.0490 -0.7580" + crEnt +
							"vn -0.6473 -0.0285 -0.7617" + crEnt +
							"vn 0.6523 -0.0991 0.7515" + crEnt +
							"vn 0.6523 0.0991 -0.7515" + crEnt +
							"vn -0.6399 0.0998 -0.7620" + crEnt +
							"vn -0.6643 -0.0970 0.7412" + crEnt +
							"vn -0.9082 0.4185 0.0000" + crEnt +
							"vn -0.6630 0.0280 0.7481" + crEnt +
							"vn 0.6552 0.0297 0.7549" + crEnt +
							"vn 0.6552 -0.0297 -0.7549" + crEnt +
							"vn 0.0000 1.0000 0.0000" + crEnt +
							"vn 0.5611 0.5171 0.6464" + crEnt +
							"vn 0.5611 0.5171 -0.6464" + crEnt +
							"vn -0.5611 0.5171 -0.6464" + crEnt +
							"vn -0.5611 0.5171 0.6464" + crEnt +
							"vn 0.9082 0.4185 0.0000" + crEnt +
							"usemtl wood" + crEnt +
							"f 1/1/1 2/2/1 4/3/1 3/4/1" + crEnt +
							"f 3/4/2 4/3/2 6/5/2 5/6/2" + crEnt +
							"f 5/6/3 6/5/3 8/7/3 7/8/3" + crEnt +
							"f 7/8/4 8/7/4 10/9/4 9/10/4" + crEnt +
							"f 10/11/5 8/12/5 16/13/5 17/14/5" + crEnt +
							"f 9/10/6 10/9/6 12/15/6 11/16/6" + crEnt +
							"f 11/16/7 12/15/7 2/2/7 1/1/7" + crEnt +
							"f 1/1/8 38/17/8 11/16/8" + crEnt +
							"f 18/18/9 17/14/9 23/19/9 24/20/9" + crEnt +
							"f 6/21/10 4/22/10 13/23/10 15/24/10" + crEnt +
							"f 12/25/9 10/11/9 17/14/9 18/18/9" + crEnt +
							"f 4/22/11 2/26/11 14/27/11 13/23/11" + crEnt +
							"f 8/28/12 6/21/12 15/24/12 16/29/12" + crEnt +
							"f 2/26/13 12/25/13 18/18/13 14/27/13" + crEnt +
							"f 20/30/14 24/20/14 30/31/14 26/32/14" + crEnt +
							"f 16/29/15 15/24/15 21/33/15 22/34/15" + crEnt +
							"f 13/23/16 14/27/16 20/30/16 19/35/16" + crEnt +
							"f 14/27/17 18/18/17 24/20/17 20/30/17" + crEnt +
							"f 17/14/18 16/13/18 22/36/18 23/19/18" + crEnt +
							"f 15/24/10 13/23/10 19/35/10 21/33/10" + crEnt +
							"f 30/37/19 29/38/19 35/39/19 36/40/19" + crEnt +
							"f 23/19/20 22/36/20 28/41/20 29/42/20" + crEnt +
							"f 21/33/10 19/35/10 25/43/10 27/44/10" + crEnt +
							"f 24/20/9 23/19/9 29/42/9 30/31/9" + crEnt +
							"f 22/34/21 21/33/21 27/44/21 28/45/21" + crEnt +
							"f 19/35/22 20/30/22 26/32/22 25/43/22" + crEnt +
							"f 31/46/23 37/47/23 33/48/23" + crEnt +
							"f 28/49/24 27/50/24 33/48/24 34/51/24" + crEnt +
							"f 25/52/25 26/53/25 32/54/25 31/46/25" + crEnt +
							"f 26/53/26 30/37/26 36/40/26 32/54/26" + crEnt +
							"f 29/38/27 28/49/27 34/51/27 35/39/27" + crEnt +
							"f 27/50/28 25/52/28 31/46/28 33/48/28" + crEnt +
							"f 33/48/23 37/47/23 34/51/23" + crEnt +
							"f 34/51/23 37/47/23 35/39/23" + crEnt +
							"f 35/39/23 37/47/23 36/40/23" + crEnt +
							"f 36/40/23 37/47/23 32/54/23" + crEnt +
							"f 32/54/23 37/47/23 31/46/23" + crEnt +
							"f 11/16/8 38/17/8 9/10/8" + crEnt +
							"f 9/10/8 38/17/8 7/8/8" + crEnt +
							"f 7/8/8 38/17/8 5/6/8" + crEnt +
							"f 5/6/8 38/17/8 3/4/8" + crEnt +
							"f 3/4/8 38/17/8 1/1/8" + crEnt +
							"o tip" + crEnt +
							"v 0.519134 0.744683 0.450562" + crEnt +
							"v 0.519134 0.849607 0.446199" + crEnt +
							"v 0.546194 0.735976 0.487599" + crEnt +
							"v 0.546194 0.860900 0.483237" + crEnt +
							"v 0.546194 0.737312 0.525844" + crEnt +
							"v 0.546194 0.862235 0.521482" + crEnt +
							"v 0.519134 0.738605 0.562881" + crEnt +
							"v 0.519134 0.863529 0.558519" + crEnt +
							"v 0.480866 0.738605 0.562881" + crEnt +
							"v 0.480866 0.863529 0.558519" + crEnt +
							"v 0.453806 0.737312 0.525844" + crEnt +
							"v 0.453806 0.862235 0.521482" + crEnt +
							"v 0.453806 0.735976 0.487599" + crEnt +
							"v 0.453806 0.860900 0.483237" + crEnt +
							"v 0.480866 0.744683 0.450562" + crEnt +
							"v 0.480866 0.849607 0.446199" + crEnt +
							"v 0.500000 0.861568 0.502359" + crEnt +
							"v 0.500000 0.736644 0.506721" + crEnt +
							"v 0.514134 0.701589 0.326729" + crEnt +
							"v 0.514134 0.883975 0.320185" + crEnt +
							"v 0.485866 0.883975 0.307685" + crEnt +
							"v 0.485866 0.701589 0.314229" + crEnt +
							"v 0.509134 0.668177 0.265357" + crEnt +
							"v 0.509134 0.925525 0.256632" + crEnt +
							"v 0.490866 0.925525 0.229132" + crEnt +
							"v 0.490866 0.655677 0.237857" + crEnt +
							"v 0.500000 0.612096 0.197150" + crEnt +
							"v 0.500000 0.966925 0.187378" + crEnt +
							"v 0.500000 0.859607 0.446199" + crEnt +
							"v 0.500000 0.898975 0.307685" + crEnt +
							"v 0.500000 0.940525 0.229132" + crEnt +
							"v 0.500000 0.734683 0.450562" + crEnt +
							"v 0.500000 0.686589 0.314229" + crEnt +
							"v 0.500000 0.640677 0.237857" + crEnt +
							"vt 0.431093 0.620684" + crEnt +
							"vt 0.431105 0.381410" + crEnt +
							"vt 0.326543 0.375022" + crEnt +
							"vt 0.326537 0.624983" + crEnt +
							"vt 0.239346 0.375022" + crEnt +
							"vt 0.239341 0.624983" + crEnt +
							"vt 0.134789 0.375022" + crEnt +
							"vt 0.134784 0.624983" + crEnt +
							"vt 0.001506 0.624983" + crEnt +
							"vt 0.002403 0.375022" + crEnt +
							"vt 0.134784 0.375022" + crEnt +
							"vt 0.134789 0.624983" + crEnt +
							"vt 0.239341 0.375022" + crEnt +
							"vt 0.239346 0.624983" + crEnt +
							"vt 0.326537 0.375022" + crEnt +
							"vt 0.326543 0.624983" + crEnt +
							"vt 0.377976 0.403815" + crEnt +
							"vt 0.437329 0.249987" + crEnt +
							"vt 0.503853 0.400852" + crEnt +
							"vt 0.431093 0.381415" + crEnt +
							"vt 0.431105 0.620689" + crEnt +
							"vt 0.426840 0.332426" + crEnt +
							"vt 0.718547 0.318400" + crEnt +
							"vt 0.716322 0.271013" + crEnt +
							"vt 0.247887 0.190795" + crEnt +
							"vt 0.437329 0.249987" + crEnt +
							"vt 0.250383 0.316644" + crEnt +
							"vt 0.216249 0.254372" + crEnt +
							"vt 0.623868 0.309238" + crEnt +
							"vt 0.621372 0.183387" + crEnt +
							"vt 0.497818 0.096603" + crEnt +
							"vt 0.371923 0.098634" + crEnt +
							"vt 0.247887 0.190795" + crEnt +
							"vt 0.250383 0.316644" + crEnt +
							"vt 0.216249 0.254372" + crEnt +
							"vt 0.377976 0.403815" + crEnt +
							"vt 0.503853 0.400852" + crEnt +
							"vt 0.623868 0.309238" + crEnt +
							"vt 0.621372 0.183387" + crEnt +
							"vt 0.497818 0.096603" + crEnt +
							"vt 0.371923 0.098634" + crEnt +
							"vt 0.718538 0.683697" + crEnt +
							"vt 0.863010 0.752527" + crEnt +
							"vt 0.863025 0.249572" + crEnt +
							"vt 0.718538 0.318401" + crEnt +
							"vt 0.718547 0.683699" + crEnt +
							"vt 0.426840 0.669673" + crEnt +
							"vt 0.716322 0.731086" + crEnt +
							"vt 0.863010 0.249572" + crEnt +
							"vt 0.863122 0.209512" + crEnt +
							"vt 0.937494 0.180668" + crEnt +
							"vt 0.863025 0.752526" + crEnt +
							"vt 0.863123 0.792587" + crEnt +
							"vt 0.937494 0.821431" + crEnt +
							"vn 0.8076 -0.0224 -0.5893" + crEnt +
							"vn 1.0000 -0.0000 0.0000" + crEnt +
							"vn 0.8076 0.0206 0.5893" + crEnt +
							"vn 0.0000 0.0349 0.9994" + crEnt +
							"vn -0.8076 0.0206 0.5893" + crEnt +
							"vn -1.0000 0.0000 0.0000" + crEnt +
							"vn 0.0000 0.9994 -0.0349" + crEnt +
							"vn -0.8076 -0.0224 -0.5893" + crEnt +
							"vn 0.5327 0.8178 0.2179" + crEnt +
							"vn 0.0000 -0.9994 0.0349" + crEnt +
							"vn 0.0831 0.9685 -0.2346" + crEnt +
							"vn -0.0831 0.9685 -0.2346" + crEnt +
							"vn 0.0843 -0.9820 -0.1693" + crEnt +
							"vn -0.0843 -0.9820 -0.1693" + crEnt +
							"vn 0.9968 -0.0028 -0.0800" + crEnt +
							"vn -0.9993 -0.0014 -0.0363" + crEnt +
							"vn 0.9992 -0.0015 -0.0399" + crEnt +
							"vn -0.5819 -0.7744 0.2483" + crEnt +
							"vn -0.8114 0.4941 0.3124" + crEnt +
							"vn 0.2536 0.8389 0.4816" + crEnt +
							"vn -0.9979 -0.0022 -0.0644" + crEnt +
							"vn -0.7475 -0.5798 0.3241" + crEnt +
							"vn -0.9763 -0.0064 -0.2161" + crEnt +
							"vn 0.5917 -0.6598 0.4632" + crEnt +
							"vn 0.9913 -0.0040 -0.1313" + crEnt +
							"vn -0.7571 0.5872 0.2865" + crEnt +
							"vn -0.5889 0.7836 0.1979" + crEnt +
							"vn 0.6169 -0.6915 0.3758" + crEnt +
							"vn 0.5090 -0.8153 0.2760" + crEnt +
							"vn -0.2157 0.8253 0.5218" + crEnt +
							"vn -0.8023 -0.4885 0.3430" + crEnt +
							"usemtl top" + crEnt +
							"f 39/55/29 40/56/29 42/57/29 41/58/29" + crEnt +
							"f 41/58/30 42/57/30 44/59/30 43/60/30" + crEnt +
							"f 43/60/31 44/59/31 46/61/31 45/62/31" + crEnt +
							"f 45/63/32 46/64/32 48/65/32 47/66/32" + crEnt +
							"f 47/66/33 48/65/33 50/67/33 49/68/33" + crEnt +
							"f 49/68/34 50/67/34 52/69/34 51/70/34" + crEnt +
							"f 42/71/35 55/72/35 44/73/35" + crEnt +
							"f 51/70/36 52/69/36 54/74/36 53/75/36" + crEnt +
							"f 67/76/37 40/56/37 58/77/37 68/78/37" + crEnt +
							"f 39/79/38 56/80/38 53/81/38 70/82/38" + crEnt +
							"f 44/73/35 55/72/35 46/83/35" + crEnt +
							"f 46/83/35 55/72/35 48/84/35" + crEnt +
							"f 48/84/35 55/72/35 50/85/35" + crEnt +
							"f 50/85/35 55/72/35 52/86/35" + crEnt +
							"f 52/86/39 55/72/39 54/87/39" + crEnt +
							"f 54/87/35 55/72/35 40/88/35 67/89/35" + crEnt +
							"f 40/88/40 55/72/40 42/71/40" + crEnt +
							"f 53/81/41 56/80/41 51/90/41" + crEnt +
							"f 51/90/38 56/80/38 49/91/38" + crEnt +
							"f 49/91/38 56/80/38 47/92/38" + crEnt +
							"f 47/92/38 56/80/38 45/93/38" + crEnt +
							"f 45/93/38 56/80/38 43/94/38" + crEnt +
							"f 43/94/38 56/80/38 41/95/38" + crEnt +
							"f 41/95/42 56/80/42 39/79/42" + crEnt +
							"f 58/77/43 57/96/43 61/97/43 62/98/43" + crEnt +
							"f 53/75/44 54/74/44 59/99/44 60/100/44" + crEnt +
							"f 40/56/45 39/55/45 57/96/45 58/77/45" + crEnt +
							"f 70/101/46 53/75/46 60/100/46 71/102/46" + crEnt +
							"f 63/103/47 69/104/47 66/105/47" + crEnt +
							"f 68/78/48 58/77/48 62/98/48 69/104/48" + crEnt +
							"f 60/100/49 59/99/49 63/103/49 64/106/49" + crEnt +
							"f 71/102/50 60/100/50 64/106/50 72/107/50" + crEnt +
							"f 64/106/51 63/103/51 66/105/51 65/108/51" + crEnt +
							"f 61/97/52 72/107/52 65/108/52" + crEnt +
							"f 62/98/53 61/97/53 65/108/53 66/105/53" + crEnt +
							"f 59/99/54 68/78/54 69/104/54 63/103/54" + crEnt +
							"f 54/74/55 67/76/55 68/78/55 59/99/55" + crEnt +
							"f 57/96/56 71/102/56 72/107/56 61/97/56" + crEnt +
							"f 39/55/57 70/101/57 71/102/57 57/96/57" + crEnt +
							"f 66/105/58 69/104/58 62/98/58" + crEnt +
							"f 65/108/59 72/107/59 64/106/59";
					saveFile(objModel, model);

					String mat_lib = "newmtl top" + crEnt +
							"Kd 1.000000 1.000000 1.000000" + crEnt +
							"d 1.000000" + crEnt +
							"map_Kd customnpcs:items/" + name + crEnt + crEnt +
							"newmtl wood" + crEnt +
							"Kd 0.200000 0.050000 0.010000" + crEnt +
							"d 1.000000";
					saveFile(new File(blockModelsDir, name.toLowerCase() + ".mtl"), mat_lib);
				}
				File blockStatesDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/blockstates");
				if (!blockStatesDir.exists() && !blockStatesDir.mkdirs()) { return; }
				File itemState = new File(blockStatesDir, "custom_" + name.toLowerCase() + ".json");
				if (!itemState.exists()) {
					String state = "{" + crEnt + crTab +
							"\"forge_marker\": 1," + crEnt + crTab +
							"\"defaults\": {" + crEnt + crTab + crTab +
							"\"model\": \"customnpcs:obj/" + name + ".obj\"" + crEnt + crTab + "}," + crEnt + crTab +
							"\"variants\": {" + crEnt + crTab + crTab +
							"\"inventory\":[{" + crEnt + crTab + crTab + crTab +
							"\"transform\": {" + crEnt + crTab + crTab + crTab + crTab +
							"\"firstperson\": {" + crEnt + crTab + crTab + crTab + crTab + crTab +
							"\"rotation\": [ { \"x\": 20 } ]" + crEnt + crTab + crTab + crTab + crTab +
							"}," + crEnt + crTab + crTab + crTab + crTab +
							"\"firstperson_lefthand\": {" + crEnt + crTab + crTab + crTab + crTab + crTab +
							"\"rotation\": [ { \"x\": 20 } ]" + crEnt + crTab + crTab + crTab + crTab +
							"}," + crEnt + crTab + crTab + crTab + crTab +
							"\"thirdperson\": {" + crEnt + crTab + crTab + crTab + crTab + crTab +
							"\"translation\": [0, 0.1875, 0.0625]," + crEnt + crTab + crTab + crTab + crTab + crTab +
							"\"rotation\": [ { \"x\": -5 } ]" + crEnt + crTab + crTab + crTab + crTab +
							"}," + crEnt + crTab + crTab + crTab + crTab +
							"\"thirdperson_lefthand\": {" + crEnt + crTab + crTab + crTab + crTab + crTab +
							"\"translation\": [0, 0.1875, 0.0625]," + crEnt + crTab + crTab + crTab + crTab + crTab +
							"\"rotation\": [ { \"x\": -5 } ]" + crEnt + crTab + crTab + crTab + crTab +
							"}," + crEnt + crTab + crTab + crTab + crTab +
							"\"gui\": {" + crEnt + crTab + crTab + crTab + crTab + crTab +
							"\"translation\": [0, -0.0625, 0]," + crEnt + crTab + crTab + crTab + crTab + crTab +
							"\"rotation\": [ { \"z\": -45 }, { \"y\": 90 } ]" + crEnt + crTab + crTab + crTab + crTab +
							"}," + crEnt + crTab + crTab + crTab + crTab +
							"\"ground\": {" + crEnt + crTab + crTab + crTab + crTab + crTab +
							"\"scale\": 0.75," + crEnt + crTab + crTab + crTab + crTab + crTab +
							"\"rotation\": [ { \"x\": 45 } ]" + crEnt + crTab + crTab + crTab + crTab +
							"}," + crEnt + crTab + crTab + crTab + crTab +
							"\"head\": {" + crEnt + crTab + crTab + crTab + crTab + crTab +
							"\"translation\": [0.0625, 0.4375, -0.6875]," + crEnt + crTab + crTab + crTab + crTab + crTab +
							"\"scale\": 1.5," + crEnt + crTab + crTab + crTab + crTab + crTab +
							"\"rotation\": [ { \"x\": 60 }, { \"y\": 195 }, { \"z\": -30 } ]" + crEnt + crTab + crTab + crTab + crTab +
							"}," + crEnt + crTab + crTab + crTab + crTab +
							"\"fixed\": {" + crEnt + crTab + crTab + crTab + crTab + crTab +
							"\"rotation\": [ { \"y\": 90 } ]" + crEnt + crTab + crTab + crTab + crTab +
							"}" + crEnt + crTab + crTab + crTab +
							"}" + crEnt + crTab + crTab +  "}]," + crEnt + crTab + crTab +
							"\"normal\": [{}]" + crEnt + crTab + "}" + crEnt + "}";
					saveFile(itemState, state);
				}
				jsonModel = "{" + crEnt + crTab + "\"_comment\": \"Custom Item Axe Model created by default\"," + crEnt + crTab +
						"\"parent\": \"customnpcs:block/" + name.toLowerCase() + "\"" + crEnt + "}";

				if (saveFile(itemModel, jsonModel)) { LogWriter.debug("Create Default Item Axe Model for \"" + name + "\" item"); }
				else { LogWriter.debug("Error Create Default Item Axe Model for \"" + name + "\" item"); }
			}
			if (jsonModel.isEmpty()) {
				jsonModel = "{" + crEnt + crTab + "\"_comment\": \"Custom Item Model created by default\"," + crEnt
						+ crTab + "\"parent\": \"minecraft:item/generated\"," + crEnt + crTab + "\"textures\": {"
						+ crEnt;
				if (customitem instanceof CustomWeapon) {
					jsonModel += crTab + crTab + "\"layer0\": \"" + CustomNpcs.MODID + ":items/weapons/"
							+ name.toLowerCase() + "\"" + crEnt;
				} else if (customitem instanceof CustomTool) {
					jsonModel += crTab + crTab + "\"layer0\": \"" + CustomNpcs.MODID + ":items/" + name.toLowerCase() + "\"" + crEnt;
					jsonModel = jsonModel.replace("item/generated", "item/handheld");
				} else if (customitem instanceof CustomArmor) {
					if (((CustomArmor) customitem).objModel == null) {
						jsonModel += crTab + crTab + "\"layer0\": \"" + CustomNpcs.MODID + ":items/armor/"
								+ name.toLowerCase() + "_" + ((CustomArmor) customitem).armorType.name().toLowerCase()
								+ "\"" + crEnt;
					} else {
						jsonModel = "{" + crEnt + crTab + "\"_comment\": \"Custom Item Armor OBJ Model created by default\"," + crEnt
								+ crTab + "\"parent\": \"minecraft:item/generated\"" + crEnt + "}";
					}
				} else {
					jsonModel += crTab + crTab + "\"layer0\": \"" + CustomNpcs.MODID + ":items/" + name.toLowerCase()
							+ "\"" + crEnt;
				}
				jsonModel += crTab + "}" + crEnt + "}";
				if (saveFile(itemModel, jsonModel)) {
					LogWriter.debug("Create Default Item Model for \"" + name + "\" item");
				} else {
					LogWriter.debug("Error Create Default Item Model for \"" + name + "\" item");
				}
			}
		}
	}

	public void checkParticleFiles(ICustomElement customparticle) {

	}

	public void checkPotionFiles(ICustomElement custompotion) {

	}

	public void checkTexture(EntityNPCInterface npc) {
	}

	public void fixTileEntityData(TileEntity tile) {
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SET_TILE_DATA, tile.writeToNBT(new NBTTagCompound()));
	}

	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	public Container getContainer(EnumGuiType gui, EntityPlayer player, int x, int y, int z, EntityNPCInterface npc) {
		switch (gui) {
			case CustomContainer: {
				TileEntity tile = player.world.getTileEntity(new BlockPos(x, y, z));
				if (tile instanceof CustomTileEntityChest) {
					return ((CustomTileEntityChest) tile).createContainer(player.inventory, player);
				}
				return null;
			}
			case CustomChest: {
				return new ContainerCustomChest(player, x);
			}
			case MainMenuInv: {
				return new ContainerNPCInv(npc, player);
			}
			case MainMenuInvDrop: {
				return new ContainerNPCDropSetup(npc, player, x, y, z);
			} // New
			case ManageTransport: {
				TransportLocation loc = TransportController.getInstance().getTransport(x);
				if (loc == null) {
					loc = new TransportLocation();
					loc.id = x;
					loc.category = TransportController.getInstance().categories.get(y);
				}
				if (player.world.isRemote) {
					loc = loc.copy();
				}
				return new ContainerNPCTransportSetup(player, loc, y);
			}
			case PlayerAnvil: {
				return new ContainerCarpentryBench(player.inventory, player.world, new BlockPos(x, y, z));
			}
			case PlayerBank: {
				Bank bank = BankController.getInstance().getBank(x);
				if (bank == null) {
					bank = new Bank();
				}
				return new ContainerNPCBank(player, bank, y, z);
			}
			case PlayerFollowerHire:
            case PlayerFollower: {
				return new ContainerNPCFollowerHire(npc, player, x);
			}
            case PlayerTrader: {
				if (npc != null) {
					return new ContainerNPCTrader(npc, player);
				}
				return new ContainerNPCTrader(x, player);
			}
			case SetupItemGiver: {
				return new ContainerNpcItemGiver(npc, player);
			}
			case SetupTraderDeal: { // Change
				MarcetController mData = MarcetController.getInstance();
				Marcet marcet = (Marcet) mData.getMarcet(x);
				if (marcet == null) {
					marcet = new Marcet(x);
				}
				Deal deal = (Deal) mData.getDeal(y);
				if (deal == null) {
					deal = new Deal(y);
				}
				return new ContainerNPCTraderSetup(marcet, deal, player);
			}
			case SetupFollower: {
				return new ContainerNPCFollowerSetup(npc, player);
			}
			case QuestReward: {
				return new ContainerNpcQuestReward(player);
			}
			case QuestTypeItem: {
				return new ContainerNpcQuestTypeItem(player, x);
			}
			case QuestRewardItem: {
				return new ContainerNpcQuestRewardItem(x);
			} // New
			case ManageRecipes: {
				return new ContainerManageRecipes(player, x);
			} // Change
			case ManageBanks: {
				return new ContainerManageBanks(player);
			}
			case MerchantAdd: {
				return new ContainerMerchantAdd(player, ServerEventsHandler.Merchant, player.world);
			}
			case PlayerMailOpen: {
				return new ContainerMail(player, x == 1, y == 1);
			}
			case CompanionInv: {
				return new ContainerNPCCompanion(npc, player);
			}
			case CustomGui: {
				return new ContainerCustomGui(new InventoryBasic("", false, x));
			}
			case BuilderSetting:
            case ReplaceSetting:
            case PlacerSetting:
            case SaverSetting:
            case RemoverSetting: {
				return new ContainerBuilderSettings(player, x, y);
			}
            default: {
				return null;
			}
		}
	}

	public EntityPlayer getPlayer() {
		return null;
	}

	public PlayerData getPlayerData(EntityPlayer player) {
		if (player == null) {
			return null;
		}
		return PlayerData.get(player);
	}

	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID > EnumGuiType.values().length) {
			return null;
		}
		EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
		EnumGuiType gui = EnumGuiType.values()[ID];
		return this.getContainer(gui, player, x, y, z, npc);
	}

	public void load() {

	}

	public void openGui(EntityNPCInterface npc, EnumGuiType gui) {
	}

	public void openGui(EntityNPCInterface npc, EnumGuiType gui, int x, int y, int z) {
	}

	public void openGui(EntityPlayer player, Object guiscreen) {
	}

	public void openGui(int i, int j, int k, EnumGuiType gui, EntityPlayer player) {
	}

	public void postload() {
	}

	public void preload() {
		CustomNpcs.Channel.register(new PacketHandlerServer());
		CustomNpcs.ChannelPlayer.register(new PacketHandlerPlayer());
	}

	public void reloadItemTextures() {
	}

	public static String loadFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8));
		String line;
		StringBuilder text = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			text.append(line).append((char) 10);
		}
		reader.close();
		return text.toString();
	}

	public static boolean saveFile(File file, String text) {
		if (file == null || text == null || text.isEmpty()) {
			return false;
		}
		if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
			LogWriter.debug("Error create path File \"" + file + "\"");
			return false;
		}
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
            writer.write(text);
        } catch (IOException e) {
            LogWriter.debug("Error Save Default Item File \"" + file + "\"");
            return false;
        }
		return true;
	}

	public void spawnParticle(EntityLivingBase player, String string, Object... ob) {
	}

	public void spawnParticle(EnumParticleTypes type, double x, double y, double z, double motionX, double motionY,
			double motionZ, float scale) {
	}

	public void updateGUI() {
	}

	public void updateKeys() {
	}

	public void updateRecipeBook(EntityPlayer player) {
		if (!(player instanceof EntityPlayerMP)) {
			return;
		}
		RecipeBook book = ((EntityPlayerMP) player).getRecipeBook();
        RecipeController rData = RecipeController.getInstance();
		BitSet recipes = ObfuscationHelper.getValue(RecipeBook.class, book, 0);
		BitSet newRecipes = ObfuscationHelper.getValue(RecipeBook.class, book, 1);
		List<Integer> delIDs = Lists.newArrayList();
        assert recipes != null;
        for (int id = recipes.nextSetBit(0); id >= 0; id = recipes.nextSetBit(id + 1)) {
			INpcRecipe recipe = rData.getRecipe(id);
			if (recipe == null) {
				delIDs.add(id);
			} else if (!CraftingManager.REGISTRY.containsKey(Objects.requireNonNull(((IRecipe) recipe).getRegistryName()))
					|| CraftingManager.REGISTRY.getObjectById(id) == null) {
				delIDs.add(id);
			}
		}
		if (!delIDs.isEmpty()) {
			for (int id : delIDs) {
				recipes.clear(id);
			}
		}
		delIDs.clear();
        assert newRecipes != null;
        for (int id = newRecipes.nextSetBit(0); id >= 0; id = newRecipes.nextSetBit(id + 1)) {
			INpcRecipe recipe = rData.getRecipe(id);
			if (recipe == null) {
				delIDs.add(id);
			} else if (!CraftingManager.REGISTRY.containsKey(Objects.requireNonNull(((IRecipe) recipe).getRegistryName()))
					|| CraftingManager.REGISTRY.getObjectById(id) == null) {
				delIDs.add(id);
			}
		}
		if (!delIDs.isEmpty()) {
			for (int id : delIDs) {
				newRecipes.clear(id);
			}
		}
		ObfuscationHelper.setValue(RecipeBook.class, book, recipes, 0);
		ObfuscationHelper.setValue(RecipeBook.class, book, newRecipes, 1);
		player.unlockRecipes(RecipeController.getInstance().getKnownRecipes());
	}

	public void updateRecipes(INpcRecipe recipe, boolean needSend, boolean delete, String debug) {
		List<EntityPlayerMP> players = CustomNpcs.Server != null
				? CustomNpcs.Server.getPlayerList().getPlayers()
				: Lists.newArrayList();
		// Update Recipe
		if (recipe != null) {
			IRecipe r = RecipeController.Registry.getValue(((IRecipe) recipe).getRegistryName());
			RecipeController.Registry.unfreeze();
			if (delete) {
				if (r != null) {
					RecipeController.Registry.remove(r.getRegistryName());
				}
			} else {
				if (recipe.isValid()) {
					if (r == null) {
						RecipeController.Registry.register((IRecipe) recipe);
						r = RecipeController.Registry.getValue(((IRecipe) recipe).getRegistryName());
					}
					if (!(r instanceof INpcRecipe) || r.getClass() != recipe.getClass()) {
                        assert r != null;
                        RecipeController.Registry.remove(r.getRegistryName());
						RecipeController.Registry.register((IRecipe) recipe);
						r = RecipeController.Registry.getValue(((IRecipe) recipe).getRegistryName());
					}
				} else {
					r = null;
				}
				if (r != null) {
					((INpcRecipe) r).copy(recipe);
				}
			}
			RecipeController.Registry.freeze();
			if (needSend) {
				NBTTagCompound nbt = recipe.getNbt().getMCNBT();
				if (delete) {
					nbt.setBoolean("delete", true);
				}
				for (EntityPlayerMP player : players) {
					this.updateRecipeBook(player);
					Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.RecipesData, nbt);
				}
			}
		}

		// Update All Recipes
		if (RecipeController.Registry == null) { return; }
		// Delete Old
		List<ResourceLocation> del = Lists.newArrayList();
		RecipeController.Registry.unfreeze();
		for (IRecipe rec : RecipeController.Registry) {
			if (!(rec instanceof INpcRecipe)) {
				continue;
			}
			INpcRecipe r = RecipeController.getInstance().getRecipe(rec.getRegistryName());
			if (r == null || !r.isValid()) {
				del.add(rec.getRegistryName());
			}
		}
		if (!del.isEmpty()) {
			for (ResourceLocation rl : del) {
				RecipeController.Registry.remove(rl);
			}
		}
		if (recipe != null) {
			return;
		}

		// Added New or Reload
		for (int i = 0; i < 2; i++) {
			for (List<INpcRecipe> list : (i == 0 ? RecipeController.getInstance().globalList.values()
					: RecipeController.getInstance().modList.values())) {
				for (INpcRecipe rec : list) {
					if (!rec.isValid()) {
						continue;
					}
					IRecipe r = RecipeController.Registry.getValue(((IRecipe) rec).getRegistryName());
					if (r == null) {
						RecipeController.Registry.register((IRecipe) rec);
						r = RecipeController.Registry.getValue(((IRecipe) rec).getRegistryName());
					} else if (r.getClass() != rec.getClass()) {
						RecipeController.Registry.remove(r.getRegistryName());
						RecipeController.Registry.register((IRecipe) rec);
						r = RecipeController.Registry.getValue(((IRecipe) rec).getRegistryName());
					}
					if (r == null) {
						continue;
					}
					((INpcRecipe) r).copy(rec);
					int nowID = RecipeController.Registry.getID(r);
					if (rec.getClass() == NpcShapedRecipes.class) {
						((NpcShapedRecipes) rec).id = nowID;
					} else {
						((NpcShapelessRecipes) rec).id = nowID;
					}
				}
			}
		}
		RecipeController.Registry.freeze();
		// Changed in Players
		for (EntityPlayerMP player : players) {
			this.updateRecipeBook(player);
			RecipeController.getInstance().sendTo(player);
		}
	}

	public void clearKeys() { }

	public boolean isLoadTexture(ResourceLocation resource) { return false; }

}
