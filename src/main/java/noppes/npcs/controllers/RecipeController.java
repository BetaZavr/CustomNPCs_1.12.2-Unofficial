package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import noppes.npcs.CustomRegisters;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.handler.IRecipeHandler;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.items.crafting.NpcShapedRecipes;
import noppes.npcs.items.crafting.NpcShapelessRecipes;

public class RecipeController
implements IRecipeHandler {
	
	private static RecipeController instance;
	private String filePath;
	
	public Map<String, List<INpcRecipe>> globalList; // [GroupName, RecipeList]
	public Map<String, List<INpcRecipe>> modList; // [GroupName, RecipeList]
	public static ForgeRegistry<IRecipe> Registry;
	public static int version = 2;

	public RecipeController() {
		this.filePath = "";
		this.globalList = Maps.<String, List<INpcRecipe>>newHashMap();
		this.modList = Maps.<String, List<INpcRecipe>>newHashMap();
		RecipeController.instance = this;
		this.load();
	}
	
	public static RecipeController getInstance() {
		if (newInstance()) { RecipeController.instance = new RecipeController(); }
		return RecipeController.instance;
	}

	private static boolean newInstance() {
		if (RecipeController.instance == null) { return true; }
		return CustomNpcs.Dir != null && !RecipeController.instance.filePath.equals(CustomNpcs.Dir.getAbsolutePath());
	}
	
	public NBTTagCompound getNBT() {
		NBTTagCompound nbtFile = new NBTTagCompound();
		NBTTagList data = new NBTTagList();
		for (int i=0; i<2; i++) {
			Map<String, List<INpcRecipe>> map = i==0 ? this.globalList : this.modList;
			for (String groupName : map.keySet()) {
				NBTTagCompound nbtG = new NBTTagCompound();
				nbtG.setString("GroupName", groupName);
				nbtG.setBoolean("isGlobal", i==0);
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

	public void load() {
		if (CustomNpcs.VerboseDebug) { CustomNpcs.debugData.startDebug("Common", null, "loadRecipes"); }
		LogWriter.info("Loading Recipes");
		this.loadFile();
		EventHooks.onRecipesLoaded(this);
		CustomNpcs.proxy.updateRecipes(null, true, false, "RecipeController.load()");
		if (CustomNpcs.VerboseDebug) { CustomNpcs.debugData.endDebug("Common", null, "loadRecipes"); }
	}
	
	private void loadFile() {
		this.filePath = CustomNpcs.Dir.getAbsolutePath();
		try {
			File file = new File(CustomNpcs.Dir, "recipes.dat");
			if (file.exists()) {
				try {
					NBTTagCompound nbtFile = CompressedStreamTools
							.readCompressed((InputStream) new FileInputStream(file));
					this.loadNBTData(nbtFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				this.globalList.clear();
				this.modList.clear();
				this.loadDefaultRecipes(-1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				File file2 = new File(CustomNpcs.Dir, "recipes.dat_old");
				if (file2.exists()) {
					try {
						NBTTagCompound nbtFile = CompressedStreamTools
								.readCompressed((InputStream) new FileInputStream(file2));
						this.loadNBTData(nbtFile);
					} catch (IOException err) {
						err.printStackTrace();
					}
				}
			} catch (Exception ee) {
				e.printStackTrace();
			}
		}
	}

	public void loadNBTData(NBTTagCompound nbtFile) {
		this.globalList.clear();
		this.modList.clear();
		if (nbtFile.hasKey("Data", 9) && RecipeController.version==nbtFile.getInteger("Version")) {
			for (int i = 0; i < nbtFile.getTagList("Data", 10).tagCount(); i++) {
				NBTTagCompound nbtG = nbtFile.getTagList("Data", 10).getCompoundTagAt(i);
				if (!nbtG.hasKey("GroupName", 8)) { continue; }
				Map<String, List<INpcRecipe>> map = nbtG.getBoolean("isGlobal") ? this.globalList : this.modList;
				if (!map.containsKey(nbtG.getString("GroupName"))) {
					map.put(nbtG.getString("GroupName"), Lists.<INpcRecipe>newArrayList());
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
		this.putRecipe(!nbtRecipe.hasKey("IsShaped") || nbtRecipe.getBoolean("IsShaped")
				? NpcShapedRecipes.read(nbtRecipe)
				: NpcShapelessRecipes.read(nbtRecipe));
	}

	private void loadDefaultRecipes(int version) {
		if (version == RecipeController.version) {
			return;
		}
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
				case 1: { key += "spruce"; break; }
				case 2: { key += "birch"; break; }
				case 3: { key += "jungle"; break; }
				case 4: { key += "acacia"; break; }
				case 5: { key += "dark_oak"; break; }
				default: { key += "oak"; }
			}
			this.addRecipe("Npc MailStand", key, new ItemStack(CustomRegisters.mailbox, 1, 1), false, true, true, " X  ", "YYY ", "Y Y ", "YYY ", 'X', Items.PAPER, 'Y', new ItemStack(Blocks.PLANKS, 1, v));
		}
		for (int v = 0; v < 6; v++) {
			String key = "wood_";
			switch (v) { case 1: { key += "spruce"; break; }
				case 2: { key += "birch"; break; }
				case 3: { key += "jungle"; break; }
				case 4: { key += "acacia"; break; }
				case 5: { key += "dark_oak"; break; }
				default: { key += "oak"; }
			}
			this.addRecipe("Npc MailStand Old", key, new ItemStack(CustomRegisters.mailbox, 1, 2), false, true, true, " X  ", "YYY ", "Z Z ", "YYY ", 'X', Items.PAPER, 'Y', new ItemStack(Blocks.PLANKS, 1, v), 'Z', Items.IRON_INGOT);
		}
		this.save();
	}
	
	public void save() {
		try {
			CompressedStreamTools.writeCompressed(this.getNBT(), (OutputStream) new FileOutputStream(new File(CustomNpcs.Dir, "recipes.dat")));
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	@Override
	public INpcRecipe getRecipe(int id) {
		for (int i = 0; i < 2; i++) {
			for (List<INpcRecipe> list : (i == 0 ? this.globalList.values() : this.modList.values())) {
				for (INpcRecipe recipe : list) {
					if (recipe.getId() == id) {
						return recipe;
					}
				}
			}
		}
		return null;
	}
	
	public INpcRecipe getRecipe(ResourceLocation regName) {
		for (int i = 0; i < 2; i++) {
			for (List<INpcRecipe> list : (i == 0 ? this.globalList.values() : this.modList.values())) {
				for (INpcRecipe recipe : list) {
					if (((IRecipe) recipe).getRegistryName().toString().equals(regName.toString())) {
						return recipe;
					}
				}
			}
		}
		return null;
	}
	
	public INpcRecipe putRecipe(INpcRecipe recipe) {
		if (recipe == null) { return null; }
		
		Map<String, List<INpcRecipe>> map = recipe.isGlobal() ? this.globalList : this.modList;
		if (!map.containsKey(recipe.getNpcGroup())) {
			map.put(recipe.getNpcGroup(), Lists.<INpcRecipe>newArrayList());
		}
		
		boolean needAdd = true;
		for (INpcRecipe rec : map.get(recipe.getNpcGroup())) {
			if (rec.getNpcGroup().equals(recipe.getNpcGroup()) && rec.getName().equals(recipe.getName())) {
				if (rec.getClass()==recipe.getClass()) {
					rec.copy(recipe);
					needAdd = false;
					recipe = rec;
					break;
				} else {
					map.get(recipe.getNpcGroup()).remove(rec);
					break;
				}
			}
		}
		if (needAdd) {
			while (this.getRecipe(recipe.getNpcGroup(), recipe.getName())!=null) {
				if (recipe.isShaped()) {
					((NpcShapedRecipes) recipe).name += "_";
				} else {
					((NpcShapelessRecipes) recipe).name += "_";
				}
			}
			map.get(recipe.getNpcGroup()).add(recipe);
		}
		LogWriter.debug((needAdd ? "Add New" : "Reset")+" recipe: "+((IRecipe) recipe).getRegistryName());
		CustomNpcs.proxy.updateRecipes(recipe, true, false, "RecipeController.putRecipe()");
		int id = RecipeController.Registry.getID((IRecipe) recipe);
		if (recipe instanceof NpcShapedRecipes) { ((NpcShapedRecipes) recipe).id = id; }
		else { ((NpcShapelessRecipes) recipe).id = id; }
		return recipe;
	}
	
	@Override
	public INpcRecipe addRecipe(String group, String name, boolean global, boolean known, ItemStack result, int width, int height, ItemStack[] stacks) {
		NonNullList<Ingredient> list = NonNullList.create();
		for (ItemStack item : stacks) {
			if (!item.isEmpty()) {
				list.add(Ingredient.fromStacks(new ItemStack[] { item }));
			}
		}
		INpcRecipe recipe;
		if (known) {
			recipe = new NpcShapedRecipes(group, name, width, height, list, result);
			((NpcShapedRecipes) recipe).known = known;
		} else {
			recipe = new NpcShapelessRecipes(group, name, list, result);
			((NpcShapedRecipes) recipe).known = known;
		}
		return this.putRecipe(recipe);
	}

	@Override
	public INpcRecipe addRecipe(String group, String name, boolean global, boolean known, ItemStack result, Object[] objects) {
		INpcRecipe recipe;
		if (known) {
			recipe = NpcShapedRecipes.createRecipe(group, name, global, result, objects);
			((NpcShapedRecipes) recipe).known = known;
		} else {
			recipe = NpcShapelessRecipes.createRecipe(group, name, global, result, objects);
			((NpcShapedRecipes) recipe).known = known;
		}
		return this.putRecipe(recipe);
	}
	

	public void addRecipe(String group, String name, Object ob, boolean global, boolean known, boolean shaped, Object... objects) {
		ItemStack item;
		if (ob instanceof Item) {
			item = new ItemStack((Item) ob);
		} else if (ob instanceof Block) {
			item = new ItemStack((Block) ob);
		} else {
			item = (ItemStack) ob;
		}
		INpcRecipe recipe = null;
		if (shaped) {
			recipe = NpcShapedRecipes.createRecipe(group, name, global, item, objects);
			((NpcShapedRecipes) recipe).known = known;
		} else {
			recipe = NpcShapelessRecipes.createRecipe(group, name, global, item, objects);
			((NpcShapelessRecipes) recipe).known = known;
		}
		this.putRecipe(recipe);
	}
	
	@Override
	public boolean delete(String group, String name) {
		return this.delete(this.getRecipe(group, name));
	}

	@Override
	public boolean delete(int id) {
		return this.delete(this.getRecipe(id));
	}

	public boolean delete(INpcRecipe recipe) {
		boolean delete = false;
		if (recipe == null) { return delete; }
		List<INpcRecipe> list = (recipe.isGlobal() ? this.globalList : this.modList).get(recipe.getNpcGroup());
		if (list == null) { return delete; }
		for (INpcRecipe rec : list) {
			if (rec.getName().equals(recipe.getName())) {
				list.remove(recipe);
				delete = true;
				CustomNpcs.proxy.updateRecipes(recipe, true, true, "RecipeController.delete()");
				break;
			}
		}
		return delete;
	}

	@Override
	public INpcRecipe[] getCarpentryData() {
		List<INpcRecipe> list = Lists.<INpcRecipe>newArrayList();
		for (List<INpcRecipe> rs : this.modList.values()) {
			for (INpcRecipe recipe : rs) {
				list.add(recipe);
			}
		}
		return list.toArray(new INpcRecipe[list.size()]);
	}

	@Override
	public INpcRecipe[] getCarpentryRecipes(String group) {
		return this.modList.get(group).toArray(new INpcRecipe[this.modList.get(group).size()]);
	}

	@Override
	public INpcRecipe[] getGlobalData() {
		List<INpcRecipe> list = Lists.<INpcRecipe>newArrayList();
		for (List<INpcRecipe> rs : this.globalList.values()) {
			for (INpcRecipe recipe : rs) {
				list.add(recipe);
			}
		}
		return list.toArray(new INpcRecipe[list.size()]);
	}

	@Override
	public INpcRecipe[] getGlobalRecipes(String group) {
		return this.globalList.get(group).toArray(new INpcRecipe[this.globalList.get(group).size()]);
	}
	
	public List<IRecipe> getKnownRecipes() {
		List<IRecipe> list = Lists.<IRecipe>newArrayList();
		for (int i = 0; i < 2; i++) {
			for (List<INpcRecipe> rs : (i == 0 ? this.globalList.values() : this.modList.values())) {
				for (INpcRecipe recipe : rs) {
					if (recipe.isKnown()
							&& RecipeController.Registry.getValue(((IRecipe) recipe).getRegistryName()) != null) {
						list.add((IRecipe) recipe);
					}
				}
			}
		}
		return list;
	}

	public INpcRecipe findMatchingRecipe(InventoryCrafting inventoryCrafting) {
		for (List<INpcRecipe> rs : this.modList.values()) {
			for (INpcRecipe recipe : rs) {
				if (!recipe.isValid()) { continue; }
				if (recipe instanceof NpcShapedRecipes && ((NpcShapedRecipes) recipe).matches(inventoryCrafting, null)) {
					return (INpcRecipe) recipe;
				}
				if (recipe instanceof NpcShapelessRecipes && ((NpcShapelessRecipes) recipe).matches(inventoryCrafting, null)) {
					return (INpcRecipe) recipe;
				}
			}
		}
		return null;
	}

	public void sendTo(EntityPlayerMP player) {
		if (CustomNpcs.Server!=null && CustomNpcs.Server.isSinglePlayer()) { return; }
		Server.sendData(player, EnumPacketClient.SYNC_UPDATE, 6, new NBTTagCompound());
		for (int i = 0; i < 2; i++) {
			for (List<INpcRecipe> rs : (i == 0 ? this.globalList.values() : this.modList.values())) {
				for (INpcRecipe recipe : rs) {
					Server.sendData(player, EnumPacketClient.SYNC_UPDATE, 6, recipe.getNbt().getMCNBT());
				}
			}
		}
	}

}
