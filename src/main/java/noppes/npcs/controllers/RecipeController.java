package noppes.npcs.controllers;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IRecipeContainer;
import net.minecraftforge.registries.ForgeRegistry;
import noppes.npcs.*;
import noppes.npcs.api.handler.IRecipeHandler;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.api.mixin.stats.IRecipeBookMixin;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.items.crafting.NpcShapedRecipes;
import noppes.npcs.items.crafting.NpcShapelessRecipes;
import noppes.npcs.util.Util;

public class RecipeController implements IRecipeHandler {

	/*
	 * 1 - Creating a recipe and save in mod data
	 * 2 - Registering on the server side and sending to the client side (ForgeRegistry -> CraftingManager)
	 * On the client:
	 * 2.1 - registering a recipe as a list of recipes in the client book; (RecipeBookClient RECIPES_BY_TAB and ALL_RECIPES)
	 * 2.2 - installing learned recipes;
	 * 2.3 - replacing the recipe book depending on the crafting grid in the GUI;
	 * 2.4 - new drawing of the tab and recipes in the GUI;
	 */

	public static ForgeRegistry<IRecipe> Registry;
	private static RecipeController instance;

	public static int version = 3;
	public static RecipeController getInstance() {
		if (RecipeController.instance == null) { RecipeController.instance = new RecipeController(); }
		return RecipeController.instance;
	}

	private final Map<String, List<INpcRecipe>> globalList = new HashMap<>(); // [GroupName, RecipeList]
	private final Map<String, List<INpcRecipe>> modList = new HashMap<>(); // [GroupName, RecipeList]

	public RecipeController() {
		RecipeController.instance = this;
		this.load();
	}

	public void checkSaves() {
		for (int i = 0; i < 2; i++) {
			for (List<INpcRecipe> list : (i == 1 ? modList : globalList).values()) {
				for (INpcRecipe r : list) {
					if (r.isChanged()) {
						save();
						return;
					}
				}
			}
		}
	}

	@Override
	public INpcRecipe addRecipe(String group, String name, boolean global, boolean isShaped, boolean isKnown, ItemStack result, ItemStack[][] stacks) {
		NonNullList<Ingredient> list = NonNullList.create();
		// setting ingredients in a recipe
		for (ItemStack[] ings : stacks) {
			list.add(Ingredient.fromStacks(ings));
		}
		INpcRecipe recipe;
		if (isShaped) { recipe = new NpcShapedRecipes(group, name, global, list, result); }
		else { recipe = new NpcShapelessRecipes(group, name, global, list, result); }
		recipe.setKnown(isKnown);
		return register(recipe);
	}

	@Override
	public INpcRecipe addRecipe(String group, String name, boolean global, boolean isShaped, boolean isKnown, ItemStack result, Object[] objects) {
		INpcRecipe recipe;
		if (isShaped) { recipe = NpcShapedRecipes.createRecipe(group, name, global, result, objects); }
		else { recipe = NpcShapelessRecipes.createRecipe(group, name, global, result, objects); }
		recipe.setKnown(isKnown);
        return register(recipe);
	}

	public INpcRecipe addRecipe(String group, String name, Object ob, boolean global, boolean isKnown, boolean isShaped, Object... objects) {
		ItemStack item;
		if (ob instanceof Item) { item = new ItemStack((Item) ob); }
		else if (ob instanceof Block) { item = new ItemStack((Block) ob); }
		else if (ob instanceof ItemStack) { item = (ItemStack) ob; }
		else { return null; }
		INpcRecipe recipe;
		if (isShaped) { recipe = NpcShapedRecipes.createRecipe(group, name, global, item, objects); }
		else { recipe = NpcShapelessRecipes.createRecipe(group, name, global, item, objects); }
		recipe.setKnown(isKnown);
		return register(recipe);
	}

	public boolean delete(INpcRecipe recipe) {
		if (recipe == null) { return false; }
		// Forge registers
		boolean isDel = false;
		IRecipe mcRecipe = RecipeController.Registry.getValue(((IRecipe) recipe).getRegistryName());
		if (mcRecipe != null) { unregisterMCRecipe(mcRecipe); }
		// Mod registers
		Map<String, List<INpcRecipe>> map = recipe.isGlobal() ? this.globalList : this.modList;
		String group = recipe.getNpcGroup();
		if (map.containsKey(group)) {
			for (INpcRecipe rec : map.get(group)) {
				if (rec.getName().equals(recipe.getName())) {
					isDel = map.get(group).remove(recipe);
					break;
				}
			}
		}
		if (map.containsKey(group) && map.get(group).isEmpty()) { map.remove(group);}
		return isDel;
	}

	@Override
	public boolean delete(int id) {
		return this.delete(this.getRecipe(id));
	}

	@Override
	public boolean delete(boolean isGlobal, String group, String name) { return this.delete(getRecipe(isGlobal, group, name)); }

	@Override
	public INpcRecipe[] getCarpentryData() {
		List<INpcRecipe> list = new ArrayList<>();
		for (List<INpcRecipe> rs : this.modList.values()) {
			list.addAll(rs);
		}
		return list.toArray(new INpcRecipe[0]);
	}

	@Override
	public INpcRecipe[] getCarpentryRecipes(String group) { return this.modList.get(Util.instance.getResourceName(group)).toArray(new INpcRecipe[0]); }

	@Override
	public INpcRecipe[] getGlobalData() {
		List<INpcRecipe> list = new ArrayList<>();
		for (List<INpcRecipe> rs : this.globalList.values()) {
			list.addAll(rs);
		}
		return list.toArray(new INpcRecipe[0]);
	}

	@Override
	public INpcRecipe[] getGlobalRecipes(String group) { return this.modList.get(Util.instance.getResourceName(group)).toArray(new INpcRecipe[0]); }

	public List<IRecipe> getKnownRecipes() {
		List<IRecipe> list = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			for (List<INpcRecipe> rs : (i == 0 ? this.globalList.values() : this.modList.values())) {
				for (INpcRecipe recipe : rs) {
					if (recipe.isKnown() && RecipeController.Registry.getValue(((IRecipe) recipe).getRegistryName()) != null) {
						list.add((IRecipe) recipe);
					}
				}
			}
		}
		return list;
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound nbtFile = new NBTTagCompound();
		NBTTagList data = new NBTTagList();
		for (int i = 0; i < 2; i++) {
			Map<String, List<INpcRecipe>> map = i == 0 ? this.globalList : this.modList;
			for (String groupName : map.keySet()) {
				NBTTagCompound nbtG = new NBTTagCompound();
				nbtG.setString("GroupName", groupName);
				nbtG.setBoolean("isGlobal", i == 0);
				NBTTagList recipes = new NBTTagList();
				for (INpcRecipe recipe : map.get(groupName)) {
					recipes.appendTag(recipe.getNbt().getMCNBT());
				}
				nbtG.setTag("Recipes", recipes);
				data.appendTag(nbtG);
			}
		}
		nbtFile.setTag("Data", data);
		nbtFile.setInteger("Version", RecipeController.version);
		return nbtFile;
	}

	@Override
	public INpcRecipe getRecipe(int id) {
		for (int i = 0; i < 2; i++) {
			for (List<INpcRecipe> list : (i == 0 ? this.globalList.values() : this.modList.values())) {
				for (INpcRecipe recipe : list) {
					if (recipe.getId() == id) { return recipe; }
				}
			}
		}
		return null;
	}

	@Override
	public INpcRecipe getRecipe(boolean isGlobal, String group, String name) {
		IRecipe recipeMC = RecipeController.Registry.getValue(new ResourceLocation(CustomNpcs.MODID, Util.instance.getResourceName(group + "_" + name)));
		if (recipeMC instanceof INpcRecipe) { return (INpcRecipe) recipeMC; }
		Map<String, List<INpcRecipe>> map = isGlobal ? globalList : modList;
		group = Util.instance.getResourceName(group);
		name = Util.instance.getResourceName(name);
		if (!map.containsKey(group)) { return null; }
		for (INpcRecipe recipe : map.get(group)) {
			if (recipe.getName().equalsIgnoreCase(name)) { return recipe; }
		}
		return null;
	}

	public void load() {
		if (CustomNpcs.VerboseDebug) { CustomNpcs.debugData.startDebug("Common", null, "loadRecipes"); }
		LogWriter.info("Loading Recipes");
		this.loadFile();
		EventHooks.onRecipesLoaded(this);
		if (CustomNpcs.VerboseDebug) { CustomNpcs.debugData.endDebug("Common", null, "loadRecipes"); }
	}

	private void loadDefaultRecipes(int version) {
		if (version == RecipeController.version && !this.modList.isEmpty() && !this.globalList.isEmpty()) { return; }
		// Remove old defaults
			// version <= 2
		if (globalList.containsKey("Npc Wand")) { for (INpcRecipe recOld : globalList.get("Npc Wand")) { delete(recOld); } }
		if (globalList.containsKey("Mob Cloner")) { for (INpcRecipe recOld : globalList.get("Mob Cloner")) { delete(recOld); } }
		if (globalList.containsKey("Soul Stone")) { for (INpcRecipe recOld : globalList.get("Soul Stone")) { delete(recOld); } }
		if (modList.containsKey("Npc MailBox")) { for (INpcRecipe recOld : modList.get("Npc MailBox")) { delete(recOld); } }
		if (modList.containsKey("Npc MailStand")) { for (INpcRecipe recOld : modList.get("Npc MailStand")) { delete(recOld); } }
		if (modList.containsKey("Npc MailStand Old")) { for (INpcRecipe recOld : modList.get("Npc MailStand Old")) { delete(recOld); } }

		// Global
		ItemStack[] ingP = {new ItemStack(Items.PAPER)};
		ItemStack[] ingS = {new ItemStack(Items.STICK)};
		// Carpentry Bench (known, is shaped)
		addRecipe("Carpentry Bench", "workbench", true, true, true, new ItemStack(CustomRegisters.carpentyBench),
				new ItemStack[][] { ingP, {new ItemStack(Items.DYE, 1, 4)}, {new ItemStack(Items.IRON_INGOT, 2, 0)},
						ingS, {new ItemStack(Blocks.PLANKS, 1, 0)}, ingS,
						ingS, {new ItemStack(Blocks.PLANKS, 1, 1)}, ingS });

		ItemStack[] ingI = { new ItemStack(Items.BREAD), new ItemStack(Items.POTATO), new ItemStack(Items.CARROT) };
		// Npc Wand Recipe via Ingredient Variants (known, is shaped)
		INpcRecipe recipe = addRecipe("Npc Wand Edit", "Npc Wand", true, true, true, new ItemStack(CustomRegisters.wand), new ItemStack[][] { ingI, ingI, {}, {}, ingS, {}, {}, ingS, {} });
		recipe.getAvailability().setGMOnly(true);

		// Mob Cloner Recipe via Recipe variants (known, is shaped)
		recipe = addRecipe("Mob Cloner Edit", "Normal", CustomRegisters.cloner, true, true, true, "XX ", "XY ", " Y ", 'X', Items.BREAD, 'Y', Items.STICK);
		recipe.getAvailability().setGMOnly(true);
		recipe = addRecipe("Mob Cloner Edit", "Potato", CustomRegisters.cloner, true, true, true, "XX ", "XY ", " Y ", 'X', Items.POTATO, 'Y', Items.STICK);
		recipe.getAvailability().setGMOnly(true);
		recipe = addRecipe("Mob Cloner Edit", "Carrot", CustomRegisters.cloner, true, true, true, "XX ", "XY ", " Y ", 'X', Items.CARROT, 'Y', Items.STICK);
		recipe.getAvailability().setGMOnly(true);

		ItemStack[] ingK = {new ItemStack(Items.DYE, 1, 15)};
		// Soul Stone (known, shapeless, only night)
		recipe = addRecipe("Soul Stone Empty", "Empty", true, false, true, new ItemStack(CustomRegisters.soulstoneEmpty), new ItemStack[][]{{new ItemStack(Items.DIAMOND)}, {new ItemStack(Items.GLOWSTONE_DUST)}, {new ItemStack(Items.REDSTONE)}, ingK});
		recipe.getAvailability().setDaytime(1);

		// Mod
		ItemStack[] ingR = {new ItemStack(Items.IRON_INGOT)};
		ItemStack[] ingL = {new ItemStack(Items.DYE, 3, 4)};
		// Npc MailBox metallic (not known, is shaped)
		addRecipe("Npc MailBox Blue", "Metallic", false, true, false, new ItemStack(CustomRegisters.mailbox, 1, 0),
				new ItemStack[][] {
						ingR, ingR, ingR, ingR,
						ingR, ingL, ingL, ingR,
						ingR, ingR, ingR, ingR,
						{}, ingP, ingK, {}
				});

		ItemStack[] ingW = {new ItemStack(Blocks.PLANKS, 1, 0), new ItemStack(Blocks.PLANKS, 1, 1), new ItemStack(Blocks.PLANKS, 1, 2), new ItemStack(Blocks.PLANKS, 1, 3), new ItemStack(Blocks.PLANKS, 1, 4), new ItemStack(Blocks.PLANKS, 1, 5)};
		ItemStack[] ingC = {new ItemStack(Blocks.COBBLESTONE) };
		// Npc MailStand metallic (known, is shaped, 3 vars)
		addRecipe("Npc MailStand Iron", "Metallic 0", false, true, true, new ItemStack(CustomRegisters.mailbox, 1, 1),
				new ItemStack[][] {
						ingW, ingW, ingW, {},
						ingR, ingP, ingR, {},
						ingW, ingW, ingW, {},
						{}, ingC, {}, {}
				});
		addRecipe("Npc MailStand Iron", "Metallic 1", false, true, true, new ItemStack(CustomRegisters.mailbox, 1, 1),
				new ItemStack[][] {
						ingR, ingP, ingR, {},
						ingW, ingW, ingW, {},
						ingW, ingW, ingW, {},
						{}, ingC, {}, {}
				});
		addRecipe("Npc MailStand Iron", "Metallic 2", false, true, true, new ItemStack(CustomRegisters.mailbox, 1, 1),
				new ItemStack[][] {
						{}, ingW, ingW, {},
						ingR, ingW, ingW, ingR,
						{}, ingW, ingW, {},
						{}, ingC, ingP, {}
				});

		// Npc MailStand wooden (known, is shaped, 3 vars)
		addRecipe("Npc MailStand Wood", "Wooden 0", false, true, true, new ItemStack(CustomRegisters.mailbox, 1, 2),
				new ItemStack[][] {
						ingW, ingW, ingW, {},
						ingW, ingP, ingW, {},
						ingW, ingW, ingW, {},
						{}, ingC, {}, {}
				});
		addRecipe("Npc MailStand Wood", "Wooden 1", false, true, true, new ItemStack(CustomRegisters.mailbox, 1, 2),
				new ItemStack[][] {
						ingW, ingP, ingW, {},
						ingW, ingW, ingW, {},
						ingW, ingW, ingW, {},
						{}, ingC, {}, {}
				});
		addRecipe("Npc MailStand Wood", "Wooden 2", false, true, true, new ItemStack(CustomRegisters.mailbox, 1, 2),
				new ItemStack[][] {
						{}, ingW, ingW, {},
						ingW, ingW, ingW, ingW,
						{}, ingW, ingW, {},
						{}, ingC, ingP, {}
				});
	}

	private void loadFile() {
		try {
			File file = new File(CustomNpcs.Dir, "recipes.dat");
			if (file.exists()) {
				try {
					NBTTagCompound nbtFile = CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath()));
					this.loadNBTData(nbtFile);
				} catch (Exception e) { LogWriter.error("Error:", e); }
			} else {
				this.globalList.clear();
				this.modList.clear();
				this.loadDefaultRecipes(-1);
			}
		} catch (Exception e) {
			LogWriter.error("Error:", e);
			try {
				File file2 = new File(CustomNpcs.Dir, "recipes.dat_old");
				if (file2.exists()) {
					try {
						NBTTagCompound nbtFile = CompressedStreamTools.readCompressed(Files.newInputStream(file2.toPath()));
						this.loadNBTData(nbtFile);
					} catch (Exception err) {
						LogWriter.error("Error:", err);
					}
				}
			} catch (Exception ee) {
				LogWriter.error("Error:", ee);
			}
		}
	}

	public void loadNBTData(NBTTagCompound nbtFile) {
		this.globalList.clear();
		this.modList.clear();
		if (nbtFile.hasKey("Data", 9) && RecipeController.version == nbtFile.getInteger("Version")) {
			for (int i = 0; i < nbtFile.getTagList("Data", 10).tagCount(); i++) {
				NBTTagCompound nbtG = nbtFile.getTagList("Data", 10).getCompoundTagAt(i);
				if (!nbtG.hasKey("GroupName", 8)) { continue; }
				Map<String, List<INpcRecipe>> map = nbtG.getBoolean("isGlobal") ? this.globalList : this.modList;
				if (!map.containsKey(nbtG.getString("GroupName"))) {
					map.put(nbtG.getString("GroupName"), new ArrayList<>());
				}
				for (int j = 0; j < nbtG.getTagList("Recipes", 10).tagCount(); j++) {
					NBTTagCompound nbtRecipe = nbtG.getTagList("Recipes", 10).getCompoundTagAt(j);
					this.loadNBTRecipe(nbtRecipe);
				}
			}
		}
		this.loadDefaultRecipes(nbtFile.getInteger("Version"));
	}

	public void loadNBTRecipe(NBTTagCompound nbtRecipe) {
		this.register(!nbtRecipe.hasKey("IsShaped") || nbtRecipe.getBoolean("IsShaped") ? NpcShapedRecipes.read(nbtRecipe) : NpcShapelessRecipes.read(nbtRecipe));
	}

	public INpcRecipe register(INpcRecipe recipe) {
		if (recipe == null) { return null; }
		// Minecraft data:
		IRecipe mcRecipe = RecipeController.Registry.getValue(((IRecipe) recipe).getRegistryName());
		if (!(mcRecipe instanceof INpcRecipe) || ((INpcRecipe) mcRecipe).isGlobal() != recipe.isGlobal() || !((INpcRecipe) mcRecipe).getNpcGroup().equals(recipe.getNpcGroup()) || !((INpcRecipe) mcRecipe).getName().equals(recipe.getName())) {
			boolean isReset = false;
			if (mcRecipe != null) {
				if (mcRecipe instanceof INpcRecipe && ((INpcRecipe) mcRecipe).isGlobal() == recipe.isGlobal()) {
					isReset = true;
					((INpcRecipe) mcRecipe).setNbt(recipe.getNbt());
					recipe = (INpcRecipe) mcRecipe;
				}
				if (!isReset) { unregisterMCRecipe(mcRecipe); }
			}
			if (!isReset) { recipe = registryMCRecipe((IRecipe) recipe); }
			else {
				LogWriter.info("Reset NPC recipe: \""+((IRecipe) recipe).getRegistryName()+"\"; "+(recipe.isGlobal() ? "is Global" : "is Mod")+"; group: \""+recipe.getNpcGroup()+"\"; name: \""+recipe.getName()+"\"");
			}
		} else {
			((INpcRecipe) mcRecipe).setNbt(recipe.getNbt());
			recipe = (INpcRecipe) mcRecipe;
			LogWriter.info("Reset NPC recipe: \""+((IRecipe) recipe).getRegistryName()+"\"; "+(recipe.isGlobal() ? "is Global" : "is Mod")+"; group: \""+recipe.getNpcGroup()+"\"; name: \""+recipe.getName()+"\"");
		}

		// Mod data
		Map<String, List<INpcRecipe>> map = recipe.isGlobal() ? this.globalList : this.modList;
		// create group
		ItemStack mainStack = null;
		if (!map.containsKey(recipe.getNpcGroup())) {
			map.put(recipe.getNpcGroup(), new ArrayList<>());
			mainStack = recipe.getProduct().getMCItemStack();
			if (recipe instanceof NpcShapedRecipes) { ((NpcShapedRecipes) recipe).main = true; }
			else { ((NpcShapelessRecipes) recipe).main = true; }
		} else {
			for (INpcRecipe npcRec : map.get(recipe.getNpcGroup())) {
				if (npcRec.isMain()) {
					mainStack = npcRec.getProduct().getMCItemStack();
					break;
				}
			}
		}
		// remove old
		if (!map.get(recipe.getNpcGroup()).remove(recipe)) {
			for (INpcRecipe npcRecipe : map.get(recipe.getNpcGroup())) {
				if (npcRecipe.getName().equals(recipe.getName()) && !npcRecipe.equal(recipe)) {
					map.get(recipe.getNpcGroup()).remove(npcRecipe);
					break;
				}
			}
		}
		// reset stack
		if (mainStack != null) {
			for (INpcRecipe npcRecipe : map.get(recipe.getNpcGroup())) {
				ItemStack cms = mainStack.copy();
				cms.setCount(((IRecipe) npcRecipe).getRecipeOutput().getCount());
				npcRecipe.setRecipeOutput(cms);
			}
		}
		// add new
		map.get(recipe.getNpcGroup()).add(recipe);
		sendToAll(recipe);
		// Registry in players
		CustomNpcs.proxy.applyRecipe(recipe, true);
		save();
		return recipe;
	}

	private void unregisterMCRecipe(IRecipe recipe) {
		if (!(recipe instanceof INpcRecipe) || recipe.getRegistryName() == null) { return; }
		boolean isLocked = RecipeController.Registry.isLocked();
		if (isLocked) { RecipeController.Registry.unfreeze(); }
		sendToAllRemove(((INpcRecipe) recipe).getId());
		// Remove in game
		RecipeController.Registry.remove(recipe.getRegistryName());
		// Remove in players
		CustomNpcs.proxy.applyRecipe((INpcRecipe) recipe, false);
		if (isLocked) { RecipeController.Registry.freeze(); }
		LogWriter.debug("Unregister NPC recipe: \""+recipe.getRegistryName()+"\"; "+(((INpcRecipe) recipe).isGlobal() ? "is Global" : "is Mod")+"; group: \""+((INpcRecipe) recipe).getNpcGroup()+"\"; name: \""+((INpcRecipe) recipe).getName()+"\"");
	}

	private INpcRecipe registryMCRecipe(IRecipe recipe) {
		boolean isLocked = RecipeController.Registry.isLocked();
		if (isLocked) { RecipeController.Registry.unfreeze(); }
		// Registry in game
		RecipeController.Registry.register(recipe);
		// Reset id
		int id = RecipeController.Registry.getID(recipe.getRegistryName());
		if (recipe instanceof NpcShapedRecipes) { ((NpcShapedRecipes) recipe).id = id; }
		else { ((NpcShapelessRecipes) recipe).id = id; }
		if (isLocked) { RecipeController.Registry.freeze(); }
		INpcRecipe recipeMod = (INpcRecipe) RecipeController.Registry.getValue(recipe.getRegistryName());
		if (recipeMod == null) {
			LogWriter.debug("Error Registry NPC recipe: \""+recipe.getRegistryName()+"\"; "+(((INpcRecipe) recipe).isGlobal() ? "is Global" : "is Mod")+"; group: \""+((INpcRecipe) recipe).getNpcGroup()+"\"; name: \""+((INpcRecipe) recipe).getName()+"\"");
		} else {
			LogWriter.debug("Registry NPC recipe: \""+recipe.getRegistryName()+"\"; "+(recipeMod.isGlobal() ? "is Global" : "is Mod")+"; group: \""+recipeMod.getNpcGroup()+"\"; name: \""+recipeMod.getName()+"\"");
		}
		return recipeMod;
	}

	public void save() {
		if (CustomNpcs.VerboseDebug) { CustomNpcs.debugData.startDebug("Common", null, "saveRecipes"); }
		try {
			CompressedStreamTools.writeCompressed(getNBT(), Files.newOutputStream(new File(CustomNpcs.Dir, "recipes.dat").toPath()));
			// Util.instance.saveFile(new File(CustomNpcs.Dir, "recipes.json"), getNBT());
		}
		catch (Exception e) { LogWriter.error("Error:", e); }
		if (CustomNpcs.VerboseDebug) { CustomNpcs.debugData.endDebug("Common", null, "saveRecipes"); }
	}

	public void clear() {
		RecipeController.getInstance().globalList.clear();
		RecipeController.getInstance().modList.clear();
	}

	public void deleteGroup(boolean isGlobal, String group) {
		if (CustomNpcs.Server == null) { return; }
		Map<String, List<INpcRecipe>> map = isGlobal ? globalList : modList;
		group = Util.instance.getResourceName(group);
		if (map.containsKey(group)) {
			for (INpcRecipe rec : map.get(group)) {
				unregisterMCRecipe(RecipeController.Registry.getValue(((IRecipe) rec).getRegistryName()));
			}
			map.remove(group);
		}
	}

	public void renameGroup(boolean isGlobal, String oldGroupName, String newGroupName) {
		if (CustomNpcs.Server == null) { return; }
		Map<String, List<INpcRecipe>> map = isGlobal ? globalList : modList;
		oldGroupName = Util.instance.getResourceName(oldGroupName);
		newGroupName = Util.instance.getResourceName(newGroupName);
		if (map.containsKey(oldGroupName)) {
			List<INpcRecipe> list = new ArrayList<>(map.get(oldGroupName));
			for (INpcRecipe recipe : list) {
				unregisterMCRecipe((IRecipe) recipe);
				map.get(oldGroupName).remove(recipe);
				NBTTagCompound compound = recipe.getNbt().getMCNBT();
				compound.setString("Group", newGroupName);
				if (recipe.isShaped()) { recipe = NpcShapedRecipes.read(compound); }
				else { recipe = NpcShapelessRecipes.read(compound); }
				register(recipe);
			}
			map.remove(oldGroupName);
		}
		updateGroupToAll(isGlobal, newGroupName);
	}

	public void renameRecipe(boolean isGlobal, String oldRecipeName, String group, String newRecipeName) {
		if (CustomNpcs.Server == null) { return; }
		group = Util.instance.getResourceName(group);
		oldRecipeName = Util.instance.getResourceName(oldRecipeName);
		newRecipeName = Util.instance.getResourceName(newRecipeName);
		Map<String, List<INpcRecipe>> map = isGlobal ? globalList : modList;
		if (map.containsKey(group)) {
			List<INpcRecipe> list = new ArrayList<>(map.get(group));
			boolean foundMainStack = false;
			for (INpcRecipe recipe : list) {
				if (!recipe.getName().equals(oldRecipeName)) { continue; }
				unregisterMCRecipe((IRecipe) recipe);
				map.get(group).remove(recipe);
				NBTTagCompound compound = recipe.getNbt().getMCNBT();
				compound.setString("Name", newRecipeName);
				if (compound.getBoolean("IsMain")) {
					if (foundMainStack) { compound.setBoolean("IsMain", false); }
					foundMainStack = true;
				}
				if (recipe.isShaped()) { recipe = NpcShapedRecipes.read(compound); }
				else { recipe = NpcShapelessRecipes.read(compound); }
				register(recipe);
			}
		}
	}

	public void updateGroupToAll(boolean isGlobal, String group) {
		if (group == null) { return; }
		group = Util.instance.getResourceName(group);
		if (CustomNpcs.Server == null || !CustomNpcs.Server.isDedicatedServer() || !(isGlobal ? globalList : modList).containsKey(group) || (isGlobal ? globalList : modList).get(group).isEmpty()) { return; }
		for (EntityPlayerMP player : CustomNpcs.Server.getPlayerList().getPlayers()) {
			for (INpcRecipe recipe : (isGlobal ? globalList : modList).get(group)) {
				if (recipe == null) { continue; }
				Container container = player.openContainer;
				if (container instanceof ContainerWorkbench || container instanceof ContainerPlayer || container instanceof IRecipeContainer) {
					player.openContainer.onContainerClosed(player);
				}
				Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.RecipesData, recipe.getNbt().getMCNBT());
			}
		}
	}

	public void sendToAllRemove(int id) {
		if (CustomNpcs.Server == null || !CustomNpcs.Server.isDedicatedServer() || getRecipe(id) == null) { return; }
		for (EntityPlayerMP player : CustomNpcs.Server.getPlayerList().getPlayers()) {
			Server.sendData(player, EnumPacketClient.SYNC_REMOVE, EnumSync.RecipesData, id);
		}
	}

	public void sendToAll(INpcRecipe recipe) {
		if (CustomNpcs.Server == null || !CustomNpcs.Server.isDedicatedServer()) { return; }
		for (EntityPlayerMP player : CustomNpcs.Server.getPlayerList().getPlayers()) {
			this.checkRecipeBook(player);
			if (recipe == null) {
				sendTo(player);
				continue;
			}
			Container container = player.openContainer;
			if (container instanceof ContainerWorkbench || container instanceof ContainerPlayer || container instanceof IRecipeContainer) {
				player.openContainer.onContainerClosed(player);
			}
			Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.RecipesData, recipe.getNbt().getMCNBT());
		}
	}

	public void sendTo(EntityPlayerMP player) {
		this.checkRecipeBook(player);
		Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.RecipesData, new NBTTagCompound());
		for (int i = 0; i < 2; i++) {
			for (List<INpcRecipe> list : (i == 0 ? this.globalList.values() : this.modList.values())) {
				for (INpcRecipe recipe : list) {
					Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.RecipesData, recipe.getNbt().getMCNBT());
				}
			}
		}
	}

	public void checkRecipeBook(EntityPlayerMP player) {
		if (player == null) { return; }
		if (((IRecipeBookMixin) player.getRecipeBook()).npcs$checkRecipes()) {
			player.getRecipeBook().init(player); // send SPacketRecipeBook
		}
	}

}
