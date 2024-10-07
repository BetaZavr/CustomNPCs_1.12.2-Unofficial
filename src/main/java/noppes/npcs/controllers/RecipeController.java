package noppes.npcs.controllers;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.registries.ForgeRegistry;
import noppes.npcs.*;
import noppes.npcs.api.handler.IRecipeHandler;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.items.crafting.NpcShapedRecipes;
import noppes.npcs.items.crafting.NpcShapelessRecipes;
import noppes.npcs.mixin.item.crafting.IShapedRecipesMixin;
import noppes.npcs.mixin.item.crafting.IShapelessRecipesMixin;

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

	public static int version = 2;
	public static RecipeController getInstance() {
		if (RecipeController.instance == null) { RecipeController.instance = new RecipeController(); }
		return RecipeController.instance;
	}

	private final Map<String, List<INpcRecipe>> globalList = Maps.newHashMap(); // [GroupName, RecipeList]
	private final Map<String, List<INpcRecipe>> modList = Maps.newHashMap(); // [GroupName, RecipeList]

	public RecipeController() {
		RecipeController.instance = this;
		this.load();
	}

	@Override
	public INpcRecipe addRecipe(String group, String name, boolean global, boolean isShaped, boolean isKnown, ItemStack result, int width, int height, ItemStack[] stacks) {
		NonNullList<Ingredient> list = NonNullList.create();
		for (ItemStack item : stacks) {
			if (!item.isEmpty()) {
				list.add(Ingredient.fromStacks(item));
			}
		}
		INpcRecipe recipe;
		if (isShaped) { recipe = new NpcShapedRecipes(group, name, width, height, list, result); }
		else { recipe = new NpcShapelessRecipes(group, name, list, result); }
		recipe.setKnown(isKnown);
		return this.register(recipe);
	}

	@Override
	public INpcRecipe addRecipe(String group, String name, boolean global, boolean isShaped, boolean isKnown, ItemStack result, Object[] objects) {
		INpcRecipe recipe;
		if (isShaped) { recipe = NpcShapedRecipes.createRecipe(group, name, global, result, objects); }
		else { recipe = NpcShapelessRecipes.createRecipe(group, name, global, result, objects); }
		recipe.setKnown(isKnown);
        return this.register(recipe);
	}

	public void addRecipe(String group, String name, Object ob, boolean global, boolean isKnown, boolean isShaped, Object... objects) {
		ItemStack item;
		if (ob instanceof Item) { item = new ItemStack((Item) ob); }
		else if (ob instanceof Block) { item = new ItemStack((Block) ob); }
		else if (ob instanceof ItemStack) { item = (ItemStack) ob; }
		else { return; }
		INpcRecipe recipe;
		if (isShaped) { recipe = NpcShapedRecipes.createRecipe(group, name, global, item, objects); }
		else { recipe = NpcShapelessRecipes.createRecipe(group, name, global, item, objects); }
		recipe.setKnown(isKnown);
		this.register(recipe);
	}

	public boolean delete(INpcRecipe recipe) {
		if (recipe == null) { return false; }
		int id = recipe.getId();
		// Forge registers:
		if (RecipeController.Registry.getValue(id) == null) {
			boolean isLocked = RecipeController.Registry.isLocked();
			if (isLocked) { RecipeController.Registry.unfreeze(); }
			RecipeController.Registry.remove(((IRecipe) recipe).getRegistryName());
			if (isLocked) { RecipeController.Registry.freeze(); }
		}
		// Mod registers:
		List<INpcRecipe> list = (recipe.isGlobal() ? this.globalList : this.modList).get(recipe.getNpcGroup());
		if (list == null) { return false; }
		for (INpcRecipe rec : list) {
			if (rec.getName().equals(recipe.getName())) {
				return list.remove(recipe);
			}
		}
		return false;
	}

	@Override
	public boolean delete(int id) {
		return this.delete(this.getRecipe(id));
	}

	@Override
	public boolean delete(String group, String name) {
		return this.delete(this.getRecipe(group, name));
	}

	@Override
	public INpcRecipe[] getCarpentryData() {
		List<INpcRecipe> list = Lists.newArrayList();
		for (List<INpcRecipe> rs : this.modList.values()) {
			list.addAll(rs);
		}
		return list.toArray(new INpcRecipe[0]);
	}

	@Override
	public INpcRecipe[] getCarpentryRecipes(String group) { return this.modList.get(group).toArray(new INpcRecipe[0]); }

	@Override
	public INpcRecipe[] getGlobalData() {
		List<INpcRecipe> list = Lists.newArrayList();
		for (List<INpcRecipe> rs : this.globalList.values()) {
			list.addAll(rs);
		}
		return list.toArray(new INpcRecipe[0]);
	}

	@Override
	public INpcRecipe[] getGlobalRecipes(String group) { return this.modList.get(group).toArray(new INpcRecipe[0]); }

	public List<IRecipe> getKnownRecipes() {
		List<IRecipe> list = Lists.newArrayList();
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
	public INpcRecipe getRecipe(String group, String name) {
		for (int i = 0; i < 2; i++) {
			for (List<INpcRecipe> list : (i == 0 ? this.globalList.values() : this.modList.values())) {
				for (INpcRecipe recipe : list) {
					if (recipe.getNpcGroup().equalsIgnoreCase(group) && recipe.getName().equalsIgnoreCase(name)) {
						return recipe;
					}
				}
			}
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
		// Global
		this.addRecipe("Npc Wand", "normal", CustomRegisters.wand, true, true, true, "XX ", " Y ", " Y ", 'X', Items.BREAD, 'Y', Items.STICK);
		this.addRecipe("Npc Wand", "potato", CustomRegisters.wand, true, true, true, "XX ", " Y ", " Y ", 'X', Items.POTATO, 'Y', Items.STICK);
		this.addRecipe("Npc Wand", "carrot", CustomRegisters.wand, true, true, true, "XX ", " Y ", " Y ", 'X', Items.CARROT, 'Y', Items.STICK);
		this.addRecipe("Mob Cloner", "normal", CustomRegisters.cloner, true, true, true, "XX ", "XY ", " Y ", 'X', Items.BREAD, 'Y', Items.STICK);
		this.addRecipe("Mob Cloner", "potato", CustomRegisters.cloner, true, true, true, "XX ", "XY ", " Y ", 'X', Items.POTATO, 'Y', Items.STICK);
		this.addRecipe("Mob Cloner", "carrot", CustomRegisters.cloner, true, true, true, "XX ", "XY ", " Y ", 'X', Items.CARROT, 'Y', Items.STICK);
		this.addRecipe("Soul Stone", "empty", CustomRegisters.soulstoneEmpty, true, true, false, "XX", 'X', Items.DIAMOND);
		// Mod
		this.addRecipe("Npc MailBox", "metallic", new ItemStack(CustomRegisters.mailbox, 1, 0), false, false, true, " XZ ", "YYYY", "Y  Y", "YYYY", 'X', Items.PAPER, 'Y', Items.IRON_INGOT, 'Z', new ItemStack(Items.DYE, 5, 4));
		for (int v = 0; v < 6; v++) {
			String key = "wood_";
			switch (v) {
				case 1: {
					key += "spruce";
					break;
				}
				case 2: {
					key += "birch";
					break;
				}
				case 3: {
					key += "jungle";
					break;
				}
				case 4: {
					key += "acacia";
					break;
				}
				case 5: {
					key += "dark_oak";
					break;
				}
				default: {
					key += "oak";
				}
			}
			this.addRecipe("Npc MailStand", key, new ItemStack(CustomRegisters.mailbox, 1, 1), false, true, true, " X  ", "YYY ", "Y Y ", "YYY ", 'X', Items.PAPER, 'Y', new ItemStack(Blocks.PLANKS, 1, v));
		}
		for (int v = 0; v < 6; v++) {
			String key = "wood_";
			switch (v) {
				case 1: {
					key += "spruce";
					break;
				}
				case 2: {
					key += "birch";
					break;
				}
				case 3: {
					key += "jungle";
					break;
				}
				case 4: {
					key += "acacia";
					break;
				}
				case 5: {
					key += "dark_oak";
					break;
				}
				default: {
					key += "oak";
				}
			}
			this.addRecipe("Npc MailStand Old", key, new ItemStack(CustomRegisters.mailbox, 1, 2), false, true, true, " X  ", "YYY ", "Z Z ", "YYY ", 'X', Items.PAPER, 'Y', new ItemStack(Blocks.PLANKS, 1, v), 'Z', Items.IRON_INGOT);
		}
		this.save();
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
				if (!nbtG.hasKey("GroupName", 8)) {
					continue;
				}
				Map<String, List<INpcRecipe>> map = nbtG.getBoolean("isGlobal") ? this.globalList : this.modList;
				if (!map.containsKey(nbtG.getString("GroupName"))) {
					map.put(nbtG.getString("GroupName"), Lists.newArrayList());
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
		INpcRecipe parent = this.getRecipe(recipe.getNpcGroup(), recipe.getName());
		boolean isNew = parent == null || parent.getId() == recipe.getId();
		if (!isNew) { // reset
			parent.copy(recipe);
			recipe = parent;
		}
		else if (parent != null) { // remove mismatched type
			this.delete(parent);
		}
		// register new
		if (isNew) {
			// Exclude variants with the same name
			while (this.getRecipe(recipe.getNpcGroup(), recipe.getName()) != null) {
				if (recipe.isShaped()) { ((NpcShapedRecipes) recipe).name += "_"; }
				else { ((NpcShapelessRecipes) recipe).name += "_"; }
			}
			// Mod registers:
			Map<String, List<INpcRecipe>> map = recipe.isGlobal() ? this.globalList : this.modList;
			if (!map.containsKey(recipe.getNpcGroup())) { map.put(recipe.getNpcGroup(), Lists.newArrayList()); }
			if (!map.get(recipe.getNpcGroup()).contains(recipe)) { map.get(recipe.getNpcGroup()).add(recipe); }
		}
		return checkGameRegistry(recipe);
	}

	private INpcRecipe checkGameRegistry(INpcRecipe recipe) {
		// Forge registers:
		if (RecipeController.Registry.getValue(recipe.getId()) == null || RecipeController.Registry.getValue(((IRecipe) recipe).getRegistryName()) == null) {
			boolean isLocked = RecipeController.Registry.isLocked();
			if (isLocked) { RecipeController.Registry.unfreeze(); }
			RecipeController.Registry.register((IRecipe) recipe);
			// Reset id
			int id = RecipeController.Registry.getID((IRecipe) recipe);
			if (recipe instanceof NpcShapedRecipes) { ((NpcShapedRecipes) recipe).id = id; }
			else { ((NpcShapelessRecipes) recipe).id = id; }
			if (isLocked) { RecipeController.Registry.freeze(); }
		}
		// check replace:
		IRecipe gameRecipe = RecipeController.Registry.getValue(((IRecipe) recipe).getRegistryName());
		if (!recipe.equals(gameRecipe)) {
			Map<String, List<INpcRecipe>> map = recipe.isGlobal() ? this.globalList : this.modList;
			if (!map.containsKey(recipe.getNpcGroup())) { map.put(recipe.getNpcGroup(), Lists.newArrayList()); }
			if (map.get(recipe.getNpcGroup()).contains(recipe)) { map.get(recipe.getNpcGroup()).add(recipe); }
			recipe = (INpcRecipe) gameRecipe;
			if (!map.get(recipe.getNpcGroup()).contains(recipe)) { map.get(recipe.getNpcGroup()).add(recipe); }
		}
		CustomNpcs.proxy.applyRecipe(recipe, true);
		return recipe;
	}

	public void save() {
		if (CustomNpcs.VerboseDebug) { CustomNpcs.debugData.startDebug("Common", null, "saveRecipes"); }
		try { CompressedStreamTools.writeCompressed(this.getNBT(), Files.newOutputStream(new File(CustomNpcs.Dir, "recipes.dat").toPath())); }
		catch (Exception e) { LogWriter.error("Error:", e); }
		if (CustomNpcs.VerboseDebug) { CustomNpcs.debugData.endDebug("Common", null, "saveRecipes"); }
	}

	public void sendTo(EntityPlayerMP player) {
		Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.RecipesData, new NBTTagCompound());
		List<INpcRecipe> list = Lists.newArrayList();
		for (int i = 0; i < 2; i++) {
			for (List<INpcRecipe> rs : (i == 0 ? this.globalList.values() : this.modList.values())) {
				for (INpcRecipe recipe : rs) {
					if (RecipeController.Registry.getValue(((IRecipe) recipe).getRegistryName()) == null) {
						list.add(this.checkGameRegistry(recipe));
						continue;
					}
					list.add(recipe);
				}
			}
		}
		for (INpcRecipe recipe : list) {
			Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.RecipesData, recipe.getNbt().getMCNBT());
		}
	}

	public void clear() {
		RecipeController.getInstance().globalList.clear();
		RecipeController.getInstance().modList.clear();
	}

    public Map<String, List<INpcRecipe>> getRecipes(boolean isGlobal) {
		return Maps.newHashMap(isGlobal ? this.globalList : this.modList);
    }

	public void renameGroup(EntityPlayerMP player, int size, String oldGroupName, String newGroupName, String recipeName) {
		Map<String, List<INpcRecipe>> map = size == 3 ? globalList : modList;
		if (map.containsKey(oldGroupName)) {
			RecipeController.Registry.unfreeze();
			map.put(newGroupName, map.get(oldGroupName));
			map.remove(oldGroupName);
			for (INpcRecipe rec : map.get(newGroupName)) {
				if (rec instanceof ShapedRecipes) {
					((IShapedRecipesMixin) rec).npcs$setGroup(newGroupName);
				} else {
					((IShapelessRecipesMixin) rec).npcs$setGroup(newGroupName);
				}

				IRecipe r = RecipeController.Registry.getValue(((IRecipe) rec).getRegistryName());
				if (r instanceof INpcRecipe) {
					if (r instanceof ShapedRecipes) {
						((IShapedRecipesMixin) r).npcs$setGroup(newGroupName);
					} else {
						((IShapelessRecipesMixin) r).npcs$setGroup(newGroupName);
					}
				}
			}
			RecipeController.Registry.freeze();
			NoppesUtilServer.sendRecipeData(player, size, newGroupName, recipeName);
			NoppesUtilServer.setRecipeGui(player, getRecipe(newGroupName, recipeName));
		}
	}

	public void deleteGroup(EntityPlayerMP player, int size, String groupName) {
		Map<String, List<INpcRecipe>> map = size == 3 ? globalList : modList;
		if (map.containsKey(groupName)) {
			RecipeController.Registry.unfreeze();
			for (INpcRecipe rec : map.get(groupName)) {
				IRecipe r = RecipeController.Registry.getValue(((IRecipe) rec).getRegistryName());
				if (r instanceof INpcRecipe) {
					RecipeController.Registry.remove(r.getRegistryName());
				}
			}
			map.remove(groupName);
			RecipeController.Registry.freeze();
			NoppesUtilServer.sendRecipeData(player, size, "", "");
			NoppesUtilServer.setRecipeGui(player, new NpcShapedRecipes());
		}
	}

	public void renameRecipe(EntityPlayerMP player, int size, String oldRecipeName, String groupName, String newRecipeName) {
		Map<String, List<INpcRecipe>> map = size == 3 ? globalList : modList;
		if (map.containsKey(groupName)) {
			for (INpcRecipe rec : map.get(groupName)) {
				if (!rec.getName().equals(oldRecipeName)) { continue; }
				if (rec instanceof NpcShapedRecipes) {
					((NpcShapedRecipes) rec).name = newRecipeName;
				} else {
					((NpcShapelessRecipes) rec).name = newRecipeName;
				}
				IRecipe r = RecipeController.Registry.getValue(((IRecipe) rec).getRegistryName());
				if (r instanceof INpcRecipe) {
					if (r instanceof NpcShapedRecipes) {
						((NpcShapedRecipes) r).name = newRecipeName;
					} else {
						((NpcShapelessRecipes) r).name = newRecipeName;
					}
				}
				RecipeController.Registry.freeze();
				NoppesUtilServer.sendRecipeData(player, size, groupName, newRecipeName);
				NoppesUtilServer.setRecipeGui(player, rec);
				break;
			}
		}
	}
}
