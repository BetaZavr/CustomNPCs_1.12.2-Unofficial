package noppes.npcs.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandBase.CoordinateArg;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.IContainerCustomChest;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IDimension;
import noppes.npcs.api.IMetods;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IRayTrace;
import noppes.npcs.api.IScoreboard;
import noppes.npcs.api.IScoreboardObjective;
import noppes.npcs.api.IScoreboardScore;
import noppes.npcs.api.IScoreboardTeam;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.block.IBlockFluidContainer;
import noppes.npcs.api.block.IBlockScripted;
import noppes.npcs.api.block.IBlockScriptedDoor;
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
import noppes.npcs.api.entity.IThrowable;
import noppes.npcs.api.entity.IVillager;
import noppes.npcs.api.gui.IButton;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.gui.ILabel;
import noppes.npcs.api.gui.IScroll;
import noppes.npcs.api.gui.ITextField;
import noppes.npcs.api.gui.ITexturedRect;
import noppes.npcs.api.handler.IDataObject;
import noppes.npcs.api.handler.data.IDataElement;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.handler.data.IScriptData;
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
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.player.GuiNpcCarpentryBench;
import noppes.npcs.client.gui.recipebook.GuiNpcButtonRecipeTab;
import noppes.npcs.client.gui.recipebook.GuiNpcRecipeBook;
import noppes.npcs.containers.SlotNpcCrafting;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.QuestData;

public class AdditionalMethods
implements IMetods {

	private Map<Class<?>, Class<?>> map = Maps.newHashMap();
	public TreeMap<String, String> obfuscations = Maps.<String, String>newTreeMap();
	private Method copyDataFromOld;
	public static AdditionalMethods instance;
	
	public AdditionalMethods() {
		AdditionalMethods.instance = this;
		try {
			this.copyDataFromOld = Entity.class.getDeclaredMethod((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")?"copyDataFromOld":"func_180432_n", Entity.class);
			this.copyDataFromOld.setAccessible(true);
		}
		catch (Exception e) { e.printStackTrace(); }
		this.map.put(IAnimal.class, AnimalWrapper.class);
		this.map.put(IArrow.class, ArrowWrapper.class);
		this.map.put(IBlockFluidContainer.class, BlockFluidContainerWrapper.class);
		this.map.put(IPos.class, BlockPosWrapper.class);
		this.map.put(IBlockScriptedDoor.class, BlockScriptedDoorWrapper.class);
		this.map.put(IBlockScripted.class, BlockScriptedWrapper.class);
		this.map.put(IBlock.class, BlockWrapper.class);
		this.map.put(IContainerCustomChest.class, ContainerCustomChestWrapper.class);
		this.map.put(IContainer.class, ContainerWrapper.class);
		this.map.put(IDamageSource.class, DamageSourceWrapper.class);
		this.map.put(IDataObject.class, DataObject.class);
		this.map.put(IDimension.class, DimensionWrapper.class);
		this.map.put(IEntityItem.class, EntityItemWrapper.class);
		this.map.put(IEntityLivingBase.class, EntityLivingBaseWrapper.class);
		this.map.put(IEntityLiving.class, EntityLivingWrapper.class);
		this.map.put(IEntity.class, EntityWrapper.class);
		this.map.put(IItemArmor.class, ItemArmorWrapper.class);
		this.map.put(IItemBlock.class, ItemBlockWrapper.class);
		this.map.put(IItemBook.class, ItemBookWrapper.class);
		this.map.put(IItemScripted.class, ItemScriptedWrapper.class);
		this.map.put(IScriptHandler.class, ItemScriptedWrapper.class);
		this.map.put(IItemStack.class, ItemStackWrapper.class);
		this.map.put(IMonster.class, MonsterWrapper.class);
		this.map.put(INbt.class, NBTWrapper.class);
		this.map.put(ICustomNpc.class, NPCWrapper.class);
		this.map.put(IPixelmon.class, PixelmonWrapper.class);
		this.map.put(IPlayer.class, PlayerWrapper.class);
		this.map.put(IProjectile.class, ProjectileWrapper.class);
		this.map.put(IRayTrace.class, RayTraceWrapper.class);
		this.map.put(IScoreboardObjective.class, ScoreboardObjectiveWrapper.class);
		this.map.put(IScoreboardScore.class, ScoreboardScoreWrapper.class);
		this.map.put(IScoreboardTeam.class, ScoreboardTeamWrapper.class);
		this.map.put(IScoreboard.class, ScoreboardWrapper.class);
		this.map.put(IThrowable.class, ThrowableWrapper.class);
		this.map.put(IVillager.class, VillagerWrapper.class);
		this.map.put(IWorld.class, WorldWrapper.class);
		this.map.put(NpcAPI.class, WrapperNpcAPI.class);
		this.map.put(IButton.class, CustomGuiButtonWrapper.class);
		this.map.put(ICustomGuiComponent.class, CustomGuiComponentWrapper.class);
		this.map.put(IItemSlot.class, CustomGuiItemSlotWrapper.class);
		this.map.put(ILabel.class, CustomGuiLabelWrapper.class);
		this.map.put(IScroll.class, CustomGuiScrollWrapper.class);
		this.map.put(ITextField.class, CustomGuiTextFieldWrapper.class);
		this.map.put(ITexturedRect.class, CustomGuiTexturedRectWrapper.class);
		this.map.put(ICustomGui.class, CustomGuiWrapper.class);
		this.map.put(IDataElement.class, DataElement.class);
		
		for (ModContainer mod : Loader.instance().getModList()) {
			if (mod.getSource().exists() && mod.getSource().getName().equals(CustomNpcs.MODID) || mod.getSource().getName().equals("bin")) {
				if (!mod.getSource().isDirectory() && (mod.getSource().getName().endsWith(".jar") || mod.getSource().getName().endsWith(".zip"))) {
					try {
						ZipFile zip = new ZipFile(mod.getSource());
						Enumeration<? extends ZipEntry> entries = zip.entries();
						while (entries.hasMoreElements()) {
							ZipEntry zipentry = (ZipEntry) entries.nextElement();
							if (zipentry.isDirectory() || zipentry.getName().indexOf("noppes/npcs/data/obfuscation_")!=0 || !zipentry.getName().endsWith(".json")) { continue; }
							StringWriter writer = new StringWriter();
							IOUtils.copy(zip.getInputStream(zipentry), writer, Charset.forName("UTF-8"));
							for (String line : writer.toString().split(""+((char) 10))) {
								String[] entry = line.split("=");
								this.obfuscations.put(entry[0], entry[1]);
							}
							writer.close();
						}
						zip.close();
					}
					catch (IOException e) {  }
				}
				else {
					File dir = new File(mod.getSource(), "noppes/npcs/data");
					if (dir.exists()) {
						for (File f : dir.listFiles()) {
							if (!f.isFile() || f.getName().indexOf("obfuscation_")!=0 || !f.getName().endsWith(".json")) { continue; }
							try {
								String line;
								BufferedReader reader = Files.newBufferedReader(f.toPath());
								while((line = reader.readLine()) != null) {
									String[] entry = line.split("=");
									this.obfuscations.put(entry[0], entry[1]);
								}
								reader.close();
							}
							catch (IOException e) { }
						}
					}
				}
			}
		}
	}

	/** Stripping a string of color */
	@Override
	public String deleteColor(String str) {
		if (str == null) { return null; }
		if (str.isEmpty()) { return str; }
		for (int i=0; i<4; i++) {
			String chr = new String(Character.toChars(0x00A7));
			if (i==1) { chr = ""+((char) 167); }
			else if (i==2) { chr = "&"; }
			else if (i==3) { chr = ""+((char) 65535); }
			try {
				while (str.indexOf(chr) != (-1)) {
					int p = str.indexOf(chr);
					str = (p>0 ? str.substring(0, p) : "" ) + (p+2==str.length() ? "" : str.substring(p + 2));
				}
			} catch (Exception e) { }
		}
		return str;
	}

	public static List<IDataElement> getClassData(Object obj, boolean onlyPublic, boolean addConstructor) {
		List<IDataElement> list = Lists.newArrayList();
		Class<?> cz = (obj instanceof Class) ? (Class<?>) obj : obj.getClass();
		/** Constructors */
		if (addConstructor) {
			Constructor<?>[] cns = onlyPublic ? cz.getConstructors() : cz.getDeclaredConstructors();
			for (Constructor<?> c : cns) {
				list.add(new DataElement(c, obj));
			}
		}
		Map<String, Class<?>> classes = Maps.newHashMap();
		Map<String, Field> fields = Maps.newHashMap();
		Map<String, Method> methods = Maps.newHashMap();

		for (Class<?> cl : onlyPublic ? cz.getClasses() : cz.getDeclaredClasses()) {
			if (!classes.containsKey(cl.getSimpleName())) {
				classes.put(cl.getSimpleName(), cl);
			}
		}
		/** Data */
		List<Class<?>> czs = Lists.newArrayList();
		czs.add(cz);
		while (cz.getSuperclass() != Object.class && !czs.contains(cz.getSuperclass())) {
			czs.add(cz.getSuperclass());
			cz = cz.getSuperclass();
		}
		for (Class<?> c : czs) {
			for (Field f : onlyPublic ? c.getFields() : c.getDeclaredFields()) {
				if (!fields.containsKey(f.getName())) {
					fields.put(f.getName(), f);
				}
			}
			for (Method m : onlyPublic ? c.getMethods() : c.getDeclaredMethods()) {
				if (!methods.containsKey(m.getName())) {
					methods.put(m.getName(), m);
				}
			}
		}
		/** Fields */
		if (fields.size() > 0) {
			List<String> sortNames = Lists.newArrayList(fields.keySet());
			Collections.sort(sortNames);
			List<String> names = Lists.newArrayList();
			for (String name : sortNames) {
				boolean next = false;
				if (names.contains(name)) {
					continue;
				}
				Field f = fields.get(name);
				for (IDataElement td : list) {
					if (td.getObject().equals(f)) {
						next = true;
						break;
					}
				}
				if (next) {
					continue;
				}
				list.add(new DataElement(f, obj));
				names.add(f.getName());
			}
		}
		/** Methods */
		if (methods.size() > 0) {
			List<String> sortNames = Lists.newArrayList(methods.keySet());
			Collections.sort(sortNames);
			List<String> names = Lists.newArrayList();
			for (String name : sortNames) {
				boolean next = false;
				if (names.contains(name)) {
					continue;
				}
				Method m = methods.get(name);
				for (IDataElement td : list) {
					if (td.getObject().equals(m)) {
						next = true;
						break;
					}
				}
				if (next) {
					continue;
				}
				list.add(new DataElement(m, obj));
				names.add(m.getName());
			}
		}
		/** Classes */
		if (classes.size() > 0) {
			List<String> sortNames = Lists.newArrayList(classes.keySet());
			Collections.sort(sortNames);
			for (String name : sortNames) {
				list.add(new DataElement(classes.get(name), obj));
			}
		}
		return list;
	}

	public static Map<String, Map<String, String[]>> getObjectVarAndMetods(String[] path, Map<String, Class<?>> inFunc) {
		Object obj = null;
		for (int i = 0; i < path.length; i++) {
			String key = path[i];
			if (key.isEmpty()) { break; }
			if (i == 0 && inFunc != null) {
				obj = inFunc.get(key);
				continue;
			}
			if (obj == null) { break; }
			Class<?> clazz = AdditionalMethods.getScriptClass(obj);
			obj = null;
			String metodName = key.indexOf('(')!=-1 ? key.substring(0, key.indexOf('(')) : "get"+(""+key.charAt(0)).toUpperCase()+key.substring(1);
			if (metodName.equals("getMCEntity")) {
				if (clazz == PlayerWrapper.class) {
					obj = EntityPlayerMP.class;
					continue;
				} else if (clazz == EntityLivingBaseWrapper.class) {
					obj = EntityLivingBase.class;
					continue;
				} else if (clazz == EntityLivingWrapper.class) {
					obj = EntityLiving.class;
					continue;
				} else if (clazz == EntityWrapper.class) {
					obj = Entity.class;
					continue;
				}
			}
			for (Method m : clazz.getMethods()) {
				if (m.getName().equals(metodName)) {
					obj = m.getReturnType();
					continue;
				}
			}
			if (!CustomNpcs.scriptHelperObfuscations) { continue; }
			Map<String, Method> methodsMap = Maps.newHashMap();
			for (Method m : clazz.getMethods()) {
				methodsMap.put(m.getName(), m);
			}
			if (AdditionalMethods.instance.obfuscations.containsValue(key)) {
				for (String s : AdditionalMethods.instance.obfuscations.keySet()) {
					if (AdditionalMethods.instance.obfuscations.get(s).equals(key)) {
						if (methodsMap.containsKey(s)) {
							obj = methodsMap.get(s).getReturnType();
						}
						break;
					}
				}
			}
			if (obj != null) {
				break;
			}
			for (Class<?> c : clazz.getClasses()) {
				if (c.getSimpleName().equals(key)) {
					obj = c;
					continue;
				}
			}
			for (Field f : clazz.getFields()) {
				if (f.getName().equals(key)) {
					obj = f.getType();
					continue;
				}
			}
			if (!CustomNpcs.scriptHelperObfuscations) {
				continue;
			}

			Map<String, Field> fieldMap = Maps.newHashMap();
			Map<String, Class<?>> classMap = Maps.newHashMap();
			for (Field f : clazz.getFields()) {
				fieldMap.put(f.getName(), f);
			}
			for (Class<?> c : clazz.getClasses()) {
				classMap.put(c.getSimpleName(), c);
			}
			if (AdditionalMethods.instance.obfuscations.containsValue(key)) {
				for (String s : AdditionalMethods.instance.obfuscations.keySet()) {
					if (AdditionalMethods.instance.obfuscations.get(s).equals(key)) {
						if (classMap.containsKey(s)) {
							obj = classMap.get(s);
						}
						if (fieldMap.containsKey(s)) {
							obj = fieldMap.get(s).getType();
						}
						break;
					}
				}
			}
			if (obj != null) {
				break;
			}
		}

		Map<String, Map<String, String[]>> map = Maps.newHashMap();
		if (obj == null) {
			return map;
		}

		char chr = Character.toChars(0x00A7)[0];
		Class<?> clazz = AdditionalMethods.getScriptClass(obj);
		if (!(obj instanceof Event) && clazz.getClasses().length > 0) {
			if (!map.containsKey("classes")) {
				map.put("classes", Maps.newHashMap());
			}
			for (Class<?> c : clazz.getClasses()) {
				String key = chr + "e" + c.getSimpleName();
				if (map.get("classes").containsKey(key)) {
					continue;
				}
				map.get("classes").put(key, new String[] { chr + "eclass " + chr + "f" + c.getName() });
			}
		}
		if (clazz.getFields().length > 0) {
			if (!map.containsKey("fields")) {
				map.put("fields", Maps.newHashMap());
			}
			for (Field f : clazz.getFields()) {
				String key = chr + "6" + f.getName();
				if (map.get("fields").containsKey(key)) {
					continue;
				}
				map.get("fields").put(key,
						new String[] { f.getType() == null ? chr + "4null" : chr + "f" + f.getType().getName() });
			}
		}
		if (clazz.getMethods().length > 0) {
			if (!map.containsKey("methods")) {
				map.put("methods", Maps.newHashMap());
			}
			for (Method m : clazz.getMethods()) {
				String key = chr + "a" + m.getName();
				String ret = m.getReturnType().getName().equals("void") ? ""
						: chr + "cReturn: " + chr + "f" + m.getReturnType().getName();
				String[] ht = ret.isEmpty() ? null : new String[] { ret };
				key += "(";
				if (m.getParameters().length > 0) {
					Parameter[] pars = m.getParameters();
					ht = new String[pars.length + (!ret.isEmpty() ? 1 : 0)];
					int i;
					for (i = 0; i < pars.length; i++) {
						String n = pars[i].getType().getSimpleName().toLowerCase();
						if (n.equalsIgnoreCase("integer")) {
							n = "int";
						}
						key += n + ",";
						ht[i] = chr + "2" + n + chr + "r - " + pars[i].getType().getName();
					}
					key = key.substring(0, key.length() - 1);
					if (!ret.isEmpty()) {
						ht[ht.length - 1] = ret;
					}
				}
				key += ")";
				if (map.get("methods").containsKey(key)) {
					continue;
				}
				map.get("methods").put(key, ht);
			}
		}
		return map;
	}

	public static Class<?> getScriptClass(Object obj) {
		Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();
		if (AdditionalMethods.instance.map.containsKey(clazz)) { clazz = AdditionalMethods.instance.map.get(clazz); }
		return clazz;
	}

	public static Object[] getScriptData(String language, ScriptContainer container,
			Map<String, Class<?>> baseFuncNames) {
		if (container == null) {
			return new Object[] { new ArrayList<IScriptData>(), "Container: null!" };
		}
		container.setEngine(language);
		String error = "";
		try {
			container.engine.eval(container.getFullCode());
		} catch (ScriptException e) {
			error = "" + e;
		}
		Bindings scriptObjectMirror = container.engine.getBindings(ScriptContext.ENGINE_SCOPE);
		List<IScriptData> list = Lists.newArrayList();
		List<ScriptData> vars = Lists.newArrayList();
		for (Map.Entry<String, Object> scopeEntry : scriptObjectMirror.entrySet()) {
			ScriptData sd = new ScriptData(scopeEntry.getKey(), scopeEntry.getValue(), language);
			sd.isConstant = container.varIsConstant(scopeEntry.getKey());
			if (sd.isConstant) {
				vars.add(sd);
			}
			list.add(sd);
		}
		for (IScriptData isd : list) {
			if (isd.getType() == 12) {
				((ScriptData) isd).setVarToFunction(vars, baseFuncNames);
			}
		}
		return new Object[] { list, error };
	}

	/**
	 * 1234567890.9 to 1,2G
	 * @param value - any number
	 * @param color - need set color
	 * @return string
	 */
	public static String getTextReducedNumber(double value, boolean isInteger, boolean color, boolean notPfx) {
		if (value == 0) {
			return "0";
		}
		String chr = new String(Character.toChars(0x00A7));
		String chrPR = new String(Character.toChars(0x2248));
		chrPR = "";
		String type = "";
		String sufc = "";
		if (value == 0L) {
			return String.valueOf(value).replace(".", ",");
		}
		double corr = value;
		int exp = 0;
		boolean negatively = false;

		if (value <= 0) {
			negatively = true;
			value *= -1.0d;
		}
		if (value < Math.pow(10, 3)) { /* xxxx,x hecto */
			corr = Math.round(value * 10.0d) / 10.0d;
		} else if (value < Math.pow(10, 6)) { /* xxx,xxK kilo */
			corr = Math.round(value / 100.0d) / 10.0d;
			if (color) {
				type = chr + "e";
			}
			type += "K";
			if (corr * Math.pow(10, 3) != value) {
				sufc = chrPR;
			}
		} else if (value < Math.pow(10, 9)) { /* xxx,xxM mega */
			corr = Math.round(value / Math.pow(10, 3)) / 10.0d;
			if (color) {
				type = chr + "a";
			}
			type += "M";
			if (corr * Math.pow(10, 6) != value) {
				sufc = chrPR;
			}
		} else if (value < Math.pow(10, 12)) { /* xxx,xxG giga */
			corr = Math.round(value / Math.pow(10, 6)) / 10.0d;
			if (color) {
				type = chr + "2";
			}
			type += "G";
			if (corr * Math.pow(10, 9) != value) {
				sufc = chrPR;
			}
		} else if (value < Math.pow(10, 15)) { /* xxx,xxT tera */
			corr = Math.round(value / Math.pow(10, 9)) / 10.0d;
			if (color) {
				type = chr + "b";
			}
			type += "T";
			if (corr * Math.pow(10, 12) != value) {
				sufc = chrPR;
			}
		} else if (value < Math.pow(10, 18)) { /* xxx,xxP peta */
			corr = Math.round(value / Math.pow(10, 12)) / 10.0d;
			if (color) {
				type = chr + "3";
			}
			type += "P";
			if (corr * Math.pow(10, 15) != value) {
				sufc = chrPR;
			}
		} else if (value < Math.pow(10, 21)) { /* xxx,xxE hexa */
			corr = Math.round(value / Math.pow(10, 15)) / 10.0d;
			if (color) {
				type = chr + "9";
			}
			type += "E";
			if (corr * Math.pow(10, 18) != value) {
				sufc = chrPR;
			}
		} else if (value < Math.pow(10, 24)) { /* xxx,xxZ zetta */
			corr = Math.round(value / Math.pow(10, 18)) / 10.0d;
			if (color) {
				type = chr + "d";
			}
			type += "Z";
			if (corr * Math.pow(10, 21) != value) {
				sufc = chrPR;
			}
		} else if (value < Math.pow(10, 27)) { /* xxx,xxY yotta */
			corr = Math.round(value / Math.pow(10, 21)) / 10.0d;
			if (color) {
				type = chr + "5";
			}
			type += "Y";
			if (corr * Math.pow(10, 24) != value) {
				sufc = chrPR;
			}
		} else { /* x,xxxe+exp */
			if (String.valueOf(value).indexOf("e+") >= 0 || String.valueOf(value).indexOf("E+") >= 0) {
				String index = "e+";
				if (String.valueOf(value).indexOf("E+") >= 0) {
					index = "E+";
				}
				exp = Integer.valueOf(String.valueOf(value).substring(String.valueOf(value).indexOf(index) + 2));
				corr = Math
						.round(Integer.valueOf(String.valueOf(value).substring(0, String.valueOf(value).indexOf(index)))
								* 1000.0d)
						/ 1000.0d;
			} else {
				exp = String.valueOf(corr).length();
				corr = value;
			}
			if (color) {
				type = chr + "4";
			}
			type = "E+" + exp;
		}
		if (negatively) { /* отрицательное или ноль */
			if (color) {
				sufc = chr + "c";
			}
			if (corr != 0.0d) {
				sufc += "-";
			}
		}
		String end = "";
		if (color) {
			end = chr + "r";
		}
		if (notPfx) {
			sufc = "";
		}
		String num = isInteger ? ("" + (int) corr) : ("" + corr).replace(".", ",");
		return sufc + num + type + end;
	}

	public static int inventoryItemCount(EntityPlayer player, ItemStack stack, Availability availability,
			boolean ignoreDamage, boolean ignoreNBT) {
		if (player == null || (availability != null && !availability.isAvailable(player)) || stack.isEmpty()) {
			return 0;
		}
		int count = 0;
		for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
			ItemStack is = player.inventory.mainInventory.get(i);
			if (NoppesUtilServer.IsItemStackNull(is)) {
				continue;
			}
			if (NoppesUtilPlayer.compareItems(stack, is, ignoreDamage, ignoreNBT)) {
				count += is.getCount();
			}
		}
		return count;
	}

	public static String match(String text, int pos, String startCharts, String endCharts) {
		try {
			int s = 0, e = text.length();
			for (int i = pos - 1; i >= 0 && i<text.length(); i--) {
				char c = text.charAt(i);
				if (startCharts != null) {
					if (startCharts.indexOf(""+c)!=-1) {
						s = i+1;
						break;
					}
					continue;
				}
				if (Character.toChars(0x000A)[0]==c || !Character.isAlphabetic(c) && !Character.isDigit(c)) {
					if ( c== '.' || c == '(' || c == Character.toChars(0x000A)[0]) { s = i + 1; }
					else { s = i+1; }
					break;
				}
			}
			for (int i = pos; i >= 0 && i < text.length(); i++) {
				char c = text.charAt(i);
				if (endCharts != null) {
					if (startCharts.indexOf(""+c)!=-1) {
						e = i;
						break;
					}
					continue;
				}
				if (!Character.isAlphabetic(text.charAt(i)) && !Character.isDigit(text.charAt(i))) {
					e = i;
					break;
				}
			}
			String key = text.substring(s, e);
			while (key.indexOf(" ") != -1) { key = key.replace(" ", ""); }
			return key;
		} catch (Exception e) { }
		return "";
	}

	/** Correct deletion of folders */
	public static boolean remove(File directory) {
		if (!directory.isDirectory()) {
			return directory.delete();
		}
		File[] list = directory.listFiles();
		if (list != null) {
			for (File tempFile : list) {
				if (tempFile.isDirectory()) {
					remove(tempFile);
				}
				tempFile.delete();
			}
		}
		return directory.delete();
	}

	public static boolean removeItem(EntityPlayerMP player, ItemStack stack, boolean ignoreDamage, boolean ignoreNBT) {
		if (player == null || stack.isEmpty()) {
			return false;
		}
		int count = stack.getCount();
		for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
			ItemStack is = player.inventory.getStackInSlot(i);
			if (NoppesUtilServer.IsItemStackNull(is)) {
				continue;
			}
			if (NoppesUtilPlayer.compareItems(stack, is, ignoreDamage, ignoreNBT)) {
				if (count < is.getCount()) {
					is.splitStack(count);
					AdditionalMethods.updatePlayerInventory(player);
					return true;
				}
				count -= is.getCount();
				player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
			}
		}
		return count <= 0;
	}
	
	public static boolean canAddItemAfterRemoveItems(EntityPlayer player, ItemStack addStack, NonNullList<ItemStack> removeItems, boolean ignoreDamage, boolean ignoreNBT) {
		if (player == null || addStack.isEmpty()) { return false; }
		if (player.capabilities.isCreativeMode) { return true; }
		NonNullList<ItemStack> inv = NonNullList.<ItemStack>withSize(36, ItemStack.EMPTY);
		for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
			if (NoppesUtilServer.IsItemStackNull(player.inventory.getStackInSlot(i))) { continue; }
			inv.set(i, player.inventory.getStackInSlot(i).copy());
		}
		if (removeItems!=null && !removeItems.isEmpty()) {
			for (ItemStack stack : removeItems) {
				if (NoppesUtilServer.IsItemStackNull(stack)) { continue; }
				int count = stack.getCount();
				for (int i = 0; i < inv.size(); ++i) {
					ItemStack is = inv.get(i);
					if (NoppesUtilServer.IsItemStackNull(is)) { continue; }
					if (NoppesUtilPlayer.compareItems(stack, is, ignoreDamage, ignoreNBT)) {
						if (count < is.getCount()) {
							is.splitStack(count);
							inv.set(i, is);
							count = 0;
						}
						else {
							count -= is.getCount();
							inv.set(i, ItemStack.EMPTY);
						}
						if (count<=0) { break; }
					}
				}
			}
		}
		for (int i = 0; i < inv.size(); ++i) {
			if (inv.get(i).isEmpty() || NoppesUtilPlayer.compareItems(addStack, inv.get(i), ignoreDamage, ignoreNBT)) { return true; }
		}
		return false;
	}
	
	public static boolean canRemoveItems(EntityPlayer player, NonNullList<ItemStack> items, boolean ignoreDamage, boolean ignoreNBT) {
		if (player == null || items.isEmpty()) {
			return false;
		}
		NonNullList<ItemStack> inv = NonNullList.<ItemStack>withSize(36, ItemStack.EMPTY);
		for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
			if (NoppesUtilServer.IsItemStackNull(player.inventory.getStackInSlot(i))) { continue; }
			inv.set(i, player.inventory.getStackInSlot(i).copy());
		}
		boolean canRemove = true;
		for (ItemStack stack : items) {
			if (NoppesUtilServer.IsItemStackNull(stack)) { continue; }
			int count = stack.getCount();
			for (int i = 0; i < inv.size(); ++i) {
				ItemStack is = inv.get(i);
				if (NoppesUtilServer.IsItemStackNull(is)) { continue; }
				if (NoppesUtilPlayer.compareItems(stack, is, ignoreDamage, ignoreNBT)) {
					if (count < is.getCount()) {
						is.splitStack(count);
						inv.set(i, is);
						count = 0;
					}
					else {
						count -= is.getCount();
						inv.set(i, ItemStack.EMPTY);
					}
					if (count<=0) { break; }
				}
			}
			if (count>0) {
				canRemove = false;
				break;
			}
		}
		return canRemove;
	}

	@SideOnly(Side.CLIENT)
	public static void resetRecipes(EntityPlayer player, GuiContainer gui) {
		CustomNpcs.proxy.updateRecipes(null, false, false, "this.resetRecipes()");

		Container conteiner = null;
		SlotCrafting slotIn = null;
		List<RecipeList> lists = null;

		if (gui instanceof GuiCrafting) {
			GuiNpcRecipeBook recipeBookGui = new GuiNpcRecipeBook(true);
			recipeBookGui.getRecipeTabs().add(new GuiNpcButtonRecipeTab(0, CustomItems.tab, true));
			ObfuscationHelper.setValue(GuiCrafting.class, (GuiCrafting) gui, recipeBookGui, GuiRecipeBook.class);

			conteiner = (ContainerWorkbench) gui.inventorySlots;
			slotIn = new SlotNpcCrafting(player, ((ContainerWorkbench) conteiner).craftMatrix,
					((ContainerWorkbench) conteiner).craftResult, 0, 124, 35);
			slotIn.slotNumber = 0;
			lists = RecipeBookClient.RECIPES_BY_TAB.get(CustomItems.tab);
		} else if (gui instanceof GuiInventory) {
			GuiNpcRecipeBook recipeBookGui = new GuiNpcRecipeBook(true);
			recipeBookGui.getRecipeTabs().add(new GuiNpcButtonRecipeTab(0, CustomItems.tab, true));
			ObfuscationHelper.setValue(GuiInventory.class, (GuiInventory) gui, recipeBookGui, 3);

			conteiner = (ContainerPlayer) gui.inventorySlots;
			slotIn = new SlotNpcCrafting(player, ((ContainerPlayer) conteiner).craftMatrix,
					((ContainerPlayer) conteiner).craftResult, 0, 154, 28);
			slotIn.slotNumber = 0;
			lists = RecipeBookClient.RECIPES_BY_TAB.get(CustomItems.tab);
		}
		if (gui instanceof GuiNpcCarpentryBench) {
			lists = ClientProxy.MOD_RECIPES_BY_TAB.get(CustomItems.tab);
		}
		if (slotIn != null && conteiner != null) {
			conteiner.inventorySlots.remove(slotIn.slotNumber);
			conteiner.inventoryItemStacks.remove(slotIn.slotNumber);
			conteiner.inventorySlots.add(slotIn.slotNumber, slotIn);
			conteiner.inventoryItemStacks.add(slotIn.slotNumber, ItemStack.EMPTY);
		}
		RecipeBook book = ((EntityPlayerSP) player).getRecipeBook();
		if (book != null && lists != null) {
			for (RecipeList list : lists) {
				list.updateKnownRecipes(book);
			}
		}
	}

	/**
	 * @param ticks - time
	 * @param isMilliSeconds = true - milliseconds (1 sec = 1000 ms)
	 * @param isMilliSeconds = false - minecraft time (1 sec = 20 tick)
	 * @param colored - added color
	 * @param upped - only the maximum period (years or months or days, etc.)
	 * @return
	 */
	public static String ticksToElapsedTime(long ticks, boolean isMilliSeconds, boolean colored, boolean upped) {
		String time = isMilliSeconds ? "0.000" : "--/--";
		String chr = new String(Character.toChars(0x00A7));
		if (ticks < 0) {
			return (colored ? chr + "8" : "") + time;
		}
		long timeSeconds = (isMilliSeconds ? ticks : ticks * 50L) / 1000L;
		int ms = (int) ((isMilliSeconds ? ticks : ticks * 50L) % 1000L);
		int sec = (int) (timeSeconds % 60L);
		int min = (int) (timeSeconds % 3600L) / 60;
		int hour = (int) (timeSeconds % 86400L) / 3600;
		int day = (int) (timeSeconds % 2592000L) / 86400;
		int month = (int) (timeSeconds % 31449600L) / 2620800;
		int year = (int) (timeSeconds / 31449600L);
		String mins, secs;
		if (min < 10) {
			mins = "0" + min;
		} else {
			mins = "" + min;
		}
		if (sec < 10) {
			secs = "0" + sec;
		} else {
			secs = "" + sec;
		}
		time = "";
		if (year > 0) {
			if (colored) {
				time += chr + "r" + year + chr + "6y ";
			} else {
				time += year + "y ";
			}
		}
		if (upped && time.length() > 0) {
			return time;
		}
		if (month > 0) {
			if (colored) {
				time += chr + "r" + month + chr + "1m ";
			} else {
				time += month + "m ";
			}
		}
		if (upped && time.length() > 0) {
			return time;
		}
		if (day > 0) {
			if (colored) {
				time += chr + "r" + day + chr + "2d ";
			} else {
				time += day + "d ";
			}
		}
		if (upped && time.length() > 0) {
			return time;
		}
		if (hour > 0 || year > 0 || month > 0 || day > 0) {
			if (colored) {
				time += chr + "r" + hour + ":";
			} else {
				time += hour + ":";
			}
		}
		time += (colored ? chr + "r" : "") + mins + ":" + secs;
		if (isMilliSeconds) {
			time += (colored ? chr + "8" : "") + "." + ms;
		}
		return time;
	}

	public static void updatePlayerInventory(EntityPlayerMP player) {
		PlayerQuestData playerdata = PlayerData.get(player).questData;
		for (QuestData data : playerdata.activeQuests.values()) {
			for (IQuestObjective obj : data.quest.getObjectives((IPlayer<?>) NpcAPI.Instance().getIEntity(player))) {
				if (obj.getType() != 0) {
					continue;
				}
				playerdata.checkQuestCompletion(player, data);
			}
		}
	}

	public static boolean equalsDeleteColor(String str0, String str1, boolean ignoreCase) {
		str0 = AdditionalMethods.instance.deleteColor(str0);
		str1 = AdditionalMethods.instance.deleteColor(str1);
		return ignoreCase ? str0.equalsIgnoreCase(str1) : str0.equals(str1);
	}

	public static String getDeleteColor(Map<?, ?> map, String str0, boolean isKey, boolean ignoreCase) {
		for (Object obj : (isKey ? map.keySet() : map.values())) {
			if (AdditionalMethods.equalsDeleteColor(obj.toString(), str0, ignoreCase)) { return obj.toString(); }
		}
		return "";
	}

	public static boolean containsDeleteColor(Set<String> set, String text, boolean ignoreCase) {
		if (set==null || text==null) { return false; }
		for (String str : set) {
			if (AdditionalMethods.equalsDeleteColor(str, text, ignoreCase)) { return true; }
		}
		return false;
	}

	public static void addStackToList(List<ItemStack> list, ItemStack stack) {
		int count = stack.getCount();
		for (ItemStack st : list) {
			if (AdditionalMethods.canMergeStacks(st, stack)) {
				if (st.getMaxStackSize() >= st.getCount()+count) {
					st.setCount(st.getCount()+count);
					count = 0;
					break;
				} else {
					count -= (st.getMaxStackSize()-st.getCount());
					st.setCount(st.getMaxStackSize());
				}
			}
		}
		if (count>0) {
			ItemStack st = stack.copy();
			st.setCount(count);
			list.add(st);
		}
	}

	public static boolean canMergeStacks(ItemStack stack1, ItemStack stack2) {
		return !stack1.isEmpty() &&
				stack1.getItem() == stack2.getItem() &&
				(!stack1.getHasSubtypes() || stack1.getMetadata() == stack2.getMetadata()) &&
				ItemStack.areItemStackTagsEqual(stack1, stack2) &&
				stack1.isStackable() &&
				stack1.getCount() < stack1.getMaxStackSize();
	}
	
	@Override
	public double distanceTo(IEntity<?> entity, IEntity<?> target) {
		return this.distanceTo(entity.getMCEntity().posX, entity.getMCEntity().posY, entity.getMCEntity().posZ, target.getMCEntity().posX, target.getMCEntity().posY, target.getMCEntity().posZ);
	}
	
	@Override
	public double distanceTo(double x0, double y0, double z0, double x1, double y1, double z1) {
		double d0 = x0 - x1;
		double d1 = y0 - y1;
		double d2 = z0 - z1;
		return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
	}

	public static double distanceTo(BlockPos pos0, BlockPos pos1) {
		return AdditionalMethods.instance.distanceTo(pos0.getX(), pos0.getY(), pos0.getZ(), pos1.getX(), pos1.getY(), pos1.getZ());
	}

	/**
	 * @param dx - posX from look angles
	 * @param dy - posY from look angles
	 * @param dz - posZ from look angles
	 * @param mx - posX to look angles
	 * @param my - posY to look angles
	 * @param mz - posZ to look angles
	 * @return {yaw, pitch, radiusXZ, dist}
	 */
	@Override
	public double[] getAngles3D(double dx, double dy, double dz, double mx, double my, double mz) {
		double yaw, pitch, dist, xVal = mx - dx, yVal = my - dy, zVal = mz - dz;
		double radiusXZ = Math.sqrt(Math.pow(xVal, 2) + Math.pow(zVal, 2));
		pitch = Math.atan(yVal / radiusXZ) * 180 / Math.PI;
		if (!(pitch >= -90 && pitch <= 90)) {pitch = 0;}
		if (xVal <= 0) {yaw=90 + Math.atan(zVal / xVal) * 180 / Math.PI;}
		else {yaw = 270 + Math.atan(zVal / xVal) * 180 / Math.PI;}
		if (!(yaw >= 0 && yaw <= 360)) {yaw = 0;}
		dist = Math.sqrt(Math.pow(radiusXZ, 2) + Math.pow(yVal, 2));
		return new double[] {yaw, pitch, radiusXZ, dist};
	}

	@Override
	public double[] getAngles3D(IEntity<?> entity, IEntity<?> target) {
		return this.getAngles3D(entity.getMCEntity().posX, entity.getMCEntity().posY, entity.getMCEntity().posZ, target.getMCEntity().posX, target.getMCEntity().posY, target.getMCEntity().posZ);
	}
	
	/**
	 * @param pos0 - block position from look angles
	 * @param pos1 - block position to look angles
	 * @return {yaw, pitch, radiusXZ, dist}
	 */
	public static double[] getAngles3D(BlockPos pos0, BlockPos pos1) {
		return AdditionalMethods.instance.getAngles3D(pos0.getX(), pos0.getY(), pos0.getZ(), pos1.getX(), pos1.getY(), pos1.getZ());
	}
	
	@Override
	public double[] getPosition(IEntity<?> entity, double yaw, double pitch, double radius) {
		return this.getPosition(entity.getMCEntity().posX, entity.getMCEntity().posY, entity.getMCEntity().posZ, yaw, pitch, radius);
	}
	
	/**
	 * @param cx - X-axis position center
	 * @param cy - Y-axis position center
	 * @param cz - Z-axis position center
	 * @param radius
	 * @return {x, y, z}
	 */
	@Override
	public double[] getPosition(double cx, double cy, double cz, double yaw, double pitch, double radius) {
		if (radius<0.0d) { radius *= -1.0d; }
		double x = Math.sin(yaw * Math.PI / 180) * radius * -1;
		double y = Math.sin(pitch * Math.PI / 180) * radius;
		double z = Math.cos(yaw * Math.PI / 180) * radius;
		return new double[] {cx+x, cy+y, cz+z};
	}

	/**
	 * @param pos - block position center
	 * @param radius
	 * @return {x, y, z}
	 */
	public static double[] getPosition(BlockPos pos, double yaw, double pitch, double radius) {
		return AdditionalMethods.instance.getPosition(pos.getX(), pos.getY(), pos.getZ(), yaw, pitch, radius);
	}
	
	@Override
	public double[] getVector3D(IEntity<?> entity, IEntity<?> target) {
		return this.getVector3D(entity.getMCEntity().posX, entity.getMCEntity().posY, entity.getMCEntity().posZ, target.getMCEntity().posX, target.getMCEntity().posY, target.getMCEntity().posZ);
	}
	
	@Override
	public double[] getVector3D(IEntity<?> entity, IPos pos) {
		return this.getVector3D(entity.getMCEntity().posX, entity.getMCEntity().posY, entity.getMCEntity().posZ, pos.getX() + 0.5d,  pos.getY(),  pos.getZ() + 0.5d);
	}
	
	@Override
	public double[] getVector3D(double dx, double dy, double dz, double mx, double my, double mz) {
		double ax,ay,az, yaw, hVal = 1.5D + my - dy;
		if (hVal < 0.35D) {hVal = 0.35D;}
		double xVal = mx - dx, zVal = mz - dz;
		ay = (-3.0D + Math.sqrt(9.0D - 16.0D * (-0.75D - hVal))) / 8.0D;
		double radiusXZ = Math.sqrt(Math.pow(xVal, 2.0D) + Math.pow(zVal, 2.0D));
		double impXZ = -4.5D + Math.sqrt(20.25D + 4.0D * radiusXZ);
		if (xVal <= 0) {yaw = 90.0D + Math.atan(zVal / xVal) * 180 / Math.PI;}
		else {yaw = 270.0D + Math.atan(zVal / xVal) * 180 / Math.PI;}
		if (!(yaw >= 0 && yaw <= 360)) {yaw = 0;}
		if (yaw == 0.0D) {yaw = 180.0D;}
		else if (yaw == 180.0D) {yaw = 0.0D;}
		ax = Math.sin(yaw * Math.PI / 180) * impXZ * -1;
		az = Math.cos(yaw * Math.PI / 180) * impXZ;
		return new double[] {ax, ay, az, yaw};
	}

	public static double[] getVector3D(BlockPos pos0, BlockPos pos1) {
		return AdditionalMethods.instance.getVector3D(pos0.getX(), pos0.getY(), pos0.getZ(), pos1.getX(), pos1.getY(), pos1.getZ());
	}

	/* Teleport Entity to Spawn next Dimensions */
	public static Entity travelAndCopyEntity(MinecraftServer server, Entity entity, int dimension) throws CommandException {
		World world = server.getWorld(dimension);
		if (world == null) { throw new CommandException("Couldn't find dimension " + dimension); }
		if (entity instanceof EntityPlayerMP) {
			server.getPlayerList().transferPlayerToDimension((EntityPlayerMP) entity, dimension, new CustomNpcsTeleporter((WorldServer) server.getEntityWorld()));
			return entity;
		}
		else { return travelEntity(server, entity, dimension); }
	}
	
	/* [Teleport] Copy and Place Entity to Spawn next Dimensions */
	public static Entity travelEntity(MinecraftServer server, Entity entity, int dimensionId) {
		if (!entity.world.isRemote && !entity.isDead) {
			entity.world.profiler.startSection("changeDimension");
			int dimensionStart = entity.dimension;
			WorldServer worldserverStart = server.getWorld(dimensionStart);
			WorldServer worldserverEnd = server.getWorld(dimensionId);
			entity.dimension = dimensionId;
			Entity newEntity = EntityList.createEntityByIDFromName(EntityList.getKey(entity.getClass()), worldserverEnd);
			if (newEntity != null) {
				try { AdditionalMethods.instance.copyDataFromOld.invoke(newEntity, entity); }
				catch (Exception e) { e.printStackTrace(); }
				entity.world.removeEntity(entity);
				newEntity.forceSpawn = true;
				worldserverEnd.spawnEntity(newEntity);
			}
			worldserverEnd.updateEntityWithOptionalForce(newEntity, true);
			entity.isDead = true;
			entity.world.profiler.endSection();
			worldserverStart.resetUpdateEntityTick();
			worldserverEnd.resetUpdateEntityTick();
			entity.world.profiler.endSection();
			return newEntity;
		}
		return entity;
	}
	
	public static Entity teleportEntity(MinecraftServer server, Entity entity, int dimension, double x, double y, double z) throws CommandException {
		if (entity==null) { return null; }
		if (entity.world.provider.getDimension()!=dimension) { entity = travelAndCopyEntity(server, entity, dimension); }
		CoordinateArg xn = CommandBase.parseCoordinate(entity.posX, ""+x, true);
		CoordinateArg yn = CommandBase.parseCoordinate(entity.posY, ""+y, -4096, 4096, false);
		CoordinateArg zn = CommandBase.parseCoordinate(entity.posZ, ""+z, true);
		CoordinateArg w = CommandBase.parseCoordinate((double)entity.rotationYaw, "~", false);
		CoordinateArg p = CommandBase.parseCoordinate((double)entity.rotationPitch, "~", false);
		teleportEntity(entity, xn, yn, zn, w, p);
		return entity;
	}
	
	public static Entity teleportEntity(MinecraftServer server, Entity entity, int dimension, BlockPos pos) throws CommandException {
		return AdditionalMethods.teleportEntity(server, entity, dimension, (double) pos.getX()+0.5d, (double) pos.getY(), (double) pos.getZ()+0.5d);
	}
	
	public static void teleportEntity(Entity entityIn, double x, double y, double z) {
		teleportEntity(entityIn, x, y, z, 0.0f, 0.0f);
	}
	
	public static void teleportEntity(Entity entityIn, double x, double y, double z, float yaw, float pitch) {
		try {
			CommandBase.CoordinateArg argX = CommandBase.parseCoordinate(entityIn.posX, ""+x, true);
			CommandBase.CoordinateArg argY = CommandBase.parseCoordinate(entityIn.posY, ""+y, -4096, 4096, false);
			CommandBase.CoordinateArg argZ = CommandBase.parseCoordinate(entityIn.posZ, ""+z, true);
			CommandBase.CoordinateArg argYaw = CommandBase.parseCoordinate((double) entityIn.rotationYaw, ""+yaw, false);
			CommandBase.CoordinateArg argPitch = CommandBase.parseCoordinate((double) entityIn.rotationPitch, ""+pitch, false);
			teleportEntity(entityIn, argX, argY, argZ, argYaw, argPitch);
		}
		catch (NumberInvalidException e) { }
	}
	
	/* Vanila Teleport in world */
	public static void teleportEntity(Entity entityIn, CommandBase.CoordinateArg argX, CommandBase.CoordinateArg argY, CommandBase.CoordinateArg argZ, CommandBase.CoordinateArg argYaw, CommandBase.CoordinateArg argPitch) {
		if (entityIn instanceof EntityPlayerMP)
		{
			Set<SPacketPlayerPosLook.EnumFlags> set = EnumSet.<SPacketPlayerPosLook.EnumFlags>noneOf(SPacketPlayerPosLook.EnumFlags.class);
			if (argX.isRelative()) { set.add(SPacketPlayerPosLook.EnumFlags.X); }
			if (argY.isRelative()) { set.add(SPacketPlayerPosLook.EnumFlags.Y); }
			if (argZ.isRelative()) { set.add(SPacketPlayerPosLook.EnumFlags.Z); }
			if (argPitch.isRelative()) { set.add(SPacketPlayerPosLook.EnumFlags.X_ROT); }
			if (argYaw.isRelative()) { set.add(SPacketPlayerPosLook.EnumFlags.Y_ROT); }
			float f = (float) argYaw.getAmount();
			if (!argYaw.isRelative()) { f = MathHelper.wrapDegrees(f); }
			float f1 = (float) argPitch.getAmount();
			if (!argPitch.isRelative()) { f1 = MathHelper.wrapDegrees(f1); }
			entityIn.dismountRidingEntity();
			((EntityPlayerMP)entityIn).connection.setPlayerLocation(argX.getAmount(), argY.getAmount(), argZ.getAmount(), f, f1, set);
			entityIn.setRotationYawHead(f);
		} else {
			float f2 = (float) MathHelper.wrapDegrees(argYaw.getResult());
			float f3 = (float) MathHelper.wrapDegrees(argPitch.getResult());
			f3 = MathHelper.clamp(f3, -90.0F, 90.0F);
			entityIn.setLocationAndAngles(argX.getResult(), argY.getResult(), argZ.getResult(), f2, f3);
			entityIn.setRotationYawHead(f2);
		}
		if (!(entityIn instanceof EntityLivingBase) || !((EntityLivingBase) entityIn).isElytraFlying()) {
			entityIn.motionY = 0.0D;
			entityIn.onGround = true;
		}
	}

	@Override
	public IEntity<?> transferEntity(IEntity<?> entity, int dimension, IPos pos) {
		Entity e = null;
		try {
			if (pos!=null) { e = AdditionalMethods.teleportEntity(CustomNpcs.Server, entity.getMCEntity(), dimension, pos.getMCBlockPos()); }
			else { e = AdditionalMethods.travelAndCopyEntity(CustomNpcs.Server, entity.getMCEntity(), dimension); }
		}
		catch (CommandException error) {}
		if (e!=null) { return NpcAPI.Instance().getIEntity(e); }
		return entity;
	}
	
	public static String deleteSpase(String str) {
		if (str==null || str.isEmpty()) { return str; }
		while (str.indexOf(" ")!=-1) { str = str.replace(" ", ""); }
		while (str.indexOf(""+((char) 9))!=-1) { str = str.replace(""+((char) 9), ""); }
		return str;
	}

	public static Map<String, Class<?>> getVariablesInBody(String body, Map<String, Map<Integer, ScriptData>> data, Map<String, Class<?>> data2) {
		Map<String, Class<?>> map = Maps.newHashMap();
		if (body==null || body.isEmpty() || body.indexOf("var")==-1) { return map;}
		try {
			while (body.indexOf("var")!=-1) {
				int e = body.indexOf(";", body.indexOf("var")+3);
				if (e==-1) { e = body.indexOf(""+((char) 10), body.indexOf("var")+3);}
				if (e==-1) { break; }
				String var = AdditionalMethods.deleteSpase(body.substring(body.indexOf("var")+3, e));
				if (var.indexOf("=")!=-1) {
					String key = var.substring(0, var.indexOf("="));
					String part = var.substring(var.indexOf("=")+1);
					
					if (part.startsWith("Java.type(")) {
						try { map.put(key, Class.forName(part.substring(11, part.lastIndexOf(")")-1))); }
						catch (Exception ec) { }
						body = body.substring(e+1);
						continue;
					}
					
					List<String> keys = Lists.<String>newArrayList();
					while (part.indexOf('.')!=-1) {
						keys.add(part.substring(0, part.indexOf('.')));
						part = part.substring(part.indexOf('.')+1);
					}
					keys.add(part);
					boolean start = true;
					Object obj = null;
					for (String k : keys) {
						if (start) {
							if (map.containsKey(k)) {
								obj = map.get(k);
							} else if (data.containsKey(k)) {
								obj = data.get(k).get(0);
							}
							start = false;
							continue;
						}
						if (obj==null) { break; }
						Class<?> clazz = AdditionalMethods.getScriptClass(obj);
						obj = null;
						String metodName = k.indexOf('(')!=-1 ? k.substring(0, k.indexOf('(')) : "get"+(""+k.charAt(0)).toUpperCase()+k.substring(1);
						if (metodName.equals("getMCEntity")) {
							if (clazz == PlayerWrapper.class) {
								obj = EntityPlayerMP.class;
								continue;
							} else if (clazz == EntityLivingBaseWrapper.class) {
								obj = EntityLivingBase.class;
								continue;
							} else if (clazz == EntityLivingWrapper.class) {
								obj = EntityLiving.class;
								continue;
							} else if (clazz == EntityWrapper.class) {
								obj = Entity.class;
								continue;
							}
						}
						for (Method m : clazz.getMethods()) {
							if (m.getName().equals(metodName)) {
								obj = m.getReturnType();
								continue;
							}
						}
						if (!CustomNpcs.scriptHelperObfuscations) { continue;}
						Map<String, Method> methodsMap = Maps.newHashMap();
						for (Method m : clazz.getMethods()) {
							methodsMap.put(m.getName(), m);
						}
						if (AdditionalMethods.instance.obfuscations.containsValue(k)) {
							for (String s : AdditionalMethods.instance.obfuscations.keySet()) {
								if (AdditionalMethods.instance.obfuscations.get(s).equals(k)) {
									if (methodsMap.containsKey(s)) {
										obj = methodsMap.get(s).getReturnType();
									}
									break;
								}
							}
						}
						if (obj != null) {
							break;
						}
						for (Class<?> c : clazz.getClasses()) {
							if (c.getSimpleName().equals(k)) {
								obj = c;
								continue;
							}
						}
						for (Field f : clazz.getFields()) {
							if (f.getName().equals(k)) {
								obj = f.getType();
								continue;
							}
						}
						if (!CustomNpcs.scriptHelperObfuscations) {
							continue;
						}

						Map<String, Field> fieldMap = Maps.newHashMap();
						Map<String, Class<?>> classMap = Maps.newHashMap();
						for (Field f : clazz.getFields()) {
							fieldMap.put(f.getName(), f);
						}
						for (Class<?> c : clazz.getClasses()) {
							classMap.put(c.getSimpleName(), c);
						}
						if (AdditionalMethods.instance.obfuscations.containsValue(k)) {
							for (String s : AdditionalMethods.instance.obfuscations.keySet()) {
								if (AdditionalMethods.instance.obfuscations.get(s).equals(k)) {
									if (classMap.containsKey(s)) {
										obj = classMap.get(s);
									}
									if (fieldMap.containsKey(s)) {
										obj = fieldMap.get(s).getType();
									}
									break;
								}
							}
						}
						if (obj != null) {
							break;
						}
					}
					map.put(key, AdditionalMethods.getScriptClass(obj));
				}
				body = body.substring(e+1);
			}
			
		} catch (Exception e) { }
		return map;
	}
	
	@Override
	public NBTBase writeObjectToNbt(Object value) {
		if (value.getClass().isArray()) {
			Object[] vs = (Object[]) value;
			if (vs.length==0) {
				return new NBTTagList();
			}
			if (vs[0] instanceof Byte) {
				List<Byte> l = Lists.<Byte>newArrayList();
				for (Object v : vs) { if (v instanceof Byte) { l.add((Byte) v); } }
				byte[] arr = new byte[l.size()];
				int i = 0;
				for (byte d : l) { arr[i] = d; i++; }
				return new NBTTagByteArray(arr);
			}
			else if (vs[0] instanceof Integer) {
				List<Integer> l = Lists.<Integer>newArrayList();
				for (Object v : vs) { if (v instanceof Integer) { l.add((Integer) v); } }
				int[] arr = new int[l.size()];
				int i = 0;
				for (int d : l) { arr[i] = d; i++; }
				return new NBTTagIntArray(arr);
			}
			else if (vs[0] instanceof Long) {
				List<Long> l = Lists.<Long>newArrayList();
				for (Object v : vs) { if (v instanceof Long) { l.add((Long) v); } }
				long[] arr = new long[l.size()];
				int i = 0;
				for (long d : l) { arr[i] = d; i++; }
				return new NBTTagLongArray(arr);
			}
			else if (vs[0] instanceof Short || vs[0] instanceof Float || vs[0] instanceof Double) {
				NBTTagList list = new NBTTagList();
				for (Object v : vs) {
					double d;
					if (v instanceof Short) { d = (double) (Short) v; }
					else if (v instanceof Float) { d = (double) (Float) v; }
					else if (v instanceof Double) { d = (Double) v; }
					else { continue; }
					list.appendTag(new NBTTagDouble(d));
				}
				return list;
			}
		}
		else if (value instanceof Byte) { return new NBTTagByte((Byte) value); }
		else if (value instanceof Short) { return new NBTTagShort((Short) value); }
		else if (value instanceof Integer) { return new NBTTagInt((Integer) value); }
		else if (value instanceof Long) { return new NBTTagLong((Long) value); }
		else if (value instanceof Float) { return new NBTTagFloat((Float) value); }
		else if (value instanceof Double) { return new NBTTagDouble((Double) value); }
		else if (value instanceof String) { return new NBTTagString((String) value); }
		else if (value instanceof Bindings) {
			String clazz = value.toString();
			if (!clazz.equals("[object Array]") && !clazz.equals("[object Object]")) { return null; }
			boolean isArray = clazz.equals("[object Array]");
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("IsArray", isArray);
			for (Map.Entry<String, Object> scopeEntry : ((Bindings) value).entrySet()) {
				Object v = scopeEntry.getValue();
				if (v.getClass().isArray()) {
					Object[] vs = (Object[]) v;
					if (vs.length==0) {
						nbt.setTag(scopeEntry.getKey(), new NBTTagList());
						continue;
					}
					if (vs[0] instanceof Byte) {
						List<Byte> l = Lists.<Byte>newArrayList();
						for (Object va : vs) { if (va instanceof Byte) { l.add((Byte) va); } }
						byte[] arr = new byte[l.size()];
						int i = 0;
						for (byte d : l) { arr[i] = d; i++; }
						nbt.setByteArray(scopeEntry.getKey(), arr);
					}
					else if (vs[0] instanceof Integer) {
						List<Integer> l = Lists.<Integer>newArrayList();
						for (Object va : vs) { if (va instanceof Integer) { l.add((Integer) va); } }
						int[] arr = new int[l.size()];
						int i = 0;
						for (int d : l) { arr[i] = d; i++; }
						nbt.setIntArray(scopeEntry.getKey(), arr);
					}
					else if (vs[0] instanceof Long) {
						List<Long> l = Lists.<Long>newArrayList();
						for (Object va : vs) { if (va instanceof Long) { l.add((Long) va); } }
						long[] arr = new long[l.size()];
						int i = 0;
						for (long d : l) { arr[i] = d; i++; }
						nbt.setTag(scopeEntry.getKey(), new NBTTagLongArray(arr));
					}
					else if (vs[0] instanceof Short || vs[0] instanceof Float || vs[0] instanceof Double) {
						NBTTagList list = new NBTTagList();
						for (Object va : vs) {
							double d;
							if (va instanceof Short) { d = (double) (Short) va; }
							else if (va instanceof Float) { d = (double) (Float) va; }
							else if (va instanceof Double) { d = (Double) va; }
							else { continue; }
							list.appendTag(new NBTTagDouble(d));
						}
						nbt.setTag(scopeEntry.getKey(), list);
					}
				}
				else if (v instanceof Byte) { nbt.setByte(scopeEntry.getKey(), (Byte) v); }
				else if (v instanceof Short) { nbt.setShort(scopeEntry.getKey(), (Short) v); }
				else if (v instanceof Integer) { nbt.setInteger(scopeEntry.getKey(), (Integer) v); }
				else if (v instanceof Long) { nbt.setLong(scopeEntry.getKey(), (Long) v); }
				else if (v instanceof Float) { nbt.setFloat(scopeEntry.getKey(), (Float) v); }
				else if (v instanceof Double) { nbt.setDouble(scopeEntry.getKey(), (Double) v); }
				else if (v instanceof String) { nbt.setString(scopeEntry.getKey(), (String) v); }
				else {
					NBTBase n = this.writeObjectToNbt(v);
					if (n != null) {
						nbt.setTag(scopeEntry.getKey(), n);
					}
				}
			}
			return nbt;
		}
		return null;
	}

	@Override
	public Object readObjectFromNbt(NBTBase tag) {
		if (tag instanceof NBTTagByte) { return ((NBTTagByte) tag).getByte(); }
		else if (tag instanceof NBTTagShort) { return ((NBTTagShort) tag).getShort(); }
		else if (tag instanceof NBTTagInt) { return ((NBTTagInt) tag).getInt(); }
		else if (tag instanceof NBTTagLong) { return ((NBTTagLong) tag).getLong(); }
		else if (tag instanceof NBTTagFloat) { return ((NBTTagFloat) tag).getFloat(); }
		else if (tag instanceof NBTTagDouble) { return ((NBTTagDouble) tag).getDouble(); }
		else if (tag instanceof NBTTagString) { return ((NBTTagString) tag).getString(); }
		else if (tag instanceof NBTTagByteArray) { return ((NBTTagByteArray) tag).getByteArray(); }
		else if (tag instanceof NBTTagIntArray) { return ((NBTTagIntArray) tag).getIntArray(); }
		else if (tag instanceof NBTTagLongArray) { return ObfuscationHelper.getValue(NBTTagLongArray.class, (NBTTagLongArray) tag, 0); }
		else if (tag instanceof NBTTagCompound && ((NBTTagCompound) tag).hasKey("IsArray", 1)) {
			boolean isArray = ((NBTTagCompound) tag).getBoolean("IsArray");
			ScriptEngine engine = ScriptController.Instance.getEngineByName("ECMAScript");
			if (engine==null) { return null; }
			try {
				String str = "JSON.parse('"+(isArray ? "[" : "{");
				Set<String> sets = ((NBTTagCompound) tag).getKeySet();
				Map<String, Object> map = Maps.<String, Object>newTreeMap();
				for (String k : sets) {
					if (k.equals("IsArray")) { continue; }
					Object v = this.readObjectFromNbt(((NBTTagCompound) tag).getTag(k));
					if (v!=null) { map.put(k, v); }
				}
				for (String k : map.keySet()) {
					String s = this.getJSONStringFromObject(map.get(k));
					if (isArray) { str += s+", "; }
					else { str += "\""+k+"\":"+s+", "; }
				}
				if (!map.isEmpty()) { str = str.substring(0, str.length()-2); }
				str += (isArray ? "]" : "}")+"')";
				return engine.eval(str);
			}
			catch (Exception e) {}
		}
		else if (tag instanceof NBTTagList) {
			Object[] arr = new Object[((NBTTagList) tag).tagCount()];
			int i = 0;
			for (NBTBase listTag : (NBTTagList) tag) {
				arr[i] = this.readObjectFromNbt(listTag);
				i++;
			}
			return arr;
		}
		return null;
	}
	
	@Override
	public String getJSONStringFromObject(Object obj) {
		String str = "";
		if (obj.getClass().isArray()) {
			str = "[";
			for (Object value : (Object[]) obj) {
				String s = this.getJSONStringFromObject(value);
				if (!str.isEmpty()) { str += ", "; }
				str += s;
			}
			str += "]";
		}
		else if (obj instanceof Number) { str = obj.toString(); }
		else if (obj instanceof String) { str = "'"+obj.toString()+"'"; }
		else if (obj instanceof Bindings) {
			ScriptEngine engine = ScriptController.Instance.getEngineByName("ECMAScript");
			if (engine!=null) {
				engine.put("temp", obj);
				try {
					str = (String) engine.eval("JSON.stringify(temp)");
				}
				catch (ScriptException e) {}
			}
		}
		return str;
	}

	/*@Override
	public File getFile(String resourceLocation) {
		return AdditionalMethods.getFile(new ResourceLocation(resourceLocation));
	}

	public static File getFile(ResourceLocation resourceLocation) {
		File file = null;
		SimpleReloadableResourceManager simplemanager = (SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
		Map<String, FallbackResourceManager> map = ObfuscationHelper.getValue(SimpleReloadableResourceManager.class, simplemanager, 2);
		for (String name : map.keySet()) {
			FallbackResourceManager manager = map.get(name);
			List<IResourcePack> list = ObfuscationHelper.getValue(FallbackResourceManager.class, manager, 1);
			for (IResourcePack pack : list) {
				if (pack instanceof DefaultResourcePack) {
					ResourceIndex resourceIndex = ObfuscationHelper.getValue(DefaultResourcePack.class, (DefaultResourcePack) pack, ResourceIndex.class);
					@SuppressWarnings("unchecked")
					Map<String, File> resourceMap = ObfuscationHelper.getValue(ResourceIndex.class, resourceIndex, Map.class);
					if (resourceMap!=null) {
						for (File f : resourceMap.values()) {
							//this.addFile(f.getAbsolutePath(), f.length());
						}
					}
					continue;
				}
				if (pack instanceof AbstractResourcePack) {
					AbstractResourcePack p = (AbstractResourcePack) pack;
					File directory = ObfuscationHelper.getValue(AbstractResourcePack.class, p, 1);
					if (directory == null || !directory.isDirectory()) { continue; }
					File dir = new File(directory, "assets");
					if (dir == null || !dir.exists() || !dir.isDirectory()) { continue; }
					AdditionalMethods.checkFolder(dir, resourceLocation);
				}
			}
		}
		for (ModContainer mod : Loader.instance().getModList()) {
			if (mod.getSource().exists()) {
				//AdditionalMethods.progressFile(mod.getSource());
			}
		}
		ResourcePackRepository repos = Minecraft.getMinecraft().getResourcePackRepository();
		List<ResourcePackRepository.Entry> list2 = (List<ResourcePackRepository.Entry>) repos.getRepositoryEntries();
		for (ResourcePackRepository.Entry entry : list2) {
			File f = new File(repos.getDirResourcepacks(), entry.getResourcePackName());
			if (f.exists()) {
				//AdditionalMethods.progressFile(file);
			}
		}
		AdditionalMethods.checkFolder(new File(CustomNpcs.Dir, "assets"), resourceLocation);
		return null;
	}

	private static void checkFolder(File dir, ResourceLocation resourceLocation) {
		// TODO Auto-generated method stub
		
	}*/
	
}
