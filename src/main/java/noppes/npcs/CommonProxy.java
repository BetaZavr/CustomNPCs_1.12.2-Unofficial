package noppes.npcs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.List;

import com.google.common.collect.Lists;

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
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import noppes.npcs.api.IPotion;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.api.item.ICustomItem;
import noppes.npcs.blocks.CustomLiquid;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
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
import noppes.npcs.items.CustomShield;
import noppes.npcs.items.CustomTool;
import noppes.npcs.items.CustomWeapon;
import noppes.npcs.items.crafting.NpcShapedRecipes;
import noppes.npcs.items.crafting.NpcShapelessRecipes;
import noppes.npcs.util.NBTJsonUtil;
import noppes.npcs.util.NBTJsonUtil.JsonException;
import noppes.npcs.util.ObfuscationHelper;

public class CommonProxy implements IGuiHandler {
	public boolean newVersionAvailable;
	public int revision;

	public CommonProxy() {
		this.newVersionAvailable = false;
		this.revision = 4;
	}

	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	public Container getContainer(EnumGuiType gui, EntityPlayer player, int x, int y, int z, EntityNPCInterface npc) { // Changed
		switch (gui) {
		case CustomChest: {
			return new ContainerCustomChest(player, x);
		}
		case MainMenuInv: {
			return new ContainerNPCInv(npc, player);
		}
		case MainMenuInvDrop: {
			return new ContainerNPCDropSetup(npc, x, player);
		} // New
		case PlayerAnvil: {
			return new ContainerCarpentryBench(player.inventory, player.world, new BlockPos(x, y, z));
		}
		case PlayerBankSmall: {
			return new ContainerNPCBankSmall(player, x, y);
		}
		case PlayerBankUnlock: {
			return new ContainerNPCBankUnlock(player, x, y);
		}
		case PlayerBankUprade: {
			return new ContainerNPCBankUpgrade(player, x, y);
		}
		case PlayerBankLarge: {
			return new ContainerNPCBankLarge(player, x, y);
		}
		case PlayerFollowerHire: {
			return new ContainerNPCFollowerHire(npc, player);
		}
		case PlayerFollower: {
			return new ContainerNPCFollower(npc, player);
		}
		case PlayerTrader: {
			return new ContainerNPCTrader(npc, player);
		}
		case SetupItemGiver: {
			return new ContainerNpcItemGiver(npc, player);
		}
		case SetupTrader: { // Change
			Marcet marcet = MarcetController.getInstance().getMarcet(x);
			if (marcet == null) {
				marcet = new Marcet();
			}
			return new ContainerNPCTraderSetup(marcet, y, player);
		}
		case SetupFollower: {
			return new ContainerNPCFollowerSetup(npc, player);
		}
		case QuestReward: {
			return new ContainerNpcQuestReward(player);
		}
		case QuestTypeItem: {
			return new ContainerNpcQuestTypeItem(player, y);
		} // Change
		case QuestRewardItem: {
			return new ContainerNpcQuestRewardItem(player, x);
		} // New
		case ManageRecipes: {
			return new ContainerManageRecipes(player, x);
		} // Change
		case ManageBanks: {
			return new ContainerManageBanks(player);
		}
		case MerchantAdd: {
			return new ContainerMerchantAdd(player, (IMerchant) ServerEventsHandler.Merchant, player.world);
		}
		case PlayerMailman: {
			return new ContainerMail(player, x == 1, y == 1);
		}
		case CompanionInv: {
			return new ContainerNPCCompanion(npc, player);
		}
		case CustomGui: {
			return new ContainerCustomGui(getPlayer(), new InventoryBasic("", false, x));
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

	public void load() {
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
			INpcRecipe r = RecipeController.instance.getRecipe(rec.getRegistryName());
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
			for (List<INpcRecipe> list : (i == 0 ? RecipeController.instance.globalList.values() : RecipeController.instance.modList.values())) {
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
			RecipeController.instance.sendTo(player);
		}
	}

	public void updateRecipeBook(EntityPlayer player) {
		if (!(player instanceof EntityPlayerMP)) { return; }
		RecipeBook book = ((EntityPlayerMP) player).getRecipeBook();
		if (book == null) { return; }
		BitSet recipes = ObfuscationHelper.getValue(RecipeBook.class, book, 0);
		BitSet newRecipes = ObfuscationHelper.getValue(RecipeBook.class, book, 1);
		List<Integer> delIDs = Lists.<Integer>newArrayList();
		for (int id = recipes.nextSetBit(0); id >= 0; id = recipes.nextSetBit(id + 1)) {
			if (CraftingManager.REGISTRY.getObjectById(id) == null) {
				delIDs.add(id);
			}
		}
		if (delIDs.size() > 0) {
			for (int id : delIDs) {
				recipes.clear(id);
			}
		}
		delIDs.clear();
		for (int id = newRecipes.nextSetBit(0); id >= 0; id = newRecipes.nextSetBit(id + 1)) {
			if (CraftingManager.REGISTRY.getObjectById(id) == null) {
				delIDs.add(id);
			}
		}
		if (delIDs.size() > 0) {
			for (int id : delIDs) {
				newRecipes.clear(id);
			}
		}
		ObfuscationHelper.setValue(RecipeBook.class, book, recipes, 0);
		ObfuscationHelper.setValue(RecipeBook.class, book, newRecipes, 1);
		player.unlockRecipes(RecipeController.instance.getKnownRecipes());
	}

	public void checkBlockFiles(ICustomItem customblock) {
		String name = customblock.getCustomName();
		String fileName = ((Block) customblock).getRegistryName().getResourcePath();
		File blockstatesDir = new File(CustomNpcs.Dir, "assets/customnpcs/blockstates");
		if (!blockstatesDir.exists()) { blockstatesDir.mkdirs(); }
		
		File blockModelsDir = new File(CustomNpcs.Dir, "assets/customnpcs/models/block");
		File itemModelsDir = new File(CustomNpcs.Dir, "assets/customnpcs/models/item");
		if (!blockModelsDir.exists()) { blockModelsDir.mkdirs(); }
		if (!itemModelsDir.exists()) { itemModelsDir.mkdirs(); }
		char crEnt = Character.toChars(0x000A)[0];
		char crTab = Character.toChars(0x0009)[0];
		
		File blockstate = new File(blockstatesDir, fileName.toLowerCase()+".json");
		if (!blockstate.exists()) {
			NBTTagCompound nbtState = new NBTTagCompound();
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
			} else {
				nbtState.setTag("variants", new NBTTagCompound());
				nbtState.setString("_comment", "Custom block created by default");
				nbtState.getCompoundTag("variants").setTag("normal", new NBTTagCompound());
				nbtState.getCompoundTag("variants").getCompoundTag("normal").setString("model", CustomNpcs.MODID+":"+fileName.toLowerCase());
			}
			if (!jsonState.isEmpty()) {
				OutputStreamWriter writer = null;
				try {
					writer = new OutputStreamWriter(new FileOutputStream(blockstate), Charset.forName("UTF-8"));
					writer.write(jsonState);
				} catch (IOException e) { }
				finally {
					try {
						if (writer != null) {
							writer.close();
							LogWriter.debug("Create Default Blockstate for \""+name+"\" block");
						}
					} catch (IOException e) { }
				}
			} else {
				try {
					NBTJsonUtil.SaveFile(blockstate, nbtState);
					LogWriter.debug("Create Default Blockstate for \""+name+"\" block");
				}
				catch (IOException | JsonException e) { }
			}
		}
		
		File blockModel = new File(blockModelsDir, fileName.toLowerCase()+".json");
		if (!blockModel.exists()) {
			NBTTagCompound nbtModel = new NBTTagCompound();
			nbtModel.setTag("textures", new NBTTagCompound());
			nbtModel.getCompoundTag("textures").setString("all", CustomNpcs.MODID+":blocks/"+name.toLowerCase());
			nbtModel.setString("_comment", "Custom block created by default");
			nbtModel.setString("parent", "block/cube_all");
			try {
				NBTJsonUtil.SaveFile(blockModel, nbtModel);
				LogWriter.debug("Create Default Block Model for \""+name+"\" block");
			}
			catch (IOException | JsonException e) { }
		}
		
		File itemModel = new File(itemModelsDir, fileName.toLowerCase()+".json");
		if (!itemModel.exists()) {
			String jsonStr = "{" + crEnt +
					crTab + "\"parent\": \"" + CustomNpcs.MODID + ":block/" + fileName.toLowerCase() + "\"," + crEnt +
					crTab + "\"display\": {" + crEnt +
					crTab + crTab + "\"thirdperson\": {" + crEnt +
					crTab + crTab + crTab + "\"rotation\": [ 10, -45, 170 ]," + crEnt +
					crTab + crTab + crTab + "\"translation\": [ 0, 1.5, -2.75 ]," + crEnt +
					crTab + crTab + crTab + "\"scale\": [ 0.375, 0.375, 0.375 ]" + crEnt +
					crTab + crTab + "}" + crEnt +
					crTab + "}" + crEnt +
					"}";
			OutputStreamWriter writer = null;
			try {
				writer = new OutputStreamWriter(new FileOutputStream(itemModel), Charset.forName("UTF-8"));
				writer.write(jsonStr);
			} catch (IOException e) { }
			finally {
				try {
					if (writer != null) {
						writer.close();
						LogWriter.debug("Create Default Item Model for \""+name+"\" block");
					}
				} catch (IOException e) { }
			}
		}
	}

	public void checkItemFiles(ICustomItem customitem) {
		String name = customitem.getCustomName();
		String fileName = ((Item) customitem).getRegistryName().getResourcePath();

		File itemModelsDir = new File(CustomNpcs.Dir, "assets/customnpcs/models/item");
		if (!itemModelsDir.exists()) { itemModelsDir.mkdirs(); }
		File itemModel = new File(itemModelsDir, fileName.toLowerCase()+".json");
		if (!itemModel.exists()) {
			if (customitem instanceof CustomShield || customitem instanceof CustomBow) {
				boolean isBow = (customitem instanceof CustomBow);
				char crEnt = Character.toChars(0x000A)[0];
				char crTab = Character.toChars(0x0009)[0];
				String jsonStr = "{" + crEnt +
						crTab + "\"_comment\": \"Custom item created by default\"," + crEnt +
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
				OutputStreamWriter writer = null;
				try {
					writer = new OutputStreamWriter(new FileOutputStream(itemModel), Charset.forName("UTF-8"));
					writer.write(jsonStr);
				} catch (IOException e) { }
				finally {
					try {
						if (writer != null) {
							writer.close();
							LogWriter.debug("Create Default Item Model for \""+name+"\" item");
						}
					} catch (IOException e) { }
				}
				if (customitem instanceof CustomShield) {
					File blockingModel = new File(itemModelsDir, fileName.toLowerCase()+"_blocking.json");
					if (!blockingModel.exists()) {
						jsonStr = "{" + crEnt +
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
						writer = null;
						try {
							writer = new OutputStreamWriter(new FileOutputStream(blockingModel), Charset.forName("UTF-8"));
							writer.write(jsonStr);
						} catch (IOException e) { }
						finally {
							try {
								if (writer != null) {
									writer.close();
								}
							} catch (IOException e) { }
						}
					}
				}
				else if (customitem instanceof CustomBow) {
					for (int i=0; i<3; i++) {
						File pulling = new File(itemModelsDir, fileName.toLowerCase()+"_pulling_"+i+".json");
						if (!pulling.exists()) {
							NBTTagCompound nbtModel = new NBTTagCompound();
							nbtModel.setTag("textures", new NBTTagCompound());
							nbtModel.setString("_comment", "Custom item created by default");
							nbtModel.setString("parent", CustomNpcs.MODID+":item/"+fileName);
							nbtModel.getCompoundTag("textures").setString("layer0", CustomNpcs.MODID+":items/weapons/"+name.toLowerCase()+"_pulling_"+i);
							try {
								NBTJsonUtil.SaveFile(pulling, nbtModel);
							}
							catch (IOException | JsonException e) { }
						}
					}
				}
				return;
			}
			NBTTagCompound nbtModel = new NBTTagCompound();
			nbtModel.setTag("textures", new NBTTagCompound());
			nbtModel.setString("_comment", "Custom item created by default");
			nbtModel.setString("parent", "minecraft:item/generated");
			if (customitem instanceof CustomWeapon) {
				nbtModel.getCompoundTag("textures").setString("layer0", CustomNpcs.MODID+":items/weapons/"+name.toLowerCase());
			}
			else if (customitem instanceof CustomTool) {
				nbtModel.getCompoundTag("textures").setString("layer0", CustomNpcs.MODID+":items/"+name.toLowerCase());
				nbtModel.setString("parent", "minecraft:item/handheld");
			}
			else if (customitem instanceof CustomArmor) {
				nbtModel.getCompoundTag("textures").setString("layer0", CustomNpcs.MODID+":items/armor/"+name.toLowerCase()+"_"+((CustomArmor) customitem).getEquipmentSlot().name().toLowerCase());
			}
			else {
				nbtModel.getCompoundTag("textures").setString("layer0", CustomNpcs.MODID+":items/"+name.toLowerCase());
			}
			try {
				NBTJsonUtil.SaveFile(itemModel, nbtModel);
				LogWriter.debug("Create Default Item Model for \""+name+"\" item");
			}
			catch (IOException | JsonException e) {  }
		}
	}

	public void checkPotionFiles(IPotion custompotion) {
		
	}

	public Side getSide() {
		return Side.SERVER;
	}
	
}
