package noppes.npcs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.blocks.CustomBlock;
import noppes.npcs.blocks.CustomBlockPortal;
import noppes.npcs.blocks.CustomBlockSlab;
import noppes.npcs.blocks.CustomBlockStairs;
import noppes.npcs.blocks.CustomLiquid;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.containers.ContainerBuilderSettings;
import noppes.npcs.containers.ContainerCarpentryBench;
import noppes.npcs.containers.ContainerCustomChest;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.containers.ContainerManageBanks;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.containers.ContainerMerchantAdd;
import noppes.npcs.containers.ContainerNPCBankLarge;
import noppes.npcs.containers.ContainerNPCBankSmall;
import noppes.npcs.containers.ContainerNPCBankUnlock;
import noppes.npcs.containers.ContainerNPCBankUpgrade;
import noppes.npcs.containers.ContainerNPCCompanion;
import noppes.npcs.containers.ContainerNPCDropSetup;
import noppes.npcs.containers.ContainerNPCFollower;
import noppes.npcs.containers.ContainerNPCFollowerHire;
import noppes.npcs.containers.ContainerNPCFollowerSetup;
import noppes.npcs.containers.ContainerNPCInv;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.containers.ContainerNPCTraderSetup;
import noppes.npcs.containers.ContainerNpcItemGiver;
import noppes.npcs.containers.ContainerNpcQuestReward;
import noppes.npcs.containers.ContainerNpcQuestRewardItem;
import noppes.npcs.containers.ContainerNpcQuestTypeItem;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.CustomArmor;
import noppes.npcs.items.CustomBow;
import noppes.npcs.items.CustomFishingRod;
import noppes.npcs.items.CustomShield;
import noppes.npcs.items.CustomTool;
import noppes.npcs.items.CustomWeapon;
import noppes.npcs.items.crafting.NpcShapedRecipes;
import noppes.npcs.items.crafting.NpcShapelessRecipes;
import noppes.npcs.util.BuilderData;
import noppes.npcs.util.ObfuscationHelper;

public class CommonProxy
implements IGuiHandler {
	
	public boolean newVersionAvailable;
	public int revision;
	public static Map<Integer, BuilderData> dataBuilder = Maps.<Integer, BuilderData>newTreeMap();

	public CommonProxy() {
		this.newVersionAvailable = false;
		this.revision = 4;
	}

	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	public Container getContainer(EnumGuiType gui, EntityPlayer player, int x, int y, int z, EntityNPCInterface npc) { // Changed
		switch (gui) {
			case CustomChest: { return new ContainerCustomChest(player, x); }
			case MainMenuInv: { return new ContainerNPCInv(npc, player); }
			case MainMenuInvDrop: { return new ContainerNPCDropSetup(npc, player, x, y, z); } // New
			case PlayerAnvil: { return new ContainerCarpentryBench(player.inventory, player.world, new BlockPos(x, y, z)); }
			case PlayerBankSmall: { return new ContainerNPCBankSmall(player, x, y); }
			case PlayerBankUnlock: { return new ContainerNPCBankUnlock(player, x, y); }
			case PlayerBankUprade: { return new ContainerNPCBankUpgrade(player, x, y); }
			case PlayerBankLarge: { return new ContainerNPCBankLarge(player, x, y); }
			case PlayerFollowerHire: { return new ContainerNPCFollowerHire(npc, player); }
			case PlayerFollower: { return new ContainerNPCFollower(npc, player); }
			case PlayerTrader: { return new ContainerNPCTrader(npc, player); }
			case SetupItemGiver: { return new ContainerNpcItemGiver(npc, player); }
			case SetupTrader: { // Change
				Marcet marcet = MarcetController.getInstance().getMarcet(x);
				if (marcet == null) { marcet = new Marcet(); }
				return new ContainerNPCTraderSetup(marcet, y, player);
			}
			case SetupFollower: { return new ContainerNPCFollowerSetup(npc, player); }
			case QuestReward: { return new ContainerNpcQuestReward(player); }
			case QuestTypeItem: { return new ContainerNpcQuestTypeItem(player, y); } // Change
			case QuestRewardItem: { return new ContainerNpcQuestRewardItem(player, x); } // New
			case ManageRecipes: { return new ContainerManageRecipes(player, x); } // Change
			case ManageBanks: { return new ContainerManageBanks(player); }
			case MerchantAdd: { return new ContainerMerchantAdd(player, (IMerchant) ServerEventsHandler.Merchant, player.world); }
			case PlayerMailman: { return new ContainerMail(player, x == 1, y == 1); }
			case CompanionInv: { return new ContainerNPCCompanion(npc, player); }
			case CustomGui: { return new ContainerCustomGui(getPlayer(), new InventoryBasic("", false, x)); }
			case BuilderSetting: { return new ContainerBuilderSettings(player, x); } // New
			default: { return null; }
		}
	}

	public EntityPlayer getPlayer() {
		return null;
	}

	public PlayerData getPlayerData(EntityPlayer player) {
		if (player == null) { return null; }
		return PlayerData.get(player);
	}

	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID > EnumGuiType.values().length) { return null; }
		EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
		EnumGuiType gui = EnumGuiType.values()[ID];
		return this.getContainer(gui, player, x, y, z, npc);
	}

	public boolean hasClient() {
		return false;
	}

	public void preload() {
		CustomNpcs.Channel.register(new PacketHandlerServer());
		CustomNpcs.ChannelPlayer.register(new PacketHandlerPlayer());
	}

	public void openGui(EntityNPCInterface npc, EnumGuiType gui) {
	}

	public void openGui(EntityNPCInterface npc, EnumGuiType gui, int x, int y, int z) {
	}

	public void openGui(EntityPlayer player, Object guiscreen) {
	}

	public void openGui(int i, int j, int k, EnumGuiType gui, EntityPlayer player) {
	}

	public void load() {

	}

	public void postload() {

	}

	public void spawnParticle(EntityLivingBase player, String string, Object... ob) {
	}

	public void spawnParticle(EnumParticleTypes type, double x, double y, double z, double motionX, double motionY,
			double motionZ, float scale) {
	}

	public void updateGUI() {
	}

	/**
	 * Common ForgeRegistry<IRecipe>
	 * @param recipe
	 * @param needSend
	 * @param delete
	 */
	public void updateRecipes(INpcRecipe recipe, boolean needSend, boolean delete, String debug) {
		List<EntityPlayerMP> players = CustomNpcs.Server != null && CustomNpcs.Server.getPlayerList() != null ? CustomNpcs.Server.getPlayerList().getPlayers() : Lists.<EntityPlayerMP>newArrayList();
		/** Update Recipe */
		if (recipe != null) {
			IRecipe r = RecipeController.Registry.getValue(((IRecipe) recipe).getRegistryName());
			RecipeController.Registry.unfreeze();
			if (delete) {
				if (r!=null) {
					RecipeController.Registry.remove(r.getRegistryName());
				}
			} else {
				if (recipe.isValid()) {
					if (r == null) {
						RecipeController.Registry.register((IRecipe) recipe);
						r = RecipeController.Registry.getValue(((IRecipe) recipe).getRegistryName());
					}
					if (!(r instanceof INpcRecipe) || r.getClass()!=recipe.getClass()) {
						RecipeController.Registry.remove(r.getRegistryName());
						RecipeController.Registry.register((IRecipe) recipe);
						r = RecipeController.Registry.getValue(((IRecipe) recipe).getRegistryName());
					}
				}
				else { r = null; }
				if (r!=null) {
					((INpcRecipe) r).copy(recipe);
				}
			}
			RecipeController.Registry.freeze();
			if (needSend) {
				NBTTagCompound nbt = recipe.writeNBT();
				if (delete) { nbt.setBoolean("delete", true); }
				for (EntityPlayerMP player : players) {
					this.updateRecipeBook(player);
					Server.sendData(player, EnumPacketClient.SYNC_UPDATE, 6, nbt);
				}
			}
		}
		
		/** Update All Recipes */
		// Delete Old
		List<ResourceLocation> del = Lists.<ResourceLocation>newArrayList();
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
		if (del.size()>0) {
			for (ResourceLocation rl : del) {
				RecipeController.Registry.remove(rl);
			}
		}
		if (recipe != null) { return; }
		
		// Added New or Reload
		for (int i = 0; i < 2; i++) {
			for (List<INpcRecipe> list : (i == 0 ? RecipeController.getInstance().globalList.values() : RecipeController.getInstance().modList.values())) {
				for (INpcRecipe rec : list) {
					if (!rec.isValid()) { continue; }
					IRecipe r = RecipeController.Registry.getValue(((IRecipe) rec).getRegistryName());
					if (r == null) {
						RecipeController.Registry.register((IRecipe) rec);
						r = RecipeController.Registry.getValue(((IRecipe) rec).getRegistryName());
					}
					else if (r.getClass()!=rec.getClass()) {
						RecipeController.Registry.remove(r.getRegistryName());
						RecipeController.Registry.register((IRecipe) rec);
						r = RecipeController.Registry.getValue(((IRecipe) rec).getRegistryName());
					}
					if (r==null) { continue; }
					((INpcRecipe) r).copy(rec);
					int nowID = RecipeController.Registry.getID(r);
					if (rec.getClass()==NpcShapedRecipes.class) {
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

	public void updateRecipeBook(EntityPlayer player) {
		if (!(player instanceof EntityPlayerMP)) { return; }
		RecipeBook book = ((EntityPlayerMP) player).getRecipeBook();
		if (book == null) { return; }
		RecipeController rData = RecipeController.getInstance();
		BitSet recipes = ObfuscationHelper.getValue(RecipeBook.class, book, 0);
		BitSet newRecipes = ObfuscationHelper.getValue(RecipeBook.class, book, 1);
		List<Integer> delIDs = Lists.<Integer>newArrayList();
		for (int id = recipes.nextSetBit(0); id >= 0; id = recipes.nextSetBit(id + 1)) {
			INpcRecipe recipe = rData.getRecipe(id);
			if (recipe==null) { delIDs.add(id); }
			else if (!CraftingManager.REGISTRY.containsKey(((IRecipe) recipe).getRegistryName()) || CraftingManager.REGISTRY.getObjectById(id) == null) { delIDs.add(id); }
		}
		if (delIDs.size() > 0) {
			for (int id : delIDs) { recipes.clear(id); }
		}
		delIDs.clear();
		for (int id = newRecipes.nextSetBit(0); id >= 0; id = newRecipes.nextSetBit(id + 1)) {
			INpcRecipe recipe = rData.getRecipe(id);
			if (recipe==null) { delIDs.add(id); }
			else if (!CraftingManager.REGISTRY.containsKey(((IRecipe) recipe).getRegistryName()) || CraftingManager.REGISTRY.getObjectById(id) == null) { delIDs.add(id); }
		}
		if (delIDs.size() > 0) {
			for (int id : delIDs) { newRecipes.clear(id); }
		}
		ObfuscationHelper.setValue(RecipeBook.class, book, recipes, 0);
		ObfuscationHelper.setValue(RecipeBook.class, book, newRecipes, 1);
		player.unlockRecipes(RecipeController.getInstance().getKnownRecipes());
	}

	public void checkBlockFiles(ICustomElement customblock) {
		String name = customblock.getCustomName();
		String fileName = ((Block) customblock).getRegistryName().getResourcePath();
		File blockstatesDir = new File(CustomNpcs.Dir, "assets/customnpcs/blockstates");
		if (!blockstatesDir.exists()) { blockstatesDir.mkdirs(); }
		
		File blockModelsDir = new File(CustomNpcs.Dir, "assets/customnpcs/models/block");
		File itemModelsDir = new File(CustomNpcs.Dir, "assets/customnpcs/models/item");
		if (!blockModelsDir.exists()) { blockModelsDir.mkdirs(); }
		if (!itemModelsDir.exists()) { itemModelsDir.mkdirs(); }
		String crEnt = ""+((char) 10);
		String crTab = ""+((char) 9);
		
		File blockstate = new File(blockstatesDir, fileName.toLowerCase()+".json");
		if (!blockstate.exists()) {
			String jsonState = "";
			if (customblock instanceof CustomLiquid) {
				jsonState = "{" + crEnt +
					crTab + "\"_comment\": \"Custom Block Fluid created by default\"," + crEnt +
					crTab + "\"forge_marker\": 1," + crEnt +
					crTab + "\"defaults\": {" + crEnt +
					crTab + crTab + "\"textures\": {" + crEnt +
					crTab + crTab + crTab + "\"particle\": \""+CustomNpcs.MODID+":fluids/"+fileName+"_flow\"," + crEnt +
					crTab + crTab + crTab + "\"all\": \""+CustomNpcs.MODID+":fluids/"+fileName+"_flow\"" + crEnt +
					crTab + crTab + "}," + crEnt +
					crTab + crTab + "\"model\": \"forge:fluid\"," + crEnt +
					crTab + crTab + "\"custom\": { \"fluid\": \""+fileName+"\" }," + crEnt +
					crTab + crTab + "\"uvlock\": false" + crEnt +
					crTab + "}," + crEnt +
					crTab + "\"variants\": {" + crEnt +
					crTab + crTab + "\"normal\": [{ }]," + crEnt +
					crTab + crTab + "\"inventory\": [{ }]," + crEnt +
					crTab + crTab + "\"level\": {" + crEnt +
					crTab + crTab + crTab + "\"0\": { }," + crEnt +
					crTab + crTab + crTab + "\"1\": { }," + crEnt +
					crTab + crTab + crTab + "\"2\": { }," + crEnt +
					crTab + crTab + crTab + "\"3\": { }," + crEnt +
					crTab + crTab + crTab + "\"4\": { }," + crEnt +
					crTab + crTab + crTab + "\"5\": { }," + crEnt +
					crTab + crTab + crTab + "\"6\": { }," + crEnt +
					crTab + crTab + crTab + "\"7\": { }," + crEnt +
					crTab + crTab + crTab + "\"8\": { }," + crEnt +
					crTab + crTab + crTab + "\"9\": { }," + crEnt +
					crTab + crTab + crTab + "\"10\": { }," + crEnt +
					crTab + crTab + crTab + "\"11\": { }," + crEnt +
					crTab + crTab + crTab + "\"12\": { }," + crEnt +
					crTab + crTab + crTab + "\"13\": { }," + crEnt +
					crTab + crTab + crTab + "\"14\": { }," + crEnt +
					crTab + crTab + crTab + "\"15\": { }" + crEnt +
					crTab + crTab + "}" + crEnt +
					crTab + "}" + crEnt +
					"}";
			}
			else if (customblock instanceof CustomBlockStairs) {
				jsonState = "{" + crEnt +
					crTab + "\"_comment\": \"Custom Block Stairs created by default\"," + crEnt +
					crTab + "\"variants\": {" + crEnt +
					crTab + crTab + "\"facing=east,half=bottom,shape=straight\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"\" }," + crEnt +
					crTab + crTab + "\"facing=west,half=bottom,shape=straight\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"\", \"y\": 180, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=south,half=bottom,shape=straight\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"\", \"y\": 90, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=north,half=bottom,shape=straight\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"\", \"y\": 270, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=east,half=bottom,shape=outer_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\" }," + crEnt +
					crTab + crTab + "\"facing=west,half=bottom,shape=outer_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\", \"y\": 180, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=south,half=bottom,shape=outer_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\", \"y\": 90, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=north,half=bottom,shape=outer_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\", \"y\": 270, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=east,half=bottom,shape=outer_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\", \"y\": 270, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=west,half=bottom,shape=outer_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\", \"y\": 90, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=south,half=bottom,shape=outer_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\" }," + crEnt +
					crTab + crTab + "\"facing=north,half=bottom,shape=outer_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\", \"y\": 180, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=east,half=bottom,shape=inner_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\" }," + crEnt +
					crTab + crTab + "\"facing=west,half=bottom,shape=inner_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\", \"y\": 180, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=south,half=bottom,shape=inner_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\", \"y\": 90, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=north,half=bottom,shape=inner_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\", \"y\": 270, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=east,half=bottom,shape=inner_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\", \"y\": 270, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=west,half=bottom,shape=inner_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\", \"y\": 90, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=south,half=bottom,shape=inner_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\" }," + crEnt +
					crTab + crTab + "\"facing=north,half=bottom,shape=inner_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\", \"y\": 180, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=east,half=top,shape=straight\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"\", \"x\": 180, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=west,half=top,shape=straight\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"\", \"x\": 180, \"y\": 180, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=south,half=top,shape=straight\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"\", \"x\": 180, \"y\": 90, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=north,half=top,shape=straight\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"\", \"x\": 180, \"y\": 270, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=east,half=top,shape=outer_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\", \"x\": 180, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=west,half=top,shape=outer_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\", \"x\": 180, \"y\": 180, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=south,half=top,shape=outer_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\", \"x\": 180, \"y\": 90, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=north,half=top,shape=outer_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\", \"x\": 180, \"y\": 270, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=east,half=top,shape=outer_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\", \"x\": 180, \"y\": 90, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=west,half=top,shape=outer_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\", \"x\": 180, \"y\": 270, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=south,half=top,shape=outer_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\", \"x\": 180, \"y\": 180, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=north,half=top,shape=outer_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_outer\", \"x\": 180, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=east,half=top,shape=inner_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\", \"x\": 180, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=west,half=top,shape=inner_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\", \"x\": 180, \"y\": 180, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=south,half=top,shape=inner_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\", \"x\": 180, \"y\": 90, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=north,half=top,shape=inner_right\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\", \"x\": 180, \"y\": 270, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=east,half=top,shape=inner_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\", \"x\": 180, \"y\": 90, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=west,half=top,shape=inner_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\", \"x\": 180, \"y\": 270, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=south,half=top,shape=inner_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\", \"x\": 180, \"y\": 180, \"uvlock\": true }," + crEnt +
					crTab + crTab + "\"facing=north,half=top,shape=inner_left\": { \"model\": \"customnpcs:"+fileName.toLowerCase()+"_inner\", \"x\": 180, \"uvlock\": true }" + crEnt +
					crTab + "}" + crEnt + "}";
			}
			else if (customblock instanceof CustomBlockSlab) {
				if (customblock instanceof CustomBlockSlab.CustomBlockSlabSingle) {
					jsonState = "{" + crEnt +
						crTab + "\"_comment\": \"Custom Block Slab created by default\"," + crEnt +
						crTab + "\"variants\": {" + crEnt +
						crTab + crTab + "\"half=bottom,type=normal\": { \"model\": \"customnpcs:bottom_"+fileName.toLowerCase()+"\" }," + crEnt +
						crTab + crTab + "\"half=top,type=normal\": { \"model\": \"customnpcs:upper_"+fileName.toLowerCase()+"\" }," + crEnt +
						crTab + crTab + "\"inventory\": { \"model\": \"customnpcs:bottom_"+fileName.toLowerCase()+"\" }" + crEnt +
						crTab + "}" + crEnt + "}";
				} else {
					jsonState = "{" + crEnt +
						crTab + "\"_comment\": \"Custom Block Double Slab created by default\"," + crEnt +
						crTab + "\"variants\": {" + crEnt +
						crTab + crTab + "\"type=normal\":  { \"model\": \"customnpcs:custom_blockexample\" }," + crEnt +
						crTab + crTab + "\"inventory\":  { \"model\": \"customnpcs:custom_blockexample\" }" + crEnt +
						crTab + "}" + crEnt + "}";
				}
			}
			else if (customblock instanceof CustomBlock && ((CustomBlock) customblock).hasProperty()) {
				NBTTagCompound data = ((CustomBlock) customblock).nbtData.getCompoundTag("Property");
				if (data.getByte("Type")==(byte) 4) {
					jsonState = "{" + crEnt +
							crTab + "\"_comment\": \"Custom Fasing Block created by default\"," + crEnt +
							crTab + "\"variants\": {" + crEnt +
							crTab + crTab + "\"normal\": { \"model\": \""+CustomNpcs.MODID+":"+fileName.toLowerCase()+"\" }, " + crEnt +
							crTab + crTab + "\"inventory\": { \"model\": \""+CustomNpcs.MODID+":"+fileName.toLowerCase()+"\" }, " + crEnt;
					for (EnumFacing ef : EnumFacing.VALUES) {
						if (ef==EnumFacing.DOWN || ef==EnumFacing.UP) { continue; }
						jsonState += crTab + crTab + "\"" + data.getString("Name") + "=" + ef.getName2() + "\": { \"model\": \""+CustomNpcs.MODID+":"+fileName.toLowerCase()+"\"";
						if (ef==EnumFacing.SOUTH) { jsonState += ",\"y\": 180"; }
						else if (ef==EnumFacing.WEST) { jsonState += ",\"y\": 270"; }
						else if (ef==EnumFacing.EAST) { jsonState += ",\"y\": 90"; }
						jsonState += " }," + crEnt;
					}
					jsonState = jsonState.substring(0, jsonState.length()-2) + crEnt + crTab + "}" + crEnt + "}";
					
				}
			}
			if (jsonState.isEmpty()) {
				jsonState = "{" + crEnt +
						crTab + "\"_comment\": \"Custom Block created by default\"," + crEnt +
						crTab + "\"variants\": {" + crEnt +
						crTab + crTab + "\"normal\": { \"model\": \""+CustomNpcs.MODID+":"+fileName.toLowerCase()+"\" }," + crEnt + 
						crTab + crTab + "\"inventory\": { \"model\": \""+CustomNpcs.MODID+":"+fileName.toLowerCase()+"\" }" + crEnt + 
						crTab + "}" + crEnt + "}";
			}
			if (this.saveFile(blockstate, jsonState)) {
				LogWriter.debug("Create Default Blockstate for \""+fileName.toLowerCase()+"\" block");
			}
		}
		
		File blockModel = new File(blockModelsDir, fileName.toLowerCase()+".json");
		if (customblock instanceof CustomBlockSlab.CustomBlockSlabSingle) {
			blockModel = new File(blockModelsDir, "bottom_"+fileName.toLowerCase()+".json");
		}
		if (!blockModel.exists()) {
			String jsonModel = "";
			if (customblock instanceof CustomBlockStairs) {
				jsonModel = "{" + crEnt +
						crTab + "\"_comment\": \"Custom Stairs Block created by default\"," + crEnt +
						crTab + "\"parent\": \"block/stairs\"," + crEnt +
						crTab + "\"textures\": {" + crEnt +
						crTab + crTab + "\"top\": \""+CustomNpcs.MODID+":blocks/"+fileName.toLowerCase()+"_top\"," + crEnt + 
						crTab + crTab + "\"bottom\": \""+CustomNpcs.MODID+":blocks/"+fileName.toLowerCase()+"_bottom\"," + crEnt + 
						crTab + crTab + "\"side\": \""+CustomNpcs.MODID+":blocks/"+fileName.toLowerCase()+"_side\"" + crEnt + 
						crTab + "}" + crEnt + "}";
				if (this.saveFile(blockModel, jsonModel)) {
					LogWriter.debug("Create Default Stairs Block Model for \""+fileName.toLowerCase()+"\" block");
				}
				jsonModel = jsonModel.replace("block/stairs", "block/inner_stairs");
				if (this.saveFile(new File(blockModelsDir, fileName.toLowerCase()+"_inner.json"), jsonModel)) {
					LogWriter.debug("Create Default Inner Stairs Block Model for \""+fileName.toLowerCase()+"\" block");
				}
				jsonModel = jsonModel.replace("block/inner_stairs", "block/outer_stairs");
				if (this.saveFile(new File(blockModelsDir, fileName.toLowerCase()+"_outer.json"), jsonModel)) {
					LogWriter.debug("Create Default Outer Stairs Block Model for \""+fileName.toLowerCase()+"\" block");
				}
			}
			else if (customblock instanceof CustomBlockSlab) {
				if (customblock instanceof CustomBlockSlab.CustomBlockSlabSingle) {
					jsonModel = "{" + crEnt +
							crTab + "\"_comment\": \"Custom Slab Block created by default\"," + crEnt +
							crTab + "\"parent\": \"block/half_slab\"," + crEnt +
							crTab + "\"textures\": {" + crEnt +
							crTab + crTab + "\"top\": \""+CustomNpcs.MODID+":blocks/"+fileName.toLowerCase()+"_top\"," + crEnt + 
							crTab + crTab + "\"bottom\": \""+CustomNpcs.MODID+":blocks/"+fileName.toLowerCase()+"_bottom\"," + crEnt + 
							crTab + crTab + "\"side\": \""+CustomNpcs.MODID+":blocks/"+fileName.toLowerCase()+"_side\"" + crEnt + 
							crTab + "}" + crEnt + "}";
					this.saveFile(blockModel, jsonModel);
					jsonModel = jsonModel.replace("block/half_slab", "block/upper_slab");
					if (this.saveFile(new File(blockModelsDir, "upper_"+fileName.toLowerCase()+".json"), jsonModel)) {
						LogWriter.debug("Create Default Slab Block Model for \""+fileName.toLowerCase()+"\" block");
					}
				}
			}
			else {
				if (customblock instanceof CustomBlock && ((CustomBlock) customblock).hasProperty()) {
					if (((CustomBlock) customblock).INT!=null) {
						
					}
					else if (((CustomBlock) customblock).FACING!=null) {
						jsonModel = "{" + crEnt +
								crTab + "\"_comment\": \"Custom Fasing Block created by default\"," + crEnt +
								crTab + "\"parent\": \"block/orientable\"," + crEnt +
								crTab + "\"textures\": {" + crEnt +
								crTab + crTab + "\"top\": \""+CustomNpcs.MODID+":blocks/"+name.toLowerCase()+"_top\"," + crEnt + 
								crTab + crTab + "\"front\": \""+CustomNpcs.MODID+":blocks/"+name.toLowerCase()+"_front\"," + crEnt + 
								crTab + crTab + "\"side\": \""+CustomNpcs.MODID+":blocks/"+name.toLowerCase()+"_side\"" + crEnt + 
								crTab + "}" + crEnt + "}";
						if (this.saveFile(blockModel, jsonModel)) {
							LogWriter.debug("Create Default Fasing Block Model for \""+fileName.toLowerCase()+"\" block");
						}
					}
				}
			}
			if (jsonModel.isEmpty()) {
				String texture = CustomNpcs.MODID+":blocks/"+name.toLowerCase();
				if (customblock instanceof CustomBlockPortal) { texture = CustomNpcs.MODID+":environment/custom_"+name.toLowerCase()+"_portal"; }
				jsonModel = "{" + crEnt +
						crTab + "\"_comment\": \"Custom Block Model created by default\"," + crEnt +
						crTab + "\"parent\": \"block/cube_all\"," + crEnt +
						crTab + "\"textures\": {" + crEnt +
						crTab + crTab + "\"all\": \""+texture+"\"" + crEnt + 
						crTab + "}" + crEnt + "}";
				if (this.saveFile(blockModel, jsonModel)) {
					LogWriter.debug("Create Default Block Model for \""+fileName.toLowerCase()+"\" block");
				}
			}
		}
		
		File itemModel = new File(itemModelsDir, fileName.toLowerCase()+".json");
		if (!itemModel.exists()) {
			String jsonStr = "{" + crEnt +
					crTab + "\"_comment\": \"Custom Item Block created by default\"," + crEnt +
					crTab + "\"parent\": \"" + CustomNpcs.MODID + ":block/" + fileName.toLowerCase() + "\"," + crEnt +
					crTab + "\"display\": {" + crEnt +
					crTab + crTab + "\"thirdperson\": {" + crEnt +
					crTab + crTab + crTab + "\"rotation\": [ 10, -45, 170 ]," + crEnt +
					crTab + crTab + crTab + "\"translation\": [ 0, 1.5, -2.75 ]," + crEnt +
					crTab + crTab + crTab + "\"scale\": [ 0.375, 0.375, 0.375 ]" + crEnt +
					crTab + crTab + "}" + crEnt +
					crTab + "}" + crEnt +
					"}";

			if (this.saveFile(itemModel, jsonStr)) {
				LogWriter.debug("Create Default Block Item Model for \""+name+"\" block");
			}
		}
	}

	public void checkItemFiles(ICustomElement customitem) {
		String name = customitem.getCustomName();
		String fileName = ((Item) customitem).getRegistryName().getResourcePath();

		File itemModelsDir = new File(CustomNpcs.Dir, "assets/customnpcs/models/item");
		if (!itemModelsDir.exists()) { itemModelsDir.mkdirs(); }
		File itemModel = new File(itemModelsDir, fileName.toLowerCase()+".json");

		String crEnt = ""+((char) 10);
		String crTab = ""+((char) 9);
		String jsonModel = "";
		if (!itemModel.exists()) {
			if (customitem instanceof CustomShield || customitem instanceof CustomBow) {
				boolean isBow = (customitem instanceof CustomBow);
				jsonModel = "{" + crEnt +
						crTab + "\"_comment\": \"Custom "+(isBow ? "Bow" : "Shield")+" Item Model created by default\"," + crEnt +
						crTab + "\"parent\": \"item/generated\"," + crEnt + 
						crTab + "\"textures\": {" + crEnt +
						crTab + crTab + "\"layer0\": \""+CustomNpcs.MODID+":items/"+(isBow ? "weapons/"+name +"_standby": name)+"\"" + crEnt +
						crTab + "}," + crEnt +
						crTab + "\"display\": {" + crEnt +
						crTab + crTab + "\"thirdperson_righthand\": {" + crEnt +
						crTab + crTab + crTab + "\"rotation\": [ "+(isBow ? "-80, 260, -40": "135, 270, 0")+" ]," + crEnt +
						crTab + crTab + crTab + "\"translation\": [ "+(isBow ? "-1, -2, 2.5": "3, -4, 3")+" ]," + crEnt +
						crTab + crTab + crTab + "\"scale\": [ "+(isBow ? "0.9, 0.9, 0.9": "1.25, 1.25, 1.25")+" ]" + crEnt +
						crTab + crTab + "}," + crEnt +
						crTab + crTab + "\"thirdperson_lefthand\": {" + crEnt +
						crTab + crTab + crTab + "\"rotation\": [ "+(isBow ? "-80, -280, 40": "135, 90, 0")+" ]," + crEnt +
						crTab + crTab + crTab + "\"translation\": [ "+(isBow ? "-1, -2, 2.5": "3, -4, 4")+" ]," + crEnt +
						crTab + crTab + crTab + "\"scale\": [ "+(isBow ? "0.9, 0.9, 0.9": "1.25, 1.25, 1.25")+" ]" + crEnt +
						crTab + crTab + "}," + crEnt +
						crTab + crTab + "\"firstperson_righthand\": {" + crEnt +
						crTab + crTab + crTab + "\"rotation\": [ "+(isBow ? "0, -90, 25": "0, 0, -135")+" ]," + crEnt +
						crTab + crTab + crTab + "\"translation\": [ "+(isBow ? "1.13, 3.2, 1.13": "3, -2, 0")+" ]," + crEnt +
						crTab + crTab + crTab + "\"scale\": [ "+(isBow ? "0.68, 0.68, 0.68": "1, 1, 1")+" ]" + crEnt +
						crTab + crTab + "}," + crEnt +
						crTab + crTab + "\"firstperson_lefthand\": {" + crEnt +
						crTab + crTab + crTab + "\"rotation\": [ "+(isBow ? "0, 90, -25": "0, 0, -45")+" ]," + crEnt +
						crTab + crTab + crTab + "\"translation\": [ "+(isBow ? "1.13, 3.2, 1.13": "3, 0, 0")+" ]," + crEnt +
						crTab + crTab + crTab + "\"scale\": [ "+(isBow ? "0.68, 0.68, 0.68": "1, 1, 1")+" ]" + crEnt +
						crTab + crTab + "},"  + crEnt +
						crTab + crTab + "\"gui\": {" + crEnt +
						crTab + crTab + crTab + "\"rotation\": [ "+(isBow ? "0, 0, 0": "0, 0, -135")+" ]," + crEnt +
						crTab + crTab + crTab + "\"translation\": [ "+(isBow ? "0, 0, 0": "0, -1, 0")+" ]," + crEnt +
						crTab + crTab + crTab + "\"scale\": [ "+(isBow ? "1, 1, 1": "0.95, 0.95, 0.95")+" ]" + crEnt +
						crTab + crTab + "}," + crEnt +
						crTab + crTab + "\"fixed\": {" + crEnt +
						crTab + crTab + crTab + "\"rotation\": [ "+(isBow ? "0, 180, 0": "0, 0, -135")+" ]," + crEnt +
						crTab + crTab + crTab + "\"translation\": [ "+(isBow ? "-2, 4, -5": "0, 0, 0")+" ]," + crEnt +
						crTab + crTab + crTab + "\"scale\": [ "+(isBow ? "0.5, 0.5, 0.5": "1, 1, 1")+" ]" + crEnt +
						crTab + crTab + "}," + crEnt +
						crTab + crTab + "\"ground\": {" + crEnt +
						crTab + crTab + crTab + "\"rotation\": [ "+(isBow ? "0, 0, 0": "0, 0, -135")+" ]," + crEnt +
						crTab + crTab + crTab + "\"translation\": [ "+(isBow ? "4, 4, 2": "0, 3, 0")+" ]," + crEnt +
						crTab + crTab + crTab + "\"scale\": [ "+(isBow ? "0.25, 0.25, 0.25": "1, 1, 1")+" ]" + crEnt +
						crTab + crTab + "}"+ crEnt +
						crTab + "}," + crEnt +
						crTab + "\"overrides\": [" + crEnt +
						crTab + crTab + "{" + crEnt +
						crTab + crTab + crTab + "\"predicate\": {" + crEnt +
						crTab + crTab + crTab + crTab + "\""+(isBow ? "pulling" : "blocking")+"\": 1" + crEnt +
						crTab + crTab + crTab + "}," + crEnt +
						crTab + crTab + crTab + "\"model\": \""+CustomNpcs.MODID+":item/"+fileName+(isBow ? "_pulling_0" : "_blocking")+"\"" + crEnt +
						crTab + crTab + "}" + 
						(isBow ?
								","+crEnt +
								crTab + crTab + "{" + crEnt +
								crTab + crTab + crTab + "\"predicate\": {" + crEnt +
								crTab + crTab + crTab + crTab + "\"pulling\": 1," + crEnt +
								crTab + crTab + crTab + crTab + "\"pull\": 0.65" + crEnt +
								crTab + crTab + crTab + "}," + crEnt +
								crTab + crTab + crTab + "\"model\": \""+CustomNpcs.MODID+":item/"+fileName+"_pulling_1\"" + crEnt +
								crTab + crTab + "}," + crEnt +
								crTab + crTab + "{" + crEnt +
								crTab + crTab + crTab + "\"predicate\": {" + crEnt +
								crTab + crTab + crTab + crTab + "\"pulling\": 1," + crEnt +
								crTab + crTab + crTab + crTab + "\"pull\": 0.9" + crEnt +
								crTab + crTab + crTab + "}," + crEnt +
								crTab + crTab + crTab + "\"model\": \""+CustomNpcs.MODID+":item/"+fileName+"_pulling_2\"" + crEnt +
								crTab + crTab + "}" + crEnt
								
						: ""+crEnt) +
						crTab + "]" + crEnt +
						"}";
				if (this.saveFile(itemModel, jsonModel)) {
					LogWriter.debug("Create Default "+(isBow ? "Bow" : "Shield")+" Item Model for \""+name+"\" item");
				}
				if (customitem instanceof CustomShield) {
					File blockingModel = new File(itemModelsDir, fileName.toLowerCase()+"_blocking.json");
					if (!blockingModel.exists()) {
						jsonModel = "{" + crEnt +
								crTab + "\"_comment\": \"Custom Shield Blocking Item Model created by default\"," + crEnt +
								crTab + "\"parent\": \"item/generated\"," + crEnt +
								crTab + "\"textures\": {" + crEnt +
								crTab + crTab + "\"layer0\": \""+CustomNpcs.MODID+":items/"+name+"\"" + crEnt +
								crTab + "}," + crEnt +
								crTab + "\"display\": {" + crEnt +
								crTab + crTab + "\"thirdperson_righthand\": {" + crEnt +
								crTab + crTab + crTab + "\"rotation\": [ 30, -30, 45 ]," + crEnt +
								crTab + crTab + crTab + "\"translation\": [ 0, 2, -2 ]," + crEnt +
								crTab + crTab + crTab + "\"scale\": [ 1.25, 1.25, 1.25 ]" + crEnt +
								crTab + crTab + "}," + crEnt +
								crTab + crTab + "\"thirdperson_lefthand\": {" + crEnt +
								crTab + crTab + crTab + "\"rotation\": [ 30, 150, -45 ]," + crEnt +
								crTab + crTab + crTab + "\"translation\": [ 0, 2, -2 ]," + crEnt +
								crTab + crTab + crTab + "\"scale\": [ 1.25, 1.25, 1.25 ]" + crEnt +
								crTab + crTab + "}," + crEnt +
								crTab + crTab + "\"firstperson_righthand\": {" + crEnt +
								crTab + crTab + crTab + "\"rotation\": [ 0, 0, -125 ]," + crEnt +
								crTab + crTab + crTab + "\"translation\": [ -2, 0, 0 ]" + crEnt +
								crTab + crTab + "}," + crEnt +
								crTab + crTab + "\"firstperson_lefthand\": {" + crEnt +
								crTab + crTab + crTab + "\"rotation\": [ 0, 0, -35 ]," + crEnt +
								crTab + crTab + crTab + "\"translation\": [ 2, -2, 0 ]" + crEnt +
								crTab + crTab + "}," + crEnt +
								crTab + crTab + "\"gui\": {" + crEnt +
								crTab + crTab + crTab + "\"rotation\": [ 0, 0, -135 ]," + crEnt +
								crTab + crTab + crTab + "\"translation\": [ 0, -1, 0 ]," + crEnt +
								crTab + crTab + crTab + "\"scale\": [ 0.95, 0.95, 0.95 ]" + crEnt +
								crTab + crTab + "}" + crEnt +
								crTab + "}" + crEnt +
								"}";
						if (this.saveFile(blockingModel, jsonModel)) {
							LogWriter.debug("Create Default Shield Blocking Item Model for \""+name+"\" item");
						}
					}
				}
				else if (customitem instanceof CustomBow) {
					for (int i=0; i<3; i++) {
						File pulling = new File(itemModelsDir, fileName.toLowerCase()+"_pulling_"+i+".json");
						if (!pulling.exists()) {
							jsonModel = "{" + crEnt +
									crTab + "\"_comment\": \"Custom Bow Pulling "+i+" Item Model created by default\"," + crEnt +
									crTab + "\"parent\": \""+CustomNpcs.MODID+":item/"+fileName+"\"," + crEnt +
									crTab + "\"textures\": {" + crEnt +
									crTab + crTab + "\"layer0\": \""+CustomNpcs.MODID+":items/weapons/"+name.toLowerCase()+"_pulling_"+i+"\"" + crEnt +
									crTab + "}" + crEnt + "}";
							if (this.saveFile(pulling, jsonModel)) {
								LogWriter.debug("Create Default Bow Pulling "+i+" Item Model for \""+name+"\" item");
							}
						}
					}
				}
			}
			else if (customitem instanceof CustomFishingRod) {
				File uncast = new File(itemModelsDir, fileName.toLowerCase()+".json");
				if (!uncast.exists()) {
					jsonModel ="{" + crEnt +
							crTab + "\"_comment\": \"Custom Fishing Rod Uncast Item Model created by default\"," + crEnt +
							crTab + "\"parent\": \"item/handheld_rod\"," + crEnt +
							crTab + "\"textures\": {" + crEnt +
							crTab + crTab + "\"layer0\": \""+CustomNpcs.MODID+":items/"+name+"_uncast\"" + crEnt +
							crTab + "}," + crEnt +
							crTab + "\"overrides\": [" + crEnt +
							crTab + crTab + "{" + crEnt +
							crTab + crTab + crTab + "\"predicate\": {" + crEnt +
							crTab + crTab + crTab + crTab + "\"cast\": 1" + crEnt +
							crTab + crTab + crTab + "}," + crEnt +
							crTab + crTab + crTab + "\"model\": \""+CustomNpcs.MODID+":item/"+fileName+"_cast\"" + crEnt +
							crTab + crTab + "}" + crEnt +
							crTab + "]" + crEnt +
							"}";
					if (this.saveFile(itemModel, jsonModel)) {
						LogWriter.debug("Create Default Fishing Rod Uncast Item Model for \""+name+"\" item");
					}
				}
				File cast = new File(itemModelsDir, fileName.toLowerCase()+"_cast.json");
				if (!cast.exists()) {
					jsonModel ="{" + crEnt +
							crTab + "\"_comment\": \"Custom Fishing Rod Cast Item Model created by default\"," + crEnt +
							crTab + "\"parent\": \"item/fishing_rod\"," + crEnt +
							crTab + "\"textures\": {" + crEnt +
							crTab + crTab + "\"layer0\": \""+CustomNpcs.MODID+":items/"+name+"_cast\"" + crEnt +
							crTab + "}" + crEnt +
							"}";
					if (this.saveFile(new File(itemModelsDir, fileName.toLowerCase()+"_cast.json"), jsonModel)) {
						LogWriter.debug("Create Default Fishing Rod Cast Item Model for \""+name+"\" item");
					}
				}
			}
			if (jsonModel.isEmpty()) {
				jsonModel ="{" + crEnt +
						crTab + "\"_comment\": \"Custom Item Model created by default\"," + crEnt +
						crTab + "\"parent\": \"minecraft:item/generated\"," + crEnt +
						crTab + "\"textures\": {" + crEnt;
				if (customitem instanceof CustomWeapon) {
					jsonModel += crTab + crTab + "\"layer0\": \""+CustomNpcs.MODID+":items/weapons/"+name.toLowerCase()+"\"" + crEnt;
				}
				else if (customitem instanceof CustomTool) {
					jsonModel += crTab + crTab + "\"layer0\": \""+CustomNpcs.MODID+":items/"+name.toLowerCase()+"\"" + crEnt;
					jsonModel = jsonModel.replace("item/generated", "item/handheld");
				}
				else if (customitem instanceof CustomArmor) {
					jsonModel += crTab + crTab + "\"layer0\": \""+CustomNpcs.MODID+":items/armor/"+name.toLowerCase()+"_"+((CustomArmor) customitem).armorType.name().toLowerCase()+"\"" + crEnt;
				}
				else {
					jsonModel += crTab + crTab + "\"layer0\": \""+CustomNpcs.MODID+":items/"+name.toLowerCase()+"\"" + crEnt;
				}
				jsonModel += crTab + "}" + crEnt + "}";
				if (this.saveFile(itemModel, jsonModel)) {
					LogWriter.debug("Create Default Item Model for \""+name+"\" item");
				} else {
					LogWriter.debug("Error Create Default Item Model for \""+name+"\" item");
				}
			}
		}
	}

	private boolean saveFile(File file, String text) {
		if (file==null || text==null || text.isEmpty()) { return false; }
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"));
			writer.write(text);
		} catch (IOException e) { return false; }
		finally {
			try { if (writer != null) { writer.close(); } } catch (IOException e) { }
		}
		return true;
	}

	public void checkPotionFiles(ICustomElement custompotion) {
		
	}

	public Side getSide() {
		return Side.SERVER;
	}
	
}
